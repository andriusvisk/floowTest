package com;

import com.entit.Chunk;
import com.entit.Global;
import com.entit.Runner;
import com.google.gson.Gson;
import com.entit.WordsStatistics;
import com.sun.org.apache.regexp.internal.RE;
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
        ReadingState readingState = createNewReadingState(parameters);
        DbUtils dbUtils = new DbUtils(parameters);

        try {
            while (!aliveService.isJobFinished(parameters)) {
                if (isIAmMaster(parameters)) {
                    wipeNotActiveRunners(parameters, dbUtils);
                    checkGlobalExistsAndActive(dbUtils);

                    remove from buffer completed jobs
                    List<Runner> listActiveRunners = dbUtils.findAll(Runner.class);
                    get all chunks from dead runners
                    for (Runner runner : listActiveRunners) {
                        int howManyChunksPerRunner =
                                (parameters.getMyId().compareTo(runner.getRunnerUUID()) == 0) ? 5 : 10;

                    }
                    read lines, put in buffer lines
                    and make a bunch of chunks for them to run

                    //-------------------------

                    doMySlaveWork();

                } else {
                    doMySlaveWork();
                }

                TimeUnit.SECONDS.sleep(parameters.getReadingPauseInS());
            }
        } finally {
            LineIterator.closeQuietly(readingState.getIt());
        }
    }

    private void doMySlaveWork(ReadingState readingState){

        act as slave and do my jobs
        read chunks for me in ascending order
        process them -

        at first look at bufer for text, if not find - read from file
    }

    private void readToBufferChunk(final Parameters parameters, ReadingState readingState, String executorUUID) {
        String buffer = "";
        int counter = 0;
        for (int i = 0; i < parameters.chunkOfLinesSize; i++) {
            if (readingState.getIt().hasNext()) {
                ++counter;
                buffer = buffer + " " + readingState.getIt().nextLine();
            }
        }
        addToBuffer(readingState.getBuffer(), buffer, readingState.getLineFromNbr(), readingState.getLineFromNbr() + counter, executorUUID);
        readingState.setLineFromNbr(readingState.getLineFromNbr() + counter);
    }

    private void addToBuffer(Map<Long, BufferChunk> buffer, String text, Long lineFromNbr, Long lineToNbr, String executorUUID) {
        Chunk chunk = new Chunk(lineFromNbr, lineToNbr, executorUUID);
        BufferChunk bufferChunk = new BufferChunk(text, chunk);
        buffer.put(lineFromNbr, bufferChunk);
    }

    private void submitForExecution(BufferChunk bufferChunk, DbUtils dbUtils) {
        dbUtils.insertOne(bufferChunk.getChunk());
    }

    private ReadingState createNewReadingState(final Parameters parameters) throws IOException {
        ReadingState newRs = new ReadingState();
        newRs.setIt(FileUtils.lineIterator(new File(parameters.getSourceFileStr()), "UTF-8"));
        newRs.setLineFromNbr(1L);
        newRs.setBuffer(new HashMap<>());
        newRs.setCounter(0);
        return newRs;
    }

    private Map<String, Long> countStatistics(String str) {
        List<String> wordList = Arrays.asList(str.split("\\P{L}+"));
        return wordList.stream().collect(Collectors.groupingBy(e -> e, Collectors.counting()));
    }

    private Map<String, Long> mergeStatistics(Map<String, Long> statL, Map<String, Long> statR) {
        statL.forEach((k, v) -> statR.merge(k, v, Long::sum));
        return statR;
    }

    private boolean isIAmMaster(final Parameters parameters) {
        return true;
    }

    private void wipeNotActiveRunners(final Parameters parameters, DbUtils dbUtils) {
        List<Runner> list = dbUtils.findAll(Runner.class);
        Long dbTime = dbUtils.getMongoDbLocalTimeInMs();
        for (Runner runner : list) {
            Long runnerLifeTime = (dbTime - Long.valueOf(runner.getPingTimeInMs()));
            if (runnerLifeTime > (parameters.getRunnerTimeOutInS() * 1000)) {
                dbUtils.deleteById(runner);
            }
        }
    }

    private void checkGlobalExistsAndActive(DbUtils dbUtils) {
        List<Global> list = dbUtils.findAll(Global.class);
        if (list.size() > 1) { // in case there are more than one record, leave just one
            List<Global> tmpArr = new ArrayList<>();
            tmpArr.add(list.get(0));
            int count = 0;
            for (Global global : list) {
                if (count > 0) {
                    dbUtils.deleteById(global);
                }
                ++count;
            }
            list = tmpArr;
        }
        if (list.size() == 1) { // update
            Global global = list.get(0);
            global.setJobIsFinished(false);
            dbUtils.updateById(global);
        }
        if (list.size() == 0) { // create new
            Global global = new Global();
            dbUtils.insertOne(global);
        }
    }
}
