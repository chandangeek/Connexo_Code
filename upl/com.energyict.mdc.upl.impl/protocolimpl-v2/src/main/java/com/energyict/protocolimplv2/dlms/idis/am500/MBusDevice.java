package com.energyict.protocolimplv2.dlms.idis.am500;

import com.energyict.mdc.protocol.tasks.support.DeviceMessageSupport;
import com.energyict.protocolimplv2.dlms.AbstractDlmsMbusProtocol;
import com.energyict.protocolimplv2.dlms.idis.am500.messages.mbus.IDISMBusMessaging;
import com.energyict.protocolimplv2.nta.abstractnta.AbstractDlmsProtocol;

/**
 * Copyrights EnergyICT
 * <p/>
 * Logical slave protocol that does not read out any data, it's merely a placeholder for the supported messages, properties, ...
 * The read out of the MBus registers, logbook, load profile, etc is implemented in the AM500 e-meter (master) protocol.
 *
 * @author khe
 * @since 15/01/2015 - 9:19
 */
public class MBusDevice extends AbstractDlmsMbusProtocol {

    private final AbstractDlmsProtocol masterProtocol = new AM500();
    private final IDISMBusMessaging idisMBusMessaging = new IDISMBusMessaging(masterProtocol);

    @Override
    public String getProtocolDescription() {
        return "AM500 DLMS (IDIS P1) MBus slave V2";
    }

    @Override
    public String getVersion() {
        return "$Date$";
    }

    protected AbstractDlmsProtocol getMasterProtocol() {
        return masterProtocol;
    }

    protected DeviceMessageSupport getDeviceMessageSupport() {
        return idisMBusMessaging;
    }
}