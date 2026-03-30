<template>
  <q-page class="users-page">
    <section class="users-header animate-header">
      <h1>Gerenciamento de Usuarios</h1>
      <p>Gerencie os usuarios da sua biblioteca digital</p>
    </section>

    <section class="stats-grid">
      <q-card flat bordered class="stat-card stat-card-1">
        <div class="stat-icon stat-icon-total">
          <q-icon name="groups_2" size="20px" />
        </div>
        <div class="stat-info">
          <span>Total de Usuarios</span>
          <strong>{{ summary.total }}</strong>
        </div>
      </q-card>

      <q-card flat bordered class="stat-card stat-card-2">
        <div class="stat-icon stat-icon-admin">
          <q-icon name="verified_user" size="20px" />
        </div>
        <div class="stat-info">
          <span>Administradores</span>
          <strong>{{ summary.admins }}</strong>
        </div>
      </q-card>

      <q-card flat bordered class="stat-card stat-card-3">
        <div class="stat-icon stat-icon-user">
          <q-icon name="badge" size="20px" />
        </div>
        <div class="stat-info">
          <span>Locatarios</span>
          <strong>{{ summary.users }}</strong>
        </div>
      </q-card>
    </section>

    <section class="toolbar-row animate-toolbar">
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
        class="add-user-btn animate-button"
        itemid="cadastroBtnUsuario"
        icon="add"
        label="Adicionar Usuario"
        @click="openRegisterDialog"
      />
    </section>

    <q-card flat bordered class="table-card animate-table-enter">
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

    <q-dialog
      v-model="showModalCadastro"
      persistent
      transition-show="scale"
      transition-hide="scale"
    >
      <q-card class="modal-card modal-animated">
        <q-linear-progress
          :value="1"
          color="primary"
          class="modal-header-bar"
        />
        <q-card-section class="modal-header-section">
          <div class="modal-title">
            <q-icon name="person_add" size="24px" color="primary" />
            <span>Cadastrar Novo Usuário</span>
          </div>
          <p class="modal-subtitle">
            Preencha os dados abaixo para criar um novo usuário
          </p>
        </q-card-section>

        <q-separator />

        <q-card-section class="modal-content">
          <q-form @submit="submitFormCadastro" class="modal-form">
            <q-input
              v-model="userCreate.name"
              outlined
              dense
              rounded
              label="Nome Completo"
              itemid="cadastroNomeUsuario"
              :rules="[(val) => !!val || 'Nome é obrigatório']"
              class="form-input"
              prefix-icon="person"
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
                (val) => !!val || 'E-mail é obrigatório',
                (val) => /.+@.+\\..+/.test(val) || 'E-mail inválido',
              ]"
              class="form-input"
              prefix-icon="email"
            />

            <q-input
              v-model="userCreate.password"
              outlined
              dense
              rounded
              :type="isPwd ? 'password' : 'text'"
              label="Senha"
              itemid="cadastrarSenhaUsuario"
              :rules="[(val) => !!val || 'Senha é obrigatória']"
              class="form-input"
              prefix-icon="lock"
            >
              <template #append>
                <q-icon
                  :name="isPwd ? 'visibility_off' : 'visibility'"
                  class="cursor-pointer"
                  @click="isPwd = !isPwd"
                />
              </template>
            </q-input>

            <div class="role-section">
              <label class="role-label">Nível de Acesso</label>
              <div class="role-group">
                <q-radio
                  v-model="userCreate.role"
                  checked-icon="task_alt"
                  unchecked-icon="panorama_fish_eye"
                  val="ADMIN"
                  label="Administrador"
                  itemid="cadastrarAdministradorUsuario"
                  class="role-radio"
                />
                <q-radio
                  v-model="userCreate.role"
                  checked-icon="task_alt"
                  unchecked-icon="panorama_fish_eye"
                  val="USER"
                  label="Locatário"
                  itemid="cadastrarLocatarioUsuario"
                  class="role-radio"
                />
              </div>
            </div>

            <q-separator class="q-my-md" />

            <div class="modal-actions">
              <q-btn
                flat
                no-caps
                label="Cancelar"
                v-close-popup
                class="btn-cancel"
              />
              <q-btn
                unelevated
                no-caps
                type="submit"
                label="Cadastrar"
                class="btn-submit"
                itemid="BtnCadastrarUsuario"
              />
            </div>
          </q-form>
        </q-card-section>
      </q-card>
    </q-dialog>

    <q-dialog
      v-model="showModalEditar"
      persistent
      transition-show="scale"
      transition-hide="scale"
    >
      <q-card class="modal-card modal-animated">
        <q-linear-progress
          :value="1"
          color="secondary"
          class="modal-header-bar"
        />
        <q-card-section class="modal-header-section">
          <div class="modal-title">
            <q-icon name="edit" size="24px" color="secondary" />
            <span>Editar Usuário</span>
          </div>
          <p class="modal-subtitle">Atualize as informações do usuário</p>
        </q-card-section>

        <q-separator />

        <q-card-section class="modal-content">
          <q-form @submit="submitFormEditar" class="modal-form">
            <q-input
              v-model="formEditar.name"
              outlined
              dense
              rounded
              label="Nome Completo"
              itemid="editarNomeUsuario"
              :rules="[(val) => !!val || 'Nome é obrigatório']"
              class="form-input"
              prefix-icon="person"
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
                (val) => !!val || 'E-mail é obrigatório',
                (val) => /.+@.+\\..+/.test(val) || 'E-mail inválido',
              ]"
              class="form-input"
              prefix-icon="email"
            />

            <div class="role-section">
              <label class="role-label">Nível de Acesso</label>
              <div class="role-group">
                <q-radio
                  v-model="formEditar.role"
                  checked-icon="task_alt"
                  unchecked-icon="panorama_fish_eye"
                  val="ADMIN"
                  label="Administrador"
                  class="role-radio"
                />
                <q-radio
                  v-model="formEditar.role"
                  checked-icon="task_alt"
                  unchecked-icon="panorama_fish_eye"
                  val="USER"
                  label="Locatário"
                  itemid="editarLocatarioUsuario"
                  class="role-radio"
                />
              </div>
            </div>

            <q-separator class="q-my-md" />

            <div class="modal-actions">
              <q-btn
                flat
                no-caps
                label="Cancelar"
                v-close-popup
                class="btn-cancel"
              />
              <q-btn
                unelevated
                no-caps
                type="submit"
                label="Atualizar"
                class="btn-submit"
                itemid="BtnEditarUsuario"
              />
            </div>
          </q-form>
        </q-card-section>
      </q-card>
    </q-dialog>

    <q-dialog
      v-model="showModalSobre"
      transition-show="scale"
      transition-hide="scale"
    >
      <q-card class="modal-card modal-animated">
        <q-linear-progress :value="1" color="info" class="modal-header-bar" />
        <q-card-section class="modal-header-section">
          <div class="modal-title">
            <q-icon name="person_outline" size="24px" color="info" />
            <span>Detalhes do Usuário</span>
          </div>
          <p class="modal-subtitle">Informações completas do usuário</p>
        </q-card-section>

        <q-separator />

        <q-card-section class="modal-content">
          <q-input
            outlined
            dense
            rounded
            readonly
            v-model="selectedRow.name"
            label="Nome Completo"
            class="form-input"
            prefix-icon="person"
          />
          <q-input
            outlined
            dense
            rounded
            readonly
            v-model="selectedRow.email"
            label="E-mail"
            class="form-input"
            prefix-icon="email"
          />
          <q-input
            outlined
            dense
            rounded
            readonly
            :model-value="mapRole(selectedRow.role)"
            label="Nível de Acesso"
            class="form-input"
            prefix-icon="security"
          />
        </q-card-section>

        <q-separator />

        <q-card-actions align="right" class="modal-actions-bottom">
          <q-btn
            flat
            no-caps
            label="Fechar"
            itemid="BtnSobreUsuario"
            @click="showModalSobre = false"
            class="btn-cancel"
          />
        </q-card-actions>
      </q-card>
    </q-dialog>

    <q-dialog
      v-model="showModalExcluir"
      persistent
      transition-show="scale"
      transition-hide="scale"
    >
      <q-card class="confirm-card modal-animated">
        <q-linear-progress
          :value="1"
          color="negative"
          class="modal-header-bar"
        />
        <q-card-section class="modal-header-section">
          <div class="modal-title">
            <q-icon name="warning" size="24px" color="negative" />
            <span>Confirmar Exclusão</span>
          </div>
          <p class="modal-subtitle">Esta ação não pode ser desfeita</p>
        </q-card-section>

        <q-separator />

        <q-card-section class="modal-content">
          <div class="delete-warning">
            <q-icon name="error_outline" size="48px" color="negative" />
            <p class="delete-message">
              Tem certeza que deseja excluir
              <strong>{{ deleteTarget?.name }}</strong
              >?
            </p>
            <p class="delete-note">
              O usuário será removido permanentemente do sistema.
            </p>
          </div>
        </q-card-section>

        <q-separator />

        <q-card-actions align="right" class="modal-actions-bottom">
          <q-btn
            flat
            no-caps
            label="Cancelar"
            v-close-popup
            class="btn-cancel"
          />
          <q-btn
            unelevated
            no-caps
            color="negative"
            label="Excluir Usuário"
            @click="confirmDelete"
            class="btn-danger"
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

  { name: "actions", align: "center", label: "Acoes", field: "actions" },
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
  box-shadow: 0 4px 16px rgba(22, 24, 22, 0.05),
    inset 0 1px 0 rgba(255, 255, 255, 0.8);
  transition: box-shadow 0.3s ease, transform 0.3s ease;
}

