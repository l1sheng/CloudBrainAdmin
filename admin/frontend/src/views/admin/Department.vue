<template>
  <div class="department-page">
    <el-card shadow="never" class="department-card">
      <template #header>
        <div class="page-header">
          <span>科室管理</span>
          <div class="header-actions">
            <el-button type="success" :icon="Refresh" @click="refreshAll">刷新</el-button>
          </div>
        </div>
      </template>

      <el-row :gutter="20" class="department-body">
        <!-- 左栏：科室树形列表 -->
        <el-col :span="6" class="tree-column">
          <div class="tree-panel">
            <div class="tree-title">科室目录</div>
            <el-tree
              ref="treeRef"
              :data="treeData"
              node-key="id"
              :default-expand-all="true"
              highlight-current
              @node-click="handleTreeNodeClick"
            >
              <template #default="{ data }">
                <span class="tree-node">
                  <el-icon><Folder /></el-icon>
                  <span class="tree-node-label">{{ data.name }}</span>
                </span>
              </template>
            </el-tree>
          </div>
        </el-col>

        <!-- 右栏：操作 + 详情 / 列表 -->
        <el-col :span="18">
          <div class="right-panel">
            <div class="action-bar">
              <el-button type="primary" :icon="Plus" @click="openCreateDialog">
                新增科室
              </el-button>
              <el-input
                v-model="keyword"
                placeholder="搜索科室名称"
                clearable
                :prefix-icon="Search"
                style="width: 240px"
                @input="loadList"
              />
              <el-button
                v-if="selectedId"
                type="primary"
                plain
                @click="switchToList"
              >
                返回列表
              </el-button>
            </div>

            <!-- 详情视图 -->
            <el-card
              v-if="selectedId && currentDetail"
              shadow="never"
              class="detail-card"
            >
              <div class="detail-header">
                <div>
                  <div class="detail-title">{{ currentDetail.name }}</div>
                  <div class="detail-sub">
                    科室编号：{{ currentDetail.code }}
                  </div>
                </div>
                <div class="detail-actions">
                  <el-button :icon="Edit" @click="openEditDialog(currentDetail)">
                    编辑
                  </el-button>
                  <el-switch
                    :model-value="currentDetail.status === 1"
                    active-text="启用"
                    inactive-text="停用"
                    @change="() => handleToggleStatus(currentDetail)"
                  />
                  <el-button
                    type="danger"
                    :icon="Delete"
                    @click="handleDelete(currentDetail)"
                  >
                    删除
                  </el-button>
                </div>
              </div>

              <el-descriptions :column="2" border class="detail-desc">
                <el-descriptions-item label="科室类型">
                  {{ currentDetail.departmentType || '-' }}
                </el-descriptions-item>
                <el-descriptions-item label="状态">
                  <el-tag
                    :type="currentDetail.status === 1 ? 'success' : 'info'"
                    effect="plain"
                  >
                    {{ currentDetail.status === 1 ? '启用' : '停用' }}
                  </el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="楼层">
                  {{ currentDetail.floor || '-' }}
                </el-descriptions-item>
                <el-descriptions-item label="联系电话">
                  {{ currentDetail.phone || '-' }}
                </el-descriptions-item>
                <el-descriptions-item label="排序号">
                  {{ currentDetail.sortOrder }}
                </el-descriptions-item>
                <el-descriptions-item label="上级科室">
                  {{ parentName(currentDetail.parentId) || '无' }}
                </el-descriptions-item>
                <el-descriptions-item label="简介" :span="2">
                  {{ currentDetail.description || '-' }}
                </el-descriptions-item>
              </el-descriptions>
            </el-card>

            <!-- 列表视图 -->
            <el-table
              v-else
              :data="tableData"
              stripe
              border
              style="width: 100%"
              empty-text="暂无科室数据"
            >
              <el-table-column prop="name" label="科室名称" min-width="160" />
              <el-table-column prop="floor" label="楼层" min-width="100" />
              <el-table-column prop="phone" label="联系电话" min-width="140" />
              <el-table-column
                prop="departmentType"
                label="类型"
                min-width="100"
              />
              <el-table-column label="状态" width="120" align="center">
                <template #default="{ row }">
                  <el-switch
                    :model-value="row.status === 1"
                    active-text="启用"
                    inactive-text="停用"
                    inline-prompt
                    @change="() => handleToggleStatus(row)"
                  />
                </template>
              </el-table-column>
              <el-table-column label="操作" width="220" fixed="right">
                <template #default="{ row }">
                  <el-button type="primary" link @click="viewDetail(row)">
                    查看
                  </el-button>
                  <el-button type="primary" link @click="openEditDialog(row)">
                    编辑
                  </el-button>
                  <el-button type="danger" link @click="handleDelete(row)">
                    删除
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <!-- 新增/编辑 弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogMode === 'create' ? '新增科室' : '编辑科室'"
      width="560px"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="90px"
      >
        <el-form-item label="科室名称" prop="name">
          <el-input v-model="formData.name" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item label="上级科室" prop="parentId">
          <el-tree-select
            v-model="formData.parentId"
            :data="selectTreeData"
            node-key="id"
            check-strictly
            render-after-expand
            placeholder="不选则为顶级科室"
            clearable
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="科室类型" prop="departmentType">
          <el-select v-model="formData.departmentType" placeholder="请选择">
            <el-option label="门诊" value="门诊" />
            <el-option label="住院" value="住院" />
            <el-option label="急诊" value="急诊" />
            <el-option label="医技" value="医技" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="楼层" prop="floor">
          <el-input v-model="formData.floor" placeholder="例如：门诊三楼" />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="formData.phone" placeholder="例如：010-12345678" />
        </el-form-item>
        <el-form-item label="排序号" prop="sortOrder">
          <el-input-number v-model="formData.sortOrder" :min="0" :max="9999" />
        </el-form-item>
        <el-form-item label="简介" prop="description">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="3"
            maxlength="255"
            show-word-limit
          />
        </el-form-item>
        <el-form-item v-if="dialogMode === 'edit'" label="状态" prop="status">
          <el-switch
            v-model="formData.status"
            :active-value="1"
            :inactive-value="0"
            active-text="启用"
            inactive-text="停用"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitForm">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Delete,
  Edit,
  Folder,
  Plus,
  Refresh,
  Search
} from '@element-plus/icons-vue'
import {
  createDepartment,
  deleteDepartment,
  getDepartmentDetail,
  getDepartmentTree,
  listDepartments,
  toggleDepartmentStatus,
  updateDepartment
} from '@/api/department'

