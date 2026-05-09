package com.eazybytes.jobportal.company.controller;

import com.eazybytes.jobportal.dto.CompanyDto;
import com.eazybytes.jobportal.company.service.ICompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * CompanyController - REST API endpoints for company data
 *
 * This controller exposes endpoints for:
 * - Retrieving all companies in the job portal
 * - Filtering and searching for specific companies
 *
 * All endpoints are protected and require JWT authentication.
 */
@RestController
@RequestMapping("api/companies")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173"})
public class CompanyController {

    // Service layer for company business logic and database operations
    private final ICompanyService companyService;

//    @Autowired // Optional
//    public CompanyController(ICompanyService companyService) {
//        this.companyService = companyService;
//    }

    @GetMapping()
    public ResponseEntity<List<CompanyDto>> getAllCompanies() {
        // Fetch all companies from the service layer
        List<CompanyDto> companyList = companyService.getAllCompanies();
        // Return with HTTP 200 OK status
        return ResponseEntity.ok().body(companyList);
    }

}
