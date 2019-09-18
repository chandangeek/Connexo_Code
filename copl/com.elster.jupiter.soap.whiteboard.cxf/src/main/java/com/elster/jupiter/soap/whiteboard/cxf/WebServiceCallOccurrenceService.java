package com.elster.jupiter.soap.whiteboard.cxf;

//import com.elster.jupiter.soap.whiteboard.cxf.impl.WebServiceCallRelatedObjectType;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.TranslationKey;

import aQute.bnd.annotation.ProviderType;

import java.util.List;
import java.util.Map;
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

    List<WebServiceCallRelatedObjectType> getRelatedObjectTypeByValue(String value);

    void addRelatedObjectTypes(String component, Layer layer, Map<String, TranslationKey> types);

    String getTranslationForType(String key);
}
