<template>
  <div class="feishu-layout">
    <el-container class="full-height">

      <!-- 1. 左侧：极简侧边栏 -->
      <el-aside width="240px" class="feishu-aside">
        <div class="aside-top">
          <div class="brand">
            <div class="logo-box"><img class="brand-mark" src="/brand/prisdvl-nexus.png" alt="" /></div>
            <span class="brand-text">Prisdoc</span></div>
          <div class="nav-menu">
            <div class="nav-item active">我的工作台</div>
            <div class="nav-item">云端文档库</div>
            <div class="nav-item" @click="goToCapabilities">能力中心</div>
            <div v-if="isAdminUser" class="nav-item aiops-nav" @click="goToAIOps">
              运维中心
            </div>
          </div>
        </div>

        <!-- 底部：真实用户信息 (头像取首字母) -->
        <div class="aside-bottom">
          <el-dropdown trigger="click" placement="top-start" @command="handleUserCommand">
            <div class="user-profile">
              <el-avatar :size="32" style="background-color: var(--pd-primary); font-weight: bold; color: #E8EEF8;">
                {{ currentUserName.charAt(0).toUpperCase() }}
              </el-avatar>
              <span class="username">{{ currentUserName }}</span>
              <el-icon class="more-icon"><MoreFilled /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout" style="color: var(--pd-danger)">退出系统</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-aside>

      <!-- 2. 右侧：主内容区 -->
      <el-main class="feishu-main" v-loading="loading">
        <div class="main-container">

          <!-- AI 欢迎与对话区 (对标飞书 aily) -->
          <div class="ai-hero">
            <el-avatar :size="64" style="background-color: var(--pd-primary); font-size: 28px; font-weight: bold; margin-bottom: 24px; color: #E8EEF8;">
              {{ currentUserName.charAt(0).toUpperCase() }}
            </el-avatar>
            <h1 class="hero-title">Hi {{ currentUserName }}，今天想先处理哪份文档？</h1>

            <!-- 飞书级交互：巨大输入框 + 气泡弹出菜单 -->
            <el-popover
                placement="bottom-start"
                :width="700"
                trigger="click"
                :show-arrow="false"
                popper-class="feishu-popover"
            >
              <template #reference>
                <div class="hero-search">
                  <input
                      v-model="aiTask"
                      class="hero-input"
                      placeholder="描述你要做的事，例如「把这份制度改写成三条要点」"
                      @keyup.enter="handleAiTask"
                  />
                  <div class="send-action-btn" @click.stop="handleAiTask">
                    <el-icon size="18"><Top /></el-icon>
                  </div>
                </div>
              </template>

              <!-- 弹出菜单：上传入口隐藏在这里 -->
              <div class="popover-content">
                <div class="menu-list">
                  <input type="file" ref="fileInputRef" style="display: none;" @change="onFileSelected" />
                  <div class="menu-item" @click="triggerUpload">
                    上传文件并解析内容
                  </div>
                  <div class="menu-item" @click="mockSkill('写会议纪要')">起草会议纪要</div>
                  <div class="menu-item" @click="mockSkill('分析文档')">提取关键数据</div>
                </div>
              </div>
            </el-popover>

            <!-- 快捷胶囊按钮 -->
            <div class="quick-prompts">
              <div class="prompt-pill" @click="triggerUpload">上传文档</div>
              <div class="prompt-pill" @click="pptDialogVisible = true">生成演示文稿</div>
              <div class="prompt-pill" @click="mockSkill('对话')">开始对话</div>
            </div>
          </div>

          <!-- 最近编辑 + 本周概览：先看发生了什么，再进文档列表 -->
          <div class="insight-row" v-if="docList.length">
            <div class="insight-panel">
              <div class="panel-head">
                <span class="panel-title">最近编辑</span>
                <span class="panel-hint">按更新时间排序</span>
              </div>
              <ul class="timeline">
                <li
                    class="timeline-item"
                    v-for="doc in recentDocs"
                    :key="doc.id"
                    @click="goToEditor(doc.id, doc.name)"
                >
                  <span class="timeline-dot" :class="doc.analyzed ? 'ok' : 'pending'"></span>
                  <div class="timeline-main">
                    <div class="timeline-name">{{ doc.name }}</div>
                    <div class="timeline-meta">{{ relativeTime(doc.updated) }} · {{ doc.analyzed ? '已解析' : '待解析' }}</div>
                  </div>
                  <span class="timeline-go">打开 →</span>
                </li>
              </ul>
            </div>

            <div class="insight-panel">
              <div class="panel-head">
                <span class="panel-title">本周概览</span>
              </div>
              <div class="insight-stats">
                <div class="insight-stat">
                  <span class="num">{{ docList.length }}</span>
                  <span class="label">文档总数</span>
                </div>
                <div class="insight-stat">
                  <span class="num">{{ weekStats.updated }}</span>
                  <span class="label">7 天内更新</span>
                </div>
                <div class="insight-stat">
                  <span class="num">{{ weekStats.analyzedRatio }}%</span>
                  <span class="label">已解析占比</span>
                </div>
              </div>
            </div>
          </div>

          <!-- 最近文档列表 -->
          <div class="list-section">
            <div class="section-header">
              <span class="section-title">我的云端文档 <span class="count">({{ docList.length }})</span></span>
              <div class="header-actions">
                <el-button type="danger" size="small" plain @click="clearDirtyData">清理文档</el-button>
                <el-input
                    v-model="search"
                    placeholder="搜索文档标题，回车检索"
                    size="small"
                    style="width: 220px"
                    clearable
                    @keyup.enter="handleSearch"
                    @clear="fetchFiles"
                />
              </div>
            </div>

            <!-- 卡片网格布局 -->
            <div class="card-grid" v-if="filteredList.length > 0">
              <div class="doc-card" v-for="doc in filteredList" :key="doc.id">

                <!-- 卡片上半部分渐变封面 -->
                <div class="card-cover" :class="doc.color" @click="goToEditor(doc.id, doc.name)">
                </div>

                <!-- 卡片下半部分 -->
                <div class="card-info">
                  <div class="info-top">
                    <el-tooltip :content="doc.name" placement="top" :show-after="500">
                      <div class="doc-name" @click="goToEditor(doc.id, doc.name)">{{ doc.name }}</div>
                    </el-tooltip>
                    <el-dropdown trigger="click" @command="(cmd) => handleCardCommand(cmd, doc)">
                      <el-icon class="more-btn"><MoreFilled /></el-icon>
                      <template #dropdown>
                        <el-dropdown-menu>
                          <!-- 下载、删除选项 -->
                          <el-dropdown-item command="download">下载到本地</el-dropdown-item>
                          <el-dropdown-item command="delete" style="color: var(--pd-danger)">删除文档</el-dropdown-item>
                        </el-dropdown-menu>
                      </template>
                    </el-dropdown>
                  </div>
                  <div class="doc-meta">
                    <span>{{ doc.time }}</span>
                    <el-tag size="small" :type="doc.analyzed ? 'success' : 'info'" round effect="light">
                      {{ doc.analyzed ? '已解析' : '待解析' }}
                    </el-tag>
                  </div>
                </div>
              </div>
            </div>

            <el-empty v-else description="还没有文档。上传一份，或用上方输入框描述你的需求。" />
          </div>

        </div>
      </el-main>
    </el-container>

    <!-- PPT 生成器弹窗 -->
    <el-dialog v-model="pptDialogVisible" title="🎨 HTML PPT 生成器" width="600px" destroy-on-close>
      <el-form :model="pptForm" label-position="top">
        <el-alert :title="`即将使用 [${currentModelName}] 为您生成内容`" type="info" show-icon :closable="false" style="margin-bottom: 15px;" />
        <el-form-item label="📌 演示标题">
          <el-input v-model="pptForm.title" placeholder="例如：2026年Prisdoc项目" />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="🎨 视觉主题">
              <el-select v-model="pptForm.theme" style="width: 100%">
                <el-option label="Tokyo Night（深色）" value="tokyo-night" />
                <el-option label="Dracula（深色）" value="dracula" />
                <el-option label="Catppuccin Mocha（深色）" value="catppuccin-mocha" />
                <el-option label="Nord（深色）" value="nord" />
                <el-option label="Corporate Clean（商务）" value="corporate-clean" />
                <el-option label="Minimal White（极简白）" value="minimal-white" />
                <el-option label="Cyberpunk Neon（赛博）" value="cyberpunk-neon" />
                <el-option label="Aurora（极光）" value="aurora" />
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="🧠 驱动大模型">
              <el-select v-model="pptForm.model" style="width: 100%" placeholder="加载中..." :loading="modelsLoading">
                <el-option
                    v-for="m in aiModelsList"
                    :key="m.code"
                    :label="m.name"
                    :value="m.code"
                    :disabled="!m.available"
                >
                  <span style="float: left">{{ m.name }}</span>
                  <span style="float: right; color: var(--pd-text-3); font-size: 12px">{{ m.provider }}</span>
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="📝 大纲（每行一个章节）">
          <el-input v-model="pptForm.outline" type="textarea" :rows="5" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handlePptAction('preview')" :loading="pptLoading">👀 浏览器预览</el-button>
          <el-button type="primary" @click="handlePptAction('download')" :loading="pptLoading">💾 下载</el-button>
        </span>
      </template>
    </el-dialog>

  </div>

