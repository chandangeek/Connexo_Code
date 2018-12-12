/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.rest.impl;

import com.elster.jupiter.export.DataExportDestination;
import com.elster.jupiter.export.ExportTask;

import org.glassfish.hk2.api.ServiceLocator;

public enum DestinationType implements DestinationInfoFactory {
    FILE(FileDestinationInfoFactory.class),
    EMAIL(EmailDestinationInfoFactory.class),
    FTP(FtpDestinationInfoFactory.class),
    FTPS(FtpsDestinationInfoFactory.class),
    SFTP(SftpDestinationInfoFactory.class),
    WEBSERVICE(WebServiceDestinationInfoFactory.class);

    private final Class<? extends DestinationInfoFactory> factoryClass;

    DestinationType(Class<? extends DestinationInfoFactory> factoryClass) {
        this.factoryClass = factoryClass;
    }

    @Override
    public void create(ServiceLocator serviceLocator, ExportTask task, DestinationInfo info) {
        serviceLocator.getService(factoryClass).create(serviceLocator, task, info);
    }

    @Override
    public DestinationInfo toInfo(ServiceLocator serviceLocator, DataExportDestination destination) {
        return serviceLocator.getService(factoryClass).toInfo(serviceLocator, destination);
    }

    @Override
    public Class<? extends DataExportDestination> getDestinationClass(ServiceLocator serviceLocator) {
        return serviceLocator.getService(factoryClass).getDestinationClass(serviceLocator);
    }

    @Override
    public void update(ServiceLocator serviceLocator, DataExportDestination destination, DestinationInfo info) {
        serviceLocator.getService(factoryClass).update(serviceLocator, destination, info);
    }

    public Class<? extends DestinationInfoFactory> getFactoryClass() {
        return factoryClass;
    }
}