// ---------- 数据 ----------
const treeData = ref([])
const tableData = ref([])
const currentDetail = ref(null)
const selectedId = ref(null)
const keyword = ref('')

// ---------- 弹窗 ----------
const dialogVisible = ref(false)
const dialogMode = ref('create')
const submitLoading = ref(false)
const formRef = ref(null)
const formData = reactive({
  name: '',
  parentId: null,
  departmentType: '门诊',
  floor: '',
  phone: '',
  sortOrder: 0,
  description: '',
  status: 1
})

const formRules = {
  name: [{ required: true, message: '请输入科室名称', trigger: 'blur' }]
}

// ---------- 计算 ----------
const selectTreeData = computed(() => treeData.value)

const allDepartmentMap = computed(() => {
  const map = new Map()
  const walk = (nodes) => {
    if (!nodes) return
    for (const n of nodes) {
      map.set(n.id, n.name)
      walk(n.children)
    }
  }
  walk(treeData.value)
  return map
})

function parentName(parentId) {
  if (parentId == null) return ''
  return allDepartmentMap.value.get(parentId) || '-'
}

// ---------- 方法 ----------
async function loadTree() {
  try {
    const data = await getDepartmentTree()
    treeData.value = data
  } catch (e) {
    // request interceptor 已提示
  }
}

async function loadList() {
  try {
    const data = await listDepartments(keyword.value || undefined)
    tableData.value = data
  } catch (e) {
    // ignore
  }
}

function refreshAll() {
  loadTree()
  if (!selectedId.value) {
    loadList()
  } else {
    loadDetail(selectedId.value)
  }
}

function handleTreeNodeClick(data) {
  selectedId.value = data.id
  loadDetail(data.id)
}

async function loadDetail(id) {
  try {
    const detail = await getDepartmentDetail(id)
    currentDetail.value = detail
  } catch (e) {
    currentDetail.value = null
    selectedId.value = null
  }
}

