/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.rest.impl;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.metering.Location;
import com.elster.jupiter.metering.LocationMember;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.security.Privileges;
import com.elster.jupiter.rest.util.QueryParameters;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.transaction.TransactionService;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Path("/devices")
public class DeviceResource {

    private final RestQueryService queryService;
    private final MeteringService meteringService;
    private final TransactionService transactionService;
    private final Clock clock;

    @Inject
    public DeviceResource(RestQueryService queryService, MeteringService meteringService, TransactionService transactionService, Clock clock) {
        this.queryService = queryService;
        this.meteringService = meteringService;
        this.transactionService = transactionService;
        this.clock = clock;
    }

    @GET
    @RolesAllowed({Privileges.Constants.VIEW_ANY_USAGEPOINT, Privileges.Constants.VIEW_OWN_USAGEPOINT})
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public MeterInfos getDevices(@Context UriInfo uriInfo, @Context SecurityContext securityContext) {
        QueryParameters params = QueryParameters.wrap(uriInfo.getQueryParameters());
        List<Meter> list = queryDevices(maySeeAny(securityContext), params);
        return toMeterInfos(params.clipToLimit(list), params.getStartInt(), params.getLimit());
    }

    private MeterInfos toMeterInfos(List<Meter> list, int start, int limit) {
        MeterInfos infos = new MeterInfos(list);
        infos.total = start + list.size();
        if (list.size() == limit) {
            infos.total++;
        }
        return infos;
    }

    private boolean maySeeAny(SecurityContext securityContext) {
        return securityContext.isUserInRole(Privileges.Constants.VIEW_ANY_USAGEPOINT);
    }

    private List<Meter> queryDevices(boolean maySeeAny, QueryParameters queryParameters) {
        Query<Meter> query = meteringService.getMeterQuery();
        if (!maySeeAny) {
            query.setRestriction(meteringService.hasAccountability());
        }
        List<Meter> meters = queryService.wrap(query).select(queryParameters);
        return meters;
    }

    private List<MeterInfo> convertToMeterInfo(List<Meter> meters) {
        List<MeterInfo> meterInfos = new ArrayList<MeterInfo>();
        for (Meter meter : meters) {
            MeterInfo mi = new MeterInfo(meter);
            meterInfos.add(mi);
        }
        return meterInfos;
    }


    @GET
    @RolesAllowed({Privileges.Constants.VIEW_ANY_USAGEPOINT, Privileges.Constants.VIEW_OWN_USAGEPOINT})
    @Path("/{name}/")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public MeterInfos getDevice(@PathParam("name") String name, @Context SecurityContext securityContext) {
        MeterInfos result = null;
        if (maySeeAny(securityContext)) {
            Optional<Meter> foundMeter = meteringService.findMeterByName(name);
            if (foundMeter.isPresent()) {
                result = new MeterInfos(foundMeter.get());
            }
        }
        return result;
    }

    @GET
    @RolesAllowed({Privileges.Constants.VIEW_ANY_USAGEPOINT})
    @Path("/{name}/location/{locale}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public LocationMemberInfos getDeviceLocation(@PathParam("name") String name, @PathParam("locale") String locale, @Context SecurityContext securityContext) {
        LocationMemberInfos result = null;
        if (maySeeAny(securityContext)) {
            Optional<Meter> foundMeter = meteringService.findMeterByName(name);
            if (foundMeter.isPresent()) {
                Optional<Location> location = foundMeter.get().getLocation();
                if (location.isPresent()) {
                    Optional<LocationMember> locationMember = location.get().getMember(locale);
                    if (locationMember.isPresent()) {
                        result = new LocationMemberInfos(locationMember.get());
                    }
                }
            }
        }
        return result;
    }


    @GET
    @RolesAllowed({Privileges.Constants.VIEW_ANY_USAGEPOINT})
    @Path("/{name}/locations")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public LocationMemberInfos getDeviceLocations(@PathParam("name") String name, @PathParam("locale") String locale, @Context SecurityContext securityContext) {
        LocationMemberInfos result = null;
        if (maySeeAny(securityContext)) {
            Optional<Meter> foundMeter = meteringService.findMeterByName(name);
            if (foundMeter.isPresent()) {
                Optional<Location> location = foundMeter.get().getLocation();
                if (location.isPresent()) {
                    result = new LocationMemberInfos(location.get().getMembers());
                }
            }
        }
        return result;
    }
}
