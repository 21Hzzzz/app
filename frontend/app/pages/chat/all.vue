<script setup lang="ts">
const { user } = useAuth();
const {
  allSessions,
  unreadSessions,
  refreshing,
  loadError,
  refresh,
  unreadSessionCount: totalUnreadSessionCount,
  totalUnreadMessageCount,
} = useChatUnread();

const currentUserId = computed(() => user.value?.id ?? 0);
const totalSessionCount = computed(() => allSessions.value.length);

onMounted(() => {
  void refresh();
});
</script>

<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between gap-4">
      <div>
        <h1 class="text-2xl font-bold">全部会话</h1>
        <p class="mt-2 text-sm text-muted">
          查看你参与的全部聊天会话，以及哪些会话还有未读消息
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
      v-if="loadError && !allSessions.length"
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
          <div class="mt-2 text-2xl font-bold">{{ totalUnreadSessionCount }}</div>
        </UCard>

        <UCard class="rounded-2xl shadow-sm">
          <div class="text-sm text-muted">未读消息数</div>
          <div class="mt-2 text-2xl font-bold">
            {{ totalUnreadMessageCount }}
          </div>
        </UCard>
      </div>

      <ChatSessionSection
        title="未读会话"
        description="这些会话里有来自对方的新消息，进入会话后会立即标记为已读。"
        :sessions="unreadSessions"
        :current-user-id="currentUserId"
        empty-text="当前没有未读会话"
      />

      <ChatSessionSection
        title="会话列表"
        description="按最近消息时间排序展示全部会话，未读会话会额外高亮。"
        :sessions="allSessions"
        :current-user-id="currentUserId"
        empty-text="暂无会话记录"
      />
    </template>
  </div>
</template>
