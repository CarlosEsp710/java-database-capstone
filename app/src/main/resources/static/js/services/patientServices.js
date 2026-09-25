import { API_BASE_URL } from "../config/config.js";
import { readJson } from "./response.js";

const PATIENT_API = `${API_BASE_URL}/patient`;

export async function patientSignup(data) {
  const response = await fetch(PATIENT_API, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(data)
  });
  const result = await readJson(response);
  return { success: true, message: result.message || "Patient registered." };
}

export function patientLogin(data) {
  return fetch(`${PATIENT_API}/login`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(data)
  });
}

export async function getPatientData(token) {
  if (!token) throw new Error("Please log in again.");
  const data = await readJson(await fetch(`${PATIENT_API}/${encodeURIComponent(token)}`));
  if (!data.patient) throw new Error("Patient response is missing patient details.");
  return data.patient;
}

export async function getPatientAppointments(id, token, user) {
  if (!token) throw new Error("Please log in again.");
  const path = [id, user, token].map(value => encodeURIComponent(value));
  const data = await readJson(await fetch(`${PATIENT_API}/${path.join("/")}`));
  if (!Array.isArray(data.appointments)) throw new Error("Patient response is missing appointments.");
  return data.appointments;
}

export async function filterAppointments(condition, name, token) {
  if (!token) throw new Error("Please log in again.");
  const path = [condition || "null", name || "null", token].map(value => encodeURIComponent(value));
  const data = await readJson(await fetch(`${PATIENT_API}/filter/${path.join("/")}`));
  if (!Array.isArray(data.appointments)) throw new Error("Patient filter response is missing appointments.");
  return data;
}
