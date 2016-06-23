package com.energyict.mdc.usagepoint.data.rest.impl;

import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.security.Privileges;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/usagepoints")
public class UsagePointResource {

    private final MeteringService meteringService;
    private final ExceptionFactory exceptionFactory;
    private final MeterInfoFactory meterInfoFactory;

    @Inject
    public UsagePointResource(MeteringService meteringService, ExceptionFactory exceptionFactory, MeterInfoFactory meterInfoFactory) {
        this.meteringService = meteringService;
        this.exceptionFactory = exceptionFactory;
        this.meterInfoFactory = meterInfoFactory;
    }

    @GET
    @Path("/{mRID}/history/devices")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ANY_USAGEPOINT, Privileges.Constants.VIEW_OWN_USAGEPOINT})
    public PagedInfoList getDevicesHistory(@PathParam("mRID") String mRID, @BeanParam JsonQueryParameters queryParameters) {
        UsagePoint usagePoint = fetchUsagePoint(mRID);
        return PagedInfoList.fromCompleteList("devices", meterInfoFactory.getDevicesHistory(usagePoint), queryParameters);
    }

    private UsagePoint fetchUsagePoint(String mRID) {
        return meteringService.findUsagePoint(mRID)
                .orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NO_USAGE_POINT_FOR_MRID, mRID));
    }
}
