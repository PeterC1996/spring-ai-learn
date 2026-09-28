package dev.learn.agent.knowledge;

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class KnowledgeIndexTest {

    @Test
    void findsPassphraseParagraph() {
        KnowledgeIndex index = new KnowledgeIndex(List.of(
                new TextDoc("passphrase.md", """
                        # 课程口令

                        当问题里出现「口令」时，资料中的答案是「橙子」。
                        """)));

        assertThat(index.search("课程口令是什么", 2))
                .anyMatch(hit -> hit.contains("橙子"));
    }

    @Test
    void emptyQuestionReturnsNothing() {
        KnowledgeIndex index = new KnowledgeIndex(List.of(new TextDoc("a.md", "口令是橙子")));
        assertThat(index.search(" ", 3)).isEmpty();
    }
}
