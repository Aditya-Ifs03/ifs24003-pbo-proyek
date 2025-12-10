package org.delcom.app;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
class ApplicationTest {

    @Test
    void contextLoads() {
        // Memastikan konteks Spring Boot berhasil dimuat (ini adalah tes bawaan)
        // Ini memberikan coverage untuk instansiasi Application.class
    }
    
    @Test
    void mainMethodTest() {
        // Menambahkan tes eksplisit untuk metode main() untuk 100% method/line coverage.
        // Metode ini harus dipanggil tanpa melempar exception, yang menyiratkan startup dasar berhasil.
        assertDoesNotThrow(() -> {
            // Memanggil metode main secara langsung.
            Application.main(new String[] {});
        }, "The main method should execute without throwing an exception.");
    }
}