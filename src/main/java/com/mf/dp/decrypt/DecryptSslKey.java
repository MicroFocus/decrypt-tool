
package com.mf.dp.decrypt;

import com.mf.dp.decrypt.exception.DecryptSslKeyException;
import com.mf.dp.decrypt.osvalidator.DPDataDirectory;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;

/*
 * DecryptSslKey decrypts the MyCompany encrypted Ssl private key &
 * gets MyCompany certificate file directory paths which can be used
 * by any other project using this tool by using class loader.
 */
public class DecryptSslKey {

    public static PrivateKey getKeyInfo() throws DecryptSslKeyException {
            String keyFilePath = DPDataDirectory.getKeyFilePath();
            String pvtKeyString = readPrivateKeyFromPEM(keyFilePath);
            final byte[] bytes = DatatypeConverter.parseBase64Binary(pvtKeyString);
            return convertToPrivateKeyFromDER(bytes);
    }

    public String getMyCompanyCertPath() throws DecryptSslKeyException {
        return DPDataDirectory.getCertFilePaths();
    }

    static PrivateKey convertToPrivateKeyFromDER(byte[] keyBytes) throws DecryptSslKeyException {
        try {
            final PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
            final KeyFactory factory = KeyFactory.getInstance("RSA");
            return factory.generatePrivate(spec);
        } catch (Exception ex) {
            throw new DecryptSslKeyException("Failed to convert to PrivateKey" + ex);
        }
    }

    static String readPrivateKeyFromPEM(String keyFilePath) throws DecryptSslKeyException {
        File encKeyFilePath = new File(keyFilePath);
        if (!encKeyFilePath.exists()) {
            throw new DecryptSslKeyException("Private Key .enc file not found");
        } else {
            BufferedReader decryptedKey;
            decryptedKey = new BufferedReader(new StringReader(decryptPrivateKey(keyFilePath)));
            return getPrivateKeyString(decryptedKey);
        }
    }

    public static String getPrivateKeyString(BufferedReader decryptedKey) throws DecryptSslKeyException {
        try {
            String decryptedPvtKeyStr = decryptedKey.readLine();
            if (decryptedPvtKeyStr == null || !decryptedPvtKeyStr.contains("BEGIN PRIVATE KEY")) {
                decryptedKey.close();
                throw new DecryptSslKeyException("Private Key not found");
            }
            final StringBuilder pvtKey = new StringBuilder();
            decryptedPvtKeyStr = decryptedKey.readLine();
            while (decryptedPvtKeyStr != null) {
                if (decryptedPvtKeyStr.contains("END PRIVATE KEY")) {
                    break;
                }
                pvtKey.append(decryptedPvtKeyStr);
                decryptedPvtKeyStr = decryptedKey.readLine();
            }
            decryptedKey.close();
            return pvtKey.toString();
        } catch (IOException e) {
            throw new DecryptSslKeyException("Failed to read Private Key file" + e);
        }
    }

    static String decryptPrivateKey(String keyFilePath) throws DecryptSslKeyException {
        try {
            byte[] keyFileData = Files.readAllBytes(Paths.get(keyFilePath));
            int dataLen = keyFileData.length;
            if(dataLen <= 48) {
                throw new DecryptSslKeyException("Invalid private key file. Error reading private key");
            }
            return getDecryptedPrivateKeyString(keyFileData, dataLen);
        } catch (IOException e) {
            throw new DecryptSslKeyException("Failed to read INET Private Key file" + e);
        } catch (Exception e) {
            throw new DecryptSslKeyException("Unknown Exception occurred,failed to read INET Private Key file");
        }
    }

    static String getDecryptedPrivateKeyString(byte[] keyFileData, int dataLen) throws DecryptSslKeyException {
        try {
            byte[] keyIvByteArray = new byte[48];
            int i;
            int st = 47;
            int c = 47;

            for(i = 0; i < 48; i++) {
                keyIvByteArray[c] =  (byte)( (keyFileData[i] & 0x0F) << 4 | (keyFileData[i] & 0xF0) >> 4 );
                c = c - 3;
                if(c < 0) { st = st - 1; c = st;}
            }

            SecretKeySpec keySpec = new SecretKeySpec(keyIvByteArray, 0, 32, "AES");
            IvParameterSpec iv = new IvParameterSpec(keyIvByteArray, 32, 16);

            Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, keySpec, iv);
            byte[] original = cipher.doFinal(keyFileData, 48, dataLen - 48);

            return new String(original);
        } catch (Exception ex) {
            throw new DecryptSslKeyException("Failed to decrypt Private Key file" + ex);
        }
    }
}