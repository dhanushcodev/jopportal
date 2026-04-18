package com.eazybytes.jobportal.contact.repository;

import com.eazybytes.jobportal.entity.Contact;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContactRepository extends JpaRepository<Contact, Long> {
}
