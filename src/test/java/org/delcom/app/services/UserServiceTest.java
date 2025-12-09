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
    void crud() {
        User u = new User("a", "b", "c");
        UUID id = UUID.randomUUID();
        when(repo.save(any())).thenReturn(u);
        when(repo.findById(id)).thenReturn(Optional.of(u));

        assertNotNull(service.createUser("a", "b", "c"));
        assertNotNull(service.getUserById(id));
        service.updateUser(id, "new", "email");
        service.updatePassword(id, "pass");
    }
}