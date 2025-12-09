package org.delcom.app.utils;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Constructor;
import static org.junit.jupiter.api.Assertions.*;

class ConstUtilTest {
    @Test
    void test() throws Exception {
        assertEquals("AUTH_TOKEN", ConstUtil.KEY_AUTH_TOKEN);
        // Coverage constructor
        Constructor<ConstUtil> c = ConstUtil.class.getDeclaredConstructor();
        c.setAccessible(true);
        c.newInstance();
    }
}