# 云脑医学管理后台 - 项目结构说明

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端框架 | Spring Boot 3 + Spring Data JPA |
| 前端框架 | Vue 3 + Vite |
| UI 组件库 | Element Plus |
| 数据库 | MySQL |
| 构建工具 | Maven（后端）/ npm（前端） |

---

## 整体结构

```
03/
├── admin/
│   ├── backen/                 # 后端 Spring Boot 项目
│   │   ├── src/main/java/.../admin/
│   │   ├── src/test/java/.../admin/
│   │   ├── pom.xml
│   │   └── .env
│   └── frontend/               # 前端 Vue 3 项目
│       ├── src/
│       ├── package.json
│       ├── vite.config.js
│       └── index.html
```

---

## 后端结构

```
src/main/java/com/neuCloudBrainMedical/admin/
├── config/                              # 全局配置
│   ├── AiClientConfig.java              # AI 服务客户端配置
│   └── CorsConfig.java                  # 跨域配置
│
├── controller/                          # 控制层（按模块分包）
│   ├── auth/
│   │   └── AuthController.java          # 认证接口：登录、管理员信息
│   ├── dashboard/
│   │   └── DashboardController.java     # 仪表盘统计接口
│   ├── department/
│   │   └── DepartmentController.java    # 科室管理接口：CRUD、树形查询
│   ├── doctor/
│   │   └── DoctorAdminController.java   # 医生管理接口：CRUD、批量导入、启停
│   └── schedule/
│       ├── ScheduleController.java      # 排班管理接口：CRUD、批量创建
│       └── AIScheduleSuggestionController.java  # AI 排班建议接口
│
├── dto/                                 # 数据传输对象（按模块分包）
│   ├── auth/
│   │   ├── LoginRequest.java            # 登录请求
│   │   ├── LoginResponse.java           # 登录响应
│   │   └── AdminInfoResponse.java       # 管理员信息响应
│   ├── dashboard/
│   │   └── DashboardStatisticsResponse.java  # 仪表盘统计响应
│   ├── department/
│   │   ├── DepartmentCreateRequest.java  # 创建科室请求
│   │   ├── DepartmentUpdateRequest.java  # 更新科室请求
│   │   ├── DepartmentResponse.java       # 科室响应
│   │   ├── DepartmentDTO.java            # 科室通用 DTO
│   │   ├── DepartmentTreeNode.java       # 科室树节点
│   │   └── DepartmentOverviewBrief.java  # 科室概览摘要
│   ├── doctor/
│   │   ├── DoctorCreateRequest.java      # 创建医生请求
│   │   ├── DoctorUpdateRequest.java      # 更新医生请求
│   │   ├── DoctorResponse.java           # 医生响应
│   │   ├── DoctorOptionDTO.java          # 医生下拉选项
│   │   ├── DoctorInfo.java               # 医生基本信息
│   │   ├── DoctorDisableCheckResponse.java # 禁用前检查响应
│   │   └── BatchImportResult.java        # 批量导入结果
│   ├── schedule/
│   │   ├── ScheduleCreateRequest.java    # 创建排班请求
│   │   ├── ScheduleUpdateRequest.java    # 更新排班请求
│   │   ├── ScheduleBatchCreateRequest.java # 批量创建排班请求
│   │   ├── ScheduleResponse.java         # 排班响应
│   │   ├── AIScheduleSuggestRequest.java # AI 排班建议请求
│   │   ├── AIScheduleSuggestionResponse.java # AI 排班建议响应
│   │   └── SuggestionDetailResponse.java # 排班建议详情响应
│   └── PageResponse.java               # 通用分页响应（共享）
│
├── entity/                              # 实体层（JPA 实体，按模块分包）
│   ├── dashboard/
│   │   └── AiConsultation.java          # AI 问诊记录
│   ├── department/
│   │   └── Department.java              # 科室
│   ├── doctor/
│   │   └── Doctor.java                  # 医生
│   ├── schedule/
│   │   ├── DoctorSchedule.java          # 医生排班
│   │   ├── AiScheduleSuggestion.java    # AI 排班建议
│   │   └── AiScheduleSuggestionDetail.java # AI 排班建议明细
│   ├── Registration.java               # 挂号记录（共享）
│   ├── SysRole.java                     # 系统角色（共享）
│   └── SysUser.java                     # 系统用户（共享）
│
├── repository/                          # 数据访问层（Spring Data JPA，按模块分包）
│   ├── dashboard/
│   │   └── AiConsultationRepository.java
│   ├── department/
│   │   └── DepartmentRepository.java
│   ├── doctor/
│   │   └── DoctorRepository.java
│   ├── schedule/
│   │   ├── ScheduleRepository.java
│   │   ├── AiScheduleSuggestionRepository.java
│   │   └── AiScheduleSuggestionDetailRepository.java
│   ├── RegistrationRepository.java      # 共享
│   ├── SysRoleRepository.java           # 共享
│   └── SysUserRepository.java           # 共享
│
├── service/                             # 业务层（接口与实现分离，按模块分包）
│   ├── auth/
│   │   ├── AuthService.java             # 认证服务接口
│   │   └── impl/
│   │       └── AuthServiceImpl.java     # 认证服务实现
│   ├── dashboard/
│   │   ├── IDashboardStatisticsService.java
│   │   └── impl/
│   │       └── DashboardStatisticsServiceImpl.java
│   ├── department/
│   │   ├── DepartmentService.java       # 科室通用服务接口
│   │   ├── IDepartmentQueryService.java # 科室查询接口
│   │   ├── IDepartmentCommandService.java # 科室命令接口
│   │   └── impl/
│   │       ├── DepartmentServiceImpl.java
│   │       ├── DepartmentQueryServiceImpl.java
│   │       ├── DepartmentCommandServiceImpl.java
│   │       └── DepartmentMapper.java    # MapStruct 映射器
│   ├── doctor/
│   │   ├── IDoctorQueryService.java     # 医生查询接口
│   │   ├── IDoctorCommandService.java   # 医生命令接口
│   │   └── impl/
│   │       ├── DoctorQueryServiceImpl.java
│   │       └── DoctorCommandServiceImpl.java
│   └── schedule/
│       ├── IScheduleQueryService.java   # 排班查询接口
│       ├── IScheduleCommandService.java # 排班命令接口
│       ├── IAIScheduleService.java      # AI 排班服务接口
│       ├── IAISchedulingClient.java     # AI 排班客户端接口
│       └── impl/
│           ├── ScheduleQueryServiceImpl.java
│           ├── ScheduleCommandServiceImpl.java
│           ├── ScheduleMapper.java
│           ├── AIScheduleServiceImpl.java
│           └── DefaultAISchedulingClient.java
│
├── exception/                           # 全局异常处理
│   ├── BusinessException.java           # 业务异常
│   ├── AIServiceException.java          # AI 服务异常
│   └── GlobalExceptionHandler.java      # 全局异常处理器
│
├── security/
│   └── JwtTokenProvider.java           # JWT 令牌工具
│
├── util/
│   ├── Result.java                      # 统一响应结果封装
│   └── ScheduleTimeSlotUtils.java       # 排班时间段工具
│
└── AdminApplication.java                # Spring Boot 启动类
```

