package com.indra.attendance_control.services.interfaces;

import java.util.List;
import com.indra.attendance_control.models.UserRole;

public interface IUserRoleService {

    List<UserRole> getRolesByUser(Long userId);

}
