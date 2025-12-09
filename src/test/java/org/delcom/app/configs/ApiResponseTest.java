package org.delcom.app.configs;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ApiResponseTest {
    @Test
    void testApiResponse() {
        String data = "data";
        ApiResponse<String> res = new ApiResponse<>("success", "msg", data);
        assertEquals("success", res.getStatus());
        assertEquals("msg", res.getMessage());
        assertEquals(data, res.getData());
    }
}