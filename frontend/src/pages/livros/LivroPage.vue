<template>
  <q-page class="books-page">
    <section class="books-header animate-header">
      <h1>Gerenciamento de Livros</h1>
      <p>Gerencie os livros da sua biblioteca digital</p>
    </section>

    <section class="stats-grid">
      <BookStatsCard
        :card-index="1"
        icon="menu_book"
        label="Total de Livros"
        :value="summary.total"
        type="total"
      />
      <BookStatsCard
        :card-index="2"
        icon="inventory_2"
        label="Disponiveis"
        :value="summary.available"
        type="available"
      />
      <BookStatsCard
        :card-index="3"
        icon="local_library"
        label="Alugados"
        :value="summary.rented"
        type="rented"
      />
    </section>

    <BookSearchBar
      :search-query="searchQuery"
      :can-add="userRole === 'ADMIN'"
      @update:search="searchQuery = $event"
      @search="handleSearch"
      @clear="clearSearch"
      @add="openRegisterDialog"
    />

    <BookTable
      :rows="rows"
      :loading="loading"
      :is-admin="userRole === 'ADMIN'"
      :books-with-rentals="booksWithRentals"
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
        class="books-pagination"
        @update:model-value="handlePageChange"
      />
    </div>

    <BookFormModal
      v-model="showModalCadastro"
      :is-edit-mode="false"
      :initial-data="bookToCreate"
      :publisher-options="publisherOptions"
      :today="today"
      @submit="submitFormCadastro"
      @filter-publisher="filterPublisher"
    />

    <BookFormModal
      v-model="showModalEditar"
      :is-edit-mode="true"
      :initial-data="bookToEdit"
      :publisher-options="publisherOptions"
      :today="today"
      @submit="submitFormEditar"
      @filter-publisher="filterPublisher"
    />

    <BookDetailsModal v-model="showModalSobre" :book-data="selectedBook" />

    <BookDeleteModal
      v-model="showModalExcluir"
      :target-name="deleteTarget?.name || ''"
      @confirm="confirmDelete"
    />
  </q-page>
</template>

