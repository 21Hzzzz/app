<script setup lang="ts">
import type { NavigationMenuItem } from "@nuxt/ui";

const items = computed<NavigationMenuItem[][]>(() => [
  [
    {
      label: "我发布的",
      icon: "i-lucide-package-plus",
      to: "/profile/published",
    },
    {
      label: "我卖出的",
      icon: "i-lucide-badge-dollar-sign",
      to: "/profile/sold",
    },
    {
      label: "我买到的",
      icon: "i-lucide-shopping-bag",
      to: "/profile/bought",
    },
  ],
]);
</script>

<template>
  <div class="min-h-[calc(100vh-var(--ui-header-height))] bg-muted/30">
    <div class="mx-auto flex max-w-7xl gap-6 p-6">
      <!-- 左侧导航 -->
      <aside class="w-64 shrink-0">
        <div
          class="overflow-hidden rounded-2xl border border-default bg-default shadow-sm"
        >
          <div class="border-b border-default px-4 py-4">
            <h2 class="text-base font-semibold">我的中心</h2>
            <p class="mt-1 text-sm text-muted">管理你发布、卖出和买到的商品</p>
          </div>

          <div class="p-3">
            <UNavigationMenu
              :items="items"
              orientation="vertical"
              :ui="{
                root: 'w-full',
                list: 'gap-1',
                link: 'rounded-xl px-3 py-2.5 text-sm font-medium transition-colors',
                linkLeadingIcon: 'size-4',
                linkTrailingBadge:
                  'rounded-full bg-primary/10 px-2 py-0.5 text-xs text-primary',
              }"
            />
          </div>
        </div>
      </aside>

      <!-- 右侧内容 -->
      <section class="min-w-0 flex-1">
        <div
          class="min-h-150 rounded-2xl border border-default bg-default p-6 shadow-sm"
        >
          <NuxtPage />
        </div>
      </section>
    </div>
  </div>
</template>
