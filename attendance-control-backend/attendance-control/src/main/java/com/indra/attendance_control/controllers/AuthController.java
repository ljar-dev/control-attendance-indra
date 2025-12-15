package com.indra.attendance_control.controllers;

import org.springframework.security.core.Authentication;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.indra.attendance_control.dtos.in.ChangePasswordRequestDto;
import com.indra.attendance_control.dtos.in.FirstLoginPasswordRequestDto;
import com.indra.attendance_control.dtos.in.LoginRequestDto;
import com.indra.attendance_control.dtos.out.MessageResponseDto;
import com.indra.attendance_control.dtos.out.TokenResponseDto;
import com.indra.attendance_control.exceptions.ValidatedRequestException;
import com.indra.attendance_control.jwt.JwtUtil;
import com.indra.attendance_control.models.Employee;
import com.indra.attendance_control.models.User;
import com.indra.attendance_control.repositories.IEmployeeRepository;
import com.indra.attendance_control.repositories.IUserRepository;
import com.indra.attendance_control.services.interfaces.IUserService;
import org.springframework.security.core.AuthenticationException;
import org.springframework.http.MediaType;
import jakarta.validation.Valid;

@RestController
@RequestMapping(value = "/auth", produces = MediaType.APPLICATION_JSON_VALUE)
public class AuthController {

    private AuthenticationManager authenticationManager;
    private JwtUtil jwtUtil;
    private IUserService userService;
    private IEmployeeRepository employeeRepository;
    private IUserRepository userRepository;

    public AuthController(JwtUtil jwtUtil,
            AuthenticationManager authenticationManage,
            IUserService userService,
            IEmployeeRepository employeeRepository,
            IUserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManage;
        this.userService = userService;
        this.employeeRepository = employeeRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponseDto> login(@RequestBody LoginRequestDto loginRequest) {
        try {
            // Autenticar
            Authentication auth = this.authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()));

            // Buscar usuario y validar si está habilitado
            User user = userRepository.getByUserName(loginRequest.getUsername())
                    .orElseThrow(() -> new ValidatedRequestException("User not found"));

            if (!user.isEnabled()) {
                throw new ValidatedRequestException("Usuario deshabilitado. Contacte al administrador");
            }

            // Extraer roles
            List<String> roles = auth.getAuthorities().stream()
                    .map(r -> r.getAuthority())
                    .toList();

            // Buscar empleado
            Employee employee = employeeRepository.findByUserUsername(loginRequest.getUsername())
                    .orElseThrow(() -> new ValidatedRequestException("Employee not found for user"));

            // Generar token CON empleado
            String token = jwtUtil.generateToken(loginRequest.getUsername(), roles, employee);

            return ResponseEntity.ok(new TokenResponseDto(token));

        } catch (AuthenticationException e) {
            throw new ValidatedRequestException("Credenciales inválidas");
        } catch (ValidatedRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new ValidatedRequestException("Error al procesar el login: " + e.getMessage());
        }
    }

    @PostMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MessageResponseDto> changePassword(
            @Valid @RequestBody ChangePasswordRequestDto request,
            Authentication authentication) {

        String username = authentication.getName();
        userService.changePassword(username, request.getOldPassword(), request.getNewPassword());

        return ResponseEntity.ok(
                MessageResponseDto.builder()
                        .message("Password changed successfully")
                        .build());
    }

    @PostMapping("/first-login/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TokenResponseDto> changePasswordFirstLogin(
            @Valid @RequestBody FirstLoginPasswordRequestDto request,
            Authentication authentication) {

        String username = authentication.getName();

        // Verificar que el usuario debe cambiar contraseña
        User user = userService.getByUserName(username);
        if (!user.isMustChangePassword()) {
            throw new ValidatedRequestException("User already changed password");
        }

        // Cambiar contraseña
        userService.changePasswordFirstLogin(username, request.getNewPassword());

        // Buscar empleado
        Employee employee = employeeRepository.findByUserUsername(username)
                .orElseThrow(() -> new ValidatedRequestException("Employee not found"));

        // Extraer roles
        List<String> roles = authentication.getAuthorities().stream()
                .map(r -> r.getAuthority())
                .toList();

        // Generar nuevo token actualizado
        String newToken = jwtUtil.generateToken(username, roles, employee);

        return ResponseEntity.ok(new TokenResponseDto(newToken));
    }
}