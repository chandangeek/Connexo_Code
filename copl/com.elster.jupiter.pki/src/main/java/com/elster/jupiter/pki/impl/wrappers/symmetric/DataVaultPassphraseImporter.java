package com.elster.jupiter.pki.impl.wrappers.symmetric;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.pki.KeyAccessorType;
import com.elster.jupiter.pki.PkiService;
import com.elster.jupiter.pki.PlaintextPassphrase;

import java.util.Optional;

/**
 * Created by bvn on 10/9/17.
 */
public class DataVaultPassphraseImporter extends AbstractDataVaultImporter {

    private final KeyAccessorType keyAccessorType;
    private final PkiService pkiService;

    public DataVaultPassphraseImporter(KeyAccessorType keyAccessorType, Thesaurus thesaurus, PkiService pkiService, Optional<String> certificateAlias) {
        super(thesaurus, pkiService, certificateAlias);
        this.keyAccessorType = keyAccessorType;
        this.pkiService = pkiService;
    }

    public PlaintextPassphrase createPlaintextWrapper(byte[] bytes) {
        PlaintextPassphrase instance = (PlaintextPassphrase) pkiService.newPassphraseWrapper(keyAccessorType);
        instance.setPassphrase(new String(bytes));
        return instance;
    }

}
