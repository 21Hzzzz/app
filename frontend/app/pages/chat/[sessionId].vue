<script setup lang="ts">
type ChatSessionDetail = {
  id: number;
  orderId: number;
  buyerId: number;
  buyerUsername?: string | null;
  sellerId: number;
  sellerUsername?: string | null;
  createTime: string;
};

type ChatMessage = {
  id: number;
  sessionId: number;
  senderId: number;
  senderUsername?: string | null;
  content: string;
  createTime: string;
};

type ApiResponse<T> = {
  code: number;
  message: string;
  data: T;
};

type SendMessageRequest = {
  sessionId: number;
  senderId: number;
  content: string;
};

const route = useRoute();
const api = useApi();
const { user } = useAuth();
const { markSessionRead } = useChatUnread();

const sessionDetail = ref<ChatSessionDetail | null>(null);
const messages = ref<ChatMessage[]>([]);

const loading = ref(false);
const loadError = ref("");

const sending = ref(false);
const sendError = ref("");
const messageText = ref("");

const messageListRef = ref<HTMLElement | null>(null);
const pollingTimer = ref<ReturnType<typeof setInterval> | null>(null);
const polling = ref(false);

const sessionId = computed(() => {
  const raw = route.params.sessionId;
  const value = Array.isArray(raw) ? raw[0] : raw;
  const id = Number(value);

  return Number.isInteger(id) && id > 0 ? id : 0;
});

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

function clearSendError() {
  sendError.value = "";
}

function getLatestMessageId(messageList: ChatMessage[] = messages.value) {
  const latestMessage = messageList[messageList.length - 1];
  return latestMessage?.id ?? null;
}

function isMine(message: ChatMessage) {
  if (!user.value) return false;
  return message.senderId === user.value.id;
}

function getMyRole() {
  if (!user.value || !sessionDetail.value) return "-";

  if (sessionDetail.value.buyerId === user.value.id) return "买家";
  if (sessionDetail.value.sellerId === user.value.id) return "卖家";

  return "-";
}

function getOtherRole() {
  if (!user.value || !sessionDetail.value) return "-";

  if (sessionDetail.value.buyerId === user.value.id) return "卖家";
  if (sessionDetail.value.sellerId === user.value.id) return "买家";

  return "-";
}

function getOtherUserName() {
  if (!user.value || !sessionDetail.value) return "-";

  if (sessionDetail.value.buyerId === user.value.id) {
    return (
      sessionDetail.value.sellerUsername ||
      `用户 ${sessionDetail.value.sellerId}`
    );
  }

  if (sessionDetail.value.sellerId === user.value.id) {
    return (
      sessionDetail.value.buyerUsername || `用户 ${sessionDetail.value.buyerId}`
    );
  }

  return "-";
}

function getSessionBadgeColor() {
  if (!user.value || !sessionDetail.value) return "neutral";

  if (sessionDetail.value.buyerId === user.value.id) return "primary";
  if (sessionDetail.value.sellerId === user.value.id) return "success";

  return "neutral";
}

function getSessionBadgeLabel() {
  if (!user.value || !sessionDetail.value) return "未知身份";

  if (sessionDetail.value.buyerId === user.value.id) return "我是买家";
  if (sessionDetail.value.sellerId === user.value.id) return "我是卖家";

  return "无权限";
}

function isNearBottom() {
  const el = messageListRef.value;
  if (!el) return true;

  return el.scrollHeight - el.scrollTop - el.clientHeight < 80;
}

async function scrollToBottom() {
  await nextTick();

  if (!messageListRef.value) return;

  messageListRef.value.scrollTop = messageListRef.value.scrollHeight;
}

function stopPolling() {
  if (pollingTimer.value) {
    clearInterval(pollingTimer.value);
    pollingTimer.value = null;
  }
}

function startPolling() {
  stopPolling();

  pollingTimer.value = setInterval(() => {
    void refreshMessagesSilently();
  }, 1000);
}

