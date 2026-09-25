import { getDoctors, filterDoctors, saveDoctor } from "./services/doctorServices.js";
import { createDoctorCard } from "./components/doctorCard.js";

const content = document.getElementById("content");
const search = document.getElementById("searchBar");
const time = document.getElementById("filterTime");
const specialty = document.getElementById("filterSpecialty");
let latestRequest = 0;

function showDoctors(doctors) {
  content.replaceChildren();
  if (!doctors.length) {
    content.textContent = "No doctors found with the given filters.";
    return;
  }
  doctors.forEach(doctor => content.appendChild(createDoctorCard(doctor)));
}

async function loadDoctors() {
  const request = ++latestRequest;
  try {
    const filtered = search.value || time.value || specialty.value;
    const data = filtered
      ? await filterDoctors(search.value.trim(), time.value, specialty.value)
      : { doctors: await getDoctors() };
    if (request === latestRequest) showDoctors(data.doctors);
  } catch (error) {
    if (request === latestRequest) content.textContent = `Unable to load doctors: ${error.message}`;
  }
}

search.addEventListener("input", loadDoctors);
time.addEventListener("change", loadDoctors);
specialty.addEventListener("change", loadDoctors);
loadDoctors();

window.adminAddDoctor = async function () {
  const doctor = {
    name: document.getElementById("doctorName").value.trim(),
    specialty: document.getElementById("specialization").value,
    email: document.getElementById("doctorEmail").value.trim(),
    password: document.getElementById("doctorPassword").value,
    phone: document.getElementById("doctorPhone").value.trim(),
    availableTimes: [...document.querySelectorAll('input[name="availability"]:checked')].map(input => input.value)
  };
  if (!doctor.name || !doctor.specialty || !doctor.email || !doctor.password || !doctor.phone) {
    window.alert("Complete all doctor fields before saving.");
    return;
  }
  try {
    const result = await saveDoctor(doctor, localStorage.getItem("token"));
    if (!result.success) throw new Error(result.message);
    const modal = document.getElementById("modal");
    modal.style.display = "none";
    modal.setAttribute("aria-hidden", "true");
    await loadDoctors();
  } catch (error) {
    window.alert(`Unable to save doctor: ${error.message}`);
  }
};
