package com.indra.attendance_control.services.impl;

import java.security.SecureRandom;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.indra.attendance_control.dtos.in.EmployeeRequestDto;
import com.indra.attendance_control.dtos.in.WorkScheduleRequestDto;
import com.indra.attendance_control.dtos.out.EmployeeResponseDto;
import com.indra.attendance_control.dtos.out.WorkScheduleSummaryResponseDto;
import com.indra.attendance_control.exceptions.ResourceNotFoundException;
import com.indra.attendance_control.exceptions.ValidatedRequestException;
import com.indra.attendance_control.models.Employee;
import com.indra.attendance_control.models.Role;
import com.indra.attendance_control.models.User;
import com.indra.attendance_control.models.UserRole;
import com.indra.attendance_control.models.WorkSchedule;
import com.indra.attendance_control.repositories.IEmployeeRepository;
import com.indra.attendance_control.repositories.IRoleRepository;
import com.indra.attendance_control.repositories.IUserRepository;
import com.indra.attendance_control.repositories.IUserRoleRepository;
import com.indra.attendance_control.repositories.IWorkScheduleRepository;
import com.indra.attendance_control.services.interfaces.IEmployeeService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements IEmployeeService {

    private final IEmployeeRepository employeeRepository;
    private final IUserRepository userRepository;
    private final IUserRoleRepository userRoleRepository;
    private final IRoleRepository roleRepository;
    private final IWorkScheduleRepository workScheduleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public EmployeeResponseDto createEmployee(EmployeeRequestDto request) {

        // 1. Generar employeeCode único
        String employeeCode = generateEmployeeCode();

        // 2. Generar username único
        String username = generateUsername(request.getFirstName(), request.getLastName());

        // 3. Generar contraseña temporal
        String temporaryPassword = generateTemporaryPassword();

        // 4. Crear el User
        User user = createUser(username, temporaryPassword);

        // 5. Crear el Employee
        Employee employee = Employee.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .employeeCode(employeeCode)
                .department(request.getDepartment())
                .position(request.getPosition())
                .hireDate(request.getHireDate())
                .user(user)
                .enabled(request.isEnabled())
                .firstLogin(true)
                .build();

        employee = employeeRepository.save(employee);

        // 6. Crear los WorkSchedules
        List<WorkSchedule> schedules = createWorkSchedules(employee, request.getWorkSchedules());

        // 7. Retornar respuesta con credenciales temporales
        return buildEmployeeResponse(employee, schedules, temporaryPassword);
    }

    /**
     * Genera código de empleado secuencial: E0001, E0002, E0003...
     */
    private String generateEmployeeCode() {
        String lastCode = employeeRepository.findLastEmployee()
                .map(Employee::getEmployeeCode)
                .orElse("E0000");

        // Extraer número y sumar 1
        int number = Integer.parseInt(lastCode.substring(1)) + 1;

        // Formatear con ceros a la izquierda (E0001, E0002, etc.)
        return String.format("E%04d", number);
    }

    /**
     * Genera username: primeras 2 letras del nombre + apellido completo
     * Ejemplos:
     * - John Doe -> jodoe
     * - María García -> magarcia
     * Si ya existe, agrega número: jodoe1, jodoe2, etc.
     */
    private String generateUsername(String firstName, String lastName) {
        // Normalizar: quitar acentos y convertir a minúsculas
        String normalizedFirstName = normalizeString(firstName);
        String normalizedLastName = normalizeString(lastName);

        // Tomar primeras 2 letras del nombre + apellido completo
        String prefix = normalizedFirstName.substring(0, Math.min(2, normalizedFirstName.length()));
        String baseUsername = (prefix + normalizedLastName).toLowerCase();

        // Si ya existe, agregar número secuencial
        String username = baseUsername;
        int counter = 1;

        while (userRepository.existsByUsername(username)) {
            username = baseUsername + counter;
            counter++;
        }

        return username;
    }

    /**
     * Normaliza string: quita acentos, caracteres especiales y espacios
     */
    private String normalizeString(String input) {
        if (input == null)
            return "";

        // Quitar acentos
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        normalized = normalized.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");

        // Quitar espacios y caracteres especiales, dejar solo letras
        normalized = normalized.replaceAll("[^a-zA-Z]", "");

        return normalized;
    }

    /**
     * Genera contraseña temporal aleatoria de 8 caracteres
     * Formato: 2 mayúsculas + 4 minúsculas + 2 números
     */
    private String generateTemporaryPassword() {
        String upperCase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lowerCase = "abcdefghijklmnopqrstuvwxyz";
        String numbers = "0123456789";

        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();

        // 2 mayúsculas
        for (int i = 0; i < 2; i++) {
            password.append(upperCase.charAt(random.nextInt(upperCase.length())));
        }

        // 4 minúsculas
        for (int i = 0; i < 4; i++) {
            password.append(lowerCase.charAt(random.nextInt(lowerCase.length())));
        }

        // 2 números
        for (int i = 0; i < 2; i++) {
            password.append(numbers.charAt(random.nextInt(numbers.length())));
        }

        // Mezclar caracteres
        List<Character> chars = password.chars()
                .mapToObj(c -> (char) c)
                .collect(Collectors.toList());
        Collections.shuffle(chars, random);

        return chars.stream()
                .map(String::valueOf)
                .collect(Collectors.joining());
    }

    private User createUser(String username, String password) {
        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .enabled(true)
                .mustChangePassword(true)
                .build();

        user = userRepository.save(user);

        // Asignar rol EMPLOYEE
        Role employeeRole = roleRepository.findByName("EMPLOYEE")
                .orElseThrow(() -> new ResourceNotFoundException("Role EMPLOYEE not found"));

        UserRole userRole = UserRole.builder()
                .user(user)
                .role(employeeRole)
                .build();

        userRoleRepository.save(userRole);

        return user;
    }

    private List<WorkSchedule> createWorkSchedules(Employee employee, List<WorkScheduleRequestDto> scheduleDtos) {
        List<WorkSchedule> schedules = new ArrayList<>();

        for (WorkScheduleRequestDto dto : scheduleDtos) {
            if (dto.getEndTime().isBefore(dto.getStartTime())) {
                throw new ValidatedRequestException("End time cannot be before start time");
            }

            WorkSchedule schedule = WorkSchedule.builder()
                    .employee(employee)
                    .dayOfWeek(dto.getDayOfWeek())
                    .startTime(dto.getStartTime())
                    .endTime(dto.getEndTime())
                    .enabled(dto.isEnabled())
                    .build();

            schedules.add(workScheduleRepository.save(schedule));
        }

        return schedules;
    }

    private EmployeeResponseDto buildEmployeeResponse(
            Employee employee,
            List<WorkSchedule> schedules,
            String temporaryPassword) {

        return EmployeeResponseDto.builder()
                .idEmployee(employee.getIdEmployee())
                .firstName(employee.getFirstName())
                .lastName(employee.getLastName())
                .employeeCode(employee.getEmployeeCode())
                .department(employee.getDepartment())
                .position(employee.getPosition())
                .hireDate(employee.getHireDate())
                .active(employee.isEnabled())
                .userId(employee.getUser().getId())
                .username(employee.getUser().getUsername())
                .temporaryPassword(temporaryPassword)
                .mustChangePassword(employee.getUser().isMustChangePassword())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponseDto getEmployeeById(Long id) {

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));

        List<WorkSchedule> schedules = workScheduleRepository.findByEmployeeIdAndEnabledTrue(employee.getIdEmployee());

        return buildEmployeeResponseWithSchedules(employee, schedules);
    }

    @Override
    @Transactional
    public EmployeeResponseDto updateEmployee(Long id, EmployeeRequestDto request) {

        // Buscar el empleado
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));

        // Actualizar datos básicos (SIN tocar employeeCode)
        employee.setFirstName(request.getFirstName());
        employee.setLastName(request.getLastName());
        employee.setDepartment(request.getDepartment());
        employee.setPosition(request.getPosition());
        employee.setHireDate(request.getHireDate());
        employee.setEnabled(request.isEnabled());
        employee.setUpdatedAt(LocalDateTime.now());

        // NO actualizar el employeeCode - es inmutable

        // Actualizar horarios de trabajo
        // Primero, obtener los horarios existentes
        List<WorkSchedule> existingSchedules = workScheduleRepository
                .findByEmployeeIdAndEnabledTrue(employee.getIdEmployee());

        // Eliminar horarios existentes
        if (!existingSchedules.isEmpty()) {
            workScheduleRepository.deleteAll(existingSchedules);
        }

        // Crear nuevos horarios
        List<WorkSchedule> newSchedules = new ArrayList<>();
        if (request.getWorkSchedules() != null && !request.getWorkSchedules().isEmpty()) {
            for (WorkScheduleRequestDto scheduleDto : request.getWorkSchedules()) {
                if (scheduleDto.getEndTime().isBefore(scheduleDto.getStartTime())) {
                    throw new ValidatedRequestException("End time cannot be before start time");
                }

                WorkSchedule schedule = WorkSchedule.builder()
                        .employee(employee)
                        .dayOfWeek(scheduleDto.getDayOfWeek())
                        .startTime(scheduleDto.getStartTime())
                        .endTime(scheduleDto.getEndTime())
                        .enabled(scheduleDto.isEnabled())
                        .build();

                newSchedules.add(schedule);
            }

            workScheduleRepository.saveAll(newSchedules);
        }

        // Guardar empleado actualizado
        Employee updatedEmployee = employeeRepository.save(employee);

        return mapToResponseDto(updatedEmployee);
    }

    @Override
    @Transactional
    public EmployeeResponseDto toggleEmployeeStatus(Long id) {
        
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));

        User user = employee.getUser();
        if (user == null) {
            throw new ResourceNotFoundException("User not found for employee: " + id);
        }

        boolean newStatus = !employee.isEnabled();
        
        employee.setEnabled(newStatus);
        employee.setUpdatedAt(LocalDateTime.now());
        
        user.setEnabled(newStatus);
        
        employeeRepository.save(employee);
        userRepository.save(user);

        return mapToResponseDto(employee);
    }

    /**
     * Mapea Employee a EmployeeResponseDto
     */
    private EmployeeResponseDto mapToResponseDto(Employee employee) {
        return EmployeeResponseDto.builder()
                .idEmployee(employee.getIdEmployee())
                .employeeCode(employee.getEmployeeCode())
                .firstName(employee.getFirstName())
                .lastName(employee.getLastName())
                .department(employee.getDepartment())
                .position(employee.getPosition())
                .hireDate(employee.getHireDate())
                .active(employee.isEnabled())
                .userId(employee.getUser() != null ? employee.getUser().getId() : null)
                .username(employee.getUser() != null ? employee.getUser().getUsername() : null)
                .mustChangePassword(employee.getUser() != null ? employee.getUser().isMustChangePassword() : false)
                .build();
    }

    private EmployeeResponseDto buildEmployeeResponseWithSchedules(
            Employee employee,
            List<WorkSchedule> schedules) {

        return EmployeeResponseDto.builder()
                .idEmployee(employee.getIdEmployee())
                .firstName(employee.getFirstName())
                .lastName(employee.getLastName())
                .employeeCode(employee.getEmployeeCode())
                .department(employee.getDepartment())
                .position(employee.getPosition())
                .hireDate(employee.getHireDate())
                .active(employee.isEnabled())
                .userId(employee.getUser().getId())
                .username(employee.getUser().getUsername())
                .temporaryPassword(null)
                .mustChangePassword(employee.getUser().isMustChangePassword())
                .workSchedules(schedules.stream()
                        .map(this::mapToWorkScheduleDto)
                        .collect(Collectors.toList()))
                .build();
    }

    private WorkScheduleSummaryResponseDto mapToWorkScheduleDto(WorkSchedule schedule) {
        return WorkScheduleSummaryResponseDto.builder()
                .idWorkSchedule(schedule.getIdWorkSchedule())
                .dayOfWeek(schedule.getDayOfWeek())
                .startTime(schedule.getStartTime())
                .endTime(schedule.getEndTime())
                .enabled(schedule.isEnabled())
                .build();
    }
}