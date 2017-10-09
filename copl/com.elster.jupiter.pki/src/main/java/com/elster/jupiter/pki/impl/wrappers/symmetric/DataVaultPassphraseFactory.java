/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.elster.jupiter.pki.impl.wrappers.symmetric;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.pki.KeyAccessorType;
import com.elster.jupiter.pki.PassphraseFactory;
import com.elster.jupiter.pki.PassphraseWrapper;
import com.elster.jupiter.pki.DeviceSecretImporter;
import com.elster.jupiter.pki.PkiService;
import com.elster.jupiter.pki.impl.wrappers.SoftwareSecurityDataModel;
import com.elster.jupiter.properties.PropertySpec;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

@Component(name="PlaintextPassphraseFactory", service = PassphraseFactory.class, immediate = true)
public class DataVaultPassphraseFactory implements PassphraseFactory {

    public static final String KEY_ENCRYPTION_METHOD = "DataVault";

    private Optional<String> certificateAlias = Optional.empty();

    private volatile DataModel dataModel;
    private volatile Thesaurus thesaurus;
    private volatile PkiService pkiService;

    // OSGi
    public DataVaultPassphraseFactory() {
    }

    @Inject // Testing only
    public DataVaultPassphraseFactory(SoftwareSecurityDataModel ssmModel) {
        this.setSsmModel(ssmModel);
    }

    @Activate
    public void activate(BundleContext bundleContext) {
        certificateAlias = Optional.ofNullable(bundleContext.getProperty(AbstractDataVaultImporter.IMPORT_KEY));
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
    public PassphraseWrapper newPassphraseWrapper(KeyAccessorType keyAccessorType) {
        PlaintextPassphraseImpl plaintextPassphrase = dataModel.getInstance(PlaintextPassphraseImpl.class)
                .init(keyAccessorType.getKeyType(), keyAccessorType.getDuration().get());
        plaintextPassphrase.save();
        return plaintextPassphrase;
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return dataModel.getInstance(PlaintextPassphraseImpl.class).getPropertySpecs();
    }

    @Override
    public DeviceSecretImporter getDevicePassphraseImporter(KeyAccessorType keyAccessorType) {
        return new DataVaultPassphraseImporter(keyAccessorType, thesaurus, pkiService, certificateAlias);
    }
}
