<template>
    <div class="register-container">
      <el-card class="register-card">
        <template #header>
          <h2>注册</h2>
        </template>
        <el-form :model="registerForm" :rules="rules" ref="registerForm">
          <el-form-item prop="username">
            <el-input v-model="registerForm.username" placeholder="用户名"></el-input>
          </el-form-item>
          <el-form-item prop="password">
            <el-input 
              v-model="registerForm.password" 
              type="password" 
              placeholder="密码"
            ></el-input>
          </el-form-item>
          <el-form-item prop="confirmPassword">
            <el-input 
              v-model="registerForm.confirmPassword" 
              type="password" 
              placeholder="确认密码"
            ></el-input>
          </el-form-item>
          <el-form-item prop="email">
            <el-input v-model="registerForm.email" placeholder="邮箱"></el-input>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleRegister" style="width: 100%;background-color: #FFB6C1;">注册</el-button>
          </el-form-item>
          <el-form-item>
            <el-link type="primary" @click="goToLogin" style="color: #FFB6C1;">已有账号？立即登录</el-link>
          </el-form-item>
        </el-form>
      </el-card>
    </div>
  </template>
  
  <script>
  import userApi from '../api/user'
  import { ElMessage } from 'element-plus'
  
  export default {
    name: 'UserRegister',
    data() {
      // 密码确认验证
      const validatePass2 = (rule, value, callback) => {
        if (value === '') {
          callback(new Error('请再次输入密码'))
        } else if (value !== this.registerForm.password) {
          callback(new Error('两次输入密码不一致!'))
        } else {
          callback()
        }
      }
      return {
        registerForm: {
          username: '',
          password: '',
          confirmPassword: '',
          email: ''
        },
        rules: {
          username: [
            { required: true, message: '请输入用户名', trigger: 'blur' },
            { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
          ],
          password: [
            { required: true, message: '请输入密码', trigger: 'blur' },
            { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
          ],
          confirmPassword: [
            { required: true, validator: validatePass2, trigger: 'blur' }
          ],
          email: [
            { required: true, message: '请输入邮箱地址', trigger: 'blur' },
            { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
          ]
        }
      }
    },
    methods: {
      handleRegister() {
        this.$refs.registerForm.validate(async (valid) => {
          if (valid) {
            try {
              const { username, password, email } = this.registerForm
              await userApi.register({ username, password, email })
              ElMessage.success('注册成功')
              const audio = new Audio('/assets/sounds/ciallo.mp3');
              audio.play();
              this.$router.push('/login')
            } catch (error) {
              ElMessage.error(error.response?.data?.error || '注册失败')
            }
          }
        })
      },
      goToLogin() {
        this.$router.push('/login')
      }
    }
  }
  </script>
  
  <style scoped>
  .register-container {
    height: 100vh;
    display: flex;
    justify-content: center;
    align-items: center;
  }
  
  .register-card {
    width: 400px;
  }
  </style>