package com.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

/**
 * Created by andrius on 09/09/2017.
 */
@Controller
public class StatisticsController {

    @RequestMapping("/")
    public ModelAndView home() {
        ModelAndView model = new ModelAndView("statistics");
        model.addObject("name", "Andrius");
        return model;
    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(StatisticsController.class, args);
    }
}
