package dev.learn.chat;

import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * 单轮问答：每次请求只带当前这句话，不保存历史。
 */
@RestController
public class ChatController {

    private final ChatClient chatClient;

    public ChatController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder
                .defaultSystem("你是 Spring AI 学习助教。用简体中文回答，尽量短，并点出相关类名。")
                .build();
    }

    @GetMapping("/")
    public Map<String, String> home() {
        return Map.of(
                "module", "01-chat",
                "hint", "POST /chat ，JSON 字段 question。这一轮没有记忆。");
    }

    @GetMapping("/chat")
    public Map<String, String> chatByQuery(@RequestParam String q) {
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
