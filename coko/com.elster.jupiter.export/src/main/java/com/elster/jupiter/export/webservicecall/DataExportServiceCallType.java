/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.webservicecall;

import com.elster.jupiter.export.ReadingTypeDataExportItem;
import com.elster.jupiter.servicecall.ServiceCall;

import aQute.bnd.annotation.ProviderType;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@ProviderType
public interface DataExportServiceCallType {
    /**
     * Creates and starts a new service call in current transaction, or in a new transaction if there's no transaction in context.
     * The service call will be performed asynchronously after commit of this transaction.
     * @param uuid UUID identifying the service call.
     * @param timeout Timeout to wait for successful service call closure in milliseconds.
     * @return A new service call.
     */
    ServiceCall startServiceCall(String uuid, long timeout, List<ReadingTypeDataExportItem> itemList);

    /**
     * Creates and starts a new service call in a new thread.
     * The service call will be performed asynchronously right after calling this method.
     * @param uuid UUID identifying the service call.
     * @param timeout Timeout to wait for successful service call closure in milliseconds.
     * @return A new service call.
     */
    ServiceCall startServiceCallAsync(String uuid, long timeout, List<ReadingTypeDataExportItem> itemList);


    /**
     * @param uuid UUID identifying the service call.
     * @return {@link Optional} of found service call, or empty if not found.
     */
    Optional<ServiceCall> findServiceCall(String uuid);

    /**
     * Tries failing a given service call. If it is already closed, does nothing.
     * @param serviceCall Service call to close.
     * @param errorMessage Error message to close the service call with.
     * @return Actual {@link ServiceCallStatus} after the attempt to fail.
     */
    ServiceCallStatus tryFailingServiceCall(ServiceCall serviceCall, String errorMessage);

    /**
     * Tries passing a given service call. If it is already closed, does nothing.
     * @param serviceCall Service call to close.
     * @return Actual {@link ServiceCallStatus} after the attempt to pass.
     */
    ServiceCallStatus tryPassingServiceCall(ServiceCall serviceCall);

    /**
     * Re-reads the service call status from database.
     * @param serviceCall Service call to check status.
     * @return {@link ServiceCallStatus} containing info about actual service call state.
     */
    ServiceCallStatus getStatus(ServiceCall serviceCall);

    List<ServiceCallStatus> getStatuses(Collection<ServiceCall> serviceCalls);

    Set<ReadingTypeDataExportItem> getDataSources(ServiceCall serviceCall);
}
