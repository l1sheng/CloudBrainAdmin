# 云脑医学管理后台

这是一个面向医院管理场景的前后端分离后台系统。项目当前位于 `admin` 目录下，包含一个 Vue 3 管理端和一个 Spring Boot 管理后端，核心覆盖管理员登录、工作台统计、科室管理、医生管理、角色管理、医生排班管理，以及 AI 辅助排班建议。

> 说明：数据库脚本中预留了更完整的 HIS 领域表，例如患者、挂号、分诊、就诊、病历、检查检验、处方、收费、药房和 AI 调用日志等；当前 admin 前后端已实现的管理功能主要集中在科室、医生、角色、排班和 AI 排班。

## 技术栈

| 层级 | 技术 |
| --- | --- |
| 前端框架 | Vue 3.5、Vite 5 |
| 前端 UI | Element Plus、@element-plus/icons-vue |
| 前端状态 | Pinia |
| 前端网络 | Axios，统一请求/响应拦截 |
| 后端框架 | Spring Boot 3.5.15 |
| 后端语言 | Java 17 |
| ORM | MyBatis-Plus 3.5.7 |
| 数据库 | MySQL，脚本默认库名 `doctor_platform` |
| 认证 | JWT，jjwt 0.12.6 |
| 配置 | `application.properties` + `java-dotenv` 读取 `.env` |
| AI 能力 | DeepSeek Chat Completions API，可配置 base URL、model、API key |
| 构建工具 | npm、Maven Wrapper |

## 目录结构

```text
CloudBrain/
├── admin/
│   ├── frontend/                 # Vue 3 + Vite 前端
│   │   ├── src/
│   │   │   ├── api/              # 前端接口封装
│   │   │   ├── router/           # 路由与路由守卫
│   │   │   ├── stores/           # Pinia 用户状态
│   │   │   ├── styles/           # 全局样式
│   │   │   ├── utils/            # Axios 请求实例
│   │   │   └── views/            # 页面视图
│   │   ├── package.json
│   │   └── vite.config.js
│   └── backen/                   # Spring Boot 后端
│       ├── src/main/java/com/neuCloudBrainMedical/admin/
│       │   ├── config/           # CORS、MyBatis-Plus、AI RestTemplate 配置
│       │   ├── controller/       # REST 接口层
│       │   ├── dto/              # 请求/响应 DTO
│       │   ├── entity/           # MyBatis-Plus 实体
│       │   ├── exception/        # 业务异常与全局异常处理
│       │   ├── mapper/           # MyBatis-Plus Mapper
│       │   ├── security/         # JWT 工具
│       │   ├── service/          # 查询/命令服务
│       │   └── util/             # 统一返回、排班时间段工具
│       ├── src/main/resources/
│       ├── sql/                  # 建表、增量变更、初始化数据脚本
│       └── pom.xml
├── PROJECT_STRUCTURE.md
└── README.md
```

## 前端架构

前端是标准的 Vue 单页应用：

- `main.js` 创建应用，挂载 Pinia、Vue Router、Element Plus，并启用中文语言包。
- `router/index.js` 配置 `/login`、`/dashboard`、`/department`、`/doctor`、`/role`、`/schedule` 路由。
- `stores/user.js` 维护登录 token、用户信息、角色编码和权限判断。
- `utils/request.js` 封装 Axios，请求时自动注入 `Authorization: Bearer <token>`，响应时统一解包后端 `Result`，遇到 401 自动清理登录态并跳转登录页。
- `api/*.js` 按业务模块封装后端接口，页面只调用模块函数，不直接拼 Axios。
- `Layout.vue` 提供顶部栏、侧边菜单和内容区，按角色隐藏部分菜单。

前端角色权限主要由 `roleCode` 推导：

- `ADMIN`、`SUPER_ADMIN` 或包含 `SYSTEM` 的角色视为超级/系统管理员。
- `OUTPATIENT_DOCTOR_ADMIN` 视为门诊医生管理员，前端会隐藏角色管理，并限制科室/排班视图只看门诊相关数据。
- 其他以 `_DOCTOR_ADMIN` 结尾的角色被识别为科室医生管理员。

