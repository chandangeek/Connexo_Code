/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.webservices.rest.impl;

import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.rest.util.ConcurrentModificationExceptionFactory;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.soap.whiteboard.cxf.ApplicationSpecific;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointLog;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallOccurrence;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallOccurrenceService;
import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;
import com.elster.jupiter.soap.whiteboard.cxf.security.Privileges;
import com.elster.jupiter.users.Privilege;
import com.elster.jupiter.users.User;
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
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.security.Principal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import com.elster.jupiter.orm.OrmService;

/**
 * Resource to manage end point configurations
 */
@Path("/endpointconfigurations")
public class EndPointConfigurationResource {

    private final EndPointConfigurationService endPointConfigurationService;
    private final EndPointConfigurationInfoFactory endPointConfigurationInfoFactory;
    private final ExceptionFactory exceptionFactory;
    private final WebServicesService webServicesService;
    private final WebServiceCallOccurrenceLogInfoFactory endpointConfigurationLogInfoFactory;
    private final ConcurrentModificationExceptionFactory concurrentModificationExceptionFactory;
    private final WebServiceCallOccurrenceInfoFactory endpointConfigurationOccurrenceInfoFactorty;
    private final OrmService ormService;
    private final ThreadPrincipalService threadPrincipalService;
    private final WebServiceCallOccurrenceService webServiceCallOccurrenceService;

    @Inject
    public EndPointConfigurationResource(EndPointConfigurationService endPointConfigurationService,
                                         EndPointConfigurationInfoFactory endPointConfigurationInfoFactory,
                                         ExceptionFactory exceptionFactory,
                                         WebServicesService webServicesService,
                                         WebServiceCallOccurrenceLogInfoFactory endpointConfigurationLogInfoFactory,
                                         ConcurrentModificationExceptionFactory concurrentModificationExceptionFactory,
                                         WebServiceCallOccurrenceInfoFactory endpointConfigurationOccurrenceInfoFactorty,
                                         OrmService ormService,
                                         ThreadPrincipalService threadPrincipalService,
                                         WebServiceCallOccurrenceService webServiceCallOccurrenceService) {
        this.endPointConfigurationService = endPointConfigurationService;
        this.endPointConfigurationInfoFactory = endPointConfigurationInfoFactory;
        this.exceptionFactory = exceptionFactory;
        this.webServicesService = webServicesService;
        this.endpointConfigurationLogInfoFactory = endpointConfigurationLogInfoFactory;
        this.concurrentModificationExceptionFactory = concurrentModificationExceptionFactory;
        this.endpointConfigurationOccurrenceInfoFactorty = endpointConfigurationOccurrenceInfoFactorty;
        this.ormService = ormService;
        this.threadPrincipalService = threadPrincipalService;
        this.webServiceCallOccurrenceService = webServiceCallOccurrenceService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Transactional
    @RolesAllowed(Privileges.Constants.VIEW_WEB_SERVICES)
    public PagedInfoList getEndPointConfigurations(@BeanParam JsonQueryParameters queryParams,
                                                   @HeaderParam("X-CONNEXO-APPLICATION-NAME") String applicationName,
                                                   @Context UriInfo uriInfo) {

        List<EndPointConfigurationInfo> infoList;
        if ("SYS".equals(applicationName)){
            infoList = endPointConfigurationService.findEndPointConfigurations()
                    .from(queryParams)
                    .stream()
                    .map(epc -> endPointConfigurationInfoFactory.from(epc, uriInfo))
                    .collect(toList());
        }else{
            Set<String> applicationNameToFilter = prepareApplicationNames(applicationName);

            Set<String> webServiceNames = webServicesService.getWebServices().stream()
                    .filter(ws -> applicationNameToFilter.contains(ws.getApplicationName()))
                    .map(ws -> ws.getName())
                    .collect(toSet());

            infoList = endPointConfigurationService.findEndPointConfigurations()
                    .from(queryParams)
                    .stream()
                    .filter(epc -> webServiceNames.contains(epc.getWebServiceName()))
                    .map(epc -> endPointConfigurationInfoFactory.from(epc, uriInfo))
                    .collect(toList());
        }


        return PagedInfoList.fromPagedList("endpoints", infoList, queryParams);
    }

    private Set<String> prepareApplicationNames(String applicationName){
        Set<String> applicationNames = new HashSet();
        switch (applicationName) {
            case "SYS":
                applicationNames.add(ApplicationSpecific.WebServiceApplicationName.MULTISENSE_INSIGHT.getName());
                applicationNames.add(ApplicationSpecific.WebServiceApplicationName.MULTISENSE.getName());
                applicationNames.add(ApplicationSpecific.WebServiceApplicationName.INSIGHT.getName());
                break;
            case "MDC":
                applicationNames.add(ApplicationSpecific.WebServiceApplicationName.MULTISENSE.getName());
                applicationNames.add(ApplicationSpecific.WebServiceApplicationName.MULTISENSE_INSIGHT.getName());
                break;
            case "INS":
                applicationNames.add(ApplicationSpecific.WebServiceApplicationName.INSIGHT.getName());
                applicationNames.add(ApplicationSpecific.WebServiceApplicationName.MULTISENSE_INSIGHT.getName());
                break;
            default:
                throw exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.INCORRECT_APPLICATION_NAME).get();
        }
        return applicationNames;
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
        List<WebServiceCallOccurrenceLogInfo> endpointConfigurationLogs = endPointConfiguration.getLogs()
                .from(queryParameters)
                .stream()
                .map(endpointConfigurationLogInfoFactory::from)
                .collect(toList());
        return PagedInfoList.fromPagedList("logs", endpointConfigurationLogs, queryParameters);
    }


