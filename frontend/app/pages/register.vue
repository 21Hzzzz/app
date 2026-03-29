<script setup lang="ts">
import * as z from "zod";
import type { FormSubmitEvent } from "@nuxt/ui";

definePageMeta({
  layout: false
});

const api = useApi();

const PHONE_PATTERN = /^1\d{10}$/;
const DEFAULT_SMS_CODE_LENGTH = 4;
const MIN_SMS_CODE_LENGTH = 4;
const MAX_SMS_CODE_LENGTH = 8;

const smsCodeLength = ref(DEFAULT_SMS_CODE_LENGTH);
const smsExpiresInSeconds = ref(300);
const smsCodeDigits = ref<number[]>([]);
const loading = ref(false);
const sendingSmsCode = ref(false);
const smsCountdown = ref(0);
const errorMsg = ref("");
const successMsg = ref("");

let smsCountdownTimer: ReturnType<typeof setInterval> | null = null;

const smsCodePattern = computed(
  () => new RegExp(`^\\d{${smsCodeLength.value}}$`)
);

const schema = z
  .object({
    username: z.string().min(3, "账号至少需要 3 个字符").max(20, "账号最多 20 个字符"),
    phone: z.string().regex(PHONE_PATTERN, "请输入 11 位大陆手机号"),
    smsCode: z.string().min(1, "请输入短信验证码"),
    password: z.string().min(6, "密码至少需要 6 个字符").max(20, "密码最多 20 个字符"),
    confirmPassword: z.string().min(6, "确认密码至少需要 6 个字符").max(20, "确认密码最多 20 个字符"),
    role: z.enum(["user", "admin"], {
      error: "请选择角色"
    })
  })
  .superRefine((data, ctx) => {
    if (data.password !== data.confirmPassword) {
      ctx.addIssue({
        code: z.ZodIssueCode.custom,
        message: "两次输入的密码不一致",
        path: ["confirmPassword"]
      });
    }

    if (!smsCodePattern.value.test(data.smsCode.trim())) {
      ctx.addIssue({
        code: z.ZodIssueCode.custom,
        message: `请输入 ${smsCodeLength.value} 位短信验证码`,
        path: ["smsCode"]
      });
    }
  });

type Schema = z.output<typeof schema>;

type ApiResponse<T = null> = {
  code: number;
  message: string;
  data: T;
};

type SendSmsCodeData = {
  cooldownSeconds: number;
  expiresInSeconds: number;
  codeLength: number;
};

const state = reactive<Schema>({
  username: "",
  phone: "",
  smsCode: "",
  password: "",
  confirmPassword: "",
  role: "user"
});

const canSendSmsCode = computed(() => {
  return (
    PHONE_PATTERN.test(normalizePhone(state.phone)) &&
    smsCountdown.value === 0 &&
    !sendingSmsCode.value
  );
});

const smsButtonLabel = computed(() => {
  if (smsCountdown.value > 0) {
    return `${smsCountdown.value}s 后重发`;
  }

  return "获取验证码";
});

const smsCodeDescription = computed(() => {
  return `验证码长度为 ${smsCodeLength.value} 位，发送后 ${Math.max(1, Math.round(smsExpiresInSeconds.value / 60))} 分钟内有效。`;
});

watch(
  smsCodeDigits,
  (value) => {
    state.smsCode = value.join("").slice(0, smsCodeLength.value);
  },
  { deep: true }
);

function normalizePhone(value: string) {
  return value.trim();
}

function clearMessages() {
  errorMsg.value = "";
  successMsg.value = "";
}

function stopSmsCountdown() {
  if (smsCountdownTimer) {
    clearInterval(smsCountdownTimer);
    smsCountdownTimer = null;
  }
}

function startSmsCountdown(seconds: number) {
  stopSmsCountdown();
  smsCountdown.value = seconds;

  smsCountdownTimer = setInterval(() => {
    if (smsCountdown.value <= 1) {
      smsCountdown.value = 0;
      stopSmsCountdown();
      return;
    }

    smsCountdown.value -= 1;
  }, 1000);
}

function applySmsCodeLength(length?: number) {
  const nextLength = Math.min(
    MAX_SMS_CODE_LENGTH,
    Math.max(MIN_SMS_CODE_LENGTH, Number(length) || DEFAULT_SMS_CODE_LENGTH)
  );

  smsCodeLength.value = nextLength;
  state.smsCode = "";
  smsCodeDigits.value = [];
}

