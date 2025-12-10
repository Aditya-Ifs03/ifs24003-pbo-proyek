package org.delcom.app.configs;

import jakarta.servlet.http.HttpServletRequest; // 1. WAJIB untuk parameter handleError
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.webmvc.error.ErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.ServletWebRequest;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock; // 4. WAJIB untuk Mockito
import static org.mockito.Mockito.when;

class CustomErrorControllerTest {

    @Test
    void testConstructorCoverage() throws Exception {
        // Menguji constructor default/implisit menggunakan reflection 
        ErrorAttributes mockErrorAttributes = mock(ErrorAttributes.class);
        CustomErrorController controller = new CustomErrorController(mockErrorAttributes);
        assertNotNull(controller);
    }

    @Test
    void testHandleError_Default() {
        ErrorAttributes mockErrorAttributes = mock(ErrorAttributes.class);
        CustomErrorController controller = new CustomErrorController(mockErrorAttributes);
        
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        ServletWebRequest webRequest = new ServletWebRequest(mockRequest);
        
        // Mock error attributes
        when(mockErrorAttributes.getErrorAttributes(webRequest, ErrorAttributeOptions.defaults()))
                .thenReturn(Map.of("status", 500, "path", "/test-path", "error", "Internal Server Error"));

        ResponseEntity<Map<String, Object>> responseEntity = controller.handleError(webRequest);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        Map<String, Object> body = responseEntity.getBody();
        assertNotNull(body);
        assertEquals("error", body.get("status"));
        assertEquals("Endpoint tidak ditemukan atau terjadi error", body.get("message"));
        assertEquals("/test-path", body.get("path"));
    }

    @Test
    void testHandleError_NotFound() {
        ErrorAttributes mockErrorAttributes = mock(ErrorAttributes.class);
        CustomErrorController controller = new CustomErrorController(mockErrorAttributes);

        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        ServletWebRequest webRequest = new ServletWebRequest(mockRequest);

        // Mock error attributes for 404
        when(mockErrorAttributes.getErrorAttributes(webRequest, ErrorAttributeOptions.defaults()))
                .thenReturn(Map.of("status", 404, "path", "/non-existent", "error", "Not Found"));

        ResponseEntity<Map<String, Object>> responseEntity = controller.handleError(webRequest);

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        Map<String, Object> body = responseEntity.getBody();
        assertNotNull(body);
        assertEquals("fail", body.get("status")); // 4xx errors should be "fail"
        assertEquals("Endpoint tidak ditemukan atau terjadi error", body.get("message"));
        assertEquals("/non-existent", body.get("path"));
    }
}