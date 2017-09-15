package com;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by andrius on 15/09/2017.
 */
public class App {

    final static Logger logger = LoggerFactory.getLogger(App.class);

    private Parameters parameters;
    private final AtomicBoolean isJobDone = new AtomicBoolean(false);

    public App(Parameters parameters) {
        this.parameters = parameters;
        isJobDone.set(false);
        init();
    }

    public void init() {

        Runnable taskWorker = () -> {
            try {
                new CountService(isJobDone).process(parameters);
            } catch (InterruptedException | IOException e) {
                logger.error(e.getMessage());
                System.exit(1);
            }
        };

        Runnable taskKeepMeAlive = () -> {
            try {
                AliveService aliveServiceT = new AliveService(isJobDone);
                DbUtils dbUtils = new DbUtils(parameters);
                while (!isJobDone.get()) {
                    aliveServiceT.sendPing(parameters, dbUtils);
                    aliveServiceT.wipeNotActiveRunners(parameters, dbUtils);
                    TimeUnit.SECONDS.sleep(parameters.keepAlivePingTimeStepInS);
                }
            } catch (InterruptedException e) {
                logger.error(e.getMessage());
                System.exit(1);
            }
        };

        new Thread(taskKeepMeAlive).start();
        new Thread(taskWorker).start();
    }
}
