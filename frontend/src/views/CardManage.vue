<template>
  <div class="card-manage">
    <el-tabs v-model="activeTab">
      <!-- Generate Card Tab -->
      <el-tab-pane label="一键发卡" name="generate">
        <el-card>
          <el-form :model="generateForm" label-width="120px" style="max-width: 500px">
            <el-form-item label="发卡数量">
              <el-input-number v-model="generateForm.count" :min="1" :max="10000" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="generating" @click="handleGenerate">生成卡密</el-button>
            </el-form-item>
          </el-form>
          
          <div v-if="lastBatch" class="batch-result">
            <el-alert
              :title="`发卡成功！批次号：${lastBatch.batchNumber}，共生成 ${lastBatch.totalCount} 张`"
              type="success"
              show-icon
              :closable="false"
            />
          </div>
        </el-card>
      </el-tab-pane>

      <!-- Card List Tab -->
      <el-tab-pane label="卡密列表" name="list">
        <el-card>
          <!-- Search Form -->
          <el-form :inline="true" :model="queryForm" class="demo-form-inline search-form">
            <el-form-item label="卡号">
              <el-input v-model="queryForm.cardNumber" placeholder="请输入卡号" clearable style="width: 150px" />
            </el-form-item>
            <el-form-item label="批次号">
              <el-input v-model="queryForm.batchNumber" placeholder="请输入批次号" clearable style="width: 180px" />
            </el-form-item>
            <el-form-item label="状态">
              <el-select v-model="queryForm.status" placeholder="全部" clearable style="width: 100px">
                <el-option label="未使用" :value="0" />
                <el-option label="已核销" :value="1" />
                <el-option label="已回收" :value="2" />
              </el-select>
            </el-form-item>
            <el-form-item label="核销日期">
              <el-date-picker
                v-model="queryForm.timeRange"
                type="datetimerange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                value-format="YYYY-MM-DD HH:mm:ss"
                style="width: 360px"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleQuery">查询</el-button>
              <el-button @click="resetQuery">重置</el-button>
              <el-button type="success" @click="handleExport">导出Excel</el-button>
              <el-button type="danger" plain @click="handleBatchRecycleVisible = true">批量回收</el-button>
            </el-form-item>
          </el-form>

          <!-- Data Table -->
          <el-table :data="tableData" border width="100%" style="width: 100%" v-loading="loading">
            <el-table-column prop="cardNumber" label="卡号" min-width="140" />
            <el-table-column prop="cardPassword" label="密码" min-width="100" />
            <el-table-column prop="batchNumber" label="批次号" min-width="180" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="生成时间" min-width="180">
              <template #default="{ row }">
                {{ formatTime(row.createTime) }}
              </template>
            </el-table-column>
            <el-table-column prop="useTime" label="核销时间" min-width="180">
              <template #default="{ row }">
                {{ formatTime(row.useTime) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" fixed="right" width="100">
              <template #default="{ row }">
                <el-popconfirm 
                  v-if="row.status === 0"
                  title="确定回收这张卡密吗？" 
                  @confirm="handleRecycle(row)"
                >
                  <template #reference>
                    <el-button size="small" type="danger">回收</el-button>
                  </template>
                </el-popconfirm>
              </template>
            </el-table-column>
          </el-table>

          <!-- Pagination -->
          <div class="pagination-container">
            <el-pagination
              v-model:current-page="currentPage"
              v-model:page-size="pageSize"
              :page-sizes="[10, 20, 50, 100]"
              layout="total, sizes, prev, pager, next, jumper"
              :total="total"
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
            />
          </div>
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <!-- Batch Recycle Dialog -->
    <el-dialog v-model="handleBatchRecycleVisible" title="批量回收" width="30%">
      <el-form>
        <el-form-item label="批次号">
          <el-input v-model="batchRecycleNumber" placeholder="请输入要回收的批次号" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleBatchRecycleVisible = false">取消</el-button>
          <el-button type="primary" @click="handleBatchRecycle">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { generateCards, getCardList, recycleCard, recycleBatch, exportCards } from '@/api'
import { useUserStore } from '@/store/user'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const activeTab = ref('generate')
const generating = ref(false)
const loading = ref(false)
const lastBatch = ref(null)

// Generate
const generateForm = reactive({
  count: 100
})

const handleGenerate = async () => {
  generating.value = true
  try {
    const res = await generateCards({
      count: generateForm.count,
      operator: userStore.userInfo?.realName
    })
    lastBatch.value = res
    ElMessage.success('发卡成功')
  } catch (error) {
    console.error(error)
  } finally {
    generating.value = false
  }
}

// List
const queryForm = reactive({
  cardNumber: '',
  batchNumber: '',
  status: '',
  timeRange: []
})
const tableData = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const resetQuery = () => {
  queryForm.cardNumber = ''
  queryForm.batchNumber = ''
  queryForm.status = ''
  queryForm.timeRange = []
  handleQuery()
}

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      ...queryForm,
      page: currentPage.value,
      size: pageSize.value,
      useTimeStart: queryForm.timeRange?.[0] || '',
      useTimeEnd: queryForm.timeRange?.[1] || ''
    }
    
    // 如果查询已核销，则默认按核销时间排序
    if (queryForm.status === 1) {
      params.sortBy = 'useTime'
    } else {
      params.sortBy = 'createTime'
    }

    delete params.timeRange
    const res = await getCardList(params)
    tableData.value = res.content
    total.value = res.totalElements
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  currentPage.value = 1
  loadData()
}

const handleSizeChange = (val) => {
  pageSize.value = val
  loadData()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  loadData()
}

const getStatusType = (status) => {
  switch (status) {
    case 0: return 'success'
    case 1: return 'info'
    case 2: return 'danger'
    default: return ''
  }
}

const getStatusText = (status) => {
  switch (status) {
    case 0: return '未使用'
    case 1: return '已核销'
    case 2: return '已回收'
    default: return '未知'
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

// Recycle
const handleRecycle = async (row) => {
  try {
    await recycleCard(row.cardNumber)
    ElMessage.success('回收成功')
    loadData()
  } catch (error) {
    console.error(error)
  }
}

// Batch Recycle
const handleBatchRecycleVisible = ref(false)
const batchRecycleNumber = ref('')

const handleBatchRecycle = async () => {
  if (!batchRecycleNumber.value) {
    ElMessage.warning('请输入批次号')
    return
  }
  try {
    const count = await recycleBatch(batchRecycleNumber.value)
    ElMessage.success(`成功回收 ${count} 张卡密`)
    handleBatchRecycleVisible.value = false
    loadData()
  } catch (error) {
    console.error(error)
  }
}

// Export
const handleExport = async () => {
  try {
    const params = {
      ...queryForm,
      useTimeStart: queryForm.timeRange?.[0] || '',
      useTimeEnd: queryForm.timeRange?.[1] || ''
    }
    if (queryForm.status === 1) {
      params.sortBy = 'useTime'
    } else {
      params.sortBy = 'createTime'
    }
    delete params.timeRange
    
    const res = await exportCards(params)
    const blob = new Blob([res], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const link = document.createElement('a')
    link.href = window.URL.createObjectURL(blob)
    link.download = `Cards_Export_${new Date().getTime()}.xlsx`
    link.click()
  } catch (error) {
    console.error(error)
    ElMessage.error('导出失败')
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.batch-result {
  margin-top: 20px;
}
.search-form {
  margin-bottom: 20px;
  background-color: #f9f9f9;
  padding: 15px;
  border-radius: 4px;
}
.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
