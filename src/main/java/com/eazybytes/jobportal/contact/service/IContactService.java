package com.eazybytes.jobportal.contact.service;

import com.eazybytes.jobportal.dto.ContactDto;
import com.eazybytes.jobportal.dto.ContactResponseDto;
import com.eazybytes.jobportal.entity.Contact;

import java.util.List;

public interface IContactService {
    Contact saveContactDetails(ContactDto contactDto);
    List<ContactResponseDto> fetchNewContactMsgs();
    List<ContactResponseDto> fetchNewContactMsgsWithSort(String sortBy, String sortOrder);
}