</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { fileApi } from '../../api/file'
import { userApi } from '../../api/user'
import { docApi } from '../../api/document'
import { aiApi } from '../../api/ai'
import { STORAGE_KEYS } from '../../constants'
import { isAdmin } from '../../utils/jwt'

const router = useRouter()
const loading = ref(false)
const currentUserName = ref('User')
const docList = ref([])
const search = ref('')
const aiTask = ref('')
const fileInputRef = ref(null)
const modelsLoading = ref(false)
const aiModelsList = ref([])
const pptDialogVisible = ref(false)
const pptLoading = ref(false)
const pptForm = ref({
  title: 'Prisdoc 项目',
  theme: 'tokyo-night',
  outline: '项目背景与痛点\n核心微服务架构\nRAG 知识检索增强\n未来商业展望',
  model: 'qwen3.6-plus'
})

const isAdminUser = ref(isAdmin())

const getCurrentUserId = () => localStorage.getItem('userId')

// 计算当前选中的模型名字（用于弹窗提示）
const currentModelName = computed(() => {
  const model = aiModelsList.value.find(m => m.code === pptForm.value.model)
  return model ? model.name : pptForm.value.model
})

// 获取用户信息
const fetchUser = async () => {
  const uid = getCurrentUserId()
  if (uid) {
    try {
      const res = await userApi.getUserInfo(uid)
      currentUserName.value = res.data.username || 'User'
    } catch (e) { currentUserName.value = '测试用户' }
  }
}

