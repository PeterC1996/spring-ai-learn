package dev.learn.agent.route;

import java.util.Locale;

/**
 * 用关键词做第一版路由器。先看清分流，再考虑让模型来分类。
 * 订单优先于知识，其余算闲聊。
 */
public final class IntentRouter {

    public RouteDecision decide(String question) {
        String text = question == null ? "" : question.toLowerCase(Locale.ROOT);
        if (containsAny(text, "订单", "查单", "物流", "order")) {
            return new RouteDecision(Route.DB, "命中订单关键词，走模拟数据库工具");
        }
        if (containsAny(text, "口令", "文档", "知识", "课程", "学习顺序", "向量", "rag", "spring ai")) {
            return new RouteDecision(Route.KNOWLEDGE, "命中课程资料关键词，走本地摘录");
        }
        return new RouteDecision(Route.CHITCHAT, "没有命中订单或资料关键词，走闲聊");
    }

    private static boolean containsAny(String text, String... needles) {
        for (String needle : needles) {
            if (text.contains(needle)) {
                return true;
            }
        }
        return false;
    }
}
