package org.delcom.app.configs;

import org.delcom.app.entities.User;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AuthContextTest {
    
    @Test
    void testAuthContextMethodsAndAuthenticationFlow() {
        AuthContext context = new AuthContext();
        User user = new User("Test", "test@test.com", "pass");
        
        // 1. Test Initial State (getAuthUser() dan isAuthenticated() -> false)
        assertNull(context.getAuthUser(), "AuthUser should be null initially.");
        assertFalse(context.isAuthenticated(), "isAuthenticated() should be false when user is null."); 
        
        // 2. Test setAuthUser (Setter)
        context.setAuthUser(user);
        
        // 3. Test State After Setting (getAuthUser() dan isAuthenticated() -> true)
        assertEquals(user, context.getAuthUser(), "AuthUser should return the set user object.");
        assertTrue(context.isAuthenticated(), "isAuthenticated() should be true when user is set."); 
        
        // Opsional: Test setAuthUser(null) untuk reset
        context.setAuthUser(null);
        assertNull(context.getAuthUser(), "AuthUser should be null after setting null.");
        assertFalse(context.isAuthenticated(), "isAuthenticated() should be false after setting null.");
    }
}