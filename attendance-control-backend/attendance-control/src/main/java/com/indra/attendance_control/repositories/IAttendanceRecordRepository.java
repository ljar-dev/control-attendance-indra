package com.indra.attendance_control.repositories;

import com.indra.attendance_control.models.AttendanceRecord;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface IAttendanceRecordRepository extends JpaRepository<AttendanceRecord,Long>{
    /**
     * Verifica si existe un registro de asistencia para un empleado en un rango de fecha y hora
     */
    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END " +
        "FROM AttendanceRecord a " +
        "WHERE a.employee.id = :employeeId " +
        "AND a.checkIn >= :startOfDay " +
        "AND a.checkIn < :endOfDay")
    boolean existsByEmployeeAndCheckInDateRange(
        @Param("employeeId") Long employeeId, 
        @Param("startOfDay") LocalDateTime startOfDay,
        @Param("endOfDay") LocalDateTime endOfDay
    );

    /**
     * Obtiene el registro de asistencia de un empleado en un rango de fecha y hora
     */
    @Query("SELECT a FROM AttendanceRecord a " +
        "WHERE a.employee.id = :employeeId " +
        "AND a.createdAt >= :startOfDay " +
        "AND a.createdAt < :endOfDay")
    Optional<AttendanceRecord> findByEmployeeAndCreatedAtDateRange(
        @Param("employeeId") Long employeeId, 
        @Param("startOfDay") LocalDateTime startOfDay,
        @Param("endOfDay") LocalDateTime endOfDay
    );

    /**
     * Obtiene los registros de asistencia en un rango de fechas con la información del empleado
     */
    @Query("SELECT ar FROM AttendanceRecord ar " +
        "LEFT JOIN FETCH ar.employee " +
        "WHERE ar.createdAt >= :startDate AND ar.createdAt < :endDate")
    List<AttendanceRecord> findByCreatedAtBetween(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * Obtiene los registros que requieren justificación en un día específico
     */
    @Query("SELECT ar FROM AttendanceRecord ar " +
        "LEFT JOIN FETCH ar.employee " +
        "WHERE ar.createdAt >= :startOfDay " +
        "AND ar.createdAt < :endOfDay " +
        "AND ar.status IN ('ABSENT', 'LATE', 'EARLY_DEPARTURE', 'JUSTIFIED_ABSENCE', 'MEDICAL_LEAVE')")
    List<AttendanceRecord> findRecordsNeedingJustification(
        @Param("startOfDay") LocalDateTime startOfDay,
        @Param("endOfDay") LocalDateTime endOfDay
    );

    /**
     * Obtiene el registro de asistencia del día actual para un empleado
     */
    @Query("SELECT ar FROM AttendanceRecord ar " +
        "WHERE ar.employee.idEmployee = :employeeId " +
        "AND ar.checkIn >= :startOfDay " +
        "AND ar.checkIn < :endOfDay " +
        "ORDER BY ar.checkIn DESC")
    Optional<AttendanceRecord> findTodayRecordByEmployeeId(
        @Param("employeeId") Long employeeId, 
        @Param("startOfDay") LocalDateTime startOfDay,
        @Param("endOfDay") LocalDateTime endOfDay
    );

    /**
     * Cuenta los registros de asistencia agrupados por estado en un rango de fechas
     */
    @Query(value = "SELECT ar.status, COUNT(*) " +
                "FROM attendance_records ar " +
                "WHERE TRUNC(ar.check_in) BETWEEN :startDate AND :endDate " +
                "GROUP BY ar.status",
        nativeQuery = true)
    List<Object[]> countByStatusBetweenDates(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    /**
     * Cuenta el total de registros de asistencia en un rango de fechas
     */
    @Query(value = "SELECT COUNT(*) " +
                "FROM attendance_records ar " +
                "WHERE TRUNC(ar.check_in) BETWEEN :startDate AND :endDate",
        nativeQuery = true)
    Long countBetweenDates(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    /**
     * Cuenta los empleados únicos con registros de asistencia en un rango de fechas
     */
    @Query(value = "SELECT COUNT(DISTINCT ar.employee_id) " +
                "FROM attendance_records ar " +
                "WHERE TRUNC(ar.check_in) BETWEEN :startDate AND :endDate",
        nativeQuery = true)
    Long countDistinctEmployeesBetweenDates(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
}
