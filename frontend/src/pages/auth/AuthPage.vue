<template>
  <q-page class="auth-page">
    <div class="auth-shell">
      <section class="brand-side">
        <div class="brand-layer" />
        <div class="brand-content">
          <div class="book-badge">
            <q-icon name="menu_book" size="24px" />
          </div>

          <h1>Sua biblioteca digital, organizada com elegancia</h1>
          <p>
            Gerencie seu acervo de livros com uma experiencia tranquila e
            intuitiva. Simplifique o aluguel, acompanhe devolucoes e encante
            seus leitores.
          </p>

          <div class="stats">
            <div class="stat-item">
              <strong>2.500+</strong>
              <span>Livros gerenciados</span>
            </div>
            <div class="divider" />
            <div class="stat-item">
              <strong>98%</strong>
              <span>Satisfacao</span>
            </div>
          </div>
        </div>
      </section>

      <section class="form-side">
        <div class="form-wrap">
          <header class="form-header">
            <h2>{{ isRegister ? "Crie sua conta" : "Bem-vindo de volta" }}</h2>
            <p>
              {{
                isRegister
                  ? "Comece a organizar sua biblioteca hoje"
                  : "Entre para continuar gerenciando seu acervo"
              }}
            </p>
          </header>

          <div
            class="auth-switch"
            role="tablist"
            aria-label="Alternar autenticacao"
          >
            <button
              type="button"
              class="switch-btn"
              :class="{ active: !isRegister }"
              @click="goLogin"
            >
              Entrar
            </button>
            <button
              type="button"
              class="switch-btn"
              :class="{ active: isRegister }"
              @click="goRegister"
            >
              Cadastrar
            </button>
          </div>

          <transition name="form-swap" mode="out-in">
            <q-form
              :key="currentMode"
              class="auth-form"
              @submit="isRegister ? onRegisterSubmit() : onLoginSubmit()"
            >
              <template v-if="isRegister">
                <div class="field-label">Nome completo</div>
                <q-input
                  v-model="registerForm.name"
                  outlined
                  dense
                  rounded
                  placeholder="Joao Silva"
                  :rules="[(val) => !!val || 'Nome e obrigatorio']"
                >
                  <template #prepend>
                    <q-icon name="person_outline" />
                  </template>
                </q-input>
              </template>

              <div class="field-label">E-mail</div>
              <q-input
                v-model="activeEmail"
                outlined
                dense
                rounded
                placeholder="seu@email.com"
                type="email"
                :rules="[
                  (val) => !!val || 'E-mail e obrigatorio',
                  (val) => /.+@.+\..+/.test(val) || 'E-mail invalido',
                ]"
              >
                <template #prepend>
                  <q-icon name="mail_outline" />
                </template>
              </q-input>

              <div class="field-row">
                <div class="field-label">Senha</div>
                <router-link
                  v-if="!isRegister"
                  class="forgot-link"
                  :to="{ name: 'recuperarSenha' }"
                >
                  Esqueceu a senha?
                </router-link>
              </div>

              <q-input
                v-model="activePassword"
                outlined
                dense
                rounded
                :type="showPassword ? 'text' : 'password'"
                placeholder="********"
                :rules="[
                  (val) => !!val || 'Senha e obrigatoria',
                  (val) =>
                    val.length >= 8 ||
                    'A senha deve ter pelo menos 8 caracteres',
                ]"
              >
                <template #prepend>
                  <q-icon name="lock_outline" />
                </template>
                <template #append>
                  <q-icon
                    :name="showPassword ? 'visibility_off' : 'visibility'"
                    class="cursor-pointer"
                    @click="showPassword = !showPassword"
                  />
                </template>
              </q-input>

              <q-checkbox
                v-if="!isRegister"
                v-model="rememberMe"
                dense
                label="Lembrar de mim"
                class="remember-checkbox"
              />

              <q-btn
                class="submit-btn"
                no-caps
                unelevated
                type="submit"
                :label="isRegister ? 'Criar conta' : 'Entrar'"
              />

              <div class="bottom-text">
                <template v-if="isRegister">
                  Ja tem uma conta?
                  <button type="button" class="plain-link" @click="goLogin">
                    Entre aqui
                  </button>
                </template>
                <template v-else>
                  Nao tem uma conta?
                  <button type="button" class="plain-link" @click="goRegister">
                    Cadastre-se
                  </button>
                </template>
              </div>

              <p v-if="isRegister" class="terms">
                Ao criar uma conta, voce concorda com nossos Termos de Uso e
                Politica de Privacidade
              </p>
            </q-form>
          </transition>
        </div>
      </section>
    </div>
  </q-page>
