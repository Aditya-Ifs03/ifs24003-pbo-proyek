package org.delcom.app.services;

import org.delcom.app.entities.AuthToken;
import org.delcom.app.repositories.AuthTokenRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.UUID;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthTokenServiceTest {
    @Mock AuthTokenRepository repo;
    @InjectMocks AuthTokenService service;

    @Test
    void testAll() {
        UUID uid = UUID.randomUUID();
        when(repo.findUserToken(uid, "t")).thenReturn(new AuthToken());
        service.findUserToken(uid, "t");
        
        service.createAuthToken(new AuthToken());
        verify(repo).save(any());
        
        service.deleteAuthToken(uid);
        verify(repo).deleteByUserId(uid);
    }
}