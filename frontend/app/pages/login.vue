<script setup lang="ts">
definePageMeta({
  layout: false,
});

import * as z from "zod";
import type { FormSubmitEvent } from "@nuxt/ui";
import type { UserInfo } from "~/composables/useAuth";

interface LoginResult {
  id: number;
  username: string;
  role: UserInfo["role"];
}

interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
}

const api = useApi();
const { login } = useAuth();

const schema = z.object({
  username: z
    .string()
    .min(3, "账号至少 3 个字符")
    .max(20, "账号最多 20 个字符"),
  password: z
    .string()
    .min(6, "密码至少 6 个字符")
    .max(20, "密码最多 20 个字符"),
});

type Schema = z.output<typeof schema>;

const state = reactive<Schema>({
  username: "",
  password: "",
});

const loading = ref(false);
const errorMessage = ref("");

async function onSubmit(event: FormSubmitEvent<Schema>) {
  try {
    loading.value = true;
    errorMessage.value = "";

    const res = await api<ApiResponse<LoginResult>>("/api/auth/login", {
      method: "POST",
      body: {
        username: event.data.username,
        password: event.data.password,
      },
    });

    if (res.code !== 200 || !res.data) {
      throw new Error(res.message || "登录失败，请重试");
    }

    login({
      id: res.data.id,
      username: res.data.username,
      role: res.data.role,
    });

    await navigateTo("/");
  } catch (error: any) {
    console.error("登录失败:", error);
    errorMessage.value =
      error?.data?.message || error?.message || "登录失败，请重试";
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
          <h1 class="text-2xl font-bold">登录</h1>
          <p class="mt-1 text-sm text-muted">请输入账号和密码登录</p>
        </div>
      </template>

      <UAlert
        v-if="errorMessage"
        color="error"
        variant="soft"
        :title="errorMessage"
        class="mb-4"
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

        <UButton type="submit" block :loading="loading">登录</UButton>
      </UForm>

      <template #footer>
        <div class="text-center text-sm text-muted">
          还没有账号？
          <NuxtLink to="/register" class="text-primary font-medium">
            去注册
          </NuxtLink>
        </div>
      </template>
    </UCard>
  </div>
</template>
