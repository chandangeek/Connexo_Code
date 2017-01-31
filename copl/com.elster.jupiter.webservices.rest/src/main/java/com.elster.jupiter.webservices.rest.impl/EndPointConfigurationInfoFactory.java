/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.webservices.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.IdWithLocalizedValueInfo;
import com.elster.jupiter.rest.util.LongIdWithNameInfo;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointAuthentication;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.soap.whiteboard.cxf.InboundEndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.LogLevel;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundEndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.WebService;
import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.UserService;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.Optional;

/**
 * Created by bvn on 6/8/16.
 */
public class EndPointConfigurationInfoFactory {
    private final Thesaurus thesaurus;
    private final EndPointConfigurationService endPointConfigurationService;
    private final UserService userService;
    private final ExceptionFactory exceptionFactory;
    private final WebServicesService webServicesService;

    @Inject
    public EndPointConfigurationInfoFactory(Thesaurus thesaurus, EndPointConfigurationService endPointConfigurationService, UserService userService, ExceptionFactory exceptionFactory, WebServicesService webServicesService) {
        this.thesaurus = thesaurus;
        this.endPointConfigurationService = endPointConfigurationService;
        this.userService = userService;
        this.exceptionFactory = exceptionFactory;
        this.webServicesService = webServicesService;
    }

    public EndPointConfigurationInfo from(EndPointConfiguration endPointConfiguration, UriInfo uriInfo) {
        Optional<WebService> webService = webServicesService.getWebService(endPointConfiguration.getWebServiceName());
        EndPointConfigurationInfo info = new EndPointConfigurationInfo();
        info.id = endPointConfiguration.getId();
        info.name = endPointConfiguration.getName();
        info.version = endPointConfiguration.getVersion();
        info.url = endPointConfiguration.getUrl();
        info.active = endPointConfiguration.isActive();
        info.available = webService.isPresent();
        info.webServiceName = endPointConfiguration.getWebServiceName();
        info.logLevel = new IdWithLocalizedValueInfo<>(endPointConfiguration.getLogLevel()
                .name(), endPointConfiguration.getLogLevel()
                .getDisplayName(thesaurus));
        info.httpCompression = endPointConfiguration.isHttpCompression();
        info.tracing = endPointConfiguration.isTracing();
        info.traceFile = endPointConfiguration.getTraceFile();
        info.schemaValidation = endPointConfiguration.isSchemaValidation();
        if(webService.isPresent()){
            info.type = webService.get().getProtocol().name();
        }
        info.authenticationMethod = new IdWithLocalizedValueInfo<>(endPointConfiguration
                .getAuthenticationMethod(),
                endPointConfiguration.getAuthenticationMethod()
                        .getDisplayName(thesaurus));
        if (endPointConfiguration.isInbound()) {
            info.direction = new IdWithLocalizedValueInfo<>(WebServiceDirection.INBOUND, WebServiceDirection.INBOUND.getDisplayName(thesaurus));
            webService.ifPresent(ws -> info.previewUrl = uriInfo.getBaseUri().getScheme() + "://" + uriInfo.getBaseUri()
                    .getAuthority()
                    + "/" + ws.getProtocol().path()
                    + endPointConfiguration.getUrl());
            ((InboundEndPointConfiguration) endPointConfiguration).getGroup()
                    .ifPresent(g -> info.group = new LongIdWithNameInfo(g.getId(), g.getName()));
        } else {
            info.direction = new IdWithLocalizedValueInfo<>(WebServiceDirection.OUTBOUND, WebServiceDirection.OUTBOUND.getDisplayName(thesaurus));
            info.username = ((OutboundEndPointConfiguration) endPointConfiguration).getUsername();
            info.password = ((OutboundEndPointConfiguration) endPointConfiguration).getPassword();
        }
        return info;
    }

