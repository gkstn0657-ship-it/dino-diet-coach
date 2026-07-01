<template>
  <!-- v-if 가 왜 필요할까? 없으면 왜 오류가 발생할까? -->
  <nav v-if="page" class="d-flex justify-content-center">
    <ul class="pagination">
      <li v-if="page.hasPre" class="page-item">
        <a class="page-link" @click.prevent="changePage(page.startPage - 1)">이전</a>
      </li>
      <li
        v-for="p in pages"
        :key="p"
        class="page-item"
        :class="{ active: page.condition?.currentPage == p }"
      >
        <a class="page-link" @click.prevent="changePage(p)">{{ p }}</a>
      </li>
      <li v-if="page.hasNext" class="page-item">
        <a class="page-link" @click.prevent="changePage(page.endPage + 1)">다음</a>
      </li>
    </ul>
  </nav>
</template>

<script setup>
import { computed } from 'vue';
const props = defineProps({
  page: {
    type: Object,
    default: null,
  },
});
const emit = defineEmits(['changePage']);

const changePage = (n) => {
  emit('changePage', n);
};

// 보여줘야 할 페이지 번호들의 배열 생성
const pages = computed(() => {
  const range = [];
  if (props.page) {
    for (let i = props.page.startPage; i <= props.page.endPage; i++) {
      range.push(i);
    }
  }
  return range;
});

// END
</script>

<style scoped>
a {
  cursor: pointer;
}
</style>
