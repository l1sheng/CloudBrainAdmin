# 云脑医学管理后台 - 项目结构说明

本文档用于说明当前仓库的前后端目录结构、模块分层、配置文件和数据库脚本用途。更完整的功能说明请参考根目录 `README.md`。

## 项目概览

当前项目是一个前后端分离的医院管理后台：

- 前端：`admin/frontend`，Vue 3 + Vite + Element Plus。
- 后端：`admin/backen`，Spring Boot 3.5.15 + Java 17 + MyBatis-Plus。
- 数据库：MySQL，默认库名 `doctor_platform`。
- 认证：JWT。
- AI：DeepSeek Chat Completions API，用于科室编号生成和 AI 排班建议。

当前 admin 后台已实现的核心模块包括：登录认证、工作台、科室管理、医生管理、角色管理、排班管理、AI 辅助排班。

## 根目录结构

```text
CloudBrain/
├── admin/
│   ├── backen/                  # 后端 Spring Boot 项目
│   └── frontend/                # 前端 Vue 3 项目
├── PROJECT_STRUCTURE.md         # 项目结构说明
├── README.md                    # 项目完整说明
└── .gitignore
```

## 前端结构

```text
admin/frontend/
├── index.html
├── package.json
├── package-lock.json
├── vite.config.js
└── src/
    ├── App.vue
    ├── main.js
    ├── api/
    │   ├── auth.js              # 登录与管理员信息接口
    │   ├── department.js        # 科室接口
    │   ├── doctor.js            # 医生接口
    │   ├── role.js              # 角色接口
    │   └── schedule.js          # 排班与 AI 排班接口
    ├── router/
    │   └── index.js             # 路由、登录守卫、角色访问控制
    ├── stores/
    │   └── user.js              # Pinia 用户状态、token、角色判断
    ├── styles/
    │   └── global.css           # 全局样式
    ├── utils/
    │   └── request.js           # Axios 实例、token 注入、统一错误处理
    └── views/
        ├── Dashboard.vue        # 工作台
        ├── Layout.vue           # 后台主布局
        ├── Login.vue            # 登录页
        └── admin/
            ├── Department.vue   # 科室管理
            ├── Doctor.vue       # 医生管理
            ├── Role.vue         # 角色管理
            └── Schedule.vue     # 排班管理与 AI 排班
```

### 前端模块说明

| 目录/文件 | 职责 |
| --- | --- |
| `main.js` | 创建 Vue 应用，挂载 Pinia、Router、Element Plus 中文包 |
| `router/index.js` | 定义页面路由，检查登录 token，限制门诊管理员访问角色管理 |
| `stores/user.js` | 管理 `admin_token`、`admin_user`，提供 `isSuperAdmin`、`isClinicAdmin` 等角色判断 |
| `utils/request.js` | 统一 Axios baseURL `/api`，注入 `Authorization`，解包后端 `Result` |
| `api/` | 按业务模块封装 HTTP 调用 |
| `views/` | 页面视图，使用 Element Plus 表格、表单、弹窗、树、日期控件等 |

### 前端路由

| 路径 | 页面 | 说明 |
| --- | --- | --- |
| `/login` | `Login.vue` | 管理员登录 |
| `/dashboard` | `Dashboard.vue` | 工作台概览 |
| `/department` | `Department.vue` | 科室管理 |
| `/doctor` | `Doctor.vue` | 医生管理 |
| `/role` | `Role.vue` | 角色管理 |
| `/schedule` | `Schedule.vue` | 排班管理 |

## 后端结构

```text
admin/backen/
├── HELP.md
├── pom.xml
├── mvnw
├── mvnw.cmd
├── docs/
│   └── backend-code-review.md
├── sql/
│   ├── newSchema.sql            # 推荐建表脚本（新环境必跑）
│   ├── seed.sql                 # 推荐初始化数据脚本（新环境必跑）
│   ├── original.sql             # 早期基础脚本（过程文件）
│   ├── change.sql               # 过程增量脚本（过程文件）
│   ├── seed-data.sql            # 早期种子数据脚本（过程文件）
│   ├── clean-data.sql           # 清理辅助脚本（过程文件）
│   ├── change-logs.md           # 数据库变更记录（辅助文档）
│   └── schema-relationship.md   # 表关系说明（辅助文档）
└── src/
    ├── main/
    │   ├── java/com/neuCloudBrainMedical/admin/
    │   │   ├── AdminApplication.java
    │   │   ├── config/
    │   │   ├── controller/
    │   │   ├── dto/
    │   │   ├── entity/
    │   │   ├── exception/
    │   │   ├── mapper/
    │   │   ├── security/
    │   │   ├── service/
    │   │   └── util/
    │   └── resources/
    │       ├── .env
    │       └── application.properties
    └── test/
        └── java/com/neuCloudBrainMedical/admin/
```

## 后端分层

```text
Controller -> Service Interface -> Service Impl -> Mapper -> Entity -> MySQL
       DTO/Result 负责前后端契约，Entity 只负责数据库映射
```

| 层 | 目录 | 职责 |
| --- | --- | --- |
| 启动层 | `AdminApplication.java` | Spring Boot 启动、读取 `.env`、Mapper 扫描 |
| 配置层 | `config/` | AI RestTemplate、CORS、MyBatis-Plus 分页插件 |
| 控制层 | `controller/` | 暴露 REST 接口，处理 HTTP 参数和统一返回 |
| DTO 层 | `dto/` | 请求、响应、跨模块传输对象 |
| 实体层 | `entity/` | MyBatis-Plus 数据库表映射 |
| Mapper 层 | `mapper/` | 数据访问，继承 MyBatis-Plus `BaseMapper` |
| 服务层 | `service/` | 业务逻辑，按 Query/Command 拆分读写职责 |
| 异常层 | `exception/` | 业务异常、AI 异常、全局异常处理 |
| 安全层 | `security/` | JWT 生成、校验、解析 |
| 工具层 | `util/` | 统一响应、排班时间段和默认挂号费工具 |

