package dev.learn.tools.tool;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Component
public class DateTimeTools {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");

    @Tool(description = "获取 Asia/Shanghai 时区的当前日期和时间。用户问现在几点、今天几号、星期几时必须调用。")
    public String currentDateTime() {
        return ZonedDateTime.now(ZoneId.of("Asia/Shanghai")).format(FORMATTER);
    }
}
