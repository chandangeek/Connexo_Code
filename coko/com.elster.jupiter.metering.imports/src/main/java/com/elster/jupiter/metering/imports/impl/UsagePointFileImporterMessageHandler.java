package com.elster.jupiter.metering.imports.impl;

import com.elster.jupiter.fileimport.FileImportService;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.upgrade.InstallIdentifier;
import com.elster.jupiter.upgrade.UpgradeService;

import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import static com.elster.jupiter.orm.Version.version;

@Component(name = "com.elster.jupiter.metering.imports.impl.UsagePointFileImporterMessageHandler",
        property = {"subscriber=" + UsagePointFileImporterMessageHandler.SUBSCRIBER_NAME,
                "destination=" + UsagePointFileImporterMessageHandler.DESTINATION_NAME,
                "name=" + UsagePointFileImporterMessageHandler.APP_NAME},
        service = {MessageHandlerFactory.class}, immediate = true)
public class UsagePointFileImporterMessageHandler implements MessageHandlerFactory {
    public static final String APP_NAME = "MTI";
    public static final String DESTINATION_NAME = "UsagePointFileImport";
    public static final String SUBSCRIBER_NAME = "UsagePointFileImport";
    private volatile FileImportService fileImportService;
    private volatile MessageService messageService;
    private volatile TransactionService transactionService;
    private volatile UpgradeService upgradeService;

    @Override
    public MessageHandler newMessageHandler() {
        return fileImportService.createMessageHandler();
    }

    @Activate
    public void activate() {
        DataModel dataModel = upgradeService.newNonOrmDataModel();
        dataModel.register(new AbstractModule() {
            @Override
            protected void configure() {
                bind(MessageService.class).toInstance(messageService);

            }
        });
        upgradeService.register(
                InstallIdentifier.identifier("Pulse", UsagePointFileImporterMessageHandler.APP_NAME),
                dataModel,
                Installer.class,
                ImmutableMap.of(
                        version(10, 2), UpgraderV10_2.class
                ));
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

    @Reference
    public void setUpgradeService(UpgradeService upgradeService) {
        this.upgradeService = upgradeService;
    }
}