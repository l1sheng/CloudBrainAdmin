# 代码审查报告

审查范围：`admin/backen` 与 `admin/frontend` 当前工作区代码。本文只基于仓库现状判断，不包含未看到的运行时上下文。

## 结论

项目能看出清晰的分层意识，但当前还不算完全满足 SOLID。最大问题集中在权限边界和职责边界：前端路由守卫只检查 token，后端没有统一鉴权拦截器，业务接口基本依赖前端约束；另外，排班查询服务把“读接口”做成了带写副作用的方法，违反查询职责。代码里还存在多处乱码/字符串闭合异常风险，`request.js` 与部分后端类很可能直接影响编译或可用性。

## 主要问题

### 1. 权限越界

1. 前端路由只做了登录态判断，没有做角色级限制。`/role`、`/department`、`/doctor`、`/schedule` 都只要有 token 就能进入。
   - 证据：[admin/frontend/src/router/index.js:62-76](D:/Exercise/ShiXun/CloudBrain/admin/frontend/src/router/index.js#L62)
   - 影响：角色信息虽然存在于 store，但没有用于路由守卫，门诊/非门诊角色的可见范围无法收敛。

2. 后端没有看到统一鉴权过滤器、拦截器或 `@PreAuthorize` 之类的服务端授权控制。
   - 证据：项目中未检出 `OncePerRequestFilter` / `HandlerInterceptor` / `SecurityFilterChain` / `@PreAuthorize`；业务控制器仅接收请求并直接转 service。
   - 证据：[admin/backen/src/main/java/com/neuCloudBrainMedical/admin/controller/department/DepartmentController.java:30-82](D:/Exercise/ShiXun/CloudBrain/admin/backen/src/main/java/com/neuCloudBrainMedical/admin/controller/department/DepartmentController.java#L30)
   - 证据：[admin/backen/src/main/java/com/neuCloudBrainMedical/admin/controller/doctor/DoctorAdminController.java:29-100](D:/Exercise/ShiXun/CloudBrain/admin/backen/src/main/java/com/neuCloudBrainMedical/admin/controller/doctor/DoctorAdminController.java#L29)
   - 证据：[admin/backen/src/main/java/com/neuCloudBrainMedical/admin/controller/role/SysRoleController.java:14-52](D:/Exercise/ShiXun/CloudBrain/admin/backen/src/main/java/com/neuCloudBrainMedical/admin/controller/role/SysRoleController.java#L14)
   - 影响：只要接口暴露出去，服务端没有最后一道门，前端守卫绕过后可直接越权调用。

3. 前端仍然保留 `roleCode` / `roleName`，但没有形成稳定的权限判定入口。
   - 证据：[admin/frontend/src/stores/user.js:24-64](D:/Exercise/ShiXun/CloudBrain/admin/frontend/src/stores/user.js#L24)
   - 影响：权限判断散落到页面级逻辑，容易出现不一致。

### 2. 职责不符 / 开发规范问题

1. `ScheduleQueryServiceImpl.listSchedules()` 是查询方法，却会先执行 `expireOutdatedSchedules()` 更新数据库。
   - 证据：[admin/backen/src/main/java/com/neuCloudBrainMedical/admin/service/schedule/impl/ScheduleQueryServiceImpl.java:62-81,324-337](D:/Exercise/ShiXun/CloudBrain/admin/backen/src/main/java/com/neuCloudBrainMedical/admin/service/schedule/impl/ScheduleQueryServiceImpl.java#L62)
   - 影响：读接口带写副作用，违反 CQRS/查询职责，也让缓存、测试和事务语义变脏。

2. `DepartmentCommandServiceImpl` 把科室写操作、关联医生/排班依赖检查、编号生成都揉在一起，功能不算错，但职责偏厚。
   - 证据：[admin/backen/src/main/java/com/neuCloudBrainMedical/admin/service/department/impl/DepartmentCommandServiceImpl.java:24-112](D:/Exercise/ShiXun/CloudBrain/admin/backen/src/main/java/com/neuCloudBrainMedical/admin/service/department/impl/DepartmentCommandServiceImpl.java#L24)
   - 影响：单一职责边界偏松，后续如果继续加校验或生成策略，会越来越胖。

3. `DoctorCommandServiceImpl` 同时做医生档案、登录账号、角色解析、禁用检查、状态联动，属于典型的“一个类做太多事”。
   - 证据：[admin/backen/src/main/java/com/neuCloudBrainMedical/admin/service/doctor/impl/DoctorCommandServiceImpl.java:23-258](D:/Exercise/ShiXun/CloudBrain/admin/backen/src/main/java/com/neuCloudBrainMedical/admin/service/doctor/impl/DoctorCommandServiceImpl.java#L23)
   - 影响：职责耦合高，改账号规则可能误伤医生规则，改医生规则也会拖到账号逻辑。

4. `Layout.vue` 把全部菜单硬编码进页面，没有依据角色动态裁剪。
   - 证据：[admin/frontend/src/views/Layout.vue:89-98](D:/Exercise/ShiXun/CloudBrain/admin/frontend/src/views/Layout.vue#L89)
   - 影响：菜单职责和权限职责混在一起，后续角色扩展成本高。

5. `Doctor.vue` 中存在不少“前端补后端”的逻辑，例如按 doctorType 再次过滤、角色前缀映射、管理员角色识别，这些都在掩盖后端接口能力不足。
   - 证据：[admin/frontend/src/views/admin/Doctor.vue:319-377, 531-621](D:/Exercise/ShiXun/CloudBrain/admin/frontend/src/views/admin/Doctor.vue#L319)
   - 影响：业务规则分散到视图层，不利于维护。

### 3. 冗余 / 无用代码

1. `Layout.vue` 的菜单列表是固定数组，但当前并没有分角色裁剪，这里的部分角色/菜单控制逻辑可以抽到权限层后再决定是否需要。
   - 证据：[admin/frontend/src/views/Layout.vue:89-98](D:/Exercise/ShiXun/CloudBrain/admin/frontend/src/views/Layout.vue#L89)

2. `admin/frontend/src/stores/user.js` 中的 `isLoggedIn` getter 目前在检索结果里没有直接被使用，属于弱引用状态。
   - 证据：[admin/frontend/src/stores/user.js:24-64](D:/Exercise/ShiXun/CloudBrain/admin/frontend/src/stores/user.js#L24)
   - 说明：这不一定是错，但如果后续不使用，可以删掉或统一改成由路由/布局消费。

3. `ScheduleQueryServiceImpl` 里很多统计方法都依赖不同 Service/Mapper 重复拼装 DTO，虽然不是纯冗余，但重复组装逻辑偏多，适合再抽一层 assembler/helper。
   - 证据：[admin/backen/src/main/java/com/neuCloudBrainMedical/admin/service/schedule/impl/ScheduleQueryServiceImpl.java:93-243](D:/Exercise/ShiXun/CloudBrain/admin/backen/src/main/java/com/neuCloudBrainMedical/admin/service/schedule/impl/ScheduleQueryServiceImpl.java#L93)

4. 工作区存在明显的编码/字符串异常痕迹，可能是无效代码或损坏文本：
   - `admin/frontend/src/utils/request.js`、`admin/backen/src/main/java/com/neuCloudBrainMedical/admin/service/auth/impl/AuthQueryServiceImpl.java`、`admin/backen/src/main/java/com/neuCloudBrainMedical/admin/service/role/impl/RoleCommandServiceImpl.java` 都能看到未闭合字符串或乱码提示。
   - 证据：[admin/frontend/src/utils/request.js:1-60](D:/Exercise/ShiXun/CloudBrain/admin/frontend/src/utils/request.js#L1)
   - 证据：[admin/backen/src/main/java/com/neuCloudBrainMedical/admin/service/auth/impl/AuthQueryServiceImpl.java:19-59](D:/Exercise/ShiXun/CloudBrain/admin/backen/src/main/java/com/neuCloudBrainMedical/admin/service/auth/impl/AuthQueryServiceImpl.java#L19)
   - 证据：[admin/backen/src/main/java/com/neuCloudBrainMedical/admin/service/role/impl/RoleCommandServiceImpl.java:20-66](D:/Exercise/ShiXun/CloudBrain/admin/backen/src/main/java/com/neuCloudBrainMedical/admin/service/role/impl/RoleCommandServiceImpl.java#L20)
   - 影响：这类内容不是“风格问题”，更像是可编译性风险，必须尽快修。

### 4. SOLID 观察

1. 单一职责原则：总体分层是对的，但 `DoctorCommandServiceImpl`、`ScheduleQueryServiceImpl`、`Layout.vue` 都偏厚，存在明显职责聚合。
2. 开闭原则：`ScheduleTimeSlotUtils`、`RoleInfo`、`DoctorRoleOption` 这类抽象整体还不错，但角色前缀、时间段、菜单显示规则有不少硬编码，扩展时需要改多处。
3. 里氏替换原则：目前没看到明显的继承层问题，风险不在这里。
4. 接口隔离原则：`IAuthQueryService`、`IDoctorQueryService`、`IScheduleQueryService` 比较符合接口隔离，优于大而全接口。
5. 依赖倒置原则：Service 层整体依赖接口，方向是对的；但实际业务里还是有不少直接依赖底层实现的厚方法，特别是查询/命令交叉处。

## 建议优先级

1. 先补后端统一鉴权和角色授权，别只靠前端守门。
2. 把 `expireOutdatedSchedules()` 从查询路径挪出去，改成定时任务或命令型维护逻辑。
3. 清理所有乱码/未闭合字符串，先保证能稳定编译。
4. 把 `DoctorCommandServiceImpl`、`ScheduleQueryServiceImpl` 里过厚的逻辑继续拆薄。
5. 把前端权限菜单从页面硬编码，改成基于角色的统一配置或从后端下发。

## 总结

当前代码不是“乱”，但有几个硬伤：服务端权限缺失、查询带写副作用、部分服务类职责过厚、以及疑似编码损坏导致的编译风险。先补安全边界，再修副作用和字符串问题，项目质量会立刻上一个台阶。