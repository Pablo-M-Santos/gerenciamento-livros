<template>
  <q-page class="dashboard-page">
    <section class="dashboard-header animate-header">
      <div>
        <h1>Painel da Biblioteca</h1>
        <p>Visao operacional dos principais indicadores da locadora</p>
      </div>

      <div class="header-actions">
        <q-select
          v-model="monthsWindow"
          :options="monthsOptions"
          option-label="label"
          option-value="value"
          emit-value
          map-options
          dense
          outlined
          color="positive"
          label="Periodo"
          class="period-select"
          @update:model-value="loadOverview"
        />
      </div>
    </section>

    <section class="kpi-grid animate-section animate-kpis">
      <q-card v-for="card in kpiCards" :key="card.label" class="kpi-card">
        <q-card-section>
          <div class="kpi-label">{{ card.label }}</div>
          <div class="kpi-value">{{ card.value }}</div>
          <div class="kpi-helper">{{ card.helper }}</div>
        </q-card-section>
      </q-card>
    </section>

    <section class="insights-grid animate-section animate-insights">
      <q-card class="panel-card">
        <q-card-section class="panel-title">Status dos Alugueis</q-card-section>
        <q-card-section>
          <div v-for="item in statusItems" :key="item.label" class="status-row">
            <div class="status-head">
              <span>{{ item.label }}</span>
              <span>{{ item.value }}</span>
            </div>
            <q-linear-progress
              :value="statusProgress(item.value)"
              color="positive"
              track-color="grey-3"
              rounded
              size="10px"
            />
          </div>
        </q-card-section>
      </q-card>

      <q-card class="panel-card">
        <q-card-section class="panel-title">Evolucao Mensal</q-card-section>
        <q-card-section>
          <div class="trend-chart">
            <div
              v-for="point in monthlyRents"
              :key="point.month"
              class="trend-col"
            >
              <div class="trend-value">{{ point.total }}</div>
              <div
                class="trend-bar"
                :style="{ height: `${trendHeight(point.total)}%` }"
              />
              <div class="trend-label">{{ formatMonth(point.month) }}</div>
            </div>
          </div>
        </q-card-section>
      </q-card>
    </section>

    <section class="ranking-grid animate-section animate-ranking">
      <q-card class="panel-card">
        <q-card-section class="panel-title">Top Livros</q-card-section>
        <q-card-section>
          <div v-if="topBooks.length" class="rank-list">
            <div
              v-for="(book, index) in topBooks"
              :key="book.name"
              class="rank-item"
            >
              <div class="rank-index">{{ index + 1 }}</div>
              <div class="rank-name">{{ book.name }}</div>
              <div class="rank-count">{{ book.totalRents }} alugueis</div>
            </div>
          </div>
          <div v-else class="empty-state">
            Sem dados no periodo selecionado.
          </div>
        </q-card-section>
      </q-card>

      <q-card class="panel-card">
        <q-card-section class="panel-title">Top Locatarios</q-card-section>
        <q-card-section>
          <q-table
            :rows="topRenters"
            :columns="renterColumns"
            row-key="name"
            flat
            dense
            hide-bottom
            class="dashboard-table"
          />
        </q-card-section>
      </q-card>
    </section>

    <q-inner-loading :showing="loading">
      <q-spinner color="positive" size="36px" />
    </q-inner-loading>
  </q-page>
</template>

