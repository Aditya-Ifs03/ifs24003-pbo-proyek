package org.delcom.app.configs;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class RequestLoggingFilterTest {
    
    private final RequestLoggingFilter filter = new RequestLoggingFilter();
    private final HttpServletRequest mockRequest = Mockito.mock(HttpServletRequest.class);

    @Test
    void testConstructorProperties() {
        // Constructor sudah dieksekusi saat instansiasi di atas.
        // Verifikasi properti yang di-set di constructor untuk memastikan semua baris constructor tercakup.
        assertDoesNotThrow(() -> new RequestLoggingFilter(), "Constructor should run without error.");
    }
    
    // Menggunakan reflection untuk menguji metode protected shouldNotFilter
    private boolean callShouldNotFilter(String uri) throws Exception {
        // Persiapan Reflection
        Method method = RequestLoggingFilter.class.getDeclaredMethod("shouldNotFilter", HttpServletRequest.class);
        method.setAccessible(true);
        
        Mockito.when(mockRequest.getRequestURI()).thenReturn(uri);
        
        return (boolean) method.invoke(filter, mockRequest);
    }
    
    @Test
    void testShouldNotFilter_StartsWitAssets() throws Exception {
        // Covers: path.startsWith("/assets/") -> true branch
        assertTrue(callShouldNotFilter("/assets/css/style.css"), "Should filter out /assets/");
    }

    @Test
    void testShouldNotFilter_StartsWitWebjars() throws Exception {
        // Covers: path.startsWith("/webjars/") -> true branch
        assertTrue(callShouldNotFilter("/webjars/jquery/jquery.js"), "Should filter out /webjars/");
    }
    
    @Test
    void testShouldNotFilter_ShouldFilter() throws Exception {
        // Covers: path.startsWith(...) -> false branch (Harus di-log)
        assertFalse(callShouldNotFilter("/home"), "Should NOT filter /home");
        assertFalse(callShouldNotFilter("/api/login"), "Should NOT filter /api/login");
    }
}