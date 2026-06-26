<template>
  <div class="dashboard">
    <el-card shadow="never" class="welcome-card">
      <div class="welcome">
        <h2>欢迎回来，{{ userStore.realName || userStore.username }} 👋</h2>
        <p>当前身份：{{ userStore.roleName || '系统管理员' }} · {{ welcomeTip }}</p>
      </div>
    </el-card>

    <el-row :gutter="16" class="stats">
      <el-col :xs="12" :sm="8" :md="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-label">
            <el-icon><UserFilled /></el-icon>
            <span>医生总数</span>
          </div>
          <div class="stat-value">{{ stats.doctorCount || 0 }}</div>
          <div class="stat-sub">在岗医生 · 单位：人</div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="8" :md="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-label">
            <el-icon><OfficeBuilding /></el-icon>
            <span>科室总数</span>
          </div>
          <div class="stat-value">{{ stats.departmentCount || 0 }}</div>
          <div class="stat-sub">已登记的科室</div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="8" :md="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-label">
            <el-icon><Calendar /></el-icon>
            <span>今日排班</span>
          </div>
          <div class="stat-value">{{ stats.todayScheduleCount || 0 }}</div>
          <div class="stat-sub">今日当值的医生排班数</div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="8" :md="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-label">
            <el-icon><DataAnalysis /></el-icon>
            <span>本月排班总数</span>
          </div>
          <div class="stat-value">{{ stats.monthScheduleCount || 0 }}</div>
          <div class="stat-sub">本自然月内的所有排班</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="quick-ops">
      <el-col :span="24">
        <el-card shadow="never">
          <div class="ops-title">快捷操作</div>
          <div class="ops-list">
            <el-button type="primary" :icon="Calendar" @click="go('/schedule')">查看排班</el-button>
            <el-button type="success" :icon="UserFilled" @click="go('/doctor')">医生管理</el-button>
            <el-button :icon="OfficeBuilding" @click="go('/department')">科室管理</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { Calendar, UserFilled, OfficeBuilding, DataAnalysis } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
// 注意：医生/科室用 doctor/department 模块的接口（支持分页），排班上用 schedule 模块的接口（返回数组）
import { listDoctors } from '@/api/doctor'
import { listDepartments } from '@/api/department'
import { listSchedules } from '@/api/schedule'

const router = useRouter()
const userStore = useUserStore()

const stats = reactive({
  doctorCount: 0,
  departmentCount: 0,
  todayScheduleCount: 0,
  monthScheduleCount: 0
})

const welcomeTip = computed(() => {
  const h = new Date().getHours()
  if (h < 6) return '夜深了，注意休息'
  if (h < 11) return '早上好，开启新的一天'
  if (h < 13) return '中午好'
  if (h < 18) return '下午好'
  return '晚上好'
})

function fmtDate(d) {
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

function extractNumber(res, fallbackKey) {
  // 接口返回可能是 { list, total } 对象，也可能是纯数组，也可能是数字
  if (res === null || res === undefined) return 0
  if (typeof res === 'number') return res
  if (Array.isArray(res)) return res.length
  if (typeof res === 'object') {
    if (typeof res.total === 'number') return res.total
    if (typeof res[fallbackKey] === 'number') return res[fallbackKey]
    if (Array.isArray(res.list)) return res.list.length
    if (Array.isArray(res.data?.list)) return res.data.list.length
  }
  return 0
}

async function loadStats() {
  const today = fmtDate(new Date())
  const now = new Date()
  const monthStart = fmtDate(new Date(now.getFullYear(), now.getMonth(), 1))
  const monthEnd = fmtDate(new Date(now.getFullYear(), now.getMonth() + 1, 0))

  // 医生总数 —— /admin/doctor/list 返回 { list, total }
  try {
    const res = await listDoctors({ pageNum: 1, pageSize: 1 })
    stats.doctorCount = extractNumber(res, 'total')
  } catch { /* ignore */ }

  // 科室总数 —— /admin/department 可能返回数组或 { list }
  try {
    const res = await listDepartments()
    stats.departmentCount = extractNumber(res, 'total')
  } catch { /* ignore */ }

  // 今日排班 —— /admin/schedules 直接返回数组（见 Schedule.vue loadList）
  try {
    const arr = await listSchedules({ startDate: today, endDate: today })
    const list = Array.isArray(arr) ? arr : (arr?.list || [])
    stats.todayScheduleCount = list.length
  } catch { /* ignore */ }

  // 本月排班
  try {
    const arr = await listSchedules({ startDate: monthStart, endDate: monthEnd })
    const list = Array.isArray(arr) ? arr : (arr?.list || [])
    stats.monthScheduleCount = list.length
  } catch { /* ignore */ }
}

function go(path) { router.push(path) }

onMounted(loadStats)
</script>

<style scoped>
.dashboard {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.welcome-card {
  border-radius: 8px;
  background: linear-gradient(135deg, #e3f2fd 0%, #ffffff 100%);
}

.welcome h2 {
  margin: 0 0 6px;
  color: #1f2d3d;
  font-size: 18px;
}

.welcome p {
  margin: 0;
  color: #606266;
  font-size: 13px;
}

.stats .stat-card {
  border-radius: 8px;
  margin-bottom: 16px;
}

.stat-label {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #909399;
  font-size: 13px;
}

.stat-value {
  margin-top: 8px;
  font-size: 28px;
  font-weight: 600;
  color: #1f2d3d;
}

.stat-sub {
  margin-top: 4px;
  font-size: 12px;
  color: #c0c4cc;
}

.quick-ops .ops-title {
  font-size: 14px;
  color: #303133;
  font-weight: 600;
  margin-bottom: 12px;
}

.ops-list {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}
</style>