</template>

<script setup>
import { computed, reactive, ref } from "vue";
import { useQuasar } from "quasar";
import { useRouter, useRoute } from "vue-router";
import { api, authenticate } from "boot/axios";

const $q = useQuasar();
const router = useRouter();
const route = useRoute();

const showPassword = ref(false);
const rememberMe = ref(false);

const loginForm = reactive({
  email: "",
  password: "",
});

const registerForm = reactive({
  name: "",
  email: "",
  password: "",
});

const isRegister = computed(() => route.name === "cadastro");
const currentMode = computed(() => (isRegister.value ? "register" : "login"));

const activeEmail = computed({
  get: () => (isRegister.value ? registerForm.email : loginForm.email),
  set: (value) => {
    if (isRegister.value) {
      registerForm.email = value;
    } else {
      loginForm.email = value;
    }
  },
});

const activePassword = computed({
  get: () => (isRegister.value ? registerForm.password : loginForm.password),
  set: (value) => {
    if (isRegister.value) {
      registerForm.password = value;
    } else {
      loginForm.password = value;
    }
  },
});

const notify = (type, message) => {
  $q.notify({
    type,
    message,
    position: "top",
    timeout: 2200,
  });
};

const goLogin = () => {
  router.push({ name: "login" });
};

const goRegister = () => {
  router.push({ name: "cadastro" });
};

const onLoginSubmit = () => {
  if (!loginForm.email || !loginForm.password) {
    notify("negative", "Preencha e-mail e senha para continuar.");
    return;
  }

  authenticate(loginForm.email, loginForm.password)
    .then(() => {
      router.push("/main/home");
    })
    .catch(() => {
      notify("negative", "Credenciais invalidas.");
    });
};

const onRegisterSubmit = async () => {
  if (!registerForm.name || !registerForm.email || !registerForm.password) {
    notify("negative", "Preencha todos os campos corretamente.");
    return;
  }

  try {
    const registeredEmail = registerForm.email;
    const registeredPassword = registerForm.password;

    await api.post("/user", {
      name: registerForm.name,
      email: registeredEmail,
      password: registeredPassword,
      role: "ADMIN",
    });

    await authenticate(registeredEmail, registeredPassword);

    notify("positive", "Cadastro realizado e login efetuado com sucesso.");
    registerForm.name = "";
    registerForm.email = "";
    registerForm.password = "";
    router.push("/main/home");
  } catch (error) {
    notify(
      "negative",
      error.response?.data?.message || "Erro ao cadastrar usuario."
    );
  }
};
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: stretch;
  background: #f2f2ef;
}

.auth-shell {
  width: 100%;
  min-height: 100vh;
  display: grid;
  grid-template-columns: 1fr 1fr;
}

.brand-side {
  position: relative;
  overflow: hidden;
  background: #e8ebe6;
}

.brand-layer {
  position: absolute;
  inset: 0;
  background-image: radial-gradient(rgba(0, 0, 0, 0.03) 1px, transparent 1px),
    linear-gradient(90deg, rgba(0, 0, 0, 0.018) 22px, transparent 22px),
    linear-gradient(90deg, rgba(0, 0, 0, 0.012) 11px, transparent 11px);
  background-size: 24px 24px, 160px 100%, 160px 100%;
  background-position: 0 0, 32px 0, 42px 0;
  opacity: 0.55;
}

.brand-content {
  position: relative;
  z-index: 1;
  max-width: 470px;
  margin: 0 auto;
  min-height: 100%;
  padding: 84px 36px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  text-align: center;
}

