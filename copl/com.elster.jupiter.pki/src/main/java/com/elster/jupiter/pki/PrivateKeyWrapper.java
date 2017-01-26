package com.elster.jupiter.pki;

import java.security.PrivateKey;

/**
 * Created by bvn on 1/12/17.
 */
public interface PrivateKeyWrapper {
    String getKeyEncryptionMethod();
    PrivateKey getPrivateKey();
}
