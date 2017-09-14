package com;

import com.entit.Chunk;
import com.entit.Runner;
import com.entit.WordsStatistics;
import com.entit.WordsStatisticsExt;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by andrius on 09/09/2017.
 */
public class CountService {

    final static org.slf4j.Logger logger = LoggerFactory.getLogger(Application.class);

    public void process(final Parameters parameters) throws InterruptedException, IOException {
        AliveService aliveService = new AliveService();
        DbUtils dbUtils = new DbUtils(parameters);
        Reading reading = createNewReadingState(parameters);

        try {
            while (!aliveService.isJobFinished(parameters, dbUtils)) {
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

            WordsStatisticsExt wordsStatExt = new WordsStatisticsExt(mainStat.getIntervals());
            if (!reading.alreadyInStatistics(chunk.getFromLineNbr(), wordsStatExt)) {

                mainStat.setCounts(mergeStatistics(mainStat.getCounts(), chunk.getStatistics()));
                mainStat = mergeStatisticsIntervals(chunk, mainStat, reading);
            }

            List newBuffer = reading.getBuffer().stream().filter(p -> p.getChunk().getFromLineNbr().compareTo(chunk.getFromLineNbr()) != 0)
                    .collect(Collectors.toList());
            reading.setBuffer(newBuffer);
        }

        if (freshStart) {
            dbUtils.insertOne(mainStat);
        } else {
            dbUtils.updateById(mainStat);
        }

        for (Chunk chunk : listCompletedChunks) {
            // remove from db, it's already in statistics updated in db
            dbUtils.deleteById(chunk);
        }

        List<Runner> listActiveRunners = dbUtils.findAll(Runner.class);
        List<Chunk> listAllChunks = dbUtils.findAll(Chunk.class);

        Set<String> activeRunnersUUID = listActiveRunners.stream().map(p -> p.getRunnerUUID()).collect(Collectors.toSet());

        List<Chunk> listChunksFromDeadRunners = listAllChunks.stream()
                .filter(p -> !activeRunnersUUID.contains(p.getRunnerUUID()))
                .collect(Collectors.toList());

        // in case of one of the worker crash collect and process all data left by that worker
        if (listChunksFromDeadRunners.size() > 0) {
            List<Chunk> listProcessingChunks = listAllChunks.stream()
                    .filter(p -> !Boolean.TRUE.equals(p.getCalculated())).collect(Collectors.toList());

            listChunksFromDeadRunners.sort((l, r) -> l.getFromLineNbr().compareTo(r.getFromLineNbr()));

            Map<String, Long> activeRunnersLastLine = listAllChunks.stream()
                    .filter(x -> activeRunnersUUID.contains(x.getRunnerUUID()))
                    .collect(Collectors.toMap(x -> x.getRunnerUUID(), x -> x.getToLineNbr(), Long::max));

            Map<String, List<Chunk>> chunksForResubmition = new HashMap<>();

            List<Chunk> chunksNeedsBackReading = new ArrayList<>();

            for (Chunk chunkForResubm : listChunksFromDeadRunners) {
//TODO pagauti cia paleidziant sleiva po intellij paleidimo
                if (activeRunnersLastLine.keySet().size() > 0) {
                    Map.Entry clMe = activeRunnersLastLine.entrySet().stream().
                            filter(p -> p.getValue() < chunkForResubm.getFromLineNbr())
                            .min((l, r) -> l.getValue().compareTo(r.getValue())).get();
                    String closestRunner = (clMe != null) ? (String) clMe.getKey() : null;

                    if (closestRunner != null) {
                        long closestRunnerAlreadyProcChnkCnt = listProcessingChunks.stream()
                                .filter(p -> p.getRunnerUUID().compareTo(closestRunner) == 0).count();

                        boolean itsMe = (parameters.getMyId().compareTo(closestRunner) == 0) ? true : false;
                        int howManyChunksPerRunner = (itsMe) ? 5 : 10;

                        List<Chunk> chunksForRunner = chunksForResubmition.get(closestRunner);

                        if (chunksForRunner == null)
                            chunksForRunner = new ArrayList<>();

                        if (chunksForRunner.size() + closestRunnerAlreadyProcChnkCnt < howManyChunksPerRunner) {
                            chunkForResubm.setRunnerUUID(closestRunner);
                            chunksForRunner.add(chunkForResubm);
                            chunksForResubmition.put(closestRunner, chunksForRunner);
                            activeRunnersLastLine.put(closestRunner, chunkForResubm.getToLineNbr());
                        }
                    } else {
                        chunksNeedsBackReading.add(chunkForResubm);
                    }
                } else {
                    chunksNeedsBackReading.add(chunkForResubm); // or master started again and working alone after crash
                }
            }

            if (chunksNeedsBackReading.size() > 0) {
                listActiveRunners.sort((r1, r2) -> r2.getStartTimeInMs().compareTo(r1.getStartTimeInMs())); //desc
                String lastJoinedRunner = listActiveRunners.get(0).getRunnerUUID();
                for (Chunk chunkForResubm : chunksNeedsBackReading) {
                    List<Chunk> chunksForRunner = chunksForResubmition.get(lastJoinedRunner);

                    if (chunksForRunner == null)
                        chunksForRunner = new ArrayList<>();
                    chunkForResubm.setRunnerUUID(lastJoinedRunner);
                    chunksForRunner.add(chunkForResubm);
                    chunksForResubmition.put(lastJoinedRunner, chunksForRunner);
                    activeRunnersLastLine.put(lastJoinedRunner, chunkForResubm.getToLineNbr());
                }
            }

            for (String runnerUUID : chunksForResubmition.keySet()) {
                List<Chunk> chunksForRunner = chunksForResubmition.get(runnerUUID);
                for (Chunk chunk : chunksForRunner) {
                    dbUtils.updateById(chunk);
                }

            }
        } else { // no crashes detected, process in regular manner
            listActiveRunners.sort((l, r) -> l.getStartTimeInMs().compareTo(r.getStartTimeInMs()));
            WordsStatisticsExt wordsStatisticsExt = new WordsStatisticsExt(mainStat.getIntervals());
            for (Runner runner : listActiveRunners) {
                boolean itsMe = (parameters.getMyId().compareTo(runner.getRunnerUUID()) == 0) ? true : false;
                int howManyChunksPerRunner = (itsMe) ? 5 : 10;
                int counter = 0;
                while (++counter <= howManyChunksPerRunner) {
                    BufferChunk bufferChunk = reading.readNextChunkForMaster(parameters, itsMe, runner.getRunnerUUID(), wordsStatisticsExt, listAllChunks);
                    if (bufferChunk != null) {
                        submitForExecution(bufferChunk, dbUtils);
                    } else {
                        //TODO readched end of file
                    }
                }

            }
        }

    }

    private void doMySlaveWork(final Parameters parameters, DbUtils dbUtils, Reading reading) {
        List<Chunk> jobs = readMyJobsToDo(parameters, dbUtils);
        for (Chunk job : jobs) {
            BufferChunk bc = reading.readChunkForSlave(job, parameters);
            if (bc != null) {
                Map<String, Long> stat = countStatistics(bc.getText());
                bc.getChunk().setStatistics(stat);
                bc.getChunk().setCalculated(true);
                bc.getChunk().setId(job.getId());
                dbUtils.updateById(bc.getChunk());
            } else {
                logger.error("Error read chunk");
            }
        }
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
        wordList = wordList.stream().map(f -> f.toLowerCase()).collect(Collectors.toList());
        return wordList.stream().filter(e -> e.length() > 0).collect(Collectors.groupingBy(e -> e, Collectors.counting()));
    }

    private Map<String, Long> mergeStatistics(Map<String, Long> statL, Map<String, Long> statR) {
        statL.forEach((k, v) -> statR.merge(k, v, Long::sum));
        return statR;
    }

    private WordsStatistics mergeStatisticsIntervals(Chunk chunk, WordsStatistics wordsStatistics, Reading reading) {
        wordsStatistics.setIntervals(reading.mergeIntervals(chunk, wordsStatistics.getIntervals()));
        return wordsStatistics;
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
