<template>
  <q-layout view="lHh Lpr lFf">
    <q-drawer
      v-model="leftDrawerOpen"
      show-if-above
      bordered
      class="custom-drawer"
    >
      <div class="drawer-header">
        <div class="drawer-brand">
          <div class="brand-logo-wrap">
            <img
              :src="brandLogo"
              alt="Logo Locadora Livros"
              class="brand-logo"
            />
          </div>
          <div class="brand-texts">
            <span class="brand-title">Locadora Livros</span>
            <span class="brand-subtitle">Sistema de Gerenciamento</span>
          </div>
        </div>
      </div>

      <q-separator class="drawer-separator" />

      <q-list class="drawer-content">
        <q-item
          v-for="link in linksList"
          :key="link.title"
          :to="link.route"
          clickable
          active-class="menu-item-active"
          :itemid="'menu-item-' + link.title.toLowerCase().replace(' ', '-')"
          class="menu-item-animated"
        >
          <q-item-section avatar class="menu-icon-section">
            <q-icon :name="link.icon" class="menu-icon" />
          </q-item-section>
          <q-item-section class="menu-label-section">
            <q-item-label class="menu-label">{{ link.title }}</q-item-label>
            <q-item-label caption class="menu-caption">{{
              link.caption
            }}</q-item-label>
          </q-item-section>
        </q-item>
      </q-list>

      <q-separator class="drawer-separator" />

      <q-list class="drawer-footer logout-item">
        <q-item
          clickable
          @click="handleLogout"
          itemid="logoutBtn"
          class="menu-item-animated"
        >
          <q-item-section avatar class="menu-icon-section">
            <q-icon name="logout" class="menu-icon" />
          </q-item-section>
          <q-item-section class="menu-label-section">
            <q-item-label class="menu-label">Logout</q-item-label>
          </q-item-section>
        </q-item>
      </q-list>
    </q-drawer>

    <q-page-container>
      <router-view />
    </q-page-container>
  </q-layout>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import { api } from "src/boot/axios.js";
import brandLogo from "src/assets/locadora-logo.svg";

const route = useRoute();
const router = useRouter();

const linksList = [
  { title: "Home", caption: "", icon: "home", route: { name: "home" } },
  { title: "Editora", caption: "", icon: "edit", route: { name: "editora" } },
  { title: "Livros", caption: "", icon: "book", route: { name: "livros" } },
  {
    title: "Locatário",
    caption: "",
    icon: "person",
    route: { name: "locatario" },
  },
  {
    title: "Aluguel",
    caption: "",
    icon: "import_contacts",
    route: { name: "aluguel" },
  },
  {
    title: "Usuário",
    caption: "",
    icon: "settings",
    route: { name: "usuario" },
  },
];
const leftDrawerOpen = ref(false);
const userMenuVisible = ref(false);

function toggleLeftDrawer() {
  leftDrawerOpen.value = !leftDrawerOpen.value;
}

const pageTitle = computed(() => {
  return route.meta.title || "Página Inicial";
});

function toggleUserMenu() {
  userMenuVisible.value = !userMenuVisible.value;
}

function handleLogout() {
  try {
    if (localStorage.getItem("authToken")) {
      localStorage.removeItem("authToken");
    }
    router.push({ name: "login" });
    logoutDialog.value = true;
  } catch (error) {
    console.error("Error logging out:", error);
  }
}

const logoutDialog = ref(false);

const user = ref({
  initials: "",
  fullName: "",
  email: "",
  role: "",
});

onMounted(() => {
  const token = localStorage.getItem("authToken");
  const name = localStorage.getItem("name");
  const email = localStorage.getItem("email");
  const role = localStorage.getItem("role");

  if (token) {
    api.defaults.headers.common["Authorization"] = `Bearer ${token}`;
    user.value.email = email;
    user.value.fullName = name;

    const nameParts = name.split(" ");
    const firstNameInitial = nameParts[0].charAt(0);
    const lastNameInitial =
      nameParts.length > 1 ? nameParts[nameParts.length - 1].charAt(0) : "";
    user.value.initials = (firstNameInitial + lastNameInitial).toUpperCase();

    user.value.role = formatRole(role);
  }
});

function formatRole(role) {
  const roleMap = {
    ADMIN: "Administrador",
    USER: "Locatário",
  };
  return roleMap[role] || role;
}
</script>

