/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.offline;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.firmware.FirmwareVersion;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageAttribute;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessageAttribute;

import java.io.File;

public class OfflineDeviceMessageAttributeImpl implements OfflineDeviceMessageAttribute {

    private final DeviceMessageAttribute deviceMessageAttribute;
    private final DeviceProtocol deviceProtocol;
    private String name;
    private String deviceMessageAttributeValue;
    private PropertySpec propertySpec;


    /**
     * Constructor only to be used by JSON (de)marshalling
     */
    public OfflineDeviceMessageAttributeImpl() {
        this.deviceMessageAttribute = null;
        this.deviceProtocol = null;
    }

    public OfflineDeviceMessageAttributeImpl(DeviceMessageAttribute deviceMessageAttribute, DeviceProtocol deviceProtocol) {
        this.deviceMessageAttribute = deviceMessageAttribute;
        this.deviceProtocol = deviceProtocol;
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

        this.deviceMessageAttributeValue = deviceProtocol.format(this.deviceMessageAttribute.getSpecification(), value);
        this.propertySpec = this.deviceMessageAttribute.getSpecification();
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
    public String getDeviceMessageAttributeValue() {
        return deviceMessageAttributeValue;
    }

}
