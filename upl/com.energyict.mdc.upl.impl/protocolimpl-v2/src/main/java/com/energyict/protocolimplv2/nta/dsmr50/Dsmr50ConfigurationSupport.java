package com.energyict.protocolimplv2.nta.dsmr50;

import com.energyict.cbo.ConfigurationSupport;
import com.energyict.cbo.TimeDuration;
import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.dlms.common.DlmsProtocolProperties;
import com.energyict.mdc.protocol.LegacyProtocolProperties;
import com.energyict.protocol.MeterProtocol;
import com.energyict.protocolimpl.dlms.g3.G3Properties;
import com.energyict.protocolimplv2.dlms.g3.properties.AS330DConfigurationSupport;
import com.energyict.protocolimplv2.nta.dsmr50.elster.am540.Dsmr50Properties;

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

    public static final String CHECK_NUMBER_OF_BLOCKS_DURING_FIRMWARE_RESUME = "CheckNumberOfBlocksDuringFirmwareResume";
    public static final String USE_EQUIPMENT_IDENTIFIER_AS_SERIAL = "UseEquipmentIdentifierAsSerialNumber";
    public static final String PROPERTY_IGNORE_DST_STATUS_CODE = "IgnoreDstStatusCode";
    public static final boolean DEFAULT_CHECK_NUMBER_OF_BLOCKS_DURING_FIRMWARE_RESUME = true;
    public static final boolean USE_EQUIPMENT_IDENTIFIER_AS_SERIAL_DEFAULT_VALUE = true;
    public static final String POLLING_DELAY = "PollingDelay";
    private static final boolean DEFAULT_VALIDATE_INVOKE_ID = true;
    public static final String REQUEST_FRAMECOUNTER = "RequestFrameCounter";

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
                this.readCachePropertySpec(),
                this.pskPropertySpec(),
                this.aarqTimeoutPropertySpec(),
                this.aarqRetriesPropertySpec(),
                this.cumulativeCaptureTimeChannelPropertySpec(),
                this.nodeAddressPropertySpec(),
                this.callHomeIdPropertySpec(),
                this.mirrorLogicalDeviceIdPropertySpec(),
                this.actualLogicalDeviceIdPropertySpec(),
                this.serverLowerMacAddressPropertySpec(),
                this.checkNumberOfBlocksDuringFirmwareResumePropertySpec(),
                this.lastSeenDatePropertySpec(),
                this.useEquipmentIdentifierAsSerialNumberPropertySpec(),
                this.ignoreDstStatusCode(),
                this.pollingDelayPropertySpec(),
                this.requestFrameCounter()
        );
    }

    private PropertySpec requestFrameCounter() {
        return PropertySpecFactory.booleanPropertySpec(REQUEST_FRAMECOUNTER);
    }

    private PropertySpec pollingDelayPropertySpec() {
        return PropertySpecFactory.timeDurationPropertySpecWithSmallUnitsAndDefaultValue(POLLING_DELAY, new TimeDuration(0));
    }

    private PropertySpec mirrorLogicalDeviceIdPropertySpec() {
        return PropertySpecFactory.bigDecimalPropertySpec(AS330DConfigurationSupport.MIRROR_LOGICAL_DEVICE_ID);
    }

    private PropertySpec actualLogicalDeviceIdPropertySpec() {
        return PropertySpecFactory.bigDecimalPropertySpec(AS330DConfigurationSupport.GATEWAY_LOGICAL_DEVICE_ID);
    }

    private PropertySpec lastSeenDatePropertySpec() {
        return PropertySpecFactory.bigDecimalPropertySpec(G3Properties.PROP_LASTSEENDATE);
    }

    private PropertySpec serverLowerMacAddressPropertySpec() {
        return PropertySpecFactory.bigDecimalPropertySpec(DlmsProtocolProperties.SERVER_LOWER_MAC_ADDRESS, BigDecimal.ZERO);
    }

    protected PropertySpec nodeAddressPropertySpec() {
        return PropertySpecFactory.bigDecimalPropertySpec(MeterProtocol.NODEID);
    }

    protected PropertySpec callHomeIdPropertySpec() {
        return PropertySpecFactory.stringPropertySpec(LegacyProtocolProperties.CALL_HOME_ID_PROPERTY_NAME);
    }

    private PropertySpec checkNumberOfBlocksDuringFirmwareResumePropertySpec() {
        return PropertySpecFactory.notNullableBooleanPropertySpec(CHECK_NUMBER_OF_BLOCKS_DURING_FIRMWARE_RESUME, DEFAULT_CHECK_NUMBER_OF_BLOCKS_DURING_FIRMWARE_RESUME);
    }

    protected PropertySpec timeZonePropertySpec() {
        return PropertySpecFactory.timeZoneInUseReferencePropertySpec(TIMEZONE);
    }

    protected PropertySpec validateInvokeIdPropertySpec() {
        return PropertySpecFactory.notNullableBooleanPropertySpec(VALIDATE_INVOKE_ID, DEFAULT_VALIDATE_INVOKE_ID);
    }

    protected PropertySpec cumulativeCaptureTimeChannelPropertySpec() {
        return PropertySpecFactory.notNullableBooleanPropertySpec(Dsmr50Properties.CumulativeCaptureTimeChannel, false);
    }

    protected PropertySpec readCachePropertySpec() {
        return PropertySpecFactory.notNullableBooleanPropertySpec(Dsmr50Properties.READCACHE_PROPERTY, false);
    }

    protected PropertySpec pskPropertySpec() {
        return PropertySpecFactory.stringPropertySpec(G3Properties.PSK);
    }

    protected PropertySpec aarqTimeoutPropertySpec() {
        return PropertySpecFactory.bigDecimalPropertySpec(Dsmr50Properties.AARQ_TIMEOUT_PROPERTY, BigDecimal.ZERO);
    }

    protected PropertySpec aarqRetriesPropertySpec() {
        return PropertySpecFactory.bigDecimalPropertySpec(Dsmr50Properties.AARQ_RETRIES_PROPERTY, BigDecimal.valueOf(2));
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

    protected PropertySpec ignoreDstStatusCode() {
        return PropertySpecFactory.notNullableBooleanPropertySpec(PROPERTY_IGNORE_DST_STATUS_CODE, false);
    }

    private PropertySpec useEquipmentIdentifierAsSerialNumberPropertySpec() {
        return PropertySpecFactory.notNullableBooleanPropertySpec(USE_EQUIPMENT_IDENTIFIER_AS_SERIAL, USE_EQUIPMENT_IDENTIFIER_AS_SERIAL_DEFAULT_VALUE);
    }
}