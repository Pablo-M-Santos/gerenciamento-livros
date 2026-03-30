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
        label="Com Livros"
        :value="summary.withBooks"
        type="with-books"
      />
      <PublisherStatsCard
        :card-index="3"
        icon="menu_book"
        label="Sem Livros"
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
const loading = ref(false);
const publishersWithBooks = ref([]);

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
  const publisherIdsWithBooks = new Set(publishersWithBooks.value);
  const total = rows.value.length;
  const withBooks = rows.value.filter((publisher) =>
    publisherIdsWithBooks.has(publisher.id)
  ).length;
  const withoutBooks = Math.max(total - withBooks, 0);

  return {
    total,
    withBooks,
    withoutBooks,
  };
});

const notify = (type, message) => {
  Notify.create({ type, message, position: "top", timeout: 2200 });
};

const loadPublishers = async (search = "") => {
  loading.value = true;
  try {
    const response = await api.get("/publisher", {
      params: { search, page: 0 },
    });
    const data = response.data?.content || response.data || [];
    rows.value = Array.isArray(data) ? data : [];
  } catch (error) {
    notify("negative", "Erro ao carregar editoras.");
    rows.value = [];
  } finally {
    loading.value = false;
  }
};

const loadBooks = async () => {
  try {
    const response = await api.get("/book", { params: { page: 0 } });
    const books = response.data?.content || [];
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
    await loadPublishers(searchQuery.value);
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
    await loadPublishers(searchQuery.value);
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
    await loadPublishers(searchQuery.value);
  } catch (error) {
    notify(
      "negative",
      error.response?.data?.message || "Erro ao excluir editora."
    );
  }
};

const handleSearch = () => {
  loadPublishers(searchQuery.value);
};

const clearSearch = () => {
  searchQuery.value = "";
  loadPublishers();
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
  await Promise.all([loadPublishers(), loadBooks()]);
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
