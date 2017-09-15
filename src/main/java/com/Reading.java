package com;

import com.entit.Chunk;
import com.entit.WordsStatisticsExt;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by andrius on 10/09/2017.
 */
public class Reading {

    final static Logger logger = LoggerFactory.getLogger(Reading.class);

    private LineIterator it;
    private String sourceFile;
    private Long currentLine;
    private boolean hasMoreLines;

    private List<BufferChunk> buffer;

    public Reading(String sourceFile) throws IOException {
        this.sourceFile = sourceFile;
        buffer = new ArrayList<>();
        init();
    }

    private void init() throws IOException {
        this.it = FileUtils.lineIterator(new File(sourceFile), "UTF-8");
        hasMoreLines = true;
        currentLine = 0L;
    }

    public void close() {
        LineIterator.closeQuietly(it);
    }

    private boolean reopen() {
        close();
        try {
            init();
            return true;
        } catch (IOException e) {

        }
        return false;
    }

    // master read
    public BufferChunk readNextChunkForMaster(final Parameters parameters, boolean masterReadForHimself, String executorUUID, WordsStatisticsExt stat, List<Chunk> listAllChunks) {
        if (alreadySubmitted(currentLine + 1, stat, listAllChunks)) {
            Long nextLineToRead = getNextLineToReadForMaster(currentLine, stat, listAllChunks);
            return readChunk(nextLineToRead, parameters, masterReadForHimself, executorUUID);
        } else {
            return readChunk(currentLine + 1, parameters, masterReadForHimself, executorUUID);
        }
    }

    // slave read
    public BufferChunk readChunkForSlave(Chunk chunk, final Parameters parameters) {
        return readChunk(chunk.getFromLineNbr(), parameters, false, chunk.getRunnerUUID());
    }

    private BufferChunk readChunk(Long lineFrom, final Parameters parameters, boolean masterReadForHimself, String executorUUID) {
        // slave has empty buffer
        BufferChunk bufferObj = buffer.stream().filter(x -> x.getChunk().getFromLineNbr().equals(lineFrom)).findFirst().orElse(null);
        if (bufferObj != null) {
            bufferObj.getChunk().setRunnerUUID(executorUUID); // reassign (in case)
            return bufferObj;
        } else {
            return readChunkFromFile(new Chunk(lineFrom, executorUUID), parameters, masterReadForHimself);
        }
    }

    private BufferChunk readChunkFromFile(Chunk chunk, final Parameters parameters, boolean putToBuffer) {

        if (currentLine >= chunk.getFromLineNbr()) {
            // need backreading, reopen file
            if (!reopen()) {
                logger.error("Could not reopen file for reading");
                return null;
            }
        }
        // forward up to required line
        while ((currentLine < chunk.getFromLineNbr() - 1) && (hasMoreLines)) {
            if (it.hasNext()) {
                it.nextLine();
                ++currentLine;
            } else {
                hasMoreLines = false;
            }
        }
        if (hasMoreLines) {
            String lineBuffer = "";
            Long startLine = new Long(currentLine) + 1;
            for (int i = 0; i < parameters.chunkOfLinesSize; i++) {
                if (it.hasNext()) {
                    ++currentLine;
                    lineBuffer = lineBuffer + " " + it.nextLine();
                }
            }
            chunk.setToLineNbr(currentLine);
            BufferChunk bufferChunk = new BufferChunk(lineBuffer, chunk);
            if (putToBuffer) {
                buffer.add(bufferChunk);
            }
            return bufferChunk;
        } else {
            return new BufferChunk(false);
        }
    }

