# AI Agent

这篇说明 Agent 是什么、常见模式怎么拆，以及它们落在本仓库 01–05 的哪一课。LLM 的 token、角色、温度和接口，先看 [LLM-BASICS.md](LLM-BASICS.md)。

## 本仓库用的栈

Spring Boot **3.5.x** + Spring AI **1.1.x**（`spring-ai-starter-model-openai`）。Spring AI 2.x 配 Spring Boot 4，这条学习线不用它。

下面提到的 LangGraph、LlamaIndex、OpenAI Agents SDK，只用来对照概念。代码按 [Spring AI 1.1 参考](https://docs.spring.io/spring-ai/reference/1.1/index.html) 写。

## Agent 是什么

这里的 Agent，指一轮用户问题背后，由程序把四件事串起来：

| 部分 | 做什么 | 本仓库里最接近的例子 |
| --- | --- | --- |
| LLM | 读提示词，生成文字，或声明要调哪个工具 | 各模块的 `ChatClient` |
| 工具 | 查时间、算数、查订单，由你的代码执行 | `03-tools` 的 `@Tool` |
| 记忆 | 让后续轮次看得到先前说过的话，或查得到外部资料 | 02 的对话窗口；04 的向量库 |
| 规划 / 控制流 | 决定下一步走闲聊、走工具，还是走资料 | `05-agent-workflow` 的 `IntentRouter` |

少了控制流，就还是单轮问答：来一句，答一句。LlamaIndex 的文档把 agent 定义成「LLM + memory + tools」，把更宽的「流程里有 LLM 做决定」叫 agentic。这个区分够用。

- [LlamaIndex：Agents](https://developers.llamaindex.ai/python/framework/module_guides/deploying/agents/)
- [OpenAI：Agents](https://developers.openai.com/api/docs/guides/agents)
- [Hugging Face Agents Course：Introduction](https://huggingface.co/learn/agents-course/unit1/introduction)
- [Spring AI 1.1：AI Concepts](https://docs.spring.io/spring-ai/reference/1.1/concepts.html)

## ReAct

ReAct（Yao 等人）把推理和行动交错写进同一条轨迹：先想一步（Thought），再选一个动作（Action），读到环境返回的观察（Observation），再想下一步。论文用的是提示词把这个循环写出来；现在很多框架改由模型的工具调用接口来做 Action。

03 里能看到这个循环的一小段：模型决定调用 `calculate` 或 `lookupOrder`，Spring AI 执行方法，把结果送回去，模型再回答。循环的步数由框架和这次请求兜住，模型自己不能无限加步骤。

05 先用关键词分流，再做一次调用。路由发生在模型之外，所以 05 是工作流，还不是开放的 ReAct 循环。

- [ReAct: Synergizing Reasoning and Acting in Language Models（Yao et al.）](https://arxiv.org/abs/2210.03629)
- [ReAct 项目页](https://react-lm.github.io/)
- [Toolformer（Schick et al.）：模型自己学着调用工具](https://arxiv.org/abs/2302.04761)

## 工具使用

工具调用的合同：你声明名字、参数和描述；模型返回「调用这个、参数是这些」；你的代码执行；你把结果塞回对话。模型不直接连数据库。

03 的三个工具是白名单：`currentDateTime`、`calculate`、`lookupOrder`。订单只有 `A1001`、`A1002`、`A1003`。计算只接受两个数和 `+ - * /`。描述写清楚，模型才知道何时该叫。

Claude 的 computer use 是同一类合同的另一头：模型声明要点击或输入，执行仍发生在你控制的环境里。本仓库没有桌面控制，读它是为了看见「声明」和「执行」是分开的。

- [Spring AI 1.1：Tool Calling](https://docs.spring.io/spring-ai/reference/1.1/api/tools.html)
- [OpenAI：Function calling](https://developers.openai.com/api/docs/guides/function-calling)
- [Claude：Tool use](https://platform.claude.com/docs/en/agents-and-tools/tool-use/overview)
- [Claude：Computer use](https://platform.claude.com/docs/en/agents-and-tools/tool-use/computer-use-tool)

## 单轮 chat 和多步工作流

| | 单轮 chat | 多步工作流 |
| --- | --- | --- |
| 一次用户问题里发生什么 | 一次模型调用，返回文字 | 代码先分支，或模型与工具来回几次，再给最终文字 |
| 谁决定下一步 | 没有下一步 | 你的 `if` / 路由，或模型的工具调用 |
| 本仓库 | `01-chat`：`prompt().user(...).call().content()` | `03-tools` 的工具往返；`05-agent-workflow` 先 `IntentRouter` 再调用 |

02 仍是「每问一次模型」，多出来的是把历史消息拼进同一次请求。那是多轮对话，还不是多步规划。

## 路由和编排

路由：看问题属于哪一类，再交给对应的提示词、工具或资料。编排：把这些步骤排成固定顺序。

05 的 `IntentRouter` 用关键词：

| 命中 | 路由 | 随后做什么 |
| --- | --- | --- |
| 订单、查单、物流、order | `DB` | 带上订单和时间工具，再调模型 |
| 口令、课程、RAG、Spring AI 等 | `KNOWLEDGE` | 用关键词从 markdown 摘段落，塞进 user 提示词 |
| 其余 | `CHITCHAT` | 只聊天，不挂工具 |

订单优先于知识。`GET /route` 不调用模型，方便先看分流对不对。

LangGraph 把同类事情画成图：节点是步骤，边是分支和循环，状态在节点之间传。05 是这张图的最小版本，边写死在 Java 里。

- [Spring AI 1.1：Advisors](https://docs.spring.io/spring-ai/reference/1.1/api/advisors.html)（在送进模型前后插入记忆、检索等步骤）
- [LangGraph overview](https://docs.langchain.com/oss/python/langgraph/overview)

## 记忆：短对话，以及 RAG

| | 短期对话记忆 | RAG |
| --- | --- | --- |
| 记的是什么 | 这个 `conversationId` 里刚说过的 user / assistant 消息 | 你事先放进库里的文档 |
| 怎么用 | 下次请求把最近若干条再送回去 | 先检索相近片段，再放进提示词 |
| 本仓库 | `MessageChatMemoryAdvisor` + `MessageWindowChatMemory` | 04：嵌入后进 `SimpleVectorStore`，`QuestionAnswerAdvisor` 取 top 3 |
| 重启之后 | 没了（存在进程内存） | 04 的向量也没了，要再 `POST /ingest` |

02 只有对话记忆。04 两者都有：窗口记聊天，向量库存课程 markdown。05 的知识分支用关键词摘录，向量检索留在 04，避免两课叠在一起。

- [Spring AI 1.1：Chat Memory](https://docs.spring.io/spring-ai/reference/1.1/api/chat-memory.html)
- [Spring AI 1.1：Retrieval Augmented Generation](https://docs.spring.io/spring-ai/reference/1.1/api/retrieval-augmented-generation.html)

## RAG 是什么，和 Agent 的关系

RAG（Retrieval-Augmented Generation，Lewis 等人）先检索，再让模型根据检索到的文字生成。模型的权重里没有你的课程代号，提示词里有了，它才能照着说。

和 Agent 的关系：RAG 是给模型补外部事实的一种办法。它可以是 Agent 的一个工具（「先检索再回答」），也可以是工作流里固定的一步（每次提问都检索，模型不决定检不检索）。04 是后者：`QuestionAnswerAdvisor` 挂在 `ChatClient` 上，提问就会查向量库。05 的知识分支更简单，用关键词摘段落，同样是「先取资料，再生成」。

- [Retrieval-Augmented Generation for Knowledge-Intensive NLP Tasks（Lewis et al.）](https://arxiv.org/abs/2005.11401)
- [Spring AI 1.1：AI Concepts（RAG）](https://docs.spring.io/spring-ai/reference/1.1/concepts.html#concept-rag)
- [Spring AI 1.1：Vector Databases](https://docs.spring.io/spring-ai/reference/1.1/api/vectordbs.html)
- [OpenAI：Vector embeddings](https://developers.openai.com/api/docs/guides/embeddings)

## 常见反模式

| 反模式 | 会怎样 | 本仓库怎么收着 |
| --- | --- | --- |
| 无边界工具 | 模型能跑 shell、打任意 HTTP、改数据。描述一模糊，它就会调用 | 03 只有三个方法。计算不走脚本引擎。订单是内存里的三笔假数据 |
| 无限循环 | 模型反复要工具，停不下来，token 一直涨 | 05 每个请求只选一条路由，再调用一次。工具往返留在 Spring AI 这一次调用内部 |
| 把 RAG 当成聊天记忆 | 用户刚说的名字去向量库里找，找不到；文档里的事实却指望对话窗口一直记得 | 02 记对话，04 检索文档。重启都会丢，因为都在内存 |
| 不设「不知道」 | 资料没提到，模型仍会编一句顺的 | 04 和 05 知识分支的 system 要求：资料没有就说不知道 |

## 概念对应到 01–05

| 概念 | 模块 | 代码里看什么 |
| --- | --- | --- |
| 单轮 chat | `01-chat` | `defaultSystem`，`prompt().user(...).call().content()`，没有历史 |
| 流式输出 + 短期记忆 | `02-stream` | `/chat/stream`；`MessageChatMemoryAdvisor`；窗口 12 条；`ChatMemory.CONVERSATION_ID` |
| 工具使用 | `03-tools` | 三个 `@Tool`，`defaultTools(...)` |
| RAG（嵌入 + 检索 + 回答） | `04-memory-rag` | `POST /ingest`；`SimpleVectorStore`；`QuestionAnswerAdvisor` topK 3；另有 8 条对话记忆 |
| 关键词路由工作流 | `05-agent-workflow` | `IntentRouter`：闲聊 / 订单工具 / 本地资料摘录。`GET /route` 不调模型 |

## 和其他框架对照

概念可以对照着读。作业和代码以 Spring AI 1.1 为准。

| 你在别处看到的 | 它在讲什么 | 回到本仓库 |
| --- | --- | --- |
| [LangGraph](https://docs.langchain.com/oss/python/langgraph/overview) | 用图编排有状态的多步 Agent：分支、循环、持久化 | 05 的路由器是手写的一条分支，还没有图运行时 |
| [LlamaIndex Agents](https://developers.llamaindex.ai/python/framework/module_guides/deploying/agents/) | Agent = LLM + memory + tools；另有 ReAct 式实现 | 03 是 tools，02/04 是 memory，04 的检索是 RAG |
| [OpenAI Agents SDK](https://openai.github.io/openai-agents-python/) | 用代码定义指令、模型和工具，再跑一轮 agent | 本仓库用 `ChatClient` 把同样三件事配在 Spring 里。[Agents 概念页](https://developers.openai.com/api/docs/guides/agents) 用来选产品形态 |
| Assistants API | 早期的托管线程 + 工具 | 官方已标为退役，新代码看 [迁移说明](https://developers.openai.com/api/docs/assistants/migration)，本仓库不使用它 |
| [Claude tool use](https://platform.claude.com/docs/en/agents-and-tools/tool-use/overview) | 模型声明工具调用，应用执行 | 与 03 的 `@Tool` 是同一类分工 |

综述如果要读一篇：Wang 等人的 [A Survey on Large Language Model based Autonomous Agents](https://arxiv.org/abs/2308.11432)。另一篇覆盖规划、记忆和工具使用的是 Xi 等人的 [The Rise and Potential of Large Language Model Based Agents: A Survey](https://arxiv.org/abs/2309.07864)。

外链清单：[READING-LIST.md](READING-LIST.md)。
