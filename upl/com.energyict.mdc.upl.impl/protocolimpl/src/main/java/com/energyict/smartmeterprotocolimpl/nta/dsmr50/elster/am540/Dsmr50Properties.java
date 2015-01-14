package com.energyict.smartmeterprotocolimpl.nta.dsmr50.elster.am540;

import com.energyict.dlms.aso.SecurityProvider;
import com.energyict.protocolimpl.dlms.g3.G3Properties;
import com.energyict.protocolimpl.dlms.g3.G3SecurityProvider;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.Dsmr40Properties;

import java.util.List;

/**
 * Copyrights EnergyICT
 * This class combines the DSMR4.0 properties with some of the G3 properties.
 * Note that the server mac address property is overruled with the contents of the node address field (RMR tab)
 *
 * @author khe
 * @since 6/06/2014 - 14:42
 */
public class Dsmr50Properties extends Dsmr40Properties {

    private static final String CHECK_NUMBER_OF_BLOCKS_DURING_FIRMWARE_RESUME = "CheckNumberOfBlocksDuringFirmwareResume";

    private G3SecurityProvider g3SecurityProvider;

    @Override
    public List<String> getOptionalKeys() {
        List<String> optionals = super.getOptionalKeys();
        optionals.add(G3Properties.PROP_LASTSEENDATE);
        optionals.add(G3Properties.AARQ_RETRIES);
        optionals.add(G3Properties.AARQ_TIMEOUT);
        optionals.add(G3Properties.PSK);
        optionals.add(CHECK_NUMBER_OF_BLOCKS_DURING_FIRMWARE_RESUME);
        return optionals;
    }

    /**
     * The G3 security provider allows us to immediately use the new keys (AK and or EK) after changing them in the meter.
     */
    @Override
    public SecurityProvider getSecurityProvider() {
        if (g3SecurityProvider == null) {
            g3SecurityProvider = new G3SecurityProvider(getProtocolProperties());
        }
        return g3SecurityProvider;
    }

    public int getAARQRetries() {
        return getIntProperty(G3Properties.AARQ_RETRIES, G3Properties.DEFAULT_AARQ_RETRIES);
    }

    @Override
    public String getServerMacAddress() {
        final String oldMacAddress = getStringValue(SERVER_MAC_ADDRESS, DEFAULT_SERVER_MAC_ADDRESS);
        return oldMacAddress.replaceAll("x", getNodeAddress());
    }

    public int getAARQTimeout() {
        return getIntProperty(G3Properties.AARQ_TIMEOUT, G3Properties.DEFAULT_AARQ_TIMEOUT);
    }

    public boolean getCheckNumberOfBlocksDuringFirmwareResume() {
        return getBooleanProperty(CHECK_NUMBER_OF_BLOCKS_DURING_FIRMWARE_RESUME, "1");
    }

}