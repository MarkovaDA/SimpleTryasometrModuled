package su.vistar.tryasometr.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import su.vistar.tryasometr.mapper.SensorDataMapper;
import su.vistar.tryasometr.model.Acceleration;
import su.vistar.tryasometr.model.Location;
import su.vistar.tryasometr.model.ResponseEntity;
import java.util.List;
import java.util.Map;
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
import su.vistar.tryasometr.model.Section;
import su.vistar.tryasometr.model.Segment;
import su.vistar.tryasometr.model.objectmanager.GeoObjectCollection;
import su.vistar.tryasometr.service.PathApproximationService;

@Controller
public class ApiController {

    @Autowired
    private SensorDataMapper sensorMapper; //сделать отдельный сервис для всех используемых операций
    
    @Autowired
    private PathApproximationService pathService;
    
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
        ResponseEntity entity = new ResponseEntity();
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
        return sensorMapper.selectSectionsByMapBounds(minLat, minLon, maxLat, maxLon);
    }

    @GetMapping(value = "/get_sections")
    @ResponseBody
    public List<Section> getSections() {
        return sensorMapper.selectAllSections();
    }

    @PostMapping(value="put_yandex_points")
    @ResponseBody
    public GeoObjectCollection anylizeWayByYandexPoints(@RequestBody List<Path> approximatePaths){
        Iterator<Path> pathIterator = approximatePaths.iterator();
        Iterator<Segment> segmentIterator;
        Path currentPath;
        Segment currentSegment;
        List<Section> sections = new ArrayList<>(); 
        //для каждого из путей
        while(pathIterator.hasNext()){
            currentPath = pathIterator.next();
            segmentIterator = currentPath.getSegments().iterator();
            //перебираем все сегменты в рамках пути
            while(segmentIterator.hasNext()){
                currentSegment = segmentIterator.next();
                //для каждой точки в рамках одного сегмента
                Map<Integer, Integer> mapSegments = new HashMap<>();
                currentSegment.getPoints().forEach(point -> {
                    Integer sectionID = pathService.getAppropriateSections(point);
                    System.out.println(sectionID);
                    if (sectionID != null){
                        if (!mapSegments.containsKey(sectionID))
                            mapSegments.put(sectionID, 1);
                        else 
                            mapSegments.replace(sectionID, mapSegments.get(sectionID) + 1);
                    }       
                });
                //выбираем наиболее часто повторяющуюся секцию при оценке принадлежности сегмента
                Integer findSectionID = 
                mapSegments.entrySet().stream()
                        .max((entry1, entry2) -> entry1.getValue() > entry2.getValue() ? 1 : -1).get().getKey();
                sections.add(sensorMapper.getSectionById(findSectionID));
            }
        }
        return pathService.getCollection(new ArrayList<>(new LinkedHashSet<>(sections)));
    }
   
}
