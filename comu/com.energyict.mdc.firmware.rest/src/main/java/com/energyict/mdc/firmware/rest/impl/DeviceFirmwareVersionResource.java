package com.energyict.mdc.firmware.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.firmware.ActivatedFirmwareVersion;
import com.energyict.mdc.firmware.FirmwareService;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Optional;

@Path("/device/{mRID}/firmwares")
public class DeviceFirmwareVersionResource {
    private final FirmwareService firmwareService;
    private final ResourceHelper resourceHelper;
    private final Thesaurus thesaurus;

    @Inject
    public DeviceFirmwareVersionResource(FirmwareService firmwareService, ResourceHelper resourceHelper, Thesaurus thesaurus) {
        this.firmwareService = firmwareService;
        this.resourceHelper = resourceHelper;
        this.thesaurus = thesaurus;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({com.energyict.mdc.device.data.security.Privileges.VIEW_DEVICE, com.energyict.mdc.device.data.security.Privileges.OPERATE_DEVICE_COMMUNICATION, com.energyict.mdc.device.data.security.Privileges.ADMINISTRATE_DEVICE_COMMUNICATION, com.energyict.mdc.device.data.security.Privileges.ADMINISTRATE_DEVICE_DATA})
    public DeviceFirmwareVersionInfos getFirmwareVersionsOnDevice(@PathParam("mRID") String mRID) {
        Device device = resourceHelper.findDeviceByMridOrThrowException(mRID);
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
}
