<template>
  <div class="lp">
    <!-- 顶部悬浮胶囊导航（参考 Prisma 落地页） -->
    <header class="lp-nav">
      <div class="lp-nav-inner">
        <div class="lp-brand" @click="scrollTop">
          <img class="lp-brand-mark" src="/brand/prisdvl-nexus.png" alt="" />
          <span class="lp-brand-text">Prisdoc</span>
        </div>
        <nav class="lp-links">
          <a href="#product">产品</a>
          <a href="#flow">流程</a>
          <a href="#stack">实现</a>
          <a href="#faq">问题</a>
        </nav>
        <div class="lp-actions">
          <button class="lp-login" type="button" @click="enter">{{ signedIn ? '进入工作台' : '登录' }}</button>
        </div>
      </div>
    </header>

    <!-- Hero -->
    <section class="lp-hero">
      <div class="lp-dots" aria-hidden="true"></div>
      <div class="lp-words" aria-hidden="true">
        <span v-for="(word, i) in WORDS" :key="word" :style="{ top: `${18 + i * 19}%` }">{{ word }}</span>
      </div>

      <p class="lp-eyebrow lp-eyebrow-center"><span class="lp-dot"></span>文档 · 知识库 · Agent</p>
      <h1 class="lp-title">Prisdoc</h1>
      <p class="lp-sub">
        把散落的资料变成可检索的知识，再让一句指令直接落到文档上：上传、提问、改写、生成演示文稿，一条链路走完。
      </p>
      <div class="lp-cta">
        <button class="lp-btn lp-btn-primary" type="button" @click="enter">
          {{ signedIn ? '进入工作台' : '用演示账号进入' }}
        </button>
        <a class="lp-btn lp-btn-ghost" href="#product">看看怎么用</a>
      </div>
      <p class="lp-hint">Mock 模式无需后端；演示账号 张伟 / prisdoc123</p>
    </section>

    <!-- 产品 -->
    <section id="product" class="lp-section">
      <p class="lp-eyebrow">产品</p>
      <h2 class="lp-h2">资料进来，文档出去</h2>
      <p class="lp-lead">上传即分段建索引；提问带原文引用；指令直接写回正文，不用来回复制粘贴。</p>

      <div class="lp-flow">
        <div class="lp-flow-card">
          <span class="lp-flow-no">01</span>
          <h3>上传与索引</h3>
          <p>章节 / 固定长度 / 语义三种分段策略</p>
        </div>
        <span class="lp-flow-line" aria-hidden="true"></span>
        <div class="lp-flow-card">
          <span class="lp-flow-no">02</span>
          <h3>检索与问答</h3>
          <p>混合检索 + 重排，答案附原文片段</p>
        </div>
        <span class="lp-flow-line" aria-hidden="true"></span>
        <div class="lp-flow-card">
          <span class="lp-flow-no">03</span>
          <h3>写回与导出</h3>
          <p>改段落、生成演示文稿、导出 Word</p>
        </div>
      </div>

      <div class="lp-grid-3">
        <div>
          <h4>知识库</h4>
          <p>文档全文入库，按章节切分并保留来源，回答可以逐条核对。</p>
        </div>
        <div>
          <h4>工具调用</h4>
          <p>检索、改写、生成幻灯片都是可调用工具，每次调用的参数与结果都能展开看。</p>
        </div>
        <div>
          <h4>审批闭环</h4>
          <p>删除、覆盖这类高危动作先停下等确认，确认后才继续执行。</p>
        </div>
      </div>

      <div class="lp-grid-4">
        <div><h5>制度与规范</h5><p>条款缺责任主体、缺考核口径这类问题，自查一遍再发。</p></div>
        <div><h5>会议与周报</h5><p>纪要、周报有骨架，填内容比从空白页开始快。</p></div>
        <div><h5>技术文档</h5><p>架构说明按「网关 → 服务 → 能力 → 基础设施」分层重排。</p></div>
        <div><h5>汇报材料</h5><p>一份文档直接拆成 5-8 页，下载即放映。</p></div>
      </div>
    </section>

    <!-- 流程 -->
    <section id="flow" class="lp-section">
      <p class="lp-eyebrow">流程</p>
      <h2 class="lp-h2">一次提问怎么走</h2>
      <p class="lp-lead">选文档 → 检索 → 读原文 → 作答并挂引用；需要动文件时再走工具与审批。</p>

      <div class="lp-terminal">
        <div class="lp-terminal-bar">
          <span class="lp-dot-red"></span><span class="lp-dot-yellow"></span><span class="lp-dot-green"></span>
          <span class="lp-terminal-title">prisdoc — agent — trace</span>
        </div>
        <div class="lp-terminal-body">
          <p><span class="lp-t-ok">▸</span> 任务：总结《企业办公用电安全管理规定》，列出三条行动项</p>
          <p class="lp-t-dim">├─ rag-search「用电 责任 考核」 · 命中 4 段</p>
          <p class="lp-t-dim">├─ document-read · 读取当前文档 1.8k 字</p>
          <p class="lp-t-dim">├─ summarize · 200 字以内</p>
          <p class="lp-t-ok">└─ answer · 已生成，附 3 处原文引用</p>
          <p class="lp-t-cursor">prisdoc@workspace:~$ <span class="lp-caret"></span></p>
        </div>
      </div>

      <div class="lp-steps">
        <div v-for="(step, i) in STEPS" :key="step.title" class="lp-step">
          <span class="lp-step-no">{{ String(i + 1).padStart(2, '0') }}</span>
          <h4>{{ step.title }}</h4>
          <p>{{ step.desc }}</p>
        </div>
      </div>
    </section>

    <!-- 实现 -->
    <section id="stack" class="lp-section">
      <p class="lp-eyebrow">实现</p>
      <h2 class="lp-h2">五个服务，一套链路</h2>
      <p class="lp-lead">网关 + 用户 / 文件 / 文档 / AI 四个服务，前端只跟网关说话。</p>

      <div class="lp-chips">
        <span v-for="chip in CHIPS" :key="chip" class="lp-chip">{{ chip }}</span>
      </div>

      <dl class="lp-spec">
        <div class="lp-spec-row"><dt>后端</dt><dd>Spring Boot 3.2 · Spring Cloud · Nacos · MyBatis-Plus</dd></div>
        <div class="lp-spec-row"><dt>检索</dt><dd>分段索引 + RAG 检索，答案强制携带原文片段</dd></div>
        <div class="lp-spec-row"><dt>Agent</dt><dd>工具注册与执行 · 计划可见 · 高危操作人工审批</dd></div>
        <div class="lp-spec-row"><dt>异步</dt><dd>RabbitMQ 削峰 · 任务状态轮询 · 前端 700ms 轮询</dd></div>
        <div class="lp-spec-row"><dt>前端</dt><dd>Vue 3 · Vite · Element Plus · CSS 变量双主题</dd></div>
        <div class="lp-spec-row"><dt>部署</dt><dd>Docker Compose / Swarm · MinIO 存对象 · MySQL 存元数据</dd></div>
      </dl>
    </section>

    <!-- FAQ -->
    <section id="faq" class="lp-section">
      <p class="lp-eyebrow">常见问题</p>
      <h2 class="lp-h2">上线前想知道的</h2>
      <div class="lp-faq">
        <details v-for="item in FAQ" :key="item.q">
          <summary>{{ item.q }}</summary>
          <p>{{ item.a }}</p>
        </details>
      </div>
    </section>

    <ThemeCorner />

    <footer class="lp-foot">
      <div class="lp-foot-brand">
        <img class="lp-brand-mark" src="/brand/prisdvl-nexus.png" alt="" />
        <span>Prisdoc · 智能文档平台</span>
      </div>
      <div class="lp-foot-links">
        <a href="#product">产品</a>
        <a href="#flow">流程</a>
        <a href="#stack">实现</a>
        <a href="#faq">问题</a>
      </div>
      <p class="lp-foot-dim">Spring Cloud + Vue 3 + RAG + Agent · 演示数据由 Mock 模式提供</p>
    </footer>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import ThemeCorner from '../../components/ThemeCorner.vue'
