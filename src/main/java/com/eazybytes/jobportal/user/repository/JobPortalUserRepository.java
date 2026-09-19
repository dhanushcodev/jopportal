package com.eazybytes.jobportal.user.repository;

import com.eazybytes.jobportal.entity.JobPortalUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JobPortalUserRepository extends JpaRepository<JobPortalUser, Integer> {
    Optional<JobPortalUser> findByEmail(String email);
}
