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
import com.elster.jupiter.util.conditions.Condition;

import com.google.common.collect.Range;

import javax.inject.Inject;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
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
    public List<WebServiceCallOccurrence> getEndPointOccurrences(JsonQueryParameters queryParameters,
                                                                 JsonQueryFilter filter,
                                                                 List<String> applicationName,
                                                                 Long epId){


        //DataModel dataModel = ormService.getDataModel(/*"WebServicesService"*/WebServicesService.COMPONENT_NAME).get();
        WebServiceCallOccurrenceFinderBuilder finderBuilder =  new WebServiceCallOccurrenceFinderBuilderImpl(dataModel, Condition.TRUE);

        if (applicationName != null && !applicationName.isEmpty()){
            finderBuilder.withApplicationName(applicationName);
        }

        if (epId != null){
            EndPointConfiguration epc = endPointConfigurationService.getEndPointConfiguration(epId).get();
                    //.orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_END_POINT_CONFIG));
            finderBuilder.withEndPointConfiguration(epc);
        }

        if (filter.hasProperty("startedOnFrom")) {
            if (filter.hasProperty("startedOnTo")) {
                finderBuilder.withStartTimeIn(Range.closed(filter.getInstant("startedOnFrom"), filter.getInstant("startedOnTo")));
            } else {
                finderBuilder.withStartTimeIn(Range.greaterThan(filter.getInstant("startedOnFrom")));
            }
        } else if (filter.hasProperty("startedOnTo")) {
            finderBuilder.withStartTimeIn(Range.closed(Instant.EPOCH, filter.getInstant("startedOnTo")));
        }
        if (filter.hasProperty("finishedOnFrom")) {
            if (filter.hasProperty("finishedOnTo")) {
                finderBuilder.withEndTimeIn(Range.closed(filter.getInstant("finishedOnFrom"), filter.getInstant("finishedOnTo")));
            } else {
                finderBuilder.withEndTimeIn(Range.greaterThan(filter.getInstant("finishedOnFrom")));
            }
        } else if (filter.hasProperty("finishedOnTo")) {
            finderBuilder.withEndTimeIn(Range.closed(Instant.EPOCH, filter.getInstant("finishedOnTo")));
        }
        /* Find endpoint by ID */
        if (filter.hasProperty("webServiceEndPoint")) {

            Long endPointId = filter.getLong("webServiceEndPoint");
            EndPointConfiguration epc = endPointConfigurationService.getEndPointConfiguration(endPointId).get();
                    //.orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_END_POINT_CONFIG));

            finderBuilder.withEndPointConfiguration(epc);
        }

        if (filter.hasProperty("status")) {

            finderBuilder.withStatusIn(filter.getStringList("status")
                    .stream()
                    .collect(Collectors.toList()));
        }

        List<WebServiceCallOccurrence> epocList = finderBuilder.build().from(queryParameters).find();

        if(filter.hasProperty("type")){
            List<String> typeList = filter.getStringList("type");
            if (typeList.contains("INBOUND") && !typeList.contains("OUTBOUND")){
                epocList = epocList.stream().
                        filter(epoc -> epoc.getEndPointConfiguration().isInbound()).collect(toList());

            } else if  (typeList.contains("OUTBOUND") && !typeList.contains("INBOUND")) {
                epocList = epocList.stream().
                        filter(epoc -> !epoc.getEndPointConfiguration().isInbound()).collect(toList());
            }
        }
        return epocList;
    }

    @Override
    public Optional<WebServiceCallOccurrence> getEndPointOccurrence(Long id){
        Optional<WebServiceCallOccurrence> epOcc = dataModel.mapper(WebServiceCallOccurrence.class)
                .getUnique("id", id);
        return epOcc;
    }

    @Override
    public List<EndPointLog> getLogForOccurrence(Long id, JsonQueryParameters queryParameters){
        //DataModel dataModel = ormService.getDataModel(WebServicesService.COMPONENT_NAME).get();
        Optional<WebServiceCallOccurrence> epOcc = dataModel.mapper(WebServiceCallOccurrence.class)
                .getUnique("id", id);

        OccurrenceLogFinderBuilder finderBuilder =  new OccurrenceLogFinderBuilderImpl(dataModel, Condition.TRUE);

        if(epOcc.isPresent()){
            finderBuilder.withOccurrenceId(epOcc.get());
        }


        //List<EndPointLog> logs = finderBuilder.build().from(queryParameters).find();

        return finderBuilder.build().from(queryParameters).find();
    }
}
