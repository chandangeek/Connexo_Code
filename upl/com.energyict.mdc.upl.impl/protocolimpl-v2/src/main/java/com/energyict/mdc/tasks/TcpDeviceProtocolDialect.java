package com.energyict.mdc.tasks;

import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.dlms.common.DlmsProtocolProperties;
import com.energyict.protocolimplv2.DeviceProtocolDialectNameEnum;
import com.energyict.protocolimplv2.dialects.AbstractDeviceProtocolDialect;

import java.math.BigDecimal;
import java.util.*;

/**
 * Models a {@link DeviceProtocolDialect} for a TCP connection type
 *
 * @author: khe
 * @since: 16/10/12 (113:25)
 */
public class TcpDeviceProtocolDialect extends AbstractDeviceProtocolDialect {

    @Override
    public String getDeviceProtocolDialectName() {
        return DeviceProtocolDialectNameEnum.TCP_DLMS_PROTOCOL_DIALECT_NAME.getName();
    }

    @Override
    public String getDisplayName() {
        return "TCP DLMS";
    }

    @Override
    public List<PropertySpec> getRequiredProperties() {
        return Collections.emptyList();
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        return Arrays.asList(
                this.serverUpperMacAddressPropertySpec(),
                this.wakeUpPropertySpec());
    }

    private PropertySpec serverUpperMacAddressPropertySpec() {
        return PropertySpecFactory.bigDecimalPropertySpec(DlmsProtocolProperties.SERVER_UPPER_MAC_ADDRESS, BigDecimal.ONE);
    }

    private PropertySpec wakeUpPropertySpec() {
        return PropertySpecFactory.notNullableBooleanPropertySpec(DlmsProtocolProperties.WAKE_UP);
    }

    @Override
    public PropertySpec getPropertySpec(String name) {
        switch (name) {
            case DlmsProtocolProperties.SERVER_UPPER_MAC_ADDRESS:
                return this.serverUpperMacAddressPropertySpec();
            case DlmsProtocolProperties.WAKE_UP:
                return this.wakeUpPropertySpec();
            default:
                return null;
        }
    }
}
