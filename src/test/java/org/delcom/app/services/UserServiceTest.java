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
    @Mock UserRepository repo;
    @InjectMocks UserService service;

    @Test
    void service_successPaths() {
        User u = new User("a", "b", "c");
        UUID id = UUID.randomUUID();
        
        // Mock success for creation
        when(repo.save(any())).thenReturn(u);
        assertNotNull(service.createUser("a", "b", "c"));

        // Mock success for lookups and updates
        when(repo.findById(id)).thenReturn(Optional.of(u));
        when(repo.findFirstByEmail(any())).thenReturn(Optional.of(u));

        // Test lookups
        assertNotNull(service.getUserById(id));
        assertNotNull(service.getUserByEmail("test@email.com"));

        // Test successful update (covers the update logic branch)
        User updatedUser = service.updateUser(id, "new", "email");
        assertNotNull(updatedUser);
        
        // Test successful password update (covers the update logic branch)
        User updatedPasswordUser = service.updatePassword(id, "pass");
        assertNotNull(updatedPasswordUser);
        
        verify(repo, times(3)).save(any());
    }

    @Test
    void service_notFoundPaths() {
        UUID nonExistentId = UUID.randomUUID();
        
        // Mock not found for all lookups
        when(repo.findById(nonExistentId)).thenReturn(Optional.empty());
        when(repo.findFirstByEmail(any())).thenReturn(Optional.empty());

        // Test lookups when not found (returns null)
        assertNull(service.getUserById(nonExistentId));
        assertNull(service.getUserByEmail("nonexistent@email.com"));

        // Test updateUser when not found (returns null) - covers the 'if (user == null)' branch
        assertNull(service.updateUser(nonExistentId, "new", "email")); 
        
        // Test updatePassword when not found (returns null) - covers the 'if (user == null)' branch
        assertNull(service.updatePassword(nonExistentId, "pass")); 
        
        verify(repo, never()).save(any());
    }
}