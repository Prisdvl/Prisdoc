# 代码注释规范

> 适用范围：`common`、`user-service`、`file-service`、`ai-service`、`document-service`、
> `gateway-service` 下的 Java 代码，以及 `vue-test-app` 下的 JS / Vue 代码。
> 本规范用于统一注释风格、消除伪标签与过期注释；新增代码必须遵守，存量代码按本文逐步修缮。

---

## 1. 总则

1. **注释解释「做什么 / 为什么」，不复述「怎么做」**。`i++ // i 加一` 这类注释一律删除。
2. **注释必须与代码同步**。过期注释、与实现矛盾的注释，视同缺陷，必须修正或删除。
3. **注释语言为中文**，专有名词（HTTP、JWT、MinIO、RabbitMQ 等）保留英文原文。
   同一段注释内不混用中英文句式。
4. **只使用 Javadoc 标准标签**：`@param`、`@return`、`@throws`、`@see`、`@since`、
   `@author`、`@deprecated`。禁止自造标签（如 `@description:`、`@date:`、`@explain:`）。
5. **禁止提交被注释掉的代码**、调试语句（`System.out.println`、`console.log`）、
   无责任人的 `TODO` / `FIXME`。确需保留的待办写成
   `// TODO(负责人, 目标版本): 具体原因与后续动作`。
6. 注释块内不出现连续多个空星号行；标签段落前空一行，标签之间不空行。

---

## 2. Java 注释规范

### 2.1 类级 Javadoc（必须）

每个 `class` / `interface` / `enum` / `record` 都必须有类级 Javadoc，写在注解之上。

```java
/**
 * 用户服务实现，完成注册、登录、令牌刷新与用户信息查询。
 *
 * <p>密码统一使用 BCrypt 编码存储；登录失败不区分「用户不存在」与「密码错误」，
 * 避免账号枚举。</p>
 *
 * @author qxk
 * @since 1.0.0
 */
@Service
public class UserServiceImpl implements UserService {
```

要求：

- **首行摘要**：一句话说明职责，不超过 60 字，以句号结尾。
- **补充说明**：可选，使用 `<p>...</p>` 独立成段，写清关键约束、线程安全性、
  依赖关系、失败语义等。
- **标签顺序**：`@param` → `@return` → `@throws` → `@see` → `@since` → `@author`。
- `@author`、`@since` 为可选项：保留历史署名，不张冠李戴，也不强制新增。
- 摘要不得写成 `@description:` 之类的伪标签，也不得把 `@author` 放在摘要位置。

### 2.2 方法级 Javadoc

**必须写**：

- `Service` / `Agent` / `RAG` / `Util` 等类的 `public` 方法；
- `Controller` 中**没有** `@Operation` 注解的端点方法；
- 带重载、带副作用的非平凡方法。

**可省略**：

- getter / setter、`toString` / `equals` / `hashCode`；
- 构造器、`@Override` 方法；
- `Controller` 中已有 `@Operation` 的端点（Swagger 注解已承担对外文档职责，
  重复书写会造成两处失同步）；
- 测试方法。

```java
/**
 * 按用户名查询用户，供文档协作授权时查找协作者。
 *
 * @param username 用户名，不允许为空
 * @return 用户信息；用户不存在时抛出业务异常
 * @throws BusinessException 用户名对应用户不存在
 */
public UserVO getUserByUsername(String username) {
```

要求：

- `@param` 必须与签名中的参数**一一对应、顺序一致、名称一致**；不得漏写，也不得
  写不存在的参数。
- 非 `void` 返回值必须有 `@return`；`void` 方法**不得**写 `@return`。
- 受检异常必须写 `@throws`；运行时异常在对外接口处建议写。
- 每个标签后**至少一句话**，禁止 `@param id`、`@return` 这类空标签。

### 2.3 字段注释

- 实体、DTO、VO 的字段用行内 Javadoc 说明业务含义：

  ```java
  /** 用户 ID，主键 */
  private Long id;
  ```

- 布尔字段要说明 `true` 的含义，例如「是否启用：true-启用，false-禁用」。
- 枚举常量逐项注释，说明取值含义与使用场景。
- 自解释字段（`id`、`name`、`createTime`）可只写中文名。

