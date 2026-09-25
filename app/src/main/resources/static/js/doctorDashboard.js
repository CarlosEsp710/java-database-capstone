import { getAllAppointments } from "./services/appointmentRecordService.js";
import { createPatientRow } from "./components/patientRows.js";

const body = document.getElementById("patientTableBody");
const search = document.getElementById("searchBar");
const datePicker = document.getElementById("datePicker");
let latestRequest = 0;
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
  const request = ++latestRequest;
  body.replaceChildren();
  try {
    const data = await getAllAppointments(datePicker.value, search.value.trim() || "null", localStorage.getItem("token"));
    if (request !== latestRequest) return;
    if (!Array.isArray(data.appointments)) throw new Error("Appointment response is missing appointments.");
    const appointments = data.appointments;
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
    if (request === latestRequest) {
      body.replaceChildren();
      message(`Unable to load appointments: ${error.message}`);
    }
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
