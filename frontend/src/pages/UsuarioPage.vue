<template>
  <div class="content">
    <!-- Button cadastrar -->
    <div class="containerButton">
      <q-btn style="width: 200px; background-color: #008080; color: white;" v-if="userRole === 'ADMIN'" itemid="cadastroBtnUsuario"
        @click="openRegisterDialog">
        <div class="buttonCadastrar">
          CADASTRAR USUÁRIO
        </div>s
      </q-btn>
    </div>

    <!-- Barra de Pesquisa -->
    <q-form @submit="getRows(srch)" class="q-ml-sm col container">
      <q-input v-model="srch" label="Pesquisar Usuário" class="q-ml-sm col" input-style="min-width: 100%"
        itemid="searchInput">
        <template v-slot:append>
          <q-icon v-if="srch !== ''" name="close" @click="srch = '', getRows(srch)" class="cursor-pointer"
            itemid="closeSearchBtn" />
        </template>

        <template v-slot:after>
          <q-btn @click="getRows(srch)" round dense flat icon="search" itemid="searchBtn" />
        </template>
      </q-input>
      <q-btn-dropdown color="teal-9" :label="filterLabel" icon="filter_list" itemid="filterBtn">
        <q-list>
          <q-item clickable v-close-popup @click="permissionFilter('ADMIN', 'Administrador')" itemid="filterEditorBtn">
            <q-item-section>
              <q-item-label>Administrador</q-item-label>
            </q-item-section>
          </q-item>

          <q-item clickable v-close-popup @click="permissionFilter('USER', 'Locatário')" itemid="filterLeitorBtn">
            <q-item-section>
              <q-item-label>Locatário</q-item-label>
            </q-item-section>
          </q-item>

          <q-item clickable v-close-popup @click="permissionFilter('', 'Todos')" itemid="filterTodosBtn">
            <q-item-section>
              <q-item-label>Todos</q-item-label>
            </q-item-section>
          </q-item>
        </q-list>
      </q-btn-dropdown>
    </q-form>


    <!-- Modal Cadastro -->
    <q-dialog v-model="showModalCadastro">
      <q-card class="modal-card">
        <q-card-section>
          <div class="titulo-cadastro">Cadastro de Usuário</div>
        </q-card-section>

        <q-card-section>
          <q-form @submit="submitFormCadastro">
            <q-input filled v-model="userCreate.name" label="Nome" required lazy-rules :rules="[
              val => !!val || 'Nome é obrigatório']"  itemid="cadastroNomeUsuario"/>

            <q-input filled v-model="userCreate.email" label="Email" type="email" required lazy-rules :rules="[
              val => !!val || 'Email é obrigatório',
              val => /.+@.+\..+/.test(val) || 'Email inválido']" itemid="cadastrarEmailUsuario" />

            <q-input filled :type="isPwd ? 'password' : 'text'" v-model="userCreate.password" required label="Senha"
              prepend-icon="fa-solid fa-lock" lazy-rules :rules="[val => !!val || 'Senha é obrigatório']" itemid="cadastrarSenhaUsuario">
              <template v-slot:append>
                <q-icon :name="isPwd ? 'visibility_off' : 'visibility'" class="cursor-pointer"
                  @click="isPwd = !isPwd"></q-icon>
              </template>
            </q-input>

            <div class="q-mt-md checkbox">
              <q-radio v-model="userCreate.role" checked-icon="task_alt" unchecked-icon="panorama_fish_eye" val="ADMIN"
                label="Editor" :error="roleError" :error-message="roleErrorMessage" itemid="cadastrarAdministradorUsuario" />
              <q-radio v-model="userCreate.role" checked-icon="task_alt" unchecked-icon="panorama_fish_eye" val="USER"
                label="Locatário" :error="roleError" :error-message="roleErrorMessage" itemid="cadastrarLocatarioUsuario" />
            </div>


            <div class="button-container">
              <q-btn type="submit" label="CADASTRAR" class="center-width q-mt-md" itemid="BtnCadastrarUsuario" />
            </div>
          </q-form>
        </q-card-section>
      </q-card>
    </q-dialog>

    <!-- Modal Editar -->
    <q-dialog v-model="showModalEditar">
      <q-card class="modal-card">
        <q-card-section>
          <div class="titulo-cadastro">Editar Usuário</div>
        </q-card-section>

        <q-card-section>
          <q-form @submit.prevent="submitFormEditar">
            <q-input filled v-model="formEditar.name" label="Nome" required lazy-rules
              :rules="[val => !!val || 'Nome é obrigatório']" itemid="editarNomeUsuario" />

            <q-input filled v-model="formEditar.email" label="Email" type="email" required lazy-rules
              :rules="[val => !!val || 'Email é obrigatório', val => /.+@.+\..+/.test(val) || 'Email inválido']"  itemid="emailNomeUsuario"/>

            <div class="q-mt-md checkbox">
              <q-radio v-model="formEditar.role" checked-icon="task_alt" unchecked-icon="panorama_fish_eye" val="ADMIN"
                label="Editor" />
              <q-radio v-model="formEditar.role" checked-icon="task_alt" unchecked-icon="panorama_fish_eye" val="USER"
                label="Locatário" itemid="editarLocatarioUsuario" />
            </div>

            <div class="button-container">
              <q-btn type="submit" label="ATUALIZAR" class="center-width q-mt-md" itemid="BtnEditarUsuario" />
            </div>
          </q-form>
        </q-card-section>
      </q-card>
    </q-dialog>


    <!-- Modal Sobre -->
    <q-dialog v-model="showModalSobre">
      <q-card class="modal-card">
        <q-card-section>
          <div class="titulo-sobre text-center">Detalhes do Usuário</div>
        </q-card-section>
        <q-card-section>
          <div class="form-grid">
            <q-input filled v-model="selectedRow.name" label="Nome" readonly />
            <br>
            <q-input filled v-model="selectedRow.email" label="Email" readonly />
            <br>
            <q-input filled v-model="mappedRole" label="Permissão" readonly />
          </div>
        </q-card-section>
        <q-card-actions class="button-sobre">
          <q-btn label="Fechar" @click="showModalSobre = false" itemid="BtnSobreUsuario" />
        </q-card-actions>
      </q-card>
    </q-dialog>

    <!-- Tabela de usuários -->
    <div class="table-container">
      <q-table class="custom-table" :rows="paginatedRows" :columns="columns" row-key="email" hide-bottom
        :footer-props="{ show: false }">
        <template v-slot:body-cell-role="props">
          <q-td :props="props" style="vertical-align: middle;">
            <div>{{ mapRole(props.row.role) }}</div>
          </q-td>
        </template>
        <template v-slot:body-cell-actions="props">
          <q-td :props="props" style="vertical-align: middle;">
            <q-btn flat color="primary" @click="showDetails(props.row)" icon="visibility" aria-label="View" :itemid="'visibility' + '-' + props.row.name"><q-tooltip
                class="bg-primary" :ffset="[10, 10]">
                Visualizar detalhes
              </q-tooltip></q-btn>
            <q-btn v-if="props.row.id !== adminId && userRole === 'ADMIN'" flat color="secondary" :itemid="'edit' + '-' + props.row.name"
              @click="editRow(props.row)" icon="edit" aria-label="Edit" ><q-tooltip class="bg-secondary"
                :ffset="[10, 10]">
                Editar Usuário
              </q-tooltip></q-btn>
          </q-td>
        </template>
      </q-table>
    </div>
    <div class="row justify-center q-my-md">
      <q-btn :disable="page.value <= 0" @click="prevPage" class="q-mx-sm">
        <q-icon name="chevron_left" />
      </q-btn>
      <q-btn :disable="page.value >= totalPages - 1" @click="nextPage" class="q-mx-sm">
        <q-icon name="chevron_right" />
      </q-btn>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { api } from 'src/boot/axios';
