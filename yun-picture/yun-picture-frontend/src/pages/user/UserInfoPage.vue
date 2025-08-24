<!-- src/pages/user/UserInfoPage.vue -->
<template>
  <div class="userInfoPage">
    <a-card :bordered="false" title="我的信息">
      <a-spin :spinning="loading.init">
        <a-form layout="vertical" :model="form" @finish="onSubmit">
          <a-form-item label="头像">
            <div class="avatar-row">
              <a-avatar :size="96" :src="form.userAvatar">
                <template #icon>
                  <UserOutlined />
                </template>
              </a-avatar>

              <a-upload
                :show-upload-list="false"
                :before-upload="beforeUpload"
                :custom-request="doUpload"
                accept="image/png,image/jpeg,image/jpg,image/gif,image/webp"
              >
                <a-button style="margin-left: 16px" :loading="loading.upload">重新上传</a-button>
              </a-upload>
            </div>

            <!-- ✅ 角色与到期信息 -->
            <div class="role-line">
              <span v-if="isVip" class="vip-badge">
                <CrownFilled class="vip-icon" /> VIP
              </span>
              <span v-if="isAdmin" class="admin-badge">
                <SecurityScanFilled class="admin-icon" /> 管理员
              </span>
              <span v-if="isVip && vipExpireDisplay" class="vip-expire">
                到期：{{ vipExpireDisplay }}（{{ vipRemainText }}）
              </span>
            </div>

            <div v-if="uploadHint" class="hint">{{ uploadHint }}</div>
          </a-form-item>

          <a-form-item label="登录账号">
            <a-input v-model:value="form.userAccount" disabled />
          </a-form-item>

          <a-form-item label="昵称">
            <a-input v-model:value="form.userName" maxlength="50" show-count placeholder="请输入昵称" />
          </a-form-item>

          <a-form-item label="个人简介">
            <a-textarea
              v-model:value="form.userProfile"
              :auto-size="{ minRows: 2, maxRows: 6 }"
              placeholder="简单介绍一下自己～"
            />
          </a-form-item>

          <a-space>
            <!-- 保存按钮禁用条件 -->
            <a-button type="primary" html-type="submit" :loading="loading.save" :disabled="!isDirty">
              保存
            </a-button>
            <a-button @click="resetForm" :disabled="loading.save || loading.upload">重置</a-button>
          </a-space>
        </a-form>
      </a-spin>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import { UserOutlined, CrownFilled, SecurityScanFilled } from '@ant-design/icons-vue'
import { uploadAvatarUsingPost, updateUserInfoUsingPost, getLoginUserUsingGet } from '@/api/userController'
import { useLoginUserStore } from '@/stores/useLoginUserStore.ts'

// （可选）如果你有全局登录用户 store，可以解开下面的引用，保存成功后刷新头像与昵称
const loginUserStore = useLoginUserStore()

type MeVO = {
  id?: number
  userAccount?: string
  userName?: string
  userAvatar?: string
  userProfile?: string
  userRole?: string
  vipExpireTime?: string | null
}

const form = reactive<Required<MeVO>>({
  id: 0,
  userAccount: '',
  userName: '',
  userAvatar: '',
  userProfile: '',
  userRole: 'user',
  vipExpireTime: '' as any,
})

const origin = reactive<MeVO>({})
const loading = reactive({ init: true, save: false, upload: false })
const uploadHint = ref<string>('支持 png / jpg / jpeg / webp，≤ 5MB')

// 是否有改动
const isDirty = computed(() =>
  form.userName !== (origin.userName || '') ||
  form.userProfile !== (origin.userProfile || '')
)

const fetchMe = async () => {
  loading.init = true
  let ok = false

  const res = await getLoginUserUsingGet()
  // 兼容不同返回壳：res.data.code === 0 且有 data
  const data = res?.data?.data ?? null
  if (res?.data?.code === 0 && data) {
    fillForm(data)
    ok = true
  }

  if (!ok) {
    message.error('获取个人信息失败!')
  }
  loading.init = false
}

