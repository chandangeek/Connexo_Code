package com.elster.jupiter.datavault;

/**
 * Created by bvn on 11/6/14.
 */
public interface DataVaultService {
    String COMPONENT_NAME = "DVA";

    String encrypt(byte[] decrypted);
    byte[] decrypt(String encrypted);

}