package com.indra.attendance_control.repositories;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.indra.attendance_control.models.Employee;
import com.indra.attendance_control.models.WorkSchedule;

public interface IWorkScheduleRepository extends JpaRepository<WorkSchedule,Long>{

    // Nuevo método para buscar horario por día de la semana
    Optional<WorkSchedule> findByEmployeeAndDayOfWeekAndEnabledTrue(
        Employee employee, 
        DayOfWeek dayOfWeek
    );

    @Query("SELECT ws FROM WorkSchedule ws " +
           "LEFT JOIN FETCH ws.employee " +
           "WHERE ws.dayOfWeek = :dayOfWeek AND ws.enabled = true")
    List<WorkSchedule> findByDayOfWeekAndEnabledTrue(@Param("dayOfWeek") DayOfWeek dayOfWeek);

    @Query("SELECT ws FROM WorkSchedule ws " +
       "LEFT JOIN FETCH ws.employee " +
       "WHERE ws.employee.idEmployee IN :employeeIds " +
       "ORDER BY ws.employee.idEmployee, ws.dayOfWeek")
    List<WorkSchedule> findByEmployeeIdIn(@Param("employeeIds") List<Long> employeeIds);

    /**
     * Busca todos los horarios de trabajo de un empleado
     */
    @Query("SELECT ws FROM WorkSchedule ws WHERE ws.employee.idEmployee = :employeeId")
    List<WorkSchedule> findByEmployeeId(@Param("employeeId") Long employeeId);

    @Query("SELECT ws FROM WorkSchedule ws WHERE ws.employee.idEmployee = :employeeId AND ws.enabled = true")
    List<WorkSchedule> findByEmployeeIdAndEnabledTrue(@Param("employeeId") Long employeeId);

    
}
