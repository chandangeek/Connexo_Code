package com.energyict.protocolimplv2.abnt.common.dialects;

import com.energyict.mdc.upl.properties.PropertySpec;

import com.energyict.dlms.common.DlmsProtocolProperties;
import com.energyict.mdc.upl.properties.PropertySpecBuilder;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;
import com.energyict.protocolimplv2.DeviceProtocolDialectNameEnum;
import com.energyict.protocolimplv2.dialects.AbstractDeviceProtocolDialect;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

/**
 * Models a {@link com.energyict.mdc.tasks.DeviceProtocolDialect} for a Serial connection type
 * (SioSerialConnectionType, RxTxSerialConnectionType)
 *
 * @author sva
 * @since 23/05/2014 - 14:05
 */
public class AbntSerialDeviceProtocolDialect extends AbstractDeviceProtocolDialect {

    public static final BigDecimal DEFAULT_RETRIES = new BigDecimal(3);
    public static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(10);
    public static final Duration DEFAULT_FORCED_DELAY = Duration.ofMillis(100);
    public static final Duration DEFAULT_DELAY_AFTER_ERROR = Duration.ofMillis(250);

    private final PropertySpecService propertySpecService;


    public AbntSerialDeviceProtocolDialect(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
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

    @Override
    public String getDeviceProtocolDialectName() {
        return DeviceProtocolDialectNameEnum.ABNT_SERIAL_DIALECT_NAME.getName();
    }

    @Override
    public String getDeviceProtocolDialectDisplayName() {
        return "Serial";
    }

    protected PropertySpec retriesPropertySpec() {
        return this.bigDecimalSpec(DlmsProtocolProperties.RETRIES, false, DEFAULT_RETRIES);
    }

    protected PropertySpec timeoutPropertySpec() {
        return this.durationSpec(DlmsProtocolProperties.TIMEOUT, false, DEFAULT_TIMEOUT);
    }

    protected PropertySpec forcedDelayPropertySpec() {
        return this.durationSpec(DlmsProtocolProperties.FORCED_DELAY, false, DEFAULT_FORCED_DELAY);
    }

    protected PropertySpec delayAfterErrorPropertySpec() {
        return this.durationSpec(DlmsProtocolProperties.DELAY_AFTER_ERROR, false, DEFAULT_DELAY_AFTER_ERROR);
    }

    private PropertySpec bigDecimalSpec(String name, boolean required, BigDecimal defaultValue) {
        PropertySpecBuilder<BigDecimal> specBuilder = UPLPropertySpecFactory.specBuilder(name, required, this.propertySpecService::bigDecimalSpec);
        specBuilder.setDefaultValue(defaultValue);
        return specBuilder.finish();
    }

    private PropertySpec durationSpec(String name, boolean required, Duration defaultValue) {
        PropertySpecBuilder<Duration> durationPropertySpecBuilder = UPLPropertySpecFactory.specBuilder(name, required, this.propertySpecService::durationSpec);
        durationPropertySpecBuilder.setDefaultValue(defaultValue);
        return durationPropertySpecBuilder.finish();
    }
}