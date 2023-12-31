/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.rest.impl;

import com.elster.jupiter.export.DataExportDestination;
import com.elster.jupiter.export.EmailDestination;
import com.elster.jupiter.export.ExportTask;

import org.glassfish.hk2.api.ServiceLocator;

public class EmailDestinationInfoFactory implements DestinationInfoFactory {
    @Override
    public void create(ServiceLocator serviceLocator, ExportTask task, DestinationInfo info) {
        task.addEmailDestination(info.recipients, info.subject, info.fileName, info.fileExtension);
    }

    @Override
    public DestinationInfo toInfo(ServiceLocator serviceLocator, DataExportDestination destination) {
        if (!(destination instanceof EmailDestination)) {
            throw new IllegalArgumentException();
        }
        EmailDestination emailDestination = (EmailDestination) destination;
        DestinationInfo destinationInfo = new DestinationInfo();
        destinationInfo.type = DestinationType.EMAIL;
        destinationInfo.id = destination.getId();
        destinationInfo.recipients = emailDestination.getRecipients();
        destinationInfo.subject = emailDestination.getSubject();
        destinationInfo.fileName = emailDestination.getFileName();
        destinationInfo.fileExtension = emailDestination.getFileExtension();
        return destinationInfo;
    }

    @Override
    public Class<? extends DataExportDestination> getDestinationClass(ServiceLocator serviceLocator) {
        return EmailDestination.class;
    }

    @Override
    public void update(ServiceLocator serviceLocator, DataExportDestination destination, DestinationInfo info) {
        if (!(destination instanceof EmailDestination)) {
            throw new IllegalArgumentException();
        }
        EmailDestination emailDestination = (EmailDestination) destination;
        emailDestination.setRecipients(info.recipients);
        emailDestination.setSubject(info.subject);
        emailDestination.setAttachmentName(info.fileName);
        emailDestination.setAttachmentExtension(info.fileExtension);
        emailDestination.save();
    }
}
