/*
 * Copyright (c) 2023 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.soap.whiteboard.cxf;

import aQute.bnd.annotation.ProviderType;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

@ProviderType
public interface BulkWebServiceCallResult {
    /**
     * @return All the endpoint configurations where the request is attempted to send.
     */
    Set<EndPointConfiguration> getRequestedEndpoints();

    /**
     * @param endPointConfiguration The endpoint configuration.
     * @return The {@link WebServiceCallOccurrence} created for the given endpoint when sending the request.
     */
    Optional<WebServiceCallOccurrence> getOccurrence(EndPointConfiguration endPointConfiguration);

    /**
     * @param endPointConfiguration The endpoint configuration.
     * @return The object representing the received response if it is received, otherwise {@link Optional#empty()}.
     */
    Optional<?> getResponse(EndPointConfiguration endPointConfiguration);

    /**
     * @param endPointConfiguration The endpoint configuration.
     * @return {@code true} in case the request is sent successfully to the endpoint, {@code false} otherwise.
     */
    boolean isSuccessful(EndPointConfiguration endPointConfiguration);

    /**
     * @return {@code true} in case the request is sent successfully to at least one of the requested endpoints, {@code false} otherwise.
     */
    boolean isAtLeastOneEndpointSuccessful();

    /**
     * @return {@code true} in case the request is sent successfully to all the requested endpoints, {@code false} otherwise.
     */
    boolean isSuccessful();

    /**
     * @return The {@link WebServiceCallOccurrence}s created when sending the request to the required endpoints.
     */
    Set<WebServiceCallOccurrence> getOccurrences();

    /**
     * @return The set of endpoints where the request is sent successfully.
     */
    Set<EndPointConfiguration> getSuccessfulEndpoints();

    /**
     * @return The set of endpoints where sending the request failed.
     */
    Set<EndPointConfiguration> getFailedEndpoints();

    /**
     * @return The map of all endpoints, where the request is sent successfully, with the received responses.
     */
    Map<EndPointConfiguration, ?> getEndpointsWithResponses();

    /**
     * @return The map of all successful {@link WebServiceCallOccurrence}s, created when sending the request to the required endpoints, with the received responses.
     */
    Map<WebServiceCallOccurrence, ?> getOccurrencesWithResponses();
}
