package org.delcom.app.dto;

import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class ProductFormTest {

    @Test
    void testProductForm_GettersAndSetters() {
        // 1. Arrange (Siapkan Data)
        String id = "prod-123";
        String name = "Laptop Gaming";
        Double price = 15000000.0;
        Integer stock = 10;
        String description = "Laptop spek tinggi";
        MultipartFile mockImage = mock(MultipartFile.class);

        // 2. Act (Lakukan Set data)
        ProductForm form = new ProductForm();
        form.setId(id);
        form.setName(name);
        form.setPrice(price);
        form.setStock(stock);
        form.setDescription(description);
        form.setImage(mockImage);

        // 3. Assert (Pastikan data yang di-Get sama dengan yang di-Set)
        assertEquals(id, form.getId());
        assertEquals(name, form.getName());
        assertEquals(price, form.getPrice());
        assertEquals(stock, form.getStock());
        assertEquals(description, form.getDescription());
        assertEquals(mockImage, form.getImage());
    }
    
    @Test
    void testProductForm_Constructor() {
        // Test Constructor kosong
        ProductForm form = new ProductForm();
        assertNotNull(form);
        assertNull(form.getName()); // Default string null
    }
}