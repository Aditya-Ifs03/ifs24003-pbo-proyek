package org.delcom.app.configs;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.ModelAndView;
import static org.junit.jupiter.api.Assertions.*;

class CustomErrorControllerTest {

    private CustomErrorController controller;
    private HttpServletRequest mockRequest;

    @BeforeEach
    void setUp() {
        controller = new CustomErrorController();
        mockRequest = Mockito.mock(HttpServletRequest.class);
    }

    // Helper method untuk menguji status code tertentu
    private void testErrorCase(int statusCode, String expectedViewName) {
        // Mock request attribute untuk ERROR_STATUS_CODE
        Mockito.when(mockRequest.getAttribute(RequestDispatcher.ERROR_STATUS_CODE))
               .thenReturn(statusCode);

        ModelAndView mav = controller.handleError(mockRequest);
        
        assertNotNull(mav, "ModelAndView should not be null for status " + statusCode);
        assertEquals(expectedViewName, mav.getViewName(), "View name incorrect for status " + statusCode);
    }
    
    @Test
    void testHandleError_404NotFound() {
        // Covers: if (statusCode == 404)
        testErrorCase(HttpStatus.NOT_FOUND.value(), "pages/error/404");
    }

    @Test
    void testHandleError_500InternalServerError() {
        // Covers: else if (statusCode == 500)
        testErrorCase(HttpStatus.INTERNAL_SERVER_ERROR.value(), "pages/error/500");
    }

    @Test
    void testHandleError_OtherError() {
        // Covers: else (cabang error lainnya, misal 403, 400)
        testErrorCase(HttpStatus.FORBIDDEN.value(), "pages/error/default");
    }

    @Test
    void testHandleError_StatusIsNull() {
        // Covers: if (status != null) -> false branch
        Mockito.when(mockRequest.getAttribute(RequestDispatcher.ERROR_STATUS_CODE)).thenReturn(null);
        
        ModelAndView mav = controller.handleError(mockRequest);
        
        assertEquals("pages/error/default", mav.getViewName(), "Should default view when status is null");
    }
    
    @Test
    void testGetErrorPath() {
        // Menguji metode yang diimplementasikan dari ErrorController (biasanya tidak digunakan di versi Spring terbaru)
        // Di kelas Anda, ini tidak dideklarasikan, jadi tidak perlu diuji secara eksplisit, 
        // tapi controller harus diuji untuk diinisialisasi.
        assertDoesNotThrow(() -> new CustomErrorController(), "Controller should be instantiable.");
    }
}