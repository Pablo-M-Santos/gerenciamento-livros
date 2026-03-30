<template>
  <q-page class="renters-page">
    <section class="renters-header animate-header">
      <h1>Gerenciamento de Locatarios</h1>
      <p>Gerencie os locatarios da sua biblioteca digital</p>
    </section>

    <section class="stats-grid">
      <RenterStatsCard
        :card-index="1"
        icon="groups"
        label="Total de Locatarios"
        :value="summary.total"
        type="total"
      />
      <RenterStatsCard
        :card-index="2"
        icon="library_books"
        label="Com Alugueis"
        :value="summary.withRentals"
        type="with-rentals"
      />
      <RenterStatsCard
        :card-index="3"
        icon="person_outline"
        label="Sem Alugueis"
        :value="summary.withoutRentals"
        type="without-rentals"
      />
    </section>

    <RenterSearchBar
      :search-query="searchQuery"
      :can-add="userRole === 'ADMIN'"
      @update:search="searchQuery = $event"
      @search="handleSearch"
      @clear="clearSearch"
      @add="openRegisterDialog"
    />

    <RenterTable
      :rows="filteredRows"
      :loading="loading"
      :is-admin="userRole === 'ADMIN'"
      :renters-with-rentals="rentersWithRentals"
      @view="showDetails"
      @edit="editRow"
      @delete="askDelete"
    />

    <RenterFormModal
      v-model="showModalCadastro"
      :is-edit-mode="false"
      :initial-data="renterCreate"
      @submit="submitFormCadastro"
    />

    <RenterFormModal
      v-model="showModalEditar"
      :is-edit-mode="true"
      :initial-data="renterEdit"
      @submit="submitFormEditar"
    />

    <RenterDetailsModal
      v-model="showModalSobre"
      :renter-data="selectedRenter"
    />

    <RenterDeleteModal
      v-model="showModalExcluir"
      :target-name="deleteTarget?.name || ''"
      @confirm="confirmDelete"
    />
  </q-page>
</template>