async function fetchSessionDetail() {
  if (!user.value) {
    throw new Error("当前未登录");
  }

  if (!sessionId.value) {
    throw new Error("会话ID无效");
  }

  const res = await api<ApiResponse<ChatSessionDetail>>(
    `/api/chat/sessions/${sessionId.value}`,
    {
      method: "GET",
      query: {
        userId: user.value.id,
      },
    },
  );

  if (res.code !== 200 || !res.data) {
    throw new Error(res.message || "获取会话详情失败");
  }

  sessionDetail.value = res.data;
}

async function fetchMessages() {
  if (!user.value) {
    throw new Error("当前未登录");
  }

  if (!sessionId.value) {
    throw new Error("会话ID无效");
  }

  const res = await api<ApiResponse<ChatMessage[]>>("/api/chat/messages", {
    method: "GET",
    query: {
      sessionId: sessionId.value,
      userId: user.value.id,
    },
  });

  if (res.code !== 200) {
    throw new Error(res.message || "获取消息列表失败");
  }

  messages.value = res.data || [];
  await markSessionRead(sessionId.value, getLatestMessageId(messages.value));
}

async function refreshMessagesSilently() {
  if (polling.value || loading.value || sending.value) return;
  if (!user.value || !sessionId.value || !sessionDetail.value) return;

  polling.value = true;

  try {
    const shouldScroll = isNearBottom();
    const prevLength = messages.value.length;
    const prevLastId = getLatestMessageId(messages.value) ?? 0;

    await fetchMessages();

    const nextLength = messages.value.length;
    const nextLastId = getLatestMessageId(messages.value) ?? 0;

    if (
      shouldScroll ||
      nextLastId !== prevLastId ||
      nextLength !== prevLength
    ) {
      await scrollToBottom();
    }
  } catch {
  } finally {
    polling.value = false;
  }
}

async function fetchChatDetail() {
  loading.value = true;
  loadError.value = "";
  stopPolling();

  try {
    await Promise.all([fetchSessionDetail(), fetchMessages()]);
    await scrollToBottom();
    startPolling();
  } catch (error) {
    sessionDetail.value = null;
    messages.value = [];
    loadError.value =
      error instanceof Error ? error.message : "获取会话详情失败";
  } finally {
    loading.value = false;
  }
}

const canSend = computed(() => {
  return !!messageText.value.trim() && !!user.value && !!sessionDetail.value;
});

async function sendMessage() {
  if (!user.value) {
    await navigateTo("/login");
    return;
  }

  if (!sessionDetail.value || !sessionId.value || sending.value) return;

  const content = messageText.value.trim();

  if (!content) {
    sendError.value = "消息内容不能为空";
    return;
  }

  sending.value = true;
  sendError.value = "";

  try {
    const payload: SendMessageRequest = {
      sessionId: sessionId.value,
      senderId: user.value.id,
      content,
    };

    const res = await api<ApiResponse<null>>("/api/chat/messages", {
      method: "POST",
      body: payload,
    });

    if (res.code !== 200) {
      throw new Error(res.message || "发送消息失败");
    }

    messageText.value = "";
    await fetchMessages();
    await scrollToBottom();
  } catch (error) {
    sendError.value = error instanceof Error ? error.message : "发送消息失败";
  } finally {
    sending.value = false;
  }
}

watch(
  () => route.params.sessionId,
  () => {
    fetchChatDetail();
  },
  { immediate: true },
);

onBeforeUnmount(() => {
  stopPolling();
});
</script>

