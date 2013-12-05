package com.energyict.protocolimplv2.identifiers;

/**
 * Copyrights EnergyICT
 * Date: 7/06/13
 * Time: 9:31
 * Author: khe
 */

import com.energyict.cbo.NotFoundException;
import com.energyict.mdc.meterdata.identifiers.CanFindDevice;
import com.energyict.mdc.protocol.api.inbound.DeviceIdentifier;
import com.energyict.mdw.core.Device;
import com.energyict.mdw.core.DeviceFactoryProvider;

/**
 * Provides an implementation for the {@link DeviceIdentifier} interface
 * that uses an {@link com.energyict.mdw.core.Device}'s database identifier.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-10-16 (15:10)
 */
public class DeviceIdentifierById implements CanFindDevice {

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
        Device device = DeviceFactoryProvider.instance.get().getDeviceFactory().find(this.id);
        if (device == null) {
            throw new NotFoundException("Device with id " + this.id + " not found");
        }
        else {
            return device;
        }
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
