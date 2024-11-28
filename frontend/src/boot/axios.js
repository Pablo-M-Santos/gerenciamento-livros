import { boot } from "quasar/wrappers";
import axios from "axios";

const api = axios.create({
  baseURL: process.env.VITE_BASE_URL || "http://localhost:8040",
  headers: {
    "Content-Type": "application/json"
  }
});

const token = localStorage.getItem("authToken");
if (token) {
  api.defaults.headers.common["Authorization"] = `Bearer ${token}`;
}

const authenticate = (email, password) => {
  return api
    .post("/auth/login", {
      email: email,
      password: password,
    })
    .then((response) => {
      const token = response.data.token;
      if (token) {
        localStorage.setItem("authToken", token);
        localStorage.setItem("name", response.data.name);
        localStorage.setItem("email", response.data.email);
        localStorage.setItem("role", response.data.role);
        api.defaults.headers.common["Authorization"] = `Bearer ${token}`;
      }
    })
    .catch((error) => {
      console.error(
        "Erro na autenticação:",
        error.response ? error.response.data : error.message
      );
      throw error;
    });
};
export default boot(({ app }) => {
  app.config.globalProperties.$axios = axios;
  app.config.globalProperties.$api = api;
  app.config.globalProperties.$authenticate = authenticate;
});

export { api, authenticate };
