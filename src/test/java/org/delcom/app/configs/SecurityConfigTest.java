package org.delcom.app.configs;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.DefaultSecurityFilterChain;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecurityConfigTest {

    @InjectMocks
    private SecurityConfig securityConfig;

    @Mock
    private HttpSecurity http;

    @Test
    void passwordEncoder_ShouldReturnBCrypt() {
        PasswordEncoder encoder = securityConfig.passwordEncoder();
        assertNotNull(encoder);
        assertTrue(encoder instanceof BCryptPasswordEncoder);
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"}) 
    void securityFilterChain_ShouldCoverAllLines() throws Exception {
        // --- 1. Persiapan Mocking ---
        ExceptionHandlingConfigurer<HttpSecurity> exceptionConfig = mock(ExceptionHandlingConfigurer.class);
        
        // Mock Registry & AuthorizedUrl
        AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry authRegistry = 
                mock(AuthorizeHttpRequestsConfigurer.AuthorizationManagerRequestMatcherRegistry.class);
        AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizedUrl authorizedUrl = 
                mock(AuthorizeHttpRequestsConfigurer.AuthorizedUrl.class);

        FormLoginConfigurer<HttpSecurity> formLoginConfig = mock(FormLoginConfigurer.class);
        LogoutConfigurer<HttpSecurity> logoutConfig = mock(LogoutConfigurer.class);
        RememberMeConfigurer<HttpSecurity> rememberMeConfig = mock(RememberMeConfigurer.class);

        // --- 2. Stubbing (Mengatur skenario) ---

        // Masuk ke dalam lambda
        when(http.authorizeHttpRequests(any(Customizer.class))).thenAnswer(invocation -> {
            Customizer c = invocation.getArgument(0);
            c.customize(authRegistry); 
            return http;
        });

        when(http.exceptionHandling(any(Customizer.class))).thenAnswer(invocation -> {
            Customizer c = invocation.getArgument(0);
            c.customize(exceptionConfig); 
            return http;
        });

        when(http.formLogin(any(Customizer.class))).thenAnswer(invocation -> {
            Customizer c = invocation.getArgument(0);
            c.customize(formLoginConfig);
            return http;
        });

        when(http.logout(any(Customizer.class))).thenAnswer(invocation -> {
            Customizer c = invocation.getArgument(0);
            c.customize(logoutConfig);
            return http;
        });

        when(http.rememberMe(any(Customizer.class))).thenAnswer(invocation -> {
            Customizer c = invocation.getArgument(0);
            c.customize(rememberMeConfig);
            return http;
        });

        // --- FIX NPE DISINI ---
        // Kita harus menyambung rantai method agar tidak return null
        when(authRegistry.requestMatchers(any(String[].class))).thenReturn(authorizedUrl);
        when(authorizedUrl.permitAll()).thenReturn(authRegistry); // Penting! Sambung balik ke registry
        when(authRegistry.anyRequest()).thenReturn(authorizedUrl);
        when(authorizedUrl.authenticated()).thenReturn(authRegistry); // Penting!

        // Mock method lainnya
        when(logoutConfig.logoutSuccessUrl(anyString())).thenReturn(logoutConfig);
        when(rememberMeConfig.key(anyString())).thenReturn(rememberMeConfig);
        when(rememberMeConfig.tokenValiditySeconds(anyInt())).thenReturn(rememberMeConfig);
        when(http.build()).thenReturn(mock(DefaultSecurityFilterChain.class));

        // --- 3. EKSEKUSI ---
        securityConfig.securityFilterChain(http);

        // --- 4. VERIFIKASI ---
        verify(authRegistry).requestMatchers(
                "/auth/**", "/assets/**", "/api/**", "/css/**", "/js/**", "/error"
        );
        verify(authorizedUrl).permitAll(); 
        verify(authRegistry).anyRequest();
        verify(authorizedUrl).authenticated();

        verify(formLoginConfig).disable();

        verify(logoutConfig).logoutSuccessUrl("/auth/login");
        verify(logoutConfig).permitAll();

        verify(rememberMeConfig).key("uniqueAndSecret");
        verify(rememberMeConfig).tokenValiditySeconds(86400);

        // --- 5. TEST Redirect Logic ---
        ArgumentCaptor<AuthenticationEntryPoint> entryPointCaptor = ArgumentCaptor.forClass(AuthenticationEntryPoint.class);
        verify(exceptionConfig).authenticationEntryPoint(entryPointCaptor.capture());

        AuthenticationEntryPoint capturedEntryPoint = entryPointCaptor.getValue();
        HttpServletRequest mockReq = mock(HttpServletRequest.class);
        HttpServletResponse mockRes = mock(HttpServletResponse.class);

        capturedEntryPoint.commence(mockReq, mockRes, null);

        verify(mockRes).sendRedirect("/auth/login");
    }
}