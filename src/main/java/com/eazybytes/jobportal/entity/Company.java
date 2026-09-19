package com.eazybytes.jobportal.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Company - JPA Entity representing a company/employer in the job portal
 *
 * This entity stores company profile information:
 * - Basic details: name, logo, website, industry, size
 * - Location information and rating/reviews
 * - Company description and metadata
 * - One-to-many relationship with jobs (a company posts multiple jobs)
 * - Audit fields (inherited from BaseEntity): track creation and modifications
 *
 * Companies can post job listings which are associated via the jobs collection.
 */
@Entity
@Table(name = "COMPANIES")
@Getter
@Setter
public class Company extends BaseEntity {

    // Primary key - auto-generated unique identifier
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false)
    private Long id;

    // Company's unique name
    @Column(name = "NAME", nullable = false, unique = true)
    private String name;

    // Company logo URL for display in the frontend
    @Column(name = "LOGO", length = 500)
    private String logo;

    // Industry sector (e.g., Technology, Finance, Healthcare, etc.)
    @Column(name = "INDUSTRY", nullable = false, length = 100)
    private String industry;

    // Company size classification (e.g., Startup, Small, Medium, Large, Enterprise)
    @Column(name = "SIZE", nullable = false, length = 50)
    private String size;

    // Average company rating from job applicants (0.00 - 5.00)
    @Column(name = "RATING", nullable = false, precision = 3, scale = 2)
    private BigDecimal rating;

    // Comma-separated list of office locations
    @Column(name = "LOCATIONS", length = 1000)
    private String locations;

    // Year company was founded
    @Column(name = "FOUNDED", nullable = false)
    private Integer founded;

    // Detailed company description/about section
    @Lob
    @Column(name = "DESCRIPTION")
    private String description;

    // Approximate number of employees
    @Column(name = "EMPLOYEES")
    private Integer employees;

    // Company website URL
    @Column(name = "WEBSITE", length = 500)
    private String website;

    // One-to-many relationship: a company can post many jobs
    // cascade = CascadeType.ALL: when a company is deleted, all its jobs are deleted
    // orphanRemoval = true: jobs with no company are automatically deleted
    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 10)
    private List<Job> jobs = new ArrayList<>();

}