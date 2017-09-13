package com;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.ConfigurableApplicationContext;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Created by andrius on 09/09/2017.
 */
@SpringBootApplication
public class Application extends SpringBootServletInitializer {

    final static Logger logger = Logger.getLogger(Application.class);

    volatile boolean finishedJob = false;

    public static void main(String[] args) throws Exception {

        //TODO change
        boolean debug = true;

        String debugPar = "-m localhost:27017 -s /Users/andrius/Desktop/tmp/floodTestData/dump.xml";

        Parameters parameters = new Parameters((debug) ? debugPar.split(" ") : args);

        AliveService aliveService = new AliveService();
        // register as runner
        aliveService.sendPing(parameters);

        aliveService.checkGlobalExistsAndActive(new DbUtils(parameters));

        Runnable taskWorker = () -> {
            try {
                new CountService().process(parameters);
            } catch (InterruptedException | IOException e) {
                logger.error(e);
                System.exit(1);
            }
        };

        Runnable taskKeepMeAlive = () -> {
            try {
                AliveService aliveServiceT = new AliveService();
                DbUtils dbUtils = new DbUtils(parameters);
                while (!aliveServiceT.isJobFinished(parameters)) {
                    aliveServiceT.sendPing(parameters);
                    aliveServiceT.wipeNotActiveRunners(parameters, dbUtils);
                    TimeUnit.SECONDS.sleep(parameters.keepAlivePingTimeStepInS);
                }
            } catch (InterruptedException e) {
                logger.error(e);
                System.exit(1);
            }
        };

        new Thread(taskKeepMeAlive).start();
        new Thread(taskWorker).start();

        SpringApplication.run(Application.class, args);

    }

}
