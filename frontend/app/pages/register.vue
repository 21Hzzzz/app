<script setup lang="ts">
definePageMeta({
  layout: false,
});

import * as z from "zod";
import type { FormSubmitEvent } from "@nuxt/ui";

const api = useApi();

const schema = z
  .object({
    username: z
      .string()
      .min(3, "账号至少 3 个字符")
      .max(20, "账号最多 20 个字符"),
    password: z
      .string()
      .min(6, "密码至少 6 个字符")
      .max(20, "密码最多 20 个字符"),
    confirmPassword: z
      .string()
      .min(6, "确认密码至少 6 个字符")
      .max(20, "确认密码最多 20 个字符"),
    role: z.enum(["user", "admin"], {
      error: "请选择角色",
    }),
  })
  .refine((data) => data.password === data.confirmPassword, {
    message: "两次输入的密码不一致",
    path: ["confirmPassword"],
  });

type Schema = z.infer<typeof schema>;

interface ApiResponse {
  code: number;
  message: string;
}

const state = reactive<Schema>({
  username: "",
  password: "",
  confirmPassword: "",
  role: "user",
});

const loading = ref(false);
const errorMsg = ref("");
const successMsg = ref("");

async function onSubmit(event: FormSubmitEvent<Schema>) {
  try {
    loading.value = true;
    errorMsg.value = "";
    successMsg.value = "";

    const res = await api<ApiResponse>("/api/auth/register", {
      method: "POST",
      body: {
        username: event.data.username,
        password: event.data.password,
        role: event.data.role,
      },
    });

    if (res.code !== 200) {
      throw new Error(res.message || "注册失败，请稍后重试");
    }

    successMsg.value = res.message || "注册成功";

    setTimeout(() => {
      navigateTo("/login");
    }, 1500);
  } catch (error: any) {
    errorMsg.value =
      error?.data?.message || error?.message || "注册失败，请稍后重试";
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <div class="flex min-h-screen items-center justify-center px-4">
    <UCard class="w-full max-w-md">
      <template #header>
        <div class="text-center">
          <h1 class="text-2xl font-bold">注册</h1>
          <p class="mt-1 text-sm text-muted">请输入账号和密码完成注册</p>
        </div>
      </template>

      <div class="space-y-4">
        <UAlert
          v-if="errorMsg"
          color="error"
          variant="soft"
          :title="errorMsg"
        />

        <UAlert
          v-if="successMsg"
          color="success"
          variant="soft"
          :title="successMsg"
        />

        <UForm
          :schema="schema"
          :state="state"
          class="space-y-4"
          @submit="onSubmit"
        >
          <UFormField label="账号" name="username">
            <UInput
              v-model="state.username"
              placeholder="请输入账号"
              class="w-full"
            />
          </UFormField>

          <UFormField label="密码" name="password">
            <UInput
              v-model="state.password"
              type="password"
              placeholder="请输入密码"
              class="w-full"
            />
          </UFormField>

          <UFormField label="确认密码" name="confirmPassword">
            <UInput
              v-model="state.confirmPassword"
              type="password"
              placeholder="请再次输入密码"
              class="w-full"
            />
          </UFormField>

          <UFormField label="角色" name="role">
            <URadioGroup
              v-model="state.role"
              :items="[
                { label: '用户', value: 'user' },
                { label: '管理员', value: 'admin' },
              ]"
              orientation="horizontal"
              class="w-full"
            />
          </UFormField>

          <UButton type="submit" block :loading="loading">注册</UButton>
        </UForm>
      </div>

      <template #footer>
        <div class="text-center text-sm text-muted">
          已有账号？
          <NuxtLink to="/login" class="text-primary font-medium">
            去登录
          </NuxtLink>
        </div>
      </template>
    </UCard>
  </div>
</template>
