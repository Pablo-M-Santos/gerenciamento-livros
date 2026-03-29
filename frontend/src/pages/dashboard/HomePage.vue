<template>
  <div class="conteudo">
    <div class="graficos">
      <div class="grafico1">
        <BarChart />
      </div>
      <div class="grafico2">
        <DoughnutChart />
      </div>
    </div>
    <div class="topLivrosContainer">
      <MaisAlugados />
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import BarChart from 'components/BarChart.vue';
import DoughnutChart from 'components/DoughnutChart.vue';
import MaisAlugados from 'src/components/MaisAlugados.vue';

const router = useRouter();
const isAuthenticated = ref(false);

const checkAuthentication = () => {
  const token = localStorage.getItem('authToken');
  if (!token) {
    router.push('/login');
  } else {
    isAuthenticated.value = true;
  }
};

onMounted(() => {
  checkAuthentication();
});
</script>

<style scoped>
.conteudo {
  overflow-y: hidden;
}

.graficos {
  display: flex;
  justify-content: center;
  align-items: center;
  justify-content: space-around;
  margin-top: 5px;
}

@media (max-width: 1350px) {
  .graficos {
    flex-direction: column;
    gap: 40px;
  }
}

@media (max-width: 550px) {

  .grafico1,
  .grafico2 {
    width: 420px;
  }

  .topLivrosContainer {
    width: 420px;
  }
}

@media (max-width: 450px) {

  .grafico1,
  .grafico2 {
    width: 320px;
  }

  .topLivrosContainer {
    width: 320px;
  }
}

@media (max-width: 370px) {

  .grafico1,
  .grafico2 {
    width: 320px;
  }

  .topLivrosContainer {
    width: 320px;
  }
}
</style>
