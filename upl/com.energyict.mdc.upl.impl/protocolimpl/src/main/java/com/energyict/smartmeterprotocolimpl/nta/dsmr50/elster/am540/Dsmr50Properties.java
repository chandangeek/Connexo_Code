package com.energyict.smartmeterprotocolimpl.nta.dsmr50.elster.am540;

import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.dlms.aso.SecurityProvider;
import com.energyict.protocolimpl.dlms.g3.G3Properties;
import com.energyict.protocolimpl.dlms.g3.G3SecurityProvider;
import com.energyict.protocolimpl.nls.PropertyTranslationKeys;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.Dsmr40Properties;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static com.energyict.protocolimpl.dlms.g3.G3Properties.AARQ_RETRIES;
import static com.energyict.protocolimpl.dlms.g3.G3Properties.AARQ_TIMEOUT;
import static com.energyict.protocolimpl.dlms.g3.G3Properties.PROP_LASTSEENDATE;
import static com.energyict.protocolimpl.dlms.g3.G3Properties.PSK;

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

    public Dsmr50Properties(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    public Dsmr50Properties(Properties properties, PropertySpecService propertySpecService) {
        super(properties, propertySpecService);
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        List<PropertySpec> propertySpecs = new ArrayList<>(super.getUPLPropertySpecs());
        propertySpecs.add(UPLPropertySpecFactory.specBuilder(PROP_LASTSEENDATE, false, PropertyTranslationKeys.NTA_LAST_SEND_DATE, this.getPropertySpecService()::stringSpec).finish());
        propertySpecs.add(UPLPropertySpecFactory.specBuilder(AARQ_RETRIES, false, PropertyTranslationKeys.NTA_AARQ_RETRIES, this.getPropertySpecService()::integerSpec).finish());
        propertySpecs.add(UPLPropertySpecFactory.specBuilder(AARQ_TIMEOUT, false, PropertyTranslationKeys.NTA_AARQ_TIMEOUT, this.getPropertySpecService()::integerSpec).finish());
        propertySpecs.add(UPLPropertySpecFactory.specBuilder(PSK, false, PropertyTranslationKeys.NTA_PSK, this.getPropertySpecService()::stringSpec).finish());
        propertySpecs.add(UPLPropertySpecFactory.specBuilder(CHECK_NUMBER_OF_BLOCKS_DURING_FIRMWARE_RESUME, false, PropertyTranslationKeys.NTA_CHECK_NUMBER_OF_BLOCKS_DURING_FIRMWARE_RESUME, this.getPropertySpecService()::integerSpec).finish());
        return propertySpecs;
    }

    /**
     * The G3 security provider allows us to immediately use the new keys (AK and or EK) after changing them in the meter.
     */
    @Override
    public SecurityProvider getSecurityProvider() {
        if (g3SecurityProvider == null) {
            g3SecurityProvider = new G3SecurityProvider(this.getPropertySpecService(), getProtocolProperties());
        }
        return g3SecurityProvider;
    }

    public int getAARQRetries() {
        return getIntProperty(AARQ_RETRIES, G3Properties.DEFAULT_AARQ_RETRIES);
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