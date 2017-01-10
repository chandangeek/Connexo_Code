package com.energyict.protocolimplv2.abnt.common.dialects;

import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.dlms.common.DlmsProtocolProperties;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;
import com.energyict.protocolimplv2.DeviceProtocolDialectNameEnum;
import com.energyict.protocolimplv2.dialects.AbstractDeviceProtocolDialect;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

/**
 * Models a {@link com.energyict.mdc.tasks.DeviceProtocolDialect} for a transparent TCP connection type
 * (SioOpticalConnectionType, RxTxOpticalConnectionType)
 *
 * @author sva
 * @since
 **/
public class AbntTransparentTCPDeviceProtocolDialect extends AbstractDeviceProtocolDialect {

    public static final BigDecimal DEFAULT_RETRIES = new BigDecimal(3);
    public static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(10);
    public static final Duration DEFAULT_FORCED_DELAY = Duration.ofMillis(0);
    public static final Duration DEFAULT_DELAY_AFTER_ERROR = Duration.ofMillis(250);

    private final PropertySpecService propertySpecService;

    public AbntTransparentTCPDeviceProtocolDialect(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Override
    public String getDeviceProtocolDialectName() {
        return DeviceProtocolDialectNameEnum.ABNT_SERIAL_DIALECT_NAME.getName();
    }

    @Override
    public String getDeviceProtocolDialectDisplayName() {
        return "Transparent TCP";
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        return Arrays.asList(
                this.retriesPropertySpec(),
                this.timeoutPropertySpec(),
                this.forcedDelayPropertySpec(),
                this.delayAfterErrorPropertySpec()
        );
    }

    protected PropertySpec retriesPropertySpec() {
        return this.bigDecimalSpec(DlmsProtocolProperties.RETRIES, DEFAULT_RETRIES);
    }

    protected PropertySpec timeoutPropertySpec() {
        return this.durationSpec(DlmsProtocolProperties.TIMEOUT, DEFAULT_TIMEOUT);
    }

    protected PropertySpec forcedDelayPropertySpec() {
        return this.durationSpec(DlmsProtocolProperties.FORCED_DELAY, DEFAULT_FORCED_DELAY);
    }

    protected PropertySpec delayAfterErrorPropertySpec() {
        return this.durationSpec(DlmsProtocolProperties.DELAY_AFTER_ERROR, DEFAULT_DELAY_AFTER_ERROR);
    }

    private PropertySpec bigDecimalSpec (String name, BigDecimal defaultValue) {
        return UPLPropertySpecFactory
                .specBuilder(name, false, this.propertySpecService::bigDecimalSpec)
                .setDefaultValue(defaultValue)
                .finish();
    }

    private PropertySpec durationSpec (String name, Duration defaultValue) {
        return UPLPropertySpecFactory
                .specBuilder(name, false, this.propertySpecService::durationSpec)
                .setDefaultValue(defaultValue)
                .finish();
    }

}