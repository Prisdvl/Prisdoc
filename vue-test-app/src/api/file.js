/**
 * 文件模块接口封装。
 *
 * 上传使用 multipart/form-data，下载与预览返回 Blob。
 */
import request from '../utils/request'

/**
 * 文件接口集合。
 */
export const fileApi = {
    /**
     * 获取文件列表（支持分页）。
     *
     * @param {object} params 查询参数：`{ page, size, sortBy, direction }`
     * @returns {Promise<Object>} 分页后的文件列表
     */
    getFileList: (params) => {
        return request.get('/files/list', { params })
    },

    /**
     * 按关键词搜索文件。
     *
     * @param {object} params 查询参数：`{ keyword, page, size }`
     * @returns {Promise<Object>} 匹配的文件列表
     */
    searchFiles: (params) => {
        return request.get('/files/search', { params })
    },

    /**
     * 获取文件详细元数据。
     *
     * @param {string} fileId 文件唯一标识
     * @returns {Promise<Object>} 文件元数据
     */
    getMetadata: (fileId) => {
        return request.get(`/files/metadata/${fileId}`)
    },


    /**
     * 单文件上传（FormData 格式）。
     *
     * 不监听上传进度：后端未稳定返回 content-length，进度条由上层的请求状态展示。
     *
     * @param {File} rawFile 原生文件对象（来自 input[type=file] 或 el-upload）
     * @returns {Promise<Object>} 上传结果，含文件 ID 与访问地址
     */
    upload: (rawFile) => {
        const formData = new FormData()
        formData.append('file', rawFile)

        return request.post('/files/upload', formData)
    },

    /**
     * 下载文件，返回二进制数据流。
     *
     * @param {string} fileId 文件 ID
     * @returns {Promise<Blob>} 文件内容
     */
    download: (fileId) => {
        return request.get(`/files/download/${fileId}`, {
            responseType: 'blob'
        })
    },

    /**
     * 预览文件（主要用于图片、PDF），返回二进制流。
     *
     * @param {string} fileId 文件 ID
     * @returns {Promise<Blob>} 预览内容
     */
    preview: (fileId) => {
        return request.get(`/files/preview/${fileId}`, {
            responseType: 'blob'
        })
    },

    /**
     * 删除文件。
     *
     * @param {string} fileId 文件 ID
     * @returns {Promise<Object>} 删除结果
     */
    delete: (fileId) => {
        return request.delete(`/files/${fileId}`)
    },

    /**
     * 重命名文件。
     *
     * @param {string} fileId 文件 ID
     * @param {string} newName 新文件名
     * @returns {Promise<Object>} 重命名结果
     */
    rename: (fileId, newName) => {
        // 后端用 @RequestParam 接收 newName，因此放在 params 中传递
        return request.put(`/files/${fileId}/rename`, null, {
            params: { newName }
        })
    }
}