## 后端架构

后端采用分层架构：

```text
Controller -> Service Interface -> Service Impl -> Mapper -> Entity -> MySQL
        DTO/Result 负责前后端契约，Entity 只作为数据库映射
```

主要设计点：

- Controller 只处理 HTTP 参数、校验注解和统一返回，不写业务逻辑。
- Service 按模块拆分，并大量使用 `I*QueryService` / `I*CommandService` 分离读写接口。
- 跨模块协作通过 DTO 和服务接口完成，例如医生服务通过 `IUserCommandService` 创建关联账号，通过 `IRoleQueryService` 获取角色，通过 `IScheduleQueryService` 检查排班依赖。
- Mapper 基本继承 MyBatis-Plus `BaseMapper`，复杂统计只在少数 Mapper 中补充 SQL。
- 全局响应统一为 `Result<T>`，成功码为 `200`，业务错误由 `BusinessException` 携带业务码。
- `GlobalExceptionHandler` 统一处理业务异常、参数校验异常和未知异常。
- `JwtTokenProvider` 负责生成、校验和解析 JWT。
- `ScheduleTimeSlotUtils` 统一排班时间段、默认时间、默认挂号费，兼容历史中英文时间段值。

## 后端模块

| 模块 | 主要文件 | 职责 |
| --- | --- | --- |
| 认证 | `AuthController`、`AuthCommandServiceImpl`、`AuthQueryServiceImpl` | 登录、JWT 生成、管理员信息查询 |
| 科室 | `DepartmentController`、`Department*ServiceImpl`、`DepartmentResponseAssembler` | 科室列表、树、详情、增删改、状态切换、自动编码 |
| 医生 | `DoctorAdminController`、`Doctor*ServiceImpl` | 医生分页、详情、导出、医生下拉、角色选择、增删改、启停用 |
| 角色 | `SysRoleController`、`Role*ServiceImpl` | 角色列表、详情、创建、更新、删除、启停用 |
| 排班 | `ScheduleController`、`Schedule*ServiceImpl` | 排班列表、详情、挂号记录、创建、批量创建、更新、删除 |
| AI 排班 | `AIScheduleSuggestionController`、`AISuggestionServiceImpl`、`DefaultAISchedulingClient` | 生成排班建议、采纳/拒绝全部建议、采纳/拒绝单条建议 |
| 用户 | `User*ServiceImpl` | 内部账号创建、更新、删除、查询，供医生和认证模块复用 |
| 基础设施 | `config`、`exception`、`security`、`util` | 跨域、分页插件、AI 客户端、异常处理、JWT、统一结果 |

## 已实现功能

### 1. 登录与会话

- 管理员登录。
- 登录成功返回 token、用户 ID、用户名、真实姓名、角色编码、角色名称。
- 前端持久化 token 和用户信息到 `localStorage`。
- 路由守卫保护后台页面，未登录访问会跳回 `/login`。
- 后端 `/api/auth/info` 可根据 token 返回当前管理员信息。
- 请求失效或 token 过期时，前端统一清理登录态。

### 2. 工作台

工作台页面不依赖单独的 Dashboard 后端接口，而是组合已有接口形成概览：

- 统计科室数量、医生数量、今日排班数量、本月排班数量。
- 展示今日排班列表。
- 展示科室概览卡片。
- 提供到排班管理等页面的快捷入口。
- 门诊管理员会在前端按门诊科室/门诊医生做过滤。

### 3. 科室管理

