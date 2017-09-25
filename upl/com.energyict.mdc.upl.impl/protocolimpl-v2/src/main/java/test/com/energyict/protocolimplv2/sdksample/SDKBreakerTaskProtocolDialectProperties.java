package test.com.energyict.protocolimplv2.sdksample;

import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.nls.Thesaurus;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;
import com.energyict.protocolimpl.properties.nls.PropertyTranslationKeys;
import com.energyict.protocolimplv2.DeviceProtocolDialectTranslationKeys;
import com.energyict.protocolimplv2.dialects.AbstractDeviceProtocolDialect;

import java.util.Arrays;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 28/05/13
 * Time: 9:59
 */
public class SDKBreakerTaskProtocolDialectProperties extends AbstractDeviceProtocolDialect {

    public static final String breakerStatus = "breakerStatus";

    private static final String CONNECTED = "connected";
    private static final String DISCONNECTED = "disconnected";
    private static final String ARMED = "armed";

    public SDKBreakerTaskProtocolDialectProperties(PropertySpecService propertySpecService, NlsService nlsService) {
        super(propertySpecService, nlsService);
    }

    public SDKBreakerTaskProtocolDialectProperties(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super(propertySpecService, thesaurus);
    }

    @Override
    public String getDeviceProtocolDialectName() {
        return DeviceProtocolDialectTranslationKeys.SDK_SAMPLE_BREAKER.getName();
    }

    @Override
    public String getDeviceProtocolDialectDisplayName() {
        return getThesaurus().getFormat(DeviceProtocolDialectTranslationKeys.SDK_SAMPLE_BREAKER).format();
    }

    @Override
    public List<com.energyict.mdc.upl.properties.PropertySpec> getUPLPropertySpecs() {
        return Arrays.asList(
                UPLPropertySpecFactory
                        .specBuilder(breakerStatus, false, PropertyTranslationKeys.SDKSAMPLE_BREAKER_STATUS, getPropertySpecService()::stringSpec)
                        .addValues(CONNECTED, DISCONNECTED, ARMED)
                        .markExhaustive()
                        .setDefaultValue(CONNECTED)
                        .finish()
        );
    }
}