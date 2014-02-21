package com.elster.jupiter.users.impl;

import com.elster.jupiter.orm.DataModel;

import javax.xml.bind.DatatypeConverter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class DigestHa1Util {
    public String createHa1(String realm, String authenticationName, String password) {
            try {
                MessageDigest messageDigest = MessageDigest.getInstance("MD5");
                messageDigest.update(authenticationName.getBytes());
                messageDigest.update(":".getBytes());
                messageDigest.update(realm.getBytes());
                messageDigest.update(":".getBytes());
                messageDigest.update(password.getBytes());
                byte[] md5 = messageDigest.digest();
                return DatatypeConverter.printHexBinary(md5).toLowerCase();
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        }

}