// 获取文档列表 (从 8084 获取)
const fetchFiles = async () => {
  const uid = getCurrentUserId()
  if (!uid) {
    router.push('/login')
    return
  }
  loading.value = true
  try {
    const res = await docApi.getUserDocs(uid)
    if (res.data && Array.isArray(res.data)) {
      docList.value = res.data.map(item => ({
        id: item.id,
        fileId: item.fileId,
        name: item.title || '未命名文档',
        time: item.createTime ? new Date(item.createTime).toLocaleDateString() : '未知',
        analyzed: !!item.summary,
        updated: item.updateTime || item.createTime,
        color: ['bg-blue', 'bg-orange', 'bg-green', 'bg-purple'][Math.floor(Math.random() * 4)]
      }))
    }
  } catch (e) {
    console.error('拉取列表失败', e)
  } finally { loading.value = false }
}

// 获取 AI 模型列表
const fetchModels = async () => {
  modelsLoading.value = true
  try {
    const res = await aiApi.getModels().catch(() => null)
    if (res && res.data && res.data.length > 0) {
      aiModelsList.value = res.data
      const firstAvailable = res.data.find(m => m.available)
      if (firstAvailable) pptForm.value.model = firstAvailable.code
    } else {
      // 容灾假数据
      aiModelsList.value = [
        { code: 'qwen-plus', name: '通义千问 Plus (Mock)', available: true, provider: '阿里云' },
        { code: 'deepseek-v3.2', name: 'DeepSeek (Mock)', available: true, provider: 'DeepSeek' }
      ]
    }
  } finally {
    modelsLoading.value = false
  }
}

onMounted(() => { fetchUser(); fetchFiles(); fetchModels() })

const filteredList = computed(() => docList.value.filter(d => d.name.toLowerCase().includes(search.value.toLowerCase())))

