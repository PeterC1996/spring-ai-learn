# 两周学习计划

按模块顺序走，不要跳。每天把结论写进仓库根目录的 `NOTES.md`。

没有真实 API Key 时，先把代码读完、把 `mvn -q -DskipTests package` 跑通。调用模型是第二步。

## 第 1 周：把对话跑起来

开始写代码前，先花半天读背景，再进入第 1 天。下面的「第 N 天」编号不变。

| 天 | 模块 | 做完的标志 |
| --- | --- | --- |
| 开始前（约半天） | [LLM-BASICS.md](LLM-BASICS.md)、[AI-AGENTS.md](AI-AGENTS.md) | 能用自己的话讲清 token、temperature、工具调用、RAG，并指出它们落在 01–05 的哪一课。外链在 [READING-LIST.md](READING-LIST.md) |
| 第 1 天 | 仓库与 `01-chat` | 能说清 `ChatClient` 和 `ChatModel` 谁负责拼提示词、谁负责发请求。改一句 system 提示词，再用 POST `/chat` 问一个问题 |
| 第 2 天 | `02-stream` | 同一个 `conversationId` 连续问两句，第二句能用上第一句的信息。再看 `/chat/stream` 的输出是一段段出来的 |
| 第 3 天 | `03-tools` | 分别问「现在几点」「12 乘 7」「订单 A1001」。对照三个 `@Tool` 方法，写下模型为什么会选这个工具 |
| 第 4 天 | `03-tools` | 把其中一个工具的中文描述改模糊，再问一次，观察模型会不会叫错工具。然后把描述改回去 |
| 第 5 天 | 复盘 | 用自己的话写：单轮、流式、记忆、工具，分别解决模型的哪一种无能 |

第 1 周不要做向量库。工具和 RAG 都是「给模型补外部事实」，但调用方式不同。

## 第 2 周：检索和分流

| 天 | 模块 | 做完的标志 |
| --- | --- | --- |
| 第 6 天 | `04-memory-rag` | 读 `knowledge/` 里的三篇 markdown。不看模型，先找出只有文档里才有的事实（例如课程代号） |
| 第 7 天 | `04-memory-rag` | `POST /ingest` 成功后，问那个文档里才有的事实。再把文档改一个词，重新入库，确认回答跟着变 |
| 第 8 天 | `05-agent-workflow` | 不调模型，只打 `GET /route`。准备三句分别落到闲聊、订单、知识 |
| 第 9 天 | `05-agent-workflow` | 调 `POST /chat`，看订单问题走工具、知识问题走摘录。故意问一句两边都不该答的话 |
| 第 10 天 | 复盘 | 写一张自己的分流表：什么问题该闲聊、该查库、该查文档。这就是后面做 Agent 的骨架 |

## 版本（避免看错文档）

- Java 17 或更高
- Spring Boot 3.5.16
- Spring AI BOM 1.1.8（`spring-ai-starter-model-openai`）

Spring AI 2.x 的文档默认对应 Spring Boot 4。类名大多还在，但依赖坐标和部分 Advisor 行为不一样。学习期间以本仓库能编译的 1.1.8 为准。
