package org.delcom.app.entities;

import org.delcom.app.dto.LoginForm;
import org.delcom.app.dto.ProductForm;
import org.delcom.app.dto.RegisterForm;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.multipart.MultipartFile;

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
                String getterName = name.replaceFirst("set", "get");
                if (getterName.equals("getRememberMe")) getterName = "isRememberMe"; // Case khusus untuk boolean di LoginForm

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
                    // **PERBAIKAN:** Menangani MultipartFile dengan Mockito
                    else if (paramType == MultipartFile.class) value = Mockito.mock(MultipartFile.class);
                    // User class handling (Untuk AuthToken.setUser, jika ada)
                    else if (paramType == User.class) value = new User();

                    if (value != null) {
                        // Panggil Setter
                        method.invoke(instance, value);
                        // Panggil Getter dan cek hasilnya
                        Object result = getter.invoke(instance);
                        assertEquals(value, result, "Gagal di method: " + name);
                    }
                } catch (NoSuchMethodException e) {
                    // Ignore jika tidak ada getter yang sesuai (misal setPassword)
                }
            }
        }
    }

    // Helper method untuk memanggil metode lifecycle (@PrePersist, @PreUpdate)
    private void invokeLifecycleMethod(Object entity, String methodName) throws Exception {
        Method method = entity.getClass().getDeclaredMethod(methodName);
        method.setAccessible(true); // Memaksa akses ke protected method
        method.invoke(entity);
    }

    @Test
    void testAllPojosAndConstructors() throws Exception {
        // 1. Test DTOs (Getters/Setters & Default Constructor)
        testGetterSetter(new LoginForm());
        testGetterSetter(new RegisterForm());
        // **PERBAIKAN:** ProductForm sekarang di-test dengan MultipartFile mock
        testGetterSetter(new ProductForm());

        // 2. Test Entities (Getters/Setters & Default Constructor)
        testGetterSetter(new User());
        testGetterSetter(new Customer());
        testGetterSetter(new AuthToken());
        
        // 3. Test non-default constructors
        // User constructors
        User u1 = new User("email@test.com", "pass123");
        assertEquals("", u1.getName()); // Karena menggunakan constructor this("", email, password)
        assertEquals("email@test.com", u1.getEmail());
        
        User u2 = new User("Name", "email@test.com", "pass123");
        assertEquals("Name", u2.getName());
        
        // AuthToken constructor
        UUID testUserId = UUID.randomUUID();
        AuthToken t1 = new AuthToken(testUserId, "testToken");
        assertEquals(testUserId, t1.getUserId());
        assertEquals("testToken", t1.getToken());
        assertNotNull(t1.getCreatedAt());
    }

    @Test
    void testEntityLifecycleMethods() throws Exception {
        // --- User Entity Lifecycle ---
        User u = new User();
        invokeLifecycleMethod(u, "onCreate");
        assertNotNull(u.getCreatedAt());
        assertNotNull(u.getUpdatedAt());
        LocalDateTime firstUpdate = u.getUpdatedAt();
        
        invokeLifecycleMethod(u, "onUpdate");
        assertTrue(u.getUpdatedAt().isAfter(firstUpdate) || u.getUpdatedAt().isEqual(firstUpdate));


        // --- Customer Entity Lifecycle ---
        // Branch 1: type == null (akan diisi "Regular")
        Customer c1 = new Customer();
        invokeLifecycleMethod(c1, "onCreate");
        assertNotNull(c1.getCreatedAt());
        assertEquals("Regular", c1.getType());
        LocalDateTime custFirstUpdate = c1.getUpdatedAt();

        // Branch 2: type != null (tidak akan diubah)
        Customer c2 = new Customer();
        c2.setType("VIP");
        invokeLifecycleMethod(c2, "onCreate");
        assertEquals("VIP", c2.getType());
        
        // Cover onUpdate()
        invokeLifecycleMethod(c1, "onUpdate");
        assertTrue(c1.getUpdatedAt().isAfter(custFirstUpdate) || c1.getUpdatedAt().isEqual(custFirstUpdate));
        
        
        // --- AuthToken Entity Lifecycle ---
        AuthToken t = new AuthToken();
        invokeLifecycleMethod(t, "onCreate");
        assertNotNull(t.getCreatedAt());
    }
}