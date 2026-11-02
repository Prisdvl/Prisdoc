/**
 * @description 设置对话框的开关状态（全局唯一，任意页面都能打开）
 */
import { ref } from 'vue'

export const settingsOpen = ref(false)

export const openSettings = () => {
  settingsOpen.value = true
}

export const closeSettings = () => {
  settingsOpen.value = false
}
