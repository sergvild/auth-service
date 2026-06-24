package com.vizavi.authserver.repository;

import com.vizavi.authserver.entity.ERole;
import com.vizavi.authserver.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(ERole name);
}
