package com.eazybytes.jobportal.contact.controller;

import com.eazybytes.jobportal.contact.service.IContactService;
import com.eazybytes.jobportal.dto.ContactDto;

import com.eazybytes.jobportal.dto.ContactResponseDto;
import com.eazybytes.jobportal.entity.Contact;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contact")
@CrossOrigin(origins = {"http://localhost:5173"})
public class ContactController {
    private final IContactService contactService;

    private record ResponseStruct(String message, Contact data) {
    }

    @Autowired
    ContactController(IContactService contactService) {
        this.contactService = contactService;
    }

    @PostMapping()
    public ResponseEntity<?> createContact(@RequestBody @Valid ContactDto contactDto) {
        Contact saved = contactService.saveContactDetails(contactDto);
        if (saved != null) {
            return new ResponseEntity<ResponseStruct>(new ResponseStruct("Contact saved Successfully",saved), HttpStatus.CREATED);
        }else  {
            throw new RuntimeException("Failed to save contact");
        }
    }

    @GetMapping("/admin")
    public ResponseEntity<List<ContactResponseDto>> fetchOpenContactMsgs(){
        List<ContactResponseDto> contactResponseDtoList = contactService.fetchNewContactMsgs();
        return new ResponseEntity<>(contactResponseDtoList, HttpStatus.OK);
    }

    @GetMapping("/sort/admin")
    public ResponseEntity<List<ContactResponseDto>> fetchNewContactMsgsWithSort(
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "asc") String sortOrder
    ){
        List<ContactResponseDto> contactResponseDtoList = contactService.fetchNewContactMsgsWithSort(sortBy,sortOrder);
        return ResponseEntity.ok(contactResponseDtoList);
    }
}
