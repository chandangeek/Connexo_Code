package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.config.security.Privileges;
import com.energyict.mdc.multisense.api.impl.utils.FieldSelection;
import com.energyict.mdc.multisense.api.impl.utils.PagedInfoList;
import java.util.List;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import static java.util.stream.Collectors.toList;

@Path("/devicetypes/{deviceTypeId}/deviceconfigurations/{deviceConfigId}/securitypropertyset")
public class ConfigurationSecurityPropertySetResource {

    private final DeviceConfigurationService deviceConfigurationService;
    private final ConfigurationSecurityPropertySetFactory securityPropertySetInfoFactory;

    @Inject
    public ConfigurationSecurityPropertySetResource(DeviceConfigurationService deviceConfigurationService,
                                                    ConfigurationSecurityPropertySetFactory securityPropertySetInfoFactory) {
        this.deviceConfigurationService = deviceConfigurationService;
        this.securityPropertySetInfoFactory = securityPropertySetInfoFactory;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_DEVICE_TYPE, Privileges.Constants.VIEW_DEVICE_TYPE})
    public PagedInfoList<ConfigurationSecurityPropertySetInfo> getSecurityPropertySets(@PathParam("deviceTypeId") long deviceTypeId, @PathParam("deviceConfigId") long deviceConfigurationId,
                                                 @BeanParam JsonQueryParameters queryParameters, @Context UriInfo uriInfo, @BeanParam FieldSelection fieldSelection) {
        DeviceConfiguration deviceConfiguration = findDeviceConfigurationOrThrowException(deviceTypeId, deviceConfigurationId);
        List<ConfigurationSecurityPropertySetInfo> securityPropertySetInfos =
                ListPager.of(deviceConfiguration.getSecurityPropertySets(), (ss1, ss2) -> ss1.getName().compareToIgnoreCase(ss2.getName()))
                         .from(queryParameters)
                         .stream()
                         .map(ss -> securityPropertySetInfoFactory.asInfo(ss, uriInfo, fieldSelection.getFields()))
                         .collect(toList());

        UriBuilder uriBuilder = uriInfo.getBaseUriBuilder().path(ConfigurationSecurityPropertySetResource.class);

        return PagedInfoList.from(securityPropertySetInfos, queryParameters, uriBuilder, uriInfo);
    }

    @GET
    @Path("/{securityPropertySetId}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_DEVICE_TYPE, Privileges.Constants.VIEW_DEVICE_TYPE})
    public ConfigurationSecurityPropertySetInfo getSecurityPropertySet(@PathParam("deviceTypeId") long deviceTypeId, @PathParam("deviceConfigId") long deviceConfigId,
                                                                       @PathParam("securityPropertySetId") long securityPropertySetId,
                                                                       @Context UriInfo uriInfo, @BeanParam FieldSelection fieldSelection) {
        SecurityPropertySet securityPropertySet = findSecurityPropertySetOrThrowException(deviceTypeId, deviceConfigId, securityPropertySetId);

        return securityPropertySetInfoFactory.asInfo(securityPropertySet, uriInfo, fieldSelection.getFields());
    }

    private SecurityPropertySet findSecurityPropertySetOrThrowException(long deviceTypeId, long deviceConfigId, long securityPropertySetId) {
        return findDeviceConfigurationOrThrowException(deviceTypeId, deviceConfigId)
                .getSecurityPropertySets().stream()
                .filter(sps -> sps.getId() == securityPropertySetId)
                .findAny()
                .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND.getStatusCode()));
    }

    private DeviceConfiguration findDeviceConfigurationOrThrowException(long deviceTypeId, long deviceConfigurationId) {
        return deviceConfigurationService.
                    findDeviceType(deviceTypeId)
                    .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND.getStatusCode())).
                            getConfigurations().stream().filter(dc -> dc.getId() == deviceConfigurationId).
                            findFirst()
                            .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND.getStatusCode()));
    }

}
