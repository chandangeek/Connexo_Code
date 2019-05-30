/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.webservices.rest.impl;

import com.elster.jupiter.domain.util.DefaultFinder;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.rest.util.ConcurrentModificationExceptionFactory;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointLog;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointOccurrence;
import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;
import com.elster.jupiter.soap.whiteboard.cxf.security.Privileges;
import com.elster.jupiter.util.Checks;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Where;

import com.google.common.collect.Range;

/**
 * Resource to manage end point configurations
 */
@Path("/endpointconfigurations")
public class EndPointConfigurationResource {

    private final EndPointConfigurationService endPointConfigurationService;
    private final EndPointConfigurationInfoFactory endPointConfigurationInfoFactory;
    private final ExceptionFactory exceptionFactory;
    private final WebServicesService webServicesService;
    private final EndpointConfigurationLogInfoFactory endpointConfigurationLogInfoFactory;
    private final ConcurrentModificationExceptionFactory concurrentModificationExceptionFactory;
    private final EndpointConfigurationOccurrenceInfoFactorty endpointConfigurationOccurrenceInfoFactorty;
    private final OrmService ormService;

    @Inject
    public EndPointConfigurationResource(EndPointConfigurationService endPointConfigurationService,
                                         EndPointConfigurationInfoFactory endPointConfigurationInfoFactory,
                                         ExceptionFactory exceptionFactory,
                                         WebServicesService webServicesService,
                                         EndpointConfigurationLogInfoFactory endpointConfigurationLogInfoFactory,
                                         ConcurrentModificationExceptionFactory concurrentModificationExceptionFactory,
                                         EndpointConfigurationOccurrenceInfoFactorty endpointConfigurationOccurrenceInfoFactorty,
                                         OrmService ormService) {
        this.endPointConfigurationService = endPointConfigurationService;
        this.endPointConfigurationInfoFactory = endPointConfigurationInfoFactory;
        this.exceptionFactory = exceptionFactory;
        this.webServicesService = webServicesService;
        this.endpointConfigurationLogInfoFactory = endpointConfigurationLogInfoFactory;
        this.concurrentModificationExceptionFactory = concurrentModificationExceptionFactory;
        this.endpointConfigurationOccurrenceInfoFactorty = endpointConfigurationOccurrenceInfoFactorty;
        this.ormService = ormService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Transactional
    @RolesAllowed(Privileges.Constants.VIEW_WEB_SERVICES)
    public PagedInfoList getEndPointConfigurations(@BeanParam JsonQueryParameters queryParams, @Context UriInfo uriInfo) {
        List<EndPointConfigurationInfo> infoList = endPointConfigurationService.findEndPointConfigurations()
                .from(queryParams)
                .stream()
                .map(epc -> endPointConfigurationInfoFactory.from(epc, uriInfo))
                .collect(toList());
        return PagedInfoList.fromPagedList("endpoints", infoList, queryParams);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Path("/{id}")
    @Transactional
    @RolesAllowed(Privileges.Constants.VIEW_WEB_SERVICES)
    public EndPointConfigurationInfo getEndPointConfiguration(@PathParam("id") long id, @Context UriInfo uriInfo) {
        return endPointConfigurationService.getEndPointConfiguration(id)
                .map(epc -> endPointConfigurationInfoFactory.from(epc, uriInfo))
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_END_POINT_CONFIG));
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Transactional
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_WEB_SERVICES)
    public Response createEndPointConfiguration(EndPointConfigurationInfo info, @Context UriInfo uriInfo) {
        validatePayload(info);
        WebServiceDirection strategy = webServicesService.getWebService(info.webServiceName)
                .get()
                .isInbound() ? WebServiceDirection.INBOUND : WebServiceDirection.OUTBOUND;
        EndPointConfiguration endPointConfiguration = strategy.create(endPointConfigurationInfoFactory, info);
        EndPointConfigurationInfo endPointConfigurationInfo = endPointConfigurationInfoFactory.from(endPointConfiguration, uriInfo);
        return Response.status(Response.Status.CREATED).entity(endPointConfigurationInfo).build();
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Path("/{id}")
    @Transactional
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_WEB_SERVICES)
    public Response updateEndPointConfiguration(@PathParam("id") long id, EndPointConfigurationInfo info, @Context UriInfo uriInfo) {
        validatePayload(info);
        EndPointConfiguration endPointConfiguration = endPointConfigurationService.findAndLockEndPointConfigurationByIdAndVersion(id, info.version)
                .orElseThrow(concurrentModificationExceptionFactory.contextDependentConflictOn(info.name)
                        .withActualVersion(() -> endPointConfigurationService.getEndPointConfiguration(info.id).map(EndPointConfiguration::getVersion).orElse(null))
                        .supplier());
        if (endPointConfiguration.isActive()) { // need to deactivate the end point so certain changes will have effect
            endPointConfigurationService.deactivate(endPointConfiguration);
        }
        WebServiceDirection webServiceDirection = webServicesService.getWebService(info.webServiceName)
                .get()
                .isInbound() ? WebServiceDirection.INBOUND : WebServiceDirection.OUTBOUND;
        webServiceDirection.applyChanges(endPointConfigurationInfoFactory, endPointConfiguration, info);
        endPointConfiguration.save();
        if (info.active) {
            endPointConfigurationService.activate(endPointConfiguration);
        }
        EndPointConfigurationInfo endPointConfigurationInfo = endPointConfigurationInfoFactory.from(endPointConfiguration, uriInfo);
        return Response.ok(endPointConfigurationInfo).build();
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Path("/{id}/activate")
    @Transactional
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_WEB_SERVICES)
    public Response activateEndPointConfiguration(@PathParam("id") long id, EndPointConfigurationInfo info, @Context UriInfo uriInfo) {
        EndPointConfiguration endPointConfiguration = endPointConfigurationService.findAndLockEndPointConfigurationByIdAndVersion(id, info.version)
                .orElseThrow(() -> {
                    Optional<EndPointConfiguration> byId = endPointConfigurationService.getEndPointConfiguration(id);
                    return concurrentModificationExceptionFactory.contextDependentConflictOn(byId.map(EndPointConfiguration::getName).orElse(null))
                            .withActualVersion(() -> byId.map(EndPointConfiguration::getVersion).orElse(null))
                            .build();
                });
        if (!endPointConfiguration.isActive()) {
            endPointConfigurationService.activate(endPointConfiguration);
        }
        EndPointConfigurationInfo endPointConfigurationInfo = endPointConfigurationInfoFactory.from(endPointConfiguration, uriInfo);
        return Response.ok(endPointConfigurationInfo).build();
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Path("/{id}/deactivate")
    @Transactional
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_WEB_SERVICES)
    public Response deactivateEndPointConfiguration(@PathParam("id") long id, EndPointConfigurationInfo info, @Context UriInfo uriInfo) {
        EndPointConfiguration endPointConfiguration = endPointConfigurationService.findAndLockEndPointConfigurationByIdAndVersion(id, info.version)
                .orElseThrow(() -> {
                    Optional<EndPointConfiguration> byId = endPointConfigurationService.getEndPointConfiguration(id);
                    return concurrentModificationExceptionFactory.contextDependentConflictOn(byId.map(EndPointConfiguration::getName).orElse(null))
                            .withActualVersion(() -> byId.map(EndPointConfiguration::getVersion).orElse(null))
                            .build();
                });
        if (endPointConfiguration.isActive()) {
            endPointConfigurationService.deactivate(endPointConfiguration);
        }
        EndPointConfigurationInfo endPointConfigurationInfo = endPointConfigurationInfoFactory.from(endPointConfiguration, uriInfo);
        return Response.ok(endPointConfigurationInfo).build();
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Path("/{id}")
    @Transactional
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_WEB_SERVICES)
    public Response deleteEndPointConfiguration(@PathParam("id") long id, EndPointConfigurationInfo info) {
        validateBasicPayload(info);
        EndPointConfiguration endPointConfiguration = endPointConfigurationService.findAndLockEndPointConfigurationByIdAndVersion(id, info.version)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_END_POINT_CONFIG));
        endPointConfigurationService.delete(endPointConfiguration);
        return Response.ok().build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Path("/{id}/logs")
    @RolesAllowed(Privileges.Constants.VIEW_WEB_SERVICES)
    public PagedInfoList getAllLogs(@PathParam("id") long id, @BeanParam JsonQueryParameters queryParameters) {
        EndPointConfiguration endPointConfiguration = endPointConfigurationService.getEndPointConfiguration(id)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_END_POINT_CONFIG));

        /* Logs contains actions not related to occurrence. */
        List<EndpointConfigurationLogInfo> endpointConfigurationLogs = endPointConfiguration.getLogs()
                .from(queryParameters)
                .stream()
                .filter(log -> !log.getOccurrence().isPresent())
                .map(endpointConfigurationLogInfoFactory::from)
                .collect(toList());
        return PagedInfoList.fromPagedList("logs", endpointConfigurationLogs, queryParameters);
    }


    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Path("/occurrences")
    @RolesAllowed(Privileges.Constants.VIEW_WEB_SERVICES)
    public PagedInfoList getAllOccurrences(@BeanParam JsonQueryParameters queryParameters,
                                           @BeanParam JsonQueryFilter filter,
                                           @HeaderParam("X-CONNEXO-APPLICATION-NAME") String applicationName,
                                           @Context UriInfo uriInfo) {
        /*EndPointConfiguration endPointConfiguration = endPointConfigurationService.getEndPointConfiguration(id)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_END_POINT_CONFIG));*/

        /*List<EndpointConfigurationOccurrenceInfo> endpointConfigurationOccurrences = findEndPointOccurences()
                .from(queryParameters)
                .stream()
                .filter(epco -> epco.getEndPointConfiguration().)
                .map(epco -> endpointConfigurationOccurrenceInfoFactorty.from(epco, uriInfo))
                .collect(toList());*/
        System.out.println("getAllOccurrences!!!!"+filter);
        List<EndPointOccurrence> endPointOccurrences = getEndPointOccurrences(queryParameters, filter, applicationName, null);
        List<EndpointConfigurationOccurrenceInfo> endPointOccurrencesInfo = endPointOccurrences.
                                                         stream().
                                                         map(epco -> endpointConfigurationOccurrenceInfoFactorty.from(epco, uriInfo)).
                                                         collect(toList());

        return PagedInfoList.fromPagedList("occurrences", endPointOccurrencesInfo , queryParameters);
    }


    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Path("/occurrences/{id}")
    @RolesAllowed(Privileges.Constants.VIEW_WEB_SERVICES)
    public EndpointConfigurationOccurrenceInfo getOccurrence(@PathParam("id") long id, @Context UriInfo uriInfo) {

        DataModel dataModel = ormService.getDataModel("WebServicesService"/*WebServicesService.COMPONENT_NAME*/).get();
        Optional<EndPointOccurrence> epOcc = dataModel.mapper(EndPointOccurrence.class)
                .getUnique("id", id);

        System.out.println("GET OCCURRENCE with ID "+id);
        return epOcc
               .map(epc -> endpointConfigurationOccurrenceInfoFactorty.from(epc, uriInfo))
               .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_END_POINT_CONFIG));

    }




    /*public EndPointConfigurationOccurrenceFinderBuilder getEndPointConfigurationOccurrenceFinderBuilder(String applicationName) {
        Condition condition = Condition.TRUE;
        if (!"SYS".equalsIgnoreCase(applicationName)) {
            condition = condition.and(Where.where("applicationName")
                    .isEqualToIgnoreCase(applicationName));
        }

        return new EndPointConfigurationOccurrenceFinderBuilderImpl(dataModel, condition);
    }*/


