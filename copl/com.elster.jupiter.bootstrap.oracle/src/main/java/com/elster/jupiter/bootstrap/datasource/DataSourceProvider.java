/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.bootstrap.datasource;

import com.elster.jupiter.bootstrap.BootstrapService;
import com.elster.jupiter.bootstrap.InvalidPasswordException;
import com.elster.jupiter.bootstrap.PropertyNotFoundException;
import com.elster.jupiter.bootstrap.oracle.impl.ConnectionProperties;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.sql.DataSource;
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
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public interface DataSourceProvider {
    DataSource createDataSource(ConnectionProperties properties) throws SQLException;

    default String getDecryptedPassword(String encryptedPassword, String filePath) {

        String decryptedPassword = "";
        List<String> list = null;
        try {
            list = Files.lines(Paths.get(filePath))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new PropertyNotFoundException(BootstrapService.JDBC_PASSWORD);
        }

        if (list.size() != 2) {
            throw new PropertyNotFoundException(BootstrapService.JDBC_PASSWORD);
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
                    | BadPaddingException e) {
                InvalidPasswordException exception = new InvalidPasswordException();
                Logger.getAnonymousLogger().log(Level.SEVERE, exception, () -> "Bootstrap service init");
                throw exception;
            }
        }
        return decryptedPassword;

    }
}
