/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export;

import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;

import aQute.bnd.annotation.ConsumerType;

import java.util.Set;
import java.util.stream.Stream;

@ConsumerType
public interface DataExportWebService {
    // TODO: move to implementers
//    String CREATE_NAME = "SAP UtilitiesTimeSeriesERPItemBulkCreateRequest_COut";
//    String CHANGE_NAME = "SAP UtilitiesTimeSeriesERPItemBulkChangeRequest_COut";
    String TIMEOUT_PROPERTY_KEY = "webService.timeout";

    ServiceCall call(EndPointConfiguration endPointConfiguration, Stream<? extends ExportData> data);

    /**
     * Returns the <b>unique</b> name of the web service.
     * @return The <b>unique</b> name of the web service.
     */
    String getName();

    Set<String> getSupportedDataTypes();

    Set<Operation> getSupportedOperations();

    enum Operation {
        CREATE,
        CHANGE
    }
}
