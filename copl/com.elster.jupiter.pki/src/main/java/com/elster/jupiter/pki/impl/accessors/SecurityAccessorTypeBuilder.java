/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.pki.impl.accessors;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.hsm.model.keys.SessionKeyCapability;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.pki.*;
import com.elster.jupiter.time.TimeDuration;

import java.util.EnumSet;

public class SecurityAccessorTypeBuilder implements SecurityAccessorType.Builder {
    private static final EnumSet<CryptographicType> CERTIFICATES = EnumSet.of(
            CryptographicType.Certificate,
            CryptographicType.ClientCertificate,
            CryptographicType.TrustedCertificate);

    private final DataModel dataModel;
    private final SecurityAccessorTypeImpl underConstruction;

    public SecurityAccessorTypeBuilder(DataModel dataModel, String name, KeyType keyType) {
        this.dataModel = dataModel;
        underConstruction = dataModel.getInstance(SecurityAccessorTypeImpl.class);
        underConstruction.setName(name);
        underConstruction.setKeyType(keyType);
    }

    @Override
    public SecurityAccessorType.Builder keyEncryptionMethod(String keyEncryptionMethod) {
        underConstruction.setKeyEncryptionMethod(keyEncryptionMethod);
        return this;
    }

    @Override
    public SecurityAccessorType.Builder description(String description) {
        underConstruction.setDescription(description);
        return this;
    }

    @Override
    public SecurityAccessorType.Builder trustStore(TrustStore trustStore) {
        underConstruction.setTrustStore(trustStore);
        return this;
    }

    @Override
    public SecurityAccessorType.Builder duration(TimeDuration duration) {
        underConstruction.setDuration(duration);
        return this;
    }

    @Override
    public SecurityAccessorType.Builder managedCentrally() {
        underConstruction.setManagedCentrally(true);
        return this;
    }

    @Override
    public SecurityAccessorType.Builder purpose(SecurityAccessorType.Purpose purpose) {
        underConstruction.setPurpose(purpose);
        return this;
    }

    @Override
    public SecurityAccessorType.Builder label(String label) {
        underConstruction.setLabel(label);
        return this;
    }

    @Override
    public SecurityAccessorType.Builder importCapability(SessionKeyCapability importCapability) {
        underConstruction.setImportCapability(importCapability);
        return this;
    }

    @Override
    public SecurityAccessorType.Builder renewCapability(SessionKeyCapability renewCapability) {
        underConstruction.setRenewCapability(renewCapability);
        return this;
    }

    @Override
    public SecurityAccessorType.Builder keySize(int keySize) {
        underConstruction.setKeySize(keySize);
        return this;
    }



    @Override
    public SecurityAccessorType add() {
        if (!CERTIFICATES.contains(underConstruction.getKeyType().getCryptographicType())) {
            if (underConstruction.isManagedCentrally()) {
                throw new UnsupportedOperationException("Default values are only supported for certificate accessor type.");
            }
            underConstruction.addUserAction(SecurityAccessorUserAction.EDIT_SECURITY_PROPERTIES_1);
            underConstruction.addUserAction(SecurityAccessorUserAction.EDIT_SECURITY_PROPERTIES_2);
            underConstruction.addUserAction(SecurityAccessorUserAction.VIEW_SECURITY_PROPERTIES_1);
            underConstruction.addUserAction(SecurityAccessorUserAction.VIEW_SECURITY_PROPERTIES_2);
        }
        Save.CREATE.save(dataModel, underConstruction);
        return underConstruction;
    }
}
