# Smart Clinic Management System Schema Design

MySQL holds accounts, schedules, and appointments that need relational integrity. MongoDB holds prescription details and flexible clinical notes. This is a design blueprint; the Java models implement the core fields, while the normalized availability table and additional prescription metadata below remain future extensions.

## MySQL Database Design

### Table: patients
- `id`: BIGINT, primary key, AUTO_INCREMENT
- `name`: VARCHAR(100), NOT NULL
- `email`: VARCHAR(255), NOT NULL, UNIQUE
- `password_hash`: VARCHAR(255), NOT NULL
- `phone`: VARCHAR(20), NOT NULL
- `address`: VARCHAR(255), NOT NULL

### Table: doctors
- `id`: BIGINT, primary key, AUTO_INCREMENT
- `name`: VARCHAR(100), NOT NULL
- `specialty`: VARCHAR(50), NOT NULL
- `email`: VARCHAR(255), NOT NULL, UNIQUE
- `password_hash`: VARCHAR(255), NOT NULL
- `phone`: VARCHAR(20), NOT NULL

### Table: appointments
- `id`: BIGINT, primary key, AUTO_INCREMENT
- `doctor_id`: BIGINT, NOT NULL, foreign key -> `doctors(id)` ON DELETE RESTRICT
- `patient_id`: BIGINT, NOT NULL, foreign key -> `patients(id)` ON DELETE RESTRICT
- `appointment_time`: DATETIME, NOT NULL (clinic-local time, with a documented clinic time zone)
- `status`: TINYINT, NOT NULL, DEFAULT 0 (0 = Scheduled, 1 = Completed, 2 = Cancelled); CHECK (`status IN (0, 1, 2)`)
- Indexes on (`doctor_id`, `appointment_time`) and (`patient_id`, `appointment_time`) for schedule and history lookups

An appointment occupies one hour, consistent with the planned Java `getEndTime` calculation. Book only within the doctor's availability. Serialize bookings for a doctor (for example, by locking the doctor row in a transaction), then check for existing *scheduled* appointments whose one-hour intervals overlap; also check the patient's bookings. A simple unique key on doctor and start time is insufficient for partial overlaps and would also prevent rebooking a cancelled slot. Completed and cancelled appointments remain in history.

### Table: admin
- `id`: BIGINT, primary key, AUTO_INCREMENT
- `username`: VARCHAR(100), NOT NULL, UNIQUE
- `password_hash`: VARCHAR(255), NOT NULL

### Table: doctor_available_times
- `id`: BIGINT, primary key, AUTO_INCREMENT
- `doctor_id`: BIGINT, NOT NULL, foreign key -> `doctors(id)` ON DELETE CASCADE
- `day_of_week`: TINYINT, NOT NULL, CHECK (`day_of_week BETWEEN 1 AND 7`) (1 = Monday)
- `start_time`: TIME, NOT NULL
- `end_time`: TIME, NOT NULL, CHECK (`end_time > start_time`)
- UNIQUE (`doctor_id`, `day_of_week`, `start_time`)

These are recurring availability windows, not booked appointments. The current `Doctor.availableTimes` model uses an `@ElementCollection` of slot strings instead of this normalized table; validate that a doctor's windows do not overlap when saving them. Holidays or exceptions can be modeled separately if needed.

Validate email and phone formats in application code. Hash passwords before storing them; never store plaintext passwords. Do not cascade deletion from patients or doctors to appointments: retain clinical history according to the clinic's retention policy, restricting deletion or anonymizing data where legally appropriate.

## MongoDB Collection Design

### Collection: prescriptions

Each prescription belongs to one MySQL appointment. Store MySQL IDs as references rather than embedding the full patient or appointment record; `patientName` is only a display snapshot and must not be treated as the source of truth. The current `Prescription` model stores `appointmentId` but not the example's `patientId`; the latter and additional metadata are future extensions. A prescription can include medication details and optional notes without changing relational tables.

```json
{
  "_id": "66f1c0e8a4b36e17d0a92f10",
  "appointmentId": 51,
  "patientId": 12,
  "patientName": "Jordan Smith",
  "medication": "Amoxicillin",
  "dosage": "500 mg",
  "doctorNotes": "Take with food.",
  "refillCount": 0,
  "pharmacy": {
    "name": "Community Pharmacy",
    "location": "Main Street"
  },
  "tags": ["antibiotic"],
  "metadata": {
    "version": 1,
    "source": "clinic"
  }
}
```

`_id` is shown as a string for valid JSON; MongoDB can store it as an ObjectId. The current model requires `patientName`, `appointmentId`, `medication`, and `dosage`. If `patientId` is added, require it and index it for patient history; index `appointmentId` for retrieval and allow multiple prescriptions per appointment. MySQL foreign keys cannot enforce MongoDB references, so the service must verify that the appointment exists (and matches the patient when an ID is available) before writing a prescription. Retain prescriptions alongside their appointments under the same clinical retention policy; avoid orphaned records when applying an approved deletion or anonymization request.
