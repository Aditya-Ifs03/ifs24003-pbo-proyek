package org.delcom.app.utils;

import org.junit.jupiter.api.Test;
import java.lang.reflect.Constructor;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {
    
    @Test
    void testToken_Success() {
        UUID uid = UUID.randomUUID();
        String t = JwtUtil.generateToken(uid);
        
        // 1. Test generateToken, extractUserId, dan validateToken (ignoreExpired=false)
        assertNotNull(t);
        assertEquals(uid, JwtUtil.extractUserId(t));
        assertTrue(JwtUtil.validateToken(t, false));
    }
    
    @Test
    void testToken_ValidationFailure() {
        String invalidToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c"; // Token dengan signature invalid
        
        // 1. Test extractUserId failure (mencakup catch block)
        assertNull(JwtUtil.extractUserId(invalidToken));
        
        // 2. Test validateToken generic Exception (mencakup catch (Exception e))
        assertFalse(JwtUtil.validateToken(invalidToken, false)); // ignoreExpired=false
        assertFalse(JwtUtil.validateToken(invalidToken, true));  // ignoreExpired=true
    }
    
    // Catatan: Menguji ExpiredJwtException murni sulit karena membutuhkan mocking waktu. 
    // Namun, cakupan baris catch (Exception e) dan return false/true sudah tercakup di test di atas.
    
    @Test
    void testGetKey_ConstructorCoverage() throws Exception {
        // Menguji getKey()
        assertNotNull(JwtUtil.getKey());
        
        // Menguji constructor default (walaupun static, ini mencakup class definition line)
        Constructor<JwtUtil> constructor = JwtUtil.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        constructor.newInstance();
    }
}