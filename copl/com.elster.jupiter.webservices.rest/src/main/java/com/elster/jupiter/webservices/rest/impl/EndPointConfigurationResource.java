/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.webservices.rest.impl;

import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.rest.util.ConcurrentModificationExceptionFactory;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.soap.whiteboard.cxf.WebService;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallOccurrenceService;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

/**
 * Resource to manage end point configurations
 */
@Path("/endpointconfigurations")
public class EndPointConfigurationResource extends BaseResource {

    private final EndPointConfigurationInfoFactory endPointConfigurationInfoFactory;
    private final WebServicesService webServicesService;
    private final ConcurrentModificationExceptionFactory concurrentModificationExceptionFactory;

    @Inject
    public EndPointConfigurationResource(EndPointConfigurationService endPointConfigurationService,
                                         EndPointConfigurationInfoFactory endPointConfigurationInfoFactory,
                                         ExceptionFactory exceptionFactory,
                                         WebServicesService webServicesService,
                                         EndPointLogInfoFactory endpointConfigurationLogInfoFactory,
                                         ConcurrentModificationExceptionFactory concurrentModificationExceptionFactory,
                                         WebServiceCallOccurrenceInfoFactory endpointConfigurationOccurrenceInfoFactorty,
                                         ThreadPrincipalService threadPrincipalService,
                                         WebServiceCallOccurrenceService webServiceCallOccurrenceService) {
        super(endPointConfigurationService, exceptionFactory, endpointConfigurationLogInfoFactory, endpointConfigurationOccurrenceInfoFactorty,
                threadPrincipalService, webServiceCallOccurrenceService);
        this.endPointConfigurationInfoFactory = endPointConfigurationInfoFactory;
        this.webServicesService = webServicesService;
        this.concurrentModificationExceptionFactory = concurrentModificationExceptionFactory;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Transactional
    @RolesAllowed({Privileges.Constants.VIEW_WEB_SERVICES,
            Privileges.Constants.ADMINISTRATE_WEB_SERVICES,
            Privileges.Constants.RETRY_WEB_SERVICES,
            Privileges.Constants.CANCEL_WEB_SERVICES})
    public PagedInfoList getEndPointConfigurations(@BeanParam JsonQueryParameters queryParams,
                                                   @HeaderParam("X-CONNEXO-APPLICATION-NAME") String applicationName,
                                                   @Context UriInfo uriInfo) {
        Set<String> applicationNamesFilter = prepareApplicationNames(applicationName);
        Set<String> webServiceNames = webServicesService.getWebServices().stream()
                .filter(ws -> applicationNamesFilter.contains(ws.getApplicationName()))
                .map(WebService::getName)
                .collect(toSet());

        List<EndPointConfigurationInfo> infoList = webServiceNames.isEmpty() && !applicationNamesFilter.isEmpty() ?
                // had filter for applications, but found no related web services
                Collections.emptyList() :
                endPointConfigurationService.findEndPointConfigurations(webServiceNames)
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
    @RolesAllowed({Privileges.Constants.VIEW_WEB_SERVICES,
            Privileges.Constants.ADMINISTRATE_WEB_SERVICES,
            Privileges.Constants.RETRY_WEB_SERVICES,
            Privileges.Constants.CANCEL_WEB_SERVICES})
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
    @RolesAllowed({Privileges.Constants.VIEW_WEB_SERVICES, Privileges.Constants.ADMINISTRATE_WEB_SERVICES})
    public PagedInfoList getAllLogs(@PathParam("id") long id, @BeanParam JsonQueryParameters queryParameters) {
        EndPointConfiguration endPointConfiguration = endPointConfigurationService.getEndPointConfiguration(id)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_END_POINT_CONFIG));

        /* Logs contains actions not related to occurrence. */
        List<EndPointLogInfo> endpointConfigurationLogs = endPointConfiguration.getLogs()
                .from(queryParameters)
                .stream()
                .map(endpointConfigurationLogInfoFactory::from)
                .collect(toList());
        return PagedInfoList.fromPagedList("logs", endpointConfigurationLogs, queryParameters);
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
