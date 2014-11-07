package com.elster.jupiter.datavault.impl;

import com.elster.jupiter.datavault.DataVault;
import com.elster.jupiter.datavault.SecretService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.callback.InstallService;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import java.util.Arrays;
import java.util.List;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Created by bvn on 11/6/14.
 */
@Component(name = "com.elster.kore.datavault", service = {SecretService.class, InstallService.class}, property = "name=" + SecretService.COMPONENT_NAME, immediate = true)
public class DataVaultServiceImpl implements SecretService, InstallService {

    public DataVaultServiceImpl() {
    }

    private volatile DataModel dataModel;
    private volatile Thesaurus thesaurus;
    private volatile NlsService nlsService;

    @Reference
    public void setDataModel(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.nlsService = nlsService;
        this.thesaurus = nlsService.getThesaurus(COMPONENT_NAME, Layer.DOMAIN);
    }

    private Module getModule() {
        return new AbstractModule() {
            @Override
            public void configure() {
                bind(DataModel.class).toInstance(dataModel);
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(NlsService.class).toInstance(nlsService);
                bind(ExceptionFactory.class).to(ExceptionFactory.class);
                bind(DataVault.class).toProvider(DataVaultProvider.class).asEagerSingleton();
            }
        };
    }

    @Activate
    public void activate() {
        this.dataModel.register(this.getModule());
    }

    @Override
    public void install() {
        new Installer(this.dataModel, this.thesaurus).install();
    }

    @Override
    public List<String> getPrerequisiteModules() {
        return Arrays.asList("ORM", "NLS");
    }

    @Override
    public String encrypt(byte[] decrypted) {
        KeyStoreDataVault dataVault = dataModel.getInstance(KeyStoreDataVault.class);
        return null;
    }

    @Override
    public byte[] decrypt(String encrypted) {
        return new byte[0];
    }
}