<script setup>
import { computed, onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import { Notify } from "quasar";
import { api } from "src/boot/axios";

const router = useRouter();

const loading = ref(false);
const monthsWindow = ref(6);
const monthsOptions = [
  { label: "1 mes", value: 1 },
  { label: "3 meses", value: 3 },
  { label: "6 meses", value: 6 },
  { label: "12 meses", value: 12 },
];

const overview = ref({
  totalBooks: 0,
  totalRenters: 0,
  totalPublishers: 0,
  totalUsers: 0,
  activeRents: 0,
  lateRents: 0,
  finishedRents: 0,
  onTimeRate: 0,
  lateRate: 0,
  statusBreakdown: [],
  monthlyRents: [],
  topBooks: [],
  topRenters: [],
});

const renterColumns = [
  { name: "name", label: "Nome", align: "left", field: "name" },
  {
    name: "rentsQuantity",
    label: "Total",
    align: "right",
    field: "rentsQuantity",
  },
  {
    name: "rentsActive",
    label: "Ativos",
    align: "right",
    field: "rentsActive",
  },
];

const kpiCards = computed(() => {
  const data = overview.value;
  return [
    {
      label: "Alugueis Ativos",
      value: data.activeRents,
      helper: "Em andamento",
    },
    {
      label: "Alugueis Atrasados",
      value: data.lateRents,
      helper: `${Number(data.lateRate || 0).toFixed(1)}% da carteira ativa`,
    },
    {
      label: "Finalizados",
      value: data.finishedRents,
      helper: "Historico de devolucoes",
    },
    {
      label: "Livros Ativos",
      value: data.totalBooks,
      helper: "Catalogo disponivel",
    },
    {
      label: "Locatarios Ativos",
      value: data.totalRenters,
      helper: "Clientes habilitados",
    },
    {
      label: "Taxa no Prazo",
      value: `${Number(data.onTimeRate || 0).toFixed(1)}%`,
      helper: "Devolucoes dentro do prazo",
    },
  ];
});

const statusItems = computed(() => overview.value.statusBreakdown || []);
const monthlyRents = computed(() => overview.value.monthlyRents || []);
const topBooks = computed(() => overview.value.topBooks || []);
const topRenters = computed(() => overview.value.topRenters || []);

const statusTotal = computed(() => {
  return statusItems.value.reduce(
    (acc, item) => acc + Number(item.value || 0),
    0
  );
});

const maxTrendValue = computed(() => {
  const values = monthlyRents.value.map((item) => Number(item.total || 0));
  return values.length ? Math.max(...values, 1) : 1;
});

const statusProgress = (value) => {
  if (!statusTotal.value) return 0;
  return Number(value || 0) / statusTotal.value;
};

const trendHeight = (value) => {
  if (!maxTrendValue.value) return 8;
  const ratio = (Number(value || 0) / maxTrendValue.value) * 100;
  return Math.max(ratio, 8);
};

const formatMonth = (yearMonth) => {
  if (!yearMonth || typeof yearMonth !== "string") return "-";
  const [year, month] = yearMonth.split("-");
  if (!year || !month) return yearMonth;

  const map = {
    "01": "Jan",
    "02": "Fev",
    "03": "Mar",
    "04": "Abr",
    "05": "Mai",
    "06": "Jun",
    "07": "Jul",
    "08": "Ago",
    "09": "Set",
    10: "Out",
    11: "Nov",
    12: "Dez",
  };

  return `${map[month] || month}/${String(year).slice(2)}`;
};

const loadOverview = async () => {
  loading.value = true;
  try {
    const response = await api.get("/dashboard/overview", {
      params: { numberOfMonths: monthsWindow.value },
    });

    overview.value = {
      ...overview.value,
      ...(response.data || {}),
    };
  } catch (error) {
    Notify.create({
      type: "negative",
      message: "Erro ao carregar dashboard.",
      position: "top",
      timeout: 2200,
    });
  } finally {
    loading.value = false;
  }
};

onMounted(async () => {
  const token = localStorage.getItem("authToken");
  if (!token) {
    router.push("/login");
    return;
  }

  await loadOverview();
});
</script>

<style scoped>
.dashboard-page {
  padding: 34px 60px;
  background: #f5f5f3;
  min-height: 100vh;
}

.dashboard-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 20px;
  margin-bottom: 18px;
  animation: slideInDown 0.35s ease-out forwards;
  opacity: 0;
}

.dashboard-header h1 {
  margin: 0;
  font-size: 2rem;
  color: #222;
  font-weight: 700;
}

.dashboard-header p {
  margin: 6px 0 0;
  color: #6b6b64;
  font-size: 1rem;
}

.period-select {
  min-width: 170px;
  background: #fff;
  border-radius: 10px;
}

.kpi-grid {
  display: grid;
  grid-template-columns: repeat(6, minmax(160px, 1fr));
  gap: 14px;
}

