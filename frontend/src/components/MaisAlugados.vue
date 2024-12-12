<template>
  <div class="mais-alugados">
    <div>
      <div class="dados">
        <q-table :rows="rows" :columns="columns" row-key="id" hide-bottom>
          <template v-slot:header="props">
            <q-th v-bind="props">
              Nome
              <q-icon name="keyboard_arrow_up" @click="sortRowsAscByName" class="cursor-pointer" size="20px" />
              <q-icon name="keyboard_arrow_down" @click="sortRowsDescByName" class="cursor-pointer" size="20px" />
            </q-th>
            <q-th v-bind="props">
              Total de Empréstimos
              <q-icon name="keyboard_arrow_up" @click="sortRowsAscByRentsQuantity" class="cursor-pointer" size="20px" />
              <q-icon name="keyboard_arrow_down" @click="sortRowsDescByRentsQuantity" class="cursor-pointer" size="20px" />
            </q-th>
            <q-th v-bind="props">
              Aluguéis Ativos
              <q-icon name="keyboard_arrow_up" @click="sortRowsAscByRentsActive" class="cursor-pointer" size="20px" />
              <q-icon name="keyboard_arrow_down" @click="sortRowsDescByRentsActive" class="cursor-pointer" size="20px" />
            </q-th>
          </template>
        </q-table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { api } from 'src/boot/axios';

const columns = [
  { name: 'name', label: 'Nome', align: 'center', field: row => row.name, format: val => `${val}` },
  { name: 'rentsQuantity', label: 'Total de Empréstimos', align: 'center', field: 'rentsQuantity' },
  { name: 'rentsActive', label: 'Aluguéis Ativos', align: 'center', field: 'rentsActive' }
];

const rows = ref([]);
const search = ref('');
onMounted(() => {
  getRows();
});

const page = ref(0)

const getRows = (search = '') => {
  api.get('/dashboard/rentsPerRenter', { params: { search: search, page: page.value } })
    .then(response => {
      console.log('Resposta da API:', response.data); // Adicione esta linha para verificar a resposta da API
      if (Array.isArray(response.data.content)) {
        const sortedData = response.data.content.sort((a, b) => {
          return b.rentsQuantity - a.rentsQuantity; // Ordena por empréstimos
        });
        rows.value = sortedData.slice(0, 3); // Limita aos 3 primeiros
      } else {
        console.error('A resposta da API não é um array:', response.data);
        rows.value = [];
      }
    })
    .catch(error => {
      console.error("Erro ao obter dados:", error);
    });
};


const sortRowsAscByName = () => {
  rows.value.sort((a, b) => a.name.localeCompare(b.name));
};

const sortRowsDescByName = () => {
  rows.value.sort((a, b) => b.name.localeCompare(a.name));
};

const sortRowsAscByRentsQuantity = () => {
  rows.value.sort((a, b) => a.rentsQuantity - b.rentsQuantity);
};

const sortRowsDescByRentsQuantity = () => {
  rows.value.sort((a, b) => b.rentsQuantity - a.rentsQuantity);
};

const sortRowsAscByRentsActive = () => {
  rows.value.sort((a, b) => a.rentsActive - b.rentsActive);
};

const sortRowsDescByRentsActive = () => {
  rows.value.sort((a, b) => b.rentsActive - a.rentsActive);
};
</script>

<style scoped>
.mais-alugados {
  width: 100%;
  max-width: 1300px;
  margin: 0 auto;
  background-color: white;
  padding: 20px;
  max-height: 400px;
}

.dados {
  box-shadow: 3px 4px 10px 0px rgba(0, 0, 0, 0.25);
  margin-top: 20px;
}

.header {
  display: flex;
  align-items: center;
  justify-content: center;
}

.title {
  font-size: 18px;
  font-weight: bold;
}

.q-table {
  width: 100%;
}

@media (max-width: 800px) {
  .mais-alugados {
    width: 100%;
  }
}
</style>
