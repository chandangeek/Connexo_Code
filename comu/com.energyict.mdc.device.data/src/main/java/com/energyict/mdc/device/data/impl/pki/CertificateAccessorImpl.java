/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.energyict.mdc.device.data.impl.pki;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.pki.CertificateWrapper;
import com.energyict.mdc.device.data.CertificateAccessor;

import javax.inject.Inject;
import java.util.Optional;

public class CertificateAccessorImpl extends AbstractKeyAccessorImpl<CertificateWrapper> implements CertificateAccessor {
    private final DataModel dataModel;

    private Reference<CertificateWrapper> actualCertificate = Reference.empty();
    private Reference<CertificateWrapper> tempCertificate = Reference.empty();

    @Inject
    public CertificateAccessorImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public CertificateWrapper getActualValue() {
        return actualCertificate.get();
    }

    @Override
    public void setActualValue(CertificateWrapper newWrapperValue) {
        this.actualCertificate.set(newWrapperValue);
    }

    @Override
    public Optional<CertificateWrapper> getTempValue() {
        return tempCertificate.getOptional();
    }

    @Override
    public void save() {
        Save.UPDATE.save(dataModel, this);
    }
}
