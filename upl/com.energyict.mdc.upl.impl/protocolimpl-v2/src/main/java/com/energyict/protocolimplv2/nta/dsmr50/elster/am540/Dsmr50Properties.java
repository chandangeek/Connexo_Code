package com.energyict.protocolimplv2.nta.dsmr50.elster.am540;

import com.energyict.protocol.MeterProtocol;
import com.energyict.protocolimplv2.nta.dsmr23.DlmsProperties;
import com.energyict.protocolimplv2.nta.dsmr50.Dsmr50ConfigurationSupport;

import java.math.BigDecimal;

import static com.energyict.dlms.common.DlmsProtocolProperties.DEFAULT_UPPER_SERVER_MAC_ADDRESS;

/**
 * Extension of the standard DLMS properties, adding DSMR50 stuff
 * <p/>
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 17/12/2014 - 17:27
 */
public class Dsmr50Properties extends DlmsProperties {

    public static final String AARQ_TIMEOUT_PROPERTY = "AARQTimeout";
    public static final String AARQ_RETRIES_PROPERTY = "AARQRetries";
    public static final String READCACHE_PROPERTY = "ReadCache";
    public static final String CumulativeCaptureTimeChannel = "CumulativeCaptureTimeChannel";

    /**
     * Property indicating to read the cache out (useful because there's no config change state)
     */
    public boolean isReadCache() {
        return getProperties().getTypedProperty(READCACHE_PROPERTY, false);
    }

    public boolean isCumulativeCaptureTimeChannel() {
        return getProperties().getTypedProperty(CumulativeCaptureTimeChannel, false);
    }

    public long getAARQTimeout() {
        return getProperties().getTypedProperty(AARQ_TIMEOUT_PROPERTY, BigDecimal.ZERO).longValue();
    }

    public int getAARQRetries() {
        return getProperties().getTypedProperty(AARQ_RETRIES_PROPERTY, BigDecimal.valueOf(2)).intValue();
    }

    @Override
    public int getServerUpperMacAddress() {
        return getNodeAddress();
    }

    public int getNodeAddress() {
        Object nodeAddressObject = getProperties().getTypedProperty(MeterProtocol.NODEID);
        if (nodeAddressObject == null) {
            return DEFAULT_UPPER_SERVER_MAC_ADDRESS.intValue();
        } else if (nodeAddressObject instanceof BigDecimal) {
            return ((BigDecimal) nodeAddressObject).intValue();
        } else {
            try {
                return Integer.parseInt((String) nodeAddressObject);
            } catch (NumberFormatException e) {
                return DEFAULT_UPPER_SERVER_MAC_ADDRESS.intValue();
            }
        }
    }

    public boolean getCheckNumberOfBlocksDuringFirmwareResume() {
        return getProperties().getTypedProperty(Dsmr50ConfigurationSupport.CHECK_NUMBER_OF_BLOCKS_DURING_FIRMWARE_RESUME, Dsmr50ConfigurationSupport.DEFAULT_CHECK_NUMBER_OF_BLOCKS_DURING_FIRMWARE_RESUME);
    }

    public boolean useEquipmentIdentifierAsSerialNumber() {
        return getProperties().getTypedProperty(Dsmr50ConfigurationSupport.USE_EQUIPMENT_IDENTIFIER_AS_SERIAL, Dsmr50ConfigurationSupport.USE_EQUIPMENT_IDENTIFIER_AS_SERIAL_DEFAULT_VALUE);
    }
}