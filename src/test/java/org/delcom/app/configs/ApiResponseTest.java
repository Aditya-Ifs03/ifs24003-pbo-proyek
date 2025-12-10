package org.delcom.app.configs;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ApiResponseTest {

    @Test
    void testApiResponseCoverage() {
        String testStatus = "success";
        String testMessage = "Operasi berhasil.";
        String testData = "Data hasil";

        // 1. Test Parameterized Constructor (Covers constructor with args, getStatus, getMessage, getData)
        ApiResponse<String> resParam = new ApiResponse<>(testStatus, testMessage, testData);
        assertEquals(testStatus, resParam.getStatus(), "Status should match the input value.");
        assertEquals(testMessage, resParam.getMessage(), "Message should match the input value.");
        assertEquals(testData, resParam.getData(), "Data should match the input value.");

        // 2. Test Default Constructor (Covers the no-argument constructor)
        ApiResponse<Void> resDefault = new ApiResponse<>();
        assertNull(resDefault.getStatus(), "Status should be null when using default constructor.");
        assertNull(resDefault.getMessage(), "Message should be null when using default constructor.");
        assertNull(resDefault.getData(), "Data should be null when using default constructor.");
    }
}