<script lang="ts" setup>
import type { NavigationMenuItem } from "@nuxt/ui";

const items = computed<NavigationMenuItem[]>(() => [
  {
    label: "首页",
    to: "/",
    icon: "i-lucide-house",
  },
  {
    label: "柜子",
    to: "/cabinet",
    icon: "i-lucide-package-open",
  },
  {
    label: "我的",
    to: "/profile/published",
    icon: "i-lucide-user",
  },
  {
    label: "消息",
    to: "/chat/all",
    icon: "i-lucide-message-circle",
  },
  {
    label: "后台",
    to: "/admin/dashboard",
    icon: "i-lucide-shield",
  },
]);

const { username, logout } = useAuth();

const handleLogout = async () => {
  logout();
  await navigateTo("/login");
};
</script>

<template>
  <UHeader
    title=""
    :ui="{
      root: 'border-b border-default bg-default/80 backdrop-blur',
      container:
        'mx-auto flex h-(--ui-header-height) max-w-7xl items-center gap-4 px-4 sm:px-6 lg:px-8',
      left: 'flex items-center gap-6',
      center: 'min-w-0 flex-1',
      right: 'flex items-center gap-2 sm:gap-3',
    }"
  >
    <template #title> </template>
    <template #left>
      <NuxtLink to="/" class="flex items-center gap-3 shrink-0">
        <div
          class="flex h-9 w-9 items-center justify-center rounded-xl bg-primary text-primary-foreground shadow-sm"
        >
          <UIcon name="i-lucide-store" class="size-5" />
        </div>

        <div class="hidden sm:block">
          <p class="text-sm font-semibold leading-none">宿舍零食柜</p>
          <p class="mt-1 text-xs text-muted">Dorm Snack Cabinet</p>
        </div>
      </NuxtLink>
    </template>

    <template #default>
      <div class="flex items-center justify-center">
        <UNavigationMenu
          :items="items"
          :ui="{
            root: 'w-auto',
            list: 'gap-1',
            link: 'rounded-xl px-3 py-2 text-sm font-medium transition-colors',
          }"
        />
      </div>
    </template>

    <template #right>
      <div
        class="hidden sm:flex items-center gap-3 rounded-xl border border-default bg-elevated/50 px-3 py-2"
      >
        <UAvatar
          size="sm"
          icon="i-lucide-user"
          :ui="{
            root: 'bg-primary/10 text-primary',
          }"
        />
        <div class="max-w-32">
          <p class="truncate text-sm font-medium">
            {{ username || "用户" }}
          </p>
        </div>
      </div>

      <UButton
        color="neutral"
        variant="soft"
        icon="i-lucide-log-out"
        class="rounded-xl"
        @click="handleLogout"
      >
        <span class="hidden sm:inline">退出登录</span>
      </UButton>

      <UColorModeButton color="neutral" variant="ghost" class="rounded-xl" />
    </template>

    <template #body>
      <UNavigationMenu :items="items" orientation="vertical" />
    </template>
  </UHeader>

  <UMain>
    <slot />
  </UMain>
</template>
