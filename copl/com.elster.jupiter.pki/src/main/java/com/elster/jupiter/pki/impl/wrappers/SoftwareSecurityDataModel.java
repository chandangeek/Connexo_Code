/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.elster.jupiter.pki.impl.wrappers;

import com.elster.jupiter.datavault.DataVaultService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.pki.PkiService;
import com.elster.jupiter.pki.impl.Installer;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.upgrade.InstallIdentifier;
import com.elster.jupiter.upgrade.UpgradeService;

import com.google.inject.AbstractModule;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import javax.validation.MessageInterpolator;
import java.util.Collections;
import java.util.stream.Stream;

/**
 * OSGi Component that will create the datamodel for the factories in the SSM bundle.
 * This approach was taken because the factories can have their respective tables in the same component
 */
@Component(name="SoftwareSecurityDataModel", property = {"name="+SoftwareSecurityDataModel.COMPONENTNAME}, immediate = true)

public class SoftwareSecurityDataModel {
    static final String COMPONENTNAME = "SSM";

    private volatile DataVaultService dataVaultService;
    private volatile PropertySpecService propertySpecService;
    private volatile DataModel dataModel;
    private volatile Thesaurus thesaurus;
    private volatile UpgradeService upgradeService;
    private PkiService pkiService;

    @Inject
    public SoftwareSecurityDataModel(OrmService ormService, UpgradeService upgradeService, NlsService nlsService,
                                     DataVaultService dataVaultService, PropertySpecService propertySpecService,
                                     PkiService pkiService) {
        this.setOrmService(ormService);
        this.setUpGradeService(upgradeService);
        this.setNlsService(nlsService);
        this.setPropertySpecService(propertySpecService);
        this.setDataVaultService(dataVaultService);
        this.setPkiService(pkiService);
        activate();
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        this.dataModel = ormService.newDataModel(COMPONENTNAME, "Plaintext keys");
    }

    @Reference
    public void setPkiService(PkiService pkiService) {
        this.pkiService = pkiService;
    }

    @Reference
    public void setUpGradeService(UpgradeService upGradeService) {
        this.upgradeService = upGradeService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(COMPONENTNAME, Layer.DOMAIN);
    }

    @Reference
    public void setDataVaultService(DataVaultService dataVaultService) {
        this.dataVaultService = dataVaultService;
    }

    @Reference
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Activate
    public void activate() {
        registerDataModel();
        registerInjector();
        upgradeService.register(InstallIdentifier.identifier("Pulse", COMPONENTNAME), dataModel, Installer.class, Collections.emptyMap());
    }

    private void registerDataModel() {
        Stream.of(TableSpecs.values()).forEach(tableSpec -> tableSpec.addTo(dataModel));
    }

    private void registerInjector() {
        this.dataModel.register(this.getModule());
    }

    private AbstractModule getModule() {
        return new AbstractModule() {
            @Override
            public void configure() {
                bind(DataModel.class).toInstance(dataModel);
                bind(MessageInterpolator.class).toInstance(thesaurus);
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(DataVaultService.class).toInstance(dataVaultService);
                bind(PropertySpecService.class).toInstance(propertySpecService);
                bind(PkiService.class).toInstance(pkiService);
            }
        };
    }

    public DataModel getDataModel() {
        return dataModel;
    }
}
