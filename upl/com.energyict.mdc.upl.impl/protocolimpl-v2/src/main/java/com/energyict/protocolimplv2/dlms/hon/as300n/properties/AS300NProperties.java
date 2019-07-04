package com.energyict.protocolimplv2.dlms.hon.as300n.properties;

import com.energyict.protocol.exception.DeviceConfigurationException;
import com.energyict.protocolimpl.dlms.idis.IDIS;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.DeviceProtocolDialectTranslationKeys;
import com.energyict.protocolimplv2.dlms.idis.am500.properties.IDISConfigurationSupport;
import com.energyict.protocolimplv2.nta.dsmr23.DlmsProperties;

import java.math.BigDecimal;

import static com.energyict.dlms.common.DlmsProtocolProperties.ADDRESSING_MODE;
import static com.energyict.dlms.common.DlmsProtocolProperties.BULK_REQUEST;
import static com.energyict.mdc.upl.DeviceProtocolDialect.Property.DEVICE_PROTOCOL_DIALECT;

public class AS300NProperties extends DlmsProperties {
    private static final int PUBLIC_CLIENT_MAC_ADDRESS = 16;
    public static final String READCACHE_PROPERTY = "ReadCache";
    public static final Boolean DEFAULT_BULK_REQUEST = true;

    /**
     * Property indicating to read the cache out (useful because there's no config change state)
     */
    public boolean isReadCache() {
        return getProperties().<Boolean>getTypedProperty(READCACHE_PROPERTY, false);
    }

    @Override
    public byte[] getSystemIdentifier() {
        //Property CallingAPTitle is used as system identifier in the AARQ
        final boolean ignoreCallingAPTitle = getProperties().getTypedProperty(IDISConfigurationSupport.IGNORE_CALLING_AP_TITLE, false);
        if (!ignoreCallingAPTitle) {
            final String callingAPTitle = getProperties().getTypedProperty(IDIS.CALLING_AP_TITLE, IDIS.CALLING_AP_TITLE_DEFAULT).trim();
            if (callingAPTitle.isEmpty()) {
                return super.getSystemIdentifier();
            } else {
                try {
                    return ProtocolTools.getBytesFromHexString(callingAPTitle, "");
                } catch (Throwable e) {
                    throw DeviceConfigurationException.invalidPropertyFormat(IDIS.CALLING_AP_TITLE, callingAPTitle, "Should be a hex string of 16 characters");
                }
            }
        }
        return null;
    }

    @Override
    public boolean isSwitchAddresses() {
        return getProperties().<Boolean>getTypedProperty(IDISConfigurationSupport.SWAP_SERVER_AND_CLIENT_ADDRESS_PROPERTY, true);
    }

    public long getLimitMaxNrOfDays() {
        return getProperties().getTypedProperty(
                IDISConfigurationSupport.LIMIT_MAX_NR_OF_DAYS_PROPERTY,
                BigDecimal.valueOf(0)   // Do not limit, but use as-is
        ).longValue();
    }

    @Override
    public boolean timeoutMeansBrokenConnection() {
        return useBeaconMirrorDeviceDialect() || useSerialDialect();
    }

    public boolean useBeaconMirrorDeviceDialect() {
        String dialectName = getProperties().getStringProperty(DEVICE_PROTOCOL_DIALECT.getName());
        return dialectName != null && dialectName.equals(DeviceProtocolDialectTranslationKeys.BEACON_MIRROR_TCP_DLMS_PROTOCOL_DIALECT_NAME.getName());
    }

    public boolean useSerialDialect() {
        String dialectName = getProperties().getStringProperty(DEVICE_PROTOCOL_DIALECT.getName());
        return dialectName != null && dialectName.equals(DeviceProtocolDialectTranslationKeys.SERIAL_DLMS_PROTOCOL_DIALECT_NAME.getName());
    }

    public boolean useLogicalDeviceNameAsSerialNumber() {
        return getProperties().<Boolean>getTypedProperty(IDISConfigurationSupport.USE_LOGICAL_DEVICE_NAME_AS_SERIAL, false);
    }

    public boolean useUndefinedAsTimeDeviation() {
        return getProperties().<Boolean>getTypedProperty(IDISConfigurationSupport.USE_UNDEFINED_AS_TIME_DEVIATION, false);
    }

    @Override
    public boolean isBulkRequest() {
        return getProperties().<Boolean>getTypedProperty(BULK_REQUEST, DEFAULT_BULK_REQUEST);
    }

    @Override
    public boolean incrementFrameCounterForRetries() {
        return true;
    }

    public boolean usesPublicClient() {
        return getClientMacAddress() == PUBLIC_CLIENT_MAC_ADDRESS;
    }


    public boolean getRequestAuthenticatedFrameCounter() {
        return getProperties().getTypedProperty(AS300NConfigurationSupport.REQUEST_AUTHENTICATED_FRAME_COUNTER, false);
    }

    public boolean useCachedFrameCounter() {
        return getProperties().getTypedProperty(AS300NConfigurationSupport.USE_CACHED_FRAME_COUNTER, false);
    }

    public boolean validateCachedFrameCounter() {
        return getProperties().getTypedProperty(AS300NConfigurationSupport.VALIDATE_CACHED_FRAMECOUNTER, true);
    }

    public int getFrameCounterRecoveryRetries() {
        return getProperties().getTypedProperty(AS300NConfigurationSupport.FRAME_COUNTER_RECOVERY_RETRIES, BigDecimal.valueOf(100)).intValue();
    }

    public int getFrameCounterRecoveryStep() {
        return getProperties().getTypedProperty(AS300NConfigurationSupport.FRAME_COUNTER_RECOVERY_STEP, BigDecimal.ONE).intValue();
    }

    public long getInitialFrameCounter() {
        return getProperties().getTypedProperty(AS300NConfigurationSupport.INITIAL_FRAME_COUNTER, BigDecimal.valueOf(100)).longValue();
    }

    public int getAARQRetries() {
        return getProperties().getTypedProperty(AS300NConfigurationSupport.AARQ_RETRIES_PROPERTY, BigDecimal.valueOf(2)).intValue();
    }

    public long getAARQTimeout() {
        return getProperties().getTypedProperty(AS300NConfigurationSupport.AARQ_TIMEOUT_PROPERTY, AS300NConfigurationSupport.DEFAULT_NOT_USED_AARQ_TIMEOUT).toMillis();
    }

    @Override
    public int getAddressingMode() {
        return getProperties().getTypedProperty(ADDRESSING_MODE, new BigDecimal(4)).intValue();
    }
}