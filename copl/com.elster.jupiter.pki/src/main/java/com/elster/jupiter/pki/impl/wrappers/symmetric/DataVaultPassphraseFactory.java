/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.elster.jupiter.pki.impl.wrappers.symmetric;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.pki.ExpirationSupport;
import com.elster.jupiter.pki.SecurityAccessorType;
import com.elster.jupiter.pki.PassphraseFactory;
import com.elster.jupiter.pki.PassphraseWrapper;
import com.elster.jupiter.pki.PlaintextPassphrase;
import com.elster.jupiter.pki.SecurityValueWrapper;
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

@Component(name="PlaintextPassphraseFactory", service = PassphraseFactory.class, immediate = true)
public class DataVaultPassphraseFactory implements PassphraseFactory, ExpirationSupport {

    public static final String KEY_ENCRYPTION_METHOD = "DataVault";

    private volatile DataModel dataModel;

    // OSGi
    @SuppressWarnings("unused")
    public DataVaultPassphraseFactory() {
    }

    @Inject // Testing only
    public DataVaultPassphraseFactory(SoftwareSecurityDataModel ssmModel) {
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
    public PassphraseWrapper newPassphraseWrapper(SecurityAccessorType securityAccessorType) {
        PlaintextPassphraseImpl plaintextPassphrase = dataModel.getInstance(PlaintextPassphraseImpl.class)
                .init(securityAccessorType.getKeyType(), securityAccessorType.getDuration().get());
        plaintextPassphrase.save();
        return plaintextPassphrase;
    }

    @Override
    public List<SecurityValueWrapper> findExpired(Expiration expiration, Instant when) {
        List<SecurityValueWrapper> wrappers = new ArrayList<>();
        wrappers.addAll(dataModel.query(PlaintextPassphrase.class).select(expiration.isExpired("expirationTime", when)));
        return wrappers;
    }

    public Comparison isExpiredCondition(Expiration expiration, Instant when) {
        // for this we use  the columnname and not the field name as it will be used in a sqlbuilder and not by orm
        return (Comparison) expiration.isExpired(TableSpecs.SSM_PLAINTEXTPW.name()+".EXPIRATION", when);
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return dataModel.getInstance(PlaintextPassphraseImpl.class).getPropertySpecs();
    }

}