import { STORAGE_KEYS } from '../../constants'

const router = useRouter()

const signedIn = computed(() => {
  try {
    return Boolean(localStorage.getItem(STORAGE_KEYS.ACCESS_TOKEN))
  } catch {
    return false
  }
})

const enter = () => router.push(signedIn.value ? '/dashboard' : '/login')
const scrollTop = () => window.scrollTo({ top: 0, behavior: 'smooth' })

const WORDS = ['分段索引', '原文引用', '写回正文', '演示文稿']

const STEPS = [
  { title: '选文档', desc: '从云端文档库或当前打开的文档开始，也可以直接提问。' },
  { title: '检索', desc: '按分段做混合检索，命中的片段带分数返回。' },
  { title: '读原文', desc: '把片段读进上下文，避免只靠模型记忆作答。' },
  { title: '作答', desc: '结构化输出结论，并挂上引用位置。' },
  { title: '写回', desc: '需要改正文时，明确写回哪一段、怎么改。' },
  { title: '留痕', desc: '版本 +1、可回溯、可导出，改动都留记录。' }
]

const CHIPS = [
  'Spring Cloud', 'Spring AI', 'Nacos', 'MySQL', 'Redis', 'RabbitMQ', 'MinIO', 'Vue 3', 'Element Plus', 'Docker Swarm'
]

