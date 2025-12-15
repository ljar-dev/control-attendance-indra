package com.indra.attendance_control.services.impl;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.indra.attendance_control.exceptions.ResourceNotFoundException;
import com.indra.attendance_control.exceptions.ValidatedRequestException;
import com.indra.attendance_control.models.User;
import com.indra.attendance_control.repositories.IUserRepository;
import com.indra.attendance_control.repositories.IUserRoleRepository;
import com.indra.attendance_control.services.interfaces.IUserService;


@Service
public class UserServiceImpl implements  UserDetailsService, IUserService  {


    private final IUserRepository userRepository;
    private final IUserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(IUserRepository userRepository, 
                           IUserRoleRepository userRoleRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.passwordEncoder = passwordEncoder;                    
    }

    @Override
    public User getByUserName(String username) {
        return userRepository.getByUserName(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        User user = userRepository.getByUserName(username)
            .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        var userRols = userRoleRepository.getRolesByUser(user.getId());

        var userDetails = new UserDetailsImpl(user, userRols);
        return userDetails;

    }

    @Override
    public void changePassword(String username, String oldPassword, String newPassword) {
        User user = userRepository.getByUserName(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // Verificar contraseña antigua
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new ValidatedRequestException("Current password is incorrect");
        }
        
        // Actualizar contraseña
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setMustChangePassword(false);
        //user.setLastPasswordChange(LocalDateTime.now());
        
        userRepository.save(user);
    }
    @Override
    public void changePasswordFirstLogin(String username, String newPassword) {
        User user = userRepository.getByUserName(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // No validar contraseña antigua
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setMustChangePassword(false);
        
        userRepository.save(user);
    }
}
