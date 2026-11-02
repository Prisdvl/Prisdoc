<template>
  <div class="appearance">
    <div class="appearance-row">
      <span class="appearance-label">主题</span>
      <ThemeToggle />
    </div>
    <p class="appearance-hint">
      选「自动」跟随系统设置；A4 纸在两种主题下都保持浅色纸面，不影响正文与导出。
    </p>
  </div>
</template>

<script setup>
import ThemeToggle from './ThemeToggle.vue'
</script>

<style scoped>
/* 与 Prisma 的设置面板同构：左侧 40px 标签列 + 右侧控件 */
.appearance { display: flex; flex-direction: column; gap: 12px; }
.appearance-row { display: flex; align-items: center; gap: 12px; }
.appearance-label { flex: none; width: 40px; font-size: 12.5px; color: var(--pd-text-3); }
.appearance-hint {
  margin: 0;
  padding-left: 52px;
  font-size: 12.5px;
  line-height: 1.7;
  color: var(--pd-text-3);
}
</style>
