/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.importers.impl.devices.topology;

import com.elster.jupiter.fileimport.csvimport.exceptions.ProcessorException;
import com.energyict.mdc.common.device.config.GatewayType;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.device.data.importers.impl.AbstractDeviceDataFileImportProcessor;
import com.energyict.mdc.device.data.importers.impl.DeviceDataImporterContext;
import com.energyict.mdc.device.data.importers.impl.FileImportLogger;
import com.energyict.mdc.device.data.importers.impl.MessageSeeds;

import java.util.List;
import java.util.Optional;

public class DeviceTopologyImportProcessor extends AbstractDeviceDataFileImportProcessor<DeviceTopologyImportRecord> {

    private final String deviceIdentifier;
    private final Boolean allowReassigning;

    public DeviceTopologyImportProcessor(DeviceDataImporterContext context, String deviceIdentifier, Boolean allowReassigning) {
        super(context);
        this.deviceIdentifier = deviceIdentifier;
        this.allowReassigning = allowReassigning;
    }

    @Override
    public void process(DeviceTopologyImportRecord data, FileImportLogger logger) throws ProcessorException {
        if (data.getMasterDeviceIdentifier() != null && data.getSlaveDeviceIdentifier() != null) {
            // create or update the link
            Device masterDevice = getDevice(data.getMasterDeviceIdentifier(), true, data.getLineNumber());
            Device slaveDevice = getDevice(data.getSlaveDeviceIdentifier(), false, data.getLineNumber());
            setNewMasterDevice(slaveDevice, masterDevice, data.getLineNumber(), logger);
        } else if (data.getMasterDeviceIdentifier() == null && data.getSlaveDeviceIdentifier() != null) {
            // unlink the master gateway from the slave device
            Device slaveDevice = getDevice(data.getSlaveDeviceIdentifier(), false, data.getLineNumber());
            unlinkSlaveDevice(slaveDevice, data.getLineNumber(), logger, null);
        } else if (data.getMasterDeviceIdentifier() != null && data.getSlaveDeviceIdentifier() == null) {
            // unlink all slave devices from the specified master device
            Device masterDevice = getDevice(data.getMasterDeviceIdentifier(), true, data.getLineNumber());
            List<Device> slaves = getContext().getTopologyService().getSlaveDevices(masterDevice);
            slaves.forEach(s -> unlinkSlaveDevice(s, data.getLineNumber(), logger, masterDevice));
        } else {
            // error case, skip the line
            throw new ProcessorException(MessageSeeds.INVALID_TOPOLOGY_IMPORT_RECORD, data.getLineNumber());
        }
    }

    @Override
    public void complete(FileImportLogger logger) {
        // do nothing
    }

    private Device getDevice(String identifier, boolean isMaster, long lineNumber) {
        Device device = null;
        switch (deviceIdentifier) {
            case DeviceTopologyImporterFactory.DEVICE_IDENTIFIER_SERIAL:
                device = findDeviceBySerialNumber(identifier, isMaster, lineNumber);
                break;
            case DeviceTopologyImporterFactory.DEVICE_IDENTIFIER_NAME:
                device = findDeviceByName(identifier, isMaster, lineNumber);
                break;
            default:
                throw new ProcessorException(MessageSeeds.UNSUPPORTED_DEVICE_IDENTIFIER, deviceIdentifier).andStopImport();
        }
        return device;
    }

    private Device findDeviceBySerialNumber(String serialNumber, boolean isMaster, long lineNumber) {
        List<Device> deviceList = getContext().getDeviceService().findDevicesBySerialNumber(serialNumber);
        if (deviceList.isEmpty()) {
            throw isMaster ? new ProcessorException(MessageSeeds.MASTER_DEVICE_NOT_FOUND, lineNumber, serialNumber)
                    : new ProcessorException(MessageSeeds.SLAVE_DEVICE_NOT_FOUND, lineNumber, serialNumber);
        } else if (deviceList.size() > 1) {
            throw new ProcessorException(MessageSeeds.SAME_SERIAL_NUMBER, lineNumber, serialNumber);
        }
        return deviceList.get(0);
    }

