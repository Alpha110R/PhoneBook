package com.example.risephonebook.utils;

import com.example.risephonebook.entity.Contact;
import com.example.risephonebook.exception.BadRequestException;
import org.springframework.data.domain.Pageable;

import java.util.Objects;
import java.util.regex.Pattern;

public class Validation {
    public static void validateContact(Contact contact) {
        if (Objects.isNull(contact)) {
            throw new BadRequestException("Contact cannot be null");
        }
        if (Objects.isNull(contact.getFirstName()) || contact.getFirstName().isEmpty()) {
            throw new BadRequestException("First name cannot be null or empty");
        }
        if (Objects.nonNull(contact.getPhone()) && !contact.getPhone().isEmpty()) {
            if (!Pattern.matches("[0-9]+", contact.getPhone())) {
                throw new BadRequestException("Phone must be only digits");
            }
        }
        if (Objects.nonNull(contact.getFirstName()) && !contact.getFirstName().isEmpty()) {
            if (!Pattern.matches("^[a-zA-Z0-9]+$", contact.getFirstName())) {
                throw new BadRequestException("Name must be only with digits or letters");
            }
        }
        if (Objects.nonNull(contact.getLastName()) && !contact.getLastName().isEmpty()) {
            if (!Pattern.matches("^[a-zA-Z0-9]+$", contact.getLastName())) {
                throw new BadRequestException("Name must be only with digits or letters");
            }
        }
    }

    public static void validatePageable(Pageable pageable) {
        if (Objects.isNull(pageable)) {
            throw new BadRequestException("Pageable cannot be null");
        }
    }

    public static void validateQueryNotNullOrEmpty(String query) {
        if (Objects.isNull(query) || query.isEmpty()) {
            throw new BadRequestException("Search query cannot be null or empty");
        }
    }
}
