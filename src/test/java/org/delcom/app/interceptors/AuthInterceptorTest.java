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

    @Test
    void preHandle_Public() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/auth/login");
        assertTrue(interceptor.preHandle(request, response, new Object()));
    }

    @Test
    void preHandle_ValidToken() throws Exception {
        UUID uid = UUID.randomUUID();
        String token = JwtUtil.generateToken(uid);
        
        when(request.getRequestURI()).thenReturn("/api/secure");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(authTokenService.findUserToken(any(), any())).thenReturn(new AuthToken());
        when(userService.getUserById(any())).thenReturn(new User());

        assertTrue(interceptor.preHandle(request, response, new Object()));
    }
    
    @Test
    void preHandle_Invalid() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/secure");
        when(response.getWriter()).thenReturn(new PrintWriter(new StringWriter()));
        assertFalse(interceptor.preHandle(request, response, new Object()));
    }
}