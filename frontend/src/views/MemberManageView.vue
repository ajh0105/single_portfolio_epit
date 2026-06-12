<template>
  <div style="padding: 20px">
    <el-card shadow="never">
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <span style="font-weight: 600">사용자 관리</span>
          <el-button type="primary" @click="openCreate">
            <el-icon><Plus /></el-icon> 사용자 추가
          </el-button>
        </div>
      </template>

      <el-table :data="members" v-loading="loading" stripe style="width:100%">
        <el-table-column prop="username" label="아이디" min-width="120" />
        <el-table-column prop="name" label="이름" min-width="100" />
        <el-table-column prop="email" label="이메일" min-width="200" />
        <el-table-column prop="role" label="역할" min-width="90">
          <template #default="{ row }">
            <el-tag :type="ROLE_TYPE[row.role]" size="small">{{ label(ROLE_LABEL, row.role) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="enabled" label="상태" min-width="70">
          <template #default="{ row }">
            <el-tag :type="row.enabled ? 'success' : 'danger'" size="small">
              {{ row.enabled ? '활성' : '비활성' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="관리" min-width="80" align="center">
          <template #default="{ row }">
            <el-button size="small" type="danger" text @click.stop="confirmDelete(row)"
              :disabled="row.username === 'admin'">삭제</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 사용자 추가 다이얼로그 -->
    <el-dialog v-model="createDialogVisible" title="사용자 추가" width="460px">
      <el-form :model="createForm" :rules="createRules" ref="createFormRef" label-width="90px">
        <el-form-item label="아이디" prop="username">
          <el-input v-model="createForm.username" placeholder="영문/숫자 4~20자" />
        </el-form-item>
        <el-form-item label="이름" prop="name">
          <el-input v-model="createForm.name" />
        </el-form-item>
        <el-form-item label="이메일" prop="email">
          <el-input v-model="createForm.email" type="email" />
        </el-form-item>
        <el-form-item label="역할">
          <el-select v-model="createForm.role" style="width: 100%">
            <el-option label="관리자" value="ADMIN" />
            <el-option label="운영자" value="OPERATOR" />
            <el-option label="조회자" value="VIEWER" />
          </el-select>
        </el-form-item>
        <el-form-item label="비밀번호" prop="password">
          <el-input v-model="createForm.password" type="password" show-password placeholder="8자 이상" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">취소</el-button>
        <el-button type="primary" :loading="saving" @click="submitCreate">추가</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'
import http from '@/api/http'
import { ROLE_LABEL, ROLE_TYPE, label } from '@/utils/labels'

const members = ref<any[]>([])
const loading = ref(false)
const createDialogVisible = ref(false)
const saving = ref(false)
const createFormRef = ref<FormInstance>()

const createForm = reactive({
  username: '', name: '', email: '', role: 'VIEWER', password: '',
})

const createRules = {
  username: [
    { required: true, message: '아이디를 입력하세요', trigger: 'blur' },
    { min: 4, max: 20, message: '4~20자로 입력하세요', trigger: 'blur' },
  ],
  name: [{ required: true, message: '이름을 입력하세요', trigger: 'blur' }],
  email: [
    { required: true, message: '이메일을 입력하세요', trigger: 'blur' },
    { type: 'email', message: '올바른 이메일 형식', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '비밀번호를 입력하세요', trigger: 'blur' },
    { min: 8, message: '8자 이상 입력하세요', trigger: 'blur' },
  ],
}

async function fetchMembers() {
  loading.value = true
  try {
    const res = await http.get('/members')
    members.value = res.data.data?.content ?? []
  } finally { loading.value = false }
}

function openCreate() {
  Object.assign(createForm, { username: '', name: '', email: '', role: 'VIEWER', password: '' })
  createDialogVisible.value = true
}

async function submitCreate() {
  await createFormRef.value?.validate(async (valid) => {
    if (!valid) return
    saving.value = true
    try {
      await http.post('/members', { ...createForm })
      ElMessage.success('사용자가 추가되었습니다.')
      createDialogVisible.value = false
      await fetchMembers()
    } catch (e: any) {
      ElMessage.error(e?.response?.data?.error?.message ?? '추가에 실패했습니다.')
    } finally { saving.value = false }
  })
}

async function confirmDelete(row: any) {
  await ElMessageBox.confirm(`'${row.username}' 계정을 삭제하시겠습니까?`, '계정 삭제', {
    confirmButtonText: '삭제', cancelButtonText: '취소', type: 'warning',
  })
  try {
    await http.delete(`/members/${row.id}`)
    ElMessage.success('삭제되었습니다.')
    await fetchMembers()
  } catch {
    ElMessage.error('삭제에 실패했습니다.')
  }
}

onMounted(fetchMembers)
</script>
