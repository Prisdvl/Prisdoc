<template>
  <div class="collaborate-panel" v-if="visible">
    <div class="panel-header" @click="collapsed = !collapsed">
      <div class="header-left">
        <el-icon><Connection /></el-icon>
        <span class="header-title">协作编辑</span>
        <el-tag :type="statusTagType" size="small" effect="dark" round>
          {{ statusText }}
        </el-tag>
      </div>
      <div class="header-right">
        <el-button
          v-if="!isCollaborating"
          type="primary"
          size="small"
          round
          :loading="isConnecting"
          @click.stop="handleStart"
        >
          加入协作
        </el-button>
        <el-button
          v-else
          type="danger"
          size="small"
          round
          plain
          @click.stop="handleStop"
        >
          退出协作
        </el-button>
        <el-icon class="collapse-icon" :class="{ rotated: collapsed }"><ArrowDown /></el-icon>
      </div>
    </div>

    <div class="panel-body" v-show="!collapsed">
      <div class="sync-info" v-if="isCollaborating">
        <div class="info-row">
          <span>{{ userName }}（我）</span>
        </div>
        <div class="info-row">
          <span>上次同步：{{ lastSyncText }}</span>
        </div>
        <div class="info-row">
          <span>每 2 秒自动拉取远程更新</span>
        </div>
        <div class="info-row">
          <span>输入后 1.5 秒自动保存</span>
        </div>
      </div>
      <div class="no-collab" v-else-if="isConnected">
        <span>点击"加入协作"开始实时同步</span>
      </div>
      <div class="share-box">
        <div class="share-title">授权协作者</div>
        <div class="share-row">
          <el-input
            v-model="collaboratorQuery"
            size="small"
            clearable
            placeholder="输入用户名或用户ID"
            @keyup.enter="handleGrant"
          />
          <el-select v-model="collaboratorRole" size="small" style="width: 96px;">
            <el-option label="可编辑" value="editor" />
            <el-option label="只读" value="viewer" />
          </el-select>
          <el-button type="primary" size="small" :loading="granting" @click="handleGrant">
            授权
          </el-button>
        </div>
        <div class="share-hint">授权后，对方会在自己的文档列表中看到这份文档；文档仍保存在创建者的存储桶中。</div>
      </div>
      <div class="connection-error" v-if="connectionError">
        <el-icon><WarningFilled /></el-icon>
        <span>{{ connectionError }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { docApi } from '../api/document'
import { userApi } from '../api/user'
import { useCollaboration } from './useCollaboration'

const props = defineProps({
  documentId: { type: String, required: true },
  userId: { type: String, default: '1' },
  userName: { type: String, default: 'User' }
})

const visible = ref(true)
const collapsed = ref(false)
const isConnecting = ref(false)
const collaboratorQuery = ref('')
const collaboratorRole = ref('editor')
const granting = ref(false)

const {
  isConnected,
  isCollaborating,
  connectionError,
  lastSyncTime,
  startCollaboration,
  stopCollaboration
} = useCollaboration(props.documentId, props.userId, props.userName)

const statusTagType = computed(() => {
  if (!isConnected.value) return 'info'
  if (isCollaborating.value) return 'success'
  return 'warning'
})

const statusText = computed(() => {
  if (isCollaborating.value) return '协作中'
  if (isConnected.value) return '已就绪'
  return '未连接'
})

const lastSyncText = computed(() => {
  if (!lastSyncTime.value) return '暂无'
  const diff = Math.floor((Date.now() - lastSyncTime.value) / 1000)
  if (diff < 5) return '刚刚'
  if (diff < 60) return `${diff} 秒前`
  return `${Math.floor(diff / 60)} 分钟前`
})

async function handleStart() {
  isConnecting.value = true
  try {
    await startCollaboration()
  } finally {
    isConnecting.value = false
  }
}

function handleStop() {
  stopCollaboration()
}

async function handleGrant() {
  const query = collaboratorQuery.value.trim()
  if (!query) {
    ElMessage.warning('请输入协作者用户名或用户ID')
    return
  }

  granting.value = true
  try {
    let collaboratorUserId = query
    if (!/^\d+$/.test(query)) {
      const userRes = await userApi.getUserByUsername(query)
      collaboratorUserId = userRes?.data?.id
    }

    if (!collaboratorUserId) {
      ElMessage.error('没有找到该用户')
      return
    }
    if (String(collaboratorUserId) === String(props.userId)) {
      ElMessage.warning('不能把文档授权给自己')
      return
    }

    await docApi.grantCollaborator(props.documentId, collaboratorUserId, collaboratorRole.value)
    ElMessage.success('协作者授权成功')
    collaboratorQuery.value = ''
  } catch (e) {
    ElMessage.error(e?.message || '协作者授权失败')
  } finally {
    granting.value = false
  }
}

onUnmounted(() => {
  stopCollaboration()
})
</script>

<style scoped>
/* 去掉 fixed 悬浮，改为 100% 宽度的内嵌卡片 */
.collaborate-panel {
  width: 100%;
  background: var(--pd-surface);
  border-radius: var(--pd-r-md);
  border: 1px solid var(--pd-border); /* 与侧边栏其它卡片同一套边界色 */
  overflow: hidden;
  transition: border-color var(--pd-dur-fast) var(--pd-ease);
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  row-gap: 8px;
  padding: 14px 16px;
  cursor: pointer;
  user-select: none;
  background: var(--pd-surface-muted);
  border-bottom: 1px solid var(--pd-border);
}

.panel-header:hover {
  background: var(--pd-hover);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1 1 auto;
  min-width: 0;
}

.header-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--pd-text);
  white-space: nowrap;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 0 0 auto;
  margin-left: auto;
}

.collapse-icon {
  transition: transform var(--pd-dur) var(--pd-ease), color var(--pd-dur-fast) var(--pd-ease);
  color: var(--pd-text-3);
}

.collapse-icon.rotated {
  transform: rotate(180deg);
}

.panel-body {
  padding: 12px 16px;
  background: var(--pd-surface);
}

.sync-info {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.info-row {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: var(--pd-text-2);
}

.info-row .el-icon {
  color: var(--pd-primary-text);
  font-size: 14px;
}

.no-collab {
  text-align: center;
  color: var(--pd-text-3);
  font-size: 13px;
  padding: 12px 0;
}

.connection-error {
  display: flex;
  align-items: center;
  gap: 6px;
  color: var(--pd-danger);
  font-size: 12px;
  padding: 8px 0;
  background: var(--pd-danger-weak);
  border-radius: var(--pd-r-sm);
  margin-top: 8px;
}

.share-box {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px dashed var(--pd-border);
}

.share-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--pd-text);
  margin-bottom: 8px;
}

.share-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}
/* 侧栏很窄：授权输入框独占一行，选择器与按钮落在第二行，避免文字被截断 */
.share-row .el-input {
  flex: 1 1 100%;
}

.share-hint {
  margin-top: 8px;
  font-size: 12px;
  line-height: 1.5;
  color: var(--pd-text-3);
}
</style>
