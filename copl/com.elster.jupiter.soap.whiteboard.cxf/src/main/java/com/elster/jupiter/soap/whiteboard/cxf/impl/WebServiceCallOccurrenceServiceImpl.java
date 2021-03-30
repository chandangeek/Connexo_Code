package com.elster.jupiter.soap.whiteboard.cxf.impl;

import com.elster.jupiter.domain.util.DefaultFinder;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.soap.whiteboard.cxf.OccurrenceLogFinderBuilder;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallOccurrence;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallOccurrenceFinderBuilder;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallOccurrenceService;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallRelatedAttribute;
import com.elster.jupiter.util.conditions.Condition;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static com.elster.jupiter.util.conditions.Where.where;

public class WebServiceCallOccurrenceServiceImpl implements WebServiceCallOccurrenceService {
    private volatile DataModel dataModel;
    private volatile NlsService nlsService;
    /* key for both maps is key of type */
    private volatile Map<String, TranslationKey> types = new ConcurrentHashMap<>();
    private volatile Map<String, LayerAndComponent> layerAndComponentsMap = new ConcurrentHashMap<>();

    private static class LayerAndComponent {
        private Layer layer;
        private String component;

        private LayerAndComponent(Layer layer, String component) {
            this.layer = layer;
            this.component = component;
        }

        @Override
        public boolean equals(Object obj) {
            return this == obj
                    || obj instanceof LayerAndComponent
                    && Objects.equals(layer, ((LayerAndComponent) obj).layer)
                    && Objects.equals(component, ((LayerAndComponent) obj).component);
        }

        @Override
        public int hashCode() {
            return Objects.hash(layer, component);
        }

        public Layer getLayer() {
            return this.layer;
        }

        public String getComponent() {
            return this.component;
        }

        @Override
        public String toString() {
            return "Layer=" + layer + " ,component = " + component;
        }
    }

    @Inject
    WebServiceCallOccurrenceServiceImpl(DataModel dataModel, NlsService nlsService) {
        this.dataModel = dataModel;
        this.nlsService = nlsService;
    }

    @Override
    public WebServiceCallOccurrenceFinderBuilder getWebServiceCallOccurrenceFinderBuilder() {
        return new WebServiceCallOccurrenceFinderBuilderImpl(dataModel);
    }

    @Override
    public Optional<WebServiceCallOccurrence> getWebServiceCallOccurrence(long id) {
        return dataModel.mapper(WebServiceCallOccurrence.class)
                .getUnique("id", id);
    }

    @Override
    public Optional<WebServiceCallOccurrence> findAndLockWebServiceCallOccurrence(long id) {
        return Optional.ofNullable(dataModel.mapper(WebServiceCallOccurrence.class).lock(id));
    }

    @Override
    public Finder<WebServiceCallRelatedAttribute> getRelatedAttributesByValueLike(String value) {
        String dbSearchText = (value != null && !value.isEmpty()) ? "*" + value + "*" : "*";
        Condition typeCondition = where(WebServiceCallRelatedAttributeImpl.Fields.ATTR_VALUE.fieldName()).likeIgnoreCase(dbSearchText);
        return DefaultFinder.of(WebServiceCallRelatedAttribute.class, typeCondition, this.dataModel).sorted(WebServiceCallRelatedAttributeImpl.Fields.ATTR_VALUE.fieldName(), true);
    }

    @Override
    public Optional<WebServiceCallRelatedAttribute> getRelatedObjectById(long id) {
        return dataModel.mapper(WebServiceCallRelatedAttribute.class)
                .getOptional(id);
    }

    @Override
    public List<WebServiceCallRelatedAttribute> getRelatedAttributesByValue(String value) {
        return dataModel.mapper(WebServiceCallRelatedAttribute.class)
                .select(where(WebServiceCallRelatedAttributeImpl.Fields.ATTR_VALUE.fieldName()).isEqualToIgnoreCase(value));
    }

    void addRelatedObjectTypes(String component, Layer layer, Map<String, TranslationKey> typesMap) {
        LayerAndComponent layerAndComponent = new LayerAndComponent(layer, component);
        types.putAll(typesMap);
        typesMap.keySet().forEach(key -> layerAndComponentsMap.put(key, layerAndComponent));
    }

    void removeRelatedObjectTypes(Map<String, TranslationKey> typesMap) {
        typesMap.keySet().forEach(key -> {
            types.remove(key);
            layerAndComponentsMap.remove(key);
        });
    }

    @Override
    public String translateAttributeType(String key) {
        LayerAndComponent layerAndComponent = layerAndComponentsMap.get(key);
        TranslationKey translationKey = types.get(key);
        Thesaurus thesaurus = nlsService.getThesaurus(layerAndComponent.getComponent(), layerAndComponent.getLayer());

        return thesaurus.getFormat(translationKey).format();
    }

    @Override
    public OccurrenceLogFinderBuilder getOccurrenceLogFinderBuilder() {
        return new OccurrenceLogFinderBuilderImpl(dataModel);
    }
}
