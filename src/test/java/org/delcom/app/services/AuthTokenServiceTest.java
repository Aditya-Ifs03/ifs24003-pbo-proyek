package org.delcom.app.services;

import org.delcom.app.entities.AuthToken;
import org.delcom.app.repositories.AuthTokenRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthTokenServiceTest {

    @Mock
    private AuthTokenRepository authTokenRepository;

    @InjectMocks
    private AuthTokenService authTokenService;

    @Test
    void testFindUserToken() {
        UUID userId = UUID.randomUUID();
        String token = "sample-token";
        AuthToken expectedToken = new AuthToken();
        
        when(authTokenRepository.findUserToken(userId, token)).thenReturn(expectedToken);

        AuthToken result = authTokenService.findUserToken(userId, token);
        assertEquals(expectedToken, result);
    }

    @Test
    void testCreateAuthToken() {
        AuthToken authToken = new AuthToken();
        when(authTokenRepository.save(authToken)).thenReturn(authToken);

        AuthToken result = authTokenService.createAuthToken(authToken);
        assertEquals(authToken, result);
        verify(authTokenRepository).save(authToken);
    }

    @Test
    void testDeleteAuthToken() {
        UUID userId = UUID.randomUUID();
        authTokenService.deleteAuthToken(userId);
        verify(authTokenRepository).deleteByUserId(userId);
    }
}