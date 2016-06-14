package com.elster.jupiter.webservices.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.IdWithDisplayValueInfo;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.soap.whiteboard.cxf.InboundEndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.LogLevel;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundEndPointConfiguration;

import javax.inject.Inject;

/**
 * Created by bvn on 6/8/16.
 */
public class EndPointConfigurationInfoFactory {
    private final Thesaurus thesaurus;
    private final EndPointConfigurationService endPointConfigurationService;

    @Inject
    public EndPointConfigurationInfoFactory(Thesaurus thesaurus, EndPointConfigurationService endPointConfigurationService) {
        this.thesaurus = thesaurus;
        this.endPointConfigurationService = endPointConfigurationService;
    }

    public EndPointConfigurationInfo from(EndPointConfiguration endPointConfiguration) {
        EndPointConfigurationInfo info = new EndPointConfigurationInfo();
        info.id = endPointConfiguration.getId();
        info.name = endPointConfiguration.getName();
        info.version = endPointConfiguration.getVersion();
        info.url = endPointConfiguration.getUrl();
        info.active = endPointConfiguration.isActive();
        info.webServiceName = endPointConfiguration.getWebServiceName();
        info.logLevel = new IdWithDisplayValueInfo<>(endPointConfiguration.getLogLevel()
                .name(), endPointConfiguration.getLogLevel()
                .getDisplayName(thesaurus));
        info.httpCompression = endPointConfiguration.isHttpCompression();
        info.tracing = endPointConfiguration.isTracing();
        info.schemaValidation = endPointConfiguration.isSchemaValidation();
        if (InboundEndPointConfiguration.class.isAssignableFrom(endPointConfiguration.getClass())) {
            info.direction = EndPointConfigDirection.Inbound;
            info.authenticated = ((InboundEndPointConfiguration) endPointConfiguration).isAuthenticated();
        } else {
            info.direction = EndPointConfigDirection.Outbound;
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
        if (Boolean.TRUE.equals(info.authenticated)) {
            builder.authenticated();
        }
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
        endPointConfiguration.setAuthenticated(info.authenticated);
        return endPointConfiguration;
    }

    private void applyCommonChanges(EndPointConfiguration endPointConfiguration, EndPointConfigurationInfo info) {
        endPointConfiguration.setName(info.name);
        endPointConfiguration.setUrl(info.url);
        endPointConfiguration.setWebServiceName(info.webServiceName);
        endPointConfiguration.setSchemaValidation(info.schemaValidation);
        endPointConfiguration.setHttpCompression(info.httpCompression);
        endPointConfiguration.setLogLevel(LogLevel.valueOf(info.logLevel.id));
        endPointConfiguration.setTracing(info.tracing);
    }
}