- 科室列表查询，支持名称/编号搜索。
- 科室树形目录，支持多级科室、父子关系展示。
- 科室详情展示，包括编号、类型、楼层、电话、简介、状态、排序等。
- 新增科室。
- 编辑科室。
- 删除科室。
- 启用/禁用科室。
- 通过 `parentId` 支持多级目录。
- 通过 `sortOrder` 控制显示顺序。
- 自动生成科室编号：如果前端未传 `code`，后端会调用 DeepSeek 生成英文医学缩写；AI 不可用时回退到本地生成逻辑。
- 删除科室前会检查是否存在在职医生或排班记录，存在依赖时拒绝删除。
- 科室详情页可展示该科室下医生列表，并支持快速编辑医生基础信息。
- 门诊管理员在前端限制只能看门诊类型科室。

### 4. 医生管理

- 医生分页列表。
- 按科室、姓名/工号关键字、职称、医生类型、状态筛选。
- 医生详情查看。
- 新增医生。
- 编辑医生。
- 删除医生。
- 医生启用/禁用。
- 禁用医生前检查未来排班和待处理挂号；存在关联数据时需要强制确认。
- 医生导出，前端将列表数据整理为 CSV 下载。
- 医生下拉接口，供排班页面按科室选择启用医生。
- 医生角色下拉接口，只返回可用的医生相关角色。
- 新增医生时同步创建 `sys_user` 账号。
- 登录账号可手动指定；未指定时按角色前缀、日期和自增 ID 自动生成。
- 医生工号按 `D + yyyyMMdd + doctorId` 自动生成。
- 默认密码为 `123456`，后端内部使用 `{noop}` 形式兼容明文密码匹配。
- 医生状态变化会同步关联用户账号状态。

### 5. 角色管理

- 角色列表。
- 角色详情。
- 新增角色。
- 编辑角色。
- 删除角色。
- 启用/禁用角色。
- 角色编码唯一性校验。
- 医生模块会复用角色查询服务，为医生账号绑定权限角色。

### 6. 排班管理

- 按科室和日期范围查询排班。
- 前端以周视图展示医生、日期和上午/下午/夜间时段。
- 支持按医生类型、职称、关键字等条件筛选。
- 新增单个医生的一个或多个日期/时段排班。
- 批量创建排班：前端将多个日期和多个时段组合成批量请求。
- 编辑排班时间段、最大接诊量、当前预约数、挂号费和状态。
- 删除排班。
- 查看排班详情。
- 查看某个排班下的挂号记录。
- 自动计算已预约数量：`totalQuota - remainQuota`。
- 自动根据职称设置默认挂号费：主任医师、副主任医师、主治医师、住院医师对应不同默认价格。
- 时间段统一归一为上午、下午、夜间，同时兼容历史英文值。
- 创建和编辑时进行冲突检测：同一医生、同一日期、同一时间段不能重复排班。
- 批量创建时同时检查请求内部重复和数据库已有冲突。
- 已有预约的排班不能直接删除。
- 查询排班时会自动把过期的可预约排班标记为已过期。
- 排班来源支持 `MANUAL` 和 `AI_SUGGESTED`，用于区分人工创建和 AI 采纳生成。

### 7. AI 辅助排班

- 前端可选择科室和日期范围生成 AI 排班建议。
- 后端先获取该科室下启用医生列表。
- AI 客户端调用 DeepSeek `/chat/completions`，要求返回 JSON 数组。
- AI 返回字段包括 doctorId、doctorName、date、timeSlot、maxAppointments、reason。
- 后端解析 AI 结果并写入建议主表和建议明细表。
- AI 不可用、未配置 API key 或返回不可解析时，回退到本地负载均衡算法生成建议。
- 支持采纳全部建议，批量生成真实排班。
- 支持拒绝全部建议。
- 支持逐条采纳或逐条拒绝。
- 采纳建议时复用排班创建服务，因此仍会执行时间段归一、冲突检测、默认挂号费和来源标记。
- 建议状态包括 `PENDING`、`ACCEPTED`、`REJECTED`。

## 接口概览