//    private List<FileImportOccurrence> getFileImportOccurrences(JsonQueryParameters queryParameters, JsonQueryFilter filter, String applicationName, Long importServiceId) {
    private List<EndPointOccurrence> getEndPointOccurrences(JsonQueryParameters queryParameters, JsonQueryFilter filter, String applicationName, Long epId) {
        //EndPointConfigurationOccurrenceFinderBuilder finderBuilder = getEndPointConfigurationOccurrenceFinderBuilder(applicationName);

        DataModel dataModel = ormService.getDataModel("WebServicesService"/*WebServicesService.COMPONENT_NAME*/).get();
        EndPointConfigurationOccurrenceFinderBuilder finderBuilder =  new EndPointConfigurationOccurrenceFinderBuilderImpl(dataModel, Condition.TRUE);

        if (applicationName != null && !applicationName.isEmpty()){
            finderBuilder.withApplicationName(applicationName);
        }

        if (epId != null){
            EndPointConfiguration epc = endPointConfigurationService.getEndPointConfiguration(epId)
                    .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_END_POINT_CONFIG));
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
            EndPointConfiguration epc = endPointConfigurationService.getEndPointConfiguration(endPointId)
                    .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_END_POINT_CONFIG));

            finderBuilder.withEndPointConfiguration(epc);
        }

        if (filter.hasProperty("status")) {

            finderBuilder.withStatusIn(filter.getStringList("status")
                    .stream()
                    //.map(OccurenceStatus::valueOf)
                    .collect(Collectors.toList()));
        }

        List<EndPointOccurrence> epocList = finderBuilder.build().from(queryParameters).find();

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

    /*
    private List<EndPointOccurrence> getEndPointOccurrences(JsonQueryParameters queryParameters) {

        DataModel dataModel = ormService.getDataModel("WebServicesService").get();
        EndPointConfigurationOccurrenceFinderBuilder finderBuilder =  new EndPointConfigurationOccurrenceFinderBuilderImpl(dataModel, Condition.TRUE);

        return finderBuilder.build().from(queryParameters).find();
    }*/


    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Path("/{epId}/occurrences")
    @RolesAllowed(Privileges.Constants.VIEW_WEB_SERVICES)
    public PagedInfoList getAllOccurrencesForEndPoint(@PathParam("epId") long epId,
                                                      @BeanParam JsonQueryParameters queryParameters,
                                                      @BeanParam JsonQueryFilter filter,
                                                      @Context UriInfo uriInfo) {
        System.out.println("getAllOccurrencesForEndPoint !!!!"+epId);
/*        EndPointConfiguration endPointConfiguration = endPointConfigurationService.getEndPointConfiguration(epId)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_END_POINT_CONFIG));*/

        /*List<EndpointConfigurationOccurrenceInfo> endpointConfigurationOccurrences = endPointConfiguration.getOccurrences(true)
                .from(queryParameters)
                .stream()
                .map(epco -> endpointConfigurationOccurrenceInfoFactorty.from(epco, uriInfo))
                .collect(toList());*/
        List<EndPointOccurrence> endPointOccurrences = getEndPointOccurrences(queryParameters, filter, null, epId);
        List<EndpointConfigurationOccurrenceInfo> endPointOccurrencesInfo = endPointOccurrences.
                stream().
                map(epco -> endpointConfigurationOccurrenceInfoFactorty.from(epco, uriInfo)).
                collect(toList());

        return PagedInfoList.fromPagedList("occurrences", endPointOccurrencesInfo , queryParameters);
        //return PagedInfoList.fromPagedList("occurrences", endpointConfigurationOccurrences, queryParameters);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Path("/occurrences/{id}/log")
    @RolesAllowed(Privileges.Constants.VIEW_WEB_SERVICES)
    public PagedInfoList getLogForOccurrence(@PathParam("id") long id, @BeanParam JsonQueryParameters queryParameters) {

        System.out.println("getLogForOccurrence !!!!"+id);



        DataModel dataModel = ormService.getDataModel("WebServicesService").get();
        Optional<EndPointOccurrence> epOcc = dataModel.mapper(EndPointOccurrence.class)
                .getUnique("id", id);

        OccurrenceLogFinderBuilder finderBuilder =  new OccurrenceLogFinderBuilderImpl(dataModel, Condition.TRUE);

        if(epOcc.isPresent()){
            finderBuilder.withOccurrenceId(epOcc.get());
        }


        List<EndPointLog> logs = finderBuilder.build().from(queryParameters).find();
        List<EndpointConfigurationLogInfo> logsInfo = logs.stream().
                                                      map(log -> endpointConfigurationLogInfoFactory.fromFull(log)).
                                                      collect(toList());

        return PagedInfoList.fromPagedList("logs", logsInfo, queryParameters);
    }



    private void validatePayload(EndPointConfigurationInfo info) {
        validateBasicPayload(info);
        if (Checks.is(info.webServiceName).emptyOrOnlyWhiteSpace()) {
            throw new LocalizedFieldValidationException(MessageSeeds.FIELD_EXPECTED, "webServiceName");
        } else if (!webServicesService.getWebService(info.webServiceName).isPresent()) {
            throw exceptionFactory.newException(Response.Status.BAD_REQUEST, MessageSeeds.NO_SUCH_WEB_SERVICE);
        }
        if ((info.authenticationMethod == null || info.authenticationMethod.id == null)) {
            throw new LocalizedFieldValidationException(MessageSeeds.FIELD_EXPECTED, "authentication.id");
        }
        if (info.schemaValidation == null) {
            throw new LocalizedFieldValidationException(MessageSeeds.FIELD_EXPECTED, "schemaValidation");
        }
        if (info.httpCompression == null) {
            throw new LocalizedFieldValidationException(MessageSeeds.FIELD_EXPECTED, "httpCompression");
        }
        if (info.tracing == null) {
            throw new LocalizedFieldValidationException(MessageSeeds.FIELD_EXPECTED, "tracing");
        } else if (info.tracing && info.traceFile == null) {
            throw new LocalizedFieldValidationException(MessageSeeds.FIELD_EXPECTED, "traceFile");
        }
        if (info.active == null) {
            throw new LocalizedFieldValidationException(MessageSeeds.FIELD_EXPECTED, "active");
        }
        if (info.properties != null) {
            info.properties.stream()
                    .filter(propertyInfo -> propertyInfo.required)
                    .filter(propertyInfo -> checkPropertyOnNullAndEmpty(propertyInfo.getPropertyValueInfo().getValue()))
                    .findAny()
                    .ifPresent(propertyInfo -> {
                        throw new LocalizedFieldValidationException(MessageSeeds.FIELD_EXPECTED, propertyInfo.key);
                    });
        }
    }

    private void validateBasicPayload(EndPointConfigurationInfo info) {
        if (info == null) {
            throw exceptionFactory.newException(Response.Status.BAD_REQUEST, MessageSeeds.PAYLOAD_EXPECTED);
        }
    }

    private boolean checkPropertyOnNullAndEmpty(Object object) {
        return object == null || object.toString().isEmpty();
    }

}
