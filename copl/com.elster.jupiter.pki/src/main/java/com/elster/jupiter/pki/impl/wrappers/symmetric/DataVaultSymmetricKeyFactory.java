/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.elster.jupiter.pki.impl.wrappers.symmetric;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.pki.ExpirationSupport;
import com.elster.jupiter.pki.SecurityAccessorType;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.pki.SecurityValueWrapper;
import com.elster.jupiter.pki.SymmetricKeyFactory;
import com.elster.jupiter.pki.SymmetricKeyWrapper;
import com.elster.jupiter.pki.DeviceSecretImporter;
import com.elster.jupiter.pki.impl.wrappers.SoftwareSecurityDataModel;
import com.elster.jupiter.pki.impl.wrappers.TableSpecs;
import com.elster.jupiter.properties.Expiration;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.util.conditions.Comparison;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component(name="PlaintextSymmetricKeyFactory", service = SymmetricKeyFactory.class, immediate = true)
public class DataVaultSymmetricKeyFactory implements SymmetricKeyFactory, ExpirationSupport {

    public static final String KEY_ENCRYPTION_METHOD = "DataVault";

    private volatile DataModel dataModel;
    private volatile SecurityManagementService securityManagementService;
    private volatile Thesaurus thesaurus;

    private Optional<String> certificateAlias = Optional.empty();

    // OSGi
    @SuppressWarnings("unused")
    public DataVaultSymmetricKeyFactory() {
    }

    @Activate
    public void activate(BundleContext bundleContext) {
        certificateAlias = Optional.ofNullable(bundleContext.getProperty(AbstractDataVaultImporter.IMPORT_KEY));
    }

    @Inject // Testing only
    public DataVaultSymmetricKeyFactory(SoftwareSecurityDataModel ssmModel) {
        this.setSsmModel(ssmModel);
    }

    @Reference
    public void setSsmModel(SoftwareSecurityDataModel ssmModel) {
        this.dataModel = ssmModel.getDataModel();
    }

    @Reference
    public void setSecurityManagementService(SecurityManagementService securityManagementService) {
        this.securityManagementService = securityManagementService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(SoftwareSecurityDataModel.COMPONENTNAME, Layer.DOMAIN);
    }

    @Override
    public String getKeyEncryptionMethod() {
        return KEY_ENCRYPTION_METHOD;
    }

    @Override
    public SymmetricKeyWrapper newSymmetricKey(SecurityAccessorType securityAccessorType) {
        KeyImpl symmetricKeyWrapper;
        if (securityAccessorType.keyTypeIsHSM()){
            if (securityAccessorType.getHsmKeyType().isReversible()) {
                symmetricKeyWrapper = dataModel.getInstance(HsmReversibleKey.class).init(securityAccessorType.getKeyType(),
                        securityAccessorType.getDuration().get(),
                        securityAccessorType.getHsmKeyType().getLabel(),
                        securityAccessorType.getHsmKeyType().getHsmJssKeyType());

            } else {
                symmetricKeyWrapper = dataModel.getInstance(HsmKeyImpl.class).init(securityAccessorType.getKeyType(),
                        securityAccessorType.getDuration().get(),
                        securityAccessorType.getHsmKeyType().getLabel(),
                        securityAccessorType.getHsmKeyType().getHsmJssKeyType());
            }
        } else {
            symmetricKeyWrapper = dataModel.getInstance(PlaintextSymmetricKeyImpl.class)
                    .init(securityAccessorType.getKeyType(), securityAccessorType.getDuration().get());
        }
        symmetricKeyWrapper.save();
        return symmetricKeyWrapper;
    }

    @Override
    public List<SecurityValueWrapper> findExpired(Expiration expiration, Instant when) {
        List<SecurityValueWrapper> wrappers = new ArrayList<>();
        wrappers.addAll(dataModel.query(KeyImpl.class).select(expiration.isExpired("expirationTime", when)));
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

    @Override
    public DeviceSecretImporter getDeviceKeyImporter(SecurityAccessorType securityAccessorType) {
        return new DataVaultSymmetricKeyImporter(securityAccessorType, thesaurus, securityManagementService, certificateAlias);
    }

}
