<template>
  <q-page class="users-page">
    <section class="users-header">
      <h1>Gerenciamento de Usuarios</h1>
      <p>Gerencie os usuarios da sua biblioteca digital</p>
    </section>

    <section class="stats-grid">
      <q-card flat bordered class="stat-card">
        <div class="stat-icon stat-icon-total">
          <q-icon name="groups_2" size="20px" />
        </div>
        <div class="stat-info">
          <span>Total de Usuarios</span>
          <strong>{{ summary.total }}</strong>
        </div>
      </q-card>

      <q-card flat bordered class="stat-card">
        <div class="stat-icon stat-icon-admin">
          <q-icon name="verified_user" size="20px" />
        </div>
        <div class="stat-info">
          <span>Administradores</span>
          <strong>{{ summary.admins }}</strong>
        </div>
      </q-card>

      <q-card flat bordered class="stat-card">
        <div class="stat-icon stat-icon-user">
          <q-icon name="badge" size="20px" />
        </div>
        <div class="stat-info">
          <span>Locatarios</span>
          <strong>{{ summary.users }}</strong>
        </div>
      </q-card>
    </section>

    <section class="toolbar-row">
      <q-input
        v-model="srch"
        outlined
        dense
        rounded
        class="search-input"
        placeholder="Buscar usuarios..."
        itemid="searchInput"
        @keyup.enter="onSearch"
      >
        <template #prepend>
          <q-icon name="search" />
        </template>

        <template #append>
          <q-icon
            v-if="srch"
            name="close"
            class="cursor-pointer"
            itemid="closeSearchBtn"
            @click="clearSearch"
          />
        </template>

     
      </q-input>

      <q-btn
        v-if="userRole === 'ADMIN'"
        unelevated
        no-caps
        class="add-user-btn"
        itemid="cadastroBtnUsuario"
        icon="add"
        label="Adicionar Usuario"
        @click="openRegisterDialog"
      />
    </section>

    <q-card flat bordered class="table-card">
      <q-table
        :rows="filteredRows"
        :columns="columns"
        row-key="id"
        flat
        :loading="loading"
        hide-bottom
      >
        <template #loading>
          <q-inner-loading showing color="primary" />
        </template>

        <template #body-cell-role="props">
          <q-td :props="props">
            <span
              class="role-chip"
              :class="props.row.role === 'ADMIN' ? 'role-admin' : 'role-user'"
            >
              {{ mapRole(props.row.role) }}
            </span>
          </q-td>
        </template>

        <template #body-cell-createdAt="props">
          <q-td :props="props">
            {{ formatDate(props.row.createdAt) }}
          </q-td>
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
              @click="showDetails(props.row)"
            >
              <q-tooltip>Visualizar detalhes</q-tooltip>
            </q-btn>

            <q-btn
              v-if="props.row.id !== adminId && userRole === 'ADMIN'"
              flat
              round
              dense
              icon="edit"
              color="secondary"
              :itemid="'edit-' + props.row.name"
              @click="editRow(props.row)"
            >
              <q-tooltip>Editar usuario</q-tooltip>
            </q-btn>

            <q-btn
              v-if="props.row.id !== adminId && userRole === 'ADMIN'"
              flat
              round
              dense
              icon="delete"
              color="negative"
              :itemid="'delete-' + props.row.name"
              @click="askDelete(props.row)"
            >
              <q-tooltip>Excluir usuario</q-tooltip>
            </q-btn>
          </q-td>
        </template>

        <template #no-data>
          <div class="q-pa-lg text-grey-7">Nenhum usuario encontrado.</div>
        </template>
      </q-table>
    </q-card>

    <q-dialog v-model="showModalCadastro" persistent>
      <q-card class="modal-card">
        <q-card-section class="modal-title">Cadastrar Usuario</q-card-section>

        <q-card-section>
          <q-form @submit="submitFormCadastro" class="modal-form">
            <q-input
              v-model="userCreate.name"
              outlined
              dense
              rounded
              label="Nome"
              itemid="cadastroNomeUsuario"
              :rules="[(val) => !!val || 'Nome e obrigatorio']"
            />

            <q-input
              v-model="userCreate.email"
              outlined
              dense
              rounded
              label="E-mail"
              type="email"
              itemid="cadastrarEmailUsuario"
              :rules="[
                (val) => !!val || 'E-mail e obrigatorio',
                (val) => /.+@.+\\..+/.test(val) || 'E-mail invalido',
              ]"
            />

            <q-input
              v-model="userCreate.password"
              outlined
              dense
              rounded
              :type="isPwd ? 'password' : 'text'"
              label="Senha"
              itemid="cadastrarSenhaUsuario"
              :rules="[(val) => !!val || 'Senha e obrigatoria']"
            >
              <template #append>
                <q-icon
                  :name="isPwd ? 'visibility_off' : 'visibility'"
                  class="cursor-pointer"
                  @click="isPwd = !isPwd"
                />
              </template>
            </q-input>

            <div class="role-group">
              <q-radio
                v-model="userCreate.role"
                checked-icon="task_alt"
                unchecked-icon="panorama_fish_eye"
                val="ADMIN"
                label="Administrador"
                itemid="cadastrarAdministradorUsuario"
              />
              <q-radio
                v-model="userCreate.role"
                checked-icon="task_alt"
                unchecked-icon="panorama_fish_eye"
                val="USER"
                label="Locatario"
                itemid="cadastrarLocatarioUsuario"
              />
            </div>

            <div class="modal-actions">
              <q-btn flat no-caps label="Cancelar" v-close-popup />
              <q-btn
                unelevated
                no-caps
                type="submit"
                label="Cadastrar"
                class="submit-modal-btn"
                itemid="BtnCadastrarUsuario"
              />
            </div>
          </q-form>
        </q-card-section>
      </q-card>
    </q-dialog>

    <q-dialog v-model="showModalEditar" persistent>
      <q-card class="modal-card">
        <q-card-section class="modal-title">Editar Usuario</q-card-section>

        <q-card-section>
          <q-form @submit="submitFormEditar" class="modal-form">
            <q-input
              v-model="formEditar.name"
              outlined
              dense
              rounded
              label="Nome"
              itemid="editarNomeUsuario"
              :rules="[(val) => !!val || 'Nome e obrigatorio']"
            />

            <q-input
              v-model="formEditar.email"
              outlined
              dense
              rounded
              label="E-mail"
              type="email"
              itemid="emailNomeUsuario"
              :rules="[
                (val) => !!val || 'E-mail e obrigatorio',
                (val) => /.+@.+\\..+/.test(val) || 'E-mail invalido',
              ]"
            />

            <div class="role-group">
              <q-radio
                v-model="formEditar.role"
                checked-icon="task_alt"
                unchecked-icon="panorama_fish_eye"
                val="ADMIN"
                label="Administrador"
              />
              <q-radio
                v-model="formEditar.role"
                checked-icon="task_alt"
                unchecked-icon="panorama_fish_eye"
                val="USER"
                label="Locatario"
                itemid="editarLocatarioUsuario"
              />
            </div>

            <div class="modal-actions">
              <q-btn flat no-caps label="Cancelar" v-close-popup />
              <q-btn
                unelevated
                no-caps
                type="submit"
                label="Atualizar"
                class="submit-modal-btn"
                itemid="BtnEditarUsuario"
              />
            </div>
          </q-form>
        </q-card-section>
      </q-card>
    </q-dialog>

    <q-dialog v-model="showModalSobre">
      <q-card class="modal-card">
        <q-card-section class="modal-title">Detalhes do Usuario</q-card-section>

        <q-card-section class="modal-form">
          <q-input
            outlined
            dense
            rounded
            readonly
            v-model="selectedRow.name"
            label="Nome"
          />
          <q-input
            outlined
            dense
            rounded
            readonly
            v-model="selectedRow.email"
            label="E-mail"
          />
          <q-input
            outlined
            dense
            rounded
            readonly
            :model-value="mapRole(selectedRow.role)"
            label="Perfil"
          />
        </q-card-section>

        <q-card-actions align="right">
          <q-btn
            flat
            no-caps
            label="Fechar"
            itemid="BtnSobreUsuario"
            @click="showModalSobre = false"
          />
        </q-card-actions>
      </q-card>
    </q-dialog>

    <q-dialog v-model="showModalExcluir" persistent>
      <q-card class="confirm-card">
        <q-card-section class="modal-title">Excluir Usuario</q-card-section>
        <q-card-section>
          Tem certeza que deseja excluir
          <strong>{{ deleteTarget?.name }}</strong
          >?
        </q-card-section>
        <q-card-actions align="right">
          <q-btn flat no-caps label="Cancelar" v-close-popup />
          <q-btn
            unelevated
            no-caps
            color="negative"
            label="Excluir"
            @click="confirmDelete"
          />
        </q-card-actions>
      </q-card>
    </q-dialog>
  </q-page>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from "vue";
