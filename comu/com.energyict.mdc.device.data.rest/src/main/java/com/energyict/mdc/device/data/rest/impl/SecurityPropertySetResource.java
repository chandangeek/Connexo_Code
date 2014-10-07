package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;
import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

/**
 * Handles SecurityPropertySets on devices
 *
 * Created by bvn on 9/30/14.
 */
public class SecurityPropertySetResource {
    private final ResourceHelper resourceHelper;
    private final SecurityPropertySetInfoFactory securityPropertySetInfoFactory;
    private final ExceptionFactory exceptionFactory;
    private final DeviceConfigurationService deviceConfigurationService;
    private final MdcPropertyUtils mdcPropertyUtils;

    @Inject
    public SecurityPropertySetResource(ResourceHelper resourceHelper, SecurityPropertySetInfoFactory securityPropertySetInfoFactory, ExceptionFactory exceptionFactory, DeviceConfigurationService deviceConfigurationService, MdcPropertyUtils mdcPropertyUtils) {
        this.resourceHelper = resourceHelper;
        this.securityPropertySetInfoFactory = securityPropertySetInfoFactory;
        this.exceptionFactory = exceptionFactory;
        this.deviceConfigurationService = deviceConfigurationService;
        this.mdcPropertyUtils = mdcPropertyUtils;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public PagedInfoList getSecurityPropertySets(@PathParam("mRID") String mrid, @Context UriInfo uriInfo, @BeanParam QueryParameters queryParameters) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        List<SecurityPropertySetInfo> securityPropertySetInfos = securityPropertySetInfoFactory.asInfo(device, uriInfo);

        List<SecurityPropertySetInfo> pagedInfos = ListPager.of(securityPropertySetInfos).from(queryParameters).find();

        return PagedInfoList.asJson("securityPropertySets", pagedInfos, queryParameters);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{securityPropertySetId}")
    public Response getSecurityPropertySet(@PathParam("mRID") String mrid, @Context UriInfo uriInfo, @PathParam("securityPropertySetId") long securityPropertySetId) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        SecurityPropertySet securityPropertySet = getSecurityPropertySetOrThrowException(securityPropertySetId, device);
        return Response.ok(securityPropertySetInfoFactory.asInfo(device, uriInfo,securityPropertySet)).build();
    }

    private SecurityPropertySet getSecurityPropertySetOrThrowException(long securityPropertySetId, Device device) {
        com.google.common.base.Optional<SecurityPropertySet> securityPropertySetOptional = deviceConfigurationService.findSecurityPropertySet(securityPropertySetId);
        if (!securityPropertySetOptional.isPresent()) {
            throw exceptionFactory.newException(MessageSeeds.NO_SUCH_SECURITY_PROPERTY_SET, securityPropertySetId);
        }
        if (securityPropertySetOptional.get().getDeviceConfiguration().getId()!=device.getDeviceConfiguration().getId()) {
            throw exceptionFactory.newException(MessageSeeds.NO_SUCH_SECURITY_PROPERTY_SET_ON_DEVICE, securityPropertySetId, device.getmRID());
        }
        return securityPropertySetOptional.get();
    }

}
