<template>
  <div class="login-container">
    <el-card class="login-card">
      <template #header>
        <h2>登录</h2>
      </template>
      <el-form :model="loginForm" :rules="rules" ref="loginForm">
        <el-form-item prop="username">
          <el-input v-model="loginForm.username" placeholder="用户名"></el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="loginForm.password" type="password" placeholder="密码"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleLogin" style="width: 100%; background-color: #FFB6C1;">登录</el-button>
        </el-form-item>
        <el-form-item>
          <div class="form-footer"> 
            <el-link type="primary" @click="goToResetPassword" style="color: #FFB6C1;">忘记密码？</el-link>
            <el-link type="primary" @click="goToRegister" style="color: #FFB6C1;">还没有账号？立即注册</el-link>
          </div>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import userApi from '../api/user'
import { ElMessage } from 'element-plus'

export default {
  name: 'UserLogin',
  data() {
    return {
      loginForm: {
        username: '',
        password: ''
      },
      rules: {
        username: [
          { required: true, message: '请输入用户名', trigger: 'blur' }
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' }
        ]
      }
    }
  },
  methods: {
    handleLogin() {
      this.$refs.loginForm.validate(async (valid) => {
        if (valid) {
          try {
            const response = await userApi.login(this.loginForm)
            if (response.token) {
              await this.$store.dispatch('login', {
                token: response.token,
                user: {
                  username: this.loginForm.username
                }
              })
              const audio = new Audio('/assets/sounds/ciallo.mp3');
              audio.play();
              ElMessage.success('登录成功')
              this.$router.push('/home')
            } else {
              throw new Error('登录响应数据不完整')
            }
          } catch (error) {
            console.error('登录错误:', error)
            // 根据错误类型显示不同的错误信息
            if (error.response) {
              switch (error.response.status) {
                case 401:
                  ElMessage.error('用户名或密码错误')
                  break
                case 404:
                  ElMessage.error('服务器接口不存在')
                  break
                case 500:
                  // 检查是否是因为接口不存在导致的500错误
                  if (error.response.data?.error?.includes('not found') || 
                      error.response.data?.path?.includes('/api/users/login')) {
                    ElMessage.error('服务器接口不存在')
                  } else {
                    ElMessage.error('服务器内部错误，请稍后重试')
                  }
                  break
                case 503:
                  ElMessage.error('服务器暂时不可用')
                  break
                default:
                  ElMessage.error(error.response.data?.message || '登录失败，请稍后重试')
              }
            } else if (error.request) {
              ElMessage.error('无法连接到服务器，请检查网络连接')
            } else {
              ElMessage.error('登录过程中发生错误：' + error.message)
            }
          }
        }
      })
    },
    goToRegister() {
      this.$router.push('/register')
    },
    goToResetPassword() {
      this.$router.push('/reset-password')
    }
  }
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
}

.login-card {
  width: 400px;
}

.form-footer {
  display: flex;
  justify-content: space-between !important; /* 添加!important确保覆盖默认样式 */
  align-items: center;
  margin-top: 10px;
  padding: 0 10px;
  width: 100%; /* 确保容器占满整个宽度 */
}

</style>