<script setup>
import { computed, onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import { Notify } from "quasar";
import { api } from "src/boot/axios.js";

import BookStatsCard from "src/components/livros/BookStatsCard.vue";
import BookSearchBar from "src/components/livros/BookSearchBar.vue";
import BookTable from "src/components/livros/BookTable.vue";
import BookFormModal from "src/components/livros/BookFormModal.vue";
import BookDetailsModal from "src/components/livros/BookDetailsModal.vue";
import BookDeleteModal from "src/components/livros/BookDeleteModal.vue";

const router = useRouter();

const loading = ref(false);
const searchQuery = ref("");
const page = ref(1);
const rowsNumber = ref(0);
const pageSize = 8;
const userRole = ref(localStorage.getItem("role") || "");

const showModalCadastro = ref(false);
const showModalEditar = ref(false);
const showModalExcluir = ref(false);
const showModalSobre = ref(false);

const rows = ref([]);
const booksWithRentals = ref([]);
const publisherOptions = ref([]);
const allPublishers = ref([]);
const deleteTarget = ref(null);

const selectedBook = ref({
  id: null,
  name: "",
  author: "",
  totalQuantity: 0,
  totalInUse: 0,
  launchDate: "",
  publisher: null,
  publisherId: null,
});

const bookToCreate = ref({
  id: null,
  name: "",
  author: "",
  totalQuantity: 1,
  launchDate: "",
  publisherId: null,
  totalInUse: 0,
});

const bookToEdit = ref({
  id: null,
  name: "",
  author: "",
  totalQuantity: 1,
  launchDate: "",
  publisherId: null,
  totalInUse: 0,
});

const today = new Date().toISOString().split("T")[0];

const notify = (type, message) => {
  Notify.create({ type, message, position: "top", timeout: 2200 });
};

const extractList = (payload) => {
  if (Array.isArray(payload)) return payload;
  if (Array.isArray(payload?.content)) return payload.content;
  if (Array.isArray(payload?.data)) return payload.data;
  if (Array.isArray(payload?.data?.content)) return payload.data.content;
  return [];
};

const extractPageData = (payload) => {
  const list = extractList(payload);
  const total =
    typeof payload?.totalElements === "number" ? payload.totalElements : list.length;
  const currentPage = typeof payload?.number === "number" ? payload.number + 1 : 1;

  return { list, total, currentPage };
};

const normalizeDate = (value) => {
  if (!value) return "";
  if (typeof value === "string" && value.length >= 10)
    return value.slice(0, 10);
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return "";
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  return `${date.getFullYear()}-${month}-${day}`;
};

const normalizeBook = (book) => ({
  id: book?.id ?? null,
  name: book?.name ?? "-",
  author: book?.author ?? "-",
  totalQuantity: Number(book?.totalQuantity ?? 0),
  totalInUse: Number(book?.totalInUse ?? 0),
  launchDate: normalizeDate(book?.launchDate),
  publisher: book?.publisher ?? null,
  publisherId: book?.publisherId ?? book?.publisher?.id ?? null,
});

const summary = computed(() => {
  const total = rowsNumber.value;
  const available = rows.value.reduce(
    (acc, row) =>
      acc + Math.max(Number(row.totalQuantity) - Number(row.totalInUse), 0),
    0
  );
  const rented = rows.value.reduce(
    (acc, row) => acc + Number(row.totalInUse || 0),
    0
  );

  return {
    total,
    available,
    rented,
  };
});

const totalPages = computed(() => {
  return Math.max(1, Math.ceil(rowsNumber.value / pageSize));
});

const loadBooks = async (search = "", targetPage = page.value) => {
  loading.value = true;
  try {
    const response = await api.get("/book", {
      params: { search: search || undefined, page: targetPage - 1 },
    });

    const { list, total, currentPage } = extractPageData(response.data);
    rows.value = list.map(normalizeBook);
    rowsNumber.value = total;
    page.value = currentPage;
  } catch (error) {
    rows.value = [];
    rowsNumber.value = 0;
    notify("negative", "Erro ao carregar livros.");
  } finally {
    loading.value = false;
  }
};

const loadPublishers = async (search = "") => {
  try {
    const response = await api.get("/publisher", {
      params: { search: search || undefined },
    });
    const data = extractList(response.data);
    allPublishers.value = Array.isArray(data) ? data : [];
    publisherOptions.value = [...allPublishers.value];
  } catch (error) {
    allPublishers.value = [];
    publisherOptions.value = [];
  }
};

const loadRents = async () => {
  try {
    const response = await api.get("/rent", {
      params: { page: 0, status: "" },
    });

    const rentals = extractList(response.data);
    booksWithRentals.value = [
      ...new Set(
        rentals
          .map((rent) => rent?.book?.id)
          .filter((id) => typeof id === "number")
      ),
    ];
  } catch (error) {
    booksWithRentals.value = [];
  }
};

const openRegisterDialog = () => {
  bookToCreate.value = {
    id: null,
    name: "",
    author: "",
    totalQuantity: 1,
    launchDate: "",
    publisherId: null,
    totalInUse: 0,
  };
  showModalCadastro.value = true;
};

const submitFormCadastro = async (formData) => {
  if (
    !formData.name ||
    !formData.author ||
    !formData.publisherId ||
    Number(formData.totalQuantity) < 1
  ) {
    notify("negative", "Preencha os campos obrigatorios para cadastrar.");
    return;
  }

  try {
    await api.post("/book", {
      name: formData.name,
      author: formData.author,
      totalQuantity: Number(formData.totalQuantity),
      launchDate: normalizeDate(formData.launchDate),
      publisherId: Number(formData.publisherId),
      totalInUse: Number(formData.totalInUse || 0),
    });

    notify("positive", "Livro criado com sucesso.");
    showModalCadastro.value = false;
    await loadBooks(searchQuery.value);
  } catch (error) {
    notify("negative", error.response?.data?.message || "Erro ao criar livro.");
  }
};

const editRow = async (row) => {
  try {
    const response = await api.get(`/book/${row.id}`);
    const detail = normalizeBook(response.data || row);
    bookToEdit.value = {
      id: detail.id,
      name: detail.name,
      author: detail.author,
      totalQuantity: detail.totalQuantity,
      launchDate: detail.launchDate,
      publisherId: detail.publisherId,
      totalInUse: detail.totalInUse,
    };
    showModalEditar.value = true;
  } catch (error) {
    notify("negative", "Erro ao carregar dados do livro para edicao.");
  }
};

const submitFormEditar = async (formData) => {
  if (
    !formData.name ||
    !formData.author ||
    !formData.publisherId ||
    Number(formData.totalQuantity) < 1
  ) {
    notify("negative", "Preencha os campos obrigatorios para atualizar.");
    return;
  }

  try {
    await api.put(`/book/${bookToEdit.value.id}`, {
      id: bookToEdit.value.id,
      name: formData.name,
      author: formData.author,
      totalQuantity: Number(formData.totalQuantity),
      launchDate: normalizeDate(formData.launchDate),
      publisherId: Number(formData.publisherId),
      totalInUse: Number(formData.totalInUse || 0),
    });

    notify("positive", "Livro atualizado com sucesso.");
    showModalEditar.value = false;
    await loadBooks(searchQuery.value);
  } catch (error) {
    notify(
      "negative",
      error.response?.data?.message || "Erro ao atualizar livro."
    );
  }
};

const showDetails = async (row) => {
  try {
    const response = await api.get(`/book/${row.id}`);
    selectedBook.value = normalizeBook(response.data || row);
  } catch (error) {
    selectedBook.value = normalizeBook(row);
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
    await api.delete(`/book/${deleteTarget.value.id}`);
    notify("positive", "Livro excluido com sucesso.");
    showModalExcluir.value = false;
    deleteTarget.value = null;
    await Promise.all([loadBooks(searchQuery.value), loadRents()]);
  } catch (error) {
    notify(
      "negative",
      error.response?.data?.message || "Erro ao excluir livro."
    );
  }
};

const handleSearch = () => {
  page.value = 1;
  loadBooks(searchQuery.value, 1);
};

const clearSearch = () => {
  searchQuery.value = "";
  page.value = 1;
  loadBooks("", 1);
};

const handlePageChange = (newPage) => {
  if (!newPage || newPage === page.value) return;
  page.value = newPage;
  loadBooks(searchQuery.value, newPage);
};

const filterPublisher = (val, update) => {
  if (val === "") {
    update(() => {
      publisherOptions.value = [...allPublishers.value];
    });
    return;
  }

  const needle = val.toLowerCase();
  update(() => {
    publisherOptions.value = allPublishers.value.filter((publisher) =>
      String(publisher.name || "")
        .toLowerCase()
        .includes(needle)
    );
  });
};

onMounted(async () => {
  const token = localStorage.getItem("authToken");

  if (!token) {
    router.push("/login");
    return;
  }

  userRole.value = localStorage.getItem("role") || "";
  await Promise.all([loadBooks(), loadPublishers(), loadRents()]);
});
</script>

<style scoped>
.books-page {
  padding: 34px 60px;
  background: #f5f5f3;
  min-height: 100vh;
  margin: 0 auto;
}

.books-header {
  animation: slideInDown 0.35s ease-out forwards;
  opacity: 0;
}

.books-header h1 {
  margin: 0;
  font-size: 2rem;
  color: #222;
  font-weight: 700;
}

.books-header p {
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

:deep(.books-pagination .q-btn) {
  min-width: 34px;
  min-height: 34px;
  border-radius: 8px;
  font-weight: 600;
}

:deep(.books-pagination .q-btn:not(.bg-positive)) {
  background: #f2f2ef;
  color: #5f5c54;
}

:deep(.books-pagination .q-btn.bg-positive) {
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
  .books-page {
    padding: 18px;
  }

  .books-header h1 {
    font-size: 1.5rem;
  }
}
</style>
