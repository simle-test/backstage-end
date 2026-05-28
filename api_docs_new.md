# 后端API接口文档

## 基础信息

- **服务基础路径**: `http://localhost:8080`
- **认证方式**: JWT Token (Bearer Token)
- **请求格式**: JSON
- **响应格式**: JSON

## 目录

1. [认证接口](#认证接口)
2. [用户管理](#用户管理)
3. [题目管理](#题目管理)
4. [题库管理](#题库管理)
5. [统计接口](#统计接口)

---

## 认证接口

### 1. 用户登录

**接口地址**: `POST /auth/login`

**请求体**:
```json
{
  "username": "string (用户名)",
  "password": "string (密码)"
}
```

**成功响应** (200):
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "string (JWT令牌)",
    "expire": "string (过期时间)",
    "user": {
      "id": "number (用户ID)",
      "username": "string (用户名)",
      "email": "string (邮箱)",
      "phone": "string (手机号)",
      "role": "string (角色)"
    }
  }
}
```

---

### 2. 用户注册

**接口地址**: `POST /auth/register`

**请求体**:
```json
{
  "username": "string (用户名)",
  "password": "string (密码)",
  "email": "string (邮箱)",
  "phone": "string (手机号)"
}
```

**成功响应** (200):
```json
{
  "code": 200,
  "message": "注册成功",
  "data": null
}
```

---

## 用户管理

### 1. 获取用户列表

**接口地址**: `GET /users`

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| page | Integer | 否 | 页码，默认1 |
| size | Integer | 否 | 每页数量，默认10 |
| keyword | String | 否 | 搜索关键词 |
| role | String | 否 | 角色筛选 |

**成功响应** (200):
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "id": "number (用户ID)",
        "username": "string (用户名)",
        "email": "string (邮箱)",
        "role": "string (角色标识)",
        "roleName": "string (角色名称)",
        "practiceCount": "number (刷题次数)",
        "passRate": "number (通过率)",
        "joinDate": "string (加入日期)",
        "status": "string (状态)",
        "statusText": "string (状态文本)",
        "avatar": "string (头像)",
        "avatarColor": "string (头像颜色)"
      }
    ],
    "total": "number (总记录数)",
    "page": "number (当前页码)",
    "size": "number (每页数量)"
  }
}
```

---

### 2. 获取用户统计

**接口地址**: `GET /users/statistics`

**成功响应** (200):
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": "number (总用户数)",
    "active": "number (活跃用户数)",
    "todayNew": "number (今日新增)",
    "avgActiveDays": "number (平均活跃天数)"
  }
}
```

---

### 3. 添加用户

**接口地址**: `POST /users`

**请求体**:
```json
{
  "username": "string (用户名)",
  "password": "string (密码)",
  "email": "string (邮箱)",
  "phone": "string (手机号)",
  "role": "string (角色，可选值: ROLE_USER, ROLE_ADMIN, ROLE_VIP)",
  "status": "string (状态，可选值: active, inactive, banned)",
  "avatar": "string (头像)"
}
```

**成功响应** (200):
```json
{
  "code": 200,
  "message": "添加成功",
  "data": {
    "id": "number (用户ID)",
    "username": "string (用户名)",
    "email": "string (邮箱)",
    "phone": "string (手机号)",
    "role": "string (角色)",
    "status": "string (状态)"
  }
}
```

---

### 4. 获取当前用户信息

**接口地址**: `GET /users/me`

**请求头**:
```
Authorization: Bearer <token>
```

**成功响应** (200):
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": "number (用户ID)",
    "username": "string (用户名)",
    "email": "string (邮箱)",
    "phone": "string (手机号)",
    "role": "string (角色)",
    "status": "string (状态)",
    "createdAt": "string (创建时间)"
  }
}
```

---

### 5. 获取用户详情

**接口地址**: `GET /users/{id}`

**路径参数**:
| 参数名 | 类型 | 说明 |
| :--- | :--- | :--- |
| id | Long | 用户ID |

**成功响应** (200):
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": "number (用户ID)",
    "username": "string (用户名)",
    "email": "string (邮箱)",
    "phone": "string (手机号)",
    "role": "string (角色)",
    "status": "string (状态)"
  }
}
```

---

### 6. 更新用户信息

**接口地址**: `PUT /users/{id}`

**路径参数**:
| 参数名 | 类型 | 说明 |
| :--- | :--- | :--- |
| id | Long | 用户ID |

**请求体**:
```json
{
  "username": "string (用户名，可选)",
  "email": "string (邮箱，可选)",
  "phone": "string (手机号，可选)",
  "role": "string (角色，可选)",
  "status": "string (状态，可选)",
  "avatar": "string (头像，可选)"
}
```

**成功响应** (200):
```json
{
  "code": 200,
  "message": "更新成功",
  "data": {
    "id": "number (用户ID)",
    "username": "string (用户名)",
    "email": "string (邮箱)",
    "phone": "string (手机号)",
    "role": "string (角色)",
    "status": "string (状态)"
  }
}
```

---

### 7. 删除用户

**接口地址**: `DELETE /users/{id}`

**路径参数**:
| 参数名 | 类型 | 说明 |
| :--- | :--- | :--- |
| id | Long | 用户ID |

**成功响应** (200):
```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

