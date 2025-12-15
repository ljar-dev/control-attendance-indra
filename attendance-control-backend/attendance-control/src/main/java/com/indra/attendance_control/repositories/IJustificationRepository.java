package com.indra.attendance_control.repositories;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.indra.attendance_control.models.Employee;
import com.indra.attendance_control.models.Justification;

public interface IJustificationRepository extends JpaRepository<Justification, Long> {

    /**
     * Obtiene una justificación a partir del identificador del registro de
     * asistencia
     */
    Optional<Justification> findByAttendanceRecordIdAttendanceRecord(Long attendanceRecordId);

    /**
     * Obtiene las justificaciones asociadas a un empleado
     */
    @Query("SELECT j FROM Justification j " +
            "LEFT JOIN FETCH j.attendanceRecord ar " +
            "LEFT JOIN FETCH ar.employee " +
            "WHERE ar.employee = :employee " +
            "ORDER BY j.createdAt DESC")
    List<Justification> findByEmployee(@Param("employee") Employee employee);

    /**
     * Obtiene las justificaciones asociadas a múltiples registros de asistencia
     */
    @Query("SELECT j FROM Justification j " +
            "LEFT JOIN FETCH j.attendanceRecord ar " +
            "WHERE ar.idAttendanceRecord IN :attendanceRecordIds")
    List<Justification> findByAttendanceRecordIdIn(
            @Param("attendanceRecordIds") List<Long> attendanceRecordIds);

    /**
     * Cuenta las justificaciones enviadas en un rango de fechas
     */
    @Query(value = "SELECT COUNT(*) FROM justifications j " +
            "JOIN attendance_records ar ON j.attendance_record_id = ar.id_attendance_record " +
            "WHERE TRUNC(ar.check_in) BETWEEN :startDate AND :endDate " +
            "AND j.justification_text IS NOT NULL " +
            "AND TRIM(j.justification_text) <> ''", nativeQuery = true)
    Long countJustifiedBetweenDates(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Cuenta las justificaciones pendientes en un rango de fechas
     */
    @Query(value = "SELECT COUNT(*) FROM justifications j " +
            "JOIN attendance_records ar ON j.attendance_record_id = ar.id_attendance_record " +
            "WHERE TRUNC(ar.check_in) BETWEEN :startDate AND :endDate " +
            "AND (j.justification_text IS NULL OR TRIM(j.justification_text) = '')", nativeQuery = true)
    Long countNotJustifiedBetweenDates(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Cuenta el total de justificaciones en un rango de fechas
     */
    @Query(value = "SELECT COUNT(*) FROM justifications j " +
            "JOIN attendance_records ar ON j.attendance_record_id = ar.id_attendance_record " +
            "WHERE TRUNC(ar.check_in) BETWEEN :startDate AND :endDate", nativeQuery = true)
    Long countTotalBetweenDates(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}