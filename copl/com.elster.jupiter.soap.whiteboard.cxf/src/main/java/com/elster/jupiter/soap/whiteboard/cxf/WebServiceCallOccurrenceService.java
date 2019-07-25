package com.elster.jupiter.soap.whiteboard.cxf;

import aQute.bnd.annotation.ProviderType;

import java.util.Optional;

@ProviderType
public interface WebServiceCallOccurrenceService {

    WebServiceCallOccurrenceFinderBuilder getWebServiceCallOccurrenceFinderBuilder();

    Optional<WebServiceCallOccurrence> getEndPointOccurrence(long id);

    OccurrenceLogFinderBuilder getOccurrenceLogFinderBuilder();
}
