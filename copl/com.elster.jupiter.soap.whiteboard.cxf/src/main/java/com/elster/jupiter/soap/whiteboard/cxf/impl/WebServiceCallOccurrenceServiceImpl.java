package com.elster.jupiter.soap.whiteboard.cxf.impl;

import com.elster.jupiter.domain.util.DefaultFinder;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallOccurrenceFinderBuilder;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.soap.whiteboard.cxf.OccurrenceLogFinderBuilder;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallOccurrence;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallOccurrenceService;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallRelatedAttribute;
import com.elster.jupiter.util.conditions.Condition;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.elster.jupiter.util.conditions.Where.where;

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
        @Override
        public String toString() {
            return "Layer="+layer+" ,component = "+component;
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
    public Finder<WebServiceCallRelatedAttribute> getRelatedAttributesByValueLike(String value){
        Condition typeCondition = Condition.TRUE;
        String dbSearchText = (value != null && !value.isEmpty()) ? "*" + value + "*" : "*";
        typeCondition = typeCondition.and(where("value").likeIgnoreCase(dbSearchText));
        return DefaultFinder.of(WebServiceCallRelatedAttribute.class, typeCondition, this.dataModel).sorted("value", true );
    };


    @Override
    public Optional<WebServiceCallRelatedAttribute> getRelatedObjectById(long id){

        Optional<WebServiceCallRelatedAttribute> relatedObject = dataModel.mapper(WebServiceCallRelatedAttribute.class)
                .getOptional(id);

        return relatedObject;
    };

    public void addRelatedObjectTypes(String component, Layer layer, Map<String, TranslationKey> typesMap){

        LayerAndComponent layerAndComponent = new LayerAndComponent(layer ,component);
        types.putAll(typesMap);
        System.out.println("TYPES="+types);
        typesMap.keySet().forEach(key->{
            layerAndComponentsMap.put(key, layerAndComponent);
            System.out.println("layerAndComponentsMap="+layerAndComponentsMap);
        });
    }

    public void removeRelatedObjectTypes(Map<String, TranslationKey> typesMap){
        typesMap.keySet().forEach(key->{
            types.remove(key);
            layerAndComponentsMap.remove(key);
        });
    }

    @Override
    public String translateAttributeType(String key){
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
