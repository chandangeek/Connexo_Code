package com.elster.jupiter.pki.impl.wrappers.symmetric;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.pki.PlaintextPassphrase;
import com.elster.jupiter.pki.SecurityAccessorType;
import com.elster.jupiter.pki.SecurityManagementService;

import java.util.Optional;

/**
 * Created by bvn on 10/9/17.
 */
public class DataVaultPassphraseImporter extends AbstractDataVaultImporter {

    private final SecurityAccessorType securityAccessorType;
    private final SecurityManagementService securityManagementService;

    public DataVaultPassphraseImporter(SecurityAccessorType securityAccessorType, Thesaurus thesaurus, SecurityManagementService securityManagementService, Optional<String> certificateAlias) {
        super(thesaurus, securityManagementService, certificateAlias);
        this.securityAccessorType = securityAccessorType;
        this.securityManagementService = securityManagementService;
    }

    public PlaintextPassphrase createPlaintextWrapper(byte[] bytes) {
        PlaintextPassphrase instance = (PlaintextPassphrase) securityManagementService.newPassphraseWrapper(securityAccessorType);
        instance.setPassphrase(new String(bytes));
        return instance;
    }

}
