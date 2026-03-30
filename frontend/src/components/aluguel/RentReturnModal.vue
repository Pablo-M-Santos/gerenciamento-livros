<template>
  <q-dialog
    :model-value="modelValue"
    persistent
    transition-show="scale"
    transition-hide="scale"
    @update:model-value="$emit('update:modelValue', $event)"
  >
    <q-card class="confirm-card modal-animated">
      <q-linear-progress :value="1" color="accent" class="modal-header-bar" />
      <q-card-section class="modal-header-section">
        <div class="modal-title">
          <q-icon name="assignment_turned_in" size="24px" color="accent" />
          <span>Confirmar Devolucao</span>
        </div>
        <p class="modal-subtitle">Deseja finalizar este aluguel?</p>
      </q-card-section>

      <q-separator />

      <q-card-section class="modal-content">
        <div class="delete-warning">
          <q-icon name="check_circle_outline" size="48px" color="accent" />
          <p class="delete-message">
            Confirmar devolucao do livro para <strong>{{ targetName }}</strong
            >?
          </p>
        </div>
      </q-card-section>

      <q-separator />

      <q-card-actions align="right" class="modal-actions-bottom">
        <q-btn
          flat
          no-caps
          label="Cancelar"
          @click="$emit('update:modelValue', false)"
          class="btn-cancel"
        />
        <q-btn
          unelevated
          no-caps
          color="accent"
          label="Confirmar Devolucao"
          itemid="BtnEntregaAluguel"
          @click="$emit('confirm')"
          class="btn-confirm"
        />
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>

<script setup>
defineProps({
  modelValue: { type: Boolean, required: true },
  targetName: { type: String, required: true },
});

defineEmits(["update:modelValue", "confirm"]);
</script>

<style scoped>
.confirm-card {
  width: min(92vw, 460px);
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

.delete-warning {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  padding: 12px;
  text-align: center;
}

.delete-message {
  font-size: 1rem;
  color: #222;
  margin: 0;
  font-weight: 600;
}

.modal-actions-bottom {
  padding: 16px 24px;
  gap: 10px;
}

.btn-cancel {
  color: #6b6b64;
  font-weight: 600;
  border-radius: 8px;
  padding: 8px 20px;
}

.btn-confirm {
  font-weight: 600;
  border-radius: 8px;
  padding: 8px 24px;
}
</style>
