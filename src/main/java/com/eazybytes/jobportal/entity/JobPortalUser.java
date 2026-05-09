package com.eazybytes.jobportal.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

/**
 * JobPortalUser - JPA Entity representing a user in the job portal system
 *
 * This entity stores user account information including:
 * - Basic details: name, email, password hash, phone number
 * - Role: determines access level (USER, ADMIN, EMPLOYER, RECRUITER)
 * - Company association: if the user is an employer, links to their company
 * - Audit fields (inherited from BaseEntity): creation/modification timestamps and user tracking
 *
 * The password is stored as a hash using BCrypt for security.
 * Users are linked to a Role which determines their permissions.
 */
@Getter
@Setter
@Entity
@Table(name = "users")
public class JobPortalUser extends BaseEntity {
    // Primary key - auto-generated unique identifier for each user
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    // User's full name
    @Size(max = 255)
    @NotNull
    @Column(name = "name", nullable = false)
    private String name;

    // User's unique email address (used as username for login)
    @Size(max = 255)
    @NotNull
    @Column(name = "email", nullable = false)
    private String email;

    // BCrypt-encoded password hash (never store plain text passwords)
    @Size(max = 500)
    @NotNull
    @Column(name = "password_hash", nullable = false, length = 500)
    private String passwordHash;

    // Optional phone number for contact
    @Size(max = 20)
    @Column(name = "mobile_number", length = 20)
    private String mobileNumber;

    // User's role which determines permissions and access levels
    // Loaded eagerly to make role available immediately after user is loaded
    @NotNull
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    // Association to company if user is an employer
    // Loaded eagerly for immediate access
    // If company is deleted, this reference is set to null (SET_NULL)
    @ManyToOne(fetch = FetchType.EAGER)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "company_id")
    private Company company;


}