.book-badge {
  width: 64px;
  height: 64px;
  border-radius: 18px;
  background: #d6e3d7;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #1f6e2d;
  margin-bottom: 26px;
}

.brand-content h1 {
  font-family: "Cormorant Garamond", "Times New Roman", serif;
  font-size: clamp(2rem, 2.8vw, 3.4rem);
  line-height: 1.02;
  margin: 0;
  color: #2e2722;
}

.brand-content p {
  margin: 18px 0 0;
  color: #6f6b64;
  font-size: 1.04rem;
  line-height: 1.6;
}

.stats {
  margin-top: 44px;
  display: flex;
  align-items: center;
  gap: 24px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stat-item strong {
  font-size: 2rem;
  color: #1d1d1d;
  line-height: 1;
}

.stat-item span {
  margin-top: 4px;
  font-size: 0.92rem;
  color: #6f6b64;
}

.divider {
  width: 1px;
  height: 46px;
  background: rgba(0, 0, 0, 0.15);
}

.form-side {
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f7f7f5;
  padding: 36px 22px;
}

.form-wrap {
  width: min(100%, 460px);
}

.form-header {
  text-align: center;
  margin-bottom: 20px;
}

.form-header h2 {
  margin: 0;
  color: #1f1f1f;
  font-size: 2.15rem;
  font-weight: 700;
}

.form-header p {
  margin: 8px 0 0;
  color: #66645e;
  font-size: 1rem;
}

.auth-switch {
  width: 100%;
  padding: 4px;
  border-radius: 14px;
  background: #d5d3ce;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 4px;
  margin-bottom: 28px;
}

.switch-btn {
  border: 0;
  background: transparent;
  border-radius: 11px;
  padding: 11px 12px;
  font-weight: 700;
  font-size: 0.95rem;
  color: #494741;
  transition: all 0.26s ease;
}

.switch-btn.active {
  background: #f4f4f1;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.15);
  color: #232323;
}

.auth-form {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.field-label {
  color: #252525;
  font-size: 0.96rem;
  font-weight: 600;
  margin-top: 2px;
}

.field-row {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  margin-top: 2px;
}

.forgot-link {
  color: #2f7d35;
  text-decoration: none;
  font-size: 0.88rem;
  font-weight: 700;
}

.remember-checkbox {
  margin-top: -2px;
  color: #59574f;
}

.submit-btn {
  margin-top: 6px;
  border-radius: 14px;
  height: 48px;
  background: #1f702b;
  color: #fff;
  font-size: 1.03rem;
  font-weight: 700;
}

.bottom-text {
  text-align: center;
  margin-top: 4px;
  color: #66645e;
  font-size: 0.95rem;
}

.plain-link {
  background: transparent;
  border: 0;
  color: #20752d;
  font-weight: 700;
  padding: 0;
  margin-left: 4px;
  cursor: pointer;
}

.terms {
  text-align: center;
  color: #76746d;
  font-size: 0.84rem;
  line-height: 1.45;
  margin: 2px 10px 0;
}

.form-swap-enter-active,
.form-swap-leave-active {
  transition: opacity 0.26s ease, transform 0.28s ease;
}

.form-swap-enter-from {
  opacity: 0;
  transform: translateX(18px);
}

.form-swap-leave-to {
  opacity: 0;
  transform: translateX(-18px);
}

@media (max-width: 1100px) {
  .auth-shell {
    grid-template-columns: 1.05fr 1fr;
  }

  .brand-content {
    max-width: 430px;
    padding: 72px 30px;
  }
}

@media (max-width: 980px) {
  .auth-shell {
    grid-template-columns: 1fr;
  }

  .brand-side {
    min-height: 420px;
  }

  .form-side {
    padding: 26px 20px 36px;
  }
}

@media (max-width: 520px) {
  .brand-content h1 {
    font-size: 2.1rem;
  }

  .form-header h2 {
    font-size: 1.85rem;
  }

  .stats {
    gap: 14px;
  }
}
</style>
