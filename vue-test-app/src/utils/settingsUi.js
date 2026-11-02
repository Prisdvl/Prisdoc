/**
 * 设置对话框的开关状态。
 *
 * 状态放在模块作用域，全局唯一，任意页面都能唤起同一个设置弹窗。
 */
import { ref } from 'vue'

/** 设置对话框是否打开 */
export const settingsOpen = ref(false)

/**
 * 打开设置对话框。
 */
export const openSettings = () => {
  settingsOpen.value = true
}

/**
 * 关闭设置对话框。
 */
export const closeSettings = () => {
  settingsOpen.value = false
}
