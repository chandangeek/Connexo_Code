/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.webservices.rest.impl;

import com.elster.jupiter.orm.QueryStream;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.soap.whiteboard.cxf.ApplicationSpecific;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointLog;
import com.elster.jupiter.soap.whiteboard.cxf.OccurrenceLogFinderBuilder;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallOccurrence;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallOccurrenceFinderBuilder;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallOccurrenceService;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallOccurrenceStatus;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallRelatedAttribute;
import com.elster.jupiter.users.User;

import com.google.common.collect.Range;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.security.Principal;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.conditions.Where.where;

public abstract class BaseResource {

    protected final EndPointConfigurationService endPointConfigurationService;
    protected final ExceptionFactory exceptionFactory;
    protected final EndPointLogInfoFactory endpointConfigurationLogInfoFactory;
    protected final WebServiceCallOccurrenceInfoFactory endpointConfigurationOccurrenceInfoFactorty;
    protected final ThreadPrincipalService threadPrincipalService;
    protected final WebServiceCallOccurrenceService webServiceCallOccurrenceService;

    public BaseResource(EndPointConfigurationService endPointConfigurationService,
                        ExceptionFactory exceptionFactory,
                        EndPointLogInfoFactory endpointConfigurationLogInfoFactory,
                        WebServiceCallOccurrenceInfoFactory endpointConfigurationOccurrenceInfoFactorty,
                        ThreadPrincipalService threadPrincipalService,
                        WebServiceCallOccurrenceService webServiceCallOccurrenceService) {
        this.endPointConfigurationService = endPointConfigurationService;
        this.exceptionFactory = exceptionFactory;
        this.endpointConfigurationLogInfoFactory = endpointConfigurationLogInfoFactory;
        this.endpointConfigurationOccurrenceInfoFactorty = endpointConfigurationOccurrenceInfoFactorty;
        this.threadPrincipalService = threadPrincipalService;
        this.webServiceCallOccurrenceService = webServiceCallOccurrenceService;
    }

    protected void checkApplicationPrivileges(String applicationName, String... privilegeNames) {
        if (privilegeNames == null || privilegeNames.length == 0 || applicationName == null) {
            throw new WebApplicationException(Response.Status.FORBIDDEN);
        }
        Principal principal = threadPrincipalService.getPrincipal();
        if (principal == null) {
            throw new WebApplicationException(Response.Status.FORBIDDEN);
        }
        Optional<String> appPrivilege = Arrays.stream(privilegeNames)
                .filter(priv -> ((User) principal).hasPrivilege(applicationName, priv))
                .findAny();
        if (!appPrivilege.isPresent()) {
            throw new WebApplicationException(Response.Status.FORBIDDEN);
        }
    }

