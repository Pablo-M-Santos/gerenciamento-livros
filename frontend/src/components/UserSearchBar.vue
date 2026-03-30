<template>
  <section class="toolbar-row animate-toolbar">
    <q-input
      :model-value="searchQuery"
      outlined
      dense
      rounded
      class="search-input"
      placeholder="Buscar usuarios..."
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
      class="add-user-btn animate-button"
      itemid="cadastroBtnUsuario"
      icon="add"
      label="Adicionar Usuario"
      @click="$emit('add')"
    />
  </section>
</template>

<script setup>
defineProps({
  searchQuery: {
    type: String,
    required: true,
  },
  canAdd: {
    type: Boolean,
    default: true,
  },
});

defineEmits(["update:search", "search", "clear", "add"]);
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

.search-input {
  width: min(100%, 460px);
}

.add-user-btn {
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

.add-user-btn:hover {
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

@media (max-width: 980px) {
  .toolbar-row {
    flex-direction: column;
    align-items: stretch;
    justify-content: initial;
  }

  .search-input {
    width: 100%;
  }

  .add-user-btn {
    width: 100%;
  }
}
</style>
