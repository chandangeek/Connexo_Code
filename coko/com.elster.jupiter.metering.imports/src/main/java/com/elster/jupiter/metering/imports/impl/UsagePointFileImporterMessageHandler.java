package com.elster.jupiter.metering.imports.impl;

import com.elster.jupiter.fileimport.FileImportService;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.exception.ExceptionCatcher;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Arrays;
import java.util.List;

@Component(name = "com.elster.jupiter.metering.imports.impl.UsagePointFileImporterMessageHandler",
        property = {"subscriber=" + UsagePointFileImporterMessageHandler.SUBSCRIBER_NAME,
                "destination=" + UsagePointFileImporterMessageHandler.DESTINATION_NAME,
                "name=" + UsagePointFileImporterMessageHandler.NAME},
        service = {MessageHandlerFactory.class, InstallService.class}, immediate = true)
public class UsagePointFileImporterMessageHandler implements MessageHandlerFactory, InstallService {
    public static final String NAME = "MTI";
    public static final String DESTINATION_NAME = "UsagePointFileImport";
    public static final String SUBSCRIBER_NAME = "UsagePointFileImport";
    private volatile FileImportService fileImportService;
    private volatile MessageService messageService;
    private volatile TransactionService transactionService;

    @Override
    public MessageHandler newMessageHandler() {
        return fileImportService.createMessageHandler();
    }

    @Reference
    public void setFileImportService(FileImportService fileImportService) {
        this.fileImportService = fileImportService;
    }

    @Reference
    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Override
    public void install() {
        if (!messageService.getDestinationSpec(DESTINATION_NAME).isPresent()) {
            transactionService.builder()
                    .principal(() -> "Jupiter Installer")
                    .run(() -> ExceptionCatcher.executing(
                            () -> {
                                QueueTableSpec queueTableSpec = messageService.getQueueTableSpec("MSG_RAWQUEUETABLE").get();
                                DestinationSpec destinationSpec = queueTableSpec.createDestinationSpec(DESTINATION_NAME, 60);
                                destinationSpec.save();
                                destinationSpec.activate();
                                destinationSpec.subscribe(SUBSCRIBER_NAME);
                            }
                    ).andHandleExceptionsWith(Throwable::printStackTrace).execute());
        }
    }

    @Override
    public List<String> getPrerequisiteModules() {
        return Arrays.asList(MessageService.COMPONENTNAME);
    }
}