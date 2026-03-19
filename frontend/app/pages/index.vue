<script setup lang="ts">
import type { ButtonProps, PageFeatureProps } from "@nuxt/ui";

useSeoMeta({
  title: "宿舍共享零食柜",
  description:
    "一个面向宿舍场景的零食共享平台，用户可上架零食、购买他人商品，平台支持订单抽成与后台管理。",
});

const heroLinks: ButtonProps[] = [
  {
    label: "立即注册",
    to: "/register",
    icon: "i-lucide-user-plus",
  },
  {
    label: "去登录",
    to: "/login",
    color: "neutral",
    variant: "subtle",
    trailingIcon: "i-lucide-arrow-right",
  },
];

const features: PageFeatureProps[] = [
  {
    title: "人人都能上架",
    description:
      "注册登录后即可发布自己的零食，填写名称、价格、库存和描述就能开始售卖。",
    icon: "i-lucide-store",
  },
  {
    title: "像逛闲鱼一样浏览",
    description:
      "支持查看他人上架的零食，后续可以扩展搜索、筛选、排序、详情和购物车。",
    icon: "i-lucide-search",
  },
  {
    title: "订单自动结算",
    description:
      "买家下单后形成订单，平台按成交金额收取手续费，便于后续统计收益。",
    icon: "i-lucide-receipt",
  },
];

const steps = [
  {
    title: "注册 / 登录",
    description: "用户先创建账号并登录系统，进入平台后即可使用全部功能。",
    icon: "i-lucide-log-in",
  },
  {
    title: "上架零食",
    description:
      "填写商品名称、价格、库存、描述等信息，提交后即可展示在平台首页。",
    icon: "i-lucide-package-plus",
  },
  {
    title: "购买下单",
    description: "其他用户浏览商品后可以下单购买，系统记录订单、明细和手续费。",
    icon: "i-lucide-shopping-cart",
  },
  {
    title: "后台管理",
    description: "管理员可管理用户、商品和订单，并对平台成交数据进行统计分析。",
    icon: "i-lucide-shield-check",
  },
];

const highlights = [
  {
    label: "共享",
    value: "让宿舍闲置零食流转起来",
  },
  {
    label: "便捷",
    value: "比群聊接龙更清晰",
  },
  {
    label: "可管理",
    value: "支持后台统一上下架与统计",
  },
];
</script>