    private void checkApplicationPriviliges(String[] priviligeNames, String applicationName) {
        /*TODO add check for name. IF it is not specified then return 403 */
        Principal principal = threadPrincipalService.getPrincipal();
        List privilegies = Arrays.asList(priviligeNames);
        Set<Privilege> appPrivilegies = ((User) principal).getPrivileges(applicationName);
        Optional<Privilege> neededPrivilege = appPrivilegies.stream()
                .filter(privilege -> privilegies.contains(privilege.getName()))
                .findFirst();

        if (!neededPrivilege.isPresent()) {
            throw new WebApplicationException(Response.Status.FORBIDDEN);
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Path("/occurrences")
    @RolesAllowed(Privileges.Constants.VIEW_WEB_SERVICES)
    public PagedInfoList getAllOccurrences(@BeanParam JsonQueryParameters queryParameters,
                                           @BeanParam JsonQueryFilter filter,
                                           @HeaderParam("X-CONNEXO-APPLICATION-NAME") String applicationName,
                                           @Context UriInfo uriInfo) {
        String[] privileges = {Privileges.Constants.VIEW_WEB_SERVICES};
        checkApplicationPriviliges(privileges, applicationName);

        Set<String> applicationNameToFilter = prepareApplicationNames(applicationName);

        List<WebServiceCallOccurrence> endPointOccurrences = webServiceCallOccurrenceService.getEndPointOccurrences(queryParameters, filter, applicationNameToFilter, null);
        List<WebServiceCallOccurrenceInfo> webServiceCallOccurrenceInfo = endPointOccurrences.
                                                         stream().
                                                         map(epco -> endpointConfigurationOccurrenceInfoFactorty.from(epco, uriInfo)).
                                                         collect(toList());

        return PagedInfoList.fromPagedList("occurrences", webServiceCallOccurrenceInfo , queryParameters);
    }


    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Path("/occurrences/{id}")
    @RolesAllowed(Privileges.Constants.VIEW_WEB_SERVICES)
    public WebServiceCallOccurrenceInfo getOccurrence(@PathParam("id") long id,
                                                      @HeaderParam("X-CONNEXO-APPLICATION-NAME") String applicationName,
                                                      @Context UriInfo uriInfo) {
        String[] privileges = {Privileges.Constants.VIEW_WEB_SERVICES};
        checkApplicationPriviliges(privileges, applicationName);

        Optional<WebServiceCallOccurrence> epOcc = webServiceCallOccurrenceService.getEndPointOccurrence(id);

        return epOcc
               .map(epc -> endpointConfigurationOccurrenceInfoFactorty.from(epc, uriInfo))
               .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_OCCURRENCE));

    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Path("/{epId}/occurrences")
    @RolesAllowed(Privileges.Constants.VIEW_WEB_SERVICES)
    public PagedInfoList getAllOccurrencesForEndPoint(@PathParam("epId") long epId,
                                                      @BeanParam JsonQueryParameters queryParameters,
                                                      @BeanParam JsonQueryFilter filter,
                                                      @HeaderParam("X-CONNEXO-APPLICATION-NAME") String applicationName,
                                                      @Context UriInfo uriInfo) {
        String[] privileges = {Privileges.Constants.VIEW_WEB_SERVICES};
        checkApplicationPriviliges(privileges, applicationName);

        Set<String> applicationNameToFilter = prepareApplicationNames(applicationName);

        List<WebServiceCallOccurrence> endPointOccurrences = webServiceCallOccurrenceService.getEndPointOccurrences(queryParameters, filter, applicationNameToFilter, epId);
        List<WebServiceCallOccurrenceInfo> endPointOccurrencesInfo = endPointOccurrences.
                stream().
                map(epco -> endpointConfigurationOccurrenceInfoFactorty.from(epco, uriInfo)).
                collect(toList());

        return PagedInfoList.fromPagedList("occurrences", endPointOccurrencesInfo, queryParameters);
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Path("/occurrences/{id}/retry")
    @Transactional
    @RolesAllowed(Privileges.Constants.RETRY_WEB_SERVICES)
    public Response retryOccurrence(@PathParam("id") long id,
                                    @HeaderParam("X-CONNEXO-APPLICATION-NAME") String applicationName) {

        String[] privileges = {Privileges.Constants.RETRY_WEB_SERVICES};
        checkApplicationPriviliges(privileges, applicationName);

        Optional<WebServiceCallOccurrence> epOcc = webServiceCallOccurrenceService.getEndPointOccurrence(id);

        WebServiceCallOccurrence occurrence = epOcc.orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_OCCURRENCE));

        occurrence.retry();

        return Response.ok().build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Path("/occurrences/{id}/log")
    @RolesAllowed(Privileges.Constants.VIEW_WEB_SERVICES)
    public PagedInfoList getLogForOccurrence(@PathParam("id") long id,
                                             @HeaderParam("X-CONNEXO-APPLICATION-NAME") String applicationName,
                                             @BeanParam JsonQueryParameters queryParameters,
                                             @Context UriInfo uriInfo) {
        String[] privileges = {Privileges.Constants.VIEW_WEB_SERVICES};
        checkApplicationPriviliges(privileges, applicationName);

        List<EndPointLog> logs = webServiceCallOccurrenceService.getLogForOccurrence(id, queryParameters);
        List<WebServiceCallOccurrenceLogInfo> logsInfo = logs.stream().
                                                      map(log -> endpointConfigurationLogInfoFactory.fromFull(log, uriInfo)).
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
