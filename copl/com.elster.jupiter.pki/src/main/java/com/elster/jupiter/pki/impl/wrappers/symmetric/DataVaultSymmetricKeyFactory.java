/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.elster.jupiter.pki.impl.wrappers.symmetric;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.pki.ExpirationSupport;
import com.elster.jupiter.pki.KeyAccessorType;
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

import javax.inject.Inject;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Component(name="PlaintextSymmetricKeyFactory", service = SymmetricKeyFactory.class, immediate = true)
public class DataVaultSymmetricKeyFactory implements SymmetricKeyFactory, ExpirationSupport {

    public static final String KEY_ENCRYPTION_METHOD = "DataVault";

    private volatile DataModel dataModel;

    // OSGi
    @SuppressWarnings("unused")
    public DataVaultSymmetricKeyFactory() {
    }

    @Inject // Testing only
    public DataVaultSymmetricKeyFactory(SoftwareSecurityDataModel ssmModel) {
        this.setSsmModel(ssmModel);
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
    public SymmetricKeyWrapper newSymmetricKey(KeyAccessorType keyAccessorType) {
        PlaintextSymmetricKeyImpl symmetricKeyWrapper = dataModel.getInstance(PlaintextSymmetricKeyImpl.class)
                .init(keyAccessorType.getKeyType(), keyAccessorType.getDuration().get());
        symmetricKeyWrapper.save();
        return symmetricKeyWrapper;
    }

    @Override
    public List<SecurityValueWrapper> findExpired(Expiration expiration, Instant when) {
        List<SecurityValueWrapper> wrappers = new ArrayList<>();
        wrappers.addAll(dataModel.query(SymmetricKeyWrapper.class).select(expiration.isExpired("expirationTime", when)));
        return wrappers;
    }

    @Override
    public Comparison isExpiredCondition(Expiration expiration, Instant when) {
        // for this we use  the columnname and not the field name as it will be used in a sqlbuilder and not by orm
        return (Comparison) expiration.isExpired(TableSpecs.SSM_PLAINTEXTSK.name()+".EXPIRATION", when);
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return dataModel.getInstance(PlaintextSymmetricKeyImpl.class).getPropertySpecs();
    }

}
