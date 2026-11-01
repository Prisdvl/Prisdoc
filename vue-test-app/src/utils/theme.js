/**
 * @description 主题（亮 / 暗 / 跟随系统）
 *
 * 与 Prisma 的做法一致：只改 `documentElement` 上的 `data-theme`，
 * 所有颜色由 `styles/prisdoc-tokens.css` 里的 `--pd-*` 变量切换，
 * 组件本身不感知主题。`html.dark` 类保留给 Element Plus 的暗色变量表。
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

const resolve = (setting) => {
  if (setting === 'system') return media && media.matches ? 'dark' : 'light'
  return setting === 'light' ? 'light' : 'dark'
}

function apply() {
  const resolved = resolve(themeSetting.value)
  resolvedTheme.value = resolved
  const root = document.documentElement
  root.dataset.theme = resolved
  root.dataset.themeSetting = themeSetting.value
  root.classList.toggle('dark', resolved === 'dark')
}

apply()

if (media) {
  media.addEventListener('change', () => {
    if (themeSetting.value === 'system') apply()
  })
}

export function setTheme(setting) {
  if (!THEME_OPTIONS.some((option) => option.value === setting)) return
  themeSetting.value = setting
  try {
    localStorage.setItem(STORAGE_KEY, setting)
  } catch {
    /* 存不下就算了，本次会话内仍然生效 */
  }
  apply()
}

export function toggleTheme() {
  setTheme(resolvedTheme.value === 'dark' ? 'light' : 'dark')
}

export function useTheme() {
  return {
    setting: themeSetting,
    resolved: resolvedTheme,
    isDark: computed(() => resolvedTheme.value === 'dark'),
    setTheme,
    toggleTheme
  }
}
