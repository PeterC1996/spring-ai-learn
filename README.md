# Spring AI 学习仓库

按 `01 → 05` 把 Spring AI 用起来。每个目录都是一个可以单独启动的 Spring Boot 应用，代码只保留这一课要看的那几行。

## 你要装什么

- JDK 17 或更高（本仓库用 17 语法编译，更高版本可以跑）
- Maven 3.8 或更高

不需要先申请 API Key。没有密钥时，整个仓库必须能编译通过。真正发问时才会连模型。

## 版本（先看这一段，避免跟错文档）

| 组件 | 版本 | 说明 |
| --- | --- | --- |
| Spring Boot | 3.5.16 | 父 POM：`spring-boot-starter-parent` |
| Spring AI BOM | 1.1.8 | `org.springframework.ai:spring-ai-bom` |
| 对话 starter | 不写版本号 | `spring-ai-starter-model-openai`，版本由 BOM 管 |

Spring AI **2.0.x** 是更新的稳定版，但它只支持 Spring Boot 4。这条学习线留在 Boot 3 上，所以用 1.1.8，不使用 2.x 的依赖名。看官方文档时，优先打开 1.1 的参考文档；2.0 页面里的类名大多还能对上，坐标和部分 Advisor 行为不要直接抄。

## 环境变量

三个对话相关变量，名字和 Spring Boot 的松散绑定一致：

| 变量 | 对应配置 | 示例 |
| --- | --- | --- |
| `SPRING_AI_OPENAI_API_KEY` | `spring.ai.openai.api-key` | 你的密钥 |
| `SPRING_AI_OPENAI_BASE_URL` | `spring.ai.openai.base-url` | `https://api.openai.com` |
| `SPRING_AI_OPENAI_CHAT_OPTIONS_MODEL` | `spring.ai.openai.chat.options.model` | `gpt-4o-mini` |

04 另外认一个嵌入模型变量：`SPRING_AI_OPENAI_EMBEDDING_OPTIONS_MODEL`，默认 `text-embedding-3-small`。

配置文件里写的是占位符 `sk-placeholder-not-a-real-key`，只为了没有密钥时进程也能启动。占位符不是密钥，调用会返回 401 一类的错误。每个模块还有一份 `application.yml.example`，内容和 `application.yml` 相同，里面没有真实密钥。

```bash
export SPRING_AI_OPENAI_API_KEY=sk-把你的密钥放这里
export SPRING_AI_OPENAI_BASE_URL=https://api.openai.com
export SPRING_AI_OPENAI_CHAT_OPTIONS_MODEL=gpt-4o-mini
```

PowerShell：

```powershell
$env:SPRING_AI_OPENAI_API_KEY="sk-把你的密钥放这里"
$env:SPRING_AI_OPENAI_BASE_URL="https://api.openai.com"
$env:SPRING_AI_OPENAI_CHAT_OPTIONS_MODEL="gpt-4o-mini"
```

也可以把同一组变量写进仓库根目录的 `.env`（参考 `.env.example`）。`.env` 已被忽略。Spring Boot **不会**自动读取 `.env`，需要你自己 `export`，或让 IDE 加载它。

兼容接口（DeepSeek、一站式网关等）通常只改 `BASE_URL` 和模型名。Spring AI 会在根地址后面拼接 `/v1/chat/completions`，所以根地址一般不要自己再加 `/v1`。只提供对话、不提供 embeddings 的网关可以做 01、02、03、05；04 的 `POST /ingest` 会失败，这是预期，先把入库代码读完即可。

## 先编译

在仓库根目录：

```bash
mvn -q -DskipTests package
```

这条命令不连接模型。想连测试一起跑：

```bash
mvn test
```

现有测试只覆盖计算器、订单查询、关键词分流和样例文档，不发起网络请求。

## 背景知识

先读概念再跑代码。后面看 `ChatClient`、Advisor 和路由时，少猜一层「这行到底在补模型的哪一种无能」。

| 文档 | 读完能回答什么 |
| --- | --- |
| [docs/LLM-BASICS.md](docs/LLM-BASICS.md) | token、上下文、temperature、三种角色、对话和嵌入、流式、工具调用、幻觉、OpenAI 兼容 API |
| [docs/AI-AGENTS.md](docs/AI-AGENTS.md) | Agent 由什么组成，ReAct、记忆和 RAG、路由，以及它们对应 01–05 的哪一课 |
| [docs/READING-LIST.md](docs/READING-LIST.md) | 按优先级排好的外链，每条一句话说明为什么读 |

技术栈以仓库为准：Spring Boot 3.5.x + Spring AI 1.1.x（`spring-ai-starter-model-openai`）。官方文档请打开 1.1 参考。Spring AI 2.x 对应 Spring Boot 4。

## 学习顺序

一次只启动一个模块。端口故意错开，避免你忘记停掉上一个。建议先花半天读上面的两篇背景，再做第 1 天。

| 顺序 | 目录 | 端口 | 这一课看什么 |
| --- | --- | --- | --- |
| 1 | `01-chat` | 8081 | `ChatClient` 单轮问答。没有历史 |
| 2 | `02-stream` | 8082 | 流式输出，外加 `MessageChatMemoryAdvisor` 多轮记忆 |
| 3 | `03-tools` | 8083 | 三个 `@Tool`：上海时间、四则运算、模拟订单 |
| 4 | `04-memory-rag` | 8084 | markdown → 嵌入 → `SimpleVectorStore` → `QuestionAnswerAdvisor` |
| 5 | `05-agent-workflow` | 8085 | 关键词路由器：闲聊 / 订单工具 / 本地资料摘录 |

