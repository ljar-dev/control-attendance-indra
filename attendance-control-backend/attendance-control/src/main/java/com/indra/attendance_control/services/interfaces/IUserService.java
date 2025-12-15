package com.indra.attendance_control.services.interfaces;

import com.indra.attendance_control.models.User;

public interface IUserService {

    User getByUserName(String username);
    void changePassword(String username, String oldPassword, String newPassword);
    void changePasswordFirstLogin(String username, String newPassword);
}
