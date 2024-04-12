package com.example.bookingapp.repository;

import com.example.bookingapp.model.Role;
import jakarta.validation.constraints.NotNull;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long>,
        JpaSpecificationExecutor<Role> {
    Role findByRoleName(Role.RoleName roleName);

    Set<Role> findByRoleNameIn(@NotNull Set<String> roleNames);
}
