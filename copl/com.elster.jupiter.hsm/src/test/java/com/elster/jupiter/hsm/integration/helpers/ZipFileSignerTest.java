package com.elster.jupiter.hsm.integration.helpers;

import com.elster.jupiter.hsm.model.krypto.AsymmetricAlgorithm;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 * This is not a real test but rather a helper for testers since we had issues validating signature of files (CSRImporter)
 */
@Ignore
public class ZipFileSignerTest {

    private static final String ts = "truststore.jks";
    private static char[] tsPassword = "password".toCharArray();

    public static final String SIGNED_FILE_PREXIF = "signed-";
    private static final String fileToSign = "some.zip";
    private static File fts;

    @BeforeClass
    public static void beforeClass() {
        fts = new File(ZipFileSignerTest.class.getClassLoader().getResource(fileToSign).getFile());
    }

    @Test
    public void signFile() throws
            CertificateException,
            NoSuchAlgorithmException,
            KeyStoreException,
            IOException,
            UnrecoverableKeyException,
            InvalidKeySpecException,
            InvalidKeyException,
            SignatureException {
        KeyStoreHelper ksh = new KeyStoreHelper(ts, tsPassword);
        Key key = ksh.getKey("shipment-file", "password".toCharArray());
        byte[] bits = Files.readAllBytes(fts.toPath());
        byte[] signature = new FileSignerHelper().signFile(AsymmetricAlgorithm.RSA_15, "SHA1WITHRSA", bits, (PrivateKey) key);
        appendSignature(signature, bits, fts, SIGNED_FILE_PREXIF);
    }

    private void appendSignature(byte[] signature, byte[] bits, File fts, String prefix) throws IOException {
        String absPath = fts.getParentFile().getPath();
        String fileName = prefix + fts.getName();
        File nFile = new File(absPath + FileSystems.getDefault().getSeparator(), fileName);
        if (nFile.exists()) {
            throw new RuntimeException("Delete target file before signing it:" + nFile);
        }
        nFile.createNewFile();

        try (FileOutputStream fout = new FileOutputStream(nFile)) {
            fout.write(bits);
            fout.write(signature);
        }

    }
}
