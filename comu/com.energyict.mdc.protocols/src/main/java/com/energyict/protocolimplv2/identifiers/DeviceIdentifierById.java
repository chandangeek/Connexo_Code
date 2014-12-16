package com.energyict.protocolimplv2.identifiers;

import com.energyict.mdc.common.Environment;
import com.energyict.mdc.common.NotFoundException;
import com.energyict.mdc.protocol.api.device.BaseDevice;
import com.energyict.mdc.protocol.api.device.DeviceFactory;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifierType;

import javax.xml.bind.annotation.XmlElement;
import java.util.List;

/**
 * Provides an implementation for the {@link DeviceIdentifier} interface
 * that uses an {@link com.energyict.mdc.protocol.api.device.BaseDevice}'s database identifier.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-10-16 (15:10)
 */
public class DeviceIdentifierById implements DeviceIdentifier {

    private long id;

    public DeviceIdentifierById(long id) {
        super();
        this.id = id;
    }

    // used for reflection
    public DeviceIdentifierById(String id) {
        super();
        this.id = Integer.parseInt(id);
    }

    @Override
    public BaseDevice findDevice () {
        BaseDevice device = this.findDevice(this.id);
        if (device == null) {
            throw new NotFoundException("Device with id " + this.id + " not found");
        }
        else {
            return device;
        }
    }

    private BaseDevice findDevice (long deviceId) {
        List<DeviceFactory> deviceFactories = Environment.DEFAULT.get().getApplicationContext().getModulesImplementing(DeviceFactory.class);
        return deviceFactories.get(0).findById(deviceId);
    }

    @Override
    public String toString () {
        return "id " + this.id;
    }

    @Override
    public String getIdentifier() {
        return Long.toString(id);
    }

    @Override
    public DeviceIdentifierType getDeviceIdentifierType() {
        return DeviceIdentifierType.DataBaseId;
    }

    @XmlElement(name = "type")
    public String getXmlType() {
        return this.getClass().getName();
    }

    public void setXmlType(String ignore) {
        // For xml unmarshalling purposes only
    }
}
