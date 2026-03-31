<template>
  <q-page class="editoras-page">
    <section class="editoras-header animate-header">
      <h1>Gerenciamento de Editoras</h1>
      <p>Gerencie as editoras da sua biblioteca digital</p>
    </section>

    <section class="stats-grid">
      <PublisherStatsCard
        :card-index="1"
        icon="business"
        label="Total de Editoras"
        :value="summary.total"
        type="total"
      />
      <PublisherStatsCard
        :card-index="2"
        icon="library_books"
        label="Ativas"
        :value="summary.withBooks"
        type="with-books"
      />
      <PublisherStatsCard
        :card-index="3"
        icon="menu_book"
        label="Deletadas"
        :value="summary.withoutBooks"
        type="without-books"
      />
    </section>

    <PublisherSearchBar
      :search-query="searchQuery"
      :can-add="userRole === 'ADMIN'"
      @update:search="searchQuery = $event"
      @search="handleSearch"
      @clear="clearSearch"
      @add="openRegisterCadastro"
    />

    <PublisherTable
      :rows="rows"
      :loading="loading"
      :is-admin="userRole === 'ADMIN'"
      :publishers-with-books="publishersWithBooks"
      @view="showDetails"
      @edit="editRow"
      @delete="askDelete"
    />

    <div v-if="totalPages > 1" class="pagination-wrap">
      <q-pagination
        :model-value="page"
        :max="totalPages"
        direction-links
        boundary-links
        color="positive"
        active-design="unelevated"
        class="editoras-pagination"
        @update:model-value="handlePageChange"
      />
    </div>

    <PublisherFormModal
      v-model="showModalCadastro"
      :is-edit-mode="false"
      :initial-data="{ id: null, name: '', email: '', telephone: '', site: '' }"
      @submit="submitFormCadastro"
    />

    <PublisherFormModal
      v-model="showModalEditar"
      :is-edit-mode="true"
      :initial-data="formEditar"
      @submit="submitFormEditar"
    />

    <PublisherDetailsModal
      v-model="showModalSobre"
      :publisher-data="selectedRow"
    />

    <PublisherDeleteModal
      v-model="showModalExcluir"
      :target-name="deleteTarget?.name || ''"
      @confirm="confirmDelete"
    />
  </q-page>
</template>

