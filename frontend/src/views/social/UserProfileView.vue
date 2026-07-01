<template>
  <div class="dino-page">
    <!-- 프로필 헤더 -->
    <div class="cloud-card profile-head">
      <div class="avatar big" />
      <div class="p-info">
        <h1 class="p-name">{{ profile.name }}</h1>
        <div class="p-email">{{ profile.email }}</div>
        <div class="follow-stats">
          <button class="stat" :class="{ active: tab === 'followers' }" @click="tab = 'followers'">
            <b>{{ profile.followers }}</b> 팔로워
          </button>
          <button class="stat" :class="{ active: tab === 'followings' }" @click="tab = 'followings'">
            <b>{{ profile.followings }}</b> 팔로잉
          </button>
        </div>
      </div>
      <button
        v-if="!profile.isMe"
        class="btn-primary"
        :class="{ following: profile.isFollowing }"
        @click="toggleFollow"
      >
        {{ profile.isFollowing ? '팔로잉' : '팔로우' }}
      </button>
    </div>

    <!-- 팔로워/팔로잉 목록 (F111) -->
    <div class="cloud-card list">
      <div class="label-caps">{{ tab === 'followers' ? '팔로워' : '팔로잉' }} 목록</div>
      <router-link
        v-for="u in follows"
        :key="u.email"
        :to="{ name: 'user-profile', params: { email: u.email } }"
        class="user-row"
      >
        <div class="avatar small" />
        <span class="u-name">{{ u.name }}</span>
        <span class="u-email">{{ u.email }}</span>
      </router-link>
    </div>

    <!-- 작성한 글 -->
    <div class="cloud-card">
      <div class="label-caps">작성한 글 {{ profile.posts?.length || 0 }}</div>
      <router-link
        v-for="p in profile.posts"
        :key="p.bno"
        :to="{ name: 'post-detail', params: { bno: p.bno } }"
        class="mini-row"
      >
        <span class="tag-teal">{{ p.boardLabel }}</span>
        <span class="mr-title">{{ p.title }}</span>
        <span class="mr-meta">💬 {{ p.comments }} · ❤ {{ p.likes }}</span>
      </router-link>
      <p v-if="!profile.posts?.length" class="empty-line">아직 작성한 글이 없어요.</p>
    </div>

    <!-- 참여 중인 챌린지 -->
    <div class="cloud-card">
      <div class="label-caps">참여 중인 챌린지 {{ profile.joinedChallenges?.length || 0 }}</div>
      <router-link
        v-for="c in profile.joinedChallenges"
        :key="c.cno"
        :to="{ name: 'challenge-detail', params: { cno: c.cno } }"
        class="mini-row"
      >
        <span class="mr-emoji">{{ c.emoji }}</span>
        <span class="mr-title">{{ c.title }}</span>
        <span class="mr-meta">{{ c.progress ?? 0 }}%</span>
      </router-link>
      <p v-if="!profile.joinedChallenges?.length" class="empty-line">참여 중인 챌린지가 없어요.</p>
    </div>

    <!-- 신청 중인 챌린지(승인 대기) -->
    <div v-if="profile.appliedChallenges?.length" class="cloud-card">
      <div class="label-caps">신청 중인 챌린지 {{ profile.appliedChallenges.length }}</div>
      <div
        v-for="c in profile.appliedChallenges"
        :key="c.cno"
        class="mini-row"
      >
        <span class="mr-emoji">{{ c.emoji }}</span>
        <span class="mr-title">{{ c.title }}</span>
        <span class="mr-meta pending">승인 대기</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue';
import { useRoute } from 'vue-router';
import { useFetchUserProfile, useFollow, useUnfollow, useFetchFollows } from '@/composables/useFollowApis';

const route = useRoute();
const email = computed(() => route.params.email);
const tab = ref('followings');

// F111. 사용자 프로필 (실제 로드)
const { data, execute } = useFetchUserProfile();
const profile = computed(
  () => data.value ?? {
    name: '', email: '', followers: 0, followings: 0, isFollowing: false, isMe: false,
    posts: [], joinedChallenges: [], appliedChallenges: [],
  }
);

const { data: followData, execute: fetchFollows } = useFetchFollows();
const follows = computed(() => followData.value ?? []);

const { execute: follow } = useFollow();
const { execute: unfollow } = useUnfollow();
const toggleFollow = async () => {
  if (profile.value.isFollowing) await unfollow(email.value);
  else await follow(email.value);
  // 프로필(팔로워 수·상태) + 현재 탭 목록 모두 즉시 갱신
  await execute(email.value);
  await fetchFollows(email.value, tab.value);
};

watch(
  [email, tab],
  () => {
    if (!email.value) return;
    execute(email.value);
    fetchFollows(email.value, tab.value);
  },
  { immediate: true }
);
</script>

<style scoped>
.profile-head {
  display: flex;
  align-items: center;
  gap: 24px;
}
.avatar.big {
  width: 88px;
  height: 88px;
  border-width: 4px;
}
.p-info {
  flex: 1;
}
.p-name {
  font-size: 24px;
  margin: 0;
}
.p-email {
  color: var(--color-tan);
  font-size: 13px;
  margin: 4px 0 10px;
}
.follow-stats {
  display: flex;
  gap: 20px;
}
.stat {
  border: none;
  background: none;
  cursor: pointer;
  font-family: inherit;
  color: var(--color-text-dark);
  font-size: 14px;
}
.stat.active b {
  color: var(--color-teal);
}
.btn-primary.following {
  background: var(--color-tan);
}
.list .user-row {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 12px 0;
  border-bottom: 2px dashed var(--color-beige);
  text-decoration: none;
  color: inherit;
}
.avatar.small {
  width: 36px;
  height: 36px;
  border-width: 2px;
}
.u-name {
  font-weight: 600;
}
.u-email {
  color: var(--color-tan);
  font-size: 12px;
}
.mini-row {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 2px dashed var(--color-beige);
  text-decoration: none;
  color: inherit;
}
.mini-row:last-child {
  border-bottom: none;
}
.mr-emoji {
  font-size: 20px;
}
.mr-title {
  flex: 1;
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.mr-meta {
  font-size: 12px;
  color: var(--color-tan);
  flex-shrink: 0;
}
.mr-meta.pending {
  color: #9a6b16;
  font-weight: 700;
}
.empty-line {
  font-size: 13px;
  color: var(--color-tan);
  margin: 10px 0 2px;
}
</style>
