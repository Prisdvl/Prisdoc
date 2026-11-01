<template>
  <div class="agent-workbench">
    <header class="workbench-header">
      <div class="workbench-crumb" @click="$router.push('/dashboard')">返回工作台</div>
      <h2><img class="brand-mark" src="/brand/prisdvl-nexus.png" alt="" /> Prisdoc Agent 工作台</h2>
      <p>查看可用工具、跑一次任务、处理高危操作审批、回看历史记录。</p>
    </header>

    <!-- 整个页面用 el-scrollbar 包裹 -->
    <el-scrollbar class="workbench-scrollbar">
      <div class="workbench-content">
        <el-card class="overview-card" shadow="hover">
          <div class="overview-header">
            <span>工作台总览</span>
            <div class="actions">
              <el-button type="primary" size="small" @click="fetchOverview" :loading="loading">刷新总览</el-button>
            </div>
          </div>
          <el-row :gutter="20" class="stat-row" v-if="overviewData">
            <el-col :span="4"><div class="stat-box"><div class="num">{{ overviewData.tools?.length || 0 }}</div><div class="label">可用工具</div></div></el-col>
            <el-col :span="4"><div class="stat-box"><div class="num">{{ overviewData.tasks?.length || 0 }}</div><div class="label">Agent 任务</div></div></el-col>
            <el-col :span="4"><div class="stat-box"><div class="num">{{ overviewData.knowledgeJobs?.length || 0 }}</div><div class="label">知识索引任务</div></div></el-col>
          </el-row>
        </el-card>

        <el-row :gutter="20" class="main-layout">
          <el-col :span="8">
            <el-card class="box-card" shadow="hover">
              <template #header>
                <div class="card-title">1. 可用工具（{{ toolsList.length }}）</div>
              </template>
              <!-- 工具列表用 el-scrollbar -->
              <el-scrollbar max-height="400px" v-loading="loading">
                <div class="tools-list">
                  <div v-for="tool in toolsList" :key="tool.name" class="tool-item">
                    <div class="tool-head">
                      <span class="tool-name">{{ tool.name }}</span>
                      <el-tag size="small" :type="tool.riskLevel === 'HIGH' ? 'danger' : 'success'">
                        {{ tool.riskLevel || 'LOW' }}
                      </el-tag>
                    </div>
                    <div class="tool-desc">{{ tool.description }}</div>
                  </div>
                </div>
              </el-scrollbar>
            </el-card>

            <el-card class="box-card mt-20" shadow="hover">
              <template #header>
                <div class="card-title">2. 高危操作审批</div>
              </template>
              <el-input v-model="approvalToken" placeholder="输入 agentApprovalToken" style="margin-bottom: 10px;" />
              <el-input v-model="approvalContext" type="textarea" :rows="3" placeholder='可选 context JSON，例如 {"objectName":"a.txt"}' style="margin-bottom: 15px;" />
              <div class="approval-actions">
                <el-button type="primary" @click="handleConfirmApproval" :loading="executing">确认继续</el-button>
                <el-button type="danger" plain @click="handleCancelApproval">取消拒绝</el-button>
              </div>
            </el-card>
          </el-col>

          <el-col :span="16">
            <el-card class="box-card" shadow="hover">
              <template #header>
                <div class="card-title">3. 执行任务</div>
              </template>
              <el-input
                  v-model="agentPrompt"
                  type="textarea"
                  :rows="4"
                  placeholder="例如：帮我把刚才的会议纪要总结一下，然后生成一份PPT存到本地..."
                  style="margin-bottom: 15px;"
              />
              <div class="execute-actions">
                <el-button type="primary" size="large" @click="executeAgent" :loading="executing">
                  执行任务
                </el-button>
              </div>

              <!-- 执行结果用 el-scrollbar -->
              <div class="result-viewer" v-if="executionResult">
                <h4>执行结果</h4>
                <el-scrollbar max-height="300px">
                  <pre class="json-code">{{ JSON.stringify(executionResult, null, 2) }}</pre>
                </el-scrollbar>
              </div>
            </el-card>

            <el-card class="box-card mt-20" shadow="hover">
              <template #header>
                <div class="card-title">4. 任务记录</div>
              </template>
              <!-- 任务历史表格用 el-scrollbar -->
              <el-scrollbar max-height="400px">
                <el-table :data="tasksHistory" stripe style="width: 100%" size="small">
                  <el-table-column prop="traceId" label="Trace ID" width="220" show-overflow-tooltip />
                  <el-table-column prop="task" label="任务内容" show-overflow-tooltip />
                  <el-table-column prop="status" label="状态" width="100">
                    <template #default="{ row }">
                      <el-tag :type="row.status === 'completed' ? 'success' : (row.status === 'action_required' ? 'warning' : 'info')">
                        {{ row.status }}
                      </el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column label="操作" width="100">
                    <template #default="{ row }">
                      <el-button type="primary" link @click="viewTaskDetail(row.traceId)">查看详情</el-button>
                    </template>
                  </el-table-column>
                </el-table>
              </el-scrollbar>
            </el-card>
          </el-col>
        </el-row>
      </div>
    </el-scrollbar>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { agentApi } from '../../api/agent'
