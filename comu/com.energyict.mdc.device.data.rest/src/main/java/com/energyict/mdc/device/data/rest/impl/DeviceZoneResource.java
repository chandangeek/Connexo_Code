/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.security.Privileges;
import com.elster.jupiter.metering.zone.EndDeviceZone;
import com.elster.jupiter.metering.zone.MeteringZoneService;
import com.elster.jupiter.metering.zone.Zone;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.rest.util.ConcurrentModificationExceptionFactory;
import com.elster.jupiter.rest.util.IdWithNameInfo;
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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;


public class DeviceZoneResource {
    private final MeteringService meteringService;
    private final MeteringZoneService meteringZoneService;
    private final TransactionService transactionService;
    private final EndDeviceZoneInfoFactory endDeviceZoneInfoFactory;
    private final ConcurrentModificationExceptionFactory conflictFactory;

    @Inject
    public DeviceZoneResource(MeteringService meteringService, MeteringZoneService meteringZoneService, TransactionService transactionService,
                              ConcurrentModificationExceptionFactory conflictFactory, EndDeviceZoneInfoFactory endDeviceZoneInfoFactory) {
        this.meteringService = meteringService;
        this.meteringZoneService = meteringZoneService;
        this.transactionService = transactionService;
        this.endDeviceZoneInfoFactory = endDeviceZoneInfoFactory;
        this.conflictFactory = conflictFactory;
    }

