package com;

import com.entit.Chunk;
import com.entit.Runner;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by andrius on 09/09/2017.
 */
public class CountService {

    final static Logger logger = Logger.getLogger(CountService.class);

    public void process(final Parameters parameters) throws InterruptedException, IOException {
        AliveService aliveService = new AliveService();
        DbUtils dbUtils = new DbUtils(parameters);
        Reading reading = createNewReadingState(parameters);

        try {
            while (!aliveService.isJobFinished(parameters)) {
                if (isIAmMaster(parameters, dbUtils)) {

                    /*remove from buffer completed jobs*/

                    List<Runner> listActiveRunners = dbUtils.findAll(Runner.class);

                    /* collect all chunks from dead runners*/

                    mastersChunkBufferSize

                    for (Runner runner : listActiveRunners) {
                        boolean itsMe = (parameters.getMyId().compareTo(runner.getRunnerUUID()) == 0) ? true : false;
                        int howManyChunksPerRunner = (itsMe) ? 5 : 10;
                        int counter = 0;
                        while (++counter <= howManyChunksPerRunner) {
                            BufferChunk bufferChunk = reading.readNextChunkForMaster(parameters, runner.getRunnerUUID());
                            submitForExecution(bufferChunk, dbUtils);
                        }

                    }

                    /*read lines, put in buffer*/

                    /*and make a bunch of chunks for them to run*/


                }
                doMySlaveWork(parameters, dbUtils, reading);

                TimeUnit.SECONDS.sleep(parameters.getReadingPauseInS());
            }
        } finally {
            LineIterator.closeQuietly(reading.getIt());
        }
    }

    private void doMySlaveWork(final Parameters parameters, DbUtils dbUtils, Reading reading) {
        List<Chunk> jobs = readMyJobsToDo(parameters, dbUtils);
        for (Chunk job : jobs) {
            BufferChunk bc = reading.readChunkForSlave(job, parameters);
            Map<String, Long> stat = countStatistics(bc.getText());
            bc.getChunk().setStatistics(stat);
            bc.getChunk().setCalculated(true);
            bc.getChunk().setId(job.getId());
            dbUtils.updateById(bc.getChunk());
        }
        int stop = 0;
        /*act as slave and do my jobs
        read chunks for me in ascending order
        process them -

                at first look at bufer for text,if not find -read from file*/
    }

    private void submitForExecution(BufferChunk bufferChunk, DbUtils dbUtils) {
        dbUtils.insertOne(bufferChunk.getChunk());
    }

    private List<Chunk> readMyJobsToDo(final Parameters parameters, DbUtils dbUtils) {
        List<Chunk> jobs = dbUtils.find(Chunk.class, "runnerUUID", parameters.getMyId());
        jobs.sort((c1, c2) -> c1.getFromLineNbr().compareTo(c2.getFromLineNbr()));
        return jobs;
    }

    private Reading createNewReadingState(final Parameters parameters) throws IOException {
        return new Reading(FileUtils.lineIterator(new File(parameters.getSourceFileStr()), "UTF-8"));
    }

    private Map<String, Long> countStatistics(String str) {
        List<String> wordList = Arrays.asList(str.split("\\P{L}+"));
        return wordList.stream().collect(Collectors.groupingBy(e -> e, Collectors.counting()));
    }

    private Map<String, Long> mergeStatistics(Map<String, Long> statL, Map<String, Long> statR) {
        statL.forEach((k, v) -> statR.merge(k, v, Long::sum));
        return statR;
    }

    private boolean isIAmMaster(final Parameters parameters, DbUtils dbUtils) {
        List<Runner> listActiveRunners = dbUtils.findAll(Runner.class);
        if (listActiveRunners.size() > 0) {
            listActiveRunners.sort((r1, r2) -> r1.getStartTimeInMs().compareTo(r2.getStartTimeInMs()));
            if (listActiveRunners.get(0).getRunnerUUID().compareTo(parameters.getMyId()) == 0) { // first started is master
                return true;
            } else {
                return false;
            }
        }
        return true;
    }

}