每天的拆法写在 [docs/LEARNING-PLAN.md](docs/LEARNING-PLAN.md)。笔记模板在 [NOTES.md](NOTES.md)。明天开始的话，只做第 1 周第 1 天：读 `01-chat`，改一句 system 提示词，问一个问题，把原话记下来。

### 01 单轮问答

```bash
mvn -pl 01-chat spring-boot:run
```

```bash
curl -s http://localhost:8081/
curl -s http://localhost:8081/chat \
  -H 'Content-Type: application/json' \
  -d '{"question":"用一句话解释 ChatClient"}'
```

打开 `ChatController`。重点只有构造器里的 `defaultSystem`，以及 `prompt().user(...).call().content()`。

### 02 流式和多轮

```bash
mvn -pl 02-stream spring-boot:run
```

先用同一个 `conversationId` 说两句，第二句不重复名字，看它还记不记得：

```bash
curl -s http://localhost:8082/chat \
  -H 'Content-Type: application/json' \
  -d '{"conversationId":"week1","question":"我叫小林，正在学 Spring AI"}'

curl -s http://localhost:8082/chat \
  -H 'Content-Type: application/json' \
  -d '{"conversationId":"week1","question":"我叫什么？"}'
```

流式（`-N` 让 curl 边收边打印）：

```bash
curl -N "http://localhost:8082/chat/stream?conversationId=week1&q=用三句话复述你记得的事"
```

记忆放在进程内存里，重启就没了。`ChatMemory.CONVERSATION_ID` 每次都要传，换一个 id 就是新会话。窗口长度在 `ChatConfig` 里是 12 条。

### 03 工具

```bash
mvn -pl 03-tools spring-boot:run
```

建议按这个顺序问，方便对照三个方法：

- 现在几点
- 12 乘 7 等于多少
- 订单 A1001 发货了吗

```bash
curl -s http://localhost:8083/chat \
  -H 'Content-Type: application/json' \
  -d '{"question":"订单 A1002 付款了吗？"}'
```

工具类在 `dev.learn.tools.tool`。描述是中文，因为模型靠描述决定叫不叫。模拟订单只有 `A1001`、`A1002`、`A1003`。计算只接受两个数和 `+ - * /`，这样不需要脚本引擎。

### 04 记忆加 RAG

```bash
mvn -pl 04-memory-rag spring-boot:run
```

样例文档在 `src/main/resources/knowledge/`，里面有一个模型原本不知道的课程代号 `SA-17`。启动不会自动入库，避免没有嵌入接口时进程起不来。

```bash
curl -s -X POST http://localhost:8084/ingest
curl -s http://localhost:8084/ask \
  -H 'Content-Type: application/json' \
  -d '{"question":"课程代号是什么？"}'
```

向量库是 `SimpleVectorStore`，只适合这门课，重启清空。改完 markdown 要重启进程再 `POST /ingest`。同一进程里重复入库会被跳过，避免同一段文字嵌两次。

### 05 简单分流

```bash
mvn -pl 05-agent-workflow spring-boot:run
```

`GET /route` 不调用模型，没有密钥也能看分流结果：

```bash
curl -s --get http://localhost:8085/route --data-urlencode "q=帮我查订单 A1001"
curl -s --get http://localhost:8085/route --data-urlencode "q=课程口令是什么"
curl -s --get http://localhost:8085/route --data-urlencode "q=今天心情不错"
```

规则写死在 `IntentRouter`：出现订单类词走 `DB`，出现口令、课程、RAG 等走 `KNOWLEDGE`，其余 `CHITCHAT`。订单优先。知识分支用关键词在 markdown 里摘段落，不走向量库；向量检索留在 04，避免两个话题叠在一起。

确认分流后再调用模型：

```bash
curl -s http://localhost:8085/chat \
  -H 'Content-Type: application/json' \
  -d '{"conversationId":"week2","question":"课程口令是什么"}'
```

资料里的口令是「橙子」。如果模型没看摘录也能答对，把文档改成别的词再问一次。

## 目录

```text
.
├── README.md
├── NOTES.md
├── .gitignore
├── .env.example
├── pom.xml
├── docs/LLM-BASICS.md
├── docs/AI-AGENTS.md
├── docs/READING-LIST.md
├── docs/LEARNING-PLAN.md
├── 01-chat/
├── 02-stream/
├── 03-tools/
├── 04-memory-rag/
└── 05-agent-workflow/
```

## 常见情况

- **编译成功，一问就失败。** 密钥、根地址或模型名不对。`/chat` 会返回 502，正文里带上游的 401。`/chat/stream` 在还没流出内容时会是 500，消息同样指向 `chat/completions`。先确认变量已经 export 到当前终端。
- **04 入库失败，01 却能聊。** 对话网关没有嵌入模型。换一个提供 embeddings 的地址，或先只读 04 的代码。
- **第二句不记得第一句。** `conversationId` 不一致，或者进程重启过。02 和 05 的记忆都在内存里。
- **模型不调用工具。** 把 `@Tool` 的中文描述写清楚，问题里出现描述里的那些词。这是第 1 周第 4 天的练习。
