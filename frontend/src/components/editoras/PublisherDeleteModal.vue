<template>
  <q-dialog
    :model-value="modelValue"
    persistent
    transition-show="scale"
    transition-hide="scale"
    @update:model-value="$emit('update:modelValue', $event)"
  >
    <q-card class="confirm-card modal-animated">
      <q-linear-progress :value="1" color="negative" class="modal-header-bar" />
      <q-card-section class="modal-header-section">
        <div class="modal-title">
          <q-icon name="warning" size="24px" color="negative" />
          <span>Confirmar Exclusão</span>
        </div>
        <p class="modal-subtitle">Esta ação não pode ser desfeita</p>
      </q-card-section>

      <q-separator />

      <q-card-section class="modal-content">
        <div class="delete-warning">
          <q-icon name="error_outline" size="48px" color="negative" />
          <p class="delete-message">
            Tem certeza que deseja excluir <strong>{{ targetName }}</strong
            >?
          </p>
          <p class="delete-note">
            A editora será removida permanentemente do sistema.
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
          color="negative"
          label="Excluir Editora"
          @click="$emit('confirm')"
          class="btn-danger"
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
  targetName: {
    type: String,
    required: true,
  },
});

defineEmits(["update:modelValue", "confirm"]);
</script>

<style scoped>
.confirm-card {
  width: min(92vw, 420px);
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

.delete-message strong {
  color: #dc2626;
  font-weight: 700;
}

.delete-note {
  font-size: 0.85rem;
  color: #999;
  margin: 0;
  line-height: 1.5;
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

.btn-danger {
  background: linear-gradient(135deg, #dc2626 0%, #b91c1c 100%);
  font-weight: 600;
  border-radius: 8px;
  padding: 8px 24px;
  transition: all 0.25s ease;
  box-shadow: 0 4px 12px rgba(220, 38, 38, 0.3);
}

.btn-danger:hover {
  box-shadow: 0 6px 16px rgba(220, 38, 38, 0.4);
  transform: translateY(-1px);
}
</style>
