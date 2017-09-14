package com.web;

import com.DbUtils;
import freemarker.template.TemplateMethodModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
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

        dbUtils = new DbUtils(mongoHost, Integer.parseInt(mongoPort), mongoDb);
    }

    public String formatDateAndTime(Long timeInMs) {
        LocalDateTime dateTime =
                Instant.ofEpochMilli(timeInMs).atZone(ZoneId.systemDefault()).toLocalDateTime();
        return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(dateTime);
    }

    public DbUtils getDbUtils() {
        return dbUtils;
    }
}
