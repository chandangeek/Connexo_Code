package com.elster.jupiter.pki.rest.impl;

import com.elster.jupiter.rest.util.IdWithNameInfo;

/**
 * Created by bvn on 9/26/17.
 */
public class KeypairWrapperInfo {
    public Long id;
    public String alias;
    public Boolean hasPublicKey;
    public Boolean hasPrivateKey;
    public String keyEncryptionMethod;
    public IdWithNameInfo keyType;
    public String key;
}
