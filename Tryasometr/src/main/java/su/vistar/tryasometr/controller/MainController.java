package su.vistar.tryasometr.controller;

import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import su.vistar.tryasometr.mapper.SensorDataMapper;
import su.vistar.tryasometr.model.Section;
import su.vistar.tryasometr.model.objectmanager.GeoObjectCollection;
import su.vistar.tryasometr.service.PathApproximationService;

@Controller
public class MainController {

    @Autowired
    private SensorDataMapper sensorMapper;
    
    @Autowired
    private PathApproximationService pathService;
    
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
       return getCollection(sensorMapper.selectAllSections());
    }
    
    @GetMapping(value = "/loading_object_manager/bounds")
    @ResponseBody
    public String getResponseObject(@RequestParam("bbox")String bbox, 
            @RequestParam("callback")String callback){
        /*String[] mapBounds = bbox.split(","); //границы области видимости    
        GeoObjectCollection collection = new GeoObjectCollection();
        Feature feature = new Feature();
        feature.setId(1);
        Geometry geometry = new Geometry();
        geometry.setType(lineType); //"LineString"
        geometry.getCoordinates()
                .add(new Double[]{Double.parseDouble(mapBounds[0]), Double.parseDouble(mapBounds[1])});
        geometry.getCoordinates()
                .add(new Double[]{Double.parseDouble(mapBounds[2]), Double.parseDouble(mapBounds[3])});
        feature.setGeometry(geometry);
        feature.getOptions().put("strokeWidth", 5);
        feature.getOptions().put("strokeColor", "#ff0000");
        collection.getFeatures().add(feature); 
        Gson gson = new Gson();    
        StringBuilder builder = new StringBuilder();
        builder.append(callback)
                .append("(")
                .append(gson.toJson(collection))
                .append(")");
        return builder.toString();*/
        return null;
    }
    
    @GetMapping(value = "/test_azimuth")
    @ResponseBody
    public void calculateLength(){
        List<Section> sections = sensorMapper.selectAllSections();
        sections.forEach(way -> {
           int value = pathService.evaluateAzimuth(way.getLat1(), way.getLat2(), way.getLon1(), way.getLon2());
           System.out.println(value);
           sensorMapper.calculateMyAzimuth(value, way.getSeсtionID());
        });
    }
    
    private GeoObjectCollection getCollection(List<Section> sections){
      return pathService.getCollection(sections);
    }   
}
