package org.delcom.app.services;

import org.delcom.app.entities.User;
import org.delcom.app.repositories.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    // --- 1. Test Create User (Sudah ada sebelumnya) ---
    @Test
    void testCreateUser_Success() {
        String name = "Test User";
        String email = "test@example.com";
        String password = "hashedpassword";
        
        User savedUser = new User(name, email, password);
        savedUser.setId(UUID.randomUUID());

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        User result = userService.createUser(name, email, password);

        Assertions.assertNotNull(result.getId());
        Assertions.assertEquals(email, result.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
    }

    // --- 2. Test Get User By Email (Sudah ada sebelumnya) ---
    @Test
    void testGetUserByEmail_Found() {
        String email = "test@example.com";
        User user = new User("Test", email, "pass");
        
        when(userRepository.findFirstByEmail(email)).thenReturn(Optional.of(user));

        User result = userService.getUserByEmail(email);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(email, result.getEmail());
    }

    @Test
    void testGetUserByEmail_NotFound() {
        String email = "notfound@example.com";
        when(userRepository.findFirstByEmail(email)).thenReturn(Optional.empty());

        User result = userService.getUserByEmail(email);

        Assertions.assertNull(result);
    }

    // ==========================================
    // 3. Test Get User By ID (Baris 29) - BARU
    // ==========================================

    @Test
    void testGetUserById_Found() {
        UUID id = UUID.randomUUID();
        User user = new User();
        user.setId(id);

        // Skenario: ID ditemukan
        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        User result = userService.getUserById(id);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(id, result.getId());
    }

    @Test
    void testGetUserById_NotFound() {
        UUID id = UUID.randomUUID();

        // Skenario: ID tidak ditemukan -> return null
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        User result = userService.getUserById(id);

        Assertions.assertNull(result);
    }

    // ==========================================
    // 4. Test Update User (Baris 33-41) - BARU
    // ==========================================

    @Test
    void testUpdateUser_Success() {
        UUID id = UUID.randomUUID();
        User existingUser = new User("Old Name", "old@mail.com", "pass");
        existingUser.setId(id);

        // Mocking: User ditemukan
        when(userRepository.findById(id)).thenReturn(Optional.of(existingUser));
        // Mocking: Save berhasil
        when(userRepository.save(existingUser)).thenReturn(existingUser);

        // Act
        User result = userService.updateUser(id, "New Name", "new@mail.com");

        // Assert
        Assertions.assertNotNull(result);
        Assertions.assertEquals("New Name", result.getName()); // Pastikan berubah
        Assertions.assertEquals("new@mail.com", result.getEmail());
        verify(userRepository).save(existingUser); // Pastikan tersimpan
    }

    @Test
    void testUpdateUser_NotFound() {
        UUID id = UUID.randomUUID();

        // Mocking: User tidak ditemukan (Optional.empty)
        // Ini menghijaukan Diamond di baris 35 (if user == null)
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        User result = userService.updateUser(id, "Name", "email");

        Assertions.assertNull(result);
        // Pastikan TIDAK PERNAH memanggil save
        verify(userRepository, never()).save(any());
    }

    // ==========================================
    // 5. Test Update Password (Baris 44-50) - BARU
    // ==========================================

    @Test
    void testUpdatePassword_Success() {
        UUID id = UUID.randomUUID();
        User existingUser = new User("Name", "email", "OldPass");
        existingUser.setId(id);

        when(userRepository.findById(id)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(existingUser)).thenReturn(existingUser);

        User result = userService.updatePassword(id, "NewPass123");

        Assertions.assertNotNull(result);
        Assertions.assertEquals("NewPass123", result.getPassword());
        verify(userRepository).save(existingUser);
    }

    @Test
    void testUpdatePassword_NotFound() {
        UUID id = UUID.randomUUID();

        // Mocking: User tidak ditemukan
        // Ini menghijaukan Diamond di baris 46 (if user == null)
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        User result = userService.updatePassword(id, "NewPass");

        Assertions.assertNull(result);
        verify(userRepository, never()).save(any());
    }
}