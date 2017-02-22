/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.elster.jupiter.pki.impl.wrappers.certificate;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.pki.ClientCertificate;
import com.elster.jupiter.pki.PrivateKeyWrapper;

import javax.inject.Inject;

public class ClientCertificateImpl extends RenewableCertificateImpl implements ClientCertificate {
    private Reference<PrivateKeyWrapper> privateKeyReference = ValueReference.absent();

    @Inject
    public ClientCertificateImpl(DataModel dataModel, Thesaurus thesaurus) {
        super(dataModel, thesaurus);
    }

    @Override
    public PrivateKeyWrapper getPrivateKey() {
        return privateKeyReference.get();
    }

    public ClientCertificateImpl init(PrivateKeyWrapper privateKeyWrapper) {
        this.privateKeyReference.set(privateKeyWrapper);
        this.save();
        return this;
    }
}
