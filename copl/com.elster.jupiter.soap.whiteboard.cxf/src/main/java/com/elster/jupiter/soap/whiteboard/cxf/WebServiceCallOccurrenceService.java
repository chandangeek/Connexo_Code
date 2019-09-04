package com.elster.jupiter.soap.whiteboard.cxf;

import com.elster.jupiter.soap.whiteboard.cxf.impl.WebServiceCallRelatedObjectType;

import aQute.bnd.annotation.ProviderType;

import java.util.Optional;

@ProviderType
public interface WebServiceCallOccurrenceService {

    WebServiceCallOccurrenceFinderBuilder getWebServiceCallOccurrenceFinderBuilder();

    Optional<WebServiceCallOccurrence> getWebServiceCallOccurrence(long id);

    OccurrenceLogFinderBuilder getOccurrenceLogFinderBuilder();

    Optional<WebServiceCallRelatedObject> getRelatedObjectById(long id);

    Optional<WebServiceCallRelatedObject> getRelatedObjectByType(long id);

    Optional<WebServiceCallRelatedObject> getRelatedObjectByOccurrence(long id);

    Optional<WebServiceCallRelatedObjectType> getRelatedObjectTypeById(long id);

    Optional<WebServiceCallRelatedObjectType> getRelatedObjectTypeByKeyAndValue(String key, String value);

    Optional<WebServiceCallRelatedObjectType> getRelatedObjectTypeByDomainKeyAndValue(String domain, String key, String value);
}
