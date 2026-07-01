<template>
  <div class="wcal cloud-card">
    <button class="nav" aria-label="이전 주" @click="$emit('prev')">‹</button>

    <div class="days">
      <button
        v-for="d in days"
        :key="d.date"
        class="day"
        :class="{ sel: d.isSelected, today: d.isToday, empty: !d.hasRecord }"
        @click="$emit('select', d.date)"
      >
        <span class="wd">{{ d.weekday }}</span>
        <span class="dn">{{ d.day }}</span>
        <span class="kc">{{ d.hasRecord ? d.kcal.toLocaleString() : '-' }}</span>
      </button>
    </div>

    <button class="nav" aria-label="다음 주" @click="$emit('next')">›</button>
    <button class="month-btn" @click="$emit('open-month')">📅 월간 보기</button>
  </div>
</template>

<script setup>
defineProps({ days: { type: Array, required: true } });
defineEmits(['select', 'prev', 'next', 'open-month']);
</script>

<style scoped>
.wcal {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-soft);
  padding: var(--space-4);
}
.nav {
  flex-shrink: 0;
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: var(--surface-muted);
  color: var(--text-muted);
  border-radius: var(--radius-md);
  font-size: 20px;
  font-weight: 700;
  cursor: pointer;
  line-height: 1;
  padding: 0;
  text-align: center;
}
.nav:hover {
  background: var(--border-soft);
}
.days {
  display: flex;
  gap: var(--space-2);
  flex: 1;
  min-width: 0;
  overflow-x: auto;
}
.day {
  flex: 1;
  min-width: 56px;
  border: 2px solid transparent;
  background: var(--surface-muted);
  border-radius: var(--radius-md);
  padding: 8px 4px;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  transition: all 0.15s ease;
}
.day .wd {
  font-size: var(--fs-label);
  font-weight: 700;
  color: var(--text-muted);
}
.day .dn {
  font-size: var(--fs-card-title);
  font-weight: 800;
  color: var(--text-strong);
}
.day .kc {
  font-size: 11px;
  font-weight: 700;
  color: var(--accent-dark);
}
.day.empty .kc {
  color: var(--text-muted);
  opacity: 0.7;
}
.day.today {
  border-color: var(--accent);
}
.day.sel {
  background: var(--accent);
  box-shadow: var(--shadow-interactive);
  border-color: var(--accent-dark);
}
.day.sel .wd,
.day.sel .dn,
.day.sel .kc {
  color: #fff;
}
.month-btn {
  flex-shrink: 0;
  border: none;
  background: var(--surface);
  color: var(--accent-dark);
  border: 2px solid var(--border-soft);
  border-radius: var(--radius-pill);
  padding: 8px 14px;
  font-family: inherit;
  font-weight: 700;
  font-size: var(--fs-body-sm);
  cursor: pointer;
  white-space: nowrap;
}
.month-btn:hover {
  border-color: var(--accent);
}

@media (max-width: 768px) {
  .month-btn {
    display: none;
  }
  .day {
    min-width: 52px;
  }
}
</style>
