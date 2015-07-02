package com.energyict.protocolimplv2.dlms.idis.am130.topology;

import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.idis.topology.IDISMeterTopology;

/**
 * Extension of the IDISMeterTopology, supports discovery of 6 MBus channels
 * <p/>
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 2/07/2015 - 11:11
 */
public class AM130MeterTopology extends IDISMeterTopology {

    private static final int MAX_MBUS_CHANNELS = 6;

    public AM130MeterTopology(AbstractDlmsProtocol protocol) {
        super(protocol);
    }

    @Override
    protected int getMaxMBusChannels() {
        return MAX_MBUS_CHANNELS;
    }
}