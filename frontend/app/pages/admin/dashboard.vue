<script setup lang="ts">
definePageMeta({
  middleware: "admin",
});

type DashboardSummary = {
  totalUsers: number;
  totalSnacks: number;
  totalOrders: number;
  totalAmount: number | string;
  onSaleSnacks: number;
  soldOutSnacks: number;
  offShelfSnacks: number;
  normalUsers: number;
  adminUsers: number;
};

type RecentOrderItem = {
  id: number;
  snackId: number;
  snackName: string;
  quantity: number;
  price: number | string;
  subtotal: number | string;
};

type RecentOrder = {
  id: number;
  buyerId: number;
  buyerUsername?: string | null;
  totalAmount: number | string;
  createTime: string;
  items: RecentOrderItem[];
};

type DashboardData = {
  summary: DashboardSummary;
  recentOrders: RecentOrder[];
};

type ApiResponse<T> = {
  code: number;
  message: string;
  data: T;
};

const api = useApi();

const loading = ref(false);
const loadError = ref("");

const dashboard = ref<DashboardData>({
  summary: {
    totalUsers: 0,
    totalSnacks: 0,
    totalOrders: 0,
    totalAmount: 0,
    onSaleSnacks: 0,
    soldOutSnacks: 0,
    offShelfSnacks: 0,
    normalUsers: 0,
    adminUsers: 0,
  },
  recentOrders: [],
});

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

async function fetchDashboard() {
  loading.value = true;
  loadError.value = "";

  try {
    const res = await api<ApiResponse<DashboardData>>("/api/admin/dashboard", {
      method: "GET",
    });

    if (res.code !== 200 || !res.data) {
      throw new Error(res.message || "获取后台概览失败");
    }

    dashboard.value = {
      summary: {
        ...res.data.summary,
        totalAmount: Number(res.data.summary.totalAmount),
      },
      recentOrders: (res.data.recentOrders || []).map((order) => ({
        ...order,
        totalAmount: Number(order.totalAmount),
        items: (order.items || []).map((item) => ({
          ...item,
          price: Number(item.price),
          subtotal: Number(item.subtotal),
        })),
      })),
    };
  } catch (error) {
    loadError.value =
      error instanceof Error ? error.message : "获取后台概览失败";
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  fetchDashboard();
});
</script>

<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between gap-4">
      <div>
        <h1 class="text-2xl font-bold">后台概览</h1>
        <p class="mt-2 text-sm text-muted">查看系统整体运行情况和最近订单</p>
      </div>

      <UButton
        icon="i-lucide-refresh-cw"
        variant="soft"
        :loading="loading"
        @click="fetchDashboard"
      >
        刷新
      </UButton>
    </div>

    <div
      v-if="loadError"
      class="rounded-2xl border border-red-200 bg-red-50 px-4 py-10 text-center"
    >
      <p class="text-sm text-red-600">{{ loadError }}</p>
    </div>

    <template v-else>
      <!-- 核心统计 -->
      <div class="grid grid-cols-1 gap-4 md:grid-cols-2 xl:grid-cols-4">
        <UCard class="rounded-2xl shadow-sm">
          <div class="text-sm text-muted">用户总数</div>
          <div class="mt-2 text-2xl font-bold">
            {{ dashboard.summary.totalUsers }}
          </div>
        </UCard>

        <UCard class="rounded-2xl shadow-sm">
          <div class="text-sm text-muted">商品总数</div>
          <div class="mt-2 text-2xl font-bold">
            {{ dashboard.summary.totalSnacks }}
          </div>
        </UCard>

        <UCard class="rounded-2xl shadow-sm">
          <div class="text-sm text-muted">订单总数</div>
          <div class="mt-2 text-2xl font-bold">
            {{ dashboard.summary.totalOrders }}
          </div>
        </UCard>

        <UCard class="rounded-2xl shadow-sm">
          <div class="text-sm text-muted">成交总额</div>
          <div class="mt-2 text-2xl font-bold">
            {{ formatPrice(dashboard.summary.totalAmount) }}
          </div>
        </UCard>
      </div>

      <!-- 分类统计 -->
      <div class="grid grid-cols-1 gap-6 xl:grid-cols-2">
        <UCard class="rounded-2xl shadow-sm">
          <template #header>
            <div>
              <h2 class="text-lg font-semibold">商品状态统计</h2>
              <p class="mt-1 text-sm text-muted">按当前商品状态汇总</p>
            </div>
          </template>

          <div class="grid grid-cols-1 gap-4 sm:grid-cols-3">
            <div class="rounded-xl bg-muted/50 px-4 py-4">
              <div class="text-sm text-muted">在售商品</div>
              <div class="mt-2 text-xl font-bold">
                {{ dashboard.summary.onSaleSnacks }}
              </div>
            </div>

            <div class="rounded-xl bg-muted/50 px-4 py-4">
              <div class="text-sm text-muted">已售罄</div>
              <div class="mt-2 text-xl font-bold">
                {{ dashboard.summary.soldOutSnacks }}
              </div>
            </div>

            <div class="rounded-xl bg-muted/50 px-4 py-4">
              <div class="text-sm text-muted">已下架</div>
              <div class="mt-2 text-xl font-bold">
                {{ dashboard.summary.offShelfSnacks }}
              </div>
            </div>
          </div>
        </UCard>

        <UCard class="rounded-2xl shadow-sm">
          <template #header>
            <div>
              <h2 class="text-lg font-semibold">用户角色统计</h2>
              <p class="mt-1 text-sm text-muted">按角色汇总用户数量</p>
            </div>
          </template>

          <div class="grid grid-cols-1 gap-4 sm:grid-cols-2">
            <div class="rounded-xl bg-muted/50 px-4 py-4">
              <div class="text-sm text-muted">普通用户</div>
              <div class="mt-2 text-xl font-bold">
                {{ dashboard.summary.normalUsers }}
              </div>
            </div>

            <div class="rounded-xl bg-muted/50 px-4 py-4">
              <div class="text-sm text-muted">管理员</div>
              <div class="mt-2 text-xl font-bold">
                {{ dashboard.summary.adminUsers }}
              </div>
            </div>
          </div>
        </UCard>
      </div>

      <!-- 最近订单 -->
      <UCard class="rounded-2xl shadow-sm">
        <template #header>
          <div>
            <h2 class="text-lg font-semibold">最近订单</h2>
            <p class="mt-1 text-sm text-muted">展示最新的几笔订单</p>
          </div>
        </template>

        <div
          v-if="loading"
          class="rounded-xl border border-dashed border-default px-4 py-10 text-center text-sm text-muted"
        >
          正在加载后台数据...
        </div>

        <div
          v-else-if="!dashboard.recentOrders.length"
          class="rounded-xl border border-dashed border-default px-4 py-10 text-center text-sm text-muted"
        >
          暂无最近订单
        </div>

        <div v-else class="space-y-4">
          <UCard
            v-for="order in dashboard.recentOrders"
            :key="order.id"
            class="rounded-2xl"
          >
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
                class="rounded-xl border border-default p-3"
              >
                <div class="flex items-start justify-between gap-3">
                  <div class="min-w-0">
                    <p class="truncate font-medium">{{ item.snackName }}</p>
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
          </UCard>
        </div>
      </UCard>
    </template>
  </div>
</template>
