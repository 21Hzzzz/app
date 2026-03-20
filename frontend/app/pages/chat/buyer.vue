<script setup lang="ts">
type BuyerChatSessionItem = {
  id: number;
  orderId: number;
  buyerId: number;
  buyerUsername?: string | null;
  sellerId: number;
  sellerUsername?: string | null;
  latestMessage?: string | null;
  latestMessageTime?: string | null;
  createTime: string;
};

type ApiResponse<T> = {
  code: number;
  message: string;
  data: T;
};

const api = useApi();
const { user } = useAuth();

const loading = ref(false);
const loadError = ref("");
const sessions = ref<BuyerChatSessionItem[]>([]);

function formatTime(time?: string | null) {
  if (!time) return "-";

  const d = new Date(time);

  if (Number.isNaN(d.getTime())) {
    return time;
  }

  return d.toLocaleString("zh-CN", {
    hour12: false,
  });
}

async function openSession(sessionId: number) {
  await navigateTo(`/chat/${sessionId}`);
}

async function fetchBuyerSessions() {
  if (!user.value) {
    loadError.value = "当前未登录";
    sessions.value = [];
    return;
  }

  loading.value = true;
  loadError.value = "";

  try {
    const res = await api<ApiResponse<BuyerChatSessionItem[]>>(
      "/api/chat/sessions/buyer",
      {
        method: "GET",
        query: {
          buyerId: user.value.id,
        },
      },
    );

    if (res.code !== 200) {
      throw new Error(res.message || "获取我发起的会话失败");
    }

    sessions.value = res.data || [];
  } catch (error) {
    sessions.value = [];
    loadError.value =
      error instanceof Error ? error.message : "获取我发起的会话失败";
  } finally {
    loading.value = false;
  }
}

const totalSessionCount = computed(() => sessions.value.length);

onMounted(() => {
  fetchBuyerSessions();
});
</script>

<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between gap-4">
      <div>
        <h1 class="text-2xl font-bold">我发起的</h1>
        <p class="mt-2 text-sm text-muted">查看你作为买家发起的聊天会话</p>
      </div>

      <UButton
        icon="i-lucide-refresh-cw"
        variant="soft"
        :loading="loading"
        @click="fetchBuyerSessions"
      >
        刷新
      </UButton>
    </div>

    <div class="grid grid-cols-1 gap-4 md:grid-cols-2">
      <UCard class="rounded-2xl shadow-sm">
        <div class="text-sm text-muted">会话数量</div>
        <div class="mt-2 text-2xl font-bold">{{ totalSessionCount }}</div>
      </UCard>

      <UCard class="rounded-2xl shadow-sm">
        <div class="text-sm text-muted">当前身份</div>
        <div class="mt-2 text-2xl font-bold">买家</div>
      </UCard>
    </div>

    <UCard class="rounded-2xl shadow-sm">
      <template #header>
        <div>
          <h2 class="text-lg font-semibold">会话列表</h2>
          <p class="mt-1 text-sm text-muted">按最近消息或创建时间展示</p>
        </div>
      </template>

      <div
        v-if="loading"
        class="rounded-xl border border-dashed border-default px-4 py-10 text-center text-sm text-muted"
      >
        正在加载会话列表...
      </div>

      <div
        v-else-if="loadError"
        class="rounded-xl border border-red-200 bg-red-50 px-4 py-10 text-center"
      >
        <p class="text-sm text-red-600">{{ loadError }}</p>
      </div>

      <div
        v-else-if="!sessions.length"
        class="rounded-xl border border-dashed border-default px-4 py-10 text-center text-sm text-muted"
      >
        你还没有发起过聊天会话
      </div>

      <div v-else class="space-y-4">
        <UCard
          v-for="session in sessions"
          :key="session.id"
          class="rounded-2xl transition hover:shadow-md"
        >
          <div class="flex flex-col gap-4">
            <div
              class="flex flex-col gap-3 md:flex-row md:items-start md:justify-between"
            >
              <div class="min-w-0">
                <div class="flex flex-wrap items-center gap-2">
                  <h3 class="text-base font-semibold">
                    会话 #{{ session.id }}
                  </h3>
                  <UBadge color="primary" variant="soft">买家发起</UBadge>
                </div>

                <p class="mt-2 text-sm text-muted">
                  订单号：#{{ session.orderId }}
                </p>

                <p class="mt-1 text-sm text-muted">
                  卖家：
                  {{ session.sellerUsername || `用户 ${session.sellerId}` }}
                </p>

                <p class="mt-1 text-sm text-muted">
                  创建时间：{{ formatTime(session.createTime) }}
                </p>
              </div>

              <div class="text-sm text-muted md:text-right">
                <div>最近消息时间</div>
                <div class="mt-1 font-medium text-foreground">
                  {{
                    formatTime(session.latestMessageTime || session.createTime)
                  }}
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
  </div>
</template>
