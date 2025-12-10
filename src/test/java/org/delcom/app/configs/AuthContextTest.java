package org.delcom.app.configs;

import org.delcom.app.entities.User;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AuthContextTest {
    
    @Test
    void testAuthContextMethods() {
        AuthContext context = new AuthContext();
        User user = new User("Test", "test@test.com", "pass");
        
        // 1. Test initial state (false branch of isAuthenticated)
        assertNull(context.getAuthUser());
        assertFalse(context.isAuthenticated()); // Covers isAuthenticated() -> false branch
        
        // 2. Test setAuthUser
        context.setAuthUser(user);
        
        // 3. Test state after setting (true branch of isAuthenticated)
        assertEquals(user, context.getAuthUser());
        assertTrue(context.isAuthenticated()); // Covers isAuthenticated() -> true branch
    }
}