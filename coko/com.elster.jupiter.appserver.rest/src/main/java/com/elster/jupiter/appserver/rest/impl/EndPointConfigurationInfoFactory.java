package com.elster.jupiter.appserver.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.IdWithLocalizedValueInfo;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;

import javax.inject.Inject;
import javax.ws.rs.core.UriInfo;

public class EndPointConfigurationInfoFactory {
    Thesaurus thesaurus;
    private final WebServicesService webServicesService;

    @Inject
    public EndPointConfigurationInfoFactory(Thesaurus thesaurus, WebServicesService webServicesService) {
        this.thesaurus = thesaurus;
        this.webServicesService = webServicesService;
    }

    public EndPointConfigurationInfo from(EndPointConfiguration endPointConfiguration, UriInfo uriInfo) {
        EndPointConfigurationInfo info = new EndPointConfigurationInfo();
        info.id = endPointConfiguration.getId();
        info.name = endPointConfiguration.getName();
        info.version = endPointConfiguration.getVersion();
        info.url = endPointConfiguration.getUrl();
        info.previewUrl = uriInfo.getBaseUriBuilder()
                .path("/soap") // don't want to hardcode this.
                .path(endPointConfiguration.getUrl()).build();
        info.active = endPointConfiguration.isActive();
        info.available = webServicesService.getWebService(endPointConfiguration.getWebServiceName()).isPresent();
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
        return info;
    }
}
