package com.eazybytes.jobportal.contact.service.impl;

import com.eazybytes.jobportal.contact.repository.ContactRepository;
import com.eazybytes.jobportal.contact.service.IContactService;
import com.eazybytes.jobportal.dto.ContactDto;
import com.eazybytes.jobportal.dto.ContactResponseDto;
import com.eazybytes.jobportal.entity.Contact;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ContactServiceImpl implements IContactService {

    private final ContactRepository contactRepository;
    @Autowired
    ContactServiceImpl(ContactRepository contactRepository) {
        this.contactRepository = contactRepository;
    }

    @Override
    public Contact saveContactDetails(ContactDto contactDto) {
        return contactRepository.save(transformToEntity(contactDto));
    }

    @Override
    public List<ContactResponseDto> fetchNewContactMsgs() {
        List<Contact> contactList = contactRepository.findContactsByStatus("NEW");
        return contactList.stream().map(this::transformToDto).toList();
    }

    private ContactResponseDto transformToDto(Contact contact){
        return new ContactResponseDto(
                contact.getId(),contact.getName(), contact.getEmail(), contact.getUserType(), contact.getSubject(),
                contact.getMessage(), contact.getStatus(), contact.getCreatedAt()
                );
    }

    private Contact transformToEntity(ContactDto contactDto) {
        Contact contact = new Contact();
        BeanUtils.copyProperties(contactDto, contact);
        contact.setStatus("NEW");
        return contact;
    }

}
