package com.indra.attendance_control.services.impl;

import com.indra.attendance_control.dtos.out.AttendanceGeneralReportDto;
import com.indra.attendance_control.models.enums.AttendanceStatus;
import com.indra.attendance_control.repositories.IAttendanceRecordRepository;
import com.indra.attendance_control.repositories.IJustificationRepository;
import com.indra.attendance_control.services.interfaces.IReportService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements IReportService {
    
    private final IAttendanceRecordRepository attendanceRecordRepository;
    private final IJustificationRepository justificationRepository;

    @Override
    @Transactional(readOnly = true)
    public AttendanceGeneralReportDto generateGeneralReport(LocalDate startDate, LocalDate endDate) {
        
        // 1. Obtener estadísticas de asistencia por estado
        AttendanceGeneralReportDto.AttendanceStatusStats attendanceStats = 
            calculateAttendanceStats(startDate, endDate);
        
        // 2. Obtener estadísticas de justificaciones
        AttendanceGeneralReportDto.JustificationStats justificationStats = 
            calculateJustificationStats(startDate, endDate);
        
        // 3. Obtener métricas generales
        Long totalEmployees = attendanceRecordRepository
            .countDistinctEmployeesBetweenDates(startDate, endDate);
        
        Long totalRecords = attendanceRecordRepository
            .countBetweenDates(startDate, endDate);
        
        // 4. Construir respuesta
        return AttendanceGeneralReportDto.builder()
            .attendanceStats(attendanceStats)
            .justificationStats(justificationStats)
            .startDate(startDate.toString())
            .endDate(endDate.toString())
            .totalEmployees(totalEmployees.intValue())
            .totalRecords(totalRecords.intValue())
            .build();
    }
    
    /**
     * Calcula estadísticas de asistencia por estado
     */
    private AttendanceGeneralReportDto.AttendanceStatusStats calculateAttendanceStats(
            LocalDate startDate, LocalDate endDate) {
        
        List<Object[]> statusCounts = attendanceRecordRepository
        .countByStatusBetweenDates(startDate, endDate);
    
        Map<AttendanceStatus, Long> countMap = new HashMap<>();
        long totalCount = 0;
        
        for (Object[] row : statusCounts) {
            // Con Native Query, el status viene como String
            String statusString = (String) row[0];
            Long count = ((Number) row[1]).longValue(); // ← Puede venir como Integer o Long
            
            // Convertir String a Enum
            try {
                AttendanceStatus status = AttendanceStatus.valueOf(statusString);
                countMap.put(status, count);
                totalCount += count;
            } catch (IllegalArgumentException e) {
            }
        }
        
        // Obtener conteos individuales (0 si no existe)
        long onTime = countMap.getOrDefault(AttendanceStatus.ON_TIME, 0L);
        long late = countMap.getOrDefault(AttendanceStatus.LATE, 0L);
        long absent = countMap.getOrDefault(AttendanceStatus.ABSENT, 0L);
        long earlyDeparture = countMap.getOrDefault(AttendanceStatus.EARLY_DEPARTURE, 0L);
        
        // Calcular porcentajes
        double onTimePercentage = totalCount > 0 ? (onTime * 100.0 / totalCount) : 0.0;
        double latePercentage = totalCount > 0 ? (late * 100.0 / totalCount) : 0.0;
        double absentPercentage = totalCount > 0 ? (absent * 100.0 / totalCount) : 0.0;
        double earlyDeparturePercentage = totalCount > 0 ? (earlyDeparture * 100.0 / totalCount) : 0.0;
        
        return AttendanceGeneralReportDto.AttendanceStatusStats.builder()
            .onTime(onTime)
            .late(late)
            .absent(absent)
            .earlyDeparture(earlyDeparture)
            .onTimePercentage(Math.round(onTimePercentage * 100.0) / 100.0)
            .latePercentage(Math.round(latePercentage * 100.0) / 100.0)
            .absentPercentage(Math.round(absentPercentage * 100.0) / 100.0)
            .earlyDeparturePercentage(Math.round(earlyDeparturePercentage * 100.0) / 100.0)
            .build();
    }
    
    /**
     * Calcula estadísticas de justificaciones
     */
    private AttendanceGeneralReportDto.JustificationStats calculateJustificationStats(
            LocalDate startDate, LocalDate endDate) {
        
        Long totalRequired = justificationRepository
            .countTotalBetweenDates(startDate, endDate);
        
        Long justified = justificationRepository
            .countJustifiedBetweenDates(startDate, endDate);
        
        Long notJustified = justificationRepository
            .countNotJustifiedBetweenDates(startDate, endDate);
        
        // Calcular porcentajes
        double justifiedPercentage = totalRequired > 0 ? (justified * 100.0 / totalRequired) : 0.0;
        double notJustifiedPercentage = totalRequired > 0 ? (notJustified * 100.0 / totalRequired) : 0.0;
        
        return AttendanceGeneralReportDto.JustificationStats.builder()
            .totalRequired(totalRequired)
            .justified(justified)
            .notJustified(notJustified)
            .justifiedPercentage(Math.round(justifiedPercentage * 100.0) / 100.0)
            .notJustifiedPercentage(Math.round(notJustifiedPercentage * 100.0) / 100.0)
            .build();
    }
}