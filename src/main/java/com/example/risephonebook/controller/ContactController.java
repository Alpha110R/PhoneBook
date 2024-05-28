package com.example.risephonebook.controller;

import com.example.risephonebook.entity.Contact;
import com.example.risephonebook.service.ContactService;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;
import org.slf4j.LoggerFactory;


import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/contacts")
public class ContactController {
    private static final Logger logger = LoggerFactory.getLogger(ContactController.class);

    @Autowired
    private ContactService contactService;

    @GetMapping
    public Page<Contact> getContacts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String direction,
            @RequestParam(required = false) String id,
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String address) {

        logger.debug("Fetching contacts with page: {}, size: {}, sort: {}, direction: {}, id: {}, firstName: {}, lastName: {}, phone: {}, address: {}",
                page, size, sort, direction, id, firstName, lastName, phone, address);

        Sort sortCriteria = getSortType(direction, sort);
        PageRequest pageRequest = PageRequest.of(page, size, sortCriteria);
        return contactService.getContacts(pageRequest, id, firstName, lastName, phone, address);
    }

    private Sort getSortType(String direction, String sort){
        Sort.Direction sortDirection = getDirection(direction);
        return (Objects.nonNull(sort) && !sort.isEmpty()) ? Sort.by(sortDirection, sort) : Sort.unsorted();
    }

    private Sort.Direction getDirection(String direction){
        return Objects.nonNull(direction) && direction.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
    }

    @PostMapping
    public Contact addContact(@RequestBody Contact contact) {
        logger.debug("Adding contact: {}", contact);
        return contactService.addContact(contact);
    }

    @GetMapping("/search")
    public List<Contact> searchContacts(@RequestParam String query) {
        logger.debug("Searching contacts with query: {}", query);
        return contactService.searchContacts(query);
    }

    @GetMapping("/{id}")
    public Contact getContact(@PathVariable Long id) {
        logger.debug("Fetching contact with id: {}", id);
        return contactService.getContactById(id);
    }

    @PutMapping("/{id}")
    public Contact updateContact(@PathVariable Long id, @RequestBody Contact contactDetails) {
        logger.debug("Updating contact with id: {}, details: {}", id, contactDetails);
        return contactService.updateContact(id, contactDetails);
    }

    @DeleteMapping("/{id}")
    public void deleteContact(@PathVariable Long id) {
        logger.debug("Deleting contact with id: {}", id);
        contactService.deleteContact(id);
    }
}