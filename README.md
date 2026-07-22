# 中介平台原型 - 服务商入驻与实名认证

前后端分离的中介平台原型，核心链路：服务商入驻 → 在线实名认证 → 平台审核。

## 目录结构

```
.
├── backend/                 # Java 后端（Spring Boot）
│   ├── pom.xml
│   └── src/main/java/com/platform/
│       ├── PlatformApplication.java    # 启动类
│       ├── common/                 # 通用类
│       │   └── Result.java           # 统一返回结构
│       ├── enums/                    # 枚举
│       │   ├── VerifyStatus.java   # 认证状态
│       │   └── ProfessionType.java # 职业类型
│       ├── model/                    # 数据模型
│       │   ├── ServiceProvider.java # 服务商
│       │   └── VerifyRecord.java   # 审核记录
│       ├── dto/                      # 数据传输对象
│       │   ├── ProviderCreateDTO.java
│       │   ├── ProviderUpdateDTO.java
│       │   ├── VerifySubmitDTO.java
│       │   └── VerifyAuditDTO.java
│       ├── service/                  # 服务层（内存存储）
│       │   └── ProviderService.java
│       └── controller/               # 控制层
│           ├── ProviderController.java    # 服务商CRUD + 审核
│           ├── StatisticsController.java # 统计
│           └── DictController.java   # 字典数据
└── frontend/                # 前端页面（原生 HTML/CSS/JS）
    ├── index.html            # 服务商列表 + 统计
    ├── detail.html           # 服务商详情 + 审核操作
    ├── form.html            # 入驻/编辑表单
    ├── css/style.css        # 样式
    └── js/common.js        # 公共函数
```

## 后端接口

统一返回格式：
```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

### 服务商接口
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/providers | 列表（支持 keyword / verifyStatus / professionType 筛选） |
| GET | /api/providers/{id} | 详情 |
| POST | /api/providers | 新增 |
| PUT | /api/providers/{id} | 编辑 |
| DELETE | /api/providers/{id} | 删除 |

### 实名认证接口
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/providers/{id}/verify/submit | 提交认证资料 |
| POST | /api/providers/{id}/verify/approve | 审核通过 |
| POST | /api/providers/{id}/verify/reject | 驳回审核 |
| GET | /api/providers/{id}/verify-records | 审核记录 |

### 统计接口
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/statistics | 统计数据（总数、各状态、各职业） |

### 字典接口
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/dict/profession-types | 职业类型列表 |
| GET | /api/dict/verify-statuses | 认证状态列表 |

## 运行方式

### 后端
```bash
cd backend
mvn spring-boot:run
```
启动后接口地址：http://localhost:8080/api

### 前端
直接用浏览器打开 `frontend/index.html`，或用任意静态服务器：
```bash
cd frontend
python -m http.server 8081
```
然后访问 http://localhost:8081

## 数据说明

- 使用内存存储（ConcurrentHashMap），重启后数据重置
- 启动时自动生成 8 条 mock 数据
- 认证状态流转：未提交 → 待审核 → 已认证 / 已驳回

## 后续可扩展方向

- 需求发布模块
- 撮合订单模块
- 平台介入模块
- 接入真实实名认证通道
- 数据库持久化（MySQL/Redis）
- 用户登录与权限管理
