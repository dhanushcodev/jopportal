package com.eazybytes.jobportal.role.repository;

import com.eazybytes.jobportal.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Integer> {
}