<script setup>
import { computed, onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import { Notify } from "quasar";
import { api } from "src/boot/axios.js";

import RenterStatsCard from "src/components/locatarios/RenterStatsCard.vue";
import RenterSearchBar from "src/components/locatarios/RenterSearchBar.vue";
import RenterTable from "src/components/locatarios/RenterTable.vue";
import RenterFormModal from "src/components/locatarios/RenterFormModal.vue";
import RenterDetailsModal from "src/components/locatarios/RenterDetailsModal.vue";
import RenterDeleteModal from "src/components/locatarios/RenterDeleteModal.vue";

const router = useRouter();

const loading = ref(false);
const userRole = ref(localStorage.getItem("role") || "");
const searchQuery = ref("");

const rows = ref([]);
const rentersWithRentals = ref([]);

const showModalCadastro = ref(false);
const showModalEditar = ref(false);
const showModalSobre = ref(false);
const showModalExcluir = ref(false);

const deleteTarget = ref(null);

const renterCreate = ref({
  id: null,
  name: "",
  email: "",
  telephone: "",
  address: "",
  cpf: "",
});

const renterEdit = ref({
  id: null,
  name: "",
  email: "",
  telephone: "",
  address: "",
  cpf: "",
});

const selectedRenter = ref({
  id: null,
  name: "",
  email: "",
  telephone: "",
  address: "",
  cpf: "",
});

const notify = (type, message) => {
  Notify.create({ type, message, position: "top", timeout: 2200 });
};

const normalizeRenter = (row) => ({
  id: row?.id ?? null,
  name: row?.name ?? "-",
  email: row?.email ?? "-",
  telephone: row?.telephone ?? "-",
  address: row?.address ?? "-",
  cpf: row?.cpf ?? "",
});

const sanitizePayload = (formData) => {
  const payload = {
    name: String(formData.name || "").trim(),
    email: String(formData.email || "").trim(),
    telephone: String(formData.telephone || "").trim(),
    address: String(formData.address || "").trim(),
  };

  const cpf = String(formData.cpf || "").trim();
  if (cpf) payload.cpf = cpf;

  return payload;
};

const filteredRows = computed(() => {
  const query = searchQuery.value.trim().toLowerCase();
  if (!query) return rows.value;

  return rows.value.filter((row) => {
    return (
      String(row.name).toLowerCase().includes(query) ||
      String(row.email).toLowerCase().includes(query) ||
      String(row.telephone).toLowerCase().includes(query) ||
      String(row.address).toLowerCase().includes(query) ||
      String(row.cpf || "")
        .toLowerCase()
        .includes(query)
    );
  });
});

const summary = computed(() => {
  const rentersWithRentSet = new Set(rentersWithRentals.value);
  const total = rows.value.length;
  const withRentals = rows.value.filter((renter) =>
    rentersWithRentSet.has(renter.id)
  ).length;
  const withoutRentals = Math.max(total - withRentals, 0);

  return {
    total,
    withRentals,
    withoutRentals,
  };
});

const loadRenters = async (search = "") => {
  loading.value = true;
  try {
    const response = await api.get("/renter", {
      params: { search: search || undefined, page: 0 },
    });

    const data = response.data?.content || response.data || [];
    rows.value = Array.isArray(data) ? data.map(normalizeRenter) : [];
  } catch (error) {
    rows.value = [];
    notify("negative", "Erro ao carregar locatarios.");
  } finally {
    loading.value = false;
  }
};

const loadRentals = async () => {
  try {
    const response = await api.get("/rent", {
      params: { search: "", page: 0 },
    });

    const rentals = response.data?.content || [];
    rentersWithRentals.value = [
      ...new Set(
        rentals
          .map((rental) => rental?.renter?.id)
          .filter((id) => typeof id === "number")
      ),
    ];
  } catch (error) {
    rentersWithRentals.value = [];
  }
};

const openRegisterDialog = () => {
  renterCreate.value = {
    id: null,
    name: "",
    email: "",
    telephone: "",
    address: "",
    cpf: "",
  };
  showModalCadastro.value = true;
};

const submitFormCadastro = async (formData) => {
  if (
    !formData.name ||
    !formData.email ||
    !formData.telephone ||
    !formData.address
  ) {
    notify("negative", "Preencha os campos obrigatorios para cadastrar.");
    return;
  }

  try {
    await api.post("/renter", sanitizePayload(formData));
    notify("positive", "Locatario criado com sucesso.");
    showModalCadastro.value = false;
    await loadRenters(searchQuery.value);
  } catch (error) {
    notify(
      "negative",
      error.response?.data?.message || "Erro ao criar locatario."
    );
  }
};

const editRow = async (row) => {
  try {
    const response = await api.get(`/renter/${row.id}`);
    renterEdit.value = normalizeRenter(response.data || row);
    showModalEditar.value = true;
  } catch (error) {
    notify("negative", "Erro ao carregar dados para edicao.");
  }
};

const submitFormEditar = async (formData) => {
  if (!renterEdit.value.id) {
    notify("negative", "Locatario nao selecionado.");
    return;
  }

  if (
    !formData.name ||
    !formData.email ||
    !formData.telephone ||
    !formData.address
  ) {
    notify("negative", "Preencha os campos obrigatorios para atualizar.");
    return;
  }

  try {
    await api.put(`/renter/${renterEdit.value.id}`, {
      id: renterEdit.value.id,
      ...sanitizePayload(formData),
    });

    notify("positive", "Locatario atualizado com sucesso.");
    showModalEditar.value = false;
    await loadRenters(searchQuery.value);
  } catch (error) {
    notify(
      "negative",
      error.response?.data?.message || "Erro ao atualizar locatario."
    );
  }
};

const showDetails = async (row) => {
  try {
    const response = await api.get(`/renter/${row.id}`);
    selectedRenter.value = normalizeRenter(response.data || row);
  } catch (error) {
    selectedRenter.value = normalizeRenter(row);
  } finally {
    showModalSobre.value = true;
  }
};

const askDelete = (row) => {
  deleteTarget.value = row;
  showModalExcluir.value = true;
};

const confirmDelete = async () => {
  if (!deleteTarget.value?.id) return;

  try {
    await api.delete(`/renter/${deleteTarget.value.id}`);
    notify("positive", "Locatario excluido com sucesso.");
    showModalExcluir.value = false;
    deleteTarget.value = null;
    await Promise.all([loadRenters(searchQuery.value), loadRentals()]);
  } catch (error) {
    notify(
      "negative",
      error.response?.data?.message || "Erro ao excluir locatario."
    );
  }
};

const handleSearch = () => {
  loadRenters(searchQuery.value);
};

const clearSearch = () => {
  searchQuery.value = "";
  loadRenters();
};

onMounted(async () => {
  const token = localStorage.getItem("authToken");

  if (!token) {
    router.push("/login");
    return;
  }

  userRole.value = localStorage.getItem("role") || "";
  await Promise.all([loadRenters(), loadRentals()]);
});
</script>

<style scoped>
.renters-page {
  padding: 34px 60px;
  background: #f5f5f3;
  min-height: 100vh;
  margin: 0 auto;
}

.renters-header {
  animation: slideInDown 0.35s ease-out forwards;
  opacity: 0;
}

.renters-header h1 {
  margin: 0;
  font-size: 2rem;
  color: #222;
  font-weight: 700;
}

.renters-header p {
  color: #6b6b64;
  font-size: 1rem;
}

.stats-grid {
  margin-top: 24px;
  display: grid;
  grid-template-columns: repeat(3, minmax(180px, 1fr));
  gap: 16px;
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

@media (max-width: 980px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 600px) {
  .renters-page {
    padding: 18px;
  }

  .renters-header h1 {
    font-size: 1.5rem;
  }
}
</style>
