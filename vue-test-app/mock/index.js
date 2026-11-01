/**
 * Prisdoc 前端 Mock 模式（Vite 开发服务器中间件）
 * ---------------------------------------------------------------------------
 * 作用：不启动任何后端微服务，也能在浏览器里完整走通 Prisdoc 的界面与交互。
 *
 * 使用：
 *   npm run dev:mock      # 等价于 vite --mode mock
 *
 * 原理：
 *   在 Vite 的 configureServer 阶段注册一个中间件，命中 /api/** 时直接返回
 *   本地假数据；未命中的请求继续交给 Vite 自身（含 proxy），因此
 *   `npm run dev` 连真实后端的行为完全不受影响。
 *
 * 注意：
 *   - 本文件运行在 Node 端，不打包进浏览器产物；
 *   - 状态保存在内存里（create / update / delete / 异步任务 / 指标），
 *     重启 dev server 即恢复初始数据。
 */

import {
  USERS,
  MODELS,
  TOOLS,
  DOCS,
  VERSIONS,
  FILES,
  TASKS,
  CONVERSATIONS,
  KNOWLEDGE_JOBS,
  SEGMENT_STRATEGIES,
  RAG_STATISTICS,
  FAULTS,
  buildMetrics,
  buildSegments,
  buildPptHtml,
  mockToken
} from './data.js'

const API_PREFIX = '/api'
const JOB_RUNNING_MS = 700 // 异步任务“处理中”的模拟时长
const NETWORK_DELAY_MS = 60 // 每个请求的模拟网络延迟

const clone = (value) => JSON.parse(JSON.stringify(value))
const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms))
const nowIso = () => new Date().toISOString().slice(0, 19)

// ---------------------------------------------------------------------------
// 内存状态
// ---------------------------------------------------------------------------

function createState() {
  return {
    users: clone(USERS),
    docs: clone(DOCS),
    versions: clone(VERSIONS),
    files: clone(FILES),
    tasks: clone(TASKS),
    conversations: clone(CONVERSATIONS),
    knowledgeJobs: clone(KNOWLEDGE_JOBS),
    metrics: buildMetrics(),
    faults: clone(FAULTS),
    segments: new Map(), // documentId -> segments[]
    jobs: new Map(), // jobId -> { status, result, createdAt }
    docSeq: 1000,
    jobSeq: 0,
    fileSeq: 0,
    traceSeq: 0
  }
}

// ---------------------------------------------------------------------------
// 响应辅助
// ---------------------------------------------------------------------------

function sendJson(res, payload, status = 200) {
  if (res.writableEnded) return
  const body = JSON.stringify(payload)
  res.statusCode = status
  res.setHeader('Content-Type', 'application/json; charset=utf-8')
  res.setHeader('Cache-Control', 'no-store')
  res.setHeader('X-Prisdoc-Mock', '1')
  res.end(body)
}

const ok = (res, data = null, message = 'success') => sendJson(res, { code: 200, message, data })
const fail = (res, message = '操作失败', code = 500) => sendJson(res, { code, message, data: null })

function sendRaw(res, body, contentType, extraHeaders = {}, status = 200) {
  if (res.writableEnded) return
  res.statusCode = status
  res.setHeader('Content-Type', contentType)
  res.setHeader('Cache-Control', 'no-store')
  res.setHeader('X-Prisdoc-Mock', '1')
  for (const [key, value] of Object.entries(extraHeaders)) res.setHeader(key, value)
  res.end(body)
}

// ---------------------------------------------------------------------------
// 请求体解析
// ---------------------------------------------------------------------------

function readBody(req) {
  return new Promise((resolve) => {
    const chunks = []
    req.on('data', (chunk) => chunks.push(chunk))
    req.on('end', () => {
      const raw = Buffer.concat(chunks).toString('utf8')
      if (!raw) return resolve({ raw: '', json: null })
      try {
        resolve({ raw, json: JSON.parse(raw) })
      } catch {
        resolve({ raw, json: null })
      }
    })
    req.on('error', () => resolve({ raw: '', json: null }))
  })
}

// ---------------------------------------------------------------------------
// 业务数据构造
// ---------------------------------------------------------------------------

function toDocBrief(doc) {
  return {
    id: doc.id,
    title: doc.title,
    fileId: doc.fileId,
    category: doc.category,
    ownerId: doc.ownerId,
    createTime: doc.createTime,
    updateTime: doc.updateTime,
    summary: doc.summary,
    currentVersion: doc.currentVersion
  }
}