.kpi-card {
  border: 1px solid #e7e6e2;
  border-radius: 14px;
  background: linear-gradient(145deg, #ffffff 0%, #f7f7f5 100%);
  box-shadow: 0 3px 14px rgba(16, 20, 16, 0.06);
  opacity: 0;
  transform: translateY(14px) scale(0.99);
  animation: cardReveal 0.42s ease-out forwards;
}

.kpi-card:nth-child(1) {
  animation-delay: 0.05s;
}

.kpi-card:nth-child(2) {
  animation-delay: 0.1s;
}

.kpi-card:nth-child(3) {
  animation-delay: 0.15s;
}

.kpi-card:nth-child(4) {
  animation-delay: 0.2s;
}

.kpi-card:nth-child(5) {
  animation-delay: 0.25s;
}

.kpi-card:nth-child(6) {
  animation-delay: 0.3s;
}

.kpi-label {
  font-size: 0.82rem;
  font-weight: 600;
  color: #6c6b63;
  text-transform: uppercase;
  letter-spacing: 0.04em;
}

.kpi-value {
  margin-top: 8px;
  font-size: 1.9rem;
  font-weight: 700;
  color: #1f1f1f;
  line-height: 1.1;
}

.kpi-helper {
  margin-top: 6px;
  font-size: 0.84rem;
  color: #808077;
}

.insights-grid {
  margin-top: 18px;
  display: grid;
  grid-template-columns: 1.1fr 1fr;
  gap: 16px;
  align-items: stretch;
}

.ranking-grid {
  margin-top: 16px;
  display: grid;
  grid-template-columns: 1.1fr 1fr;
  gap: 16px;
  align-items: stretch;
}

.panel-card {
  border: 1px solid #e7e6e2;
  border-radius: 14px;
  background: #fff;
  box-shadow: 0 3px 14px rgba(16, 20, 16, 0.05);
  height: 100%;
}

.animate-section {
  opacity: 0;
  transform: translateY(16px);
  animation: sectionReveal 0.45s ease-out forwards;
}

.animate-kpis {
  animation-delay: 0.08s;
}

.animate-insights {
  animation-delay: 0.18s;
}

.animate-ranking {
  animation-delay: 0.28s;
}

.panel-title {
  font-weight: 700;
  color: #2a2a24;
  font-size: 1rem;
}

.status-row {
  margin-bottom: 16px;
}

.status-row:last-child {
  margin-bottom: 0;
}

.status-head {
  display: flex;
  justify-content: space-between;
  margin-bottom: 6px;
  color: #4f4e47;
  font-weight: 600;
}

.trend-chart {
  min-height: 190px;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(52px, 1fr));
  gap: 12px;
  align-items: end;
}

.trend-col {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 7px;
}

.trend-value {
  font-size: 0.78rem;
  color: #66655e;
  font-weight: 600;
}

.trend-bar {
  width: 100%;
  border-radius: 10px;
  background: linear-gradient(180deg, #62a86a 0%, #1f722c 100%);
  min-height: 10px;
  transition: height 0.25s ease;
}

.trend-label {
  font-size: 0.74rem;
  color: #6b6b64;
  font-weight: 600;
}

.rank-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.rank-item {
  display: grid;
  grid-template-columns: 28px 1fr auto;
  gap: 10px;
  align-items: center;
  padding: 10px;
  border-radius: 10px;
  background: #f7f8f6;
  border: 1px solid #ecece9;
}

.rank-index {
  width: 28px;
  height: 28px;
  display: flex;
  justify-content: center;
  align-items: center;
  border-radius: 50%;
  font-size: 0.82rem;
  font-weight: 700;
  color: #fff;
  background: #1f722c;
}

.rank-name {
  font-weight: 600;
  color: #2b2b24;
}

.rank-count {
  font-size: 0.84rem;
  color: #5e5d56;
  font-weight: 600;
}

.empty-state {
  color: #75746d;
  font-size: 0.92rem;
}

.dashboard-table :deep(th) {
  font-weight: 700;
  color: #4e4d45;
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

@keyframes sectionReveal {
  from {
    opacity: 0;
    transform: translateY(16px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes cardReveal {
  from {
    opacity: 0;
    transform: translateY(14px) scale(0.99);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

@media (max-width: 1280px) {
  .kpi-grid {
    grid-template-columns: repeat(3, minmax(170px, 1fr));
  }
}

@media (max-width: 980px) {
  .dashboard-page {
    padding: 20px;
  }

  .dashboard-header {
    flex-direction: column;
    align-items: stretch;
  }

  .insights-grid,
  .ranking-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 720px) {
  .kpi-grid {
    grid-template-columns: 1fr;
  }

  .dashboard-header h1 {
    font-size: 1.55rem;
  }
}
</style>
