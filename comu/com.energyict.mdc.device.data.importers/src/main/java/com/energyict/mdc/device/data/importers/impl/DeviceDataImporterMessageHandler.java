package com.energyict.mdc.device.data.importers.impl;

import com.elster.jupiter.fileimport.FileImportService;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.util.exception.ExceptionCatcher;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component(name = "com.energyict.mdc.device.data.importers.DeviceDataImporterMessageHandler",
        service = {MessageHandlerFactory.class, TranslationKeyProvider.class, InstallService.class},
        property = {"name=" + DeviceDataImporterMessageHandler.COMPONENT,
                    "subscriber=" + DeviceDataImporterMessageHandler.SUBSCRIBER_NAME,
                    "destination=" + DeviceDataImporterMessageHandler.DESTINATION_NAME},
        immediate = true)
public class DeviceDataImporterMessageHandler implements MessageHandlerFactory, TranslationKeyProvider, InstallService {

    public static final String COMPONENT = "DDI";

    public static final String DESTINATION_NAME = "DataImport";
    public static final String SUBSCRIBER_NAME = "DataImport";

    private volatile MessageService messageService;
    private volatile FileImportService fileImportService;

    public DeviceDataImporterMessageHandler() {}

    @Override
    public String getComponentName() {
        return COMPONENT;
    }

    @Override
    public Layer getLayer() {
        return Layer.DOMAIN;
    }

    @Override
    public List<TranslationKey> getKeys() {
        List<TranslationKey> keys = new ArrayList<>();
        keys.addAll(Arrays.asList(TranslationKeys.values()));
        keys.addAll(Arrays.asList(MessageSeeds.values()));
        return keys;
    }

    @Override
    public void install() {
        ExceptionCatcher.executing(
                this::createDestinationAndSubscriber
        ).andHandleExceptionsWith(Throwable::printStackTrace)
                .execute();
    }

    @Override
    public List<String> getPrerequisiteModules() {
        return Arrays.asList(OrmService.COMPONENTNAME, MessageService.COMPONENTNAME);
    }

    private void createDestinationAndSubscriber() {
        QueueTableSpec queueTableSpec = messageService.getQueueTableSpec("MSG_RAWQUEUETABLE").get();
        DestinationSpec destinationSpec = queueTableSpec.createDestinationSpec(DESTINATION_NAME, 60);
        destinationSpec.save();
        destinationSpec.activate();
        destinationSpec.subscribe(SUBSCRIBER_NAME);
    }

    @Reference
    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }

    @Reference
    public void setFileImportService(FileImportService fileImportService) {
        this.fileImportService = fileImportService;
    }

    @Override
    public MessageHandler newMessageHandler() {
        return fileImportService.createMessageHandler();
    }
}
