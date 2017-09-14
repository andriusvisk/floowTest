package com.web;

import com.DbUtils;
import com.Utilities;
import com.charting.BarColor;
import com.entit.Runner;
import com.entit.WordsStatistics;
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

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
        String mongoHost = System.getProperty("mongoHost");
        String mongoPort = System.getProperty("mongoPort");
        String mongoDb = System.getProperty("mongoDatabase");
        String myUUID = System.getProperty("myUUID");

        DbUtils dbUtils = new DbUtils(mongoHost, Integer.parseInt(mongoPort), mongoDb);

        List<Runner> listActiveRunners = dbUtils.findAll(Runner.class);

        model.addObject("listActiveRunners", listActiveRunners);
        model.addObject("mongoServerLocalTime", dbUtils.getMongoDbLocalTimeInMs());
        model.addObject("myUUID", myUUID);
        model.addObject("webUtils", webUtils);


        int wordsCount = 2;
        int chartHeight = wordsCount * 19 + 150;
        String[] charColors = new BarColor().getRandomColorsForChart(wordsCount);
        model.addObject("chartHeight", chartHeight);
        String mostWordsList = "\"word1\",\"word2\"";
        model.addObject("mostWords", mostWordsList);
        model.addObject("mostWordCounts", "101,102");
        model.addObject("backgroundColor", charColors[0]);
        model.addObject("borderColor", charColors[1]);


        return model;
    }

    private void prepareResponse(ModelAndView model, String requestedWord, DbUtils dbUtils) {

        WordsStatistics mainStat = dbUtils.findOne(WordsStatistics.class);

        if (requestedWord != null) {
            Long requestedWordCounter = mainStat.getCounts().get(requestedWord.toLowerCase());
            if (requestedWordCounter == null) requestedWordCounter = 0L;

            model.addObject("requestedWord", requestedWord);
            model.addObject("requestedWordCounter", requestedWordCounter);
        }

        if (mainStat.getCounts().size() > 0) {
            Map<String, Long> sorted = mainStat.getCounts().entrySet().stream()
                    .sorted(Map.Entry.comparingByValue(Comparator.naturalOrder()))
                    .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue(),
                            (oldValue, newValue) -> oldValue, LinkedHashMap::new));
            Map.Entry<String, Long>[] wk = (Map.Entry<String, Long>[]) sorted.entrySet().toArray();

            int maxResults = 100;

            if(wk.length > maxResults){

            }else{
                // just one report
            }
        }


    }

}