.stat-card:hover {
  box-shadow: 0 8px 28px rgba(22, 24, 22, 0.12),
    inset 0 1px 0 rgba(255, 255, 255, 0.8);
  transform: scale(1.02);
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
  box-shadow: 0 4px 12px rgba(31, 114, 44, 0.3);
  transition: box-shadow 0.3s ease;
}

.add-user-btn:hover {
  box-shadow: 0 6px 16px rgba(31, 114, 44, 0.4);
}

.table-card {
  margin-top: 18px;
  border-radius: 18px;
  border: 1px solid #dfdfdb;
  overflow: hidden;
  background: #fff;
  box-shadow: 0 8px 32px rgba(22, 24, 22, 0.08),
    inset 0 1px 0 rgba(255, 255, 255, 0.6);
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
  box-shadow: 0 20px 60px rgba(22, 24, 22, 0.15);
}

.confirm-card {
  width: min(92vw, 420px);
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(22, 24, 22, 0.15);
}

.modal-animated {
  animation: modalSlideIn 0.35s cubic-bezier(0.34, 1.56, 0.64, 1);
}

@keyframes modalSlideIn {
  from {
    opacity: 0;
    transform: scale(0.9) translateY(20px);
  }
  to {
    opacity: 1;
    transform: scale(1) translateY(0);
  }
}

