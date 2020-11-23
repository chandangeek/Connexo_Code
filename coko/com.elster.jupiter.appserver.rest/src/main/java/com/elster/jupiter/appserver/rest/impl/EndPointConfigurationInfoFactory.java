/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.appserver.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.rest.PropertyValueInfoService;
import com.elster.jupiter.rest.util.IdWithLocalizedValueInfo;
import com.elster.jupiter.rest.util.LongIdWithNameInfo;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.InboundEndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.WebService;
import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;

import javax.inject.Inject;
import javax.ws.rs.core.UriInfo;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class EndPointConfigurationInfoFactory {
    Thesaurus thesaurus;
    private final WebServicesService webServicesService;
    private final PropertyValueInfoService propertyValueInfoService;

    @Inject
    public EndPointConfigurationInfoFactory(Thesaurus thesaurus, WebServicesService webServicesService, PropertyValueInfoService propertyValueInfoService) {
        this.thesaurus = thesaurus;
        this.webServicesService = webServicesService;
        this.propertyValueInfoService = propertyValueInfoService;
    }

    public EndPointConfigurationInfo from(EndPointConfiguration endPointConfiguration, UriInfo uriInfo) {
        Optional<WebService> webService = webServicesService.getWebService(endPointConfiguration.getWebServiceName());
        EndPointConfigurationInfo info = new EndPointConfigurationInfo();
        info.id = endPointConfiguration.getId();
        info.name = endPointConfiguration.getName();
        info.version = endPointConfiguration.getVersion();
        info.url = endPointConfiguration.getUrl();
        if (endPointConfiguration.isInbound()) {
            webService.ifPresent(ws -> info.previewUrl = uriInfo.getBaseUri().getScheme() + "://" + uriInfo.getBaseUri()
                    .getAuthority()
                    + "/" + ws.getProtocol().path()
                    + endPointConfiguration.getUrl());
            ((InboundEndPointConfiguration) endPointConfiguration).getGroup()
                    .ifPresent(g -> info.group = new LongIdWithNameInfo(g.getId(), g.getName()));
        }
        info.active = endPointConfiguration.isActive();
        info.available = webService.isPresent();
        info.webServiceName = endPointConfiguration.getWebServiceName();
        info.logLevel = new IdWithLocalizedValueInfo<>(endPointConfiguration.getLogLevel()
                .name(), endPointConfiguration.getLogLevel()
                .getDisplayName(thesaurus));
        info.payloadStrategy = new IdWithLocalizedValueInfo<>(endPointConfiguration.getPayloadSaveStrategy(),
                endPointConfiguration.getPayloadSaveStrategy().getDisplayName(thesaurus));
        info.httpCompression = endPointConfiguration.isHttpCompression();
        info.tracing = endPointConfiguration.isTracing();
        info.traceFile = endPointConfiguration.getTraceFile();
        info.schemaValidation = endPointConfiguration.isSchemaValidation();
        info.authenticationMethod = new IdWithLocalizedValueInfo<>(endPointConfiguration
                .getAuthenticationMethod(),
                endPointConfiguration.getAuthenticationMethod()
                        .getDisplayName(thesaurus));
        webService.ifPresent(ws -> info.type = ws.getProtocol().name());
        List<PropertySpec> propertySpecs = endPointConfiguration.getPropertySpecs();
        info.properties = propertySpecs.isEmpty() ? Collections.emptyList()
                : propertyValueInfoService.getPropertyInfos(propertySpecs, endPointConfiguration.getPropertiesWithValue());
        return info;
    }
}
