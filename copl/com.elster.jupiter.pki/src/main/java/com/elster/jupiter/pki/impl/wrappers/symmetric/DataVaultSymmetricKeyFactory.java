/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.elster.jupiter.pki.impl.wrappers.symmetric;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.pki.KeyAccessorType;
import com.elster.jupiter.pki.PkiService;
import com.elster.jupiter.pki.SymmetricKeyFactory;
import com.elster.jupiter.pki.SymmetricKeyWrapper;
import com.elster.jupiter.pki.DeviceSecretImporter;
import com.elster.jupiter.pki.impl.wrappers.SoftwareSecurityDataModel;
import com.elster.jupiter.properties.PropertySpec;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

@Component(name="PlaintextSymmetricKeyFactory", service = SymmetricKeyFactory.class, immediate = true)
public class DataVaultSymmetricKeyFactory implements SymmetricKeyFactory {

    public static final String KEY_ENCRYPTION_METHOD = "DataVault";
    public static final String IMPORT_KEY = "com.elster.jupiter.shipment.importer.certificate.alias";

    private volatile DataModel dataModel;
    private volatile PkiService pkiService;
    private volatile Thesaurus thesaurus;

    private Optional<String> certificateAlias = Optional.empty();

    // OSGi
    public DataVaultSymmetricKeyFactory() {
    }

    @Activate
    public void activate(BundleContext bundleContext) {
        certificateAlias = Optional.ofNullable(bundleContext.getProperty(IMPORT_KEY));
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
    public void setPkiService(PkiService pkiService) {
        this.pkiService = pkiService;
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
    public SymmetricKeyWrapper newSymmetricKey(KeyAccessorType keyAccessorType) {
        PlaintextSymmetricKeyImpl symmetricKeyWrapper = dataModel.getInstance(PlaintextSymmetricKeyImpl.class)
                .init(keyAccessorType.getKeyType(), keyAccessorType.getDuration().get());
        symmetricKeyWrapper.save();
        return symmetricKeyWrapper;
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return dataModel.getInstance(PlaintextSymmetricKeyImpl.class).getPropertySpecs();
    }

    @Override
    public DeviceSecretImporter getDeviceKeyImporter(KeyAccessorType keyAccessorType) {
        return new DataVaultSymmetricKeyImporter(keyAccessorType, thesaurus, pkiService, certificateAlias);
    }

}
