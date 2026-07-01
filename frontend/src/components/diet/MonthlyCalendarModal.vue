<template>
  <div class="mc-overlay" @click.self="$emit('close')">
    <div class="mc-card">
      <div class="mc-head">
        <button class="nav" aria-label="이전 달" @click="shift(-1)">‹</button>
        <div class="mc-title">{{ anchor.getFullYear() }}년 {{ anchor.getMonth() + 1 }}월</div>
        <button class="nav" aria-label="다음 달" @click="shift(1)">›</button>
        <button class="mc-close" aria-label="닫기" @click="$emit('close')">✕</button>
      </div>

      <div class="mc-weekdays">
        <span v-for="w in ['월', '화', '수', '목', '금', '토', '일']" :key="w">{{ w }}</span>
      </div>

      <div class="mc-grid">
        <button
          v-for="(cell, i) in cells"
          :key="i"
          class="mc-cell"
          :class="{ blank: !cell, sel: cell && cell.date === selected, today: cell && cell.isToday }"
          :disabled="!cell"
          @click="cell && pick(cell.date)"
        >
          <template v-if="cell">
            <span class="mc-day">{{ cell.day }}</span>
            <span v-if="cell.hasRecord" class="mc-dot" />
          </template>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';

const props = defineProps({
  selected: { type: String, required: true }, // yyyy-MM-dd
  recordSet: { type: Object, required: true }, // Set of 'MM-dd'
  today: { type: String, required: true },
});
const emit = defineEmits(['select', 'close']);

const pad = (n) => String(n).padStart(2, '0');
const toStr = (d) => `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;

const [sy, sm] = props.selected.split('-').map(Number);
const anchor = ref(new Date(sy, sm - 1, 1));

const shift = (delta) => {
  anchor.value = new Date(anchor.value.getFullYear(), anchor.value.getMonth() + delta, 1);
};

const cells = computed(() => {
  const y = anchor.value.getFullYear();
  const m = anchor.value.getMonth();
  const first = new Date(y, m, 1);
  const lead = (first.getDay() + 6) % 7; // 월요일 시작
  const daysInMonth = new Date(y, m + 1, 0).getDate();
  const out = [];
  for (let i = 0; i < lead; i++) out.push(null);
  for (let day = 1; day <= daysInMonth; day++) {
    const date = `${y}-${pad(m + 1)}-${pad(day)}`;
    out.push({ day, date, isToday: date === props.today, hasRecord: props.recordSet.has(date.slice(5)) });
  }
  return out;
});

const pick = (date) => {
  emit('select', date);
  emit('close');
};
</script>

<style scoped>
.mc-overlay {
  position: fixed;
  inset: 0;
  background: rgba(80, 61, 80, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 200;
  padding: var(--space-4);
}
.mc-card {
  background: var(--surface);
  border-radius: var(--radius-lg);
  padding: var(--space-5);
  width: 100%;
  max-width: 380px;
  box-shadow: var(--shadow-elevated);
}
.mc-head {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  margin-bottom: var(--space-4);
}
.mc-title {
  flex: 1;
  text-align: center;
  font-size: var(--fs-card-title);
  font-weight: 800;
  color: var(--text-strong);
}
.nav {
  width: 34px;
  height: 34px;
  border: none;
  background: var(--surface-muted);
  color: var(--text-muted);
  border-radius: var(--radius-md);
  font-size: 18px;
  font-weight: 700;
  cursor: pointer;
}
.nav:hover {
  background: var(--border-soft);
}
.mc-close {
  width: 34px;
  height: 34px;
  border: none;
  background: none;
  color: var(--text-muted);
  font-size: 16px;
  cursor: pointer;
}
.mc-weekdays {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  margin-bottom: var(--space-2);
}
.mc-weekdays span {
  text-align: center;
  font-size: var(--fs-label);
  font-weight: 700;
  color: var(--text-muted);
}
.mc-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 4px;
}
.mc-cell {
  position: relative;
  aspect-ratio: 1;
  border: 2px solid transparent;
  background: var(--surface-muted);
  border-radius: var(--radius-md);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: inherit;
}
.mc-cell.blank {
  background: transparent;
  cursor: default;
}
.mc-day {
  font-size: var(--fs-body-sm);
  font-weight: 700;
  color: var(--text-strong);
}
.mc-dot {
  position: absolute;
  bottom: 5px;
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background: var(--accent-dark);
}
.mc-cell.today {
  border-color: var(--accent);
}
.mc-cell.sel {
  background: var(--accent);
}
.mc-cell.sel .mc-day {
  color: #fff;
}
.mc-cell.sel .mc-dot {
  background: #fff;
}
</style>
