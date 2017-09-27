package com.elster.jupiter.pki.rest.impl;

import com.elster.jupiter.pki.KeypairWrapper;
import com.elster.jupiter.rest.util.IdWithNameInfo;

/**
 * Created by bvn on 9/26/17.
 */
public class KeypairInfoFactory {

    public KeypairWrapperInfo asInfo(KeypairWrapper keypairWrapper) {
        KeypairWrapperInfo info = new KeypairWrapperInfo();
        info.id = keypairWrapper.getId();
        info.alias = keypairWrapper.getAlias();
        info.hasPrivateKey = keypairWrapper.hasPrivateKey();
        info.hasPublicKey = keypairWrapper.getPublicKey().isPresent();
        keypairWrapper.getKeyEncryptionMethod().ifPresent(keyEncryptionMethod -> info.keyEncryptionMethod = keyEncryptionMethod);
        info.keyType = new IdWithNameInfo(keypairWrapper.getKeyType());
        return info;
    }
}
