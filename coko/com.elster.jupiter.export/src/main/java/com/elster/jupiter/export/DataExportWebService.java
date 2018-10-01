/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export;

import com.elster.jupiter.export.webservicecall.DataExportServiceCallType;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointProp;

import aQute.bnd.annotation.ConsumerType;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

@ConsumerType
public interface DataExportWebService extends EndPointProp {
    String TIMEOUT_PROPERTY_KEY = "webService.timeout";

    /**
     * @param endPointConfiguration The configuration of web service endpoint selected for sending the data.
     * @param data The {@link Stream} of data to send. Feel free to cast the particular items to a heir class corresponding to the data type claimed with {@link #getSupportedDataType()}.
     * @return Must return a service call created with the help of {@link DataExportServiceCallType} for response tracking in case of async response foreseen;
     * {@code Optional.empty()} in case the response is synchronous and treated inside the implementation, or if we don't care about the response.
     */
    Optional<ServiceCall> call(EndPointConfiguration endPointConfiguration, Stream<? extends ExportData> data);

    /**
     * @return The <b>unique</b> name of the web service.
     */
    String getName();

    String getSupportedDataType();

    Set<Operation> getSupportedOperations();

    enum Operation {
        CREATE,
        CHANGE
    }
}
