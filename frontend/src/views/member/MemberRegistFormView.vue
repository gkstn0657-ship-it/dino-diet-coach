<template>
  <div class="dino-page narrow">
    <h1 class="dino-page-title">회원가입 🦕</h1>
    <p class="dino-page-subtitle">프로필을 입력하면 맞춤 분석이 정확해져요.</p>

    <form class="cloud-card" @submit.prevent="handleSubmit">
      <div class="dino-field">
        <label>이름</label>
        <input v-model="member.name" class="dino-input" required />
      </div>

      <div class="dino-field">
        <label>이메일</label>
        <input v-model="member.email" type="email" class="dino-input"
               placeholder="email@ssafy.com" required @blur="checkEmail(member.email)" />
        <small v-if="emailResult" :class="emailResult.canUse ? 'ok' : 'no'">
          {{ emailResult.canUse ? '사용 가능한 이메일입니다.' : '이미 사용 중인 이메일입니다.' }}
        </small>
      </div>

      <div class="dino-field">
        <label>비밀번호</label>
        <input v-model="member.password" type="password" class="dino-input" required />
      </div>

      <div class="dino-grid dino-grid-2">
        <div class="dino-field">
          <label>키 (cm)</label>
          <input v-model.number="member.height" type="number" class="dino-input" />
        </div>
        <div class="dino-field">
          <label>몸무게 (kg)</label>
          <input v-model.number="member.weight" type="number" class="dino-input" />
        </div>
      </div>

      <div class="dino-field">
        <label>질환 (선택)</label>
        <input v-model="member.disease" class="dino-input" placeholder="예: 고혈압, 당뇨 (없으면 비워두세요)" />
      </div>
      <div class="dino-field">
        <label>건강 목표</label>
        <select v-model="member.goal" class="dino-select">
          <option value="체중 감량">체중 감량</option>
          <option value="근육 증가">근육 증가</option>
          <option value="건강 유지">건강 유지</option>
          <option value="체력 향상">체력 향상</option>
        </select>
      </div>

      <div class="form-actions">
        <router-link :to="{ name: 'login-form' }" class="btn-ghost">취소</router-link>
        <button type="submit" class="btn-primary" :disabled="isLoading || emailResult?.canUse === false">
          {{ isLoading ? '가입 중...' : '가입하기' }}
        </button>
      </div>
    </form>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { useMemberStore } from '@/stores/memberStore';
import { useCheckEmail, useRegistMember } from '@/composables/useMemberApis';

const router = useRouter();
const memberStore = useMemberStore();

const member = ref({
  name: '', email: '', password: '',
  height: null, weight: null, disease: '', goal: '건강 유지',
});

const { data: emailResult, execute: checkEmail } = useCheckEmail();
const { execute, isLoading } = useRegistMember();

const handleSubmit = async () => {
  const res = await execute(member.value);
  if (res) {
    memberStore.setAlertMsg('회원가입 완료! 로그인해주세요.');
    router.push({ name: 'login-form' });
  }
};
</script>

<style scoped>
.narrow {
  max-width: 560px;
}
.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 8px;
}
.btn-ghost {
  text-decoration: none;
}
small.ok {
  color: var(--color-teal-dark);
  font-size: 12px;
}
small.no {
  color: var(--color-red);
  font-size: 12px;
}
</style>
