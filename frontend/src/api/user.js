import request from './config'
import axios from 'axios';

const api = {
  login: (data) => {
    return request.post('/users/login', data)
  },
  resetPassword: async (data) => {
    return await axios.post('/api/users/reset-password', data);
  },
  register: (data) => {
    return request.post('/users/register', data)
  }
}

// 验证用户名和邮箱
export function verifyUserEmail(data) {
  return request({
    url: '/users/verify-email',
    method: 'post',
    data
  })
}

// 重置密码
export function resetPassword(data) {
  return request({
    url: '/users/reset-password',
    method: 'post',
    data
  })
}

export default api