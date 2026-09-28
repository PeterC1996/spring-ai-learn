package dev.learn.agent.chat;

import java.util.List;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;

import dev.learn.agent.knowledge.ClasspathKnowledge;
import dev.learn.agent.route.IntentRouter;
import dev.learn.agent.route.Route;
import dev.learn.agent.route.RouteDecision;
import dev.learn.agent.tool.DateTimeTools;
import dev.learn.agent.tool.OrderTools;

@Service
public class AgentChatService {

    private final ChatClient chatClient;

    private final IntentRouter router = new IntentRouter();

    private final ClasspathKnowledge knowledge;

    private final OrderTools orderTools;

    private final DateTimeTools dateTimeTools;

    public AgentChatService(ChatClient chatClient, ClasspathKnowledge knowledge, OrderTools orderTools,
            DateTimeTools dateTimeTools) {
        this.chatClient = chatClient;
        this.knowledge = knowledge;
        this.orderTools = orderTools;
        this.dateTimeTools = dateTimeTools;
    }

    public RouteDecision decide(String question) {
        return this.router.decide(question);
    }

    public List<String> snippets(String question) {
        return this.knowledge.index().search(question, 3);
    }

    public String answer(String question, String conversationId) {
        RouteDecision decision = decide(question);
        String userPrompt = userPrompt(decision.route(), question);
        ChatClient.ChatClientRequestSpec spec = this.chatClient.prompt()
                .system(systemPrompt(decision.route()))
                .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, conversationId))
                .user(userPrompt);
        if (decision.route() == Route.DB) {
            spec = spec.tools(this.orderTools, this.dateTimeTools);
        }
        String content = spec.call().content();
        return content == null ? "" : content;
    }

    private String userPrompt(Route route, String question) {
        if (route != Route.KNOWLEDGE) {
            return question;
        }
        List<String> snippets = snippets(question);
        String context = snippets.isEmpty() ? "（没有检索到资料）" : String.join("\n---\n", snippets);
        return """
                资料：
                %s

                问题：%s
                """.formatted(context, question);
    }

    private static String systemPrompt(Route route) {
        return switch (route) {
            case CHITCHAT -> "你是友好的中文学习伙伴。不要编造订单状态，也不要编造课程口令。";
            case DB -> "你是订单查询员。订单状态只能来自 lookupOrder 的返回值，用简体中文复述。";
            case KNOWLEDGE -> "你是课程资料助教。只根据给出的资料回答，资料没有就说不知道。用简体中文。";
        };
    }
}
