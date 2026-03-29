<script setup lang="ts">
definePageMeta({
  middleware: "admin",
});

type AdminOrderItem = {
  id: number;
  snackId: number;
  snackName: string;
  snackImage?: string | null;
  sellerId: number;
  sellerUsername?: string | null;
  quantity: number;
  price: number | string;
  subtotal: number | string;
};

type AdminOrder = {
  id: number;
  buyerId: number;
  buyerUsername?: string | null;
  totalAmount: number | string;
  createTime: string;
  items: AdminOrderItem[];
};

type ApiResponse<T> = {
  code: number;
  message: string;
  data: T;
};

const api = useApi();
const resolveImageUrl = useImageUrl();

const loading = ref(false);
const loadError = ref("");
const orders = ref<AdminOrder[]>([]);

function getDefaultImage(id: number) {
  return `https://picsum.photos/seed/admin-order-${id}/240/160`;
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

async function fetchAdminOrders() {
  loading.value = true;
  loadError.value = "";

  try {
    const res = await api<ApiResponse<AdminOrder[]>>("/api/admin/orders", {
      method: "GET",
    });

    if (res.code !== 200) {
      throw new Error(res.message || "获取订单列表失败");
    }

    orders.value = (res.data || []).map((order) => ({
      ...order,
      totalAmount: Number(order.totalAmount),
      items: (order.items || []).map((item) => ({
        ...item,
        price: Number(item.price),
        subtotal: Number(item.subtotal),
      })),
    }));
  } catch (error) {
    orders.value = [];
    loadError.value =
      error instanceof Error ? error.message : "获取订单列表失败";
  } finally {
    loading.value = false;
  }
}

const totalOrderCount = computed(() => orders.value.length);

const totalItemCount = computed(() =>
  orders.value.reduce(
    (sum, order) =>
      sum + order.items.reduce((itemSum, item) => itemSum + item.quantity, 0),
    0,
  ),
);

const totalAmount = computed(() =>
  orders.value.reduce((sum, order) => sum + Number(order.totalAmount), 0),
);

onMounted(() => {
  fetchAdminOrders();
});
</script>

<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between gap-4">
      <div>
        <h1 class="text-2xl font-bold">订单管理</h1>
        <p class="mt-2 text-sm text-muted">查看全部订单和订单明细</p>
      </div>

      <UButton
        icon="i-lucide-refresh-cw"
        variant="soft"
        :loading="loading"
        @click="fetchAdminOrders"
      >
        刷新
      </UButton>
    </div>

    <div class="grid grid-cols-1 gap-4 md:grid-cols-3">
      <UCard class="rounded-2xl shadow-sm">
        <div class="text-sm text-muted">订单总数</div>
        <div class="mt-2 text-2xl font-bold">{{ totalOrderCount }}</div>
      </UCard>

      <UCard class="rounded-2xl shadow-sm">
        <div class="text-sm text-muted">商品件数</div>
        <div class="mt-2 text-2xl font-bold">{{ totalItemCount }}</div>
      </UCard>

      <UCard class="rounded-2xl shadow-sm">
        <div class="text-sm text-muted">成交总额</div>
        <div class="mt-2 text-2xl font-bold">
          {{ formatPrice(totalAmount) }}
        </div>
      </UCard>
    </div>

    <UCard class="rounded-2xl shadow-sm">
      <template #header>
        <div>
          <h2 class="text-lg font-semibold">订单列表</h2>
          <p class="mt-1 text-sm text-muted">按下单时间倒序展示全部订单</p>
        </div>
      </template>

      <div
        v-if="loading"
        class="rounded-xl border border-dashed border-default px-4 py-10 text-center text-sm text-muted"
      >
        正在加载订单数据...
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
        暂无订单数据
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
                  买家：{{ order.buyerUsername || `用户 ${order.buyerId}` }}
                </p>
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
                :src="resolveImageUrl(item.snackImage, getDefaultImage(item.snackId))"
                :alt="item.snackName"
                class="h-20 w-28 rounded-lg object-cover"
              />

              <div class="min-w-0 flex-1">
                <div
                  class="flex flex-col gap-3 md:flex-row md:items-start md:justify-between"
                >
                  <div class="min-w-0">
                    <p class="truncate font-medium">{{ item.snackName }}</p>
                    <p class="mt-1 text-sm text-muted">
                      卖家：{{ item.sellerUsername || `用户 ${item.sellerId}` }}
                    </p>
                    <p class="mt-1 text-sm text-muted">
                      单价 {{ formatPrice(item.price) }}
                    </p>
                    <p class="mt-1 text-sm text-muted">
                      数量 {{ item.quantity }}
                    </p>
                  </div>

                  <div class="text-right">
                    <p class="text-sm text-muted">小计</p>
                    <p class="mt-1 font-semibold">
                      {{ formatPrice(item.subtotal) }}
                    </p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </UCard>
      </div>
    </UCard>
  </div>
</template>
