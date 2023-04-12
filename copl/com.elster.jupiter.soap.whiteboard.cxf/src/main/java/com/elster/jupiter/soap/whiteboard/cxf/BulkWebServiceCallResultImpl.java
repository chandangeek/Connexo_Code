/*
 * Copyright (c) 2023 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.soap.whiteboard.cxf;

import com.elster.jupiter.util.Pair;

import com.google.common.collect.ImmutableSet;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class BulkWebServiceCallResultImpl implements BulkWebServiceCallResult {
    private final Set<EndPointConfiguration> requestedEndpoints;
    private final Map<EndPointConfiguration, Pair<WebServiceCallOccurrence, Object>> successResult = new HashMap<>();
    private final Map<EndPointConfiguration, WebServiceCallOccurrence> failureResult = new HashMap<>();

    BulkWebServiceCallResultImpl(ImmutableSet<EndPointConfiguration> requestedEndpoints) {
        this.requestedEndpoints = requestedEndpoints;
    }

    void addSuccessResult(WebServiceCallOccurrence occurrence, Object response) {
        successResult.put(occurrence.getEndPointConfiguration(), Pair.of(occurrence, response));
    }

    void addFailureResult(WebServiceCallOccurrence occurrence) {
        failureResult.put(occurrence.getEndPointConfiguration(), occurrence);
    }

    public Set<EndPointConfiguration> getRequestedEndpoints() {
        return requestedEndpoints;
    }

    public Optional<WebServiceCallOccurrence> getOccurrence(EndPointConfiguration endPointConfiguration) {
        Optional<WebServiceCallOccurrence> result = Optional.ofNullable(successResult.get(endPointConfiguration))
                .map(Pair::getFirst);
        if (!result.isPresent()) {
            result = Optional.ofNullable(failureResult.get(endPointConfiguration));
        }
        return result;
    }
}
