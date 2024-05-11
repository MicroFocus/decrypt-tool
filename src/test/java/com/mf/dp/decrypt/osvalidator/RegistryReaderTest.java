
package com.mf.dp.decrypt.osvalidator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class RegistryReaderTest {

    @Test
    @DisplayName("Test to validate constructor of RegistryReader")
    public void testConstructorOfRegistryReader() {
        try {
            Constructor<RegistryReader> constructor = RegistryReader.class.getDeclaredConstructor();
            assertTrue(Modifier.isPrivate(constructor.getModifiers()));
            constructor.setAccessible(true);
            constructor.newInstance();
        }
        catch (Exception e){
            assertEquals(InvocationTargetException.class,e.getClass());
        }
    }

    @Test
    @DisplayName("Test to validate output of readRegistryRegQuery")
    @EnabledOnOs(OS.WINDOWS)
    public void testOutputOf_readRegistryRegQuery() {
        try {
            String valid_key ="HKLM\\SOFTWARE /ve";
            String invalid_key = "invalid";
            assertNotNull(RegistryReader.readRegistryRegQuery(valid_key));
            assertNull(RegistryReader.readRegistryRegQuery(invalid_key));
        }
        catch (Throwable e){
            e.printStackTrace();
            fail("should not throw exception");
        }
    }

    @Test
    @DisplayName("Test to validate exception in StreamReader")
    public void testExceptionInStreamReader(){
        try {
            RegistryReader.StreamReader s = mock(RegistryReader.StreamReader.class);
            Mockito.doThrow(new RuntimeException("testing exception")).when(s).run();
            s.run();
        }
        catch (Exception e) {
            assertEquals(RuntimeException.class, e.getClass());
        }
    }

    @Test
    @DisplayName("Test to validate output of readRegistry")
    @EnabledOnOs(OS.WINDOWS)
    public void testOutputOf_readRegistry() {
        try(MockedStatic<RegistryReader> mock_rr = Mockito.mockStatic(RegistryReader.class)) {
            String[] input_ = {"a\\c", "b"};
            String expected = "\"a\\c\" /v b";
            mock_rr.when(()->RegistryReader.readRegistryRegQuery(expected)).thenReturn(expected);
            mock_rr.when(()->RegistryReader.readRegistry(input_)).thenCallRealMethod();
            assertEquals(expected, RegistryReader.readRegistry(input_));
        }
        catch (Throwable e){
            e.printStackTrace();
            fail("Should not throw exception");
        }
    }
}
