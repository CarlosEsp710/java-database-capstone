# Stored procedure outputs (synthetic clinic data)

Run on 2026-09-25 against the local `cms` seed. The seed uses dates relative to its creation date; the literal 2025 dates in the lab have no matching appointments in this database. These are the complete outputs for each call.

`CALL GetDailyAppointmentReportByDoctor('2026-09-24');`

```text
doctor_name	appointment_time	status	patient_name	patient_phone
Dr. Sample 01	2026-09-24 09:00:00.000000	1	Patient 01	8880000001
```

`CALL GetDoctorWithMostPatientsByMonth(9, 2026);`

```text
doctor_id	patients_seen
1	1
```

`CALL GetDoctorWithMostPatientsByYear(2026);`

```text
doctor_id	patients_seen
1	1
```

The 24 completed visits each belong to a different doctor, so the monthly and yearly reports tie at one patient. The procedures return doctor 1 because ties are broken by doctor ID.
