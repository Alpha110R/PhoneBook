package com.example.risephonebook;

import com.example.risephonebook.entity.Contact;
import com.example.risephonebook.exception.BadRequestException;
import com.example.risephonebook.repository.ContactRepository;
import com.example.risephonebook.service.ContactService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContactServiceTest {

    @Mock
    private ContactRepository contactRepository;

    @InjectMocks
    private ContactService contactService;

    private Contact contact;
    private Contact updatedContact;

    @BeforeEach
    void setUp() {
        contact = Contact.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .phone("1234567890")
                .address("123 Main St")
                .build();

        updatedContact = Contact.builder()
                .id(1L)
                .firstName("John")
                .lastName("Smith")
                .phone("1234567890")
                .address("456 Main St")
                .build();
    }

    @Test
    void shouldGetContacts() {
        Page<Contact> page = new PageImpl<>(Collections.singletonList(contact));
        when(contactRepository.findAll(any(Example.class), any(Pageable.class))).thenReturn(page);

        Page<Contact> result = contactService.getContacts(PageRequest.of(0, 10), "1", "John", "Doe", "1234567890", "123 Main St");

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getFirstName()).isEqualTo("John");
    }

    @Test
    void shouldGetContactsWithEmptyFilter() {
        Page<Contact> page = new PageImpl<>(Collections.singletonList(contact));
        when(contactRepository.findAll(any(Example.class), any(Pageable.class))).thenReturn(page);

        Page<Contact> result = contactService.getContacts(PageRequest.of(0, 10), "", "", "", "", "");

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getFirstName()).isEqualTo("John");
    }

    @Test
    void shouldThrowExceptionForInvalidIdFormat() {
        assertThrows(BadRequestException.class, () -> contactService.getContacts(PageRequest.of(0, 10), "invalid", "", "", "", ""));
    }

    @Test
    void shouldThrowExceptionForNullPageable() {
        assertThrows(BadRequestException.class, () -> contactService.getContacts(null, "1", "John", "Doe", "1234567890", "123 Main St"));
    }

    @Test
    void shouldAddContact() {
        when(contactRepository.save(any(Contact.class))).thenReturn(contact);

        Contact result = contactService.addContact(contact);

        assertThat(result.getFirstName()).isEqualTo("John");
    }

    @Test
    void shouldThrowExceptionForNullContact() {
        assertThrows(BadRequestException.class, () -> contactService.addContact(null));
    }

    @Test
    void shouldThrowExceptionForContactPhoneInvalid() {
        contact.setPhone("123asdf");
        assertThrows(BadRequestException.class, () -> contactService.addContact(contact));
    }

    @Test
    void shouldThrowExceptionForContactNameInvalid() {
        contact.setFirstName("asdf123!@#");
        assertThrows(BadRequestException.class, () -> contactService.addContact(contact));
    }

    @Test
    void shouldThrowExceptionForInvalidContact() {
        Contact invalidContact = new Contact();
        assertThrows(BadRequestException.class, () -> contactService.addContact(invalidContact));
    }

    @Test
    void shouldUpdateContact() {
        when(contactRepository.findById(anyLong())).thenReturn(Optional.of(contact));
        when(contactRepository.save(any(Contact.class))).thenReturn(contact);

        Contact result = contactService.updateContact(1L, updatedContact);

        assertThat(result.getLastName()).isEqualTo("Smith");
        assertThat(result.getAddress()).isEqualTo("456 Main St");
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentContact() {
        when(contactRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> contactService.updateContact(1L, updatedContact));
    }

    @Test
    void shouldThrowExceptionWhenGetContactNotFound() {
        when(contactRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> contactService.getContactById(1L));
    }

    @Test
    void shouldDeleteContactWhenExists() {
        when(contactRepository.existsById(eq(1L))).thenReturn(true);
        doNothing().when(contactRepository).deleteById(eq(1L));

        contactService.deleteContact(1L);
    }

    @Test
    void shouldThrowExceptionWhenContactNotFound() {
        when(contactRepository.existsById(eq(1L))).thenReturn(false);

        assertThrows(NoSuchElementException.class, () -> contactService.deleteContact(1L));
    }


    @Test
    void shouldSearchContacts() {
        when(contactRepository.findByFirstNameContainingOrLastNameContaining(anyString(), anyString()))
                .thenReturn(Collections.singletonList(contact));

        List<Contact> result = contactService.searchContacts("John");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFirstName()).isEqualTo("John");
    }

    @Test
    void shouldThrowExceptionForEmptySearchQuery() {
        assertThrows(BadRequestException.class, () -> contactService.searchContacts(""));
    }
}
