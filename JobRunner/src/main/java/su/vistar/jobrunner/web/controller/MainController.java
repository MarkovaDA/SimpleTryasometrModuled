package ru.alidi.horeca.jobrunner.web.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Main controller
 * @author Aleksandr Gorovoi<alexander.gorovoy@vistar.su>
 */
@Controller
@RequestMapping("/")
public class MainController {

    private Logger log = LoggerFactory.getLogger(this.getClass());
    
    /**
     * View resolve method
     * @return 
     */
    @RequestMapping("/*")
    public String main() {
        log.info("Index requested");
        return "index";
    }

}
