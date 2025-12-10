package org.delcom.app.utils;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Constructor;
import static org.junit.jupiter.api.Assertions.*;

class ConstUtilTest {

    /**
     * Test untuk memastikan semua konstanta memiliki nilai yang benar dan
     * untuk menutupi (cover) constructor pribadi dari kelas utilitas.
     */
    @Test
    void testAllConstantsAndConstructor() throws Exception {
        // Test untuk Constants
        assertEquals("AUTH_TOKEN", ConstUtil.KEY_AUTH_TOKEN, "KEY_AUTH_TOKEN should be 'AUTH_TOKEN'");
        assertEquals("USER_ID", ConstUtil.KEY_USER_ID, "KEY_USER_ID should be 'USER_ID'");
        assertEquals("pages/home", ConstUtil.TEMPLATE_PAGES_HOME, "TEMPLATE_PAGES_HOME value is incorrect");
        assertEquals("pages/auth/login", ConstUtil.TEMPLATE_PAGES_AUTH_LOGIN, "TEMPLATE_PAGES_AUTH_LOGIN value is incorrect");
        assertEquals("pages/auth/register", ConstUtil.TEMPLATE_PAGES_AUTH_REGISTER, "TEMPLATE_PAGES_AUTH_REGISTER value is incorrect");
        assertEquals("pages/customers/index", ConstUtil.TEMPLATE_PAGES_CUSTOMERS_INDEX, "TEMPLATE_PAGES_CUSTOMERS_INDEX value is incorrect");
        assertEquals("pages/customers/form", ConstUtil.TEMPLATE_PAGES_CUSTOMERS_FORM, "TEMPLATE_PAGES_CUSTOMERS_FORM value is incorrect");
        assertEquals("pages/customers/detail", ConstUtil.TEMPLATE_PAGES_CUSTOMERS_DETAIL, "TEMPLATE_PAGES_CUSTOMERS_DETAIL value is incorrect");
        assertEquals("pages/customers/image-form", ConstUtil.TEMPLATE_PAGES_CUSTOMERS_IMAGE, "TEMPLATE_PAGES_CUSTOMERS_IMAGE value is incorrect");

        // Coverage constructor: Memastikan constructor kelas utilitas (private) tercakup,
        // meskipun seharusnya tidak diinstansiasi.
        Constructor<ConstUtil> c = ConstUtil.class.getDeclaredConstructor();
        assertTrue(c.isAccessible(), "Constructor should be accessible for testing");
        c.setAccessible(true);
        c.newInstance();
    }
}