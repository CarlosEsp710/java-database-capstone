// patientDashboard.js
import { getDoctors } from './services/doctorServices.js';
import { createDoctorCard } from './components/doctorCard.js';
import { filterDoctors } from './services/doctorServices.js';
import { patientSignup, patientLogin } from './services/patientServices.js';
import { readJson } from './services/response.js';

const content = document.getElementById("content");
let latestRequest = 0;

function renderDoctorCards(doctors) {
  content.replaceChildren();
  if (!doctors.length) {
    content.textContent = "No doctors found with the given filters.";
    return;
  }
  doctors.forEach(doctor => content.appendChild(createDoctorCard(doctor)));
}

async function loadDoctors() {
  const request = ++latestRequest;
  const name = document.getElementById("searchBar").value.trim();
  const time = document.getElementById("filterTime").value;
  const specialty = document.getElementById("filterSpecialty").value;
  try {
    const doctors = name || time || specialty
      ? (await filterDoctors(name, time, specialty)).doctors
      : await getDoctors();
    if (request === latestRequest) renderDoctorCards(doctors);
  } catch (error) {
    if (request === latestRequest) content.textContent = `Unable to load doctors: ${error.message}`;
  }
}

document.getElementById("searchBar").addEventListener("input", loadDoctors);
document.getElementById("filterTime").addEventListener("change", loadDoctors);
document.getElementById("filterSpecialty").addEventListener("change", loadDoctors);
loadDoctors();

window.signupPatient = async function () {
  try {
    const name = document.getElementById("name").value;
    const email = document.getElementById("email").value;
    const password = document.getElementById("password").value;
    const phone = document.getElementById("phone").value;
    const address = document.getElementById("address").value;

    const data = { name, email, password, phone, address };
    const { message } = await patientSignup(data);
    alert(message);
    document.getElementById("modal").style.display = "none";
    document.getElementById("modal").setAttribute("aria-hidden", "true");
    window.location.reload();
  } catch (error) {
    alert(`Unable to sign up: ${error.message}`);
  }
};

window.loginPatient = async function () {
  try {
    const email = document.getElementById("email").value;
    const password = document.getElementById("password").value;

    const data = {
      email,
      password
    }
    const result = await readJson(await patientLogin(data));
    if (!result.token) throw new Error("Login response did not include a token.");
    localStorage.setItem('token', result.token);
    selectRole('loggedPatient');
  }
  catch (error) {
    alert(`Unable to log in: ${error.message}`);
  }


}
