package com.web;

import com.DbUtils;
import freemarker.template.TemplateMethodModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Created by andrius on 14/09/2017.
 */
@Service
public class WebUtils {

    DbUtils dbUtils;

    @PostConstruct
    private void post(){
        String mongoHost = System.getProperty("mongoHost");
        String mongoPort = System.getProperty("mongoPort");
        String mongoDb = System.getProperty("mongoDatabase");
        String mongoUsername = System.getProperty("mongoUsername");
        String mongoPassowrd = System.getProperty("mongoPassword"); // it's not good :)

        dbUtils = new DbUtils(mongoHost, Integer.parseInt(mongoPort), mongoDb, mongoUsername, mongoPassowrd);
    }

    public String formatDateAndTime(Long timeInMs) {
        LocalDateTime dateTime =
                Instant.ofEpochMilli(timeInMs).atZone(ZoneId.systemDefault()).toLocalDateTime();
        return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(dateTime);
    }

    public DbUtils getDbUtils() {
        return dbUtils;
    }

    @PreDestroy
    private void dispose(){
        dbUtils.closeConnections();
    }
}
