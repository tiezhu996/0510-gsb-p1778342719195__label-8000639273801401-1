<template>
  <div class="user-manage">
    <el-card>
      <div class="header-action">
        <el-button type="primary" @click="handleAdd">添加用户</el-button>
      </div>
      
      <el-table :data="tableData" border style="width: 100%" v-loading="loading">
        <el-table-column prop="username" label="账号" />
        <el-table-column prop="realName" label="姓名" />
        <el-table-column prop="role" label="角色">
          <template #default="{ row }">
            <el-tag :type="row.role === 'admin' ? 'danger' : 'success'">
              {{ row.role === 'admin' ? '管理员' : '操作员' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态">
          <template #default="{ row }">
            <el-tag :type="row.status === 0 ? 'success' : 'info'">
              {{ row.status === 0 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button size="small" @click="handleEdit(row)">编辑</el-button>
            <el-popconfirm title="确定删除该用户吗？" @confirm="handleDelete(row)">
              <template #reference>
                <el-button size="small" type="danger" :disabled="row.username === 'admin'">删除</el-button>
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
              layout="total, prev, pager, next"
              :total="total"
              @current-change="loadData"
            />
      </div>
    </el-card>

    <!-- Dialog -->
    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="500px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="账号" prop="username">
          <el-input v-model="form.username" :disabled="!!form.id" />
        </el-form-item>
        <el-form-item label="密码" prop="password" :required="!form.id">
          <el-input v-model="form.password" type="password" placeholder="如果不修改请留空" />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="form.realName" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="form.role">
            <el-option label="管理员" value="admin" />
            <el-option label="操作员" value="operator" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="0" :inactive-value="1" active-text="启用" inactive-text="禁用" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getUserList, saveUser, deleteUser } from '@/api'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const dialogVisible = ref(false)
const dialogTitle = ref('添加用户')
const formRef = ref(null)

const form = reactive({
  id: null,
  username: '',
  password: '',
  realName: '',
  role: 'operator',
  status: 0
})

const rules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }]
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getUserList({
      page: currentPage.value,
      size: pageSize.value
    })
    tableData.value = res.content
    total.value = res.totalElements
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  dialogTitle.value = '添加用户'
  form.id = null
  form.username = ''
  form.password = ''
  form.realName = ''
  form.role = 'operator'
  form.status = 0
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑用户'
  Object.assign(form, row)
  form.password = '' // Clear password field
  dialogVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await deleteUser(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    console.error(error)
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      if (!form.id && !form.password) {
        ElMessage.error('新建用户必须填写密码')
        return
      }
      try {
        await saveUser(form)
        ElMessage.success('保存成功')
        dialogVisible.value = false
        loadData()
      } catch (error) {
        console.error(error)
      }
    }
  })
}

const formatTime = (time) => {
  if (!time) return '-'
  let t = time.replace('T', ' ')
  if (t.includes('.')) {
    t = t.split('.')[0]
  }
  return t
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.header-action {
  margin-bottom: 20px;
}
.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
