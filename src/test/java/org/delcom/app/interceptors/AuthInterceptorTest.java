package org.delcom.app.interceptors;

import jakarta.servlet.http.HttpServletResponse;
import org.delcom.app.entities.User;
import org.delcom.app.utils.ConstUtil;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthInterceptorTest {

    private final AuthInterceptor authInterceptor = new AuthInterceptor();

    @Test
    void testPreHandle_PublicUrl_ShouldPass() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/auth/login"); // URL Publik
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = authInterceptor.preHandle(request, response, new Object());

        assertTrue(result);
    }

    @Test
    void testPreHandle_NotLoggedIn_ShouldRedirect() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setSession(new MockHttpSession()); // Session Kosong
        
        // --- PERBAIKAN: Set URI agar tidak null ---
        request.setRequestURI("/customers"); 
        
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = authInterceptor.preHandle(request, response, new Object());

        assertFalse(result);
        assertTrue(response.getRedirectedUrl().contains("/auth/login"));
    }

    @Test
    void testPreHandle_LoggedIn_ShouldPass() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(ConstUtil.SESSION_USER_KEY, new User());
        request.setSession(session);
        
        // --- PERBAIKAN: Set URI agar tidak null ---
        request.setRequestURI("/customers");

        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = authInterceptor.preHandle(request, response, new Object());

        assertTrue(result);
    }
}