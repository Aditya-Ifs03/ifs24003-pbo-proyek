package org.delcom.app.services;

import org.delcom.app.entities.AuthToken;
import org.delcom.app.repositories.AuthTokenRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthTokenServiceTest {
    
    @Mock 
    AuthTokenRepository repo; // Mock untuk repository
    
    @InjectMocks 
    AuthTokenService service; // Service yang akan diuji (meng-cover constructor)

    private final UUID testUserId = UUID.randomUUID();
    private final String testToken = "dummy-token";
    
    // Helper untuk membuat AuthToken
    private AuthToken createTestAuthToken() {
        return new AuthToken(testUserId, testToken);
    }
    
    @Test
    void testFindUserToken_ShouldReturnToken() {
        // Arrange
        AuthToken mockToken = createTestAuthToken();
        // Mocking: Ketika repo.findUserToken dipanggil, kembalikan mockToken
        when(repo.findUserToken(testUserId, testToken)).thenReturn(mockToken);
        
        // Act
        AuthToken result = service.findUserToken(testUserId, testToken);
        
        // Assert & Verify (Covers findUserToken method)
        assertNotNull(result, "Resulting token should not be null");
        assertEquals(mockToken, result, "The returned token should be the mocked token");
        verify(repo, times(1)).findUserToken(testUserId, testToken);
    }
    
    @Test
    void testCreateAuthToken_ShouldReturnSavedToken() {
        // Arrange
        AuthToken tokenToSave = createTestAuthToken();
        AuthToken savedToken = createTestAuthToken();
        savedToken.setId(UUID.randomUUID()); // Simulasikan ID terisi setelah save
        
        // Mocking: Ketika repo.save() dipanggil, kembalikan savedToken
        when(repo.save(tokenToSave)).thenReturn(savedToken);
        
        // Act
        AuthToken result = service.createAuthToken(tokenToSave);
        
        // Assert & Verify (Covers createAuthToken method)
        assertEquals(savedToken.getId(), result.getId(), "The returned token should have the new ID");
        verify(repo, times(1)).save(tokenToSave);
    }
    
    @Test
    void testDeleteAuthToken_ShouldCallDeleteByUserId() {
        // Act (Covers deleteAuthToken method)
        service.deleteAuthToken(testUserId);
        
        // Verify: Memastikan repo.deleteByUserId dipanggil dengan userId yang benar
        verify(repo, times(1)).deleteByUserId(testUserId);
    }
}