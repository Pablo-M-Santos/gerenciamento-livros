  <template>
    <div class="content">
      <!-- Button cadastrar -->
      <div class="containerButton">
        <q-btn style="width: 200px; background-color: #008080; color: white;" v-if="userRole === 'ADMIN'" itemid="cadastroBtnEditora"
          @click="openRegisterDialog">
          <div class="buttonCadastrar">
            CADASTRAR EDITORA
          </div>
        </q-btn>
      </div>

      <!-- Barra de Pesquisa -->
      <q-form @submit="getRows(srch)" class="q-ml-sm col container">
        <q-input v-model="srch" label="Pesquisar Editora" class="q-ml-sm col" input-style="min-width: 100%"
          itemid="searchInput">
          <template v-slot:append>
            <q-icon v-if="srch !== ''" name="close" @click="srch = '', getRows(srch)" class="cursor-pointer"
              itemid="closeSearchBtn" />
          </template>

          <template v-slot:after>
            <q-btn @click="getRows(srch)" round dense flat icon="search" itemid="searchBtn" />
          </template>
        </q-input>
      </q-form>

      <!-- Modal Cadastro -->
      <q-dialog v-model="showModalCadastro">
        <q-card class="modal-card">
          <q-card-section>
            <div class="titulo-cadastro">Cadastro de Editora</div>
          </q-card-section>

          <q-card-section>
            <q-form @submit.prevent="saveNewPublisher">
              <q-input filled v-model="newPublisher.name" label="Nome da Editora" required lazy-rules
                :rules="[val => !!val || 'Nome da Editora é obrigatório']" itemid="cadastroNomeEditora" />

              <q-input filled v-model="newPublisher.telephone" label="Telefone" type="tel" required lazy-rules
                mask="(##) #####-####"
                :rules="[val => !!val || 'Telefone é obrigatório', val => /^\(\d{2}\) \d{5}-\d{4}$/.test(val) || 'Telefone inválido']" itemid="cadastroTelefoneEditora" />

              <q-input filled v-model="newPublisher.email" label="Email" type="email" required lazy-rules
                :rules="[val => !!val || 'Email é obrigatório', val => /^.+@gmail\.com$/.test(val) || 'O e-mail deve ser um endereço Gmail válido']" itemid="cadastrarEmailEditora" />

              <q-input filled v-model="newPublisher.site" label="Site " lazy-rules :rules="[
                val => !val || /^https:\/\/.+/.test(val) || 'O site deve começar com https://'
              ]" itemid="cadastrarSiteEditora" />

              <div class="button-container">
                <q-btn type="submit" label="CADASTRAR" class="center-width q-mt-md" itemid="BtnCadastrarEditora" />
              </div>
            </q-form>
          </q-card-section>
        </q-card>
      </q-dialog>

      <!-- Modal Editar -->
      <q-dialog v-model="showModalEditar">
        <q-card class="modal-card">
          <q-card-section>
            <div class="titulo-cadastro">Editar Editora</div>
          </q-card-section>
          <q-card-section>
            <q-form>
              <q-input filled v-model="editPublisher.name" label="Nome da Editora" required lazy-rules
                :rules="[val => !!val || 'Nome da Editora é obrigatório']" itemid="editarNomeEditora" />

              <q-input filled v-model="editPublisher.telephone" label="Telefone" type="tel" required lazy-rules
                mask="(##) #####-####"
                :rules="[val => !!val || 'Telefone é obrigatório', val => /^\(\d{2}\) \d{5}-\d{4}$/.test(val) || 'Telefone inválido']"  itemid="editarTelefoneEditora"/>

              <q-input filled v-model="editPublisher.email" label="Email" type="email" required lazy-rules
                :rules="[val => !!val || 'Email é obrigatório', val => /^.+@gmail\.com$/.test(val) || 'O e-mail deve ser um endereço Gmail válido']"  itemid="editarEmailEditora" />

              <q-input filled v-model="editPublisher.site" label="Site " lazy-rules :rules="[
                val => !val || /^https:\/\/.+/.test(val) || 'O site deve começar com https://'
              ]"  itemid="editarSiteEditora"/>
              <div class="button-container">
                <q-btn type="submit" label="ATUALIZAR" @click="saveEdit" class="center-width q-mt-md"  itemid="BtnEditarEditora"/>
              </div>
            </q-form>
          </q-card-section>
        </q-card>
      </q-dialog>

      <!-- Modal Sobre -->
      <q-dialog v-model="showModalSobre">
        <q-card class="modal-card">
          <q-card-section>
            <div class="titulo-sobre text-center">Detalhes da Editora</div>
          </q-card-section>
          <q-card-section>
            <div class="form-grid">
              <q-input filled v-model="selectedRow.name" label="Nome" readonly />
              <br>
              <q-input filled v-model="selectedRow.telephone" label="Telefone" readonly />
              <br>
              <q-input filled v-model="selectedRow.email" label="Email" readonly />
              <br>
              <q-input filled v-model="selectedRow.site" label="Site" readonly />
            </div>
          </q-card-section>
          <q-card-actions class="button-sobre">
            <q-btn label="Fechar" @click="showModalSobre = false"  itemid="BtnSobreUsuario" />
          </q-card-actions>
        </q-card>
      </q-dialog>

      <!-- Modal Exclusão -->
      <q-dialog v-model="showModalExcluir">
        <q-card class="modal-card-exclusao">
          <q-card-section class="text-center">
            <div class="circulo">
              <i class="fa-solid fa-exclamation"></i>
            </div>
            <h3 class="titulo-exclusao">Tem certeza que deseja excluir?</h3>
          </q-card-section>

          <q-card-actions class="button-exclusao">
            <q-btn label="SIM" color="negative" @click="confirmDelete" class="q-mr-sm" itemid="BtnExcluirUsuario" />
            <q-btn label="NÃO" color="secondary" @click="cancelDelete"  />
          </q-card-actions>
        </q-card>
      </q-dialog>

      <!-- Table -->
      <div class="table-container">
        <q-table class="custom-table" :pagination="pagination" :rows="paginatedRows" :columns="columns" row-key="id"
          hide-bottom>
          <template v-slot:body-cell-actions="props">
            <q-td :props="props" class="text-center">
              <q-btn flat color="primary" @click="showDetails(props.row)" icon="visibility" aria-label="View" :itemid="'visibility' + '-' + props.row.name"><q-tooltip
                  class="bg-primary" :ffset="[10, 10]">
                  Visualizar detalhes
                </q-tooltip></q-btn>
              <q-btn flat color="secondary" v-if="userRole === 'ADMIN'" @click="editRow(props.row)" icon="edit" :itemid="'edit' + '-' + props.row.name"
                aria-label="Edit"><q-tooltip class="bg-secondary" :ffset="[10, 10]">
                  Editar Editora
                </q-tooltip></q-btn>
              <q-btn flat color="negative" v-if="userRole === 'ADMIN'" @click="showDeleteModal(props.row)" icon="delete" :itemid="'delete' + '-' + props.row.name"
                aria-label="Delete"><q-tooltip class="bg-negative" :ffset="[10, 10]">
                  Excluir Editora
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
import { api } from 'src/boot/axios.js';
import { Notify } from 'quasar';

