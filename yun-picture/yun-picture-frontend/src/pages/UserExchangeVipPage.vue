<template>
  <div id="vipExchangePage">
    <h2 style="margin-bottom: 16px">会员码兑换</h2>
    <!-- 兑换码表单 -->
    <a-form name="exchangeForm" layout="vertical" :model="formData" @finish="handleSubmit">
      <a-form-item
        name="vipCode"
        label="兑换码"
        :rules="[{ required: true, message: '请输入有效的会员兑换码' }]"
      >
        <a-input
          v-model:value="formData.vipCode"
          placeholder="请输入会员兑换码"
          allow-clear
          :maxlength="20"
        />
      </a-form-item>
      <a-form-item>
        <a-button
          type="primary"
          html-type="submit"
          style="width: 100%"
          :loading="loading"
          :disabled="!formData.vipCode"
        >
          立即兑换
        </a-button>
      </a-form-item>
    </a-form>

    <!-- 兑换说明 -->
    <a-card style="margin-top: 24px" title="兑换说明">
      <p>1. 请输入有效的会员兑换码，兑换码通常由字母和数字组成</p>
      <p>2. 每个兑换码只能使用一次，成功兑换后将获得相应会员权益</p>
      <p>3. 兑换成功后，会员权益将立即生效</p>
      <p>4. 如有问题，请联系客服支持</p>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import { exchangeVipUsingPost } from '@/api/userController.ts'

const formData = reactive({
  vipCode: ''
})

// 提交任务状态
const loading = ref(false)

const router = useRouter()

/**
 * 提交表单
 */
const handleSubmit = async () => {
  loading.value = true
  try {
    const res = await exchangeVipUsingPost({
      vipCode: formData.vipCode
    })

    // 操作成功
    if (res.data.code === 0 && res.data.data) {
      message.success('兑换成功！会员权益已生效')
      // 可以跳转到用户中心或首页
      setTimeout(() => {
        router.push({
          path: `/`,
        })
      }, 1500)
    } else {
      // 兑换失败
      message.error('兑换失败: ' + res.data.message)
    }
    loading.value = false
  } catch (e) {
    message.error('兑换失败，请稍后重试: ' + e.message)
    loading.value = false
  }
}
</script>

<style scoped>
#vipExchangePage {
  max-width: 480px;
  margin: 0 auto;
  padding: 24px;
}
</style>
