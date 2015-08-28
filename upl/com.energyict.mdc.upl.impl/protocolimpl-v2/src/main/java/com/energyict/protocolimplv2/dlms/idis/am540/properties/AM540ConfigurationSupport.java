package com.energyict.protocolimplv2.dlms.idis.am540.properties;

import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.dlms.common.DlmsProtocolProperties;
import com.energyict.protocol.MeterProtocol;
import com.energyict.protocolimpl.dlms.g3.G3Properties;
import com.energyict.protocolimplv2.dlms.g3.properties.AS330DConfigurationSupport;
import com.energyict.protocolimplv2.dlms.idis.am130.properties.AM130ConfigurationSupport;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author sva
 * @since 11/08/2015 - 15:15
 */
public class AM540ConfigurationSupport extends AM130ConfigurationSupport {

    public static final String USE_EQUIPMENT_IDENTIFIER_AS_SERIAL = "UseEquipmentIdentifierAsSerialNumber";
    public static final boolean USE_EQUIPMENT_IDENTIFIER_AS_SERIAL_DEFAULT_VALUE = false;
    public static final BigDecimal DEFAULT_SERVER_LOWER_MAC_ADDRESS = BigDecimal.valueOf(17);

    @Override
    public List<PropertySpec> getRequiredProperties() {
        return Collections.emptyList();
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        return Arrays.asList(
                this.forcedDelayPropertySpec(),
                this.maxRecPduSizePropertySpec(),
                this.timeZonePropertySpec(),
                this.validateInvokeIdPropertySpec(),
                this.limitMaxNrOfDaysPropertySpec(),
                this.readCachePropertySpec(),
                this.callingAPTitlePropertySpec(),
                this.callHomeIdPropertySpec(),
                this.serverLowerMacAddressPropertySpec(),
                this.mirrorLogicalDeviceIdPropertySpec(),
                this.actualLogicalDeviceIdPropertySpec(),
                this.nodeAddressPropertySpec(),
                this.pskPropertySpec(),
                this.useEquipmentIdentifierAsSerialNumberPropertySpec()
        );
    }

    private PropertySpec pskPropertySpec() {
        return PropertySpecFactory.hexStringPropertySpec(G3Properties.PSK);
    }

    private PropertySpec nodeAddressPropertySpec() {
        return PropertySpecFactory.bigDecimalPropertySpec(MeterProtocol.NODEID);
    }

    private PropertySpec serverLowerMacAddressPropertySpec() {
        return PropertySpecFactory.bigDecimalPropertySpec(DlmsProtocolProperties.SERVER_LOWER_MAC_ADDRESS, DEFAULT_SERVER_LOWER_MAC_ADDRESS);
    }

    private PropertySpec mirrorLogicalDeviceIdPropertySpec() {
        return PropertySpecFactory.bigDecimalPropertySpec(AS330DConfigurationSupport.MIRROR_LOGICAL_DEVICE_ID);
    }

    private PropertySpec actualLogicalDeviceIdPropertySpec() {
        return PropertySpecFactory.bigDecimalPropertySpec(AS330DConfigurationSupport.GATEWAY_LOGICAL_DEVICE_ID);
    }

    private PropertySpec useEquipmentIdentifierAsSerialNumberPropertySpec() {
        return PropertySpecFactory.notNullableBooleanPropertySpec(USE_EQUIPMENT_IDENTIFIER_AS_SERIAL, USE_EQUIPMENT_IDENTIFIER_AS_SERIAL_DEFAULT_VALUE);
    }
}