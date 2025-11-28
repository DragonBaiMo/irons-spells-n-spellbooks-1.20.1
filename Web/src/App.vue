<script setup lang="ts">
import { NConfigProvider, NGlobalStyle, NMessageProvider, darkTheme } from 'naive-ui';
import MainLayout from './MainLayout.vue';
</script>

<template>
  <NConfigProvider :theme="darkTheme">
    <NGlobalStyle />
    <NMessageProvider>
        <MainLayout />
    </NMessageProvider>
  </NConfigProvider>
</template>
