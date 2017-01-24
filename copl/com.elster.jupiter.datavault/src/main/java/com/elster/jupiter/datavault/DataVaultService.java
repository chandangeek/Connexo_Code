package com.elster.jupiter.datavault;

import com.elster.jupiter.orm.Encrypter;

/**
 * Created by bvn on 11/6/14.
 */
public interface DataVaultService extends Encrypter {
    String COMPONENT_NAME = "DVA";

    String encrypt(byte[] decrypted);
    byte[] decrypt(String encrypted);

}