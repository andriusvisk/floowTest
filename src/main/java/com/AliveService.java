package com;

import com.entit.Runner;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by andrius on 09/09/2017.
 */
public class AliveService {

    final static Logger logger = Logger.getLogger(AliveService.class);

    AtomicBoolean isJobDone;

    public AliveService(AtomicBoolean isJobDone) {
        this.isJobDone = isJobDone;
    }

    public void sendPing(final Parameters parameters, DbUtils dbUtils) {

        List<Runner> meAsRunners = dbUtils.find(Runner.class, "runnerUUID", parameters.getMyId());

        if (meAsRunners.size() > 1) { // in case there are more than one record, leave just one
            List<Runner> tmpArr = new ArrayList<>();
            tmpArr.add(meAsRunners.get(0));
            int count = 0;
            for (Runner runner : meAsRunners) {
                if (count > 0) {
                    dbUtils.deleteById(runner);
                }
                ++count;
            }
            meAsRunners = tmpArr;
        }
        if (meAsRunners.size() == 1) { // update
            Long dbLocalTime = dbUtils.getMongoDbLocalTimeInMs();
            Runner meAsRunner = meAsRunners.get(0);
            meAsRunner.setPingTimeInMs(dbLocalTime);
            meAsRunner.setFreeMemPerc(MemoryService.getFreeMemoryPerc());
            dbUtils.updateById(meAsRunner);

        }
        if (meAsRunners.size() == 0) { // create new
            Long dbLocalTime = dbUtils.getMongoDbLocalTimeInMs();
            Runner runner = new Runner(parameters.getMyId(), dbLocalTime);
            runner.setFreeMemPerc(MemoryService.getFreeMemoryPerc());
            dbUtils.insertOne(runner);
        }
    }

    public void wipeNotActiveRunners(final Parameters parameters, DbUtils dbUtils) {
        List<Runner> list = dbUtils.findAll(Runner.class);
        Long dbTime = dbUtils.getMongoDbLocalTimeInMs();
        for (Runner runner : list) {
            Long runnerLifeTime = (dbTime - Long.valueOf(runner.getPingTimeInMs()));
            if (runnerLifeTime > (parameters.getRunnerTimeOutInS() * 1000)) {
                if (runner.getRunnerUUID().compareTo(parameters.getMyId()) != 0) { // not me
                    dbUtils.deleteById(runner);
                }
            }
        }
    }

}
