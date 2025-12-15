package com.indra.attendance_control.services.impl;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.indra.attendance_control.models.AttendanceRecord;
import com.indra.attendance_control.models.Employee;
import com.indra.attendance_control.models.Justification;
import com.indra.attendance_control.models.WorkSchedule;
import com.indra.attendance_control.models.enums.AttendanceStatus;
import com.indra.attendance_control.repositories.IAttendanceRecordRepository;
import com.indra.attendance_control.repositories.IEmployeeRepository;
import com.indra.attendance_control.repositories.IJustificationRepository;
import com.indra.attendance_control.repositories.IWorkScheduleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AttendanceBatchServiceImpl {
    
    private final IEmployeeRepository employeeRepository;
    private final IAttendanceRecordRepository attendanceRecordRepository;
    private final IWorkScheduleRepository workScheduleRepository;
    private final IJustificationRepository justificationRepository;

    /**
     * Se ejecuta todos los días a las 00:00:00
     * Crea attendance records para el día actual
     */
    @Scheduled(cron = "0 0 0 * * ?")
    //@Scheduled(cron = "0 * * * * ?")
    @Transactional
    public void createDailyAttendanceRecords() {
        LocalDate today = LocalDate.now();
        DayOfWeek dayOfWeek = today.getDayOfWeek();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();

        List<Employee> activeEmployees = employeeRepository.findByEnabledTrue();

        List<WorkSchedule> todaySchedules = workScheduleRepository
                .findByDayOfWeekAndEnabledTrue(dayOfWeek);

        Set<Long> employeesWithSchedule = todaySchedules.stream()
                .map(ws -> ws.getEmployee().getIdEmployee())
                .collect(Collectors.toSet());

        List<AttendanceRecord> existingRecords = attendanceRecordRepository
                .findByCreatedAtBetween(startOfDay, endOfDay);

        Set<Long> employeesWithRecord = existingRecords.stream()
                .map(ar -> ar.getEmployee().getIdEmployee())
                .collect(Collectors.toSet());

        List<AttendanceRecord> recordsToCreate = new ArrayList<>();

        for (Employee employee : activeEmployees) {
            if (!employeesWithSchedule.contains(employee.getIdEmployee())) {
                continue;
            }

            if (employeesWithRecord.contains(employee.getIdEmployee())) {
                continue;
            }

            AttendanceRecord record = AttendanceRecord.builder()
                    .employee(employee)
                    .checkIn(null)
                    .checkOut(null)
                    .status(AttendanceStatus.ABSENT)
                    .build();

            recordsToCreate.add(record);
        }

        if (!recordsToCreate.isEmpty()) {
            attendanceRecordRepository.saveAll(recordsToCreate);
        }
    }

    /**
     * Crea justificaciones vacías para attendance records de AYER que necesitan justificación
     * Se ejecuta a las 00:01:00 todos los días
     */
    @Scheduled(cron = "0 1 0 * * ?")
    //@Scheduled(cron = "0 * * * * ?")
    @Transactional
    public void createDailyJustifications() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalDateTime startOfDay = yesterday.atStartOfDay();
        LocalDateTime endOfDay = yesterday.plusDays(1).atStartOfDay();

        List<AttendanceRecord> recordsNeedingJustification = 
            attendanceRecordRepository.findRecordsNeedingJustification(startOfDay, endOfDay);

        if (recordsNeedingJustification.isEmpty()) {
            return;
        }

        List<Long> attendanceRecordIds = recordsNeedingJustification.stream()
                .map(AttendanceRecord::getIdAttendanceRecord)
                .collect(Collectors.toList());

        List<Justification> existingJustifications = justificationRepository
                .findByAttendanceRecordIdIn(attendanceRecordIds);

        Set<Long> attendanceIdsWithJustification = existingJustifications.stream()
                .map(j -> j.getAttendanceRecord().getIdAttendanceRecord())
                .collect(Collectors.toSet());

        List<Justification> justificationsToCreate = new ArrayList<>();

        for (AttendanceRecord record : recordsNeedingJustification) {
            if (attendanceIdsWithJustification.contains(record.getIdAttendanceRecord())) {
                continue;
            }

            Justification justification = Justification.builder()
                    .attendanceRecord(record)
                    .justificationText(null)
                    .submittedAt(null)
                    .build();

            justificationsToCreate.add(justification);
        }

        if (!justificationsToCreate.isEmpty()) {
            justificationRepository.saveAll(justificationsToCreate);
        }
    }
}