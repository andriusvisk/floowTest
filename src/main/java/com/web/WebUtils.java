package com.web;

import freemarker.template.TemplateMethodModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

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
    public String formatDateAndTime(Long timeInMs) {
        LocalDateTime dateTime =
                Instant.ofEpochMilli(timeInMs).atZone(ZoneId.systemDefault()).toLocalDateTime();
        return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(dateTime);
    }
}
