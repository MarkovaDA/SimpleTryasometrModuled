package su.vistar.tryasometr.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import su.vistar.tryasometr.mapper.SensorDataMapper;
import su.vistar.tryasometr.model.Section;
import su.vistar.tryasometr.model.objectmanager.GeoObjectCollection;
import su.vistar.tryasometr.service.PathService;

@Controller
public class MainController {

    @Autowired
    private SensorDataMapper sensorMapper;
    
    @Autowired
    private PathService pathService;
    
    @GetMapping(value = "/object_manager")
    public ModelAndView objectmanagerPage(ModelMap map) {
        ModelAndView model = new ModelAndView("objectmanager");
        return model;
    }
    
    @GetMapping(value = "/loading_object_manager")
    public ModelAndView loadingobjectmanagerPage(ModelMap map) {
        ModelAndView model = new ModelAndView("loadingobjectmanager");
        return model;
    }

    @GetMapping(value = "/object_manager/get_features")
    @ResponseBody
    public GeoObjectCollection getFeatures() {
       //вот здесь подставить другой запрос
       return getCollection(sensorMapper.selectAllSections());
    }
    
    @GetMapping(value = "/test_azimuth")
    @ResponseBody
    public void calculateLength(){
        List<Section> sections = sensorMapper.selectAllSections();
        sections.forEach(way -> {
           int value = pathService.evaluateAzimuth(way.getLat1(), way.getLat2(), way.getLon1(), way.getLon2());
           System.out.println(value);
           sensorMapper.calculateMyAzimuth(value, way.getSectionID());
        });
    }
    
    private GeoObjectCollection getCollection(List<Section> sections){
      return pathService.getCollection(sections);
    }   
}