    @GET
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ZONE,
            Privileges.Constants.ADMINISTRATE_ZONE})
    public PagedInfoList getZones(@PathParam("name") String name, @BeanParam JsonQueryParameters queryParameters) {
        return PagedInfoList.fromPagedList("zones", meteringZoneService.getByEndDevice(getDeviceByName(name))
                .from(queryParameters)
                .stream()
                .sorted(getSortComparators())
                .map(deviceZones -> endDeviceZoneInfoFactory.from(deviceZones))
                .collect(Collectors.toList()), queryParameters);
    }

    @POST
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_ZONE})
    public Response createZone(@PathParam("name") String name, EndDeviceZoneInfo endDeviceZoneInfo) {
        validateEndDeviceZoneInfo(endDeviceZoneInfo);
        meteringZoneService.newEndDeviceZone()
                .withEndDevice(getDeviceByName(name))
                .withZone(getZoneById(endDeviceZoneInfo.zoneId))
                .create();
        return Response.status(Response.Status.CREATED).build();
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Path("/{endDeviceZone}")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_ZONE})
    public Response updateZone(EndDeviceZoneInfo endDeviceZoneInfo, @PathParam("endDeviceZone") Long endDeviceZoneId) {
        validateEndDeviceZoneInfo(endDeviceZoneInfo);
        EndDeviceZone zone = getEndDeviceZone(endDeviceZoneId);
        try (TransactionContext context = transactionService.getContext()) {
            zone.setZone(getZoneById(endDeviceZoneInfo.zoneId));
            zone.save(); // don't touch zonetype
            context.commit();
        } catch (UnderlyingSQLFailedException | CommitException ex) {
            throw new LocalizedFieldValidationException(MessageSeeds.ZONE_SAVING_FAIL, "zones", endDeviceZoneInfo.zoneTypeName + "-" + endDeviceZoneInfo.zoneName);
        }
        return Response.status(Response.Status.OK).build();
    }

    @DELETE
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Path("/{endDeviceZone}")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_ZONE})
    public Response deleteZone(@PathParam("endDeviceZone") Long endDeviceZoneId) {
        getEndDeviceZone(endDeviceZoneId).delete();
        return Response.status(Response.Status.OK).build();
    }

    @GET
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Path("/remainingZoneTypes")
    @RolesAllowed({Privileges.Constants.VIEW_ZONE,
            Privileges.Constants.ADMINISTRATE_ZONE})
    public Response getRemainingZones(@HeaderParam("X-CONNEXO-APPLICATION-NAME") String appKey, @PathParam("name") String name) {
        List<Long> usedZoneTypes = meteringZoneService.getByEndDevice(getDeviceByName(name))
                .stream()
                .map(endDeviceZone -> endDeviceZone.getZone().getZoneType().getId())
                .collect(Collectors.toList());

        return Response.ok(meteringZoneService.getZoneTypes(appKey)
                .stream()
                .filter(zoneType -> !usedZoneTypes.contains(zoneType.getId()))
                .sorted((z1, z2) -> z1.getName().compareToIgnoreCase(z2.getName()))
                .map(IdWithNameInfo::new)
                .collect(Collectors.toList())).build();
    }

    /*@GET
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Path("/remainingZones/{endDeviceZone}")
    @RolesAllowed({Privileges.Constants.VIEW_ZONE,
            Privileges.Constants.ADMINISTRATE_ZONE})
    public Response getRemainingZones(@HeaderParam("X-CONNEXO-APPLICATION-NAME") String appKey, @PathParam("name") String name, @PathParam("endDeviceZone") Long endDeviceZoneId) {
        EndDeviceZone endDeviceZone = getEndDeviceZone(endDeviceZoneId);
        Long zoneTypeId = endDeviceZone.getZone().getZoneType().getId();
        List<Long> assignedZones = getAssignedZones(name, zoneTypeId);
        assignedZones.remove(endDeviceZone.getZone().getId());  // remove the current assigment

        return Response.ok(meteringZoneService.getZones(appKey, meteringZoneService.newZoneFilter().setZoneTypes(Arrays.asList(zoneTypeId)))
                .stream()
                .filter(zone -> !assignedZones.contains(zone.getId()))
                .sorted((z1, z2) -> z1.getName().compareToIgnoreCase(z2.getName()))
                .map(IdWithNameInfo::new)
                .collect(Collectors.toList())).build();
    }

    @GET
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Path("/remainingZones/type/{zoneTypeId}")
    @RolesAllowed({Privileges.Constants.VIEW_ZONE,
            Privileges.Constants.ADMINISTRATE_ZONE})
    public Response getRemainingZonesByZoneType(@HeaderParam("X-CONNEXO-APPLICATION-NAME") String appKey, @PathParam("name") String name, @PathParam("zoneTypeId") Long zoneTypeId) {
        List<Long> assignedZones = getAssignedZones(name, zoneTypeId);
        return Response.ok(meteringZoneService.getZones(appKey, meteringZoneService.newZoneFilter().setZoneTypes(Arrays.asList(zoneTypeId)))
                .stream()
                .filter(zone -> !assignedZones.contains(zone.getId()))
                .sorted((z1, z2) -> z1.getName().compareToIgnoreCase(z2.getName()))
                .map(IdWithNameInfo::new)
                .collect(Collectors.toList())).build();
    }*/

    private List<Long> getAssignedZones(String deviceName, Long zoneType) {
        return meteringZoneService.getByEndDevice(getDeviceByName(deviceName))
                .stream()
                .filter(endDeviceZone -> endDeviceZone.getZone().getZoneType().getId() == zoneType)
                .map(endDeviceZone -> endDeviceZone.getZone().getId())
                .collect(Collectors.toList());

    }

    private void validateEndDeviceZoneInfo(EndDeviceZoneInfo endDeviceZoneInfo) {
        new RestValidationBuilder()
                .notEmpty(endDeviceZoneInfo.zoneId, "zoneId")
                .validate();
    }

    public EndDevice getDeviceByName(String name) {
        return meteringService
                .findEndDeviceByName(name)
                .orElseThrow(() -> new NoSuchElementException("Device with name " + name + " not found."));
    }

    public Zone getZoneById(long id) {
        return meteringZoneService
                .getZone(id)
                .orElseThrow(() -> new NoSuchElementException("Zone with id " + id + " not found."));
    }

    public EndDeviceZone getEndDeviceZone(Long endDeviceZoneId) {
        return meteringZoneService
                .getEndDeviceZone(endDeviceZoneId)
                .orElseThrow(() -> new NoSuchElementException("EndDeviceZone with id " + endDeviceZoneId + " not found."));
    }

    private Comparator<EndDeviceZone> getSortComparators() {
        Comparator<EndDeviceZone> compareByTypeZoneName = (zone1, zone2) -> zone1.getZone().getZoneType().getName().compareToIgnoreCase(zone2.getZone().getZoneType().getName());
        Comparator<EndDeviceZone> compareByZoneName = (zone1, zone2) -> zone1.getZone().getName().compareToIgnoreCase(zone2.getZone().getName());
        return compareByTypeZoneName.thenComparing(compareByZoneName);
    }

}
