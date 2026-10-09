# mall — 在线商城后端

一个用于学习与实习准备的 Java 后端项目,覆盖电商核心链路:商品浏览、用户注册登录(JWT)、下单扣库存、Redis 缓存。分层清晰、注释完整,适合作为面试作品。

## 技术栈

| 分类 | 选型 |
| --- | --- |
| 语言 / 运行时 | Java 21 |
| 框架 | Spring Boot 3.5.3 |
| 持久层 | MyBatis 3.0.4 + PageHelper 2.1.1(分页) |
| 数据库 | MySQL 5.7 |
| 缓存 | Redis 5.x(Spring Data Redis + Lettuce) |
| 鉴权 | JWT(jjwt 0.12.6)+ BCrypt(spring-security-crypto) |
| 构建 | Maven 3.9 |

## 功能特性

- **商品**:分页列表、详情、新增(详情走 Redis 缓存)
- **用户**:注册、登录(JWT 签发)、当前用户信息
- **订单**:下单(事务 + 悲观锁防超卖)、我的订单、订单详情
- **缓存**:商品详情缓存,覆盖缓存穿透 / 击穿 / 雪崩,下单后保证缓存一致性

## 项目亮点(面试考点)

1. **下单防超卖(事务 + 悲观锁双保险)**:`@Transactional` 把「锁行校验 → 扣库存 → 建主单 → 写明细」绑成一个事务;`SELECT ... FOR UPDATE` 悲观锁串行化并发下单,再加 `UPDATE ... SET stock = stock - #{q} WHERE stock >= #{q}` 兜底,即使没锁到也不会扣成负数。
2. **Redis 缓存三大经典问题**:见下。
3. **JWT 鉴权**:`AuthInterceptor` 拦截 `/api/**`,校验 `Authorization: Bearer <token>`,通过后把用户 id 放入 `ThreadLocal`(`UserContext`),请求结束清理防串号。
4. **统一返回体 + 全局异常**:`Result<T>` 统一响应,`GlobalExceptionHandler` 兜底参数校验 / 业务异常 / 404 / 500。

### 缓存三大问题是怎么解的

| 问题 | 场景 | 方案 | 代码位置 |
| --- | --- | --- | --- |
| 缓存穿透 | 查一个不存在的 id,每次都打到 DB | 空值缓存(存空标记,短 TTL 60s) | `CacheClient#setEmpty` |
| 缓存击穿 | 热点 key 过期瞬间,大量请求打到 DB | `SET NX EX` 互斥锁 + 双重检查,只放一个线程回源重建 | `CacheClient#tryLock` / `ProductService#getById` |
| 缓存雪崩 | 大量 key 同一时刻过期 | TTL 在基准值上随机 ±20%,错开过期时间 | `CacheClient#randomTtl` |
| 缓存一致性 | 下单扣库存后缓存仍是旧值 | 事务提交后(afterCommit)删缓存,下次查询回源拿最新值 | `OrderService#createOrder` |

## 项目结构

```
src/main/java/com/stewie/mall
├── common/       # Result 统一返回、PageResult 分页体、BizErrorCode 错误码
├── config/       # WebConfig(拦截器注册)、PasswordConfig(BCrypt)
├── controller/   # 接口层(Auth/User/Product/Order)
├── dto/          # 请求/响应 DTO(JDK record)
├── entity/       # 实体(对应表)
├── exception/    # 业务异常 + 全局异常处理
├── interceptor/  # JWT 登录拦截器
├── mapper/       # MyBatis Mapper 接口
├── service/      # 业务层(事务、缓存逻辑)
└── util/         # JwtUtil、UserContext、CacheClient
src/main/resources
├── application.yml
└── mapper/*.xml  # SQL
```

## 快速开始

### 1. 环境要求

- JDK 21、Maven 3.9
- MySQL 5.7(本机运行在 3306)
- Redis 5.x(本机运行在 6379)

### 2. 初始化数据库

```sql
CREATE DATABASE IF NOT EXISTS mall DEFAULT CHARACTER SET utf8mb4;
USE mall;

CREATE TABLE product (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    name        VARCHAR(100) NOT NULL,
    price       DECIMAL(10,2) NOT NULL,
    stock       INT NOT NULL DEFAULT 0,
    description VARCHAR(500),
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE users (
    id         BIGINT PRIMARY KEY AUTO_INCREMENT,
    username   VARCHAR(50) NOT NULL UNIQUE,
    password   VARCHAR(100) NOT NULL,
    nickname   VARCHAR(50),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE orders (
    id           BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id      BIGINT NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    status       TINYINT NOT NULL DEFAULT 0,
    created_at   DATETIME DEFAULT CURRENT_TIMESTAMP,
    KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE order_items (
    id           BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id     BIGINT NOT NULL,
    product_id   BIGINT NOT NULL,
    product_name VARCHAR(100) NOT NULL,
    unit_price   DECIMAL(10,2) NOT NULL,
    quantity     INT NOT NULL,
    KEY idx_order_id (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

### 3. 修改配置

编辑 `src/main/resources/application.yml`,把 `spring.datasource.password` 改成本机 MySQL 密码,`spring.data.redis` 改成本机 Redis 地址(默认 `localhost:6379` 无需改)。

### 4. 运行

```bash
mvn spring-boot:run
```

启动后访问 `http://localhost:8080`。

## API 一览

| 方法 | 路径 | 说明 | 鉴权 |
| --- | --- | --- | --- |
| POST | `/api/auth/register` | 注册 | 公开 |
| POST | `/api/auth/login` | 登录,返回 JWT | 公开 |
| GET | `/api/products?page=&size=` | 商品分页列表 | 公开 |
| GET | `/api/products/{id}` | 商品详情(带缓存) | 公开 |
| POST | `/api/products` | 新增商品 | 公开 |
| GET | `/api/user/me` | 当前用户信息 | 需登录 |
| POST | `/api/orders` | 下单 | 需登录 |
| GET | `/api/orders` | 我的订单 | 需登录 |
| GET | `/api/orders/{id}` | 订单详情 | 需登录 |

受保护接口需携带请求头:`Authorization: Bearer <token>`。

### 调用示例

```bash
# 注册并登录
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"stewie","password":"123456"}'

curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"stewie","password":"123456"}'

# 商品分页(第 1 页,每页 10 条)
curl "http://localhost:8080/api/products?page=1&size=10"

# 下单(带上登录返回的 token)
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{"items":[{"productId":1,"quantity":2}]}'
```
