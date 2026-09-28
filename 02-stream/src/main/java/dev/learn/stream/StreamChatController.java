package dev.learn.stream;

import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;

/**
 * 同一个 conversationId 会把最近若干条消息拼回提示词。
 * /chat 一次返回全文，/chat/stream 按片段流出。
 */
@RestController
public class StreamChatController {

    private final ChatClient chatClient;

    public StreamChatController(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @GetMapping("/")
    public Map<String, String> home() {
        return Map.of(
                "module", "02-stream",
                "hint", "POST /chat 与 GET /chat/stream 都要带 conversationId。换一个 id 就是新会话。");
    }

    @GetMapping("/chat")
    public Map<String, String> chat(@RequestParam String q,
            @RequestParam(defaultValue = "demo") String conversationId) {
        return answer(q, conversationId);
    }

    @PostMapping("/chat")
    public Map<String, String> chatByBody(@RequestBody ChatRequest request) {
        if (request.question() == null || request.question().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "question 不能为空");
        }
        String conversationId = request.conversationId() == null || request.conversationId().isBlank()
                ? "demo" : request.conversationId();
        return answer(request.question(), conversationId);
    }

    @GetMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> stream(@RequestParam String q,
            @RequestParam(defaultValue = "demo") String conversationId) {
        return this.chatClient.prompt()
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, conversationId))
                .user(q)
                .stream()
                .content();
    }

    private Map<String, String> answer(String question, String conversationId) {
        try {
            String content = this.chatClient.prompt()
                    .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, conversationId))
                    .user(question)
                    .call()
                    .content();
            return Map.of(
                    "conversationId", conversationId,
                    "question", question,
                    "answer", content == null ? "" : content);
        }
        catch (RuntimeException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "调用模型失败：" + ex.getMessage(), ex);
        }
    }

    public record ChatRequest(String question, String conversationId) {
    }
}
