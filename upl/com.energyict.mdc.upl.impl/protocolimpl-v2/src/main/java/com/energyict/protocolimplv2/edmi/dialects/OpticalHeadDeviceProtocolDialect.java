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
 * @since 22/02/2017 - 16:31
 */
public class OpticalHeadDeviceProtocolDialect extends AbstractDeviceProtocolDialect {

    public static final BigDecimal DEFAULT_RETRIES = new BigDecimal(3);
    public static final TimeDuration DEFAULT_TIMEOUT = new TimeDuration(10, TimeDuration.SECONDS);
    public static final TimeDuration DEFAULT_FORCED_DELAY = new TimeDuration(100, TimeDuration.MILLISECONDS);

    @Override
    public String getDeviceProtocolDialectName() {
        return DeviceProtocolDialectNameEnum.EDMI_OPTICAL_DIALECT_NAME.getName();
    }

    @Override
    public String getDisplayName() {
        return "Optical";
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
        return PropertySpecFactory.timeDurationPropertySpecWithSmallUnitsAndDefaultValue(DlmsProtocolProperties.TIMEOUT, DEFAULT_TIMEOUT);
    }

    protected PropertySpec forcedDelayPropertySpec() {
        return PropertySpecFactory.timeDurationPropertySpecWithSmallUnitsAndDefaultValue(DlmsProtocolProperties.FORCED_DELAY, DEFAULT_FORCED_DELAY);
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