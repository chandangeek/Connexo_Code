package com.energyict.protocolimplv2.dlms.g3.properties;

import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.dlms.common.DlmsProtocolProperties;
import com.energyict.mdc.protocol.LegacyProtocolProperties;
import com.energyict.protocolimpl.dlms.g3.G3Properties;
import com.energyict.protocolimplv2.nta.dsmr23.DlmsConfigurationSupport;
import com.energyict.protocolimplv2.nta.dsmr50.elster.am540.Dsmr50Properties;

import java.math.BigDecimal;
import java.util.ArrayList;
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

    @Override
    public List<PropertySpec> getOptionalProperties() {
        List<PropertySpec> optionalProperties = new ArrayList<>(super.getOptionalProperties());
        optionalProperties.add(pskPropertySpec());
        optionalProperties.add(aarqTimeoutPropertySpec());
        optionalProperties.add(aarqRetriesPropertySpec());
        optionalProperties.add(readCachePropertySpec());
        optionalProperties.add(callHomeIdPropertySpec());
        optionalProperties.add(serverUpperMacAddressPropertySpec());
        optionalProperties.remove(ntaSimulationToolPropertySpec());
        optionalProperties.remove(manufacturerPropertySpec());
        optionalProperties.remove(fixMbusHexShortIdPropertySpec());
        optionalProperties.remove(deviceId());
        return optionalProperties;
    }

    protected PropertySpec callHomeIdPropertySpec() {
        return PropertySpecFactory.hexStringPropertySpec(LegacyProtocolProperties.CALL_HOME_ID_PROPERTY_NAME);
    }

    protected PropertySpec serverUpperMacAddressPropertySpec() {
        return PropertySpecFactory.bigDecimalPropertySpec(DlmsProtocolProperties.SERVER_UPPER_MAC_ADDRESS, BigDecimal.ONE);
    }

    private PropertySpec pskPropertySpec() {
        return PropertySpecFactory.hexStringPropertySpec(G3Properties.PSK);
    }

    private PropertySpec aarqTimeoutPropertySpec() {
        return PropertySpecFactory.bigDecimalPropertySpec(Dsmr50Properties.AARQ_TIMEOUT_PROPERTY, BigDecimal.ZERO);
    }

    private PropertySpec readCachePropertySpec() {
        return PropertySpecFactory.notNullableBooleanPropertySpec(READCACHE_PROPERTY, false);
    }

    private PropertySpec aarqRetriesPropertySpec() {
        return PropertySpecFactory.bigDecimalPropertySpec(Dsmr50Properties.AARQ_RETRIES_PROPERTY, BigDecimal.valueOf(2));
    }

    protected PropertySpec maxRecPduSizePropertySpec() {
        return PropertySpecFactory.bigDecimalPropertySpec(MAX_REC_PDU_SIZE, AS330DProperties.DEFAULT_MAX_REC_PDU_SIZE);
    }
}