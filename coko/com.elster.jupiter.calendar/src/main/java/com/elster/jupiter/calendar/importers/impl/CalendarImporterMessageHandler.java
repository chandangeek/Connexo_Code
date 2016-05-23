package com.elster.jupiter.calendar.importers.impl;

import com.elster.jupiter.calendar.MessageSeeds;
import com.elster.jupiter.fileimport.FileImportService;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.util.exception.ExceptionCatcher;
import com.elster.jupiter.util.exception.MessageSeed;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;


@Component(name = "com.elster.jupiter.calendar.importers.CalendarImporterMessageHandler",
        service = {MessageHandlerFactory.class, TranslationKeyProvider.class, MessageSeedProvider.class, InstallService.class},
        property = {"name=" + CalendarImporterMessageHandler.COMPONENT,
                "subscriber=" + CalendarImporterMessageHandler.SUBSCRIBER_NAME,
                "destination=" + CalendarImporterMessageHandler.DESTINATION_NAME},
        immediate = true)
public class CalendarImporterMessageHandler implements MessageHandlerFactory, TranslationKeyProvider, MessageSeedProvider, InstallService {

    public static final String COMPONENT = "CLI";

    public static final String DESTINATION_NAME = "CalendarImport";
    public static final String SUBSCRIBER_NAME = "CalendarImport";

    private volatile MessageService messageService;
    private volatile FileImportService fileImportService;

    public CalendarImporterMessageHandler() {}

    @Activate
    public void activate() {
    }

    @Inject
    public CalendarImporterMessageHandler(MessageService messageService, FileImportService fileImportService) {
        this();
        setMessageService(messageService);
        setFileImportService(fileImportService);
    }

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
        return Arrays.asList(TranslationKeys.values());
    }

    @Override
    public List<MessageSeed> getSeeds() {
        return Arrays.asList(MessageSeeds.values());
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
        return Arrays.asList(OrmService.COMPONENTNAME, MessageService.COMPONENTNAME, FileImportService.COMPONENT_NAME);
    }

    private void createDestinationAndSubscriber() {
        QueueTableSpec queueTableSpec = messageService.getQueueTableSpec("MSG_RAWQUEUETABLE").get();
        DestinationSpec destinationSpec = queueTableSpec.createDestinationSpec(DESTINATION_NAME, 60);
        destinationSpec.save();
        destinationSpec.activate();
        destinationSpec.subscribe(SUBSCRIBER_NAME);
    }

    @Reference
    public final void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }

    @Reference
    public final void setFileImportService(FileImportService fileImportService) {
        this.fileImportService = fileImportService;
    }

    @Override
    public MessageHandler newMessageHandler() {
        return fileImportService.createMessageHandler();
    }
}


