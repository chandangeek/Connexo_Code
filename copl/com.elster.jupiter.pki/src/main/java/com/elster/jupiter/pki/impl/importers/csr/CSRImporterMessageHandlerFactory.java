/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.pki.impl.importers.csr;

import com.elster.jupiter.fileimport.FileImportService;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;

@Component(name = "com.elster.jupiter.pki.impl.importers.csr.CSRImporterMessageHandlerFactory",
        service = MessageHandlerFactory.class,
        property = {
                "subscriber=" + CSRImporterMessageHandlerFactory.SUBSCRIBER_NAME,
                "destination=" + CSRImporterMessageHandlerFactory.DESTINATION_NAME
        },
        immediate = true)
public class CSRImporterMessageHandlerFactory implements MessageHandlerFactory {
    public static final String DESTINATION_NAME = "CSRImport";
    public static final String SUBSCRIBER_NAME = "CSRImport";

    private volatile FileImportService fileImportService;

    public CSRImporterMessageHandlerFactory() {
        // for OSGI
    }

    @Inject
    public CSRImporterMessageHandlerFactory(FileImportService fileImportService) {
        this();
        setFileImportService(fileImportService);
    }

    @Reference
    public final void setFileImportService(FileImportService fileImportService) {
        this.fileImportService = fileImportService;
    }

    @Override
    public MessageHandler newMessageHandler() {
        return fileImportService.createMessageHandler();
    }

    @Override
    public boolean allowsMessageValidation() {
        return fileImportService.isLocalImportAllowedOnly();
    }
}
