package dev.learn.rag;

import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class RagController {

    private final ChatClient chatClient;

    private final KnowledgeBase knowledgeBase;

    public RagController(ChatClient chatClient, KnowledgeBase knowledgeBase) {
        this.chatClient = chatClient;
        this.knowledgeBase = knowledgeBase;
    }

    @GetMapping("/")
    public Map<String, Object> home() {
        return Map.of(
                "module", "04-memory-rag",
                "chunks", this.knowledgeBase.chunkCount(),
                "hint", "先 POST /ingest，再问「课程代号是什么」。向量库存放在内存里，重启会清空。");
    }

    @PostMapping("/ingest")
    public Map<String, Object> ingest() {
        try {
            int count = this.knowledgeBase.ingest();
            return Map.of("status", "ok", "chunks", count);
        }
        catch (RuntimeException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "入库失败。嵌入接口需要可用的 API Key，纯对话网关通常没有 embeddings：" + ex.getMessage(), ex);
        }
    }

    @GetMapping("/ask")
    public Map<String, String> ask(@RequestParam String q,
            @RequestParam(defaultValue = "demo") String conversationId) {
        return answer(q, conversationId);
    }

    @PostMapping("/ask")
    public Map<String, String> askByBody(@RequestBody AskRequest request) {
        if (request.question() == null || request.question().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "question 不能为空");
        }
        String conversationId = request.conversationId() == null || request.conversationId().isBlank()
                ? "demo" : request.conversationId();
        return answer(request.question(), conversationId);
    }

    private Map<String, String> answer(String question, String conversationId) {
        if (this.knowledgeBase.chunkCount() == 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "知识库还是空的。请先 POST /ingest。");
        }
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

    public record AskRequest(String question, String conversationId) {
    }
}