const showModalCadastro = ref(false);
const showModalSobre = ref(false);
const showModalEditar = ref(false);
const showModalExcluir = ref(false);
const rowToDelete = ref(null);
const selectedRow = ref(null);
const search = ref('');
const page = ref(0);
const rowsPerPage = 10;
const currentPage = ref(1);
const maxRowsPerPage = 10;

const newPublisher = ref({ name: '', email: '', telephone: '', site: '' });
const editPublisher = ref([]);


const columns = [
  { name: 'name', required: true, label: 'Nome da Editora', align: 'center', field: row => row.name, format: val => `${val}`, sortable: true },
  { name: 'email', required: true, label: 'Email', align: 'center', field: row => row.email, format: val => `${val}`, sortable: true },
  { name: 'telephone', required: true, label: 'Telefone', align: 'center', field: row => row.telephone, format: val => `${val}`, sortable: true },
  { name: 'actions', align: 'center', label: 'Ações', field: 'actions' },
];
const rows = ref([]);

const pagination = ref({
  page: 1,
  rowsPerPage: 8,
});



const saveNewPublisher = async () => {
  try {
    const response = await api.post('/publisher', newPublisher.value, {
      headers: {
        'Content-Type': 'application/json',
      },
    });

    rows.value.push(response.data);
    Notify.create({
      color: 'green',
      textColor: 'white',
      icon: 'check_circle',
      message: 'Editora criada com sucesso!',
      position: 'top'
    });
    getRows();
    showModalCadastro.value = false;
  } catch (error) {
    let errorMessage = 'Erro ao criar editora!';

    if (error.response) {
      if (error.response.status === 400) {
        errorMessage = Object.values(error.response.data).join(', ') || errorMessage;
      } else if (error.response.data.message) {
        errorMessage = error.response.data.message;
      }
    }

    console.error('Erro ao criar nova editora:', error.response ? error.response.data : error.message);
    Notify.create({
      color: 'red',
      textColor: 'white',
      icon: 'error',
      message: errorMessage,
      position: 'top'
    });
  }
};

