/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.sap.soap.webservices.SAPCustomPropertySets;
import com.energyict.mdc.sap.soap.webservices.UtilitiesDeviceRegisteredNotification;
import com.energyict.mdc.sap.soap.webservices.security.Privileges;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.Clock;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Path("/")
public class SapResource {
    private final ExceptionFactory exceptionFactory;
    private final DeviceService deviceService;
    private final SAPCustomPropertySets sapCustomPropertySets;
    private final EndPointConfigurationService endPointConfigurationService;
    private final RegisteredNotificationEndPointInfoFactory registeredNotificationEndPointInfoFactory;
    private final Thesaurus thesaurus;
    private final UtilitiesDeviceRegisteredNotification utilitiesDeviceRegisteredNotification;
    private final Clock clock;

    @Inject
    public SapResource(ExceptionFactory exceptionFactory, DeviceService deviceService, SAPCustomPropertySets sapCustomPropertySets,
                       EndPointConfigurationService endPointConfigurationService, RegisteredNotificationEndPointInfoFactory registeredEndPointConfigurationInfoFactory,
                       Thesaurus thesaurus, UtilitiesDeviceRegisteredNotification utilitiesDeviceRegisteredNotification, Clock clock) {
        this.exceptionFactory = exceptionFactory;
        this.deviceService = deviceService;
        this.sapCustomPropertySets = sapCustomPropertySets;
        this.endPointConfigurationService = endPointConfigurationService;
        this.registeredNotificationEndPointInfoFactory = registeredEndPointConfigurationInfoFactory;
        this.thesaurus = thesaurus;
        this.utilitiesDeviceRegisteredNotification = utilitiesDeviceRegisteredNotification;
        this.clock = clock;
    }

    @GET
    @Transactional
    @Path("/devices/{deviceName}/hassapcas")
    @Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.SEND_WEB_SERVICE_REQUEST})
    public Response hasSapCas(@PathParam("deviceName") String deviceName) {
        Device device = deviceService.findDeviceByName(deviceName).orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_DEVICE, deviceName));
        boolean hasSapCas = false;

        if (sapCustomPropertySets.doesDeviceHaveSapCPS(device)) {
            hasSapCas = true;
        }

        return Response.ok(new HasSapCas(hasSapCas)).build();
    }

    @GET
    @Transactional
    @Path("/registerednotificationendpoints")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public Response getAvailableRegisteredNotificationEndpoints(@BeanParam JsonQueryParameters queryParams) {
        List<RegisteredNotificationEndPointInfo> registeredNotificationEndPointInfos = getActiveRegisteredNotificationEndpointConfigurations()
                .stream()
                .map(registeredNotificationEndPointInfoFactory::from)
                .sorted((e1, e2) -> e1.name.compareToIgnoreCase(e2.name))
                .collect(Collectors.toList());

        return Response.ok(PagedInfoList.fromCompleteList("registeredNotificationEndpoints", registeredNotificationEndPointInfos, queryParams)).build();
    }

    @POST
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.SEND_WEB_SERVICE_REQUEST})
    @Path("/devices/{deviceName}/sendregisterednotification/{endpointId}")
    public Response sendRegisteredNotification(@PathParam("deviceName") String deviceName, @PathParam("endpointId") long endpointId, RegisteredNotificationEndPointInfo registeredNotificationEndPointInfo) {

        EndPointConfiguration endPointConfiguration = getActiveRegisteredNotificationEndpointConfigurations().stream()
                .filter(endPoint -> endPoint.getId() == registeredNotificationEndPointInfo.id)
                .findFirst()
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_REGISTERED_NOTIFICATION_ENDPOINT, registeredNotificationEndPointInfo.id));

        Device device = deviceService.findDeviceByName(deviceName)
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_DEVICE, deviceName));

        String sapDeviceId = sapCustomPropertySets.getSapDeviceId(device)
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.DEVICE_ID_ATTRIBUTE_IS_NOT_SET));

        long deviceId = device.getId();

        if (sapCustomPropertySets.isRegistered(device)) {
            throw exceptionFactory.newException(MessageSeeds.DEVICE_ALREADY_REGISTERED);
        }

        if (!sapCustomPropertySets.isAnyLrnPresent(deviceId, clock.instant())) {
            throw exceptionFactory.newException(MessageSeeds.NO_LRN);
        }

        utilitiesDeviceRegisteredNotification.call(sapDeviceId, Collections.singletonList(endPointConfiguration));

        return Response.ok().build();
    }

    private List<EndPointConfiguration> getActiveRegisteredNotificationEndpointConfigurations() {
        return endPointConfigurationService
                .getEndPointConfigurationsForWebService(UtilitiesDeviceRegisteredNotification.NAME)
                .stream()
                .filter(EndPointConfiguration::isActive).collect(Collectors.toList());
    }
}
