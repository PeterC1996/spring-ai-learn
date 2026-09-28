package dev.learn.agent.tool;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Component
public class DateTimeTools {

    @Tool(description = "获取 Asia/Shanghai 的当前时间。查订单时如果用户同时问现在几点，可以调用。")
    public String currentDateTime() {
        return ZonedDateTime.now(ZoneId.of("Asia/Shanghai"))
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z"));
    }
}
