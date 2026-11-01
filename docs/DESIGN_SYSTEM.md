# Prisdoc 设计体系（DESIGN_SYSTEM）

> 视觉语言取自 Prisma-recover / Prisdvl-Nexus 的 `html[data-palette="prisdvl"]`：
> **深海军蓝底 + 深钴蓝主色 + 电光蓝仅用于 hover + 冰蓝仅用于文字高光**，极简现代 SaaS（Linear / Vercel 气质）。
> 令牌唯一来源：`vue-test-app/src/styles/prisdoc-tokens.css`。改颜色只改这一处。

## 一、令牌

| 分组 | 令牌 | 值 | 用途 |
| --- | --- | --- | --- |
| 背景 | `--pd-bg` | `#080B12` | 页面底 |
| | `--pd-surface` | `#101522` | 卡片 / 侧栏 / 弹窗 |
| | `--pd-surface-muted` | `#171F31` | 次级块、气泡、表头 |
| | `--pd-input` | `#0A0E16` | 输入框、代码块 |
| | `--pd-border` / `--pd-border-strong` | `#273349` / `#334663` | 1px 分隔线 / hover 边界 |
| 文字 | `--pd-text` / `--pd-text-2` / `--pd-text-3` | `#E8EEF8` / `#A3B1C7` / `#71809A` | 正文 / 次要 / 辅助 |
| 品牌 | `--pd-primary` | `#2457D6` | 唯一强调色（实心按钮、选中） |
| | `--pd-primary-hover` | `#3978F6` | 仅 hover / focus 边界 |
| | `--pd-primary-strong` | `#193FA8` | 按下态、渐变暗端 |
| | `--pd-primary-text` | `#75C8FF` | 链接与高亮文字（冰蓝） |
| | `--pd-primary-weak` | `color-mix(18% 主色, surface)` | 选中底、Tag 底 |
| | `--pd-grad-brand` | `135° #193FA8 → #2457D6 → #3987EC` | 品牌块、头部、悬浮球 |
| 状态 | `--pd-danger` / `--pd-warning` / `--pd-success` | `#f07178` / `#e6b450` / `#7fd99a` | 危险 / 警告 / 成功 |
| 圆角 | `--pd-r-sm/md/lg/xl/pill` | `6 / 10 / 14 / 18 / 999px` | 入框·代码 / 按钮 / 卡片 / 弹窗 |
| 阴影 | `--pd-shadow-sm/md/lg` | 双层（黑 45–55% + 深蓝 40–45%） | 卡片 / 悬浮 / 弹窗 |
| 动效 | `--pd-dur-fast` / `--pd-dur` / `--pd-dur-panel` | `130 / 200 / 280ms` | 颜色 / 控件 / 面板 |
| | `--pd-ease` | `cubic-bezier(0.23, 1, 0.32, 1)` | 全局唯一缓动 |

字体：`--pd-font`（Space Grotesk 自托管 400–700 → 中文回退 PingFang SC / 微软雅黑），数字与等宽另用 `--pd-font-mono`。**禁止 `fonts.googleapis.com` 外链**（离线会白屏）。

## 二、组件规范

| 元素 | 规范 |
| --- | --- |
| 卡片 | `--pd-surface` 底 + `1px solid --pd-border` + `--pd-r-lg` + `--pd-shadow-sm`；hover 升 `--pd-shadow-md` |
| 主按钮 | 实心 `--pd-primary`，hover `--pd-primary-hover`，字 `#E8EEF8`，`--pd-r-md`，字重 500 |
| 次 / 幽灵按钮 | 透明底 + `1px --pd-border`，字 `--pd-text-2`，hover 底 `--pd-hover` |
| 文字 / 链接按钮 | `--pd-primary-text`，hover `--pd-primary-hover` |
| 输入框 | `--pd-input` 底 + `--pd-border` 边，hover 边 `--pd-border-strong`，focus 边 `--pd-primary-hover` + `0 0 0 3px rgba(57,135,236,.18)` |
| 表格 | 表头 `--pd-surface-muted` / 字 `--pd-text-2`，行 hover `--pd-hover`，斑马 `#0C1220`，分隔线 `--pd-border` |
| Tag | `--pd-*-weak` 底 + `--pd-*` 字与边；形状沿用原有（pill 或 6px） |
| 弹窗 / 抽屉 | `--pd-surface` 底 + `--pd-border` 边 + `--pd-r-xl` + `--pd-shadow-lg`，遮罩 `rgba(8,11,18,.72)` |
| 侧边栏 | `--pd-surface` 底 + 右边框；选中 `--pd-primary-weak` + `--pd-primary-text`（原设计无指示条，故不加） |
| 代码 / JSON | `#0A0E16` 底 + `#C7D3E6` 字 + `--pd-font-mono` + `--pd-r-md` |
| 图标 | 常态 `--pd-text-2`，激活 `--pd-primary-text`；除语义色外不做彩色图标 |

Element Plus 的暗色映射写在 `prisdoc-tokens.css` 的 `html.dark{}` 与 `html.dark .el-*` 段里 ——
`el-dialog`、`el-drawer`、`el-message`、`el-loading` 会被 teleport 到 body，**变量与覆盖必须放全局样式，不能带 scoped**。

## 三、禁止项

紫色渐变、发光/霓虹、玻璃拟态（`backdrop-filter`）、彩色糖果色卡片、花哨动效、超过 300ms 的动画、
外链字体与图标 CDN。动效只动具体属性，`prefers-reduced-motion` 必须生效。

## 四、两个刻意的例外

1. **A4 纸保持浅色**：编辑器的 `.paper` 固定 `#F7F8FA` 底 + `#1F2329` 字。深色 UI 的包围 + 浅色纸面是这套设计成立的核心对比（也让打印 / 导出 Word 的观感一致），因此纸面**不参与**暗色化。
2. **封面不用糖果色**：文档卡片的四套封面色改为同一族深蓝梯度（`#0B1730→#12294F` 等），靠明度差区分，不靠色相。

## 五、布局红线

本次换肤只改视觉：DOM 层级、栅格、组件位置、页面分区、侧边栏宽度（工作台 240px、编辑器左栏 260px / AI 栏 380px）、
内容区 `max-width: 1050px`、A4 纸 780px、卡片列数均未改动。
允许的属性：颜色、背景、边框、圆角、阴影、透明度、字体、字距、行高、过渡、光标。
