package com.energyict.protocolimplv2.abnt.common.dialects;

import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.nls.Thesaurus;
import com.energyict.mdc.upl.nls.TranslationKey;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecBuilder;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.dlms.common.DlmsProtocolProperties;
import com.energyict.nls.PropertyTranslationKeys;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;
import com.energyict.protocolimplv2.DeviceProtocolDialectTranslationKeys;
import com.energyict.protocolimplv2.dialects.AbstractDeviceProtocolDialect;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

/**
 * Models a {@link com.energyict.mdc.upl.DeviceProtocolDialect} for a Serial connection type
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

    public AbntSerialDeviceProtocolDialect(PropertySpecService propertySpecService, NlsService nlsService) {
        super(propertySpecService, nlsService);
    }

    public AbntSerialDeviceProtocolDialect(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super(propertySpecService, thesaurus);
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

    @Override
    public String getDeviceProtocolDialectName() {
        return DeviceProtocolDialectTranslationKeys.ABNT_SERIAL_DIALECT_NAME.getName();
    }

    @Override
    public String getDeviceProtocolDialectDisplayName() {
        return getThesaurus().getFormat(DeviceProtocolDialectTranslationKeys.ABNT_SERIAL_DIALECT_NAME).format();
    }

    protected PropertySpec retriesPropertySpec() {
        return this.bigDecimalSpec(DlmsProtocolProperties.RETRIES, false, DEFAULT_RETRIES, PropertyTranslationKeys.V2_ABNT_RETRIES);
    }

    protected PropertySpec timeoutPropertySpec() {
        return this.durationSpec(DlmsProtocolProperties.TIMEOUT, false, DEFAULT_TIMEOUT, PropertyTranslationKeys.V2_ABNT_TIMEOUT);
    }

    protected PropertySpec forcedDelayPropertySpec() {
        return this.durationSpec(DlmsProtocolProperties.FORCED_DELAY, false, DEFAULT_FORCED_DELAY, PropertyTranslationKeys.V2_ABNT_FORCED_DELAY);
    }

    protected PropertySpec delayAfterErrorPropertySpec() {
        return this.durationSpec(DlmsProtocolProperties.DELAY_AFTER_ERROR, false, DEFAULT_DELAY_AFTER_ERROR, PropertyTranslationKeys.V2_ABNT_DELAY_AFTER_ERROR);
    }

    private PropertySpec bigDecimalSpec(String name, boolean required, BigDecimal defaultValue, TranslationKey translationKey) {
        PropertySpecBuilder<BigDecimal> specBuilder = UPLPropertySpecFactory.specBuilder(name, required, translationKey, getPropertySpecService()::bigDecimalSpec);
        specBuilder.setDefaultValue(defaultValue);
        return specBuilder.finish();
    }

    private PropertySpec durationSpec(String name, boolean required, Duration defaultValue, TranslationKey translationKey) {
        PropertySpecBuilder<Duration> durationPropertySpecBuilder = UPLPropertySpecFactory.specBuilder(name, required, translationKey, getPropertySpecService()::durationSpec);
        durationPropertySpecBuilder.setDefaultValue(defaultValue);
        return durationPropertySpecBuilder.finish();
    }
}