<template>
  <div class="auth-bg">
    <el-card class="auth-card" shadow="hover">
      <div class="logo"><img class="logo-mark" src="/brand/prisdvl-nexus.png" alt="" />Prisdoc 智能平台</div>
      <h2 class="title">欢迎回来</h2>
      <el-form :model="form" label-position="top">
        <el-form-item label="用户名">
          <el-input v-model="form.username" placeholder="请输入用户名" size="large" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" size="large" @keyup.enter="handleLogin" />
        </el-form-item>
        <el-button type="primary" class="submit-btn" size="large" @click="handleLogin" :loading="loading">登录</el-button>
        <div class="footer-text">
          <router-link to="/register">没有账号？免费注册</router-link>
        </div>
      </el-form>
    </el-card>
    <ThemeCorner />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { userApi } from '../../api/user'
import ThemeCorner from '../../components/ThemeCorner.vue'
import { STORAGE_KEYS } from '../../constants'

const router = useRouter()
const loading = ref(false)
const form = ref({ username: '', password: '' })

const handleLogin = async () => {
  if (!form.value.username || !form.value.password) {
    return ElMessage.warning('请填写完整的账号和密码')
  }
  loading.value = true
  try {
    const res = await userApi.login(form.value)
    const loginData = res.data || {}
    const accessToken = loginData.accessToken || loginData.token
    const refreshToken = loginData.refreshToken
    const user = loginData.user || {}

    if (!accessToken) {
      throw new Error('登录成功但后端未返回 accessToken')
    }

    // 保存 Token
    localStorage.setItem(STORAGE_KEYS.ACCESS_TOKEN, accessToken)
    if (refreshToken) {
      localStorage.setItem(STORAGE_KEYS.REFRESH_TOKEN, refreshToken)
    }
    if (user.id) {
      localStorage.setItem('userId', user.id)
    }
    localStorage.setItem(STORAGE_KEYS.USER_INFO, JSON.stringify(user))
    if (user.username) {
      localStorage.setItem('userName', user.username)
    }

    ElMessage.success('登录成功！')
    router.push('/dashboard')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
/* 沉浸式深色背景：只有深底 + 顶部一抹品牌光晕，不做玻璃拟态/粒子动画 */
.auth-bg {
  position: relative;
  height: 100vh; display: flex; justify-content: center; align-items: center;
  background: var(--pd-bg);
  background-image: var(--pd-bg-glow);
  background-repeat: no-repeat;
}
.auth-card {
  width: 400px; padding: 20px 30px; border-radius: var(--pd-r-xl);
  background: var(--pd-surface);
  border: 1px solid var(--pd-border);
  box-shadow: var(--pd-shadow-lg);
}
.logo {
  text-align: center; font-size: 24px; font-weight: bold; margin-bottom: 10px;
  letter-spacing: 0.02em;
  background: var(--pd-grad-text);
  -webkit-background-clip: text; background-clip: text;
  color: transparent;
}
.title { text-align: center; font-size: 18px; color: var(--pd-text); margin-bottom: 25px; font-weight: 500; }
.logo-mark { display: inline-block; height: 26px; width: auto; vertical-align: -4px; margin-right: 8px; }
.submit-btn { width: 100%; margin-top: 10px; font-weight: 500; letter-spacing: 0.05em; }
.footer-text { text-align: center; margin-top: 15px; font-size: 14px; color: var(--pd-text-3); }
.footer-text a { color: var(--pd-primary-text); text-decoration: none; transition: color var(--pd-dur-fast) var(--pd-ease); }
.footer-text a:hover { color: var(--pd-primary-hover); }
</style>
