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
import java.util.stream.Stream;

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

    @Override
    public Set<EndPointConfiguration> getRequestedEndpoints() {
        return requestedEndpoints;
    }

    @Override
    public Optional<WebServiceCallOccurrence> getOccurrence(EndPointConfiguration endPointConfiguration) {
        Optional<WebServiceCallOccurrence> result = Optional.ofNullable(successResult.get(endPointConfiguration))
                .map(Pair::getFirst);
        if (!result.isPresent()) {
            result = Optional.ofNullable(failureResult.get(endPointConfiguration));
        }
        return result;
    }

    @Override
    public Optional<?> getResponse(EndPointConfiguration endPointConfiguration) {
        return Optional.ofNullable(successResult.get(endPointConfiguration))
                .map(Pair::getLast);
    }

    @Override
    public boolean isSuccessful(EndPointConfiguration endPointConfiguration) {
        return successResult.containsKey(endPointConfiguration);
    }

    @Override
    public boolean isAtLeastOneEndpointSuccessful() {
        return !successResult.isEmpty();
    }

    @Override
    public boolean isSuccessful() {
        return requestedEndpoints.size() == successResult.size()
                && failureResult.isEmpty();
    }

    @Override
    public Set<WebServiceCallOccurrence> getOccurrences() {
        return Stream.concat(successResult.values().stream().map(Pair::getFirst), failureResult.values().stream())
                .collect(ImmutableSet.toImmutableSet());
    }

    @Override
    public Set<EndPointConfiguration> getSuccessfulEndpoints() {
        return ImmutableSet.copyOf(successResult.keySet());
    }

    @Override
    public Set<EndPointConfiguration> getFailedEndpoints() {
        return ImmutableSet.copyOf(failureResult.keySet());
    }

    @Override
    public Map<EndPointConfiguration, ?> getEndpointsWithResponses() {
        return successResult.entrySet().stream()
                .collect(HashMap::new, (map, entry) -> map.put(entry.getKey(), entry.getValue().getLast()), Map::putAll); // to avoid NPE in case response is null
    }

    @Override
    public Map<WebServiceCallOccurrence, ?> getOccurrencesWithResponses() {
        return successResult.values().stream()
                .collect(HashMap::new, (map, pair) -> map.put(pair.getFirst(), pair.getLast()), Map::putAll); // to avoid NPE in case response is null
    }
}