import { ElMessage } from 'element-plus'

// 数据状态
const loading = ref(false)
const executing = ref(false)
const overviewData = ref(null)
const toolsList = ref([])
const tasksHistory = ref([])

// 表单输入
const agentPrompt = ref('')
const approvalToken = ref('')
const approvalContext = ref('')
const executionResult = ref(null)

// 1. 获取全局大盘数据
const fetchOverview = async () => {
  loading.value = true
  try {
    const res = await agentApi.getOverview()
    overviewData.value = res.data
    toolsList.value = res.data.tools || []
    tasksHistory.value = res.data.tasks || []
  } catch (e) {
    ElMessage.error('获取工作台数据失败')
  } finally {
    loading.value = false
  }
}

// 2. 发起 Agent 任务
const executeAgent = async () => {
  if (!agentPrompt.value.trim()) return ElMessage.warning('请输入指令')

  executing.value = true
  executionResult.value = null
  try {
    const res = await agentApi.executeTask({
      task: agentPrompt.value,
      context: {}
    })
    executionResult.value = res.data

    // 如果需要审批
    if (res.data.status === 'action_required') {
      ElMessage.warning('触发高危操作，请在左侧面板进行审批！')
      approvalToken.value = res.data.approvalToken // 自动填入 token 方便测试
    } else {
      ElMessage.success('执行完毕')
    }

    fetchOverview() // 刷新列表
  } catch (e) {
    ElMessage.error('执行失败')
  } finally {
    executing.value = false
  }
}

// 3. 审批通过
const handleConfirmApproval = async () => {
  if (!approvalToken.value) return ElMessage.warning('请输入审批 Token')
  executing.value = true
  try {
    let ctx = {}
    if (approvalContext.value) {
      ctx = JSON.parse(approvalContext.value)
    }
    const res = await agentApi.confirmApproval({
      approvalToken: approvalToken.value,
      context: ctx
    })
    ElMessage.success('审批已确认，任务继续执行！')
    executionResult.value = res.data
    approvalToken.value = ''
    fetchOverview()
  } catch (e) {
    ElMessage.error('审批格式错误或执行失败')
  } finally {
    executing.value = false
  }
}

// 4. 审批拒绝
const handleCancelApproval = async () => {
  if (!approvalToken.value) return
  try {
    await agentApi.cancelApproval(approvalToken.value)
    ElMessage.success('已丢弃该审批')
    approvalToken.value = ''
    fetchOverview()
  } catch (e) {
    ElMessage.error('取消失败')
  }
}

