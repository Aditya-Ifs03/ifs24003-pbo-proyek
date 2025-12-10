package org.delcom.app.configs;

import org.delcom.app.interceptors.AuthInterceptor;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import static org.junit.jupiter.api.Assertions.*;

class WebMvcConfigTest {

    @Test
    void testWebMvcConfigInitializationAndInterceptors() {
        // Mock AuthInterceptor dan InterceptorRegistry
        AuthInterceptor mockInterceptor = Mockito.mock(AuthInterceptor.class);
        InterceptorRegistry mockRegistry = Mockito.mock(InterceptorRegistry.class);

        // Stubbing: Mengembalikan objek yang sama agar rantai pemanggilan berfungsi
        Mockito.when(mockRegistry.addInterceptor(Mockito.any())).thenReturn(Mockito.mock(org.springframework.web.servlet.config.annotation.InterceptorRegistration.class));

        // Inisialisasi konfigurasi dengan mock interceptor
        WebMvcConfig config = new WebMvcConfig(mockInterceptor);

        // Test constructor coverage
        assertNotNull(config, "WebMvcConfig should be initialized.");
        
        // Panggil metode yang akan diuji
        config.addInterceptors(mockRegistry);

        // Verifikasi bahwa addInterceptor dipanggil (Coverage untuk addInterceptors)
        Mockito.verify(mockRegistry, Mockito.times(1)).addInterceptor(mockInterceptor);
    }
}