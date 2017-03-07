package com.energyict.protocolimplv2.edmi.dialects;

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

/**
 * @author sva
 * @since 22/02/2017 - 16:30
 */
public class UdpDeviceProtocolDialect extends AbstractDeviceProtocolDialect {
    public static final String TIMEOUT = "Timeout";
    public static final String RETRIES = "Retries";
    public static final String FORCED_DELAY = "ForcedDelay";

    public static final BigDecimal DEFAULT_RETRIES = new BigDecimal(3);
    public static final TimeDuration DEFAULT_TIMEOUT = new TimeDuration(10, TimeDuration.SECONDS);
    public static final TimeDuration DEFAULT_FORCED_DELAY = new TimeDuration(0, TimeDuration.MILLISECONDS);

    @Override
    public String getDeviceProtocolDialectName() {
        return DeviceProtocolDialectNameEnum.EDMI_UDP_DIALECT_NAME.getName();
    }

    @Override
    public String getDisplayName() {
        return "UDP";
    }

    @Override
    public List<PropertySpec> getRequiredProperties() {
        return Collections.emptyList();
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        return Arrays.asList(
                this.retriesPropertySpec(),
                this.timeoutPropertySpec(),
                this.forcedDelayPropertySpec()
        );
    }

    protected PropertySpec retriesPropertySpec() {
        return PropertySpecFactory.bigDecimalPropertySpec(DlmsProtocolProperties.RETRIES, DEFAULT_RETRIES);
    }

    protected PropertySpec timeoutPropertySpec() {
        return PropertySpecFactory.timeDurationPropertySpecWithSmallUnitsAndDefaultValue(TIMEOUT, DEFAULT_TIMEOUT);
    }

    protected PropertySpec forcedDelayPropertySpec() {
        return PropertySpecFactory.timeDurationPropertySpecWithSmallUnitsAndDefaultValue(FORCED_DELAY, DEFAULT_FORCED_DELAY);
    }

    @Override
    public PropertySpec getPropertySpec(String name) {
        switch (name) {
            case DlmsProtocolProperties.RETRIES:
                return this.retriesPropertySpec();
            case DlmsProtocolProperties.TIMEOUT:
                return this.timeoutPropertySpec();
            case DlmsProtocolProperties.FORCED_DELAY:
                return this.forcedDelayPropertySpec();
            default:
                return null;
        }
    }
}