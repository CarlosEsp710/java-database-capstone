import { API_BASE_URL } from "../config/config.js";
import { openModal } from "../components/modals.js";

document.getElementById("patientRole").addEventListener("click", () => selectRole("patient"));
document.getElementById("adminLogin").addEventListener("click", () => openModal("adminLogin"));
document.getElementById("doctorLogin").addEventListener("click", () => openModal("doctorLogin"));

async function login(path, credentials, role) {
  try {
    const response = await fetch(`${API_BASE_URL}/${path}/login`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(credentials)
    });
    if (!response.ok) {
      window.alert(`Login failed (${response.status}). Please check your credentials or try again later.`);
      return;
    }
    const data = await response.json();
    if (!data.token) throw new Error("Login response did not include a token.");
    localStorage.setItem("token", data.token);
    selectRole(role);
  } catch (error) {
    window.alert(`Unable to sign in: ${error.message}`);
  }
}

window.adminLoginHandler = () => login("admin", {
  username: document.getElementById("username").value,
  password: document.getElementById("password").value
}, "admin");

window.doctorLoginHandler = () => login("doctor", {
  email: document.getElementById("email").value,
  password: document.getElementById("password").value
}, "doctor");
