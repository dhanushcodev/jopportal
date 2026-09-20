package com.eazybytes.jobportal.contact.repository;

import com.eazybytes.jobportal.entity.Contact;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContactRepository extends JpaRepository<Contact, Long> {
    List<Contact> findContactsByStatus(String status);
    List<Contact> findContactsByStatus(String status, Sort sort);
}
