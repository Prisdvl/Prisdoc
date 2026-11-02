/**
 * 异步任务轮询工具。
 *
 * 后端把长耗时推理放进 RabbitMQ，接口只返回 jobId。调用方提交任务后，
 * 由本工具按固定间隔轮询状态，直到成功、失败或超时。
 */
const DEFAULT_POLL_INTERVAL_MS = 1200
const DEFAULT_TIMEOUT_MS = 180000

const delayMs = (ms) => new Promise(resolve => setTimeout(resolve, ms))

/**
 * 取出响应体中的业务数据（兼容 axios 包装与裸响应）。
 *
 * @param {any} response 接口响应
 * @returns {any} 业务数据
 */
function unwrapData(response) {
  return response?.data ?? response
}

/**
 * 从提交响应中取出 jobId，缺失时直接抛错。
 *
 * @param {any} response 提交任务的响应
 * @returns {string} 任务 ID
 * @throws {Error} 响应中不含 jobId
 */
function requireJobId(response) {
  const job = unwrapData(response)
  if (!job?.jobId) {
    throw new Error('异步任务提交失败：后端未返回 jobId')
  }
  return job.jobId
}

/**
 * 判断任务状态是否仍属于进行中。
 *
 * @param {string} status 任务状态
 * @returns {boolean} 是否进行中
 */
function isRunning(status) {
  return ['queued', 'running', 'processing', 'pending'].includes(String(status || '').toLowerCase())
}

/**
 * 创建一个异步任务执行器。
 *
 * @param {object} options 执行器配置
 * @param {Function} options.submit 提交任务，返回提交响应
 * @param {Function} options.getJob 按 jobId 查询任务状态
 * @param {Function} [options.delay] 等待函数，默认 setTimeout 实现
 * @param {number} [options.pollIntervalMs=1200] 轮询间隔（毫秒）
 * @param {number} [options.timeoutMs=180000] 整体超时（毫秒）
 * @returns {{ runAsyncJob: (...args: any[]) => Promise<any> }} 执行器实例
 */
export function createAsyncJobRunner({
  submit,
  getJob,
  delay = delayMs,
  pollIntervalMs = DEFAULT_POLL_INTERVAL_MS,
  timeoutMs = DEFAULT_TIMEOUT_MS
}) {
  return {
    /**
     * 提交任务并等待最终结果。
     *
     * @param {...any} args 透传给 submit 的参数
     * @returns {Promise<any>} 任务结果
     * @throws {Error} 任务失败、状态未知或轮询超时
     */
    async runAsyncJob(...args) {
      const jobId = requireJobId(await submit(...args))
      const startedAt = Date.now()

      while (Date.now() - startedAt <= timeoutMs) {
        const job = unwrapData(await getJob(jobId))
        const status = String(job?.status || '').toLowerCase()

        if (status === 'success') {
          return job.result
        }

        if (status === 'failed' || status === 'error') {
          throw new Error(job?.error || '异步任务执行失败')
        }

        if (!isRunning(status)) {
          throw new Error(`未知异步任务状态：${job?.status || 'empty'}`)
        }

        await delay(pollIntervalMs)
      }

      throw new Error('异步任务查询超时')
    }
  }
}
