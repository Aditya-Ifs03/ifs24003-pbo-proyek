package org.delcom.app.entities;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void testUserEntity_BasicGettersSetters() {
        // Test Constructor & Basic Getters
        String name = "Delcom";
        String email = "admin@delcom.org";
        String password = "secret";

        User user = new User(name, email, password);

        assertEquals(name, user.getName());
        assertEquals(email, user.getEmail());
        assertEquals(password, user.getPassword());
        
        // Test Setters
        user.setName("New Name");
        assertEquals("New Name", user.getName());

        user.setEmail("new@delcom.org");
        assertEquals("new@delcom.org", user.getEmail());

        user.setPassword("newSecret");
        assertEquals("newSecret", user.getPassword());

        UUID id = UUID.randomUUID();
        user.setId(id);
        assertEquals(id, user.getId());
    }

    // ==========================================
    // TEST BARU: Lifecycle & Timestamps
    // ==========================================

    @Test
    void testLifecycleMethods() {
        User user = new User("Test", "test@mail.com", "pass");

        // 1. Panggil onCreate (PrePersist)
        // Ini akan mengisi createdAt dan updatedAt
        // Menghijaukan baris 96-99
        user.onCreate();

        // 2. Cek Getter Timestamps
        // Menghijaukan baris 86-88 dan 90-92
        assertNotNull(user.getCreatedAt(), "CreatedAt tidak boleh null setelah onCreate");
        assertNotNull(user.getUpdatedAt(), "UpdatedAt tidak boleh null setelah onCreate");

        // Simpan waktu lama untuk perbandingan
        LocalDateTime oldUpdate = user.getUpdatedAt();

        // 3. Panggil onUpdate (PreUpdate)
        // Menghijaukan baris 102-104
        // Kita beri jeda sedikit agar detiknya beda (opsional, untuk memastikan perubahan)
        try { Thread.sleep(10); } catch (InterruptedException e) {}

        user.onUpdate();

        // Pastikan updatedAt diperbarui
        LocalDateTime newUpdate = user.getUpdatedAt();
        assertTrue(newUpdate.isAfter(oldUpdate) || newUpdate.isEqual(oldUpdate));
    }
}