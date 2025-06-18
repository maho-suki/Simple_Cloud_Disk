<template>
  <div class="home-container">
    <el-container>
      <el-header>
        <div class="header-content">
          <h2>maho网盘</h2>
          <div class="user-info">
            <span>欢迎，{{ currentUser?.username }}</span>
            <el-button type="text" class="logout-btn" @click="handleLogout">退出登录</el-button>
          </div>
        </div>
      </el-header>
      <el-main>
        <file-list></file-list>
      </el-main>
    </el-container>
  </div>
</template>

<script>
import { ElMessage } from 'element-plus'
import FileList from './FileList.vue'

export default {
  name: 'UserHome',
  components: {
    FileList
  },
  computed: {
    currentUser() {
      return this.$store.getters.currentUser
    }
  },
  methods: {
    async handleLogout() {
      try {
        await this.$store.dispatch('logout')
        ElMessage.success('退出登录成功')
        this.$router.push('/login')
      } catch (error) {
        ElMessage.error('退出登录失败')
      }
    }
  }
}
</script>

<style scoped>
.home-container {
  height: 100vh;
}

.el-header {
  background-color: #FFB6C1;
  color: white;
  padding: 0 20px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 100%;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 20px;
}

.el-main {
  padding: 20px;
  background-color: #f5f7fa;
}

.logout-btn {
  color: #ffffff !important;  
  font-weight: bold;         
  text-decoration: underline; 
  opacity: 0.9;              /* 轻微透明效果 */
}

.logout-btn:hover {
  opacity: 1;                
}
</style>