function viewDetail(row) {
  selectedId.value = row.id
  loadDetail(row.id)
}

function switchToList() {
  selectedId.value = null
  currentDetail.value = null
  loadList()
}

function openCreateDialog() {
  dialogMode.value = 'create'
  formData.name = ''
  formData.parentId = null
  formData.departmentType = '门诊'
  formData.floor = ''
  formData.phone = ''
  formData.sortOrder = 0
  formData.description = ''
  formData.status = 1
  dialogVisible.value = true
}

function openEditDialog(row) {
  dialogMode.value = 'edit'
  formData.name = row.name
  formData.parentId = row.parentId ?? null
  formData.departmentType = row.departmentType || '门诊'
  formData.floor = row.floor || ''
  formData.phone = row.phone || ''
  formData.sortOrder = row.sortOrder ?? 0
  formData.description = row.description || ''
  formData.status = row.status ?? 1
  dialogVisible.value = true
}

async function submitForm() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch (e) {
    return
  }

  submitLoading.value = true
  try {
    if (dialogMode.value === 'create') {
      await createDepartment({
        name: formData.name.trim(),
        parentId: formData.parentId ?? undefined,
        departmentType: formData.departmentType,
        floor: formData.floor || undefined,
        phone: formData.phone || undefined,
        sortOrder: formData.sortOrder,
        description: formData.description || undefined
      })
      ElMessage.success('新增科室成功')
    } else {
      await updateDepartment(currentDetail.value.id, {
        name: formData.name.trim(),
        parentId: formData.parentId ?? undefined,
        departmentType: formData.departmentType,
        floor: formData.floor,
        phone: formData.phone,
        sortOrder: formData.sortOrder,
        description: formData.description,
        status: formData.status
      })
      ElMessage.success('编辑科室成功')
    }
    dialogVisible.value = false
    refreshAll()
  } catch (e) {
    // 已在 interceptor 中提示
  } finally {
    submitLoading.value = false
  }
}

async function handleToggleStatus(row) {
  try {
    await toggleDepartmentStatus(row.id)
    ElMessage.success('状态已切换')
    refreshAll()
  } catch (e) {
    // ignore
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(
      `确认删除科室"${row.name}"？该操作不可恢复，且若科室下有关联医生或排班记录将无法删除。`,
      '删除科室',
      {
        confirmButtonText: '确认删除',
        cancelButtonText: '取消',
        type: 'warning',
        confirmButtonClass: 'el-button--danger'
      }
    )
  } catch (e) {
    return
  }
  try {
    await deleteDepartment(row.id)
    ElMessage.success('删除科室成功')
    if (selectedId.value === row.id) {
      selectedId.value = null
      currentDetail.value = null
    }
    refreshAll()
  } catch (e) {
    // ignore
  }
}

// ---------- 组件通信（预留，当前页面内部状态管理） ----------
const props = defineProps({
  initialDepartmentId: {
    type: Number,
    default: null
  }
})
const emit = defineEmits(['select'])

onMounted(() => {
  loadTree()
  loadList()
  if (props.initialDepartmentId) {
    selectedId.value = props.initialDepartmentId
    loadDetail(props.initialDepartmentId)
    emit('select', props.initialDepartmentId)
  }
})
</script>

<style scoped>
.department-page {
  width: 100%;
}

.department-card {
  border-radius: 8px;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.header-actions {
  display: flex;
  gap: 8px;
}

.department-body {
  display: flex;
}

.tree-column {
  display: flex;
}

.tree-panel {
  background: #fafafa;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 12px 8px;
  width: 100%;
  min-height: 520px;
}

.tree-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  padding: 0 8px 10px;
  border-bottom: 1px solid #ebeef5;
  margin-bottom: 8px;
}

.tree-node {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.tree-node-label {
  font-size: 14px;
}

.right-panel {
  display: flex;
  flex-direction: column;
  gap: 16px;
  width: 100%;
}

.action-bar {
  display: flex;
  align-items: center;
  gap: 12px;
}

.detail-card {
  border-radius: 8px;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding-bottom: 16px;
  border-bottom: 1px solid #ebeef5;
  margin-bottom: 16px;
}

.detail-title {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.detail-sub {
  font-size: 13px;
  color: #909399;
}

.detail-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.detail-desc {
  font-size: 14px;
}
</style>