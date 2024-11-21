  <template>
    <div class="content">
      <!-- Button cadastrar -->
      <div class="containerButton">
        <q-btn style="width: 200px; background-color: #008080; color: white;" v-if="userRole === 'ADMIN'"
          itemid="cadastroBtnAluguel" @click="openRegisterDialog">
          <div class="buttonCadastrar">
            CADASTRAR ALUGUEL
          </div>
        </q-btn>
      </div>

      <!-- Barra de Pesquisa -->
      <div class="container">
        <q-input v-model="search" placeholder="Pesquisar Aluguel" class="q-ml-sm col pesquisa"
          @keyup.enter="performSearch">
          <template v-slot:append>
            <q-icon v-if="search !== ''" name="close" @click="search = '', getRows(search)" class="cursor-pointer" />
          </template>
          <template v-slot:after>
            <q-btn @click="getRows(search)" round dense flat icon="search" />
          </template>
        </q-input>
      </div>


      <!-- Modal Cadastro -->
      <q-dialog v-model="showModalCadastro">
        <q-card class="modal-card">
          <q-card-section>
            <div class="titulo-cadastro">Cadastro de Aluguel</div>
          </q-card-section>
          <q-card-section>
            <q-form @submit.prevent="rentAction()">

              <q-select itemid="renterInput" filled v-model="selectedRenter" use-input hide-selected fill-input
                input-debounce="0" :options="renters" option-label="name" @filter="rentersFilter"
                @update:model-value="onItemClickRent" label="Locatário" required="true" />


              <q-select itemid="bookInput" filled v-model="selectedBook" use-input hide-selected fill-input
                input-debounce="0" :options="books" option-label="name" @filter="booksFilter"
                @update:model-value="onItemClickBook" label="Livro" required="true" />

              <q-input v-model="bookToRent.deadLine" label="Devolução" type="date" :min="today" :max="maxReturnDate"
                required filled itemid="deliveryInput" />


              <div class="button-container">
                <q-btn type="submit" label="CADASTRAR" class="center-width q-mt-md" itemid="BtnCadastrarAluguel" />
              </div>
            </q-form>
          </q-card-section>
        </q-card>
      </q-dialog>

      <!-- Modal Edição -->
      <q-dialog v-model="showModalEdicao">
        <q-card class="modal-card">
          <q-card-section>
            <div class="titulo-cadastro">Editar Aluguel</div>
          </q-card-section>
          <q-card-section>
            <q-form @submit.prevent="editRent">
              <q-select v-model="rentToEdit.renterId" label="Selecione o Locatário" filled use-input input-debounce="0"
                :options="renterOptions" @filter="filterPublisher" option-label="name" option-value="id" emit-value
                map-options class="q-mb-md" :rules="[val => !!val || 'É obrigatório selecionar um locatário']"
                itemid="editarLocatarioAluguel" />

              <q-select v-model="rentToEdit.bookId" label="Selecione o Livro" filled use-input input-debounce="0"
                :options="bookOptions" @filter="filterBook" option-label="name" option-value="id" emit-value map-options
                class="q-mb-md" :rules="[val => !!val || 'É obrigatório selecionar um livro']"
                itemid="editarLivroAluguel" />

              <q-input v-model="rentToEdit.deadLine" label="Prazo final" type="date"
                :rules="[val => !!val || 'É obrigatório informar um prazo']" itemid="editarDataAluguel" />

              <div class="button-container">
                <q-btn type="submit" label="SALVAR" class="center-width q-mt-md" />
              </div>
            </q-form>
          </q-card-section>
        </q-card>
      </q-dialog>



      <!-- Modal Devolução -->
      <q-dialog v-model="showModalDevolucao">
        <q-card class="modal-card-confirmacao">
          <q-card-section class="text-center">
            <div class="circulo">
              <i class="fa-solid fa-exclamation"></i>
            </div>
            <h3 class="titulo-confirmacao">Tem certeza que deseja devolver?</h3>
          </q-card-section>
          <q-card-actions class="button-confirmacao">
            <q-btn label="SIM" color="secondary" @click="confirmReturn" class="q-mr-sm" />
            <q-btn label="NÃO" color="negative" @click="cancelReturn" />
          </q-card-actions>
        </q-card>
      </q-dialog>

      <!-- Tabela de livros -->
      <div class="table-container">
        <q-table class="custom-table" :pagination="pagination" :rows="paginatedRows" :columns="columns" row-key="id"
          hide-bottom>

          <template v-slot:body-cell-actions="props">
            <q-td :props="props" style="vertical-align: middle;">
              <q-btn flat color="accent"
                v-if="userRole === 'ADMIN' && props.row.status !== 'ENTREGUE' && props.row.status !== 'ENTREGUE_COM_ATRASO' && props.row.status !== 'NO_PRAZO'"
                @click="showReturnModal(props.row)" icon="check" aria-label="Confirm"
                :itemid="'confirmar' + '-' + props.row.id"><q-tooltip class="bg-accent" :ffset="[10, 10]">
                  Devolução de Livro
                </q-tooltip></q-btn>
              <q-btn flat color="secondary"
                v-if="userRole === 'ADMIN' && props.row.status !== 'ENTREGUE' && props.row.status !== 'ENTREGUE_COM_ATRASO' && props.row.status !== 'NO_PRAZO'"
                @click="editRow(props.row)" icon="edit" aria-label="Edit"
                :itemid="'edit' + '-' + props.row.id"><q-tooltip class="bg-secondary" :ffset="[10, 10]">
                  Editar Aluguel
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
import { ref, reactive, computed, onMounted } from 'vue'
import { useQuasar } from 'quasar'
import { api } from 'src/boot/axios.js'

