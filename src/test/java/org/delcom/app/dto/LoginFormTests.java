package org.delcom.app.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class LoginFormTests {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Valid Login Form: Email & Password Benar")
    void testValidLoginForm() {
        LoginForm form = new LoginForm();
        form.setEmail("test@example.com");
        form.setPassword("password123");
        form.setRememberMe(true);

        Set<ConstraintViolation<LoginForm>> violations = validator.validate(form);

        // Harusnya tidak ada error
        assertTrue(violations.isEmpty());
        
        // Cek Getter
        assertEquals("test@example.com", form.getEmail());
        assertEquals("password123", form.getPassword());
        assertTrue(form.isRememberMe());
    }

    @Test
    @DisplayName("Invalid Login Form: Email Format Salah")
    void testInvalidEmailFormat() {
        LoginForm form = new LoginForm();
        form.setEmail("bukan-email"); // Format salah
        form.setPassword("password123");

        Set<ConstraintViolation<LoginForm>> violations = validator.validate(form);

        assertFalse(violations.isEmpty());
        // Pastikan errornya ada di field email
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("email")));
    }

    @Test
    @DisplayName("Invalid Login Form: Email Kosong")
    void testEmptyEmail() {
        LoginForm form = new LoginForm();
        form.setEmail(""); // Kosong
        form.setPassword("password123");

        Set<ConstraintViolation<LoginForm>> violations = validator.validate(form);
        assertFalse(violations.isEmpty());
    }

    @Test
    @DisplayName("Invalid Login Form: Password Kosong")
    void testEmptyPassword() {
        LoginForm form = new LoginForm();
        form.setEmail("test@example.com");
        form.setPassword(""); // Kosong

        Set<ConstraintViolation<LoginForm>> violations = validator.validate(form);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("password")));
    }
}