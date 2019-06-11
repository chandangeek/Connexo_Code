package com.elster.jupiter.soap.whiteboard.cxf;

import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.JsonQueryParameters;

import aQute.bnd.annotation.ProviderType;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@ProviderType
public interface WebServiceCallOccurrenceService {

    List<WebServiceCallOccurrence> getEndPointOccurrences(JsonQueryParameters queryParameters, JsonQueryFilter filter, Set<String> applicationName, Long epId);

    Optional<WebServiceCallOccurrence> getEndPointOccurrence(Long id);

    List<EndPointLog> getLogForOccurrence(Long id, JsonQueryParameters queryParameters);
}