### 2.4 行内注释

- `//` 之后必须跟一个空格；中文与英文之间加空格。
- 注释独立成行，放在被说明代码的上方；行尾注释只用于极短说明（≤ 20 字）。
- 分步逻辑用 `// 1. ...`、`// 2. ...` 编号，便于对照。
- 方法内部的区块分隔使用空行，不用 `// =====` 横幅填充。

### 2.5 禁止项

| 禁止 | 说明 |
| --- | --- |
| `/** @author xxx @description: ... */` | 伪标签 + 标签置于摘要位 |
| `// TODO` / `// FIXME` 无负责人 | 无法追踪 |
| 注释掉的代码 | 交给 Git 历史保存 |
| `【待实现】` 但代码已实现 | 过期注释，必须删除或改为真实约束 |
| 空 Javadoc `/** */`、`@param id` | 空标签无信息量 |
| `@return` 用于 `void` 方法 | 标签与签名不符 |
| 注释与代码逻辑相反 | 误导性注释 |

---

## 3. 前端（JS / Vue）注释规范

### 3.1 JSDoc

导出的函数、组合式函数、API 封装必须写 JSDoc：

```js
/**
 * 拉取文档详情。
 *
 * @param {string|number} id 文档 ID
 * @param {{ signal?: AbortSignal }} [options] 可选配置
 * @returns {Promise<{ id: string, title: string, content: string }>} 文档详情
 * @throws {Error} 接口返回非 2xx 时抛出
 */
export async function fetchDocument(id, options = {}) { }
```

要求：

- 使用 `@param {类型} 名称 说明`、`@returns {类型} 说明`、`@throws {Error} 说明`；
- 可选参数写成 `[name]`，带默认值时写成 `[name=值]`；
- 不使用 `@description` 伪标签。

### 3.2 Vue 单文件组件

- `<script setup>` 顶部用块注释说明页面/组件职责与关键交互；
- `defineProps` / `defineEmits` 的每个字段用行尾注释或上方注释说明含义；
- 复杂 `watch` / `computed` 用 `//` 说明触发条件与副作用；
- 模板注释统一 `<!-- 说明 -->`，禁止用注释包裹整段被禁用的模板。

### 3.3 行内注释

- `//` 后一个空格；禁止 `//注释`；
- 不保留 `console.log` 调试输出及其注释。

---

## 4. 自查清单

提交前逐条确认：

1. 新增的类是否有类级 Javadoc，首行摘要是否说明了职责？
2. 新增/修改的 public 方法是否补齐 `@param` / `@return`，且与签名一致？
3. 是否引入了伪标签、空标签、`@return` 用于 `void`？
4. 是否残留注释掉的代码、无主 TODO、过期说明？
5. 行内注释是否为 `// ` 开头，且不与代码矛盾？
6. 前端导出函数是否有 JSDoc，类型是否写清？

代码评审时按以上 6 条逐条对照即可。

---

## 5. 示例对照

### 5.1 类注释

```java
// 修缮前：伪标签、标签抢占摘要位置
/**
 * @author qxk
 * @description: 用户控制器
 */

// 修缮后：摘要 + 可选详情 + 规范标签
/**
 * 用户接口控制器，提供登录、注册、信息查询与令牌刷新接口。
 *
 * @author qxk
 */
```

### 5.2 方法注释

```java
// 修缮前：缺 @param、@return 与签名不符
/**
 * 用户登录
 * @return 登录结果
 */
public Result<LoginVO> login(@RequestBody LoginDTO loginDTO) { }

// 修缮后：摘要、参数、返回值齐全
/**
 * 校验用户名与密码，签发访问令牌与刷新令牌。
 *
 * @param loginDTO 登录请求参数，包含用户名与明文密码
 * @return 登录结果，含访问令牌、刷新令牌与用户基本信息
 */
public Result<LoginVO> login(@RequestBody LoginDTO loginDTO) { }
```

### 5.3 行内注释

```java
// 修缮前
//TODO 后续改成异步
send(msg);//发送消息

// 修缮后
// TODO(张三, 1.2.0): 改为异步发送，避免阻塞主流程
send(msg); // 发送消息
```
