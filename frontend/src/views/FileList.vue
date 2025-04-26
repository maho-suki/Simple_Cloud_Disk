<template>
    <div class="file-list">
      <div class="operation-bar">
        <el-button type="primary" @click="handleUpload">上传文件</el-button>
      </div>
      <el-table :data="fileList" style="width: 100%">
        <el-table-column prop="fileName" label="文件名" />
        <el-table-column prop="fileSize" label="大小" width="180">
          <template #default="scope">
            {{ formatFileSize(scope.row.fileSize) }}
          </template>
        </el-table-column>
        <el-table-column prop="uploadTime" label="修改时间" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.uploadTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button type="text" @click="handleDownload(scope.row)">下载</el-button>
            <el-button type="text" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </template>
  
  <script>
  import fileApi from '../api/file'
  import { ElMessage, ElMessageBox } from 'element-plus'
  import axios from 'axios'
  
  export default {
    name: 'FileList',
    data() {
      return {
        fileList: []
      }
    },
    created() {
      // 组件创建时加载文件列表
      this.loadFileList()
    },
    methods: {
      // 加载文件列表
      async loadFileList() {
          try {
              const response = await fileApi.list()
              console.log('文件列表数据:', response)  // 添加这行来调试
              this.fileList = response
              console.log('更新后的fileList:', this.fileList)  // 添加这行来调试
          } catch (error) {
              ElMessage.error('获取文件列表失败：' + error.message)
          }
      },
      
      // 格式化文件大小
      formatFileSize(size) {
        if (size < 1024) return size + ' B'
        if (size < 1024 * 1024) return (size / 1024).toFixed(2) + ' KB'
        if (size < 1024 * 1024 * 1024) return (size / (1024 * 1024)).toFixed(2) + ' MB'
        return (size / (1024 * 1024 * 1024)).toFixed(2) + ' GB'
      },
      
      // 格式化日期
      formatDate(date) {
        return new Date(date).toLocaleString()
      },
      
      handleUpload() {
        const input = document.createElement('input')
        input.type = 'file'
        input.click()
        input.onchange = async (e) => {
          const file = e.target.files[0]

          if (file) {
            // 10MB = 10 * 1024 * 1024 bytes
            const maxSize = 10 * 1024 * 1024
            if (file.size > maxSize) {
              ElMessage.error(`文件大小超过限制（1000MB），请选择更小的文件`)
              e.target.value = '' // 清空选择
              return
            }
            this.selectedFile = file
            const formData = new FormData()
            formData.append('file', file)
            // 移除userId的传递，让后端从token中获取

            try {
              await fileApi.upload(formData)
              ElMessage.success('文件上传成功')
              this.loadFileList() // 刷新文件列表
            } catch (error) {
              if (error.response && error.response.status === 413) {
                ElMessage.error(`文件超过最大限制（1000MB），请选择更小的文件`)
              } else {
                ElMessage.error('文件上传失败：' + (error.response?.data?.message || '未知错误'))
              }
            }
              finally {
                e.target.value = ''; // 无论成功失败都清空输入
              }
          }
        }
      },
  
      async handleDownload(file) {  // 注意这里参数应该是 file 而不是 fileId
          try {
              const response = await axios({
                  url: `/api/files/download/${file.fileId}`,  // 不需要 process.env.VUE_APP_API_URL
                  method: 'GET',
                  responseType: 'blob',
                  headers: {
                      'Authorization': `Bearer ${localStorage.getItem('token')}`  // 从 localStorage 获取 token
                  }
              });
  
              // 创建 Blob 对象
              const blob = new Blob([response.data]);
              
              // 从响应头中获取文件名
              const contentDisposition = response.headers['content-disposition'];
              let fileName = 'downloaded_file';
              if (contentDisposition) {
                const matches = /filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/.exec(contentDisposition);
                if (matches != null && matches[1]) {
                  fileName = decodeURIComponent(matches[1].replace(/['"]/g, ''));
                }
              }
  
              // 创建下载链接并触发下载
              const url = window.URL.createObjectURL(blob);
              const link = document.createElement('a');
              link.href = url;
              link.setAttribute('download', fileName);
              document.body.appendChild(link);
              link.click();
              document.body.removeChild(link);
              window.URL.revokeObjectURL(url);
  
              ElMessage.success('文件下载成功');  // 使用 ElMessage 而不是 this.$message
          } catch (error) {
              console.error('下载失败:', error);
              ElMessage.error('文件下载失败');  // 使用 ElMessage 而不是 this.$message
          }
      },
  
      // 在删除成功后刷新文件列表
      async handleDelete(file) {
        try {
          await ElMessageBox.confirm('确定要删除这个文件吗？', '提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          })
          
          await fileApi.delete(file.fileId)
          ElMessage.success('文件删除成功')
          this.loadFileList() // 刷新文件列表
        } catch (error) {
          if (error !== 'cancel') {
            ElMessage.error('文件删除失败：' + error.message)
          }
        }
      }
    }
  }
  </script>
  
  <style scoped>
  .file-list {
    background-color: white;
    padding: 20px;
    border-radius: 4px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  }
  
  .operation-bar {
    margin-bottom: 20px;
  }
  </style>