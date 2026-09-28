# LLM 基础知识

跑 `01-chat` 到 `05-agent-workflow` 之前，先把这页扫一遍。代码里的类名，多数是在实现这里的概念。

Agent、RAG 和路由见 [AI-AGENTS.md](AI-AGENTS.md)。外链按优先级排在 [READING-LIST.md](READING-LIST.md)。

## 本仓库用的栈

| 组件 | 版本 | 说明 |
| --- | --- | --- |
| Spring Boot | 3.5.x（父 POM 3.5.16） | 学习线留在 Boot 3 |
| Spring AI | 1.1.x（BOM 1.1.8） | `spring-ai-starter-model-openai`，版本由 BOM 管 |
| 对话协议 | OpenAI Chat Completions | starter 请求 `{base-url}/v1/chat/completions` |

Spring AI **2.0.x** 配 Spring Boot **4**。看文档时打开 [Spring AI 1.1 参考](https://docs.spring.io/spring-ai/reference/1.1/index.html)。1.1 页面顶部会提示最新稳定版是 2.x：类名大多还能对上，依赖坐标和部分 Advisor 以 1.1 页面为准。

## 模型在做什么

对话模型按已经看到的 token，继续预测下一个 token，再把 token 拼成文字。单独一次生成里，它没有「先去查库」这一步。要查库、要算数、要读你的 markdown，得把结果放进提示词，或让它通过工具调用把活交给你的代码。

- [OpenAI：Key concepts](https://developers.openai.com/api/docs/concepts)
- [Hugging Face LLM Course：Introduction](https://huggingface.co/learn/llm-course/chapter1/1)
- [Hugging Face LLM Course：Transformers, what can they do?](https://huggingface.co/learn/llm-course/chapter1/3)
- [Spring AI 1.1：AI Concepts](https://docs.spring.io/spring-ai/reference/1.1/concepts.html)

## Token

Token 是模型读写文本的单位。英文里一个常见词，或词的一段，经常是一个 token。中文往往一个字对应一个或多个 token。计费、耗时、能不能放进上下文，都按 token 算。输入和输出都计入。

- [OpenAI：Key concepts（Tokens 一节）](https://developers.openai.com/api/docs/concepts)
- [OpenAI：Advanced usage（Managing tokens）](https://developers.openai.com/api/docs/guides/advanced-usage)
- [Hugging Face NLP Course：Byte-Pair Encoding tokenization](https://huggingface.co/learn/nlp-course/chapter6/5)
- [Gemini：Tokens](https://ai.google.dev/gemini-api/docs/tokens)

## 上下文窗口

一次请求里，系统提示、历史消息、工具描述、检索摘录，再加上模型正要生成的内容，合在一起不能超过该模型的上下文长度。超了，请求失败，或者更早的内容被截掉。各模型的窗口长度不同。

应用自己也可以先截一刀。本仓库的对话记忆用 `MessageWindowChatMemory`：`02-stream` 保留最近 12 条，`04-memory-rag` 保留最近 8 条。这是窗口条数，模型的上下文上限还在。

- [OpenAI：Compare models](https://developers.openai.com/api/docs/models/compare)
- [Spring AI 1.1：Chat Memory](https://docs.spring.io/spring-ai/reference/1.1/api/chat-memory.html)

## Temperature

`temperature` 控制抽样有多散。Chat Completions 里大约取 0 到 2：更高更随机，更低更集中。同一个问题多问几次，高 temperature 更容易答得不一样。

本仓库写在各模块 `application.yml` 的 `spring.ai.openai.chat.options.temperature`：

| 模块 | 值 | 用意 |
| --- | --- | --- |
| `01-chat`、`02-stream` | 0.3 | 讲解类回答，稍微稳一点 |
| `03-tools` | 0.1 | 工具调用，少发散 |
| `04-memory-rag`、`05-agent-workflow` | 0.2 | 按资料或工具结果复述 |

- [Chat Completions：`temperature`](https://developers.openai.com/api/reference/resources/chat#chat-create-temperature)
- [Spring AI 1.1：OpenAI Chat](https://docs.spring.io/spring-ai/reference/1.1/api/chat/openai-chat.html)（`chat.options.temperature`）
- [Gemini：Text generation](https://ai.google.dev/gemini-api/docs/text-generation)（`generation_config` 里的 temperature）

## system、user、assistant

一次对话是一组带角色的消息：

| 角色 | 谁写的 | 在本仓库里 |
| --- | --- | --- |
| `system` | 开发者定的规矩 | `ChatClient` 的 `defaultSystem`。01 里是「用简体中文、尽量短，并点出类名」 |
| `user` | 这一问 | `prompt().user(question)` |
| `assistant` | 模型上一轮的回答 | 02 的记忆 Advisor 会把 user 和 assistant 再送回下一次请求 |

Claude 的 Messages、Gemini 的 system instruction，处理的是同一件事：把「规矩」和「这一问」分开。

- [Chat Completions 参考](https://developers.openai.com/api/reference/resources/chat)（`role` 为 `system` / `user` / `assistant`）
- [Claude：Working with messages](https://platform.claude.com/docs/en/build-with-claude/working-with-messages)
- [Gemini：System instructions](https://ai.google.dev/gemini-api/docs/text-generation#system-instructions)
- [Spring AI 1.1：ChatClient](https://docs.spring.io/spring-ai/reference/1.1/api/chatclient.html)
- [Spring AI 1.1：ChatModel](https://docs.spring.io/spring-ai/reference/1.1/api/chatmodel.html)

## Chat Completions 和 Embeddings

两种接口，输入输出都不同。

| | Chat Completions | Embeddings |
| --- | --- | --- |
| 要什么 | 一组消息，得到一段回答，或一次工具调用 | 一段文本，得到一串浮点向量 |
| 用来做什么 | 问答、总结、决定调哪个工具 | 比相似度，给检索用 |
| HTTP | `POST /v1/chat/completions` | `POST /v1/embeddings` |
| 本仓库 | 01、02、03、05，以及 04 的回答 | 04 的 `POST /ingest` |

只提供对话、不提供 embeddings 的网关，可以做 01、02、03、05。04 入库会失败，这是预期。

- [OpenAI：Text generation](https://developers.openai.com/api/docs/guides/text)
- [OpenAI：Vector embeddings](https://developers.openai.com/api/docs/guides/embeddings)
- [Chat Completions 参考](https://developers.openai.com/api/reference/resources/chat)
- [Gemini：Embeddings](https://ai.google.dev/gemini-api/docs/embeddings)
- [Spring AI 1.1：Embeddings](https://docs.spring.io/spring-ai/reference/1.1/api/embeddings.html)
- [Spring AI 1.1：OpenAI Embeddings](https://docs.spring.io/spring-ai/reference/1.1/api/embeddings/openai-embeddings.html)

## 流式输出

默认等整段生成完再返回。请求里 `stream: true` 时，服务端用 SSE 把已经写出的片段先推过来。长回答可以先看到开头。

02 的 `GET /chat/stream` 调 `ChatClient` 的 `.stream().content()`，响应类型是 `text/event-stream`。curl 要加 `-N`，否则会攒着再打印。

- [OpenAI：Streaming API responses](https://developers.openai.com/api/docs/guides/streaming-responses)（`stream=true` 与 SSE；示例偏 Responses API，机制与 Chat Completions 的流式相同）
- [Chat Completions 参考](https://developers.openai.com/api/reference/resources/chat)（本仓库 starter 走的就是这个接口，参考页里有 Streaming）
- [Claude：Streaming](https://platform.claude.com/docs/en/build-with-claude/streaming)
- [Gemini：Streaming responses](https://ai.google.dev/gemini-api/docs/text-generation#streaming-responses)
- [Spring AI 1.1：ChatClient](https://docs.spring.io/spring-ai/reference/1.1/api/chatclient.html)

## 工具调用（function calling）

模型只负责声明：我要调用某个函数，参数是这些。函数在你的进程里执行。Spring AI 接到调用后，跑带 `@Tool` 的方法，把返回值送回模型，模型再写成给用户的话。

03 注册了三个工具：当前时间、四则运算、模拟订单。描述写成中文，因为模型靠描述决定叫不叫、传什么参数。

- [OpenAI：Function calling](https://developers.openai.com/api/docs/guides/function-calling)（同一页写了 Responses API 和 Chat Completions）
- [Claude：Tool use](https://platform.claude.com/docs/en/agents-and-tools/tool-use/overview)
- [Claude：How tool use works](https://platform.claude.com/docs/en/agents-and-tools/tool-use/how-tool-use-works)
- [Gemini：Function calling](https://ai.google.dev/gemini-api/docs/function-calling)
- [Spring AI 1.1：Tool Calling](https://docs.spring.io/spring-ai/reference/1.1/api/tools.html)
- [Spring AI 1.1：AI Concepts（Tool Calling）](https://docs.spring.io/spring-ai/reference/1.1/concepts.html#concept-fc)

## 幻觉和局限

模型在续写看起来合理的 token。训练数据里没有的事实、过期的事实、算错的数，它仍可能说得很顺。这就是幻觉。

本仓库用三种办法把范围收小，都不是把幻觉消掉：

- 提示词写明「资料没有就说不知道」（04，以及 05 的知识分支）
- 订单状态只许来自工具返回值（03，以及 05 的订单分支）
- 计算交给 `calculate`，工具描述写「不要心算」

上下文里没有的内容，模型看不到。进程一重启，内存里的对话和向量就没了。

- [OpenAI：Safety best practices](https://developers.openai.com/api/docs/guides/safety-best-practices)
- [Spring AI 1.1：Evaluating AI responses](https://docs.spring.io/spring-ai/reference/1.1/concepts.html#concept-evaluating-ai-responses)

## 「OpenAI 兼容 API」是什么意思

兼容，指的是 HTTP 合同和 OpenAI Chat Completions 对齐：同样的路径，JSON 里同样有 `messages`、`temperature`、`tools`、`stream`。换 DeepSeek 或一站式网关时，通常只改 `SPRING_AI_OPENAI_BASE_URL` 和模型名。

Spring AI 1.1 OpenAI starter 的默认值：

| 配置 | 默认 |
| --- | --- |
| `spring.ai.openai.base-url` | `https://api.openai.com` |
| `spring.ai.openai.chat.completions-path` | `/v1/chat/completions` |

根地址自己再加 `/v1`，会拼成 `/v1/v1/chat/completions`。嵌入是另一条路径。对话兼容，不等于对方也提供 embeddings。

- [Chat Completions：Create chat completion](https://developers.openai.com/api/reference/resources/chat)（`POST /chat/completions`）
- [Spring AI 1.1：OpenAI Chat](https://docs.spring.io/spring-ai/reference/1.1/api/chat/openai-chat.html)（`base-url` 与 `completions-path`）

## 和 01–05 的对应

| 概念 | 去哪看 |
| --- | --- |
| `system` + 单轮 `user` | `01-chat` 的 `ChatController` |
| 流式，以及把 assistant 历史拼回去 | `02-stream` |
| 工具调用 | `03-tools` |
| embeddings 和检索 | `04-memory-rag` |
| 按问题换提示词和工具 | `05-agent-workflow` |

下一篇：[AI-AGENTS.md](AI-AGENTS.md)。
