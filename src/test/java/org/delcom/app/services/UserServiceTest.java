package org.delcom.app.services;

import org.delcom.app.entities.User;
import org.delcom.app.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void testCreateUser() {
        User user = new User("Test", "test@mail.com", "pass");
        when(userRepository.save(any(User.class))).thenReturn(user);

        User result = userService.createUser("Test", "test@mail.com", "pass");
        assertNotNull(result);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testGetUserByEmail() {
        User user = new User();
        when(userRepository.findFirstByEmail("test@mail.com")).thenReturn(Optional.of(user));

        User result = userService.getUserByEmail("test@mail.com");
        assertNotNull(result);
    }

    @Test
    void testGetUserById() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.of(new User()));

        User result = userService.getUserById(id);
        assertNotNull(result);
    }

    // --- Update User Tests ---

    @Test
    void testUpdateUser_Success() {
        UUID id = UUID.randomUUID();
        User existing = new User("Old", "old@mail.com", "pass");
        when(userRepository.findById(id)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]);

        User result = userService.updateUser(id, "New", "new@mail.com");
        
        assertEquals("New", result.getName());
        assertEquals("new@mail.com", result.getEmail());
    }

    @Test
    void testUpdateUser_NotFound() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        User result = userService.updateUser(id, "New", "new@mail.com");
        assertNull(result);
        verify(userRepository, never()).save(any());
    }

    // --- Update Password Tests ---

    @Test
    void testUpdatePassword_Success() {
        UUID id = UUID.randomUUID();
        User existing = new User();
        when(userRepository.findById(id)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]);

        User result = userService.updatePassword(id, "newPass");
        assertEquals("newPass", result.getPassword());
    }

    @Test
    void testUpdatePassword_NotFound() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        User result = userService.updatePassword(id, "newPass");
        assertNull(result);
    }
}