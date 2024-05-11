
package com.mf.dp.decrypt.osvalidator;

import com.mf.dp.decrypt.exception.DecryptSslKeyException;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

public class DPDataDirectory {
    private static final String MyCompany_LOCAL_CERT_FILE = "localhost_cert.pem";
    private static final String MyCompany_ENC_KEY_FILE = "localhost_key.enc";
    private static final String MyCompany_LINUX_CERT_PATH = "/etc/opt/omni/client/sscertificates/";
    private static final String MyCompany_WIN_REL_CERT_PATH = "Config\\client\\sscertificates\\";
    public static final String DELIMITER = ";;";

    private static String sscertificatesPath = null;
    private static String sscertificateFilePath = null;
    private static String sscertificateKeyFilePath = null;
    private static String certificatesPaths = null;

    private static final String MyCompany_COMMON_KEY_WIN_DIR_COMMON_KEY_WIN_DIR =
            "HKEY_LOCAL_MACHINE\\SOFTWARE\\MyCompany\\OpenView\\OmniBackII\\Common";
    private static final String MyCompany_DATADIR = "DataDir";
    public static final Logger log = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    private DPDataDirectory() {
        throw new IllegalStateException("DPDataDirectory is a Utility class");
    }

    public static String getSSCertificatesPath() throws DecryptSslKeyException {
        OSValidator os=new OSValidator();
        if (sscertificatesPath == null) {
            if (os.isWindows()) {
                try {
                    sscertificatesPath = RegistryReader.readRegistry(
                            new String[]{MyCompany_COMMON_KEY_WIN_DIR, MyCompany_DATADIR});

                    if (!sscertificatesPath.endsWith("\\")) {
                        sscertificatesPath += "\\";
                    }
                    sscertificatesPath += MyCompany_WIN_REL_CERT_PATH;
                } catch (IOException e) {
                    throw new DecryptSslKeyException("Failed to get MyCompany certificates path");
                }
            } else if (os.isUnix()) {
                sscertificatesPath = MyCompany_LINUX_CERT_PATH;
                File file = new File(sscertificatesPath);
                if (!file.isDirectory()) {
                    throw new DecryptSslKeyException("Failed to get MyCompany certificates path");
                }
            }
        }
        return sscertificatesPath;
    }

    public static String getSSCertificateFilePath() throws DecryptSslKeyException {
        if (sscertificateFilePath == null) {
            sscertificateFilePath = getSSCertificatesPath() + MyCompany_LOCAL_CERT_FILE;
            File file = new File(sscertificateFilePath);
            if (!file.isFile()) {
                throw new DecryptSslKeyException("Failed to get MyCompany sscertificate");
            }
        }
        return sscertificateFilePath;
    }

    public static String getKeyFilePath() throws DecryptSslKeyException {
        if (sscertificateKeyFilePath == null) {
            sscertificateKeyFilePath = getSSCertificatesPath() + MyCompany_ENC_KEY_FILE;
            File file = new File(sscertificateKeyFilePath);
            if (!file.isFile()) {
                throw new DecryptSslKeyException("Failed to get MyCompany sscertificate");
            }
        }
        return sscertificateKeyFilePath;
    }

    public static String getCertFilePaths() throws DecryptSslKeyException {
        if (certificatesPaths == null) {
            certificatesPaths = getSSCertificateFilePath() + DELIMITER + getSSCertificatesPath();
        }
        return certificatesPaths;
    }
}
