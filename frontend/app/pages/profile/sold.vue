<script setup lang="ts">
type SoldRecord = {
  orderId: number;
  buyerId: number;
  buyerUsername?: string | null;
  createTime: string;
  snackId: number;
  snackName: string;
  snackImage?: string | null;
  quantity: number;
  price: number | string;
  subtotal: number | string;
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
const records = ref<SoldRecord[]>([]);

function getDefaultImage(id: number) {
  return `https://picsum.photos/seed/sold-${id}/240/160`;
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

async function fetchSoldRecords() {
  if (!user.value) {
    loadError.value = "当前未登录";
    records.value = [];
    return;
  }

  loading.value = true;
  loadError.value = "";

  try {
    const res = await api<ApiResponse<SoldRecord[]>>("/api/orders/sold", {
      method: "GET",
      query: {
        sellerId: user.value.id,
      },
    });

    if (res.code !== 200) {
      throw new Error(res.message || "获取卖出记录失败");
    }

    records.value = (res.data || []).map((item) => ({
      ...item,
      price: Number(item.price),
      subtotal: Number(item.subtotal),
    }));
  } catch (error) {
    records.value = [];
    loadError.value =
      error instanceof Error ? error.message : "获取卖出记录失败";
  } finally {
    loading.value = false;
  }
}

const totalRecordCount = computed(() => records.value.length);

const totalSoldCount = computed(() =>
  records.value.reduce((sum, item) => sum + item.quantity, 0),
);

const totalIncome = computed(() =>
  records.value.reduce((sum, item) => sum + Number(item.subtotal), 0),
);

onMounted(() => {
  fetchSoldRecords();
});
</script>

<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between gap-4">
      <div>
        <h1 class="text-2xl font-bold">我卖出的</h1>
        <p class="mt-2 text-sm text-muted">查看你售出的零食明细和收入</p>
      </div>

      <UButton
        icon="i-lucide-refresh-cw"
        variant="soft"
        :loading="loading"
        @click="fetchSoldRecords"
      >
        刷新
      </UButton>
    </div>

    <div class="grid grid-cols-1 gap-4 md:grid-cols-3">
      <UCard class="rounded-2xl shadow-sm">
        <div class="text-sm text-muted">卖出记录数</div>
        <div class="mt-2 text-2xl font-bold">{{ totalRecordCount }}</div>
      </UCard>

      <UCard class="rounded-2xl shadow-sm">
        <div class="text-sm text-muted">卖出件数</div>
        <div class="mt-2 text-2xl font-bold">{{ totalSoldCount }}</div>
      </UCard>

      <UCard class="rounded-2xl shadow-sm">
        <div class="text-sm text-muted">累计收入</div>
        <div class="mt-2 text-2xl font-bold">
          {{ formatPrice(totalIncome) }}
        </div>
      </UCard>
    </div>

    <UCard class="rounded-2xl shadow-sm">
      <template #header>
        <div>
          <h2 class="text-lg font-semibold">卖出记录</h2>
          <p class="mt-1 text-sm text-muted">按下单时间倒序展示</p>
        </div>
      </template>

      <div
        v-if="loading"
        class="rounded-xl border border-dashed border-default px-4 py-10 text-center text-sm text-muted"
      >
        正在加载卖出记录...
      </div>

      <div
        v-else-if="loadError"
        class="rounded-xl border border-red-200 bg-red-50 px-4 py-10 text-center"
      >
        <p class="text-sm text-red-600">{{ loadError }}</p>
      </div>

      <div
        v-else-if="!records.length"
        class="rounded-xl border border-dashed border-default px-4 py-10 text-center text-sm text-muted"
      >
        你还没有卖出过零食
      </div>

      <div v-else class="space-y-4">
        <UCard
          v-for="record in records"
          :key="`${record.orderId}-${record.snackId}`"
          class="rounded-2xl"
        >
          <div class="flex gap-4">
            <img
              :src="record.snackImage || getDefaultImage(record.snackId)"
              :alt="record.snackName"
              class="h-24 w-32 rounded-xl object-cover"
            />

            <div class="min-w-0 flex-1">
              <div
                class="flex flex-col gap-3 md:flex-row md:items-start md:justify-between"
              >
                <div class="min-w-0">
                  <h3 class="truncate text-base font-semibold">
                    {{ record.snackName }}
                  </h3>

                  <p class="mt-1 text-sm text-muted">
                    订单号：#{{ record.orderId }}
                  </p>

                  <p class="mt-1 text-sm text-muted">
                    买家：
                    {{ record.buyerUsername || `用户 ${record.buyerId}` }}
                  </p>

                  <p class="mt-1 text-sm text-muted">
                    下单时间：{{ formatTime(record.createTime) }}
                  </p>
                </div>

                <UBadge color="success" variant="soft" size="lg">
                  收入 {{ formatPrice(record.subtotal) }}
                </UBadge>
              </div>

              <div class="mt-4 grid grid-cols-1 gap-3 sm:grid-cols-3">
                <div class="rounded-xl bg-muted/50 px-3 py-2">
                  <div class="text-sm text-muted">单价</div>
                  <div class="mt-1 font-semibold">
                    {{ formatPrice(record.price) }}
                  </div>
                </div>

                <div class="rounded-xl bg-muted/50 px-3 py-2">
                  <div class="text-sm text-muted">数量</div>
                  <div class="mt-1 font-semibold">{{ record.quantity }}</div>
                </div>

                <div class="rounded-xl bg-muted/50 px-3 py-2">
                  <div class="text-sm text-muted">小计</div>
                  <div class="mt-1 font-semibold">
                    {{ formatPrice(record.subtotal) }}
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