<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between gap-4">
      <div>
        <h1 class="text-2xl font-bold">会话详情</h1>
        <p class="mt-2 text-sm text-muted">
          查看当前会话消息，并向对方发送新消息
        </p>
      </div>

      <div class="flex items-center gap-2">
        <UButton
          icon="i-lucide-arrow-left"
          variant="soft"
          @click="navigateTo('/chat/all')"
        >
          返回列表
        </UButton>

        <UButton
          icon="i-lucide-refresh-cw"
          variant="soft"
          :loading="loading"
          @click="fetchChatDetail"
        >
          刷新
        </UButton>
      </div>
    </div>

    <div
      v-if="loading"
      class="rounded-xl border border-dashed border-default px-4 py-10 text-center text-sm text-muted"
    >
      正在加载会话详情...
    </div>

    <div
      v-else-if="loadError"
      class="rounded-xl border border-red-200 bg-red-50 px-4 py-10 text-center"
    >
      <p class="text-sm text-red-600">{{ loadError }}</p>
    </div>

    <template v-else-if="sessionDetail">
      <div class="grid grid-cols-1 gap-4 md:grid-cols-3">
        <UCard class="rounded-2xl shadow-sm">
          <div class="flex items-center gap-2">
            <div class="text-sm text-muted">会话编号</div>
            <UBadge :color="getSessionBadgeColor()" variant="soft">
              {{ getSessionBadgeLabel() }}
            </UBadge>
          </div>
          <div class="mt-2 text-2xl font-bold">#{{ sessionDetail.id }}</div>
        </UCard>

        <UCard class="rounded-2xl shadow-sm">
          <div class="text-sm text-muted">关联订单</div>
          <div class="mt-2 text-2xl font-bold">
            #{{ sessionDetail.orderId }}
          </div>
        </UCard>

        <UCard class="rounded-2xl shadow-sm">
          <div class="text-sm text-muted">消息数量</div>
          <div class="mt-2 text-2xl font-bold">{{ messages.length }}</div>
        </UCard>
      </div>

      <UCard class="rounded-2xl shadow-sm">
        <template #header>
          <div
            class="flex flex-col gap-3 md:flex-row md:items-start md:justify-between"
          >
            <div>
              <h2 class="text-lg font-semibold">会话信息</h2>
              <p class="mt-1 text-sm text-muted">
                创建时间：{{ formatTime(sessionDetail.createTime) }}
              </p>
            </div>

            <div class="text-sm text-muted md:text-right">
              <p>我的身份：{{ getMyRole() }}</p>
              <p class="mt-1">对方身份：{{ getOtherRole() }}</p>
              <p class="mt-1">对方用户：{{ getOtherUserName() }}</p>
            </div>
          </div>
        </template>

        <div
          ref="messageListRef"
          class="max-h-[480px] space-y-4 overflow-y-auto rounded-xl bg-muted/30 p-4"
        >
          <div
            v-if="!messages.length"
            class="rounded-xl border border-dashed border-default px-4 py-10 text-center text-sm text-muted"
          >
            暂无消息记录
          </div>

          <div
            v-for="message in messages"
            :key="message.id"
            class="flex"
            :class="isMine(message) ? 'justify-end' : 'justify-start'"
          >
            <div
              class="max-w-[85%] rounded-2xl px-4 py-3 shadow-sm"
              :class="
                isMine(message)
                  ? 'bg-primary text-primary-foreground'
                  : 'border border-default bg-default'
              "
            >
              <div
                class="flex flex-wrap items-center gap-2 text-xs"
                :class="
                  isMine(message) ? 'text-primary-foreground/80' : 'text-muted'
                "
              >
                <span>
                  {{ message.senderUsername || `用户 ${message.senderId}` }}
                </span>
                <span>·</span>
                <span>{{ formatTime(message.createTime) }}</span>
              </div>

              <div class="mt-2 break-words text-sm leading-6">
                {{ message.content }}
              </div>
            </div>
          </div>
        </div>
      </UCard>

      <UCard class="rounded-2xl shadow-sm">
        <template #header>
          <div>
            <h2 class="text-lg font-semibold">发送消息</h2>
            <p class="mt-1 text-sm text-muted">输入内容后发送给当前会话对方</p>
          </div>
        </template>

        <div class="space-y-4">
          <UTextarea
            v-model="messageText"
            :rows="5"
            autoresize
            placeholder="请输入消息内容"
            @input="clearSendError"
          />

          <div
            v-if="sendError"
            class="rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-600"
          >
            {{ sendError }}
          </div>

          <div class="flex items-center justify-end">
            <UButton
              icon="i-lucide-send"
              :loading="sending"
              :disabled="!canSend"
              @click="sendMessage"
            >
              发送消息
            </UButton>
          </div>
        </div>
      </UCard>
    </template>
  </div>
</template>