## 后端模块

### 认证模块

```text
controller/auth/AuthController.java
service/auth/IAuthCommandService.java
service/auth/IAuthQueryService.java
service/auth/impl/AuthCommandServiceImpl.java
service/auth/impl/AuthQueryServiceImpl.java
dto/auth/
security/JwtTokenProvider.java
```

职责：管理员登录、JWT 生成、当前管理员信息查询、用户启用状态校验。

### 科室模块

```text
controller/department/DepartmentController.java
service/department/
service/department/converter/DepartmentResponseAssembler.java
entity/department/Department.java
mapper/department/DepartmentMapper.java
dto/department/
```

职责：科室列表、树形目录、详情、新增、编辑、删除、启停用、父子科室、排序、科室编号自动生成。

### 医生模块

```text
controller/doctor/DoctorAdminController.java
service/doctor/
entity/doctor/Doctor.java
mapper/doctor/DoctorMapper.java
dto/doctor/
```

职责：医生分页、筛选、详情、导出、新增、编辑、删除、启停用、禁用前检查、医生角色下拉、排班医生下拉。新增医生时会联动创建 `sys_user` 登录账号。

### 角色模块

```text
controller/role/SysRoleController.java
service/role/
entity/SysRole.java
mapper/SysRoleMapper.java
dto/role/
```

职责：角色列表、详情、新增、编辑、删除、启停用、角色编码唯一性校验。

### 排班模块

```text
controller/schedule/ScheduleController.java
service/schedule/
service/schedule/impl/ScheduleCommandServiceImpl.java
service/schedule/impl/ScheduleQueryServiceImpl.java
entity/schedule/DoctorSchedule.java
mapper/schedule/ScheduleMapper.java
dto/schedule/
util/ScheduleTimeSlotUtils.java
```

职责：排班列表、详情、挂号记录、创建、批量创建、更新、删除、冲突检测、预约数约束、过期状态更新、默认时间段和默认挂号费。

### AI 排班模块

```text
controller/schedule/AIScheduleSuggestionController.java
service/schedule/IAISchedulingClient.java
service/schedule/IAISuggestionCommandService.java
service/schedule/IAISuggestionQueryService.java
service/schedule/impl/AISuggestionServiceImpl.java
service/schedule/impl/AISuggestionQueryServiceImpl.java
service/schedule/impl/DefaultAISchedulingClient.java
entity/schedule/AiScheduleSuggestion.java
entity/schedule/AiScheduleSuggestionDetail.java
mapper/schedule/AiScheduleSuggestionMapper.java
mapper/schedule/AiScheduleSuggestionDetailMapper.java
```

职责：调用 DeepSeek 生成排班建议，保存建议主记录和明细，支持采纳/拒绝全部建议，支持逐条采纳/拒绝。AI 不可用时回退到本地负载均衡排班建议。

### 用户内部模块

```text
service/user/
entity/SysUser.java
mapper/SysUserMapper.java
dto/user/
```

职责：内部用户账号创建、更新、删除、查询、用户名/手机号/邮箱占用检查。该模块主要供认证模块和医生模块复用，当前没有单独的前端用户管理页面。

## 数据库脚本

数据库初始化只需要执行两个脚本，先建表再写入初始化数据：

```sql
source admin/backen/sql/newSchema.sql;
source admin/backen/sql/seed.sql;
```

`original.sql`、`change.sql`、`seed-data.sql`、`clean-data.sql`、`change-logs.md` 和 `schema-relationship.md` 属于早期版本、过程变更、清理辅助或设计说明文件，用于追溯开发过程和辅助维护；新环境初始化时不需要执行这些过程文件。

## 环境配置

### application.properties

路径：

```text
admin/backen/src/main/resources/application.properties
```

主要配置：

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/doctor_platform?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=123456

jwt.secret=doctor-platform-development-secret-key-with-at-least-32-bytes
jwt.expiration=86400000

ai.deepseek.base-url=https://api.deepseek.com
ai.deepseek.model=deepseek-v4-flash
ai.deepseek.api-key=${DEEPSEEK_API_KEY:}
```

### .env

路径：

```text
admin/backen/src/main/resources/.env
```

内容示例：

```env
DEEPSEEK_API_KEY=你的 DeepSeek API Key
```

当前代码只从 `.env` 读取 `DEEPSEEK_API_KEY`。该变量用于科室编号自动生成和 AI 排班建议；未配置时，科室编号会使用本地回退逻辑，AI 排班建议会使用本地负载均衡回退算法。

不要把真实 API Key 写入 README、PROJECT_STRUCTURE、提交说明或公开文档。

## 运行方式

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

前端默认访问地址：

```text
http://localhost:5173
```

Vite 会把 `/api` 代理到：

```text
http://localhost:8080
```

## 当前结构特点

- 前后端分离，前端通过 `/api` 代理访问后端。
- 后端按模块组织 Controller、DTO、Entity、Mapper、Service。
- Service 读写接口拆分为 `I*QueryService` 和 `I*CommandService`。
- 跨模块协作使用 DTO 和 Service 接口，避免直接暴露 Entity。
- 科室、医生、角色、排班和 AI 排班是当前 admin 后台主业务。
- 数据库脚本包含当前 admin 模块和后续 HIS 扩展所需的完整领域表。