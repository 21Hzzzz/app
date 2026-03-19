<script setup lang="ts">
import * as z from "zod";
import type { FormSubmitEvent } from "@nuxt/ui";

type SnackStatus = "on_sale" | "sold_out" | "off_shelf";

type MySnack = {
  id: number;
  name: string;
  price: number;
  stock: number;
  description: string | null;
  sellerId: number;
  status: SnackStatus;
  createTime: string;
  image?: string | null;
};

type ApiResponse<T> = {
  code: number;
  message: string;
  data: T;
};

const api = useApi();
const { user } = useAuth();

const loading = ref(false);
const submitting = ref(false);
const offShelfLoadingId = ref<number | null>(null);
const loadError = ref("");
const submitError = ref("");

const schema = z.object({
  name: z
    .string()
    .min(1, "请输入零食名称")
    .max(100, "零食名称不能超过 100 个字符"),
  price: z.coerce.number().gt(0, "价格必须大于 0"),
  stock: z.coerce.number().int("库存必须是整数").min(1, "库存至少为 1"),
  description: z
    .string()
    .max(255, "描述不能超过 255 个字符")
    .optional()
    .default(""),
  image: z
    .union([z.string().url("图片地址格式不正确"), z.literal("")])
    .optional()
    .default(""),
});

type Schema = z.infer<typeof schema>;

const state = reactive({
  name: "",
  price: "",
  stock: "",
  description: "",
  image: "",
});

const mySnacks = ref<MySnack[]>([]);

