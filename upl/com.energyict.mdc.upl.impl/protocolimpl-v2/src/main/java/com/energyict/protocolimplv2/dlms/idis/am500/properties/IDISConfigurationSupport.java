package com.energyict.protocolimplv2.dlms.idis.am500.properties;

import com.energyict.cbo.ConfigurationSupport;
import com.energyict.cbo.TimeDuration;
import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.mdc.protocol.LegacyProtocolProperties;
import com.energyict.protocolimpl.dlms.idis.IDIS;
import com.energyict.protocolimplv2.nta.dsmr50.elster.am540.DSMR50Properties;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import static com.energyict.dlms.common.DlmsProtocolProperties.*;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 19/12/2014 - 15:53
 */
public class IDISConfigurationSupport implements ConfigurationSupport {

    private static final boolean DEFAULT_VALIDATE_INVOKE_ID = true;
    public static final String SWAP_SERVER_AND_CLIENT_ADDRESS_PROPERTY = "SwapServerAndClientAddress";
    public static final String LIMIT_MAX_NR_OF_DAYS_PROPERTY = "LimitMaxNrOfDays";

    @Override
    public List<PropertySpec> getRequiredProperties() {
        return Collections.emptyList();
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        return Arrays.asList(
                this.callingAPTitlePropertySpec(),
                this.serverUpperMacAddressPropertySpec(),
                this.retriesPropertySpec(),
                this.timeoutPropertySpec(),
                this.forcedDelayPropertySpec(),
                this.maxRecPduSizePropertySpec(),
                this.timeZonePropertySpec(),
                this.validateInvokeIdPropertySpec(),
                this.roundTripCorrectionPropertySpec(),
                this.swapServerAndClientAddress(),
                this.callHomeIdProperty(),
                this.limitMaxNrOfDaysPropertySpec(),
                this.readCachePropertySpec());
    }

    public PropertySpec callingAPTitlePropertySpec() {
        return PropertySpecFactory.stringPropertySpec(IDIS.CALLING_AP_TITLE, IDIS.CALLING_AP_TITLE_DEFAULT);
    }

    private PropertySpec callHomeIdProperty() { //Contains the MAC address of the meter
        return PropertySpecFactory.stringPropertySpec(LegacyProtocolProperties.CALL_HOME_ID_PROPERTY_NAME);
    }

    private PropertySpec limitMaxNrOfDaysPropertySpec() {
        return PropertySpecFactory.bigDecimalPropertySpec(LIMIT_MAX_NR_OF_DAYS_PROPERTY, BigDecimal.ZERO);
    }

    public PropertySpec serverUpperMacAddressPropertySpec() {
        return PropertySpecFactory.bigDecimalPropertySpec(SERVER_UPPER_MAC_ADDRESS, BigDecimal.ONE);
    }

    private PropertySpec retriesPropertySpec() {
        return PropertySpecFactory.bigDecimalPropertySpec(RETRIES, DEFAULT_RETRIES);
    }

    private PropertySpec timeoutPropertySpec() {
        return PropertySpecFactory.timeDurationPropertySpecWithSmallUnitsAndDefaultValue(TIMEOUT, new TimeDuration(DEFAULT_TIMEOUT.intValue(), Calendar.MILLISECOND));
    }

    private PropertySpec roundTripCorrectionPropertySpec() {
        return PropertySpecFactory.bigDecimalPropertySpec(ROUND_TRIP_CORRECTION, DEFAULT_ROUND_TRIP_CORRECTION);
    }

    protected PropertySpec timeZonePropertySpec() {
        return PropertySpecFactory.timeZoneInUseReferencePropertySpec(TIMEZONE);
    }

    protected PropertySpec validateInvokeIdPropertySpec() {
        return PropertySpecFactory.notNullableBooleanPropertySpec(VALIDATE_INVOKE_ID, DEFAULT_VALIDATE_INVOKE_ID);
    }

    protected PropertySpec swapServerAndClientAddress() {
        return PropertySpecFactory.notNullableBooleanPropertySpec(SWAP_SERVER_AND_CLIENT_ADDRESS_PROPERTY, true);
    }

    protected PropertySpec readCachePropertySpec() {
        return PropertySpecFactory.notNullableBooleanPropertySpec(DSMR50Properties.READCACHE_PROPERTY, false);
    }

    protected PropertySpec forcedDelayPropertySpec() {
        return PropertySpecFactory.timeDurationPropertySpecWithSmallUnitsAndDefaultValue(
                FORCED_DELAY,
                new TimeDuration(DEFAULT_FORCED_DELAY.intValue() / 1000));
    }

    protected PropertySpec maxRecPduSizePropertySpec() {
        return PropertySpecFactory.bigDecimalPropertySpec(MAX_REC_PDU_SIZE, DEFAULT_MAX_REC_PDU_SIZE);
    }
}