import { Notify } from 'quasar';

const showModalCadastro = ref(false);
const showModalEditar = ref(false);
const showModalSobre = ref(false);
const search = ref('');
const currentPage = ref(1);
const maxRowsPerPage = 10;
const isPwd = ref(true);

const userCreate = ref({
  name: '',
  email: '',
  password: '',
  role: ''
});

const formEditar = ref({
  id: null,
  name: '',
  email: '',
  role: ''
});

const selectedRow = ref({
  name: '',
  email: '',
  password: '',
  role: ''
});

const adminId = 1;


const sortRowsAscByName = () => {
  rows.value.sort((a, b) => a.name.localeCompare(b.name));
};

const sortRowsDescByName = () => {
  rows.value.sort((a, b) => b.name.localeCompare(a.name));
};

const sortRowsAscByEmail = () => {
  rows.value.sort((a, b) => a.email.localeCompare(b.email));
};

const sortRowsDescByEmail = () => {
  rows.value.sort((a, b) => b.email.localeCompare(a.email));
};


const openRegisterDialog = () => {
  showModalCadastro.value = true;
};

const columns = [
  { name: 'name', required: true, label: 'Nome do usuário', align: 'center', field: row => row.name, format: val => `${val}`, sortable: true },
  { name: 'email', required: true, label: 'Email do usuário', align: 'center', field: row => row.email, format: val => `${val}`, sortable: true },
  { name: 'role', align: 'center', label: 'Permissão', field: row => traduzirRole(row.role), sortable: true },
  { name: 'actions', align: 'center', label: 'Ações', field: 'actions', sortable: true },
]

