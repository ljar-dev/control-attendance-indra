package com.indra.attendance_control.repositories;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.indra.attendance_control.models.User;

public interface IUserRepository extends JpaRepository<User,Long> {

    @Query("SELECT u FROM User u WHERE u.username = :username")
    Optional<User> getByUserName(@Param("username") String username);

    boolean existsByUsername(String username);
}
