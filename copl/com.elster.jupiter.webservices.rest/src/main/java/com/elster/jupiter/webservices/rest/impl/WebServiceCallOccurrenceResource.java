/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.webservices.rest.impl;

import com.elster.jupiter.domain.util.Finder;
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
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallRelatedAttribute;
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@Path("/occurrences")
public class WebServiceCallOccurrenceResource extends BaseResource {

    private static final int XML_INDENT = 4;

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
    @RolesAllowed({Privileges.Constants.VIEW_WEB_SERVICES, Privileges.Constants.VIEW_HISTORY_WEB_SERVICES, Privileges.Constants.ADMINISTRATE_WEB_SERVICES})
    public PagedInfoList getAllOccurrences(@BeanParam JsonQueryParameters queryParameters,
                                           @BeanParam JsonQueryFilter filter,
                                           @HeaderParam("X-CONNEXO-APPLICATION-NAME") String applicationName,
                                           @Context UriInfo uriInfo) {
        checkApplicationPrivileges(applicationName, Privileges.Constants.VIEW_HISTORY_WEB_SERVICES, Privileges.Constants.VIEW_WEB_SERVICES, Privileges.Constants.ADMINISTRATE_WEB_SERVICES);

        Set<String> applicationNameToFilter = prepareApplicationNames(applicationName);

        List<WebServiceCallOccurrence> webServiceCallOccurrences = getWebServiceCallOccurrences(queryParameters, filter, applicationNameToFilter);
        List<WebServiceCallOccurrenceInfo> webServiceCallOccurrenceInfo = webServiceCallOccurrences
                .stream()
                .map(epco -> endpointConfigurationOccurrenceInfoFactorty.from(epco, uriInfo))
                .collect(toList());

        return PagedInfoList.fromPagedList("occurrences", webServiceCallOccurrenceInfo, queryParameters);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Path("/{id}")
    @Transactional
    @RolesAllowed({Privileges.Constants.VIEW_WEB_SERVICES, Privileges.Constants.VIEW_HISTORY_WEB_SERVICES, Privileges.Constants.ADMINISTRATE_WEB_SERVICES})
    public WebServiceCallOccurrenceInfo getOccurrence(@PathParam("id") long id,
                                                      @HeaderParam("X-CONNEXO-APPLICATION-NAME") String applicationName,
                                                      @Context UriInfo uriInfo) {
        checkApplicationPrivileges(applicationName, Privileges.Constants.VIEW_HISTORY_WEB_SERVICES, Privileges.Constants.VIEW_WEB_SERVICES, Privileges.Constants.ADMINISTRATE_WEB_SERVICES);

        Optional<WebServiceCallOccurrence> epOcc = webServiceCallOccurrenceService.getWebServiceCallOccurrence(id);

        return epOcc
                .map(epc -> endpointConfigurationOccurrenceInfoFactorty.from(epc, uriInfo))
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_OCCURRENCE));

    }

    @GET
    @Produces(MediaType.APPLICATION_XML + "; charset=UTF-8")
    @Path("/{id}/payload")
    @Transactional
    @RolesAllowed({Privileges.Constants.VIEW_WEB_SERVICES, Privileges.Constants.VIEW_HISTORY_WEB_SERVICES, Privileges.Constants.ADMINISTRATE_WEB_SERVICES})
    public String getPayload(@PathParam("id") long id,
                             @HeaderParam("X-CONNEXO-APPLICATION-NAME") String applicationName,
                             @Context UriInfo uriInfo) {
        checkApplicationPrivileges(applicationName, Privileges.Constants.VIEW_HISTORY_WEB_SERVICES, Privileges.Constants.VIEW_WEB_SERVICES, Privileges.Constants.ADMINISTRATE_WEB_SERVICES);

        Optional<WebServiceCallOccurrence> epOcc = webServiceCallOccurrenceService.getWebServiceCallOccurrence(id);
        return epOcc.orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_OCCURRENCE))
                .getPayload()
                .map(payload -> endpointConfigurationOccurrenceInfoFactorty.formatXml(payload, XML_INDENT))
                .orElse(null);
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Path("/{id}/retry")
    @Transactional
    @RolesAllowed(Privileges.Constants.RETRY_WEB_SERVICES)
    public Response retryOccurrence(@PathParam("id") long id,
                                    @HeaderParam("X-CONNEXO-APPLICATION-NAME") String applicationName) {
        checkApplicationPrivileges(applicationName, Privileges.Constants.RETRY_WEB_SERVICES);

        Optional<WebServiceCallOccurrence> epOcc = webServiceCallOccurrenceService.getWebServiceCallOccurrence(id);

        WebServiceCallOccurrence occurrence = epOcc.orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_OCCURRENCE));

        occurrence.retry();

        return Response.ok().build();
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Path("/{id}/cancel")
    @Transactional
    @RolesAllowed(Privileges.Constants.CANCEL_WEB_SERVICES)
    public Response cancelOccurrence(@PathParam("id") long id,
                                     @HeaderParam("X-CONNEXO-APPLICATION-NAME") String applicationName) {
        checkApplicationPrivileges(applicationName, Privileges.Constants.CANCEL_WEB_SERVICES);

        Optional<WebServiceCallOccurrence> epOcc = webServiceCallOccurrenceService.getWebServiceCallOccurrence(id);

        WebServiceCallOccurrence occurrence = epOcc.orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_OCCURRENCE));

        occurrence.cancel();

        return Response.ok().build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Path("/{id}/log")
    @Transactional
    @RolesAllowed({Privileges.Constants.VIEW_WEB_SERVICES, Privileges.Constants.VIEW_HISTORY_WEB_SERVICES, Privileges.Constants.ADMINISTRATE_WEB_SERVICES})
    public PagedInfoList getLogForOccurrence(@PathParam("id") long id,
                                             @HeaderParam("X-CONNEXO-APPLICATION-NAME") String applicationName,
                                             @BeanParam JsonQueryParameters queryParameters,
                                             @Context UriInfo uriInfo) {
        checkApplicationPrivileges(applicationName, Privileges.Constants.VIEW_HISTORY_WEB_SERVICES, Privileges.Constants.VIEW_WEB_SERVICES, Privileges.Constants.ADMINISTRATE_WEB_SERVICES);

        WebServiceCallOccurrence epOcc = webServiceCallOccurrenceService.getWebServiceCallOccurrence(id)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_OCCURRENCE));

        List<EndPointLog> logs = getLogForOccurrence(epOcc, queryParameters);
        List<EndPointLogInfo> logsInfo = logs.stream().
                map(log -> endpointConfigurationLogInfoFactory.fullInfoFrom(log, uriInfo)).
                collect(toList());

        return PagedInfoList.fromPagedList("logs", logsInfo, queryParameters);

    }

    @GET
    @Path("/relatedattributes")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_WEB_SERVICES, Privileges.Constants.VIEW_HISTORY_WEB_SERVICES, Privileges.Constants.ADMINISTRATE_WEB_SERVICES})
    public PagedInfoList getRelatedAttributes(@BeanParam JsonQueryParameters params) {
        String searchText = params.getLike();
        String txtToFind = null;
        final String translationTxt;

        if (searchText == null) {
            searchText = "";
        }

        if (searchText.contains("(") && searchText.contains(")")) {
            txtToFind = searchText.substring(0, searchText.lastIndexOf("(") - 1);
            translationTxt = searchText.substring(searchText.lastIndexOf("(") + 1, searchText.length() - 1);
        } else {
            translationTxt = null;
        }

        String toFind = txtToFind == null ? searchText.trim() : txtToFind.trim();
        Finder<WebServiceCallRelatedAttribute> finder = webServiceCallOccurrenceService
                .getRelatedAttributesByValueLike(toFind);

        List<WebServiceCallRelatedAttribute> listRelatedObjects = finder.find();
        Stream<WebServiceCallRelatedAttribute> streamInfo = listRelatedObjects.stream()
                .sorted(Comparator.comparingInt((WebServiceCallRelatedAttribute obj) -> obj.getValue().length())
                        .thenComparingInt(obj -> obj.getValue().toLowerCase().indexOf(toFind.toLowerCase()))
                        .thenComparing(WebServiceCallRelatedAttribute::getValue))
                .filter(obj -> translationTxt == null || webServiceCallOccurrenceService.translateAttributeType(obj.getKey()).equals(translationTxt));
        // CONM-1728 TODO: move sorting & pagination to query level
        streamInfo = params.getStart().map(streamInfo::skip).orElse(streamInfo);
        streamInfo = params.getLimit().map(i -> i + 1).map(streamInfo::limit).orElse(streamInfo);
        List<RelatedAttributeInfo> listInfo = streamInfo
                .map(obj -> {
                    return new RelatedAttributeInfo(obj.getId(),
                            obj.getValue() + " (" + webServiceCallOccurrenceService.translateAttributeType(obj.getKey()) + ")");
                }).collect(toList());

        return PagedInfoList.fromPagedList("relatedattributes", listInfo, params);
    }

    @GET
    @Path("/relatedattributes/{id}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_WEB_SERVICES, Privileges.Constants.VIEW_HISTORY_WEB_SERVICES, Privileges.Constants.ADMINISTRATE_WEB_SERVICES})
    public Response getRelatedAttributeById(@BeanParam JsonQueryParameters params,
                                            @PathParam("id") long id) {

        Optional<WebServiceCallRelatedAttribute> relatedObject = webServiceCallOccurrenceService.getRelatedObjectById(id);

        RelatedAttributeInfo info = new RelatedAttributeInfo(relatedObject.get().getId(),
                relatedObject.get().getValue() + " (" + webServiceCallOccurrenceService.translateAttributeType(relatedObject.get().getKey()) + ")");
        /* Extjs parse response as list of elements. So here send list with one element */
        List<RelatedAttributeInfo> listInfo = new ArrayList<>();
        listInfo.add(info);

        return Response.ok().entity(listInfo).build();
    }

}