<template>
  <div>
    <UPageHero
      headline="宿舍共享零食柜"
      title="把宿舍里的零食买卖，做成一个清晰好用的小平台"
      description="用户注册登录后，既可以上架自己的零食，也可以购买别人发布的商品。管理员负责管理用户与商品，平台则对每笔成交订单收取手续费。"
      orientation="horizontal"
      :links="heroLinks"
      reverse
    >
      <div class="w-full max-w-xl">
        <UCard class="shadow-2xl ring-1 ring-default">
          <template #header>
            <div class="flex items-center justify-between">
              <div>
                <p class="text-sm text-muted">今日推荐</p>
                <h3 class="text-lg font-semibold text-highlighted">
                  宿舍零食柜
                </h3>
              </div>

              <UBadge color="primary" variant="subtle"> 共享交易 </UBadge>
            </div>
          </template>

          <div class="space-y-3">
            <div
              class="flex items-center justify-between rounded-lg border border-default px-4 py-3"
            >
              <div>
                <p class="font-medium text-highlighted">乐事薯片</p>
                <p class="text-sm text-muted">原味 · 库存 8</p>
              </div>
              <div class="text-right">
                <p class="font-semibold text-primary">¥6.00</p>
                <p class="text-xs text-muted">卖家：A 同学</p>
              </div>
            </div>

            <div
              class="flex items-center justify-between rounded-lg border border-default px-4 py-3"
            >
              <div>
                <p class="font-medium text-highlighted">可乐 330ml</p>
                <p class="text-sm text-muted">冰镇 · 库存 12</p>
              </div>
              <div class="text-right">
                <p class="font-semibold text-primary">¥3.50</p>
                <p class="text-xs text-muted">卖家：B 同学</p>
              </div>
            </div>

            <div
              class="flex items-center justify-between rounded-lg border border-default px-4 py-3"
            >
              <div>
                <p class="font-medium text-highlighted">泡面桶装</p>
                <p class="text-sm text-muted">夜宵必备 · 库存 5</p>
              </div>
              <div class="text-right">
                <p class="font-semibold text-primary">¥5.00</p>
                <p class="text-xs text-muted">卖家：C 同学</p>
              </div>
            </div>
          </div>

          <template #footer>
            <div class="flex items-center justify-between">
              <div class="text-sm text-muted">
                支持发布、购买、订单、抽成、管理
              </div>
              <UButton to="/login" size="sm" icon="i-lucide-arrow-right">
                进入平台
              </UButton>
            </div>
          </template>
        </UCard>

        <div class="mt-4 grid grid-cols-1 gap-3 sm:grid-cols-3">
          <UCard v-for="item in highlights" :key="item.label" class="shadow-sm">
            <p class="text-sm text-muted">{{ item.label }}</p>
            <p class="mt-1 text-sm font-medium text-highlighted">
              {{ item.value }}
            </p>
          </UCard>
        </div>
      </div>
    </UPageHero>

    <UPageSection
      id="features"
      headline="核心功能"
      title="这个系统能做什么"
      description="首页先把业务讲清楚，后面你再逐步补商品列表、详情、购物车、订单和后台页面。"
      :features="features"
    />

    <UPageSection
      headline="业务流程"
      title="从发布到成交，流程一眼看懂"
      description="前端页面建议围绕用户端和管理端两套视角来组织。"
    >
      <UPageGrid>
        <UPageCard
          v-for="item in steps"
          :key="item.title"
          :title="item.title"
          :description="item.description"
          :icon="item.icon"
          variant="soft"
        />
      </UPageGrid>
    </UPageSection>

    <UPageSection
      headline="为什么适合宿舍场景"
      title="简单、直接、可持续扩展"
      description="先做出一个有产品感的首页，后面再逐步接后端接口。"
    >
      <div class="grid grid-cols-1 gap-4 md:grid-cols-3">
        <UCard>
          <template #header>
            <div class="flex items-center gap-2">
              <UIcon name="i-lucide-users" class="size-5 text-primary" />
              <span class="font-semibold">适合多人共享</span>
            </div>
          </template>

          <p class="text-sm leading-6 text-muted">
            同一个宿舍、楼层或者班级都可以作为用户群体，天然适合做校园小范围交易。
          </p>
        </UCard>

        <UCard>
          <template #header>
            <div class="flex items-center gap-2">
              <UIcon
                name="i-lucide-layout-dashboard"
                class="size-5 text-primary"
              />
              <span class="font-semibold">便于后台管理</span>
            </div>
          </template>

          <p class="text-sm leading-6 text-muted">
            管理员可以统一管理用户、商品上下架和订单数据，后续也方便做统计面板。
          </p>
        </UCard>

        <UCard>
          <template #header>
            <div class="flex items-center gap-2">
              <UIcon name="i-lucide-chart-column" class="size-5 text-primary" />
              <span class="font-semibold">方便后续扩展</span>
            </div>
          </template>

          <p class="text-sm leading-6 text-muted">
            后面可以继续加搜索、购物车、支付、评价、销量排行、库存预警和平台收益分析。
          </p>
        </UCard>
      </div>
    </UPageSection>

    <UPageSection
      headline="开始使用"
      title="先注册账号，再体验完整流程"
      description="你现在可以先把登录、注册、首页介绍、上架商品这几个页面做出来，整套系统雏形就已经很明显了。"
      :links="heroLinks"
    />
  </div>
</template>