const $q = useQuasar()

const showModalCadastro = ref(false)
const showModalDevolucao = ref(false)
const showModalEdicao = ref(false)
const rowToReturn = ref(null)
const search = ref('')
const currentPage = ref(1);
const maxRowsPerPage = 10;


const today = new Date().toISOString().split('T')[0];

const maxReturnDate = ref(new Date(new Date().setDate(new Date().getDate() + 29)).toISOString().split('T')[0]);


const page = ref(0);
const rowsPerPage = 5;

const newRent = ref({
  renterId: '',
  bookId: '',
  deadLine: ''
})

const rentToEdit = ref({
  renterId: '',
  bookId: '',
  deadLine: '',
});

const rows = ref([])
const columns = computed(() => {
  const baseColumns = [
    { name: 'renter.name', align: 'center', label: 'Locatário', field: row => row.renter.name, sortable: true },
    { name: 'book.name', align: 'center', label: 'Livro', field: row => row.book.name, sortable: true },
    { name: 'rentDate', align: 'center', label: 'Alugado', field: 'rentDate', sortable: true },
    { name: 'deadLine', align: 'center', label: 'Devolução', field: 'deadLine', sortable: true },
    { name: 'status', align: 'center', label: 'Status', field: row => traduzirStatus(row.status), sortable: true },
    { name: 'actions', align: 'center', label: 'Ações', field: 'actions' }
  ];

  if (userRole.value === 'ADMIN') {
    baseColumns.push({ name: 'actions', align: 'center', label: 'Ações', field: 'actions' });
  }

  return baseColumns;
});


const srch = ref('');
const statusFiltered = ref('');
const sortBy = ref('');
const sortDesc = ref(false);

const handleSort = (colName) => {
  if (sortBy.value === colName) {
    sortDesc.value = !sortDesc.value;
  } else {
    sortBy.value = colName;
    sortDesc.value = false;
  }
};

const sortedRows = computed(() => {
  const sorted = [...rows.value];
  if (sortBy.value) {
    sorted.sort((a, b) => {
      const aVal = typeof a[sortBy.value] === 'string' ? a[sortBy.value].toLowerCase() : a[sortBy.value];
      const bVal = typeof b[sortBy.value] === 'string' ? b[sortBy.value].toLowerCase() : b[sortBy.value];

      if (aVal < bVal) return sortDesc.value ? 1 : -1;
      if (aVal > bVal) return sortDesc.value ? -1 : 1;
      return 0;
    });
  }
  return sorted;
});


const editRow = (row) => {
  rentToEdit.value = {
    id: row.id,
    renterId: row.renter.id,
    bookId: row.book.id,
    deadLine: row.deadLine
  };
  showModalEdicao.value = true;
};

const performSearch = () => {
  console.log("Executando pesquisa para:", search.value);
  getRows(search.value);
};

const editRent = () => {
  if (!rentToEdit.value.renterId || !rentToEdit.value.bookId || !rentToEdit.value.deadLine) {
    showNotification('negative', "Todos os campos são obrigatórios!");
    return;
  }

  api.put('/rent/update/' + rentToEdit.value.id, rentToEdit.value)
    .then(response => {
      console.log("Sucesso", response);
      showNotification('positive', "Aluguel atualizado com sucesso!");
      showModalEdicao.value = false;
      getRows();
    })
    .catch(error => {
      console.log("Erro ao editar aluguel", error);
      showNotification('negative', "Erro ao atualizar aluguel!");
    });
};



