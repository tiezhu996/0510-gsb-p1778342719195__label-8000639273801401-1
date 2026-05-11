<template>
  <div class="verify-manage">
    <el-card class="verify-card">
      <template #header>
        <div class="card-header">
          <span>卡密核销</span>
        </div>
      </template>
      
      <el-form :model="form" ref="formRef" :rules="rules" label-width="100px">
        <el-form-item label="卡号" prop="cardNumber">
          <el-input v-model="form.cardNumber" placeholder="请输入9位卡号" />
        </el-form-item>
        <el-form-item label="密码" prop="cardPassword">
          <el-input v-model="form.cardPassword" placeholder="请输入6位密码" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleVerify">确认核销</el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
      
      <div v-if="verifyResult" class="result-box">
         <el-result
            icon="success"
            title="核销成功"
            sub-title="该卡密已成功核销"
          >
            <template #extra>
              <el-button type="primary" @click="verifyResult = false">继续核销</el-button>
            </template>
          </el-result>
      </div>
    </el-card>

    <el-card class="history-card">
      <template #header>
        <div class="card-header">
          <span>最近核销记录</span>
          <el-button type="primary" link @click="loadData">一键刷新</el-button>
        </div>
      </template>

      <!-- Search Form -->
      <el-form :inline="true" :model="historyQuery" class="search-form">
        <el-form-item label="卡号">
          <el-input v-model="historyQuery.cardNumber" placeholder="卡号" clearable style="width: 140px" />
        </el-form-item>
        <el-form-item label="批次号">
          <el-input v-model="historyQuery.batchNumber" placeholder="批次号" clearable style="width: 160px" />
        </el-form-item>
        <el-form-item label="核销日期">
          <el-date-picker
            v-model="historyQuery.timeRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始"
            end-placeholder="结束"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 320px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" stripe width="100%" style="width: 100%" v-loading="listLoading">
        <el-table-column prop="cardNumber" label="卡号" min-width="140" />
        <el-table-column prop="batchNumber" label="批次号" min-width="180" />
        <el-table-column prop="useTime" label="核销时间" min-width="180">
          <template #default="{ row }">
            {{ formatTime(row.useTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="operator" label="操作员" min-width="120" />
      </el-table>

      <!-- Pagination -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { verifyCard, getVerificationHistory } from '@/api'
import { useUserStore } from '@/store/user'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)
const verifyResult = ref(false)

const form = reactive({
  cardNumber: '',
  cardPassword: ''
})

const rules = {
  cardNumber: [
    { required: true, message: '请输入卡号', trigger: 'blur' },
    { len: 9, message: '卡号长度应为9位', trigger: 'blur' }
  ],
  cardPassword: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { len: 6, message: '密码长度应为6位', trigger: 'blur' }
  ]
}

const handleVerify = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        await verifyCard({
          ...form,
          operator: userStore.userInfo?.realName
        })
        verifyResult.value = true
        resetForm()
        loadData()
      } catch (error) {
        console.error(error)
      } finally {
        loading.value = false
      }
    }
  })
}

const resetForm = () => {
  form.cardNumber = ''
  form.cardPassword = ''
}

const formatTime = (time) => {
  if (!time) return '-'
  let t = time.replace('T', ' ')
  if (t.includes('.')) {
    t = t.split('.')[0]
  }
  return t
}

// History List
const tableData = ref([])
const listLoading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const historyQuery = reactive({
  cardNumber: '',
  batchNumber: '',
  timeRange: []
})

const handleSearch = () => {
  currentPage.value = 1
  loadData()
}

const handleReset = () => {
  historyQuery.cardNumber = ''
  historyQuery.batchNumber = ''
  historyQuery.timeRange = []
  handleSearch()
}

const handleSizeChange = (val) => {
  pageSize.value = val
  loadData()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  loadData()
}

const loadData = async () => {
  listLoading.value = true
  try {
    const res = await getVerificationHistory({
      page: currentPage.value,
      size: pageSize.value,
      cardNumber: historyQuery.cardNumber,
      batchNumber: historyQuery.batchNumber,
      useTimeStart: historyQuery.timeRange && historyQuery.timeRange[0] ? historyQuery.timeRange[0] : '',
      useTimeEnd: historyQuery.timeRange && historyQuery.timeRange[1] ? historyQuery.timeRange[1] : '',
    })
    tableData.value = res.content
    total.value = res.totalElements
  } catch (error) {
    console.error(error)
  } finally {
    listLoading.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.verify-card {
  max-width: 600px;
  margin-bottom: 20px;
}
.history-card {
  max-width: 100%;
}
.search-form {
  margin-bottom: 20px;
  background-color: #f9f9f9;
  padding: 15px;
  border-radius: 4px;
}
.result-box {
  margin-top: 20px;
  border-top: 1px solid #ebeef5;
}
.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