const rows = ref([]);

const performSearch = () => {
  console.log("Executando pesquisa para:", search.value);
  getRows(search.value);
};

const page = ref(0)

const prevPage = () => {
  if (page.value > 0) {
    page.value--;
    getRows(search.value);
  }
};


const nextPage = () => {
  page.value++;
  getRows(search.value);
};


const getRows = (srch = '', role = roleFilter.value) => {
  api.get('/user', { params: { search: srch, page: page.value, role: role } })
    .then(response => {
      rows.value = response.data.content;
    })
    .catch(error => {
      console.error("Erro ao obter dados:", error);
    });
};

const checkUserExists = (name, email) => {
  const userExists = rows.value.some(user => user.name === name);
  const emailExists = rows.value.some(user => user.email === email);

  return {
    userExists,
    emailExists,
  };
};

const userRole = ref('');

onMounted(() => {
  const token = localStorage.getItem('authToken');
  if (!token) {
    router.push('/login');
  } else {
    userRole.value = localStorage.getItem('role')
    getRows();
  }
});

const filteredRows = computed(() => {
  const searchLower = search.value.toLowerCase();
  return rows.value.filter(row =>
    row.name.toLowerCase().includes(searchLower) ||
    row.role.toLowerCase().includes(searchLower)
  );
});


const paginatedRows = computed(() => {
  const start = (currentPage.value - 1) * maxRowsPerPage;
  return filteredRows.value.slice(start, start + maxRowsPerPage);
});

const roleFilter = ref('')
const filterLabel = ref('Filtrar');

const permissionFilter = (permission, label) => {
  console.log('Filtro selecionado:', permission);
  roleFilter.value = permission;
  filterLabel.value = label;
  getRows();
};

const submitFormCadastro = () => {
  if (!userCreate.value.role) {
    showNotification('negative', 'Você deve selecionar uma dos níveis de acesso editor ou locatário!');
    return;
  }

  api.post('/user', userCreate.value)
    .then(response => {
      showNotification('positive', "Usuário cadastrado com sucesso!");
      getRows();
      showModalCadastro.value = false;
      resetFormCadastro();
    })
    .catch(error => {
      let errorMessage = "Erro ao cadastrar usuário!";

      if (error.response) {
        if (error.response.status === 400) {
          errorMessage = Object.values(error.response.data).join(', ') || errorMessage;
        } else if (error.response.data.message) {
          errorMessage = error.response.data.message;
        }
      }

      console.error("Erro ao cadastrar usuário:", error.response ? error.response.data : error.message);
      showNotification('negative', errorMessage);
    });
};

