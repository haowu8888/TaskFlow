<template>
  <div class="page-header-component">
    <div class="page-header-left">
      <slot name="back"></slot>
      <h2 class="page-header-title">{{ title }}</h2>
      <slot name="extra"></slot>
    </div>
    <div class="page-header-right">
      <slot></slot>
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{ title: string }>()
</script>

<style lang="scss" scoped>
.page-header-component {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.page-header-title {
  font-size: 24px;
  font-weight: 700;
  color: var(--tf-text-primary);
  margin: 0;
}

.page-header-right {
  display: flex;
  align-items: center;
  gap: 8px;
}
</style>
