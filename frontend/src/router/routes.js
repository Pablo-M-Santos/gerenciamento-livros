// src/router/routes.js
const LoginLayout = () => import("layouts/LoginLayout.vue");
const MainLayout = () => import("layouts/MainLayout.vue");

const AuthPage = () => import("pages/auth/AuthPage.vue");
const RecuperarSenhaPage = () => import("pages/auth/RecuperarSenhaPage.vue");
const ResetPasswordPage = () => import("pages/auth/ResetPasswordPage.vue");

const HomePage = () => import("pages/dashboard/HomePage.vue");
const EditoraPage = () => import("pages/editora/EditoraPage.vue");
const LivroPage = () => import("pages/livros/LivroPage.vue");
const LocatarioPage = () => import("pages/locatario/LocatarioPage.vue");
const AluguelPage = () => import("pages/aluguel/AluguelPage.vue");
const UsuarioPage = () => import("pages/usuarios/UsuarioPage.vue");

const ErrorNotFound = () => import("pages/error/ErrorNotFound.vue");

const routes = [
  {
    path: "/",
    component: LoginLayout,
    children: [
      {
        path: "",
        name: "login",
        component: AuthPage,
        meta: { title: "Login" },
      },
      {
        path: "login",
        redirect: { name: "login" },
      },
      {
        path: "register",
        name: "cadastro",
        component: AuthPage,
        meta: { title: "Cadastro" },
      },
      {
        path: "recuperar-senha",
        name: "recuperarSenha",
        component: RecuperarSenhaPage,
        meta: { title: "Recuperar Senha" },
      },
      {
        path: "reset-password",
        name: "resetPassword",
        component: ResetPasswordPage,
        meta: { title: "Redefinir Senha" },
      },
    ],
  },
  {
    path: "/main",
    component: MainLayout,
    children: [
      {
        path: "home",
        name: "home",
        component: HomePage,
        meta: { title: "Página Inicial" },
      },
      {
        path: "editora",
        name: "editora",
        component: EditoraPage,
        meta: { title: "Controle de Editoras" },
      },
      {
        path: "livros",
        name: "livros",
        component: LivroPage,
        meta: { title: "Controle de Livros" },
      },
      {
        path: "locatario",
        name: "locatario",
        component: LocatarioPage,
        meta: { title: "Controle Locatário" },
      },
      {
        path: "aluguel",
        name: "aluguel",
        component: AluguelPage,
        meta: { title: "Controle de Aluguel" },
      },
      {
        path: "usuario",
        name: "usuario",
        component: UsuarioPage,
        meta: { title: "Controle de Usuário" },
      },
    ],
  },
  {
    path: "/:catchAll(.*)*",
    name: "notfound",
    component: ErrorNotFound,
    meta: { title: "Página não encontrada" },
  },
];

export default routes;
