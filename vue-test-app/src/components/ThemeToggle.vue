<template>
  <div class="theme-toggle" :class="[`size-${size}`]" role="group" aria-label="主题">
    <button
        v-for="option in THEME_OPTIONS"
        :key="option.value"
        type="button"
        class="theme-option"
        :class="{ active: setting === option.value }"
        :aria-pressed="setting === option.value"
        @click="setTheme(option.value)"
    >{{ option.label }}</button>
  </div>
</template>

<script setup>

/**
 * 主题切换按钮，支持 md / sm 两种尺寸。
 */
import { THEME_OPTIONS, setTheme, useTheme } from '../utils/theme'

defineProps({
  size: { type: String, default: 'md' } // md | sm
})

const { setting } = useTheme()
</script>

<style scoped>
/* 三段式胶囊：选中态用 --pd-surface + 品牌色，未选中是纯文字 */
.theme-toggle {
  display: inline-flex;
  align-items: center;
  gap: 2px;
  padding: 2px;
  border: 1px solid var(--pd-border);
  border-radius: var(--pd-r-pill);
  background: var(--pd-surface-muted);
}

.theme-option {
  border: 0;
  background: transparent;
  border-radius: var(--pd-r-pill);
  color: var(--pd-text-3);
  font-family: inherit;
  font-weight: 500;
  cursor: pointer;
  transition: background var(--pd-dur-fast) var(--pd-ease), color var(--pd-dur-fast) var(--pd-ease);
}

.theme-option:hover { color: var(--pd-text-2); }

.theme-option.active {
  background: var(--pd-surface);
  color: var(--pd-text);
  box-shadow: var(--pd-shadow-sm);
}

.size-md .theme-option { padding: 4px 12px; font-size: 12.5px; }
.size-sm .theme-option { padding: 2px 9px; font-size: 11.5px; }
</style>
