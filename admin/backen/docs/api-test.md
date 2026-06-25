# 管理员端接口测试文档

## 测试准备

1. 创建并初始化数据库：

```sql
source sql/schema.sql;
source sql/test-data.sql;
```

2. 启动后端：

```bash
mvn spring-boot:run
```

3. 默认服务地址：

```text
http://localhost:8080
```

## 统一响应格式

所有接口返回：

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

## 管理员登录

- URL：`POST /api/auth/login`
- 完整地址：`http://localhost:8080/api/auth/login`
- 请求头：`Content-Type: application/json`
- 测试账号：`admin`
- 测试密码：`123456`

请求体：

```json
{
  "username": "admin",
  "password": "123456"
}
```

成功响应示例：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "JWT_TOKEN",
    "userId": 1,
    "username": "admin",
    "realName": "系统管理员",
    "roleCode": "ADMIN",
    "roleName": "管理员"
  }
}
```

密码错误响应示例：

```json
{
  "code": 401,
  "message": "用户名或密码错误",
  "data": null
}
```

## 查询启用科室列表

- URL：`GET /api/departments`
- 完整地址：`http://localhost:8080/api/departments`
- 请求头：可不传；如需携带登录令牌，使用 `Authorization: Bearer <token>`

成功响应示例：

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "deptId": 1,
      "deptCode": "NEUROLOGY",
      "deptName": "神经内科",
      "deptType": "临床",
      "location": "门诊楼2层",
      "description": "神经系统疾病诊疗"
    }
  ]
}
```

## 根据 ID 查询科室

- URL：`GET /api/departments/{id}`
- 示例地址：`http://localhost:8080/api/departments/1`

成功响应示例：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "deptId": 1,
    "deptCode": "NEUROLOGY",
    "deptName": "神经内科",
    "deptType": "临床",
    "location": "门诊楼2层",
    "description": "神经系统疾病诊疗"
  }
}
```

不存在响应示例：

```json
{
  "code": 404,
  "message": "科室不存在",
  "data": null
}
```
