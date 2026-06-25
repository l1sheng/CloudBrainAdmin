<template>
  <div class="doctor-page">
    <el-card shadow="never" class="doctor-card">
      <template #header>
        <div class="page-header">
          <span>医生账号管理</span>
          <div class="header-actions">
            <el-button type="primary" :icon="Plus" @click="openCreateDialog">新增医生</el-button>
            <el-button type="success" :icon="Refresh" @click="loadList">刷新</el-button>
            <el-button :icon="Download" @click="exportList">导出</el-button>
          </div>
        </div>
      </template>

      <el-form :inline="true" :model="queryForm" class="query-bar" @submit.prevent>
        <el-form-item label="科室">
          <el-select v-model="queryForm.departmentId" placeholder="请选择科室" clearable filterable style="width: 180px">
            <el-option v-for="dept in departments" :key="dept.id" :label="dept.name" :value="dept.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="姓名 / 工号">
          <el-input v-model="queryForm.keyword" placeholder="姓名 / 工号 / 专长" clearable style="width: 220px" @keyup.enter="onSearch" />
        </el-form-item>
        <el-form-item label="职称">
          <el-select v-model="queryForm.title" placeholder="请选择" clearable style="width: 130px">
            <el-option v-for="t in titleOptions" :key="t" :label="t" :value="t" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="请选择" clearable style="width: 120px">
            <el-option label="启用" :value="1" />
            <el-option label="停用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="onSearch">搜索</el-button>
          <el-button :icon="RefreshLeft" @click="onReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" stripe border style="width: 100%" empty-text="暂无医生数据" v-loading="loading">
        <el-table-column prop="doctorNo" label="工号" width="110" />
        <el-table-column prop="doctorName" label="姓名" width="100" />
        <el-table-column label="科室" width="140">
          <template #default="{ row }">
            <el-tag size="small" effect="plain">{{ row.departmentName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="职称" width="100" />
        <el-table-column prop="doctorType" label="医生类型" width="90" />
        <el-table-column prop="phone" label="手机" width="130" />
        <el-table-column prop="email" label="邮箱" show-overflow-tooltip min-width="170" />
        <el-table-column prop="specialty" label="专长" show-overflow-tooltip min-width="150" />
        <el-table-column prop="hireDate" label="入职日期" width="110" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-switch :model-value="row.status === 1" active-color="#13ce66" inactive-color="#ff4949" @change="handleToggle(row)" />
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="140">
          <template #default="{ row }">
            <el-button link type="primary" @click="viewDetail(row)">详情</el-button>
            <el-button link type="primary" @click="openEditDialog(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-bar">
        <el-pagination
          v-model:current-page="queryForm.pageNum"
          v-model:page-size="queryForm.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadList"
          @current-change="loadList"
        />
      </div>
    </el-card>

    <!-- 新增 / 编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogMode === 'create' ? '新增医生' : '编辑医生'" width="640px" :close-on-click-modal="false" destroy-on-close>
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="90px" class="doctor-form">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="姓名" prop="name">
              <el-input v-model="formData.name" placeholder="请输入姓名" maxlength="50" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="工号" prop="doctorNo">
              <el-input v-model="formData.doctorNo" placeholder="请输入工号" maxlength="50" :readonly="dialogMode === 'edit'" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="科室" prop="departmentId">
              <el-select v-model="formData.departmentId" placeholder="请选择科室" filterable style="width: 100%">
                <el-option v-for="dept in departments" :key="dept.id" :label="dept.name" :value="dept.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="手机" prop="phone">
              <el-input v-model="formData.phone" placeholder="1 开头的 11 位手机号" maxlength="20" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="formData.email" placeholder="可选" maxlength="100" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="职称" prop="title">
              <el-select v-model="formData.title" placeholder="请选择职称" filterable allow-create style="width: 100%">
                <el-option v-for="t in titleOptions" :key="t" :label="t" :value="t" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="医生类型">
              <el-select v-model="formData.doctorType" placeholder="请选择" filterable allow-create style="width: 100%">
                <el-option label="主治" value="主治" />
                <el-option label="副主治" value="副主治" />
                <el-option label="住院" value="住院" />
                <el-option label="实习" value="实习" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="入职日期">
              <el-date-picker v-model="formData.hireDate" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="专长">
              <el-input v-model="formData.specialty" placeholder="请输入专长" type="textarea" :rows="2" maxlength="255" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="医生详情" width="560px" destroy-on-close>
      <el-descriptions :column="2" border v-if="currentDetail">
        <el-descriptions-item label="姓名">{{ currentDetail.doctorName }}</el-descriptions-item>
        <el-descriptions-item label="工号">{{ currentDetail.doctorNo }}</el-descriptions-item>
        <el-descriptions-item label="科室">{{ currentDetail.departmentName }}</el-descriptions-item>
        <el-descriptions-item label="职称">{{ currentDetail.title || '-' }}</el-descriptions-item>
        <el-descriptions-item label="医生类型">{{ currentDetail.doctorType || '-' }}</el-descriptions-item>
        <el-descriptions-item label="手机">{{ currentDetail.phone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ currentDetail.email || '-' }}</el-descriptions-item>
        <el-descriptions-item label="入职日期">{{ currentDetail.hireDate || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="currentDetail.status === 1 ? 'success' : 'info'" size="small">
            {{ currentDetail.status === 1 ? '启用' : '停用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="专长" :span="2">{{ currentDetail.specialty || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh, Download, Search, RefreshLeft } from '@element-plus/icons-vue'
import { listDoctors, createDoctor, updateDoctor, toggleDoctorStatus, checkDoctorDisable, exportDoctors, listDepartments } from '@/api/doctor'

const loading = ref(false)
const submitting = ref(false)
const total = ref(0)
const tableData = ref([])
const departments = ref([])
const titleOptions = ['主任医师', '副主任医师', '主治医师', '住院医师', '护士']

const queryForm = reactive({ departmentId: undefined, keyword: '', title: '', status: undefined, pageNum: 1, pageSize: 10 })

const dialogVisible = ref(false)
const dialogMode = ref('create')
const formRef = ref(null)
const formData = reactive({
  doctorId: undefined, name: '', doctorNo: '', phone: '', email: '',
  departmentId: undefined, title: '', doctorType: '主治', specialty: '', hireDate: ''
})

const formRules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  doctorNo: [{ required: true, message: '请输入工号', trigger: 'blur' }],
  departmentId: [{ required: true, message: '请选择科室', trigger: 'change' }],
  phone: [{ validator: (_r, v, cb) => (!v || /^1[3-9]\d{9}$/.test(v)) ? cb() : cb(new Error('手机号格式不正确')), trigger: 'blur' }],
  email: [{ type: 'email', message: '邮箱格式不正确', trigger: 'blur' }],
  title: [{ required: true, message: '请选择 / 输入职称', trigger: 'change' }]
}

const detailVisible = ref(false)
const currentDetail = ref(null)

onMounted(async () => {
  try { departments.value = await listDepartments() } catch { departments.value = [] }
  await loadList()
})

async function loadList() {
  loading.value = true
  try {
    const result = await listDoctors({
      departmentId: queryForm.departmentId || undefined,
      keyword: queryForm.keyword || undefined,
      title: queryForm.title || undefined,
      status: queryForm.status,
      pageNum: queryForm.pageNum,
      pageSize: queryForm.pageSize
    })
    tableData.value = result?.list || []
    total.value = result?.total || 0
  } catch {
    tableData.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function onSearch() { queryForm.pageNum = 1; loadList() }

function onReset() {
  Object.assign(queryForm, { departmentId: undefined, keyword: '', title: '', status: undefined, pageNum: 1 })
  loadList()
}

function resetFormData() {
  Object.assign(formData, {
    doctorId: undefined, name: '', doctorNo: '', phone: '', email: '',
    departmentId: undefined, title: '', doctorType: '主治', specialty: '', hireDate: ''
  })
}

function openCreateDialog() { dialogMode.value = 'create'; resetFormData(); dialogVisible.value = true }

function openEditDialog(row) {
  dialogMode.value = 'edit'
  resetFormData()
  Object.assign(formData, {
    doctorId: row.doctorId, name: row.doctorName || '', doctorNo: row.doctorNo || '',
    phone: row.phone || '', email: row.email || '',
    departmentId: row.departmentId, title: row.title || '', doctorType: row.doctorType || '主治',
    specialty: row.specialty || '', hireDate: row.hireDate || ''
  })
  dialogVisible.value = true
}

async function submitForm() {
  if (!formRef.value) return
  try { await formRef.value.validate() } catch { return }

  submitting.value = true
  try {
    const payload = {
      name: formData.name?.trim(), doctorNo: formData.doctorNo?.trim(),
      phone: formData.phone?.trim() || null, email: formData.email?.trim() || null,
      departmentId: formData.departmentId, title: formData.title,
      doctorType: formData.doctorType || '主治', specialty: formData.specialty?.trim() || null,
      hireDate: formData.hireDate || null
    }

    if (dialogMode.value === 'create') {
      await createDoctor(payload)
      ElMessage.success('医生创建成功，默认密码 123456')
    } else {
      const update = { ...payload }
      delete update.doctorNo
      await updateDoctor(formData.doctorId, update)
      ElMessage.success('医生信息已更新')
    }
    dialogVisible.value = false
    loadList()
  } catch { /* request.js 已处理 */ } finally { submitting.value = false }
}

async function handleToggle(row) {
  if (row.status === 1) {
    try {
      const check = await checkDoctorDisable(row.doctorId)
      if (check && (check.pendingSchedules > 0 || check.pendingRegistrations > 0)) {
        await ElMessageBox.confirm(
          '该医生仍有 ' + check.pendingSchedules + ' 个排班和 ' + check.pendingRegistrations + ' 个挂号未完成，是否确认禁用？',
          '禁用确认', { confirmButtonText: '强制禁用', cancelButtonText: '取消', type: 'warning', confirmButtonClass: 'el-button--danger' }
        )
        await toggleDoctorStatus(row.doctorId, true)
        ElMessage.success('已禁用')
        loadList()
        return
      }
    } catch { /* 校验失败继续普通切换 */ }

    try {
      await ElMessageBox.confirm('确认禁用该医生账号？禁用后该医生将无法登录。', '禁用确认', {
        confirmButtonText: '确认禁用', cancelButtonText: '取消', type: 'warning', confirmButtonClass: 'el-button--danger'
      })
    } catch { return }
  }

  try {
    await toggleDoctorStatus(row.doctorId, false)
    ElMessage.success(row.status === 1 ? '已禁用' : '已启用')
    loadList()
  } catch { /* request.js 已处理 */ }
}

function viewDetail(row) { currentDetail.value = row; detailVisible.value = true }

async function exportList() {
  try {
    const list = await exportDoctors(queryForm.departmentId)
    if (!list || list.length === 0) { ElMessage.warning('当前筛选条件下没有可导出的数据'); return }

    const headers = ['工号', '姓名', '科室', '职称', '医生类型', '专长', '手机', '邮箱', '入职日期', '状态']
    const rows = list.map(d => [d.doctorNo, d.doctorName, d.departmentName, d.title || '', d.doctorType || '', d.specialty || '', d.phone || '', d.email || '', d.hireDate || '', d.status === 1 ? '启用' : '停用'])
    const csv = '\uFEFF' + [headers, ...rows].map(r => r.map(c => '"' + String(c).replace(/"/g, '""') + '"').join(',')).join('\n')
    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = 'doctors-' + new Date().toISOString().slice(0, 10) + '.csv'
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
    URL.revokeObjectURL(url)
    ElMessage.success('已导出 ' + list.length + ' 条数据')
  } catch { /* request.js 已处理 */ }
}
</script>

<style scoped>
.doctor-page { min-height: 100%; }
.doctor-card { background: #fff; }
.page-header { display: flex; justify-content: space-between; align-items: center; font-size: 16px; font-weight: 600; color: #303133; }
.header-actions { display: flex; gap: 8px; }
.query-bar { padding: 4px 8px 12px; }
.pagination-bar { display: flex; justify-content: flex-end; padding-top: 16px; }
.doctor-form { padding: 4px 8px; }
</style>