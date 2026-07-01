# 云脑医学管理后台 - 项目结构说明

本文档根据当前前后端源码整理，只说明 Git 跟踪的项目文件；`.env`、`node_modules/`、`dist/`、`target/`、`.vscode/` 等被 `.gitignore` 忽略的本地配置、依赖和构建产物不在本文档中展开。

## 项目总览

```text
CloudBrain/
├── .gitignore                  # Git 忽略规则，排除本地环境、依赖、构建产物、日志等文件
├── PROJECT_STRUCTURE.md        # 当前项目结构说明文档
├── README.md                   # 项目功能、技术栈、运行方式和接口概览说明
└── admin/                      # 医院管理后台主目录
    ├── backen/                 # 后端 Spring Boot 管理服务
    └── frontend/               # 前端 Vue 3 管理端单页应用
```

## 后端结构

后端位于 `admin/backen`，技术栈为 Spring Boot 3.5.15、Java 17、MyBatis-Plus、MySQL、JWT。代码按 `controller -> service -> mapper -> entity` 分层，DTO 负责前后端数据契约，`Result` 负责统一响应格式。

```text
admin/backen/
├── .gitattributes                                      # Git 属性配置，控制仓库中文件换行符等行为
├── HELP.md                                             # Spring Initializr 生成的项目帮助文档，记录 Maven、Spring Boot 参考资料
├── pom.xml                                             # Maven 项目配置，声明 Spring Boot、MyBatis-Plus、JWT、MySQL、dotenv、测试依赖
├── mvnw                                                # Linux/macOS Maven Wrapper 启动脚本，无需本机预装 Maven 即可执行 Maven 命令
├── mvnw.cmd                                            # Windows Maven Wrapper 启动脚本
├── docs/
│   └── backend-code-review.md                          # 后端代码审查记录，说明曾经发现的问题、风险和改进建议
├── sql/
│   ├── newSchema.sql                                   # 当前推荐建表脚本，新环境初始化时优先执行
│   ├── seed.sql                                        # 当前推荐初始化数据脚本，写入角色、用户、科室、医生、排班等基础数据
│   ├── original.sql                                    # 早期基础数据库脚本，用于追溯历史结构
│   ├── change.sql                                      # 过程增量变更脚本，记录开发中追加字段和表结构调整
│   ├── seed-data.sql                                   # 早期种子数据脚本，保留历史初始化数据
│   ├── clean-data.sql                                  # 数据清理辅助脚本，用于清理开发或测试数据
│   ├── change-logs.md                                  # 数据库变更日志，解释表结构演进过程
│   └── schema-relationship.md                          # 数据库表关系说明，描述 HIS 领域表之间的关联
└── src/
    ├── main/
    │   ├── java/com/neuCloudBrainMedical/admin/
    │   │   ├── AdminApplication.java                   # Spring Boot 启动类，启用 Mapper 扫描，并加载本地 dotenv 环境变量
    │   │   ├── config/
    │   │   │   ├── AiClientConfig.java                 # AI 客户端配置，提供用于调用 DeepSeek 接口的 RestTemplate Bean
    │   │   │   ├── CorsConfig.java                     # 跨域配置，允许前端开发环境访问后端接口
    │   │   │   └── MyBatisPlusConfig.java              # MyBatis-Plus 配置，注册分页插件等数据库访问增强能力
    │   │   ├── controller/
    │   │   │   ├── auth/
    │   │   │   │   └── AuthController.java             # 认证接口控制器，提供管理员登录和当前管理员信息查询接口
    │   │   │   ├── department/
    │   │   │   │   └── DepartmentController.java       # 科室管理控制器，提供科室列表、树、详情、新增、编辑、启停用、删除接口
    │   │   │   ├── doctor/
    │   │   │   │   └── DoctorAdminController.java      # 医生管理控制器，提供医生分页、详情、导出、下拉、角色选项、新增、编辑、启停用、删除接口
    │   │   │   ├── role/
    │   │   │   │   └── SysRoleController.java          # 角色管理控制器，提供角色列表、详情、新增、编辑、启停用、删除接口
    │   │   │   └── schedule/
    │   │   │       ├── AIScheduleSuggestionController.java # AI 排班建议控制器，提供生成建议、采纳/拒绝全部建议、采纳/拒绝单条建议接口
    │   │   │       └── ScheduleController.java         # 排班管理控制器，提供排班列表、详情、挂号记录、新增、批量新增、更新、删除接口
    │   │   ├── dto/
    │   │   │   ├── PageResponse.java                   # 通用分页响应 DTO，封装列表数据、总数、页码和页大小
    │   │   │   ├── auth/
    │   │   │   │   ├── AdminInfoResponse.java          # 当前管理员信息响应 DTO，返回用户 ID、用户名、真实姓名、角色编码和角色名称
    │   │   │   │   ├── LoginRequest.java               # 登录请求 DTO，承载用户名和密码
    │   │   │   │   └── LoginResponse.java              # 登录响应 DTO，承载 JWT token 和管理员基础信息
    │   │   │   ├── department/
    │   │   │   │   ├── DepartmentCreateRequest.java    # 新增科室请求 DTO，承载名称、编码、父级、类型、楼层、电话、简介、排序等字段
    │   │   │   │   ├── DepartmentOverviewBrief.java    # 科室概览 DTO，用于前端概览或轻量列表展示
    │   │   │   │   ├── DepartmentResponse.java         # 科室详情/列表响应 DTO，返回科室完整展示字段
    │   │   │   │   ├── DepartmentTreeNode.java         # 科室树节点 DTO，承载父子层级和 children 列表
    │   │   │   │   └── DepartmentUpdateRequest.java    # 更新科室请求 DTO，承载可修改的科室字段
    │   │   │   ├── doctor/
    │   │   │   │   ├── DoctorCreateRequest.java        # 新增医生请求 DTO，承载医生档案、登录账号、角色和科室绑定信息
    │   │   │   │   ├── DoctorDisableCheckResponse.java # 医生禁用检查响应 DTO，返回是否存在未来排班、待处理挂号等依赖
    │   │   │   │   ├── DoctorInfo.java                 # 医生内部信息 DTO，用于服务层跨模块传递医生基础信息
    │   │   │   │   ├── DoctorOptionDTO.java            # 医生下拉选项 DTO，用于排班等页面选择启用医生
    │   │   │   │   ├── DoctorResponse.java             # 医生列表/详情响应 DTO，返回医生、科室、用户账号、角色等展示字段
    │   │   │   │   ├── DoctorRoleOption.java           # 医生角色下拉 DTO，用于新增或编辑医生时选择可用角色
    │   │   │   │   └── DoctorUpdateRequest.java        # 更新医生请求 DTO，承载医生档案、账号、角色、状态等可编辑字段
    │   │   │   ├── role/
    │   │   │   │   ├── RoleInfo.java                   # 角色内部信息 DTO，供用户、医生等模块查询角色基础信息
    │   │   │   │   ├── SysRoleRequest.java             # 角色新增/编辑请求 DTO，承载角色编码、名称、描述和状态
    │   │   │   │   └── SysRoleResponse.java            # 角色响应 DTO，返回角色管理页面需要展示的字段
    │   │   │   ├── schedule/
    │   │   │   │   ├── AIScheduleSuggestRequest.java   # AI 排班建议请求 DTO，承载科室、日期范围等生成建议所需参数
    │   │   │   │   ├── AIScheduleSuggestionResponse.java # AI 排班建议响应 DTO，返回建议主记录、状态和建议明细
    │   │   │   │   ├── ScheduleBatchCreateRequest.java # 批量创建排班请求 DTO，承载医生、日期集合、时间段集合、号源和费用等信息
    │   │   │   │   ├── ScheduleCreateRequest.java      # 创建单个排班请求 DTO，承载医生、日期、时间段、号源、费用、来源等字段
    │   │   │   │   ├── ScheduleRegistrationResponse.java # 排班挂号记录响应 DTO，展示某个排班下的挂号患者和状态信息
    │   │   │   │   ├── ScheduleResponse.java           # 排班列表/详情响应 DTO，返回医生、科室、日期、时间段、号源、费用、状态等展示字段
    │   │   │   │   ├── ScheduleUpdateRequest.java      # 更新排班请求 DTO，承载可修改的时间段、号源、费用和状态字段
    │   │   │   │   └── SuggestionDetailResponse.java   # AI 建议明细响应 DTO，返回建议中的医生、日期、时间段、号源、理由和处理状态
    │   │   │   └── user/
    │   │   │       ├── UserCreateRequest.java          # 内部用户创建请求 DTO，供医生模块同步创建登录账号
    │   │   │       ├── UserInfo.java                   # 内部用户信息 DTO，供认证和业务模块传递用户基础资料
    │   │   │       └── UserUpdateRequest.java          # 内部用户更新请求 DTO，供医生模块同步维护账号资料和状态
    │   │   ├── entity/
    │   │   │   ├── Registration.java                   # 挂号记录实体，映射 registration 表，用于排班预约数、禁用检查和挂号明细查询
    │   │   │   ├── SysRole.java                        # 系统角色实体，映射 sys_role 表
    │   │   │   ├── SysUser.java                        # 系统用户实体，映射 sys_user 表，保存管理员和医生登录账号
    │   │   │   ├── dashboard/
    │   │   │   │   └── AiConsultation.java             # AI 问诊记录实体，映射 ai_consultation 表，当前作为预留业务实体
    │   │   │   ├── department/
    │   │   │   │   └── Department.java                 # 科室实体，映射 department 表，支持父子科室、类型、排序和启停用
    │   │   │   ├── doctor/
    │   │   │   │   └── Doctor.java                     # 医生实体，映射 doctor 表，关联科室和用户账号
    │   │   │   └── schedule/
    │   │   │       ├── AiScheduleSuggestion.java       # AI 排班建议主实体，映射 ai_schedule_suggestion 表
    │   │   │       ├── AiScheduleSuggestionDetail.java # AI 排班建议明细实体，映射 ai_schedule_suggestion_detail 表
    │   │   │       └── DoctorSchedule.java             # 医生排班实体，映射 doctor_schedule 表，保存日期、时间段、号源、费用、状态和来源
    │   │   ├── exception/
    │   │   │   ├── AIServiceException.java             # AI 服务异常，表示 AI 调用、解析或业务处理失败
    │   │   │   ├── BusinessException.java              # 通用业务异常，携带业务错误码和错误信息
    │   │   │   └── GlobalExceptionHandler.java         # 全局异常处理器，统一处理业务异常、参数校验异常和未知异常并返回 Result
    │   │   ├── mapper/
    │   │   │   ├── RegistrationMapper.java             # 挂号记录 Mapper，提供 registration 表基础 CRUD
    │   │   │   ├── SysRoleMapper.java                  # 系统角色 Mapper，提供 sys_role 表基础 CRUD
    │   │   │   ├── SysUserMapper.java                  # 系统用户 Mapper，提供 sys_user 表基础 CRUD
    │   │   │   ├── dashboard/
    │   │   │   │   └── AiConsultationMapper.java       # AI 问诊 Mapper，提供 ai_consultation 表基础 CRUD 和后续扩展入口
    │   │   │   ├── department/
    │   │   │   │   └── DepartmentMapper.java           # 科室 Mapper，提供 department 表基础 CRUD
    │   │   │   ├── doctor/
    │   │   │   │   └── DoctorMapper.java               # 医生 Mapper，提供 doctor 表基础 CRUD
    │   │   │   └── schedule/
    │   │   │       ├── AiScheduleSuggestionDetailMapper.java # AI 建议明细 Mapper，提供建议明细表基础 CRUD
    │   │   │       ├── AiScheduleSuggestionMapper.java # AI 建议主记录 Mapper，提供建议主表基础 CRUD
    │   │   │       └── ScheduleMapper.java             # 排班 Mapper，提供 doctor_schedule 表 CRUD，并补充排班关联查询
    │   │   ├── security/
    │   │   │   └── JwtTokenProvider.java               # JWT 工具类，负责生成、校验、解析 token 并提取用户身份信息
    │   │   ├── service/
    │   │   │   ├── auth/
    │   │   │   │   ├── IAuthCommandService.java        # 认证命令服务接口，定义登录等会改变会话状态的操作
    │   │   │   │   ├── IAuthQueryService.java          # 认证查询服务接口，定义当前管理员信息查询
    │   │   │   │   └── impl/
    │   │   │   │       ├── AuthCommandServiceImpl.java # 认证命令服务实现，校验账号密码、用户状态并生成 JWT
    │   │   │   │       └── AuthQueryServiceImpl.java   # 认证查询服务实现，根据 token 用户信息组装管理员资料
    │   │   │   ├── department/
    │   │   │   │   ├── IDepartmentCommandService.java  # 科室命令服务接口，定义新增、更新、启停用、删除科室
    │   │   │   │   ├── IDepartmentQueryService.java    # 科室查询服务接口，定义列表、树、详情、存在性等查询能力
    │   │   │   │   ├── IDeptCodeGenerator.java         # 科室编号生成接口，抽象 AI 生成和本地回退生成策略
    │   │   │   │   ├── converter/
    │   │   │   │   │   └── DepartmentResponseAssembler.java # 科室响应装配器，将 Department 实体转换为列表、详情和树节点 DTO
    │   │   │   │   └── impl/
    │   │   │   │       ├── DepartmentCommandServiceImpl.java # 科室命令实现，处理新增、更新、启停用、删除、依赖检查和编号生成
    │   │   │   │       ├── DepartmentQueryServiceImpl.java # 科室查询实现，处理筛选、树构建、详情查询和状态过滤
    │   │   │   │       └── DeptCodeGeneratorImpl.java  # 科室编号生成实现，优先调用 DeepSeek，失败时使用本地规则生成编号
    │   │   │   ├── doctor/
    │   │   │   │   ├── IDoctorCommandService.java      # 医生命令服务接口，定义新增、更新、启停用、删除医生
    │   │   │   │   ├── IDoctorQueryService.java        # 医生查询服务接口，定义分页、详情、导出、下拉、禁用检查等查询
    │   │   │   │   └── impl/
    │   │   │   │       ├── DoctorCommandServiceImpl.java # 医生命令实现，维护医生档案并同步创建/更新/禁用关联用户账号
    │   │   │   │       └── DoctorQueryServiceImpl.java # 医生查询实现，处理分页筛选、详情组装、医生下拉、角色选项和导出数据
    │   │   │   ├── role/
    │   │   │   │   ├── IRoleCommandService.java        # 角色命令服务接口，定义新增、更新、删除、启停用角色
    │   │   │   │   ├── IRoleQueryService.java          # 角色查询服务接口，定义角色列表、详情、医生角色选项和唯一性查询
    │   │   │   │   └── impl/
    │   │   │   │       ├── RoleCommandServiceImpl.java # 角色命令实现，处理角色编码唯一性校验、状态切换和删除约束
    │   │   │   │       └── RoleQueryServiceImpl.java   # 角色查询实现，提供角色列表、详情和医生相关角色选项
    │   │   │   ├── schedule/
    │   │   │   │   ├── IAISchedulingClient.java        # AI 排班客户端接口，抽象外部 AI 生成排班建议的能力
    │   │   │   │   ├── IAISuggestionCommandService.java # AI 建议命令服务接口，定义生成、采纳、拒绝建议等写操作
    │   │   │   │   ├── IAISuggestionQueryService.java  # AI 建议查询服务接口，定义建议主记录和明细查询
    │   │   │   │   ├── IScheduleCommandService.java    # 排班命令服务接口，定义创建、批量创建、更新、删除排班
    │   │   │   │   ├── IScheduleQueryService.java      # 排班查询服务接口，定义列表、详情、挂号记录、冲突检查等查询
    │   │   │   │   ├── converter/
    │   │   │   │   │   └── ScheduleResponseAssembler.java # 排班响应装配器，将排班实体和关联医生/科室信息转换为响应 DTO
    │   │   │   │   └── impl/
    │   │   │   │       ├── AISuggestionQueryServiceImpl.java # AI 建议查询实现，组装建议主记录和明细响应
    │   │   │   │       ├── AISuggestionServiceImpl.java # AI 建议命令实现，调用 AI 或本地回退算法生成建议，并处理采纳/拒绝流程
    │   │   │   │       ├── DefaultAISchedulingClient.java # DeepSeek AI 客户端实现，发送排班提示词、解析 JSON 结果并做异常包装
    │   │   │   │       ├── ScheduleCommandServiceImpl.java # 排班命令实现，处理单个/批量创建、更新、取消、冲突校验和预约数约束
    │   │   │   │       └── ScheduleQueryServiceImpl.java # 排班查询实现，处理日期范围、科室、医生筛选、详情、挂号记录和过期状态更新
    │   │   │   └── user/
    │   │   │       ├── IUserCommandService.java         # 用户命令服务接口，定义创建、更新、删除和状态维护内部用户账号
    │   │   │       ├── IUserQueryService.java           # 用户查询服务接口，定义按用户名、手机号、邮箱、ID 查询和占用检查
    │   │   │       └── impl/
    │   │   │           ├── UserCommandServiceImpl.java  # 用户命令实现，供医生模块联动维护 sys_user 登录账号
    │   │   │           └── UserQueryServiceImpl.java    # 用户查询实现，供认证、医生、角色等模块复用用户信息和唯一性校验
    │   │   └── util/
    │   │       ├── Result.java                          # 统一响应包装类，封装 code、message、data 以及成功/失败快捷方法
    │   │       └── ScheduleTimeSlotUtils.java           # 排班时间段工具，统一上午/下午/夜间时间段、兼容历史值并计算默认挂号费
    │   └── resources/
    │       └── application.properties                   # 后端应用配置，包含应用名、MySQL 连接、MyBatis-Plus、JWT 和 DeepSeek 配置
    └── test/
        └── java/com/neuCloudBrainMedical/admin/
            └── AdminApplicationTests.java               # Spring Boot 上下文加载测试，验证应用基础配置可启动
```