const performSearch = () => {
  onSearch();
};

const getRows = (srch = '') => {
  api.get('/publisher', { params: { search: srch, page: page.value } })
    .then(response => {
      if (Array.isArray(response.data.content)) {
        rows.value = response.data.content;
      } else {
        console.error('A resposta da API não é um array:', response.data);
        rows.value = [];
      }
      console.log('Resposta da API:', response.data);
    })
    .catch(error => {
      console.error("Erro ao obter dados:", error);
    });
}


const confirmDelete = async () => {
  if (rowToDelete.value && rowToDelete.value.id) {
    try {
      const response = await api.delete(`/publisher/${rowToDelete.value.id}`);
      const index = rows.value.findIndex(r => r.id === rowToDelete.value.id);
      if (index !== -1) {
        rows.value.splice(index, 1);
      }
      Notify.create({
        color: 'red',
        textColor: 'white',
        icon: 'delete',
        message: response.data.message || 'Registro excluído com sucesso!',
        position: 'top'
      });
    } catch (error) {
      let errorMessage = 'Erro ao excluir registro!';

      if (error.response) {
        if (error.response.data.message) {
          errorMessage = error.response.data.message;
        } else if (error.response.status === 400) {
          errorMessage = Object.values(error.response.data).join(', ') || errorMessage;
        }
      }

      console.error("Erro ao excluir:", error.response ? error.response.data : error.message);
      Notify.create({
        color: 'red',
        textColor: 'white',
        icon: 'error',
        message: errorMessage,
        position: 'top'
      });
    } finally {
      showModalExcluir.value = false;
    }
  }
};


const onSearch = () => {
};


const totalPages = computed(() => {
  return Math.ceil(rows.value.length / rowsPerPage);
});



const paginatedRows = computed(() => {
  const start = (currentPage.value - 1) * maxRowsPerPage;
  return rows.value.slice(start, start + maxRowsPerPage);
});

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

const loadPublisherDetails = (id) => {
  api.get(`/publisher/${id}`)
    .then(response => {
      if (response.data) {
        selectedRow.value = response.data;
        showModalSobre.value = true;
      } else {
        console.error('Editora não encontrada:', response.data);
      }
    })
    .catch(error => {
      console.error("Erro ao obter detalhes da editora:", error);
    });
};


