<script setup lang="ts">
type SnackStatus = "on_sale" | "sold_out" | "off_shelf";

type SnackDTO = {
  id: number;
  name: string;
  price: number | string;
  stock: number;
  description: string | null;
  sellerId: number;
  status: SnackStatus;
  createTime: string;
  image?: string | null;
};

type Snack = {
  id: number;
  name: string;
  price: number;
  stock: number;
  description: string;
  image: string;
};

type CartItem = Snack & {
  quantity: number;
};

type ApiResponse<T> = {
  code: number;
  message: string;
  data: T;
};

type CreateOrderItem = {
  snackId: number;
  quantity: number;
};

type CreateOrderRequest = {
  buyerId: number;
  items: CreateOrderItem[];
};

type CreateOrderResult = {
  orderId: number;
  totalAmount: number | string;
  createTime: string;
};

const api = useApi();
const { user } = useAuth();

const snacks = ref<Snack[]>([]);
const cart = ref<CartItem[]>([]);
const loading = ref(false);
const loadError = ref("");

const checkoutLoading = ref(false);
const checkoutError = ref("");
const checkoutSuccess = ref("");

function clearCheckoutMessage() {
  checkoutError.value = "";
  checkoutSuccess.value = "";
}

function getDefaultImage(id: number) {
  return `https://picsum.photos/seed/snack${id}/400/300`;
}

function mapSnack(dto: SnackDTO): Snack {
  return {
    id: dto.id,
    name: dto.name,
    price: Number(dto.price),
    stock: dto.stock,
    description: dto.description ?? "",
    image: dto.image || getDefaultImage(dto.id),
  };
}

function syncCartWithSnacks() {
  const snackMap = new Map(snacks.value.map((item) => [item.id, item]));

  cart.value = cart.value.flatMap((item) => {
    const latestSnack = snackMap.get(item.id);

    if (!latestSnack || latestSnack.stock <= 0) {
      return [];
    }

    return [
      {
        ...latestSnack,
        quantity: Math.min(item.quantity, latestSnack.stock),
      },
    ];
  });
}

async function fetchSnacks() {
  loading.value = true;
  loadError.value = "";

  try {
    const res = await api<ApiResponse<SnackDTO[]>>("/api/snacks", {
      method: "GET",
    });

    if (res.code !== 200) {
      throw new Error(res.message || "获取零食列表失败");
    }

    snacks.value = res.data.map(mapSnack);
    syncCartWithSnacks();
  } catch (error) {
    snacks.value = [];
    loadError.value =
      error instanceof Error ? error.message : "获取零食列表失败";
  } finally {
    loading.value = false;
  }
}

function addToCart(snack: Snack) {
  clearCheckoutMessage();

  if (snack.stock <= 0) return;

  const existing = cart.value.find((item) => item.id === snack.id);

  if (existing) {
    if (existing.quantity < snack.stock) {
      existing.quantity++;
    }
    return;
  }

  cart.value.push({
    ...snack,
    quantity: 1,
  });
}

function increase(item: CartItem) {
  clearCheckoutMessage();

  if (item.quantity < item.stock) {
    item.quantity++;
  }
}

function decrease(item: CartItem) {
  clearCheckoutMessage();

  if (item.quantity > 1) {
    item.quantity--;
    return;
  }

  removeItem(item.id);
}

function removeItem(id: number) {
  clearCheckoutMessage();
  cart.value = cart.value.filter((item) => item.id !== id);
}

const totalCount = computed(() =>
  cart.value.reduce((sum, item) => sum + item.quantity, 0),
);

const totalPrice = computed(() =>
  cart.value.reduce((sum, item) => sum + item.price * item.quantity, 0),
);

const cartList = computed(() => cart.value);

