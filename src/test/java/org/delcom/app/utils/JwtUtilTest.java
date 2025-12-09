package org.delcom.app.utils;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {
    @Test
    void testToken() {
        UUID uid = UUID.randomUUID();
        String t = JwtUtil.generateToken(uid);
        assertNotNull(t);
        assertEquals(uid, JwtUtil.extractUserId(t));
        assertTrue(JwtUtil.validateToken(t, false));
    }
}