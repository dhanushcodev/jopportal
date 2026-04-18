package com.eazybytes.jobportal.contact.service;

import com.eazybytes.jobportal.dto.ContactDto;
import com.eazybytes.jobportal.entity.Contact;

public interface IContactService {
    Contact saveContactDetails(ContactDto contactDto);
}
