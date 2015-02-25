package com.energyict.protocolimplv2.nta.dsmr50.elster.am540;

import com.energyict.mdc.protocol.security.DeviceProtocolSecurityCapabilities;
import com.energyict.mdc.protocol.tasks.support.DeviceMessageSupport;
import com.energyict.protocolimplv2.dlms.AbstractDlmsMbusProtocol;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.idis.am500.messages.mbus.IDISMBusMessaging;

/**
 * Copyrights EnergyICT
 * <p/>
 * Logical slave protocol that does not read out any data, it's merely a placeholder for the supported messages, properties, ...
 * The read out of the MBus registers, logbook, load profile, etc is implemented in the AM450 e-meter (master) protocol.
 *
 * @author sva
 * @since 23/01/2015 - 9:26
 */
public class MBusDevice extends AbstractDlmsMbusProtocol {

    private final AbstractDlmsProtocol masterProtocol = new AM540();
    private final IDISMBusMessaging idisMBusMessaging = new IDISMBusMessaging(masterProtocol);

    @Override
    public String getProtocolDescription() {
        return "Elster AM540 DLMS (IDIS P2) MBus slave V2";
    }

    @Override
    public String getVersion() {
        return "$Date$";
    }

    protected DeviceProtocolSecurityCapabilities getSecurityCapabilities() {
        return masterProtocol;
    }

    protected DeviceMessageSupport getDeviceMessageSupport() {
        return idisMBusMessaging;
    }
}