.modal-header-bar {
  height: 4px;
  border-radius: 16px 16px 0 0;
}

.modal-header-section {
  padding: 24px;
  background: linear-gradient(135deg, #fafaf8 0%, #ffffff 100%);
}

.modal-title {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 1.25rem;
  font-weight: 700;
  color: #222;
  margin: 0;
}

.modal-subtitle {
  font-size: 0.9rem;
  color: #6b6b64;
  margin: 8px 0 0 36px;
  font-weight: 500;
}

.modal-content {
  padding: 24px;
}

.form-input {
  margin-bottom: 14px;
}

.role-section {
  padding: 12px;
  background: rgba(31, 114, 44, 0.04);
  border-radius: 12px;
  border: 1px solid rgba(31, 114, 44, 0.1);
}

.role-label {
  display: block;
  font-size: 0.9rem;
  font-weight: 600;
  color: #222;
  margin-bottom: 12px;
}

.role-group {
  display: flex;
  align-items: center;
  gap: 24px;
  padding: 8px 0;
}

.role-radio {
  margin: 0;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 12px;
}

.modal-actions-bottom {
  padding: 16px 24px;
  gap: 10px;
}

.btn-cancel {
  color: #6b6b64;
  font-weight: 600;
  transition: all 0.25s ease;
  border-radius: 8px;
  padding: 8px 20px;
}

.btn-cancel:hover {
  background: rgba(0, 0, 0, 0.05);
  color: #222;
}

.btn-submit {
  background: linear-gradient(135deg, #1f722c 0%, #235f2f 100%);
  color: #fff;
  font-weight: 600;
  border-radius: 8px;
  padding: 8px 24px;
  transition: all 0.25s ease;
  box-shadow: 0 4px 12px rgba(31, 114, 44, 0.3);
}

.btn-submit:hover {
  box-shadow: 0 6px 16px rgba(31, 114, 44, 0.4);
  transform: translateY(-1px);
}

.btn-danger {
  background: linear-gradient(135deg, #dc2626 0%, #b91c1c 100%);
  font-weight: 600;
  border-radius: 8px;
  padding: 8px 24px;
  transition: all 0.25s ease;
  box-shadow: 0 4px 12px rgba(220, 38, 38, 0.3);
}

.btn-danger:hover {
  box-shadow: 0 6px 16px rgba(220, 38, 38, 0.4);
  transform: translateY(-1px);
}

.delete-warning {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  padding: 12px;
  text-align: center;
}

.delete-message {
  font-size: 1rem;
  color: #222;
  margin: 0;
  font-weight: 600;
}

.delete-message strong {
  color: #dc2626;
  font-weight: 700;
}

.delete-note {
  font-size: 0.85rem;
  color: #999;
  margin: 0;
  line-height: 1.5;
}

.modal-form {
  display: flex;
  flex-direction: column;
  gap: 14px;
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

/* Animações de Entrada */
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

@keyframes slideInRight {
  from {
    opacity: 0;
    transform: translateX(20px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.animate-header {
  animation: slideInDown 0.35s ease-out forwards;
}

.stat-card {
  animation: slideInUp 0.35s ease-out forwards;
}

.stat-card-1 {
  animation-delay: 0.08s;
}

.stat-card-2 {
  animation-delay: 0.13s;
}

.stat-card-3 {
  animation-delay: 0.18s;
}

.animate-toolbar {
  animation: slideInUp 0.35s ease-out 0.23s forwards;
}

.animate-button {
  animation: slideInUp 0.35s ease-out 0.28s forwards;
}

.animate-table-enter {
  animation: fadeInScale 0.4s ease-out 0.33s forwards;
}

/* Inicialmente oculto para aplicar animação */
.animate-header,
.stat-card,
.animate-toolbar,
.animate-button,
.animate-table-enter {
  opacity: 0;
}
</style>
