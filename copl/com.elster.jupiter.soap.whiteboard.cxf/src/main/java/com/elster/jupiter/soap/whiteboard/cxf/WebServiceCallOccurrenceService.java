package com.elster.jupiter.soap.whiteboard.cxf;

import com.elster.jupiter.domain.util.Finder;

import aQute.bnd.annotation.ProviderType;

import java.util.List;
import java.util.Optional;

@ProviderType
public interface WebServiceCallOccurrenceService {

    WebServiceCallOccurrenceFinderBuilder getWebServiceCallOccurrenceFinderBuilder();

    Optional<WebServiceCallOccurrence> getWebServiceCallOccurrence(long id);

    Optional<WebServiceCallOccurrence> findAndLockWebServiceCallOccurrence(long id);

    OccurrenceLogFinderBuilder getOccurrenceLogFinderBuilder();

    Finder<WebServiceCallRelatedAttribute> getRelatedAttributesByValueLike(String value);

    Optional<WebServiceCallRelatedAttribute> getRelatedObjectById(long id);

    List<WebServiceCallRelatedAttribute> getRelatedAttributesByValue(String value);

    String translateAttributeType(String key);

    WebServiceCallOccurrence startOccurrence(EndPointConfiguration endPointConfiguration, String requestName, String application);

    WebServiceCallOccurrence startOccurrence(EndPointConfiguration endPointConfiguration, String requestName, String application, String payload);

    WebServiceCallOccurrence passOccurrence(long id);

    WebServiceCallOccurrence failOccurrence(long id, String message);

    WebServiceCallOccurrence failOccurrence(long id, Exception exception);

    WebServiceCallOccurrence failOccurrence(long id, String message, Exception exception);

    WebServiceCallOccurrence cancelOccurrence(long id);

    WebServiceCallOccurrence getOngoingOccurrence(long id);
}
