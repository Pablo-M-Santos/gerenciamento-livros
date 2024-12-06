<template>
  <div class="chart-container">
    <div class="title">
      <span class="text-h6">Livros mais alugados</span>
      <span class="flex">
        Meses:
        <q-input v-model="numberOfMonths" type="number" filled class="inputMonths" dense item-aligned min=1 />
      </span>
    </div>
    <canvas id="mostRentedBooksChart" style="width: 100px; height: 100px;"></canvas>
  </div>
</template>

<script setup>
import { useQuasar } from 'quasar';
import { ref, onMounted, watch } from 'vue';
import { Chart, registerables } from 'chart.js';
import { api } from 'src/boot/axios';

const $q = useQuasar();

const showNotification = (type, msg) => {
  $q.notify({
    type: type,
    message: msg,
    position: 'bottom-right',
    timeout: 3000
  });
};

Chart.register(...registerables);

const numberOfMonths = ref(1);
const mostRented1 = ref({});
const mostRented2 = ref({});
const mostRented3 = ref({});

let chartInstance = null;

defineOptions({
  name: 'chartPieComponent'
});

const getRents = async () => {
  try {
    const response = await api.get('/dashboard/bookMoreRented', { params: { numberOfMonths: numberOfMonths.value } });
    mostRented1.value = response.data[0] || { name: '', totalRents: 0 };
    mostRented2.value = response.data[1] || { name: '', totalRents: 0 };
    mostRented3.value = response.data[2] || { name: '', totalRents: 0 };
  } catch (error) {
    console.error("Erro ao obter dados:", error);
  }
};

const updateChart = () => {
  if (chartInstance) {
    chartInstance.data.labels = [mostRented1.value.name, mostRented2.value.name, mostRented3.value.name];
    chartInstance.data.datasets[0].data = [mostRented1.value.totalRents, mostRented2.value.totalRents, mostRented3.value.totalRents];
    chartInstance.update();
  }
};

const fetchDataAndUpdateChart = async () => {
  await getRents();
  updateChart();
};

watch(() => numberOfMonths.value, async () => {
  await fetchDataAndUpdateChart();
});

onMounted(async () => {
  await fetchDataAndUpdateChart();

  const ctx2 = document.getElementById('mostRentedBooksChart').getContext('2d');
  chartInstance = new Chart(ctx2, {
    type: 'pie',
    data: {
      labels: [mostRented1.value.name, mostRented2.value.name, mostRented3.value.name],
      datasets: [{
        label: 'Livros mais alugados',
        data: [mostRented1.value.totalRents, mostRented2.value.totalRents, mostRented3.value.totalRents],
        backgroundColor: ['#509358', '#B22222', '#46769A'],
        borderWidth: 0
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
    }
  });
});
</script>

<style scoped>
#mostRentedBooksChart {
  margin-bottom: 1rem;
}

.inputMonths {
  display: flex;
  justify-content: center;
  width: 90px;
  border-radius: 15px;
}

.chart-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 505px;
  height: 400px;
  box-shadow: 3px 4px 10px 0px rgba(0, 0, 0, 0.25);
  border-radius: 20px;
  margin: 10px;
  background-color: white;
  padding: 40px;
}

.flex {
  display: flex;
  align-items: center;
  margin-left: 15px;
}

.title {
  display: flex;
  align-items: center;
  justify-content: space-around;
  margin-top: 10px;
  font-weight: bold;
}

@media (max-width: 450px) {
  .chart-container {
    width: 210px;
  }
}

@media (max-width: 550px) {
  .chart-container {
    width: 420px;
  }
}
</style>
