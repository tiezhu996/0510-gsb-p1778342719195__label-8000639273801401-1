<template>
  <el-container class="layout-container">
    <el-aside width="200px">
      <el-menu
        :default-active="activeMenu"
        class="el-menu-vertical"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
        router
      >
        <div class="logo">
          <span>卡密管理系统</span>
        </div>
        <el-menu-item index="/dashboard">
          <el-icon><DataLine /></el-icon>
          <span>首页</span>
        </el-menu-item>
        <el-menu-item index="/card-manage">
          <el-icon><CreditCard /></el-icon>
          <span>发卡管理</span>
        </el-menu-item>
        <el-menu-item index="/verify-manage">
          <el-icon><Checked /></el-icon>
          <span>核销管理</span>
        </el-menu-item>
        <el-menu-item index="/user-manage" v-if="userStore.userInfo?.role === 'admin'">
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    
    <el-container>
      <el-header>
        <div class="header-content">
          <div class="breadcrumb">
            {{ currentRouteName }}
          </div>
          <div class="user-info">
            <el-dropdown>
              <span class="el-dropdown-link">
                {{ userStore.userInfo?.realName }}
                <el-icon class="el-icon--right"><arrow-down /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="handleLogout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </el-header>
      
      <el-main>
        <router-view></router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { logout } from '@/api'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)
const currentRouteName = computed(() => route.meta.title)

const handleLogout = async () => {
  try {
    await logout()
    userStore.clearUserInfo()
    ElMessage.success('退出成功')
    router.push('/login')
  } catch (error) {
    console.error(error)
  }
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
}
.el-aside {
  background-color: #304156;
  color: #333;
}
.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  color: #fff;
  font-weight: bold;
  font-size: 18px;
}
.el-header {
  background-color: #fff;
  border-bottom: 1px solid #dcdfe6;
  padding: 0 20px;
}
.header-content {
  height: 60px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.el-dropdown-link {
  cursor: pointer;
  display: flex;
  align-items: center;
}
</style>
