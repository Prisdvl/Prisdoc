<template>
  <el-dialog v-model="settingsOpen" title="设置" width="460px" align-center>
    <section class="settings-section">
      <h3 class="settings-title">外观</h3>
      <p class="settings-desc">主题只影响界面配色，不改文档正文。</p>
      <AppearanceControls />
    </section>

    <template #footer>
      <el-button @click="closeSettings">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import AppearanceControls from './AppearanceControls.vue'
import { settingsOpen, closeSettings } from '../utils/settingsUi'
</script>

<style scoped>
.settings-section { display: flex; flex-direction: column; gap: 6px; }
.settings-title { margin: 0; font-size: 14px; font-weight: 600; color: var(--pd-text); }
.settings-desc { margin: 0 0 8px; font-size: 12.5px; color: var(--pd-text-3); }
</style>
