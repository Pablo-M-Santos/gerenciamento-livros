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
            :name="isEditMode ? 'edit' : 'business'"
            size="24px"
            :color="isEditMode ? 'secondary' : 'primary'"
          />
          <span>{{
            isEditMode ? "Editar Editora" : "Cadastrar Nova Editora"
          }}</span>
        </div>
        <p class="modal-subtitle">
          {{
            isEditMode
              ? "Atualize as informações da editora"
              : "Preencha os dados abaixo para criar uma nova editora"
          }}
        </p>
      </q-card-section>

      <q-separator />

      <q-card-section class="modal-content">
        <q-form @submit="handleSubmit" class="modal-form">
          <q-input
            v-model="form.name"
            outlined
            dense
            rounded
            label="Nome da Editora"
            :itemid="isEditMode ? 'editarNomeEditora' : 'cadastroNomeEditora'"
            :rules="[(val) => !!val || 'Nome é obrigatório']"
            class="form-input"
            prefix-icon="business"
          />

          <q-input
            v-model="form.email"
            outlined
            dense
            rounded
            label="E-mail"
            type="email"
            :itemid="
              isEditMode ? 'editarEmailEditora' : 'cadastrarEmailEditora'
            "
            :rules="[
              (val) => !!val || 'E-mail é obrigatório',
              (val) => /.+@.+\..+/.test(val) || 'E-mail inválido',
            ]"
            class="form-input"
            prefix-icon="email"
          />

          <q-input
            v-model="form.telephone"
            outlined
            dense
            rounded
            label="Telefone"
            type="tel"
            mask="(##) #####-####"
            :itemid="
              isEditMode ? 'editarTelefoneEditora' : 'cadastroTelefoneEditora'
            "
            :rules="[
              (val) => !!val || 'Telefone é obrigatório',
              (val) =>
                /^\(\d{2}\) \d{5}-\d{4}$/.test(val) || 'Telefone inválido',
            ]"
            class="form-input"
            prefix-icon="phone"
          />

          <q-input
            v-model="form.site"
            outlined
            dense
            rounded
            label="Site"
            :itemid="isEditMode ? 'editarSiteEditora' : 'cadastroSiteEditora'"
            :rules="[
              (val) =>
                !val ||
                /^https:\/\/.+/.test(val) ||
                'Site deve começar com https://',
            ]"
            class="form-input"
            prefix-icon="language"
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
              :label="isEditMode ? 'Atualizar' : 'Cadastrar'"
              class="btn-submit"
              :itemid="isEditMode ? 'BtnEditarEditora' : 'BtnCadastrarEditora'"
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
  modelValue: {
    type: Boolean,
    required: true,
  },
  isEditMode: {
    type: Boolean,
    default: false,
  },
  initialData: {
    type: Object,
    default: () => ({
      id: null,
      name: "",
      email: "",
      telephone: "",
      site: "",
    }),
  },
});

const emit = defineEmits(["update:modelValue", "submit"]);

const form = ref({
  id: null,
  name: "",
  email: "",
  telephone: "",
  site: "",
});

watch(
  () => props.modelValue,
  (newVal) => {
    if (newVal) {
      form.value = { ...props.initialData };
    }
  }
);

watch(
  () => props.initialData,
  (newData) => {
    if (newData) {
      form.value = { ...newData };
    }
  },
  { deep: true }
);

const handleSubmit = () => {
  emit("submit", { ...form.value });
};

const handleCancel = () => {
  form.value = { ...props.initialData };
  emit("update:modelValue", false);
};
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

.btn-cancel:hover {
  background: rgba(0, 0, 0, 0.05);
  color: #222;
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