import { useRouter } from "vue-router";
import { Notify } from "quasar";
import { api } from "src/boot/axios";

const router = useRouter();

const showModalCadastro = ref(false);
const showModalEditar = ref(false);
const showModalSobre = ref(false);
const showModalExcluir = ref(false);
const loading = ref(false);
const isPwd = ref(true);
const srch = ref("");

const userRole = ref(localStorage.getItem("role") || "");
const adminId = Number(localStorage.getItem("userId") || 0);

const rows = ref([]);
const summary = reactive({
  total: 0,
  admins: 0,
  users: 0,
});

const metricsConfig = {
  enabled: false,
  endpoints: {
    total: "/user/metrics/total",
    admins: "/user/metrics/admins",
    users: "/user/metrics/users",
  },
};

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
    name: "role",
    align: "left",
    label: "Perfil",
    field: "role",
    sortable: true,
  },
  {
    name: "createdAt",
    align: "left",
    label: "Criado em",
    field: "createdAt",
    sortable: true,
  },
  { name: "actions", align: "right", label: "Acoes", field: "actions" },
];

const roleMap = {
  ADMIN: "Administrador",
  USER: "Locatario",
};

const filteredRows = computed(() => {
  const query = srch.value.trim().toLowerCase();

  if (!query) {
    return rows.value;
  }

  return rows.value.filter((row) => {
    return (
      row.name.toLowerCase().includes(query) ||
      row.email.toLowerCase().includes(query) ||
      mapRole(row.role).toLowerCase().includes(query)
    );
  });
});

