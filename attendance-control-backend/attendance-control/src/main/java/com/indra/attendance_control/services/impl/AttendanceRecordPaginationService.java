package com.indra.attendance_control.services.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
import com.indra.attendance_control.commons.SortModel;
import com.indra.attendance_control.dtos.out.AttendanceRecordWithJustificationResponseDto;
import com.indra.attendance_control.dtos.out.JustificationSummaryResponseDto;
import com.indra.attendance_control.models.AttendanceRecord;
import com.indra.attendance_control.models.Employee;
import com.indra.attendance_control.models.Justification;
import com.indra.attendance_control.repositories.IJustificationRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AttendanceRecordPaginationService implements IPaginationCommons<AttendanceRecordWithJustificationResponseDto> {
    
    private final EntityManager entityManager;
    private final IJustificationRepository justificationRepository;
    
    private static final DateTimeFormatter DATETIME_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    @Override
    @Transactional(readOnly = true)
    public PageImpl<AttendanceRecordWithJustificationResponseDto> getPagination(PaginationModel paginationModel) {
        
        Integer page = paginationModel.getPageNumber();
        Integer rowsPerPage = paginationModel.getRowsPerPage();
        
        Pageable pageable = PageRequest.of(page, rowsPerPage);
        
        // Fecha límite: desde ayer hacia atrás
        LocalDateTime yesterday = LocalDate.now().minusDays(1).plusDays(1).atStartOfDay();
        
        // Construir WHERE clause con filtros
        StringBuilder whereClause = new StringBuilder("WHERE ar.createdAt < :yesterday ");
        
        if (paginationModel.getFilters() != null && !paginationModel.getFilters().isEmpty()) {
            for (FilterModel filter : paginationModel.getFilters()) {
                whereClause.append(buildFilterClause(filter));
            }
        }
        
        // SQL para datos (con LEFT JOIN FETCH para cargar empleado)
        String sql = "SELECT DISTINCT ar FROM AttendanceRecord ar " +
                    "LEFT JOIN FETCH ar.employee e " +
                    whereClause.toString() +
                    buildSortClause(paginationModel.getSorts());
        
        // SQL para count (con JOIN simple, sin FETCH)
        String sqlCount = "SELECT COUNT(DISTINCT ar.idAttendanceRecord) FROM AttendanceRecord ar " +
                        "JOIN ar.employee e " +
                        whereClause.toString();
        
        // Ejecutar query de datos
        TypedQuery<AttendanceRecord> query = entityManager.createQuery(sql, AttendanceRecord.class);
        query.setParameter("yesterday", yesterday);
        applyFilterParameters(query, paginationModel.getFilters());
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());
        
        List<AttendanceRecord> results = query.getResultList();
        
        // Ejecutar query de conteo
        TypedQuery<Long> queryCount = entityManager.createQuery(sqlCount, Long.class);
        queryCount.setParameter("yesterday", yesterday);
        applyFilterParameters(queryCount, paginationModel.getFilters());
        
        Long totalRegistros = queryCount.getSingleResult();
        
        // Obtener justificaciones para estos registros
        List<Long> attendanceRecordIds = results.stream()
                .map(AttendanceRecord::getIdAttendanceRecord)
                .collect(Collectors.toList());
        
        Map<Long, Justification> justificationMap = new java.util.HashMap<>();
        if (!attendanceRecordIds.isEmpty()) {
            List<Justification> justifications = justificationRepository
                    .findByAttendanceRecordIdIn(attendanceRecordIds);
            
            justificationMap = justifications.stream()
                    .collect(Collectors.toMap(
                        j -> j.getAttendanceRecord().getIdAttendanceRecord(),
                        j -> j
                    ));
        }
        
        // Convertir a DTOs
        Map<Long, Justification> finalJustificationMap = justificationMap;
        List<AttendanceRecordWithJustificationResponseDto> dtos = results.stream()
                .map(record -> buildDto(record, finalJustificationMap.get(record.getIdAttendanceRecord())))
                .collect(Collectors.toList());
        
        return new PageImpl<>(dtos, pageable, totalRegistros);
    }

    /**
     * Obtiene la paginación filtrada por un empleado específico
     */
    public PageImpl<AttendanceRecordWithJustificationResponseDto> getPaginationByEmployee(
            PaginationModel paginationModel, 
            Long employeeId) {
        
        // Agregar el employeeId como filtro
        List<FilterModel> filters = paginationModel.getFilters();
        
        if (filters == null) {
            filters = new ArrayList<>();
            paginationModel.setFilters(filters);
        }
        
        // Agregar el filtro de employeeId
        FilterModel employeeFilter = new FilterModel();
        employeeFilter.setField("employeeId");
        employeeFilter.setValue(employeeId.toString());
        
        filters.add(employeeFilter);
        
        // Llamar al método original de paginación
        return getPagination(paginationModel);
    }
    
    /**
     * Construye las cláusulas WHERE
     */
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
            
            case "employeeName":
                return "AND (LOWER(e.firstName) LIKE LOWER(:employeeName) OR LOWER(e.lastName) LIKE LOWER(:employeeName)) ";
            
            case "department":
                return "AND LOWER(e.department) LIKE LOWER(:department) ";
            
            case "status":
                return "AND ar.status = :status ";
            
            case "date":
                return "AND ar.createdAt >= :dateStart AND ar.createdAt < :dateEnd ";
            
            // Filtros de rango de fechas
            case "startDate":
                return "AND ar.checkIn >= :startDate ";
            
            case "endDate":
                return "AND ar.checkIn <= :endDate ";
            
            default:
                return "";
        }
    }
    
    private String buildSortClause(List<SortModel> sorts) {
        if (sorts == null || sorts.isEmpty()) {
            return "ORDER BY ar.createdAt DESC";
        }
        
        StringBuilder orderBy = new StringBuilder("ORDER BY ");
        
        for (int i = 0; i < sorts.size(); i++) {
            SortModel sort = sorts.get(i);
            String colName = sort.getColName();
            String direction = sort.getDirection() != null ? sort.getDirection() : "DESC";
            
            switch (colName) {
                case "attendanceDate":
                case "createdAt":
                    orderBy.append("ar.createdAt ").append(direction);
                    break;
                case "checkIn":
                    orderBy.append("ar.checkIn ").append(direction);
                    break;
                case "employeeCode":
                    orderBy.append("e.employeeCode ").append(direction);
                    break;
                case "employeeName":
                    orderBy.append("e.firstName ").append(direction);
                    break;
                case "status":
                    orderBy.append("ar.status ").append(direction);
                    break;
                default:
                    orderBy.append("ar.createdAt ").append(direction);
            }
            
            if (i < sorts.size() - 1) {
                orderBy.append(", ");
            }
        }
        
        return orderBy.toString();
    }
    
    /**
     * Aplica los parámetros de filtros
     */
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
                
                case "employeeName":
                    query.setParameter("employeeName", "%" + value + "%");
                    break;
                
                case "department":
                    query.setParameter("department", "%" + value + "%");
                    break;
                
                case "status":
                    query.setParameter("status", 
                        com.indra.attendance_control.models.enums.AttendanceStatus.valueOf(value));
                    break;
                
                case "date":
                    LocalDate date = LocalDate.parse(value);
                    LocalDateTime dateStart = date.atStartOfDay();
                    LocalDateTime dateEnd = date.plusDays(1).atStartOfDay();
                    query.setParameter("dateStart", dateStart);
                    query.setParameter("dateEnd", dateEnd);
                    break;
                
                // Parámetros de rango de fechas
                case "startDate":
                    try {
                        LocalDateTime startDate = LocalDateTime.parse(value, DateTimeFormatter.ISO_DATE_TIME);
                        query.setParameter("startDate", startDate);
                    } catch (Exception e) {
                        // Si falla el parse ISO, intentar como LocalDate
                        LocalDate startDateOnly = LocalDate.parse(value);
                        query.setParameter("startDate", startDateOnly.atStartOfDay());
                    }
                    break;
                
                case "endDate":
                    try {
                        LocalDateTime endDate = LocalDateTime.parse(value, DateTimeFormatter.ISO_DATE_TIME);
                        query.setParameter("endDate", endDate);
                    } catch (Exception e) {
                        // Si falla el parse ISO, intentar como LocalDate
                        LocalDate endDateOnly = LocalDate.parse(value);
                        query.setParameter("endDate", endDateOnly.atTime(23, 59, 59));
                    }
                    break;
            }
        }
    }
    
    private AttendanceRecordWithJustificationResponseDto buildDto(AttendanceRecord record, Justification justification) {
        Employee employee = record.getEmployee();
        
        JustificationSummaryResponseDto justificationDto = null;
        if (justification != null) {
            justificationDto = JustificationSummaryResponseDto.builder()
                    .idJustification(justification.getIdJustification())
                    .justificationText(justification.getJustificationText())
                    .submittedAt(justification.getSubmittedAt() != null ?
                        justification.getSubmittedAt().format(DATETIME_FORMATTER) : null)
                    .approved(justification.getApproved())
                    .build();
        }
        
        return AttendanceRecordWithJustificationResponseDto.builder()
                .employeeId(employee.getIdEmployee())
                .employeeCode(employee.getEmployeeCode())
                .employeeName(employee.getFirstName() + " " + employee.getLastName())
                .department(employee.getDepartment())
                .idAttendanceRecord(record.getIdAttendanceRecord())
                .attendanceDate(record.getCreatedAt() != null ?
                    record.getCreatedAt().toLocalDate().toString() : null)
                .checkIn(record.getCheckIn() != null ?
                    record.getCheckIn().format(DATETIME_FORMATTER) : null)
                .checkOut(record.getCheckOut() != null ?
                    record.getCheckOut().format(DATETIME_FORMATTER) : null)
                .status(record.getStatus())
                .justification(justificationDto)
                .createdAt(record.getCreatedAt() != null ?
                    record.getCreatedAt().format(DATETIME_FORMATTER) : null)
                .updatedAt(record.getUpdatedAt() != null ?
                    record.getUpdatedAt().format(DATETIME_FORMATTER) : null)
                .build();
    }
}