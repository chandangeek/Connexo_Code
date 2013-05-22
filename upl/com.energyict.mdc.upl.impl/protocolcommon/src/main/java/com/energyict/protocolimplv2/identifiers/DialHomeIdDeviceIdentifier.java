package com.energyict.protocolimplv2.identifiers;

import com.energyict.cbo.NotFoundException;
import com.energyict.mdc.protocol.inbound.DeviceIdentifier;
import com.energyict.mdw.core.Device;
import com.energyict.mdw.core.MeteringWarehouse;

import java.util.List;

/**
 * Provides an implementation for the {@link com.energyict.mdc.protocol.inbound.DeviceIdentifier} interface
 * that uses an {@link com.energyict.mdw.core.Device}'s Call Home ID to uniquely identify it.
 *
 * @author: sva
 * @since: 26/10/12 (11:26)
 */
public class DialHomeIdDeviceIdentifier implements DeviceIdentifier {

    private final String callHomeID;

    public DialHomeIdDeviceIdentifier(String callHomeId) {
        super();
        this.callHomeID = callHomeId;
    }

    @Override
    public Device findDevice() {
        List<Device> devicesByDialHomeId = MeteringWarehouse.getCurrent().getDeviceFactory().findByDialHomeId(this.callHomeID);
        if (devicesByDialHomeId.isEmpty()) {
            return null;
        } else {
            if (devicesByDialHomeId.size() > 1) {
                throw new NotFoundException("More than one device found with call home ID " + this.callHomeID);
            } else {
                return devicesByDialHomeId.get(0);
            }
        }
    }

    @Override
    public String toString() {
        return "device with call home id " + this.callHomeID;
    }

    @Override
    public String getIdentifier() {
        return callHomeID;
    }
}