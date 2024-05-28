package com.example.risephonebook;

import com.example.risephonebook.controller.ContactController;
import com.example.risephonebook.entity.Contact;
import com.example.risephonebook.exception.BadRequestException;
import com.example.risephonebook.service.ContactService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ContactController.class)
class ContactControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ContactService contactService;

    @Autowired
    private ObjectMapper objectMapper;

    private Contact contact;

    @BeforeEach
    void setUp() {
        contact = Contact.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .phone("1234567890")
                .address("123 Main St")
                .build();
    }

    @Test
    void shouldGetContacts() throws Exception {
        Mockito.when(contactService.getContacts(any(PageRequest.class), eq("1"), eq("John"), eq("Doe"), eq("1234567890"), eq("123 Main St")))
                .thenReturn(new PageImpl<>(Collections.singletonList(contact)));

        MvcResult result = mockMvc.perform(get("/api/contacts")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "firstName")
                        .param("direction", "asc")
                        .param("id", "1")
                        .param("firstName", "John")
                        .param("lastName", "Doe")
                        .param("phone", "1234567890")
                        .param("address", "123 Main St"))
                .andExpect(status().isOk())
                .andReturn();

        assertThat(result.getResponse().getContentAsString()).contains("John");
    }

    @Test
    void shouldGetContactsWithEmptyFilter() throws Exception {
        List<Contact> contacts = Collections.singletonList(contact);
        Page<Contact> contactPage = new PageImpl<>(contacts);
        Mockito.when(contactService.getContacts(any(PageRequest.class), eq(null), eq(null), eq(null), eq(null), eq(null)))
                .thenReturn(contactPage);

        MvcResult result = mockMvc.perform(get("/api/contacts")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "firstName")
                        .param("direction", "asc"))
                .andExpect(status().isOk())
                .andReturn();

        // Log the response
        String jsonResponse = result.getResponse().getContentAsString();
        System.out.println("JSON Response: " + jsonResponse);
        System.out.println("Expected JSON: " + objectMapper.writeValueAsString(new PageImpl<>(contacts)));

        // Verify the response content
        assertThat(jsonResponse).contains("John");
    }

    @Test
    void shouldAddContact() throws Exception {
        Mockito.when(contactService.addContact(any(Contact.class))).thenReturn(contact);

        String contactJson = objectMapper.writeValueAsString(contact);

        MvcResult result = mockMvc.perform(post("/api/contacts")
                        .contentType("application/json")
                        .content(contactJson))
                .andExpect(status().isOk())
                .andReturn();

        assertThat(result.getResponse().getContentAsString()).contains("John");
    }

    @Test
    void shouldUpdateContact() throws Exception {
        Mockito.when(contactService.updateContact(anyLong(), any(Contact.class))).thenReturn(contact);

        String contactJson = objectMapper.writeValueAsString(contact);

        MvcResult result = mockMvc.perform(put("/api/contacts/1")
                        .contentType("application/json")
                        .content(contactJson))
                .andExpect(status().isOk())
                .andReturn();

        assertThat(result.getResponse().getContentAsString()).contains("John");
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentContact() throws Exception {
        Mockito.when(contactService.updateContact(anyLong(), any(Contact.class))).thenThrow(new NoSuchElementException("Contact not found"));

        String contactJson = objectMapper.writeValueAsString(contact);

        mockMvc.perform(put("/api/contacts/1")
                        .contentType("application/json")
                        .content(contactJson))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteContact() throws Exception {
        mockMvc.perform(delete("/api/contacts/1"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentContact() throws Exception {
        Mockito.doThrow(new NoSuchElementException("Contact not found")).when(contactService).deleteContact(anyLong());

        mockMvc.perform(delete("/api/contacts/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldGetContactById() throws Exception {
        Mockito.when(contactService.getContactById(anyLong())).thenReturn(contact);
        String contactJson = objectMapper.writeValueAsString(contact);

        MvcResult result = mockMvc.perform(get("/api/contacts/1"))
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = result.getResponse().getContentAsString();
        System.out.println("JSON Response: " + jsonResponse);
        System.out.println("Expected JSON: " + contactJson);

        assertThat(jsonResponse).isEqualTo(contactJson);
        assertThat(jsonResponse).contains("John");
    }

    @Test
    void shouldThrowExceptionWhenGettingNonExistentContactById() throws Exception {
        Mockito.when(contactService.getContactById(anyLong())).thenThrow(new NoSuchElementException("Contact not found"));

        mockMvc.perform(get("/api/contacts/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldSearchContacts() throws Exception {
        Mockito.when(contactService.searchContacts(anyString())).thenReturn(Collections.singletonList(contact));

        MvcResult result = mockMvc.perform(get("/api/contacts/search")
                        .param("query", "John"))
                .andExpect(status().isOk())
                .andReturn();

        assertThat(result.getResponse().getContentAsString()).contains("John");
    }

    @Test
    void shouldHandleNoSuchElementException() throws Exception {
        Mockito.when(contactService.getContactById(anyLong())).thenThrow(new NoSuchElementException("Contact not found"));

        mockMvc.perform(get("/api/contacts/999"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Resource not found"));
    }

    @Test
    void shouldHandleBadRequestException() throws Exception {
        Mockito.doThrow(new BadRequestException("Bad request")).when(contactService).deleteContact(anyLong());

        mockMvc.perform(delete("/api/contacts/1"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Bad request"));
    }

    @Test
    void shouldGetContactsSortedAndPaged() throws Exception {
        List<Contact> contacts = Arrays.asList(contact, contact);
        Page<Contact> contactPage = new PageImpl<>(contacts);
        Mockito.when(contactService.getContacts(any(PageRequest.class), eq(null), eq(null), eq(null), eq(null), eq(null)))
                .thenReturn(contactPage);

        MvcResult result = mockMvc.perform(get("/api/contacts")
                        .param("page", "0")
                        .param("size", "2")
                        .param("sort", "lastName")
                        .param("direction", "desc"))
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = result.getResponse().getContentAsString();
        assertThat(jsonResponse).contains("John");
        assertThat(jsonResponse).contains("Doe");
    }
}