const notify = (type, message) => {
  Notify.create({ type, message, position: "top", timeout: 2200 });
};

const mapRole = (role) => roleMap[role] || role;

const formatDate = (value) => {
  if (!value) return "-";
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return "-";

  return date.toLocaleDateString("pt-BR", {
    day: "2-digit",
    month: "short",
    year: "numeric",
  });
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

const loadUsers = async () => {
  loading.value = true;

  try {
    const response = await api.get("/user", {
      params: { search: srch.value || undefined },
    });

    const data = response.data?.content || response.data || [];
    rows.value = Array.isArray(data) ? data.map(normalizeUser) : [];
    await loadSummary();
  } catch (error) {
    notify("negative", "Erro ao carregar usuarios.");
    rows.value = [];
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
  loadUsers();
};

const clearSearch = () => {
  srch.value = "";
  loadUsers();
};

const submitFormCadastro = async () => {
  if (!userCreate.role) {
    notify("negative", "Selecione um nivel de acesso.");
    return;
  }

  try {
    await api.post("/user", {
      name: userCreate.name,
      email: userCreate.email,
      password: userCreate.password,
      role: userCreate.role,
    });

    notify("positive", "Usuario cadastrado com sucesso.");
    showModalCadastro.value = false;
    await loadUsers();
  } catch (error) {
    notify(
      "negative",
      error.response?.data?.message || "Erro ao cadastrar usuario."
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

const submitFormEditar = async () => {
  try {
    await api.put(`/user/${formEditar.id}`, {
      id: formEditar.id,
      name: formEditar.name,
      email: formEditar.email,
      role: formEditar.role,
    });

    notify("positive", "Usuario atualizado com sucesso.");
    showModalEditar.value = false;
    await loadUsers();
  } catch (error) {
    notify(
      "negative",
      error.response?.data?.message || "Erro ao atualizar usuario."
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
    notify("positive", "Usuario excluido com sucesso.");
    showModalExcluir.value = false;
    deleteTarget.value = null;
    await loadUsers();
  } catch (error) {
    notify(
      "negative",
      error.response?.data?.message || "Erro ao excluir usuario."
    );
  }
};

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
  padding: 34px 60px; 
  background: #f5f5f3;
  min-height: 100vh;
  margin: 0 auto;
}

.users-header h1 {
  margin: 0;
  font-size: 2rem;
  color: #222;
  font-weight: 700;
}

.users-header p {
  margin: 6px 0 0;
  color: #6b6b64;
  font-size: 1rem;
}

.stats-grid {
  margin-top: 24px;
  display: grid;
  grid-template-columns: repeat(3, minmax(180px, 1fr));
  gap: 16px;
}

.stat-card {
  border-radius: 18px;
  min-height: 98px;
  padding: 18px 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  background: #fff;
  border: 1px solid #dfdfdb;
  box-shadow: 0 4px 16px rgba(22, 24, 22, 0.05);
}

.stat-icon {
  width: 42px;
  height: 42px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-icon-total {
  background: #e8f1e9;
  color: #286f37;
}

.stat-icon-admin {
  background: #e5efe6;
  color: #2e7a3a;
}

.stat-icon-user {
  background: #ece9e1;
  color: #5d5546;
}

.stat-info {
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.stat-info span {
  color: #75736a;
  font-size: 0.92rem;
}

.stat-info strong {
  color: #1f1f1f;
  font-size: 2rem;
  line-height: 1;
  margin-top: 2px;
}

.toolbar-row {
  margin-top: 20px;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 12px;
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
}

.table-card {
  margin-top: 18px;
  border-radius: 18px;
  border: 1px solid #dfdfdb;
  overflow: hidden;
  background: #fff;
  box-shadow: 0 5px 22px rgba(22, 24, 22, 0.06);
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



.role-chip {
  display: inline-flex;
  align-items: center;
  border-radius: 999px;
  padding: 5px 12px;
  font-size: 0.78rem;
  font-weight: 700;
}

.role-admin {
  background: #e8f1e9;
  color: #2b6d39;
}

.role-user {
  background: #edece8;
  color: #615e54;
}

.modal-card {
  width: min(92vw, 540px);
  border-radius: 16px;
}

.confirm-card {
  width: min(92vw, 420px);
  border-radius: 16px;
}

.modal-title {
  font-size: 1.2rem;
  font-weight: 700;
  color: #222;
  padding-bottom: 4px;
}

.modal-form {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.role-group {
  display: flex;
  align-items: center;
  gap: 18px;
  padding: 2px 4px;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 8px;
}

.submit-modal-btn {
  background: #1f722c;
  color: #fff;
}

@media (max-width: 980px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }

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

@media (max-width: 600px) {
  .users-page {
    padding: 18px;
  }

  .users-header h1 {
    font-size: 1.5rem;
  }
}
</style>
