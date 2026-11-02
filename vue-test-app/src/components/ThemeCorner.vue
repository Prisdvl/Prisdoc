<template>
  <div class="theme-corner">
    <el-popover placement="top-start" :width="260" trigger="click" popper-class="theme-corner-pop">
      <template #reference>
        <button class="theme-corner-btn" type="button" :title="`当前外观：${label}`">
          <span class="theme-corner-dot" :class="{ filled: resolved === 'dark' }"></span>
          {{ label }}
        </button>
      </template>

      <div class="theme-corner-body">
        <p class="theme-corner-title">外观</p>
        <ThemeToggle size="sm" />
        <p class="theme-corner-hint">登录后可在「设置 → 外观」里改。</p>
      </div>
    </el-popover>
  </div>
</template>

<script setup>
/**
 * 登录前的角落外观控件（参考 Prisma：主题不放在导航里，藏在左下角）。
 * 登录之后改主题的入口在「设置」对话框里，两者共用 ThemeToggle。
 */
import { computed } from 'vue'
import ThemeToggle from './ThemeToggle.vue'
import { useTheme } from '../utils/theme'

const { resolved } = useTheme()
const label = computed(() => (resolved.value === 'dark' ? '深色' : '浅色'))
</script>

<style scoped>
.theme-corner {
  position: fixed;
  left: 20px;
  bottom: 20px;
  z-index: 30;
}

.theme-corner-btn {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  height: 30px;
  padding: 0 12px;
  border: 1px solid var(--pd-border);
  border-radius: var(--pd-r-pill);
  background: var(--pd-surface);
  color: var(--pd-text-3);
  font-family: inherit;
  font-size: 12px;
  cursor: pointer;
  opacity: 0.72;
  transition: opacity var(--pd-dur-fast) var(--pd-ease), color var(--pd-dur-fast) var(--pd-ease),
    border-color var(--pd-dur-fast) var(--pd-ease);
}
.theme-corner-btn:hover {
  opacity: 1;
  color: var(--pd-text);
  border-color: var(--pd-border-strong);
}

/* 实心 = 深色，空心 = 浅色 */
.theme-corner-dot {
  width: 9px;
  height: 9px;
  border-radius: 50%;
  border: 1px solid var(--pd-text-3);
  background: transparent;
}
.theme-corner-dot.filled { background: var(--pd-text-2); border-color: var(--pd-text-2); }

.theme-corner-body { display: flex; flex-direction: column; gap: 10px; }
.theme-corner-title { margin: 0; font-size: 12.5px; font-weight: 600; color: var(--pd-text); }
.theme-corner-hint { margin: 0; font-size: 11.5px; line-height: 1.6; color: var(--pd-text-3); }
</style>
