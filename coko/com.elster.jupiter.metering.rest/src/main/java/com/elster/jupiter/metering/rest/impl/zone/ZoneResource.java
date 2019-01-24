/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.rest.impl.zone;

import com.elster.jupiter.metering.rest.impl.MessageSeeds;
import com.elster.jupiter.metering.security.Privileges;
import com.elster.jupiter.metering.zone.MeteringZoneService;
import com.elster.jupiter.metering.zone.Zone;
import com.elster.jupiter.metering.zone.ZoneFilter;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.rest.util.ConcurrentModificationExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.RestValidationBuilder;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.transaction.CommitException;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Comparator;
import java.util.stream.Collectors;

@Path("/zones")
public class ZoneResource {
    private final MeteringZoneService meteringZoneService;
    private final TransactionService transactionService;
    private final ZoneTypeInfoFactory zoneTypeInfoFactory;
    private final ZoneInfoFactory zoneInfoFactory;
    private final ConcurrentModificationExceptionFactory conflictFactory;

    @Inject
    public ZoneResource(MeteringZoneService meteringZoneService, TransactionService transactionService,
                        ConcurrentModificationExceptionFactory conflictFactory, ZoneInfoFactory zoneInfoFactory, ZoneTypeInfoFactory zoneTypeInfoFactory) {
        this.meteringZoneService = meteringZoneService;
        this.transactionService = transactionService;
        this.zoneInfoFactory = zoneInfoFactory;
        this.zoneTypeInfoFactory = zoneTypeInfoFactory;
        this.conflictFactory = conflictFactory;
    }

    @GET
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ZONE,
            Privileges.Constants.ADMINISTRATE_ZONE})
    public PagedInfoList getZones(@HeaderParam("X-CONNEXO-APPLICATION-NAME") String appKey, @BeanParam JsonQueryParameters queryParameters, @BeanParam JsonQueryFilter filter) {
        return PagedInfoList.fromPagedList("zones", meteringZoneService.getZones(appKey, getZoneFilter(filter))
                .from(queryParameters)
                .stream()
                .sorted(getSortComparators())
                .map(zones -> zoneInfoFactory.from(zones))
                .collect(Collectors.toList()), queryParameters);
    }

    @GET
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Path("/types")
    @RolesAllowed({Privileges.Constants.VIEW_ZONE,
            Privileges.Constants.ADMINISTRATE_ZONE})
    public PagedInfoList getZoneTypes(@HeaderParam("X-CONNEXO-APPLICATION-NAME") String appKey, @BeanParam JsonQueryParameters queryParameters) {
        return PagedInfoList.fromPagedList("types", meteringZoneService.getZoneTypes(appKey).stream()
                .sorted((zoneType1, zoneType2) -> zoneType1.getName().compareToIgnoreCase(zoneType2.getName()))
                .map(zoneTypes -> zoneTypeInfoFactory.from(zoneTypes))
                .collect(Collectors.toList()), queryParameters);
    }

    @GET
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Path("/{zoneId}")
    @RolesAllowed({Privileges.Constants.VIEW_ZONE,
            Privileges.Constants.ADMINISTRATE_ZONE})
    public ZoneInfo getZone(@HeaderParam("X-CONNEXO-APPLICATION-NAME") String appKey, @PathParam("zoneId") Long zoneId) {
        return meteringZoneService.getZone(zoneId)
                .map(zone -> zoneInfoFactory.from(zone))
                .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
    }

    @POST
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_ZONE})
    public Response createZone(ZoneInfo zoneInfo, @HeaderParam("X-CONNEXO-APPLICATION-NAME") String appKey) {
        validateZoneInfo(zoneInfo);
        meteringZoneService.newZoneBuilder()
                .withName(zoneInfo.name)
                .withZoneType(meteringZoneService.getZoneType(zoneInfo.zoneTypeName, appKey)
                        .orElseGet(() -> {
                            return meteringZoneService.newZoneTypeBuilder()
                                    .withName(zoneInfo.zoneTypeName)
                                    .withApplication(appKey)
                                    .create();
                        }))
                .create();
        return Response.status(Response.Status.CREATED).build();
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Path("/{zoneId}")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_ZONE})
    public Response updateZone(ZoneInfo zoneInfo, @HeaderParam("X-CONNEXO-APPLICATION-NAME") String appKey, @PathParam("zoneId") Long zoneId) {
        validateZoneInfo(zoneInfo);
        Zone zone = getAndLockZone(zoneId, zoneInfo);
        try (TransactionContext context = transactionService.getContext()) {
            zone.setName(zoneInfo.name);
            zone.save(); // don't touch zonetype
            context.commit();
        } catch (UnderlyingSQLFailedException | CommitException ex) {
            throw new LocalizedFieldValidationException(MessageSeeds.ZONE_SAVING_FAIL, "zones", zoneInfo.name);
        }
        return Response.status(Response.Status.OK).build();
    }

    @DELETE
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Path("/{zoneId}")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_ZONE})
    public Response deleteZone(@HeaderParam("X-CONNEXO-APPLICATION-NAME") String appKey, @PathParam("zoneId") Long zoneId) {
        meteringZoneService.getZone(zoneId)
                .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND))
                .delete();
        return Response.status(Response.Status.OK).build();
    }

    private void validateZoneInfo(ZoneInfo zoneInfo) {
        new RestValidationBuilder()
                .notEmpty(zoneInfo.name, "name")
                .notEmpty(zoneInfo.zoneTypeName, "zoneTypeName")
                .validate();
    }

    private Comparator<Zone> getSortComparators() {
        Comparator<Zone> compareByTypeZoneName = (zone1, zone2) -> zone1.getZoneType().getName().compareToIgnoreCase(zone2.getZoneType().getName());
        Comparator<Zone> compareByZoneName = (zone1, zone2) -> zone1.getName().compareToIgnoreCase(zone2.getName());
        return compareByTypeZoneName.thenComparing(compareByZoneName);
    }

    private Zone getAndLockZone(long zoneId, ZoneInfo zoneInfo) {
        return meteringZoneService.getAndLockZone(zoneId, zoneInfo.version)
                .orElseThrow(conflictFactory.contextDependentConflictOn(zoneInfo.name)
                        .withActualVersion(() -> meteringZoneService.getZone(zoneId).map(Zone::getVersion).orElse(null))
                        .supplier());
    }

    private ZoneFilter getZoneFilter(JsonQueryFilter filter) {
        ZoneFilter zoneFilter = meteringZoneService.newZoneFilter();
        if (filter.hasProperty("zoneTypes")) {
            zoneFilter.setZoneTypes(filter.getLongList("zoneTypes"));
        }
        return zoneFilter;
    }
}
