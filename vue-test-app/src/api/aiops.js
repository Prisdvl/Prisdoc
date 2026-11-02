/**
 * AIOps 运维接口封装。
 *
 * 指标读写与故障检测均直连 ai-service 的 /api/ai/aiops/** 端点。
 */
import request from '../utils/request'

/**
 * AIOps 接口集合。
 */
export const aiopsApi = {
  // 读取当前指标快照
  getMetrics: () => request.get('/ai/aiops/monitor'),

  // 健康检查
  healthCheck: () => request.get('/ai/aiops/health'),

  // 重置全部指标
  resetMetrics: () => request.post('/ai/aiops/metrics/reset'),

  // 触发一次故障检测
  detectFaults: () => request.get('/ai/aiops/detect'),

  // 获取全部故障记录
  getAllFaults: () => request.get('/ai/aiops/faults'),

  // 将指定故障标记为已处理
  resolveFault: (faultId) => request.post(`/ai/aiops/faults/${faultId}/resolve`),

  // 计数器自增
  incrementCounter: (name, delta = 1) =>
    request.post('/ai/aiops/metrics/counter', null, { params: { name, delta } }),

  // 记录一次耗时打点（单位毫秒）
  recordTimer: (name, duration) =>
    request.post('/ai/aiops/metrics/timer', null, { params: { name, duration } })
}