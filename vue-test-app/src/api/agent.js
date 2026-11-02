/**
 * Agent 工作台接口封装。
 *
 * executeTask 走异步任务（提交 + 轮询），executeTaskSync 为同步调试入口。
 */
import request from '../utils/request'
import { createAsyncJobRunner } from './asyncJob.js'

const wrapResult = (data) => ({ code: 200, message: 'success', data })
const getAsyncJob = (jobId) => request.get(`/ai/async/jobs/${jobId}`)
const runAsyncAgentJob = (data) => createAsyncJobRunner({
    submit: () => request.post('/ai/async/agent/execute', data),
    getJob: getAsyncJob,
    timeoutMs: 300000
}).runAsyncJob().then(wrapResult)

/**
 * Agent 接口集合。
 */
export const agentApi = {
    // 获取工作台总览（包含工具列表、任务数等）
    getOverview: () => request.get('/ai/agent/workbench/overview'),

    // 获取工具 Schema 列表
    getTools: () => request.get('/ai/agent/tools'),

    // 执行 Agent 任务（核心交互，异步）
    executeTask: (data) => runAsyncAgentJob(data),

    // 同步执行 Agent 任务，仅用于联调与排障
    executeTaskSync: (data) => request.post('/ai/agent/execute', data),

    // 查询单个任务快照
    getTaskHistory: (traceId) => request.get(`/ai/agent/tasks/${traceId}`),

    // 审批危险操作：确认继续
    confirmApproval: (data) => request.post('/ai/agent/approvals/confirm', data),

    // 审批危险操作：取消
    cancelApproval: (token) => request.post(`/ai/agent/approvals/${token}/cancel`),

    // 列出任务历史
    listTasks: () => request.get('/ai/agent/tasks')
}
