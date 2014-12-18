package com.energyict.protocolimplv2.eict.rtuplusserver.g3.properties;


import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.protocolimplv2.edp.EDPProperties;
import com.energyict.protocolimplv2.dlms.DlmsProperties;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 11/06/2014 - 13:46
 */
public class G3GatewayProperties extends DlmsProperties {

    public static final String AARQ_TIMEOUT_PROP_NAME = "AARQ_Timeout";
    public static final TimeDuration AARQ_TIMEOUT_DEFAULT = TimeDuration.NONE;
    public static final String G3_MAC_ADDRESS_PROP_NAME = "MAC_address";
    public static final String G3_SHORT_ADDRESS_PROP_NAME = "Short_MAC_address";
    public static final String G3_LOGICAL_DEVICE_ID_PROP_NAME ="Logical_device_id";

    public G3GatewayProperties(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    public long getAarqTimeout() {
        return getProperties().getTypedProperty(AARQ_TIMEOUT_PROP_NAME, AARQ_TIMEOUT_DEFAULT).getMilliSeconds();
    }

    public boolean isReadCache() {
        return getProperties().<Boolean>getTypedProperty(EDPProperties.READCACHE_PROPERTY, false);
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        List<PropertySpec> propertySpecs = new ArrayList<>(super.getPropertySpecs());
        propertySpecs.addAll(Arrays.asList(validateInvokeIdPropertySpec(),
                aarqTimeoutPropertySpec(),
                readCachePropertySpec(),
                forcedDelayPropertySpec(),
                maxRecPduSizePropertySpec(),
                getMacAddressPropertySPec(),
                getShortAddressPropertySpec(),
                getLogicalDeviceIdPropertySpec()));
        return propertySpecs;
    }

    private PropertySpec validateInvokeIdPropertySpec() {
        return getPropertySpecService().booleanPropertySpec(VALIDATE_INVOKE_ID, false, true);
    }

    private PropertySpec aarqTimeoutPropertySpec() {
        return getPropertySpecService().timeDurationPropertySpec(G3GatewayProperties.AARQ_TIMEOUT_PROP_NAME, false, G3GatewayProperties.AARQ_TIMEOUT_DEFAULT);
    }

    private PropertySpec readCachePropertySpec() {
        return getPropertySpecService().booleanPropertySpec(EDPProperties.READCACHE_PROPERTY, false, false);
    }

    private PropertySpec forcedDelayPropertySpec() {
        return getPropertySpecService().timeDurationPropertySpec(FORCED_DELAY, false, DEFAULT_FORCED_DELAY);
    }

    private PropertySpec maxRecPduSizePropertySpec() {
        return getPropertySpecService().bigDecimalPropertySpec(MAX_REC_PDU_SIZE, false, DEFAULT_MAX_REC_PDU_SIZE);
    }

    public PropertySpec getMacAddressPropertySPec() {
        return getPropertySpecService().stringPropertySpec(G3_MAC_ADDRESS_PROP_NAME, false, "");
    }

    public PropertySpec getShortAddressPropertySpec() {
        return getPropertySpecService().bigDecimalPropertySpec(G3_SHORT_ADDRESS_PROP_NAME, false, BigDecimal.ZERO);
    }

    public PropertySpec getLogicalDeviceIdPropertySpec() {
        return getPropertySpecService().bigDecimalPropertySpec(G3_LOGICAL_DEVICE_ID_PROP_NAME, false, BigDecimal.ZERO);
    }

}