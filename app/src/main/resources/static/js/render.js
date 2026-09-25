// render.js

function selectRole(role) {
  setRole(role);
  const token = localStorage.getItem('token');
  if (role === "admin" && token) {
    window.location.href = `/adminDashboard/${encodeURIComponent(token)}`;
  } else if (role === "doctor" && token) {
    window.location.href = `/doctorDashboard/${encodeURIComponent(token)}`;
  } else if (role === "patient") {
    window.location.href = "/pages/patientDashboard.html";
  } else if (role === "loggedPatient" && token) {
    window.location.href = "/pages/loggedPatientDashboard.html";
  }
}


function renderContent() {
  const role = getRole();
  if (!role) {
    window.location.href = "/";
    return;
  }
}