// 查看任务详情
const viewTaskDetail = async (traceId) => {
  try {
    const res = await agentApi.getTaskHistory(traceId)
    executionResult.value = res.data // 直接在右侧的 JSON 框里展示快照
    ElMessage.success('已加载任务快照')
  } catch (e) {}
}

onMounted(() => {
  fetchOverview()
})
</script>

<style scoped>
.agent-workbench {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: var(--pd-bg);
  overflow: hidden; /* 防止整个页面出现双滚动条 */
}

/* 头部：品牌渐变（高度与内边距沿用原值） */
.workbench-header {
  background: var(--pd-grad-brand);
  color: #E8EEF8;
  padding: 20px 30px;
  flex-shrink: 0; /* 固定头部 */
}

.workbench-header h2 {
  margin: 0 0 8px 0;
  display: flex;
  align-items: center;
  gap: 10px;
}

.brand-mark { display: block; height: 24px; width: auto; }

.workbench-crumb {
  display: inline-block;
  margin-bottom: 10px;
  font-size: 12.5px;
  color: rgba(232, 238, 248, 0.72);
  cursor: pointer;
  transition: color var(--pd-dur-fast) var(--pd-ease);
}
.workbench-crumb:hover { color: #E8EEF8; }

.workbench-header p {
  margin: 0;
  font-size: 14px;
  color: rgba(232, 238, 248, 0.78);
}

/* 👇 整个内容区域的滚动容器 */
.workbench-scrollbar {
  flex: 1;
  overflow: hidden;
}

.workbench-content {
  padding: 20px 30px;
}

.mt-20 {
  margin-top: 20px;
}

/* 概览卡片 */
.overview-card {
  margin-bottom: 20px;
  border-radius: var(--pd-r-lg);
}

.overview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  font-weight: 500;
  color: var(--pd-text);
}

.stat-box {
  background: var(--pd-surface-muted);
  border: 1px solid var(--pd-border);
  padding: 15px;
  border-radius: var(--pd-r-md);
  text-align: center;
}

.stat-box .num {
  font-size: 24px;
  font-weight: bold;
  color: var(--pd-primary-text);
  font-family: var(--pd-font-mono);
}

.stat-box .label {
  font-size: 12px;
  color: var(--pd-text-3);
  margin-top: 5px;
}

/* 工具列表 */
.tools-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding-right: 10px;
}

.tool-item {
  border: 1px solid var(--pd-border);
  padding: 10px 15px;
  border-radius: var(--pd-r-md);
  background: var(--pd-surface);
  transition: border-color var(--pd-dur-fast) var(--pd-ease), background var(--pd-dur-fast) var(--pd-ease);
}

.tool-item:hover {
  border-color: var(--pd-primary-hover);
  background: var(--pd-surface-muted);
}

.tool-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}

.tool-name {
  font-weight: 600;
  color: var(--pd-text);
}

.tool-desc {
  font-size: 12px;
  color: var(--pd-text-2);
  line-height: 1.5;
}

/* 审批操作 */
.approval-actions {
  display: flex;
  gap: 10px;
}

/* 执行与代码展示区 */
.execute-actions {
  display: flex;
  justify-content: flex-end;
}

.result-viewer {
  margin-top: 20px;
  background: var(--pd-input);
  border: 1px solid var(--pd-border);
  border-radius: var(--pd-r-md);
  padding: 15px;
}

.result-viewer h4 {
  color: var(--pd-text-3);
  margin: 0 0 10px 0;
  font-size: 13px;
}

.json-code {
  margin: 0;
  color: var(--pd-text-2);
  font-family: var(--pd-font-mono);
  white-space: pre-wrap;
  word-break: break-all;
  font-size: 13px;
  line-height: 1.6;
}

/* 卡片样式 */
.box-card {
  border-radius: var(--pd-r-lg);
}

.card-title {
  font-weight: 600;
  color: var(--pd-text);
}
</style>