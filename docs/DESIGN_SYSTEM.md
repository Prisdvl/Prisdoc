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

> 上表是**深色主题**取值（`:root` 默认）；浅色主题在同一文件里以 `html[data-theme="light"]` 覆盖同名令牌，见第六节。

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
| 卡片网格 | `repeat(auto-fill, minmax(300px, 1fr))` + 16–24px gap；卡片 `--pd-r-lg` + 1px `--pd-border`，hover 边框 `--pd-border-strong` + `--pd-shadow-md` + 上浮 2px |
| 时间轴 / 动态列表 | 6px 状态点（成功 / 警告）+ 13.5px 主文 + 12px 次文；整行可点，hover 底 `--pd-hover`，动作文案（`打开 →`）只在 hover 出现 |
| 代码 / JSON | `#0A0E16` 底 + `#C7D3E6` 字 + `--pd-font-mono` + `--pd-r-md` |
| 图标 | 常态 `--pd-text-2`，激活 `--pd-primary-text`；除语义色外不做彩色图标 |

Element Plus 的暗色映射写在 `prisdoc-tokens.css` 的 `html.dark{}` 与 `html.dark .el-*` 段里 ——
`el-dialog`、`el-drawer`、`el-message`、`el-loading` 会被 teleport 到 body，**变量与覆盖必须放全局样式，不能带 scoped**。

## 三、图标与品牌资产

**品牌图标**（源自 Prisma / Prisdvl 的原始标识，透明底，非方形按高度等比缩放、不裁切拉伸）：

| 资产 | 尺寸 | 用在哪 |
| --- | --- | --- |
| `public/brand/prisdvl-nexus.png` | 192×151 | 侧边栏品牌、登录/注册字标、Agent 工作台标题、全局助手（悬浮球 / 头部 / 消息头像）、AI 对话头像 |
| `public/favicon.png` | 64×64 | `<link rel="icon">` 与导出的 PPT HTML |

**图标原则：只保留「有功能、且没有文字可替代」的图标。**

- 保留：返回、侧栏折叠、下拉更多、发送箭头、RAG 模式切换、加载态、密码规则勾选/未勾选、错误提示。
- 已移除（装饰性）：侧栏导航、欢迎区输入框的 `+` 与麦克风、快捷胶囊、弹出菜单项、标题栏按钮（保存/导出/协作/刷新数据/重置指标/检测故障/执行任务）、
  小标题（文本分析 / 摘要）、操作结果卡片、协作面板信息行、下拉菜单项图标（`el-dropdown-item` 的 `icon` 属性）。
- 文档卡片封面不再放文件图标：改用纯 CSS 的叠纸纹理（`.card-cover::before`）+ 一层柔光，保留「这是一份文档」的暗示而不引入字形图标。

## 四、禁止项

紫色渐变、发光/霓虹、玻璃拟态（`backdrop-filter`）、彩色糖果色卡片、花哨动效、超过 300ms 的动画、
外链字体与图标 CDN。动效只动具体属性，`prefers-reduced-motion` 必须生效。

## 五、两个刻意的例外

1. **A4 纸永远浅色**：编辑器的 `.paper` 固定 `#F7F8FA` 底 + `#1F2329` 字 —— 深色主题里它是「纸」，浅色主题里它与页面底（`#F6F7FA`）几乎同色，所以补了一圈 `inset 0 0 0 1px` 的极淡描边（不占布局）来保住边界。
2. **封面不用糖果色**：文档卡片的四套封面色是同一族深蓝梯度（`#0B1730→#12294F` 等），靠明度差区分，不靠色相；两套主题下都不改（相当于"封面图"）。

## 六、双主题（浅色 / 深色 / 自动）

- **入口**：落地页胶囊导航、登录/注册页右上角、工作台侧栏底部（三段式胶囊 浅色 / 深色 / 自动）。
- **存储**：`localStorage.prisdoc-theme = light | dark | system`；首屏由 `index.html` 内联脚本先定住，避免闪白/闪黑。
- **实现**：`src/utils/theme.js` 只写 `<html data-theme>`，深色时额外加 `html.dark` 让 Element Plus 的暗色变量表生效；颜色全部走令牌，**组件里没有一行判断主题**。
- **亮色令牌要点**：底 `#F6F7FA`、卡片纯白、主色仍是 `#2457D6`（hover 换成更深的 `#1C46B4`）、文字 `#12161F / #4A5464 / #788392`、状态色改深（`#C63A42 / #9A6A10 / #1B7A46`）以保证白底对比度；文字渐变换成 `#16327F → #2457D6 → #3978F6`（浅底上左端不能再是冰蓝）。
- **Element Plus**：组件级覆盖统一写成 `html .el-*`（不带 `.dark`），两套主题共用；`--el-*` 变量分别在 `html.dark` 与 `html[data-theme="light"]` 里各写一份。

## 七、布局红线

本次换肤只改视觉：DOM 层级、栅格、组件位置、页面分区、侧边栏宽度（工作台 240px、编辑器左栏 260px / AI 栏 380px）、
内容区 `max-width: 1050px`、A4 纸 780px、卡片列数均未改动。
允许的属性：颜色、背景、边框、圆角、阴影、透明度、字体、字距、行高、过渡、光标。
