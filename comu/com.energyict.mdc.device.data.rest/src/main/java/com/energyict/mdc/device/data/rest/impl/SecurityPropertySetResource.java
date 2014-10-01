package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.device.data.Device;
import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

/**
 * Handles SecurityPropertySets on devices
 *
 * Created by bvn on 9/30/14.
 */
public class SecurityPropertySetResource {
    private final ResourceHelper resourceHelper;
    private final SecurityPropertySetInfoFactory securityPropertySetInfoFactory;
    @Inject
    public SecurityPropertySetResource(ResourceHelper resourceHelper, SecurityPropertySetInfoFactory securityPropertySetInfoFactory) {

        this.resourceHelper = resourceHelper;
        this.securityPropertySetInfoFactory = securityPropertySetInfoFactory;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public PagedInfoList getSecurityPropertySets(@PathParam("mRID") String mrid, @Context SecurityContext securityContext, @Context UriInfo uriInfo, @BeanParam QueryParameters queryParameters) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        List<SecurityPropertySetInfo> securityPropertySetInfos = securityPropertySetInfoFactory.from(device, uriInfo);

        List<SecurityPropertySetInfo> pagedInfos = ListPager.of(securityPropertySetInfos).from(queryParameters).find();

        return PagedInfoList.asJson("securityPropertySets", pagedInfos, queryParameters);
    }
}
