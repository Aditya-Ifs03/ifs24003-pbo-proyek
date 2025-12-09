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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomErrorControllerTest {
    @Mock private ErrorAttributes errorAttributes;
    @Mock private ServletWebRequest webRequest;
    @InjectMocks private CustomErrorController controller;

    @Test
    void handleError_500() {
        Map<String, Object> attr = new HashMap<>();
        attr.put("status", 500);
        when(errorAttributes.getErrorAttributes(any(), any(ErrorAttributeOptions.class))).thenReturn(attr);

        ResponseEntity<Map<String, Object>> resp = controller.handleError(webRequest);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, resp.getStatusCode());
    }
}