<script setup>
import { computed, ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import { api } from "src/boot/axios.js";
import { Notify } from "quasar";

import PublisherStatsCard from "src/components/editoras/PublisherStatsCard.vue";
import PublisherSearchBar from "src/components/editoras/PublisherSearchBar.vue";
import PublisherTable from "src/components/editoras/PublisherTable.vue";
import PublisherFormModal from "src/components/editoras/PublisherFormModal.vue";
import PublisherDetailsModal from "src/components/editoras/PublisherDetailsModal.vue";
import PublisherDeleteModal from "src/components/editoras/PublisherDeleteModal.vue";

const router = useRouter();

const userRole = ref("");
const searchQuery = ref("");
const rows = ref([]);
const page = ref(1);
const rowsNumber = ref(0);
const pageSize = 8;
const loading = ref(false);
const publishersWithBooks = ref([]);

const countActive = ref(0);
const countDeleted = ref(0);
const countTotal = ref(0);

const showModalCadastro = ref(false);
const showModalEditar = ref(false);
const showModalSobre = ref(false);
const showModalExcluir = ref(false);

const formEditar = ref({
  id: null,
  name: "",
  email: "",
  telephone: "",
  site: "",
});

const selectedRow = ref({
  id: null,
  name: "",
  email: "",
  telephone: "",
  site: "",
});

const deleteTarget = ref(null);

const summary = computed(() => {
  return {
    total: countTotal.value,
    withBooks: countActive.value,
    withoutBooks: countDeleted.value,
  };
});

const totalPages = computed(() => {
  return Math.max(1, Math.ceil(rowsNumber.value / pageSize));
});

const notify = (type, message) => {
  Notify.create({ type, message, position: "top", timeout: 2200 });
};

const extractPageData = (payload) => {
  const list = Array.isArray(payload?.content)
    ? payload.content
    : Array.isArray(payload)
    ? payload
    : [];

  const total =
    typeof payload?.totalElements === "number"
      ? payload.totalElements
      : list.length;
  const currentPage =
    typeof payload?.number === "number" ? payload.number + 1 : 1;

  return { list, total, currentPage };
};

const loadPublishers = async (search = "", targetPage = page.value) => {
  loading.value = true;
  try {
    const response = await api.get("/publisher", {
      params: { search: search || undefined, page: targetPage - 1 },
    });
    const { list, total, currentPage } = extractPageData(response.data);
    rows.value = list;
    rowsNumber.value = total;
    page.value = currentPage;
  } catch (error) {
    notify("negative", "Erro ao carregar editoras.");
    rows.value = [];
    rowsNumber.value = 0;
  } finally {
    loading.value = false;
  }
};

const loadPublisherCounts = async () => {
  const parseCount = (payload) => {
    if (typeof payload === "number") return payload;
    if (typeof payload === "string") {
      const parsed = Number(payload);
      return Number.isNaN(parsed) ? 0 : parsed;
    }

    if (typeof payload?.count === "number") return payload.count;
    if (typeof payload?.total === "number") return payload.total;
    if (typeof payload?.value === "number") return payload.value;

    const firstNumeric = Object.values(payload || {}).find(
      (value) => typeof value === "number"
    );
    return typeof firstNumeric === "number" ? firstNumeric : 0;
  };

  try {
    const [activeRes, deletedRes, totalRes] = await Promise.all([
      api.get("/publisher/count/active"),
      api.get("/publisher/count/deleted"),
      api.get("/publisher/count/total"),
    ]);

    countActive.value = parseCount(activeRes.data);
    countDeleted.value = parseCount(deletedRes.data);
    countTotal.value = parseCount(totalRes.data);
  } catch (error) {
    countActive.value = 0;
    countDeleted.value = 0;
    countTotal.value = 0;
  }
};

const loadBooks = async () => {
  try {
    const response = await api.get("/book");
    const books = Array.isArray(response.data?.content)
      ? response.data.content
      : Array.isArray(response.data)
      ? response.data
      : [];
    publishersWithBooks.value = books
      .map((book) => book?.publisher?.id)
      .filter((id) => typeof id === "number");
  } catch (error) {
    publishersWithBooks.value = [];
  }
};

const submitFormCadastro = async (formData) => {
  if (!formData.name || !formData.email || !formData.telephone) {
    notify("negative", "Preencha todos os campos obrigatorios.");
    return;
  }

  try {
    await api.post("/publisher", {
      name: formData.name,
      email: formData.email,
      telephone: formData.telephone,
      site: formData.site,
    });

    notify("positive", "Editora cadastrada com sucesso.");
    showModalCadastro.value = false;
    await Promise.all([
      loadPublishers(searchQuery.value),
      loadPublisherCounts(),
    ]);
  } catch (error) {
    notify(
      "negative",
      error.response?.data?.message || "Erro ao cadastrar editora."
    );
  }
};

const editRow = (row) => {
  formEditar.value = { ...row };
  showModalEditar.value = true;
};

const submitFormEditar = async (formData) => {
  if (!formData.name || !formData.email || !formData.telephone) {
    notify("negative", "Preencha todos os campos obrigatorios.");
    return;
  }

  try {
    await api.put(`/publisher/${formEditar.value.id}`, {
      id: formEditar.value.id,
      name: formData.name,
      email: formData.email,
      telephone: formData.telephone,
      site: formData.site,
    });

    notify("positive", "Editora atualizada com sucesso.");
    showModalEditar.value = false;
    await Promise.all([
      loadPublishers(searchQuery.value),
      loadPublisherCounts(),
    ]);
  } catch (error) {
    notify(
      "negative",
      error.response?.data?.message || "Erro ao atualizar editora."
    );
  }
};

const showDetails = async (row) => {
  try {
    const response = await api.get(`/publisher/${row.id}`);
    selectedRow.value = response.data || row;
  } catch (error) {
    selectedRow.value = row;
  } finally {
    showModalSobre.value = true;
  }
};

const askDelete = (row) => {
  deleteTarget.value = row;
  showModalExcluir.value = true;
};

const confirmDelete = async () => {
  if (!deleteTarget.value?.id) return;

  try {
    await api.delete(`/publisher/${deleteTarget.value.id}`);
    notify("positive", "Editora excluida com sucesso.");
    showModalExcluir.value = false;
    deleteTarget.value = null;
    await Promise.all([
      loadPublishers(searchQuery.value),
      loadPublisherCounts(),
    ]);
  } catch (error) {
    notify(
      "negative",
      error.response?.data?.message || "Erro ao excluir editora."
    );
  }
};

const handleSearch = () => {
  page.value = 1;
  loadPublishers(searchQuery.value, 1);
};

const clearSearch = () => {
  searchQuery.value = "";
  page.value = 1;
  loadPublishers("", 1);
};

const handlePageChange = (newPage) => {
  if (!newPage || newPage === page.value) return;
  page.value = newPage;
  loadPublishers(searchQuery.value, newPage);
};

const openRegisterCadastro = () => {
  showModalCadastro.value = true;
};

onMounted(async () => {
  const token = localStorage.getItem("authToken");
  if (!token) {
    router.push("/login");
    return;
  }

  userRole.value = localStorage.getItem("role") || "";
  await Promise.all([loadPublishers(), loadBooks(), loadPublisherCounts()]);
});
</script>

<style scoped>
.editoras-page {
  padding: 34px 60px;
  background: #f5f5f3;
  min-height: 100vh;
  margin: 0 auto;
}

.editoras-header {
  margin-bottom: 12px;
  animation: slideInUp 0.35s ease-out forwards;
  opacity: 0;
}

.editoras-header h1 {
  font-size: 2rem;
  font-weight: 700;
  color: #1f1f1f;
  margin: 0 0 8px 0;
}

.editoras-header p {
  font-size: 1rem;
  color: #75736a;
  margin: 0;
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

:deep(.editoras-pagination .q-btn) {
  min-width: 34px;
  min-height: 34px;
  border-radius: 8px;
  font-weight: 600;
}

:deep(.editoras-pagination .q-btn:not(.bg-positive)) {
  background: #f2f2ef;
  color: #5f5c54;
}

:deep(.editoras-pagination .q-btn.bg-positive) {
  background: #1f722c !important;
  box-shadow: 0 3px 10px rgba(31, 114, 44, 0.22);
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
  .stats-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 600px) {
  .editoras-page {
    padding: 18px;
  }

  .editoras-header h1 {
    font-size: 1.5rem;
  }
}
</style>