async function checkout() {
  if (!cart.value.length || checkoutLoading.value) return;

  clearCheckoutMessage();

  if (!user.value) {
    await navigateTo("/login");
    return;
  }

  checkoutLoading.value = true;

  try {
    const payload: CreateOrderRequest = {
      buyerId: user.value.id,
      items: cart.value.map((item) => ({
        snackId: item.id,
        quantity: item.quantity,
      })),
    };

    const res = await api<ApiResponse<CreateOrderResult>>("/api/orders", {
      method: "POST",
      body: payload,
    });

    if (res.code !== 200 || !res.data) {
      throw new Error(res.message || "下单失败");
    }

    const amount = Number(res.data.totalAmount);

    cart.value = [];
    await fetchSnacks();

    checkoutSuccess.value = `下单成功，订单号：${res.data.orderId}，总金额：¥${amount.toFixed(2)}`;
  } catch (error) {
    checkoutError.value = error instanceof Error ? error.message : "下单失败";
    await fetchSnacks();
  } finally {
    checkoutLoading.value = false;
  }
}

onMounted(() => {
  fetchSnacks();
});
</script>

<template>
  <div class="bg-muted/30 min-h-screen">
    <div class="mx-auto max-w-7xl px-6 py-6">
      <div class="mb-6 flex items-center justify-between gap-4">
        <div>
          <h1 class="text-2xl font-bold">宿舍零食柜</h1>
          <p class="mt-1 text-sm text-muted">
            选择你想购买的零食，右侧购物车会实时更新
          </p>
          <UButton
            icon="i-lucide-refresh-cw"
            variant="soft"
            :loading="loading"
            @click="fetchSnacks"
          >
            刷新商品
          </UButton>
        </div>
      </div>

      <div class="mb-6 space-y-3">
        <UAlert
          v-if="checkoutSuccess"
          color="success"
          variant="soft"
          title="下单成功"
          :description="checkoutSuccess"
        />

        <UAlert
          v-if="checkoutError"
          color="error"
          variant="soft"
          title="下单失败"
          :description="checkoutError"
        />
      </div>

      <div class="grid grid-cols-1 gap-6 lg:grid-cols-[1fr_320px]">
        <!-- 左侧商品区 -->
        <div>
          <div
            v-if="loading"
            class="rounded-2xl border border-dashed border-default bg-background px-6 py-10 text-center text-sm text-muted"
          >
            正在加载商品...
          </div>

          <div
            v-else-if="loadError"
            class="rounded-2xl border border-red-200 bg-red-50 px-6 py-10 text-center"
          >
            <p class="text-sm text-red-600">{{ loadError }}</p>
            <UButton class="mt-4" variant="soft" @click="fetchSnacks">
              重新加载
            </UButton>
          </div>

          <div
            v-else-if="!snacks.length"
            class="rounded-2xl border border-dashed border-default bg-background px-6 py-10 text-center text-sm text-muted"
          >
            暂无在售零食
          </div>

          <div
            v-else
            class="grid grid-cols-1 gap-5 sm:grid-cols-2 xl:grid-cols-3"
          >
            <UCard
              v-for="snack in snacks"
              :key="snack.id"
              class="overflow-hidden rounded-2xl shadow-sm"
              :ui="{
                body: 'p-0',
              }"
            >
              <img
                :src="snack.image"
                :alt="snack.name"
                class="h-44 w-full object-cover"
              />

              <div class="p-4">
                <div class="flex items-start justify-between gap-3">
                  <div>
                    <h2 class="text-base font-semibold">{{ snack.name }}</h2>
                    <p class="mt-1 text-sm text-muted">
                      {{ snack.description || "暂无描述" }}
                    </p>
                  </div>

                  <UBadge color="neutral" variant="soft">
                    库存 {{ snack.stock }}
                  </UBadge>
                </div>

                <div class="mt-4 flex items-center justify-between">
                  <span class="text-lg font-bold text-primary">
                    ¥{{ snack.price.toFixed(2) }}
                  </span>

                  <UButton
                    size="sm"
                    icon="i-lucide-shopping-cart"
                    :disabled="snack.stock <= 0"
                    @click="addToCart(snack)"
                  >
                    加入购物车
                  </UButton>
                </div>
              </div>
            </UCard>
          </div>
        </div>

        <!-- 右侧固定购物车 -->
        <div class="relative hidden lg:block">
          <div
            class="fixed top-[calc(var(--ui-header-height,64px)+24px)] w-[320px]"
          >
            <UCard class="rounded-2xl shadow-sm">
              <template #header>
                <div class="flex items-center justify-between">
                  <div>
                    <h2 class="text-lg font-semibold">购物车</h2>
                    <p class="mt-1 text-sm text-muted">
                      共 {{ totalCount }} 件商品
                    </p>
                  </div>

                  <UBadge color="primary" variant="soft">
                    ¥{{ totalPrice.toFixed(2) }}
                  </UBadge>
                </div>
              </template>

              <div class="max-h-[50vh] space-y-3 overflow-y-auto pr-1">
                <div
                  v-if="!cartList.length"
                  class="rounded-xl border border-dashed border-default px-4 py-8 text-center text-sm text-muted"
                >
                  购物车还是空的
                </div>

                <div
                  v-for="item in cartList"
                  :key="item.id"
                  class="rounded-xl border border-default p-3"
                >
                  <div class="flex items-start justify-between gap-3">
                    <div class="min-w-0 flex-1">
                      <p class="truncate font-medium">{{ item.name }}</p>
                      <p class="mt-1 text-sm text-muted">
                        ¥{{ item.price.toFixed(2) }} / 件
                      </p>
                    </div>

                    <UButton
                      color="neutral"
                      variant="ghost"
                      icon="i-lucide-trash-2"
                      @click="removeItem(item.id)"
                    />
                  </div>

                  <div class="mt-3 flex items-center justify-between">
                    <div class="flex items-center gap-2">
                      <UButton
                        size="xs"
                        color="neutral"
                        variant="soft"
                        icon="i-lucide-minus"
                        @click="decrease(item)"
                      />
                      <span class="w-8 text-center text-sm font-medium">
                        {{ item.quantity }}
                      </span>
                      <UButton
                        size="xs"
                        color="neutral"
                        variant="soft"
                        icon="i-lucide-plus"
                        :disabled="item.quantity >= item.stock"
                        @click="increase(item)"
                      />
                    </div>

                    <span class="font-semibold">
                      ¥{{ (item.price * item.quantity).toFixed(2) }}
                    </span>
                  </div>
                </div>
              </div>

              <template #footer>
                <div class="space-y-3">
                  <div class="flex items-center justify-between text-sm">
                    <span class="text-muted">商品总数</span>
                    <span>{{ totalCount }} 件</span>
                  </div>

                  <div
                    class="flex items-center justify-between text-base font-semibold"
                  >
                    <span>合计</span>
                    <span class="text-primary">
                      ¥{{ totalPrice.toFixed(2) }}
                    </span>
                  </div>

                  <UButton
                    block
                    size="lg"
                    icon="i-lucide-credit-card"
                    :disabled="!cartList.length || checkoutLoading"
                    :loading="checkoutLoading"
                    @click="checkout"
                  >
                    去结算
                  </UButton>
                </div>
              </template>
            </UCard>
          </div>
        </div>
      </div>

      <!-- 小屏购物车 -->
      <div class="mt-6 lg:hidden">
        <UCard class="rounded-2xl shadow-sm">
          <template #header>
            <div class="flex items-center justify-between">
              <h2 class="text-lg font-semibold">购物车</h2>
              <UBadge color="primary" variant="soft">
                ¥{{ totalPrice.toFixed(2) }}
              </UBadge>
            </div>
          </template>

          <div v-if="!cartList.length" class="text-sm text-muted">
            购物车还是空的
          </div>

          <div v-else class="space-y-3">
            <div
              v-for="item in cartList"
              :key="item.id"
              class="rounded-xl border border-default p-3"
            >
              <div class="flex items-center justify-between gap-3">
                <div>
                  <p class="font-medium">{{ item.name }}</p>
                  <p class="text-sm text-muted">
                    ¥{{ item.price.toFixed(2) }} × {{ item.quantity }}
                  </p>
                </div>

                <span class="font-semibold">
                  ¥{{ (item.price * item.quantity).toFixed(2) }}
                </span>
              </div>
            </div>

            <UButton
              block
              size="lg"
              :disabled="!cartList.length || checkoutLoading"
              :loading="checkoutLoading"
              @click="checkout"
            >
              去结算
            </UButton>
          </div>
        </UCard>
      </div>
    </div>
  </div>
</template>
