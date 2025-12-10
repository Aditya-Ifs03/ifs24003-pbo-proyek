package org.delcom.app.services;

import org.delcom.app.entities.User;
import org.delcom.app.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    // InjectMocks mencakup coverage untuk constructor
    @InjectMocks UserService userService;

    private User testUser;
    private final UUID ID = UUID.randomUUID();
    private final String EMAIL = "test@example.com";
    private final String RAW_PASS = "rawpass";
    private final String ENCODED_PASS = "encodedpass";

    @BeforeEach
    void setUp() {
        testUser = new User("Test", EMAIL, ENCODED_PASS);
        testUser.setId(ID);
    }

    @Test
    void testFindById_ShouldReturnOptional() {
        // 1. Found (Covers Optional.of path)
        when(userRepository.findById(ID)).thenReturn(Optional.of(testUser));
        assertTrue(userService.findById(ID).isPresent(), "User should be found by ID.");

        // 2. Not Found (Covers Optional.empty path)
        when(userRepository.findById(any())).thenReturn(Optional.empty());
        assertFalse(userService.findById(UUID.randomUUID()).isPresent(), "User should not be found.");
    }

    @Test
    void testFindByEmail_ShouldReturnOptional() {
        // 1. Found (Covers Optional.of path)
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(testUser));
        assertTrue(userService.findByEmail(EMAIL).isPresent(), "User should be found by email.");

        // 2. Not Found (Covers Optional.empty path)
        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());
        assertFalse(userService.findByEmail("nonexistent@example.com").isPresent(), "User should not be found.");
    }

    @Test
    void testCreateUser_ShouldEncodePasswordAndSave() {
        User newUser = new User("New", "new@test.com", RAW_PASS);

        // Mocking: Encodes raw password
        when(passwordEncoder.encode(RAW_PASS)).thenReturn(ENCODED_PASS);
        
        // Mocking: save returns the user (simulating repository)
        when(userRepository.save(any(User.class))).thenAnswer(i -> {
            User savedUser = i.getArgument(0);
            return savedUser;
        });

        User result = userService.createUser(newUser);

        // Assert: Ensure password was encoded and save was called
        assertEquals(ENCODED_PASS, result.getPassword(), "Password should be encoded.");
        
        // Verify: Memastikan encode dan save dipanggil (Covers createUser method)
        verify(passwordEncoder, times(1)).encode(RAW_PASS);
        verify(userRepository, times(1)).save(newUser);
    }

    @Test
    void testVerifyPassword_Match() {
        // Covers: return true path
        when(passwordEncoder.matches(RAW_PASS, testUser.getPassword())).thenReturn(true);
        assertTrue(userService.verifyPassword(testUser, RAW_PASS), "Password should match.");
        verify(passwordEncoder, times(1)).matches(RAW_PASS, testUser.getPassword());
    }
    
    @Test
    void testVerifyPassword_NoMatch() {
        // Covers: return false path
        when(passwordEncoder.matches(RAW_PASS, testUser.getPassword())).thenReturn(false);
        assertFalse(userService.verifyPassword(testUser, RAW_PASS), "Password should not match.");
        verify(passwordEncoder, times(1)).matches(RAW_PASS, testUser.getPassword());
    }
}