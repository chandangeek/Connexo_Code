/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.pki;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.pki.HsmKey;
import com.elster.jupiter.pki.SecurityAccessorType;
import com.elster.jupiter.pki.SecurityManagementService;

import com.energyict.mdc.device.data.impl.MessageSeeds;

import com.google.inject.Inject;

public class HsmSymmetricKeyAccessorImpl extends SymmetricKeyAccessorImpl {

    @Inject
    public HsmSymmetricKeyAccessorImpl(DataModel dataModel, SecurityManagementService securityManagementService, Thesaurus thesaurus) {
        super(dataModel, securityManagementService, thesaurus);
    }

    @Override
    public void renew() {
        HsmKey actualValue = (HsmKey) getActualValue()
                .orElseThrow(() -> new PkiLocalizedException(thesaurus, MessageSeeds.ACTUAL_VALUE_NOT_SET));

        if (tempSymmetricKeyWrapperReference.isPresent()) {
            clearTempValue();
        }

        doRenewValue(actualValue);
    }

    private void doRenewValue(HsmKey actualValue) {
        SecurityAccessorType keyAccessorType = getKeyAccessorType();
        HsmKey symmetricKeyWrapper = (HsmKey) securityManagementService.newSymmetricKeyWrapper(keyAccessorType);
        symmetricKeyWrapper.generateValue(keyAccessorType, actualValue);
        tempSymmetricKeyWrapperReference = dataModel.asRefAny(symmetricKeyWrapper);
        this.save();
    }
}
