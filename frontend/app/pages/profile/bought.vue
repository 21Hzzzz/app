<script setup lang="ts">
type BoughtOrderItem = {
  id: number;
  snackId: number;
  snackName: string;
  snackImage?: string | null;
  quantity: number;
  price: number | string;
  subtotal: number | string;
  sellerId: number;
  sellerUsername?: string | null;
};

type BoughtOrder = {
  id: number;
  buyerId: number;
  totalAmount: number | string;
  createTime: string;
  items: BoughtOrderItem[];
};

type ApiResponse<T> = {
  code: number;
  message: string;
  data: T;
};

type CreateChatSessionRequest = {
  orderId: number;
  buyerId: number;
  sellerId: number;
};

type CreateChatSessionResult = {
  sessionId: number;
};

const api = useApi();
const { user } = useAuth();

const loading = ref(false);
const loadError = ref("");
const orders = ref<BoughtOrder[]>([]);

const chatError = ref("");
const chatLoadingMap = ref<Record<string, boolean>>({});

function getDefaultImage(id: number) {
  return `https://picsum.photos/seed/bought-${id}/240/160`;
}

function formatPrice(value: number | string) {
  return `¥${Number(value).toFixed(2)}`;
}

function formatTime(time: string) {
  if (!time) return "-";

  const d = new Date(time);

  if (Number.isNaN(d.getTime())) {
    return time;
  }

  return d.toLocaleString("zh-CN", {
    hour12: false,
  });
}

function getChatKey(orderId: number, itemId: number) {
  return `${orderId}-${itemId}`;
}

function isChatLoading(orderId: number, itemId: number) {
  return !!chatLoadingMap.value[getChatKey(orderId, itemId)];
}

function setChatLoading(orderId: number, itemId: number, value: boolean) {
  chatLoadingMap.value = {
    ...chatLoadingMap.value,
    [getChatKey(orderId, itemId)]: value,
  };
}

async function fetchBoughtOrders() {
  if (!user.value) {
    loadError.value = "当前未登录";
    orders.value = [];
    return;
  }

  loading.value = true;
  loadError.value = "";

  try {
    const res = await api<ApiResponse<BoughtOrder[]>>("/api/orders/bought", {
      method: "GET",
      query: {
        buyerId: user.value.id,
      },
    });

    if (res.code !== 200) {
      throw new Error(res.message || "获取购买记录失败");
    }

    orders.value = (res.data || []).map((order) => ({
      ...order,
      totalAmount: Number(order.totalAmount),
      items: (order.items || []).map((item) => ({
        ...item,
        price: Number(item.price),
        subtotal: Number(item.subtotal),
        sellerId: Number(item.sellerId),
      })),
    }));
  } catch (error) {
    orders.value = [];
    loadError.value =
      error instanceof Error ? error.message : "获取购买记录失败";
  } finally {
    loading.value = false;
  }
}

async function startChat(order: BoughtOrder, item: BoughtOrderItem) {
  if (!user.value) {
    await navigateTo("/login");
    return;
  }

  if (!item.sellerId) {
    chatError.value = "当前零食缺少卖家信息，无法发起聊天";
    return;
  }

  const keyOrderId = order.id;
  const keyItemId = item.id;

  if (isChatLoading(keyOrderId, keyItemId)) return;

  chatError.value = "";
  setChatLoading(keyOrderId, keyItemId, true);

  try {
    const payload: CreateChatSessionRequest = {
      orderId: order.id,
      buyerId: user.value.id,
      sellerId: item.sellerId,
    };

    const res = await api<ApiResponse<CreateChatSessionResult>>(
      "/api/chat/sessions",
      {
        method: "POST",
        body: payload,
      },
    );

    if (res.code !== 200 || !res.data?.sessionId) {
      throw new Error(res.message || "发起聊天失败");
    }

    await navigateTo(`/chat/${res.data.sessionId}`);
  } catch (error) {
    chatError.value = error instanceof Error ? error.message : "发起聊天失败";
  } finally {
    setChatLoading(keyOrderId, keyItemId, false);
  }
}

const totalOrderCount = computed(() => orders.value.length);

const totalBoughtCount = computed(() =>
  orders.value.reduce(
    (sum, order) =>
      sum + order.items.reduce((itemSum, item) => itemSum + item.quantity, 0),
    0,
  ),
);

const totalSpent = computed(() =>
  orders.value.reduce((sum, order) => sum + Number(order.totalAmount), 0),
);

