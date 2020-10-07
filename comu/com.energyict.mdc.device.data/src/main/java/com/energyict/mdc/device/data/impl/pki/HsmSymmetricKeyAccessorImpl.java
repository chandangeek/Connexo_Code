/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.pki;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.pki.HsmKey;
import com.elster.jupiter.pki.SecurityAccessorType;
import com.elster.jupiter.pki.SecurityManagementService;
import com.energyict.mdc.device.data.SecurityAccessor;
import com.energyict.mdc.device.data.impl.MessageSeeds;
import com.google.inject.Inject;

import java.util.Optional;

import java.util.Optional;

public class HsmSymmetricKeyAccessorImpl extends SymmetricKeyAccessorImpl {

    public HsmSymmetricKeyAccessorImpl() {
        super();
    }

    @Inject
    public HsmSymmetricKeyAccessorImpl(DataModel dataModel, SecurityManagementService securityManagementService, Thesaurus thesaurus) {
        super(dataModel, securityManagementService, thesaurus);
    }

    @Override
    public void renew() {
        Optional<SecurityAccessorType> wrappingSecurityAccessorType =  getDevice().getDeviceType().getWrappingSecurityAccessorType(this.getKeyAccessorType());

        Optional<HsmKey> masterKey;

        if (wrappingSecurityAccessorType.isPresent()) {
            // AES keys wrapped by a master key
            SecurityAccessorType masterKeyAccessorType = wrappingSecurityAccessorType.get();

            Optional<SecurityAccessor> securityAccessor = getDevice().getSecurityAccessor(masterKeyAccessorType);
            if (!securityAccessor.isPresent()) {
                throw new PkiLocalizedException(thesaurus, MessageSeeds.NO_WRAPPER_ACTUAL_VALUE);
            }
            SecurityAccessor wrapperSecAccessor = securityAccessor.get();

            Optional actualValueWrapperAccessor = wrapperSecAccessor.getActualValue();
            if (!actualValueWrapperAccessor.isPresent()) {
                throw new PkiLocalizedException(thesaurus, MessageSeeds.NO_WRAPPER_ACTUAL_VALUE);
            }

            if (actualValueWrapperAccessor.get() instanceof HsmKey) {
                masterKey = Optional.of((HsmKey)actualValueWrapperAccessor.get());
            } else {
                throw new PkiLocalizedException(thesaurus, MessageSeeds.WRAPPER_NOT_HSMKEY);
            }
        } else {
            // plain text passwords or HLS Secrets without wrap key
            masterKey = Optional.empty();
        }


        if (tempSymmetricKeyWrapperReference.isPresent()) {
            clearTempValue();
        }
        doRenewValue(masterKey);
    }

    private void doRenewValue(Optional<HsmKey> masterKey) {
        SecurityAccessorType keyAccessorType = getKeyAccessorTypeReference();
        HsmKey symmetricKeyWrapper = (HsmKey) securityManagementService.newSymmetricKeyWrapper(keyAccessorType);
        symmetricKeyWrapper.generateValue(keyAccessorType, masterKey);
        tempSymmetricKeyWrapperReference = dataModel.asRefAny(symmetricKeyWrapper);
        this.save();
    }
}
