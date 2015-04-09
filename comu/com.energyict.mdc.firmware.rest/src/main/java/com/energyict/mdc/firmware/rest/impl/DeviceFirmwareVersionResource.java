package com.energyict.mdc.firmware.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.firmware.ActivatedFirmwareVersion;
import com.energyict.mdc.firmware.FirmwareService;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Optional;

@Path("/device/{mRID}/firmwares")
public class DeviceFirmwareVersionResource {
    private final FirmwareService firmwareService;
    private final DeviceService deviceService;
    private final Thesaurus thesaurus;

    @Inject
    public DeviceFirmwareVersionResource(FirmwareService firmwareService, DeviceService deviceService, Thesaurus thesaurus) {
        this.firmwareService = firmwareService;
        this.deviceService = deviceService;
        this.thesaurus = thesaurus;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({com.energyict.mdc.device.data.security.Privileges.VIEW_DEVICE, com.energyict.mdc.device.data.security.Privileges.OPERATE_DEVICE_COMMUNICATION, com.energyict.mdc.device.data.security.Privileges.ADMINISTRATE_DEVICE_COMMUNICATION, com.energyict.mdc.device.data.security.Privileges.ADMINISTRATE_DEVICE_DATA})
    public DeviceFirmwareVersionInfos getFirmwareVersionsOnDevice(@PathParam("mRID") String mRID) {
        Device device = findDeviceByMrIdOrThrowException(mRID);
        Optional<ActivatedFirmwareVersion> activeMeterVersion = firmwareService.getCurrentMeterFirmwareVersionFor(device);
        Optional<ActivatedFirmwareVersion> activeCommunicationVersion = firmwareService.getCurrentCommunicationFirmwareVersionFor(device);

        DeviceFirmwareVersionInfos deviceFirmwareVersionInfo = new DeviceFirmwareVersionInfos();
        if (activeMeterVersion.isPresent()) {
            deviceFirmwareVersionInfo.addVersion(activeMeterVersion.get(), thesaurus);
        }
        if(activeCommunicationVersion.isPresent()) {
            deviceFirmwareVersionInfo.addVersion(activeCommunicationVersion.get(), thesaurus);
        }
        return deviceFirmwareVersionInfo;
    }

    public Device findDeviceByMrIdOrThrowException(String mRID) {
        Optional<Device> deviceRef = deviceService.findByUniqueMrid(mRID);
        if (!deviceRef.isPresent()) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        return deviceRef.get();
    }

}
