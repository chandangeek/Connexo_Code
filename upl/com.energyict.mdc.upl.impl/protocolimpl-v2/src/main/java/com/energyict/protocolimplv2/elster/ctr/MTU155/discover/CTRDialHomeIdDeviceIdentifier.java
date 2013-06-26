package com.energyict.protocolimplv2.elster.ctr.MTU155.discover;


import com.energyict.cbo.NotFoundException;
import com.energyict.mdc.channels.ip.CTRInboundDialHomeIdConnectionType;
import com.energyict.mdc.protocol.inbound.DeviceIdentifier;
import com.energyict.mdw.core.Device;
import com.energyict.mdw.core.DeviceFactory;
import com.energyict.mdw.core.MeteringWarehouse;

import java.util.List;

/**
 * Provides an implementation for the {@link com.energyict.mdc.protocol.inbound.DeviceIdentifier} interface,
 * specific for the CTR protocol base (MTU155 and EK155 device types). <br></br>
 * These protocols use a Call Home ID to uniquely identify the calling device.
 *
 * @author: sva
 * @since: 26/10/12 (11:26)
 */
public class CTRDialHomeIdDeviceIdentifier implements DeviceIdentifier {

    public static final String CALL_HOME_ID_PROPERTY_NAME = "callHomeId";

    private final String callHomeID;

    public CTRDialHomeIdDeviceIdentifier(String callHomeId) {
        super();
        this.callHomeID = callHomeId;
    }

    @Override
    public Device findDevice() {
        DeviceFactory deviceFactory = MeteringWarehouse.getCurrent().getDeviceFactory();
        List<Device> devicesByDialHomeId = deviceFactory.findByConnectionTypeProperty(CTRInboundDialHomeIdConnectionType.class, CALL_HOME_ID_PROPERTY_NAME, callHomeID);

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