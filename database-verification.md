# Clinic database verification (synthetic local data)

Run on 2026-09-25 after initializing a fresh `cms` database and the `prescriptions` collection. Appointment dates are relative to the day the seed runs, so they will differ on a later run. All account passwords below are deliberately disabled placeholders.

## MySQL

`SHOW TABLES;`

```text
Tables_in_cms
admin
appointment
doctor
doctor_available_times
patient
```

`SELECT * FROM doctor LIMIT 5;`

```text
id	email	name	password_hash	phone	specialty
1	doctor01@clinic.example	Dr. Sample 01	SEED_ACCOUNT_DISABLED	5550000001	Cardiology
2	doctor02@clinic.example	Dr. Sample 02	SEED_ACCOUNT_DISABLED	5550000002	Neurology
3	doctor03@clinic.example	Dr. Sample 03	SEED_ACCOUNT_DISABLED	5550000003	Orthopedics
4	doctor04@clinic.example	Dr. Sample 04	SEED_ACCOUNT_DISABLED	5550000004	Pediatrics
5	doctor05@clinic.example	Dr. Sample 05	SEED_ACCOUNT_DISABLED	5550000005	Dermatology
```

`SELECT * FROM doctor_available_times LIMIT 5;`

```text
doctor_id	available_times
1	15:00-16:00
1	14:00-15:00
1	10:00-11:00
1	09:00-10:00
2	15:00-16:00
```

`SELECT * FROM patient LIMIT 5;`

```text
id	address	email	name	password_hash	phone
1	1 Example Street	patient01@clinic.example	Patient 01	SEED_ACCOUNT_DISABLED	8880000001
2	2 Example Street	patient02@clinic.example	Patient 02	SEED_ACCOUNT_DISABLED	8880000002
3	3 Example Street	patient03@clinic.example	Patient 03	SEED_ACCOUNT_DISABLED	8880000003
4	4 Example Street	patient04@clinic.example	Patient 04	SEED_ACCOUNT_DISABLED	8880000004
5	5 Example Street	patient05@clinic.example	Patient 05	SEED_ACCOUNT_DISABLED	8880000005
```

`SELECT * FROM appointment ORDER BY appointment_time LIMIT 5;`

```text
id	appointment_time	status	doctor_id	patient_id
74	2026-09-01 09:00:00.000000	1	24	24
73	2026-09-02 09:00:00.000000	1	23	23
72	2026-09-03 09:00:00.000000	1	22	22
71	2026-09-04 09:00:00.000000	1	21	21
70	2026-09-05 09:00:00.000000	1	20	20
```

`SELECT * FROM admin;`

```text
id	password_hash	username
1	SEED_ACCOUNT_DISABLED	sample-admin
```

Counts: 25 doctors, 100 available slots, 25 patients, 74 appointments, 1 admin. All 24 completed appointment IDs 51–74 belong to the matching synthetic patients.

## MongoDB

`use prescriptions; db.prescriptions.find().limit(5).pretty();`

```text
[
  {
    _id: ObjectId('6807dd712725f013281e7201'),
    appointmentId: 51,
    patientName: 'Patient 01',
    medication: 'Sample medicine',
    dosage: 'Demo only',
    doctorNotes: 'Synthetic test record; not medical advice.'
  },
  {
    _id: ObjectId('6807dd712725f013281e7202'),
    appointmentId: 52,
    patientName: 'Patient 02',
    medication: 'Sample medicine',
    dosage: 'Demo only',
    doctorNotes: 'Synthetic test record; not medical advice.'
  },
  {
    _id: ObjectId('6807dd712725f013281e7203'),
    appointmentId: 53,
    patientName: 'Patient 03',
    medication: 'Sample medicine',
    dosage: 'Demo only',
    doctorNotes: 'Synthetic test record; not medical advice.'
  },
  {
    _id: ObjectId('6807dd712725f013281e7204'),
    appointmentId: 54,
    patientName: 'Patient 04',
    medication: 'Sample medicine',
    dosage: 'Demo only',
    doctorNotes: 'Synthetic test record; not medical advice.'
  },
  {
    _id: ObjectId('6807dd712725f013281e7205'),
    appointmentId: 55,
    patientName: 'Patient 05',
    medication: 'Sample medicine',
    dosage: 'Demo only',
    doctorNotes: 'Synthetic test record; not medical advice.'
  }
]
```

Total prescriptions: 24; `appointmentId` has an index.
