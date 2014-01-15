package com.energyict.protocolimplv2.identifiers;

/**
 * Copyrights EnergyICT
 * Date: 7/06/13
 * Time: 9:31
 * Author: khe
 */

import com.energyict.mdc.common.Environment;
import com.energyict.mdc.common.FactoryIds;
import com.energyict.mdc.common.IdBusinessObjectFactory;
import com.energyict.mdc.common.NotFoundException;
import com.energyict.mdc.protocol.api.device.Device;
import com.energyict.mdc.protocol.api.inbound.DeviceIdentifier;

/**
 * Provides an implementation for the {@link DeviceIdentifier} interface
 * that uses an {@link Device}'s database identifier.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-10-16 (15:10)
 */
public class DeviceIdentifierById implements DeviceIdentifier {

    private int id;

    public DeviceIdentifierById(int id) {
        super();
        this.id = id;
    }

    // used for reflection
    public DeviceIdentifierById(String id) {
        super();
        this.id = Integer.parseInt(id);
    }

    @Override
    public Device findDevice () {
        Device device = this.findDevice(this.id);
        if (device == null) {
            throw new NotFoundException("Device with id " + this.id + " not found");
        }
        else {
            return device;
        }
    }

    private Device findDevice (int deviceId) {
        IdBusinessObjectFactory<Device> factory = (IdBusinessObjectFactory<Device>) Environment.DEFAULT.get().findFactory(FactoryIds.DEVICE.id());
        return factory.get(deviceId);
    }

    @Override
    public String toString () {
        return "id " + this.id;
    }

    @Override
    public String getIdentifier() {
        return Integer.toString(id);
    }
}
