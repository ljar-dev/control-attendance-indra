package com.indra.attendance_control.services.impl;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.indra.attendance_control.dtos.in.AttendanceRecordRequestDto;
import com.indra.attendance_control.dtos.out.AttendanceRecordResponseDto;
import com.indra.attendance_control.dtos.out.TodayAttendanceDto;
import com.indra.attendance_control.exceptions.ResourceNotFoundException;
import com.indra.attendance_control.exceptions.ValidatedRequestException;
import com.indra.attendance_control.models.AttendanceRecord;
import com.indra.attendance_control.models.Employee;
import com.indra.attendance_control.models.WorkSchedule;
import com.indra.attendance_control.models.enums.AttendanceStatus;
import com.indra.attendance_control.repositories.IAttendanceRecordRepository;
import com.indra.attendance_control.repositories.IEmployeeRepository;
import com.indra.attendance_control.repositories.IWorkScheduleRepository;
import com.indra.attendance_control.services.interfaces.IAttendanceRecordService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AttendanceRecordServiceImpl implements IAttendanceRecordService{
    private final IAttendanceRecordRepository attendanceRecordRepository;
    private final IEmployeeRepository employeeRepository;
    private final IWorkScheduleRepository workScheduleRepository;
    
    private static final DateTimeFormatter DATETIME_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    @Override
    @Transactional
    public AttendanceRecordResponseDto createAttendanceRecord(AttendanceRecordRequestDto request) {
        
        // 1. Validar que el empleado existe y está activo
        Employee employee = employeeRepository.findById(request.getIdEmployee())
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Employee not found with id: " + request.getIdEmployee()));
        
        if (!employee.isEnabled()) {
            throw new ValidatedRequestException("Employee is not active");
        }
        
        // 2. Validaciones de fechas
        validateCheckInCheckOut(request.getCheckIn(), request.getCheckOut());
        
        // 3. Validar que no exista ya un registro para este empleado en la misma fecha
        LocalDate targetDate = request.getCheckIn() != null 
            ? request.getCheckIn().toLocalDate() 
            : LocalDate.now(); 
        LocalDateTime startOfDay = targetDate.atStartOfDay();
        LocalDateTime endOfDay = targetDate.plusDays(1).atStartOfDay();
        
        if (attendanceRecordRepository.existsByEmployeeAndCheckInDateRange(
                employee.getIdEmployee(), startOfDay, endOfDay)) {
            throw new ValidatedRequestException(
                "Attendance record already exists for this employee on this date");
        }
        
        // 4. Determinar el status automáticamente si no viene en el request
        AttendanceStatus status = request.getStatus();
        if (status == null) {
            status = determineAttendanceStatus(employee, request.getCheckIn(), request.getCheckOut());
        }
        
        // 5. Crear el registro
        AttendanceRecord record = AttendanceRecord.builder()
                .employee(employee)
                .checkIn(request.getCheckIn())
                .checkOut(request.getCheckOut())
                .status(status)
                .build();
        
        record = attendanceRecordRepository.save(record);
        
        return buildResponse(record);
    }
    
    /**
     * Valida que checkOut no sea anterior a checkIn
     */
    private void validateCheckInCheckOut(LocalDateTime checkIn, LocalDateTime checkOut) {
        // Solo validar si ambos existen
        if (checkIn != null && checkOut != null && checkOut.isBefore(checkIn)) {
            throw new ValidatedRequestException("Check-out time cannot be before check-in time");
        }
        
        // Validar que no sea una fecha futura (solo si checkIn existe)
        if (checkIn != null && checkIn.isAfter(LocalDateTime.now())) {
            throw new ValidatedRequestException("Check-in time cannot be in the future");
        }
    }
    
    /**
     * Determina el status de asistencia automáticamente basado en el horario del empleado
     */
    private AttendanceStatus determineAttendanceStatus(
            Employee employee, 
            LocalDateTime checkIn, 
            LocalDateTime checkOut) {
        
        // Si no hay checkIn, es ausente
        if (checkIn == null) {
            return AttendanceStatus.ABSENT;
        }
        
        // Obtener el día de la semana del checkIn
        DayOfWeek dayOfWeek = checkIn.getDayOfWeek();
        
        // Buscar el horario del empleado para ese día
        Optional<WorkSchedule> scheduleOpt = workScheduleRepository
                .findByEmployeeAndDayOfWeekAndEnabledTrue(employee, dayOfWeek);
        
        if (scheduleOpt.isEmpty()) {
            return AttendanceStatus.ON_TIME; // Por defecto
        }
        
        WorkSchedule schedule = scheduleOpt.get();
        LocalTime scheduledStartTime = schedule.getStartTime();
        LocalTime scheduledEndTime = schedule.getEndTime();
        LocalTime actualCheckInTime = checkIn.toLocalTime();
        
        // Tolerancia de 15 minutos para tardanza
        LocalTime lateThreshold = scheduledStartTime.plusMinutes(15);
        
        // Determinar status basado en la hora de entrada
        if (actualCheckInTime.isAfter(lateThreshold)) {
            return AttendanceStatus.LATE;
        }
        
        // Si hay checkOut, verificar salida temprana
        if (checkOut != null) {
            LocalTime actualCheckOutTime = checkOut.toLocalTime();
            // Salida temprana: salir más de 30 minutos antes
            LocalTime earlyDepartureThreshold = scheduledEndTime.minusMinutes(30);
            
            if (actualCheckOutTime.isBefore(earlyDepartureThreshold)) {
                return AttendanceStatus.EARLY_DEPARTURE;
            }
        }
        
        return AttendanceStatus.ON_TIME;
    }
    
    /**
     * Construye el DTO de respuesta
     */
    private AttendanceRecordResponseDto buildResponse(AttendanceRecord record) {
        return AttendanceRecordResponseDto.builder()
                .idAttendanceRecord(record.getIdAttendanceRecord())
                .idEmployee(record.getEmployee().getIdEmployee())
                .checkIn(record.getCheckIn())
                .checkOut(record.getCheckOut())
                .status(record.getStatus())
                .createdAt(record.getCreatedAt() != null ? 
                    record.getCreatedAt().format(DATETIME_FORMATTER) : null)
                .updatedAt(record.getUpdatedAt() != null ? 
                    record.getUpdatedAt().format(DATETIME_FORMATTER) : null)
                .build();
    }

    //@Override
    @Transactional
    public AttendanceRecordResponseDto checkIn(Long employeeId) {
        
        // 1. Validar que el empleado existe y está activo
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Employee not found with id: " + employeeId));
        
        if (!employee.isEnabled()) {
            throw new ValidatedRequestException("Employee is not active");
        }
        
        // 2. Buscar el registro de asistencia de hoy (por fecha de creación)
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();
        
        // CAMBIO AQUÍ: Buscar por createdAt en lugar de checkIn
        AttendanceRecord record = attendanceRecordRepository
                .findByEmployeeAndCreatedAtDateRange(employeeId, startOfDay, endOfDay)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "No attendance record found for today. Please contact administrator."));
        
        // 3. Validar que no haya hecho check-in ya
        if (record.getCheckIn() != null) {
            throw new ValidatedRequestException(
                "Check-in already registered at: " + record.getCheckIn().format(DATETIME_FORMATTER));
        }
        
        // 4. Registrar check-in
        LocalDateTime checkInTime = LocalDateTime.now();
        record.setCheckIn(checkInTime);
        
        // 5. Calcular status automáticamente
        AttendanceStatus status = determineAttendanceStatus(employee, checkInTime, null);
        record.setStatus(status);
        record.setUpdatedAt(LocalDateTime.now());
        
        record = attendanceRecordRepository.save(record);
        
        return buildResponse(record);
    }

    //@Override
    @Transactional
    public AttendanceRecordResponseDto checkOut(Long employeeId) {
        
        // 1. Validar que el empleado existe
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Employee not found with id: " + employeeId));
        
        // 2. Buscar el registro de asistencia de hoy (por fecha de creación)
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();
        
        // CAMBIO AQUÍ: Buscar por createdAt en lugar de checkIn
        AttendanceRecord record = attendanceRecordRepository
                .findByEmployeeAndCreatedAtDateRange(employeeId, startOfDay, endOfDay)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "No attendance record found for today."));
        
        // 3. Validar que ya hizo check-in
        if (record.getCheckIn() == null) {
            throw new ValidatedRequestException(
                "Cannot check-out without check-in first.");
        }
        
        // 4. Validar que no haya hecho check-out ya
        if (record.getCheckOut() != null) {
            throw new ValidatedRequestException(
                "Check-out already registered at: " + record.getCheckOut().format(DATETIME_FORMATTER));
        }
        
        // 5. Registrar check-out
        LocalDateTime checkOutTime = LocalDateTime.now();
        record.setCheckOut(checkOutTime);
        
        // 6. Recalcular status con check-out
        AttendanceStatus status = determineAttendanceStatus(
            employee, 
            record.getCheckIn(), 
            checkOutTime
        );
        record.setStatus(status);
        record.setUpdatedAt(LocalDateTime.now());
        
        record = attendanceRecordRepository.save(record);
        
        return buildResponse(record);
    }


    /**
     * Marca check-in para el usuario autenticado (por username)
     */
    @Override
    @Transactional
    public AttendanceRecordResponseDto checkInByUsername(String username) {
        Employee employee = employeeRepository.findByUserUsername(username)
            .orElseThrow(() -> new RuntimeException("No hay empleado asociado a este usuario"));
        
        return checkIn(employee.getIdEmployee());
    }

    /**
     * Marca check-out para el usuario autenticado (por username)
     */
    @Override
    @Transactional
    public AttendanceRecordResponseDto checkOutByUsername(String username) {
        Employee employee = employeeRepository.findByUserUsername(username)
            .orElseThrow(() -> new RuntimeException("No hay empleado asociado a este usuario"));
        
        return checkOut(employee.getIdEmployee());
    }

    /**
     * Obtiene los registros de asistencia del día actual para un empleado
     */
    public TodayAttendanceDto getTodayAttendanceByEmployeeId(Long employeeId) {
        // Verificar que el empleado existe
        Employee employee = employeeRepository.findById(employeeId)
            .orElseThrow(() -> new RuntimeException("Empleado no encontrado con ID: " + employeeId));
        
        // Calcular inicio y fin del día actual
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        
        // Buscar registro de hoy usando rangos
        Optional<AttendanceRecord> todayRecord = attendanceRecordRepository
            .findTodayRecordByEmployeeId(employeeId, startOfDay, endOfDay);
        
        // Construir el DTO
        if (todayRecord.isPresent()) {
            AttendanceRecord record = todayRecord.get();
            return TodayAttendanceDto.builder()
                .employeeId(employee.getIdEmployee())
                .employeeName(employee.getFirstName() + " " + employee.getLastName())
                .checkIn(record.getCheckIn())
                .checkOut(record.getCheckOut())
                .hasCheckIn(record.getCheckIn() != null)
                .hasCheckOut(record.getCheckOut() != null)
                .status(record.getStatus() != null ? record.getStatus().name() : null)
                .build();
        } 
        else {
            // No hay registro hoy
            return TodayAttendanceDto.builder()
                .employeeId(employee.getIdEmployee())
                .employeeName(employee.getFirstName() + " " + employee.getLastName())
                .checkIn(null)
                .checkOut(null)
                .hasCheckIn(false)
                .hasCheckOut(false)
                .status(null)
                .build();
        }
    }

    /**
     * Obtiene los registros por username
     */
    @Override
    @Transactional(readOnly = true)
    public TodayAttendanceDto getTodayAttendanceByUsername(String username) {
        // Buscar el empleado por username del usuario
        Employee employee = employeeRepository.findByUserUsername(username)
            .orElseThrow(() -> new RuntimeException("No hay empleado asociado a este usuario"));
        
        return getTodayAttendanceByEmployeeId(employee.getIdEmployee());
    }
    
}