const FAQ = [
  {
    q: '支持哪些文件？',
    a: '当前以纯文本与 HTML 为主：上传后按章节或语义切分成片段入库，PDF / DOCX 走文件中转后同样进入分段流程。'
  },
  {
    q: '和直接用通用大模型聊天有什么区别？',
    a: '回答基于你自己的文档片段，带原文引用；模型只说「文档里没有」时不会硬编。改动直接落到正文，不用复制粘贴。'
  },
  {
    q: '高危操作会直接执行吗？',
    a: '不会。删除、覆盖、批量变更会中断执行并生成审批令牌，确认后才继续，取消则整条任务终止。'
  },
  {
    q: '没有后端能看吗？',
    a: '可以。仓库里带一套零依赖的 Mock 模式（npm run dev:mock），界面、交互、演示数据都能完整走通。'
  }
]
</script>

<style scoped>
/* #app 是 overflow:hidden，落地页自己当滚动容器，锚点跳转才会生效 */
.lp {
  height: 100vh;
  overflow-y: auto;
  scroll-behavior: smooth;
  background: var(--pd-bg);
  color: var(--pd-text);
}

/* ---------- 顶部胶囊导航 ---------- */
.lp-nav {
  position: fixed;
  top: 20px;
  left: 0;
  right: 0;
  z-index: 20;
  display: flex;
  justify-content: center;
  pointer-events: none;
}
.lp-nav-inner {
  pointer-events: auto;
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 8px 8px 8px 14px;
  border: 1px solid var(--pd-border);
  border-radius: var(--pd-r-pill);
  background: color-mix(in oklab, var(--pd-surface) 92%, transparent);
  box-shadow: var(--pd-shadow-md);
}
.lp-brand { display: flex; align-items: center; gap: 8px; cursor: pointer; }
.lp-brand-mark { display: block; height: 24px; width: auto; }
.lp-brand-text { font-size: 15px; font-weight: 600; letter-spacing: -0.01em; color: var(--pd-text); }

.lp-links { display: flex; align-items: center; gap: 4px; }
.lp-links a {
  padding: 6px 12px;
  border-radius: var(--pd-r-pill);
  font-size: 13px;
  color: var(--pd-text-2);
  text-decoration: none;
  transition: background var(--pd-dur-fast) var(--pd-ease), color var(--pd-dur-fast) var(--pd-ease);
}
.lp-links a:hover { background: var(--pd-hover); color: var(--pd-text); }

.lp-actions { display: flex; align-items: center; gap: 8px; }
.lp-login {
  border: 0;
  border-radius: var(--pd-r-pill);
  background: var(--pd-primary);
  color: #E8EEF8;
  font-family: inherit;
  font-size: 13px;
  font-weight: 500;
  padding: 7px 16px;
  cursor: pointer;
  transition: background var(--pd-dur-fast) var(--pd-ease);
}
.lp-login:hover { background: var(--pd-primary-hover); }

/* ---------- Hero ---------- */
.lp-hero {
  position: relative;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 140px 32px 96px;
  overflow: hidden;
  text-align: center;
}