const pagination = ref({
  page: 1,
  rowsPerPage: 8,
});

const filter = ref('')

const openRegisterDialog = () => {
  newRent.value = { renterId: '', bookId: '', deadLine: '' }
  showModalCadastro.value = true
}
const sortRowsAscByRenterName = () => {
  rows.value.sort((a, b) => a.renterName.localeCompare(b.renterName));
};

const sortRowsDescByRenterName = () => {
  rows.value.sort((a, b) => b.renterName.localeCompare(a.renterName));
};

const sortRowsAscByBookName = () => {
  rows.value.sort((a, b) => a.bookName.localeCompare(b.bookName));
};

const sortRowsDescByBookName = () => {
  rows.value.sort((a, b) => b.bookName.localeCompare(a.bookName));
};

const sortRowsAscByDeadLineDate = () => {
  rows.value.sort((a, b) => new Date(a.deadLineDate) - new Date(b.deadLineDate));
};

const sortRowsDescByDeadLineDate = () => {
  rows.value.sort((a, b) => new Date(b.deadLineDate) - new Date(a.deadLineDate));
};

const sortRowsAscByRentDate = () => {
  rows.value.sort((a, b) => new Date(a.rentDate) - new Date(b.rentDate));
};

const sortRowsDescByRentDate = () => {
  rows.value.sort((a, b) => new Date(b.rentDate) - new Date(a.rentDate));
};


const saveNewRent = () => {
  const today = new Date();
  const deadLine = new Date();
  deadLine.setDate(today.getDate() + 30);

  api.post('/rent', {
    renterId: newRent.value.renterId,
    bookId: newRent.value.bookId,
    deadLine: deadLine.toISOString().split('T')[0]
  })
    .then(response => {
      showModalCadastro.value = false;
      showNotification('positive', "Aluguel cadastrado com sucesso!");
      getRows();
    })
    .catch(error => {
      let errorMessage = 'Erro desconhecido ao cadastrar aluguel.';

      if (error.response) {
        if (error.response.status === 400) {

          errorMessage = Object.values(error.response.data).join(', ') || errorMessage;
        } else if (error.response.data.message) {

          errorMessage = error.response.data.message;
        }
      }

      console.error("Erro ao cadastrar aluguel:", error.response ? error.response.data : error.message);
      showNotification('negative', errorMessage);
    });
};



const confirmReturn = () => {
  if (!rowToReturn.value) {
    showNotification('negative', "Nenhuma linha selecionada para devolução.")
    return
  }

  const row = rowToReturn.value
  const updatedStatus = "ENTREGUE"

  if (!row.id) {
    showNotification('negative', "ID do livro não disponível.")
    return
  }

  if (row.status === updatedStatus) {
    showNotification('negative', "Este aluguel já foi devolvido.")
    return
  }

  api.put(`/rent/${row.id}`, { status: updatedStatus })
    .then(response => {
      const index = rows.value.findIndex(r => r.id === row.id)
      if (index !== -1) {
        rows.value[index].status = updatedStatus
      }
      showNotification('positive', "Status atualizado com sucesso!")
      showModalDevolucao.value = false
      getRows()
    }).catch(error => {
      handleError(error, "Erro ao atualizar status!")
    })
}

const showReturnModal = (row) => {
  if (row.status === "ENTREGUE" || row.status === "ENTREGUE_COM_ATRASO" || row.status === "NO_PRAZO") {
    showNotification('negative', "Este aluguel já foi devolvido.")
    return
  }
  rowToReturn.value = row
  showModalDevolucao.value = true
}

const cancelReturn = () => {
  showModalDevolucao.value = false
}

const showNotification = (type, message) => {
  $q.notify({
    type: type,
    message: message,
    timeout: 3000,
    position: 'top'
  })
}

const formatStatus = (status) => {
  return status
    .replace(/_/g, ' ')
    .replace(/\b\w/g, (l) => l.toUpperCase())
}

const onSearch = () => {
}

const getRows = (srch = '', status = statusFiltered.value) => {
  api.get('/rent', { params: { search: srch, page: page.value, status: status } })
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
};

const traduzirStatus = (status) => {
  switch (status) {
    case 'RENTED':
      return 'Alugado';

    case 'IN_TIME':
      return 'Devolvido no prazo';

    case 'LATE':
      return 'Atrasado';

    case 'DELIVERED_WITH_DELAY':
      return 'Devolvido fora prazo';
  }
};

