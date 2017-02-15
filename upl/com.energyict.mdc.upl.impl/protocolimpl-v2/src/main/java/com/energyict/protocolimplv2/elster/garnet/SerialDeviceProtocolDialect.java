package com.energyict.protocolimplv2.elster.garnet;

import com.energyict.dlms.common.DlmsProtocolProperties;
import com.energyict.mdc.upl.nls.TranslationKey;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.nls.PropertyTranslationKeys;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;
import com.energyict.protocolimplv2.DeviceProtocolDialectNameEnum;
import com.energyict.protocolimplv2.dialects.AbstractDeviceProtocolDialect;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

/**
 * Models a {@link com.energyict.mdc.tasks.DeviceProtocolDialect} for a TCP connection type.
 *
 * @author khe
 * @since 16/10/12 (113:25)
 */
public class SerialDeviceProtocolDialect extends AbstractDeviceProtocolDialect {

    public static final BigDecimal DEFAULT_RETRIES = new BigDecimal(3);
    public static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(10);
    public static final Duration DEFAULT_FORCED_DELAY = Duration.ofMillis(100);
    public static final Duration DEFAULT_DELAY_AFTER_ERROR = Duration.ofMillis(100);

    public SerialDeviceProtocolDialect(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @Override
    public String getDeviceProtocolDialectName() {
        return DeviceProtocolDialectNameEnum.GARNET_SERIAL_DIALECT_NAME.getName();
    }

    @Override
    public String getDeviceProtocolDialectDisplayName() {
        return "Serial";
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
        return this.bigDecimalSpec(DlmsProtocolProperties.RETRIES, DEFAULT_RETRIES, PropertyTranslationKeys.V2_ELSTER_RETRIES);
    }

    protected PropertySpec timeoutPropertySpec() {
        return this.durationSpec(DlmsProtocolProperties.TIMEOUT, DEFAULT_TIMEOUT, PropertyTranslationKeys.V2_ELSTER_TIMEOUT);
    }

    protected PropertySpec forcedDelayPropertySpec() {
        return this.durationSpec(DlmsProtocolProperties.FORCED_DELAY, DEFAULT_FORCED_DELAY, PropertyTranslationKeys.V2_ELSTER_FORCED_DELAY);
    }

    protected PropertySpec delayAfterErrorPropertySpec() {
        return this.durationSpec(DlmsProtocolProperties.DELAY_AFTER_ERROR, DEFAULT_DELAY_AFTER_ERROR, PropertyTranslationKeys.V2_ELSTER_DELAY_AFTER_ERROR);
    }

    private PropertySpec bigDecimalSpec (String name, BigDecimal defaultValue, TranslationKey translationKey) {
        return UPLPropertySpecFactory
                .specBuilder(name, false, translationKey, this.propertySpecService::bigDecimalSpec)
                .setDefaultValue(defaultValue)
                .finish();
    }

    private PropertySpec durationSpec (String name, Duration defaultValue, TranslationKey translationKey) {
        return UPLPropertySpecFactory
                .specBuilder(name, false, translationKey, this.propertySpecService::durationSpec)
                .setDefaultValue(defaultValue)
                .finish();
    }

}