package dev.learn.rag;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

/**
 * 启动时不调用嵌入接口，避免没有 Key 时进程起不来。
 * 学习者准备好密钥后再 POST /ingest。
 */
@Service
public class KnowledgeBase {

    private final VectorStore vectorStore;

    private final AtomicInteger chunks = new AtomicInteger();

    private final AtomicBoolean ingested = new AtomicBoolean();

    public KnowledgeBase(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    public int chunkCount() {
        return this.chunks.get();
    }

    public int ingest() {
        if (!this.ingested.compareAndSet(false, true)) {
            return this.chunks.get();
        }
        try {
            List<Document> documents = readDocuments();
            this.vectorStore.add(documents);
            return this.chunks.addAndGet(documents.size());
        }
        catch (RuntimeException ex) {
            this.ingested.set(false);
            throw ex;
        }
    }

    private List<Document> readDocuments() {
        Resource[] files;
        try {
            files = new PathMatchingResourcePatternResolver().getResources("classpath:knowledge/*.md");
        }
        catch (IOException ex) {
            throw new IllegalStateException("读取 knowledge 目录失败", ex);
        }
        List<Document> documents = new ArrayList<>();
        for (Resource file : files) {
            String filename = file.getFilename() == null ? "unknown.md" : file.getFilename();
            MarkdownDocumentReaderConfig config = MarkdownDocumentReaderConfig.builder()
                    .withHorizontalRuleCreateDocument(true)
                    .withIncludeCodeBlock(true)
                    .withIncludeBlockquote(true)
                    .withAdditionalMetadata("filename", filename)
                    .build();
            documents.addAll(new MarkdownDocumentReader(file, config).get());
        }
        if (documents.isEmpty()) {
            throw new IllegalStateException("classpath:knowledge 下没有 markdown");
        }
        return documents;
    }
}
