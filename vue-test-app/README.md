# Prisdoc 前端（vue-test-app）

Prisdoc 智能文档平台的前端，Vue 3 + Vite + Element Plus + Vue Router。

- 工作台 / 文档编辑器 / Agent 工作台 / AI Ops 看板 / 沉浸式 AI 对话 / HTML PPT 生成
- 后端为 Spring Cloud 微服务（gateway 8080、user 8081、file 8082、ai 8083、document 8084）

## 启动

```bash
npm install

# 方式一：Mock 模式（推荐，零后端依赖，用于演示与 UI 走查）
npm run dev:mock

# 方式二：连接真实后端（需先启动网关 8080 与 AI 服务 8083）
npm run dev
```

两种方式都访问 <http://localhost:5173>。Mock 模式的演示账号：`张伟 / prisdoc123`（任意密码均可）。

## Mock 模式

`mock/` 目录在 Vite 开发服务器里注册了一个中间件，命中 `/api/**` 时返回本地假数据：

```
mock/
├─ data.js   # 数据集：用户、文档全文、Agent 工具/任务、AI Ops 指标、PPT 模板
└─ index.js  # 路由表、异步任务状态机、内存状态（新增 prisdocMockPlugin）
```

- 只在 `vite --mode mock`（或 `VITE_MOCK=1`）时启用，`npm run build` 产物中不含 Mock 代码；
- 未命中的 `/api` 请求会继续走 Vite proxy，因此原有代理配置不受影响；
- 查看已注册接口：<http://localhost:5173/api/__mock__/info>；
- 详细说明见 [`../docs/MOCK_MODE_GUIDE.md`](../docs/MOCK_MODE_GUIDE.md)。

## 设计体系

深海军蓝底 + 深钴蓝主色的单主题（dark）界面，令牌集中在 `src/styles/prisdoc-tokens.css`：

- `--pd-bg / --pd-surface / --pd-border` 等背景与边界令牌，`--pd-primary*` 品牌色，`--pd-r-*` 圆角，`--pd-shadow-*` 双层阴影；
- Element Plus 通过 `element-plus/theme-chalk/dark/css-vars.css` + `html.dark{}` 变量覆盖接入，弹层样式写在全局（teleport 到 body）；
- 字体用 `@fontsource/space-grotesk` 自托管（400/500/600/700），中文回退 PingFang SC / 微软雅黑，不引外链；
- 唯一刻意的例外：编辑器的 A4 纸保持浅色（`#F7F8FA`），它是深色 UI 成立的前提。
- 完整规范（令牌表、组件规则、禁止项）见 [`../docs/DESIGN_SYSTEM.md`](../docs/DESIGN_SYSTEM.md)。

## 目录结构

```
src/
├─ api/            # 各业务模块接口封装
├─ collaboration/  # 协同编辑（轮询同步 + 协作者授权）
├─ styles/         # 设计令牌 + Element Plus 暗色映射
├─ views/
│  ├─ auth/        # 登录、注册
│  ├─ dashboard/   # 工作台、PPT 运行时
│  ├─ editor/      # 文档编辑器 / 沉浸式对话
│  ├─ agent/       # Agent 工作台
│  └─ aiops/       # AI Ops 运维看板
├─ utils/          # request 封装、JWT、全局 AI 状态
└─ router/
```

> 本目录基于 Vite 官方 `vue` 模板初始化，模板自带的说明保留在 git 历史中。
