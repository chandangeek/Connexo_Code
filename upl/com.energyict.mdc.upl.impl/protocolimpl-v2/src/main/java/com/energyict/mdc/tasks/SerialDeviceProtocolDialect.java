package com.energyict.mdc.tasks;

import com.energyict.mdc.upl.DeviceProtocolDialect;
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

import static com.energyict.dlms.common.DlmsProtocolProperties.DEFAULT_RETRIES;
import static com.energyict.dlms.common.DlmsProtocolProperties.DEFAULT_ROUND_TRIP_CORRECTION;
import static com.energyict.dlms.common.DlmsProtocolProperties.RETRIES;
import static com.energyict.dlms.common.DlmsProtocolProperties.ROUND_TRIP_CORRECTION;
import static com.energyict.dlms.common.DlmsProtocolProperties.TIMEOUT;

/**
 * Models a {@link DeviceProtocolDialect} for a serial HDLC connection type (optical/RS485/... interface)
 *
 * @author khe
 * @since 16/10/12 (113:25)
 */
public class SerialDeviceProtocolDialect extends AbstractDeviceProtocolDialect {

    private static final Duration DEFAULT_TIMEOUT = Duration.ofMillis(DlmsProtocolProperties.DEFAULT_TIMEOUT.intValue());

    public SerialDeviceProtocolDialect(PropertySpecService propertySpecService, NlsService nlsService) {
        super(propertySpecService, nlsService);
    }

    public SerialDeviceProtocolDialect(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super(propertySpecService, thesaurus);
    }

    @Override
    public String getDeviceProtocolDialectName() {
        return DeviceProtocolDialectTranslationKeys.SERIAL_DLMS_PROTOCOL_DIALECT_NAME.getName();
    }

    @Override
    public String getDeviceProtocolDialectDisplayName() {
        return getThesaurus().getFormat(DeviceProtocolDialectTranslationKeys.SERIAL_DLMS_PROTOCOL_DIALECT_NAME).format();
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        return Arrays.asList(
                this.addressingModePropertySpec(),
                this.informationFieldSizePropertySpec(),
                this.timeoutPropertySpec(),
                this.retriesPropertySpec(),
                this.roundTripCorrectionPropertySpec()
        );
    }

    private PropertySpec addressingModePropertySpec() {
        return UPLPropertySpecFactory
                .specBuilder(DlmsProtocolProperties.ADDRESSING_MODE, false, PropertyTranslationKeys.V2_TASKS_ADDRESSING_MODE, getPropertySpecService()::bigDecimalSpec)
                .setDefaultValue(BigDecimal.valueOf(2))
                .addValues(BigDecimal.valueOf(1), BigDecimal.valueOf(2), BigDecimal.valueOf(4))
                .markExhaustive()
                .finish();
    }

    private PropertySpec informationFieldSizePropertySpec() {
        return UPLPropertySpecFactory
                .specBuilder(DlmsProtocolProperties.INFORMATION_FIELD_SIZE, false, PropertyTranslationKeys.V2_TASKS_INFORMATION_FIELD_SIZE, getPropertySpecService()::bigDecimalSpec)
                .setDefaultValue(BigDecimal.valueOf(128))
                .finish();
    }

    private PropertySpec retriesPropertySpec() {
        return UPLPropertySpecFactory
                .specBuilder(RETRIES, false, PropertyTranslationKeys.V2_TASKS_RETRIES, getPropertySpecService()::bigDecimalSpec)
                .setDefaultValue(DEFAULT_RETRIES)
                .finish();
    }

    private PropertySpec timeoutPropertySpec() {
        return UPLPropertySpecFactory
                .specBuilder(TIMEOUT, false, PropertyTranslationKeys.V2_TASKS_TIMEOUT, getPropertySpecService()::durationSpec)
                .setDefaultValue(DEFAULT_TIMEOUT)
                .finish();
    }

    private PropertySpec roundTripCorrectionPropertySpec() {
        return UPLPropertySpecFactory
                .specBuilder(ROUND_TRIP_CORRECTION, false, PropertyTranslationKeys.V2_TASKS_ROUNDTRIPCORRECTION, getPropertySpecService()::bigDecimalSpec)
                .setDefaultValue(DEFAULT_ROUND_TRIP_CORRECTION)
                .finish();
    }

}