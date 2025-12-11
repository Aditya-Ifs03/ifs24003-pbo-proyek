package org.delcom.app.configs;

import org.delcom.app.interceptors.AuthInterceptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebMvcConfigTests {

    @Mock
    private AuthInterceptor authInterceptor;

    @Mock
    private InterceptorRegistry interceptorRegistry;

    @Mock
    private ResourceHandlerRegistry resourceHandlerRegistry;

    @Mock
    private InterceptorRegistration interceptorRegistration;

    @Mock
    private ResourceHandlerRegistration resourceHandlerRegistration;

    @InjectMocks
    private WebMvcConfig webMvcConfig;

    @BeforeEach
    void setUp() {
        // Menyuntikkan nilai @Value("${app.upload.dir}") secara manual
        ReflectionTestUtils.setField(webMvcConfig, "uploadDir", "./uploads");
    }

    @Test
    @DisplayName("Add Interceptors: Memastikan AuthInterceptor didaftarkan dengan Path yang benar")
    void addInterceptors() {
        // Setup chaining method: registry.add() -> return registration object
        when(interceptorRegistry.addInterceptor(authInterceptor)).thenReturn(interceptorRegistration);
        when(interceptorRegistration.addPathPatterns(anyString())).thenReturn(interceptorRegistration);
        when(interceptorRegistration.excludePathPatterns(anyString())).thenReturn(interceptorRegistration);

        // Eksekusi
        webMvcConfig.addInterceptors(interceptorRegistry);

        // Validasi
        verify(interceptorRegistry).addInterceptor(authInterceptor);
        verify(interceptorRegistration).addPathPatterns("/api/**"); // Sesuai kode asli
        verify(interceptorRegistration).excludePathPatterns("/api/auth/**");
        verify(interceptorRegistration).excludePathPatterns("/api/public/**");
    }

    @Test
    @DisplayName("Add Resource Handlers: Memastikan folder Uploads bisa diakses")
    void addResourceHandlers() {
        // Setup chaining method
        when(resourceHandlerRegistry.addResourceHandler(anyString())).thenReturn(resourceHandlerRegistration);
        
        // Eksekusi
        webMvcConfig.addResourceHandlers(resourceHandlerRegistry);

        // Validasi
        verify(resourceHandlerRegistry).addResourceHandler("/uploads/**");
        // Pastikan path upload dir ditambahkan dengan prefix "file:"
        verify(resourceHandlerRegistration).addResourceLocations("file:./uploads/");
    }
}