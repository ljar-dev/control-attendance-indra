package com.indra.attendance_control.repositories;


import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.indra.attendance_control.models.Role;


public interface IRoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(String name);
}
