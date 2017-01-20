package com.elster.jupiter.pki.impl;

import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.pki.CertificateWrapper;
import com.elster.jupiter.pki.PrivateKeyWrapper;

public class CertificateWrapperImpl implements CertificateWrapper {
    private long id;
    private byte[] csr;
    private Reference<PrivateKeyWrapper> privateKeyWrapperReference = ValueReference.absent();

}
