package com.energyict.protocolimplv2.nta.dsmr50;

import com.energyict.cbo.ConfigurationSupport;
import com.energyict.cbo.TimeDuration;
import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.protocolimplv2.nta.dsmr50.elster.am540.DSMR50Properties;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.energyict.dlms.common.DlmsProtocolProperties.*;

/**
 * A collection of general DSMR50 properties.
 * These properties are not related to the security or the protocol dialects.
 * The parsing and the usage of the property values is done in implementations of {@link com.energyict.dlms.protocolimplv2.DlmsSessionProperties}
 * <p/>
 * Copyrights EnergyICT
 * Date: 22/10/13
 * Time: 15:41
 * Author: khe
 */
public class Dsmr50ConfigurationSupport implements ConfigurationSupport {

    private static final boolean DEFAULT_VALIDATE_INVOKE_ID = true;

    @Override
    public List<PropertySpec> getRequiredProperties() {
        return Collections.emptyList();
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        return Arrays.asList(
                this.forcedDelayPropertySpec(),
                this.maxRecPduSizePropertySpec(),
                this.bulkRequestPropertySpec(),
                this.ntaSimulationToolPropertySpec(),
                this.requestTimeZonePropertySpec(),
                this.timeZonePropertySpec(),
                this.validateInvokeIdPropertySpec(),
                this.deviceId(),
                this.readCachePropertySpec(),
                this.pskPropertySpec(),
                this.aarqTimeoutPropertySpec(),
                this.aarqRetriesPropertySpec(),
                this.cumulativeCaptureTimeChannelPropertySpec());
    }

    protected PropertySpec timeZonePropertySpec() {
        return PropertySpecFactory.timeZoneInUseReferencePropertySpec(TIMEZONE);
    }

    protected PropertySpec validateInvokeIdPropertySpec() {
        return PropertySpecFactory.notNullableBooleanPropertySpec(VALIDATE_INVOKE_ID, DEFAULT_VALIDATE_INVOKE_ID);
    }

    protected PropertySpec cumulativeCaptureTimeChannelPropertySpec() {
        return PropertySpecFactory.notNullableBooleanPropertySpec(DSMR50Properties.CumulativeCaptureTimeChannel, false);
    }

    protected PropertySpec readCachePropertySpec() {
        return PropertySpecFactory.notNullableBooleanPropertySpec(DSMR50Properties.READCACHE_PROPERTY, false);
    }

    protected PropertySpec pskPropertySpec() {
        return PropertySpecFactory.hexStringPropertySpec(DSMR50Properties.PSK_PROPERTY);
    }

    protected PropertySpec aarqTimeoutPropertySpec() {
        return PropertySpecFactory.bigDecimalPropertySpec(DSMR50Properties.AARQ_TIMEOUT_PROPERTY, BigDecimal.ZERO);
    }

    protected PropertySpec aarqRetriesPropertySpec() {
        return PropertySpecFactory.bigDecimalPropertySpec(DSMR50Properties.AARQ_RETRIES_PROPERTY, BigDecimal.valueOf(2));
    }

    protected PropertySpec deviceId() {
        return PropertySpecFactory.stringPropertySpec(DEVICE_ID, DEFAULT_DEVICE_ID);
    }

    protected PropertySpec requestTimeZonePropertySpec() {
        return PropertySpecFactory.notNullableBooleanPropertySpec(REQUEST_TIMEZONE);
    }

    protected PropertySpec forcedDelayPropertySpec() {
        return PropertySpecFactory.timeDurationPropertySpecWithSmallUnitsAndDefaultValue(
                FORCED_DELAY,
                new TimeDuration(DEFAULT_FORCED_DELAY.intValue() / 1000));
    }

    protected PropertySpec maxRecPduSizePropertySpec() {
        return PropertySpecFactory.bigDecimalPropertySpec(MAX_REC_PDU_SIZE, DEFAULT_MAX_REC_PDU_SIZE);
    }

    protected PropertySpec bulkRequestPropertySpec() {
        return PropertySpecFactory.notNullableBooleanPropertySpec(BULK_REQUEST);
    }

    protected PropertySpec ntaSimulationToolPropertySpec() {
        return PropertySpecFactory.notNullableBooleanPropertySpec(NTA_SIMULATION_TOOL);
    }
}