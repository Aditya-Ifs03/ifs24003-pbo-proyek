package org.delcom.app;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ApplicationTests {
    @Test
    void contextLoads() {
        // Memastikan konteks spring boot berhasil dimuat
        Application.main(new String[]{}); 
    }
}