function logout() {
  localStorage.removeItem("token");
  localStorage.removeItem("userRole");
  window.location.assign("/");
}

function logoutPatient() {
  localStorage.removeItem("token");
  localStorage.setItem("userRole", "patient");
  window.location.assign("/pages/patientDashboard.html");
}

function renderHeader() {
  const container = document.getElementById("header");
  if (!container) return;

  const isHome = window.location.pathname === "/" || window.location.pathname === "/index.html";
  if (isHome) {
    localStorage.removeItem("userRole");
    localStorage.removeItem("token");
  }

  const role = isHome ? null : localStorage.getItem("userRole");
  const token = localStorage.getItem("token");
  if (["admin", "doctor", "loggedPatient"].includes(role) && !token) {
    localStorage.removeItem("userRole");
    window.alert("Session expired or invalid login. Please log in again.");
    window.location.assign("/");
    return;
  }

  const header = document.createElement("header");
  header.className = "header";
  header.innerHTML = `<a class="logo-link" href="/" aria-label="Smart Clinic home">
    <img class="logo-img" src="/assets/images/logo/logo.png" alt="">
    <span class="logo-title">Smart Clinic</span></a><nav aria-label="Main navigation"></nav>`;
  const nav = header.querySelector("nav");

  function addLink(label, href) {
    const link = document.createElement("a");
    link.href = href;
    link.textContent = label;
    nav.appendChild(link);
  }

  function addButton(label, id, handler) {
    const button = document.createElement("button");
    button.type = "button";
    button.id = id;
    button.className = "adminBtn";
    button.textContent = label;
    button.addEventListener("click", handler);
    nav.appendChild(button);
  }

  if (role === "admin") {
    addButton("Add Doctor", "addDocBtn", () => import("./modals.js").then(({ openModal }) => openModal("addDoctor")));
    addButton("Logout", "logoutBtn", logout);
  } else if (role === "doctor") {
    addLink("Home", `/doctorDashboard/${encodeURIComponent(token)}`);
    addButton("Logout", "logoutBtn", logout);
  } else if (role === "patient") {
    addButton("Login", "patientLogin", () => import("./modals.js").then(({ openModal }) => openModal("patientLogin")));
    addButton("Sign Up", "patientSignup", () => import("./modals.js").then(({ openModal }) => openModal("patientSignup")));
  } else if (role === "loggedPatient") {
    addLink("Home", "/pages/loggedPatientDashboard.html");
    addLink("Appointments", "/pages/patientAppointments.html");
    addButton("Logout", "logoutBtn", logoutPatient);
  }

  container.replaceChildren(header);
}

renderHeader();
