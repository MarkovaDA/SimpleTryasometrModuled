package su.vistar.tryasometr.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import su.vistar.commons.model.Path;
import su.vistar.tryasometr.model.objectmanager.GeoObjectCollection;
import su.vistar.tryasometr.service.GeoObjectService;
import su.vistar.tryasometr.service.RouteService;

@Controller
public class ApiController {

    @Autowired
    private GeoObjectService pathService;
    
    @Autowired
    private RouteService routeService;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public ModelAndView index(ModelMap map) {
        ModelAndView model = new ModelAndView("index");
        return model;
    }
   
    @PostMapping(value = "put_yandex_points")
    @ResponseBody
    public GeoObjectCollection anylizeWayByYandexPoints(@RequestBody List<Path> paths) {     
        return pathService.getSectionCollection(routeService.approimateRouteByBasePoints(paths));
    }

    @PostMapping(value = "draw_base_points")
    @ResponseBody
    public GeoObjectCollection drawBasePoints(@RequestBody List<Path> paths){
        return pathService.getBasePointsCollection(paths);
    }
    
    @PostMapping(value = "draw_rectangles")
    @ResponseBody
    public GeoObjectCollection drawWrapperRectangles(@RequestBody List<Path> paths) {    
        return pathService.getRectangleCollection(routeService.getWrappedRectangles(paths));
    }  
}
