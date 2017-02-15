package com.energyict.protocolimplv2.dlms.g3.properties;

import com.energyict.mdc.protocol.LegacyProtocolProperties;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.nls.PropertyTranslationKeys;
import com.energyict.protocolimpl.dlms.g3.G3Properties;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;
import com.energyict.protocolimplv2.nta.dsmr23.DlmsConfigurationSupport;
import com.energyict.protocolimplv2.nta.dsmr50.elster.am540.Dsmr50Properties;

import java.math.BigDecimal;
import java.util.List;

import static com.energyict.dlms.common.DlmsProtocolProperties.MAX_REC_PDU_SIZE;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 10/06/2015 - 10:52
 */
public class AS330DConfigurationSupport extends DlmsConfigurationSupport {

    public static final String READCACHE_PROPERTY = "ReadCache";
    public static final String MIRROR_LOGICAL_DEVICE_ID = "MirrorLogicalDeviceId";
    public static final String GATEWAY_LOGICAL_DEVICE_ID = "GatewayLogicalDeviceId";

    public AS330DConfigurationSupport(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        List<PropertySpec> propertySpecs = super.getUPLPropertySpecs();
        propertySpecs.add(pskPropertySpec());
        propertySpecs.add(aarqTimeoutPropertySpec());
        propertySpecs.add(aarqRetriesPropertySpec());
        propertySpecs.add(readCachePropertySpec());
        propertySpecs.add(callHomeIdPropertySpec());
        propertySpecs.add(mirrorLogicalDeviceIdPropertySpec());
        propertySpecs.add(actualLogicalDeviceIdPropertySpec());
        propertySpecs.remove(ntaSimulationToolPropertySpec());
        propertySpecs.remove(serverUpperMacAddressPropertySpec());
        propertySpecs.remove(serverLowerMacAddressPropertySpec());
        propertySpecs.remove(manufacturerPropertySpec());
        propertySpecs.remove(fixMbusHexShortIdPropertySpec());
        propertySpecs.remove(deviceId());
        return propertySpecs;
    }

    private PropertySpec callHomeIdPropertySpec() {
        return UPLPropertySpecFactory
                .specBuilder(LegacyProtocolProperties.CALL_HOME_ID_PROPERTY_NAME, false, PropertyTranslationKeys.V2_DLMS_CALL_HOME_ID, this.getPropertySpecService()::stringSpec)
                .finish();
    }

    private PropertySpec mirrorLogicalDeviceIdPropertySpec() {
        return UPLPropertySpecFactory
                .specBuilder(MIRROR_LOGICAL_DEVICE_ID, false, PropertyTranslationKeys.V2_DLMS_MIRROR_LOGICAL_DEVICE_ID, this.getPropertySpecService()::bigDecimalSpec)
                .finish();
    }

    private PropertySpec actualLogicalDeviceIdPropertySpec() {
        return UPLPropertySpecFactory
                .specBuilder(GATEWAY_LOGICAL_DEVICE_ID, false, PropertyTranslationKeys.V2_DLMS_GATEWAY_LOGICAL_DEVICE_ID, this.getPropertySpecService()::bigDecimalSpec)
                .finish();
    }

    private PropertySpec pskPropertySpec() {
        return UPLPropertySpecFactory
                .specBuilder(G3Properties.PSK, false, PropertyTranslationKeys.V2_DLMS_PSK, this.getPropertySpecService()::hexStringSpec)
                .finish();
    }

    private PropertySpec aarqTimeoutPropertySpec() {
        return UPLPropertySpecFactory
                .specBuilder(Dsmr50Properties.AARQ_TIMEOUT_PROPERTY, false, PropertyTranslationKeys.V2_DLMS_AARQ_TIMEOUT, this.getPropertySpecService()::bigDecimalSpec)
                .setDefaultValue(BigDecimal.ZERO)
                .finish();
    }

    private PropertySpec readCachePropertySpec() {
        return UPLPropertySpecFactory
                .specBuilder(READCACHE_PROPERTY, false, PropertyTranslationKeys.V2_DLMS_READCACHE, this.getPropertySpecService()::booleanSpec)
                .setDefaultValue(false)
                .finish();
    }

    private PropertySpec aarqRetriesPropertySpec() {
        return UPLPropertySpecFactory
                .specBuilder(Dsmr50Properties.AARQ_RETRIES_PROPERTY, false, PropertyTranslationKeys.V2_DLMS_AARQ_RETRIES, this.getPropertySpecService()::bigDecimalSpec)
                .setDefaultValue(BigDecimal.valueOf(2))
                .finish();
    }

    protected PropertySpec maxRecPduSizePropertySpec() {
        return UPLPropertySpecFactory
                .specBuilder(MAX_REC_PDU_SIZE, false, PropertyTranslationKeys.V2_DLMS_MAX_REC_PDU_SIZE, this.getPropertySpecService()::bigDecimalSpec)
                .setDefaultValue(AS330DProperties.DEFAULT_MAX_REC_PDU_SIZE)
                .finish();
    }

}