// 搜索逻辑
const handleSearch = async () => {
  if (!search.value.trim()) {
    return fetchFiles() // 如果搜空，恢复全量列表
  }

  loading.value = true
  try {
    // 调 8084 接口进行全文检索（搜标题+内容）
    const res = await docApi.searchDocs(search.value)

    if (res.data) {
      docList.value = res.data.map(item => ({
        id: item.id,
        fileId: item.fileId,
        name: item.title,
        time: new Date(item.createTime).toLocaleDateString(),
        analyzed: !!item.summary,
        updated: item.updateTime || item.createTime,
        color: ['bg-blue', 'bg-orange', 'bg-green', 'bg-purple'][Math.floor(Math.random() * 4)]
      }))
    }
    ElMessage.success(`搜索到 ${docList.value.length} 篇相关文档`)
  } catch (e) {
    ElMessage.error('搜索失败')
  } finally {
    loading.value = false
  }
}


// 飞书化上传交互
const triggerUpload = () => fileInputRef.value?.click()

const onFileSelected = async (event) => {
  const rawFile = event.target.files[0]
  if (!rawFile) return

  loading.value = true
  try {
    // 先传文件仓库
    const fileRes = await fileApi.upload(rawFile)
    const fileId = fileRes?.data?.fileId || fileRes?.data?.id
    if (!fileId) {
      throw new Error('文件上传成功但没有返回 fileId')
    }

    // 在文档服务注册
    await docApi.createDoc({
      title: rawFile.name,
      fileId: fileId,
      category: 'default'
    })

    ElMessage.success(`《${rawFile.name}》已成功存入云端！`)
    await fetchFiles()
    event.target.value = ''
  } catch (e) {
    console.error('上传链路异常', e)
    ElMessage.error('上传链路异常')
  } finally {
    event.target.value = ''
    loading.value = false
  }
}

// 卡片逻辑(删除下载)
const handleCardCommand = async (cmd, doc) => {
  if (cmd === 'delete') {
    ElMessageBox.confirm(`确定要永久删除《${doc.name}》吗？`).then(async () => {
      loading.value = true
      try {
        await docApi.deleteDoc(doc.id)
        if (doc.fileId) await fileApi.delete(doc.fileId).catch(()=>{})
        ElMessage.success('已删除')
        fetchFiles()
      } catch (e) { ElMessage.error('删除失败') }
      finally { loading.value = false }
    }).catch(() => {})
  }
  // 下载部分
  else if (cmd === 'download') {
    loading.value = true
    try {
      const res = await docApi.getDocDetail(doc.id)
      const content = res.data.content || ""

      // 使用 pre 标签保护格式
      const wordHtml = `
        <html xmlns:o='urn:schemas-microsoft-com:office:office' xmlns:w='urn:schemas-microsoft-com:office:office:word' xmlns='http://www.w3.org/TR/REC-html40'>
        <head><meta charset='utf-8'></head>
        <body style="font-family: sans-serif;">
          <pre style="white-space: pre-wrap;">${content}</pre>
        </body>
        </html>`

      const blob = new Blob([wordHtml], { type: 'application/msword' })
      const url = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url; link.setAttribute('download', `[最新]_${doc.name}.doc`)
      document.body.appendChild(link); link.click(); document.body.removeChild(link)
      window.URL.revokeObjectURL(url); ElMessage.success('下载完成')
    } catch (e) { ElMessage.error('获取最新内容失败') }
    finally { loading.value = false }
  }
}

// 一键清理文档
const clearDirtyData = () => {
  ElMessageBox.confirm('这会强行清空列表里所有的文档记录，确定吗？').then(async () => {
    loading.value = true
    for (const doc of docList.value) {
      await docApi.deleteDoc(doc.id).catch(()=>{})
    }
    docList.value = []
    loading.value = false
    ElMessage.success('清理完成')
  })
}

