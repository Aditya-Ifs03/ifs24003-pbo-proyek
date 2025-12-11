package org.delcom.app.configs;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.webmvc.error.ErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.ServletWebRequest;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomErrorControllerTest {

    @Mock
    private ErrorAttributes errorAttributes;

    @InjectMocks
    private CustomErrorController customErrorController;

    @Test
    void testHandleError_500_InternalServerError() {
        // 1. Persiapan Data (Mocking)
        ServletWebRequest mockRequest = mock(ServletWebRequest.class);
        
        Map<String, Object> errorMap = new HashMap<>();
        errorMap.put("status", 500);
        errorMap.put("error", "Internal Server Error");
        errorMap.put("path", "/api/broken");

        // Ketika controller minta atribut error, kasih data di atas
        when(errorAttributes.getErrorAttributes(any(ServletWebRequest.class), any(ErrorAttributeOptions.class)))
                .thenReturn(errorMap);

        // 2. Jalankan Method Controller secara langsung
        ResponseEntity<Map<String, Object>> response = customErrorController.handleError(mockRequest);

        // 3. Cek Hasilnya
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode()); // Harus 500
        assertEquals("error", response.getBody().get("status")); // Status text harus 'error'
        assertEquals("Internal Server Error", response.getBody().get("error"));
    }

    @Test
    void testHandleError_404_NotFound() {
        // 1. Persiapan Data
        ServletWebRequest mockRequest = mock(ServletWebRequest.class);

        Map<String, Object> errorMap = new HashMap<>();
        errorMap.put("status", 404);
        errorMap.put("error", "Not Found");
        errorMap.put("path", "/api/missing");

        when(errorAttributes.getErrorAttributes(any(ServletWebRequest.class), any(ErrorAttributeOptions.class)))
                .thenReturn(errorMap);

        // 2. Jalankan
        ResponseEntity<Map<String, Object>> response = customErrorController.handleError(mockRequest);

        // 3. Cek Hasil
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode()); // Harus 404
        assertEquals("fail", response.getBody().get("status")); // Kalau bukan 500, harus 'fail'
    }
}