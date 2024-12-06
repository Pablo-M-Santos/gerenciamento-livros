<template>
  <div class="grafico1">
    <div class="title">
      <span class="text-h6">Estatísticas de Empréstimos</span>
      <span class="flex">
        Meses:
        <q-input v-model="numberOfMonths" type="number" filled class="inputMonths" dense item-aligned min=1 />
      </span>
    </div>
    <canvas id="chartBarComponent"></canvas>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue';
import { Chart, registerables } from 'chart.js';
import { api } from 'src/boot/axios';

Chart.register(...registerables);

defineOptions({
  name: 'BarChar'
});

const rentsQtd = ref(0);
const late = ref(0);
const delivered = ref(0);
const delayed = ref(0);
const numberOfMonths = ref(1);
let chartInstance = null;

const getRents = async () => {
  try {
    const response = await api.get('/dashboard/rentsQuantity', { params: { numberOfMonths: numberOfMonths.value } });
    rentsQtd.value = response.data;
  } catch (error) {
    console.error("Erro ao obter dados:", error);
  }
};

const getRentsLate = async () => {
  try {
    const response = await api.get('/dashboard/rentsLateQuantity', { params: { numberOfMonths: numberOfMonths.value } });
    late.value = response.data;
  } catch (error) {
    console.error("Erro ao obter dados:", error);
  }
};

const getRentsDelivered = async () => {
  try {
    const response = await api.get('/dashboard/deliveredInTimeQuantity', { params: { numberOfMonths: numberOfMonths.value } });
    delivered.value = response.data;
  } catch (error) {
    console.error("Erro ao obter dados:", error);
  }
};

const getRentsDelayed = async () => {
  try {
    const response = await api.get('/dashboard/deliveredWithDelayQuantity', { params: { numberOfMonths: numberOfMonths.value } });
    delayed.value = response.data;
  } catch (error) {
    console.error("Erro ao obter dados:", error);
  }
};

const updateChart = () => {
  if (chartInstance) {
    chartInstance.data.datasets[0].data = [rentsQtd.value, late.value, delivered.value, delayed.value];
    chartInstance.update();
  }
};

onMounted(async () => {
  await getRents();
  await getRentsLate();
  await getRentsDelivered();
  await getRentsDelayed();
  updateChart();

  watch(numberOfMonths, async () => {
    await fetchDataAndUpdateChart();
  });

  const ctx = document.getElementById('chartBarComponent').getContext('2d');
  chartInstance = new Chart(ctx, {
    type: 'bar',
    data: {
      labels: ['Alugados', 'Atrasados', 'Devolvidos no prazo', 'Devolvidos fora do prazo'],
      datasets: [{
        data: [rentsQtd.value, late.value, delivered.value, delayed.value],
        backgroundColor: ['#509358', '#B22222', '#46769A', '#C65F15'],
        borderWidth: 0,
        borderRadius: 5
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      scales: {
        y: {
          beginAtZero: true
        }
      },
      plugins: {
        legend: {
          display: false,
          position: 'top',
        }
      }
    }
  });
});
</script>

<style scoped>
.grafico1 {
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
  padding: 20px;
}

#chartBarComponent {
  margin-bottom: 3rem;
}

.title {
  display: flex;
  align-items: center;
  justify-content: space-around;
  margin-top: 10px;
  font-weight: bold;
}

.inputMonths {
  display: flex;
  justify-content: center;
  width: 90px;
  border-radius: 15px;
}

.flex {
  display: flex;
  align-items: center;
  margin-left: 15px;
}

#relacoesLivrosChart {
  margin-bottom: 1rem;
}

@media (max-width: 370px) {
  .grafico1 {
    width: 320px;
  }
}
</style>
