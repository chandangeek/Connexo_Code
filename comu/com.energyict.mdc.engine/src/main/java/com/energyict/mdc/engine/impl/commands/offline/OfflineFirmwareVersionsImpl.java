package com.energyict.mdc.engine.impl.commands.offline;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.firmware.FirmwareStatus;
import com.energyict.mdc.firmware.FirmwareType;
import com.energyict.mdc.protocol.api.device.offline.OfflineFirmwareVersions;

/**
 * Copyrights EnergyICT
 * Date: 16.04.15
 * Time: 14:53
 */
public class OfflineFirmwareVersionsImpl implements OfflineFirmwareVersions{

    private final OfflineFirmwareVersion activeMeterFirmwareVersion;

    public OfflineFirmwareVersionsImpl(Device device, FirmwareService firmwareService) {
        firmwareService.getCurrentMeterFirmwareVersionFor()
    }

    @Override
    public OfflineFirmwareVersion getActiveMeterFirmwareVersion() {
        return null;
    }

    @Override
    public OfflineFirmwareVersion getPassiveMeterFirmwareVersion() {
        return null;
    }

    @Override
    public OfflineFirmwareVersion getActiveCommunicationFirmwareVersion() {
        return null;
    }

    @Override
    public OfflineFirmwareVersion getPassiveCommunicationFirmwareVersion() {
        return null;
    }

    public class OfflineFirmwareVersionImpl implements OfflineFirmwareVersion{

        @Override
        public String getVersion() {
            return null;
        }

        @Override
        public FirmwareStatus getFirmwareStatus() {
            return null;
        }

        @Override
        public FirmwareType getFirmwareType() {
            return null;
        }
    }
}
