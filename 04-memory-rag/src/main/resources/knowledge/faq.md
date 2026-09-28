# 常见配置

环境变量 SPRING_AI_OPENAI_BASE_URL 填接口根地址。Spring AI 会自己拼接 /v1/chat/completions，所以根地址一般不要再带 /v1。

没有真实 API Key 时，`mvn -q -DskipTests package` 仍然应该成功。占位符 sk-placeholder-not-a-real-key 只为了让进程能启动。

04 模块的入库会调用嵌入模型。只提供对话、不提供 embeddings 的网关，可以先阅读代码，入库会失败，这是预期现象。
