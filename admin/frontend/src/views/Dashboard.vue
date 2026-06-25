<template>
  <div class="dashboard">
    <el-card shadow="never" class="welcome-card">
      <div class="welcome">
        <h2>欢迎回来，{{ userStore.realName || userStore.username }}</h2>
        <p>当前身份：{{ userStore.roleName || '管理员' }}</p>
      </div>
    </el-card>

    <el-row :gutter="16" class="stats">
      <el-col :xs="12" :sm="8" :md="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-title">登录账号</div>
          <div class="stat-value">{{ userStore.username || '-' }}</div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="8" :md="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-title">角色编码</div>
          <div class="stat-value">{{ userStore.roleCode || '-' }}</div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="8" :md="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-title">登录状态</div>
          <div class="stat-value" style="color: #67c23a">已登录</div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="8" :md="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-title">登录日期</div>
          <div class="stat-value">{{ today }}</div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

const today = computed(() => {
  const d = new Date()
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
})
</script>

<style scoped>
.dashboard {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.welcome-card {
  border-radius: 8px;
}

.welcome h2 {
  margin: 0 0 6px;
  color: #1f2d3d;
}

.welcome p {
  margin: 0;
  color: #606266;
}

.stats .stat-card {
  border-radius: 8px;
  margin-bottom: 16px;
}

.stat-title {
  color: #909399;
  font-size: 13px;
}

.stat-value {
  margin-top: 8px;
  font-size: 20px;
  font-weight: 600;
  color: #1f2d3d;
  word-break: break-all;
}
</style>