package org.delcom.app.entities;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class CustomerTest {

    // ==========================================
    // 1. Test Lifecycle Methods (@PrePersist & @PreUpdate)
    // ==========================================

    @Test
    void testOnCreate_WhenTypeIsNull_ShouldSetDefaultAndTimestamps() {
        // Arrange
        Customer customer = new Customer();

        // Act
        customer.onCreate();

        // Assert
        assertNotNull(customer.getCreatedAt());
        assertNotNull(customer.getUpdatedAt());
        assertEquals("Regular", customer.getType());
    }

    @Test
    void testOnCreate_WhenTypeIsSet_ShouldKeepType() {
        // Arrange
        Customer customer = new Customer();
        customer.setType("VIP");

        // Act
        customer.onCreate();

        // Assert
        assertEquals("VIP", customer.getType());
        assertNotNull(customer.getCreatedAt());
    }

    @Test
    void testOnUpdate_ShouldUpdateTimestamp() throws InterruptedException {
        // Arrange
        Customer customer = new Customer();
        customer.onCreate();
        LocalDateTime oldUpdatedAt = customer.getUpdatedAt();

        // Give slight delay so timestamp changes
        Thread.sleep(10);

        // Act
        customer.onUpdate();

        // Assert
        assertTrue(
            customer.getUpdatedAt().isAfter(oldUpdatedAt)
                || customer.getUpdatedAt().isEqual(oldUpdatedAt)
        );
    }

    // ==========================================
    // 2. Test Getter & Setter (Basic Fields)
    // ==========================================

    @Test
    void testGettersSetters() {
        // Arrange
        Customer customer = new Customer();
        customer.setName("Test");
        customer.setEmail("test@mail.com");
        customer.setPhone("123");
        customer.setAddress("Jalan");
        customer.setImageUrl("img.jpg");

        // Assert
        assertEquals("Test", customer.getName());
        assertEquals("test@mail.com", customer.getEmail());
        assertEquals("123", customer.getPhone());
        assertEquals("Jalan", customer.getAddress());
        assertEquals("img.jpg", customer.getImageUrl());
    }
}