async function sendSmsCode() {
  const phone = normalizePhone(state.phone);

  if (!PHONE_PATTERN.test(phone)) {
    errorMsg.value = "请输入 11 位大陆手机号";
    successMsg.value = "";
    return;
  }

  try {
    sendingSmsCode.value = true;
    clearMessages();

    const res = await api<ApiResponse<SendSmsCodeData>>("/api/auth/register/sms-code", {
      method: "POST",
      body: {
        phone
      }
    });

    if (res.code !== 200 || !res.data) {
      throw new Error(res.message || "验证码发送失败，请稍后重试");
    }

    state.phone = phone;
    applySmsCodeLength(res.data.codeLength);
    smsExpiresInSeconds.value = res.data.expiresInSeconds || 300;
    startSmsCountdown(res.data.cooldownSeconds || 60);

    const expiresMinutes = Math.max(1, Math.round(smsExpiresInSeconds.value / 60));
    successMsg.value = `验证码已发送，请注意查收。当前验证码为 ${smsCodeLength.value} 位，${expiresMinutes} 分钟内有效。`;
  } catch (error: any) {
    errorMsg.value =
      error?.data?.message || error?.message || "验证码发送失败，请稍后重试";
  } finally {
    sendingSmsCode.value = false;
  }
}

async function onSubmit(event: FormSubmitEvent<Schema>) {
  try {
    loading.value = true;
    clearMessages();

    const res = await api<ApiResponse>("/api/auth/register", {
      method: "POST",
      body: {
        username: event.data.username.trim(),
        phone: normalizePhone(event.data.phone),
        smsCode: event.data.smsCode.trim(),
        password: event.data.password,
        role: event.data.role
      }
    });

    if (res.code !== 200) {
      throw new Error(res.message || "注册失败，请稍后重试");
    }

    successMsg.value = res.message || "注册成功";

    setTimeout(() => {
      navigateTo("/login");
    }, 1500);
  } catch (error: any) {
    errorMsg.value =
      error?.data?.message || error?.message || "注册失败，请稍后重试";
  } finally {
    loading.value = false;
  }
}

onBeforeUnmount(() => {
  stopSmsCountdown();
});
</script>

<template>
  <div
    class="flex min-h-screen items-center justify-center bg-[radial-gradient(circle_at_top,_rgba(59,130,246,0.14),_transparent_35%),linear-gradient(180deg,_#f8fafc_0%,_#eef4ff_100%)] px-4 py-10"
  >
    <UCard class="w-full max-w-lg border border-default/60 shadow-xl">
      <template #header>
        <div class="text-center">
          <h1 class="text-2xl font-bold">注册</h1>
          <p class="mt-1 text-sm text-muted">填写账号、手机号和短信验证码完成注册</p>
        </div>
      </template>

      <div class="space-y-4">
        <UAlert
          color="info"
          variant="soft"
          title="短信认证使用提示"
          description="推荐使用 PNVS 控制台中的赠送签名和赠送模板；测试阶段仅支持已绑定的测试手机号；发送失败可到 PNVS 控制台的发送记录中排查。"
        />

        <UAlert
          v-if="errorMsg"
          color="error"
          variant="soft"
          :title="errorMsg"
        />

        <UAlert
          v-if="successMsg"
          color="success"
          variant="soft"
          :title="successMsg"
        />

        <UForm
          :schema="schema"
          :state="state"
          class="space-y-5"
          @submit="onSubmit"
        >
          <UFormField label="账号" name="username" required>
            <UInput
              v-model="state.username"
              placeholder="请输入账号"
              autocomplete="username"
              class="w-full"
            />
          </UFormField>

          <UFormField label="手机号" name="phone" required>
            <div class="flex flex-col gap-3 sm:flex-row">
              <UInput
                v-model="state.phone"
                type="tel"
                inputmode="numeric"
                autocomplete="tel"
                placeholder="请输入 11 位手机号"
                class="w-full"
              />

              <UButton
                type="button"
                color="neutral"
                variant="soft"
                :loading="sendingSmsCode"
                :disabled="!canSendSmsCode"
                class="shrink-0"
                @click="sendSmsCode"
              >
                {{ smsButtonLabel }}
              </UButton>
            </div>
          </UFormField>

          <UFormField
            label="短信验证码"
            name="smsCode"
            required
            :description="smsCodeDescription"
          >
            <UPinInput
              v-model="smsCodeDigits"
              type="number"
              otp
              :length="smsCodeLength"
              placeholder="○"
              class="w-full"
            />
          </UFormField>

          <UFormField label="密码" name="password" required>
            <UInput
              v-model="state.password"
              type="password"
              placeholder="请输入密码"
              autocomplete="new-password"
              class="w-full"
            />
          </UFormField>

          <UFormField label="确认密码" name="confirmPassword" required>
            <UInput
              v-model="state.confirmPassword"
              type="password"
              placeholder="请再次输入密码"
              autocomplete="new-password"
              class="w-full"
            />
          </UFormField>

          <UFormField label="角色" name="role" required>
            <URadioGroup
              v-model="state.role"
              :items="[
                { label: '普通用户', value: 'user' },
                { label: '管理员', value: 'admin' }
              ]"
              orientation="horizontal"
              class="w-full"
            />
          </UFormField>

          <UButton type="submit" block :loading="loading">
            注册
          </UButton>
        </UForm>
      </div>

      <template #footer>
        <div class="text-center text-sm text-muted">
          已有账号？
          <NuxtLink to="/login" class="font-medium text-primary">
            去登录
          </NuxtLink>
        </div>
      </template>
    </UCard>
  </div>
</template>
