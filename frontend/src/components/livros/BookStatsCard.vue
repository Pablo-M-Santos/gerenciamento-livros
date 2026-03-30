<template>
  <q-card flat bordered class="stat-card" :class="`stat-card-${cardIndex}`">
    <div class="stat-icon" :class="`stat-icon-${type}`">
      <q-icon :name="icon" size="20px" />
    </div>
    <div class="stat-info">
      <span>{{ label }}</span>
      <strong>{{ value }}</strong>
    </div>
  </q-card>
</template>

<script setup>
defineProps({
  cardIndex: {
    type: Number,
    required: true,
  },
  icon: {
    type: String,
    required: true,
  },
  label: {
    type: String,
    required: true,
  },
  value: {
    type: Number,
    required: true,
  },
  type: {
    type: String,
    required: true,
    validator: (value) => ["total", "available", "rented"].includes(value),
  },
});
</script>

<style scoped>
.stat-card {
  border-radius: 18px;
  min-height: 98px;
  padding: 18px 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  background: #fff;
  border: 1px solid #dfdfdb;
  box-shadow: 0 4px 16px rgba(22, 24, 22, 0.05),
    inset 0 1px 0 rgba(255, 255, 255, 0.8);
  transition: box-shadow 0.3s ease, transform 0.3s ease;
  animation: slideInUp 0.35s ease-out forwards;
  opacity: 0;
}

.stat-card:hover {
  box-shadow: 0 8px 28px rgba(22, 24, 22, 0.12),
    inset 0 1px 0 rgba(255, 255, 255, 0.8);
  transform: translateY(-2px) scale(1.02);
}

.stat-card-1 {
  animation-delay: 0.08s;
}

.stat-card-2 {
  animation-delay: 0.13s;
}

.stat-card-3 {
  animation-delay: 0.18s;
}

.stat-icon {
  width: 42px;
  height: 42px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-icon-total {
  background: #e8f1e9;
  color: #286f37;
}

.stat-icon-available {
  background: #e5efe6;
  color: #2e7a3a;
}

.stat-icon-rented {
  background: #ece9e1;
  color: #5d5546;
}

.stat-info {
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.stat-info span {
  color: #75736a;
  font-size: 0.92rem;
}

.stat-info strong {
  color: #1f1f1f;
  font-size: 2rem;
  line-height: 1;
  margin-top: 2px;
}

@keyframes slideInUp {
  from {
    opacity: 0;
    transform: translateY(24px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