// ppt生成逻辑
const handlePptAction = async (actionType) => {
  pptLoading.value = true
  try {
    // 获取原始 HTML (根据类型选择接口)
    let rawHtml = ""
    if (actionType === 'download') {
      const blob = await aiApi.downloadPpt(pptForm.value)
      rawHtml = await blob.text()
    } else {
      const result = await aiApi.previewPpt(pptForm.value)
      rawHtml = typeof result === 'string' ? result : result.data
    }

    if (!rawHtml || rawHtml.length < 10) throw new Error('生成内容为空')

    rawHtml = rawHtml.replace(/<li>\s*(?:[-*•○]|o\s+|O\s+|\d+[.、)）])\s*/g, '<li>')
    //  **加粗** 符号，也可以顺手清理掉
    rawHtml = rawHtml.replace(/\*\*/g, '')

    // 统一净化与增强
    const cdn = "https://cdn.jsdelivr.net/npm/reveal.js@5.0.4"
    let processedHtml = rawHtml
        .replace(/dist\/reveal\.css/g, `${cdn}/dist/reveal.css`)
        .replace(/dist\/reveal\.js/g, `${cdn}/dist/reveal.js`)
        .replace(/plugin\/notes\/notes\.js/g, `${cdn}/plugin/notes/notes.js`)
        .replace(/dist\/theme\/[a-z-]+\.css/g, (m) => `${cdn}/${m}`)
        .replace(/history:\s*true/g, 'history: false')

    // 注入安全补丁
    const injection = `
    <script>
      if (window.top !== window.self && !window.location.search.includes('preview')) { window.stop(); }
    <\/script>`
    processedHtml = processedHtml.replace('</head>', `${injection}</head>`)

    // 执行最终动作
    if (actionType === 'download') {
      const blob = new Blob([processedHtml], { type: 'text/html' })
      const url = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.setAttribute('download', `${pptForm.value.title || '演示文稿'}.html`)
      document.body.appendChild(link); link.click(); document.body.removeChild(link)
      window.URL.revokeObjectURL(url)
      ElMessage.success('PPT 已导出')
    } else {
      sessionStorage.setItem('temp_ppt_render_source', processedHtml)
      window.open('/ppt-runtime', '_blank')
    }

  } catch (e) {
    console.error('PPT 处理失败:', e)
    ElMessage.error('生成失败，请重试')
  } finally {
    pptLoading.value = false
  }
}


// 跳转编辑器
const goToEditor = (id, name) => {
  sessionStorage.setItem('currentDocName', name)
  router.push(`/editor/${id}`)
}


const handleAiTask = () => {
  if (!aiTask.value.trim()) return
  sessionStorage.setItem('global_ai_task', aiTask.value)
  aiTask.value = ''
  router.push('/editor/chat-mode')
}

const mockSkill = (name) => {
  aiTask.value = name
  handleAiTask()
}

const handleUserCommand = (cmd) => {
  if (cmd === 'logout') { localStorage.clear(); router.push('/login'); }
}

const goToAIOps = () => {
  router.push('/aiops')
}

const goToCapabilities = () => {
  router.push('/capabilities')
}

// 最近编辑：按 updateTime 倒序取前 5 条
const recentDocs = computed(() => [...docList.value]
    .sort((a, b) => new Date(b.updated || 0) - new Date(a.updated || 0))
    .slice(0, 5))

// 本周概览：7 天内更新数 + 已解析占比
const weekStats = computed(() => {
  const week = 7 * 24 * 60 * 60 * 1000
  const now = Date.now()
  const updated = docList.value.filter((d) => {
    const t = d.updated ? new Date(d.updated).getTime() : NaN
    return !Number.isNaN(t) && now - t <= week
  }).length
  const analyzed = docList.value.filter((d) => d.analyzed).length
  const total = docList.value.length || 1
  return { updated, analyzedRatio: Math.round((analyzed / total) * 100) }
})

