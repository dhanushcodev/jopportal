# Job Portal Application - Code Flow Documentation

**Last Updated:** May 10, 2026  
**Project:** Job Portal Application  
**Stack:** Spring Boot 3.3.5, Java 21, MySQL, JWT Authentication

---

## Table of Contents
1. [Project Overview](#project-overview)
2. [Architecture Overview](#architecture-overview)
3. [Data Model & Entity Relationships](#data-model--entity-relationships)
4. [Authentication & Security Flow](#authentication--security-flow)
5. [Request Processing Flow](#request-processing-flow)
6. [Key Components & Responsibilities](#key-components--responsibilities)
7. [API Endpoints Overview](#api-endpoints-overview)
8. [Database Schema](#database-schema)

---

## Project Overview

The Job Portal is a full-stack web application that connects job seekers with employers. The backend is built with Spring Boot 3.3.5 (Java 21) and uses MySQL for data storage.

### Key Features:
- **User Management:** Registration, login, and profile management
- **Job Listings:** Browse, search, and apply for jobs
- **Company Profiles:** Employer company information and ratings
- **Role-Based Access Control:** Different permissions for regular users, employers, and admins
- **JWT-based Authentication:** Stateless API authentication using JSON Web Tokens
- **Audit Trails:** Track creation and modification timestamps for all records

---

## Architecture Overview

### Layered Architecture

The application follows a **three-tier layered architecture**:

```
┌─────────────────────────────────────────┐
│      REST Controllers (HTTP Layer)      │
│  AuthController, CompanyController, ... │
└──────────────────┬──────────────────────┘
                   │
┌──────────────────▼──────────────────────┐
│      Service Layer (Business Logic)     │
│  ICompanyService, IContactService, ...  │
└──────────────────┬──────────────────────┘
                   │
┌──────────────────▼──────────────────────┐
│   Repository Layer (Data Access)        │
│  Spring Data JPA Repositories           │
└──────────────────┬──────────────────────┘
                   │
┌──────────────────▼──────────────────────┐
│         Database (MySQL)                │
└─────────────────────────────────────────┘
```

### Security Layer

Security is implemented through:
- **JwtTokenValidatorFilter:** Validates JWT tokens on each request
- **SecurityConfig:** Configures Spring Security with path-based authorization
- **AuthController:** Handles user authentication and token issuance

---

## Data Model & Entity Relationships

### Entity Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                     JobPortalUser (User)                    │
│  ┌──────────────────────────────────────────────────────┐   │
│  │ id (PK)              │ Long (Auto-incremented)       │   │
│  │ name                 │ String (max 255)              │   │
│  │ email                │ String (max 255, unique)      │   │
│  │ password_hash        │ String (BCrypt hashed)        │   │
│  │ mobile_number        │ String (max 20)               │   │
│  │ role_id (FK)         │ References Role entity        │   │
│  │ company_id (FK)      │ References Company (nullable) │   │
│  │ created_at, updated_at │ Timestamps (audit)         │   │
│  └──────────────────────────────────────────────────────┘   │
└─────────────────────┬───────────────────────────────────────┘
                      │ (Many Users can have One Role)
                      │
        ┌─────────────▼───────────────┐
        │         Role                 │
        ├──────────────────────────────┤
        │ id (PK)                      │
        │ role_name (USER, ADMIN, ...)  │
        └──────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                      Company                                │
│  ┌──────────────────────────────────────────────────────┐   │
│  │ id (PK)              │ Long (Auto-incremented)       │   │
│  │ name                 │ String (unique)               │   │
│  │ logo                 │ URL to logo image             │   │
│  │ industry             │ Industry sector               │   │
│  │ size                 │ Company size classification   │   │
│  │ rating               │ BigDecimal (0.00 - 5.00)      │   │
│  │ locations            │ Comma-separated locations     │   │
│  │ founded              │ Year established              │   │
│  │ description          │ Rich text (CLOB)              │   │
│  │ employees            │ Approximate employee count    │   │
│  │ website              │ Company website URL           │   │
│  │ created_at, updated_at │ Timestamps (audit)         │   │
│  └──────────────────────────────────────────────────────┘   │
└─────────────────────┬───────────────────────────────────────┘
                      │ (One Company posts Many Jobs)
                      │
        ┌─────────────▼───────────────────────────┐
        │           Job                            │
        ├────────────────────────────────────────┤
        │ id (PK)                                 │
        │ title                                   │
        │ company_id (FK) ◄────────────────────────┤
        │ location                                │
        │ work_type (REMOTE, ONSITE, HYBRID)      │
        │ job_type (FULL_TIME, PART_TIME, etc)    │
        │ category                                │
        │ experience_level                        │
        │ salary_min, salary_max, salary_currency │
        │ description, requirements, benefits     │
        │ posted_date, application_deadline       │
        │ created_at, updated_at                  │
        └────────────────────────────────────────┘
```

### Relationships Summary

| From | To | Type | Cardinality | Notes |
|------|----|----|-----------|-------|
| JobPortalUser | Role | Many-to-One | Eager | Every user must have a role |
| JobPortalUser | Company | Many-to-One | Eager, Optional | Employer users linked to company |
| Company | Job | One-to-Many | Cascade All | Delete company = delete its jobs |
| Job | Company | Many-to-One | Lazy | Job must belong to a company |

---

## Authentication & Security Flow

### Login Flow (Request → Token)

```
┌──────────────────────────────────────────────────────────────────┐
│ 1. CLIENT SENDS LOGIN REQUEST                                    │
│                                                                  │
│    POST /api/login                                              │
│    Content-Type: application/json                              │
│    {                                                            │
│      "username": "user@example.com",                           │
│      "password": "password123"                                 │
│    }                                                            │
└──────────────────┬───────────────────────────────────────────────┘
                   │
        ┌──────────▼──────────────┐
        │  AuthController.login() │
        │  (REST Endpoint)        │
        └──────────┬──────────────┘
                   │
┌──────────────────▼──────────────────────────────────────────────┐
│ 2. AUTHENTICATION MANAGER VALIDATES CREDENTIALS                  │
│                                                                  │
│    AuthenticationManager.authenticate(                          │
│      UsernamePasswordAuthenticationToken(username, password)    │
│    )                                                             │
│                                                                  │
│    - Looks up user in UserDetailsService (in-memory for now)   │
│    - Compares password hash using BCryptPasswordEncoder        │
│    - Returns Authentication object if credentials are valid     │
│    - Throws BadCredentialsException if invalid                 │
└──────────────────┬───────────────────────────────────────────────┘
                   │
        ┌──────────▼──────────────────┐
        │  JwtUtil.generateToken()    │
        │  (Token Creation)           │
        └──────────┬──────────────────┘
                   │
┌──────────────────▼──────────────────────────────────────────────┐
│ 3. JWT TOKEN GENERATION                                         │
│                                                                  │
│    Token Contents:                                              │
│    {                                                            │
│      "iss": "job-portal",              // Issuer              │
│      "sub": "JWT TOKEN",               // Subject             │
│      "username": "user@example.com",   // Custom claim        │
│      "role": "USER",                   // Custom claim        │
│      "iat": 1715000000,                // Issued at time      │
│      "exp": 1715003600                 // Expiration (1 hour) │
│    }                                                            │
│                                                                  │
│    Token Format: Header.Payload.Signature (Base64-encoded)     │
│    Signature created using HMAC-SHA256 with secret key          │
└──────────────────┬───────────────────────────────────────────────┘
                   │
┌──────────────────▼──────────────────────────────────────────────┐
│ 4. SERVER RETURNS TOKEN                                         │
│                                                                  │
│    HTTP 200 OK                                                  │
│    {                                                            │
│      "status": "OK",                                           │
│      "user": {...},                                            │
│      "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."       │
│    }                                                            │
└──────────────────┬───────────────────────────────────────────────┘
                   │
        ┌──────────▼──────────────┐
        │  CLIENT STORES TOKEN    │
        │  (In localStorage)      │
        └─────────────────────────┘
```

### Request Authentication Flow (Token → Resource Access)

```
┌──────────────────────────────────────────────────────────────────┐
│ 1. CLIENT SENDS API REQUEST WITH TOKEN                           │
│                                                                  │
│    GET /api/companies                                           │
│    Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...│
└──────────────────┬───────────────────────────────────────────────┘
                   │
        ┌──────────▼──────────────────────────┐
        │  JwtTokenValidatorFilter.doFilter() │
        │  (Executed before route handler)    │
        └──────────┬─────────────────────────┘
                   │
┌──────────────────▼──────────────────────────────────────────────┐
│ 2. EXTRACT & VALIDATE JWT TOKEN                                 │
│                                                                  │
│    a) Extract "Bearer <token>" from Authorization header        │
│    b) Remove "Bearer " prefix to get raw token                  │
│    c) Call JwtUtil.validateToken():                             │
│       - Verify signature using secret key                      │
│       - Check token hasn't expired                             │
│       - Validate token structure                               │
│                                                                  │
│    If token is invalid/expired:                                │
│    - Return HTTP 401 UNAUTHORIZED                              │
│    - Response: "Token is expired" or "Invalid Token"           │
└──────────────────┬───────────────────────────────────────────────┘
                   │
        ┌──────────▼───────────────────────────────────┐
        │  Extract Claims from Valid Token             │
        │  - Username (from "username" claim)          │
        │  - Role (from "role" claim)                  │
        └──────────┬──────────────────────────────────┘
                   │
┌──────────────────▼──────────────────────────────────────────────┐
│ 3. SET SPRING SECURITY CONTEXT                                  │
│                                                                  │
│    Create UsernamePasswordAuthenticationToken:                  │
│    {                                                            │
│      principal: "user@example.com",                            │
│      credentials: null,                                        │
│      authorities: ["ROLE_USER"]  // Prefixed for Spring       │
│    }                                                            │
│                                                                  │
│    SecurityContextHolder.getContext()                          │
│      .setAuthentication(token)                                 │
│                                                                  │
│    Result: User is now \"authenticated\" for this request       │
└──────────────────┬───────────────────────────────────────────────┘
                   │
        ┌──────────▼──────────────────────────┐
        │  Continue to SecurityFilterChain    │
        │  (Enforce authorization rules)      │
        └──────────┬─────────────────────────┘
                   │
┌──────────────────▼──────────────────────────────────────────────┐
│ 4. AUTHORIZE REQUEST BASED ON PATH                              │
│                                                                  │
│    SecurityConfig.filterChain() applies rules:                  │
│                                                                  │
│    If path is in publicPaths:                                  │
│    → Allow request (no authentication needed)                  │
│                                                                  │
│    If path is in privatePaths:                                 │
│    → Check if request.isAuthenticated() = true                │
│    → Allow (user has valid token)                             │
│                                                                  │
│    If path is in adminPaths:                                   │
│    → Check if request.isAuthenticated() = true                │
│    → Check if user has role \"ADMIN\"                          │
│    → Allow only if both conditions met                        │
└──────────────────┬───────────────────────────────────────────────┘
                   │
        ┌──────────▼──────────────────────────┐
        │  Route Handler Executes             │
        │  (CompanyController.getAllCompanies)│
        └──────────┬─────────────────────────┘
                   │
┌──────────────────▼──────────────────────────────────────────────┐
│ 5. RETURN AUTHORIZED RESPONSE                                   │
│                                                                  │
│    HTTP 200 OK                                                  │
│    [                                                            │
│      {                                                          │
│        \"id\": 1,                                              │
│        \"name\": \"Tech Corp\",                               │
│        \"industry\": \"Technology\",                          │
│        \"rating\": 4.5                                        │
│      },                                                         │
│      ...                                                        │
│    ]                                                            │
└──────────────────────────────────────────────────────────────────┘
```

---

## Request Processing Flow

### Complete Request-Response Cycle for API Endpoint

```
Client Request
    ↓
HTTP Request arrives at Spring Boot server
    ↓
DispatcherServlet routes to appropriate filter chain
    ↓
JwtTokenValidatorFilter.doFilterInternal()
    ├─ Extract Authorization header
    ├─ Validate JWT token
    ├─ Set SecurityContext if valid
    └─ Continue to next filter
    ↓
SecurityFilterChain
    ├─ Check CORS settings
    ├─ Apply authorization rules
    └─ Allow or deny request
    ↓
Route Mapping (@GetMapping, @PostMapping, etc.)
    ↓
Controller Method Executes
    ├─ Receive request parameters
    ├─ Call service layer methods
    └─ Build response
    ↓
Service Layer
    ├─ Implement business logic
    ├─ Call repository methods
    └─ Return results to controller
    ↓
Repository Layer
    ├─ Execute database queries
    └─ Return entities/data
    ↓
DTO Conversion
    ├─ Convert entities to DTOs
    ├─ Hide sensitive fields
    └─ Optimize response payload
    ↓
HTTP Response Sent to Client
    ├─ JSON serialization
    ├─ HTTP status code
    └─ Response headers
    ↓
Client receives and processes response
```

---

## Key Components & Responsibilities

### 1. Controllers (HTTP Interface)

**AuthController** (`auth/AuthController.java`)
- **Purpose:** Handle user authentication requests
- **Endpoints:**
  - `POST /api/login` - User login with email/password
- **Responsibilities:**
  - Receive login credentials
  - Delegate to AuthenticationManager
  - Generate JWT token using JwtUtil
  - Return token and user info to client
- **Security:** Public endpoint (login required to access)

**CompanyController** (`company/controller/CompanyController.java`)
- **Purpose:** Expose company data through REST API
- **Endpoints:**
  - `GET /api/companies` - Get all companies
- **Responsibilities:**
  - Call company service to fetch data
  - Convert entities to DTOs
  - Return JSON response
- **Security:** Requires authentication (private path)
- **CORS:** Allows requests from http://localhost:5173 (React frontend)

### 2. Security Components

**SecurityConfig** (`security/SecurityConfig.java`)
- **Purpose:** Configure Spring Security for the application
- **Key Responsibilities:**
  - Define SecurityFilterChain (order of filters and authorization rules)
  - Configure CORS settings for frontend communication
  - Define path-based authorization (public/private/admin)
  - Provide password encoder (BCryptPasswordEncoder)
  - Create UserDetailsService (in-memory for development)
- **Configuration:**
  - Public paths: `/api/login`, `/api/register`, `/api/companies` (GET)
  - Private paths: `/api/jobs`, `/api/profile`, `/api/saved-jobs`
  - Admin paths: `/api/admin/**`

**JwtTokenValidatorFilter** (`security/filter/JwtTokenValidatorFilter.java`)
- **Purpose:** Validate JWT tokens on each request
- **Responsibilities:**
  - Intercept HTTP requests
  - Extract Bearer token from Authorization header
  - Validate token signature and expiration
  - Extract user info (username, role) from token
  - Set Spring SecurityContext with authenticated user
  - Skip filter for public paths
- **Execution:** Runs before BasicAuthenticationFilter for each request

**JwtUtil** (`security/util/JwtUtil.java`)
- **Purpose:** JWT token creation, validation, and claim extraction
- **Key Methods:**
  - `generateToken(Authentication)` - Create JWT after successful login
  - `validateToken(String)` - Verify token signature and expiration
  - `extractUsername(String)` - Get username from token claims
  - `extractRole(String)` - Get user role from token claims
- **Token Expiration:** 1 hour from issuance
- **Algorithm:** HMAC-SHA256
- **Secret:** Stored in ApplicationConstants (should be externalized in production)

### 3. Service Layer

**ICompanyService** (`company/service/ICompanyService.java`)
- Interface defining company data operations

**CompanyServiceImpl** (`company/service/impl/CompanyServiceImpl.java`)
- Implementation of business logic for company operations
- Methods:
  - `getAllCompanies()` - Fetch all companies from database
  - `getCompanyById(Long)` - Get specific company
  - `createCompany(CompanyDto)` - Save new company
  - Data transformation: Entity ↔ DTO conversion

**IContactService** (`contact/service/IContactService.java`)
**ContactServiceImpl** (`contact/service/impl/ContactServiceImpl.java`)
- Handle contact/message submissions from visitors

### 4. Repository Layer (Data Access)

Spring Data JPA Repositories provide database operations:

**CompanyRepository** (`company/repository/CompanyRepository.java`)
- Extends JpaRepository<Company, Long>
- Auto-generated CRUD methods:
  - `save()`, `findAll()`, `findById()`, `update()`, `delete()`
- Custom query methods can be added for specific searches

**RoleRepository** (`role/repository/RoleRepository.java`)
- Manages Role entities
- Custom methods:
  - `findByRoleName(String)` - Find role by name

**ContactRepository** (`contact/repository/ContactRepository.java`)
- Manage contact form submissions

### 5. Entity Classes (Data Model)

**BaseEntity** (`entity/BaseEntity.java`)
- Abstract parent class for all entities
- Provides audit fields:
  - `createdAt` - Timestamp when record created
  - `createdBy` - Username who created record
  - `updatedAt` - Timestamp of last modification
  - `updatedBy` - Username who last modified record
- **AuditorAware:** Automatically filled by AuditorAwareImpl (gets current user)

**JobPortalUser** (`entity/JobPortalUser.java`)
- Represents a user account in the system
- Fields: id, name, email, passwordHash, mobileNumber, role, company
- Relationships: ManyToOne with Role (required), ManyToOne with Company (optional)
- Used by Spring Security UserDetailsService

**Company** (`entity/Company.java`)
- Employer/company profiles in the portal
- Fields: name, logo, industry, size, rating, locations, founded, description, employees, website
- Relationship: OneToMany with Job entities (cascade all operations)

**Job** (`entity/Job.java`)
- Job listings posted by companies
- Fields: title, location, workType, jobType, category, experienceLevel, salaryMin, salaryMax, description, requirements, benefits, postedDate, applicationDeadline
- Relationship: ManyToOne with Company (lazy loaded, cascade delete)

**Role** (`entity/Role.java`)
- User roles/permissions: USER, ADMIN, EMPLOYER, RECRUITER
- Relationship: OneToMany with JobPortalUser

**Contact** (`entity/Contact.java`)
- Contact form submissions/messages
- Fields: name, email, message, subject, status

### 6. DTO Classes (Data Transfer Objects)

DTOs are used to:
- Transfer data between controller and client (JSON format)
- Hide sensitive database fields
- Provide focused data views for different use cases

**LoginRequestDto** (`dto/LoginRequestDto.java`)
- Accepts username and password from login form

**LoginResponseDto** (`dto/LoginResponseDto.java`)
- Returns status, user info, and JWT token after successful login

**UserDto** (`dto/UserDto.java`)
- User information for API responses (no password field)

**CompanyDto** (`dto/CompanyDto.java`)
- Company information in API responses

**JobDto** (`dto/JobDto.java`)
- Job posting information for listings

**ContactDto** (`dto/ContactDto.java`)
- Contact form data

### 7. Utility & Configuration

**ApplicationConstants** (`constants/ApplicationConstants.java`)
- Centralized configuration and constants
- JWT secret key
- Path definitions
- Other application-wide constants

**AuditorAwareImpl** (`audit/AuditorAwareImpl.java`)
- Implements AuditorAware interface
- Returns current authenticated username for audit fields
- Integrated with Spring Data Auditing

**Logging Aspect** (`aspects/Logging.java`)
- AOP aspect for cross-cutting logging
- Logs method entry/exit with parameters
- Logs execution time

**GlobalExceptionHandler** (`exception/GlobalExceptionHandler.java`)
- Centralized exception handling
- Converts exceptions to appropriate HTTP responses
- Provides consistent error response format

---

## API Endpoints Overview

### Authentication Endpoints
| Method | Endpoint | Auth Required | Description |
|--------|----------|---------------|-------------|
| POST | `/api/login` | No | User login - returns JWT token |
| POST | `/api/register` | No | User registration |

### Company Endpoints
| Method | Endpoint | Auth Required | Description |
|--------|----------|---------------|-------------|
| GET | `/api/companies` | Yes | Get all companies |
| GET | `/api/companies/{id}` | Yes | Get company details |
| POST | `/api/companies` | Yes (Admin) | Create new company |
| PUT | `/api/companies/{id}` | Yes (Admin) | Update company |
| DELETE | `/api/companies/{id}` | Yes (Admin) | Delete company |

### Job Endpoints
| Method | Endpoint | Auth Required | Description |
|--------|----------|---------------|-------------|
| GET | `/api/jobs` | Yes | Get all jobs |
| GET | `/api/jobs/{id}` | Yes | Get job details |
| POST | `/api/jobs` | Yes (Employer) | Post new job |
| PUT | `/api/jobs/{id}` | Yes (Employer) | Edit job posting |
| DELETE | `/api/jobs/{id}` | Yes (Employer) | Delete job posting |

### User Endpoints
| Method | Endpoint | Auth Required | Description |
|--------|----------|---------------|-------------|
| GET | `/api/profile` | Yes | Get current user profile |
| PUT | `/api/profile` | Yes | Update user profile |
| POST | `/api/jobs/{id}/apply` | Yes | Apply for job |
| GET | `/api/applied-jobs` | Yes | Get user's job applications |

### Admin Endpoints
| Method | Endpoint | Auth Required | Description |
|--------|----------|---------------|-------------|
| GET | `/api/admin/users` | Yes (Admin) | List all users |
| GET | `/api/admin/jobs` | Yes (Admin) | Manage job postings |
| GET | `/api/admin/companies` | Yes (Admin) | Manage companies |
| POST | `/api/admin/contact-messages` | Yes (Admin) | View contact submissions |

---

## Database Schema

### Users Table
```sql
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(500) NOT NULL,
    mobile_number VARCHAR(20),
    role_id BIGINT NOT NULL REFERENCES roles(id),
    company_id BIGINT REFERENCES companies(id),
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(20) NOT NULL,
    updated_at TIMESTAMP,
    updated_by VARCHAR(20),
    INDEX idx_email (email),
    INDEX idx_role (role_id),
    INDEX idx_company (company_id)
);
```

### Roles Table
```sql
CREATE TABLE roles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_name VARCHAR(50) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(20) NOT NULL,
    updated_at TIMESTAMP,
    updated_by VARCHAR(20)
);
```

### Companies Table
```sql
CREATE TABLE companies (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL UNIQUE,
    logo VARCHAR(500),
    industry VARCHAR(100) NOT NULL,
    size VARCHAR(50) NOT NULL,
    rating DECIMAL(3,2) NOT NULL,
    locations VARCHAR(1000),
    founded INT NOT NULL,
    description LONGTEXT,
    employees INT,
    website VARCHAR(500),
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(20) NOT NULL,
    updated_at TIMESTAMP,
    updated_by VARCHAR(20),
    INDEX idx_industry (industry),
    INDEX idx_rating (rating)
);
```

### Jobs Table
```sql
CREATE TABLE jobs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    company_id BIGINT NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    location VARCHAR(255) NOT NULL,
    work_type VARCHAR(50) NOT NULL,
    job_type VARCHAR(50) NOT NULL,
    category VARCHAR(100) NOT NULL,
    experience_level VARCHAR(50) NOT NULL,
    salary_min DECIMAL(12,2) NOT NULL,
    salary_max DECIMAL(12,2) NOT NULL,
    salary_currency VARCHAR(10) DEFAULT 'USD',
    salary_period VARCHAR(20) DEFAULT 'year',
    description LONGTEXT NOT NULL,
    requirements LONGTEXT,
    benefits LONGTEXT,
    posted_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    application_deadline TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(20) NOT NULL,
    updated_at TIMESTAMP,
    updated_by VARCHAR(20),
    INDEX idx_company (company_id),
    INDEX idx_location (location),
    INDEX idx_posted_date (posted_date)
);
```

### Contacts Table
```sql
CREATE TABLE contacts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    subject VARCHAR(255),
    message LONGTEXT NOT NULL,
    status VARCHAR(50) DEFAULT 'NEW',
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(20) NOT NULL,
    updated_at TIMESTAMP,
    updated_by VARCHAR(20),
    INDEX idx_email (email),
    INDEX idx_status (status)
);
```

---

## Common Development Workflows

### Adding a New API Endpoint

1. **Create DTO classes** (if needed)
   - Request DTO for input data
   - Response DTO for output data

2. **Create/Update Controller**
   - Add @GetMapping/@PostMapping/@PutMapping/@DeleteMapping
   - Add authorization annotations if needed
   - Call service layer methods

3. **Create/Update Service Interface**
   - Define business logic method signature

4. **Implement Service Method**
   - Add business logic
   - Call repository methods
   - Convert entities to DTOs

5. **Ensure appropriate security configuration**
   - Add path to public/private/admin lists if needed
   - Update SecurityConfig if new role required

### Debugging Tips

1. **JWT Token Issues**
   - Check token format in Authorization header: `Bearer <token>`
   - Verify token hasn't expired (1 hour limit)
   - Check secret key matches in JwtUtil

2. **Authentication Failures**
   - Check user exists in UserDetailsService
   - Verify password matches (BCrypt encoded)
   - Check user has appropriate role for endpoint

3. **CORS Issues**
   - Verify origin is allowed in CorsConfigurationSource
   - Check request method is included in allowedMethods
   - Verify credentials flag if using cookies

4. **Database Issues**
   - Check MySQL server is running
   - Verify database credentials in application.properties
   - Check entity mapping matches database columns

---

## Production Checklist

- [ ] Move JWT secret from ApplicationConstants to external configuration (environment variables or properties file)
- [ ] Replace in-memory UserDetailsService with database-backed implementation
- [ ] Remove debug logging (System.out.println in AuthController)
- [ ] Restrict CORS origins from \"*\" to specific frontend domain
- [ ] Enable HTTPS/TLS for all API communication
- [ ] Implement rate limiting for login attempts
- [ ] Add request logging and monitoring
- [ ] Enable database connection pooling (HikariCP)
- [ ] Set up proper error logging (Log4j2 or similar)
- [ ] Review and strengthen password requirements
- [ ] Implement refresh token mechanism for better security
- [ ] Add API versioning (/api/v1/, /api/v2/) for backward compatibility
- [ ] Implement pagination for list endpoints
- [ ] Add API documentation (Swagger/OpenAPI)
- [ ] Set up automated backups for MySQL database
- [ ] Configure deployment pipeline (CI/CD)
- [ ] Performance testing and load testing

---

**Document Version:** 1.0  
**Last Modified:** May 10, 2026  
**Maintainer:** Development Team
