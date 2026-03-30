<template>
  <q-layout view="lHh Lpr lFf">
    <q-header elevated class="custom-header">
      <q-toolbar>
        <q-btn
          flat
          dense
          round
          icon="menu"
          aria-label="Menu"
          @click="toggleLeftDrawer"
        />
        <q-toolbar-title>{{ pageTitle }}</q-toolbar-title>
        <div style="margin-right: auto"></div>

        <q-btn flat @click="toggleUserMenu" aria-label="User Menu">
          <q-avatar style="background-color: #008080; color: white" size="lg">
            <span class="text-h5">{{ user.initials }}</span>
            <q-menu
              v-model="userMenuVisible"
              @hide="userMenuVisible = false"
              class="custom-user-menu q-mr-md"
            >
              <q-card class="teste">
                <q-card-section>
                  <div class="q-mx-auto text-center">
                    <q-avatar
                      style="background-color: #008080; color: white"
                      class="custom-avatar"
                    >
                      <span class="text-h5">{{ user.initials }}</span>
                    </q-avatar>
                    <h6 class="custom-fullname">{{ user.fullName }}</h6>
                    <p class="text-caption q-mt-xs">{{ user.email }}</p>
                    <q-separator class="q-my-md" />
                    <p>{{ user.role }}</p>
                  </div>
                </q-card-section>
              </q-card>
            </q-menu>
          </q-avatar>
        </q-btn>
      </q-toolbar>
    </q-header>

    <q-drawer
      v-model="leftDrawerOpen"
      show-if-above
      bordered
      class="custom-drawer"
    >
      <q-list class="drawer-content">
        <q-item
          v-for="link in linksList"
          :key="link.title"
          :to="link.route"
          clickable
          active-class="menu-item-active"
          :itemid="'menu-item-' + link.title.toLowerCase().replace(' ', '-')"
        >
          <q-item-section avatar>
            <q-icon :name="link.icon" />
          </q-item-section>
          <q-item-section>
            <q-item-label>{{ link.title }}</q-item-label>
            <q-item-label caption>{{ link.caption }}</q-item-label>
          </q-item-section>
        </q-item>
      </q-list>

      <q-separator />

      <q-list class="drawer-footer logout-item">
        <q-item clickable @click="handleLogout" itemid="logoutBtn">
          <q-item-section avatar>
            <q-icon name="logout" />
          </q-item-section>
          <q-item-section>
            <q-item-label>Logout</q-item-label>
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
  background: #ffffff;
  color: #1f1f1f;
  border-right: 1px solid #e8e8e5;
  box-shadow: 8px 0 28px rgba(20, 20, 20, 0.06);
  display: flex;
  flex-direction: column;
}

.custom-drawer .q-item {
  margin: 2px 10px;
  border-radius: 10px;
}

.custom-drawer .q-item:hover {
  background: #f2f5f2;
}

.menu-item-active {
  background: #e7f0e8;
  color: #235f2f;
  font-weight: 700;
}

.drawer-content {
  flex: 1;
}

.drawer-footer {
  margin-top: auto;
}

.logout-item {
  border: none;
  box-shadow: none;
}

.logout-item:hover {
  background-color: #f5f5f3;
}

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
</style>
