package com.energyict.protocolimplv2.dlms.g3.properties;

import com.energyict.mdc.protocol.LegacyProtocolProperties;
import com.energyict.mdc.upl.properties.PropertySpec;

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
        return UPLPropertySpecFactory.string(LegacyProtocolProperties.CALL_HOME_ID_PROPERTY_NAME, false);
    }

    private PropertySpec mirrorLogicalDeviceIdPropertySpec() {
        return UPLPropertySpecFactory.bigDecimal(MIRROR_LOGICAL_DEVICE_ID, false);
    }

    private PropertySpec actualLogicalDeviceIdPropertySpec() {
        return UPLPropertySpecFactory.bigDecimal(GATEWAY_LOGICAL_DEVICE_ID, false);
    }

    private PropertySpec pskPropertySpec() {
        return UPLPropertySpecFactory.hexString(G3Properties.PSK, false);
    }

    private PropertySpec aarqTimeoutPropertySpec() {
        return UPLPropertySpecFactory.bigDecimal(Dsmr50Properties.AARQ_TIMEOUT_PROPERTY, false, BigDecimal.ZERO);
    }

    private PropertySpec readCachePropertySpec() {
        return UPLPropertySpecFactory.booleanValue(READCACHE_PROPERTY, false, false);
    }

    private PropertySpec aarqRetriesPropertySpec() {
        return UPLPropertySpecFactory.bigDecimal(Dsmr50Properties.AARQ_RETRIES_PROPERTY, false, BigDecimal.valueOf(2));
    }

    protected PropertySpec maxRecPduSizePropertySpec() {
        return UPLPropertySpecFactory.bigDecimal(MAX_REC_PDU_SIZE, false, AS330DProperties.DEFAULT_MAX_REC_PDU_SIZE);
    }
}