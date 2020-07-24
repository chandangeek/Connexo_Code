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
 * @since 22/02/2017 - 16:30
 */
public class TcpDeviceProtocolDialect extends CommonEDMIDeviceProtocolDialect {

    private static final Duration DEFAULT_FORCED_DELAY = Duration.ofMillis(0);

    public TcpDeviceProtocolDialect(PropertySpecService propertySpecService, NlsService nlsService) {
        super(propertySpecService, nlsService);
    }

    public TcpDeviceProtocolDialect(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super(propertySpecService, thesaurus);
    }

    @Override
    public String getDeviceProtocolDialectName() {
        return DeviceProtocolDialectTranslationKeys.EDMI_TCP_DIALECT_NAME.getName();
    }

    @Override
    public String getDeviceProtocolDialectDisplayName() {
        return getThesaurus().getFormat(DeviceProtocolDialectTranslationKeys.EDMI_TCP_DIALECT_NAME).format();
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
        return ConnectionMode.MINI_E_COMMAND_LINE.getName();
    }

}