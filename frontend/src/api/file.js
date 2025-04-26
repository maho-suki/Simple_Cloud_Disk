import request from './config'

const api = {
  upload: (formData) => {
    return request.post('/files/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },
  download: (fileId) => {
    return request.get(`/files/download/${fileId}`, {
      responseType: 'blob'
    })
  },
  delete: (fileId) => {
    return request.delete(`/files/delete/${fileId}`)
  },
  list: () => {
    return request.get('/files/list')
  }
}

export default api