function textAnalyze(content) {
  const text = String(content || '')
  const chinese = (text.match(/[\u4e00-\u9fa5]/g) || []).length
  const english = (text.match(/[a-zA-Z]+/g) || []).length
  const digits = (text.match(/\d+/g) || []).length
  const punctuations = (text.match(/[，。；：！？、,.;:!?"'()《》【】]/g) || []).length
  const lines = text.split(/\n+/).filter((line) => line.trim()).length
  return {
    totalCharacters: text.length,
    chineseCharacters: chinese,
    englishWords: english,
    digits,
    punctuations,
    spaces: (text.match(/\s/g) || []).length,
    lines
  }
}

const KEYWORD_POOL = [
  '用电安全', '配电设施', '隐患整改', '责任划分', '绩效考核',
  '需求评审', '优先级', '跨团队依赖', '灰度发布', '验收标准',
  '微服务', '网关路由', 'RAG 检索', '异步任务', '可观测性',
  '知识库', '向量化', 'Agent 规划', '人工审批', '版本回溯'
]

const STOP_WORDS = new Set([
  '我们', '你们', '他们', '可以', '以及', '进行', '相关', '需要', '通过', '如果', '并且',
  '同时', '或者', '这个', '那个', '以下', '其中', '对于', '按照', '根据', '目前', '已经',
  '必须', '不得', '应当', '工作', '内容', '情况', '方面', '要求', '员工', '公司', '人员',
  '规定', '负责', '一个', '没有', '就是', '不得超', '的规', '定了',
  '本规定', '本手册', '本清单', '本文', '如下', '其中', '有关'
])

function countOccurrences(text, word) {
  let count = 0
  let index = text.indexOf(word)
  while (index >= 0) {
    count += 1
    index = text.indexOf(word, index + word.length)
  }
  return count
}

/**
 * 关键词提取：
 * 1) 先命中业务词表（权重最高，结果可读性好）；
 * 2) 再把正文切成长中文串，统计 2~6 字词频，按“词频 × 词长”打分；
 * 3) 去掉被更长关键词包含的碎片，最终按 count 截断。
 */
function pickKeywords(content, count = 5) {
  const text = String(content || '')

  const poolHits = KEYWORD_POOL
    .map((word) => {
      const hits = countOccurrences(text, word)
      return { word, hits, score: hits * 2 + word.length * 0.5 }
    })
    .filter((item) => item.hits > 0)

  const candidates = new Map()
  for (const run of text.match(/[\u4e00-\u9fa5]{2,}/g) || []) {
    const capped = run.slice(0, 80)
    for (let len = 2; len <= 6; len++) {
      for (let i = 0; i + len <= capped.length; i++) {
        const term = capped.slice(i, i + len)
        if (STOP_WORDS.has(term)) continue
        candidates.set(term, (candidates.get(term) || 0) + 1)
      }
    }
  }

  const mined = [...candidates.entries()]
    .filter(([term, times]) => times >= 2 && term.length >= 3 && !STOP_WORDS.has(term))
    .map(([term, times]) => ({ word: term, score: times * (1 + term.length * 0.9) }))
    .sort((a, b) => b.score - a.score)

  const merged = [...poolHits.sort((a, b) => b.score - a.score), ...mined]
  const picked = []
  for (const item of merged) {
    if (picked.length >= count) break
    const duplicated = picked.some((word) => word.includes(item.word) || item.word.includes(word))
    if (!duplicated) picked.push(item.word)
  }

  const fillers = ['AI 助手', '文档管理', '效率提升', '知识沉淀', '智能办公']
  while (picked.length < count && fillers.length) picked.push(fillers.shift())

  return picked.map((word, index) => ({ word, weight: Number((0.96 - index * 0.06).toFixed(2)) }))
}

/**
 * 摘要：优先挑含数字 / 百分比的句子（通常是关键结论），按原文顺序拼回。
 */
function buildSummary(content, maxLength = 200) {
  const text = String(content || '').replace(/\s+/g, ' ')
  const sentences = text
    .split(/(?<=[。！？])/)
    .map((sentence) => sentence.trim())
    .filter((sentence) => sentence.length >= 16 && !/^(周期|编写人|会议时间|参会人员)/.test(sentence))

  const ranked = sentences
    .map((sentence, index) => ({
      sentence,
      index,
      score: sentence.length * 0.4 + (/[\d%]/.test(sentence) ? 10 : 0) + (/\d+%/.test(sentence) ? 8 : 0)
    }))
    .sort((a, b) => b.score - a.score)
    .slice(0, 2)
    .sort((a, b) => a.index - b.index)

  const cleanSentence = (sentence) => sentence
    // 去掉句首残留的“三、隐患报告与应急处理 1.”这类标题编号
    .replace(/^[一二三四五六七八九十]+、[^\d。]{2,15}?\s*\d+\.\s*/, '')
    .replace(/^[一二三四五六七八九十]+、[^\d。]{2,15}?[：:]\s*/, '')
    .replace(/^\d+\.\s*/, '')
    .trim()

  const summary = (ranked.map((item) => cleanSentence(item.sentence)).filter(Boolean).join('')
    || text.slice(0, 160))
  const limit = Math.max(80, Number(maxLength) || 200)
  return summary.length > limit ? `${summary.slice(0, limit - 1)}…` : summary
}

function buildPptToolResult(options = {}) {
  const title = options.title || 'Prisdoc 项目汇报'
  const outline = options.outline || '项目背景与痛点\n核心微服务架构\nRAG 知识检索增强\n未来商业展望'
  return {
    toolName: 'html-ppt-generate',
    stepId: 'step-3',
    status: 'success',
    message: `已生成 ${String(outline).split('\n').filter(Boolean).length + 1} 页演示文稿`,
    requiresAction: false,
    durationMs: 9180,
    data: {
      title,
      theme: options.theme || 'tokyo-night',
      htmlContent: buildPptHtml({ title, theme: options.theme, outline }),
      fileName: `${title}.html`,
      filePath: `/doc-ai/ppt/${Date.now()}.html`,
      downloadUrl: '/api/skills/file/download?bucket=doc-ai&objectName=prisdoc-demo.html',
      pageCount: String(outline).split('\n').filter(Boolean).length + 1
    }
  }
}

/**
 * 生成一次（假）Agent 执行结果。
 * 分支依据用户输入，尽量贴近真实后端返回结构。
 */
function buildAgentResult(payload = {}) {
  const task = String(payload.task || '')
  const model = payload.model || 'qwen3.6-plus'
  const conversationId = payload.conversationId || 'conv-mock-001'
  const traceId = `trace-mock-${String(++globalSeq.trace).padStart(4, '0')}`
  const base = {
    traceId,
    conversationId,
    model,
    iterations: 2,
    executionTimeMs: 4210,
    tokensUsed: 2846,
    dryRun: Boolean(payload.dryRun)
  }

  // 1) 生成演示文稿
  if (/ppt|演示|幻灯片|汇报材料/i.test(task)) {
    return {
      ...base,
      status: 'success',
      answer: `已根据《${payload.context?.documentTitle || '技术架构说明'}》生成 5 页演示文稿，主题为 tokyo-night。\n\n页面结构：\n1. 封面：Prisdoc 项目汇报\n2. 项目背景与痛点\n3. 核心微服务架构\n4. RAG 知识检索增强\n5. 未来商业展望\n\n点击下方「下载」可直接得到可放映的 HTML 文件，或在浏览器中预览。`,
      plan: [
        { id: 'step-1', description: '读取文档并提取架构要点', toolName: 'document-read', status: 'success', riskLevel: 'low', reasoning: '需要原文才能生成贴合的页面内容。', observation: '读取 1 篇文档，抽取 12 个要点。' },
        { id: 'step-2', description: '提炼页面标题与要点', toolName: 'keyword-extract', status: 'success', riskLevel: 'low', observation: '输出 5 个章节标题。' },
        { id: 'step-3', description: '生成 HTML 演示文稿', toolName: 'html-ppt-generate', status: 'success', riskLevel: 'low', observation: '生成 5 页 HTML，体积 18.4 KB。' }
      ],
      toolResults: [buildPptToolResult({ title: payload.context?.documentTitle || 'Prisdoc 项目汇报', theme: payload.context?.theme })]
    }
  }

  // 2) 删除类高危操作 -> 触发审批闭环
  if (/删除|删掉|移除|清除/.test(task)) {
    const token = `apr-${Date.now().toString(36)}`
    return {
      ...base,
      status: 'action_required',
      iterations: 1,
      executionTimeMs: 1620,
      answer: '即将永久删除文件《客户合同评审要点清单-旧版.docx》。该操作不可恢复，请确认后继续。',
      approvalToken: token,
      pendingApproval: {
        toolName: 'file-delete',
        params: { objectName: '客户合同评审要点清单-旧版.docx', documentId: 105 },
        agentApprovalToken: token,
        riskLevel: 'high'
      },
      plan: [
        { id: 'step-1', description: '定位目标文件', toolName: 'document-read', status: 'success', riskLevel: 'low' },
        { id: 'step-2', description: '永久删除文件', toolName: 'file-delete', status: 'waiting_user', riskLevel: 'high', requiresApproval: true, reasoning: '删除不可恢复，需要人工审批。' }
      ],
      toolResults: [
        { toolName: 'file-delete', status: 'action_required', requiresAction: true, message: '等待人工审批后执行删除', data: { objectName: '客户合同评审要点清单-旧版.docx', approvalToken: token } }
      ]
    }
  }

  // 3) 润色 / 纠错 / 改写 -> 返回前端可写入的内容
  if (/润色|纠错|改写|优化|更专业|扩写/.test(task)) {
    const selection = payload.context?.selectedText || ''
    const rewritten = selection
      ? `为杜绝安全隐患，严禁私拉乱接电线，离岗时须关闭相关设备电源。`
      : `为杜绝安全隐患，严禁私拉乱接电线，离岗时须关闭相关设备电源。各部门负责人应每周开展一次自查，发现问题当日报行政部备案。`
    return {
      ...base,
      status: 'success',
      iterations: 2,
      answer: `已完成润色，主要调整如下：\n\n1. 将口语化表述「私拉电线」统一为规范用语「私拉乱接电线」；\n2. 补充「离岗时关闭电源」的动作主体，避免责任不清；\n3. 增加自查与报备要求，使条款具备可执行性。\n\n修改后的内容已写入当前文档，点击「保存」后同步到云端。`,
      plan: [
        { id: 'step-1', description: '读取当前段落原文', toolName: 'document-read', status: 'success', riskLevel: 'low' },
        { id: 'step-2', description: '润色并生成替换内容', toolName: 'text-correct', status: 'success', riskLevel: 'low' },
        { id: 'step-3', description: '交回前端写入文档', toolName: 'document-write', status: 'success', riskLevel: 'medium' }
      ],
      toolResults: [
        {
          toolName: 'document-write',
          status: 'success',
          message: '内容已返回前端编辑器',
          data: {
            requiresFrontendWrite: true,
            result: 'frontend-document-write',
            content: rewritten,
            contentFormat: 'text',
            writeMode: selection ? 'replace-selection' : 'append',
            selectionText: selection,
            changeLog: selection ? '替换选中段落' : '追加润色后的段落',
            contentLength: rewritten.length
          }
        }
      ]
    }
  }

  // 4) 默认：问答 / 总结
  return {
    ...base,
    status: 'success',
    iterations: 2,
    executionTimeMs: 3860,
    answer: `根据当前文档，我整理出以下要点：\n\n一、核心结论\n1. 制度类文档需明确「谁使用、谁负责」的责任划分，并把考核标准量化到具体分值。\n2. 技术类文档建议按「网关层 → 业务服务层 → AI 能力层 → 基础设施层」分层描述，再补充三条关键链路。\n3. 会议纪要应同时记录结论、待办与责任人，便于后续跟踪闭环。\n\n二、建议补充的内容\n1. 增加一组可量化的验收指标（如响应时间 P95、答案采纳率）。\n2. 对高风险动作（删除、覆盖、批量变更）保留人工审批环节。\n\n三、下一步\n如果需要，我可以把以上内容直接写入当前文档，或生成一份汇报用演示文稿。`,
    plan: [
      { id: 'step-1', description: '读取当前文档内容', toolName: 'document-read', status: 'success', riskLevel: 'low', reasoning: '先拿到原文再做归纳，避免凭空生成。', observation: '读取 1 篇文档，约 1.8k 字。' },
      { id: 'step-2', description: '检索知识库补充依据', toolName: 'rag-search', status: 'success', riskLevel: 'low', observation: '召回 4 个片段，最高相似度 0.87。' },
      { id: 'step-3', description: '生成结构化回答', toolName: 'direct-answer', status: 'success', riskLevel: 'low', observation: '输出 3 段共 386 字。' }
    ],
    toolResults: [
      {
        toolName: 'rag-search',
        status: 'success',
        message: '召回 4 个知识片段',
        durationMs: 612,
        data: {
          hits: 4,
          topScore: 0.87,
          sources: [
            { documentId: 101, title: '企业办公用电安全管理规定', score: 0.87 },
            { documentId: 103, title: 'Prisdoc 平台技术架构说明', score: 0.81 }
          ]
        }
      },
      {
        toolName: 'direct-answer',
        status: 'success',
        message: '已生成回答',
        durationMs: 1180,
        data: { answerLength: 386 }
      }
    ]
  }
}

// 简单自增序列（用于 traceId / jobId）
const globalSeq = { trace: 1000, job: 0 }

function buildTaskSnapshot(task) {
  const steps = (task.plan || []).map((step, index) => ({
    ...step,
    startedAt: Date.parse(task.createdAt || nowIso()) + index * 1200,
    durationMs: 600 + index * 900
  }))
  return {
    ...task,
    steps,
    timeline: steps.map((step, index) => ({
      index: index + 1,
      stepId: step.id,
      status: step.status,
      toolName: step.toolName,
      at: new Date(Date.parse(task.createdAt || nowIso()) + index * 1400).toISOString()
    }))
  }
}

// ---------------------------------------------------------------------------
// 路由表
// ---------------------------------------------------------------------------

function createRouter(state) {
  const routes = []
  const add = (method, pattern, handler) => {
    routes.push({ method, pattern, handler })
  }

  // ------------------------------ 用户 / 鉴权 ------------------------------

  add('POST', /^\/api\/users\/login$/, async (req, res, { body }) => {
    const payload = body.json || {}
    const username = String(payload.username || '').trim()
    const password = String(payload.password || '')
    if (!username || !password) return fail(res, '用户名或密码不能为空', 400)

    const user = state.users.find((item) => item.username === username) || {
      id: 1,
      username,
      email: `${username}@prisdoc.example.com`,
      phone: '13800138000',
      role: 'ADMIN',
      status: 1,
      createTime: '2025-09-12T09:30:00'
    }

    ok(res, {
      accessToken: mockToken(user),
      refreshToken: mockToken({ ...user, exp: undefined }),
      user
    }, '登录成功')
  })

  add('POST', /^\/api\/users\/register$/, async (req, res, { body }) => {
    const payload = body.json || {}
    const username = String(payload.username || '').trim()
    if (!username) return fail(res, '用户名不能为空', 400)
    const exists = state.users.find((item) => item.username === username)
    if (exists) return fail(res, '用户名已存在', 400)

    const user = {
      id: state.users.length + 1,
      username,
      email: payload.email || `${username}@prisdoc.example.com`,
      phone: payload.phone || '',
      role: 'USER',
      status: 1,
      createTime: nowIso()
    }
    state.users.push(user)
    ok(res, user, '注册成功')
  })

  add('GET', /^\/api\/users\/lookup$/, async (req, res, { query }) => {
    const username = String(query.get('username') || '').trim()
    const user = state.users.find((item) => item.username === username) || {
      id: 2,
      username: username || '李静',
      role: 'USER',
      status: 1,
      email: 'lijing@prisdoc.example.com'
    }
    ok(res, user)
  })

  add('GET', /^\/api\/users\/(\d+)$/, async (req, res, { match }) => {
    const id = Number(match[1])
    const user = state.users.find((item) => item.id === id) || state.users[0]
    ok(res, user)
  })

  // ------------------------------ 文档 ------------------------------

  add('GET', /^\/api\/documents\/user\/([^/]+)$/, async (req, res, { match }) => {
    const userId = String(match[1])
    const list = state.docs
      .filter((doc) => String(doc.ownerId) === userId || userId === '1')
      .map(toDocBrief)
      .sort((a, b) => String(b.updateTime).localeCompare(String(a.updateTime)))
    ok(res, list)
  })

  add('GET', /^\/api\/documents\/search$/, async (req, res, { query }) => {
    const keyword = String(query.get('keyword') || '').trim().toLowerCase()
    const list = state.docs
      .filter((doc) => !keyword
        || doc.title.toLowerCase().includes(keyword)
        || String(doc.content).toLowerCase().includes(keyword)
        || String(doc.summary).toLowerCase().includes(keyword))
      .map(toDocBrief)
    ok(res, list)
  })

  add('GET', /^\/api\/documents\/([^/]+)\/versions$/, async (req, res, { match }) => {
    const id = Number(match[1])
    const versions = state.versions[id] || [{
      id: `v-${id}-1`,
      versionNumber: 1,
      title: state.docs.find((doc) => doc.id === id)?.title || '未命名文档',
      createTime: nowIso(),
      operator: '张伟',
      remark: '初始版本'
    }]
    ok(res, versions)
  })

  add('POST', /^\/api\/documents\/([^/]+)\/restore\/(\d+)$/, async (req, res, { match }) => {
    const id = Number(match[1])
    const versionNumber = Number(match[2])
    const doc = state.docs.find((item) => item.id === id)
    if (!doc) return fail(res, '文档不存在', 404)

    const versions = state.versions[id] || []
    const version = versions.find((item) => item.versionNumber === versionNumber)
    doc.currentVersion = versionNumber
    doc.updateTime = nowIso()
    if (version) doc.summary = `已回溯至 V${versionNumber}（${version.remark || '历史版本'}）`
    ok(res, toDocBrief(doc), `已恢复到 V${versionNumber}`)
  })

  add('POST', /^\/api\/documents\/([^/]+)\/collaborators\/([^/]+)$/, async (req, res, { match, query }) => {
    const collaboratorUserId = String(match[2])
    const role = String(query.get('role') || 'editor')
    ok(res, {
      documentId: Number(match[1]),
      collaboratorUserId,
      role,
      grantedAt: nowIso(),
      collaboratorName: state.users.find((user) => String(user.id) === collaboratorUserId)?.username || '协作者'
    }, '协作者已添加')
  })

  add('POST', /^\/api\/documents$/, async (req, res, { body }) => {
    const payload = body.json || {}
    const doc = {
      id: ++state.docSeq,
      title: payload.title || '未命名文档',
      fileId: payload.fileId || `file-mock-${state.docSeq}`,
      category: payload.category || 'default',
      ownerId: Number(payload.ownerId || 1),
      createTime: nowIso(),
      updateTime: nowIso(),
      summary: '',
      content: payload.content || '<p>这是一个新创建的空白文档，可以直接开始编辑。</p>',
      currentVersion: 1
    }
    state.docs.unshift(doc)
    state.versions[doc.id] = [{
      id: `v-${doc.id}-1`,
      versionNumber: 1,
      title: doc.title,
      createTime: doc.createTime,
      operator: '张伟',
      remark: '创建文档'
    }]
    ok(res, toDocBrief(doc), '文档已创建')
  })

  add('PUT', /^\/api\/documents\/([^/]+)$/, async (req, res, { match, body }) => {
    const id = Number(match[1])
    const doc = state.docs.find((item) => item.id === id)
    if (!doc) return fail(res, '文档不存在', 404)

    const payload = body.json || {}
    if (payload.content !== undefined) doc.content = payload.content
    if (payload.title !== undefined) doc.title = payload.title
    if (payload.summary !== undefined) doc.summary = payload.summary
    doc.updateTime = nowIso()
    doc.currentVersion = (doc.currentVersion || 1) + 1
    state.versions[id] = state.versions[id] || []
    state.versions[id].unshift({
      id: `v-${id}-${doc.currentVersion}`,
      versionNumber: doc.currentVersion,
      title: doc.title,
      createTime: doc.updateTime,
      operator: '张伟',
      remark: '在线编辑自动保存'
    })
    ok(res, toDocBrief(doc), '保存成功')
  })

  add('DELETE', /^\/api\/documents\/([^/]+)$/, async (req, res, { match }) => {
    const id = Number(match[1])
    const index = state.docs.findIndex((item) => item.id === id)
    if (index >= 0) state.docs.splice(index, 1)
    delete state.versions[id]
    ok(res, true, '已删除')
  })

  add('GET', /^\/api\/documents\/([^/]+)$/, async (req, res, { match }) => {
    const id = Number(match[1])
    const doc = state.docs.find((item) => item.id === id)
    if (!doc) return fail(res, '文档不存在', 404)
    ok(res, doc)
  })

  // ------------------------------ 文件 ------------------------------

  add('GET', /^\/api\/files\/list$/, async (req, res, { query }) => {
    const page = Number(query.get('page') || 1)
    const size = Number(query.get('size') || 10)
    const start = (page - 1) * size
    ok(res, {
      records: state.files.slice(start, start + size),
      total: state.files.length,
      page,
      size,
      pages: Math.ceil(state.files.length / size)
    })
  })

  add('GET', /^\/api\/files\/search$/, async (req, res, { query }) => {
    const keyword = String(query.get('keyword') || '').toLowerCase()
    const records = state.files.filter((file) => !keyword || file.fileName.toLowerCase().includes(keyword))
    ok(res, { records, total: records.length, page: 1, size: records.length, pages: 1 })
  })

  add('POST', /^\/api\/files\/upload$/, async (req, res) => {
    const fileId = `file-mock-${Date.now().toString(36)}`
    const file = {
      fileId,
      fileName: `新上传文档-${state.files.length + 1}.docx`,
      objectName: `2026/11/01/upload-${Date.now()}.docx`,
      bucketName: 'doc-ai',
      size: 24576,
      contentType: 'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
      uploadTime: nowIso(),
      uploaderId: 1
    }
    state.files.unshift(file)
    ok(res, file, '上传成功')
  })

  add('GET', /^\/api\/files\/metadata\/([^/]+)$/, async (req, res, { match }) => {
    const file = state.files.find((item) => item.fileId === match[1])
    if (!file) return fail(res, '文件不存在', 404)
    ok(res, file)
  })

  add('PUT', /^\/api\/files\/([^/]+)\/rename$/, async (req, res, { match, query }) => {
    const file = state.files.find((item) => item.fileId === match[1])
    const newName = String(query.get('newName') || file?.fileName || '未命名')
    if (file) file.fileName = newName
    ok(res, file || { fileId: match[1], fileName: newName }, '重命名成功')
  })

  add('DELETE', /^\/api\/files\/([^/]+)$/, async (req, res, { match }) => {
    const index = state.files.findIndex((item) => item.fileId === match[1])
    if (index >= 0) state.files.splice(index, 1)
    ok(res, true, '文件已删除')
  })

  add('GET', /^\/api\/files\/(download|preview)\/([^/]+)$/, async (req, res, { match }) => {
    const file = state.files.find((item) => item.fileId === match[2])
    const name = file?.fileName || 'prisdoc-file.txt'
    sendRaw(res, Buffer.from(`这是 Mock 模式生成的文件内容。\n\n文件名：${name}\n生成时间：${new Date().toLocaleString('zh-CN')}\n`, 'utf8'),
      'application/octet-stream', { 'Content-Disposition': `attachment; filename*=UTF-8''${encodeURIComponent(name)}` })
  })

  // ------------------------------ AI：模型 / 会话 ------------------------------

  add('GET', /^\/api\/ai\/models$/, async (req, res) => ok(res, MODELS))

  add('POST', /^\/api\/ai\/agent\/chat\/start$/, async (req, res, { query }) => {
    const userId = String(query.get('userId') || '1')
    const conversationId = `conv-mock-${userId}-${Date.now().toString(36)}`
    state.conversations.unshift({
      conversationId,
      title: '新对话',
      userId,
      messageCount: 0,
      createTime: nowIso(),
      updateTime: nowIso()
    })
    ok(res, conversationId, '会话已创建')
  })

  // ------------------------------ AI：同步接口 ------------------------------

  add('POST', /^\/api\/ai\/analyze$/, async (req, res, { body }) => {
    const payload = body.json || {}
    ok(res, textAnalyze(payload.content || ''))
  })

  add('GET', /^\/api\/ai\/agent\/tools$/, async (req, res) => ok(res, TOOLS))

  add('GET', /^\/api\/ai\/agent\/workbench\/overview$/, async (req, res) => {
    ok(res, {
      tools: TOOLS,
      conversations: state.conversations,
      tasks: state.tasks,
      knowledgeJobs: state.knowledgeJobs,
      user: { userId: '1', role: 'ADMIN' }
    })
  })

  add('GET', /^\/api\/ai\/agent\/tasks$/, async (req, res) => ok(res, state.tasks))

  add('GET', /^\/api\/ai\/agent\/tasks\/([^/]+)$/, async (req, res, { match }) => {
    const task = state.tasks.find((item) => item.traceId === match[1])
    if (!task) return ok(res, { status: 'not_found', traceId: match[1] })
    ok(res, buildTaskSnapshot(task))
  })

  add('POST', /^\/api\/ai\/agent\/approvals\/confirm$/, async (req, res, { body }) => {
    const payload = body.json || {}
    const token = payload.approvalToken || payload.agentApprovalToken || payload.context?.agentApprovalToken
    if (!token) return fail(res, '缺少审批令牌', 400)

    const result = buildAgentResult({ task: '删除文件', context: payload.context || {} })
    result.status = 'success'
    result.approvalToken = null
    result.pendingApproval = null
    result.answer = '审批已确认，文件已永久删除（Mock 模式仅模拟删除动作，未真正操作存储）。'
    result.toolResults = [{
      toolName: 'file-delete',
      status: 'success',
      message: '文件已永久删除',
      requiresAction: false,
      data: { objectName: payload.context?.objectName || '客户合同评审要点清单-旧版.docx', status: 'deleted', deletedAt: nowIso() }
    }]
    ok(res, result, '审批已确认')
  })

  add('POST', /^\/api\/ai\/agent\/approvals\/([^/]+)\/cancel$/, async (req, res) => {
    ok(res, true, '审批已取消')
  })

  add('POST', /^\/api\/ai\/agent\/execute$/, async (req, res, { body }) => {
    ok(res, buildAgentResult(body.json || {}))
  })

  // ------------------------------ AI：异步任务 ------------------------------

  const submitJob = (res, type, result, taskText = '') => {
    const jobId = `job-mock-${Date.now().toString(36)}-${++globalSeq.job}`
    state.jobs.set(jobId, { jobId, type, status: 'running', result, createdAt: Date.now() })

    // Agent 任务执行完同步落一条历史记录，工作台的“Agent 任务”数量会随之增长
    if (type === 'agent') {
      state.tasks.unshift({
        traceId: result.traceId,
        task: taskText || '未命名任务',
        status: result.status === 'action_required' ? 'action_required' : (result.status === 'success' ? 'completed' : 'failed'),
        userId: '1',
        model: result.model,
        createdAt: nowIso(),
        durationMs: result.executionTimeMs || 0,
        iterations: result.iterations || 1,
        toolCalls: (result.toolResults || []).length,
        plan: result.plan || []
      })
    }

    ok(res, { jobId, type, status: 'queued', submittedAt: Date.now() }, '任务已提交')
  }

  add('POST', /^\/api\/ai\/async\/agent\/execute$/, async (req, res, { body }) => {
    const payload = body.json || {}
    submitJob(res, 'agent', buildAgentResult(payload), payload.task)
  })

  add('POST', /^\/api\/ai\/async\/summarize$/, async (req, res, { body }) => {
    const payload = body.json || {}
    const content = String(payload.content || '')
    const summary = buildSummary(content, payload.maxLength || 200)
    submitJob(res, 'summarize', {
      summary,
      originalLength: content.length,
      summaryLength: summary.length,
      model: payload.model || 'qwen3.6-plus'
    })
  })

  add('POST', /^\/api\/ai\/async\/keywords$/, async (req, res, { body }) => {
    const payload = body.json || {}
    const count = Number(payload.count || 5)
    const keywords = pickKeywords(payload.content, count)
    submitJob(res, 'keywords', { keywords, count: keywords.length, model: payload.model || 'qwen3.6-plus' })
  })

  add('POST', /^\/api\/ai\/async\/analyze$/, async (req, res, { body }) => {
    submitJob(res, 'analyze', textAnalyze(body.json?.content || ''))
  })

  add('GET', /^\/api\/ai\/async\/jobs\/([^/]+)$/, async (req, res, { match }) => {
    const job = state.jobs.get(match[1])
    if (!job) return fail(res, '任务不存在', 404)
    const running = Date.now() - job.createdAt < JOB_RUNNING_MS
    ok(res, {
      jobId: job.jobId,
      type: job.type,
      status: running ? 'running' : 'success',
      submittedAt: job.createdAt,
      startedAt: job.createdAt + 20,
      finishedAt: running ? null : job.createdAt + JOB_RUNNING_MS,
      result: running ? null : job.result,
      error: null
    })
  })

  // ------------------------------ AI：RAG ------------------------------

  add('GET', /^\/api\/ai\/rag\/segment\/strategies$/, async (req, res) => ok(res, SEGMENT_STRATEGIES))

  add('GET', /^\/api\/ai\/rag\/statistics$/, async (req, res) => ok(res, RAG_STATISTICS))

  add('GET', /^\/api\/ai\/rag\/document\/([^/]+)\/segments$/, async (req, res, { match }) => {
    const id = Number(match[1])
    let segments = state.segments.get(id)
    if (!segments) {
      const doc = state.docs.find((item) => item.id === id) || state.docs[0]
      segments = buildSegments(doc)
      state.segments.set(id, segments)
    }
    ok(res, segments)
  })

  add('POST', /^\/api\/ai\/rag\/index\/segment$/, async (req, res, { query, body }) => {
    const id = Number(query.get('documentId') || 0)
    const strategy = String(query.get('strategy') || 'AUTO')
    const doc = state.docs.find((item) => item.id === id) || state.docs[0]
    const segments = buildSegments({ ...doc, content: body.raw || doc.content })
      .map((segment) => ({ ...segment, strategy }))
    state.segments.set(id || doc.id, segments)
    ok(res, {
      documentId: id || doc.id,
      strategy,
      segmentCount: segments.length,
      vectorCount: segments.length,
      indexedAt: nowIso()
    }, '索引完成')
  })

  add('POST', /^\/api\/ai\/rag\/query$/, async (req, res, { body }) => {
    const question = String(body.raw || '').trim()
    ok(res, {
      answer: `关于「${question || '文档内容'}」，知识库检索结果如下：\n\n1. 制度层面要求明确责任主体与考核标准，避免条款无法落地；\n2. 技术层面建议按分层架构描述，并补充关键链路与异常处理；\n3. 对高风险操作保留人工审批，删除类动作必须二次确认。`,
      question,
      strategy: 'HYBRID',
      hits: 4,
      sources: [
        { documentId: 101, title: '企业办公用电安全管理规定', snippet: '严禁私拉乱接电线，离岗须关闭电源…', score: 0.91 },
        { documentId: 103, title: 'Prisdoc 平台技术架构说明', snippet: '平台划分为网关层、业务服务层、AI 能力层与基础设施层…', score: 0.86 }
      ]
    })
  })

  // ------------------------------ AI Ops ------------------------------

  add('GET', /^\/api\/ai\/aiops\/monitor$/, async (req, res) => ok(res, state.metrics))

  add('GET', /^\/api\/ai\/aiops\/health$/, async (req, res) => {
    ok(res, {
      status: 'UP',
      uptimeSeconds: 86412,
      version: '1.4.2-mock',
      components: { mysql: 'UP', redis: 'UP', rabbitmq: 'UP', minio: 'UP', vectorStore: 'UP' }
    })
  })

  add('GET', /^\/api\/ai\/aiops\/faults$/, async (req, res) => ok(res, state.faults))

  add('GET', /^\/api\/ai\/aiops\/detect$/, async (req, res) => {
    ok(res, state.faults.filter((fault) => fault.status !== 'resolved'))
  })

  add('POST', /^\/api\/ai\/aiops\/faults\/([^/]+)\/resolve$/, async (req, res, { match }) => {
    const fault = state.faults.find((item) => item.faultId === match[1])
    if (fault) {
      fault.status = 'resolved'
      fault.resolveTime = nowIso()
    }
    ok(res, true, '故障已处理')
  })

  add('POST', /^\/api\/ai\/aiops\/metrics\/reset$/, async (req, res) => {
    state.metrics = buildMetrics()
    for (const key of Object.keys(state.metrics)) {
      if (key.endsWith('.count')) state.metrics[key] = 0
    }
    state.metrics['ai.request.stats'] = { count: 0, total: 0, avg: 0, min: 0, max: 0, p95: 0 }
    ok(res, state.metrics, '指标已重置')
  })

  add('POST', /^\/api\/ai\/aiops\/metrics\/counter$/, async (req, res, { query }) => {
    const rawName = String(query.get('name') || 'unnamed.counter')
    const delta = Number(query.get('delta') || 1)
    // 前端埋点上报的是 ai.requests / ai.errors，归一到看板上的 .count 指标
    const name = state.metrics[rawName] === undefined && state.metrics[`${rawName}.count`] !== undefined
      ? `${rawName}.count`
      : rawName
    state.metrics[name] = Number(state.metrics[name] || 0) + delta
    ok(res, state.metrics[name])
  })

  add('POST', /^\/api\/ai\/aiops\/metrics\/timer$/, async (req, res, { query }) => {
    const name = String(query.get('name') || 'unnamed.timer')
    const duration = Number(query.get('duration') || 0)
    const statsKey = `${name}.stats`
    const stats = state.metrics[statsKey] || { count: 0, total: 0, avg: 0, min: duration, max: duration, p95: duration }
    stats.count += 1
    stats.total += duration
    stats.avg = Number((stats.total / stats.count).toFixed(1))
    stats.min = Math.min(stats.min || duration, duration)
    stats.max = Math.max(stats.max || duration, duration)
    stats.p95 = Math.round(stats.avg * 1.8)
    state.metrics[statsKey] = stats
    ok(res, stats)
  })

  // ------------------------------ 技能（PPT / 文件下载） ------------------------------

  add('POST', /^\/api\/skills\/html-ppt\/view$/, async (req, res, { body }) => {
    const payload = body.json || {}
    sendRaw(res, buildPptHtml(payload), 'text/html; charset=utf-8')
  })

  add('POST', /^\/api\/skills\/html-ppt\/download$/, async (req, res, { body }) => {
    const payload = body.json || {}
    const fileName = `${payload.title || 'Prisdoc-演示文稿'}.html`
    sendRaw(res, buildPptHtml(payload), 'text/html; charset=utf-8', {
      'Content-Disposition': `attachment; filename*=UTF-8''${encodeURIComponent(fileName)}`
    })
  })

  add('GET', /^\/api\/skills\/file\/download$/, async (req, res, { query }) => {
    const objectName = String(query.get('objectName') || 'prisdoc-file.txt')
    const content = `Prisdoc Mock 文件\n\n对象名：${objectName}\n存储桶：${query.get('bucket') || 'doc-ai'}\n生成时间：${new Date().toLocaleString('zh-CN')}\n`
    sendRaw(res, Buffer.from(content, 'utf8'), 'application/octet-stream', {
      'Content-Disposition': `attachment; filename*=UTF-8''${encodeURIComponent(objectName)}`
    })
  })

  // ------------------------------ Mock 自身信息 ------------------------------

  add('GET', /^\/api\/__mock__\/info$/, async (req, res) => {
    ok(res, {
      enabled: true,
      name: 'Prisdoc Mock Server',
      routes: routes.map((route) => `${route.method} ${route.pattern.source.replace(/\\\//g, '/').replace(/\^|\$/g, '')}`),
      state: {
        documents: state.docs.length,
        files: state.files.length,
        tasks: state.tasks.length,
        jobs: state.jobs.size
      }
    })
  })

  return {
    async handle(req, res) {
      const url = new URL(req.url, 'http://localhost')
      const path = url.pathname

      if (!path.startsWith(API_PREFIX)) return false

      await sleep(NETWORK_DELAY_MS)

      const body = req.method === 'GET' || req.method === 'HEAD' ? { raw: '', json: null } : await readBody(req)

      for (const route of routes) {
        if (route.method !== req.method) continue
        const match = route.pattern.exec(path)
        if (!match) continue
        try {
          await route.handler(req, res, { match, query: url.searchParams, body })
        } catch (error) {
          console.error('[mock] 处理请求失败', req.method, path, error)
          if (!res.writableEnded) fail(res, `Mock 处理异常：${error.message}`)
        }
        return true
      }

      // 未实现的 /api 路由：返回空成功响应，避免前端抛“服务器开了小差”
      console.warn(`[mock] 未定义的接口 ${req.method} ${path}，返回空数据`)
      ok(res, null, 'mock: 接口未定义')
      return true
    }
  }
}

// ---------------------------------------------------------------------------
// Vite 插件
// ---------------------------------------------------------------------------

/**
 * @param {{ enabled?: boolean }} options
 * @returns {import('vite').Plugin}
 */
export function prisdocMockPlugin(options = {}) {
  const enabled = options.enabled !== false
  let state = null
  let router = null

  return {
    name: 'prisdoc-mock',
    apply: 'serve',
    configureServer(server) {
      if (!enabled) return

      state = createState()
      router = createRouter(state)

      // 该中间件在 Vite 内部中间件（含 proxy）之前执行
      server.middlewares.use(async (req, res, next) => {
        const path = (req.url || '').split('?')[0]
        if (!path.startsWith(API_PREFIX)) return next()
        try {
          const handled = await router.handle(req, res)
          if (!handled) next()
        } catch (error) {
          console.error('[mock] 中间件异常', error)
          next(error)
        }
      })

      server.config.logger.info('\n  \x1b[36m[prisdoc-mock]\x1b[0m prisdoc-mock 已启用：/api/** 由本地假数据响应，无需启动后端')
      server.config.logger.info('  \x1b[36m[prisdoc-mock]\x1b[0m 查看已注册接口：GET /api/__mock__/info\n')
    }
  }
}

export default prisdocMockPlugin
