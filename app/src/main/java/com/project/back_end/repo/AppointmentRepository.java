package com.project.back_end.repo;

import com.project.back_end.models.Appointment;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    @Query("select a from Appointment a join fetch a.patient join fetch a.doctor where a.doctor.id = :id and a.appointmentTime >= :start and a.appointmentTime < :end")
    List<Appointment> findByDoctorIdAndAppointmentTimeBetween(@Param("id") Long id, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("select a from Appointment a join fetch a.patient join fetch a.doctor where a.doctor.id = :id and lower(a.patient.name) like lower(concat('%', :name, '%')) and a.appointmentTime >= :start and a.appointmentTime < :end")
    List<Appointment> findByDoctorIdAndPatient_NameContainingIgnoreCaseAndAppointmentTimeBetween(
            @Param("id") Long id, @Param("name") String name, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("select a from Appointment a join fetch a.patient join fetch a.doctor where a.patient.id = :id order by a.appointmentTime")
    List<Appointment> findByPatientId(@Param("id") Long id);

    @Query("select a from Appointment a join fetch a.patient join fetch a.doctor where a.patient.id = :id and a.status = :status order by a.appointmentTime")
    List<Appointment> findByPatient_IdAndStatusOrderByAppointmentTimeAsc(@Param("id") Long id, @Param("status") int status);

    @Query("select a from Appointment a join fetch a.patient join fetch a.doctor where a.patient.id = :id and lower(a.doctor.name) like lower(concat('%', :name, '%')) order by a.appointmentTime")
    List<Appointment> filterByDoctorNameAndPatientId(@Param("name") String name, @Param("id") Long id);

    @Query("select a from Appointment a join fetch a.patient join fetch a.doctor where a.patient.id = :id and a.status = :status and lower(a.doctor.name) like lower(concat('%', :name, '%')) order by a.appointmentTime")
    List<Appointment> filterByDoctorNameAndPatientIdAndStatus(@Param("name") String name, @Param("id") Long id, @Param("status") int status);
}
