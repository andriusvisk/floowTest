package com;

import com.google.gson.Gson;
import com.entit.WordsStatistics;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by andrius on 09/09/2017.
 */
public class CountService {

    final static Logger logger = Logger.getLogger(CountService.class);

    public void startReading(final Parameters parameters) throws InterruptedException {
        try {
            LineIterator it = FileUtils.lineIterator(new File(parameters.getSourceFileStr()), "UTF-8");
            try {
                Long lineFromNbr = 1L;
                String buffer = "";
                int counter = 0;
                Map<String, Long> totalCounts = new HashMap<>();
                while (it.hasNext()) {
                    buffer = buffer + " " + it.nextLine();
                    ++counter;
                    if (counter == parameters.chunkOfLinesSize) {
                        processChunk(totalCounts, buffer, lineFromNbr, lineFromNbr + counter, parameters);
                        counter = 0;
                        buffer = "";
                        lineFromNbr = lineFromNbr + counter;
                    }

                }
                if (buffer.length() > 0) {
                    processChunk(totalCounts, buffer, lineFromNbr, lineFromNbr + counter, parameters);
                }
            } finally {
                LineIterator.closeQuietly(it);
            }
        } catch (IOException e) {
            logger.error(e);
        }
    }

    private void processChunk(Map<String, Long> counts, String str, Long lineFromNbr, Long lineToNbr, Parameters parameters) {

        List<String> wordList = Arrays.asList(str.split("\\P{L}+"));

        wordList.stream().collect(Collectors.groupingBy(e -> e, Collectors.counting()))
                .forEach((k, v) -> counts.merge(k, v, Long::sum));

        WordsStatistics statistics = new WordsStatistics(parameters.getMyId(), lineFromNbr, lineToNbr, counts);

        new DbUtils(parameters).insertOne(statistics, parameters.getMongoChunksCol());

        Gson gson = new Gson();
        String jsonStr = gson.toJson(statistics);

        logger.info(jsonStr);

        new DbUtils(parameters).getMongoDbLocalTimeInMs();

    }
}
