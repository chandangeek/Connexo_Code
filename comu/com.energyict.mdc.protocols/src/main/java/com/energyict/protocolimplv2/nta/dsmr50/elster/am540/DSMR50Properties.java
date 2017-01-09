package com.energyict.protocolimplv2.nta.dsmr50.elster.am540;


import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.pluggable.DeviceProtocolDialectPropertyRelationAttributeTypeNames;
import com.energyict.mdc.upl.properties.HexString;
import com.energyict.protocolimplv2.DeviceProtocolDialectName;
import com.energyict.protocolimplv2.dlms.g3.properties.AS330DConfigurationSupport;
import com.energyict.protocolimplv2.g3.common.G3Properties;
import com.energyict.protocolimplv2.security.SecurityPropertySpecName;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Extension of the standard DLMS properties, adding DSMR50 stuff.
 * <p>
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 17/12/2014 - 17:27
 */
public class DSMR50Properties extends G3Properties {

    public static final String AARQ_TIMEOUT_PROPERTY = "AARQTimeout";
    public static final String AARQ_RETRIES_PROPERTY = "AARQRetries";
    public static final String READCACHE_PROPERTY = "ReadCache";
    public static final String CumulativeCaptureTimeChannel = "CumulativeCaptureTimeChannel";
    public static final String PSK_PROPERTY = "PSK";
    public static final String CHECK_NUMBER_OF_BLOCKS_DURING_FIRMWARE_RESUME = "CheckNumberOfBlocksDuringFirmwareResume";
    public static final String USE_EQUIPMENT_IDENTIFIER_AS_SERIAL = "UseEquipmentIdentifierAsSerialNumber";
    private static final TimeDuration DEFAULT_AARQ_TIMEOUT_PROPERTY = TimeDuration.NONE;
    private static final BigDecimal DEFAULT_AARQ_RETRIES_PROPERTY = BigDecimal.valueOf(2);
    private static final boolean DEFAULT_CHECK_NUMBER_OF_BLOCKS_DURING_FIRMWARE_RESUME = true;
    private static final boolean USE_EQUIPMENT_IDENTIFIER_AS_SERIAL_DEFAULT_VALUE = true;
    private static final int PUBLIC_CLIENT_MAC_ADDRESS = 16;

