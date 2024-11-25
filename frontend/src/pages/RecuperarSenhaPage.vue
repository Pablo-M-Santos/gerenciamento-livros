<template>
  <q-layout view="lHh Lpr lff" class="conteudo">
    <q-page-container>
      <q-page class="container">
        <div class="container-externo">
          <div class="container-textos">
            <h1 class="titulo">Recuperação de Senha</h1>
            <div class="subtitulo">Será enviado um token no email para a recuperação de senha</div>
            <q-form @submit="onSubmit">
              <div class="input">
                <q-input filled v-model="email" label="Email" type="email" required lazy-rules
                  :rules="[val => !!val || 'Email é obrigatório', val => /.+@.+\..+/.test(val) || 'Email inválido']" />
              </div>

              <div class="input" v-if="showTokenInput">
                <q-input filled v-model="token" label="Token" type="text" required lazy-rules
                  :rules="[val => !!val || 'Token é obrigatório']" />
              </div>

              <div class="button">
                <q-btn type="submit" :label="showTokenInput ? 'VALIDAR TOKEN' : 'ENVIAR'" class="login-button"
                  color="primary" rounded />

                <q-btn type="submit" label="Voltar" class="login-button" color="primary" rounded @click="voltar" />
              </div>
            </q-form>
          </div>

          <div class="container-interno">
            <img src="../assets/logo.png" />
          </div>
        </div>
      </q-page>
    </q-page-container>
  </q-layout>
</template>

<script setup>
import { ref } from 'vue';
import { useQuasar } from 'quasar';
import axios from 'axios';
import { useRouter } from 'vue-router';
import { useRoute } from 'vue-router';

const $q = useQuasar();
const router = useRouter();
const route = useRoute();


const showNotification = (type, msg) => {
  $q.notify({
    type: type,
    message: msg,
    position: 'top',
    timeout: 2000
  });
};

const email = ref('');
const token = ref('');
const showTokenInput = ref(false);

const onSubmit = () => {
  if (!showTokenInput.value) {
    if (!email.value) {
      showNotification('negative', 'Por favor, digite seu email');
      return;
    }

    axios.post('http://localhost:8000/api/forgot', {
      email: email.value
    }, {
      headers: {
        'Content-Type': 'application/json'
      }
    })
      .then(response => {
        showNotification('positive', 'Um token de recuperação foi enviado para o seu email!');
        showTokenInput.value = true;
      })
      .catch(error => {
        console.error('Erro ao enviar o token de recuperação:', error);
        showNotification('negative', 'Erro ao enviar o token de recuperação. Verifique o email e tente novamente.');
      });

  } else {
    if (!token.value) {
      showNotification('negative', 'Por favor, digite o token');
      return;
    }

    axios.post('http://localhost:8000/api/reset-password/validate', {
      token: token.value
    }, {
      headers: {
        'Content-Type': 'application/json'
      }
    })
      .then(response => {
        router.push({ name: 'resetPassword', query: { token: token.value } });
        showNotification('positive', 'Token válido! Redirecionando para a página de redefinição de senha...');
      })
      .catch(error => {
        console.error('Erro ao validar o token:', error);
        showNotification('negative', 'Token inválido. Tente novamente.');
      });
  }
};


const voltar = () => {
  router.push('/');
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
  margin-top: 100px;
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
  gap: 20px;
}

.container-textos .titulo {
  margin: 0px;
}

.container-textos .subtitulo {
  font-size: 17px;
  margin-bottom: 20px;
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
  width: 200px;
  height: auto;
  margin-top: 10px;
  border-radius: 30px;
  background-color: #006666;
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