| 功能 | 方法与路径 |
| --- | --- |
| 登录 | `POST /api/auth/login` |
| 当前管理员信息 | `GET /api/auth/info` |
| 科室列表 | `GET /api/admin/department` |
| 科室树 | `GET /api/admin/department/tree` |
| 科室详情 | `GET /api/admin/department/{id}` |
| 新增科室 | `POST /api/admin/department` |
| 更新科室 | `PUT /api/admin/department/{id}` |
| 切换科室状态 | `POST /api/admin/department/{id}/toggle-status` |
| 删除科室 | `DELETE /api/admin/department/{id}` |
| 医生分页 | `GET /api/admin/doctor/list` |
| 医生详情 | `GET /api/admin/doctor/{id}` |
| 医生导出 | `GET /api/admin/doctor/export` |
| 启用医生下拉 | `GET /api/admin/doctor` 或 `GET /api/admin/doctors` |
| 医生角色下拉 | `GET /api/admin/doctor/roles` |
| 新增医生 | `POST /api/admin/doctor` |
| 更新医生 | `PUT /api/admin/doctor/{id}` |
| 禁用前检查 | `GET /api/admin/doctor/{id}/disable-check` |
| 切换医生状态 | `PATCH /api/admin/doctor/{id}/toggle-status` |
| 删除医生 | `DELETE /api/admin/doctor/{id}` |
| 角色列表 | `GET /api/admin/roles` |
| 角色详情 | `GET /api/admin/roles/{id}` |
| 新增角色 | `POST /api/admin/roles` |
| 更新角色 | `PUT /api/admin/roles/{id}` |
| 删除角色 | `DELETE /api/admin/roles/{id}` |
| 切换角色状态 | `PATCH /api/admin/roles/{id}/toggle-status` |
| 排班列表 | `GET /api/admin/schedules` |
| 排班详情 | `GET /api/admin/schedules/{id}` |
| 排班挂号记录 | `GET /api/admin/schedules/{id}/registrations` |
| 新增排班 | `POST /api/admin/schedules` |
| 批量新增排班 | `POST /api/admin/schedules/batch` |
| 更新排班 | `PUT /api/admin/schedules/{id}` |
| 删除排班 | `DELETE /api/admin/schedules/{id}` |
| 生成 AI 排班建议 | `POST /api/admin/schedules/ai-suggestions` |
| 采纳全部 AI 建议 | `POST /api/admin/schedules/ai-suggestions/{suggestionId}/accept` |
| 拒绝全部 AI 建议 | `POST /api/admin/schedules/ai-suggestions/{suggestionId}/reject` |
| 采纳单条 AI 建议 | `POST /api/admin/schedules/ai-suggestions/{suggestionId}/details/{detailId}/accept` |
| 拒绝单条 AI 建议 | `POST /api/admin/schedules/ai-suggestions/{suggestionId}/details/{detailId}/reject` |

## 数据模型

当前后端实体和 Mapper 直接使用的核心表包括：

- `sys_role`：系统角色。
- `sys_user`：系统用户和管理员/医生登录账号。
- `department`：科室，增量脚本补充了 `parent_id`、`floor`、`phone`、`sort_order`。
- `doctor`：医生档案，关联用户和科室。
- `doctor_schedule`：医生排班，增量脚本补充了 `source`。
- `ai_schedule_suggestion`：AI 排班建议主记录。
- `ai_schedule_suggestion_detail`：AI 排班建议明细。
- `registration`：挂号记录，排班详情和依赖检查会读取。
- `ai_consultation`：AI 问诊记录，目前只有实体和 Mapper，主要由数据库脚本预留。

数据库脚本还定义了完整 HIS 业务链路的表：`patient`、`triage_record`、`fee_order`、`fee_order_item`、`payment_record`、`refund_record`、`outpatient_visit`、`medical_record`、`medical_item`、`exam_lab_order`、`exam_lab_order_item`、`exam_lab_report`、`drug`、`prescription`、`prescription_item`、`pharmacy_dispense`、`pharmacy_return`、`drug_stock_record`、`ai_call_log` 等。这些表是后续患者端、门诊、收费、药房、检查检验模块的扩展基础。

数据库初始化只需要执行两个脚本，先建表再写入初始化数据：