    public Long getNextLineToReadForMaster(Long fromLine, WordsStatisticsExt statExt, List<Chunk> listAllChunks) {

        Map<Long, Long> intervals = statExt.getIntervals().entrySet().stream().filter(v -> v.getValue() > (fromLine + 1))
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));

        for (Chunk chunk : listAllChunks) {
            intervals = mergeIntervalsWithLong(chunk, intervals);
        }
        Map.Entry<Long, Long> foundFromLineEntry = intervals.entrySet().stream().filter(v -> v.getValue() > (fromLine + 1))
                .min((l, r) -> l.getValue().compareTo(r.getValue())).orElse(null);

        Long foundFromLine = -0L;

        if (foundFromLineEntry != null) {
            foundFromLine = foundFromLineEntry.getValue();
        }

        if (foundFromLine > 0) {
            return foundFromLine + 1;
        } else {
            return fromLine + 1;
        }
    }

    public Map<String, Long> mergeIntervals(Chunk chunk, Map<String, Long> initialInteval) {

        Map<String, Long> intervalForReturn = new HashMap<>(initialInteval);

        if (intervalForReturn.get(chunk.getFromLineNbr().toString()) == null) {
            intervalForReturn.put(chunk.getFromLineNbr().toString(), chunk.getToLineNbr());
        }
        WordsStatisticsExt wSE = new WordsStatisticsExt(intervalForReturn);

        boolean foundAppen = false;
        boolean checkAgain = true;
        while (checkAgain) {
            checkAgain = false;
            for (Long start : wSE.getIntervals().keySet()) {
                Long stop = wSE.getIntervals().get(start);
                // try to find appending interval
                Long newStop = wSE.getIntervals().get(stop + 1);
                if (newStop != null) {
                    checkAgain = true;
                    foundAppen = true;
                    wSE.getIntervals().put(start, new Long(newStop));
                    wSE.getIntervals().remove(stop + 1);
                    break;
                }
            }
        }
        if (foundAppen) {
            Map<String, Long> interNew = new HashMap<>();
            for (Long key : wSE.getIntervals().keySet()) {
                String keyS = new String(key.toString());
                interNew.put(keyS, new Long(wSE.getIntervals().get(key)));
            }
            return interNew;
        }
        return intervalForReturn;
    }

    public Map<Long, Long> mergeIntervalsWithLong(Chunk chunk, Map<Long, Long> initialInteval) {

        Map<Long, Long> intervalForReturn = new HashMap<>(initialInteval);

        if (intervalForReturn.get(chunk.getFromLineNbr()) == null) {
            intervalForReturn.put(chunk.getFromLineNbr(), chunk.getToLineNbr());
        }

        boolean foundAppen = false;
        boolean checkAgain = true;
        while (checkAgain) {
            checkAgain = false;
            for (Long start : intervalForReturn.keySet()) {
                Long stop = intervalForReturn.get(start);
                // try to find appending interval
                Long newStop = intervalForReturn.get(stop + 1);
                if (newStop != null) {
                    checkAgain = true;
                    foundAppen = true;
                    intervalForReturn.put(start, newStop);
                    intervalForReturn.remove(stop + 1);
                    break;
                }
            }
        }
        if (foundAppen) {
            Map<Long, Long> interNew = new HashMap<>();
            for (Long key : intervalForReturn.keySet()) {
                interNew.put(key, new Long(intervalForReturn.get(key)));
            }
            return interNew;
        }
        return intervalForReturn;
    }


    public boolean alreadySubmitted(Long chunkFromLine, WordsStatisticsExt stat, List<Chunk> listAllChunks) {
        boolean foundInRunCh = (listAllChunks.stream().parallel().filter(p -> chunkFromLine == p.getFromLineNbr()).findFirst().orElse(null) == null) ? false : true;
        if (!alreadyInStatistics(chunkFromLine, stat) && !foundInRunCh) {
            return false;
        }
        return true;
    }

    public boolean alreadyInStatistics(Long chunkFromLine, WordsStatisticsExt stat) {
        for (Long startStat : stat.getIntervals().keySet()) {
            Long statStop = stat.getIntervals().get(startStat);
            if ((startStat <= chunkFromLine) && (statStop >= chunkFromLine)) {
                return true;
            }
        }
        return false;
    }

    public LineIterator getIt() {
        return it;
    }

    public List<BufferChunk> getBuffer() {
        return buffer;
    }

    public void setBuffer(List<BufferChunk> buffer) {
        this.buffer = buffer;
    }

    public Long getCurrentLine() {
        return currentLine;
    }

    public boolean isHasMoreLines() {
        return hasMoreLines;
    }
}
