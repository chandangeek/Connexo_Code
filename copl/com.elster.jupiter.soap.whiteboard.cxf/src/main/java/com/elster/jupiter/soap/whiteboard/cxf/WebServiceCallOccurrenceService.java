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

    //Optional<WebServiceCallRelatedObjectBinding> getRelatedObjectById(long id);


    Optional<WebServiceCallRelatedObject> getRelatedObjectTypeByDomainKeyAndValue(String domain, String key, String value);

    List<WebServiceCallRelatedObject> getRelatedObjectByValue(String value);

    public Optional<WebServiceCallRelatedObject> getRelatedObjectById(long id);

    void addRelatedObjectTypes(String component, Layer layer, Map<String, TranslationKey> types);

    String getTranslationForType(String key);
}
