package su.vistar.tryasometr.controller;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashSet;
import su.vistar.tryasometr.mapper.SensorDataMapper;
import su.vistar.tryasometr.model.Acceleration;
import su.vistar.tryasometr.model.Location;
import su.vistar.tryasometr.model.ResponseEntity;
import java.util.List;
import java.util.ListIterator;
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
import su.vistar.tryasometr.model.MapBounds;
import su.vistar.tryasometr.model.Path;
import su.vistar.tryasometr.model.Rectangle;
import su.vistar.tryasometr.model.Section;
import su.vistar.tryasometr.model.Segment;
import su.vistar.tryasometr.model.objectmanager.GeoObjectCollection;
import su.vistar.tryasometr.service.PathService;

@Controller
public class ApiController {

    @Autowired
    private SensorDataMapper sensorMapper; //сделать отдельный сервис для всех используемых операций

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
        sensorMapper.insertListOfLocations(list);
        ResponseEntity entity = new ResponseEntity();
        entity.setStatus("OK");
        return entity;
    }

    @PostMapping(value = "/save_acceleration/")
    @ResponseBody
    public ResponseEntity saveAccelerations(@RequestBody List<Acceleration> list) {
        sensorMapper.insertListOfAcceleration(list);
        ResponseEntity entity = new ResponseEntity();
        entity.setStatus("OK");
        return entity;
    }

    @PostMapping(value = "/bounds_change")
    @ResponseBody
    public List<Section> getSectionsOnBoundsChange(@RequestBody MapBounds mapBounds) {
        //сформировать ограничительный прямоугольник
        double minLat = Math.min(mapBounds.getBottomCorner()[0], mapBounds.getTopCorner()[0]);
        double maxLat = (minLat == mapBounds.getBottomCorner()[0])
                ? mapBounds.getTopCorner()[0]
                : mapBounds.getBottomCorner()[0];
        double minLon = Math.min(mapBounds.getBottomCorner()[1], mapBounds.getTopCorner()[1]);
        double maxLon = (minLon == mapBounds.getBottomCorner()[1])
                ? mapBounds.getTopCorner()[1]
                : mapBounds.getBottomCorner()[1];
        //получаем все секции внутри ограничивающего прямоугольника
        return sensorMapper.selectSectionsByBounds(minLat, minLon, maxLat, maxLon);
    }

    @GetMapping(value = "/get_sections")
    @ResponseBody
    public List<Section> getSections() {
        return sensorMapper.selectAllSections();
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
        List<Section> searchedSectionsForSegment = new ArrayList<>();//набор секций, найденных для одного сегмента
        //собрали все точки, формирующие прямоугольник
        while (pathIterator.hasNext()) {
            currentPath = pathIterator.next();
            segmentIterator = currentPath.getSegments().iterator();
            while (segmentIterator.hasNext()) {
                searchedSectionsForSegment.clear();
                nextSegment = segmentIterator.next();
                allPoints.addAll(nextSegment.getPoints());
            }
        }
        Double[] prevPoint;
        Double[] nextPoint;
        //переделать эту конструкцию,заведя два итератора
        for (int i = 0; i < allPoints.size() - 1; i++) {
            Double[] bottomPoint = new Double[2];
            Double[] topPoint = new Double[2];
            prevPoint = allPoints.get(i);
            nextPoint = allPoints.get(i + 1);
            bottomPoint[0] = Math.min(nextPoint[0], prevPoint[0]);
            bottomPoint[1] = Math.min(nextPoint[1], prevPoint[1]);
            topPoint[0] = Math.max(nextPoint[0], prevPoint[0]);
            topPoint[1] = Math.max(nextPoint[1], prevPoint[1]);
            //азимут именно для отрезка считается
            //(double lat1, double lat2, double lon1, double lon2)
            int azimuth = pathService.evaluateAzimuth(prevPoint[0], nextPoint[0], prevPoint[1], nextPoint[1]);
            System.out.println("азимут кусочка " + azimuth);
            //minLat, minLon, maxLat, maxLon
            searchedSectionsForSegment
                    = sensorMapper.selectSectionsByBounds(bottomPoint[0], bottomPoint[1],topPoint[0], topPoint[1]);
            System.out.println("число секций до фильтрации" + searchedSectionsForSegment.size());

            searchedSectionsForSegment = searchedSectionsForSegment.stream()
                    .filter(p -> Math.abs(p.getAzimuth1() - azimuth) < 2
                            || Math.abs(p.getAzimuth2() - azimuth) < 2
                            || Math.abs(p.getAzimuth3() - azimuth) < 2
                    )
                    .collect(Collectors.toList());
            System.out.println("число секций после фильтрации  " + searchedSectionsForSegment.size());
            searchedSectionsForSegment.forEach(item-> { 
                    System.out.println(item); 
            });
            sections.addAll(searchedSectionsForSegment);         
        }
        return pathService.getCollection(new ArrayList<>(new LinkedHashSet<>(sections)));
    }
    ///проработать алгоритм разбиения на прямоугольники.
    ///взаимопересекающиеся области с расстоянием не меньше заданного
    @PostMapping(value = "draw_rectangles")
    @ResponseBody
    public GeoObjectCollection drawWrapperRectangles(@RequestBody List<Path> approximatePaths) {
        Iterator<Path> pathIterator = approximatePaths.iterator();
        List<Rectangle> rectangles = new ArrayList<>();
        Path currentPath;
        List<Double[]> allPoints = new ArrayList<>();
        while (pathIterator.hasNext()) {
            currentPath = pathIterator.next();
            Iterator<Segment> segmentIterator = currentPath.getSegments().iterator();
            Segment nextSegment;
            while (segmentIterator.hasNext()) {
                nextSegment = segmentIterator.next();
                allPoints.addAll(nextSegment.getPoints());
            }
        }
        Double[] nextPoint;
        Double[] prevPoint;
        //переделать эту конструкцию,заведя два итератора
        for (int i = 0; i < allPoints.size() - 1; i++) {
            Double[] bottomPoint = new Double[2];
            Double[] topPoint = new Double[2];
            nextPoint = allPoints.get(i);
            prevPoint = allPoints.get(i + 1);
            bottomPoint[0] = Math.min(prevPoint[0], nextPoint[0]);
            bottomPoint[1] = Math.min(prevPoint[1], nextPoint[1]);
            topPoint[0] = Math.max(prevPoint[0], nextPoint[0]);
            topPoint[1] = Math.max(prevPoint[1], nextPoint[1]);
            Rectangle drawedRectangle = new Rectangle(bottomPoint, topPoint);
            rectangles.add(drawedRectangle);
        }
        return pathService.getRectangleCollection(rectangles);
    }
}
