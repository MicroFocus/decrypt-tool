
package com.mf.dp.decrypt.osvalidator;

public class OSValidator {
    private static final String OS_NAME="os.name";

    public boolean isWindows() {

        String os = System.getProperty(OS_NAME).toLowerCase();
        // windows
        return (os.contains("win"));

    }

    public boolean isMac() {

        String os = System.getProperty(OS_NAME).toLowerCase();
        // Mac
        return (os.contains("mac"));

    }

    public boolean isUnix() {

        String os = System.getProperty(OS_NAME).toLowerCase();
        // linux or unix
        return (os.contains("nix") || os.contains("nux"));

    }
}
