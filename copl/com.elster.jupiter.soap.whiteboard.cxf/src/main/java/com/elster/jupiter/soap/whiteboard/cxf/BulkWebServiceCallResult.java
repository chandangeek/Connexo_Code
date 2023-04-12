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
    Set<EndPointConfiguration> getRequestedEndpoints();

    Optional<WebServiceCallOccurrence> getOccurrence(EndPointConfiguration endPointConfiguration);

    Optional<?> getResponse(EndPointConfiguration endPointConfiguration);

    boolean isSuccessful(EndPointConfiguration endPointConfiguration);

    boolean isAtLeastOneEndpointSuccessful();

    boolean isSuccessful();

    Set<WebServiceCallOccurrence> getOccurrences();

    Set<EndPointConfiguration> getSuccessfulEndpoints();

    Set<EndPointConfiguration> getFailedEndpoints();

    Map<EndPointConfiguration, ?> getEndpointsWithResponses();

    Map<WebServiceCallOccurrence, ?> getOccurrencesWithResponses();
}
