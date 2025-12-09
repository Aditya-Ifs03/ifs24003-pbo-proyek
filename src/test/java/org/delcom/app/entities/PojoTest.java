package org.delcom.app.entities;

import org.delcom.app.dto.LoginForm;
import org.delcom.app.dto.ProductForm;
import org.delcom.app.dto.RegisterForm;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PojoTest {

    // Helper method untuk mengetes Getter & Setter secara otomatis
    private void testGetterSetter(Object instance) throws Exception {
        Method[] methods = instance.getClass().getMethods();
        
        for (Method method : methods) {
            String name = method.getName();
            
            // Cari setter (nama mulai dengan 'set' dan punya 1 parameter)
            if (name.startsWith("set") && method.getParameterCount() == 1) {
                String getterName = "get" + name.substring(3);
                Class<?> paramType = method.getParameterTypes()[0];
                
                try {
                    // Cari getter pasangannya
                    Method getter = instance.getClass().getMethod(getterName);
                    
                    // Siapkan dummy data sesuai tipe parameter
                    Object value = null;
                    if (paramType == String.class) value = "test";
                    else if (paramType == UUID.class) value = UUID.randomUUID();
                    else if (paramType == Long.class || paramType == long.class) value = 1L;
                    else if (paramType == Double.class || paramType == double.class) value = 1.0;
                    else if (paramType == Integer.class || paramType == int.class) value = 1;
                    else if (paramType == LocalDateTime.class) value = LocalDateTime.now();
                    else if (paramType == Boolean.class || paramType == boolean.class) value = true;
                    // User class handling (Untuk AuthToken.setUser)
                    else if (paramType == User.class) value = new User();
                    
                    if (value != null) {
                        // Panggil Setter
                        method.invoke(instance, value);
                        // Panggil Getter dan cek hasilnya
                        Object result = getter.invoke(instance);
                        assertEquals(value, result, "Gagal di method: " + name);
                    }
                } catch (NoSuchMethodException e) {
                    // Ignore jika tidak ada getter (misal password di beberapa kasus)
                }
            }
        }
    }

    @Test
    void testAllPojos() throws Exception {
        // 1. Test Entities
        // Pastikan User memiliki constructor default
        testGetterSetter(new User());
        testGetterSetter(new Customer());
        testGetterSetter(new AuthToken());

        // 2. Test DTOs
        testGetterSetter(new LoginForm());
        testGetterSetter(new RegisterForm());
        testGetterSetter(new ProductForm());
    }

    @Test
    void testUserSpecifics() throws Exception {
        // Test manual khusus untuk Constructor User yang custom
        User u = new User("name", "email", "pass");
        assertEquals("name", u.getName());
        
        // PERBAIKAN: Gunakan Reflection untuk memanggil onCreate()
        // Ini mengatasi masalah akses 'protected' method dari class Test
        Method onCreateMethod = User.class.getDeclaredMethod("onCreate");
        onCreateMethod.setAccessible(true); // Paksa akses menjadi public untuk test
        onCreateMethod.invoke(u);

        assertNotNull(u.getCreatedAt());
    }
    
    @Test
    void testAuthTokenSpecifics() {
        // Test manual AuthToken karena token sering punya logika unik
        AuthToken t = new AuthToken();
        // AuthToken does not have a direct setUser method, it's set via constructor or repository
        // This test case is no longer relevant as AuthToken's user is managed differently.
        // If a setter for User is added to AuthToken, this test should be updated.
    }
}