<style>
.custom-header {
  background: #ffffff;
  color: #1f1f1f;
  border-bottom: 1px solid #e8e8e5;
  box-shadow: 0 6px 24px rgba(20, 20, 20, 0.06);
}

.custom-drawer {
  background: linear-gradient(135deg, #fafaf8 0%, #ffffff 100%);
  color: #1f1f1f;
  border-right: 1px solid #e8e8e5;
  box-shadow: 8px 0 28px rgba(20, 20, 20, 0.06);
  display: flex;
  flex-direction: column;
  position: relative;
}

/* Header do Drawer */
.drawer-header {
  padding: 22px 14px 14px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.drawer-brand {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
  padding: 12px;
  border-radius: 14px;
  background: rgba(31, 114, 44, 0.08);
  border: 1px solid rgba(31, 114, 44, 0.12);
}

.brand-logo-wrap {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #ffffff;
  box-shadow: 0 4px 12px rgba(20, 20, 20, 0.08);
  flex-shrink: 0;
}

.brand-logo {
  width: 30px;
  height: 30px;
  object-fit: contain;
}

.brand-texts {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.brand-title {
  font-weight: 700;
  font-size: 0.96rem;
  color: #1f722c;
  letter-spacing: 0.2px;
  white-space: nowrap;
}

.brand-subtitle {
  margin-top: 1px;
  font-size: 0.78rem;
  color: #6b6b64;
  white-space: nowrap;
}

.drawer-separator {
  background: linear-gradient(90deg, transparent, #e0e0e0, transparent);
  margin: 0;
}

/* Conteúdo do Drawer */
.drawer-content {
  flex: 1;
  padding: 12px 8px;
}

.menu-item-animated {
  margin: 6px 8px;
  border-radius: 12px;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

.menu-item-animated::before {
  content: "";
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 3px;
  background: #1f722c;
  transform: scaleY(0);
  transform-origin: center;
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.menu-item-animated:hover {
  background: rgba(31, 114, 44, 0.08);
}

.menu-item-animated:hover .menu-icon {
  color: #1f722c;
}

.custom-drawer .menu-item-active {
  background: rgba(31, 114, 44, 0.12);
  color: #1f722c;
  font-weight: 700;
}

.custom-drawer .menu-item-active::before {
  transform: scaleY(1);
}

.custom-drawer .menu-item-active .menu-icon {
  color: #1f722c;
  animation: iconBounce 0.4s ease;
}

.menu-icon-section {
  min-width: 40px;
  padding: 0 4px;
}

.menu-icon {
  color: #6b6b64;
  transition: color 0.25s ease;
  font-size: 1.3rem;
}

.custom-drawer .menu-item-active .menu-icon {
  color: #1f722c;
  animation: iconBounce 0.4s ease;
}

.menu-label-section {
  padding: 6px 0;
}

.menu-label {
  font-weight: 600;
  font-size: 0.95rem;
  color: #222;
  transition: color 0.25s ease;
}

.menu-caption {
  font-size: 0.8rem;
  color: #999;
  margin-top: 2px;
}

.custom-drawer .menu-item-active .menu-label {
  color: #1f722c;
}

/* Footer */
.drawer-footer {
  margin-top: auto;
  margin-bottom: 0;
  padding: 8px;
}

.logout-item {
  border: none;
  box-shadow: none;
}

.logout-item .q-item {
  margin: 8px 8px;
  border-radius: 12px;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
}

.logout-item .q-item:hover {
  background-color: rgba(220, 38, 38, 0.08);
}

.logout-item .q-item:hover .menu-icon {
  color: #dc2626;
}

.logout-item .q-item:hover .menu-label {
  color: #dc2626;
}

/* Elementos antigos mantidos para compatibilidade */
.logo {
  width: 110.62px;
  height: 56px;
  margin: 22px auto 30px;
}

.custom-user-menu {
  min-width: 230px;
  max-height: 500px;
  border-radius: 12px;
}

h6 {
  margin: 0;
}

.custom-fullname {
  color: #333;
  font-weight: 700;
}

.custom-btn {
  color: #007bff;
  transition: background-color 0.3s;
}

.custom-btn:hover {
  background-color: rgba(0, 123, 255, 0.1);
}

.q-separator {
  background-color: #e0e0e0;
}

.q-toolbar {
  min-height: 62px;
}

.q-toolbar-title {
  font-weight: 700;
}

/* Animação dos ícones */
@keyframes iconBounce {
  0% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.2);
  }
  100% {
    transform: scale(1);
  }
}
</style>
