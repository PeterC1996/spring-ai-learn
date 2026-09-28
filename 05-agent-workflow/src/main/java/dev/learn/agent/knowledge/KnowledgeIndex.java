package dev.learn.agent.knowledge;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * 不调用嵌入模型的关键词摘录，用来在分流练习里看清「先检索再回答」。
 * 真正的向量检索在 04-memory-rag。
 */
public final class KnowledgeIndex {

    private static final List<String> PHRASES = List.of(
            "spring ai", "chatclient", "rag", "口令", "向量", "订单", "学习顺序", "橙子");

    private final List<TextDoc> docs;

    public KnowledgeIndex(List<TextDoc> docs) {
        this.docs = List.copyOf(docs);
    }

    public List<String> search(String question, int limit) {
        List<String> terms = terms(question);
        if (terms.isEmpty() || limit <= 0) {
            return List.of();
        }
        List<Hit> hits = new ArrayList<>();
        for (TextDoc doc : this.docs) {
            for (String paragraph : paragraphs(doc.text())) {
                int score = score(paragraph.toLowerCase(Locale.ROOT), terms);
                if (score > 0) {
                    hits.add(new Hit(score, doc.name() + "：" + paragraph.trim()));
                }
            }
        }
        return hits.stream()
                .sorted(Comparator.comparingInt(Hit::score).reversed())
                .limit(limit)
                .map(Hit::text)
                .toList();
    }

    private static int score(String paragraph, List<String> terms) {
        int score = 0;
        for (String term : terms) {
            if (paragraph.contains(term)) {
                score++;
            }
        }
        return score;
    }

    private static List<String> terms(String question) {
        if (question == null || question.isBlank()) {
            return List.of();
        }
        String normalized = question.toLowerCase(Locale.ROOT);
        List<String> terms = new ArrayList<>();
        for (String phrase : PHRASES) {
            if (normalized.contains(phrase)) {
                terms.add(phrase);
            }
        }
        for (String part : normalized.split("[\\s,，。！？、；;:：]+")) {
            if (part.length() >= 2) {
                terms.add(part);
            }
        }
        return terms;
    }

    private static List<String> paragraphs(String text) {
        String[] parts = text.split("\\n\\s*\\n");
        List<String> paragraphs = new ArrayList<>();
        for (String part : parts) {
            if (!part.isBlank()) {
                paragraphs.add(part);
            }
        }
        return paragraphs;
    }

    private record Hit(int score, String text) {
    }
}
