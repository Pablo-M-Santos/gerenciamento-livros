<template>
  <q-page class="rents-page">
    <section class="rents-header animate-header">
      <h1>Gerenciamento de Alugueis</h1>
      <p>Gerencie os alugueis da sua biblioteca digital</p>
    </section>

    <section class="stats-grid">
      <RentStatsCard
        :card-index="1"
        icon="receipt_long"
        label="Total de Alugueis"
        :value="summary.total"
        type="total"
      />
      <RentStatsCard
        :card-index="2"
        icon="schedule"
        label="Ativos"
        :value="summary.active"
        type="active"
      />
      <RentStatsCard
        :card-index="3"
        icon="done_all"
        label="Finalizados"
        :value="summary.finished"
        type="finished"
      />
    </section>

    <RentSearchBar
      :search-query="searchQuery"
      :status-filter="statusFilter"
      :can-add="userRole === 'ADMIN'"
      @update:search="searchQuery = $event"
      @update:status="handleStatusChange"
      @search="handleSearch"
      @clear="clearSearch"
      @add="openRegisterDialog"
    />

    <RentTable
      :rows="rows"
      :loading="loading"
      :is-admin="userRole === 'ADMIN'"
      @edit="openEditDialog"
      @return="openReturnDialog"
    />

    <div v-if="totalPages > 1" class="pagination-wrap">
      <q-pagination
        :model-value="page"
        :max="totalPages"
        direction-links
        boundary-links
        color="positive"
        active-design="unelevated"
        class="rents-pagination"
        @update:model-value="handlePageChange"
      />
    </div>

    <RentFormModal
      v-model="showModalCadastro"
      :is-edit-mode="false"
      :initial-data="rentCreate"
      :renter-options="renterOptions"
      :book-options="bookOptions"
      :today="today"
      :max-return-date="maxReturnDate"
      @submit="submitCreate"
      @filter-renter="filterRenter"
      @filter-book="filterBook"
    />

    <RentFormModal
      v-model="showModalEditar"
      :is-edit-mode="true"
      :initial-data="rentEdit"
      :renter-options="renterOptions"
      :book-options="bookOptions"
      :today="today"
      :max-return-date="maxReturnDate"
      @submit="submitEdit"
      @filter-renter="filterRenter"
      @filter-book="filterBook"
    />

    <RentReturnModal
      v-model="showModalDevolucao"
      :target-name="returnTargetLabel"
      @confirm="confirmReturn"
    />
  </q-page>
</template>

