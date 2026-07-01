<template>
  <div class="dino-page narrow">
    <div class="cloud-card profile-head">
      <div class="avatar big" />
      <div class="info">
        <h1 class="name">{{ member?.name }}</h1>
        <div class="email">{{ member?.email }}</div>
        <span class="tag-teal">{{ member?.goal || '건강 목표 미설정' }}</span>
      </div>
    </div>

    <div class="cloud-card">
      <div class="label-caps">건강 정보</div>
      <ul class="info-list">
        <li><span>키</span><b>{{ member?.height ? member.height + ' cm' : '-' }}</b></li>
        <li><span>몸무게</span><b>{{ member?.weight ? member.weight + ' kg' : '-' }}</b></li>
        <li><span>질환</span><b>{{ member?.disease || '없음' }}</b></li>
        <li><span>권한</span><b>{{ member?.role }}</b></li>
      </ul>
    </div>

    <div v-if="isMe" class="actions">
      <router-link :to="{ name: 'member-modify-form' }" class="btn-teal">프로필 수정</router-link>
      <button class="btn-ghost danger" @click="handleWithdraw">회원 탈퇴</button>
    </div>
  </div>
</template>

<script setup>
import { computed, watch } from 'vue';
import { useRoute } from 'vue-router';
import { useMemberStore } from '@/stores/memberStore';
import { useFetchMemberDetail, useDeleteMember } from '@/composables/useMemberApis';

const route = useRoute();
const memberStore = useMemberStore();

// 쿼리 email 우선, 없으면 로그인 사용자(마이페이지)
const targetEmail = computed(() => route.query.email || memberStore.loginUser?.email);
const isMe = computed(() => targetEmail.value === memberStore.loginUser?.email);

const { data: member, execute } = useFetchMemberDetail();
const { execute: deleteMember } = useDeleteMember();

const handleWithdraw = () => {
  if (confirm('정말 탈퇴하시겠어요? 되돌릴 수 없습니다.')) {
    deleteMember(member.value);
  }
};

watch(targetEmail, (email) => email && execute(email), { immediate: true });
</script>

<style scoped>
.narrow {
  max-width: 560px;
  display: flex;
  flex-direction: column;
  gap: var(--space-4, 16px);
}
.profile-head {
  display: flex;
  align-items: center;
  gap: 20px;
}
.avatar.big {
  width: 80px;
  height: 80px;
  border-width: 4px;
}
.name {
  font-size: 24px;
  margin: 0;
}
.email {
  color: var(--color-tan);
  font-size: 13px;
  margin: 4px 0 8px;
}
.info-list {
  list-style: none;
  padding: 0;
  margin: 12px 0 0;
}
.info-list li {
  display: flex;
  justify-content: space-between;
  padding: 12px 0;
  border-bottom: 2px dashed var(--color-beige);
}
.info-list li:last-child {
  border: none;
}
.info-list span {
  color: var(--color-tan);
  font-weight: 600;
}
.actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: var(--space-2, 8px);
}
.btn-teal {
  text-decoration: none;
}
.btn-ghost.danger {
  color: var(--color-red);
  border-color: var(--color-red);
}
</style>
