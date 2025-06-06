<template>
  <div class="reset-password-container">
    <el-form :model="resetForm" :rules="rules" ref="resetForm" class="reset-form">
      <h2>重置密码</h2>
      <el-form-item prop="username">
        <el-input v-model="resetForm.username" placeholder="请输入用户名"></el-input>
      </el-form-item>
      <el-form-item prop="email">
        <el-input v-model="resetForm.email" placeholder="请输入注册邮箱"></el-input>
      </el-form-item>
      <el-form-item prop="newPassword">
        <el-input v-model="resetForm.newPassword" type="password" placeholder="请输入新密码"></el-input>
      </el-form-item>
      <el-form-item prop="confirmPassword">
        <el-input v-model="resetForm.confirmPassword" type="password" placeholder="请确认新密码"></el-input>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleReset" style="width: 100% ;background-color: #FFB6C1;" >重置密码</el-button>
      </el-form-item>
      <div class="form-footer">
        <router-link to="/login" class="back-to-login" style="color: #FFB6C1;">返回登录</router-link>
      </div>
    </el-form>
  </div>
</template>

<script>
import { ElMessage } from 'element-plus'
import userApi from '../api/user' 

export default {
  name: 'ResetPassword',
  data() {
    // 确认密码的验证规则
    const validateConfirmPassword = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('请再次输入密码'))
      } else if (value !== this.resetForm.newPassword) {
        callback(new Error('两次输入密码不一致'))
      } else {
        callback()
      }
    }
    return {
      resetForm: {
        username: '',
        email: '',
        newPassword: '',
        confirmPassword: ''
      },
      rules: {
        username: [
          { required: true, message: '请输入用户名', trigger: 'blur' }
        ],
        email: [
          { required: true, message: '请输入邮箱地址', trigger: 'blur' },
          { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
        ],
        newPassword: [
          { required: true, message: '请输入新密码', trigger: 'blur' },
          { min: 6, message: '密码长度不能小于6位', trigger: 'blur' }
        ],
        confirmPassword: [
          { required: true, message: '请再次输入密码', trigger: 'blur' },
          { validator: validateConfirmPassword, trigger: 'blur' }
        ]
      }
    }
  },
  methods: {
    async handleReset() {
      try {
        // 先进行表单验证
        await this.$refs.resetForm.validate()
        
        try {
          // 直接调用重置密码接口，让后端处理验证
          await userApi.resetPassword({ 
            username: this.resetForm.username,
            email: this.resetForm.email,
            newPassword: this.resetForm.newPassword
          })
          
          ElMessage.success('密码重置成功')
          this.$router.push('/login')
          
        } catch (apiError) {
          console.error('API调用错误:', apiError)
          if (apiError.response) {
            ElMessage.error(apiError.response.data.message || '重置密码失败')
          } else {
            ElMessage.error('网络请求失败，请检查网络连接')
          }
        }
      } catch (validationError) {
        console.error('表单验证错误:', validationError)
        ElMessage.error('请检查输入信息是否正确')
      }
    }
  }
}
</script>

<style scoped>
.reset-password-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
}

.reset-form {
  width: 400px;
  padding: 30px;
  background-color:#f5f7fa;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

h2 {
  text-align: center;
  margin-bottom: 30px;
  color: #303133;
}

.form-footer {
  text-align: center;
  margin-top: 20px;
}

.back-to-login {
  color: #909399;
  text-decoration: none;
  font-size: 14px;
}

.back-to-login:hover {
  color: #f5f7fa;
}
</style>