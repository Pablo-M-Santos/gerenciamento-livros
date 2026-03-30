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
      :rows="filteredRows"
      :loading="loading"
      :is-admin="userRole === 'ADMIN'"
      :books-with-rentals="booksWithRentals"
      @view="showDetails"
      @edit="editRow"
      @delete="askDelete"
    />

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

const filteredRows = computed(() => {
  const query = searchQuery.value.trim().toLowerCase();
  if (!query) return rows.value;

  return rows.value.filter((row) => {
    return (
      String(row.name).toLowerCase().includes(query) ||
      String(row.author).toLowerCase().includes(query) ||
      String(row.publisher?.name || "")
        .toLowerCase()
        .includes(query)
    );
  });
});

const summary = computed(() => {
  const total = rows.value.length;
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

const loadBooks = async (search = "") => {
  loading.value = true;
  try {
    const response = await api.get("/book", {
      params: { search: search || undefined, page: 0 },
    });

    const data = response.data?.content || response.data || [];
    rows.value = Array.isArray(data) ? data.map(normalizeBook) : [];
  } catch (error) {
    rows.value = [];
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
    const data = response.data?.content || response.data || [];
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

    const rentals = response.data?.content || [];
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
  loadBooks(searchQuery.value);
};

const clearSearch = () => {
  searchQuery.value = "";
  loadBooks();
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
