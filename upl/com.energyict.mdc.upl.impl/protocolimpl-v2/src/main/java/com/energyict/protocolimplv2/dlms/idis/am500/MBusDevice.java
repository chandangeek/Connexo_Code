package com.energyict.protocolimplv2.dlms.idis.am500;

import com.energyict.mdc.upl.security.DeviceProtocolSecurityCapabilities;
import com.energyict.mdc.upl.tasks.support.DeviceMessageSupport;

import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.AbstractDlmsSlaveProtocol;
import com.energyict.protocolimplv2.dlms.idis.am500.messages.mbus.IDISMBusMessaging;

/**
 * Copyrights EnergyICT
 * <p/>
 * Logical slave protocol that does not read out any data, it's merely a placeholder for the supported messages, properties, ...
 * The read out of the MBus registers, logbook, load profile, etc is implemented in the AM500 e-meter (master) protocol.
 *
 * @author khe
 * @since 15/01/2015 - 9:19
 */
public class MBusDevice extends AbstractDlmsSlaveProtocol {

    private final AbstractDlmsProtocol masterProtocol = new AM500();
    private final IDISMBusMessaging idisMBusMessaging = new IDISMBusMessaging(masterProtocol);

    @Override
    public String getProtocolDescription() {
        return "Elster AM500 DLMS (IDIS P1) MBus slave V2";
    }

    @Override
    public String getVersion() {
        return "$Date: 2016-01-25 15:02:12 +0100 (Mon, 25 Jan 2016)$";
    }

    protected DeviceProtocolSecurityCapabilities getSecurityCapabilities() {
        return masterProtocol;
    }

    protected DeviceMessageSupport getDeviceMessageSupport() {
        return idisMBusMessaging;
    }
}