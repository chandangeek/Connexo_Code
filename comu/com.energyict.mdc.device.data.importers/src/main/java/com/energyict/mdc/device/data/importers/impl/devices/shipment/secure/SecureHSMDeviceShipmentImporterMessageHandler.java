/*
 *
 *  * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 *
 */

package com.energyict.mdc.device.data.importers.impl.devices.shipment.secure;

import com.elster.jupiter.fileimport.FileImportService;
import com.elster.jupiter.hsm.HsmEnergyService;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.SimpleTranslationKey;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.upgrade.InstallIdentifier;
import com.elster.jupiter.upgrade.UpgradeService;

import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;

import static com.elster.jupiter.orm.Version.version;

@Component(name = "com.energyict.mdc.device.data.importers.impl.devices.shipment.secure.SecureHSMDeviceShipmentImporterMessageHandler",
        service = {MessageHandlerFactory.class, TranslationKeyProvider.class},
        property = {
                "name=" + SecureHSMDeviceShipmentImporterMessageHandler.COMPONENT_NAME,
                "subscriber=" + SecureHSMDeviceShipmentImporterMessageHandler.SUBSCRIBER_NAME,
                "destination=" + SecureHSMDeviceShipmentImporterMessageHandler.DESTINATION_NAME},
        immediate = true)
public class SecureHSMDeviceShipmentImporterMessageHandler implements MessageHandlerFactory, TranslationKeyProvider {
    static final String DESTINATION_NAME = "SecHSMShipmntImport";
    static final String SUBSCRIBER_NAME = "SecHSMShipmntImport";
    static final String COMPONENT_NAME = "SHI";
    static final SimpleTranslationKey SECURE_HSM_SHIPMENT_IMPORT_SUBSCRIBER =
            new SimpleTranslationKey(SUBSCRIBER_NAME, "Handle hsm secure shipment import");

    private volatile FileImportService fileImportService;
    private volatile UpgradeService upgradeService;
    private volatile MessageService messageService;
    private volatile OrmService ormService;
    private volatile HsmEnergyService hsmEnergyService;

    public SecureHSMDeviceShipmentImporterMessageHandler() {
        // for OSGi
    }

    @Inject // for test purposes
    public SecureHSMDeviceShipmentImporterMessageHandler(FileImportService fileImportService,
                                                         UpgradeService upgradeService,
                                                         MessageService messageService,
                                                         OrmService ormService,
                                                         HsmEnergyService hsmEnergyService) {
        setFileImportService(fileImportService);
        setUpgradeService(upgradeService);
        setMessageService(messageService);
        setOrmService(ormService);
        setHsmEnergyService(hsmEnergyService);
    }

    @Activate
    public void activate() {
        DataModel dataModel = upgradeService.newNonOrmDataModel();
        dataModel.register(new AbstractModule() {
            @Override
            protected void configure() {
                bind(MessageService.class).toInstance(messageService);
                bind(OrmService.class).toInstance(ormService);
                bind(HsmEnergyService.class).toInstance(hsmEnergyService);
            }
        });

        upgradeService.register(
                InstallIdentifier.identifier("MultiSense", SecureHSMDeviceShipmentImporterMessageHandler.COMPONENT_NAME),
                dataModel,
                HsmInstaller.class,
                ImmutableMap.of(
                        version(10, 4), UpgraderV10_4_HSM.class
                ));
    }

    @Reference
    public final void setFileImportService(FileImportService fileImportService) {
        this.fileImportService = fileImportService;
    }

    @Reference
    public void setUpgradeService(UpgradeService upgradeService) {
        this.upgradeService = upgradeService;
    }

    @Reference
    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        this.ormService = ormService;
    }

    @Reference
    public void setHsmEnergyService(HsmEnergyService hsmEnergyService) {
        this.hsmEnergyService = hsmEnergyService;
    }

    @Override
    public MessageHandler newMessageHandler() {
        return fileImportService.createMessageHandler();
    }

    @Override
    public String getComponentName() {
        return COMPONENT_NAME;
    }

    @Override
    public Layer getLayer() {
        return Layer.DOMAIN;
    }

    @Override
    public List<TranslationKey> getKeys() {
        return Collections.singletonList(SECURE_HSM_SHIPMENT_IMPORT_SUBSCRIBER);
    }

    @Override
    public boolean allowsMessageValidation() {
        return fileImportService.isLocalImportAllowedOnly();
    }
}
