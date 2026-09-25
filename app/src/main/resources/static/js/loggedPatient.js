// loggedPatient.js 
import { getDoctors } from './services/doctorServices.js';
import { createDoctorCard } from './components/doctorCard.js';
import { filterDoctors } from './services/doctorServices.js';
import { bookAppointment } from './services/appointmentRecordService.js';


document.addEventListener("DOMContentLoaded", () => {
  loadDoctorCards();
});

function loadDoctorCards() {
  getDoctors()
    .then(doctors => {
      const contentDiv = document.getElementById("content");
      contentDiv.innerHTML = "";

      doctors.forEach(doctor => {
        const card = createDoctorCard(doctor);
        contentDiv.appendChild(card);
      });
    })
    .catch(error => {
      console.error("Failed to load doctors:", error);
      document.getElementById("content").textContent = `Unable to load doctors: ${error.message}`;
    });
}

export function showBookingOverlay(_event, doctor, patient) {
  if (document.querySelector(".modalApp")) return;
  const modalApp = document.createElement("div");
  modalApp.classList.add("modalApp");
  modalApp.setAttribute("role", "dialog");
  modalApp.setAttribute("aria-label", "Book appointment");
  const title = document.createElement("h2");
  title.textContent = "Book Appointment";
  modalApp.appendChild(title);
  for (const value of [patient.name, doctor.name, doctor.specialty, doctor.email]) {
    const field = document.createElement("input");
    field.className = "input-field";
    field.value = value;
    field.disabled = true;
    field.setAttribute("aria-label", value);
    modalApp.appendChild(field);
  }
  const date = document.createElement("input");
  date.className = "input-field";
  date.type = "date";
  date.id = "appointment-date";
  date.setAttribute("aria-label", "Appointment date");
  const today = new Date();
  date.min = `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, "0")}-${String(today.getDate()).padStart(2, "0")}`;
  const time = document.createElement("select");
  time.className = "input-field";
  time.id = "appointment-time";
  time.setAttribute("aria-label", "Appointment time");
  time.add(new Option("Select time", ""));
  (doctor.availableTimes || []).forEach(slot => time.add(new Option(slot, slot)));
  const confirm = document.createElement("button");
  confirm.className = "confirm-booking";
  confirm.textContent = "Confirm Booking";
  const cancel = document.createElement("button");
  cancel.type = "button";
  cancel.className = "cancel-booking";
  cancel.textContent = "Cancel";
  cancel.addEventListener("click", () => modalApp.remove());
  modalApp.append(date, time, confirm, cancel);
  document.body.appendChild(modalApp);
  requestAnimationFrame(() => modalApp.classList.add("active"));
  date.focus();

  confirm.addEventListener("click", async () => {
    if (!date.value || !time.value) {
      window.alert("Choose a date and time.");
      return;
    }
    const token = localStorage.getItem("token");
    const startTime = time.value.split('-')[0];
    const appointment = {
      doctor: { id: doctor.id },
      patient: { id: patient.id },
      appointmentTime: `${date.value}T${startTime}:00`,
      status: 0
    };
    confirm.disabled = true;
    const { success, message } = await bookAppointment(appointment, token);
    confirm.disabled = false;
    if (success) {
      alert("Appointment Booked successfully");
      modalApp.remove();
    } else {
      alert("❌ Failed to book an appointment :: " + message);
    }
  });
}



// Filter Input
document.getElementById("searchBar").addEventListener("input", filterDoctorsOnChange);
document.getElementById("filterTime").addEventListener("change", filterDoctorsOnChange);
document.getElementById("filterSpecialty").addEventListener("change", filterDoctorsOnChange);



function filterDoctorsOnChange() {
  const searchBar = document.getElementById("searchBar").value.trim();
  const filterTime = document.getElementById("filterTime").value;
  const filterSpecialty = document.getElementById("filterSpecialty").value;


  const name = searchBar.length > 0 ? searchBar : null;
  const time = filterTime.length > 0 ? filterTime : null;
  const specialty = filterSpecialty.length > 0 ? filterSpecialty : null;

  filterDoctors(name, time, specialty)
    .then(response => {
      const doctors = response.doctors;
      const contentDiv = document.getElementById("content");
      contentDiv.innerHTML = "";

      if (doctors.length > 0) {
        console.log(doctors);
        doctors.forEach(doctor => {
          const card = createDoctorCard(doctor);
          contentDiv.appendChild(card);
        });
      } else {
        contentDiv.innerHTML = "<p>No doctors found with the given filters.</p>";
      }
    })
    .catch(error => {
      console.error("Failed to filter doctors:", error);
      document.getElementById("content").textContent = `Unable to filter doctors: ${error.message}`;
    });
}

export function renderDoctorCards(doctors) {
  const contentDiv = document.getElementById("content");
  contentDiv.innerHTML = "";

  doctors.forEach(doctor => {
    const card = createDoctorCard(doctor);
    contentDiv.appendChild(card);
  });

}
