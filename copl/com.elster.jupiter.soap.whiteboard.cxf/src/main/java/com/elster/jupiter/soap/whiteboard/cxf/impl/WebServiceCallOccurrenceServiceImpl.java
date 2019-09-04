package com.elster.jupiter.soap.whiteboard.cxf.impl;

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
import com.elster.jupiter.util.conditions.Condition;

import com.google.common.collect.Range;

import javax.inject.Inject;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

public class WebServiceCallOccurrenceServiceImpl implements WebServiceCallOccurrenceService {

    private volatile DataModel dataModel;
    private volatile EndPointConfigurationService endPointConfigurationService;

    @Inject
    WebServiceCallOccurrenceServiceImpl(DataModel dataModel, EndPointConfigurationService endPointConfigurationService) {
        this.dataModel = dataModel;
        this.endPointConfigurationService = endPointConfigurationService;
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
    public OccurrenceLogFinderBuilder getOccurrenceLogFinderBuilder(){
        return new OccurrenceLogFinderBuilderImpl(dataModel);
    }
}
