package com.indra.attendance_control.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.indra.attendance_control.models.UserRole;

@Repository
public interface IUserRoleRepository extends JpaRepository<UserRole, Long> {

    @Query("SELECT ur FROM UserRole ur WHERE ur.user.id = :userId")
    List<UserRole> getRolesByUser(@Param("userId") Long userId);

}
