/**
 * Axios 全局请求封装。
 *
 * 统一处理鉴权头注入、401 跳转登录与业务错误提示，
 * 并对 /ai/** 请求上报 AIOps 指标（/ai/aiops/** 自身除外，避免递归统计）。
 */
import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'
import { STORAGE_KEYS, RES_CODE } from '../constants'

const service = axios.create({
    baseURL: '/api',
    timeout: 300000
})

const AI_PATH_PREFIX = '/ai/'
const AIOPS_PATH_PREFIX = '/ai/aiops/'

/**
 * 读取本地保存的访问令牌。
 *
 * @returns {string|null} 访问令牌
 */
function getAccessToken() {
  return localStorage.getItem(STORAGE_KEYS.ACCESS_TOKEN)
}

/**
 * 构造鉴权请求头。
 *
 * @returns {Object} 含 Authorization 的请求头；未登录时返回空对象
 */
function buildAuthHeaders() {
  const token = getAccessToken()
  return token ? { Authorization: `Bearer ${token}` } : {}
}

/**
 * 判断是否为需要统计的 AI 请求。
 *
 * @param {string} url 请求地址
 * @returns {boolean} 是 AI 业务请求时返回 true
 */
function isAiRequest(url) {
  return url && url.startsWith(AI_PATH_PREFIX) && !url.startsWith(AIOPS_PATH_PREFIX)
}

/**
 * 上报 AI 请求的调用次数、耗时与错误数。
 *
 * 指标上报失败不影响主流程，因此统一吞掉异常。
 *
 * @param {Object} config 请求配置，需含 _startTime
 * @param {boolean} hasError 本次请求是否失败
 */
function reportAiMetrics(config, hasError) {
  const url = config.url || ''
  if (!isAiRequest(url)) return

  const startTime = config._startTime
  const duration = startTime ? Date.now() - startTime : 0

  const baseURL = config.baseURL || '/api'

  axios.post(baseURL + '/ai/aiops/metrics/counter', null, {
    headers: buildAuthHeaders(),
    params: { name: 'ai.requests', delta: 1 }
  }).catch(() => {})

  if (duration > 0) {
    axios.post(baseURL + '/ai/aiops/metrics/timer', null, {
      headers: buildAuthHeaders(),
      params: { name: 'ai.request', duration }
    }).catch(() => {})
  }

  if (hasError) {
    axios.post(baseURL + '/ai/aiops/metrics/counter', null, {
      headers: buildAuthHeaders(),
      params: { name: 'ai.errors', delta: 1 }
    }).catch(() => {})
  }
}

service.interceptors.request.use(
    config => {
        config._startTime = Date.now()
        const token = getAccessToken()
        if (token) {
            config.headers['Authorization'] = 'Bearer ' + token
        }
        return config
    },
    error => Promise.reject(error)
)

service.interceptors.response.use(
    response => {
        if (response.config.responseType === 'blob' || response.config.responseType === 'arraybuffer') {
            reportAiMetrics(response.config, false)
            return response.data
        }

        const res = response.data

        // 后端可能直接返回字符串（例如 HTML 源码），不属于统一 Result 结构，原样放行
        if (typeof res === 'string') {
            return res
        }

        // 兼容后端直接返回数组/对象而没有 code 的情况（如 getFileList）
        if (res && res.code === undefined) {
            reportAiMetrics(response.config, false)
            return { data: res, code: 200, message: 'success' }
        }

        if (res.code === 200 || res.code === 0) {
            reportAiMetrics(response.config, false)
            return res
        } else if (res.code === RES_CODE.UNAUTHORIZED || res.code === 604 || res.code === 605) {
            reportAiMetrics(response.config, true)
            ElMessage.error('登录已过期，请重新登录')
            localStorage.removeItem(STORAGE_KEYS.ACCESS_TOKEN)
            localStorage.removeItem(STORAGE_KEYS.REFRESH_TOKEN)
            localStorage.removeItem(STORAGE_KEYS.USER_INFO)
            router.push('/login')
            return Promise.reject(new Error(res.message || 'Unauthorized'))
        } else {
            reportAiMetrics(response.config, true)
            ElMessage.error(res.message || '操作失败')
            return Promise.reject(new Error(res.message || 'Error'))
        }
    },
    error => {
        if (error.config) {
            reportAiMetrics(error.config, true)
        }
        if (error.response && error.response.status === 401) {
            ElMessage.error('登录已过期，请重新登录')
            localStorage.removeItem(STORAGE_KEYS.ACCESS_TOKEN)
            localStorage.removeItem(STORAGE_KEYS.REFRESH_TOKEN)
            localStorage.removeItem(STORAGE_KEYS.USER_INFO)
            router.push('/login')
        } else {
            ElMessage.error('服务器开了小差，请稍后再试')
        }
        return Promise.reject(error)
    }
)

/**
 * 全局 axios 实例：业务代码统一通过它访问 /api。
 */
export default service