---

## 前端结构

```
src/
├── api/                     # API 封装（按模块划分）
│   ├── auth.js              # 认证相关 API
│   ├── department.js        # 科室相关 API
│   ├── doctor.js            # 医生相关 API
│   └── schedule.js          # 排班相关 API
│
├── router/
│   └── index.js             # Vue Router 路由配置
│
├── stores/
│   └── user.js              # Pinia 用户状态管理
│
├── styles/
│   └── global.css           # 全局样式
│
├── utils/
│   └── request.js           # Axios 请求封装（拦截器、Token 注入）
│
├── views/
│   ├── admin/
│   │   ├── Schedule.vue     # 排班管理页面
│   │   ├── Doctor.vue       # 医生管理页面
│   │   └── Department.vue   # 科室管理页面
│   ├── Dashboard.vue        # 仪表盘页面
│   ├── Layout.vue           # 主布局（侧边栏 + 顶部栏）
│   └── Login.vue            # 登录页面
│
├── App.vue                  # 根组件
└── main.js                  # 应用入口
```

---

## 分层架构说明

项目采用经典的分层架构，严格遵循 **SOLID 原则**：

```
Controller → Service 接口 → Service 实现 → Repository → Entity → DB
     ↑                        ↑
   DTO ←────────────────── MapStruct Mapper
```

| 层 | 职责 | 规则 |
|----|------|------|
| **Controller** | 接收 HTTP 请求，参数校验，调用 Service | 不包含业务逻辑，只做协议转换 |
| **DTO** | 定义请求/响应数据结构 | 与 Entity 分离，避免暴露数据库结构 |
| **Service** | 业务逻辑编排 | 接口与实现分离，查询/命令接口隔离 |
| **Repository** | 数据持久化 | 仅定义数据访问方法，不包含业务逻辑 |
| **Entity** | 数据库表映射 | 纯 POJO，不依赖任何业务层 |

### 模块化设计

所有业务模块按 `layer/module` 结构组织，确保高内聚低耦合：

- 每个模块的 Controller、DTO、Entity、Repository、Service 独立成子包
- 跨模块共享的类型（如 `SysUser`、`PageResponse`）放在各层根目录
- 模块间通过 Service 接口通信，不直接依赖具体实现

---

## 测试结构

```
src/test/java/com/neuCloudBrainMedical/admin/
├── AdminApplicationTests.java           # 应用启动测试
├── service/
│   ├── AuthServiceImplTest.java         # 认证服务测试
│   └── DepartmentServiceImplTest.java   # 科室服务测试
├── security/
│   └── JwtTokenProviderTest.java        # JWT 工具测试
└── util/
    └── ResultTest.java                  # 统一响应测试
```

---

## 配置文件

| 文件 | 说明 |
|------|------|
| `backen/pom.xml` | Maven 依赖与构建配置 |
| `backen/.env` | 环境变量（数据库连接等） |
| `frontend/package.json` | npm 依赖与脚本 |
| `frontend/vite.config.js` | Vite 构建配置 |

---

## 启动方式

### 后端
```bash
cd admin/backen
./mvnw spring-boot:run
```

### 前端
```bash
cd admin/frontend
npm install
npm run dev
```