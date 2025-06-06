<template>
  <div class="file-list">
    <div class="operation-bar">
      <el-button type="primary" @click="handleUpload" style="background-color: #FFB6C1;">上传文件</el-button>
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
            console.log('文件列表数据:', response)
            this.fileList = response
            console.log('更新后的fileList:', this.fileList)
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
      input.onchange = async (e) => {
        const file = e.target.files[0]

        if (file) {
          const maxSize = 1000 * 1024 * 1024
          if (file.size > maxSize) {
            ElMessage.error(`文件大小超过限制（1000MB），请选择更小的文件`)
            e.target.value = '' // 清空选择
            return
          }
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
        }
      }
      input.click()
    },

    async handleDownload(file) {
        let progressMessage = null;
        try {
            // 设置块大小为5MB
            const CHUNK_SIZE = 5 * 1024 * 1024;
            let downloadedBytes = parseInt(localStorage.getItem(`download_${file.fileId}`)) || 0;
            //const downloadProgress = new Map();

            // 获取文件大小
            const headResponse = await axios({
                url: `/api/files/download/${file.fileId}`,
                method: 'HEAD',
                headers: {
                    'Authorization': `Bearer ${localStorage.getItem('token')}`
                }
            });

            const totalSize = parseInt(headResponse.headers['content-length']);
            const chunks = [];
            let completed = false;

            // 创建进度提示
            progressMessage = ElMessage({
                message: '开始下载...',
                duration: 0,
                type: 'info'
            });

            while (!completed) {
                const endByte = Math.min(downloadedBytes + CHUNK_SIZE - 1, totalSize - 1);
                
                try {
                    const response = await axios({
                        url: `/api/files/download/${file.fileId}`,
                        method: 'GET',
                        responseType: 'blob',
                        headers: {
                            'Authorization': `Bearer ${localStorage.getItem('token')}`,
                            'Range': `bytes=${downloadedBytes}-${endByte}`
                        },
                        onDownloadProgress: (progressEvent) => {
                            // 计算当前块的进度
                            //const currentChunkSize = endByte - downloadedBytes + 1;
                            const currentChunkProgress = progressEvent.loaded;
                            
                            // 计算总体进度：已完成的块 + 当前块的进度
                            const totalProgress = downloadedBytes + currentChunkProgress;
                            const percentCompleted = Math.round((totalProgress * 100) / totalSize);
                            
                            // 更新进度提示
                            progressMessage.message = `下载进度: ${percentCompleted}%`;
                        }
                    });

                    // 保存当前块
                    chunks.push(response.data);
                    downloadedBytes = endByte + 1;
                    localStorage.setItem(`download_${file.fileId}`, downloadedBytes);

                    // 检查是否下载完成
                    if (downloadedBytes >= totalSize) {
                        completed = true;
                    }
                } catch (error) {
                    console.error('块下载失败:', error);
                    // 保存断点位置，允许稍后继续
                    localStorage.setItem(`download_${file.fileId}`, downloadedBytes);
                    throw error;
                }
            }

            // 合并所有块
            const blob = new Blob(chunks, {
                type: headResponse.headers['content-type'] || 'application/octet-stream'
            });

            // 获取文件名
            let fileName = file.fileName;
            const contentDisposition = headResponse.headers['content-disposition'];
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

            // 下载完成后清除进度记录和关闭进度消息
            localStorage.removeItem(`download_${file.fileId}`);
            progressMessage.close();
            ElMessage.success('文件下载成功');
        } catch (error) {
            console.error('下载失败:', error);
            if (progressMessage) {  // 检查 progressMessage 是否存在
                progressMessage.close();
            }
            ElMessage.error('文件下载失败');
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
