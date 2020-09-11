/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.impl;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.DataExportWebService;
import com.elster.jupiter.export.ExportTask;
import com.elster.jupiter.export.impl.webservicecall.WebServiceDataExportDomainExtension;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.time.RelativePeriod;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public interface IDataExportService extends DataExportService {

    DestinationSpec getDestination();

    IDataExportOccurrence createExportOccurrence(TaskOccurrence taskOccurrence);

    Optional<IDataExportOccurrence> findDataExportOccurrence(TaskOccurrence occurrence);

    Thesaurus getThesaurus();

    List<ExportTask> findExportTaskUsing(RelativePeriod relativePeriod);

    Path getTempDirectory();

    LocalFileWriter getLocalFileWriter();

    Optional<DataExportWebService> getExportWebService(String name);

    CustomPropertySet<ServiceCall, WebServiceDataExportDomainExtension> getServiceCallCPS();

    boolean isUsedAsADestination(EndPointConfiguration endPointConfiguration);

    boolean shouldCombineCreatedAndUpdatedDataInOneWebRequest();
}
