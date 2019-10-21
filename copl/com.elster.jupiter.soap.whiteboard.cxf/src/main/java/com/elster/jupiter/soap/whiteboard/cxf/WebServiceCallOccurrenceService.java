package com.elster.jupiter.soap.whiteboard.cxf;

import com.elster.jupiter.domain.util.Finder;

import aQute.bnd.annotation.ProviderType;

import java.util.List;
import java.util.Optional;

@ProviderType
public interface WebServiceCallOccurrenceService {

    WebServiceCallOccurrenceFinderBuilder getWebServiceCallOccurrenceFinderBuilder();

    Optional<WebServiceCallOccurrence> getWebServiceCallOccurrence(long id);

    OccurrenceLogFinderBuilder getOccurrenceLogFinderBuilder();

    Finder<WebServiceCallRelatedAttribute> getRelatedAttributesByValueLike(String value);

    Optional<WebServiceCallRelatedAttribute> getRelatedObjectById(long id);

    List<WebServiceCallRelatedAttribute> getRelatedAttributesByValue(String value);

    String translateAttributeType(String key);
}
