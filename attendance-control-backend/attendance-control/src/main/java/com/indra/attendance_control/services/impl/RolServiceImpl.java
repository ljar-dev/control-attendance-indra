package com.indra.attendance_control.services.impl;

import java.util.List;
import com.indra.attendance_control.models.UserRole;
import com.indra.attendance_control.repositories.IUserRoleRepository;
import com.indra.attendance_control.services.interfaces.IRoleSevice;

public class RolServiceImpl implements IRoleSevice {

    private final IUserRoleRepository iUserRoleRepository;

    public RolServiceImpl(IUserRoleRepository iUserRoleRepository) {
        this.iUserRoleRepository = iUserRoleRepository;
    }

    @Override
    public List<String> getRolesByUserId(Long userId) {
        List<UserRole> userRoles = iUserRoleRepository.getRolesByUser(userId);
        List<String> roleNames = userRoles.stream().map( userRol -> {
            return userRol.getRole().getName();
        })
        .toList();

        return roleNames;
    }
}
