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
        //TODO: retreive somehow the masterKey so we can pass it instead of actual value...
        //This is just to make it work on the very short term for beacon device. change it asap by a proper way of linking sec accessor AK,  EK, etc to their master key used for wrapping at keyRenewal
        SecurityAccessorType masterKeyAccessorType = getDevice().getDeviceProtocolProperties().getTypedProperty("DlmsWanKEK");
        if (masterKeyAccessorType == null) {
            throw new PkiLocalizedException(thesaurus, MessageSeeds.ACTUAL_VALUE_NOT_SET);
        }

        HsmKey masterKey = (HsmKey) getDevice().getSecurityAccessor(masterKeyAccessorType).get().getActualValue().get();

        if (tempSymmetricKeyWrapperReference.isPresent()) {
            clearTempValue();
        }

        doRenewValue(masterKey);
    }

    private void doRenewValue(HsmKey masterKey) {
        SecurityAccessorType keyAccessorType = getKeyAccessorType();
        HsmKey symmetricKeyWrapper = (HsmKey) securityManagementService.newSymmetricKeyWrapper(keyAccessorType);
        symmetricKeyWrapper.generateValue(keyAccessorType, masterKey);
        tempSymmetricKeyWrapperReference = dataModel.asRefAny(symmetricKeyWrapper);
        this.save();
    }
}
