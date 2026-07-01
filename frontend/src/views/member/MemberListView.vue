<template>
  <div class="dino-page">
    <h1 class="dino-page-title">회원 목록 👥</h1>
    <p class="dino-page-subtitle">가입한 회원을 검색하고 프로필을 확인하세요.</p>

    <div class="cloud-card" style="margin-bottom: 16px">
      <input v-model="keyword" class="dino-input" placeholder="이름 또는 이메일 검색"
             @input="fetch" />
    </div>

    <div class="cloud-card list">
      <router-link
        v-for="m in members"
        :key="m.mno"
        :to="{ name: 'user-profile', params: { email: m.email } }"
        class="row"
      >
        <div class="avatar small" />
        <span class="name">{{ m.name }}</span>
        <span class="email">{{ m.email }}</span>
        <span class="tag-teal">{{ m.role }}</span>
      </router-link>
      <div v-if="!members.length" class="dino-empty"><span class="emoji">🦕</span>회원이 없습니다.</div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';
import { useFetchMembers } from '@/composables/useMemberApis';

const keyword = ref('');
const { data, execute } = useFetchMembers();
const members = computed(() => data.value?.payload?.list ?? []);

const fetch = () => execute({ keyword: keyword.value });
fetch();
</script>

<style scoped>
.list .row {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px 0;
  border-bottom: 2px dashed var(--color-beige);
  text-decoration: none;
  color: inherit;
}
.list .row:last-child {
  border: none;
}
.avatar.small {
  width: 38px;
  height: 38px;
  border-width: 2px;
}
.name {
  font-weight: 600;
}
.email {
  flex: 1;
  color: var(--color-tan);
  font-size: 13px;
}
</style>
