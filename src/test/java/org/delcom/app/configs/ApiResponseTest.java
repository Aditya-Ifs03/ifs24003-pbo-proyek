package org.delcom.app.configs;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ApiResponseTest {
    @Test
    void testApiResponse_ParameterizedConstructor() {
        String data = "data";
        ApiResponse<String> res = new ApiResponse<>("success", "msg", data);
        assertEquals("success", res.getStatus());
        assertEquals("msg", res.getMessage());
        assertEquals(data, res.getData());
    }
    
    @Test
    void testApiResponse_DefaultConstructor() {
        // Test constructor default untuk JaCoCo
        ApiResponse<Void> res = new ApiResponse<>();
        assertNull(res.getStatus());
        assertNull(res.getMessage());
        assertNull(res.getData());
    }
}