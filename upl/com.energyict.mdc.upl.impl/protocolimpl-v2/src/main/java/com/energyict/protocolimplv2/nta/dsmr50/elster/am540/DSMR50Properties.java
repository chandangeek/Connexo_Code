package com.energyict.protocolimplv2.nta.dsmr50.elster.am540;

import com.energyict.cbo.HexString;
import com.energyict.protocol.MeterProtocol;
import com.energyict.protocolimplv2.nta.dsmr23.DlmsProperties;

import java.math.BigDecimal;

import static com.energyict.dlms.common.DlmsProtocolProperties.DEFAULT_UPPER_SERVER_MAC_ADDRESS;
import static com.energyict.dlms.common.DlmsProtocolProperties.SERVER_UPPER_MAC_ADDRESS;

/**
 * Extension of the standard DLMS properties, adding DSMR50 stuff
 * <p/>
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 17/12/2014 - 17:27
 */
public class DSMR50Properties extends DlmsProperties {

    public static final String AARQ_TIMEOUT_PROPERTY = "AARQTimeout";
    public static final String AARQ_RETRIES_PROPERTY = "AARQRetries";
    public static final String READCACHE_PROPERTY = "ReadCache";
    public static final String CumulativeCaptureTimeChannel = "CumulativeCaptureTimeChannel";
    public static final String PSK_PROPERTY = "PSK";
    public static final String CHECK_NUMBER_OF_BLOCKS_DURING_FIRMWARE_RESUME = "CheckNumberOfBlocksDuringFirmwareResume";

    /**
     * Property indicating to read the cache out (useful because there's no config change state)
     */
    public boolean isReadCache() {
        return getProperties().<Boolean>getTypedProperty(READCACHE_PROPERTY, false);
    }

    public boolean isCumulativeCaptureTimeChannel() {
        return getProperties().<Boolean>getTypedProperty(CumulativeCaptureTimeChannel, false);
    }

    public HexString getPSK() { //TODO use this for the push event notification mechanism
        return getProperties().<HexString>getTypedProperty(PSK_PROPERTY);
    }

    public long getAARQTimeout() {
        return getProperties().<BigDecimal>getTypedProperty(AARQ_TIMEOUT_PROPERTY, BigDecimal.ZERO).longValue();
    }

    public long getAARQRetries() {
        return getProperties().<BigDecimal>getTypedProperty(AARQ_RETRIES_PROPERTY, BigDecimal.valueOf(2)).longValue();
    }

    @Override
    public int getServerUpperMacAddress() {
        final int oldMacAddress = parseBigDecimalProperty(SERVER_UPPER_MAC_ADDRESS, new BigDecimal(-1));
        if (oldMacAddress == -1) {
            return getNodeAddress();
        }
        return oldMacAddress;
    }

    public int getNodeAddress() {
        return getProperties().<BigDecimal>getTypedProperty(MeterProtocol.NODEID, DEFAULT_UPPER_SERVER_MAC_ADDRESS).intValue();
    }

    public boolean getCheckNumberOfBlocksDuringFirmwareResume() {
        return getProperties().<Boolean>getTypedProperty(CHECK_NUMBER_OF_BLOCKS_DURING_FIRMWARE_RESUME, true);
    }
}