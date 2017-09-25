package com.elster.us.protocolimplv2.mercury.minimax;

import com.energyict.mdc.upl.DeviceProtocolDialect;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.nls.Thesaurus;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.elster.us.nls.PropertyTranslationKeys;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;
import com.energyict.protocolimplv2.DeviceProtocolDialectTranslationKeys;
import com.energyict.protocolimplv2.dialects.AbstractDeviceProtocolDialect;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

/**
 * Models a {@link DeviceProtocolDialect} for a TCP connection type.
 *
 * @author khe
 * @since 16/10/12 (113:25)
 */
public class MiniMaxTcpDeviceProtocolDialect extends AbstractDeviceProtocolDialect implements DeviceProtocolDialect {

    public static final String RETRIES = "Retries";
    public static final String TIMEOUT = "Timeout";
    public static final String ROUND_TRIP_CORRECTION = "RoundTripCorrection";

    public static final long DEFAULT_TCP_TIMEOUT = 30;
    public static final int DEFAULT_ROUND_TRIP_CORRECTION = 0;

    public MiniMaxTcpDeviceProtocolDialect(PropertySpecService propertySpecService, NlsService nlsService) {
        super(propertySpecService, nlsService);
    }

    public MiniMaxTcpDeviceProtocolDialect(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super(propertySpecService, thesaurus);
    }

    @Override
    public String getDeviceProtocolDialectName() {
        return DeviceProtocolDialectTranslationKeys.TCP_MERCURY_DIALECT_NAME.getName();
    }

    @Override
    public String getDeviceProtocolDialectDisplayName() {
        return getThesaurus().getFormat(DeviceProtocolDialectTranslationKeys.TCP_MERCURY_DIALECT_NAME).format();
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        return Arrays.asList(
                this.timeoutPropertySpec(),
                this.retriesPropertySpec(),
                this.roundTripCorrectionPropertySpec()
        );
    }

    private PropertySpec retriesPropertySpec() {
        return UPLPropertySpecFactory
                .specBuilder(RETRIES, false, PropertyTranslationKeys.MERCURY_RETRIES, getPropertySpecService()::bigDecimalSpec)
                .setDefaultValue(new BigDecimal(3))
                .finish();
    }

    private PropertySpec timeoutPropertySpec() {
        return UPLPropertySpecFactory
                .specBuilder(TIMEOUT, false, PropertyTranslationKeys.MERCURY_TIMEOUT, getPropertySpecService()::durationSpec)
                .setDefaultValue(Duration.ofSeconds(DEFAULT_TCP_TIMEOUT))
                .finish();
    }

    private PropertySpec roundTripCorrectionPropertySpec() {
        return UPLPropertySpecFactory
                .specBuilder(ROUND_TRIP_CORRECTION, false, PropertyTranslationKeys.MERCURY_ROUND_TRIP_CORRECTION, getPropertySpecService()::bigDecimalSpec)
                .setDefaultValue(new BigDecimal(DEFAULT_ROUND_TRIP_CORRECTION))
                .finish();
    }

}