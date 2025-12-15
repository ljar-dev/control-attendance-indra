package com.indra.attendance_control.repositories;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.indra.attendance_control.models.Employee;

public interface IEmployeeRepository extends JpaRepository<Employee,Long>{
    
    /**
     * Obtiene el último empleado registrado según el identificador
     */
    @Query("SELECT e FROM Employee e ORDER BY e.idEmployee DESC LIMIT 1")
    Optional<Employee> findLastEmployee();

    /**
     * Obtiene un empleado a partir del nombre de usuario asociado
     */
    Optional<Employee> findByUserUsername(String username);

    /**
     * Obtiene la lista de empleados habilitados con su información de usuario
     */
    @Query("SELECT DISTINCT e FROM Employee e " +
        "LEFT JOIN FETCH e.user " +
        "WHERE e.enabled = true")
    List<Employee> findByEnabledTrue();
}
