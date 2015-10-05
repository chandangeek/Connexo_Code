package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.multisense.api.impl.utils.FieldSelection;
import com.energyict.mdc.multisense.api.impl.utils.MessageSeeds;
import com.energyict.mdc.multisense.api.impl.utils.PagedInfoList;
import com.energyict.mdc.multisense.api.security.Privileges;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
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

@Path("/devicetypes/{deviceTypeId}/deviceconfigurations/{deviceConfigId}/securitypropertysets")
public class ConfigurationSecurityPropertySetResource {

    private final DeviceConfigurationService deviceConfigurationService;
    private final ConfigurationSecurityPropertySetFactory securityPropertySetInfoFactory;
    private final ExceptionFactory exceptionFactory;

    @Inject
    public ConfigurationSecurityPropertySetResource(DeviceConfigurationService deviceConfigurationService,
                                                    ConfigurationSecurityPropertySetFactory securityPropertySetInfoFactory, ExceptionFactory exceptionFactory) {
        this.deviceConfigurationService = deviceConfigurationService;
        this.securityPropertySetInfoFactory = securityPropertySetInfoFactory;
        this.exceptionFactory = exceptionFactory;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.PUBLIC_REST_API})
    public PagedInfoList<ConfigurationSecurityPropertySetInfo> getSecurityPropertySets(@PathParam("deviceTypeId") long deviceTypeId, @PathParam("deviceConfigId") long deviceConfigurationId,
                                                 @BeanParam JsonQueryParameters queryParameters, @Context UriInfo uriInfo, @BeanParam FieldSelection fieldSelection) {
        DeviceConfiguration deviceConfiguration = findDeviceConfigurationOrThrowException(deviceTypeId, deviceConfigurationId);
        List<ConfigurationSecurityPropertySetInfo> securityPropertySetInfos =
                ListPager.of(deviceConfiguration.getSecurityPropertySets(), (ss1, ss2) -> ss1.getName().compareToIgnoreCase(ss2.getName()))
                         .from(queryParameters)
                         .stream()
                         .map(ss -> securityPropertySetInfoFactory.from(ss, uriInfo, fieldSelection.getFields()))
                         .collect(toList());

        UriBuilder uriBuilder = uriInfo.getBaseUriBuilder().path(ConfigurationSecurityPropertySetResource.class);

        return PagedInfoList.from(securityPropertySetInfos, queryParameters, uriBuilder, uriInfo);
    }

    @GET
    @Path("/{securityPropertySetId}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.PUBLIC_REST_API})
    public ConfigurationSecurityPropertySetInfo getSecurityPropertySet(@PathParam("deviceTypeId") long deviceTypeId, @PathParam("deviceConfigId") long deviceConfigId,
                                                                       @PathParam("securityPropertySetId") long securityPropertySetId,
                                                                       @Context UriInfo uriInfo, @BeanParam FieldSelection fieldSelection) {
        SecurityPropertySet securityPropertySet = findSecurityPropertySetOrThrowException(deviceTypeId, deviceConfigId, securityPropertySetId);

        return securityPropertySetInfoFactory.from(securityPropertySet, uriInfo, fieldSelection.getFields());
    }

    private SecurityPropertySet findSecurityPropertySetOrThrowException(long deviceTypeId, long deviceConfigId, long securityPropertySetId) {
        return findDeviceConfigurationOrThrowException(deviceTypeId, deviceConfigId)
                .getSecurityPropertySets().stream()
                .filter(sps -> sps.getId() == securityPropertySetId)
                .findAny()
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_SECURITY_PROPERTY_SET));
    }

    private DeviceConfiguration findDeviceConfigurationOrThrowException(long deviceTypeId, long deviceConfigurationId) {
        return deviceConfigurationService.
                    findDeviceType(deviceTypeId)
                    .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_DEVICE_TYPE)).
                            getConfigurations().stream().filter(dc -> dc.getId() == deviceConfigurationId).
                            findFirst()
                            .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_DEVICE_CONFIG));
    }

}