<script setup>
import { computed, onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import { Notify } from "quasar";
import { api } from "src/boot/axios.js";

import RentStatsCard from "src/components/aluguel/RentStatsCard.vue";
import RentSearchBar from "src/components/aluguel/RentSearchBar.vue";
import RentTable from "src/components/aluguel/RentTable.vue";
import RentFormModal from "src/components/aluguel/RentFormModal.vue";
import RentReturnModal from "src/components/aluguel/RentReturnModal.vue";

const router = useRouter();

const loading = ref(false);
const userRole = ref(localStorage.getItem("role") || "");
const searchQuery = ref("");
const statusFilter = ref("");
const page = ref(1);
const rowsNumber = ref(0);
const pageSize = 8;

const rows = ref([]);

const showModalCadastro = ref(false);
const showModalEditar = ref(false);
const showModalDevolucao = ref(false);

const returnTarget = ref(null);

const rentCreate = ref({
  id: null,
  renterId: null,
  bookId: null,
  deadLine: "",
});

const rentEdit = ref({
  id: null,
  renterId: null,
  bookId: null,
  deadLine: "",
});

const renterOptions = ref([]);
const allRenters = ref([]);
const bookOptions = ref([]);
const allBooks = ref([]);

const today = new Date().toISOString().split("T")[0];
const maxReturnDate = new Date(new Date().setDate(new Date().getDate() + 29))
  .toISOString()
  .split("T")[0];

const notify = (type, message) => {
  Notify.create({ type, message, position: "top", timeout: 2200 });
};

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

const normalizeDate = (val) => {
  if (!val) return "";
  if (typeof val === "string" && val.length >= 10) return val.slice(0, 10);
  return "";
};

const normalizeRent = (row) => ({
  id: row?.id ?? null,
  renter: row?.renter ?? null,
  renterId: row?.renterId ?? row?.renter?.id ?? null,
  book: row?.book ?? null,
  bookId: row?.bookId ?? row?.book?.id ?? null,
  rentDate: normalizeDate(row?.rentDate),
  deadLine: normalizeDate(row?.deadLine),
  devolutionDate: normalizeDate(row?.devolutionDate),
  status: row?.status ?? "",
  statusLabel: mapStatus(row?.status),
});

const extractPageData = (payload) => {
  const list = Array.isArray(payload?.content)
    ? payload.content
    : Array.isArray(payload)
      ? payload
      : [];

  const total =
    typeof payload?.totalElements === "number" ? payload.totalElements : list.length;
  const currentPage = typeof payload?.number === "number" ? payload.number + 1 : 1;

  return { list, total, currentPage };
};

const summary = computed(() => {
  const total = rowsNumber.value;
  const active = rows.value.filter((row) =>
    ["RENTED", "LATE"].includes(row.status)
  ).length;
  const finished = rows.value.filter((row) =>
    ["IN_TIME", "DELIVERED_WITH_DELAY"].includes(row.status)
  ).length;

  return { total, active, finished };
});

const totalPages = computed(() => {
  return Math.max(1, Math.ceil(rowsNumber.value / pageSize));
});

const returnTargetLabel = computed(() => {
  if (!returnTarget.value) return "";
  const renter = returnTarget.value.renter?.name || "locatario";
  const book = returnTarget.value.book?.name || "livro";
  return `${renter} (${book})`;
});

const loadRows = async (
  search = "",
  status = statusFilter.value,
  targetPage = page.value
) => {
  loading.value = true;
  try {
    const response = await api.get("/rent", {
      params: {
        search: search || undefined,
        page: targetPage - 1,
        status: status || undefined,
      },
    });

    const { list, total, currentPage } = extractPageData(response.data);
    rows.value = list.map(normalizeRent);
    rowsNumber.value = total;
    page.value = currentPage;
  } catch (error) {
    rows.value = [];
    rowsNumber.value = 0;
    notify("negative", "Erro ao carregar alugueis.");
  } finally {
    loading.value = false;
  }
};

const loadRenters = async (search = "") => {
  try {
    const response = await api.get("/renter", {
      params: { search: search || undefined },
    });
    const data = response.data?.content || response.data || [];
    allRenters.value = Array.isArray(data) ? data : [];
    renterOptions.value = [...allRenters.value];
  } catch (error) {
    allRenters.value = [];
    renterOptions.value = [];
  }
};

const loadBooks = async (search = "") => {
  try {
    const response = await api.get("/book", {
      params: { search: search || undefined },
    });
    const data = response.data?.content || response.data || [];
    allBooks.value = Array.isArray(data) ? data : [];
    bookOptions.value = [...allBooks.value];
  } catch (error) {
    allBooks.value = [];
    bookOptions.value = [];
  }
};

const openRegisterDialog = () => {
  rentCreate.value = {
    id: null,
    renterId: null,
    bookId: null,
    deadLine: "",
  };
  showModalCadastro.value = true;
};

const submitCreate = async (formData) => {
  if (!formData.renterId || !formData.bookId || !formData.deadLine) {
    notify("negative", "Preencha os campos obrigatorios para cadastrar.");
    return;
  }

  try {
    await api.post("/rent", {
      renterId: Number(formData.renterId),
      bookId: Number(formData.bookId),
      rentDate: today,
      deadLine: normalizeDate(formData.deadLine),
    });

    notify("positive", "Aluguel cadastrado com sucesso.");
    showModalCadastro.value = false;
    await loadRows(searchQuery.value, statusFilter.value);
  } catch (error) {
    notify(
      "negative",
      error.response?.data?.message || "Erro ao cadastrar aluguel."
    );
  }
};

const openEditDialog = (row) => {
  rentEdit.value = {
    id: row.id,
    renterId: row.renter?.id || null,
    bookId: row.book?.id || null,
    deadLine: normalizeDate(row.deadLine),
  };
  showModalEditar.value = true;
};

const submitEdit = async (formData) => {
  if (!rentEdit.value.id) {
    notify("negative", "Aluguel nao selecionado.");
    return;
  }

  if (!formData.renterId || !formData.bookId || !formData.deadLine) {
    notify("negative", "Preencha os campos obrigatorios para atualizar.");
    return;
  }

  try {
    await api.put(`/rent/update/${rentEdit.value.id}`, {
      id: rentEdit.value.id,
      renterId: Number(formData.renterId),
      bookId: Number(formData.bookId),
      deadLine: normalizeDate(formData.deadLine),
    });

    notify("positive", "Aluguel atualizado com sucesso.");
    showModalEditar.value = false;
    await loadRows(searchQuery.value, statusFilter.value);
  } catch (error) {
    notify(
      "negative",
      error.response?.data?.message || "Erro ao atualizar aluguel."
    );
  }
};

const openReturnDialog = (row) => {
  if (!row || ["IN_TIME", "DELIVERED_WITH_DELAY"].includes(row.status)) {
    notify("negative", "Este aluguel ja foi finalizado.");
    return;
  }

  returnTarget.value = row;
  showModalDevolucao.value = true;
};

const confirmReturn = async () => {
  if (!returnTarget.value?.id) {
    notify("negative", "Nenhum aluguel selecionado para devolucao.");
    return;
  }

  try {
    await api.put(`/rent/${returnTarget.value.id}`, { status: "ENTREGUE" });
    notify("positive", "Status atualizado com sucesso.");
    showModalDevolucao.value = false;
    returnTarget.value = null;
    await loadRows(searchQuery.value, statusFilter.value);
  } catch (error) {
    notify(
      "negative",
      error.response?.data?.message || "Erro ao atualizar status."
    );
  }
};

const filterRenter = (val, update) => {
  if (val === "") {
    update(() => {
      renterOptions.value = [...allRenters.value];
    });
    return;
  }

  const needle = val.toLowerCase();
  update(() => {
    renterOptions.value = allRenters.value.filter((renter) =>
      String(renter.name || "")
        .toLowerCase()
        .includes(needle)
    );
  });
};

const filterBook = (val, update) => {
  if (val === "") {
    update(() => {
      bookOptions.value = [...allBooks.value];
    });
    return;
  }

  const needle = val.toLowerCase();
  update(() => {
    bookOptions.value = allBooks.value.filter((book) =>
      String(book.name || "")
        .toLowerCase()
        .includes(needle)
    );
  });
};

const handleSearch = () => {
  page.value = 1;
  loadRows(searchQuery.value, statusFilter.value, 1);
};

const clearSearch = () => {
  searchQuery.value = "";
  page.value = 1;
  loadRows("", statusFilter.value, 1);
};

const handleStatusChange = (value) => {
  statusFilter.value = value || "";
  page.value = 1;
  loadRows(searchQuery.value, statusFilter.value, 1);
};

const handlePageChange = (newPage) => {
  if (!newPage || newPage === page.value) return;
  page.value = newPage;
  loadRows(searchQuery.value, statusFilter.value, newPage);
};

onMounted(async () => {
  const token = localStorage.getItem("authToken");
  if (!token) {
    router.push("/login");
    return;
  }

  userRole.value = localStorage.getItem("role") || "";
  await Promise.all([loadRows(), loadRenters(), loadBooks()]);
});
</script>

<style scoped>
.rents-page {
  padding: 34px 60px;
  background: #f5f5f3;
  min-height: 100vh;
  margin: 0 auto;
}

.rents-header {
  animation: slideInDown 0.35s ease-out forwards;
  opacity: 0;
}

.rents-header h1 {
  margin: 0;
  font-size: 2rem;
  color: #222;
  font-weight: 700;
}

.rents-header p {
  color: #6b6b64;
  font-size: 1rem;
}

.stats-grid {
  margin-top: 24px;
  display: grid;
  grid-template-columns: repeat(3, minmax(180px, 1fr));
  gap: 16px;
}

.pagination-wrap {
  margin: 18px auto 0;
  width: fit-content;
  padding: 8px 10px;
  border: 1px solid #e7e6e2;
  border-radius: 12px;
  background: #fff;
}

:deep(.rents-pagination .q-btn) {
  min-width: 34px;
  min-height: 34px;
  border-radius: 8px;
  font-weight: 600;
}

:deep(.rents-pagination .q-btn:not(.bg-positive)) {
  background: #f2f2ef;
  color: #5f5c54;
}

:deep(.rents-pagination .q-btn.bg-positive) {
  background: #1f722c !important;
  box-shadow: 0 3px 10px rgba(31, 114, 44, 0.22);
}

@keyframes slideInDown {
  from {
    opacity: 0;
    transform: translateY(-16px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@media (max-width: 980px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 600px) {
  .rents-page {
    padding: 18px;
  }

  .rents-header h1 {
    font-size: 1.5rem;
  }
}
</style>
