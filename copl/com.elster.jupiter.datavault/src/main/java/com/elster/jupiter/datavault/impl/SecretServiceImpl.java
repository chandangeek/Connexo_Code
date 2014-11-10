package com.elster.jupiter.datavault.impl;

import com.elster.jupiter.datavault.DataVault;
import com.elster.jupiter.datavault.SecretService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.callback.InstallService;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import java.util.Arrays;
import java.util.List;
import javax.validation.MessageInterpolator;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Created by bvn on 11/6/14.
 */
@Component(name = "com.elster.kore.datavault", service = {SecretService.class, InstallService.class}, property = "name=" + SecretService.COMPONENT_NAME, immediate = true)
public class SecretServiceImpl implements SecretService, InstallService {

    private volatile DataModel dataModel;
    private volatile Thesaurus thesaurus;
    private volatile NlsService nlsService;

    public SecretServiceImpl() {
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        DataModel dataModel = ormService.newDataModel(COMPONENT_NAME, "Data vault");
        for (TableSpecs tableSpecs : TableSpecs.values()) {
            tableSpecs.addTo(dataModel);
        }
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
                bind(MessageInterpolator.class).toInstance(thesaurus);
                bind(NlsService.class).toInstance(nlsService);
                bind(ExceptionFactory.class);
                bind(DataVault.class).toProvider(DataVaultProvider.class);
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
    public String encrypt(byte[] plainText) {
        DataVault dataVault = dataModel.getInstance(DataVault.class);
        return dataVault.encrypt(plainText);
    }

    @Override
    public byte[] decrypt(String encrypted) {
        DataVault dataVault = dataModel.getInstance(DataVault.class);
        return dataVault.decrypt(encrypted);
    }
}
