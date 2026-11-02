/**
 * 协同编辑模块出口。
 *
 * 对外只暴露组合式函数、面板组件与初始化方法。
 */
export { useCollaboration } from './useCollaboration'
export { default as CollaboratePanel } from './CollaboratePanel.vue'
export { initCollaboration } from './autoInit'