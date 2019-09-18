package com.elster.jupiter.soap.whiteboard.cxf.impl;

import com.elster.jupiter.domain.util.DefaultFinder;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallOccurrenceFinderBuilder;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointLog;
import com.elster.jupiter.soap.whiteboard.cxf.OccurrenceLogFinderBuilder;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallOccurrence;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallOccurrenceService;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallOccurrenceStatus;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallRelatedObject;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallRelatedObjectType;
import com.elster.jupiter.util.conditions.Condition;

import com.google.common.collect.Range;

import javax.inject.Inject;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.conditions.Where.where;
import static java.util.stream.Collectors.toList;

public class WebServiceCallOccurrenceServiceImpl implements WebServiceCallOccurrenceService {

    private volatile DataModel dataModel;
    private volatile EndPointConfigurationService endPointConfigurationService;
    private volatile NlsService nlsService;
    /* key for both maps is key of type */
    private volatile Map<String, TranslationKey> types = new HashMap<>();
    private volatile Map<String, LayerAndComponent>  layerAndComponentsMap = new HashMap<>();

    private class LayerAndComponent {
        Layer layer;
        String component;

        public LayerAndComponent(Layer layer, String component){
            this.layer = layer;
            this.component = component;
        }

        public LayerAndComponent(String component, Layer layer) {
        }

        @Override
        public boolean equals(Object obj){
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            LayerAndComponent that = (LayerAndComponent) obj;
            return this.layer == that.layer && this.component.equals(that.component);
        }

        @Override
        public int hashCode() {
            int result = 31*layer.hashCode() + component.hashCode();
            return result;
        }

        public Layer getLayer(){
            return this.layer;
        }

        public String getComponent(){
            return this.component;
        }
    }

    @Inject
    WebServiceCallOccurrenceServiceImpl(DataModel dataModel,
                                        EndPointConfigurationService endPointConfigurationService,
                                        NlsService nlsService) {
        this.dataModel = dataModel;
        this.endPointConfigurationService = endPointConfigurationService;
        this.nlsService = nlsService;
    }

    @Override
    public WebServiceCallOccurrenceFinderBuilder getWebServiceCallOccurrenceFinderBuilder(){
        return new WebServiceCallOccurrenceFinderBuilderImpl(dataModel);
    }


    @Override
    public Optional<WebServiceCallOccurrence> getWebServiceCallOccurrence(long id){
        Optional<WebServiceCallOccurrence> epOcc = dataModel.mapper(WebServiceCallOccurrence.class)
                .getUnique("id", id);
        return epOcc;
    }

    @Override
    public Optional<WebServiceCallRelatedObject> getRelatedObjectById(long id){
        Optional<WebServiceCallRelatedObject> relatedObject = dataModel.mapper(WebServiceCallRelatedObject.class)
                .getUnique("id",id);
        return relatedObject;
    }

    @Override
    public Optional<WebServiceCallRelatedObject> getRelatedObjectByType(long id){
        Optional<WebServiceCallRelatedObject> relatedObject = dataModel.mapper(WebServiceCallRelatedObject.class)
                .getUnique("type",id);
        return relatedObject;
    }

    @Override
    public Optional<WebServiceCallRelatedObject> getRelatedObjectByOccurrence(long id){
        Optional<WebServiceCallRelatedObject> relatedObject = dataModel.mapper(WebServiceCallRelatedObject.class)
                .getUnique("WebServiceCallOccurrenceId",id);
        return relatedObject;
    }

    @Override
    public Optional<WebServiceCallRelatedObjectType> getRelatedObjectTypeById(long id){
        Optional<WebServiceCallRelatedObjectType> relatedObjectType = dataModel.mapper(WebServiceCallRelatedObjectType.class)
                .getUnique("id", id);
        return relatedObjectType;
    }

    @Override
    public Optional<WebServiceCallRelatedObjectType> getRelatedObjectTypeByKeyAndValue(String key, String value){
        Optional<WebServiceCallRelatedObjectType> relatedObjectType = dataModel.mapper(WebServiceCallRelatedObjectType.class)
                .getUnique("key", key, "value", value);
        return relatedObjectType;
    }

    @Override
    public Optional<WebServiceCallRelatedObjectType> getRelatedObjectTypeByDomainKeyAndValue(String domain, String key, String value){
        String[] fieldName = {"typeDomain", "key", "value"};
        String[] values = {domain, key, value};
        Optional<WebServiceCallRelatedObjectType> relatedObjectType = dataModel.mapper(WebServiceCallRelatedObjectType.class)
                .getUnique(fieldName, values);
        return relatedObjectType;
    }

    @Override
    public List<WebServiceCallRelatedObjectType> getRelatedObjectTypeByValue(String value){
        Condition typeCondition = Condition.TRUE;

        typeCondition = typeCondition.and(where("value").likeIgnoreCase(value));

        return DefaultFinder.of(WebServiceCallRelatedObjectType.class, typeCondition, this.dataModel).find();
    };

    @Override
    public void addRelatedObjectTypes(String component, Layer layer, Map<String, TranslationKey> typesMap){
        LayerAndComponent layerAndComponent = new LayerAndComponent(layer ,component);
        types.putAll(typesMap);
        typesMap.keySet().forEach(key->{
            layerAndComponentsMap.put(key, layerAndComponent);
        });
    }

    @Override
    public String getTranslationForType(String key){
        LayerAndComponent layerAndComponent = layerAndComponentsMap.get(key);
        TranslationKey translationKey = types.get(key);
        Thesaurus thesaurus = nlsService.getThesaurus(layerAndComponent.getComponent(), layerAndComponent.getLayer());

        return thesaurus.getFormat(translationKey).format();
    }

    @Override
    public OccurrenceLogFinderBuilder getOccurrenceLogFinderBuilder(){
        return new OccurrenceLogFinderBuilderImpl(dataModel);
    }
}
