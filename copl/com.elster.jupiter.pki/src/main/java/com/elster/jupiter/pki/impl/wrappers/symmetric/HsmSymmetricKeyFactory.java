/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.pki.impl.wrappers.symmetric;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.pki.ExpirationSupport;
import com.elster.jupiter.pki.SecurityAccessorType;
import com.elster.jupiter.pki.SecurityValueWrapper;
import com.elster.jupiter.pki.SymmetricKeyFactory;
import com.elster.jupiter.pki.SymmetricKeyWrapper;
import com.elster.jupiter.pki.impl.wrappers.SoftwareSecurityDataModel;
import com.elster.jupiter.pki.impl.wrappers.TableSpecs;
import com.elster.jupiter.properties.Expiration;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.util.conditions.Comparison;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Component(name="HsmSymmetricKeyFactory", service = SymmetricKeyFactory.class, immediate = true)
public class HsmSymmetricKeyFactory implements SymmetricKeyFactory, ExpirationSupport {

    public static final String KEY_ENCRYPTION_METHOD = "HSM";

    private volatile DataModel dataModel;

    // OSGi
    @SuppressWarnings("unused")
    public HsmSymmetricKeyFactory() {
    }

    @Reference
    public void setSsmModel(SoftwareSecurityDataModel ssmModel) {
        this.dataModel = ssmModel.getDataModel();
    }

    @Override
    public String getKeyEncryptionMethod() {
        return KEY_ENCRYPTION_METHOD;
    }

    @Override
    public SymmetricKeyWrapper newSymmetricKey(SecurityAccessorType securityAccessorType) {
        return dataModel.getInstance(HsmSymmetricKeyImpl.class)
                .init(securityAccessorType.getKeyType(), securityAccessorType.getDuration().get(), securityAccessorType.getLabel());
    }

    @Override
    public List<SecurityValueWrapper> findExpired(Expiration expiration, Instant when) {
        List<SecurityValueWrapper> wrappers = new ArrayList<>();
        wrappers.addAll(dataModel.query(HsmSymmetricKeyImpl.class).select(expiration.isExpired("expirationTime", when)));
        return wrappers;
    }

    @Override
    public Comparison isExpiredCondition(Expiration expiration, Instant when) {
        // for this we use  the columnname and not the field name as it will be used in a sqlbuilder and not by orm
        return (Comparison) expiration.isExpired(TableSpecs.SSM_HSMSK.name()+".EXPIRATION", when);
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return dataModel.getInstance(HsmSymmetricKeyImpl.class).getPropertySpecs();
    }
}
