package su.vistar.tryasometr.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import su.vistar.tryasometr.model.objectmanager.GeoObjectCollection;
import su.vistar.tryasometr.service.PathService;

@Controller
public class MainController {

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
       return pathService.getAllSectionsCollection();
    }
}
