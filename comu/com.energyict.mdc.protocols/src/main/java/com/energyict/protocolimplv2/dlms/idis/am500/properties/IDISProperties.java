package com.energyict.protocolimplv2.dlms.idis.am500.properties;


import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.protocol.api.exceptions.DeviceConfigurationException;

import com.energyict.protocolimpl.dlms.idis.IDIS;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.DeviceProtocolDialectName;
import com.energyict.protocolimplv2.dlms.DlmsProperties;

import java.math.BigDecimal;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 19/12/2014 - 16:39
 */
public class IDISProperties extends DlmsProperties {

    public static final String READCACHE_PROPERTY = "ReadCache";

    public IDISProperties(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super(propertySpecService, thesaurus);
    }

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
        String dialectName = getProperties().getStringProperty(DeviceProtocolDialect.DEVICE_PROTOCOL_DIALECT_NAME);
        return dialectName != null && dialectName.equals(DeviceProtocolDialectName.BEACON_MIRROR_TCP_DLMS_PROTOCOL.getName());
    }

    public boolean useSerialDialect() {
        String dialectName = getProperties().getStringProperty(DeviceProtocolDialect.DEVICE_PROTOCOL_DIALECT_NAME);
        return dialectName != null && dialectName.equals(DeviceProtocolDialectName.SERIAL_DLMS_PROTOCOL.getName());
    }

    public boolean useLogicalDeviceNameAsSerialNumber(){
        return getProperties().<Boolean>getTypedProperty(IDISConfigurationSupport.USE_LOGICAL_DEVICE_NAME_AS_SERIAL, false);
    }

    public boolean useUndefinedAsTimeDeviation(){
        return getProperties().<Boolean>getTypedProperty(IDISConfigurationSupport.USE_UNDEFINED_AS_TIME_DEVIATION, false);
    }
}