    public DSMR50Properties(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super(propertySpecService, thesaurus);
    }

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
        return getProperties().getTypedProperty(AARQ_TIMEOUT_PROPERTY, DEFAULT_AARQ_TIMEOUT_PROPERTY).getMilliSeconds();
    }

    public long getAARQRetries() {
        return getProperties().getTypedProperty(AARQ_RETRIES_PROPERTY, DEFAULT_AARQ_RETRIES_PROPERTY).longValue();
    }

    private PropertySpec readCachePropertySpec() {
        return getPropertySpecService()
                .booleanSpec()
                .named(READCACHE_PROPERTY, TranslationKeys.READCACHE_PROPERTY)
                .fromThesaurus(getThesaurus())
                .setDefaultValue(false)
                .finish();
    }

    private PropertySpec aarqTimeoutPropertySPec() {
        return getPropertySpecService()
                .temporalAmountSpec()
                .named(AARQ_TIMEOUT_PROPERTY, TranslationKeys.AARQ_TIMEOUT_PROPERTY)
                .fromThesaurus(getThesaurus())
                .setDefaultValue(DEFAULT_AARQ_TIMEOUT_PROPERTY)
                .finish();
    }

    private PropertySpec aarqRetriesPropertySPec() {
        return getPropertySpecService()
                .bigDecimalSpec()
                .named(AARQ_RETRIES_PROPERTY, TranslationKeys.AARQ_RETRIES_PROPERTY)
                .fromThesaurus(getThesaurus())
                .setDefaultValue(DEFAULT_AARQ_RETRIES_PROPERTY)
                .finish();
    }

    private PropertySpec pskPropertySPec() {
        return getPropertySpecService()
                .hexStringSpec()
                .named(PSK_PROPERTY, TranslationKeys.PSK_PROPERTY)
                .fromThesaurus(getThesaurus())
                .finish();
    }

    private PropertySpec cumulativeCaptureTimePropertySPec() {
        return getPropertySpecService()
                .booleanSpec()
                .named(CumulativeCaptureTimeChannel, TranslationKeys.CumulativeCaptureTimeChannel)
                .fromThesaurus(getThesaurus())
                .setDefaultValue(false)
                .finish();
    }

    @Override
    public int getServerUpperMacAddress() {
        if (useBeaconMirrorDeviceDialect()) {
            return getMirrorLogicalDeviceId();  // The Beacon mirrored device
        } else if (useBeaconGatewayDeviceDialect()) {
            return getGatewayLogicalDeviceId(); // Beacon acts as a gateway
        } else {
            return super.getServerUpperMacAddress();
        }
    }

    private int getMirrorLogicalDeviceId() {
        return getProperties().getTypedProperty(AS330DConfigurationSupport.MIRROR_LOGICAL_DEVICE_ID);
    }

    private int getGatewayLogicalDeviceId() {
        return getProperties().getTypedProperty(AS330DConfigurationSupport.GATEWAY_LOGICAL_DEVICE_ID);
    }

    @Override
    public int getClientMacAddress() {
        if (useBeaconMirrorDeviceDialect() && !usesPublicClient()) {
            return BigDecimal.ONE.intValue();   // When talking to the Beacon mirrored device, we should always use client address 1 (except for the public client, which is always 16)
        } else {
            return parseBigDecimalProperty(SecurityPropertySpecName.CLIENT_MAC_ADDRESS.getKey(), BigDecimal.ONE);
        }
    }

    public boolean usesPublicClient() {
        return parseBigDecimalProperty(SecurityPropertySpecName.CLIENT_MAC_ADDRESS.getKey(), BigDecimal.ONE) == PUBLIC_CLIENT_MAC_ADDRESS;
    }

    public boolean useBeaconMirrorDeviceDialect() {
        String dialectName = getProperties().getStringProperty(DeviceProtocolDialectPropertyRelationAttributeTypeNames.DEVICE_PROTOCOL_DIALECT_ATTRIBUTE_NAME);
        return dialectName != null && dialectName.equals(DeviceProtocolDialectName.BEACON_MIRROR_TCP_DLMS_PROTOCOL.getName());
    }

    public boolean useBeaconGatewayDeviceDialect() {
        String dialectName = getProperties().getStringProperty(DeviceProtocolDialectPropertyRelationAttributeTypeNames.DEVICE_PROTOCOL_DIALECT_ATTRIBUTE_NAME);
        return dialectName != null && dialectName.equals(DeviceProtocolDialectName.BEACON_GATEWAY_TCP_DLMS_PROTOCOL.getName());
    }


    @Override
    public List<PropertySpec> getPropertySpecs() {
        List<PropertySpec> propertySpecs = new ArrayList<>(super.getPropertySpecs());
        propertySpecs.add(aarqTimeoutPropertySPec());
        propertySpecs.add(aarqRetriesPropertySPec());
        propertySpecs.add(pskPropertySPec());
        propertySpecs.add(cumulativeCaptureTimePropertySPec());
        propertySpecs.add(readCachePropertySpec());
        propertySpecs.add(checkNumberOfBlocksDuringFirmwareResumePropertySpec());
        propertySpecs.add(useEquipmentIdentifierAsSerialNumberPropertySpec());
        return propertySpecs;
    }

    public boolean getCheckNumberOfBlocksDuringFirmwareResume() {
        return getProperties().getTypedProperty(CHECK_NUMBER_OF_BLOCKS_DURING_FIRMWARE_RESUME, DEFAULT_CHECK_NUMBER_OF_BLOCKS_DURING_FIRMWARE_RESUME);
    }

    public boolean useEquipmentIdentifierAsSerialNumber() {
        return getProperties().getTypedProperty(USE_EQUIPMENT_IDENTIFIER_AS_SERIAL, USE_EQUIPMENT_IDENTIFIER_AS_SERIAL_DEFAULT_VALUE);
    }

    private PropertySpec checkNumberOfBlocksDuringFirmwareResumePropertySpec() {
        return getPropertySpecService()
                .booleanSpec()
                .named(CHECK_NUMBER_OF_BLOCKS_DURING_FIRMWARE_RESUME, TranslationKeys.CHECK_NUMBER_OF_BLOCKS_DURING_FIRMWARE_RESUME)
                .fromThesaurus(getThesaurus())
                .setDefaultValue(DEFAULT_CHECK_NUMBER_OF_BLOCKS_DURING_FIRMWARE_RESUME)
                .finish();
    }

    private PropertySpec useEquipmentIdentifierAsSerialNumberPropertySpec() {
        return getPropertySpecService()
                .booleanSpec()
                .named(USE_EQUIPMENT_IDENTIFIER_AS_SERIAL, TranslationKeys.USE_EQUIPMENT_IDENTIFIER_AS_SERIAL)
                .fromThesaurus(getThesaurus())
                .setDefaultValue(USE_EQUIPMENT_IDENTIFIER_AS_SERIAL_DEFAULT_VALUE)
                .finish();
    }

}