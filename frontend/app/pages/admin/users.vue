<script setup lang="ts">
definePageMeta({
  middleware: "admin",
});

type UserRole = "user" | "admin";

type AdminUser = {
  id: number;
  username: string;
  role: UserRole;
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
const users = ref<AdminUser[]>([]);
const deletingUserId = ref<number | null>(null);

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

function getRoleLabel(role: UserRole) {
  return role === "admin" ? "管理员" : "普通用户";
}

function getRoleColor(role: UserRole) {
  return role === "admin" ? "primary" : "neutral";
}

async function fetchAdminUsers() {
  loading.value = true;
  loadError.value = "";

  try {
    const res = await api<ApiResponse<AdminUser[]>>("/api/admin/users", {
      method: "GET",
    });

    if (res.code !== 200) {
      throw new Error(res.message || "获取用户列表失败");
    }

    users.value = res.data || [];
  } catch (error) {
    users.value = [];
    loadError.value =
      error instanceof Error ? error.message : "获取用户列表失败";
  } finally {
    loading.value = false;
  }
}

async function handleDeleteUser(user: AdminUser) {
  if (user.role !== "user") return;

  const confirmed = window.confirm(
    `确定要删除用户“${user.username}”吗？此操作不可恢复。`,
  );

  if (!confirmed) return;

  deletingUserId.value = user.id;

  try {
    const res = await api<ApiResponse<null>>(`/api/admin/users/${user.id}`, {
      method: "DELETE",
    });

    if (res.code !== 200) {
      throw new Error(res.message || "删除用户失败");
    }

    users.value = users.value.filter((item) => item.id !== user.id);
  } catch (error) {
    window.alert(error instanceof Error ? error.message : "删除用户失败");
  } finally {
    deletingUserId.value = null;
  }
}

const totalCount = computed(() => users.value.length);

const adminCount = computed(
  () => users.value.filter((item) => item.role === "admin").length,
);

const userCount = computed(
  () => users.value.filter((item) => item.role === "user").length,
);

onMounted(() => {
  fetchAdminUsers();
});
</script>

<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between gap-4">
      <div>
        <h1 class="text-2xl font-bold">用户管理</h1>
        <p class="mt-2 text-sm text-muted">查看系统中的全部注册用户</p>
      </div>

      <UButton
        icon="i-lucide-refresh-cw"
        variant="soft"
        :loading="loading"
        @click="fetchAdminUsers"
      >
        刷新
      </UButton>
    </div>

    <div class="grid grid-cols-1 gap-4 md:grid-cols-3">
      <UCard class="rounded-2xl shadow-sm">
        <div class="text-sm text-muted">用户总数</div>
        <div class="mt-2 text-2xl font-bold">{{ totalCount }}</div>
      </UCard>

      <UCard class="rounded-2xl shadow-sm">
        <div class="text-sm text-muted">普通用户</div>
        <div class="mt-2 text-2xl font-bold">{{ userCount }}</div>
      </UCard>

      <UCard class="rounded-2xl shadow-sm">
        <div class="text-sm text-muted">管理员</div>
        <div class="mt-2 text-2xl font-bold">{{ adminCount }}</div>
      </UCard>
    </div>

    <UCard class="rounded-2xl shadow-sm">
      <template #header>
        <div>
          <h2 class="text-lg font-semibold">用户列表</h2>
          <p class="mt-1 text-sm text-muted">按注册时间倒序展示全部用户</p>
        </div>
      </template>

      <div
        v-if="loading"
        class="rounded-xl border border-dashed border-default px-4 py-10 text-center text-sm text-muted"
      >
        正在加载用户数据...
      </div>

      <div
        v-else-if="loadError"
        class="rounded-xl border border-red-200 bg-red-50 px-4 py-10 text-center"
      >
        <p class="text-sm text-red-600">{{ loadError }}</p>
      </div>

      <div
        v-else-if="!users.length"
        class="rounded-xl border border-dashed border-default px-4 py-10 text-center text-sm text-muted"
      >
        暂无用户数据
      </div>

      <div v-else class="grid grid-cols-1 gap-4 xl:grid-cols-2">
        <UCard
          v-for="item in users"
          :key="item.id"
          class="rounded-2xl shadow-sm"
        >
          <div class="space-y-4">
            <div class="flex items-start justify-between gap-3">
              <div>
                <h3 class="text-base font-semibold">{{ item.username }}</h3>
                <p class="mt-1 text-sm text-muted">用户 ID：{{ item.id }}</p>
              </div>

              <UBadge :color="getRoleColor(item.role)" variant="soft">
                {{ getRoleLabel(item.role) }}
              </UBadge>
            </div>

            <div class="grid grid-cols-1 gap-3 sm:grid-cols-2">
              <div class="rounded-xl bg-muted/50 px-3 py-2">
                <div class="text-sm text-muted">角色</div>
                <div class="mt-1 font-semibold">
                  {{ getRoleLabel(item.role) }}
                </div>
              </div>

              <div class="rounded-xl bg-muted/50 px-3 py-2">
                <div class="text-sm text-muted">注册时间</div>
                <div class="mt-1 font-semibold">
                  {{ formatTime(item.createTime) }}
                </div>
              </div>
            </div>

            <div
              v-if="item.role === 'user'"
              class="flex justify-end border-t border-default pt-2"
            >
              <UButton
                color="error"
                variant="soft"
                icon="i-lucide-trash-2"
                :loading="deletingUserId === item.id"
                @click="handleDeleteUser(item)"
              >
                删除用户
              </UButton>
            </div>
          </div>
        </UCard>
      </div>
    </UCard>
  </div>
</template>
