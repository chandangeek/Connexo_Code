package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PROPFIND;
import com.elster.jupiter.rest.util.Transactional;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.multisense.api.impl.utils.FieldSelection;
import com.energyict.mdc.multisense.api.impl.utils.MessageSeeds;
import com.energyict.mdc.multisense.api.impl.utils.PagedInfoList;
import com.energyict.mdc.multisense.api.security.Privileges;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.util.List;

import static java.util.stream.Collectors.toList;


@Path("/devicetypes/{deviceTypeId}/deviceconfigurations/{deviceConfigId}/protocoldialectconfigurationproperties")
public class ProtocolDialectConfigurationPropertiesResource {

    private final ProtocolDialectConfigurationPropertiesInfoFactory protocolDialectConfigurationPropertiesInfoFactory;
    private final DeviceConfigurationService deviceConfigurationService;
    private final ExceptionFactory exceptionFactory;

    @Inject
    public ProtocolDialectConfigurationPropertiesResource(DeviceConfigurationService deviceConfigurationService, ProtocolDialectConfigurationPropertiesInfoFactory protocolDialectConfigurationPropertiesInfoFactory, ExceptionFactory exceptionFactory) {
        this.deviceConfigurationService = deviceConfigurationService;
        this.protocolDialectConfigurationPropertiesInfoFactory = protocolDialectConfigurationPropertiesInfoFactory;
        this.exceptionFactory = exceptionFactory;
    }

    @GET @Transactional
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @Path("/{id}")
    @RolesAllowed(Privileges.Constants.PUBLIC_REST_API)
    public ProtocolDialectConfigurationPropertiesInfo getProtocolDialectConfigurationProperty(
            @PathParam("deviceTypeId") long deviceTypeId,
            @PathParam("deviceConfigId") long devicConfigId,
            @PathParam("id") long id,
            @BeanParam FieldSelection fieldSelection, @Context UriInfo uriInfo) {
        ProtocolDialectConfigurationPropertiesInfo info = deviceConfigurationService.
                findDeviceType(deviceTypeId)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_DEVICE_TYPE))
                .getConfigurations().stream()
                .filter(dc -> dc.getId() == devicConfigId)
                .findFirst()
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_DEVICE_CONFIG))
                .getProtocolDialectConfigurationPropertiesList().stream()
                .filter(props->props.getId()==id)
                .map(props->protocolDialectConfigurationPropertiesInfoFactory.from(props, uriInfo, fieldSelection.getFields()))
                .findFirst()
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_PROTOCOL_DIALECT_PROPERTIES));

        return info;
    }

    @GET @Transactional
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @RolesAllowed(Privileges.Constants.PUBLIC_REST_API)
    public PagedInfoList<ProtocolDialectConfigurationPropertiesInfo> getProtocolDialectConfigurationProperties(
            @PathParam("deviceTypeId") long deviceTypeId,
            @PathParam("deviceConfigId") long deviceConfigId,
            @BeanParam FieldSelection fieldSelection, @Context UriInfo uriInfo,
            @BeanParam JsonQueryParameters queryParameters) {
        List<ProtocolDialectConfigurationPropertiesInfo> infos = deviceConfigurationService.
                findDeviceType(deviceTypeId)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_DEVICE_TYPE))
                .getConfigurations().stream()
                .filter(dc -> dc.getId() == deviceConfigId)
                .findFirst()
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_DEVICE_CONFIG))
                .getProtocolDialectConfigurationPropertiesList().stream()
                .map(props->protocolDialectConfigurationPropertiesInfoFactory.from(props, uriInfo, fieldSelection.getFields()))
                .collect(toList());

        UriBuilder uriBuilder = uriInfo.getBaseUriBuilder()
                .path(ProtocolDialectConfigurationPropertiesResource.class)
                .resolveTemplate("deviceTypeId", deviceTypeId)
                .resolveTemplate("deviceConfigId", deviceConfigId);

        return PagedInfoList.from(infos, queryParameters, uriBuilder, uriInfo);
    }

    @PROPFIND
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @RolesAllowed(Privileges.Constants.PUBLIC_REST_API)
    public List<String> getFields() {
        return protocolDialectConfigurationPropertiesInfoFactory.getAvailableFields().stream().sorted().collect(toList());
    }



}
