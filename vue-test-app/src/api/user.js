/**
 * 用户模块接口封装。
 */
import request from '../utils/request'

/**
 * 用户接口集合。
 */
export const userApi = {
    // 登录：成功后返回访问令牌与刷新令牌
    login: (data) => request.post('/users/login', data),
    // 注册：创建新用户并返回用户信息
    register: (data) => request.post('/users/register', data),

    // 按用户 ID 获取用户信息
    getUserInfo: (id) => {
        return request.get(`/users/${id}`)
    },

    // 按用户名查询用户，用于文档协作授权时查找协作者
    getUserByUsername: (username) => {
        return request.get('/users/lookup', {
            params: { username }
        })
    }
}
