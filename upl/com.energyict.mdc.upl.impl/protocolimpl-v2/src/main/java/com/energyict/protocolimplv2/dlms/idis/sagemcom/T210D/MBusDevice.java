package com.energyict.protocolimplv2.dlms.idis.sagemcom.T210D;

import com.energyict.mdc.protocol.security.DeviceProtocolSecurityCapabilities;
import com.energyict.mdc.protocol.tasks.support.DeviceMessageSupport;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.AbstractDlmsSlaveProtocol;
import com.energyict.protocolimplv2.dlms.idis.am500.messages.mbus.IDISMBusMessaging;

/**
 * Logical slave protocol that does not read out any data, it's merely a placeholder for the supported messages, properties, ...
 * The read out of the MBus registers, logbook, load profile, etc is implemented in the AM540 E-meter (master) protocol.
 *
 * @author sva
 * @since 11/08/2015 - 14:49
 */
public class MBusDevice extends AbstractDlmsSlaveProtocol {

    private final AbstractDlmsProtocol masterProtocol = new T210D();
    private final IDISMBusMessaging idisMBusMessaging = new IDISMBusMessaging(masterProtocol);

    @Override
    public String getProtocolDescription() {
        return "Sagemcom T/S210-D DLMS (IDIS P2) MBus slave";
    }

    @Override
    public String getVersion() {
        return "$Date: 2017-03-20 16:29:40 +0200 (Mon, 20 Mar 2017)$";
    }

    protected DeviceProtocolSecurityCapabilities getSecurityCapabilities() {
        return masterProtocol;
    }

    protected DeviceMessageSupport getDeviceMessageSupport() {
        return idisMBusMessaging;
    }
}
