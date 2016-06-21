package com.elster.jupiter.webservices.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.IdWithLocalizedValueInfo;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.soap.whiteboard.cxf.InboundEndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.LogLevel;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundEndPointConfiguration;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.UserService;

import javax.inject.Inject;

/**
 * Created by bvn on 6/8/16.
 */
public class EndPointConfigurationInfoFactory {
    private final Thesaurus thesaurus;
    private final EndPointConfigurationService endPointConfigurationService;
    private final UserService userService;
    private final ExceptionFactory exceptionFactory;

    @Inject
    public EndPointConfigurationInfoFactory(Thesaurus thesaurus, EndPointConfigurationService endPointConfigurationService, UserService userService, ExceptionFactory exceptionFactory) {
        this.thesaurus = thesaurus;
        this.endPointConfigurationService = endPointConfigurationService;
        this.userService = userService;
        this.exceptionFactory = exceptionFactory;
    }

    public EndPointConfigurationInfo from(EndPointConfiguration endPointConfiguration) {
        EndPointConfigurationInfo info = new EndPointConfigurationInfo();
        info.id = endPointConfiguration.getId();
        info.name = endPointConfiguration.getName();
        info.version = endPointConfiguration.getVersion();
        info.url = endPointConfiguration.getUrl();
        info.active = endPointConfiguration.isActive();
        info.webServiceName = endPointConfiguration.getWebServiceName();
        info.logLevel = new IdWithLocalizedValueInfo<>(endPointConfiguration.getLogLevel()
                .name(), endPointConfiguration.getLogLevel()
                .getDisplayName(thesaurus));
        info.httpCompression = endPointConfiguration.isHttpCompression();
        info.tracing = endPointConfiguration.isTracing();
        info.traceFile = endPointConfiguration.getTraceFile();
        info.schemaValidation = endPointConfiguration.isSchemaValidation();
        info.authenticationMethod = new IdWithLocalizedValueInfo<>(endPointConfiguration
                .getAuthenticationMethod(),
                endPointConfiguration.getAuthenticationMethod()
                        .getDisplayName(thesaurus));
        if (InboundEndPointConfiguration.class.isAssignableFrom(endPointConfiguration.getClass())) {
            info.direction = new IdWithLocalizedValueInfo<>(WebServiceDirection.INBOUND, WebServiceDirection.INBOUND.getDisplayName(thesaurus));
            ((InboundEndPointConfiguration) endPointConfiguration).getGroup().ifPresent(g -> info.group = g.getName());
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
        if (info.username != null) {
            builder.username(info.username);
        }
        if (info.password != null) {
            builder.username(info.password);
        }

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
        Group group = userService.getGroup(info.group)
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_GROUP));
        endPointConfiguration.setGroup(group);
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