---

## 题目管理

### 1. 获取题目列表

**接口地址**: `GET /questions`

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| page | Integer | 否 | 页码，默认1 |
| size | Integer | 否 | 每页数量，默认10 |
| keyword | String | 否 | 搜索关键词 |
| difficulty | String | 否 | 难度筛选 (easy/medium/hard) |
| category | String | 否 | 类别筛选 |

**成功响应** (200):
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "id": "number (题目ID)",
        "questionId": "string (题目编号)",
        "title": "string (题目标题)",
        "content": "string (题目内容)",
        "options": ["string (选项数组)"],
        "answer": "string (答案)",
        "analysis": "string (解析)",
        "difficulty": "string (难度)",
        "difficultyText": "string (难度文本)",
        "category": "string (类别)",
        "categoryText": "string (类别文本)",
        "bankId": "number (题库ID)",
        "bankName": "string (题库名称)",
        "createdAt": "string (创建时间)"
      }
    ],
    "total": "number (总记录数)",
    "page": "number (当前页码)",
    "size": "number (每页数量)"
  }
}
```

---

### 2. 获取题目统计

**接口地址**: `GET /questions/statistics`

**成功响应** (200):
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": "number (总题目数)",
    "easyCount": "number (简单题数)",
    "mediumCount": "number (中等题数)",
    "hardCount": "number (困难题数)",
    "categoryList": [
      {
        "category": "string (类别)",
        "categoryText": "string (类别文本)",
        "count": "number (数量)"
      }
    ]
  }
}
```

---

### 3. 添加题目

**接口地址**: `POST /questions`

**请求体**:
```json
{
  "questionId": "string (题目编号)",
  "title": "string (题目标题)",
  "content": "string (题目内容)",
  "options": ["string (选项数组)"],
  "answer": "string (答案)",
  "analysis": "string (解析)",
  "difficulty": "string (难度: easy/medium/hard)",
  "category": "string (类别)",
  "bankId": "number (题库ID)"
}
```

**成功响应** (200):
```json
{
  "code": 200,
  "message": "添加成功",
  "data": null
}
```

---

### 4. 更新题目

**接口地址**: `PUT /questions/{id}`

**路径参数**:
| 参数名 | 类型 | 说明 |
| :--- | :--- | :--- |
| id | Integer | 题目ID |

**请求体**:
```json
{
  "questionId": "string (题目编号，可选)",
  "title": "string (题目标题，可选)",
  "content": "string (题目内容，可选)",
  "options": ["string (选项数组，可选)"],
  "answer": "string (答案，可选)",
  "analysis": "string (解析，可选)",
  "difficulty": "string (难度，可选)",
  "category": "string (类别，可选)",
  "bankId": "number (题库ID，可选)"
}
```

**成功响应** (200):
```json
{
  "code": 200,
  "message": "更新成功",
  "data": null
}
```

---

### 5. 删除题目

**接口地址**: `DELETE /questions/{id}`

**路径参数**:
| 参数名 | 类型 | 说明 |
| :--- | :--- | :--- |
| id | Integer | 题目ID |

**成功响应** (200):
```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

---

### 6. 获取题目详情

**接口地址**: `GET /questions/{id}`

**路径参数**:
| 参数名 | 类型 | 说明 |
| :--- | :--- | :--- |
| id | Integer | 题目ID |

**成功响应** (200):
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": "number (题目ID)",
    "questionId": "string (题目编号)",
    "title": "string (题目标题)",
    "content": "string (题目内容)",
    "options": ["string (选项数组)"],
    "answer": "string (答案)",
    "analysis": "string (解析)",
    "difficulty": "string (难度)",
    "category": "string (类别)",
    "bankId": "number (题库ID)",
    "bankName": "string (题库名称)",
    "createdAt": "string (创建时间)",
    "updatedAt": "string (更新时间)"
  }
}
```

