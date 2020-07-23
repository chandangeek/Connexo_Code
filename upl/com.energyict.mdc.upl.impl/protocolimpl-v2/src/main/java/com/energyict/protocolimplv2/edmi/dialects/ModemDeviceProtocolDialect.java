package com.energyict.protocolimplv2.edmi.dialects;

import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.nls.Thesaurus;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocolimplv2.DeviceProtocolDialectTranslationKeys;
import com.energyict.protocolimplv2.edmi.mk10.properties.MK10ConfigurationSupport;

import java.math.BigDecimal;
import java.time.Duration;

/**
 * @author sva
 * @since 22/02/2017 - 16:31
 */
public class ModemDeviceProtocolDialect extends CommonEDMIDeviceProtocolDialect {

    private static final Duration DEFAULT_FORCED_DELAY = Duration.ofMillis(100);

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
    public String getDeviceProtocolDialectDisplayName() {
        return getThesaurus().getFormat(DeviceProtocolDialectTranslationKeys.EDMI_MODEM_DIALECT_NAME).format();
    }

    @Override
    BigDecimal getDefaultRetries() {
        return DEFAULT_RETRIES;
    }

    @Override
    Duration getDefaultTimeout() {
        return DEFAULT_TIMEOUT;
    }

    @Override
    Duration getDefaultForcedDelay() {
        return DEFAULT_FORCED_DELAY;
    }

    @Override
    String getDefaultConnectionMode() {
        return ConnectionMode.EXTENDED_COMMAND_LINE.getName();
    }

}