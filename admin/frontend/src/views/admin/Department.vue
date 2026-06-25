<template>
  <div class="department-page">
    <!-- 顶部：与排班管理一致的 el-card 风格 -->
    <el-card shadow="never" class="department-card">
      <template #header>
        <div class="page-header">
          <span>科室管理</span>
          <div class="header-actions">
            <el-button type="primary" :icon="Plus" @click="openCreateDialog">
              新增科室
            </el-button>
            <el-button :icon="Refresh" @click="refreshAll">刷新</el-button>
          </div>
        </div>
      </template>

      <!-- 主体：左栏目录 + 右栏内容 -->
      <div class="main-layout">
        <!-- 左栏：科室目录 -->
        <div class="sidebar">
          <div class="sidebar-head">
            <span class="sidebar-title">科室目录</span>
            <el-tag size="small" type="info" effect="plain">{{ totalTreeCount }} 个</el-tag>
          </div>

          <el-input
            v-model="treeKeyword"
            placeholder="搜索科室"
            clearable
            :prefix-icon="Search"
            size="small"
            class="tree-search"
          />

          <el-tree
            ref="treeRef"
            class="dept-tree"
            :data="filteredTreeData"
            node-key="id"
            :default-expand-all="true"
            :highlight-current="true"
            :expand-on-click-node="true"
            :show-icon="false"
            @node-click="handleTreeNodeClick"
          >
            <template #default="{ data, node }">
              <div class="dept-tree-node" :class="{ active: selectedId === data.id }">
                <div class="dept-tree-main">
                  <span
                    class="dept-tree-icon"
                    :class="{
                      'icon-root': node.level === 1 && (data.children || []).length > 0,
                      'icon-leaf': node.level === 1 && !(data.children || []).length,
                      'icon-sub': node.level > 1
                    }"
                  >
                    <el-icon v-if="(data.children || []).length > 0 && node.expanded"><CaretBottom /></el-icon>
                    <el-icon v-else-if="(data.children || []).length > 0"><CaretRight /></el-icon>
                    <el-icon v-else><Document /></el-icon>
                  </span>
                  <span class="dept-tree-name">{{ data.name }}</span>
                  <span v-if="data.code" class="dept-tree-code">{{ data.code }}</span>
                </div>
              </div>
            </template>
          </el-tree>
        </div>

        <!-- 右栏：列表/详情 -->
        <div class="content-area">
          <!-- 列表视图 -->
          <div v-if="!selectedId || !currentDetail" class="list-toolbar">
            <div class="list-toolbar-left">
              <el-input
                v-model="keyword"
                placeholder="搜索科室名称或编号"
                clearable
                :prefix-icon="Search"
                style="width: 260px"
                @input="loadList"
              />
            </div>
            <div class="list-toolbar-right">
              <el-dropdown @command="handleTypeFilter">
                <el-button>
                  类型筛选
                  <el-icon class="el-icon--right"><ArrowDown /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="">全部</el-dropdown-item>
                    <el-dropdown-item
                      v-for="t in departmentTypeOptions"
                      :key="t"
                      :command="t"
                    >
                      {{ t }}
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>

          <!-- 列表表格 -->
          <el-table
            v-if="!selectedId || !currentDetail"
            :data="filteredTableData"
            stripe
            style="width: 100%"
            empty-text="暂无科室数据"
            :header-cell-style="{ background: '#fafafa' }"
          >
            <el-table-column label="科室" min-width="240">
              <template #default="{ row }">
                <div class="table-dept-cell">
                  <div class="table-dept-icon" :class="row.departmentType === '住院' ? 'icon-purple' : 'icon-blue'">
                    <el-icon><OfficeBuilding /></el-icon>
                  </div>
                  <div>
                    <div class="table-dept-name">{{ row.name }}</div>
                    <div class="table-dept-code">{{ row.code || '未分配编号' }}</div>
                  </div>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="类型" width="80" align="center">
              <template #default="{ row }">
                <el-tag
                  size="small"
                  :type="row.departmentType === '住院' ? 'warning' : 'primary'"
                  effect="plain"
                >
                  {{ row.departmentType || '-' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="上级科室" width="120" show-overflow-tooltip>
              <template #default="{ row }">
                <span class="table-parent">{{ parentName(row.parentId) || '顶级科室' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="楼层" width="120" align="center">
              <template #default="{ row }">
                <span>{{ row.floor || '-' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="联系电话" width="130" show-overflow-tooltip>
              <template #default="{ row }">
                <span>{{ row.phone || '-' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="排序" width="70" align="center">
              <template #default="{ row }">
                <span class="table-sort-num">{{ row.sortOrder }}</span>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="70" align="center">
              <template #default="{ row }">
                <el-tag
                  size="small"
                  :type="row.status === 1 ? 'success' : 'info'"
                  effect="light"
                >
                  {{ row.status === 1 ? '启用' : '停用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="110" fixed="right" align="center">
              <template #default="{ row }">
                <el-tooltip content="查看" placement="top">
                  <el-button type="primary" link :icon="View" @click="viewDetail(row)" />
                </el-tooltip>
                <el-tooltip content="编辑" placement="top">
                  <el-button type="primary" link :icon="Edit" @click="openEditDialog(row)" />
                </el-tooltip>
                <el-tooltip content="删除" placement="top">
                  <el-button type="danger" link :icon="Delete" @click="handleDelete(row)" />
                </el-tooltip>
              </template>
            </el-table-column>
          </el-table>

          <!-- 详情视图 -->
          <div v-if="selectedId && currentDetail" class="detail-wrapper">
            <div class="detail-header">
              <div class="detail-head-left">
                <div class="detail-avatar">
                  <el-icon><OfficeBuilding /></el-icon>
                </div>
                <div>
                  <div class="detail-name">{{ currentDetail.name }}</div>
                  <div class="detail-meta">
                    <el-tag size="small" type="info" effect="plain">
                      {{ currentDetail.code || '未分配编号' }}
                    </el-tag>
                    <el-tag
                      size="small"
                      :type="currentDetail.status === 1 ? 'success' : 'info'"
                      effect="light"
                    >
                      {{ currentDetail.status === 1 ? '启用中' : '已停用' }}
                    </el-tag>
                    <span class="detail-meta-item" v-if="currentDetail.departmentType">
                      <el-icon><Menu /></el-icon>
                      {{ currentDetail.departmentType }}
                    </span>
                  </div>
                </div>
              </div>
              <div class="detail-head-right">
                <el-button plain :icon="Back" @click="switchToList">返回列表</el-button>
                <el-button type="primary" :icon="Edit" @click="openEditDialog(currentDetail)">编辑</el-button>
                <el-button type="danger" plain :icon="Delete" @click="handleDelete(currentDetail)">删除</el-button>
              </div>
            </div>

            <el-row :gutter="16" class="info-cards">
              <el-col :span="8">
                <div class="info-card">
                  <div class="info-card-title">
                    <el-icon><Location /></el-icon>
                    <span>位置信息</span>
                  </div>
                  <div class="info-card-row">
                    <span class="info-card-label">楼层</span>
                    <span class="info-card-value">{{ currentDetail.floor || '-' }}</span>
                  </div>
                  <div class="info-card-row">
                    <span class="info-card-label">联系电话</span>
                    <span class="info-card-value">{{ currentDetail.phone || '-' }}</span>
                  </div>
                </div>
              </el-col>
              <el-col :span="8">
                <div class="info-card">
                  <div class="info-card-title">
                    <el-icon><Setting /></el-icon>
                    <span>组织信息</span>
                  </div>
                  <div class="info-card-row">
                    <span class="info-card-label">上级科室</span>
                    <span class="info-card-value">{{ parentName(currentDetail.parentId) || '无' }}</span>
                  </div>
                  <div class="info-card-row">
                    <span class="info-card-label">科室类型</span>
                    <span class="info-card-value">{{ currentDetail.departmentType || '-' }}</span>
                  </div>
                  <div class="info-card-row">
                    <span class="info-card-label">排序号</span>
                    <span class="info-card-value">{{ currentDetail.sortOrder }}</span>
                  </div>
                </div>
              </el-col>
              <el-col :span="8">
                <div class="info-card">
                  <div class="info-card-title">
                    <el-icon><Document /></el-icon>
                    <span>科室简介</span>
                  </div>
                  <div class="info-card-desc">{{ currentDetail.description || '暂无简介' }}</div>
                </div>
              </el-col>
            </el-row>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 新增/编辑 弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogMode === 'create' ? '新增科室' : '编辑科室'"
      width="620px"
      destroy-on-close
      top="8vh"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
        label-position="right"
      >
        <el-form-item label="科室名称" prop="name">
          <el-input v-model="formData.name" maxlength="50" show-word-limit placeholder="例如：神经内科" />
        </el-form-item>
        <el-form-item label="科室编号" prop="code">
          <el-input
            v-model="formData.code"
            placeholder="不填则自动生成，例如：NEUROLOGY"
            maxlength="50"
          />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="上级科室" prop="parentId">
              <el-tree-select
                v-model="formData.parentId"
                :data="selectTreeData"
                node-key="id"
                :props="{ label: 'name', children: 'children' }"
                check-strictly
                render-after-expand
                placeholder="不选则为顶级科室"
                clearable
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="科室类型" prop="departmentType">
              <el-select v-model="formData.departmentType" placeholder="请选择" style="width: 100%">
                <el-option
                  v-for="t in departmentTypeOptions"
                  :key="t"
                  :label="t"
                  :value="t"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="楼层" prop="floor">
              <el-input v-model="formData.floor" placeholder="例如：门诊三楼" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话" prop="phone">
              <el-input v-model="formData.phone" placeholder="例如：010-12345678" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="排序号" prop="sortOrder">
          <el-input-number v-model="formData.sortOrder" :min="0" :max="9999" controls-position="right" />
        </el-form-item>
        <el-form-item label="简介" prop="description">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="3"
            maxlength="255"
            show-word-limit
            placeholder="简要描述科室的诊疗方向或特色"
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
  ArrowDown,
  Back,
  CaretBottom,
  CaretRight,
  Delete,
  Document,
  Edit,
  Location,
  Menu,
  OfficeBuilding,
  Phone,
  Plus,
  Refresh,
  Search,
  View
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
const treeKeyword = ref('')
const typeFilter = ref('')

// ---------- 弹窗 ----------
const dialogVisible = ref(false)
const dialogMode = ref('create')
const submitLoading = ref(false)
const formRef = ref(null)
const formData = reactive({
  name: '',
  code: '',
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
const enabledCount = computed(() =>
  (tableData.value || []).filter((d) => d.status === 1).length
)
const disabledCount = computed(() =>
  (tableData.value || []).filter((d) => d.status !== 1).length
)
const typeCount = computed(() => {
  const set = new Set()
  for (const d of tableData.value || []) {
    if (d.departmentType) set.add(d.departmentType)
  }
  return set.size
})

const selectTreeData = computed(() => treeData.value)

// 计算完整树的节点总数（包括子科室）
const totalTreeCount = computed(() => {
  let count = 0
  const walk = (nodes) => {
    if (!nodes) return
    for (const n of nodes) {
      count++
      walk(n.children)
    }
  }
  walk(treeData.value)
  return count
})

const departmentTypeOptions = computed(() => {
  const set = new Set()
  for (const d of tableData.value || []) {
    if (d.departmentType) set.add(d.departmentType)
  }
  if (set.size === 0) set.add('门诊')
  return Array.from(set)
})

// 按关键字过滤树节点（保留父节点）
function filterTreeNodes(nodes, kw) {
  if (!kw) return nodes
  const lower = kw.toLowerCase()
  const walk = (list) => {
    const result = []
    for (const n of list) {
      const nameMatch = (n.name || '').toLowerCase().includes(lower)
      const codeMatch = (n.code || '').toLowerCase().includes(lower)
      const children = n.children && n.children.length > 0 ? walk(n.children) : []
      if (nameMatch || codeMatch || children.length > 0) {
        result.push({ ...n, children })
      }
    }
    return result
  }
  return walk(nodes)
}
const filteredTreeData = computed(() => filterTreeNodes(treeData.value, treeKeyword.value))

// 列表筛选（关键字 + 类型）
const filteredTableData = computed(() => {
  const kw = (keyword.value || '').toLowerCase()
  return (tableData.value || []).filter((d) => {
    const nameMatch = !kw || (d.name || '').toLowerCase().includes(kw) || (d.code || '').toLowerCase().includes(kw)
    const typeMatch = !typeFilter.value || d.departmentType === typeFilter.value
    return nameMatch && typeMatch
  })
})

function handleTypeFilter(cmd) {
  typeFilter.value = cmd
}

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
  formData.code = ''
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
  formData.code = row.code || ''
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
        code: formData.code || undefined,
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
        code: formData.code || undefined,
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

// ---------- 组件通信 ----------
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
  min-height: 100%;
  padding: 24px;
  box-sizing: border-box;
}

/* ========== 顶部：与排班管理页面一致 ========== */
.department-card {
  border-radius: 12px;
}

.department-card :deep(.el-card__header) {
  padding: 18px 24px;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.department-card :deep(.el-card__body) {
  padding: 20px 24px;
}

/* ========== 主体：左栏目录 + 右栏列表/详情 ========== */
.main-layout {
  display: grid;
  grid-template-columns: 280px 1fr;
  gap: 20px;
  align-items: start;
}

/* ========== 左栏：科室目录 ========== */
.sidebar {
  background: #fafbfc;
  border: 1px solid #ebeef5;
  border-radius: 10px;
  padding: 16px;
  box-sizing: border-box;
  position: sticky;
  top: 0;
}

.sidebar-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-bottom: 12px;
  border-bottom: 1px solid #ebeef5;
  margin-bottom: 12px;
}

.sidebar-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.tree-search {
  margin-bottom: 6px;
}

.dept-tree {
  background: transparent;
}

/* 隐藏 el-tree 默认的展开箭头图标（避免与自定义图标重复） */
.dept-tree :deep(.el-tree-node__expand-icon) {
  display: none;
}

/* 隐藏 leaf 节点的占位图标（leaf 节点没有 expand-icon 但有 leaf-icon 占位） */
.dept-tree :deep(.el-tree-node.is-leaf > .el-tree-node__content > .el-tree-node__expand-icon) {
  display: none;
}

/* 增强子科室缩进层次：每一级更深一点的缩进 + 视觉区分 */
.dept-tree :deep(.el-tree-node__content) {
  height: 38px;
  border-radius: 6px;
  transition: background 0.15s;
}

.dept-tree :deep(.el-tree-node__content:hover) {
  background: #ecf5ff;
}

/* 父节点选中：浅色高亮背景 */
.dept-tree :deep(.el-tree-node.is-current:not(.is-leaf) > .el-tree-node__content) {
  background: #ecf5ff;
}

/* 子科室（叶子节点）选中：深蓝色背景 + 左侧蓝色色条 + 文字加粗 */
.dept-tree :deep(.el-tree-node.is-current.is-leaf > .el-tree-node__content) {
  background: #409eff;
  color: #fff;
  box-shadow: 0 2px 6px rgba(64, 158, 255, 0.25);
}

.dept-tree :deep(.el-tree-node.is-current.is-leaf > .el-tree-node__content .dept-tree-name) {
  color: #fff;
  font-weight: 600;
}

.dept-tree :deep(.el-tree-node.is-current.is-leaf > .el-tree-node__content .dept-tree-code) {
  background: rgba(255, 255, 255, 0.25);
  color: #fff;
  border-color: rgba(255, 255, 255, 0.35);
}

.dept-tree :deep(.el-tree-node.is-current.is-leaf > .el-tree-node__content .dept-tree-icon) {
  background: rgba(255, 255, 255, 0.25);
  color: #fff;
}

/* 子科室左侧连接线 + 背景层次 */
.dept-tree :deep(.el-tree-node.is-leaf .el-tree-node__content) {
  background: transparent;
}

/* 调整每级缩进视觉：使用 padding-left 区分层级（在 el-tree 基础上叠加） */
.dept-tree :deep(.el-tree-node .el-tree-node__children .el-tree-node__content) {
  background: #fff;
}

.dept-tree :deep(.el-tree-node .el-tree-node__children .el-tree-node__children .el-tree-node__content) {
  background: #fafbfc;
}

.dept-tree-node {
  display: flex;
  align-items: center;
  width: 100%;
  padding-right: 10px;
  box-sizing: border-box;
}

.dept-tree-main {
  display: flex;
  align-items: center;
  gap: 6px;
  min-width: 0;
  flex: 1;
}

.dept-tree-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 18px;
  height: 18px;
  border-radius: 4px;
  font-size: 13px;
  flex-shrink: 0;
}

.dept-tree-icon.icon-root {
  color: #409eff;
  background: #ecf5ff;
}

.dept-tree-icon.icon-leaf {
  color: #67c23a;
  background: #f0f9eb;
}

.dept-tree-icon.icon-sub {
  color: #909399;
  background: #f4f4f5;
}

.dept-tree-name {
  font-size: 14px;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.dept-tree-code {
  font-size: 11px;
  color: #c0c4cc;
  background: #fff;
  border: 1px solid #ebeef5;
  padding: 1px 6px;
  border-radius: 8px;
  margin-left: 4px;
  flex-shrink: 0;
}

/* ========== 右栏：列表工具条 ========== */
.content-area {
  min-width: 0;
}

.list-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
}

/* ========== 列表表格单元格 ========== */
.table-dept-cell {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 2px 0;
}

.table-dept-icon {
  width: 34px;
  height: 34px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 16px;
  flex-shrink: 0;
}

.table-dept-icon.icon-blue {
  background: #409eff;
}

.table-dept-icon.icon-purple {
  background: #8e44ad;
}

.table-dept-name {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
  margin-bottom: 2px;
}

.table-dept-code {
  font-size: 12px;
  color: #909399;
}

.table-parent {
  color: #606266;
  font-size: 13px;
}

.table-simple-cell {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #606266;
  font-size: 14px;
}

.table-simple-cell .el-icon {
  color: #c0c4cc;
  font-size: 13px;
}

.table-sort-num {
  display: inline-block;
  min-width: 28px;
  padding: 2px 8px;
  background: #f5f7fa;
  border-radius: 6px;
  font-size: 12px;
  color: #909399;
}

/* ========== 详情视图 ========== */
.detail-wrapper {
  background: #fff;
  border-radius: 10px;
  padding: 20px;
  border: 1px solid #ebeef5;
}

.detail-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-bottom: 20px;
  border-bottom: 1px solid #ebeef5;
  margin-bottom: 20px;
}

.detail-head-left {
  display: flex;
  align-items: center;
  gap: 14px;
}

.detail-avatar {
  width: 56px;
  height: 56px;
  background: #409eff;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 28px;
  flex-shrink: 0;
}

.detail-name {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 6px;
}

.detail-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.detail-meta-item {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #909399;
}

.detail-head-right {
  display: flex;
  gap: 10px;
}

.info-cards {
  margin: 0;
}

.info-card {
  background: #fafbfc;
  border: 1px solid #f0f2f5;
  border-radius: 10px;
  padding: 18px;
  height: 100%;
  box-sizing: border-box;
}

.info-card-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 12px;
  padding-bottom: 10px;
  border-bottom: 1px dashed #ebeef5;
}

.info-card-title .el-icon {
  color: #409eff;
}

.info-card-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 6px 0;
  font-size: 13px;
}

.info-card-label {
  color: #909399;
}

.info-card-value {
  color: #303133;
  font-weight: 500;
  text-align: right;
  max-width: 60%;
  overflow: hidden;
  text-overflow: ellipsis;
}

.info-card-desc {
  font-size: 13px;
  color: #606266;
  line-height: 1.8;
}
</style>