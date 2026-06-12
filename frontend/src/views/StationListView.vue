<template>
  <div class="page-wrap">
    <el-card shadow="never" style="margin: 20px">
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <span style="font-weight: 600">충전소 목록</span>
          <el-button v-if="authStore.hasRole('ADMIN')" type="primary" @click="openCreate">
            <el-icon><Plus /></el-icon> 충전소 등록
          </el-button>
        </div>
      </template>

      <el-table :data="stationStore.stations" v-loading="stationStore.loading" stripe style="width:100%">
        <el-table-column prop="stationCode" label="충전소 코드" min-width="130" />
        <el-table-column prop="name" label="충전소명" min-width="150" />
        <el-table-column prop="address" label="주소" min-width="220" />
        <el-table-column prop="operationStatus" label="상태" min-width="90">
          <template #default="{ row }">
            <el-tag :type="OPERATION_STATUS_TYPE[row.operationStatus]" size="small">
              {{ label(OPERATION_STATUS_LABEL, row.operationStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="totalChargers" label="충전기" min-width="70" align="center" />
        <el-table-column label="관리" min-width="80" align="center">
          <template #default="{ row }">
            <el-button size="small" @click="$router.push(`/stations/${row.id}`)">상세</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 충전소 등록 다이얼로그 -->
    <el-dialog v-model="createDialogVisible" title="충전소 등록" width="500px">
      <el-form :model="createForm" :rules="createRules" ref="createFormRef" label-width="100px">
        <el-form-item label="충전소 코드" prop="stationCode">
          <el-input v-model="createForm.stationCode" placeholder="예: EPIT-GN-002" />
        </el-form-item>
        <el-form-item label="충전소명" prop="name">
          <el-input v-model="createForm.name" placeholder="예: 강남 E-pit 2호" />
        </el-form-item>
        <el-form-item label="주소" prop="address">
          <el-input v-model="createForm.address" placeholder="도로명 주소" />
        </el-form-item>
        <el-form-item label="위도" prop="latitude">
          <el-input-number v-model="createForm.latitude" :precision="6" :step="0.0001" style="width: 100%" />
        </el-form-item>
        <el-form-item label="경도" prop="longitude">
          <el-input-number v-model="createForm.longitude" :precision="6" :step="0.0001" style="width: 100%" />
        </el-form-item>
        <el-form-item label="운영 상태">
          <el-select v-model="createForm.operationStatus" style="width: 100%">
            <el-option label="운영중" value="ACTIVE" />
            <el-option label="점검중" value="MAINTENANCE" />
            <el-option label="폐쇄" value="CLOSED" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">취소</el-button>
        <el-button type="primary" :loading="saving" @click="submitCreate">등록</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { useStationStore } from '@/stores/station'
import { useAuthStore } from '@/stores/auth'
import { stationApi } from '@/api/station.api'
import { OPERATION_STATUS_LABEL, OPERATION_STATUS_TYPE, label } from '@/utils/labels'
import type { OperationStatus } from '@/types'

const stationStore = useStationStore()
const authStore = useAuthStore()

const createDialogVisible = ref(false)
const saving = ref(false)
const createFormRef = ref<FormInstance>()

const createForm = reactive({
  stationCode: '',
  name: '',
  address: '',
  latitude: 37.5,
  longitude: 127.0,
  operationStatus: 'ACTIVE' as string,
})

const createRules = {
  stationCode: [{ required: true, message: '충전소 코드를 입력하세요', trigger: 'blur' }],
  name: [{ required: true, message: '충전소명을 입력하세요', trigger: 'blur' }],
  address: [{ required: true, message: '주소를 입력하세요', trigger: 'blur' }],
  latitude: [{ required: true, message: '위도를 입력하세요', trigger: 'blur' }],
  longitude: [{ required: true, message: '경도를 입력하세요', trigger: 'blur' }],
}

function openCreate() {
  Object.assign(createForm, { stationCode: '', name: '', address: '', latitude: 37.5, longitude: 127.0, operationStatus: 'ACTIVE' })
  createDialogVisible.value = true
}

async function submitCreate() {
  await createFormRef.value?.validate(async (valid) => {
    if (!valid) return
    saving.value = true
    try {
      await stationApi.create({ ...createForm, operationStatus: createForm.operationStatus as OperationStatus })
      ElMessage.success('충전소가 등록되었습니다.')
      createDialogVisible.value = false
      await stationStore.fetchAll()
    } catch {
      ElMessage.error('등록에 실패했습니다.')
    } finally {
      saving.value = false
    }
  })
}

onMounted(() => stationStore.fetchAll())
</script>
