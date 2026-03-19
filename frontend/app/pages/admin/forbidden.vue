<script setup lang="ts"></script>

<template>
  <div class="min-h-screen flex items-center justify-center px-4">
    <UCard class="w-full max-w-lg">
      <template #header>
        <div class="text-center">
          <h1 class="text-2xl font-bold">无权访问后台</h1>
          <p class="mt-2 text-sm text-muted">
            你已经登录，但当前账号不是管理员，无法进入后台页面。
          </p>
        </div>
      </template>

      <div class="space-y-3 text-sm text-muted text-center">
        <p>如果你认为这是权限配置错误，请联系管理员处理。</p>
      </div>

      <template #footer>
        <div class="flex items-center justify-center gap-3">
          <UButton color="neutral" variant="soft" to="/"> 返回首页 </UButton>
          <UButton to="/profile"> 去个人中心 </UButton>
        </div>
      </template>
    </UCard>
  </div>
</template>
