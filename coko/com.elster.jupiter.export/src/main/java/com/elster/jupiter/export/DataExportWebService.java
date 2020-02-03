/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export;

import com.elster.jupiter.export.webservicecall.DataExportSCCustomInfo;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointProp;

import aQute.bnd.annotation.ConsumerType;
import aQute.bnd.annotation.ProviderType;
import org.osgi.service.component.annotations.Reference;

import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/**
 * All implementations should be registered as OSGi services and explicitly or implicitly depend (via {@link Reference}) on {@link DataExportService}
 * to be registered in the export framework correctly.
 */
@ConsumerType
public interface DataExportWebService extends EndPointProp {
    String TIMEOUT_PROPERTY_KEY = "webService.timeout";

    /**
     * @param endPointConfiguration The configuration of web service endpoint selected for sending the data.
     * @param data The {@link Stream} of data to send. Feel free to cast the particular items to a heir class corresponding to the data type claimed with {@link #getSupportedDataType()}.
     * @param context Contains the list of service calls for exported data tracking;
     * All service calls for response tracking must be created with the help of {@link ExportContext} in case of async response foreseen.
     * In case the response is synchronous and treated inside the implementation, or if we don't care about the response, just don't deal with the context.
     */
    void call(EndPointConfiguration endPointConfiguration, Stream<? extends ExportData> data, ExportContext context);

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

    @ProviderType
    interface ExportContext {
        //interface: customInformation getName, toString, fromString + Map<ReadingTypeDataExportItem, CustomInformation>
        ServiceCall startAndRegisterServiceCall(String uuid, long timeout, Map<ReadingTypeDataExportItem, DataExportSCCustomInfo> dataSources);
    }
}
