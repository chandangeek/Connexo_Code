package test.com.energyict.protocolimplV2.nta.dsmr23;

import com.energyict.dlms.DLMSReference;
import com.energyict.dlms.aso.SecurityProvider;
import com.energyict.protocolimpl.base.ProtocolProperty;
import com.energyict.dlms.common.DlmsProtocolProperties;
import test.com.energyict.protocolimplV2.nta.abstractnta.NTASecurityProvider;

import java.math.BigDecimal;

/**
 * Copyrights EnergyICT
 * Date: 14-jul-2011
 * Time: 11:26:48
 */
public class Dsmr23Properties extends DlmsProtocolProperties {

    public static final String OLD_MBUS_DISCOVERY = "OldMbusDiscovery";
    public static final String FIX_MBUS_HEX_SHORT_ID = "FixMbusHexShortId";

    public static final BigDecimal DEFAULT_CLIENT_MAC_ADDRESS = new BigDecimal(1);
    public static final Boolean DEFAULT_FIX_MBUS_HEX_SHORT_ID = false;
    public static final Boolean DEFAULT_OLD_MBUS_DISCOVERY = false;
    public static final Boolean DEFAULT_BULK_REQUEST = true;


    public DLMSReference getReference() {
        return DLMSReference.LN;
    }

    @ProtocolProperty
    public boolean getFixMbusHexShortId() {
        return getBooleanProperty(FIX_MBUS_HEX_SHORT_ID, DEFAULT_FIX_MBUS_HEX_SHORT_ID);
    }

    @ProtocolProperty
    public boolean getOldMbusDiscovery() {
        return getBooleanProperty(OLD_MBUS_DISCOVERY, DEFAULT_OLD_MBUS_DISCOVERY);
    }

    @Override
    public SecurityProvider getSecurityProvider() {
        return new NTASecurityProvider(getProtocolProperties());
    }

    @ProtocolProperty
    public boolean isBulkRequest() {
        return getBooleanProperty(BULK_REQUEST, DEFAULT_BULK_REQUEST);
    }

    @ProtocolProperty
    public int getClientMacAddress() {
        return getIntProperty(CLIENT_MAC_ADDRESS, DEFAULT_CLIENT_MAC_ADDRESS);
    }
}
