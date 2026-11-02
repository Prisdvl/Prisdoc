/**
 * 全局 AI 助手状态。
 *
 * 状态放在模块作用域，多个页面共享同一个面板与同一段对话历史。
 */
import { reactive, ref } from 'vue'

/** 面板是否展开 */
const isAiPanelVisible = ref(false)
/** 当前对话历史 */
const chatHistory = ref([])
/** 是否正在等待 AI 回复 */
const isAiThinking = ref(false)
/** 当前会话 ID：从 localStorage 恢复，刷新页面后对话不丢失 */
const currentConvId = ref(localStorage.getItem('global_conv_id') || null)

/**
 * 组合式函数：向组件暴露全局 AI 面板的状态与操作。
 *
 * @returns {Object} 面板状态与操作方法
 */
export function useGlobalAi() {
    // 打开 / 关闭 AI 面板
    const toggleAiPanel = () => {
        isAiPanelVisible.value = !isAiPanelVisible.value
    }

    // 重置对话：清空消息与本地保存的会话 ID
    const resetConversation = () => {
        chatHistory.value = []
        currentConvId.value = null
        localStorage.removeItem('global_conv_id')
    }

    // 保存会话 ID，供刷新后恢复上下文
    const saveConvId = (convId) => {
        currentConvId.value = convId
        localStorage.setItem('global_conv_id', convId)
    }

    // 追加一条消息到对话历史
    const appendMessage = (msg) => {
        chatHistory.value.push(msg)
    }

    // 清空消息，保留当前会话 ID
    const clearChat = () => {
        chatHistory.value = []
    }

    return {
        isAiPanelVisible,
        chatHistory,
        currentConvId,
        isAiThinking,
        toggleAiPanel,
        appendMessage,
        clearChat,
        resetConversation,
        saveConvId

    }
}