    public EndPointConfiguration createInboundEndPointConfiguration(EndPointConfigurationInfo info) {
        EndPointConfigurationService.InboundEndPointConfigBuilder builder = endPointConfigurationService
                .newInboundEndPointConfiguration(info.name, info.webServiceName, info.url);
        if (info.logLevel != null && info.logLevel.id != null) {
            builder.logLevel(LogLevel.valueOf(info.logLevel.id));
        }
        if (Boolean.TRUE.equals(info.httpCompression)) {
            builder.httpCompression();
        }
        if (Boolean.TRUE.equals(info.schemaValidation)) {
            builder.schemaValidation();
        }
        if (Boolean.TRUE.equals(info.tracing)) {
            builder.tracing();
        }
        builder.setAuthenticationMethod(info.authenticationMethod.id);
        if (EndPointAuthentication.BASIC_AUTHENTICATION.equals(info.authenticationMethod.id)) {
            if (info.group != null && info.group.id != null) {
                Group group = userService.getGroup(info.group.id)
                        .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.BAD_REQUEST, MessageSeeds.NO_SUCH_GROUP));
                builder.group(group);
            }
        }
        builder.traceFile(info.traceFile);
        EndPointConfiguration endPointConfiguration = builder.create();
        if (Boolean.TRUE.equals(info.active)) {
            endPointConfigurationService.activate(endPointConfiguration);
        }

        return endPointConfiguration;
    }

    public EndPointConfiguration createOutboundEndPointConfiguration(EndPointConfigurationInfo info) {
        EndPointConfigurationService.OutboundEndPointConfigBuilder builder = endPointConfigurationService
                .newOutboundEndPointConfiguration(info.name, info.webServiceName, info.url);
        if (info.logLevel != null && info.logLevel.id != null) {
            builder.logLevel(LogLevel.valueOf(info.logLevel.id));
        }
        if (Boolean.TRUE.equals(info.httpCompression)) {
            builder.httpCompression();
        }
        if (Boolean.TRUE.equals(info.schemaValidation)) {
            builder.schemaValidation();
        }
        if (Boolean.TRUE.equals(info.tracing)) {
            builder.tracing();
        }
        builder.username(info.username);
        builder.password(info.password);
        builder.setAuthenticationMethod(info.authenticationMethod.id);
        builder.traceFile(info.traceFile);
        EndPointConfiguration endPointConfiguration = builder.create();
        if (Boolean.TRUE.equals(info.active)) {
            endPointConfigurationService.activate(endPointConfiguration);
        }

        return endPointConfiguration;
    }

    public EndPointConfiguration updateEndPointConfiguration(OutboundEndPointConfiguration endPointConfiguration, EndPointConfigurationInfo info) {
        this.applyCommonChanges(endPointConfiguration, info);
        endPointConfiguration.setPassword(info.password);
        endPointConfiguration.setUsername(info.username);
        return endPointConfiguration;
    }

    public EndPointConfiguration updateEndPointConfiguration(InboundEndPointConfiguration endPointConfiguration, EndPointConfigurationInfo info) {
        this.applyCommonChanges(endPointConfiguration, info);
        if (EndPointAuthentication.BASIC_AUTHENTICATION.equals(info.authenticationMethod.id)) {
            if (info.group == null || info.group.id == null) {
                endPointConfiguration.setGroup(null);
            } else {
                Group group = userService.getGroup(info.group.id)
                        .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_GROUP));
                endPointConfiguration.setGroup(group);
            }
        }
        return endPointConfiguration;
    }

    private void applyCommonChanges(EndPointConfiguration endPointConfiguration, EndPointConfigurationInfo info) {
        endPointConfiguration.setName(info.name);
        endPointConfiguration.setAuthenticationMethod(info.authenticationMethod.id);
        endPointConfiguration.setUrl(info.url);
        endPointConfiguration.setWebServiceName(info.webServiceName);
        endPointConfiguration.setSchemaValidation(info.schemaValidation);
        endPointConfiguration.setHttpCompression(info.httpCompression);
        endPointConfiguration.setLogLevel(LogLevel.valueOf(info.logLevel.id));
        endPointConfiguration.setTracing(info.tracing);
        endPointConfiguration.setTraceFile(info.traceFile);
    }
}
