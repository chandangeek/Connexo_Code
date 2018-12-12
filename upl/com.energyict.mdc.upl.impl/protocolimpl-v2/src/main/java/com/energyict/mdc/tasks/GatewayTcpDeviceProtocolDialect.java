package com.energyict.mdc.tasks;

import com.energyict.mdc.upl.DeviceProtocolDialect;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.nls.Thesaurus;
import com.energyict.mdc.upl.nls.TranslationKey;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.nls.PropertyTranslationKeys;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;
import com.energyict.protocolimplv2.DeviceProtocolDialectTranslationKeys;
import com.energyict.protocolimplv2.dialects.AbstractDeviceProtocolDialect;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import static com.energyict.dlms.common.DlmsProtocolProperties.DEFAULT_RETRIES;
import static com.energyict.dlms.common.DlmsProtocolProperties.DEFAULT_ROUND_TRIP_CORRECTION;
import static com.energyict.dlms.common.DlmsProtocolProperties.RETRIES;
import static com.energyict.dlms.common.DlmsProtocolProperties.ROUND_TRIP_CORRECTION;
import static com.energyict.dlms.common.DlmsProtocolProperties.TIMEOUT;

/**
 * Models a {@link DeviceProtocolDialect} for a TCP connection type to a Beacon 3100, that acts as a transparent gateway to a connected PLC G3 e-meter.
 * Note that, using this dialect, the protocol will read out the actual meter (using the Beacon as a gateway).
 *
 * @author khe
 */
public class GatewayTcpDeviceProtocolDialect extends AbstractDeviceProtocolDialect {

    public static final int DEFAULT_TCP_TIMEOUT = 30;

    public GatewayTcpDeviceProtocolDialect(PropertySpecService propertySpecService, NlsService nlsService) {
        super(propertySpecService, nlsService);
    }

   public GatewayTcpDeviceProtocolDialect(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super(propertySpecService, thesaurus);
    }

    @Override
    public String getDeviceProtocolDialectName() {
        return DeviceProtocolDialectTranslationKeys.BEACON_GATEWAY_TCP_DLMS_PROTOCOL_DIALECT_NAME.getName();
    }

    @Override
    public String getDeviceProtocolDialectDisplayName() {
        return getThesaurus().getFormat(DeviceProtocolDialectTranslationKeys.BEACON_GATEWAY_TCP_DLMS_PROTOCOL_DIALECT_NAME).format();
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        return Arrays.asList(
                this.timeoutPropertySpec(),
                this.retriesPropertySpec(),
                this.roundTripCorrectionPropertySpec()
        );
    }

    protected PropertySpec retriesPropertySpec() {
        return this.bigDecimalSpec(RETRIES, DEFAULT_RETRIES, PropertyTranslationKeys.V2_TASKS_RETRIES);
    }

    protected PropertySpec timeoutPropertySpec() {
        return this.durationSpec(TIMEOUT, Duration.ofSeconds(DEFAULT_TCP_TIMEOUT), PropertyTranslationKeys.V2_TASKS_TIMEOUT);
    }

    protected PropertySpec roundTripCorrectionPropertySpec() {
        return this.bigDecimalSpec(ROUND_TRIP_CORRECTION, DEFAULT_ROUND_TRIP_CORRECTION, PropertyTranslationKeys.V2_TASKS_ROUNDTRIPCORRECTION);
    }

    private PropertySpec bigDecimalSpec(String name, BigDecimal defaultValue, TranslationKey translationKey) {
        return UPLPropertySpecFactory
                .specBuilder(name, false, translationKey, getPropertySpecService()::bigDecimalSpec)
                .setDefaultValue(defaultValue)
                .finish();
    }

    private PropertySpec durationSpec(String name, Duration defaultValue, TranslationKey translationKey) {
        return UPLPropertySpecFactory
                .specBuilder(name, false, translationKey, getPropertySpecService()::durationSpec)
                .setDefaultValue(defaultValue)
                .finish();
    }

}
