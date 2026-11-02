/**
 * 主题管理（亮色 / 暗色 / 跟随系统）。
 *
 * 只改 `documentElement` 上的 `data-theme` 与 `html.dark` 类，
 * 所有颜色由 `styles/prisdoc-tokens.css` 里的 `--pd-*` 变量切换，组件不感知主题。
 */
import { ref, computed } from 'vue'

const STORAGE_KEY = 'prisdoc-theme'
export const THEME_OPTIONS = [
  { value: 'light', label: '浅色' },
  { value: 'dark', label: '深色' },
  { value: 'system', label: '自动' }
]

const media = typeof window !== 'undefined' && window.matchMedia
    ? window.matchMedia('(prefers-color-scheme: dark)')
    : null

const readSetting = () => {
  try {
    const value = localStorage.getItem(STORAGE_KEY)
    if (value === 'light' || value === 'dark' || value === 'system') return value
  } catch {
    /* 隐私模式下 localStorage 可能不可用，退回默认值 */
  }
  return 'dark'
}

/** 用户的选择（含 system） */
export const themeSetting = ref(readSetting())
/** 实际生效的主题：system 已解析成 light / dark */
export const resolvedTheme = ref('dark')

/**
 * 把「用户设置」解析成实际生效的主题。
 *
 * @param {string} setting 用户设置：light / dark / system
 * @returns {string} 实际主题：light / dark
 */
const resolve = (setting) => {
  if (setting === 'system') return media && media.matches ? 'dark' : 'light'
  return setting === 'light' ? 'light' : 'dark'
}

/**
 * 将当前主题应用到根元素。
 */
function apply() {
  const resolved = resolve(themeSetting.value)
  resolvedTheme.value = resolved
  const root = document.documentElement
  root.dataset.theme = resolved
  root.dataset.themeSetting = themeSetting.value
  root.classList.toggle('dark', resolved === 'dark')
}

// 模块加载即应用一次，避免首屏闪烁
apply()

if (media) {
  media.addEventListener('change', () => {
    if (themeSetting.value === 'system') apply()
  })
}

/**
 * 设置主题并持久化。
 *
 * @param {string} setting 主题取值：light / dark / system
 */
export function setTheme(setting) {
  if (!THEME_OPTIONS.some((option) => option.value === setting)) return
  themeSetting.value = setting
  try {
    localStorage.setItem(STORAGE_KEY, setting)
  } catch {
    /* 隐私模式下 localStorage 不可用，本次会话内仍然生效 */
  }
  apply()
}

/**
 * 在亮色与暗色之间切换。
 */
export function toggleTheme() {
  setTheme(resolvedTheme.value === 'dark' ? 'light' : 'dark')
}

/**
 * 组合式函数：向组件暴露主题状态与操作。
 *
 * @returns {{ setting: import('vue').Ref<string>, resolved: import('vue').Ref<string>, isDark: import('vue').ComputedRef<boolean>, setTheme: Function, toggleTheme: Function }} 主题上下文
 */
export function useTheme() {
  return {
    setting: themeSetting,
    resolved: resolvedTheme,
    isDark: computed(() => resolvedTheme.value === 'dark'),
    setTheme,
    toggleTheme
  }
}
