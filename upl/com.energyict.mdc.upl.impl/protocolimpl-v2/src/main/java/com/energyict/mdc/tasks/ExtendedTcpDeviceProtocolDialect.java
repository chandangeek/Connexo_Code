package com.energyict.mdc.tasks;

import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.dlms.common.DlmsProtocolProperties;
import com.energyict.protocolimplv2.DeviceProtocolDialectNameEnum;

import java.util.Arrays;
import java.util.List;

/**
 * Models a {@link DeviceProtocolDialect} for a TCP connection type
 * This is the normal TCP dialect, but adds the wakeUp property.
 *
 * @author: khe
 * @since: 16/10/12 (113:25)
 */
public class ExtendedTcpDeviceProtocolDialect extends TcpDeviceProtocolDialect {

    @Override
    public String getDeviceProtocolDialectName() {
        return DeviceProtocolDialectNameEnum.EXTENDED_TCP_DLMS_PROTOCOL_DIALECT_NAME.getName();
    }

    @Override
    public String getDisplayName() {
        return "Extended TCP DLMS";
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        return Arrays.asList(
                this.serverUpperMacAddressPropertySpec(),
                this.wakeUpPropertySpec(),
                this.timeoutPropertySpec(),
                this.retriesPropertySpec(),
                this.roundTripCorrectionPropertySpec()
        );
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
            case DlmsProtocolProperties.RETRIES:
                return this.retriesPropertySpec();
            case DlmsProtocolProperties.TIMEOUT:
                return this.timeoutPropertySpec();
            case DlmsProtocolProperties.ROUND_TRIP_CORRECTION:
                return this.roundTripCorrectionPropertySpec();
            default:
                return null;
        }
    }
}
