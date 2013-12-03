package com.energyict.mdc.tasks;

import com.energyict.cbo.TimeDuration;
import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.dlms.common.DlmsProtocolProperties;
import com.energyict.protocolimplv2.DeviceProtocolDialectNameEnum;
import com.energyict.protocolimplv2.dialects.AbstractDeviceProtocolDialect;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.energyict.dlms.common.DlmsProtocolProperties.*;

/**
 * Models a {@link com.energyict.mdc.tasks.DeviceProtocolDialect} for an optical connection type (HDLC)
 *
 * @author: khe
 * @since: 16/10/12 (113:25)
 */
public class OpticalDeviceProtocolDialect extends AbstractDeviceProtocolDialect {

    @Override
    public String getDeviceProtocolDialectName() {
        return DeviceProtocolDialectNameEnum.OPTICAL_DLMS_PROTOCOL_DIALECT_NAME.getName();
    }

    @Override
    public String getDisplayName() {
        return "Optical DLMS";
    }

    @Override
    public List<PropertySpec> getRequiredProperties() {
        return Collections.emptyList();
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        return Arrays.asList(
                this.addressingModePropertySpec(),
                this.serverUpperMacAddressPropertySpec(),
                this.serverLowerMacAddressPropertySpec(),
                this.informationFieldSizePropertySpec(),
                this.timeoutPropertySpec(),
                this.retriesPropertySpec(),
                this.roundTripCorrectionPropertySpec()
        );
    }

    private PropertySpec addressingModePropertySpec() {
        return PropertySpecFactory.bigDecimalPropertySpecWithValues(BigDecimal.valueOf(2), DlmsProtocolProperties.ADDRESSING_MODE, BigDecimal.valueOf(1), BigDecimal.valueOf(2), BigDecimal.valueOf(4));
    }

    private PropertySpec serverUpperMacAddressPropertySpec() {
        return PropertySpecFactory.bigDecimalPropertySpec(DlmsProtocolProperties.SERVER_UPPER_MAC_ADDRESS, BigDecimal.ONE);
    }

    private PropertySpec serverLowerMacAddressPropertySpec() {
        return PropertySpecFactory.bigDecimalPropertySpec(DlmsProtocolProperties.SERVER_LOWER_MAC_ADDRESS, BigDecimal.ZERO);
    }

    private PropertySpec informationFieldSizePropertySpec() {
        return PropertySpecFactory.bigDecimalPropertySpec(DlmsProtocolProperties.INFORMATION_FIELD_SIZE, BigDecimal.valueOf(128));
    }

    private PropertySpec retriesPropertySpec() {
        return PropertySpecFactory.bigDecimalPropertySpec(RETRIES, DEFAULT_RETRIES);
    }

    private PropertySpec timeoutPropertySpec() {
        return PropertySpecFactory.timeDurationPropertySpecWithSmallUnitsAndDefaultValue(TIMEOUT, new TimeDuration(DEFAULT_TIMEOUT.intValue() / 1000));
    }

    private PropertySpec roundTripCorrectionPropertySpec() {
        return PropertySpecFactory.bigDecimalPropertySpec(ROUND_TRIP_CORRECTION, DEFAULT_ROUND_TRIP_CORRECTION);
    }

    @Override
    public PropertySpec getPropertySpec(String name) {
        switch (name) {
            case DlmsProtocolProperties.ADDRESSING_MODE:
                return this.addressingModePropertySpec();
            case DlmsProtocolProperties.SERVER_UPPER_MAC_ADDRESS:
                return this.serverUpperMacAddressPropertySpec();
            case DlmsProtocolProperties.SERVER_LOWER_MAC_ADDRESS:
                return this.serverLowerMacAddressPropertySpec();
            case DlmsProtocolProperties.INFORMATION_FIELD_SIZE:
                return this.informationFieldSizePropertySpec();
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