/**
 * JWT 解析工具。
 *
 * 仅解码 payload 用于前端读取角色，不做签名校验；权限判断必须由后端完成。
 */

/**
 * 解码 JWT 的 payload。
 *
 * @param {string} token 令牌字符串
 * @returns {Object|null} payload 对象；格式非法时返回 null
 */
export function decodeJwt(token) {
  try {
    const parts = token.split('.')
    if (parts.length !== 3) return null
    const payload = parts[1]
    const decoded = atob(payload.replace(/-/g, '+').replace(/_/g, '/'))
    return JSON.parse(decoded)
  } catch {
    return null
  }
}

/**
 * 读取当前登录用户的角色。
 *
 * @returns {string|null} 角色标识，未登录或解析失败时返回 null
 */
export function getUserRole() {
  const token = localStorage.getItem('accessToken')
  if (!token) return null
  const payload = decodeJwt(token)
  return payload ? payload.role : null
}

/**
 * 判断当前用户是否为管理员。
 *
 * @returns {boolean} 角色为 ADMIN 时返回 true
 */
export function isAdmin() {
  return getUserRole() === 'ADMIN'
}