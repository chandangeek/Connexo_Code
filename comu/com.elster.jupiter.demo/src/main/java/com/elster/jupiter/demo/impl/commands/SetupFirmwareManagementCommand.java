package com.elster.jupiter.demo.impl.commands;

import com.elster.jupiter.demo.impl.UnableToCreate;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.firmware.*;
import com.energyict.mdc.protocol.api.firmware.ProtocolSupportedFirmwareOptions;

import javax.inject.Inject;
import java.io.*;
import java.util.*;

/**
 * Purpose for this command is to install 2 Firmware versions for each device type (Except 'Landis+Gyr ZMD')
 * the first in a 'Final' state, the second one in a 'Test' state.
 *
 * Copyrights EnergyICT
 * Date: 10/09/2015
 * Time: 14:28
 */
public class SetupFirmwareManagementCommand {

    private final static String FIRMWARE_VERSION_V1 = "NTA-Sim_V_1.0.0";
    private final static String FIRMWARE_VERSION_V2 = "NTA-Sim_V_2.0.0";
    private final static String LANDIS_GYR_ZMD_DEVICETYPE = "Landis+Gyr ZMD";

    private Set<ProtocolSupportedFirmwareOptions> supportedOptions = EnumSet.of(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_IMMEDIATE,
                                                                          ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_WITH_DATE);

    private final FirmwareService firmwareService;
    private final DeviceConfigurationService deviceConfigurationService;

    @Inject
    public SetupFirmwareManagementCommand(FirmwareService firmwareService,
                                          DeviceConfigurationService deviceConfigurationService) {
        this.firmwareService = firmwareService;
        this.deviceConfigurationService = deviceConfigurationService;
    }

    public void run() {
        deviceConfigurationService.findAllDeviceTypes().stream().forEach(this::setUpDeviceTypeForFirmwareManagement);
    }

    public static boolean isExcluded(DeviceType deviceType){
        return deviceType.getName().equals(LANDIS_GYR_ZMD_DEVICETYPE);
    }

    private void setUpDeviceTypeForFirmwareManagement(DeviceType deviceType){
        if (!isExcluded(deviceType)) {
            FirmwareVersion v1 = firmwareService.newFirmwareVersion(deviceType,FIRMWARE_VERSION_V1,FirmwareStatus.FINAL, FirmwareType.METER);
            setFirmwareBytes(v1, getClass().getClassLoader().getResourceAsStream(FIRMWARE_VERSION_V1+".firm"));

            FirmwareVersion v2 = firmwareService.newFirmwareVersion(deviceType, FIRMWARE_VERSION_V2, FirmwareStatus.TEST, FirmwareType.METER);
            setFirmwareBytes(v2, getClass().getClassLoader().getResourceAsStream(FIRMWARE_VERSION_V2+".firm"));

            FirmwareManagementOptions options = firmwareService.newFirmwareManagementOptions(deviceType);
            options.setOptions(supportedOptions);
            options.save();
        }
    }

    private void setFirmwareBytes(FirmwareVersion firmwareVersion, InputStream inputStream){
        try {
            firmwareVersion.setFirmwareFile(getBytes(inputStream));
            firmwareVersion.save();
        }catch(IOException exception){
           throw new UnableToCreate("FirmwareFile " + firmwareVersion.getFirmwareVersion() + ".firm could not be read");
        }
    }

    private byte[] getBytes(InputStream inputStream) throws IOException {
        byte[] bytes = new byte[inputStream.available()];
        inputStream.read(bytes);
        return bytes;
    }




}