const resetFormCadastro = () => {
  userCreate.value = {
    name: '',
    email: '',
    password: '',
    role: ''
  };
};

const editRow = (row) => {
  formEditar.value = { ...row };
  showModalEditar.value = true;
};

const onSearch = () => {
  if (search.value.trim() === '') {
    getRows();
  } else {
    getRows(search.value);
  }
};


const submitFormEditar = () => {
  api.put(`/user/${formEditar.value.id}`, formEditar.value)
    .then(response => {
      showNotification('positive', "Usuário atualizado com sucesso!");
      getRows();
      showModalEditar.value = false;
    })
    .catch(error => {
      let errorMessage = "Erro ao atualizar usuário!";

      if (error.response) {
        if (error.response.status === 400) {

          errorMessage = Object.values(error.response.data).join(', ') || errorMessage;
        } else if (error.response.data.message) {
          errorMessage = error.response.data.message;
        }
      }

      console.error("Erro ao atualizar usuário:", error.response ? error.response.data : error.message);
      showNotification('negative', errorMessage);
    });
};


const roleMapping = {
  ADMIN: 'Administrador',
  USER: 'Locatário'
};
const roleError = ref(false);
const roleErrorMessage = ref('');

const current = ref();
const mappedRole = computed(() => mapRole(selectedRow.value.role));
const mapRole = (role) => roleMapping[role] || role;

const showNotification = (type, message) => {
  Notify.create({
    type: type,
    message: message,
    position: 'top'
  });
};

const showDetails = (row) => {
  showModalSobre.value = true;
  loadUserDetails(row.id);
};

const loadUserDetails = (id) => {
  api.get(`/user/${id}`)
    .then(response => {
      if (response.data) {
        selectedRow.value = response.data;
        showModalSobre.value = true;
      } else {
        console.error('Usuário não encontrado:', response.data);
      }
    })
    .catch(error => {
      console.error("Erro ao obter detalhes do usuário:", error);
    });
};

</script>

<style scoped>
.content {
  padding: 16px;
  min-height: 90vh;
}

.containerButton {
  display: flex;
  justify-content: center;
  margin-bottom: 16px;
}

.modal-card {
  width: 600px;
  padding: 10px;
  border-radius: 20px;
  max-width: 90vw;
  box-shadow: 15px 13px 61px -17px rgba(0, 0, 0, 0.49);
}


.titulo-cadastro {
  font-size: 1.2rem;
  text-align: center;
  margin-bottom: 16px;
}

.checkbox {
  display: flex;
  justify-content: space-around;
}

.button-container {
  display: flex;
  justify-content: center;
}

.table-container {
  margin-top: 16px;
}

.buttonCadastrar {
  display: flex;
  justify-content: center;
  align-items: center;
}

.titulo-sobre {
  font-size: 1.2rem;
  text-align: center;
  margin-bottom: 16px;
}

.cursor-pointer {
  cursor: pointer;
  margin-left: 5px;
}

.center-width {
  width: 100%;
}

.pagination-container {
  display: flex;
  justify-content: center;
  align-items: center
}

.container {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 20px;
  padding-bottom: 20px;
  max-width: 1300px;
  width: 100%;
  margin: 0 auto;
}

.q-input.pesquisa {
  font-size: 16px;
  font-weight: 800;
  color: rgba(0, 0, 0, 0.60);
}

.custom-table {
  max-width: 1300px;
  width: 100%;
  margin: 0 auto;
}

.pesquisa {
  display: flex;
  max-width: 1300px;
  height: 53px;
  border-radius: 4px;
  width: 100%;
  margin: 0 auto;
}

.button-pesquisar {
  font-size: 15px;
  font-weight: 800;
}


.footer {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 60px;
  background-color: white;
}

@media (max-width: 700px) {
  .button-pesquisar {
    display: none;
  }
}
</style>
