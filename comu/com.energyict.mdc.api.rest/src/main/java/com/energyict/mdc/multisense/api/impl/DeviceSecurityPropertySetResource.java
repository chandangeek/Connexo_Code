package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PROPFIND;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
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
import java.util.Comparator;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Path("/devices/{mrid}/securitypropertysets")
public class DeviceSecurityPropertySetResource {

    private final DeviceSecurityPropertySetInfoFactory deviceSecurityPropertySetInfoFactory;
    private final DeviceService deviceService;
    private final ExceptionFactory exceptionFactory;

    @Inject
    public DeviceSecurityPropertySetResource(DeviceService deviceService, DeviceSecurityPropertySetInfoFactory deviceSecurityPropertySetInfoFactory, ExceptionFactory exceptionFactory) {
        this.deviceService = deviceService;
        this.deviceSecurityPropertySetInfoFactory = deviceSecurityPropertySetInfoFactory;
        this.exceptionFactory = exceptionFactory;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @Path("/{securityPropertySetId}")
    @RolesAllowed(Privileges.Constants.PUBLIC_REST_API)
    public DeviceSecurityPropertySetInfo getDeviceSecurityPropertySet(@PathParam("mrid") String mrid, @PathParam("securityPropertySetId") long deviceSecurityPropertySetId, @BeanParam FieldSelection fieldSelection, @Context UriInfo uriInfo) {
        Device device = deviceService.findByUniqueMrid(mrid)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_DEVICE));
        SecurityPropertySet securityPropertySet = device.getDeviceConfiguration().getSecurityPropertySets().stream()
                .filter(sps -> sps.getId() == deviceSecurityPropertySetId)
                .findFirst()
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_SECURITY_PROPERTY_SET));

        return deviceSecurityPropertySetInfoFactory.from(device, securityPropertySet, uriInfo, fieldSelection.getFields());
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @RolesAllowed(Privileges.Constants.PUBLIC_REST_API)
    public PagedInfoList<DeviceSecurityPropertySetInfo> getDeviceSecurityPropertySets(@PathParam("mrid") String mrid, @BeanParam FieldSelection fieldSelection, @Context UriInfo uriInfo, @BeanParam JsonQueryParameters queryParameters) {
        Device device = deviceService.findByUniqueMrid(mrid)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_DEVICE));
        List<DeviceSecurityPropertySetInfo> infos = device.getDeviceConfiguration().getSecurityPropertySets().stream()
                .sorted(Comparator.comparing(SecurityPropertySet::getName))
                .map(sps -> deviceSecurityPropertySetInfoFactory.from(device, sps, uriInfo, fieldSelection.getFields()))
                .collect(toList());

        UriBuilder uriBuilder = uriInfo.getBaseUriBuilder()
                .path(DeviceSecurityPropertySetResource.class)
		        .resolveTemplate("mrid", device.getmRID());
        return PagedInfoList.from(infos, queryParameters, uriBuilder, uriInfo);
    }

    @PROPFIND
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @RolesAllowed(Privileges.Constants.PUBLIC_REST_API)
    public List<String> getFields() {
        return deviceSecurityPropertySetInfoFactory.getAvailableFields().stream().sorted().collect(toList());
    }

}
