<template>
  <div class="login-container">
    <div class="login-card">
      <div class="login-header">
        <div class="brand">
          <span class="brand-icon">🩺</span>
          <div class="brand-text">
            <h1>医生排班管理系统</h1>
            <p>Doctor Scheduling Platform</p>
          </div>
        </div>
      </div>

      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="rules"
        size="large"
        label-position="top"
        @submit.prevent="handleSubmit"
      >
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名"
            autocomplete="username"
          >
            <template #prefix>
              <el-icon><User /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            show-password
            autocomplete="current-password"
            @keyup.enter="handleSubmit"
          >
            <template #prefix>
              <el-icon><Lock /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            class="login-btn"
            :loading="loading"
            @click="handleSubmit"
          >
            {{ loading ? '登录中...' : '登 录' }}
          </el-button>
        </el-form-item>
      </el-form>

      <div class="login-footer">
        <el-alert
          type="info"
          :closable="false"
          show-icon
          title="测试账号：admin / 123456"
        />
      </div>
    </div>

    <div class="copyright">
      <span>© {{ currentYear }} 医生排班管理系统 · 管理员端</span>
    </div>
  </div>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const loginFormRef = ref(null)
const loading = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, message: '用户名长度至少为 2', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 4, message: '密码长度至少为 4', trigger: 'blur' }
  ]
}

const currentYear = computed(() => new Date().getFullYear())

async function handleSubmit() {
  if (!loginFormRef.value) return
  try {
    await loginFormRef.value.validate()
  } catch {
    return
  }

  loading.value = true
  try {
    await userStore.login({
      username: loginForm.username.trim(),
      password: loginForm.password
    })
    ElMessage.success(`欢迎，${userStore.realName || userStore.username}`)
    const redirect = route.query.redirect
    if (redirect && typeof redirect === 'string') {
      router.replace(redirect)
    } else {
      router.replace('/')
    }
  } catch (err) {
    // 错误消息已由 request 拦截器处理
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  position: relative;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1e88e5 0%, #42a5f5 50%, #26c6da 100%);
  padding: 24px;
  box-sizing: border-box;
}

.login-container::before {
  content: '';
  position: absolute;
  inset: 0;
  background: radial-gradient(circle at 20% 30%, rgba(255, 255, 255, 0.15), transparent 40%),
    radial-gradient(circle at 80% 70%, rgba(255, 255, 255, 0.15), transparent 40%);
  pointer-events: none;
}

.login-card {
  position: relative;
  width: 100%;
  max-width: 420px;
  background: #ffffff;
  border-radius: 12px;
  padding: 40px 36px 28px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
}

.login-header {
  margin-bottom: 28px;
  text-align: center;
}

.brand {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
}

.brand-icon {
  font-size: 40px;
  line-height: 1;
}

.brand-text h1 {
  margin: 0;
  font-size: 22px;
  color: #1f2d3d;
  font-weight: 600;
}

.brand-text p {
  margin: 4px 0 0;
  font-size: 12px;
  color: #909399;
  letter-spacing: 1px;
}

.login-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
  letter-spacing: 4px;
}

.login-footer {
  margin-top: 8px;
}

.copyright {
  position: absolute;
  bottom: 16px;
  color: rgba(255, 255, 255, 0.9);
  font-size: 12px;
}
</style>