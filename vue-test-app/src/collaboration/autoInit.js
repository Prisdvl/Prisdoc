/**
 * 协同编辑面板的自动挂载。
 *
 * 进入 `/editor/:id` 时把协同面板挂到 body，离开编辑器或进入对话模式时卸载。
 * 采用轮询监听路由变化，避免与路由守卫的执行顺序耦合。
 */
import { createApp, h } from 'vue'
import CollaboratePanel from './CollaboratePanel.vue'

let panelApp = null
let panelContainer = null

/**
 * 从当前地址解析编辑器上下文。
 *
 * @returns {{docId: string, userId: string, userName: string}|null} 编辑器信息；非编辑器页面返回 null
 */
function getEditorInfo() {
  const path = window.location.pathname
  const match = path.match(/^\/editor\/(.+)$/)
  if (!match) return null

  const docId = match[1]
  // 对话模式不需要协同面板
  if (docId === 'chat-mode') return null

  const userId = localStorage.getItem('userId') || '1'
  const userName = localStorage.getItem('userName') || 'User'

  return { docId, userId, userName }
}

/**
 * 按用户 ID 拉取用户名，用于面板上的协作者标识。
 *
 * @param {string} userId 用户 ID
 * @returns {Promise<string|null>} 用户名；获取失败返回 null
 */
async function fetchUserName(userId) {
  try {
    const token = localStorage.getItem('accessToken') || ''
    const headers = token ? { Authorization: `Bearer ${token}` } : {}
    const res = await fetch(`/api/users/${userId}`, { headers })
    const data = await res.json()
    if (data.code === 200 && data.data && data.data.username) {
      localStorage.setItem('userName', data.data.username)
      return data.data.username
    }
  } catch (e) {
    console.warn('[Collaboration] 获取用户名失败', e)
  }
  return null
}

/**
 * 创建协同面板并挂载到 body。
 *
 * @param {{docId: string, userId: string, userName: string}} info 编辑器上下文
 */
async function injectPanel(info) {
  if (panelContainer) return

  let userName = info.userName
  if (userName === 'User' && info.userId !== '1') {
    const fetched = await fetchUserName(info.userId)
    if (fetched) userName = fetched
  }

  panelContainer = document.createElement('div')
  panelContainer.id = '__collaborate_panel__'
  document.body.appendChild(panelContainer)

  panelApp = createApp({
    render() {
      return h(CollaboratePanel, {
        documentId: info.docId,
        userId: info.userId,
        userName: userName
      })
    }
  })
  panelApp.mount(panelContainer)
}

/**
 * 卸载并移除协同面板。
 */
function removePanel() {
  if (panelApp) {
    panelApp.unmount()
    panelApp = null
  }
  if (panelContainer) {
    panelContainer.remove()
    panelContainer = null
  }
}

/**
 * 根据当前路由决定挂载或卸载面板。
 */
function checkAndInject() {
  const info = getEditorInfo()
  if (info) {
    injectPanel(info)
  } else {
    removePanel()
  }
}

let lastPath = window.location.pathname
setInterval(() => {
  if (window.location.pathname !== lastPath) {
    lastPath = window.location.pathname
    setTimeout(checkAndInject, 300)
  }
}, 500)

document.addEventListener('DOMContentLoaded', () => {
  setTimeout(checkAndInject, 500)
})

if (document.readyState === 'interactive' || document.readyState === 'complete') {
  setTimeout(checkAndInject, 500)
}

/**
 * 手动初始化协同面板（模块被导入时也会自动初始化）。
 */
export function initCollaboration() {
  checkAndInject()
}
