package su.vistar.tryasometr.controller;


import su.vistar.tryasometr.mapper.SensorDataMapper;
import su.vistar.tryasometr.model.Acceleration;
import su.vistar.tryasometr.model.Location;
import su.vistar.tryasometr.model.ResponseEntity;
import java.util.List;
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
import su.vistar.tryasometr.model.Section;


@Controller
public class ApiController {

    @Autowired
    private SensorDataMapper sensorMapper;
    //http://localhost:8080/tryasometr/

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

    @GetMapping(value="/get_sections")
    @ResponseBody
    public List<Section> getSections(){
        return sensorMapper.selectAllSections();
    }
}
