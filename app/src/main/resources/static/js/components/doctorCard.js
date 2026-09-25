import { deleteDoctor } from "../services/doctorServices.js";
import { getPatientData } from "../services/patientServices.js";

export function createDoctorCard(doctor) {
  const card = document.createElement("article");
  card.className = "doctor-card";
  const info = document.createElement("div");
  info.className = "doctor-info";

  const name = document.createElement("h3");
  name.textContent = doctor.name;
  const specialty = document.createElement("p");
  specialty.textContent = `Specialty: ${doctor.specialty}`;
  const email = document.createElement("p");
  email.textContent = `Email: ${doctor.email}`;
  const availability = document.createElement("p");
  availability.textContent = `Available: ${(doctor.availableTimes || []).join(", ") || "Contact clinic"}`;
  info.append(name, specialty, email, availability);
  card.appendChild(info);

  const actions = document.createElement("div");
  actions.className = "card-actions";
  const role = localStorage.getItem("userRole");
  if (role === "admin") {
    const remove = document.createElement("button");
    remove.type = "button";
    remove.textContent = "Delete";
    remove.addEventListener("click", async () => {
      if (!window.confirm(`Delete ${doctor.name}?`)) return;
      try {
        const result = await deleteDoctor(doctor.id, localStorage.getItem("token"));
        if (!result.success) throw new Error(result.message);
        card.remove();
      } catch (error) {
        window.alert(`Could not delete doctor: ${error.message}`);
      }
    });
    actions.appendChild(remove);
  } else if (role === "patient" || role === "loggedPatient") {
    const book = document.createElement("button");
    book.type = "button";
    book.textContent = "Book Now";
    book.addEventListener("click", async event => {
      if (role === "patient") {
        window.alert("Please log in before booking.");
        return;
      }
      const token = localStorage.getItem("token");
      if (!token) {
        window.alert("Please log in before booking.");
        return;
      }
      try {
        const patient = await getPatientData(token);
        if (!patient) throw new Error("Could not load your patient record.");
        const { showBookingOverlay } = await import("../loggedPatient.js");
        showBookingOverlay(event, doctor, patient);
      } catch (error) {
        window.alert(error.message);
      }
    });
    actions.appendChild(book);
  }
  if (actions.childElementCount) card.appendChild(actions);
  return card;
}
