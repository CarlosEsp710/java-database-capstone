import { getAllAppointments } from "./services/appointmentRecordService.js";
import { createPatientRow } from "./components/patientRows.js";

const body = document.getElementById("patientTableBody");
const search = document.getElementById("searchBar");
const datePicker = document.getElementById("datePicker");
const today = () => {
  const date = new Date();
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, "0")}-${String(date.getDate()).padStart(2, "0")}`;
};

function message(text) {
  const row = body.insertRow();
  row.insertCell().colSpan = 5;
  row.cells[0].textContent = text;
  row.cells[0].className = "noPatientRecord";
}

async function loadAppointments() {
  body.replaceChildren();
  try {
    const data = await getAllAppointments(datePicker.value, search.value.trim() || "null", localStorage.getItem("token"));
    const appointments = data.appointments || [];
    if (!appointments.length) {
      message("No appointments found for this date.");
      return;
    }
    appointments.forEach(appointment => {
      const patient = {
        id: appointment.patientId,
        name: appointment.patientName,
        phone: appointment.patientPhone,
        email: appointment.patientEmail
      };
      body.appendChild(createPatientRow(patient, appointment.id, appointment.doctorId));
    });
  } catch (error) {
    message(`Unable to load appointments: ${error.message}`);
  }
}

datePicker.value = today();
document.getElementById("todayButton").addEventListener("click", () => {
  datePicker.value = today();
  loadAppointments();
});
datePicker.addEventListener("change", loadAppointments);
search.addEventListener("input", loadAppointments);
loadAppointments();
