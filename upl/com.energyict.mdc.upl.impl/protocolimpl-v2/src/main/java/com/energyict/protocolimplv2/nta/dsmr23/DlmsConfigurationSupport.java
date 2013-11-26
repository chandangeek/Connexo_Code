package com.energyict.protocolimplv2.nta.dsmr23;

import com.energyict.cbo.ConfigurationSupport;
import com.energyict.cbo.TimeDuration;
import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.dlms.aso.ConformanceBlock;

import java.math.BigDecimal;
import java.util.*;

import static com.energyict.dlms.common.DlmsProtocolProperties.*;

/**
 * A collection of general DLMS properties.
 * These properties are not related to the security or the protocol dialects.
 * The parsing and the usage of the property values is done in implementations of {@link com.energyict.dlms.protocolimplv2.DlmsSessionProperties}
 * <p/>
 * Copyrights EnergyICT
 * Date: 22/10/13
 * Time: 15:41
 * Author: khe
 */
public class DlmsConfigurationSupport implements ConfigurationSupport {

    @Override
    public List<PropertySpec> getRequiredProperties() {
        return Collections.emptyList();
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        return Arrays.asList(
                this.forcedDelayPropertySpec(),
                this.maxRecPduSizePropertySpec(),
                this.retriesPropertySpec(),
                this.timeoutPropertySpec(),
                this.roundTripCorrectionPropertySpec(),
                this.bulkRequestPropertySpec(),
                this.cipheringTypePropertySpec(),
                this.ntaSimulationToolPropertySpec(),
                this.fixMbusHexShortIdPropertySpec(),
                this.manufacturerPropertySpec(),
                this.conformanceBlockValuePropertySpec(),
                this.requestTimeZonePropertySpec(),
                this.timeZonePropertySpec(),
                this.validateInvokeIdPropertySpec(),
                this.deviceId());
    }

    private PropertySpec timeZonePropertySpec() {
        return PropertySpecFactory.timeZoneInUseReferencePropertySpec(TIMEZONE);
    }

    private PropertySpec validateInvokeIdPropertySpec() {
        return PropertySpecFactory.notNullableBooleanPropertySpec(VALIDATE_INVOKE_ID);
    }

    private PropertySpec deviceId() {
        return PropertySpecFactory.stringPropertySpec(DEVICE_ID, DEFAULT_DEVICE_ID);
    }

    private PropertySpec requestTimeZonePropertySpec() {
        return PropertySpecFactory.notNullableBooleanPropertySpec(REQUEST_TIMEZONE);
    }

    private PropertySpec forcedDelayPropertySpec() {
        return PropertySpecFactory.timeDurationPropertySpecWithSmallUnitsAndDefaultValue(
                FORCED_DELAY,
                new TimeDuration(DEFAULT_FORCED_DELAY.intValue() / 1000));
    }

    private PropertySpec conformanceBlockValuePropertySpec() {
        return PropertySpecFactory.bigDecimalPropertySpec(CONFORMANCE_BLOCK_VALUE, BigDecimal.valueOf(ConformanceBlock.DEFAULT_LN_CONFORMANCE_BLOCK));
    }

    private PropertySpec manufacturerPropertySpec() {
        return PropertySpecFactory.stringPropertySpecWithValuesAndDefaultValue(MANUFACTURER, DEFAULT_MANUFACTURER, "WKP", "ISK", "LGZ", "SLB", "ActarisPLCC", "SLB::SL7000");
    }

    private PropertySpec maxRecPduSizePropertySpec() {
        return PropertySpecFactory.bigDecimalPropertySpec(MAX_REC_PDU_SIZE, DEFAULT_MAX_REC_PDU_SIZE);
    }

    private PropertySpec retriesPropertySpec() {
        return PropertySpecFactory.bigDecimalPropertySpec(RETRIES, DEFAULT_RETRIES);
    }

    private PropertySpec timeoutPropertySpec() {
        return PropertySpecFactory.timeDurationPropertySpecWithSmallUnitsAndDefaultValue(TIMEOUT, new TimeDuration(DEFAULT_TIMEOUT.intValue() / 1000));
    }

    private PropertySpec roundTripCorrectionPropertySpec() {
        return PropertySpecFactory.bigDecimalPropertySpec(ROUND_TRIP_CORRECTION, DEFAULT_ROUND_TRIP_CORRECTION);
    }

    private PropertySpec bulkRequestPropertySpec() {
        return PropertySpecFactory.notNullableBooleanPropertySpec(BULK_REQUEST);
    }

    private PropertySpec cipheringTypePropertySpec() {
        return PropertySpecFactory.bigDecimalPropertySpec(CIPHERING_TYPE, DEFAULT_CIPHERING_TYPE);
    }

    private PropertySpec ntaSimulationToolPropertySpec() {
        return PropertySpecFactory.notNullableBooleanPropertySpec(NTA_SIMULATION_TOOL);
    }

    private PropertySpec fixMbusHexShortIdPropertySpec() {
        return PropertySpecFactory.notNullableBooleanPropertySpec(FIX_MBUS_HEX_SHORT_ID);
    }
}
