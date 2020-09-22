/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.webservicecall;

import com.elster.jupiter.export.ReadingTypeDataExportItem;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;

import aQute.bnd.annotation.ProviderType;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@ProviderType
public interface DataExportServiceCallType {
    String HANDLER_NAME = "WebServiceDataExportServiceCallHandler";
    String CHILD_HANDLER_NAME = "Exported data source";

    /**
     * Creates and starts a new service call in current transaction, or in a new transaction if there's no transaction in context.
     * The service call will be performed asynchronously after commit of this transaction.
     *
     * @param uuid UUID identifying the service call.
     * @param timeout Timeout to wait for successful service call closure in milliseconds.
     * @param data a map containing custom information per ReadingTypeDataExportItem.
     * @return A new service call.
     */
    ServiceCall startServiceCall(String uuid, long timeout, Map<ReadingTypeDataExportItem, String> data);

    /**
     * Creates and starts a new service call in a new thread.
     * The service call will be performed asynchronously right after calling this method.
     *
     * @param uuid UUID identifying the service call.
     * @param timeout Timeout to wait for successful service call closure in milliseconds.
     * @param data a map containing custom information per ReadingTypeDataExportItem.
     * @return A new service call.
     */
    ServiceCall startServiceCallAsync(String uuid, long timeout, Map<ReadingTypeDataExportItem, String> data);


    /**
     * @param uuid UUID identifying the service call.
     * @return {@link Optional} of found service call, or empty if not found.
     */
    Optional<ServiceCall> findServiceCall(String uuid);

    /**
     * @param states {@code EnumSet<DefaultState>} set of the service call states.
     * @return {@link List} of found service calls.
     */
    List<ServiceCall> findServiceCalls(EnumSet<DefaultState> states);

    /**
     * Tries failing a given service call. If it is already closed, does nothing.
     *
     * @param serviceCall Service call to close.
     * @param errorMessage Error message to close the service call with.
     * @return Actual {@link ServiceCallStatus} after the attempt to fail.
     */
    ServiceCallStatus tryFailingServiceCall(ServiceCall serviceCall, String errorMessage);

    /**
     * Tries passing a given service call. If it is already closed, does nothing.
     *
     * @param serviceCall Service call to close.
     * @return Actual {@link ServiceCallStatus} after the attempt to pass.
     */
    ServiceCallStatus tryPassingServiceCall(ServiceCall serviceCall);

    /**
     * Tries moving service call to partial success state.
     * Before moving tries closing child service calls by custom info (profile ids)
     * If it is already closed, does nothing.
     *
     * @param serviceCall Service call to close.
     * @param successfulChildren list of successful children
     * @param errorMessage Error message to close the service call with.
     * @return Actual {@link ServiceCallStatus} after the attempt to pass.
     */
    ServiceCallStatus tryPartiallyPassingServiceCall(ServiceCall serviceCall, Collection<ServiceCall> successfulChildren, String errorMessage);

    /**
     * Re-reads the service call status from database.
     *
     * @param serviceCall Service call to check status.
     * @return {@link ServiceCallStatus} containing info about actual service call state.
     */
    ServiceCallStatus getStatus(ServiceCall serviceCall);

    List<ServiceCallStatus> getStatuses(Collection<ServiceCall> serviceCalls);

    Set<ReadingTypeDataExportItem> getDataSources(Collection<ServiceCall> childServiceCalls);

    String getCustomInfoFromChildServiceCall(ServiceCall serviceCall);
}
