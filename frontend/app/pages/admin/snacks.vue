<script setup lang="ts">
definePageMeta({
  middleware: "admin",
});

type SnackStatus = "on_sale" | "sold_out" | "off_shelf";

type AdminSnack = {
  id: number;
  name: string;
  image?: string | null;
  price: number | string;
  stock: number;
  description: string | null;
  sellerId: number;
  sellerUsername?: string | null;
  status: SnackStatus;
  createTime: string;
};

type ApiResponse<T> = {
  code: number;
  message: string;
  data: T;
};

const api = useApi();

const loading = ref(false);
const loadError = ref("");
const offShelfLoadingId = ref<number | null>(null);
const snacks = ref<AdminSnack[]>([]);

function getDefaultImage(id: number) {
  return `https://picsum.photos/seed/admin-snack-${id}/400/300`;
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

function getStatusLabel(status: SnackStatus) {
  switch (status) {
    case "on_sale":
      return "在售";
    case "sold_out":
      return "已售罄";
    case "off_shelf":
      return "已下架";
    default:
      return status;
  }
}

function getStatusColor(status: SnackStatus) {
  switch (status) {
    case "on_sale":
      return "success";
    case "sold_out":
      return "warning";
    case "off_shelf":
      return "neutral";
    default:
      return "neutral";
  }
}

async function fetchAdminSnacks() {
  loading.value = true;
  loadError.value = "";

  try {
    const res = await api<ApiResponse<AdminSnack[]>>("/api/admin/snacks", {
      method: "GET",
    });

    if (res.code !== 200) {
      throw new Error(res.message || "获取商品列表失败");
    }

    snacks.value = (res.data || []).map((item) => ({
      ...item,
      price: Number(item.price),
    }));
  } catch (error) {
    snacks.value = [];
    loadError.value =
      error instanceof Error ? error.message : "获取商品列表失败";
  } finally {
    loading.value = false;
  }
}

async function offShelf(snackId: number) {
  offShelfLoadingId.value = snackId;

  try {
    const res = await api<ApiResponse<null>>(
      `/api/admin/snacks/${snackId}/off-shelf`,
      {
        method: "PATCH",
      },
    );

    if (res.code !== 200) {
      throw new Error(res.message || "下架失败");
    }

    await fetchAdminSnacks();
  } catch (error) {
    alert(error instanceof Error ? error.message : "下架失败");
  } finally {
    offShelfLoadingId.value = null;
  }
}

const totalCount = computed(() => snacks.value.length);

const onSaleCount = computed(
  () => snacks.value.filter((item) => item.status === "on_sale").length,
);

const soldOutCount = computed(
  () => snacks.value.filter((item) => item.status === "sold_out").length,
);

const offShelfCount = computed(
  () => snacks.value.filter((item) => item.status === "off_shelf").length,
);

onMounted(() => {
  fetchAdminSnacks();
});
</script>

<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between gap-4">
      <div>
        <h1 class="text-2xl font-bold">商品管理</h1>
        <p class="mt-2 text-sm text-muted">
          查看全部商品，并对在售商品执行下架操作
        </p>
      </div>

      <UButton
        icon="i-lucide-refresh-cw"
        variant="soft"
        :loading="loading"
        @click="fetchAdminSnacks"
      >
        刷新
      </UButton>
    </div>

    <div class="grid grid-cols-1 gap-4 md:grid-cols-4">
      <UCard class="rounded-2xl shadow-sm">
        <div class="text-sm text-muted">商品总数</div>
        <div class="mt-2 text-2xl font-bold">{{ totalCount }}</div>
      </UCard>

      <UCard class="rounded-2xl shadow-sm">
        <div class="text-sm text-muted">在售商品</div>
        <div class="mt-2 text-2xl font-bold">{{ onSaleCount }}</div>
      </UCard>

      <UCard class="rounded-2xl shadow-sm">
        <div class="text-sm text-muted">已售罄</div>
        <div class="mt-2 text-2xl font-bold">{{ soldOutCount }}</div>
      </UCard>

      <UCard class="rounded-2xl shadow-sm">
        <div class="text-sm text-muted">已下架</div>
        <div class="mt-2 text-2xl font-bold">{{ offShelfCount }}</div>
      </UCard>
    </div>

    <UCard class="rounded-2xl shadow-sm">
      <template #header>
        <div>
          <h2 class="text-lg font-semibold">商品列表</h2>
          <p class="mt-1 text-sm text-muted">按发布时间倒序展示全部商品</p>
        </div>
      </template>

      <div
        v-if="loading"
        class="rounded-xl border border-dashed border-default px-4 py-10 text-center text-sm text-muted"
      >
        正在加载商品数据...
      </div>

      <div
        v-else-if="loadError"
        class="rounded-xl border border-red-200 bg-red-50 px-4 py-10 text-center"
      >
        <p class="text-sm text-red-600">{{ loadError }}</p>
      </div>

      <div
        v-else-if="!snacks.length"
        class="rounded-xl border border-dashed border-default px-4 py-10 text-center text-sm text-muted"
      >
        暂无商品数据
      </div>

      <div v-else class="grid grid-cols-1 gap-5 xl:grid-cols-2">
        <UCard
          v-for="snack in snacks"
          :key="snack.id"
          class="overflow-hidden rounded-2xl"
          :ui="{ body: 'p-0' }"
        >
          <img
            :src="snack.image || getDefaultImage(snack.id)"
            :alt="snack.name"
            class="h-44 w-full object-cover"
          />

          <div class="space-y-4 p-4">
            <div class="flex items-start justify-between gap-3">
              <div class="min-w-0">
                <h3 class="truncate text-base font-semibold">
                  {{ snack.name }}
                </h3>
                <p class="mt-1 text-sm text-muted">
                  {{ snack.description || "暂无描述" }}
                </p>
              </div>

              <UBadge :color="getStatusColor(snack.status)" variant="soft">
                {{ getStatusLabel(snack.status) }}
              </UBadge>
            </div>

            <div class="grid grid-cols-2 gap-3 text-sm">
              <div class="rounded-xl bg-muted/50 px-3 py-2">
                <div class="text-muted">价格</div>
                <div class="mt-1 font-semibold">
                  {{ formatPrice(snack.price) }}
                </div>
              </div>

              <div class="rounded-xl bg-muted/50 px-3 py-2">
                <div class="text-muted">库存</div>
                <div class="mt-1 font-semibold">{{ snack.stock }}</div>
              </div>

              <div class="rounded-xl bg-muted/50 px-3 py-2">
                <div class="text-muted">卖家</div>
                <div class="mt-1 font-semibold">
                  {{ snack.sellerUsername || `用户 ${snack.sellerId}` }}
                </div>
              </div>

              <div class="rounded-xl bg-muted/50 px-3 py-2">
                <div class="text-muted">商品 ID</div>
                <div class="mt-1 font-semibold">{{ snack.id }}</div>
              </div>

              <div class="col-span-2 rounded-xl bg-muted/50 px-3 py-2">
                <div class="text-muted">发布时间</div>
                <div class="mt-1 font-semibold">
                  {{ formatTime(snack.createTime) }}
                </div>
              </div>
            </div>

            <div class="flex justify-end gap-2">
              <UButton
                v-if="snack.status === 'on_sale'"
                color="error"
                variant="soft"
                icon="i-lucide-package-x"
                :loading="offShelfLoadingId === snack.id"
                @click="offShelf(snack.id)"
              >
                下架商品
              </UButton>
            </div>
          </div>
        </UCard>
      </div>
    </UCard>
  </div>
</template>
