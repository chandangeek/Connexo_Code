/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.rest.impl;

import com.elster.jupiter.export.DataExportDestination;
import com.elster.jupiter.export.ExportTask;

import org.glassfish.hk2.api.ServiceLocator;

interface DestinationInfoFactory {
    void create(ServiceLocator serviceLocator, ExportTask task, DestinationInfo info);

    void update(ServiceLocator serviceLocator, DataExportDestination destination, DestinationInfo info);

    DestinationInfo toInfo(ServiceLocator serviceLocator, DataExportDestination destination);

    Class<? extends DataExportDestination> getDestinationClass(ServiceLocator serviceLocator);
}
