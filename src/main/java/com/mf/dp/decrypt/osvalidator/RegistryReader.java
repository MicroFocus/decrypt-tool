
package com.mf.dp.decrypt.osvalidator;

import java.io.IOException;
import java.io.InputStream;

import java.io.StringWriter;

public class RegistryReader {

    public static final int KEY_SZ = 0;
    public static final int KEY_DWORD = 1;
    private static final String REG_QUERY = "reg query ";
    private static final String REG_STR = "REG_SZ";
    private static final String REG_DWORD = "REG_DWORD";

    private RegistryReader() {
        throw new IllegalStateException("RegistryReader is a Utility class");
    }

    static String readRegistryRegQuery(String key) throws IOException {
        String useKey = REG_QUERY + key;
        int keyType = -1;

        Process process = Runtime.getRuntime().exec(useKey);
        StreamReader reader = new StreamReader(process.getInputStream());

        reader.start();
        try {
            process.waitFor();
            reader.join();
        } catch (InterruptedException e) {
            DPDataDirectory.log.info(e.getMessage());
            Thread.currentThread().interrupt();
        }

        String result = reader.getResult();
        int p = -1;
        if (result.contains(REG_STR)) {
            p = result.indexOf(REG_STR);
            keyType = KEY_SZ;
        } else if (result.contains(REG_DWORD)) {
            p = result.indexOf(REG_DWORD);
            keyType = KEY_DWORD;
        }

        if (p == -1) {
            return null;
        }

        switch (keyType) {
            case KEY_SZ:
                return result.substring(p + REG_STR.length()).trim();
            case KEY_DWORD:
                String temp = result.substring(p + REG_DWORD.length()).trim();
                return Integer.toString((Integer.parseInt(temp.substring("0x".length()), 16)));
            default:
                return "";
        }
    }

    static class StreamReader extends Thread {
        private final InputStream mIs;

        private final StringWriter mSw;

        StreamReader(InputStream is) {
            mIs = is;
            mSw = new StringWriter();
        }

        @Override
        public void run() {
            int c;
            try {
                while ((c = mIs.read()) != -1) {
                    mSw.write(c);
                }
            } catch (IOException e) {
                DPDataDirectory.log.info(e.getMessage());
            }

        }

        String getResult() {
            return mSw.toString();
        }
    }

    public static String readRegistry(String[] regKey) throws IOException {
        String key = "\"" +
                regKey[0] +
                "\" /v " +
                regKey[1];
        return readRegistryRegQuery(key);
    }
}
