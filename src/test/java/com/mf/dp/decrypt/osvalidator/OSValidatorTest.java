
package com.mf.dp.decrypt.osvalidator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OSValidatorTest {

    @Test
    @DisplayName("Test to validate OS")
    public void testToValidateOS(){
        OSValidator os=new OSValidator();
        String s = System.getProperty("os.name").toLowerCase();
        if(s.contains("win"))
        {
            assertTrue(os.isWindows());
            assertFalse(os.isUnix());
            assertFalse(os.isMac());
        }
        if(s.contains("nix") || s.contains("nux"))
        {
            assertTrue(os.isUnix());
            assertFalse(os.isWindows());
            assertFalse(os.isMac());
        }
        if(s.contains("mac"))
        {
            assertTrue(os.isMac());
            assertFalse(os.isUnix());
            assertFalse(os.isWindows());
        }
    }
}
