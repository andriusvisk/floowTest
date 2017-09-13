package com;

import com.entit.Chunk;
import com.entit.Runner;
import com.entit.WordsStatistics;
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
                    doMyMasteWork(parameters, dbUtils, reading);
                }
                doMySlaveWork(parameters, dbUtils, reading);

                TimeUnit.SECONDS.sleep(parameters.getReadingPauseInS());
            }
        } finally {
            LineIterator.closeQuietly(reading.getIt());
        }
    }

    private void doMyMasteWork(final Parameters parameters, DbUtils dbUtils, Reading reading) {

        boolean freshStart = false;
        WordsStatistics mainStat = dbUtils.findOne(WordsStatistics.class);

        if (mainStat == null) {
            freshStart = true;
            mainStat = new WordsStatistics();
        }

        List<Chunk> listCompletedChunks = dbUtils.find(Chunk.class, "calculated", true);
        for (Chunk chunk : listCompletedChunks) {
            //TODO check intervals, do i need to add to statistics and then change intervals

            mainStat.setCounts(mergeStatistics(mainStat.getCounts(), chunk.getStatistics()));

            //remove from buffer completed jobs
            reading.setBuffer(reading.getBuffer().stream().filter(p -> p.getChunk().getFromLineNbr() != chunk.getFromLineNbr())
                    .collect(Collectors.toList()));

            dbUtils.deleteById(chunk);
        }

        if (freshStart) {
            dbUtils.insertOne(mainStat);
        } else {
            dbUtils.updateById(mainStat);
        }

        //TODO mastersChunkBufferSize

        List<Runner> listActiveRunners = dbUtils.findAll(Runner.class);


        Set<String> activeRunnersUUID = listActiveRunners.stream().map(p -> p.getRunnerUUID()).collect(Collectors.toSet());

        // collect all chunks from dead runners
        List<Chunk> listIncompletedChunks = dbUtils.find(Chunk.class, "calculated", false);
        List<Chunk> chunksToBeResubmited = listIncompletedChunks.stream().filter(p -> !activeRunnersUUID.contains(p.getRunnerUUID())).collect(Collectors.toList());

        for (Runner runner : listActiveRunners) {
            boolean itsMe = (parameters.getMyId().compareTo(runner.getRunnerUUID()) == 0) ? true : false;
            int howManyChunksPerRunner = (itsMe) ? 5 : 10;
            int counter = 0;
            while (++counter <= howManyChunksPerRunner) {
                if (chunksToBeResubmited.size() > 0) {
                    Chunk chForResubm = chunksToBeResubmited.get(chunksToBeResubmited.size()-1);
                    chForResubm.setRunnerUUID(runner.getRunnerUUID());
                    dbUtils.updateById(chForResubm);
                    chunksToBeResubmited.remove(chunksToBeResubmited.size()-1);
                }else {
                    BufferChunk bufferChunk = reading.readNextChunkForMaster(parameters, runner.getRunnerUUID());
                    submitForExecution(bufferChunk, dbUtils);
                }
            }

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
    }

    private void submitForExecution(BufferChunk bufferChunk, DbUtils dbUtils) {
        dbUtils.insertOne(bufferChunk.getChunk());
    }

    private void submitForExecution(Chunk chunk, DbUtils dbUtils) {
        dbUtils.insertOne(chunk);
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
        return wordList.stream().filter(e -> e.length() > 0).collect(Collectors.groupingBy(e -> e, Collectors.counting()));
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
