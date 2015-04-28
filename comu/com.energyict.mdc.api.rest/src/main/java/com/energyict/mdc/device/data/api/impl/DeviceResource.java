package com.energyict.mdc.device.data.api.impl;

import com.elster.jupiter.util.conditions.Condition;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.security.Privileges;
import java.util.Optional;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import static java.util.stream.Collectors.toList;

/**
 * Created by bvn on 4/22/15.
 */
@Path("/devices")
public class DeviceResource {

    private final DeviceService deviceService;
    private final DeviceConfigurationService deviceConfigurationService;

    @Inject
    public DeviceResource(DeviceService deviceService, DeviceConfigurationService deviceConfigurationService) {
        this.deviceService = deviceService;
        this.deviceConfigurationService = deviceConfigurationService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_DEVICE, Privileges.OPERATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_DATA})
    public Response getDevices() {
        return Response.ok(deviceService.findAllDevices(Condition.TRUE).stream().limit(10).map(DeviceInfo::from).collect(toList())).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.ADD_DEVICE)
    public DeviceInfo addDevice(DeviceInfo info, @Context SecurityContext securityContext) {
        try {
            Optional<DeviceConfiguration> deviceConfiguration = Optional.empty();
            if (info.deviceConfigurationId != null) {
                deviceConfiguration = deviceConfigurationService.findDeviceConfiguration(info.deviceConfigurationId);
            }

            Device newDevice = deviceService.newDevice(deviceConfiguration.orElse(null), info.mIRD, info.mIRD);
            newDevice.setSerialNumber(info.serialNumber);
            newDevice.setYearOfCertification(2015);
            newDevice.save();

            return DeviceInfo.from(newDevice);
        } catch (ConstraintViolationException e) {
            for (ConstraintViolation<?> constraintViolation : e.getConstraintViolations()) {
                System.out.println("BEAN:"+constraintViolation.getLeafBean());
            }
            throw e;
        }
    }

}
