package com.example.risephonebook.service;

import com.example.risephonebook.entity.Contact;
import com.example.risephonebook.exception.BadRequestException;
import com.example.risephonebook.repository.ContactRepository;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import org.slf4j.LoggerFactory;
import static com.example.risephonebook.utils.Validation.*;

@Service
public class ContactService {
    private static final Logger logger = LoggerFactory.getLogger(ContactService.class);

    @Autowired
    private ContactRepository contactRepository;

    public Page<Contact> getContacts(Pageable pageable, String id, String firstName, String lastName, String phone, String address) {
        validatePageable(pageable);
        logger.debug("Getting contacts with id: {}, firstName: {}, lastName: {}, phone: {}, address: {}",
                id, firstName, lastName, phone, address);

        ExampleMatcher matcher = ExampleMatcher.matching()
                .withIgnoreNullValues()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);

        Contact contactToFetch = buildContactToFetch(id, firstName, lastName, phone, address);
        Example<Contact> example = Example.of(contactToFetch, matcher);

        return contactRepository.findAll(example, pageable);
    }

    private Contact buildContactToFetch(String id, String firstName, String lastName, String phone, String address){
        Contact contact = new Contact();

        if (Objects.nonNull(id) && !id.isEmpty()) {
            try {
                contact.setId(Long.parseLong(id));
            } catch (NumberFormatException e) {
                logger.error("Invalid ID format: {}", id, e);
                throw new BadRequestException("Invalid ID format");
            }
        }
        if (Objects.nonNull(firstName) && !firstName.isEmpty()) {
            contact.setFirstName(firstName);
        }
        if (Objects.nonNull(lastName) && !lastName.isEmpty()) {
            contact.setLastName(lastName);
        }
        if (Objects.nonNull(phone) && !phone.isEmpty()) {
            contact.setPhone(phone);
        }
        if (Objects.nonNull(address) && !address.isEmpty()) {
            contact.setAddress(address);
        }
        return contact;
    }

    public Contact addContact(Contact contact) {
        validateContact(contact);
        logger.debug("Adding contact: {}", contact);

        return contactRepository.save(contact);
    }

    public List<Contact> searchContacts(String query) {
        validateQueryNotNullOrEmpty(query);
        logger.debug("Searching contacts with query, name: {}", query);

        return contactRepository.findByFirstNameContainingOrLastNameContaining(query, query);
    }

    public Contact updateContact(Long id, Contact contactDetails) {
        validateContact(contactDetails);
        logger.debug("Updating contact with id: {}, contact: {}", id, contactDetails);

        Contact contact = contactRepository.findById(id).orElseThrow();
        contact.setFirstName(contactDetails.getFirstName());
        contact.setLastName(contactDetails.getLastName());
        contact.setPhone(contactDetails.getPhone());
        contact.setAddress(contactDetails.getAddress());
        return contactRepository.save(contact);
    }

    public void deleteContact(Long id) {
        if (!contactRepository.existsById(id)) {
            logger.error("Contact not found with id: {}", id);
            throw new NoSuchElementException("Contact not found");
        }
        logger.debug("Deleting contact with id: {}", id);
        contactRepository.deleteById(id);
    }

    public Contact getContactById(Long id){
        logger.debug("Getting contact by id: {}", id);
        return contactRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Contact not found"));
    }

}
