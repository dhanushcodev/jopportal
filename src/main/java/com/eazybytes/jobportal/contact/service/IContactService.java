package com.eazybytes.jobportal.contact.service;

import com.eazybytes.jobportal.dto.ContactDto;
import com.eazybytes.jobportal.dto.ContactResponseDto;
import com.eazybytes.jobportal.entity.Contact;
import org.springframework.data.domain.Page;

import java.util.List;

public interface IContactService {
    Contact saveContactDetails(ContactDto contactDto);
    List<ContactResponseDto> fetchNewContactMsgs();
    List<ContactResponseDto> fetchNewContactMsgsWithSort(String sortBy, String sortOrder);
    Page<ContactResponseDto> fetchNewContactMsgsWithPaginationAndSort(int pageNumber, int pageSize, String sortBy, String sortOrder);
}