    private Device findDeviceByName(String name, boolean isMaster, long lineNumber) {
        return getContext().getDeviceService().findDeviceByName(name).orElseThrow(() -> isMaster
                ? new ProcessorException(MessageSeeds.MASTER_DEVICE_NOT_FOUND, lineNumber, name)
                : new ProcessorException(MessageSeeds.SLAVE_DEVICE_NOT_FOUND, lineNumber, name));
    }

    private void setNewMasterDevice(Device slaveDevice, Device masterDevice, long lineNumber, FileImportLogger logger) {
        if (slaveDevice.equals(masterDevice)) {
            throw new ProcessorException(MessageSeeds.TOPOLOGY_SAME_DEVICE, lineNumber, getDeviceIdentifier(masterDevice));
        }
        Optional<Device> oldGatewayOptional = getContext().getTopologyService().getPhysicalGateway(slaveDevice);
        if (oldGatewayOptional.isPresent() && oldGatewayOptional.get().equals(masterDevice)) {
            logger.warning(MessageSeeds.LINK_ALREADY_EXISTS, lineNumber, getDeviceIdentifier(slaveDevice), getDeviceIdentifier(masterDevice));
            return;
        }
        if (!allowReassigning && oldGatewayOptional.isPresent()) {
            throw new ProcessorException(MessageSeeds.SLAVE_DEVICE_LINKED_TO_ANOTHER_MASTER, lineNumber, getDeviceIdentifier(slaveDevice), getDeviceIdentifier(oldGatewayOptional.get()), getDeviceIdentifier(masterDevice));
        } else {
            if (GatewayType.NONE.equals(masterDevice.getConfigurationGatewayType())) {
                throw new ProcessorException(MessageSeeds.MASTER_DEVICE_NOT_CONFIGURED, lineNumber, getDeviceIdentifier(masterDevice));
            }
            if (slaveDevice.getDeviceConfiguration().isDirectlyAddressable()) {
                throw new ProcessorException(MessageSeeds.SLAVE_DEVICE_NOT_CONFIGURED, lineNumber, getDeviceIdentifier(slaveDevice));
            }
            getContext().getTopologyService().setPhysicalGateway(slaveDevice, masterDevice);
            if (oldGatewayOptional.isPresent()) {
                logger.warning(MessageSeeds.SLAVE_DEVICE_SUCCESSFULLY_REASSIGNED, lineNumber, getDeviceIdentifier(slaveDevice), getDeviceIdentifier(oldGatewayOptional.get()), getDeviceIdentifier(masterDevice));
            } else {
                logger.warning(MessageSeeds.SLAVE_SUCCESSFULLY_LINKED, lineNumber, getDeviceIdentifier(slaveDevice), getDeviceIdentifier(masterDevice));
            }
        }
    }

    private void unlinkSlaveDevice(Device slaveDevice, long lineNumber, FileImportLogger logger, Device supposedMasterDevice) {
        Optional<Device> oldGatewayOptional = getContext().getTopologyService().getPhysicalGateway(slaveDevice);
        if (oldGatewayOptional.isPresent() && (supposedMasterDevice == null || oldGatewayOptional.get().equals(supposedMasterDevice))) {
            getContext().getTopologyService().clearPhysicalGateway(slaveDevice);
            logger.warning(MessageSeeds.SLAVE_SUCCESSFULLY_UNLINKED, lineNumber, getDeviceIdentifier(slaveDevice), getDeviceIdentifier(oldGatewayOptional.get()));
        } else {
            logger.warning(MessageSeeds.NO_LINK_EXISTS, lineNumber, getDeviceIdentifier(slaveDevice));
        }
    }

    private String getDeviceIdentifier(Device device) {
        String identifier = null;
        switch (deviceIdentifier) {
            case DeviceTopologyImporterFactory.DEVICE_IDENTIFIER_SERIAL:
                identifier = device.getSerialNumber();
                break;
            case DeviceTopologyImporterFactory.DEVICE_IDENTIFIER_NAME:
                identifier = device.getName();
                break;
            default:
                throw new ProcessorException(MessageSeeds.UNSUPPORTED_DEVICE_IDENTIFIER, deviceIdentifier).andStopImport();
        }
        return identifier;
    }
}
