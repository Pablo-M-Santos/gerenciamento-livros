<template>
  <section class="toolbar-row animate-toolbar">
    <q-select
      :model-value="statusFilter"
      :options="statusOptions"
      outlined
      dense
      rounded
      emit-value
      map-options
      class="status-input"
      @update:model-value="$emit('update:status', $event)"
    />

    <q-input
      :model-value="searchQuery"
      outlined
      dense
      rounded
      class="search-input"
      placeholder="Buscar alugueis..."
      itemid="searchInput"
      @update:model-value="$emit('update:search', $event)"
      @keyup.enter="$emit('search')"
    >
      <template #prepend>
        <q-icon name="search" />
      </template>
      <template #append>
        <q-icon
          v-if="searchQuery"
          name="close"
          class="cursor-pointer"
          itemid="closeSearchBtn"
          @click="$emit('clear')"
        />
      </template>
    </q-input>

    <q-btn
      v-if="canAdd"
      unelevated
      no-caps
      class="add-rent-btn"
      itemid="cadastroBtnAluguel"
      icon="add"
      label="Adicionar Aluguel"
      @click="$emit('add')"
    />
  </section>
</template>

<script setup>
defineProps({
  searchQuery: { type: String, required: true },
  statusFilter: { type: String, required: true },
  canAdd: { type: Boolean, default: true },
});

defineEmits(["update:search", "update:status", "search", "clear", "add"]);

const statusOptions = [
  { label: "Todos", value: "" },
  { label: "Alugado", value: "RENTED" },
  { label: "Atrasado", value: "LATE" },
  { label: "Devolvido no prazo", value: "IN_TIME" },
  { label: "Devolvido fora prazo", value: "DELIVERED_WITH_DELAY" },
];
</script>

<style scoped>
.toolbar-row {
  margin-top: 20px;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 12px;
  animation: slideInUp 0.35s ease-out 0.23s forwards;
  opacity: 0;
}

.status-input {
  min-width: 220px;
}

.search-input {
  width: min(100%, 420px);
}

.add-rent-btn {
  border-radius: 13px;
  height: 42px;
  padding: 0 20px;
  background: #1f722c;
  color: #fff;
  font-weight: 700;
  box-shadow: 0 4px 12px rgba(31, 114, 44, 0.3);
  transition: box-shadow 0.3s ease;
  animation: slideInUp 0.35s ease-out 0.28s forwards;
  opacity: 0;
}

.add-rent-btn:hover {
  box-shadow: 0 6px 16px rgba(31, 114, 44, 0.4);
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

@media (max-width: 1100px) {
  .toolbar-row {
    flex-direction: column;
    align-items: stretch;
  }

  .status-input,
  .search-input,
  .add-rent-btn {
    width: 100%;
  }
}
</style>
