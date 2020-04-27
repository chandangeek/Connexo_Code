package com.elster.jupiter.bootstrap.oracle.impl;

import com.elster.jupiter.bootstrap.InvalidPasswordException;
import com.elster.jupiter.bootstrap.PropertyNotFoundException;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import com.elster.jupiter.bootstrap.PasswordDecryptService;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.inject.Inject;
import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.elster.jupiter.bootstrap.BootstrapService.KEY_FILE;

@Component(name = "com.elster.jupiter.bootstrap.password", service = PasswordDecryptService.class, immediate = true)
public class PasswordDecryptServiceImpl implements PasswordDecryptService {

    public PasswordDecryptServiceImpl(){

    }

    @Inject
    public PasswordDecryptServiceImpl(BundleContext bundleContext){
        activate(bundleContext);
    }

    @Activate
    public void activate(BundleContext context) {

    }

    @Override
    public String getDecryptPassword(String encryptedPassword, String filePath) {

        String decryptedPassword = "";
        List<String> list = null;
        try {
            list = Files.lines(Paths.get(filePath))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
        } catch (IOException e) {
            //Logger.getAnonymousLogger().log(Level.SEVERE, exception, () -> "Bootstrap service initialization: encryption failed");
            Logger.getAnonymousLogger().log(Level.SEVERE, () -> "Cannot establish a connection to the database. Check the connection details.");
            throw new PropertyNotFoundException(KEY_FILE);
        }

        if (list.size() != 2) {
            Logger.getAnonymousLogger().log(Level.SEVERE, () -> "Cannot establish a connection to the database. Check the connection details.");
            throw new PropertyNotFoundException(KEY_FILE);
        } else {
            try {
                byte[] aesEncryptionKey = list.get(0).getBytes("UTF-8");
                String id = list.get(1);

                MessageDigest md5 = MessageDigest.getInstance("MD5");
                md5.update(id.getBytes("UTF-8"));
                byte[] encryptedPasswordData = DatatypeConverter.parseBase64Binary(encryptedPassword);


                String initVector = new BigInteger(1, md5.digest()).toString(16).substring(0, 16);
                byte[] iv = initVector.getBytes("UTF-8");
                Cipher decrypt = Cipher.getInstance("AES/CBC/PKCS5Padding");
                decrypt.init(Cipher.DECRYPT_MODE, new SecretKeySpec(aesEncryptionKey, "AES"), new IvParameterSpec(iv));
                byte[] decryptedPasswordData = decrypt.doFinal(encryptedPasswordData);
                decryptedPassword = new String(decryptedPasswordData, "UTF-8");
            } catch (UnsupportedEncodingException | NoSuchAlgorithmException | NoSuchPaddingException |
                    InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException
                    | BadPaddingException | ArrayIndexOutOfBoundsException e) {
                InvalidPasswordException exception = new InvalidPasswordException();
                Logger.getAnonymousLogger().log(Level.SEVERE, () -> "Cannot establish a connection to the database. Check the connection details.");
                throw exception;
            }
        }
        return decryptedPassword;

    }
}
