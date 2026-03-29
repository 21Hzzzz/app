<script setup lang="ts">
const { user } = useAuth();
const {
  sellerSessions,
  sellerUnreadSessions,
  refreshing,
  loadError,
  refresh,
  sellerUnreadSessionCount,
  sellerTotalUnreadMessageCount,
} = useChatUnread();

const currentUserId = computed(() => user.value?.id ?? 0);
const totalSessionCount = computed(() => sellerSessions.value.length);

onMounted(() => {
  void refresh();
});
</script>

<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between gap-4">
      <div>
        <h1 class="text-2xl font-bold">收到的咨询</h1>
        <p class="mt-2 text-sm text-muted">
          查看你作为卖家收到的会话，以及买家还有哪些新消息未读
        </p>
      </div>

      <UButton
        icon="i-lucide-refresh-cw"
        variant="soft"
        :loading="refreshing"
        @click="refresh"
      >
        刷新
      </UButton>
    </div>

    <div
      v-if="loadError && !sellerSessions.length"
      class="rounded-xl border border-red-200 bg-red-50 px-4 py-10 text-center"
    >
      <p class="text-sm text-red-600">{{ loadError }}</p>
    </div>

    <template v-else>
      <div class="grid grid-cols-1 gap-4 md:grid-cols-3">
        <UCard class="rounded-2xl shadow-sm">
          <div class="text-sm text-muted">会话总数</div>
          <div class="mt-2 text-2xl font-bold">{{ totalSessionCount }}</div>
        </UCard>

        <UCard class="rounded-2xl shadow-sm">
          <div class="text-sm text-muted">未读会话数</div>
          <div class="mt-2 text-2xl font-bold">
            {{ sellerUnreadSessionCount }}
          </div>
        </UCard>

        <UCard class="rounded-2xl shadow-sm">
          <div class="text-sm text-muted">未读消息数</div>
          <div class="mt-2 text-2xl font-bold">
            {{ sellerTotalUnreadMessageCount }}
          </div>
        </UCard>
      </div>

      <ChatSessionSection
        title="未读会话"
        description="这些会话里有买家发来的新消息。"
        :sessions="sellerUnreadSessions"
        :current-user-id="currentUserId"
        empty-text="当前没有未读会话"
      />

      <ChatSessionSection
        title="会话列表"
        description="这里只展示你作为卖家参与的会话。"
        :sessions="sellerSessions"
        :current-user-id="currentUserId"
        empty-text="当前没有买家咨询"
      />
    </template>
  </div>
</template>
