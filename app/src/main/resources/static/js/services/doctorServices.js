import { API_BASE_URL } from "../config/config.js";

const DOCTOR_API = `${API_BASE_URL}/doctor`;

async function readResponse(response) {
  if (!response.ok) throw new Error(`Doctor service unavailable (${response.status}).`);
  return response.json();
}

export async function getDoctors() {
  const data = await readResponse(await fetch(DOCTOR_API));
  return data.doctors;
}

export async function filterDoctors(name, time, specialty) {
  const doctors = await getDoctors();
  return {
    doctors: doctors.filter(doctor => {
      const slots = doctor.availableTimes || [];
      const matchesTime = !time || slots.some(slot => {
        const hour = Number(slot.slice(0, 2));
        return time === "AM" ? hour < 12 : hour >= 12;
      });
      return (!name || doctor.name.toLowerCase().includes(name.toLowerCase()))
        && (!specialty || doctor.specialty.toLowerCase() === specialty.toLowerCase())
        && matchesTime;
    })
  };
}

export async function deleteDoctor(id, token) {
  if (!token) throw new Error("Please log in again.");
  const response = await fetch(`${DOCTOR_API}/${encodeURIComponent(id)}/${encodeURIComponent(token)}`, { method: "DELETE" });
  const data = await readResponse(response);
  return { success: response.ok, message: data.message };
}

export async function saveDoctor(doctor, token) {
  if (!token) throw new Error("Please log in again.");
  const response = await fetch(`${DOCTOR_API}/${encodeURIComponent(token)}`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(doctor)
  });
  const data = await readResponse(response);
  return { success: response.ok, message: data.message };
}