## 前端结构

前端位于 `admin/frontend`，技术栈为 Vue 3、Vite、Element Plus、Pinia、Vue Router、Axios。页面通过 `utils/request.js` 统一访问后端 `/api`，Vite 开发代理会把 `/api` 转发到 `http://localhost:8080`。

```text
admin/frontend/
├── index.html                                           # Vite 应用 HTML 入口，提供根挂载节点
├── package.json                                         # 前端包配置，声明 dev/build/preview 脚本以及 Vue、Vite、Element Plus、Pinia、Axios 依赖
├── package-lock.json                                    # npm 依赖锁定文件，固定依赖版本以保证安装结果一致
├── vite.config.js                                       # Vite 配置，启用 Vue 插件、配置 @ 别名、开发端口和 /api 代理
└── src/
    ├── App.vue                                          # Vue 根组件，承载路由出口，是所有页面的顶层容器
    ├── main.js                                          # 前端入口文件，创建 Vue 应用并注册 Pinia、Router、Element Plus 中文包和全局样式
    ├── api/
    │   ├── auth.js                                      # 登录认证接口封装，调用登录和当前管理员信息接口
    │   ├── department.js                                # 科室接口封装，调用列表、树、详情、新增、编辑、启停用、删除接口
    │   ├── doctor.js                                    # 医生接口封装，调用分页、详情、导出、下拉、角色、新增、编辑、启停用、删除接口
    │   ├── role.js                                      # 角色接口封装，调用列表、详情、新增、编辑、启停用、删除接口
    │   └── schedule.js                                  # 排班和 AI 排班接口封装，调用排班 CRUD、挂号记录、AI 建议生成和采纳/拒绝接口
    ├── router/
    │   └── index.js                                     # Vue Router 配置，定义登录、布局、工作台、科室、医生、角色、排班路由和登录/角色守卫
    ├── stores/
    │   └── user.js                                      # Pinia 用户状态仓库，维护 token、用户信息、角色判断、登录、拉取信息和退出登录
    ├── styles/
    │   └── global.css                                   # 全局样式，统一页面背景、基础排版、滚动条和 Element Plus 细节样式
    ├── utils/
    │   └── request.js                                   # Axios 实例封装，配置 baseURL、token 注入、Result 解包、错误提示和 401 登录失效处理
    └── views/
        ├── Dashboard.vue                                # 工作台页面，汇总科室、医生、今日排班、本月排班和快捷入口
        ├── Layout.vue                                   # 后台主布局，提供侧边菜单、顶部用户信息、退出登录和子路由内容区
        ├── Login.vue                                    # 登录页面，提供账号密码表单、校验、登录提交和登录后跳转
        └── admin/
            ├── Department.vue                           # 科室管理页面，提供科室列表/树、搜索、新增、编辑、详情、启停用、删除和科室医生查看
            ├── Doctor.vue                               # 医生管理页面，提供筛选、分页、新增、编辑、导出、禁用检查、启停用和删除
            ├── Role.vue                                 # 角色管理页面，提供角色列表、新增、编辑、详情、启停用和删除
            └── Schedule.vue                             # 排班管理页面，提供周视图、医生筛选、单个/批量排班、编辑、取消、挂号记录和 AI 排班建议处理
```

## 关键分层关系

```text
前端页面 views/
  -> 前端接口 api/*.js
  -> Axios 封装 utils/request.js
  -> Vite /api 代理
  -> 后端 controller/*
  -> service 接口与实现
  -> mapper
  -> entity
  -> MySQL
```

## 运行入口

后端入口：

```powershell
cd admin/backen
.\mvnw.cmd spring-boot:run
```

前端入口：

```powershell
cd admin/frontend
npm install
npm run dev
```

默认访问地址：

```text
http://localhost:5173
```
