<template>
  <q-dialog
    :model-value="modelValue"
    transition-show="scale"
    transition-hide="scale"
    @update:model-value="$emit('update:modelValue', $event)"
  >
    <q-card class="modal-card modal-animated">
      <q-linear-progress :value="1" color="info" class="modal-header-bar" />
      <q-card-section class="modal-header-section">
        <div class="modal-title">
          <q-icon name="menu_book" size="24px" color="info" />
          <span>Detalhes do Livro</span>
        </div>
        <p class="modal-subtitle">Informacoes completas do livro</p>
      </q-card-section>

      <q-separator />

      <q-card-section class="modal-content">
        <q-input
          outlined
          dense
          rounded
          readonly
          :model-value="bookData.name"
          label="Titulo"
          class="form-input"
        />
        <q-input
          outlined
          dense
          rounded
          readonly
          :model-value="bookData.author"
          label="Autor"
          class="form-input"
        />
        <q-input
          outlined
          dense
          rounded
          readonly
          :model-value="bookData.totalQuantity"
          label="Disponiveis"
          class="form-input"
        />
        <q-input
          outlined
          dense
          rounded
          readonly
          :model-value="bookData.totalInUse"
          label="Alugados"
          class="form-input"
        />
        <q-input
          outlined
          dense
          rounded
          readonly
          :model-value="bookData.launchDate"
          label="Lancamento"
          class="form-input"
        />
        <q-input
          outlined
          dense
          rounded
          readonly
          :model-value="bookData.publisher?.name || '-'"
          label="Editora"
          class="form-input"
        />
      </q-card-section>

      <q-separator />

      <q-card-actions align="right" class="modal-actions-bottom">
        <q-btn
          flat
          no-caps
          label="Fechar"
          itemid="BtnSobreLivro"
          @click="$emit('update:modelValue', false)"
          class="btn-cancel"
        />
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>

<script setup>
defineProps({
  modelValue: {
    type: Boolean,
    required: true,
  },
  bookData: {
    type: Object,
    required: true,
  },
});

defineEmits(["update:modelValue"]);
</script>

<style scoped>
.modal-card {
  width: min(92vw, 540px);
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

.modal-actions-bottom {
  padding: 16px 24px;
  gap: 10px;
}

.btn-cancel {
  color: #6b6b64;
  font-weight: 600;
  transition: all 0.25s ease;
  border-radius: 8px;
  padding: 8px 20px;
}

.btn-cancel:hover {
  background: rgba(0, 0, 0, 0.05);
  color: #222;
}
</style>
