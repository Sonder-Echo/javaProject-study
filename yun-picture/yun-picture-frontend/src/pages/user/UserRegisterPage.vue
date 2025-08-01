<template>
  <div id="UserRegisterPage">
    <h2 class="title">云图库平台 - 用户注册</h2>
    <div class="desc">企业级智能协同云图库</div>
    <a-form :model="formState" name="basic" autocomplete="off" @finish="handleSubmit">
      <a-form-item name="userAccount" :rules="[{ required: true }]">
        <a-input v-model:value="formState.userAccount" placeholder="请输入账号" />
      </a-form-item>

      <a-form-item
        name="userPassword"
        :rules="[
          { required: true, message: '请输入密码!' },
          { min: 8, message: '密码长度不能小于8位' },
        ]"
      >
        <a-input-password v-model:value="formState.userPassword" placeholder="请输入密码" />
      </a-form-item>

      <a-form-item
        name="checkPassword"
        :rules="[
          { required: true, message: '请输入确认密码!' },
          { min: 8, message: '确认密码长度不能小于8位' },
        ]"
      >
        <a-input-password v-model:value="formState.checkPassword" placeholder="请输入确认密码" />
      </a-form-item>

      <div class="tips">
        已有账号？
        <router-link to="/user/login">点击登录</router-link>
      </div>

      <a-form-item>
        <a-button type="primary" html-type="submit" style="width: 100%">注册</a-button>
      </a-form-item>
    </a-form>
  </div>
</template>

<script lang="ts" setup>
import { reactive } from 'vue'
import { userLoginUsingPost, userRegisterUsingPost } from '@/api/userController.ts'
import { useLoginUserStore } from '@/stores/useLoginUserStore.ts'
import { message } from 'ant-design-vue'
import router from '@/router'

// 用于接收表单输入数据
const formState = reactive<API.UserRegisterRequest>({
  userAccount: '',
  userPassword: '',
  checkPassword: '',
})

const loginUserStore = useLoginUserStore()

/**
 * 提交表单
 * @param values
 */
const handleSubmit = async (values: any) => {
  // 验证两次密码是否一致
  if (values.userPassword !== values.checkPassword) {
    message.error('两次输入密码不一致');
    return;
  }
  try {
    const res = await userRegisterUsingPost(values)
    // 判断注册成功
    if (res.data.code === 0 && res.data.data) {
      // 注册成功, 跳转到登录页
      message.success('注册成功')
      router.push({
        path: '/user/login',
        replace: true,
      })
    } else {
      // 注册失败
      message.error('注册失败，' + res.data.message)
    }
  } catch (e) {
    message.error('注册失败，' + e.message)
  }
}
</script>

<style scoped>
#UserRegisterPage {
  max-width: 360px;
  margin: 0 auto;
}

.title {
  text-align: center;
  margin-bottom: 16px;
}

.desc {
  color: #bbb;
  text-align: center;
  margin-bottom: 32px;
}

.tips {
  color: #bbb;
  text-align: right;
  margin-top: 8px;
  margin-bottom: 16px;
  font-size: 13px;
}
</style>
