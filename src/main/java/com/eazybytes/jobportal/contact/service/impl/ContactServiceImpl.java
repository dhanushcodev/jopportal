package com.eazybytes.jobportal.contact.service.impl;

import com.eazybytes.jobportal.contact.repository.ContactRepository;
import com.eazybytes.jobportal.contact.service.IContactService;
import com.eazybytes.jobportal.dto.ContactDto;
import com.eazybytes.jobportal.dto.ContactResponseDto;
import com.eazybytes.jobportal.entity.Contact;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

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

    @Override
    public List<ContactResponseDto> fetchNewContactMsgsWithSort(String sortBy, String sortOrder) {
        Sort sort = sortOrder.equalsIgnoreCase("desc")?
                Sort.by(sortBy).descending():Sort.by(sortBy).ascending();
        List<Contact> contactList = contactRepository.findContactsByStatus("NEW",sort);
        return contactList.stream().map(this::transformToDto).toList();
    }

    @Override
    public Page<ContactResponseDto> fetchNewContactMsgsWithPaginationAndSort(
            int pageNumber, int pageSize, String sortBy, String sortOrder) {
        //create sort object based on sortBy and sortDir parameters
        Sort sort = sortOrder.equalsIgnoreCase("desc")?
                Sort.by(sortBy).descending():Sort.by(sortBy).ascending();
        //create pageable object with page number, page size, and sorting
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);
        Page<Contact> contactPage = contactRepository.findContactsByStatus("NEW",pageable);
        return contactPage.map(this::transformToDto);
    }

    @Override
    public boolean closeContactMsg(Long id, String closedMsg) {
        Contact contact = contactRepository.findById(id).orElse(null);
        if(contact == null){
            return false;
        }else {
            contact.setStatus("CLOSED");
            contactRepository.save(contact);
            return true;
        }
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
