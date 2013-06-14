package com.energyict.protocolimplv2.identifiers;

/**
 * Copyrights EnergyICT
 * Date: 7/06/13
 * Time: 9:31
 * Author: khe
 */

import com.energyict.cbo.NotFoundException;
import com.energyict.mdc.protocol.inbound.DeviceIdentifier;
import com.energyict.mdw.core.Device;
import com.energyict.mdw.core.MeteringWarehouse;

/**
 * Provides an implementation for the {@link com.energyict.mdc.protocol.inbound.DeviceIdentifier} interface
 * that uses an {@link com.energyict.mdw.core.Device}'s database identifier.
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
        Device device = MeteringWarehouse.getCurrent().getDeviceFactory().find(this.id);
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
