package com.energyict.protocolimplv2.abnt.common.dialects;

import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties;
import com.energyict.protocolimplv2.DeviceProtocolDialectNameEnum;
import com.energyict.protocolimplv2.dialects.AbstractDeviceProtocolDialect;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Models a DeviceProtocolDialect for a Optical connection type
 * (SioOpticalConnectionType, RxTxOpticalConnectionType)
 *
 * @author sva
 * @since 23/05/2014 - 14:05
 */
public class AbntOpticalDeviceProtocolDialect extends AbstractDeviceProtocolDialect {

    public static final BigDecimal DEFAULT_RETRIES = new BigDecimal(3);
    public static final TimeDuration DEFAULT_TIMEOUT = new TimeDuration(10, TimeDuration.TimeUnit.SECONDS);
    public static final TimeDuration DEFAULT_FORCED_DELAY = new TimeDuration(100, TimeDuration.TimeUnit.MILLISECONDS);
    public static final TimeDuration DEFAULT_DELAY_AFTER_ERROR = new TimeDuration(250, TimeDuration.TimeUnit.MILLISECONDS);

    public AbntOpticalDeviceProtocolDialect(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }


    @Override
    public String getDeviceProtocolDialectName() {
        return DeviceProtocolDialectNameEnum.ABNT_OPTICAL_DIALECT_NAME.getName();
    }

    @Override
    public String getDisplayName() {
        return "Optical";
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return Arrays.asList(
                this.retriesPropertySpec(),
                this.timeoutPropertySpec(),
                this.forcedDelayPropertySpec(),
                this.delayAfterErrorPropertySpec()
        );
    }

    protected PropertySpec retriesPropertySpec() {
        return getPropertySpecService().bigDecimalPropertySpec(DlmsProtocolProperties.RETRIES, false, DEFAULT_RETRIES);
    }

    protected PropertySpec timeoutPropertySpec() {
        return getPropertySpecService().timeDurationPropertySpec(DlmsProtocolProperties.TIMEOUT, false, DEFAULT_TIMEOUT);
    }

    protected PropertySpec forcedDelayPropertySpec() {
        return getPropertySpecService().timeDurationPropertySpec(DlmsProtocolProperties.FORCED_DELAY, false, DEFAULT_FORCED_DELAY);
    }

    protected PropertySpec delayAfterErrorPropertySpec() {
        return getPropertySpecService().timeDurationPropertySpec(DlmsProtocolProperties.DELAY_AFTER_ERROR, false, DEFAULT_DELAY_AFTER_ERROR);
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
            case DlmsProtocolProperties.DELAY_AFTER_ERROR:
                return this.delayAfterErrorPropertySpec();
            default:
                return null;
        }
    }
}