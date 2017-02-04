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
import su.vistar.tryasometr.model.objectmanager.Feature;
import su.vistar.tryasometr.model.objectmanager.GeoObjectCollection;
import su.vistar.tryasometr.model.objectmanager.Geometry;

@Controller
public class MainController {

    @Autowired
    private SensorDataMapper sensorMapper;
    private final String lineType = "LineString";
    private final String circleType = "Circle";

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
        String[] mapBounds = bbox.split(","); //границы области видимости    
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
        return builder.toString();
    }
    
    private GeoObjectCollection getCollection(List<Section> sections){
        GeoObjectCollection collection = new GeoObjectCollection();
        Iterator<Section> iterator = sections.iterator();
        Feature feature;
        Geometry geometry;
        Section current;
        int counter = 0;
        Random rnd = new Random();
        String colorPattern = "rgba(%d,%d,%d,1)";
        String rgbaColor;
        while (iterator.hasNext()) {
            current = iterator.next();
            feature = new Feature();
            feature.setId(counter++);
            geometry = new Geometry();          
            geometry.setType(lineType);
            geometry.getCoordinates().add(new Double[]{current.getLat1(), current.getLon1()});
            geometry.getCoordinates().add(new Double[]{current.getLat2(), current.getLon2()});
            geometry.getCoordinates().add(new Double[]{current.getLat3(), current.getLon3()});
            geometry.getCoordinates().add(new Double[]{current.getLat4(), current.getLon4()});
            feature.setGeometry(geometry);
            feature.getProperties().put("sectionId", current.getSeсtionID());
            feature.getOptions().put("strokeWidth", 5);
            rgbaColor = String.format(colorPattern, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
            feature.getOptions().put("strokeColor", rgbaColor);
            collection.getFeatures().add(feature); 
            
            feature = new Feature();
            feature.setId(counter++);
            geometry = new Geometry();          
            geometry.setType(circleType);
            geometry.getCoordinates().add(new Double[]{current.getLat1(), current.getLon1()});
            feature.setGeometry(geometry);
            feature.getOptions().put("strokeWidth", 4);
            feature.getOptions().put("strokeColor", "#000000");
            feature.getOptions().put("fillColor", "#ffffff");
            collection.getFeatures().add(feature); 
        }
        return collection;
    }
        //технология для получения секций
        /*double minLat = Math.min(Double.parseDouble(mapBounds[0]), Double.parseDouble(mapBounds[2]));
        double maxLat = (minLat == Double.parseDouble(mapBounds[0]))
                ? Double.parseDouble(mapBounds[2]) 
                : Double.parseDouble(mapBounds[0]);
        
        double minLon = Math.min(Double.parseDouble(mapBounds[1]), Double.parseDouble(mapBounds[3]));
        double maxLon = (minLon ==Double.parseDouble(mapBounds[1]))
                ? Double.parseDouble(mapBounds[3])
                : Double.parseDouble(mapBounds[1]);
        //получаем все секции внутри ограничивающего прямоугольника
        GeoObjectCollection collection 
                = getCollection(sensorMapper.selectSectionsByMapBounds(minLat, minLon, maxLat, maxLon));    
        Gson gson = new Gson();    
        StringBuilder builder = new StringBuilder();
        builder.append(callback)
                .append("(")
                .append(gson.toJson(collection))
                .append(")");
        return gson.toJson(builder.toString());*/
        //попробую нарисовать палку от левого верхнего до нижнего углов
}
