package com.energyict.mdc.engine.impl.commands.offline;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageAttribute;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessageAttribute;

/**
 * Straightforward implementation of an OfflineDeviceMessageAttribute
 *
 * Copyrights EnergyICT
 * Date: 11/6/14
 * Time: 8:57 AM
 */
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
        this.deviceMessageAttributeValue = deviceProtocol.format(this.deviceMessageAttribute.getSpecification(), this.deviceMessageAttribute.getValue());
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
