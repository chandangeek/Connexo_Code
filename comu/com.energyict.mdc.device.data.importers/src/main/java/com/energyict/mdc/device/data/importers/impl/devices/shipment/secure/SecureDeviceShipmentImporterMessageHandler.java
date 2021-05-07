/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.importers.impl.devices.shipment.secure;

import com.elster.jupiter.fileimport.FileImportService;
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
import com.energyict.mdc.device.data.importers.ImporterExtension;

import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.elster.jupiter.orm.Version.version;

@Component(name = "com.energyict.mdc.device.data.importers.impl.devices.shipment.secure.SecureDeviceShipmentImporterMessageHandler",
        service = {MessageHandlerFactory.class, TranslationKeyProvider.class},
        property = {
                "name=" + SecureDeviceShipmentImporterMessageHandler.COMPONENT_NAME,
                "subscriber=" + SecureDeviceShipmentImporterMessageHandler.SUBSCRIBER_NAME,
                "destination=" + SecureDeviceShipmentImporterMessageHandler.DESTINATION_NAME},
        immediate = true)
public class SecureDeviceShipmentImporterMessageHandler implements MessageHandlerFactory, TranslationKeyProvider {

    static final String DESTINATION_NAME = "SecureShipmentImport";
    static final String SUBSCRIBER_NAME = "SecureShipmentImport";
    static final String COMPONENT_NAME = "SSI";
    static final SimpleTranslationKey SECURE_SHIPMENT_IMPORT_SUBSCRIBER =
            new SimpleTranslationKey(SUBSCRIBER_NAME, "Handle secure shipment import");

    private volatile FileImportService fileImportService;
    private volatile UpgradeService upgradeService;
    private volatile MessageService messageService;
    private volatile OrmService ormService;
    private volatile Optional<ImporterExtension> importerExtension = Optional.empty();

    // OSGi constructor
    public SecureDeviceShipmentImporterMessageHandler() {

    }

    @Activate
    public void activate() {
        DataModel dataModel = upgradeService.newNonOrmDataModel();
        dataModel.register(new AbstractModule() {
            @Override
            protected void configure() {
                bind(MessageService.class).toInstance(messageService);
                bind(OrmService.class).toInstance(ormService);
            }
        });

        upgradeService.register(
                InstallIdentifier.identifier("MultiSense", SecureDeviceShipmentImporterMessageHandler.COMPONENT_NAME),
                dataModel,
                SsiInstaller.class,
                ImmutableMap.of(
                        version(10, 4), UpgraderV10_4_SSI.class
                ));

    }

    @Inject // for test purposes
    public SecureDeviceShipmentImporterMessageHandler(FileImportService fileImportService, UpgradeService upgradeService, MessageService messageService, OrmService ormService) {
        this();
        setFileImportService(fileImportService);
        setUpgradeService(upgradeService);
        setMessageService(messageService);
        setOrmService(ormService);
    }

    @Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC)
    public void addImporterExtension(ImporterExtension importerExtension) {
        this.importerExtension = Optional.of(importerExtension);
    }

    public void removeImporterExtension(ImporterExtension importerExtension) {
        this.importerExtension = Optional.empty();
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
        return Collections.singletonList(SECURE_SHIPMENT_IMPORT_SUBSCRIBER);
    }

    @Override
    public boolean allowsMessageValidation() {
        return fileImportService.isLocalImportAllowedOnly();
    }
}