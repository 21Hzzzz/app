<script setup lang="ts">
import type { ChatUnreadSession } from "~/composables/useChatUnread";

const props = defineProps<{
  title: string;
  description: string;
  sessions: ChatUnreadSession[];
  currentUserId: number;
  emptyText: string;
}>();

function formatTime(time?: string | null) {
  if (!time) return "-";

  const date = new Date(time);

  if (Number.isNaN(date.getTime())) {
    return time;
  }

  return date.toLocaleString("zh-CN", {
    hour12: false,
  });
}

function getSessionType(session: ChatUnreadSession) {
  if (!props.currentUserId) return "未知身份";

  return session.buyerId === props.currentUserId ? "我发起的" : "收到的咨询";
}

function getSessionTypeColor(session: ChatUnreadSession) {
  if (!props.currentUserId) return "neutral";

  return session.buyerId === props.currentUserId ? "primary" : "success";
}

function getCounterpartName(session: ChatUnreadSession) {
  if (!props.currentUserId) return "-";

  if (session.buyerId === props.currentUserId) {
    return session.sellerUsername || `用户 ${session.sellerId}`;
  }

  return session.buyerUsername || `用户 ${session.buyerId}`;
}

function getCounterpartRole(session: ChatUnreadSession) {
  if (!props.currentUserId) return "-";

  return session.buyerId === props.currentUserId ? "卖家" : "买家";
}

async function openSession(sessionId: number) {
  await navigateTo(`/chat/${sessionId}`);
}
</script>

<template>
  <UCard class="rounded-2xl shadow-sm">
    <template #header>
      <div>
        <h2 class="text-lg font-semibold">{{ title }}</h2>
        <p class="mt-1 text-sm text-muted">{{ description }}</p>
      </div>
    </template>

    <div
      v-if="!sessions.length"
      class="rounded-xl border border-dashed border-default px-4 py-10 text-center text-sm text-muted"
    >
      {{ emptyText }}
    </div>

    <div v-else class="space-y-4">
      <UCard
        v-for="session in sessions"
        :key="session.id"
        class="rounded-2xl shadow-sm transition hover:shadow-md"
        :ui="{
          root:
            (session.unreadCount || 0) > 0
              ? 'border border-error/30 bg-error/5'
              : '',
        }"
      >
        <div class="flex flex-col gap-4">
          <div
            class="flex flex-col gap-3 md:flex-row md:items-start md:justify-between"
          >
            <div class="min-w-0">
              <div class="flex flex-wrap items-center gap-2">
                <h3 class="text-base font-semibold">会话 #{{ session.id }}</h3>

                <UBadge :color="getSessionTypeColor(session)" variant="soft">
                  {{ getSessionType(session) }}
                </UBadge>

                <UBadge
                  v-if="(session.unreadCount || 0) > 0"
                  color="error"
                  variant="soft"
                >
                  {{ `未读 ${session.unreadCount} 条` }}
                </UBadge>
              </div>

              <p class="mt-2 text-sm text-muted">订单号：#{{ session.orderId }}</p>

              <p class="mt-1 text-sm text-muted">
                对方身份：{{ getCounterpartRole(session) }}
              </p>

              <p class="mt-1 text-sm text-muted">
                对方用户：{{ getCounterpartName(session) }}
              </p>

              <p class="mt-1 text-sm text-muted">
                创建时间：{{ formatTime(session.createTime) }}
              </p>
            </div>

            <div class="text-sm text-muted md:text-right">
              <div>最近消息时间</div>
              <div class="mt-1 font-medium text-foreground">
                {{ formatTime(session.latestMessageTime || session.createTime) }}
              </div>
            </div>
          </div>

          <div class="rounded-xl bg-muted/50 px-4 py-3">
            <div class="text-sm text-muted">最近一条消息</div>
            <div class="mt-1 break-all text-sm">
              {{ session.latestMessage || "暂无消息内容" }}
            </div>
          </div>

          <div class="flex justify-end">
            <UButton
              icon="i-lucide-arrow-right"
              @click="openSession(session.id)"
            >
              进入会话
            </UButton>
          </div>
        </div>
      </UCard>
    </div>
  </UCard>
</template>
