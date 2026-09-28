# 阅读清单

按这个顺序读。每一条先看标题里的那一页，够用再往下。本仓库是 Spring Boot 3.5.x + Spring AI 1.1.x，Spring AI 的链接都指 1.1，不要改用 2.x 页面抄依赖。

概念消化写在 [LLM-BASICS.md](LLM-BASICS.md) 和 [AI-AGENTS.md](AI-AGENTS.md)。

1. [LLM 基础知识（本仓库）](LLM-BASICS.md) — token、上下文、temperature、三种角色、对话和嵌入、流式、工具调用、幻觉、OpenAI 兼容 API，并标出落在哪一课。
2. [AI Agent（本仓库）](AI-AGENTS.md) — Agent 的四块、ReAct、路由、记忆和 RAG、反模式，以及和 01–05 的对照表。
3. [Spring AI 1.1：AI Concepts](https://docs.spring.io/spring-ai/reference/1.1/concepts.html) — 和本仓库同一套词：模型、token、RAG、工具调用。
4. [OpenAI：Key concepts](https://developers.openai.com/api/docs/concepts) — token、上下文长度、生成和嵌入的差别，短，适合当词典。
5. [Chat Completions 参考](https://developers.openai.com/api/reference/resources/chat) — 本仓库 starter 实际调用的 `POST /chat/completions`：角色、temperature、stream。
6. [OpenAI：Function calling](https://developers.openai.com/api/docs/guides/function-calling) — 工具描述、模型发起调用、应用执行并回传。读完再看 `03-tools`。
7. [Spring AI 1.1：ChatClient](https://docs.spring.io/spring-ai/reference/1.1/api/chatclient.html) — `prompt().user().call()` / `stream()` 的官方形状，对照 01 和 02。
8. [Spring AI 1.1：Tool Calling](https://docs.spring.io/spring-ai/reference/1.1/api/tools.html) — `@Tool` 在 1.1 里怎么挂到 `ChatClient`。
9. [Spring AI 1.1：Chat Memory](https://docs.spring.io/spring-ai/reference/1.1/api/chat-memory.html) 与 [RAG](https://docs.spring.io/spring-ai/reference/1.1/api/retrieval-augmented-generation.html) — 短期消息窗口和检索增强是两条线，04 两样都用了。
10. [ReAct（Yao et al.）](https://arxiv.org/abs/2210.03629) — 推理和行动交错的那篇奠基论文；[项目页](https://react-lm.github.io/) 有例子。读摘要和循环示意图即可。
11. [RAG（Lewis et al.）](https://arxiv.org/abs/2005.11401) — 「先检索，再生成」这个名字的出处。读摘要，再回来看 04 的 `QuestionAnswerAdvisor`。
12. [Hugging Face LLM Course 第 1 章](https://huggingface.co/learn/llm-course/chapter1/1) — 想知道 Transformer 能做什么、token 从哪来时再读；[BPE 分词](https://huggingface.co/learn/nlp-course/chapter6/5) 是可选的一节。
13. [LangGraph overview](https://docs.langchain.com/oss/python/langgraph/overview)、[LlamaIndex Agents](https://developers.llamaindex.ai/python/framework/module_guides/deploying/agents/)、[OpenAI Agents SDK](https://openai.github.io/openai-agents-python/) — 三份里挑一份看「别人怎么编排」。概念可对照，实现仍用 Spring AI。
14. [A Survey on LLM-based Autonomous Agents（Wang et al.）](https://arxiv.org/abs/2308.11432) — 想要一张领域地图时再读。规划、记忆、工具的另一篇综述是 [Xi et al.](https://arxiv.org/abs/2309.07864)。
