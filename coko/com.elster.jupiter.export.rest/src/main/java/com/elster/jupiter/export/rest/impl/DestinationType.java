package com.elster.jupiter.export.rest.impl;

import com.elster.jupiter.export.DataExportDestination;
import com.elster.jupiter.export.ExportTask;

public enum DestinationType implements DestinationInfoFactory {
    FILE(new FileDestinationInfoFactory()),
    EMAIL(new EmailDestinationInfoFactory()),
    FTP(new FtpDestinationInfoFactory());

    private final DestinationInfoFactory factory;

    DestinationType(DestinationInfoFactory factory) {
        this.factory = factory;
    }

    @Override
    public void create(ExportTask task, DestinationInfo info) {
        factory.create(task, info);
    }

    @Override
    public DestinationInfo toInfo(DataExportDestination destination) {
        return factory.toInfo(destination);
    }

    @Override
    public Class<? extends DataExportDestination> getDestinationClass() {
        return factory.getDestinationClass();
    }

    @Override
    public void update(DataExportDestination destination, DestinationInfo info) {
        factory.update(destination, info);
    }
}
