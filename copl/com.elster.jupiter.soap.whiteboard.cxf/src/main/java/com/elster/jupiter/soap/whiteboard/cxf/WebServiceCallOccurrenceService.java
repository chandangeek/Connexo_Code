package com.elster.jupiter.soap.whiteboard.cxf;

import java.util.Optional;

public interface WebServiceCallOccurrenceService {

    WebServiceCallOccurrenceFinderBuilder getWebServiceCallOccurrenceFinderBuilder();

    Optional<WebServiceCallOccurrence> getEndPointOccurrence(Long id);

    OccurrenceLogFinderBuilder getOccurrenceLogFinderBuilder();
}
