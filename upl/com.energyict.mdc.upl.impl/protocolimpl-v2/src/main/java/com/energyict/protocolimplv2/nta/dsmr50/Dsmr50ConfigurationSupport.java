package com.energyict.protocolimplv2.nta.dsmr50;

import com.energyict.dlms.common.DlmsProtocolProperties;
import com.energyict.mdc.protocol.LegacyProtocolProperties;
import com.energyict.mdc.upl.nls.TranslationKey;
import com.energyict.mdc.upl.properties.HasDynamicProperties;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecBuilder;
import com.energyict.mdc.upl.properties.PropertySpecBuilderWizard;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.properties.TypedProperties;
import com.energyict.nls.PropertyTranslationKeys;
import com.energyict.protocolimpl.dlms.g3.G3Properties;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;
import com.energyict.protocolimplv2.dlms.g3.properties.AS330DConfigurationSupport;
import com.energyict.protocolimplv2.nta.dsmr50.elster.am540.Dsmr50Properties;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import static com.energyict.dlms.common.DlmsProtocolProperties.BULK_REQUEST;
import static com.energyict.dlms.common.DlmsProtocolProperties.DEFAULT_FORCED_DELAY;
import static com.energyict.dlms.common.DlmsProtocolProperties.DEFAULT_MAX_REC_PDU_SIZE;
import static com.energyict.dlms.common.DlmsProtocolProperties.FORCED_DELAY;
import static com.energyict.dlms.common.DlmsProtocolProperties.MAX_REC_PDU_SIZE;
import static com.energyict.dlms.common.DlmsProtocolProperties.NTA_SIMULATION_TOOL;
import static com.energyict.dlms.common.DlmsProtocolProperties.REQUEST_TIMEZONE;
import static com.energyict.dlms.common.DlmsProtocolProperties.TIMEZONE;
import static com.energyict.dlms.common.DlmsProtocolProperties.VALIDATE_INVOKE_ID;

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
public class Dsmr50ConfigurationSupport implements HasDynamicProperties {

    public static final String CHECK_NUMBER_OF_BLOCKS_DURING_FIRMWARE_RESUME = "CheckNumberOfBlocksDuringFirmwareResume";
    public static final String USE_EQUIPMENT_IDENTIFIER_AS_SERIAL = "UseEquipmentIdentifierAsSerialNumber";
    public static final String PROPERTY_IGNORE_DST_STATUS_CODE = "IgnoreDstStatusCode";
    public static final boolean DEFAULT_CHECK_NUMBER_OF_BLOCKS_DURING_FIRMWARE_RESUME = true;
    public static final boolean USE_EQUIPMENT_IDENTIFIER_AS_SERIAL_DEFAULT_VALUE = true;
    public static final String POLLING_DELAY = "PollingDelay";
    private static final boolean DEFAULT_VALIDATE_INVOKE_ID = true;
    public static final String REQUEST_FRAMECOUNTER = "RequestFrameCounter";

    private final PropertySpecService propertySpecService;
    private com.energyict.mdc.upl.TypedProperties properties;

