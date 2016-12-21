package com.elster.jupiter.orm;

public interface Encrypter {

    String encrypt(byte[] decrypted);

    byte[] decrypt(String encrypted);
}
