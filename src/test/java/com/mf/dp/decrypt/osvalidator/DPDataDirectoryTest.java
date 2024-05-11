
package com.mf.dp.decrypt.osvalidator;

import com.mf.dp.decrypt.exception.DecryptSslKeyException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.mockito.ArgumentMatchers;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import static org.junit.jupiter.api.Assertions.*;

public class DPDataDirectoryTest {

    @Test
    @DisplayName("Test to validate constructor of DPDataDirectory")
    public void testConstructorOfDPDataDirectory() {
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
    @DisplayName("Test to validate object creation by logger")
    public void testObjectCreationBy_logger() {
        assertNotNull(DPDataDirectory.log);
    }


    @Test
    @DisplayName("Test to validate Output and Exception in getSSCertificatesPath")
    @EnabledOnOs(OS.WINDOWS)
    public void testOutputAndExceptionIn_getSSCertificatesPath() {
        OSValidator os = new OSValidator();
        if(os.isWindows()) {
            try (MockedStatic<RegistryReader> mock_rr = Mockito.mockStatic(RegistryReader.class)) {
                String[] input = {"HKEY_LOCAL_MACHINE\\SOFTWARE\\MyCompany\\OpenView\\OmniBackII\\Common", "DataDir"};
                mock_rr.when(() -> RegistryReader.readRegistry(input)).thenReturn("invalid");
                assertTrue(DPDataDirectory.getSSCertificatesPath().contains("invalid"));
            } catch (Throwable e) {
                e.printStackTrace();
                fail("Should not have thrown any exception");
            }
        }
        else if(os.isUnix()){
            Throwable exception = assertThrows(DecryptSslKeyException.class, DPDataDirectory::getSSCertificatesPath);
            assertEquals("Failed to get MyCompany certificates path", exception.getMessage());
        }
    }

    @Test
    @DisplayName("Test to validate IOException in getSSCertificatesPath")
    @EnabledOnOs(OS.WINDOWS)
    public void testIOExceptionIn_getSSCertificatesPath(){
        try(MockedStatic<RegistryReader> mock_rr = Mockito.mockStatic(RegistryReader.class)){
            mock_rr.when(() -> RegistryReader.readRegistry(ArgumentMatchers.any(String[].class))).thenThrow(IOException.class);
            Throwable exception = assertThrows(DecryptSslKeyException.class,DPDataDirectory::getSSCertificatesPath);
            assertEquals("Failed to get MyCompany certificates path",exception.getMessage());
        }
        catch (Throwable e){
            e.printStackTrace();
            fail("Should not have thrown any exception");
        }
    }

    @Test
    @DisplayName("Test to validate Exception in getSSCertificateFilePath")
    public void testExceptionIn_getSSCertificateFilePath() {
        try (MockedStatic<DPDataDirectory> mock_dir = Mockito.mockStatic(DPDataDirectory.class)) {
            String invalid_path = "invalid_path";
            mock_dir.when(DPDataDirectory::getSSCertificatesPath).thenReturn(invalid_path);
            mock_dir.when(DPDataDirectory::getSSCertificateFilePath).thenCallRealMethod();
            Throwable exception = Assertions.assertThrows(
                    Exception.class, DPDataDirectory::getSSCertificateFilePath
            );
            assertEquals("Failed to get MyCompany sscertificate", exception.getMessage());
        }
    }

    @Test
    @DisplayName("Test to validate Exception in getKeyFilePath")
    public void testExceptionIn_getKeyFilePath() {
        try (MockedStatic<DPDataDirectory> mock_dir = Mockito.mockStatic(DPDataDirectory.class)) {
            String invalid_path = "invalid_path";
            mock_dir.when(DPDataDirectory::getSSCertificatesPath).thenReturn(invalid_path);
            mock_dir.when(DPDataDirectory::getKeyFilePath).thenCallRealMethod();
            Throwable exception = Assertions.assertThrows(
                    Exception.class, DPDataDirectory::getKeyFilePath
            );
            assertEquals("Failed to get MyCompany sscertificate", exception.getMessage());
        }
    }

    @Test
    @DisplayName("Test to validate method getCertFilePaths")
    public void testMethod_getCertFilePaths() {
        try (MockedStatic<DPDataDirectory> mock_dir = Mockito.mockStatic(DPDataDirectory.class)) {
            String invalid_path1 = "invalid_path_1";
            String invalid_path2 = "invalid_path_2";
            mock_dir.when(DPDataDirectory::getSSCertificatesPath).thenReturn(invalid_path1);
            mock_dir.when(DPDataDirectory::getSSCertificateFilePath).thenReturn(invalid_path2);
            mock_dir.when(DPDataDirectory::getCertFilePaths).thenCallRealMethod();
            assertEquals("invalid_path_2;;invalid_path_1", DPDataDirectory.getCertFilePaths());
        }
        catch (Exception e){
            fail("Should not have thrown any exception");
        }
    }

}