const showNotification = (type, message) => {
  Notify.create({
    color: type === 'positive' ? 'green' : 'red',
    textColor: 'white',
    icon: type === 'positive' ? 'check_circle' : 'error',
    message: message,
    position: 'top',
  });
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

const openRegisterDialog = () => {
  showModalCadastro.value = true;
};

const showDetails = (row) => {
  loadPublisherDetails(row.id);
};

const showDeleteModal = (row) => {
  rowToDelete.value = row;
  showModalExcluir.value = true;
};

const cancelDelete = () => {
  showModalExcluir.value = false;
};

const editRow = (row) => {
  api.get(`/publisher/${row.id}`)
    .then(response => {
      if (response.data) {
        editPublisher.value = { ...response.data };
        showModalEditar.value = true;
      } else {
        Notify.create({
          color: 'red',
          textColor: 'white',
          icon: 'error',
          message: 'Editora não encontrada!',
          position: 'top'
        });
        console.error('Editora não encontrada:', response.data);
      }
    })
    .catch(error => {
      Notify.create({
        color: 'red',
        textColor: 'white',
        icon: 'error',
        message: 'Erro ao obter dados da editora!',
        position: 'top'
      });
      console.error("Erro ao obter dados da editora:", error);
    });
};


const saveEdit = async () => {
  try {
    const response = await api.put(`/publisher/${editPublisher.value.id}`, editPublisher.value, {
      headers: {
        'Content-Type': 'application/json',
      },
    });

    const index = rows.value.findIndex(r => r.id === editPublisher.value.id);
    if (index !== -1) {
      rows.value[index] = response.data;
    }

    Notify.create({
      color: 'green',
      textColor: 'white',
      icon: 'check_circle',
      message: 'Editora atualizada com sucesso!',
      position: 'top',
    });
    showModalEditar.value = false;
  } catch (error) {
    let errorMessage = 'Erro ao atualizar editora!';

    if (error.response) {
      if (error.response.status === 400) {
        errorMessage = Object.values(error.response.data).join(', ') || errorMessage;
      } else if (error.response.data.message) {
        errorMessage = error.response.data.message;
      }
    }

    console.error('Erro ao atualizar editora:', error.response ? error.response.data : error.message);
    Notify.create({
      color: 'red',
      textColor: 'white',
      icon: 'error',
      message: errorMessage,
      position: 'top',
    });
  }
};


</script>


<style scoped>
.content {
  padding: 16px;
}

.containerButton {
  display: flex;
  justify-content: center;
  margin-bottom: 16px;
}

.titulo-sobre {
  font-size: 1.2rem;
  text-align: center;
  margin-bottom: 16px;
}

.modal-card {
  width: 600px;
  padding: 10px;
  border-radius: 20px;
  max-width: 90vw;
  box-shadow: 15px 13px 61px -17px rgba(0, 0, 0, 0.49);
}

.modal-card-exclusao {
  width: 400px;
  border-radius: 10px;
  box-shadow: 15px 13px 61px -17px rgba(0, 0, 0, 0.49);
}

.titulo-cadastro {
  font-size: 18px;
  text-align: center;
}

.button-container {
  display: flex;
  justify-content: center;
  margin-top: 16px;
}

.center-width {
  width: 100%;
  max-width: 200px;
  margin: 0 auto;
}

.table-container {
  margin-top: 16px;
}

.titulo-exclusao {
  font-size: 18px;
  margin: 16px 0;
}

.button-exclusao {
  display: flex;
  justify-content: center;
}

.custom-table {
  max-width: 1300px;
  width: 100%;
  margin: 0 auto;
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

.pesquisa {
  display: flex;
  max-width: 1300px;
  height: 53px;
  border-radius: 4px;
  width: 100%;
  margin: 0 auto;
}

.q-input.pesquisa {
  font-size: 16px;
  font-weight: 800;
  color: rgba(0, 0, 0, 0.60);
}

.button-pesquisar {
  font-size: 15px;
  font-weight: 800;
}

@media (max-width: 700px) {
  .button-pesquisar {
    display: none;
  }
}
</style>
