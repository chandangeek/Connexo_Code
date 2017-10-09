package com.elster.jupiter.pki.impl.wrappers.symmetric;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.pki.KeyAccessorType;
import com.elster.jupiter.pki.PkiService;
import com.elster.jupiter.pki.PlaintextSymmetricKey;

import javax.crypto.spec.SecretKeySpec;
import java.util.Optional;

/**
 * Created by bvn on 10/9/17.
 */
public class DataVaultSymmetricKeyImporter extends AbstractDataVaultImporter {

    private final KeyAccessorType keyAccessorType;
    private final PkiService pkiService;

    public DataVaultSymmetricKeyImporter(KeyAccessorType keyAccessorType, Thesaurus thesaurus, PkiService pkiService, Optional<String> certificateAlias) {
        super(thesaurus, pkiService, certificateAlias);
        this.keyAccessorType = keyAccessorType;
        this.pkiService = pkiService;
    }

    public PlaintextSymmetricKey createPlaintextWrapper(byte[] bytes) {
        PlaintextSymmetricKey instance = (PlaintextSymmetricKey) pkiService.newSymmetricKeyWrapper(keyAccessorType);
        SecretKeySpec secretKeySpec = new SecretKeySpec(bytes, keyAccessorType.getKeyType().getKeyAlgorithm());
        instance.setKey(secretKeySpec);
        return instance;
    }

}
