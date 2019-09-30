package com.energyict.mdc.sap.soap.webservices.impl.deviceinitialization;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.common.device.config.DeviceConfiguration;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.DeviceBuilder;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.sap.soap.webservices.SAPCustomPropertySets;
import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;
import com.energyict.mdc.sap.soap.webservices.impl.SAPWebServiceException;

import javax.inject.Inject;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public class DeviceHelper {
    private final Thesaurus thesaurus;
    private final DeviceService deviceService;
    private final DeviceConfigurationService deviceConfigurationService;
    private final SAPCustomPropertySets sapCustomPropertySets;

    @Inject
    public DeviceHelper(Thesaurus thesaurus, DeviceService deviceService, DeviceConfigurationService deviceConfigurationService,
                 SAPCustomPropertySets sapCustomPropertySets) {
        this.thesaurus = thesaurus;
        this.deviceService = deviceService;
        this.deviceConfigurationService = deviceConfigurationService;
        this.sapCustomPropertySets = sapCustomPropertySets;
    }

    public void processDeviceCreation(String sapDeviceId, String serialId, String deviceType,
                                      Instant shipmentDate, String manufacture, String modelNumber) throws SAPWebServiceException {
        validateSapDeviceIdUniqueness(sapDeviceId, serialId);
        Device device = getOrCreateDevice(deviceType, serialId, shipmentDate, manufacture, modelNumber);
        sapCustomPropertySets.setSapDeviceId(device, sapDeviceId);
    }

    private void validateSapDeviceIdUniqueness(String sapDeviceId, String serialId) {
        Optional<Device> other = sapCustomPropertySets.getDevice(sapDeviceId);
        if (other.isPresent() && !other.get().getSerialNumber().equals(serialId)) {
            throw new SAPWebServiceException(thesaurus, MessageSeeds.SAP_DEVICE_IDENTIFIER_MUST_BE_UNIQUE);
        }
    }

    private Device getOrCreateDevice(String deviceType, String serialId, Instant shipmentDate, String manufacture, String modelNumber) {
        Device device = null;
        List<Device> devices = deviceService.findDevicesBySerialNumber(serialId);
        if (!devices.isEmpty()) {
            if (devices.size() == 1) {
                device = devices.get(0);
            } else {
                throw new SAPWebServiceException(thesaurus, MessageSeeds.SEVERAL_DEVICES, serialId);
            }
        } else {
            device = createDevice(deviceType, serialId, shipmentDate, manufacture, modelNumber);
        }

        return device;
    }

    public Device createDevice(String deviceType, String serialId, Instant shipmentDate, String manufacture, String modelNumber) {
        DeviceConfiguration deviceConfig = findDeviceConfiguration(deviceType);
        DeviceBuilder deviceBuilder = deviceService.newDeviceBuilder(deviceConfig,
                serialId, shipmentDate);
        deviceBuilder.withSerialNumber(serialId);
        deviceBuilder.withManufacturer(manufacture);
        deviceBuilder.withModelNumber(modelNumber);
        return deviceBuilder.create();
    }

    private DeviceConfiguration findDeviceConfiguration(String deviceTypeName) {
        DeviceConfiguration deviceConfiguration =
                deviceConfigurationService.findDeviceTypeByName(deviceTypeName)
                        .orElseThrow(() -> new SAPWebServiceException(thesaurus, MessageSeeds.NO_DEVICE_TYPE_FOUND, deviceTypeName))
                        .getConfigurations()
                        .stream()
                        .filter(config -> config.isDefault())
                        .findAny().orElseThrow(() -> new SAPWebServiceException(thesaurus, MessageSeeds.NO_DEFAULT_DEVICE_CONFIGURATION, deviceTypeName));
        return deviceConfiguration;
    }


}