onMounted(() => {
  fetchBoughtOrders();
});
</script>

<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between gap-4">
      <div>
        <h1 class="text-2xl font-bold">我买到的</h1>
        <p class="mt-2 text-sm text-muted">查看你购买过的零食和订单明细</p>
      </div>

      <UButton
        icon="i-lucide-refresh-cw"
        variant="soft"
        :loading="loading"
        @click="fetchBoughtOrders"
      >
        刷新
      </UButton>
    </div>

    <div class="grid grid-cols-1 gap-4 md:grid-cols-3">
      <UCard class="rounded-2xl shadow-sm">
        <div class="text-sm text-muted">订单数</div>
        <div class="mt-2 text-2xl font-bold">{{ totalOrderCount }}</div>
      </UCard>

      <UCard class="rounded-2xl shadow-sm">
        <div class="text-sm text-muted">购买件数</div>
        <div class="mt-2 text-2xl font-bold">{{ totalBoughtCount }}</div>
      </UCard>

      <UCard class="rounded-2xl shadow-sm">
        <div class="text-sm text-muted">累计花费</div>
        <div class="mt-2 text-2xl font-bold">
          {{ formatPrice(totalSpent) }}
        </div>
      </UCard>
    </div>

    <div
      v-if="chatError"
      class="rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-600"
    >
      {{ chatError }}
    </div>

    <UCard class="rounded-2xl shadow-sm">
      <template #header>
        <div>
          <h2 class="text-lg font-semibold">购买记录</h2>
          <p class="mt-1 text-sm text-muted">按下单时间倒序展示</p>
        </div>
      </template>

      <div
        v-if="loading"
        class="rounded-xl border border-dashed border-default px-4 py-10 text-center text-sm text-muted"
      >
        正在加载购买记录...
      </div>

      <div
        v-else-if="loadError"
        class="rounded-xl border border-red-200 bg-red-50 px-4 py-10 text-center"
      >
        <p class="text-sm text-red-600">{{ loadError }}</p>
      </div>

      <div
        v-else-if="!orders.length"
        class="rounded-xl border border-dashed border-default px-4 py-10 text-center text-sm text-muted"
      >
        你还没有购买过零食
      </div>

      <div v-else class="space-y-4">
        <UCard v-for="order in orders" :key="order.id" class="rounded-2xl">
          <template #header>
            <div
              class="flex flex-col gap-3 md:flex-row md:items-center md:justify-between"
            >
              <div>
                <h3 class="text-base font-semibold">订单 #{{ order.id }}</h3>
                <p class="mt-1 text-sm text-muted">
                  下单时间：{{ formatTime(order.createTime) }}
                </p>
              </div>

              <UBadge color="primary" variant="soft" size="lg">
                总金额 {{ formatPrice(order.totalAmount) }}
              </UBadge>
            </div>
          </template>

          <div class="space-y-3">
            <div
              v-for="item in order.items"
              :key="item.id"
              class="flex gap-4 rounded-xl border border-default p-3"
            >
              <img
                :src="item.snackImage || getDefaultImage(item.snackId)"
                :alt="item.snackName"
                class="h-20 w-28 rounded-lg object-cover"
              />

              <div class="min-w-0 flex-1">
                <div class="flex items-start justify-between gap-3">
                  <div class="min-w-0">
                    <p class="truncate font-medium">{{ item.snackName }}</p>

                    <p class="mt-1 text-sm text-muted">
                      单价 {{ formatPrice(item.price) }}
                    </p>

                    <p class="mt-1 text-sm text-muted">
                      数量 {{ item.quantity }}
                    </p>

                    <p class="mt-1 text-sm text-muted">
                      卖家
                      {{
                        item.sellerUsername ||
                        (item.sellerId ? `用户 ${item.sellerId}` : "-")
                      }}
                    </p>
                  </div>

                  <div class="text-right">
                    <p class="text-sm text-muted">小计</p>
                    <p class="mt-1 font-semibold">
                      {{ formatPrice(item.subtotal) }}
                    </p>
                  </div>
                </div>

                <div class="mt-3 flex justify-end">
                  <UButton
                    icon="i-lucide-messages-square"
                    size="sm"
                    :loading="isChatLoading(order.id, item.id)"
                    @click="startChat(order, item)"
                  >
                    发起聊天
                  </UButton>
                </div>
              </div>
            </div>
          </div>
        </UCard>
      </div>
    </UCard>
  </div>
</template>
