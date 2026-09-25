import { API_BASE_URL } from "../config/config.js";
import { openModal } from "../components/modals.js";
import { readJson } from "./response.js";

document.getElementById("patientRole").addEventListener("click", () => selectRole("patient"));
document.getElementById("adminLogin").addEventListener("click", () => openModal("adminLogin"));
document.getElementById("doctorLogin").addEventListener("click", () => openModal("doctorLogin"));

async function login(endpoint, credentials, role) {
  try {
    const response = await fetch(`${API_BASE_URL}${endpoint}`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(credentials)
    });
    const data = await readJson(response);
    if (!data.token) throw new Error("Login response did not include a token.");
    localStorage.setItem("token", data.token);
    selectRole(role);
  } catch (error) {
    window.alert(`Unable to sign in: ${error.message}`);
  }
}

window.adminLoginHandler = () => login("/admin", {
  username: document.getElementById("username").value,
  password: document.getElementById("password").value
}, "admin");

window.doctorLoginHandler = () => login("/doctor/login", {
  email: document.getElementById("email").value,
  password: document.getElementById("password").value
}, "doctor");
