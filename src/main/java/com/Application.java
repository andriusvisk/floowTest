package com;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.support.SpringBootServletInitializer;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by andrius on 09/09/2017.
 */
@SpringBootApplication
public class Application extends SpringBootServletInitializer {

    final static Logger logger = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) throws Exception {

        boolean debug = false;

        String debugPar = "-m localhost:27017 -s /Users/andrius/Desktop/tmp/floodTestData/dump.xml -u user1 -p password1";

        Parameters parameters = new Parameters((debug) ? debugPar.split(" ") : args);

        AliveService aliveService = new AliveService(new AtomicBoolean(false));
        DbUtils dbUtilsT = new DbUtils(parameters);
        // register as a runner
        aliveService.sendPing(parameters, dbUtilsT);

        App app = new App(parameters);

        int portToUse = (debug) ? 8080 : new Utilities().getRandomPortToUse();
        logger.warn("Use link to track system - http://localhost:" + portToUse);
        System.getProperties().put("server.port", portToUse);
        SpringApplication.run(Application.class, args);

    }

}
