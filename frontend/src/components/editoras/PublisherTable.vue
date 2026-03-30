<template>
  <q-card flat bordered class="table-card animate-table-enter">
    <q-table
      :rows="rows"
      :columns="columns"
      row-key="id"
      flat
      :loading="loading"
      hide-bottom
    >
      <template #loading>
        <q-inner-loading showing color="primary" />
      </template>

      <template #body-cell-actions="props">
        <q-td :props="props" class="actions-cell">
          <q-btn
            flat
            round
            dense
            icon="visibility"
            color="primary"
            :itemid="'visibility-' + props.row.name"
            @click="$emit('view', props.row)"
          >
            <q-tooltip>Visualizar detalhes</q-tooltip>
          </q-btn>

          <q-btn
            v-if="isAdmin"
            flat
            round
            dense
            icon="edit"
            color="secondary"
            :itemid="'edit-' + props.row.name"
            @click="$emit('edit', props.row)"
          >
            <q-tooltip>Editar editora</q-tooltip>
          </q-btn>

          <q-btn
            v-if="isAdmin && !hasBooks(props.row.id)"
            flat
            round
            dense
            icon="delete"
            color="negative"
            :itemid="'delete-' + props.row.name"
            @click="$emit('delete', props.row)"
          >
            <q-tooltip>Excluir editora</q-tooltip>
          </q-btn>
        </q-td>
      </template>

      <template #no-data>
        <div class="q-pa-lg text-grey-7">Nenhuma editora encontrada.</div>
      </template>
    </q-table>
  </q-card>
</template>

<script setup>
defineProps({
  rows: {
    type: Array,
    required: true,
  },
  loading: {
    type: Boolean,
    default: false,
  },
  isAdmin: {
    type: Boolean,
    required: true,
  },
  publishersWithBooks: {
    type: Array,
    default: () => [],
  },
});

defineEmits(["view", "edit", "delete"]);

const columns = [
  { name: "name", align: "left", label: "Nome", field: "name", sortable: true },
  {
    name: "email",
    align: "left",
    label: "E-mail",
    field: "email",
    sortable: true,
  },
  {
    name: "telephone",
    align: "left",
    label: "Telefone",
    field: "telephone",
    sortable: true,
  },
  { name: "actions", align: "center", label: "Ações", field: "actions" },
];

const hasBooks = (publisherId) => {
  return publishersWithBooks.includes(publisherId);
};
</script>

<style scoped>
.table-card {
  margin-top: 18px;
  border-radius: 18px;
  border: 1px solid #dfdfdb;
  overflow: hidden;
  background: #fff;
  box-shadow: 0 8px 32px rgba(22, 24, 22, 0.08),
    inset 0 1px 0 rgba(255, 255, 255, 0.6);
  animation: fadeInScale 0.4s ease-out 0.33s forwards;
  opacity: 0;
}

:deep(.q-table thead tr th) {
  background: #f8f8f6;
  color: #4d4b42;
  font-weight: 700;
  font-size: 0.9rem;
  padding-top: 16px;
  padding-bottom: 16px;
}

:deep(.q-table tbody tr td) {
  border-color: #ecebe7;
  color: #34332f;
  font-size: 0.95rem;
  padding-top: 15px;
  padding-bottom: 15px;
}

:deep(.q-table tbody tr:hover) {
  background: #fafaf8;
}

.actions-cell {
  display: flex;
  gap: 4px;
}

@keyframes fadeInScale {
  from {
    opacity: 0;
    transform: scale(0.95) translateY(16px);
  }
  to {
    opacity: 1;
    transform: scale(1) translateY(0);
  }
}
</style>