function getDefaultImage(id: number) {
  return `https://picsum.photos/seed/published-${id}/400/300`;
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

async function fetchMySnacks() {
  if (!user.value) {
    return;
  }

  loading.value = true;
  loadError.value = "";

  try {
    const res = await api<ApiResponse<MySnack[]>>("/api/snacks/mine", {
      method: "GET",
      query: {
        sellerId: user.value.id,
      },
    });

    if (res.code !== 200) {
      throw new Error(res.message || "获取我发布的零食失败");
    }

    mySnacks.value = (res.data || []).map((item) => ({
      ...item,
      price: Number(item.price),
    }));
  } catch (error) {
    mySnacks.value = [];
    loadError.value =
      error instanceof Error ? error.message : "获取我发布的零食失败";
  } finally {
    loading.value = false;
  }
}

async function onSubmit(event: FormSubmitEvent<Schema>) {
  if (!user.value) {
    submitError.value = "当前未登录";
    return;
  }

  submitting.value = true;
  submitError.value = "";

  try {
    const res = await api<ApiResponse<null>>("/api/snacks", {
      method: "POST",
      body: {
        name: event.data.name,
        price: event.data.price,
        stock: event.data.stock,
        description: event.data.description || "",
        image: event.data.image || null,
        sellerId: user.value.id,
      },
    });

    if (res.code !== 200) {
      throw new Error(res.message || "发布失败");
    }

    state.name = "";
    state.price = "";
    state.stock = "";
    state.description = "";
    state.image = "";

    await fetchMySnacks();
    alert("发布成功");
  } catch (error) {
    submitError.value = error instanceof Error ? error.message : "发布失败";
  } finally {
    submitting.value = false;
  }
}

async function offShelf(snackId: number) {
  if (!user.value) return;

  offShelfLoadingId.value = snackId;

  try {
    const res = await api<ApiResponse<null>>(
      `/api/snacks/${snackId}/off-shelf`,
      {
        method: "PATCH",
        body: {
          sellerId: user.value.id,
        },
      },
    );

    if (res.code !== 200) {
      throw new Error(res.message || "下架失败");
    }

    await fetchMySnacks();
  } catch (error) {
    alert(error instanceof Error ? error.message : "下架失败");
  } finally {
    offShelfLoadingId.value = null;
  }
}

const onSaleCount = computed(
  () => mySnacks.value.filter((item) => item.status === "on_sale").length,
);

const soldOutCount = computed(
  () => mySnacks.value.filter((item) => item.status === "sold_out").length,
);

const offShelfCount = computed(
  () => mySnacks.value.filter((item) => item.status === "off_shelf").length,
);

onMounted(() => {
  fetchMySnacks();
});
</script>

<template>
  <div class="space-y-6">
    <!-- 发布表单 -->
    <UCard class="rounded-2xl shadow-sm">
      <template #header>
        <div>
          <h2 class="text-lg font-semibold">上架零食</h2>
          <p class="mt-1 text-sm text-muted">填写信息后发布到零食柜</p>
        </div>
      </template>

      <UAlert
        v-if="submitError"
        color="error"
        variant="soft"
        title="发布失败"
        :description="submitError"
        class="mb-4"
      />

      <UForm
        :schema="schema"
        :state="state"
        class="space-y-4"
        @submit="onSubmit"
      >
        <div class="grid grid-cols-1 gap-4 md:grid-cols-2">
          <UFormField label="零食名称" name="name" required>
            <UInput v-model="state.name" placeholder="例如：乐事薯片" />
          </UFormField>

          <UFormField label="图片地址" name="image">
            <UInput
              v-model="state.image"
              placeholder="可选，填写图片 URL；不填则使用默认图"
            />
          </UFormField>

          <UFormField label="价格" name="price" required>
            <UInput
              v-model="state.price"
              type="number"
              placeholder="例如：6.5"
            />
          </UFormField>

          <UFormField label="库存" name="stock" required>
            <UInput
              v-model="state.stock"
              type="number"
              placeholder="例如：10"
            />
          </UFormField>
        </div>

        <UFormField label="描述" name="description">
          <UTextarea
            v-model="state.description"
            :rows="4"
            placeholder="简单介绍一下这款零食"
          />
        </UFormField>

        <div class="flex justify-end">
          <UButton
            type="submit"
            icon="i-lucide-package-plus"
            :loading="submitting"
          >
            发布零食
          </UButton>
        </div>
      </UForm>
    </UCard>

    <!-- 数据概览 -->
    <div class="grid grid-cols-1 gap-4 md:grid-cols-3">
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

    <!-- 我发布的列表 -->
    <UCard class="rounded-2xl shadow-sm">
      <template #header>
        <div class="flex items-center justify-between gap-4">
          <div>
            <h2 class="text-lg font-semibold">我发布的零食</h2>
            <p class="mt-1 text-sm text-muted">查看和管理你上架过的商品</p>
          </div>

          <UButton
            icon="i-lucide-refresh-cw"
            variant="soft"
            :loading="loading"
            @click="fetchMySnacks"
          >
            刷新
          </UButton>
        </div>
      </template>

      <div
        v-if="loading"
        class="rounded-xl border border-dashed border-default px-4 py-10 text-center text-sm text-muted"
      >
        正在加载...
      </div>

      <div
        v-else-if="loadError"
        class="rounded-xl border border-red-200 bg-red-50 px-4 py-10 text-center"
      >
        <p class="text-sm text-red-600">{{ loadError }}</p>
      </div>

      <div
        v-else-if="!mySnacks.length"
        class="rounded-xl border border-dashed border-default px-4 py-10 text-center text-sm text-muted"
      >
        你还没有发布过零食
      </div>

      <div v-else class="grid grid-cols-1 gap-5 xl:grid-cols-2">
        <UCard
          v-for="snack in mySnacks"
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
              <div>
                <h3 class="text-base font-semibold">{{ snack.name }}</h3>
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
                  ¥{{ snack.price.toFixed(2) }}
                </div>
              </div>

              <div class="rounded-xl bg-muted/50 px-3 py-2">
                <div class="text-muted">库存</div>
                <div class="mt-1 font-semibold">{{ snack.stock }}</div>
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
                下架
              </UButton>
            </div>
          </div>
        </UCard>
      </div>
    </UCard>
  </div>
</template>
