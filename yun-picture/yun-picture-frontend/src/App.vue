<template>
  <div id="app">
    <a-config-provider :locale="zhCN">
      <BasicLayout />
    </a-config-provider>
  </div>
</template>

<script setup lang="ts">
import BasicLayout from '@/layouts/BasicLayout.vue'
// import { healthUsingGet } from '@/api/mainController.ts'
// import { useLoginUserStore } from '@/stores/useLoginUserStore.ts'

import zhCN from 'ant-design-vue/es/locale/zh_CN'
import dayjs from 'dayjs'
import 'dayjs/locale/zh-cn'  // 引入 dayjs 中文语言包

dayjs.locale('zh-cn') // 设置 dayjs 为中文

import { ConfigProvider } from 'ant-design-vue'

// 已经改为在权限校验中获取
// const loginUserStore = useLoginUserStore()
// loginUserStore.fetchLoginUser()
//
// healthUsingGet()
//   .then((res) => {
//     console.log('健康检查成功', res)
//   })
//   .catch((err) => {
//     console.error('健康检查失败', err)
//   })
</script>

<style scoped></style>
