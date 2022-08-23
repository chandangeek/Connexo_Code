/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.elster.jupiter.pki.impl.wrappers;

import com.elster.jupiter.datavault.DataVaultService;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.hsm.HsmEncryptionService;
import com.elster.jupiter.hsm.HsmEnergyService;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.pki.impl.Installer;
import com.elster.jupiter.pki.impl.SecurityManagementServiceImpl;
import com.elster.jupiter.pki.impl.UpgraderV10_4_3;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.upgrade.InstallIdentifier;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.upgrade.V10_4_2SimpleUpgrader;
import com.elster.jupiter.upgrade.V10_4_6SimpleUpgrader;
import com.elster.jupiter.upgrade.V10_8SimpleUpgrader;
import com.elster.jupiter.users.UserService;

import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import javax.validation.MessageInterpolator;
import java.util.stream.Stream;

import static com.elster.jupiter.orm.Version.version;

/**
 * OSGi Component that will create the datamodel for the factories in the SSM bundle.
 * This approach was taken because the factories can have their respective tables in the same component
 */
@Component(name="SoftwareSecurityDataModel",
        service = SoftwareSecurityDataModel.class,
        property = {"name="+SoftwareSecurityDataModel.COMPONENTNAME},
        immediate = true)
public class SoftwareSecurityDataModel {
    public static final String COMPONENTNAME = "SSM";

    private volatile DataVaultService dataVaultService;
    private volatile PropertySpecService propertySpecService;
    private volatile DataModel dataModel;
    private volatile Thesaurus thesaurus;
    private volatile UpgradeService upgradeService;
    private volatile EventService eventService;
    private volatile UserService userService;
    private volatile MessageService messageService;
    private SecurityManagementService securityManagementService;
    private volatile HsmEnergyService hsmEnergyService;
    private volatile HsmEncryptionService hsmEncryptionService;

    // OSGi
    public SoftwareSecurityDataModel() {
    }

    @Inject // Testing purposes
    public SoftwareSecurityDataModel(OrmService ormService, UpgradeService upgradeService, NlsService nlsService,
                                     DataVaultService dataVaultService, PropertySpecService propertySpecService,
                                     SecurityManagementService securityManagementService, EventService eventService,
                                     UserService userService, MessageService messageService, HsmEnergyService hsmEnergyService, HsmEncryptionService hsmEncryptionService) {
        this.setOrmService(ormService);
        this.setUpGradeService(upgradeService);
        this.setNlsService(nlsService);
        this.setPropertySpecService(propertySpecService);
        this.setDataVaultService(dataVaultService);
        this.setSecurityManagementService(securityManagementService);
        this.setEventService(eventService);
        this.setUserService(userService);
        this.setMessageService(messageService);
        this.setHsmEnergyService(hsmEnergyService);
        this.setHsmEncryptionService(hsmEncryptionService);
        activate();
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        this.dataModel = ormService.newDataModel(COMPONENTNAME, "Plaintext keys");
    }

    @Reference
    public void setSecurityManagementService(SecurityManagementService securityManagementService) {
        this.securityManagementService = securityManagementService;
    }

    @Reference
    public void setUpGradeService(UpgradeService upGradeService) {
        this.upgradeService = upGradeService;
    }

    @Reference
    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(SecurityManagementServiceImpl.COMPONENTNAME, Layer.DOMAIN);
    }

    @Reference
    public void setDataVaultService(DataVaultService dataVaultService) {
        this.dataVaultService = dataVaultService;
    }

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Reference
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Reference
    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }

    @Reference
    public void setHsmEnergyService(HsmEnergyService hsmEnergyService) {
        this.hsmEnergyService = hsmEnergyService;
    }

    @Reference
    public void setHsmEncryptionService(HsmEncryptionService hsmEncryptionService) {
        this.hsmEncryptionService = hsmEncryptionService;
    }

    @Activate
    public void activate() {
        registerDataModel();
        registerInjector();
        upgradeService.register(
                InstallIdentifier.identifier("Pulse", COMPONENTNAME),
                dataModel,
                Installer.class,
                ImmutableMap.of(
                        version(10, 4, 2), V10_4_2SimpleUpgrader.class,
                        version(10, 4, 3), UpgraderV10_4_3.class,
                        version(10,4,4), V10_4_6SimpleUpgrader.class,
                        version(10, 8), V10_8SimpleUpgrader.class));
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
                bind(SecurityManagementService.class).toInstance(securityManagementService);
                bind(EventService.class).toInstance(eventService);
                bind(UserService.class).toInstance(userService);
                bind(MessageService.class).toInstance(messageService);
                bind(HsmEnergyService.class).toInstance(hsmEnergyService);
                bind(HsmEncryptionService.class).toInstance(hsmEncryptionService);
            }
        };
    }

    public DataModel getDataModel() {
        return dataModel;
    }




}
