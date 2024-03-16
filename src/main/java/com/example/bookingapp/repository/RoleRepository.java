package com.example.bookingapp.repository;

import com.example.bookingapp.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface RoleRepository extends JpaRepository<Role, Long>,
        JpaSpecificationExecutor<Role> {
    Role findByRoleName(Role.RoleName roleName);
}