/* 点阵背景：一层重复圆点，再用径向遮罩淡出边缘 */
.lp-dots {
  position: absolute;
  inset: 0;
  background-image: radial-gradient(var(--pd-dots) 1px, transparent 1px);
  background-size: 22px 22px;
  -webkit-mask-image: radial-gradient(ellipse 70% 60% at 50% 42%, #000 20%, transparent 78%);
  mask-image: radial-gradient(ellipse 70% 60% at 50% 42%, #000 20%, transparent 78%);
  pointer-events: none;
}

/* 左侧竖排的浅色词，纯装饰 */
.lp-words {
  position: absolute;
  left: max(12px, calc(50% - 610px));
  top: 0;
  bottom: 0;
  width: 130px;
  pointer-events: none;
  -webkit-mask-image: linear-gradient(to right, #000 0%, transparent 92%);
  mask-image: linear-gradient(to right, #000 0%, transparent 92%);
}
.lp-words span {
  position: absolute;
  left: 0;
  font-size: 18px;
  font-weight: 600;
  letter-spacing: 0.02em;
  color: var(--pd-text);
  opacity: 0.06;
  white-space: nowrap;
}

.lp-eyebrow {
  margin: 0 0 18px;
  font-size: 12.5px;
  font-weight: 500;
  letter-spacing: 0.04em;
  color: var(--pd-primary-text);
}
.lp-eyebrow-center { display: inline-flex; align-items: center; gap: 8px; }
.lp-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--pd-primary);
}

.lp-title {
  margin: 0 0 22px;
  font-size: clamp(48px, 8vw, 88px);
  line-height: 1.02;
  font-weight: 600;
  letter-spacing: -0.03em;
  background: var(--pd-grad-text);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}

.lp-sub {
  margin: 0 0 34px;
  max-width: 620px;
  font-size: 15.5px;
  line-height: 1.75;
  color: var(--pd-text-2);
}

.lp-cta { display: flex; align-items: center; gap: 12px; }
.lp-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  height: 44px;
  padding: 0 22px;
  border-radius: var(--pd-r-md);
  border: 1px solid transparent;
  font-family: inherit;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  text-decoration: none;
  transition: background var(--pd-dur-fast) var(--pd-ease), border-color var(--pd-dur-fast) var(--pd-ease),
    color var(--pd-dur-fast) var(--pd-ease);
}
.lp-btn-primary { background: var(--pd-primary); color: #E8EEF8; }
.lp-btn-primary:hover { background: var(--pd-primary-hover); }
.lp-btn-ghost { background: transparent; border-color: var(--pd-border); color: var(--pd-text-2); }
.lp-btn-ghost:hover { background: var(--pd-hover); border-color: var(--pd-border-strong); color: var(--pd-text); }

.lp-hint { margin: 18px 0 0; font-size: 12px; color: var(--pd-text-3); }

/* ---------- 通用 section ---------- */
.lp-section {
  max-width: 1120px;
  margin: 0 auto;
  padding: 88px 32px;
  scroll-margin-top: 96px;
}
.lp-h2 {
  margin: 0 0 12px;
  font-size: clamp(28px, 3.4vw, 40px);
  line-height: 1.15;
  font-weight: 600;
  letter-spacing: -0.02em;
  color: var(--pd-text);
}
.lp-lead { margin: 0 0 40px; font-size: 14.5px; line-height: 1.75; color: var(--pd-text-2); max-width: 640px; }

/* ---------- 三张流程卡 + 连接线 ---------- */
.lp-flow { display: flex; align-items: center; gap: 0; margin-bottom: 44px; }
.lp-flow-card {
  flex: 1;
  min-width: 0;
  padding: 20px 22px;
  background: var(--pd-surface);
  border: 1px solid var(--pd-border);
  border-radius: var(--pd-r-lg);
  box-shadow: var(--pd-shadow-sm);
}
.lp-flow-no {
  display: block;
  margin-bottom: 14px;
  font-family: var(--pd-font-mono);
  font-size: 12px;
  color: var(--pd-primary-text);
}
.lp-flow-card h3 { margin: 0 0 6px; font-size: 15px; font-weight: 600; color: var(--pd-text); }
.lp-flow-card p { margin: 0; font-size: 12.5px; line-height: 1.6; color: var(--pd-text-3); }
.lp-flow-line {
  flex: 0 0 28px;
  height: 1px;
  background: var(--pd-border-strong);
}

.lp-grid-3 {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 32px;
  padding-top: 28px;
  border-top: 1px solid var(--pd-border);
}
.lp-grid-4 {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 32px;
  margin-top: 36px;
  padding-top: 28px;
  border-top: 1px solid var(--pd-border);
}
.lp-grid-3 h4, .lp-grid-4 h5 { margin: 0 0 8px; font-size: 14px; font-weight: 600; color: var(--pd-text); }
.lp-grid-3 p, .lp-grid-4 p { margin: 0; font-size: 13px; line-height: 1.7; color: var(--pd-text-2); }

/* ---------- 终端窗口 ---------- */
.lp-terminal {
  border: 1px solid var(--pd-border);
  border-radius: var(--pd-r-lg);
  background: var(--pd-input);
  box-shadow: var(--pd-shadow-md);
  overflow: hidden;
  margin-bottom: 32px;
}
.lp-terminal-bar {
  display: flex;
  align-items: center;
  gap: 7px;
  padding: 10px 14px;
  border-bottom: 1px solid var(--pd-border);
  background: var(--pd-surface-muted);
}
.lp-dot-red, .lp-dot-yellow, .lp-dot-green { width: 9px; height: 9px; border-radius: 50%; }
.lp-dot-red { background: #E5726D; }
.lp-dot-yellow { background: #E0B45C; }
.lp-dot-green { background: #6FBF8A; }
.lp-terminal-title { margin-left: 8px; font-family: var(--pd-font-mono); font-size: 12px; color: var(--pd-text-3); }
.lp-terminal-body { padding: 18px 20px 22px; font-family: var(--pd-font-mono); font-size: 12.5px; line-height: 2; color: var(--pd-text-2); }
.lp-terminal-body p { margin: 0; white-space: pre-wrap; }
.lp-t-dim { color: var(--pd-text-3); }
.lp-t-ok { color: var(--pd-success); }
.lp-t-cursor { margin-top: 6px; color: var(--pd-text); }
.lp-caret {
  display: inline-block;
  width: 7px;
  height: 14px;
  vertical-align: -2px;
  background: var(--pd-primary-text);
  animation: lp-blink 1.1s steps(1) infinite;
}
@keyframes lp-blink { 50% { opacity: 0; } }

/* ---------- 六步 ---------- */
.lp-steps {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 1px;
  background: var(--pd-border);
  border: 1px solid var(--pd-border);
  border-radius: var(--pd-r-lg);
  overflow: hidden;
}
.lp-step { background: var(--pd-surface); padding: 18px 20px; }
.lp-step-no { font-family: var(--pd-font-mono); font-size: 12px; color: var(--pd-text-3); }
.lp-step h4 { margin: 8px 0 6px; font-size: 14px; font-weight: 600; color: var(--pd-text); }
.lp-step p { margin: 0; font-size: 12.5px; line-height: 1.65; color: var(--pd-text-2); }

/* ---------- 技术栈 ---------- */
.lp-chips { display: flex; flex-wrap: wrap; gap: 8px; margin-bottom: 36px; }
.lp-chip {
  padding: 6px 14px;
  border: 1px solid var(--pd-border);
  border-radius: var(--pd-r-pill);
  background: var(--pd-surface);
  font-size: 12.5px;
  color: var(--pd-text-2);
}

.lp-spec { margin: 0; border-top: 1px solid var(--pd-border); }
.lp-spec-row {
  display: grid;
  grid-template-columns: 110px 1fr;
  gap: 16px;
  padding: 14px 2px;
  border-bottom: 1px solid var(--pd-border);
}
.lp-spec-row dt { font-size: 13px; color: var(--pd-text-3); }
.lp-spec-row dd { margin: 0; font-size: 13.5px; color: var(--pd-text); }

/* ---------- FAQ ---------- */
.lp-faq { border-top: 1px solid var(--pd-border); }
.lp-faq details { border-bottom: 1px solid var(--pd-border); }
.lp-faq summary {
  padding: 16px 2px;
  font-size: 14px;
  font-weight: 500;
  color: var(--pd-text);
  cursor: pointer;
  list-style: none;
}
.lp-faq summary::-webkit-details-marker { display: none; }
.lp-faq details p {
  margin: 0 0 18px;
  max-width: 760px;
  font-size: 13.5px;
  line-height: 1.75;
  color: var(--pd-text-2);
}

/* ---------- 页脚 ---------- */
.lp-foot {
  max-width: 1120px;
  margin: 0 auto;
  padding: 40px 32px 64px;
  border-top: 1px solid var(--pd-border);
}
.lp-foot-brand { display: flex; align-items: center; gap: 10px; font-size: 13.5px; font-weight: 500; color: var(--pd-text); }
.lp-foot-links { display: flex; flex-wrap: wrap; gap: 16px; margin: 16px 0 12px; }
.lp-foot-links a { font-size: 12.5px; color: var(--pd-text-2); text-decoration: none; }
.lp-foot-links a:hover { color: var(--pd-primary-text); }
.lp-foot-dim { margin: 0; font-size: 12px; color: var(--pd-text-3); }

@media (max-width: 980px) {
  .lp-words { display: none; }
  .lp-flow { flex-direction: column; align-items: stretch; }
  .lp-flow-line { flex: 0 0 20px; width: 1px; height: 20px; margin: 0 auto; }
  .lp-grid-3, .lp-grid-4, .lp-steps { grid-template-columns: 1fr; }
  .lp-links { display: none; }
}

@media (prefers-reduced-motion: reduce) {
  .lp-caret { animation: none; }
}
</style>
