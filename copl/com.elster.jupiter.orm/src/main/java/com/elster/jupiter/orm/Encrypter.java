package com.elster.jupiter.orm;

import aQute.bnd.annotation.ConsumerType;

@ConsumerType
public interface Encrypter {

    String encrypt(byte[] decrypted);

    byte[] decrypt(String encrypted);
}
