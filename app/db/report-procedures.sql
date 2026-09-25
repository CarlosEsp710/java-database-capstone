-- Run against cms after Spring Boot creates its tables. Re-running replaces these reports.
DELIMITER $$

DROP PROCEDURE IF EXISTS GetDailyAppointmentReportByDoctor$$
CREATE PROCEDURE GetDailyAppointmentReportByDoctor(IN report_date DATE)
BEGIN
    SELECT d.name AS doctor_name,
           a.appointment_time,
           a.status,
           p.name AS patient_name,
           p.phone AS patient_phone
    FROM appointment a
    JOIN doctor d ON a.doctor_id = d.id
    JOIN patient p ON a.patient_id = p.id
    WHERE a.appointment_time >= report_date
      AND a.appointment_time < report_date + INTERVAL 1 DAY
    ORDER BY d.name, a.appointment_time, a.id;
END$$

DROP PROCEDURE IF EXISTS GetDoctorWithMostPatientsByMonth$$
CREATE PROCEDURE GetDoctorWithMostPatientsByMonth(IN input_month INT, IN input_year INT)
BEGIN
    SELECT doctor_id, COUNT(DISTINCT patient_id) AS patients_seen
    FROM appointment
    WHERE status = 1
      AND MONTH(appointment_time) = input_month
      AND YEAR(appointment_time) = input_year
    GROUP BY doctor_id
    ORDER BY patients_seen DESC, doctor_id ASC
    LIMIT 1;
END$$

DROP PROCEDURE IF EXISTS GetDoctorWithMostPatientsByYear$$
CREATE PROCEDURE GetDoctorWithMostPatientsByYear(IN input_year INT)
BEGIN
    SELECT doctor_id, COUNT(DISTINCT patient_id) AS patients_seen
    FROM appointment
    WHERE status = 1
      AND YEAR(appointment_time) = input_year
    GROUP BY doctor_id
    ORDER BY patients_seen DESC, doctor_id ASC
    LIMIT 1;
END$$

DELIMITER ;
