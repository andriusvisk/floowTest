package com;

import com.entit.Global;
import com.entit.Runner;
import com.entit.WordsStatistics;
import com.google.gson.Gson;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by andrius on 09/09/2017.
 */
public class AliveService {

    final static Logger logger = Logger.getLogger(AliveService.class);

    public void sendPing(final Parameters parameters) {
        DbUtils dbUtils = new DbUtils(parameters);

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
            meAsRunner.setPingTimeInMs(dbLocalTime.toString());
            dbUtils.updateById(meAsRunner);

        }
        if (meAsRunners.size() == 0) { // create new
            Long dbLocalTime = dbUtils.getMongoDbLocalTimeInMs();
            Runner runner = new Runner(parameters.getMyId(), dbLocalTime.toString());
            dbUtils.insertOne(runner);
        }
    }

    public boolean isJobFinished(final Parameters parameters){
        DbUtils dbUtils = new DbUtils(parameters);
        Global global = dbUtils.findOne(Global.class);
        if(global==null){
            return false; // might be even haven't started
        }else{
            return global.getJobIsFinished();
        }
    }

}
