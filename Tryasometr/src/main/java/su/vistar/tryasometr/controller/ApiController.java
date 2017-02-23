package su.vistar.tryasometr.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import su.vistar.commons.model.Acceleration;
import su.vistar.commons.model.Location;
import su.vistar.commons.model.ResponseEntity;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import su.vistar.commons.model.BasePointData;
import su.vistar.commons.model.Path;
import su.vistar.commons.model.Rectangle;
import su.vistar.commons.model.Section;
import su.vistar.commons.model.Segment;
import su.vistar.tryasometr.model.objectmanager.GeoObjectCollection;
import su.vistar.tryasometr.service.PathService;
import su.vistar.commons.db.TryasometrAndroidMapper;

@Controller
public class ApiController {

    @Autowired
    private TryasometrAndroidMapper tryasometrMapper; 

    @Autowired
    private PathService pathService;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public ModelAndView index(ModelMap map) {
        ModelAndView model = new ModelAndView("index");
        return model;
    }

    @PostMapping(value = "/save_location/")
    @ResponseBody
    public ResponseEntity saveLocations(@RequestBody List<Location> list) {
        tryasometrMapper.insertListOfLocations(list);
        ResponseEntity entity = new ResponseEntity();
        entity.setStatus("OK");
        return entity;
    }

    @PostMapping(value = "/save_acceleration/")
    @ResponseBody
    public ResponseEntity saveAccelerations(@RequestBody List<Acceleration> list) {
        tryasometrMapper.insertListOfAcceleration(list);
        ResponseEntity entity = new ResponseEntity();
        entity.setStatus("OK");
        return entity;
    }
    
    @PostMapping(value = "put_yandex_points")
    @ResponseBody
    public GeoObjectCollection anylizeWayByYandexPoints(@RequestBody List<Path> approximatePaths) {
        Iterator<Path> pathIterator = approximatePaths.iterator();
        Iterator<Segment> segmentIterator;
        List<Double[]> allPoints = new ArrayList<>();
        Path currentPath;
        Segment nextSegment;
        List<Section> sections = new ArrayList<>();//итоговый набор найденных секций
        List<Section> searchedSectionsForPart = new ArrayList<>();//набор секций, найденных для одного сегмента
        List<Section> filteredSectionsForPart = new ArrayList<>();
        //собрали все точки, формирующие прямоугольник
        while (pathIterator.hasNext()) {
            currentPath = pathIterator.next();
            segmentIterator = currentPath.getSegments().iterator();
            while (segmentIterator.hasNext()) {
                nextSegment = segmentIterator.next();
                allPoints.addAll(nextSegment.getPoints());
            }
        }
        //сама оценка по прямоугольникам
        Double[] prevPoint;
        Double[] nextPoint = null;
        double distanceDiag;
        prevPoint = allPoints.get(0);
        int i = 0;
        System.out.println("кол-во точек:" + allPoints.size());
        while (i < allPoints.size() - 1) {
            int j = i + 1;
            do {
                if (j == allPoints.size()) break;               
                nextPoint = allPoints.get(j);
                distanceDiag = pathService.euclideanDistance(prevPoint, nextPoint);
                j++;
            } 
            while (distanceDiag < 0.002); //еще ограничение поставить        
            int azimuth = pathService.evaluateAzimuth(prevPoint[0], nextPoint[0], prevPoint[1], nextPoint[1]); //азимут отрезка
            System.out.println("азимут кусочка " + azimuth);
            searchedSectionsForPart = pathService.getSectionsIntoRectangle(prevPoint, nextPoint);
            System.out.println("число секций до фильтрации" + searchedSectionsForPart.size());     
            searchedSectionsForPart.forEach(item -> {
                System.out.println(item);
            });
            searchedSectionsForPart = searchedSectionsForPart.stream()
                    .filter(p -> Math.abs(p.getAzimuth1() - azimuth) <= 30
                            || Math.abs(p.getAzimuth2() - azimuth) <= 30
                            || Math.abs(p.getAzimuth3() - azimuth) <= 30
                    )
                    .collect(Collectors.toList());           
            sections.addAll(searchedSectionsForPart);
            System.out.println("число секций после фильтрации" + filteredSectionsForPart.size());
            filteredSectionsForPart.forEach(item -> {
                System.out.println(item);
            });
            prevPoint = nextPoint;
            i = j;
        }
        return pathService.getSectionCollection(sections);
    }

    @PostMapping(value = "draw_base_points")
    @ResponseBody
    public GeoObjectCollection drawBasePoints(@RequestBody List<Path> approximatePaths){
        return pathService.getBasePointsCollection(approximatePaths);
    }
    
    @PostMapping(value = "draw_rectangles")
    @ResponseBody
    public GeoObjectCollection drawWrapperRectangles(@RequestBody List<Path> approximatePaths) {
        Iterator<Path> pathIterator = approximatePaths.iterator();
        List<Rectangle> rectangles = new ArrayList<>();
        Path currentPath;
        List<Double[]> allPoints = new ArrayList<>();
        Map<Integer, BasePointData> dataMap = new HashMap<>();
        Double[] nextPoint = null;
        Double[] prevPoint;
        while (pathIterator.hasNext()) {
            currentPath = pathIterator.next();
            Iterator<Segment> segmentIterator = currentPath.getSegments().iterator();
            Segment nextSegment;
            while (segmentIterator.hasNext()) {
                nextSegment = segmentIterator.next();
                allPoints.addAll(nextSegment.getPoints());
            }
        }
        
        double distanceDiag;
        prevPoint = allPoints.get(0);
        int i = 0;
        System.out.println("кол-во точек:" + allPoints.size());
        while (i < allPoints.size() - 1) {
            int j = i + 1;
            do {
                if (j == allPoints.size()) break;               
                nextPoint = allPoints.get(j);
                distanceDiag = pathService.euclideanDistance(prevPoint, nextPoint);
                j++;
            } 
            while (distanceDiag < 0.002); //еще ограничение поставить        
            rectangles.add(pathService.getRectangleByPoints(prevPoint, nextPoint));
            prevPoint = nextPoint;
            System.out.println("i=" + i + ",j="+j);
            i = j;
        }
        return pathService.getRectangleCollection(rectangles);
    }  
}
