package org.delcom.app.interceptors;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.delcom.app.configs.AuthContext;
import org.delcom.app.entities.AuthToken;
import org.delcom.app.entities.User;
import org.delcom.app.services.AuthTokenService;
import org.delcom.app.services.UserService;
import org.delcom.app.utils.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthInterceptorTest {
    @Mock AuthContext authContext;
    @Mock AuthTokenService authTokenService;
    @Mock UserService userService;
    @Mock HttpServletRequest request;
    @Mock HttpServletResponse response;
    @InjectMocks AuthInterceptor interceptor;
    
    private final StringWriter responseWriter = new StringWriter();
    
    // Setup untuk menangkap output dari sendErrorResponse
    private void setupErrorResponse() throws Exception {
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));
        // Setup default response headers for verification
        doNothing().when(response).setStatus(anyInt());
        doNothing().when(response).setContentType(anyString());
        doNothing().when(response).setCharacterEncoding(anyString());
    }
    
    // Helper untuk mendapatkan pesan error
    private String getErrorMessage() {
        return responseWriter.toString();
    }


    // --- Success Paths ---

    @Test
    void preHandle_PublicEndpoint_Success() throws Exception {
        // Covers: isPublicEndpoint(request) returns true
        when(request.getRequestURI()).thenReturn("/api/auth/login");
        assertTrue(interceptor.preHandle(request, response, new Object()));
        verifyNoInteractions(authContext);
    }
    
    @Test
    void preHandle_ErrorEndpoint_Success() throws Exception {
        // Covers: isPublicEndpoint(request) returns true for /error
        when(request.getRequestURI()).thenReturn("/error");
        assertTrue(interceptor.preHandle(request, response, new Object()));
        verifyNoInteractions(authContext);
    }

    @Test
    void preHandle_ValidToken_Success() throws Exception {
        UUID uid = UUID.randomUUID();
        String token = "validToken";
        User mockUser = new User();
        AuthToken mockAuthToken = new AuthToken(uid, token);
        
        // Mock static methods
        try (MockedStatic<JwtUtil> mocked = mockStatic(JwtUtil.class)) {
            mocked.when(() -> JwtUtil.extractUserId(token)).thenReturn(uid);
            mocked.when(() -> JwtUtil.validateToken(token, true)).thenReturn(true);
            
            when(request.getRequestURI()).thenReturn("/api/secure");
            when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
            when(authTokenService.findUserToken(uid, token)).thenReturn(mockAuthToken);
            when(userService.getUserById(uid)).thenReturn(mockUser);

            assertTrue(interceptor.preHandle(request, response, new Object()));
            verify(authContext, times(1)).setAuthUser(mockUser);
        }
    }


    // --- Failure Paths (Mencakup Red Lines) ---

    @Test
    void preHandle_Failure_NoRawTokenHeader() throws Exception {
        // Covers: rawAuthToken = null; -> extractToken returns null
        when(request.getRequestURI()).thenReturn("/api/secure");
        when(request.getHeader("Authorization")).thenReturn(null);
        setupErrorResponse();

        assertFalse(interceptor.preHandle(request, response, new Object()));
        verify(response).setStatus(401);
        assertTrue(getErrorMessage().contains("Token autentikasi tidak ditemukan"));
    }

    @Test
    void preHandle_Failure_NotBearerToken() throws Exception {
        // Covers: extractToken returns null (tidak diawali "Bearer ")
        when(request.getRequestURI()).thenReturn("/api/secure");
        when(request.getHeader("Authorization")).thenReturn("Basic user:pass");
        setupErrorResponse();

        assertFalse(interceptor.preHandle(request, response, new Object()));
        verify(response).setStatus(401);
        assertTrue(getErrorMessage().contains("Token autentikasi tidak ditemukan"));
    }

    @Test
    void preHandle_Failure_EmptyToken() throws Exception {
        // Covers: token.isEmpty() branch
        when(request.getRequestURI()).thenReturn("/api/secure");
        when(request.getHeader("Authorization")).thenReturn("Bearer "); // token akan menjadi ""
        setupErrorResponse();

        assertFalse(interceptor.preHandle(request, response, new Object()));
        verify(response).setStatus(401);
        assertTrue(getErrorMessage().contains("Token autentikasi tidak ditemukan"));
    }

    @Test
    void preHandle_Failure_InvalidJwtFormat() throws Exception {
        // Covers: !JwtUtil.validateToken(token, true)
        String token = "invalidJwtToken";
        
        try (MockedStatic<JwtUtil> mocked = mockStatic(JwtUtil.class)) {
            mocked.when(() -> JwtUtil.validateToken(token, true)).thenReturn(false);
            
            when(request.getRequestURI()).thenReturn("/api/secure");
            when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
            setupErrorResponse();

            assertFalse(interceptor.preHandle(request, response, new Object()));
            verify(response).setStatus(401);
            assertTrue(getErrorMessage().contains("Token autentikasi tidak valid"));
        }
    }

    @Test
    void preHandle_Failure_UserIdExtractionFails() throws Exception {
        // Covers: UUID userId = null (JwtUtil.extractUserId returns null)
        String token = "validFormatButBadContentToken";

        try (MockedStatic<JwtUtil> mocked = mockStatic(JwtUtil.class)) {
            mocked.when(() -> JwtUtil.validateToken(token, true)).thenReturn(true);
            mocked.when(() -> JwtUtil.extractUserId(token)).thenReturn(null);
            
            when(request.getRequestURI()).thenReturn("/api/secure");
            when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
            setupErrorResponse();

            assertFalse(interceptor.preHandle(request, response, new Object()));
            verify(response).setStatus(401);
            assertTrue(getErrorMessage().contains("Format token autentikasi tidak valid"));
        }
    }

    @Test
    void preHandle_Failure_AuthTokenNotFound() throws Exception {
        // Covers: authToken == null (Token expired/revoked dari DB)
        UUID uid = UUID.randomUUID();
        String token = "expiredToken";

        try (MockedStatic<JwtUtil> mocked = mockStatic(JwtUtil.class)) {
            mocked.when(() -> JwtUtil.validateToken(token, true)).thenReturn(true);
            mocked.when(() -> JwtUtil.extractUserId(token)).thenReturn(uid);

            when(request.getRequestURI()).thenReturn("/api/secure");
            when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
            when(authTokenService.findUserToken(uid, token)).thenReturn(null); // AuthToken not found
            setupErrorResponse();

            assertFalse(interceptor.preHandle(request, response, new Object()));
            verify(response).setStatus(401);
            assertTrue(getErrorMessage().contains("Token autentikasi sudah expired"));
        }
    }

    @Test
    void preHandle_Failure_UserNotFound() throws Exception {
        // Covers: authUser == null (User dihapus dari sistem)
        UUID uid = UUID.randomUUID();
        String token = "validToken";
        AuthToken mockAuthToken = new AuthToken(uid, token);

        try (MockedStatic<JwtUtil> mocked = mockStatic(JwtUtil.class)) {
            mocked.when(() -> JwtUtil.validateToken(token, true)).thenReturn(true);
            mocked.when(() -> JwtUtil.extractUserId(token)).thenReturn(uid);

            when(request.getRequestURI()).thenReturn("/api/secure");
            when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
            when(authTokenService.findUserToken(uid, token)).thenReturn(mockAuthToken);
            when(userService.getUserById(uid)).thenReturn(null); // User not found
            setupErrorResponse();

            assertFalse(interceptor.preHandle(request, response, new Object()));
            verify(response).setStatus(404);
            assertTrue(getErrorMessage().contains("User tidak ditemukan"));
        }
    }
}