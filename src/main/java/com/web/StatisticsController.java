package com.web;

import com.DbUtils;
import com.Utilities;
import com.entit.Runner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

/**
 * Created by andrius on 09/09/2017.
 */
@Controller
public class StatisticsController {

    @Autowired
    WebUtils webUtils;

    @RequestMapping("/")
    public ModelAndView home() {
        ModelAndView model = new ModelAndView("statistics");
        String mongoHost = System.getProperty("mongoHost");
        String mongoPort = System.getProperty("mongoPort");
        String mongoDb = System.getProperty("mongoDatabase");
        String myUUID = System.getProperty("myUUID");

        DbUtils dbUtils = new DbUtils(mongoHost, Integer.parseInt(mongoPort), mongoDb);

        List<Runner> listActiveRunners = dbUtils.findAll(Runner.class);

        model.addObject("listActiveRunners", listActiveRunners);
        model.addObject("mongoServerLocalTime", dbUtils.getMongoDbLocalTimeInMs());
        model.addObject("myUUID", myUUID);
        model.addObject("webUtils", webUtils);
        return model;
    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(StatisticsController.class, args);
    }
}
