package dev.learn.rag;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.StreamUtils;

import static org.assertj.core.api.Assertions.assertThat;

class SampleDocsTest {

    @Test
    void knowledgeFolderHasCourseFacts() throws Exception {
        Resource[] docs = new PathMatchingResourcePatternResolver().getResources("classpath:knowledge/*.md");
        assertThat(docs).hasSizeGreaterThanOrEqualTo(3);
        StringBuilder all = new StringBuilder();
        for (Resource doc : docs) {
            all.append(StreamUtils.copyToString(doc.getInputStream(), StandardCharsets.UTF_8));
        }
        assertThat(all.toString()).contains("SA-17").contains("第一周只做 01 到 03");
    }
}