---

## 题库管理

### 1. 获取题库列表

**接口地址**: `GET /banks`

**成功响应** (200):
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": "number (题库ID)",
      "name": "string (题库名称)",
      "desc": "string (题库描述)",
      "color": "string (主题色)",
      "questionCount": "number (题目数量)",
      "updatedAt": "string (更新时间)"
    }
  ]
}
```

---

### 2. 获取题库详情

**接口地址**: `GET /banks/{id}`

**路径参数**:
| 参数名 | 类型 | 说明 |
| :--- | :--- | :--- |
| id | Integer | 题库ID |

**成功响应** (200):
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": "number (题库ID)",
    "name": "string (题库名称)",
    "desc": "string (题库描述)",
    "color": "string (主题色)",
    "tags": ["string (标签数组)"],
    "questionCount": "number (题目数量)"
  }
}
```

---

### 3. 创建题库

**接口地址**: `POST /banks`

**请求体**:
```json
{
  "name": "string (题库名称)",
  "desc": "string (题库描述)",
  "color": "string (主题色)"
}
```

**成功响应** (200):
```json
{
  "code": 200,
  "message": "创建成功",
  "data": null
}
```

---

### 4. 更新题库

**接口地址**: `PUT /banks/{id}`

**路径参数**:
| 参数名 | 类型 | 说明 |
| :--- | :--- | :--- |
| id | Integer | 题库ID |

**请求体**:
```json
{
  "name": "string (题库名称，可选)",
  "desc": "string (题库描述，可选)",
  "color": "string (主题色，可选)"
}
```

**成功响应** (200):
```json
{
  "code": 200,
  "message": "更新成功",
  "data": null
}
```

---

## 统计接口

### 1. 获取综合统计

**接口地址**: `GET /statistics`

**成功响应** (200):
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "totalQuestions": "number (题目总数)",
    "totalSubmissions": "number (总提交数)",
    "totalUsers": "number (用户总数)",
    "avgPassRate": "number (平均通过率)"
  }
}
```

---

### 2. 获取每日提交趋势

**接口地址**: `GET /statistics/daily`

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| days | Integer | 否 | 查询天数，默认7天 |

**成功响应** (200):
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "date": "string (日期，格式: yyyy-MM-dd)",
      "count": "number (提交数量)"
    }
  ]
}
```

---

### 3. 获取模块得分分布

**接口地址**: `GET /statistics/categories`

**成功响应** (200):
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "easyScore": "number (简单题得分率)",
    "mediumScore": "number (中等题得分率)",
    "hardScore": "number (困难题得分率)"
  }
}
```

---

### 4. 获取刷题排行榜

**接口地址**: `GET /statistics/ranking`

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| limit | Integer | 否 | 返回数量，默认10 |

**成功响应** (200):
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "rank": "number (排名)",
      "username": "string (用户名)",
      "avatar": "string (头像)",
      "avatarColor": "string (头像颜色)",
      "title": "string (称号)",
      "practiceCount": "number (刷题数量)"
    }
  ]
}
```

---

## 错误响应格式

所有接口失败时返回统一格式：

```json
{
  "code": "number (错误码，非200)",
  "message": "string (错误信息)",
  "data": null
}
```

---

## 通用响应结构

| 字段 | 类型 | 说明 |
| :--- | :--- | :--- |
| code | Integer | 状态码，200表示成功 |
| message | String | 响应消息 |
| data | Object/Array | 响应数据 |

---

## 类别映射表

| category | categoryText | 说明 |
| :--- | :--- | :--- |
| political_theory | 政治理论 | 政治理论类题目 |
| quantity_relation | 数量关系 | 数量关系类题目 |
| material_analysis | 资料分析 | 资料分析类题目 |
| common_sense_judgment | 常识判断 | 常识判断题 |
| logical_judgment | 判断推理 | 判断推理题 |
| language_understanding | 言语理解 | 言语理解题 |

---

## 难度映射表

| difficulty | difficultyText | 说明 |
| :--- | :--- | :--- |
| easy | 简单 | 简单难度 |
| medium | 中等 | 中等难度 |
| hard | 困难 | 困难难度 |