const totalPages = computed(() => Math.ceil(rows.value.length / rowsPerPage));

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


const renterOptions = ref([]);
const allRenter = ref([]);
const bookOptions = ref([]);
const allbook = ref([]);

const loadRenter = (search = '') => {
  api.get('/renter', { params: { search: search } })
    .then(response => {
      allRenter.value = response.data;
      renterOptions.value = response.data;
    })
    .catch(error => {
      console.error('Erro ao carregar locatários:', error);
    });
};

const loadBook = (search = '') => {
  api.get('/book', { params: { search: search, page: page.value } })
    .then(response => {
      allbook.value = response.data.content;
      bookOptions.value = response.data.content;
    })
    .catch(error => {
      console.error('Erro ao carregar livros:', error);
    });
};


const filterPublisher = (val, update) => {
  if (val === '') {
    update(() => {
      renterOptions.value = allRenter.value;
    });
    return;
  }

  const needle = val.toLowerCase();
  update(() => {
    renterOptions.value = allRenter.value.filter(publisher =>
      publisher.name.toLowerCase().includes(needle)
    );
  });
};

const filterBook = (val, update) => {
  if (val === '') {
    update(() => {
      bookOptions.value = allbook.value;
    });
    return;
  }

  const needle = val.toLowerCase();
  update(() => {
    bookOptions.value = allbook.value.filter(renter =>
      renter.name.toLowerCase().includes(needle)
    );
  });
};

const userRole = ref('');

onMounted(() => {
  userRole.value = localStorage.getItem('role')
  getRows();
  getRenters();
});




// Teste do Aluguel

const rentAction = () => {
  if (!newRent.value.renterId || !newRent.value.bookId || !newRent.value.deadLine) {
    showNotification('negative', 'Todos os campos são obrigatórios!');
    return;
  }

  api.post('/rent/create', newRent.value, {
    headers: {
      Authorization: `Bearer ${localStorage.getItem('token')}`,
    },
  })
    .then(response => {
      showNotification('positive', 'Aluguel cadastrado com sucesso!');
      showModalCadastro.value = false;
      getRows();
    })
    .catch(error => {
      console.error('Erro ao cadastrar aluguel:', error.response?.data || error.message);
      showNotification('negative', error.response?.data?.message || 'Você não tem permissão para essa ação!');
    });
};


const idRenter = ref('')
const idBook = ref('')

const bookToRent = ref({
  renterId: idRenter,
  bookId: idBook,
  deadLine: 'Escolha uma data',
});


const renters = ref([]);

const getRenters = (srch = '') => {
  api.get('/renter', { params: { search: srch } })
    .then(response => {
      renters.value = response.data;
    })
    .catch(error => {
      console.log(error);
    });
};


const rentersFilter = (val, update, abort) => {
  if (val === '') {
    update(() => {
      getRenters();
    });
  } else {
    update(() => {
      const needle = val.toLowerCase();
      renters.value = renters.value.filter(renter =>
        renter.name.toLowerCase().includes(needle)
      );
    });
  }
};

const onItemClickRent = (renterItem) => {
  bookToRent.value.renterId = renterItem.id;
};


const books = ref([]);

const getBooks = (srch = '') => {
  api.get('/book', { params: { search: srch, page: page.value } })
    .then(response => {
      if (Array.isArray(response.data.content)) {
        books.value = response.data.content;
        console.log(response.data)
      } else {
        console.error('A resposta da API não é um array:', response.data);
        rows.value = [];
      }
    })
    .catch(error => {
      console.error("Erro ao obter dados:", error);
    });
};

const booksFilter = (val, update, abort) => {
  if (val === '') {
    update(() => {
      getBooks();
    });
  } else {
    update(() => {
      const needle = val.toLowerCase();
      books.value = books.value.filter(book =>
        book.name.toLowerCase().includes(needle)
      );
    });
  }
};

const onItemClickBook = (bookItem) => {
  bookToRent.value.bookId = bookItem.id;
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

.modal-card {
  width: 600px;
  padding: 10px;
  border-radius: 20px;
  box-shadow: 15px 13px 61px -17px rgba(0, 0, 0, 0.49);
}

.modal-card-confirmacao {
  width: 400px;
  border-radius: 10px;
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


.text-center {
  text-align: center;
}

.titulo-confirmacao {
  font-size: 1.2rem;
  margin-bottom: 16px;
}

.button-confirmacao {
  display: flex;
  justify-content: center;
}

.center-width {
  width: 100%;
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

@media (max-width: 700px) {
  .button-pesquisar {
    display: none;
  }
}
</style>
