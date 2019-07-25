/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.webservices.rest.impl;

import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointLog;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallOccurrence;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallOccurrenceService;
import com.elster.jupiter.soap.whiteboard.cxf.security.Privileges;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toList;

@Path("/occurrences")
public class WebServiceCallOccurrenceResource extends BaseResource {


    @Inject
    public WebServiceCallOccurrenceResource(EndPointConfigurationService endPointConfigurationService,
                                            ExceptionFactory exceptionFactory,
                                            EndPointLogInfoFactory endpointConfigurationLogInfoFactory,
                                            WebServiceCallOccurrenceInfoFactory endpointConfigurationOccurrenceInfoFactorty,
                                            ThreadPrincipalService threadPrincipalService,
                                            WebServiceCallOccurrenceService webServiceCallOccurrenceService) {
        super(endPointConfigurationService, exceptionFactory, endpointConfigurationLogInfoFactory, endpointConfigurationOccurrenceInfoFactorty,
                threadPrincipalService, webServiceCallOccurrenceService);
    }


    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Transactional
    @RolesAllowed(Privileges.Constants.VIEW_WEB_SERVICES)
    public PagedInfoList getAllOccurrences(@BeanParam JsonQueryParameters queryParameters,
                                           @BeanParam JsonQueryFilter filter,
                                           @HeaderParam("X-CONNEXO-APPLICATION-NAME") String applicationName,
                                           @Context UriInfo uriInfo) {
        String[] privileges = {Privileges.Constants.VIEW_WEB_SERVICES};
        checkApplicationPrivilegies(privileges, applicationName);

        Set<String> applicationNameToFilter = prepareApplicationNames(applicationName);

        List<WebServiceCallOccurrence> webServiceCallOccurrences = getWebServiceCallOccurrences(queryParameters, filter, applicationNameToFilter);
        List<WebServiceCallOccurrenceInfo> webServiceCallOccurrenceInfo = webServiceCallOccurrences
                .stream()
                .map(epco -> endpointConfigurationOccurrenceInfoFactorty.from(epco, uriInfo, false))
                .collect(toList());

        return PagedInfoList.fromPagedList("occurrences", webServiceCallOccurrenceInfo, queryParameters);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Path("/{id}")
    @Transactional
    @RolesAllowed(Privileges.Constants.VIEW_WEB_SERVICES)
    public WebServiceCallOccurrenceInfo getOccurrence(@PathParam("id") long id,
                                                      @HeaderParam("X-CONNEXO-APPLICATION-NAME") String applicationName,
                                                      @Context UriInfo uriInfo) {
        String[] privileges = {Privileges.Constants.VIEW_WEB_SERVICES};
        checkApplicationPrivilegies(privileges, applicationName);

        Optional<WebServiceCallOccurrence> epOcc = webServiceCallOccurrenceService.getEndPointOccurrence(id);

        return epOcc
                .map(epc -> endpointConfigurationOccurrenceInfoFactorty.from(epc, uriInfo, true))
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_OCCURRENCE));

    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Path("/{id}/retry")
    @Transactional
    @RolesAllowed(Privileges.Constants.RETRY_WEB_SERVICES)
    public Response retryOccurrence(@PathParam("id") long id,
                                    @HeaderParam("X-CONNEXO-APPLICATION-NAME") String applicationName) {

        String[] privileges = {Privileges.Constants.RETRY_WEB_SERVICES};
        checkApplicationPrivilegies(privileges, applicationName);

        Optional<WebServiceCallOccurrence> epOcc = webServiceCallOccurrenceService.getEndPointOccurrence(id);

        WebServiceCallOccurrence occurrence = epOcc.orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_OCCURRENCE));

        occurrence.retry();

        return Response.ok().build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Path("/{id}/log")
    @Transactional
    @RolesAllowed(Privileges.Constants.VIEW_WEB_SERVICES)
    public PagedInfoList getLogForOccurrence(@PathParam("id") long id,
                                             @HeaderParam("X-CONNEXO-APPLICATION-NAME") String applicationName,
                                             @BeanParam JsonQueryParameters queryParameters,
                                             @Context UriInfo uriInfo) {
        String[] privileges = {Privileges.Constants.VIEW_WEB_SERVICES};
        checkApplicationPrivilegies(privileges, applicationName);

        WebServiceCallOccurrence epOcc = webServiceCallOccurrenceService.getEndPointOccurrence(id)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_OCCURRENCE));

        List<EndPointLog> logs = getLogForOccurrence(epOcc, queryParameters);
        List<EndPointLogInfo> logsInfo = logs.stream().
                map(log -> endpointConfigurationLogInfoFactory.fullInfoFrom(log, uriInfo)).
                collect(toList());

        return PagedInfoList.fromPagedList("logs", logsInfo, queryParameters);

    }

}
