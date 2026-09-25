// Run in mongosh after loading seed.sql into a fresh cms database.
const prescriptions = db.getSiblingDB("prescriptions").prescriptions;
prescriptions.insertMany(Array.from({ length: 24 }, (_, index) => ({
  _id: ObjectId(`6807dd712725f013281e72${(index + 1).toString(16).padStart(2, "0")}`),
  appointmentId: index + 51,
  patientName: `Patient ${String(index + 1).padStart(2, "0")}`,
  medication: "Sample medicine",
  dosage: "Demo only",
  doctorNotes: "Synthetic test record; not medical advice."
})));
prescriptions.createIndex({ appointmentId: 1 });
