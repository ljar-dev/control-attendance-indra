package com.indra.attendance_control.services.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.indra.attendance_control.commons.FilterModel;
import com.indra.attendance_control.commons.IPaginationCommons;
import com.indra.attendance_control.commons.PaginationModel;
import com.indra.attendance_control.dtos.out.EmployeeListResponseDto;
import com.indra.attendance_control.dtos.out.WorkScheduleSummaryResponseDto;
import com.indra.attendance_control.models.Employee;
import com.indra.attendance_control.models.WorkSchedule;
import com.indra.attendance_control.repositories.IWorkScheduleRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmployeePaginationService implements IPaginationCommons<EmployeeListResponseDto> {
    private final EntityManager entityManager;
    private final IWorkScheduleRepository workScheduleRepository;
    
    private static final java.time.format.DateTimeFormatter DATETIME_FORMATTER = 
        java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    @Override
    @Transactional(readOnly = true)
    public PageImpl<EmployeeListResponseDto> getPagination(PaginationModel paginationModel) {
        
        Integer page = paginationModel.getPageNumber();
        Integer rowsPerPage = paginationModel.getRowsPerPage();
        
        Pageable pageable = PageRequest.of(page, rowsPerPage);
        
        //  Construir WHERE clause con filtros
        StringBuilder whereClause = new StringBuilder("WHERE 1=1 ");
        
        if (paginationModel.getFilters() != null && !paginationModel.getFilters().isEmpty()) {
            for (FilterModel filter : paginationModel.getFilters()) {
                whereClause.append(buildFilterClause(filter));
            }
        }
        
        //  SQL para datos (con LEFT JOIN FETCH para cargar usuario)
        String sql = "SELECT DISTINCT e FROM Employee e " +
                     "LEFT JOIN FETCH e.user u " +
                     whereClause.toString() +
                     buildSortClause(paginationModel.getSorts());
        
        //  SQL para count (con JOIN simple, sin FETCH)
        String sqlCount = "SELECT COUNT(DISTINCT e.idEmployee) FROM Employee e " +
                          "JOIN e.user u " +
                          whereClause.toString();
        
        // Ejecutar query de datos
        TypedQuery<Employee> query = entityManager.createQuery(sql, Employee.class);
        applyFilterParameters(query, paginationModel.getFilters());
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());
        
        List<Employee> results = query.getResultList();
        
        // Ejecutar query de conteo
        TypedQuery<Long> queryCount = entityManager.createQuery(sqlCount, Long.class);
        applyFilterParameters(queryCount, paginationModel.getFilters());
        
        Long totalRegistros = queryCount.getSingleResult();
        
        // Obtener work schedules para estos empleados
        List<Long> employeeIds = results.stream()
                .map(Employee::getIdEmployee)
                .collect(Collectors.toList());
        
        Map<Long, List<WorkSchedule>> schedulesMap = new java.util.HashMap<>();
        if (!employeeIds.isEmpty()) {
            List<WorkSchedule> schedules = workScheduleRepository.findByEmployeeIdIn(employeeIds);
            
            schedulesMap = schedules.stream()
                    .collect(Collectors.groupingBy(ws -> ws.getEmployee().getIdEmployee()));
        }
        
        // Convertir a DTOs
        Map<Long, List<WorkSchedule>> finalSchedulesMap = schedulesMap;
        List<EmployeeListResponseDto> dtos = results.stream()
                .map(employee -> buildDto(employee, finalSchedulesMap.getOrDefault(employee.getIdEmployee(), new ArrayList<>())))
                .collect(Collectors.toList());
        
        return new PageImpl<>(dtos, pageable, totalRegistros);
    }
    
    private String buildFilterClause(FilterModel filter) {
        String field = filter.getField();
        String value = filter.getValue();
        
        if (value == null || value.trim().isEmpty()) {
            return "";
        }
        
        switch (field) {
            case "employeeId":
                return "AND e.idEmployee = :employeeId ";
            case "employeeCode":
                return "AND LOWER(e.employeeCode) LIKE LOWER(:employeeCode) ";
            case "firstName":
                return "AND LOWER(e.firstName) LIKE LOWER(:firstName) ";
            case "lastName":
                return "AND LOWER(e.lastName) LIKE LOWER(:lastName) ";
            case "fullName":
                return "AND (LOWER(e.firstName) LIKE LOWER(:fullName) OR LOWER(e.lastName) LIKE LOWER(:fullName)) ";
            case "department":
                return "AND LOWER(e.department) LIKE LOWER(:department) ";
            case "position":
                return "AND LOWER(e.position) LIKE LOWER(:position) ";
            case "enabled":
                return "AND e.enabled = :enabled ";
            case "username":
                return "AND LOWER(u.username) LIKE LOWER(:username) ";
            default:
                return "";
        }
    }
    
    private String buildSortClause(List<com.indra.attendance_control.commons.SortModel> sorts) {
        if (sorts == null || sorts.isEmpty()) {
            return "ORDER BY e.employeeCode ASC";
        }
        
        StringBuilder orderBy = new StringBuilder("ORDER BY ");
        
        for (int i = 0; i < sorts.size(); i++) {
            com.indra.attendance_control.commons.SortModel sort = sorts.get(i);
            String colName = sort.getColName();
            String direction = sort.getDirection() != null ? sort.getDirection() : "ASC";
            
            switch (colName) {
                case "employeeCode":
                    orderBy.append("e.employeeCode ").append(direction);
                    break;
                case "firstName":
                    orderBy.append("e.firstName ").append(direction);
                    break;
                case "lastName":
                    orderBy.append("e.lastName ").append(direction);
                    break;
                case "department":
                    orderBy.append("e.department ").append(direction);
                    break;
                case "position":
                    orderBy.append("e.position ").append(direction);
                    break;
                case "hireDate":
                    orderBy.append("e.hireDate ").append(direction);
                    break;
                default:
                    orderBy.append("e.employeeCode ").append(direction);
            }
            
            if (i < sorts.size() - 1) {
                orderBy.append(", ");
            }
        }
        
        return orderBy.toString();
    }
    
    private void applyFilterParameters(TypedQuery<?> query, List<FilterModel> filters) {
        if (filters == null) return;
        
        for (FilterModel filter : filters) {
            String field = filter.getField();
            String value = filter.getValue();
            
            if (value == null || value.trim().isEmpty()) {
                continue;
            }
            
            switch (field) {
                case "employeeId":
                    query.setParameter("employeeId", Long.parseLong(value));
                    break;
                case "employeeCode":
                    query.setParameter("employeeCode", "%" + value + "%");
                    break;
                case "firstName":
                    query.setParameter("firstName", "%" + value + "%");
                    break;
                case "lastName":
                    query.setParameter("lastName", "%" + value + "%");
                    break;
                case "fullName":
                    query.setParameter("fullName", "%" + value + "%");
                    break;
                case "department":
                    query.setParameter("department", "%" + value + "%");
                    break;
                case "position":
                    query.setParameter("position", "%" + value + "%");
                    break;
                case "enabled":
                    query.setParameter("enabled", Boolean.parseBoolean(value));
                    break;
                case "username":
                    query.setParameter("username", "%" + value + "%");
                    break;
            }
        }
    }
    
    private EmployeeListResponseDto buildDto(Employee employee, List<WorkSchedule> schedules) {
        
        // Convertir horarios
        List<WorkScheduleSummaryResponseDto> scheduleDtos = schedules.stream()
                .map(ws -> WorkScheduleSummaryResponseDto.builder()
                        .idWorkSchedule(ws.getIdWorkSchedule())
                        .dayOfWeek(ws.getDayOfWeek())
                        .startTime(ws.getStartTime())
                        .endTime(ws.getEndTime())
                        .enabled(ws.isEnabled())
                        .build())
                .collect(Collectors.toList());
        
        return EmployeeListResponseDto.builder()
                .idEmployee(employee.getIdEmployee())
                .employeeCode(employee.getEmployeeCode())
                .firstName(employee.getFirstName())
                .lastName(employee.getLastName())
                .fullName(employee.getFirstName() + " " + employee.getLastName())
                .department(employee.getDepartment())
                .position(employee.getPosition())
                .hireDate(employee.getHireDate())
                .enabled(employee.isEnabled())
                .userId(employee.getUser() != null ? employee.getUser().getId() : null)
                .username(employee.getUser() != null ? employee.getUser().getUsername() : null)
                .userEnabled(employee.getUser() != null ? employee.getUser().isEnabled() : false)
                .mustChangePassword(employee.getUser() != null ? employee.getUser().isMustChangePassword() : false)
                .workSchedules(scheduleDtos)
                .createdAt(employee.getCreatedAt() != null ? 
                    employee.getCreatedAt().format(DATETIME_FORMATTER) : null)
                .updatedAt(employee.getUpdatedAt() != null ? 
                    employee.getUpdatedAt().format(DATETIME_FORMATTER) : null)
                .build();
    }
}
