/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.calendar.impl.importers;

import com.elster.jupiter.fileimport.FileImportService;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;

@Component(name = "com.elster.jupiter.calendar.importers.CalendarImporterMessageHandler",
        service = MessageHandlerFactory.class,
        property = {
                "name=CLI",
                "subscriber=" + CalendarImporterMessageHandler.SUBSCRIBER_NAME,
                "destination=" + CalendarImporterMessageHandler.DESTINATION_NAME},
        immediate = true)
public class CalendarImporterMessageHandler implements MessageHandlerFactory {

    public static final String DESTINATION_NAME = "CalendarImport";
    public static final String SUBSCRIBER_NAME = "CalendarImport";

    private volatile FileImportService fileImportService;

    public CalendarImporterMessageHandler() {}

    @Inject
    public CalendarImporterMessageHandler(FileImportService fileImportService) {
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

}