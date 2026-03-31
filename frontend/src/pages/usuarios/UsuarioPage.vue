<template>
  <q-page class="users-page">
    <!-- Header -->
    <section class="users-header animate-header">
      <h1>Gerenciamento de Usuarios</h1>

    </section>

    <!-- Stats Cards -->
    <section class="stats-grid">
      <UserStatsCard
        :card-index="1"
        icon="groups_2"
        label="Total de Usuarios"
        :value="summary.total"
        type="total"
      />
      <UserStatsCard
        :card-index="2"
        icon="verified_user"
        label="Administradores"
        :value="summary.admins"
        type="admin"
      />
      <UserStatsCard
        :card-index="3"
        icon="badge"
        label="Locatarios"
        :value="summary.users"
        type="user"
      />
    </section>

    <!-- Search and Add Button -->
    <UserSearchBar
      :search-query="srch"
      :can-add="userRole === 'ADMIN'"
      @update:search="srch = $event"
      @search="onSearch"
      @clear="clearSearch"
      @add="openRegisterDialog"
    />

    <!-- User Table -->
    <UserTable
      :rows="rows"
      :loading="loading"
      :admin-id="adminId"
      :is-admin="userRole === 'ADMIN'"
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
        class="users-pagination"
        @update:model-value="handlePageChange"
      />
    </div>

    <!-- Modals -->
    <UserFormModal
      v-model="showModalCadastro"
      :is-edit-mode="false"
      :initial-data="userCreate"
      @submit="submitFormCadastro"
    />

    <UserFormModal
      v-model="showModalEditar"
      :is-edit-mode="true"
      :initial-data="formEditar"
      @submit="submitFormEditar"
    />

    <UserDetailsModal v-model="showModalSobre" :user-data="selectedRow" />

    <UserDeleteModal
      v-model="showModalExcluir"
      :target-name="deleteTarget?.name || ''"
      @confirm="confirmDelete"
    />
  </q-page>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from "vue";
import { useRouter } from "vue-router";
import { Notify } from "quasar";
import { api } from "src/boot/axios";
import UserStatsCard from "src/components/usuarios/UserStatsCard.vue";
import UserSearchBar from "src/components/usuarios/UserSearchBar.vue";
import UserTable from "src/components/usuarios/UserTable.vue";
import UserFormModal from "src/components/usuarios/UserFormModal.vue";
import UserDetailsModal from "src/components/usuarios/UserDetailsModal.vue";
import UserDeleteModal from "src/components/usuarios/UserDeleteModal.vue";

const router = useRouter();

// State
const showModalCadastro = ref(false);
const showModalEditar = ref(false);
const showModalSobre = ref(false);
const showModalExcluir = ref(false);
const loading = ref(false);
const srch = ref("");
const page = ref(1);
const rowsNumber = ref(0);

const userRole = ref(localStorage.getItem("role") || "");
const adminId = Number(localStorage.getItem("userId") || 0);

const rows = ref([]);
const pageSize = 8;
const summary = reactive({
  total: 0,
  admins: 0,
  users: 0,
});

const userCreate = reactive({
  name: "",
  email: "",
  password: "",
  role: "",
});

const formEditar = reactive({
  id: null,
  name: "",
  email: "",
  role: "USER",
});

const selectedRow = reactive({
  id: null,
  name: "",
  email: "",
  role: "",
  createdAt: null,
});

const deleteTarget = ref(null);

