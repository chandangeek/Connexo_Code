package com.elster.jupiter.fileimport.impl;

import com.elster.jupiter.fileimport.FileImportService;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(name = "com.elster.jupiter.fileimport.FileImportMessageHandler",
        property = {"subscriber=" + FileImportServiceImpl.SUBSCRIBER_NAME, "destination="+ FileImportServiceImpl.DESTINATION_NAME},
        service = MessageHandlerFactory.class, immediate=true)
public class FileImportMessageHandler implements MessageHandlerFactory {

	private volatile FileImportService fileImportService;

    @Override
    public MessageHandler newMessageHandler() {
        return fileImportService.createMessageHandler();
    }

    @Reference
    public void setFileImportService(FileImportService fileImportService) {
        this.fileImportService = fileImportService;
    }

}
