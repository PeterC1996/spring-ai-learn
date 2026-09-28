package dev.learn.agent.knowledge;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

@Component
public class ClasspathKnowledge {

    private final KnowledgeIndex index;

    public ClasspathKnowledge() throws IOException {
        Resource[] resources = new PathMatchingResourcePatternResolver().getResources("classpath:knowledge/*.md");
        List<TextDoc> docs = new ArrayList<>();
        for (Resource resource : resources) {
            String name = resource.getFilename() == null ? "doc.md" : resource.getFilename();
            String text = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
            docs.add(new TextDoc(name, text));
        }
        this.index = new KnowledgeIndex(docs);
    }

    public KnowledgeIndex index() {
        return this.index;
    }
}
