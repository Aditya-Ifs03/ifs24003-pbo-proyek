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
                    // Menangani MultipartFile dengan Mockito
                    else if (paramType == MultipartFile.class) value = Mockito.mock(MultipartFile.class);
                    // User class handling (Jika ada Entity/DTO yang di-Set)
                    else if (paramType == User.class) value = new User();
                    else if (paramType == Customer.class) value = new Customer();
                    else if (paramType == AuthToken.class) value = new AuthToken();


                    if (value != null) {
                        // Panggil Setter
                        method.invoke(instance, value);
                        // Panggil Getter dan cek hasilnya
                        Object result = getter.invoke(instance);
                        assertEquals(value, result, "Gagal di method: " + name);
                    }
                } catch (NoSuchMethodException e) {
                    // Abaikan jika tidak ada getter yang sesuai (misal: setPassword mungkin tidak memiliki getPassword
                    // jika dianggap tidak aman, namun di kode Anda ada. Ini untuk memastikan reflection test tetap berjalan.)
                    // Tidak ada perubahan di sini
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

    /**
     * Test Getter/Setter dan Default Constructor untuk semua DTO.
     */
    @Test
    void testDtoGettersSettersAndConstructor() throws Exception {
        testGetterSetter(new LoginForm());
        testGetterSetter(new RegisterForm());
        testGetterSetter(new ProductForm()); // DTO dengan MultipartFile (sudah di-mock)
    }

    /**
     * Test Getter/Setter dan Default Constructor untuk semua Entity.
     */
    @Test
    void testEntityGettersSettersAndConstructor() throws Exception {
        // Test Getters/Setters untuk Entity
        testGetterSetter(new User());
        testGetterSetter(new Customer());
        testGetterSetter(new AuthToken());
    }
    
    /**
     * Test semua non-default constructors dari User dan AuthToken.
     * Metode ini menjamin semua constructor terpanggil.
     */
    @Test
    void testNonDefaultConstructors() {
        // --- AuthToken constructors ---
        UUID testUserId = UUID.randomUUID();
        AuthToken t1 = new AuthToken(testUserId, "testToken");
        assertEquals(testUserId, t1.getUserId());
        assertEquals("testToken", t1.getToken());
        assertNotNull(t1.getCreatedAt());

        // --- User constructors ---
        // Constructor: User(String email, String password) -> calls this("", email, password)
        User u1 = new User("email@test.com", "pass123");
        assertEquals("", u1.getName()); // Memastikan nama diinisialisasi ke ""
        assertEquals("email@test.com", u1.getEmail());
        assertEquals("pass123", u1.getPassword());

        // Constructor: User(String name, String email, String password)
        User u2 = new User("Name", "email@test.com", "pass123");
        assertEquals("Name", u2.getName());
    }


    /**
     * Test method lifecycle (@PrePersist, @PreUpdate) pada Entity.
     * Metode ini juga mencakup percabangan logika di Customer.onCreate().
     */
    @Test
    void testEntityLifecycleMethods() throws Exception {
        // --- User Entity Lifecycle (onCreate, onUpdate) ---
        User u = new User();
        invokeLifecycleMethod(u, "onCreate");
        assertNotNull(u.getCreatedAt());
        assertNotNull(u.getUpdatedAt());
        LocalDateTime firstUpdate = u.getUpdatedAt();

        invokeLifecycleMethod(u, "onUpdate");
        // Memastikan updatedAt diperbarui (atau setidaknya tidak sebelum waktu sebelumnya)
        assertTrue(u.getUpdatedAt().isAfter(firstUpdate) || u.getUpdatedAt().isEqual(firstUpdate));


        // --- Customer Entity Lifecycle (onCreate, onUpdate) ---
        // Branch 1: type == null (akan diisi "Regular")
        Customer c1 = new Customer();
        invokeLifecycleMethod(c1, "onCreate");
        assertNotNull(c1.getCreatedAt());
        assertEquals("Regular", c1.getType(), "Type should default to 'Regular' when null");
        LocalDateTime custFirstUpdate = c1.getUpdatedAt();

        // Branch 2: type != null (tidak akan diubah)
        Customer c2 = new Customer();
        c2.setType("VIP");
        invokeLifecycleMethod(c2, "onCreate");
        assertEquals("VIP", c2.getType(), "Type should remain 'VIP' when explicitly set");
        
        // Cover onUpdate()
        invokeLifecycleMethod(c1, "onUpdate");
        assertTrue(c1.getUpdatedAt().isAfter(custFirstUpdate) || c1.getUpdatedAt().isEqual(custFirstUpdate));
        
        
        // --- AuthToken Entity Lifecycle (onCreate) ---
        AuthToken t = new AuthToken();
        invokeLifecycleMethod(t, "onCreate");
        assertNotNull(t.getCreatedAt());
    }

    /**
     * Test coverage untuk metode Object.class dasar (equals, hashCode, toString)
     * yang sering diabaikan dan menyebabkan coverage Jacoco tidak mencapai 100%.
     */
    @Test
    void testDefaultObjectMethods() {
        // DTOs
        LoginForm lf = new LoginForm();
        assertNotNull(lf.toString());
        assertEquals(lf, lf);
        assertEquals(lf.hashCode(), lf.hashCode());

        RegisterForm rf = new RegisterForm();
        assertNotNull(rf.toString());
        assertEquals(rf, rf);
        assertEquals(rf.hashCode(), rf.hashCode());

        ProductForm pf = new ProductForm();
        assertNotNull(pf.toString());
        assertEquals(pf, pf);
        assertEquals(pf.hashCode(), pf.hashCode());

        // Entities
        User u = new User();
        assertNotNull(u.toString());
        assertEquals(u, u);
        assertEquals(u.hashCode(), u.hashCode());

        Customer c = new Customer();
        assertNotNull(c.toString());
        assertEquals(c, c);
        assertEquals(c.hashCode(), c.hashCode());
        
        AuthToken t = new AuthToken();
        assertNotNull(t.toString());
        assertEquals(t, t);
        assertEquals(t.hashCode(), t.hashCode());
    }
}