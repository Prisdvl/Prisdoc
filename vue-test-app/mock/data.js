/**
 * Prisdoc 前端 Mock 数据集
 *
 * 该文件只被 mock/index.js（Vite 开发服务器中间件）引用，
 * 不会进入任何生产构建产物，也不会修改 src/ 下的业务代码。
 */

// ---------------------------------------------------------------------------
// 基础工具
// ---------------------------------------------------------------------------

const b64url = (value) => Buffer.from(value, 'utf8').toString('base64').replace(/\+/g, '-').replace(/\//g, '_')

/**
 * 生成一个结构合法的 JWT（仅用于前端 decodeJwt 读取 role，不做签名校验）。
 * role = ADMIN 时工作台才会出现「AI Ops 运维中心」入口。
 */
export function mockToken(user = {}) {
  const now = Math.floor(Date.now() / 1000)
  const header = b64url(JSON.stringify({ alg: 'HS256', typ: 'JWT' }))
  const payload = b64url(JSON.stringify({
    sub: String(user.id ?? 1),
    userId: Number(user.id ?? 1),
    username: user.username || '张伟',
    role: user.role || 'ADMIN',
    iat: now,
    exp: now + 60 * 60 * 12
  }))
  return `${header}.${payload}.mock-signature-not-verified`
}

// ---------------------------------------------------------------------------
// 用户
// ---------------------------------------------------------------------------

export const USERS = [
  {
    id: 1,
    username: '张伟',
    email: 'zhangwei@prisdoc.example.com',
    phone: '13800138000',
    role: 'ADMIN',
    status: 1,
    createTime: '2025-09-12T09:30:00',
    avatarText: '张'
  },
  {
    id: 2,
    username: '李静',
    email: 'lijing@prisdoc.example.com',
    phone: '13900139000',
    role: 'USER',
    status: 1,
    createTime: '2025-11-03T14:12:00',
    avatarText: '李'
  },
  {
    id: 3,
    username: '王强',
    email: 'wangqiang@prisdoc.example.com',
    phone: '13700137000',
    role: 'USER',
    status: 1,
    createTime: '2026-01-08T10:05:00',
    avatarText: '王'
  }
]

// ---------------------------------------------------------------------------
// 模型列表（与 ai-service ModelType 枚举保持一致）
// ---------------------------------------------------------------------------

export const MODELS = [
  { code: 'qwen3.6-plus', name: '通义千问3.6 Plus', provider: '阿里云百炼', available: true },
  { code: 'glm-5', name: '智谱GLM-5', provider: '智谱AI', available: true },
  { code: 'kimi-k2.5', name: '月之暗面K2.5', provider: 'Moonshot', available: true },
  { code: 'MiniMax-M2.5', name: 'MiniMax M2.5', provider: 'MiniMax', available: false }
]

// ---------------------------------------------------------------------------
// Agent 工具（与 AgentToolRegistry 注册的工具名一致）
// ---------------------------------------------------------------------------

export const TOOLS = [
  { name: 'direct-answer', description: '不调用外部工具，直接基于模型和上下文回答用户。', category: 'general', riskLevel: 'low', destructive: false },
  { name: 'ask-user', description: '当任务参数缺失或存在歧义时，向用户提出澄清问题。', category: 'general', riskLevel: 'low', destructive: false },
  { name: 'rag-answer', description: '检索知识库并调用模型生成最终问答答案，带引用来源。', category: 'knowledge', riskLevel: 'low', destructive: false },
  { name: 'rag-search', description: '只检索知识库，返回相关片段和来源，不生成答案。', category: 'knowledge', riskLevel: 'low', destructive: false },
  { name: 'text-summarize', description: '对文本进行摘要，可按目标字数压缩。', category: 'text', riskLevel: 'low', destructive: false },
  { name: 'text-analyze', description: '统计文本字符、中文、英文、数字、空格、标点、行数。', category: 'text', riskLevel: 'low', destructive: false },
  { name: 'keyword-extract', description: '从文本中提取关键词并给出权重。', category: 'text', riskLevel: 'low', destructive: false },
  { name: 'text-correct', description: '对文本进行纠错、润色或改写，支持替换选中段落。', category: 'text', riskLevel: 'low', destructive: false },
  { name: 'file-download-url', description: '生成文件预签名下载地址。', category: 'file', riskLevel: 'low', destructive: false },
  { name: 'file-delete', description: '永久删除指定文件，不进入回收站；需要人工审批。', category: 'file', riskLevel: 'high', destructive: true },
  { name: 'file-restore', description: '从回收站恢复文件。', category: 'file', riskLevel: 'medium', destructive: false },
  { name: 'recycle-list', description: '查看回收站文件列表。', category: 'file', riskLevel: 'low', destructive: false },
  { name: 'file-version-list', description: '查看文件版本列表。', category: 'file', riskLevel: 'low', destructive: false },
  { name: 'file-version-switch', description: '切换文件当前版本。', category: 'file', riskLevel: 'medium', destructive: false },
  { name: 'html-ppt-generate', description: '根据大纲生成 HTML PPT，可直接在浏览器放映或导出。', category: 'skill', riskLevel: 'low', destructive: false },
  { name: 'document-read', description: '读取业务文档当前内容，用于扩写、改写、润色前获取原文。', category: 'document', riskLevel: 'low', destructive: false },
  { name: 'document-write', description: '将 AI 生成的文本返回给前端编辑器，由页面直接写入当前文档。', category: 'document', riskLevel: 'medium', destructive: false },
  { name: 'text-to-file', description: '兼容旧名：将 AI 生成文本交给前端编辑器待写入。', category: 'document', riskLevel: 'medium', destructive: false }
]

// ---------------------------------------------------------------------------
// 文档
// ---------------------------------------------------------------------------

const DOC_ELEC = `
<h2>企业办公用电安全管理规定</h2>
<p>为规范公司各办公区域的用电行为，杜绝电气火灾隐患，依据《中华人民共和国安全生产法》及公司《安全生产管理制度》，制定本规定。本规定适用于公司总部、各地分公司及驻外办公场所的全体在职员工、实习生与外包人员。</p>
<h3>一、总则</h3>
<p>1. 办公区域用电坚持「谁使用、谁负责」原则，各部门负责人为本部门用电安全第一责任人。</p>
<p>2. 行政部负责公共区域配电设施的日常巡检，每季度组织一次用电安全专项检查，并留存检查记录。</p>
<p>3. 严禁私自改装、拆卸配电箱、插座及线路。因业务需要增容的，须提交书面申请，由行政部与物业共同评估后实施。</p>
<h3>二、日常用电规范</h3>
<p>1. 离开工位超过 30 分钟，应关闭显示器与台灯；下班离开办公区，必须关闭本人负责的电脑、饮水机、打印机等设备电源。</p>
<p>2. 严禁在办公区使用电热壶、电暖器、电磁炉等大功率电器。单插座总功率不得超过 2000W，禁止多台设备串联插排使用。</p>
<p>3. 严禁私拉乱接电线，禁止将电源线铺设于地面通道、地毯下方或人员通行处；确需临时布线的，须使用线槽并报行政部备案。</p>
<p>4. 手机、笔记本等设备充电完成后应及时拔除充电器，避免长时间空载通电。</p>
<h3>三、隐患报告与应急处理</h3>
<p>1. 发现插座发烫、异味、打火、线缆破皮等情况，应立即切断该回路电源，并在 10 分钟内报告行政部（分机 8001）。</p>
<p>2. 发生电气火灾时，先断电、后灭火，严禁用水或泡沫灭火器直接扑救带电设备，应使用二氧化碳灭火器并立即拨打 119。</p>
<p>3. 任何人不得擅自恢复已拉闸断电的回路，须经行政部确认隐患排除后方可合闸。</p>
<h3>四、责任与考核</h3>
<p>1. 违反本规定的，首次予以口头警告并记录；再次违反的，扣减当月绩效考核 5 分；造成设备损坏的，按损失金额的 30% 至 50% 承担赔偿责任。</p>
<p>2. 因违规用电引发火灾、触电等事故的，除依法追究责任外，公司将解除劳动合同并保留追偿权利。</p>
<p>3. 对主动发现并报告重大用电隐患的员工，公司给予 500 元至 2000 元奖励。</p>
<h3>五、附则</h3>
<p>本规定自发布之日起施行，由行政部负责解释。原有相关规定与本规定不一致的，以本规定为准。</p>
`.trim()

const DOC_PRD = `
<h2>2026 年 Q3 产品需求评审纪要</h2>
<p>会议时间：2026 年 9 月 18 日 14:00 至 16:30。会议地点：A 座 12 层第三会议室。主持人：张伟。记录人：李静。</p>
<p>参会人员：产品部张伟、李静，研发部王强、陈磊、赵敏，设计部周舟，测试部孙悦，运营部吴楠。</p>
<h3>一、会议目标</h3>
<p>评审 Q3 迭代的四项核心需求，确认优先级、验收标准与上线节奏，识别跨团队依赖风险。</p>
<h3>二、需求评审结论</h3>
<p><strong>需求一：智能文档问答（P0）</strong>。用户上传文档后可直接提问，回答须带原文片段引用。结论：通过，9 月 30 日前完成知识库切分与召回链路联调，10 月 15 日灰度 20% 用户。</p>
<p><strong>需求二：一键生成演示文稿（P0）</strong>。由大纲生成可放映的 HTML 演示稿，支持 8 套主题。结论：通过，需补充导出 PPTX 的降级方案说明。</p>
<p><strong>需求三：多人协同编辑（P1）</strong>。支持 2 秒内的内容同步与光标位置提示。结论：有条件通过，当前轮询方案的同步延迟在弱网下超过 5 秒，需评估 WebSocket 改造工作量后再排期。</p>
<p><strong>需求四：AI Ops 运维看板（P2）</strong>。展示请求量、错误率、平均响应时间与故障记录。结论：通过，指标口径由研发部与运营部在 9 月 25 日前对齐。</p>
<h3>三、跨团队依赖与风险</h3>
<p>1. 文档服务与 AI 服务的文件 ID 映射存在不一致，需要在网关层统一，责任人王强，9 月 26 日前给出方案。</p>
<p>2. 大模型限流配额不足，高峰期 P95 响应时间已达 4.2 秒，运营部负责申请提额。</p>
<p>3. 演示文稿主题样式稿尚未定稿，设计部 9 月 24 日前交付。</p>
<h3>四、待办事项</h3>
<p>1. 张伟：输出 Q3 迭代排期表，9 月 22 日前。</p>
<p>2. 李静：整理评审意见并更新需求文档，9 月 21 日前。</p>
<p>3. 王强：完成文件 ID 映射方案评审，9 月 26 日前。</p>
<p>4. 周舟：交付 8 套演示主题视觉稿，9 月 24 日前。</p>
<p>5. 孙悦：补充回归测试用例清单，9 月 29 日前。</p>
`.trim()

const DOC_ARCH = `
<h2>Prisdoc 平台技术架构说明</h2>
<p>Prisdoc 是一套面向企业知识场景的智能文档平台，采用 Spring Cloud 微服务架构，前端基于 Vue 3 与 Element Plus，AI 能力通过多模型路由接入。</p>
<h3>一、整体架构</h3>
<p>平台划分为网关层、业务服务层、AI 能力层与基础设施层四个部分。网关层统一鉴权、限流与路由；业务服务层包含用户服务、文档服务、文件服务与 AI 服务；基础设施层提供 MySQL、Redis、RabbitMQ、MinIO 与向量检索能力。</p>
<p>所有对外请求统一从 8080 端口进入，由 Spring Cloud Gateway 按路径前缀分发：/api/users 转发至用户服务，/api/documents 转发至文档服务，/api/files 转发至文件服务，/api/ai 与 /api/skills 转发至 AI 服务。</p>
<h3>二、关键链路</h3>
<p>1. 文档上传：前端将文件提交至文件服务，文件落库 MinIO，元数据写入 MySQL，返回 fileId；前端再调用文档服务创建业务文档，建立 fileId 与文档的关联。</p>
<p>2. RAG 检索：文档按策略分段后向量化写入向量库；提问时先召回 Top-K 片段，经重排序后拼接上下文，由大模型生成带引用的答案。</p>
<p>3. Agent 执行：请求进入异步任务队列，规划器拆解步骤，逐步调用工具；遇到高危工具时中断并生成审批令牌，人工确认后从断点继续。</p>
<h3>三、异步任务与消息</h3>
<p>耗时的 AI 调用统一走异步任务：提交后立即返回 jobId，任务状态与结果写入 Redis，前端以 1.2 秒间隔轮询直至成功。RabbitMQ 用于索引任务与通知的削峰填谷，避免大批量上传时将模型配额打满。</p>
<h3>四、可观测性</h3>
<p>AI 服务内置指标采集过滤器，统计请求量、错误数与耗时分布，定时任务每分钟检测一次异常并写入故障表，前端 AI Ops 看板直接展示这些指标。</p>
<h3>五、部署形态</h3>
<p>开发环境可用 Docker Compose 一键拉起基础设施与服务；生产环境采用 Kubernetes 部署，各服务独立伸缩，文件与向量数据持久化到独立存储。</p>
`.trim()

const DOC_ONBOARD = `
<h2>新员工入职培训手册</h2>
<p>欢迎加入 Prisdoc 团队。本手册用于帮助你在入职首周完成账号开通、环境准备与基础协作规范的学习。</p>
<h3>一、入职第一天</h3>
<p>1. 上午 9:30 到行政部领取工牌与办公设备，完成门禁登记。</p>
<p>2. 上午 10:00 参加入职介绍会，了解公司组织架构、业务范围与考勤制度。</p>
<p>3. 下午 14:00 与直属主管进行一对一沟通，明确试用期目标与考核方式。</p>
<h3>二、账号与环境</h3>
<p>1. 企业邮箱、办公 IM、代码仓库账号由 IT 统一开通，首次登录须修改初始密码并绑定二次验证。</p>
<p>2. 研发同事按《开发环境搭建指南》安装 JDK 21、Node.js 18 及以上版本、Maven 与 Docker，并拉取项目代码。</p>
<p>3. 测试环境地址与演示账号由导师提供，禁止在测试环境录入真实客户数据。</p>
<h3>三、协作规范</h3>
<p>1. 每日站会 10:00 开始，每人用不超过两分钟说明昨天完成、今天计划和当前阻塞。</p>
<p>2. 代码提交遵循约定式提交规范，分支命名为 feature/工号-需求简称，合并前须通过至少一人评审。</p>
<p>3. 会议纪要当天归档到文档库，关键决策须在群内同步结论。</p>
<h3>四、首周目标</h3>
<p>1. 完成开发环境搭建并跑通本地服务。</p>
<p>2. 阅读《技术架构说明》并画出一张自己理解的调用链路图。</p>
<p>3. 完成一个入门级需求或修复一个已知缺陷，走完从提交到上线的完整流程。</p>
`.trim()

const DOC_CONTRACT = `
<h2>客户合同评审要点清单</h2>
<p>本清单用于商务、法务、财务与技术多方联合评审客户合同时逐项确认，降低履约风险。</p>
<h3>一、商务条款</h3>
<p>1. 合同主体名称与营业执照是否一致，签约人是否具备授权文件。</p>
<p>2. 金额、税率、付款节点与发票类型是否明确，是否存在账期超过 90 天的条款。</p>
<p>3. 验收标准是否可量化，避免出现「客户满意为止」等主观表述。</p>
<h3>二、法务条款</h3>
<p>1. 违约责任是否对等，逾期赔付比例是否超过合同总额的 20%。</p>
<p>2. 知识产权归属是否清晰，定制开发部分的著作权归属须单独约定。</p>
<p>3. 数据条款是否包含数据出境、二次利用与删除义务，是否与我方隐私政策冲突。</p>
<h3>三、技术与交付</h3>
<p>1. 交付范围是否包含需求范围外的定制功能，工作量评估是否经过研发确认。</p>
<p>2. 服务等级条款中的可用性指标是否与现有架构能力匹配，避免承诺 99.99%。</p>
<p>3. 是否约定了客户环境的网络与账号配合义务，未配合导致的延期如何界定。</p>
<h3>四、财务与合规</h3>
<p>1. 收款账户是否为对公账户，是否涉及第三方代收。</p>
<p>2. 是否触发招投标或合规审批流程，是否需报备集团法务。</p>
<p>3. 保密协议是否与主合同同步签署，有效期是否覆盖合同终止后三年。</p>
`.trim()

const DOC_WEEKLY = `
<h2>项目周报（第 44 周）</h2>
<p>周期：2026 年 10 月 26 日至 10 月 30 日。编写人：张伟。参与团队：产品、研发、测试、运营。</p>
<h3>一、本周进展</h3>
<p>1. 智能文档问答功能完成灰度放量至 40% 用户，累计提问 3821 次，答案采纳率 78.6%，较上周提升 4.2 个百分点。</p>
<p>2. HTML 演示文稿生成器上线 8 套主题，平均生成耗时 9.4 秒，导出成功率 96.1%。</p>
<p>3. 多人协同编辑完成 WebSocket 方案技术验证，单文档 5 人并发下同步延迟降至 380 毫秒。</p>
<p>4. AI Ops 看板接入故障自动检测，本周自动识别 3 起异常并全部闭环。</p>
<h3>二、存在问题</h3>
<p>1. 高峰期模型配额仍然紧张，10 月 28 日 15:00 至 16:00 出现 12 次限流重试，影响 0.6% 的请求。</p>
<p>2. 文件服务与文档服务的 ID 映射在历史数据中存在 37 条不一致记录，需专项清洗。</p>
<p>3. 演示文稿在低版本浏览器中字体回退效果不稳定。</p>
<h3>三、下周计划</h3>
<p>1. 完成历史数据清洗脚本并灰度执行，责任人王强。</p>
<p>2. 提交模型配额提额申请并跟进审批，责任人吴楠。</p>
<p>3. 协同编辑 WebSocket 版本进入开发，预计 11 月 13 日提测，责任人陈磊。</p>
<p>4. 补充问答功能的引用准确率评测集，责任人孙悦。</p>
`.trim()

export const DOCS = [
  {
    id: 101,
    title: '企业办公用电安全管理规定',
    fileId: 'file-8f21c3a1',
    category: '制度规范',
    ownerId: 1,
    createTime: '2026-10-28T09:12:00',
    updateTime: '2026-10-30T17:40:00',
    summary: '全文共五章，明确办公区域用电责任划分、日常用电规范（大功率电器禁用、禁止私拉乱接）、隐患报告流程与考核标准，违规最高扣减绩效 5 分并承担赔偿责任。',
    content: DOC_ELEC,
    currentVersion: 3
  },
  {
    id: 102,
    title: '2026 年 Q3 产品需求评审纪要',
    fileId: 'file-2d77b9e4',
    category: '会议纪要',
    ownerId: 1,
    createTime: '2026-09-18T16:45:00',
    updateTime: '2026-09-19T10:20:00',
    summary: '评审四项核心需求：智能文档问答与一键生成 PPT 列为 P0，协同编辑因弱网延迟超 5 秒需评估改造，AI Ops 看板为 P2。明确五项待办与三处跨团队依赖。',
    content: DOC_PRD,
    currentVersion: 2
  },
  {
    id: 103,
    title: 'Prisdoc 平台技术架构说明',
    fileId: 'file-5c90a1d7',
    category: '技术文档',
    ownerId: 1,
    createTime: '2026-10-09T11:30:00',
    updateTime: '2026-10-12T15:05:00',
    summary: '说明网关、业务服务、AI 能力与基础设施四层架构，梳理文档上传、RAG 检索与 Agent 执行三条关键链路，并给出异步任务与可观测性设计。',
    content: DOC_ARCH,
    currentVersion: 5
  },
  {
    id: 104,
    title: '新员工入职培训手册',
    fileId: 'file-1b34e6c2',
    category: '人事制度',
    ownerId: 1,
    createTime: '2026-08-21T14:00:00',
    updateTime: '2026-10-08T09:15:00',
    summary: '覆盖入职首日流程、账号与环境准备、协作规范与首周目标，供新同事快速上手并明确试用期考核要求。',
    content: DOC_ONBOARD,
    currentVersion: 1
  },
  {
    id: 105,
    title: '客户合同评审要点清单',
    fileId: 'file-7a02f8b5',
    category: '法务模板',
    ownerId: 1,
    createTime: '2026-07-14T10:05:00',
    updateTime: '2026-09-02T16:30:00',
    summary: '从商务、法务、技术交付与财务合规四个维度给出 12 项检查要点，重点提示验收标准量化、违约赔付上限与数据条款风险。',
    content: DOC_CONTRACT,
    currentVersion: 2
  },
  {
    id: 106,
    title: '项目周报（第 44 周）',
    fileId: 'file-3e6d4a09',
    category: '项目周报',
    ownerId: 1,
    createTime: '2026-10-30T18:20:00',
    updateTime: '2026-10-30T18:20:00',
    summary: '问答功能灰度至 40% 用户，采纳率 78.6%；协同编辑同步延迟降至 380 毫秒；模型配额与历史数据映射仍待处理。',
    content: DOC_WEEKLY,
    currentVersion: 1
  }
]

// 版本历史（按文档 id 组织，最新在前）
export const VERSIONS = {
  101: [
    { id: 'v-101-3', versionNumber: 3, title: '企业办公用电安全管理规定', createTime: '2026-10-30T17:40:00', operator: '张伟', remark: '补充违规用电赔偿比例' },
    { id: 'v-101-2', versionNumber: 2, title: '企业办公用电安全管理规定（修订稿）', createTime: '2026-10-30T11:02:00', operator: '张伟', remark: '新增应急处理章节' },
    { id: 'v-101-1', versionNumber: 1, title: '企业办公用电安全管理规定', createTime: '2026-10-28T09:12:00', operator: '张伟', remark: '初稿' }
  ],
  102: [
    { id: 'v-102-2', versionNumber: 2, title: '2026 年 Q3 产品需求评审纪要', createTime: '2026-09-19T10:20:00', operator: '李静', remark: '补充待办责任人' },
    { id: 'v-102-1', versionNumber: 1, title: '2026 年 Q3 产品需求评审纪要', createTime: '2026-09-18T16:45:00', operator: '李静', remark: '会议当场记录' }
  ],
  103: [
    { id: 'v-103-5', versionNumber: 5, title: 'Prisdoc 平台技术架构说明', createTime: '2026-10-12T15:05:00', operator: '王强', remark: '补充可观测性章节' },
    { id: 'v-103-4', versionNumber: 4, title: 'Prisdoc 平台技术架构说明', createTime: '2026-10-11T10:10:00', operator: '王强', remark: '修订部署形态' },
    { id: 'v-103-3', versionNumber: 3, title: 'Prisdoc 平台技术架构说明', createTime: '2026-10-10T18:22:00', operator: '王强', remark: '补充异步任务说明' }
  ]
}

// ---------------------------------------------------------------------------
// 文件（MinIO 对象映射）
// ---------------------------------------------------------------------------

export const FILES = [
  { fileId: 'file-8f21c3a1', fileName: '企业办公用电安全管理规定.docx', objectName: '2026/10/28/elec-safety.docx', bucketName: 'doc-ai', size: 48612, contentType: 'application/vnd.openxmlformats-officedocument.wordprocessingml.document', uploadTime: '2026-10-28T09:12:00', uploaderId: 1 },
  { fileId: 'file-2d77b9e4', fileName: 'Q3产品需求评审纪要.docx', objectName: '2026/09/18/q3-prd-review.docx', bucketName: 'doc-ai', size: 39204, contentType: 'application/vnd.openxmlformats-officedocument.wordprocessingml.document', uploadTime: '2026-09-18T16:45:00', uploaderId: 1 },
  { fileId: 'file-5c90a1d7', fileName: 'Prisdoc平台技术架构说明.pdf', objectName: '2026/10/09/prisdoc-arch.pdf', bucketName: 'doc-ai', size: 1048576, contentType: 'application/pdf', uploadTime: '2026-10-09T11:30:00', uploaderId: 1 },
  { fileId: 'file-1b34e6c2', fileName: '新员工入职培训手册.docx', objectName: '2026/08/21/onboarding.docx', bucketName: 'doc-ai', size: 27136, contentType: 'application/vnd.openxmlformats-officedocument.wordprocessingml.document', uploadTime: '2026-08-21T14:00:00', uploaderId: 1 },
  { fileId: 'file-7a02f8b5', fileName: '客户合同评审要点清单.xlsx', objectName: '2026/07/14/contract-checklist.xlsx', bucketName: 'doc-ai', size: 15360, contentType: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet', uploadTime: '2026-07-14T10:05:00', uploaderId: 1 },
  { fileId: 'file-3e6d4a09', fileName: '项目周报-第44周.docx', objectName: '2026/10/30/weekly-44.docx', bucketName: 'doc-ai', size: 18432, contentType: 'application/vnd.openxmlformats-officedocument.wordprocessingml.document', uploadTime: '2026-10-30T18:20:00', uploaderId: 1 }
]

// ---------------------------------------------------------------------------
// Agent 任务 / 对话 / 知识索引任务
// ---------------------------------------------------------------------------

export const TASKS = [
  {
    traceId: 'trace-7f3a91c2',
    task: '把会议纪要总结成三条结论，并发给项目群',
    status: 'completed',
    userId: '1',
    model: 'qwen3.6-plus',
    createdAt: '2026-10-31T10:12:00',
    durationMs: 8420,
    iterations: 2,
    toolCalls: 3,
    plan: [
      { id: 'step-1', description: '读取会议纪要文档内容', toolName: 'document-read', status: 'success', riskLevel: 'low', reasoning: '需要原文才能准确提炼结论。', observation: '读取到 1873 字，含四项需求结论。' },
      { id: 'step-2', description: '生成三条结论摘要', toolName: 'text-summarize', status: 'success', riskLevel: 'low', reasoning: '用户要求压缩到三条。', observation: '输出 3 条结论，共 246 字。' },
      { id: 'step-3', description: '生成可直接粘贴的群消息文本', toolName: 'direct-answer', status: 'success', riskLevel: 'low', reasoning: '无需外部工具。', observation: '已生成格式化文本。' }
    ]
  },
  {
    traceId: 'trace-2b8d40e6',
    task: '根据技术架构说明生成一份 5 页的汇报 PPT',
    status: 'completed',
    userId: '1',
    model: 'glm-5',
    createdAt: '2026-10-30T15:36:00',
    durationMs: 15680,
    iterations: 3,
    toolCalls: 4,
    plan: [
      { id: 'step-1', description: '读取技术架构文档', toolName: 'document-read', status: 'success', riskLevel: 'low' },
      { id: 'step-2', description: '抽取架构分层与关键链路要点', toolName: 'keyword-extract', status: 'success', riskLevel: 'low' },
      { id: 'step-3', description: '按大纲生成 HTML 演示稿', toolName: 'html-ppt-generate', status: 'success', riskLevel: 'low' },
      { id: 'step-4', description: '整理生成结果并返回下载地址', toolName: 'file-download-url', status: 'success', riskLevel: 'low' }
    ]
  },
  {
    traceId: 'trace-9c15e7a3',
    task: '删除过期的《客户合同评审要点清单-旧版》',
    status: 'action_required',
    userId: '1',
    model: 'qwen3.6-plus',
    createdAt: '2026-10-31T16:02:00',
    durationMs: 2140,
    iterations: 1,
    toolCalls: 1,
    plan: [
      { id: 'step-1', description: '定位目标文件', toolName: 'document-read', status: 'success', riskLevel: 'low' },
      { id: 'step-2', description: '永久删除文件', toolName: 'file-delete', status: 'waiting_user', riskLevel: 'high', requiresApproval: true, reasoning: '删除不可恢复，需人工确认。' }
    ]
  },
  {
    traceId: 'trace-4a62fb18',
    task: '统计本周周报里的关键指标并做同比',
    status: 'failed',
    userId: '1',
    model: 'kimi-k2.5',
    createdAt: '2026-10-30T20:11:00',
    durationMs: 4210,
    iterations: 2,
    toolCalls: 2,
    error: '知识库检索超时：vector-store 在 3000ms 内未返回结果',
    plan: [
      { id: 'step-1', description: '检索周报文档片段', toolName: 'rag-search', status: 'error', riskLevel: 'low', lastError: 'vector-store timeout' }
    ]
  }
]

export const CONVERSATIONS = [
  { conversationId: 'conv-20261031-a1', title: '周报指标梳理', userId: '1', messageCount: 6, createTime: '2026-10-31T09:40:00', updateTime: '2026-10-31T10:12:00' },
  { conversationId: 'conv-20261030-b7', title: '架构汇报 PPT 大纲', userId: '1', messageCount: 4, createTime: '2026-10-30T15:20:00', updateTime: '2026-10-30T15:36:00' }
]

export const KNOWLEDGE_JOBS = [
  { jobId: 'idx-20261030-01', documentId: 103, documentTitle: 'Prisdoc 平台技术架构说明', status: 'completed', strategy: 'SEMANTIC', chunks: 42, updatedAt: '2026-10-30T16:05:00' },
  { jobId: 'idx-20261031-02', documentId: 101, documentTitle: '企业办公用电安全管理规定', status: 'running', strategy: 'CHAPTER', chunks: 18, updatedAt: '2026-10-31T18:22:00' }
]

// ---------------------------------------------------------------------------
// 知识库分段与统计
// ---------------------------------------------------------------------------

export const SEGMENT_STRATEGIES = {
  AUTO: '智能自动分段（按文档结构自动选择）',
  FIXED_LENGTH: '固定长度（1000 字符 / 10% 重叠）',
  CHAPTER: '按章节分段（依据标题层级）',
  SEMANTIC: '语义分段（句子边界聚合）'
}

export const RAG_STATISTICS = {
  documentCount: 6,
  segmentCount: 128,
  vectorCount: 128,
  knowledgeBaseCount: 1,
  averageSegmentLength: 486,
  lastIndexTime: '2026-10-30T16:05:00',
  embeddingModel: 'text-embedding-v3',
  vectorDimension: 1024
}

export function buildSegments(doc) {
  const text = String(doc?.content || '').replace(/<[^>]+>/g, '\n').split('\n').map(s => s.trim()).filter(Boolean)
  const chunks = []
  let buffer = []
  let length = 0
  for (const line of text) {
    buffer.push(line)
    length += line.length
    if (length >= 220) {
      chunks.push(buffer.join('\n'))
      buffer = []
      length = 0
    }
  }
  if (buffer.length) chunks.push(buffer.join('\n'))

  return chunks.slice(0, 6).map((content, index) => ({
    segmentId: `${doc.id}-seg-${index + 1}`,
    documentId: doc.id,
    index: index + 1,
    strategy: 'SEMANTIC',
    content,
    tokens: Math.round(content.length * 0.9),
    createdAt: '2026-10-30T16:05:00'
  }))
}

// ---------------------------------------------------------------------------
// AI Ops 指标与故障
// ---------------------------------------------------------------------------

export function buildMetrics() {
  return {
    'ai.requests.count': 1843,
    'ai.errors.count': 7,
    'ai.requests.rate': 2.4,
    'ai.request.stats': { count: 1843, total: 494612, avg: 268.4, min: 42, max: 3980, p95: 812 },
    'ai.tokens.count': 1284630,
    'ai.rag.hits.count': 612,
    'ai.rag.miss.count': 24,
    'ai.agent.tasks.count': 96,
    'ai.agent.approvals.count': 12,
    'jvm.memory.used': 412,
    'jvm.memory.max': 1024
  }
}

export const FAULTS = [
  {
    faultId: 'fault-20261030-03',
    type: 'RATE_LIMIT',
    message: '模型调用触发限流：qwen3.6-plus 在 15:00-16:00 出现 12 次 429 重试',
    status: 'open',
    severity: 'medium',
    createTime: '2026-10-30T15:12:00'
  },
  {
    faultId: 'fault-20261030-02',
    type: 'SLOW_RESPONSE',
    message: 'P95 响应时间 3980ms，超过 3000ms 阈值（rag-search 链路）',
    status: 'open',
    severity: 'high',
    createTime: '2026-10-30T15:08:00'
  },
  {
    faultId: 'fault-20261029-01',
    type: 'HIGH_ERROR_RATE',
    message: '错误率 2.1% 超过 1% 阈值，集中在文件上传解析阶段',
    status: 'resolved',
    severity: 'medium',
    createTime: '2026-10-29T11:20:00',
    resolveTime: '2026-10-29T13:05:00'
  }
]

// ---------------------------------------------------------------------------
// PPT 生成结果
// ---------------------------------------------------------------------------

export function buildPptHtml({ title = 'Prisdoc 项目', theme = 'tokyo-night', outline = '' } = {}) {
  const themes = {
    'tokyo-night': { bg: '#1a1b26', fg: '#c0caf5', accent: '#7aa2f7', muted: '#565f89' },
    dracula: { bg: '#282a36', fg: '#f8f8f2', accent: '#bd93f9', muted: '#6272a4' },
    'catppuccin-mocha': { bg: '#1e1e2e', fg: '#cdd6f4', accent: '#cba6f7', muted: '#6c7086' },
    nord: { bg: '#2e3440', fg: '#eceff4', accent: '#88c0d0', muted: '#4c566a' },
    'corporate-clean': { bg: '#ffffff', fg: '#1f2329', accent: '#3370ff', muted: '#8f959e' },
    'minimal-white': { bg: '#fafafa', fg: '#222222', accent: '#111111', muted: '#999999' },
    'cyberpunk-neon': { bg: '#0b0b12', fg: '#f7f7ff', accent: '#ff2e88', muted: '#5b5b7a' },
    aurora: { bg: '#0f172a', fg: '#e2e8f0', accent: '#38bdf8', muted: '#64748b' }
  }
  const t = themes[theme] || themes['tokyo-night']
  const chapters = String(outline).split('\n').map(s => s.trim()).filter(Boolean)
  const bulletSets = [
    { test: /背景|痛点|现状/, items: ['现状：资料散落在网盘、邮箱与聊天记录中，检索成本高', '痛点：近 80% 的时间花在找资料和重复整理上', '目标：把个人经验沉淀为可检索、可复用的组织知识'] },
    { test: /架构|微服务|技术/, items: ['网关统一鉴权与限流，业务服务按领域拆分', '文档、文件、AI 三条链路独立部署、独立伸缩', '异步任务 + 消息队列削峰，P95 稳定在 800ms 以内'] },
    { test: /RAG|检索|知识库|向量/, items: ['按章节与语义分段，1000 字符窗口、10% 重叠', '混合检索 + 重排序，Top-4 命中率 87%', '答案强制携带原文引用，显著降低模型幻觉'] },
    { test: /商业|展望|未来|规划|价值/, items: ['从工具走向平台：开放 API 与技能插件市场', '按席位 + 调用量订阅，客单价预计提升 30%', '2027 年覆盖制造、金融、政务三个行业模板'] },
    { test: /安全|合规|风险|权限/, items: ['数据分级 + 最小权限，关键操作全量审计', '删除、覆盖等高危动作保留人工审批环节', '敏感字段脱敏后再进入模型上下文'] }
  ]
  const slides = (chapters.length ? chapters : ['项目背景与痛点', '核心微服务架构', 'RAG 知识检索增强', '未来商业展望'])
    .map((chapter, i) => {
      const matched = bulletSets.find(set => set.test.test(chapter))
      const bullets = (matched ? matched.items : ['关键指标：响应时间、采纳率与推理成本', '落地节奏：灰度验证 → 数据复盘 → 全量推广', '风险对策：配额管理、并发控制与降级预案'])
        .map(item => `<li>${escapeHtmlText(item)}</li>`).join('\n          ')
      return `
      <section class="slide">
        <div class="slide-index">0${i + 1}</div>
        <h2>${escapeHtmlText(chapter)}</h2>
        <ul>
          ${bullets}
        </ul>
      </section>`
    }).join('')

  return `<!DOCTYPE html>
<html lang="zh-CN">
<head>
<meta charset="utf-8">
<title>${escapeHtmlText(title)}</title>
<link rel="icon" type="image/png" href="/favicon.png">
<link rel="stylesheet" href="dist/reveal.css">
<style>
  :root { color-scheme: dark; }
  body { margin:0; background:${t.bg}; color:${t.fg}; font-family:"Microsoft YaHei","PingFang SC",sans-serif; }
  .deck { width:1200px; margin:0 auto; padding:60px 0; }
  .cover { text-align:center; padding:80px 0 60px; }
  .cover h1 { font-size:56px; margin:0 0 16px; letter-spacing:2px; }
  .cover p { color:${t.muted}; font-size:18px; margin:0; }
  .cover .badge { display:inline-block; margin-top:28px; padding:8px 18px; border-radius:999px; border:1px solid ${t.accent}; color:${t.accent}; font-size:14px; }
  .slide { background:linear-gradient(180deg, rgba(255,255,255,0.03), rgba(255,255,255,0)); border:1px solid rgba(255,255,255,0.08); border-radius:18px; padding:36px 44px; margin:0 0 28px; position:relative; }
  .slide-index { position:absolute; top:28px; right:36px; color:${t.muted}; font-size:14px; letter-spacing:2px; }
  .slide h2 { font-size:30px; margin:0 0 22px; color:${t.accent}; }
  .slide ul { margin:0; padding-left:22px; line-height:2.1; font-size:17px; }
  .slide li::marker { color:${t.accent}; }
  .footer { text-align:center; color:${t.muted}; font-size:13px; padding:20px 0 40px; }
</style>
</head>
<body>
<div class="deck">
  <div class="cover">
    <h1>${escapeHtmlText(title)}</h1>
    <p>由 Prisdoc 智能演示文稿生成器 · ${new Date().toLocaleDateString('zh-CN')}</p>
    <div class="badge">主题：${escapeHtmlText(theme)}</div>
  </div>
  ${slides}
  <div class="footer">本页面由 Mock 模式生成，仅用于界面演示</div>
</div>
<script src="dist/reveal.js"></script>
</body>
</html>`
}

function escapeHtmlText(value) {
  return String(value ?? '')
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
}