const relativeTime = (iso) => {
  if (!iso) return '未知'
  const t = new Date(iso).getTime()
  if (Number.isNaN(t)) return '未知'
  const minutes = Math.round((Date.now() - t) / 60000)
  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes} 分钟前`
  const hours = Math.round(minutes / 60)
  if (hours < 24) return `${hours} 小时前`
  const days = Math.round(hours / 24)
  if (days < 30) return `${days} 天前`
  return new Date(iso).toLocaleDateString('zh-CN')
}
</script>

<style scoped>
/* 页面底：深海军蓝 */
.feishu-layout { height: 100vh; background-color: var(--pd-bg); }
.full-height { height: 100%; }

/* 侧边栏样式 */
.feishu-aside { background: var(--pd-surface); border-right: 1px solid var(--pd-border); display: flex; flex-direction: column; justify-content: space-between; }
.aside-top { padding: 24px 16px; }
.brand { display: flex; align-items: center; gap: 12px; margin-bottom: 20px; font-weight: bold; font-size: 18px; padding: 0 8px; }
.logo-box { width: 32px; height: 32px; display: flex; align-items: center; justify-content: center; }
.brand-mark { display: block; height: 30px; width: auto; }
.nav-item { padding: 12px 16px; border-radius: var(--pd-r-md); cursor: pointer; display: flex; align-items: center; gap: 10px; font-size: 14px; margin-bottom: 4px; color: var(--pd-text-2); transition: background var(--pd-dur-fast) var(--pd-ease), color var(--pd-dur-fast) var(--pd-ease); }
.nav-item:hover { background: var(--pd-hover); color: var(--pd-text); }
.nav-item.active { background: var(--pd-primary-weak); color: var(--pd-primary-text); font-weight: 500; }
/* AI Ops 入口保留 warning 语义色（新 UI 里唯一一处非品牌色，用来区分运维区） */
.nav-item.aiops-nav { color: var(--pd-warning); }
.nav-item.aiops-nav:hover { background: var(--pd-warning-weak); color: var(--pd-warning); }

.aside-bottom { padding: 16px; border-top: 1px solid var(--pd-border); }
.user-profile { display: flex; align-items: center; gap: 10px; padding: 8px; cursor: pointer; border-radius: var(--pd-r-md); transition: background var(--pd-dur-fast) var(--pd-ease); }
.user-profile:hover { background: var(--pd-hover); }
.username { font-size: 14px; font-weight: 500; color: var(--pd-text);}

/* AI 欢迎区 */
.feishu-main { padding: 0; display: flex; justify-content: center; overflow-y: auto; }
.main-container { width: 100%; max-width: 1050px; padding: 40px 32px; }
.ai-hero { display: flex; flex-direction: column; align-items: center; margin-bottom: 50px; padding-top: 40px;}
.hero-title { font-size: 26px; color: var(--pd-text); margin-bottom: 36px; font-weight: 600; letter-spacing: 0.5px;}

/* 巨大输入框 */
.hero-search {
  width: 100%; max-width: 700px; height: 64px; background: var(--pd-surface); border-radius: 32px;
  box-shadow: var(--pd-shadow-md); display: flex; align-items: center; padding: 0 12px 0 24px;
  border: 1px solid var(--pd-border); transition: border-color var(--pd-dur) var(--pd-ease), box-shadow var(--pd-dur) var(--pd-ease); cursor: text;
}
.hero-search:focus-within { border-color: var(--pd-primary-hover); box-shadow: var(--pd-shadow-md), 0 0 0 3px rgba(57, 135, 236, 0.18); }
.prefix-icon { font-size: 22px; color: var(--pd-text-3); margin-right: 12px; }
.hero-input { flex: 1; border: none; outline: none; font-size: 16px; color: var(--pd-text); background: transparent; }
.hero-input::placeholder { color: var(--pd-text-3); }
.send-action-btn { width: 40px; height: 40px; border-radius: 50%; background: var(--pd-primary); color: #E8EEF8; display: flex; justify-content: center; align-items: center; cursor: pointer; transition: background var(--pd-dur-fast) var(--pd-ease); }
.send-action-btn:hover { background: var(--pd-primary-hover); }

/* 菜单项样式 */
.menu-item { padding: 12px; border-radius: var(--pd-r-sm); cursor: pointer; display: flex; align-items: center; gap: 10px; font-size: 14px; color: var(--pd-text-2); transition: background var(--pd-dur-fast) var(--pd-ease), color var(--pd-dur-fast) var(--pd-ease); }
.menu-item:hover { background: var(--pd-hover); color: var(--pd-text); }

/* 快捷胶囊 */
.quick-prompts { display: flex; gap: 12px; margin-top: 24px; justify-content: center; }
.prompt-pill { padding: 8px 16px; background: var(--pd-surface); border: 1px solid var(--pd-border); border-radius: 20px; font-size: 13px; font-weight: 500; color: var(--pd-text-2); cursor: pointer; transition: border-color var(--pd-dur-fast) var(--pd-ease), color var(--pd-dur-fast) var(--pd-ease); }
.prompt-pill:hover { border-color: var(--pd-primary-hover); color: var(--pd-primary-text); }

/* 文档卡片区 */
.list-section { margin-top: 40px; }
.section-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; padding-bottom: 12px; border-bottom: 1px solid var(--pd-border);}
.section-title { font-size: 18px; font-weight: 600; color: var(--pd-text);}

.card-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(240px, 1fr)); gap: 24px; }
.doc-card { background: var(--pd-surface); border-radius: var(--pd-r-lg); border: 1px solid var(--pd-border); overflow: hidden; box-shadow: var(--pd-shadow-sm); transition: transform var(--pd-dur) var(--pd-ease), box-shadow var(--pd-dur) var(--pd-ease), border-color var(--pd-dur) var(--pd-ease); }
.doc-card:hover { transform: translateY(-4px); box-shadow: var(--pd-shadow-md); border-color: var(--pd-border-strong); }
.card-cover { height: 130px; display: flex; align-items: center; justify-content: center; position: relative; }
/* 无图标的封面：靠一层柔光 + 一条左侧书脊线做出"封面"的层次 */
.card-cover::before {
  content: '';
  position: absolute;
  inset: 0;
  margin: auto;
  width: 44px;
  height: 56px;
  border-radius: 5px;
  border: 1px solid rgba(232, 238, 248, 0.22);
  background: rgba(232, 238, 248, 0.06);
  box-shadow: 6px 6px 0 -1px rgba(232, 238, 248, 0.05), 12px 12px 0 -2px rgba(232, 238, 248, 0.03);
}
.card-cover::after {
  content: '';
  position: absolute;
  inset: 0;
  pointer-events: none;
  background: radial-gradient(100% 120% at 18% -20%, rgba(117, 200, 255, 0.12), transparent 62%);
  box-shadow: inset 2px 0 0 rgba(232, 238, 248, 0.08);
}
.card-info { padding: 12px 16px; }
.info-top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.doc-name { font-weight: 500; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; color: var(--pd-text); cursor: pointer; flex: 1; }
.doc-meta { color: var(--pd-text-3); }
.more-btn { color: var(--pd-text-3); cursor: pointer; font-size: 18px; transition: color var(--pd-dur-fast) var(--pd-ease); }
.more-btn:hover { color: var(--pd-text); }

/* 最近编辑 + 本周概览 */
.insight-row {
  display: grid;
  grid-template-columns: 1fr 300px;
  gap: 24px;
  margin-top: 40px;
}
.insight-panel {
  background: var(--pd-surface);
  border: 1px solid var(--pd-border);
  border-radius: var(--pd-r-lg);
  box-shadow: var(--pd-shadow-sm);
  padding: 18px 20px;
}
.panel-head { display: flex; align-items: baseline; justify-content: space-between; margin-bottom: 12px; }
.panel-title { font-size: 15px; font-weight: 600; color: var(--pd-text); }
.panel-hint { font-size: 12px; color: var(--pd-text-3); }

.timeline { list-style: none; margin: 0; padding: 0; }
.timeline-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 8px;
  border-radius: var(--pd-r-md);
  cursor: pointer;
  transition: background var(--pd-dur-fast) var(--pd-ease);
}
.timeline-item:hover { background: var(--pd-hover); }
.timeline-dot { width: 6px; height: 6px; border-radius: 50%; flex-shrink: 0; background: var(--pd-text-3); }
.timeline-dot.ok { background: var(--pd-success); }
.timeline-dot.pending { background: var(--pd-warning); }
.timeline-main { flex: 1; min-width: 0; }
.timeline-name { font-size: 13.5px; color: var(--pd-text); white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.timeline-meta { font-size: 12px; color: var(--pd-text-3); margin-top: 3px; }
.timeline-go { font-size: 12px; color: var(--pd-primary-text); opacity: 0; transition: opacity var(--pd-dur-fast) var(--pd-ease); }
.timeline-item:hover .timeline-go { opacity: 1; }

.insight-stats { display: flex; flex-direction: column; gap: 14px; }
.insight-stat { display: flex; align-items: baseline; justify-content: space-between; }
.insight-stat .num { font-family: var(--pd-font-mono); font-size: 22px; font-weight: 600; color: var(--pd-text); }
.insight-stat .label { font-size: 12.5px; color: var(--pd-text-3); }

@media (max-width: 1120px) {
  .insight-row { grid-template-columns: 1fr; }
}

/* 封面：统一为一套深蓝梯度（去掉糖果色），图标仍是白色 90% 透明 */
.bg-blue { background: linear-gradient(135deg, #0B1730, #12294F); }
.bg-orange { background: linear-gradient(135deg, #0D1B36, #173056); }
.bg-green { background: linear-gradient(135deg, #0A1428, #14264A); }
.bg-purple { background: linear-gradient(135deg, #101C38, #1B2F5C); }
</style>
<style>
/* teleport 到 body 的弹出层：必须写全局，且不能带 scoped */
.feishu-popover {
  padding: 8px !important;
  border-radius: var(--pd-r-md) !important;
  background: var(--pd-surface) !important;
  border: 1px solid var(--pd-border) !important;
  box-shadow: var(--pd-shadow-md) !important;
}
</style>
