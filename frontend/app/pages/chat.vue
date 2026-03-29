<script setup lang="ts">
import type { NavigationMenuItem } from "@nuxt/ui";

type ChatNavigationMenuItem = NavigationMenuItem & {
  badge?: string | number;
};

const {
  totalUnreadMessageCount,
  buyerTotalUnreadMessageCount,
  sellerTotalUnreadMessageCount,
} = useChatUnread();

const items = computed<ChatNavigationMenuItem[][]>(() => [
  [
    {
      label: "全部会话",
      icon: "i-lucide-messages-square",
      to: "/chat/all",
      badge:
        totalUnreadMessageCount.value > 0
          ? String(totalUnreadMessageCount.value)
          : undefined,
    },
    {
      label: "我发起的",
      icon: "i-lucide-message-circle-more",
      to: "/chat/buyer",
      badge:
        buyerTotalUnreadMessageCount.value > 0
          ? String(buyerTotalUnreadMessageCount.value)
          : undefined,
    },
    {
      label: "收到的咨询",
      icon: "i-lucide-message-square-reply",
      to: "/chat/seller",
      badge:
        sellerTotalUnreadMessageCount.value > 0
          ? String(sellerTotalUnreadMessageCount.value)
          : undefined,
    },
  ],
]);
</script>

<template>
  <div class="min-h-[calc(100vh-var(--ui-header-height))] bg-muted/30">
    <div class="mx-auto flex max-w-7xl gap-6 p-6">
      <aside class="w-64 shrink-0">
        <div
          class="overflow-hidden rounded-2xl border border-default bg-default shadow-sm"
        >
          <div class="border-b border-default px-4 py-4">
            <h2 class="text-base font-semibold">聊天中心</h2>
            <p class="mt-1 text-sm text-muted">
              查看哪些会话未读，以及每个会话还有多少条新消息
            </p>
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

      <section class="min-w-0 flex-1">
        <div
          class="min-h-[150px] rounded-2xl border border-default bg-default p-6 shadow-sm"
        >
          <NuxtPage />
        </div>
      </section>
    </div>
  </div>
</template>
