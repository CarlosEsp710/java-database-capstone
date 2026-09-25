-- Run once on a new cms database after Hibernate has created the five tables.
-- Accounts are intentionally not usable for login; provision passwords through the application.
INSERT INTO doctor (name, specialty, email, password_hash, phone)
WITH RECURSIVE numbers AS (SELECT 1 AS n
                           UNION ALL
                           SELECT n + 1
                           FROM numbers
                           WHERE n < 25)
SELECT CONCAT('Dr. Sample ', LPAD(n, 2, '0')),
       CASE n % 5 WHEN 0 THEN 'Dermatology' WHEN 1 THEN 'Cardiology'
            WHEN 2 THEN 'Neurology' WHEN 3 THEN 'Orthopedics' ELSE 'Pediatrics'
END,
       CONCAT('doctor', LPAD(n, 2, '0'), '@clinic.example'),
       'SEED_ACCOUNT_DISABLED',
       CONCAT('555', LPAD(n, 7, '0'))
FROM numbers ORDER BY n;

INSERT INTO doctor_available_times (doctor_id, available_times)
SELECT id, slots.available_times
FROM doctor
         CROSS JOIN (SELECT '09:00-10:00' AS available_times
                     UNION ALL
                     SELECT '10:00-11:00'
                     UNION ALL
                     SELECT '14:00-15:00'
                     UNION ALL
                     SELECT '15:00-16:00') AS slots
WHERE email LIKE 'doctor__@clinic.example';

INSERT INTO patient (name, email, password_hash, phone, address)
WITH RECURSIVE numbers AS (SELECT 1 AS n
                           UNION ALL
                           SELECT n + 1
                           FROM numbers
                           WHERE n < 25)
SELECT CONCAT('Patient ', LPAD(n, 2, '0')),
       CONCAT('patient', LPAD(n, 2, '0'), '@clinic.example'),
       'SEED_ACCOUNT_DISABLED',
       CONCAT('888', LPAD(n, 7, '0')),
       CONCAT(n, ' Example Street')
FROM numbers
ORDER BY n;

-- Explicit IDs keep MongoDB appointment references stable across MySQL insert strategies.
-- 50 future scheduled visits followed by 24 completed past visits (IDs 51-74).
INSERT INTO appointment (id, appointment_time, status, doctor_id, patient_id)
SELECT n, TIMESTAMP (DATE_ADD(CURDATE(), INTERVAL (n + 7) DAY), '09:00:00'), 0, n, n
FROM (SELECT id AS n FROM doctor WHERE email LIKE 'doctor__@clinic.example') AS doctors
ORDER BY n;

INSERT INTO appointment (id, appointment_time, status, doctor_id, patient_id)
SELECT n + 25, TIMESTAMP (DATE_ADD(CURDATE(), INTERVAL (n + 37) DAY), '14:00:00'), 0, n, n
FROM (SELECT id AS n FROM doctor WHERE email LIKE 'doctor__@clinic.example') AS doctors
ORDER BY n;

INSERT INTO appointment (id, appointment_time, status, doctor_id, patient_id)
SELECT n + 50, TIMESTAMP (DATE_SUB(CURDATE(), INTERVAL n DAY), '09:00:00'), 1, n, n
FROM (SELECT id AS n FROM doctor WHERE email LIKE 'doctor__@clinic.example' AND id <= 24) AS doctors
ORDER BY n;

INSERT INTO admin (username, password_hash)
VALUES ('sample-admin', 'SEED_ACCOUNT_DISABLED');
