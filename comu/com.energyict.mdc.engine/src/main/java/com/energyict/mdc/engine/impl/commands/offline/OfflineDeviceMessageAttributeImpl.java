package com.energyict.mdc.engine.impl.commands.offline;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.firmware.FirmwareVersion;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageAttribute;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessageAttribute;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.protocol.pluggable.adapters.upl.TypedPropertiesValueAdapter;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.offline.OfflineDevice;

import java.io.File;

/**
 * Straightforward implementation of an OfflineDeviceMessageAttribute
 * <p>
 * Copyrights EnergyICT
 * Date: 11/6/14
 * Time: 8:57 AM
 */
public class OfflineDeviceMessageAttributeImpl implements OfflineDeviceMessageAttribute {

    private final DeviceMessageAttribute deviceMessageAttribute;
    private final DeviceProtocol deviceProtocol;
    private final OfflineDeviceMessage offlineDeviceMessage;
    private final OfflineDevice offlineDevice;
    private ProtocolPluggableService protocolPluggableService;
    private String name;
    private String value;
    private PropertySpec propertySpec;
    private long deviceMessageId;

    /**
     * Constructor only to be used by JSON (de)marshalling
     */
    public OfflineDeviceMessageAttributeImpl() {
        this(null, null, null, null);
    }

    private OfflineDeviceMessageAttributeImpl(DeviceMessageAttribute deviceMessageAttribute, OfflineDeviceMessage offlineDeviceMessage, OfflineDevice offlineDevice, DeviceProtocol deviceProtocol) {
        this.deviceMessageAttribute = deviceMessageAttribute;
        this.deviceProtocol = deviceProtocol;
        this.offlineDeviceMessage = offlineDeviceMessage;
        this.offlineDevice = offlineDevice;
    }

    public OfflineDeviceMessageAttributeImpl(DeviceMessageAttribute deviceMessageAttribute, OfflineDeviceMessage offlineDeviceMessage, OfflineDevice offlineDevice, DeviceProtocol deviceProtocol, ProtocolPluggableService protocolPluggableService) {
        this(deviceMessageAttribute, offlineDeviceMessage, offlineDevice, deviceProtocol);
        this.protocolPluggableService = protocolPluggableService;
        goOffline();
    }

    private void goOffline() {
        this.name = this.deviceMessageAttribute.getName();
        Object value = this.deviceMessageAttribute.getValue();

        if (value instanceof FirmwareVersion) {
            //If the attribute is a FirmwareVersion, use the caching mechanism of the ComServer.
            //A temp file will be created for this FirmwareVersion, if it did not exist yet.
            //The path to this temp file is then provided to the protocols, which can then use it to send the file to the device.

            FirmwareVersion firmwareVersion = (FirmwareVersion) value;
            File tempFile = FirmwareCache.findOrCreateTempFile(firmwareVersion);
            value = tempFile.getAbsolutePath();
        }

        value = TypedPropertiesValueAdapter.adaptToUPLValue(value);

        this.value = deviceProtocol.format(
                offlineDevice,
                offlineDeviceMessage,
                this.protocolPluggableService.adapt(deviceMessageAttribute.getSpecification()),
                value
        );
        this.propertySpec = this.deviceMessageAttribute.getSpecification();

        this.deviceMessageId = deviceMessageAttribute.getDeviceMessage().getMessageId();
    }

    @Override
    public PropertySpec getPropertySpec() {
        return this.propertySpec;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public long getDeviceMessageId() {
        return deviceMessageId;
    }
}