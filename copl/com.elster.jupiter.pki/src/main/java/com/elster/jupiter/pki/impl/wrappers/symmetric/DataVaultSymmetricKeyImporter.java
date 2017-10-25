package com.elster.jupiter.pki.impl.wrappers.symmetric;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.pki.PlaintextSymmetricKey;
import com.elster.jupiter.pki.SecurityAccessorType;
import com.elster.jupiter.pki.SecurityManagementService;

import javax.crypto.spec.SecretKeySpec;
import java.util.Optional;

/**
 * Created by bvn on 10/9/17.
 */
public class DataVaultSymmetricKeyImporter extends AbstractDataVaultImporter {

    private final SecurityAccessorType securityAccessorType;
    private final SecurityManagementService securityManagementService;

    public DataVaultSymmetricKeyImporter(SecurityAccessorType securityAccessorType, Thesaurus thesaurus, SecurityManagementService securityManagementService, Optional<String> certificateAlias) {
        super(thesaurus, securityManagementService, certificateAlias);
        this.securityAccessorType = securityAccessorType;
        this.securityManagementService = securityManagementService;
    }

    public PlaintextSymmetricKey createPlaintextWrapper(byte[] bytes) {
        PlaintextSymmetricKey instance = (PlaintextSymmetricKey) securityManagementService.newSymmetricKeyWrapper(securityAccessorType);
        SecretKeySpec secretKeySpec = new SecretKeySpec(bytes, securityAccessorType.getKeyType().getKeyAlgorithm());
        instance.setKey(secretKeySpec);
        return instance;
    }

}
