package com.energyict.protocolimplv2.dlms.idis.am130;

import com.energyict.mdc.protocol.security.DeviceProtocolSecurityCapabilities;
import com.energyict.mdc.protocol.tasks.support.DeviceMessageSupport;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.AbstractDlmsSlaveProtocol;
import com.energyict.protocolimplv2.dlms.idis.am500.messages.mbus.IDISMBusMessaging;

/**
 * Copyrights EnergyICT
 * <p/>
 * Logical slave protocol that does not read out any data, it's merely a placeholder for the supported messages, properties, ...
 * The read out of the MBus registers, logbook, load profile, etc is implemented in the AM130 e-meter (master) protocol.
 *
 * @author khe
 * @since 15/01/2015 - 9:19
 */
public class MBusDevice extends AbstractDlmsSlaveProtocol {

    private final AbstractDlmsProtocol masterProtocol = new AM130();
    private final IDISMBusMessaging idisMBusMessaging = new IDISMBusMessaging(masterProtocol);

    @Override
    public String getProtocolDescription() {
        return "Elster AM130 DLMS (IDIS P2) MBus slave";
    }

    @Override
    public String getVersion() {
        return "$Date: 2015-07-02 13:30:55 +0200 (Thu, 02 Jul 2015) $";
    }

    protected DeviceProtocolSecurityCapabilities getSecurityCapabilities() {
        return masterProtocol;
    }

    protected DeviceMessageSupport getDeviceMessageSupport() {
        return idisMBusMessaging;
    }
}