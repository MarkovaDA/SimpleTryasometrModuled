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
        Path currentPath;
        Segment currentSegment;
        List<Section> sections = new ArrayList<>();//итоговый набор найденных секций
        List<Section> searchedSectionsForSegment = new ArrayList<>();//набор секций, найденных для одного сегмента
        while (pathIterator.hasNext()) {
            currentPath = pathIterator.next();
            segmentIterator = currentPath.getSegments().iterator();
            Double[] point1 = null, point2 = null;
            int azimuth = -1;
            while (segmentIterator.hasNext()) {
                searchedSectionsForSegment.clear();
                currentSegment = segmentIterator.next();
                if (currentSegment.getPoints().size() > 1) {
                    point1 = currentSegment.getPoints().get(0);
                    point2 = currentSegment.getPoints().get(currentSegment.getPoints().size() - 1);
                    azimuth = pathService.evaluateAzimuth(point1[0], point2[0], point1[1], point2[1]);
                }
                if (azimuth > -1) {
                    //выбор по геолокации
                    searchedSectionsForSegment
                    = sensorMapper.selectSectionsByBounds(Math.min(point1[0], point2[0]) - pathService.MAX_DISTANCE, Math.min(point1[1], point2[1]) - pathService.MAX_DISTANCE,
                    Math.max(point1[0], point2[0]) + pathService.MAX_DISTANCE, Math.max(point1[1], point2[1]) + pathService.MAX_DISTANCE);
                    System.out.println(searchedSectionsForSegment.size());
                    //фильтрация по зонам
                    /*int _azimuth = azimuth;
                    searchedSectionsForSegment = searchedSectionsForSegment.stream().filter(p -> Math.abs(p.getMy_azimuth() - _azimuth) < 2).collect(Collectors.toList());*/
                    
                    sections.addAll(searchedSectionsForSegment);
                }
            }
        }
        return pathService.getCollection(new ArrayList<>(new LinkedHashSet<>(sections)));
    }

}
