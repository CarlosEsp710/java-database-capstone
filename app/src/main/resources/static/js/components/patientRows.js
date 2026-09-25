// patientRows.js
export function createPatientRow(patient, appointmentId, doctorId) {
  const tr = document.createElement("tr");
  const idCell = tr.insertCell();
  const record = document.createElement("a");
  record.href = `/pages/patientRecord.html?id=${encodeURIComponent(patient.id)}&doctorId=${encodeURIComponent(doctorId)}`;
  record.textContent = patient.id;
  idCell.appendChild(record);
  for (const value of [patient.name, patient.phone, patient.email]) {
    tr.insertCell().textContent = value ?? "";
  }
  const action = tr.insertCell();
  const prescription = document.createElement("a");
  prescription.href = `/pages/addPrescription.html?appointmentId=${encodeURIComponent(appointmentId)}&patientName=${encodeURIComponent(patient.name)}`;
  prescription.setAttribute("aria-label", `Add prescription for ${patient.name}`);
  const icon = document.createElement("img");
  icon.src = "/assets/images/addPrescriptionIcon/addPrescription.png";
  icon.alt = "";
  icon.className = "prescription-btn";
  prescription.appendChild(icon);
  action.appendChild(prescription);

  return tr;
}