const metricsConfig = {
  enabled: true,
  endpoints: {
    total: "/user/count/total",
    admins: "/user/count/admins",
    users: "/user/count/locatarios",
  },
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

const totalPages = computed(() => {
  return Math.max(1, Math.ceil(rowsNumber.value / pageSize));
});

// Methods
const notify = (type, message) => {
  Notify.create({ type, message, position: "top", timeout: 2200 });
};

const normalizeUser = (row) => {
  return {
    id: row.id,
    name: row.name || "-",
    email: row.email || "-",
    role: row.role || "USER",
    createdAt:
      row.createdAt ||
      row.creationDate ||
      row.created_date ||
      row.createdAtDate ||
      null,
  };
};

const updateSummaryFromRows = () => {
  summary.total = rows.value.length;
  summary.admins = rows.value.filter((user) => user.role === "ADMIN").length;
  summary.users = rows.value.filter((user) => user.role === "USER").length;
};

const loadSummary = async () => {
  if (!metricsConfig.enabled) {
    updateSummaryFromRows();
    return;
  }

  try {
    const [totalRes, adminsRes, usersRes] = await Promise.all([
      api.get(metricsConfig.endpoints.total),
      api.get(metricsConfig.endpoints.admins),
      api.get(metricsConfig.endpoints.users),
    ]);

    summary.total = Number(totalRes.data?.value ?? totalRes.data ?? 0);
    summary.admins = Number(adminsRes.data?.value ?? adminsRes.data ?? 0);
    summary.users = Number(usersRes.data?.value ?? usersRes.data ?? 0);
  } catch {
    updateSummaryFromRows();
  }
};

const loadUsers = async (targetPage = page.value) => {
  loading.value = true;

  try {
    const response = await api.get("/user", {
      params: {
        search: srch.value || undefined,
        page: targetPage - 1,
      },
    });

    const { list, total, currentPage } = extractPageData(response.data);
    rows.value = list.map(normalizeUser);
    rowsNumber.value = total;
    page.value = currentPage;
    await loadSummary();
  } catch (error) {
    notify("negative", "Erro ao carregar usuarios.");
    rows.value = [];
    rowsNumber.value = 0;
    updateSummaryFromRows();
  } finally {
    loading.value = false;
  }
};

const openRegisterDialog = () => {
  userCreate.name = "";
  userCreate.email = "";
  userCreate.password = "";
  userCreate.role = "";
  showModalCadastro.value = true;
};

const onSearch = () => {
  page.value = 1;
  loadUsers(1);
};

const clearSearch = () => {
  srch.value = "";
  page.value = 1;
  loadUsers(1);
};

const handlePageChange = (newPage) => {
  if (!newPage || newPage === page.value) return;
  page.value = newPage;
  loadUsers(newPage);
};

const submitFormCadastro = async (formData) => {
  if (!formData.role) {
    notify("negative", "Selecione um nível de acesso.");
    return;
  }

  try {
    await api.post("/user", {
      name: formData.name,
      email: formData.email,
      password: formData.password,
      role: formData.role,
    });

    notify("positive", "Usuário cadastrado com sucesso.");
    showModalCadastro.value = false;
    await loadUsers();
  } catch (error) {
    notify(
      "negative",
      error.response?.data?.message || "Erro ao cadastrar usuário."
    );
  }
};

const editRow = (row) => {
  formEditar.id = row.id;
  formEditar.name = row.name;
  formEditar.email = row.email;
  formEditar.role = row.role;
  showModalEditar.value = true;
};

const submitFormEditar = async (formData) => {
  try {
    await api.put(`/user/${formEditar.id}`, {
      id: formEditar.id,
      name: formData.name,
      email: formData.email,
      role: formData.role,
    });

    notify("positive", "Usuário atualizado com sucesso.");
    showModalEditar.value = false;
    await loadUsers();
  } catch (error) {
    notify(
      "negative",
      error.response?.data?.message || "Erro ao atualizar usuário."
    );
  }
};

const showDetails = async (row) => {
  try {
    const response = await api.get(`/user/${row.id}`);
    const detail = normalizeUser(response.data || row);
    selectedRow.id = detail.id;
    selectedRow.name = detail.name;
    selectedRow.email = detail.email;
    selectedRow.role = detail.role;
    selectedRow.createdAt = detail.createdAt;
    showModalSobre.value = true;
  } catch (error) {
    selectedRow.id = row.id;
    selectedRow.name = row.name;
    selectedRow.email = row.email;
    selectedRow.role = row.role;
    selectedRow.createdAt = row.createdAt;
    showModalSobre.value = true;
  }
};

const askDelete = (row) => {
  deleteTarget.value = row;
  showModalExcluir.value = true;
};

const confirmDelete = async () => {
  if (!deleteTarget.value?.id) {
    return;
  }

  try {
    await api.delete(`/user/${deleteTarget.value.id}`);
    notify("positive", "Usuário excluído com sucesso.");
    showModalExcluir.value = false;
    deleteTarget.value = null;
    await loadUsers();
  } catch (error) {
    notify(
      "negative",
      error.response?.data?.message || "Erro ao excluir usuário."
    );
  }
};

// Lifecycle
onMounted(async () => {
  const token = localStorage.getItem("authToken");

  if (!token) {
    router.push({ name: "login" });
    return;
  }

  await loadUsers();
});
</script>

<style scoped>
.users-page {
  padding: 0px 60px 34px;
  background: #f5f5f3;
  min-height: 100vh;
  margin: 0 auto;
}

.users-header {
  animation: slideInDown 0.35s ease-out forwards;
  opacity: 0;
}

.users-header h1 {
  margin: 0;
  font-size: 2rem;
  color: #222;
  font-weight: 700;
  height: 80px;
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

:deep(.users-pagination .q-btn) {
  min-width: 34px;
  min-height: 34px;
  border-radius: 8px;
  font-weight: 600;
}

:deep(.users-pagination .q-btn:not(.bg-positive)) {
  background: #f2f2ef;
  color: #5f5c54;
}

:deep(.users-pagination .q-btn.bg-positive) {
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
  .users-page {
    padding: 18px;
  }

  .users-header h1 {
    font-size: 1.5rem;
  }
}
</style>