    public Dsmr50ConfigurationSupport(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Override
    public List<com.energyict.mdc.upl.properties.PropertySpec> getUPLPropertySpecs() {
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
        return this.spec(REQUEST_FRAMECOUNTER, PropertyTranslationKeys.V2_REQUEST_FRAMECOUNTER, this.propertySpecService::booleanSpec);
    }

    @Override
    public void setUPLProperties(TypedProperties properties) throws PropertyValidationException {
        this.properties = com.energyict.mdc.upl.TypedProperties.copyOf(properties);
    }

    private PropertySpec pollingDelayPropertySpec() {
        return this.spec(POLLING_DELAY, PropertyTranslationKeys.V2_ELSTER_POLLING_DELAY, this.propertySpecService::durationSpec);
    }

    private PropertySpec mirrorLogicalDeviceIdPropertySpec() {
        return this.spec(AS330DConfigurationSupport.MIRROR_LOGICAL_DEVICE_ID, PropertyTranslationKeys.V2_ELSTER_MIRROR_LOGICAL_DEVICE_ID, this.propertySpecService::bigDecimalSpec);
    }

    private PropertySpec actualLogicalDeviceIdPropertySpec() {
        return this.spec(AS330DConfigurationSupport.GATEWAY_LOGICAL_DEVICE_ID, PropertyTranslationKeys.V2_ELSTER_GATEWAY_LOGICAL_DEVICE_ID, this.propertySpecService::bigDecimalSpec);
    }

    private PropertySpec lastSeenDatePropertySpec() {
        return this.spec(G3Properties.PROP_LASTSEENDATE, PropertyTranslationKeys.V2_ELSTER_LAST_SEND_DATE, this.propertySpecService::bigDecimalSpec);
    }

    private PropertySpec serverLowerMacAddressPropertySpec() {
        return this
                .specBuilder(DlmsProtocolProperties.SERVER_LOWER_MAC_ADDRESS, PropertyTranslationKeys.V2_NTA_SERVER_LOWER_MAC_ADDRESS, this.propertySpecService::bigDecimalSpec)
                .setDefaultValue(BigDecimal.ZERO)
                .finish();
    }

    protected PropertySpec nodeAddressPropertySpec() {
        return this.spec(com.energyict.mdc.upl.MeterProtocol.Property.NODEID.getName(), PropertyTranslationKeys.V2_NTA_NODEID, this.propertySpecService::bigDecimalSpec);
    }

    protected PropertySpec callHomeIdPropertySpec() {
        return this.spec(LegacyProtocolProperties.CALL_HOME_ID_PROPERTY_NAME, PropertyTranslationKeys.V2_ELSTER_CALL_HOME_ID, this.propertySpecService::stringSpec);
    }

    private PropertySpec checkNumberOfBlocksDuringFirmwareResumePropertySpec() {
        return this
                .specBuilder(CHECK_NUMBER_OF_BLOCKS_DURING_FIRMWARE_RESUME, PropertyTranslationKeys.V2_NTA_CHECK_NUMBER_OF_BLOCKS_DURING_FIRMWARE_RESUME, this.propertySpecService::booleanSpec)
                .setDefaultValue(DEFAULT_CHECK_NUMBER_OF_BLOCKS_DURING_FIRMWARE_RESUME)
                .finish();
    }

    protected PropertySpec timeZonePropertySpec() {
        return this.spec(TIMEZONE, PropertyTranslationKeys.V2_NTA_TIMEZONE, this.propertySpecService::timeZoneSpec);
    }

    protected PropertySpec validateInvokeIdPropertySpec() {
        return this
                .specBuilder(VALIDATE_INVOKE_ID, PropertyTranslationKeys.V2_NTA_VALIDATE_INVOKE_ID, this.propertySpecService::booleanSpec)
                .setDefaultValue(DEFAULT_VALIDATE_INVOKE_ID)
                .finish();
    }

    protected PropertySpec cumulativeCaptureTimeChannelPropertySpec() {
        return this
                .specBuilder(Dsmr50Properties.CumulativeCaptureTimeChannel, PropertyTranslationKeys.V2_NTA_CUMULATIVE_CAPTURE_TIME_CHANNEL, this.propertySpecService::booleanSpec)
                .setDefaultValue(false)
                .finish();
    }

    protected PropertySpec readCachePropertySpec() {
        return this
                .specBuilder(Dsmr50Properties.READCACHE_PROPERTY, PropertyTranslationKeys.V2_NTA_READCACHE, this.propertySpecService::booleanSpec)
                .setDefaultValue(false)
                .finish();
    }

    protected PropertySpec pskPropertySpec() {
        return this.spec(G3Properties.PSK, PropertyTranslationKeys.V2_NTA_PSK, this.propertySpecService::stringSpec);
    }

    protected PropertySpec aarqTimeoutPropertySpec() {
        return this
                .specBuilder(Dsmr50Properties.AARQ_TIMEOUT_PROPERTY, PropertyTranslationKeys.V2_NTA_AARQ_TIMEOUT, this.propertySpecService::bigDecimalSpec)
                .setDefaultValue(BigDecimal.ZERO)
                .finish();
    }

    protected PropertySpec aarqRetriesPropertySpec() {
        return this
                .specBuilder(Dsmr50Properties.AARQ_RETRIES_PROPERTY, PropertyTranslationKeys.V2_NTA_AARQ_RETRIES, this.propertySpecService::bigDecimalSpec)
                .setDefaultValue(BigDecimal.valueOf(2))
                .finish();
    }

    protected PropertySpec requestTimeZonePropertySpec() {
        return this.spec(REQUEST_TIMEZONE, PropertyTranslationKeys.V2_NTA_REQUEST_TIMEZONE, this.propertySpecService::booleanSpec);
    }

    protected PropertySpec forcedDelayPropertySpec() {
        return this
                .specBuilder(FORCED_DELAY, PropertyTranslationKeys.V2_NTA_FORCED_DELAY, this.propertySpecService::durationSpec)
                .setDefaultValue(DEFAULT_FORCED_DELAY)
                .finish();
    }

    protected PropertySpec maxRecPduSizePropertySpec() {
        return this
                .specBuilder(MAX_REC_PDU_SIZE, PropertyTranslationKeys.V2_NTA_MAX_REC_PDU_SIZE, this.propertySpecService::bigDecimalSpec)
                .setDefaultValue(DEFAULT_MAX_REC_PDU_SIZE)
                .finish();
    }

    protected PropertySpec bulkRequestPropertySpec() {
        return this.spec(BULK_REQUEST, PropertyTranslationKeys.V2_NTA_BULK_REQUEST, this.propertySpecService::booleanSpec);
    }

    protected PropertySpec ntaSimulationToolPropertySpec() {
        return this.spec(NTA_SIMULATION_TOOL, PropertyTranslationKeys.V2_NTA_NTA_SIMULATION_TOOL, this.propertySpecService::booleanSpec);
    }

    protected PropertySpec ignoreDstStatusCode() {
        return this.spec(PROPERTY_IGNORE_DST_STATUS_CODE, PropertyTranslationKeys.V2_NTA_IGNORE_DST_STATUS_CODE, this.propertySpecService::booleanSpec);
    }

    private PropertySpec useEquipmentIdentifierAsSerialNumberPropertySpec() {
        return this
                .specBuilder(USE_EQUIPMENT_IDENTIFIER_AS_SERIAL, PropertyTranslationKeys.V2_NTA_USE_EQUIPMENT_IDENTIFIER_AS_SERIAL, this.propertySpecService::booleanSpec)
                .setDefaultValue(USE_EQUIPMENT_IDENTIFIER_AS_SERIAL_DEFAULT_VALUE)
                .finish();
    }

    private <T> PropertySpec spec(String name, TranslationKey translationKey, Supplier<PropertySpecBuilderWizard.NlsOptions<T>> optionsSupplier) {
        return this.specBuilder(name, translationKey, optionsSupplier).finish();
    }

    private <T> PropertySpecBuilder<T> specBuilder(String name, TranslationKey translationKey, Supplier<PropertySpecBuilderWizard.NlsOptions<T>> optionsSupplier) {
        return UPLPropertySpecFactory.specBuilder(name, false, translationKey, optionsSupplier);
    }

}