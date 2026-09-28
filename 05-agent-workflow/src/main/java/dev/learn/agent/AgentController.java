package dev.learn.agent;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import dev.learn.agent.chat.AgentChatService;
import dev.learn.agent.route.RouteDecision;

@RestController
public class AgentController {

    private final AgentChatService agentChatService;

    public AgentController(AgentChatService agentChatService) {
        this.agentChatService = agentChatService;
    }

    @GetMapping("/")
    public Map<String, String> home() {
        return Map.of(
                "module", "05-agent-workflow",
                "hint", "GET /route 不调用模型。POST /chat 才会按分流去闲聊、查订单或查资料。");
    }

    @GetMapping("/route")
    public Map<String, Object> route(@RequestParam String q) {
        return preview(q);
    }

    @PostMapping("/chat")
    public Map<String, Object> chat(@RequestBody ChatRequest request) {
        if (request.question() == null || request.question().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "question 不能为空");
        }
        String conversationId = request.conversationId() == null || request.conversationId().isBlank()
                ? "demo" : request.conversationId();
        Map<String, Object> body = preview(request.question());
        try {
            body.put("conversationId", conversationId);
            body.put("answer", this.agentChatService.answer(request.question(), conversationId));
            return body;
        }
        catch (RuntimeException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "调用模型失败：" + ex.getMessage(), ex);
        }
    }

    private Map<String, Object> preview(String question) {
        RouteDecision decision = this.agentChatService.decide(question);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("question", question);
        body.put("route", decision.route().name());
        body.put("reason", decision.reason());
        body.put("snippets", this.agentChatService.snippets(question));
        return body;
    }

    public record ChatRequest(String question, String conversationId) {
    }
}
