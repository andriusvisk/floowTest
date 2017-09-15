package com.web;

import com.DbUtils;
import com.Utilities;
import com.charting.BarColor;
import com.entit.Runner;
import com.entit.WordsStatistics;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by andrius on 09/09/2017.
 */
@Controller
public class StatisticsController {

    @Autowired
    WebUtils webUtils;

    @RequestMapping("/")
    public ModelAndView home(WebRequest webRequest) {

        String parameterWord = webRequest.getParameter("word");

        ModelAndView model = new ModelAndView("statistics");

        String myUUID = System.getProperty("myUUID");

        List<Runner> listActiveRunners = webUtils.dbUtils.findAll(Runner.class);

        model.addObject("listActiveRunners", listActiveRunners);
        model.addObject("mongoServerLocalTime", webUtils.dbUtils.getMongoDbLocalTimeInMs());
        model.addObject("myUUID", myUUID);
        model.addObject("webUtils", webUtils);

        prepareResponse(model, parameterWord, webUtils.dbUtils);

        return model;
    }

    private void prepareResponse(ModelAndView model, String requestedWord, DbUtils dbUtils) {

        WordsStatistics mainStat = dbUtils.findOne(WordsStatistics.class);

        if ((requestedWord != null) && (StringUtils.isNotEmpty(requestedWord.trim()))) {
            Long requestedWordCounter = mainStat.getCounts().get(requestedWord.toLowerCase().trim());
            if (requestedWordCounter == null) requestedWordCounter = 0L;

            model.addObject("requestedWord", requestedWord);
            model.addObject("requestedWordCounter", requestedWordCounter);
        }

        if (mainStat.getCounts().size() > 0) {

            model.addObject("longestWords", getLongesWords(mainStat));
            model.addObject("totalWords", getTotalWordsCount(mainStat));

            Map<String, Long> sorted = mainStat.getCounts().entrySet().stream().parallel()
                    .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                    .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue(),
                            (oldValue, newValue) -> oldValue, LinkedHashMap::new));
            Map.Entry<String, Long>[] wk = sorted.entrySet().toArray(new Map.Entry[sorted.keySet().size()]);

            int maxResults = 30;

            if (wk.length > maxResults) {
                model.addObject("twoReports", true);
                // most
                Map.Entry<String, Long>[] most = Arrays.copyOfRange(wk, 0, ((wk.length < maxResults) ? wk.length : maxResults) - 1);
                String[] charColorsM = new BarColor().getRandomColorsForChart(most.length);
                model.addObject("backgroundColorM", charColorsM[0]);
                model.addObject("borderColorM", charColorsM[1]);
                model.addObject("wordsM", prepareWordList(most));
                model.addObject("countsM", prepareCountsList(most));
                model.addObject("chartHeightM", most.length * 19 + 150);
                // least
                Map.Entry<String, Long>[] least = Arrays.copyOfRange(wk, ((wk.length < maxResults) ? 0 : wk.length - maxResults), wk.length - 1);
                ArrayUtils.reverse(least);
                String[] charColorsL = new BarColor().getRandomColorsForChart(least.length);
                model.addObject("backgroundColorL", charColorsL[0]);
                model.addObject("borderColorL", charColorsL[1]);
                model.addObject("wordsL", prepareWordList(least));
                model.addObject("countsL", prepareCountsList(least));
                model.addObject("chartHeightL", least.length * 19 + 150);

            } else {
                // just one report
                String[] charColorsM = new BarColor().getRandomColorsForChart(wk.length);
                model.addObject("backgroundColorM", charColorsM[0]);
                model.addObject("borderColorM", charColorsM[1]);
                model.addObject("wordsM", prepareWordList(wk));
                model.addObject("countsM", prepareCountsList(wk));
                int height = wk.length * 19 + 150;
                model.addObject("chartHeightM", height);
            }
        }


    }

    private List<String> getLongesWords(WordsStatistics mainStat) {
        String tmpStrLongest = mainStat.getCounts().keySet().stream().parallel()
                .max(Comparator.comparingInt(String::length)).orElse("");
        if (tmpStrLongest.length() > 0) {
            List<String> longestWords = mainStat.getCounts().keySet().stream().parallel().filter(e -> e.length() == (tmpStrLongest.length())).collect(Collectors.toList());
            longestWords.sort(Comparator.naturalOrder());
            return longestWords;
        }
        return null;
    }

    private Long getTotalWordsCount(WordsStatistics mainStat) {
        if (mainStat.getCounts().keySet().size() > 0) {
            return mainStat.getCounts().values().stream().parallel().collect(Collectors.summingLong(Long::longValue));
        }
        return null;
    }

    private String prepareWordList(Map.Entry<String, Long>[] arrMe) {
        String colect = "";
        for (Map.Entry<String, Long> entry : arrMe) {
            if (colect.length() > 0) {
                colect = colect + ", ";
            }
            colect = colect + "\"" + (entry.getKey()).replaceAll("\"", "'") + "\"";
        }
        return colect;
    }

    private String prepareCountsList(Map.Entry<String, Long>[] arrMe) {
        String colect = "";
        for (Map.Entry<String, Long> entry : arrMe) {
            if (colect.length() > 0) {
                colect = colect + ", ";
            }
            colect = colect + entry.getValue().toString();
        }
        return colect;
    }

}
