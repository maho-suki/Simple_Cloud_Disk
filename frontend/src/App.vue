<template>
  <div id="app" :style="backgroundStyle">
    <router-view></router-view>
  </div>
</template>

<script>
export default {
  name: 'App',
  data() {
    return {
      backgroundImage: ''
    }
  },
  computed: {
    backgroundStyle() {
      return {
        backgroundImage: `url(${this.backgroundImage})`,  
        backgroundSize: 'cover',  
        backgroundPosition: 'center',  // 添加这行来居中背景
        backgroundRepeat: 'no-repeat',  // 添加这行防止背景重复
        minHeight: '100vh'  // 确保覆盖整个视口高度
      }
    }
  },
  created() {
    const imageCount = 11;
    const randomIndex = Math.floor(Math.random() * imageCount) + 1;
    // 可以使用随机的背景图片
    this.backgroundImage = `/assets/images/background${randomIndex}.jpg`;
  }
}
</script>

<style>
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  min-height: 100vh;
  color: #2c3e50;
  background-size: cover;
  background-position: center;
  background-attachment: fixed;
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px;
}

/* 背景遮罩层，用于调整透明度 */
#app::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 0;
}

/* 确保内容在遮罩层之上 */
#app > * {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 1200px; /* 限制最大宽度 */
}

/* 响应式布局调整 */
@media (max-width: 768px) {
  #app {
    padding: 10px;
  }
  
  #app > * {
    max-width: 100%;
  }
}
</style>
