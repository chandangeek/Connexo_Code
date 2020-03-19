/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.rest.impl;

import com.elster.jupiter.rest.util.BooleanValueInfo;
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
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Path("/")
public class SapResource {
    private final ExceptionFactory exceptionFactory;
    private final DeviceService deviceService;
    private final SAPCustomPropertySets sapCustomPropertySets;
    private final EndPointConfigurationService endPointConfigurationService;
    private final RegisteredNotificationEndPointInfoFactory registeredNotificationEndPointInfoFactory;
    private final UtilitiesDeviceRegisteredNotification utilitiesDeviceRegisteredNotification;
    private final Clock clock;

    @Inject
    public SapResource(ExceptionFactory exceptionFactory, DeviceService deviceService, SAPCustomPropertySets sapCustomPropertySets,
                       EndPointConfigurationService endPointConfigurationService, RegisteredNotificationEndPointInfoFactory registeredEndPointConfigurationInfoFactory,
                       UtilitiesDeviceRegisteredNotification utilitiesDeviceRegisteredNotification, Clock clock) {
        this.exceptionFactory = exceptionFactory;
        this.deviceService = deviceService;
        this.sapCustomPropertySets = sapCustomPropertySets;
        this.endPointConfigurationService = endPointConfigurationService;
        this.registeredNotificationEndPointInfoFactory = registeredEndPointConfigurationInfoFactory;
        this.utilitiesDeviceRegisteredNotification = utilitiesDeviceRegisteredNotification;
        this.clock = clock;
    }

    @GET
    @Transactional
    @Path("/devices/{deviceName}/hassapcas")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.SEND_WEB_SERVICE_REQUEST})
    public Response hasSapCas(@PathParam("deviceName") String deviceName) {
        Device device = deviceService.findDeviceByName(deviceName).orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_DEVICE, deviceName));
        boolean hasSapCas = false;

        if (sapCustomPropertySets.doesDeviceHaveSapCPS(device)) {
            hasSapCas = true;
        }

        return Response.ok().entity("{\"exist\":\"" + hasSapCas + "\"}").build();
    }

    @GET
    @Transactional
    @Path("/devices/{deviceName}/registers/havesapcas")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({com.energyict.mdc.device.data.security.Privileges.Constants.VIEW_DEVICE,
            com.energyict.mdc.device.data.security.Privileges.Constants.ADMINISTRATE_DEVICE_DATA,
            com.energyict.mdc.device.data.security.Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION,
            com.energyict.mdc.device.data.security.Privileges.Constants.OPERATE_DEVICE_COMMUNICATION})
    public Response isSapCasAssignedToAnyRegister(@PathParam("deviceName") String deviceName) {
        Device device = deviceService.findDeviceByName(deviceName).orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_DEVICE, deviceName));
        boolean haveSapCas = device.getRegisters().stream().anyMatch(sapCustomPropertySets::doesRegisterHaveSapCPS);
        return Response.ok().entity(new BooleanValueInfo(haveSapCas)).build();
    }

    @GET
    @Transactional
    @Path("/devices/{deviceName}/channels/havesapcas")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({com.energyict.mdc.device.data.security.Privileges.Constants.VIEW_DEVICE,
            com.energyict.mdc.device.data.security.Privileges.Constants.ADMINISTRATE_DEVICE_DATA,
            com.energyict.mdc.device.data.security.Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION,
            com.energyict.mdc.device.data.security.Privileges.Constants.OPERATE_DEVICE_COMMUNICATION})
    public Response isSapCasAssignedToAnyChannel(@PathParam("deviceName") String deviceName) {
        Device device = deviceService.findDeviceByName(deviceName).orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_DEVICE, deviceName));
        boolean haveSapCas = device.getChannels().stream().anyMatch(sapCustomPropertySets::doesChannelHaveSapCPS);
        return Response.ok().entity(new BooleanValueInfo(haveSapCas)).build();
    }

    @GET
    @Transactional
    @Path("/registerednotificationendpoints")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.SEND_WEB_SERVICE_REQUEST})
    public Response getAvailableRegisteredNotificationEndpoints(@BeanParam JsonQueryParameters queryParams) {
        List<RegisteredNotificationEndPointInfo> registeredNotificationEndPointInfos = getActiveRegisteredNotificationEndpointConfigurations()
                .map(registeredNotificationEndPointInfoFactory::from)
                .sorted(Comparator.comparing(info -> info.name, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());

        return Response.ok(PagedInfoList.fromCompleteList("registeredNotificationEndpoints", registeredNotificationEndPointInfos, queryParams)).build();
    }

    @POST
    @Transactional
    @Path("/devices/{deviceName}/sendregisterednotification/{endpointId}") //TODO: remove "endpointId" query parameter on BE/FE
    @Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.SEND_WEB_SERVICE_REQUEST})
    public Response sendRegisteredNotification(@PathParam("deviceName") String deviceName, RegisteredNotificationEndPointInfo registeredNotificationEndPointInfo) {

        EndPointConfiguration endPointConfiguration = endPointConfigurationService.getEndPointConfiguration(registeredNotificationEndPointInfo.id)
                .filter(EndPointConfiguration::isActive)
                .filter(e -> e.getWebServiceName().equals(UtilitiesDeviceRegisteredNotification.NAME))
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_REGISTERED_NOTIFICATION_ENDPOINT, registeredNotificationEndPointInfo.id));

        Device device = deviceService.findDeviceByName(deviceName)
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_DEVICE, deviceName));

        String sapDeviceId = sapCustomPropertySets.getSapDeviceId(device)
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.DEVICE_ID_ATTRIBUTE_IS_NOT_SET));

        if (sapCustomPropertySets.isRegistered(device)) {
            throw exceptionFactory.newException(MessageSeeds.DEVICE_ALREADY_REGISTERED);
        }

        if (!sapCustomPropertySets.isAnyLrnPresent(device.getId(), clock.instant())) {
            throw exceptionFactory.newException(MessageSeeds.NO_LRN);
        }

        if (!utilitiesDeviceRegisteredNotification.call(sapDeviceId, Collections.singleton(endPointConfiguration))) {
            throw exceptionFactory.newException(MessageSeeds.REQUEST_SENDING_HAS_FAILED);
        }

        return Response.ok().build();
    }

    private Stream<EndPointConfiguration> getActiveRegisteredNotificationEndpointConfigurations() {
        return endPointConfigurationService
                .getEndPointConfigurationsForWebService(UtilitiesDeviceRegisteredNotification.NAME)
                .stream()
                .filter(EndPointConfiguration::isActive);
    }
}
