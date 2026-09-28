package dev.learn.tools;

import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import dev.learn.tools.tool.CalculatorTools;
import dev.learn.tools.tool.DateTimeTools;
import dev.learn.tools.tool.OrderTools;

@RestController
public class ToolsController {

    private final ChatClient chatClient;

    public ToolsController(ChatClient.Builder builder, DateTimeTools dateTimeTools, CalculatorTools calculatorTools,
            OrderTools orderTools) {
        this.chatClient = builder
                .defaultSystem("""
                        你是工具调用练习助教。用简体中文回答。
                        问时间就调用 currentDateTime，问计算就调用 calculate，问订单就调用 lookupOrder。
                        不要编造订单状态。
                        """)
                .defaultTools(dateTimeTools, calculatorTools, orderTools)
                .build();
    }

    @GetMapping("/")
    public Map<String, String> home() {
        return Map.of(
                "module", "03-tools",
                "hint", "试试：现在几点、12*7、订单 A1001 发货了吗。工具在 dev.learn.tools.tool 包。");
    }

    @GetMapping("/chat")
    public Map<String, String> chat(@RequestParam String q) {
        return answer(q);
    }

    @PostMapping("/chat")
    public Map<String, String> chatByBody(@RequestBody ChatRequest request) {
        if (request.question() == null || request.question().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "question 不能为空");
        }
        return answer(request.question());
    }

    private Map<String, String> answer(String question) {
        try {
            String content = this.chatClient.prompt().user(question).call().content();
            return Map.of("question", question, "answer", content == null ? "" : content);
        }
        catch (RuntimeException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "调用模型失败：" + ex.getMessage(), ex);
        }
    }

    public record ChatRequest(String question) {
    }
}
