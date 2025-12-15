package com.indra.attendance_control.services.interfaces;

import java.util.List;

public interface IRoleSevice {
    List<String> getRolesByUserId(Long userId);
}