```sql
source admin/backen/sql/newSchema.sql;
source admin/backen/sql/seed.sql;
```

`original.sql`、`change.sql`、`seed-data.sql`、`clean-data.sql`、`change-logs.md` 和 `schema-relationship.md` 属于早期版本、过程变更、清理辅助或设计说明文件，用于追溯开发过程和辅助维护；新环境初始化时不需要执行这些过程文件。

## 配置

后端默认配置在 `admin/backen/src/main/resources/application.properties`：

- 数据库地址：`jdbc:mysql://localhost:3306/doctor_platform`
- 数据库用户：`root`
- 数据库密码：`123456`
- JWT secret：`jwt.secret`
- JWT 过期时间：`jwt.expiration`
- AI base URL：`ai.deepseek.base-url`
- AI model：`ai.deepseek.model`
- AI key：`ai.deepseek.api-key=${DEEPSEEK_API_KEY:}`

`AdminApplication` 会使用 `java-dotenv` 读取 `.env` 并写入系统属性，因此可以在 `.env` 中配置 `DEEPSEEK_API_KEY` 等敏感信息。

`.env` 文件路径：

```text
admin/backen/src/main/resources/.env
```

`.env` 文件内容示例：

```env
DEEPSEEK_API_KEY=你的 DeepSeek API Key
```

当前代码只从 `.env` 中读取 `DEEPSEEK_API_KEY`。该变量会被 `application.properties` 中的 `ai.deepseek.api-key=${DEEPSEEK_API_KEY:}` 引用，用于科室编号自动生成和 AI 排班建议。没有配置该值时，科室编号会使用本地回退生成逻辑，AI 排班建议会回退到本地负载均衡算法。

如果需要调整 AI 服务地址或模型，通常直接修改 `application.properties` 中的：

```properties
ai.deepseek.base-url=https://api.deepseek.com
ai.deepseek.model=deepseek-v4-flash
```

不要把真实 API Key 写进 README、提交说明或公开文档中。

前端 Vite 配置：

- 本地端口：`5173`
- host：`0.0.0.0`
- `/api` 代理到 `http://localhost:8080`

## 启动方式

后端：

```bash
cd admin/backen
./mvnw spring-boot:run
```

Windows PowerShell：

```powershell
cd admin/backen
.\mvnw.cmd spring-boot:run
```

前端：

```bash
cd admin/frontend
npm install
npm run dev
```

访问：

```text
http://localhost:5173
```

## 项目特性

- 前后端分离，前端 `/api` 代理后端接口。
- 统一登录态、统一 token 注入、统一错误处理。
- 后端统一响应格式，便于前端集中解包。
- 后端服务按查询/命令拆分，业务读写职责清晰。
- 模块间通过服务接口和 DTO 协作，避免直接暴露 Entity。
- 科室支持树形结构，适合医院多级科室目录。
- 医生档案与登录账号联动创建和维护。
- 排班支持批量创建、冲突检测、预约数约束、状态管理和来源标记。
- AI 能力具有降级策略，DeepSeek 不可用时仍可生成基础排班建议。
- 数据库脚本覆盖当前 admin 模块和更完整的 HIS 后续扩展模型。
- Element Plus 组件化页面，提供表格、树、弹窗、表单、分页、日期选择、消息确认等交互。

## 注意事项

- 当前没有 Spring Security 过滤器链，认证主要由登录接口、JWT 工具和前端路由守卫支撑；除 `/auth/info` 外，后端控制器本身没有统一鉴权拦截。
- 数据库初始化顺序为 `newSchema.sql` -> `seed.sql`；其它 SQL 和 Markdown 文件主要是早期脚本、过程文件、清理脚本或结构说明，不作为新环境初始化必跑脚本。
- 部分 SQL 脚本文本在当前终端输出中存在编码显示异常，但源码和前端页面中大量中文仍按业务语义使用。
- `docs` 目录当前只保留 `backend-code-review.md`，README 以当前源码、配置和 SQL 为准。
