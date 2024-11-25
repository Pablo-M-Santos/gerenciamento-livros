<template>
  <q-layout view="lHh Lpr lff" class="conteudo">
    <q-page-container>
      <q-page class="container">
        <div class="container-externo">
          <div class="container-textos">
            <h1 class="titulo">Recuperação de Senha</h1>
            <div class="subtitulo">
              Digite a nova senha e confirme a senha para concluir a recuperação.
            </div>

            <!-- Formulário para recuperação de senha -->
            <q-form @submit.prevent="onSubmit" class="form">
              <div class="input">
                <q-input filled v-model="newPassword" label="Nova Senha" type="password" required />
              </div>

              <div class="input">
                <q-input filled v-model="confirmPassword" label="Confirmar Senha" type="password" required />
              </div>

              <div class="button">
                <q-btn type="submit" label="REDIFINIR SENHA" class="q-mt-md login-button" color="primary" rounded />

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
import { useRoute, useRouter } from 'vue-router';
import { Notify } from 'quasar';

const route = useRoute();
const router = useRouter();

const token = route.query.token;

const newPassword = ref('');
const confirmPassword = ref('');

const onSubmit = async () => {
  if (newPassword.value !== confirmPassword.value) {
    Notify.create({
      type: 'negative',
      message: 'As senhas não coincidem.',
      position: 'top',
    });
    return;
  }

  try {
    const response = await fetch('http://localhost:8000/api/reset-password', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        token,
        newPassword: newPassword.value,
      }),
    });

    if (response.ok) {
      Notify.create({
        type: 'positive',
        message: 'Senha redefinida com sucesso!',
        position: 'top',
      });
      setTimeout(() => {
        router.push({ name: 'login' })
      }, 2000);
    } else {
      const error = await response.text();
      Notify.create({
        type: 'negative',
        message: `Erro: ${error}`,
        position: 'top',
      });
    }
  } catch (error) {
    Notify.create({
      type: 'negative',
      message: 'Erro ao redefinir a senha. Tente novamente mais tarde.',
      position: 'top',
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
  margin-top: 80px;
  flex-direction: column;
  align-items: center;
  float: right;
}

.titulo {
  font-size: 32px;
  font-weight: 800;
  margin: 0;
}

.subtitulo {
  margin: 0;
  font-weight: 500;
}

.input {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  width: auto;
  margin: 20px 0;
}

.q-input {
  width: 480px;
}

.button {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 20px;
}

.q-btn.login-button {
  width: 230px;
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
}

.container-interno img {
  margin: 207px 0px 0px 75px;
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

  .q-input {
    width: 400px;
  }

  .titulo {
    font-size: 25px;
  }

  .subtitulo {
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

  .titulo {
    font-size: 26px;
  }

  .subtitulo {
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

  .titulo {
    font-size: 20px;
  }
}

@media (max-width: 320px) {
  .q-input {
    width: 300px;
  }
}
</style>
