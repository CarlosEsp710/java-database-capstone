import { API_BASE_URL } from "../config/config.js";
import { readJson } from "./response.js";

const DOCTOR_API = `${API_BASE_URL}/doctor`;

export async function getDoctors() {
  const data = await readJson(await fetch(DOCTOR_API));
  if (!Array.isArray(data.doctors)) throw new Error("Doctor response is missing a doctors list.");
  return data.doctors;
}

export async function filterDoctors(name, time, specialty) {
  const segments = [name, time, specialty].map(value => encodeURIComponent(value || "null"));
  const data = await readJson(await fetch(`${DOCTOR_API}/filter/${segments.join("/")}`));
  if (!Array.isArray(data.doctors)) throw new Error("Doctor filter response is missing a doctors list.");
  return data;
}

export async function deleteDoctor(id, token) {
  if (!token) throw new Error("Please log in again.");
  const response = await fetch(`${DOCTOR_API}/${encodeURIComponent(id)}/${encodeURIComponent(token)}`, { method: "DELETE" });
  const data = await readJson(response);
  return { success: true, message: data.message || "Doctor deleted." };
}

export async function saveDoctor(doctor, token) {
  if (!token) throw new Error("Please log in again.");
  const response = await fetch(`${DOCTOR_API}/${encodeURIComponent(token)}`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(doctor)
  });
  const data = await readJson(response);
  return { success: true, message: data.message || "Doctor saved." };
}