const fillForm = (data: MeVO) => {
  form.id = Number(data.id || 0)
  form.userAccount = data.userAccount || ''
  form.userName = data.userName || ''
  form.userAvatar = data.userAvatar || ''
  form.userProfile = data.userProfile || ''
  form.userRole = data.userRole || 'user'
  form.vipExpireTime = (data.vipExpireTime as any) || ''
  Object.assign(origin, { ...form })
}

const resetForm = () => {
  Object.assign(form, { ...origin })
}

const onSubmit = async () => {
  if (!isDirty.value) {
    message.info('没有需要保存的改动')
    return
  }
  loading.save = true
  try {
    const res = await updateUserInfoUsingPost({
      userName: form.userName,
      userProfile: form.userProfile,
    })
    if (res.data.code === 0) {
      message.success('保存成功')
      Object.assign(origin, { ...form })
      // 同步更新全局 Header
      mergeLoginUser({
        userName: form.userName,
        userProfile: form.userProfile,
      })
    } else {
      message.error('保存失败：' + res.data.message)
    }
  } catch (e: any) {
    message.error('保存失败：' + e.message)
  } finally {
    loading.save = false
  }
}

// 前端兜底校验
const beforeUpload = (file: File) => {
  const okType = /image\/(png|jpeg|jpg|webp)/i.test(file.type)
  if (!okType) {
    message.error('仅支持 png / jpg / jpeg / webp')
    return false
  }
  const okSize = file.size / 1024 / 1024 <= 5
  if (!okSize) {
    message.error('图片不能超过 5MB')
    return false
  }
  return true
}

// 使用你提供的 uploadAvatarUsingPost
const doUpload = async ({ file, onSuccess, onError }: any) => {
  loading.upload = true
  try {
    const res = await uploadAvatarUsingPost({}, file)
    if (res.data.code === 0 && res.data.data) {
      // 防缓存：加时间戳
      const bust = `${res.data.data}${res.data.data.includes('?') ? '&' : '?'}t=${Date.now()}`
      form.userAvatar = bust
      message.success('头像已更新')
      // 同步更新全局 Header
      mergeLoginUser({ userAvatar: bust })
      onSuccess?.(res.data)
    } else {
      message.error('上传失败：' + res.data.message)
      onError?.(new Error(res.data.message))
    }
  } catch (e: any) {
    message.error('上传失败：' + e.message)
    onError?.(e)
  } finally {
    loading.upload = false
  }
}

onMounted(fetchMe)

// === 新增：VIP 展示的计算 ===
const isVip = computed(() => form.userRole === 'vip')
const isAdmin = computed(() => form.userRole === 'admin')

const vipExpireDisplay = computed(() => {
  if (!form.vipExpireTime) return ''
  // 简单格式化：YYYY-MM-DD HH:mm
  const d = new Date(form.vipExpireTime as any)
  if (isNaN(d.getTime())) return String(form.vipExpireTime)
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
})

const vipRemainText = computed(() => {
  if (!form.vipExpireTime) return ''
  const ts = new Date(form.vipExpireTime as any).getTime()
  if (Number.isNaN(ts)) return ''
  const diff = ts - Date.now()
  const days = Math.ceil(diff / (1000 * 60 * 60 * 24))
  if (days < 0) return '已过期'
  return `剩余 ${days} 天`
})

function mergeLoginUser(patch: Partial<API.LoginUserVO>) {
  const cur = (loginUserStore as any).loginUser || {}
  ;(loginUserStore as any).setLoginUser({ ...cur, ...patch })
}

</script>

<style scoped>
.userInfoPage {
  max-width: 720px;
  margin: 0 auto;
}
.avatar-row {
  display: flex;
  align-items: center;
}
.role-line {
  margin-top: 8px;
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 13px;
}
.vip-badge, .admin-badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}
.vip-icon {
  color: #faad14;
  font-size: 14px;
}
.admin-icon {
  color: #1890ff;
  font-size: 14px;
}
.vip-expire {
  color: #8c8c8c;
}
.hint {
  color: #8c8c8c;
  margin-top: 8px;
  font-size: 12px;
}
</style>
