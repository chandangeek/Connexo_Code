package com.elster.jupiter.datavault;

import com.elster.jupiter.orm.Encrypter;

/**
 * Created by bvn on 11/6/14.
 */
public interface DataVaultService extends Encrypter {
    public static final String COMPONENT_NAME = "DVA";

    public String encrypt(byte[] decrypted);
    public byte[] decrypt(String encrypted);
}
