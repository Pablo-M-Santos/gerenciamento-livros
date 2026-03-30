<template>
  <q-dialog
    :model-value="modelValue"
    persistent
    transition-show="scale"
    transition-hide="scale"
    @update:model-value="$emit('update:modelValue', $event)"
  >
    <q-card class="modal-card modal-animated">
      <q-linear-progress
        :value="1"
        :color="isEditMode ? 'secondary' : 'primary'"
        class="modal-header-bar"
      />
      <q-card-section class="modal-header-section">
        <div class="modal-title">
          <q-icon
            :name="isEditMode ? 'edit' : 'receipt_long'"
            size="24px"
            :color="isEditMode ? 'secondary' : 'primary'"
          />
          <span>{{
            isEditMode ? "Editar Aluguel" : "Cadastrar Novo Aluguel"
          }}</span>
        </div>
        <p class="modal-subtitle">
          {{
            isEditMode
              ? "Atualize as informacoes do aluguel"
              : "Preencha os dados abaixo para registrar um novo aluguel"
          }}
        </p>
      </q-card-section>

      <q-separator />

      <q-card-section class="modal-content">
        <q-form @submit="handleSubmit" class="modal-form">
          <q-select
            v-model="form.renterId"
            outlined
            dense
            rounded
            label="Selecione o locatario"
            :options="renterOptions"
            option-label="name"
            option-value="id"
            emit-value
            map-options
            use-input
            input-debounce="0"
            :itemid="
              isEditMode
                ? 'editarLocatarioAluguel'
                : 'cadastrarLocatarioAluguel'
            "
            :rules="[(val) => !!val || 'Locatario e obrigatorio']"
            class="form-input"
            @filter="(val, update) => emit('filter-renter', val, update)"
          />

          <q-select
            v-model="form.bookId"
            outlined
            dense
            rounded
            label="Selecione o livro"
            :options="bookOptions"
            option-label="name"
            option-value="id"
            emit-value
            map-options
            use-input
            input-debounce="0"
            :itemid="
              isEditMode ? 'editarLivroAluguel' : 'cadastrarLivroAluguel'
            "
            :rules="[(val) => !!val || 'Livro e obrigatorio']"
            class="form-input"
            @filter="(val, update) => emit('filter-book', val, update)"
          />

          <q-input
            v-model="form.deadLine"
            outlined
            dense
            rounded
            label="Prazo final"
            type="date"
            :min="today"
            :max="maxReturnDate"
            :itemid="isEditMode ? 'editarDataAluguel' : 'cadastrarDataAluguel'"
            :rules="[(val) => !!val || 'Prazo e obrigatorio']"
            class="form-input"
          />

          <q-separator class="q-my-md" />

          <div class="modal-actions">
            <q-btn
              flat
              no-caps
              label="Cancelar"
              @click="handleCancel"
              class="btn-cancel"
            />
            <q-btn
              unelevated
              no-caps
              type="submit"
              :label="isEditMode ? 'Salvar' : 'Cadastrar'"
              :itemid="isEditMode ? 'BtnEditarAluguel' : 'BtnCadastrarAluguel'"
              class="btn-submit"
            />
          </div>
        </q-form>
      </q-card-section>
    </q-card>
  </q-dialog>
</template>

<script setup>
import { ref, watch } from "vue";

const props = defineProps({
  modelValue: { type: Boolean, required: true },
  isEditMode: { type: Boolean, default: false },
  initialData: {
    type: Object,
    default: () => ({ id: null, renterId: null, bookId: null, deadLine: "" }),
  },
  renterOptions: { type: Array, default: () => [] },
  bookOptions: { type: Array, default: () => [] },
  today: { type: String, required: true },
  maxReturnDate: { type: String, required: true },
});

const emit = defineEmits([
  "update:modelValue",
  "submit",
  "filter-renter",
  "filter-book",
]);

const form = ref({ id: null, renterId: null, bookId: null, deadLine: "" });

const normalizeDate = (val) => {
  if (!val) return "";
  if (typeof val === "string" && val.length >= 10) return val.slice(0, 10);
  return "";
};

const syncForm = (data) => {
  form.value = {
    id: data?.id ?? null,
    renterId: data?.renterId ?? data?.renter?.id ?? null,
    bookId: data?.bookId ?? data?.book?.id ?? null,
    deadLine: normalizeDate(data?.deadLine),
  };
};

watch(
  () => props.modelValue,
  (newVal) => {
    if (newVal) syncForm(props.initialData);
  }
);

watch(
  () => props.initialData,
  (newData) => {
    if (newData) syncForm(newData);
  },
  { deep: true }
);

const handleSubmit = () => emit("submit", { ...form.value });

const handleCancel = () => {
  syncForm(props.initialData);
  emit("update:modelValue", false);
};
</script>

<style scoped>
.modal-card {
  width: min(92vw, 560px);
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

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 12px;
}

.btn-cancel {
  color: #6b6b64;
  font-weight: 600;
  transition: all 0.25s ease;
  border-radius: 8px;
  padding: 8px 20px;
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
</style>
