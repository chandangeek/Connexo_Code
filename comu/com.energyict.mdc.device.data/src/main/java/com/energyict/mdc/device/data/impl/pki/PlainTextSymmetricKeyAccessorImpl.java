/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.pki;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.pki.PlaintextSymmetricKey;
import com.elster.jupiter.pki.SecurityManagementService;

import com.google.inject.Inject;

public class PlainTextSymmetricKeyAccessorImpl extends SymmetricKeyAccessorImpl {

    public PlainTextSymmetricKeyAccessorImpl(){
        super();

    }

    @Inject
    public PlainTextSymmetricKeyAccessorImpl(DataModel dataModel, SecurityManagementService securityManagementService, Thesaurus thesaurus) {
        super(dataModel, securityManagementService, thesaurus);
    }

    @Override
    public void renew() {
        if (tempSymmetricKeyWrapperReference.isPresent()) {
            clearTempValue();
        }
        doRenewValue();
    }

    private void doRenewValue() {
        PlaintextSymmetricKey symmetricKeyWrapper = (PlaintextSymmetricKey) securityManagementService.newSymmetricKeyWrapper(getSecurityAccessorType());
        symmetricKeyWrapper.generateValue();
        tempSymmetricKeyWrapperReference = dataModel.asRefAny(symmetricKeyWrapper);
        this.save();
    }
}
