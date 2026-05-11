<template>
  <div class="public-query-container">
    <el-card class="query-card">
      <template #header>
        <div class="header">
          <h2>卡密状态查询</h2>
        </div>
      </template>
      
      <el-form :model="form" ref="formRef" :rules="rules" label-width="80px">
        <el-form-item label="卡号" prop="cardNumber">
          <el-input v-model="form.cardNumber" placeholder="请输入9位卡号" />
        </el-form-item>
        <el-form-item label="密码" prop="cardPassword">
          <el-input v-model="form.cardPassword" placeholder="请输入6位密码" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleQuery" class="full-width">查询</el-button>
        </el-form-item>
         <div class="login-link">
          <router-link to="/login">管理员登录</router-link>
        </div>
      </el-form>

      <div v-if="result" class="result-info">
        <el-descriptions title="查询结果" :column="1" border>
          <el-descriptions-item label="卡号">{{ result.cardNumber }}</el-descriptions-item>
          <el-descriptions-item label="状态">
             <el-tag :type="getStatusType(result.status)">{{ result.statusText }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="生成时间">{{ formatTime(result.createTime) }}</el-descriptions-item>
          <el-descriptions-item v-if="result.useTime" label="核销时间">{{ formatTime(result.useTime) }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { queryPublicCard } from '@/api'

const loading = ref(false)
const result = ref(null)
const formRef = ref(null)

const form = reactive({
  cardNumber: '',
  cardPassword: ''
})

const rules = {
  cardNumber: [{ required: true, message: '请输入卡号', trigger: 'blur' }],
  cardPassword: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleQuery = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      result.value = null
      try {
        const res = await queryPublicCard(form)
        result.value = res
      } catch (error) {
        console.error(error)
      } finally {
        loading.value = false
      }
    }
  })
}

const getStatusType = (status) => {
  switch (status) {
    case 0: return 'success'
    case 1: return 'info'
    case 2: return 'danger'
    default: return ''
  }
}

const formatTime = (time) => {
  if (!time) return '-'
  let t = time.replace('T', ' ')
  if (t.includes('.')) {
    t = t.split('.')[0]
  }
  return t
}
</script>

<style scoped>
.public-query-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f0f2f5;
}
.query-card {
  width: 400px;
}
.header {
  text-align: center;
}
.full-width {
  width: 100%;
}
.result-info {
  margin-top: 20px;
}
.login-link {
  text-align: center;
  margin-top: 10px;
}
</style>
