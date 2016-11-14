package com.energyict.mdc.tasks;

import com.energyict.cbo.TimeDuration;
import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.dlms.common.DlmsProtocolProperties;
import com.energyict.protocolimplv2.DeviceProtocolDialectNameEnum;
import com.energyict.protocolimplv2.dialects.AbstractDeviceProtocolDialect;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.energyict.dlms.common.DlmsProtocolProperties.DEFAULT_RETRIES;
import static com.energyict.dlms.common.DlmsProtocolProperties.DEFAULT_ROUND_TRIP_CORRECTION;
import static com.energyict.dlms.common.DlmsProtocolProperties.RETRIES;
import static com.energyict.dlms.common.DlmsProtocolProperties.ROUND_TRIP_CORRECTION;
import static com.energyict.dlms.common.DlmsProtocolProperties.TIMEOUT;

/**
 * Models a {@link com.energyict.mdc.tasks.DeviceProtocolDialect} for a TCP connection type
 *
 * @author: khe
 * @since: 16/10/12 (113:25)
 */
public class TcpDeviceProtocolDialect extends AbstractDeviceProtocolDialect {

    public static final int DEFAULT_TCP_TIMEOUT = 30;

    @Override
    public String getDeviceProtocolDialectName() {
        return DeviceProtocolDialectNameEnum.TCP_DLMS_PROTOCOL_DIALECT_NAME.getName();
    }

    @Override
    public String getDeviceProtocolDialectDisplayName() {
        return "TCP DLMS";
    }

    @Override
    public List<PropertySpec> getRequiredProperties() {
        return Collections.emptyList();
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        return Arrays.asList(
                this.timeoutPropertySpec(),
                this.retriesPropertySpec(),
                this.roundTripCorrectionPropertySpec()
        );
    }

    protected PropertySpec retriesPropertySpec() {
        return PropertySpecFactory.bigDecimalPropertySpec(RETRIES, DEFAULT_RETRIES);
    }

    protected PropertySpec timeoutPropertySpec() {
        return PropertySpecFactory.timeDurationPropertySpecWithSmallUnitsAndDefaultValue(TIMEOUT, new TimeDuration(DEFAULT_TCP_TIMEOUT));
    }

    protected PropertySpec roundTripCorrectionPropertySpec() {
        return PropertySpecFactory.bigDecimalPropertySpec(ROUND_TRIP_CORRECTION, DEFAULT_ROUND_TRIP_CORRECTION);
    }

    @Override
    public PropertySpec getPropertySpec(String name) {
        switch (name) {
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
