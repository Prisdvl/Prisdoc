/**
 * 文档业务接口封装。
 */
import request from '../utils/request'

/**
 * 文档接口集合。
 */
export const docApi = {
    // 获取用户所有的文档列表
    getUserDocs: (userId) => {
        return request.get(`/documents/user/${userId}`)
    },

    // 获取单个文档详情（含 content 和 summary）
    getDocDetail: (id) => {
        return request.get(`/documents/${id}`)
    },

    // 全文搜索：按关键词检索文档
    searchDocs: (keyword) => {
        return request.get('/documents/search', {
            params: { keyword }
        })
    },

    // 创建文档，data 对应后端的 DocumentCreateDTO
    createDoc: (data) => {
        return request.post('/documents', data)
    },

    // 更新文档内容（保存编辑）
    updateDoc: (id, data) => {
        return request.put(`/documents/${id}`, data)
    },

    // 获取文档所有历史版本
    getVersions: (id) => {
        return request.get(`/documents/${id}/versions`)
    },

    // 恢复到指定版本
    restoreVersion: (id, versionNumber) => {
        return request.post(`/documents/${id}/restore/${versionNumber}`)
    },

    // 删除文档（后端为软删除）
    deleteDoc: (id) => {
        return request.delete(`/documents/${id}`)
    },

    // 授权协作者，role 取值 editor / viewer
    grantCollaborator: (id, collaboratorUserId, role = 'editor') => {
        return request.post(`/documents/${id}/collaborators/${collaboratorUserId}`, null, {
            params: { role }
        })
    }
}