    protected Set<String> prepareApplicationNames(String applicationName) {
        Set<String> applicationNames = new HashSet<>();
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

    protected List<EndPointLog> getLogForOccurrence(WebServiceCallOccurrence epOcc, JsonQueryParameters queryParameters) {
        OccurrenceLogFinderBuilder finderBuilder = webServiceCallOccurrenceService.getOccurrenceLogFinderBuilder();

        finderBuilder.withOccurrence(epOcc);

        return finderBuilder.build().from(queryParameters).find();
    }

    protected List<WebServiceCallOccurrence> getWebServiceCallOccurrences(JsonQueryParameters queryParameters,
                                                                          JsonQueryFilter filter,
                                                                          Set<String> applicationNames) {
        WebServiceCallOccurrenceFinderBuilder finderBuilder = webServiceCallOccurrenceService.getWebServiceCallOccurrenceFinderBuilder();

        if (applicationNames != null && !applicationNames.isEmpty()) {
            finderBuilder.withApplicationNames(applicationNames);
        }

        if (filter.hasProperty("startedOnFrom")) {
            if (filter.hasProperty("startedOnTo")) {
                finderBuilder.withStartTimeIn(Range.closed(filter.getInstant("startedOnFrom"), filter.getInstant("startedOnTo")));
            } else {
                finderBuilder.withStartTimeIn(Range.atLeast(filter.getInstant("startedOnFrom")));
            }
        } else if (filter.hasProperty("startedOnTo")) {
            finderBuilder.withStartTimeIn(Range.atMost(filter.getInstant("startedOnTo")));
        }
        if (filter.hasProperty("finishedOnFrom")) {
            if (filter.hasProperty("finishedOnTo")) {
                finderBuilder.withEndTimeIn(Range.closed(filter.getInstant("finishedOnFrom"), filter.getInstant("finishedOnTo")));
            } else {
                finderBuilder.withEndTimeIn(Range.atLeast(filter.getInstant("finishedOnFrom")));
            }
        } else if (filter.hasProperty("finishedOnTo")) {
            finderBuilder.withEndTimeIn(Range.atMost(filter.getInstant("finishedOnTo")));
        }
        /* Find endpoints by ID */
        if (filter.hasProperty("webServiceEndPoint")) {
            try (QueryStream<EndPointConfiguration> epcSet = endPointConfigurationService.streamEndPointConfigurations()
                    .filter(where("id").in(filter.getLongList("webServiceEndPoint")))) {
                finderBuilder.withEndPointConfigurations(epcSet.collect(Collectors.toSet()));
            }
        }

        if (filter.hasProperty("status")) {
            finderBuilder.withStatuses(filter.getStringList("status")
                    .stream()
                    .map(WebServiceCallOccurrenceStatus::fromString)
                    .collect(Collectors.toSet()));
        }

        if (filter.hasProperty("type")) {
            List<String> typeList = filter.getStringList("type");
            if (typeList.contains("INBOUND") && !typeList.contains("OUTBOUND")) {
                finderBuilder.onlyInbound();
            } else if (typeList.contains("OUTBOUND") && !typeList.contains("INBOUND")) {
                finderBuilder.onlyOutbound();
            }
        }

        if (filter.hasProperty("wsRelatedObjectId")) {
            long objectId = -1;
            String objectIdStr = null;
            /* wsRelatedObjectId value from FE can be received as number for example 123 or string as "123".
             * It is some ExtJS specific */
            try {
                objectId = filter.getInteger("wsRelatedObjectId");
            } catch (NullPointerException e) {
                objectIdStr = filter.getString("wsRelatedObjectId");
                try {
                    objectId = Long.parseLong(objectIdStr);
                    objectIdStr = null;
                } catch (NumberFormatException ex) {
                    objectId = -1;
                }
            }

            if (objectId != -1) {
                Optional<WebServiceCallRelatedAttribute> wscRo = webServiceCallOccurrenceService.getRelatedObjectById(objectId);
                if (wscRo.isPresent()) {
                    finderBuilder.withRelatedAttribute(wscRo.get());
                } else {
                    return Collections.emptyList();
                }
            }

            if (objectIdStr != null) {
                String txtToFind = null;
                final String translationTxt;

                if (objectIdStr.contains("(") && objectIdStr.contains(")")) {
                    txtToFind = objectIdStr.substring(0, objectIdStr.lastIndexOf("(") - 1);
                    translationTxt = objectIdStr.substring(objectIdStr.lastIndexOf("(") + 1, objectIdStr.length() - 1);
                } else {
                    translationTxt = null;
                }

                if (translationTxt == null) {
                    /* No related attribute is specified. So just return empty list */
                    return Collections.emptyList();
                }

                List<WebServiceCallRelatedAttribute> wscRattrList = webServiceCallOccurrenceService.getRelatedAttributesByValue(txtToFind.trim());

                Optional<WebServiceCallRelatedAttribute> wecRattribute = wscRattrList.stream().
                        filter(attr -> webServiceCallOccurrenceService.translateAttributeType(attr.getKey()).equals(translationTxt)).findFirst();

                if (wecRattribute.isPresent()) {
                    finderBuilder.withRelatedAttribute(wecRattribute.get());
                } else {
                    return Collections.emptyList();
                }
            }
        }

        return finderBuilder.build()
                .from(queryParameters)
                .find();
    }
}
