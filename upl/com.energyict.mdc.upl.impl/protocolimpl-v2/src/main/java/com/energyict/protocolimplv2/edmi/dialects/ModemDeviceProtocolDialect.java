package com.energyict.protocolimplv2.edmi.dialects;

import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.nls.Thesaurus;
import com.energyict.mdc.upl.properties.PropertySpec;
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
 * @author sva
 * @since 22/02/2017 - 16:31
 */
public class ModemDeviceProtocolDialect extends AbstractDeviceProtocolDialect {

    public static final BigDecimal DEFAULT_RETRIES = new BigDecimal(3);
    public static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(10);
    public static final Duration DEFAULT_FORCED_DELAY = Duration.ofMillis(100);

    public ModemDeviceProtocolDialect(PropertySpecService propertySpecService, NlsService nlsService) {
        super(propertySpecService, nlsService);
    }

    public ModemDeviceProtocolDialect(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super(propertySpecService, thesaurus);
    }

    @Override
    public String getDeviceProtocolDialectName() {
        return DeviceProtocolDialectTranslationKeys.EDMI_MODEM_DIALECT_NAME.getName();
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        return Arrays.asList(
                this.retriesPropertySpec(),
                this.timeoutPropertySpec(),
                this.forcedDelayPropertySpec()
        );
    }

    @Override
    public String getDeviceProtocolDialectDisplayName() {
        return getThesaurus().getFormat(DeviceProtocolDialectTranslationKeys.EDMI_MODEM_DIALECT_NAME).format();
    }

    protected PropertySpec retriesPropertySpec() {
        return UPLPropertySpecFactory.specBuilder(DlmsProtocolProperties.RETRIES, false, PropertyTranslationKeys.V2_ELSTER_RETRIES, getPropertySpecService()::bigDecimalSpec)
                .setDefaultValue(DEFAULT_RETRIES)
                .finish();
    }

    protected PropertySpec timeoutPropertySpec() {
        return UPLPropertySpecFactory.specBuilder(DlmsProtocolProperties.TIMEOUT, false, PropertyTranslationKeys.V2_ELSTER_TIMEOUT, getPropertySpecService()::durationSpec)
                .setDefaultValue(DEFAULT_TIMEOUT)
                .finish();
    }

    protected PropertySpec forcedDelayPropertySpec() {
        return UPLPropertySpecFactory.specBuilder(DlmsProtocolProperties.FORCED_DELAY, false, PropertyTranslationKeys.V2_ELSTER_FORCED_DELAY, getPropertySpecService()::durationSpec)
                .setDefaultValue(DEFAULT_FORCED_DELAY)
                .finish();
    }
}