
package com.mf.dp.decrypt;

import com.mf.dp.decrypt.exception.DecryptSslKeyException;
import com.mf.dp.decrypt.osvalidator.DPDataDirectory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class DecryptSslKeyTest {

    @Test
    @DisplayName("Test to validate getKeyInfo")
    public void test_getKeyInfo(){
        try (MockedStatic<DPDataDirectory> mock_dir = Mockito.mockStatic(DPDataDirectory.class)) {
            String invalid_path = "invalid_path";
            String invalid_pvt_key = "invalid_key";
            mock_dir.when(DPDataDirectory::getKeyFilePath).thenReturn(invalid_path);
            try (MockedStatic<DecryptSslKey> mock_key = Mockito.mockStatic(DecryptSslKey.class)){
                mock_key.when(() -> DecryptSslKey.readPrivateKeyFromPEM(invalid_path)).thenReturn(invalid_pvt_key);
                mock_key.when(DecryptSslKey::getKeyInfo).thenCallRealMethod();
                assertDoesNotThrow(DecryptSslKey::getKeyInfo);
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail("Should not have thrown any exception");
        }
    }

    @Test
    @DisplayName("Test to validate method getDPCertPath")
    public void test_getDPCertPath(){
        try(MockedStatic<DPDataDirectory> mock_dir = Mockito.mockStatic(DPDataDirectory.class)){
            DecryptSslKey obj = new DecryptSslKey();
            String invalid_path = "C:\\ProgramData\\MyCompanyTmp";
            mock_dir.when(DPDataDirectory::getCertFilePaths).thenReturn(invalid_path);
            assertEquals(invalid_path,obj.getDPCertPath());
        } catch (DecryptSslKeyException e) {
            fail("Should not have thrown any exception");
        }
    }

    @Test
    @DisplayName("Test to validate Exception and output in convertToPrivateKeyFromDER")
    public void testExceptionAndOutputIn_convertToPrivateKeyFromDER() {
        byte[] encrypted = { -102, -22, 73, -117, 96, -2};
        Throwable exception = Assertions.assertThrows(
                DecryptSslKeyException.class, () -> DecryptSslKey.convertToPrivateKeyFromDER(encrypted)
        );
        assertTrue(exception.getMessage().contains("Failed to convert to PrivateKey"));
    }

    @Test
    @DisplayName("Test to validate Exception and output in readPrivateKeyFromPEM")
    public void testExceptionAndOutputIn_readPrivateKeyFromPEM() {
        Throwable exception = Assertions.assertThrows(
                DecryptSslKeyException.class, () -> DecryptSslKey.readPrivateKeyFromPEM("abcdefg")
        );
        assertEquals("Private Key .enc file not found", exception.getMessage());
    }

    @Test
    @DisplayName("Test to validate Exception in getPrivateKeyString")
    public void testExceptionIn_getPrivateKeyString(){
        BufferedReader br_1 = new BufferedReader(new StringReader("-----BEGIN PRIVATE KEY-----\n" +
                "MIGEAgEAMBAGByqGSM49AgEGBSuBBAAKBG0wawIBAQQgVcB/UNPxalR9zDYAjQIf\n" +
                "jojUDiQuGnSJrFEEzZPT/92hRANCAASc7UJtgnF/abqWM60T3XNJEzBv5ez9TdwK\n" +
                "H0M6xpM2q+53wmsN/eYLdgtjgBd3DBmHtPilCkiFICXyaA8z9LkJ\n" +
                "-----END PRIVATE KEY-----"));
        BufferedReader br_2 = new BufferedReader(new StringReader(""));
        Assertions.assertDoesNotThrow(
                () -> DecryptSslKey.getPrivateKeyString(br_1)
        );
        Throwable exception = Assertions.assertThrows(
                DecryptSslKeyException.class, () -> DecryptSslKey.getPrivateKeyString(br_2)
        );
        assertEquals("Private Key not found", exception.getMessage());
        try {
            BufferedReader mocked_br = mock(BufferedReader.class);
            when(mocked_br.readLine()).thenThrow(IOException.class);
            DecryptSslKey.getPrivateKeyString(mocked_br);
        } catch (Throwable e) {
            assertTrue(e.getMessage().contains("Failed to read Private Key file"));
        }
    }

    @Test
    @DisplayName("Test to validate Exception and output in decryptPrivateKey")
    public void testExceptionAndOutputIn_decryptPrivateKey(){
        Throwable exception_1 = Assertions.assertThrows(
                DecryptSslKeyException.class, () -> DecryptSslKey.decryptPrivateKey("")
        );
        assertTrue(exception_1.getMessage().contains("Failed to read INET Private Key file"));
        Throwable exception_2 = Assertions.assertThrows(
                DecryptSslKeyException.class, () -> DecryptSslKey.decryptPrivateKey("\0")
        );
        assertEquals("Unknown Exception occurred,failed to read INET Private Key file", exception_2.getMessage());
        try(MockedStatic<Files> mock_f = Mockito.mockStatic(Files.class)) {
            byte[] key = { -102, -22, 73,-102, -22, 73,-102, -22, 73,-102, -22, 73,-102, -22, 73,-102, -22, 73,-102, -22, 73,-102, -22, 73,-102, -22, 73,-102, -22, 73,-102, -22, 73,-102, -22, 73,-102, -22, 73,-102, -22, 73,-102, -22, 73,-102, -22, 73,-102, -22, 73};
            mock_f.when(()->Files.readAllBytes(any(Path.class))).thenReturn(key);
            try(MockedStatic<DecryptSslKey> mock_key = Mockito.mockStatic(DecryptSslKey.class)){
                mock_key.when(()->DecryptSslKey.getDecryptedPrivateKeyString(any(byte[].class),anyInt())).thenReturn("invalid_output");
                mock_key.when(()->DecryptSslKey.decryptPrivateKey(anyString())).thenCallRealMethod();
                assertEquals("invalid_output",DecryptSslKey.decryptPrivateKey("c:"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    @DisplayName("Test to validate Exception in getDecryptedPrivateKeyString")
    public void testExceptionIn_getDecryptedPrivateKeyString(){
        byte[] encrypted = { -102, -22, 73, -117, 96, -2, 104, 63, 104, 45, -120, -41, 99, 108, 7, -12, -41, -93, -100, -85, 127, 127, 113, 53, -58, 120, -122, -88, 70, -106, -31, 55, -87, -34, 100, 120, -73, 77, -20, 62, -113, 120, -73, -70, -28, -62, -113, 116, -34, -2, 51, -31, -87, -8, 22, 27, 80, 43, -101, -91, -8, 5, -113, -22, 35, 101, 62, -115, -75, 44, -79, 51, 118, -6, 61, 50, 90, 21, -89, 14, 74, -69, 27, -120, 67, -44, 36, -7, 27, 30, 12, -128, -101, -114, 64, 20, 110, 18, -115, 37, -23, 80, 75, -51, -27, -79, 70, 76, -116, -125, -126, -68, 27, 74, -8, 58, 59, 32, 109, 102, 117, -105, 60, -92, -20, -6, 100, -38, 32, 95, 127, -125, -80, 62, 72, 25, 8, -74, -107, -126, -112, 102, 44, -39, -49, 96, 114, 25, -33, -31, -32, -27, -17, 19, -69, 119, 38, 13, -54, -55, 25, -98, -10, -105, -17, 36, 74, 114, -9, -110, 59, 40, -101, -92, 125, 86, -123, 111, -1, -20, 96, -11, 119, -36, -88, 41, -108, -50, -21, 7, -44, 84, 111, -53, 57, -91, -67, -75, 96, 58, 70, -27, 29, -88, -23, -41, 20, -12, 71, 41, -50, 66, 104, -15, -80, 121, -101, -22, 34, -26, 55, 5, -95, 120, -4, -61, 70, 9, -88, -58, 84, 69, 22, -81, 79, 69, -2, 82, 15, -91, 44, -111, -5, -128, 118, 94, 8, -61, -79, 11, 108, 17, 19, 65, 94, -18, -76, 61, 82, -107, 125, 101, -92, 98, -124, -10, -103, -65, 90, -37, -126, -18, -14, 61, -108, -35, 108, -124, -22, 116, -95, -118, -38, 2, 120, -24, 58, -53, 123, 2, -81, -88, 84, 119, 72, -69, -119, 70, -21, -26, -128, -28, 57, -81, 108, -10, -24, 93, 60, 95, -49, -17, -86, 21, 21, 95, -57, -36, -53, 56, -101, -16, -51, -26, 81, 46, 40, 40, 13, 3, 32, -33, 60, -85, 7, 90, 11, -102, -75, -122, -106, -118, -41, -41, -34, -122, -99, -93, 83, 21, -19, -5, -28, 37, 105, 91, -48, -93, -83, 38, -32, 32, 26, -90, -52, 15, 9, -89, 19, -39, 38, 110, 98, 41, -72, 62, 36, -62, 11, 87, -105, 94, 65, 60, -104, -51, 105, 109, 108, -68, 44, -123, 29, 125, 15, -30, -101, 33, 27, -49, 74, 80, 16, -32, 60, -90, -27, 21, 44, -26, -64, 71, -10, 88, -118, 19, 3, -37, 71, 61, 83, -95, -44, -70, 32, -46, 76, 89, -46, -83, -121, 101, -37, 19, 108, 36, -109, 71, 22, 15, 37, -59, -65, 76, -30, -25, -87, -1, -92, -28, 53, -24, -70, 56, -59, -68, -92, -27, -128, 33, 20, 75, -43, 70, -88, 15, 72, -106, 48, -127, -112, -71, -74, -68, 91, -95, 69, 33, -111, -12, 85, -76, -19, -110, -14, -71, 62, 121, 86, -52, -111, 116, -108, -80, 47, -85, 108, 116, 93, 104, -93, 78, -2, 11, 46, 43, -127, 48, -117, 51, -69, 19, -125, 87, 100, -72, -12, 121, -103, 25, -38, 79, 19, -24, -53, 56, -33, 125, 98, -6, 104, 22, -92, -67, -53, -124, 93, -23, -31, 2, 34, 92, 56, 84, -78, -11, -67, -99, 8, 33, -81, 22, 33, 24, -120, -44, -82, -99, -10, -89, 23, -94, 122, 41, -104, -105, 31, 19, 16, 47, -76, 103, 2, -123, 48, 103, -82, -28, 85, 19, 101, -6, 10, 57, 86, -63, -12, 34, -60, 60, 26, -25, -4, -88, 125, 102, 60, -21, -59, -7, 12, -11, 13, 100, -47, 47, 120, 27, -76, -120, -93, -101, -57, 90, -22, 106, 48, 119, -76, 36, 76, -76, -116, -54, 86, 94, 96, 89, -126, 63, 21, 37, 84, 95, 45, -9, -63, -114, 75, -91, -36, 77, -110, -39, 95, -46, 56, 23, 38, -29, -53, -78, 79, -11, -83, 86, -104, 0, 70, 104, -105, 34, -114, 29, 14, 45, 21, -72, 6, 46, 53, -119, -6, 92, -35, -89, 11, -67, -71, -54, 34, -13, -124, 63, 75, -91, -39, 26, -39, 121, -91, 66, 107, -68, 93, 100, 27, 62, 123, 115, -26, 120, -116, 48, -34, -32, 55, 57, -28, 89, -59, 26, 108, -6, -109, 126, 28, 25, -50, 50, 3, -58, -122, 36, -89, 84, -64, 101, 8, -3, -124, -46, 122, 111, 114, -9, 17, 59, 0, -91, -44, -110, 41, 64, -38, 121, -34, -120, 66, -87, -101, 6, -16, 95, 44, 28, -43, -17, -53, 92, 15, -82, -71, 52, 110, 100, 32, -7, 83, -99, 121, -23, -87, 81, -125, -6, -58, 14, -88, 33, 8, 83, -63, 7, 90, -68, 47, 40, 63, 85, -128, 69, -76, -64, 66, -32, -29, 75, -44, -35, 92, -90, 95, 77, -52, -1, -47, -32, -48, 95, 73, -118, -85, 90, 26, 106, 71, 95, 121, 78, -83, -98, 123, 17, -45, -16, 84, 68, 40, -14, 49, -53, -28, 122, -30, 94, -125, -2, 9, 88, 30, 22, 7, -67, -37, 3, 36, 127, 125, -34, 31, 100, -4, -58, 4, 32, 59, -83, -21, 108, 76, 117, 80, -53, -39, -53, -12, 93, -18, -66, -79, 56, -98, 120, -41, 53, 102, -74, 9, 99, 98, -104, 74, 43, -64, -28, -56, -97, 4, 66, 29, 33, 55, 98, 96, -82, -125, -55, 15, -69, 125, -62, -126, -97, -104, -49, -68, 92, 91, -35, -78, -54, 118, 122, -80, 101, 82, 0, -122, 116, -95, -9, -28, -26, 116, -33, -35, 123, 110, -109, -109, 29, -68, -25, 117, 23, 57, 53, -15, 48, 72, 75, 28, 79, -15, 9, -48, 29, 19, 3, 86, -78, -4, -20, -78, -54, 25, -13, -125, -21, 27, -55, 32, -23, -66, -76, 115, -30, -99, 115, -22, 81, 94, -16, 68, 82, 47, -93, 37, 55, 108, 67, -8, -113, -30, 98, 125, -87, -92, 15, 33, -78, 48, -85, 25, 123, -56, 72, -76, -42, 45, 53, 54, 107, -101, -45, 27, -16, -38, 106, -82, 112, 46, 114, 102, -46, -121, 69, -76, -17, -112, -7, -86, 87, 72, 109, 83, 53, 63, -35, 101, 117, 82, -93, 61, 83, -128, -41, -62, 110, -65, -11, 18, 101, -116, 33, -70, -90, 19, -126, 5, -86, 50, -123, 15, 17, 30, -54, -92, -104, -96, 42, 116, -113, -18, -91, 109, 8, -111, 4, -50, 70, -121, -99, 124, 7, -55, -60, 45, -7, -8, -55, -114, 44, -89, -47, -34, -101, -58, 92, 17, -106, 20, -51, 12, -20, -8, 20, 111, 41, 23, 30, -9, 18, 80, 4, 63, -79, 20, -2, -36, 4, 111, -84, 100, -89, -18, -73, 122, 0, 43, -31, -65, -41, -30, -32, -53, -22, 4, 72, -50, -54, 65, 125, 66, 98, 97, -43, -115, 17, -33, -36, -114, -25, -59, 106, 66, 6, 118, -89, 126, 51, 100, 53, 33, -52, 6, 4, 37, 94, -35, 17, -82, 56, -70, -78, 115, 1, 91, -40, -95, 92, 6, -49, 14, -68, -60, -58, -22, 17, -100, 12, -99, -1, -41, 15, 62, 70, 33, -69, -67, 63, 30, -98, -18, 31, 57, -12, 113, 78, 75, 75, -111, -24, -24, 23, 47, -71, 47, -82, 67, -43, -47, -26, 67, 65, -55, -13, 24, 116, 116, -109, -92, -29, -58, -79, 112, 125, -81, 109, 51, 44, -45, 70, 102, 11, 99, -105, 70, -52, -85, -127, 74, 115, -74, 40, 59, -77, 31, -38, 26, -65, 63, -13, -36, -106, -71, 73, -54, -25, -45, -61, 17, 84, -73, -124, 37, -32, 87, 57, 4, 77, -24, 122, 114, 38, 26, 48, -68, 97, 36, 98, 109, -62, -79, 12, -111, -29, -89, -88, 90, 40, -69, 62, -55, 77, 23, 16, 121, -51, -92, 39, -39, -82, 95, -122, 21, 122, 116, 77, 47, -114, -85, -101, 93, -88, 22, -97, 50, -21, 61, 88, -92, -71, 91, 38, 47, -86, -53, 117, 111, -80, 41, 59, 39, -14, 52, 87, -17, 79, 103, 100, -102, 7, -36, 45, -104, 91, 51, 47, 35, -62, -92, -87, 45, 105, 48, 71, -98, -100, -92, 31, -41, -22, -75, 87, 21, 80, 17, -42, -125, 77, 119, -119, -126, 93, -33, 42, -122, 61, -91, 49, 94, 68, -35, 111, -84, -81, 52, 16, 27, -104, -70, 32, -120, 85, 94, 82, 21, -32, 57, 51, 92, 101, 41, 72, 111, -15, -27, -98, -88, -99, -65, 111, 84, -30, 27, -96, 30, -124, -93, -69, -111, -69, -85, -80, 109, 101, 111, 71, -116, -43, 2, -88, -101, -9, 71, -77, -74, -120, 110, 101, -18, -34, -75, 53, 109, 57, 34, -40, -92, -39, 86, -45, 70, -85, -63, 76, 95, 109, -29, -124, -47, 13, -88, 54, -62, 72, 121, 63, -38, -83, 106, -75, 23, -55, 44, 62, -87, 37, -58, 38, 64, -86, 9, -57, -110, 97, -4, -60, -15, -83, 107, -28, -35, 60, -39, -88, 48, 41, -97, -102, -104, -121, -45, -66, 122, 30, -45, 62, 67, 15, 119, -59, -113, 96, 122, -99, 35, -6, 55, -27, -71, -77, -91, 106, -77, 13, -25, -99, -1, -104, 39, -122, -82, -90, -27, 3, 9, -87, 85, -35, 57, -30, 66, -34, -90, 113, -111, 6, 99, 21, 87, -57, -101, 44, 39, 115, 6, 19, 9, 59, -84, -14, -115, 123, -1, 80, -22, 104, 30, -30, 79, 74, -19, 52, 107, 87, -82, 111, -110, -42, -56, 126, -30, -105, 24, -119, 69, 73, 88, -93, -14, -13, -2, -120, -63, 61, -4, 64, -79, -22, -22, -75, 106, -73, 56, -122, -34, 46, -11, 6, 54, -14, -29, 74, 116, 90, 79, 50, -9, -16, -79, 82, -82, 117, 75, 114, 106, -10, 75, -38, -70, -65, 102, -31, -29, 114, -55, 68, 119, -75, 95, 58, 37, 23, 94, 58, 15, -5, 86, -118, 28, 95, -23, 52, 31, -102, -54, -83, 88, -38, 34, 67, -40, 31, -90, -99, -113, 27, -86, 8, 95, 39, -95, 20, -96, -48, 3, -96, -94, 50, 88, -64, -72, 60, 28, -113, 80, 120, -82, -117, -73, -74, 53, 70, 32, 37, -112, 28, 114, -68, 108, 99, -67, 102, 12, 56, -82, -45, -66, 35, 81, -65, 126, 94, -19, 99, -119, -97, -6, 43, 6, 46, -2, 31, -79, 121, 89, -62, 37, 105, 72, 82, -81, -2, -100, 15, 77, -108, 78, 69, 112, 106, -21, 29, -123, 74, -79, -51, 48, 116, -46, 73, -91, -78, -3, -55, -121, -7, -9, -20, -115, 87, -55, 99, 79, 120, -12, 112, 112, -124, -36, -90, 19, 39, 38, -87, 104, -2, 75, 20, -98, -97, 121, 37, 54, -105, -66, -14, -105, -5, 65, -38, 100, 13, 18, -86, 126, 92, 11, 9, 51, -37, -84, 90, -35, -112, -62, 110, -98, 49, 99, 71, 61, 27, -60, 121, 123, 36, -43, 122, 73, -13, 39, 90, 31, -107, -50, 91, 43, -13, 69, 22, 58, -66, -106, -26, 12, -84, 17, 96, -9, 1, 10, -85, 94, 76, 61, 115, -125, 109, -25, -118, -124, -80, -37, 6, 105, -55, 35, -124, -75, 66, -55, 10, 107, -90, 44, 76, 59, -65, -56, 14, -5, -31, 51, 75, -7, 112, 121, 65, 24, 78, 47, -31, -29, 47, 52, -76, -3, 40, 72, -15, 29, -70, 124, 90, -124, -21, 75, 62, -16, -127, -86, -53, 32, -44, 54, -125, -21, -30, 66, 25, -22, 18, 53, 112, 17, 43, -92, -51, -97, 92, 39, 2, -12, -55, 6, 117, 100, -101, -116, 71, 17, 64, 104, 120, -115, 84, 121, 94, 19, 19, -13, -127, -42, 105, 58, -78, 100, 95, 60, 120, -16, 59, -16, -4, -92, -74, -71, 110, -2, 70, 8, -39, 122, -85, 69, -9, 89, 16, 124, 46, -34, 56, -10, -58, 118, 122, -127, -67, 104, 118, 81, 71, 27, -41, 103, 15, 21, 57, 44, -36, -26, 119, 45, -17, -51, 59, -126, -106, 51, 103, 73, 118, -30, 5, -117, 99, -100, -93, -18, -58, 102, 89, 103, 43, -58, 22, -122, 89, 45, -55, -67, -117, 41, 109, -112, -48, 11, 86, 101, 75, 42, 97, 93, -118, 59, 110, 79, -91, -66, -3, 90, 35, -87, -123, -79, -34, -35, -35, 6, 22, -91, 23, 74, -78, 28, 78, 81, 48, 19, -46, -48, 93, -84, 113, -18, -10, -98, 13, 6, 79, -61, -52, 76, -68, 7, 116, 12, -79, 64, 24, 88, -3, 24, 34, -63, 55, -106, 51, -3, -76, -112, 114, -54, 114, -59, -110, -120, -93, -77, 83, -1, -123, 66, -59, 32, -69, -99, -3, -32, -20, -84, 107, 5, -23, 28, 26, -88, 6, 13, 37, 36, 17, 109, -18, -70, 72, -44, -96, 120, -94, -30, -3, -71, 42, -94, 101, 19, -126, 81, -88, -22, 42, -113, 80, -46, 82, -44, -59, 118, 18, 84, 43, 28, 104, 102, -83, -28, 69, -32, 69, -34, -56, 116, -99, 30, -53, 92, -89, -71, -112, 50, 18, 53, -51, -10, 80, -109, -125, -14, -32, 79, 50, -101, -44, 39, 39, -80, 31, -21, -12, 104, 126, 56, -128, 64, -55, 62, -54, 8, 5, 98, -53, 17, -104, -60, -74, 24, 94, -2, -96, 102, -31, 105, 84, -50, -19, -86, 52, 29, 111, -92, -104, -2, 36, 13, -118, -79, -93, 94, -41, -66, -107, -81, -103, -49, 106, -77, -23, 3, 11, -98, 67, 119, 30, 17, -21, 41, 22, 86, 38, 91, -72, 93, 122, -38, -31, -74, 70, -27, -19, -95, 85, -1, -38, -28, -48, 70, 14, -103, -31, 29, 57, -128, 0, -50, 39, -10, -67, -19, -82, -32, 64, -21, 83, -61, -4, 12, -42, 64, 38, 53, -92, -115, 108, -110, -67, 28, 41, -78, 23, 62, 51, -35, -91, 84, -10, -102, -124, 75, -121, 127, -76, 14, 71, 84, -127, -119, -54, 25, 86, -66, -61, 27, 7, -94, -79, 117, 80, -88, -112, -60, -22, -100, -57, 10, -6, 119, -113, -114, 123, 16, 116, 117, 44, 4, 10, -106, -22, -1, -6, 5, 0, 91, 47, -99, 87, -37, -24, 65, 15, 119, 113, -23, 63, 80, 114, 89, -39, 39, 74, 125, 93, -93, 40, -97, 65, 44, -75, -128, 41, 66, -10, 125, -66, -4, -91, 95, -102, -71, 61, -54, -9, -98, -43, 88, 9, 50, 35, -25, -28, 61, 70, -51, -77, 11, 46, 105, -103, -113, 21, -52, 42, -93, 123, -67, -80, -18, 78, -67, 31, 125, 54, -36, 63, -124, 90, 122, 33, -124, -123, -13, -91, 44, 37, 127, 27, -21, 41, 8, -86, 62, 51, -70, -98, -116, -9, 125, -40, 107, 66, -41, 4, 94, -107, 20, 71, -108, -104, 123, 78, -87, 123, -113, 1, -76, -43, -4, -81, 99, -77, -8, -126, 19, 56, -43, -65, 111, 73, 74, 58, -32, 23, -107, -112, 42, -48, 51, 37, -72, -39, 60, 37, -61, -126, -94, 5, -98, -117, -43, 32, 72, -82, -76, -102, -49, 85, 110, -5, 79, 75, -111, -42, -56, -68, 60, 56, 60, -20, -108, 23, -128, 97, -11, 93, 40, 44, -121, 34, 28, -57, 40, -47, -59, 115, -16, 107, -49, -3, -24, -26, -89, 60, -60, 15, -71, -105, -21, -70, -24, -59, 25, 116, -53, 52, 110, -55, 91, -76, 78, 13, 67, 115, 83, 101, 89, -127, 28, 73, -87, 69, -57, 74, 60, 0, 0, -120, -102, -119, 126, 54, -59, 34, 54, -70, 81, 74, -92, -35, 98, 5, -73, -102, -126, 118, -25, -80, 31, -71, -105, 107, -83, 14, 28, 19, 20, -75, -19, 127, -121, -49, 4, -94, -43, 112, 75, 72, -125, 69, 33, 127, -40, -100, -37, -125, -87, 52, -59, 81, 89, 113, -64, -80, 37, 108, 75, 24, -49, 12, 94, -17, -68, -8, -97, 7, -127, -40, 89, 10, 110, -21, 103, 126, 35, -126, -52, 42, 108, -23, 87, -66, -96, 88, -36, 113, 25, -41, -76, -71, -124, -106, -117, 106, 127, -57, -56, -19, -98, -43, 75, 10, -78, -75, 116, 91, 21, -103, 105, 123, -2, 114, 110, -64, 3, 102, 23, -80, 106, 24, -59, -20, -5, -8, -11, 105, -57, -83, -96, 54, 36, 92, -14, -13, -103, 108, 58, -115, -60, -34, -92, 47, -85, -98, 74, -38, 13, 126, 78, -32, -78, 78, 104, 50, 41, -118, -55, -99, -40, -88, 62, -103, 99, -21, 49, -60, 6, -33, 76, 9, 15, -10, 114, -109, -40, -127, -72, 40, -119, -28, 15, -47, -83, -5, 33, -83, 64, -98, 122, -61, -128, -20, -20, -65, -24, 23, -106, -105, -69, 57, -49, -110, 90, 73, 77, 15, -97, -80, 24, 1, -107, 74, -105, 85, -110, -12, 84, -14, 0, 84, 101, 24, -49, 112, -119, -11, 13, -119, -110, -16, -80, -86, 99, 50, 37, -100, -31, 57, -42, 68, 82, 62, 16, 23, -117, 11, -126, -120, 19, -18, 117, 2, 118, 21, 87, -128, -65, -118, 1, -88, 9, 125, 36, -59, -71, 80, -99, -48, 95, 75, 94, -18, -116, -121, 44, -44, 6, -45, 72, 118, -108, -95, -76, 53, 115, -42, -4, -38, 68, -122, -60, -87, 126, -66, -124, 15, 87, 25, -116, -21, 92, -77, 98, -99, -69, -4, 45, -18, -106, -15, 117, -109, 44, 71, 67, 73, -90, 37, -20, -54, 51, 3, 0, 106, -106, 62, -4, 113, -39, 98, -58, 9, -127, 49, -55, -20, 98, -16, -50, -96, -56, 114, -108, 75, 55, -118, -15, -124, -21, -17, 80, 98, 29, -85, -24, 101, -128, -112, 77, -18, -63, -90, 51, -7, 88, 38, -47, -3, -89, -9, 32, -51, -101, 9, -20, 36, 29, -87, 102, -19, -69, -10, 82, 73, 18, 6, -27, -70, -99, -25, 52, 40, 89, 93, -3, 16, 86, 105, -16, -7, 104, -2, 23, 53, -62, -60, 75, 55, -41, -31, 9, 123, -109, 43, -22, 49, -26, 49, 91, 123, 124, 34, -52, 87, 106, -38, 36, -111, 106, 72, 37, 98, 79, -4, 27, -8, -63, -58, -13, -57, -40, -124, -108, 26, 112, -49, -7, 25, -120, 36, 43, 45, 52, -58, 28, 14, -47, -108, 13, 106, -6, 42, 36, 23, -24, -73, -48, -47, 126, 43, -55, 104, -81, -95, -65, 104, 92, -109, 54, -56, -111, 119, -20, -117, -113, -71, 50, 50, -124, 95, 108, 14, -17, -112, 9, -127, 67, -95, 60, 72, -120, -28, -13, -98, -96, -67, -71, 102, 50, -19, 114, -9, 99, -69, 108, -62, -31, -100, -59, 26, -50, 91, -101, -11, -12, 30, -107, -60, 61, -85, -62, -43, -78, -120, -109, -69, -102, -31, -77, -71, 121, -59, -120, 97, -88, -121, 87, -38, 101, -47, 85, 11, -124, 4, 68, -51, -106, -114, 90, -69, -20, -43, -92, -19, 87, -31, -83, -20, -57, 76, 22, 31, 60, -98, -127, -84, 46, 33, 67, 65, -34, 116, -54, 85, 22, -13, 55, 11, -90, 51, 76, -57, 102, -124, 29, -102, -104, 94, 71, 44, 22, -33, 97, -123, -84, -41, -120, -36, -56, -28, 93, -118, 126, -48, -19, 7, 92, -25, -110, -101, -100, -91, 28, 127, -54, -6, 30, -118, -126, 111, 124, -71, 93, 58, -116, -24, -26, 42, 107, -17, 27, 46, 91, 54, -75, -12, 114, 105, -109, -9, 37, 29, 117, 0, 77, -65, -22, -21, 1, 117, -100, 17, 69, -106, -96, -115, -56, -118, 23, 100, 42, -104, 77, -87, -47, 92, -15, -44, -73, -38, 104, 124, -39, 62, -13, -92, 103, -33, -106, 105, -128, -91, 105, 7, -121, -59, 100, 26, 76, -85, -9, -114, 81, -7, 51, -88, -114, -95, -54, -82, -116, -83, -26, 111, -12, 111, -120, -104, -108, -100, -9, -116, 14, 59, -59, -72, -34, -4, -28, -19, -33, -95, -93, 58, -39, -114, 62, 23};
        Assertions.assertDoesNotThrow(() -> DecryptSslKey.getDecryptedPrivateKeyString(encrypted,48));
        Throwable e = Assertions.assertThrows(
                DecryptSslKeyException.class, () -> DecryptSslKey.getDecryptedPrivateKeyString(encrypted,2)
        );
        assertTrue(e.getMessage().contains("Failed to decrypt Private Key file"));
    }
}