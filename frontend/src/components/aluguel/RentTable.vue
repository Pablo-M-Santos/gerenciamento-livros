<template>
  <q-card flat bordered class="table-card animate-table-enter">
    <q-table
      :rows="rows"
      :columns="columns"
      row-key="id"
      flat
      :loading="loading"
      :pagination="{ rowsPerPage: 0 }"
      hide-bottom
    >
      <template #loading>
        <q-inner-loading showing color="primary" />
      </template>

      <template #body-cell-status="props">
        <q-td :props="props">
          <span class="status-chip" :class="statusClass(props.row.status)">
            {{ mapStatus(props.row.status) }}
          </span>
        </q-td>
      </template>

      <template #body-cell-actions="props">
        <q-td :props="props">
          <q-btn
            v-if="isAdmin && canManage(props.row.status)"
            flat
            round
            dense
            icon="check"
            color="accent"
            :itemid="'confirmar-' + props.row.id"
            @click="$emit('return', props.row)"
          >
            <q-tooltip>Devolucao de livro</q-tooltip>
          </q-btn>

          <q-btn
            v-if="isAdmin && canManage(props.row.status)"
            flat
            round
            dense
            icon="edit"
            color="secondary"
            :itemid="'edit-' + props.row.id"
            @click="$emit('edit', props.row)"
          >
            <q-tooltip>Editar aluguel</q-tooltip>
          </q-btn>
        </q-td>
      </template>

      <template #no-data>
        <div class="q-pa-lg text-grey-7">Nenhum aluguel encontrado.</div>
      </template>
    </q-table>
  </q-card>
</template>

<script setup>
defineProps({
  rows: { type: Array, required: true },
  loading: { type: Boolean, default: false },
  isAdmin: { type: Boolean, required: true },
});

defineEmits(["edit", "return"]);

const columns = [
  {
    name: "renter",
    align: "left",
    label: "Locatario",
    field: (row) => row.renter?.name || "-",
    sortable: true,
  },
  {
    name: "book",
    align: "left",
    label: "Livro",
    field: (row) => row.book?.name || "-",
    sortable: true,
  },
  {
    name: "rentDate",
    align: "left",
    label: "Data aluguel",
    field: "rentDate",
    sortable: true,
  },
  {
    name: "deadLine",
    align: "left",
    label: "Prazo devolucao",
    field: "deadLine",
    sortable: true,
  },
  {
    name: "devolutionDate",
    align: "left",
    label: "Data devolucao",
    field: (row) => row.devolutionDate || "-",
    sortable: true,
  },
  {
    name: "status",
    align: "left",
    label: "Status",
    field: "status",
    sortable: true,
  },
  { name: "actions", align: "center", label: "Acoes", field: "actions" },
];

const mapStatus = (status) => {
  switch (status) {
    case "RENTED":
      return "Alugado";
    case "LATE":
      return "Atrasado";
    case "IN_TIME":
      return "Devolvido no prazo";
    case "DELIVERED_WITH_DELAY":
      return "Devolvido fora prazo";
    default:
      return status || "-";
  }
};

const canManage = (status) =>
  !["IN_TIME", "DELIVERED_WITH_DELAY"].includes(status);

const statusClass = (status) => {
  if (status === "RENTED") return "status-rented";
  if (status === "LATE") return "status-late";
  if (status === "IN_TIME" || status === "DELIVERED_WITH_DELAY")
    return "status-delivered";
  return "status-default";
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

.status-chip {
  display: inline-flex;
  align-items: center;
  border-radius: 999px;
  padding: 5px 12px;
  font-size: 0.78rem;
  font-weight: 700;
}

.status-rented {
  background: #e8f1e9;
  color: #2b6d39;
}

.status-late {
  background: #f6ece4;
  color: #8a4b24;
}

.status-delivered {
  background: #ecebe7;
  color: #615e54;
}

.status-default {
  background: #f0f0f0;
  color: #555;
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
