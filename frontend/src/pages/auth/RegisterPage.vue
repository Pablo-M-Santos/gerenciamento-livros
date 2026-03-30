<template>
  <q-layout view="lHh Lpr lff" class="conteudo">
    <q-page-container>
      <q-page class="container">
        <div class="container-externo">
          <div class="container-textos">
            <h1 class="titulo">Cadastrar Usuário</h1>
            <h2 class="subtitulo">Preencha o formulário</h2>

            <q-form @submit="onSubmit" @reset="onReset">
              <!-- Nome -->
              <div class="input">
                <q-input
                  filled
                  v-model="name"
                  label="Nome"
                  prepend-icon="fa-solid fa-user"
                  lazy-rules
                  :rules="[(val) => !!val || 'Nome é obrigatório']"
                />
              </div>

              <!-- Email -->
              <div class="input">
                <q-input
                  filled
                  v-model="email"
                  label="Email"
                  prepend-icon="bx bx-envelope"
                  lazy-rules
                  :rules="[
                    (val) => !!val || 'Email é obrigatório',
                    (val && val.includes('@')) || 'Email inválido',
                  ]"
                />
              </div>

              <!-- Senha -->
              <div class="input">
                <q-input
                  filled
                  :type="isPwd ? 'password' : 'text'"
                  v-model="password"
                  label="Senha"
                  prepend-icon="fa-solid fa-lock"
                  lazy-rules
                  :rules="[
                    (val) => !!val || 'Senha é obrigatória',
                    (val && val.length >= 8) ||
                      'A senha deve ter pelo menos 8 caracteres',
                  ]"
                >
                  <template v-slot:append>
                    <q-icon
                      :name="isPwd ? 'visibility_off' : 'visibility'"
                      class="cursor-pointer"
                      @click="isPwd = !isPwd"
                    />
                  </template>
                </q-input>
              </div>

              <div class="button">
                <q-btn
                  type="submit"
                  label="Cadastrar"
                  class="q-mt-md login-button"
                  color="primary"
                  rounded
                />
              </div>

              <div class="button q-mt-sm">
                <router-link style="color: #006666" to="/login"
                  >Já tem conta? Entrar</router-link
                >
              </div>
            </q-form>
          </div>

          <div class="container-interno">
            <img src="../../assets/logo.png" />
            <h2>Seja bem-vindo!</h2>
          </div>
        </div>
      </q-page>
    </q-page-container>
  </q-layout>
</template>

<script setup>
import { ref } from "vue";
import { useQuasar } from "quasar";
import { useRouter } from "vue-router";
import { api } from "boot/axios";

const $q = useQuasar();
const router = useRouter();

const isPwd = ref(true);
const name = ref("");
const email = ref("");
const password = ref("");

const showNotification = (type, msg) => {
  $q.notify({
    type,
    message: msg,
    position: "top",
    timeout: 2000,
  });
};

const onSubmit = async () => {
  if (!name.value || !email.value || !password.value) {
    showNotification("negative", "Preencha todos os campos corretamente!");
    return;
  }

  try {
    await api.post("/user", {
      name: name.value,
      email: email.value,
      password: password.value,
    });

    showNotification("positive", "Cadastro realizado com sucesso!");
    router.push("/login");
  } catch (err) {
    showNotification(
      "negative",
      err.response?.data?.message || "Erro no cadastro!"
    );
  }
};

const onReset = () => {
  name.value = "";
  email.value = "";
  password.value = "";
};
</script>

<style scoped>
.container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
}

.container-externo {
  width: 944px;
  height: 555px;
  background-color: rgba(255, 255, 255, 0.9);
  border-radius: 16px;
  box-shadow: 3px 4px 10px 0px rgba(0, 0, 0, 0.25);
}

.container-textos {
  display: flex;
  width: 60%;
  flex-direction: column;
  align-items: center;
  float: right;

  h1 {
    font-size: 32px;
    font-weight: 800;
  }

  h2 {
    font-size: 19px;
    font-weight: medium;
  }
}

.button {
  display: flex;
  justify-content: center;
  align-items: center;
  color: #006666 !important;
}

.container-textos .titulo {
  margin: 0px;
}

.container-textos .subtitulo {
  margin: 0px;
}

.container-textos .titulo {
  margin: 0px;
}

.input {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  width: auto;
  margin: 10px 0;
}

.q-input {
  width: 480px;
}

#input-2 {
  margin-top: 45px;
}

.q-btn.login-button {
  width: 232px;
  height: 59px;
  margin-top: 60px;
  border-radius: 30px;
  background-color: #006666 !important;
  font-size: 21px;
  font-weight: 800;
  color: white;
}

.container-interno {
  width: 320px;
  height: 555px;
  background-color: #e8e8e8;
  border-radius: 16px 0px 0px 16px;
  align-items: center;

  img {
    margin: 207px 0px 0px 75px;
  }

  h2 {
    font-size: 26px;
    font-weight: 800;
    margin: 24.71px 0px 0px 52px;
  }
}
.welcome-card {
  position: fixed;
  bottom: 20px;
  right: 20px;
  width: 300px;
  background: #f8f9fa;
  color: #333;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.15);
  font-family: "Roboto", sans-serif;
  animation: slideIn 0.5s ease-out;
  z-index: 9999;
}

@keyframes slideIn {
  from {
    transform: translateX(100%);
    opacity: 0;
  }
  to {
    transform: translateX(0);
    opacity: 1;
  }
}

.card-header {
  background-color: #006666;
  color: white;
  padding: 12px 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h4 {
  font-size: 16px;
  font-weight: 700;
  margin: 0;
}

.close-btn {
  min-width: 24px;
}

.card-body {
  padding: 16px;
  font-size: 14px;
  line-height: 1.5;
}

.card-body p {
  margin: 0 0 8px 0;
}

.credentials {
  background-color: #e0f2f1;
  padding: 10px;
  border-radius: 8px;
  font-weight: 500;
  font-size: 14px;
  color: #004d40;
}

@media (max-width: 1000px) {
  .container-interno h2 {
    display: none;
  }

  .container-interno {
    width: auto;
    height: 800px;
    background-color: white;
  }

  .container-interno img {
    margin: 0px !important;
  }

  img {
    margin: 0px !important;
  }

  .container-externo {
    display: flex;
    align-items: center;
    justify-content: center;
    flex-direction: column-reverse;
    width: auto;
    height: 700px;
    padding: 50px;
  }

  .container-textos {
    margin: 0px;
  }

  .container-textos .form,
  .login-button {
    margin-top: 20px;
  }

  .q-input {
    width: 400px;
  }

  .container-textos h1 {
    font-size: 25px;
  }

  .container-textos h2 {
    font-size: 17px;
  }
}

@media (max-width: 600px) {
  .container-externo {
    box-shadow: none;
    width: 100%;
    padding: 0px;
  }

  .container-interno {
    height: 100px;
  }

  .titulo {
    font-size: 28px;
  }

  .button-redefinir {
    font-size: 18px;
  }
}

@media (max-width: 500px) {
  .q-input {
    width: 350px;
  }

  .container-textos h1 {
    font-size: 26px;
  }

  .container-textos h2 {
    font-size: 15px;
  }

  .container-interno img {
    width: 160px;
  }

  .q-btn.login-button {
    width: 200px;
    height: 50px;
  }

  .button-redefinir {
    width: 280px;
  }
}

@media (max-width: 400px) {
  .q-input {
    width: 300px;
  }

  .container-textos h1 {
    font-size: 20px;
  }
}

@media (max-width: 320px) {
  .q-input {
    width: 300px;
  }
}
</style>
