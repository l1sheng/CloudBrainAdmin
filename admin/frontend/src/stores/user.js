import { defineStore } from 'pinia'
import { login, getAdminInfo } from '@/api/auth'

const TOKEN_KEY = 'admin_token'
const USER_KEY = 'admin_user'

function readToken() {
  try {
    return localStorage.getItem(TOKEN_KEY) || ''
  } catch {
    return ''
  }
}

function readUser() {
  try {
    const raw = localStorage.getItem(USER_KEY)
    return raw ? JSON.parse(raw) : null
  } catch {
    return null
  }
}

export const useUserStore = defineStore('user', {
  state: () => ({
    token: readToken(),
    userInfo: readUser()
  }),
  getters: {
    isLoggedIn: (state) => !!state.token,
    username: (state) => state.userInfo?.username || '',
    realName: (state) => state.userInfo?.realName || '',
    roleCode: (state) => state.userInfo?.roleCode || '',
    roleName: (state) => state.userInfo?.roleName || '',
    // 科室医生管理员：格式为 [XXX]_DOCTOR_ADMIN，例如 OUTPATIENT_DOCTOR_ADMIN
    // 这类角色只管理对应科室类型下的事务
    isDepartmentAdmin: (state) => {
      const code = (state.userInfo?.roleCode || '').toUpperCase()
      // 排除纯系统管理员 ADMIN/SUPER_ADMIN
      if (code === 'ADMIN' || code === 'SUPER_ADMIN' || code.includes('SYSTEM')) return false
      return code.endsWith('_DOCTOR_ADMIN')
    },
    // 门诊医生管理员：OUTPATIENT_DOCTOR_ADMIN
    isClinicAdmin: (state) => {
      const code = (state.userInfo?.roleCode || '').toUpperCase()
      return code === 'OUTPATIENT_DOCTOR_ADMIN'
    },
    // 超级管理员/系统管理员：完整权限
    isSuperAdmin: (state) => {
      const code = (state.userInfo?.roleCode || '').toUpperCase()
      return code === 'ADMIN' || code === 'SUPER_ADMIN' || code.includes('SYSTEM')
    }
  },
  actions: {
    async login({ username, password }) {
      const data = await login({ username, password })
      this.token = data.token || ''
      this.userInfo = {
        userId: data.userId,
        username: data.username,
        realName: data.realName,
        roleCode: data.roleCode,
        roleName: data.roleName
      }
      try {
        localStorage.setItem(TOKEN_KEY, this.token)
        localStorage.setItem(USER_KEY, JSON.stringify(this.userInfo))
      } catch {
        // ignore storage error
      }
      return this.userInfo
    },
    async fetchInfo() {
      const data = await getAdminInfo()
      this.userInfo = {
        userId: data.userId,
        username: data.username,
        realName: data.realName,
        roleCode: data.roleCode,
        roleName: data.roleName
      }
      try {
        localStorage.setItem(USER_KEY, JSON.stringify(this.userInfo))
      } catch {
        // ignore
      }
      return this.userInfo
    },
    logout() {
      this.token = ''
      this.userInfo = null
      try {
        localStorage.removeItem(TOKEN_KEY)
        localStorage.removeItem(USER_KEY)
      } catch {
        // ignore
      }
    }
  }
})