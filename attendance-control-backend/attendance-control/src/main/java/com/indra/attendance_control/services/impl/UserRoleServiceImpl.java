package com.indra.attendance_control.services.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.indra.attendance_control.models.UserRole;
import com.indra.attendance_control.repositories.IUserRoleRepository;
import com.indra.attendance_control.services.interfaces.IUserRoleService;

@Service
public class UserRoleServiceImpl implements IUserRoleService {

    private final IUserRoleRepository iUserRoleRepository;

    public UserRoleServiceImpl(IUserRoleRepository iUserRoleRepository) {
        this.iUserRoleRepository = iUserRoleRepository;
    }

    @Override
    public List<UserRole> getRolesByUser(Long userId) {
        return iUserRoleRepository.getRolesByUser(userId);
       
    }
}
