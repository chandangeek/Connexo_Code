package com.energyict.protocolimplv2.dlms.idis.am540.properties;

import com.energyict.cbo.TimeDuration;
import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.dlms.common.DlmsProtocolProperties;
import com.energyict.protocol.MeterProtocol;
import com.energyict.protocolimpl.dlms.g3.G3Properties;
import com.energyict.protocolimplv2.dlms.g3.properties.AS330DConfigurationSupport;
import com.energyict.protocolimplv2.dlms.idis.am130.properties.AM130ConfigurationSupport;

import static com.energyict.dlms.common.DlmsProtocolProperties.DEFAULT_MAX_REC_PDU_SIZE;
import static com.energyict.dlms.common.DlmsProtocolProperties.MAX_REC_PDU_SIZE;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.energyict.dlms.common.DlmsProtocolProperties.VALIDATE_LOAD_PROFILE_CHANNELS;

/**
 * @author sva
 * @since 11/08/2015 - 15:15
 */
public class AM540ConfigurationSupport extends AM130ConfigurationSupport {

    public static final String AARQ_TIMEOUT_PROPERTY = "AARQTimeout";
    public static final String AARQ_RETRIES_PROPERTY = "AARQRetries";
    public static final String USE_EQUIPMENT_IDENTIFIER_AS_SERIAL = "UseEquipmentIdentifierAsSerialNumber";
    public static final String POLLING_DELAY = "PollingDelay";
    public static final String INITIAL_FRAME_COUNTER = "InitialFrameCounter";
    public static final String USE_METER_IN_TRANSPARENT_MODE = "UseMeterInTransparentMode";
    public static final String TRANSP_CONNECT_TIME = "TransparentConnectTime";
    public static final String PASSWORD = "Password";
    public static final String METER_SECURITY_LEVEL = "SecurityLevel";
    public static final String REQUEST_AUTHENTICATED_FRAME_COUNTER = "RequestAuthenticatedFrameCounter";
    public static final String USE_CACHED_FRAME_COUNTER = "UseCachedFrameCounter";
    public static final String VALIDATE_CACHED_FRAMECOUNTER = "ValidateCachedFrameCounterAndFallback";
    public static final String FRAME_COUNTER_RECOVERY_RETRIES = "FrameCounterRecoveryRetries";
    public static final String FRAME_COUNTER_RECOVERY_STEP = "FrameCounterRecoveryStep"; 
    
    /** 
     * Indicates whether the meter supports hundreths or not.
     * 
     * For example SAG meters will generate an other-reason if this field is included.
     */
    public static final String SUPPORTS_HUNDRETHS_TIMEFIELD = "SupportsHundredthsTimeField";
    
    /**
     * Indicates whether the meter does not accept a time deviation other than undefined. (SAG again).
     */
    public static final String USE_UNDEFINED_AS_TIME_DEVIATION = "UseUndefinedAsTimeDeviation";
    
    /** Indicates whether the meter will accept anything else but undefined as clock status. */
    public static final String USE_UNDEFINED_AS_CLOCK_STATUS = "UseUndefinedAsClockStatus";
    
    /** Indicates whether or not to skip the authentication tag validation. */
    public static final String SKIP_FC_AUTH_TAG_VALIDATION = "SkipFrameCounterAuthenticationTag";

    /** Indicates whether or not to use a static object list. */
    public static final String USE_FIXED_OBJECT_LIST = "UseFixedObjectList";
    
    /** Skips slave devices. */
    public static final String SKIP_SLAVE_DEVICES = "SkipSlaveDevices";
   
    /** The default max-apdu-size when using G3. */
    private static final BigDecimal DEFAULT_MAX_REC_PDU_SIZE = new BigDecimal(1224);
    
    public static final boolean USE_EQUIPMENT_IDENTIFIER_AS_SERIAL_DEFAULT_VALUE = false;
    public static final BigDecimal DEFAULT_SERVER_LOWER_MAC_ADDRESS = BigDecimal.valueOf(17);
    public static final int NOT_USED_AARQ_TIMEOUT = 0;

    @Override
    public List<PropertySpec> getRequiredProperties() {
        return Collections.emptyList();
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        return Arrays.asList(
                this.forcedDelayPropertySpec(),
                this.maxRecPduSizePropertySpec(),
                this.timeZonePropertySpec(),
                this.validateInvokeIdPropertySpec(),
                this.limitMaxNrOfDaysPropertySpec(),
                this.readCachePropertySpec(),
                this.callingAPTitlePropertySpec(),
                this.callHomeIdPropertySpec(),
                this.serverLowerMacAddressPropertySpec(),
                this.mirrorLogicalDeviceIdPropertySpec(),
                this.actualLogicalDeviceIdPropertySpec(),
                this.nodeAddressPropertySpec(),
                this.pskPropertySpec(),
                this.useEquipmentIdentifierAsSerialNumberPropertySpec(),
                this.aarqTimeoutPropertySpec(),
                this.lastSeenDatePropertySpec(),
                this.aarqRetriesPropertySpec(),
                this.pollingDelayPropertySpec(),
                this.initialFrameCounter(),
                this.useMeterInTransparentMode(),
                this.transparentConnectTime(),
                this.transparentSecurityLevel(),
                this.transparentPassword(),
                this.requestAuthenticatedFrameCounter(),
                this.useCachedFrameCounter(),
                this.validateCachedFrameCounter(),
                this.frameCounterRecoveryRetries(),
                this.frameCounterRecoveryStep(),
                this.deviceSystemTitlePropertySpec(),
                this.supportsHundrethsTimeField(),
                this.useCachedFrameCounter(),
                this.useGeneralBlockTransferPropertySpec(),
                this.generalBlockTransferWindowSizePropertySpec(),
                this.supportsHundrethsTimeField(),
                this.useUndefinedForClockStatus(),
                this.useUndefinedForTimeDeviation(),
                this.skipFramecounterAuthenticationTagValidation(),
                this.useFixedObjectList(),
                this.skipSlaveDevices(),
                this.validateLoadProfileChannelsPropertySpec()
        );
    }
    
    private PropertySpec frameCounterRecoveryRetries() {
        return PropertySpecFactory.bigDecimalPropertySpec(AM540ConfigurationSupport.FRAME_COUNTER_RECOVERY_RETRIES, BigDecimal.valueOf(100));
    }
    private PropertySpec frameCounterRecoveryStep() {
        return PropertySpecFactory.bigDecimalPropertySpec(AM540ConfigurationSupport.FRAME_COUNTER_RECOVERY_STEP, BigDecimal.ONE);
    }

    private PropertySpec validateCachedFrameCounter() {
        return PropertySpecFactory.booleanPropertySpec(AM540ConfigurationSupport.VALIDATE_CACHED_FRAMECOUNTER);
    }
    
    /**
     * Returns the "SupportsHundrethsTimefield" property spec.
     * 
     * @return	The property specification.
     */
    private final PropertySpec<Boolean> supportsHundrethsTimeField() {
    	return PropertySpecFactory.notNullableBooleanPropertySpec(AM540ConfigurationSupport.SUPPORTS_HUNDRETHS_TIMEFIELD, Boolean.FALSE);
    }
    
    /**
     * Returns the "UseUndefinedAsClockStatus" property spec.
     * 
     * @return	The property specification.
     */
    private final PropertySpec<Boolean> skipFramecounterAuthenticationTagValidation() {
    	return PropertySpecFactory.notNullableBooleanPropertySpec(AM540ConfigurationSupport.SKIP_FC_AUTH_TAG_VALIDATION, Boolean.FALSE);
    }
    
    /**
     * Returns the "SkipSlaveDecives" property spec.
     * 
     * @return	The property specification.
     */
    private final PropertySpec<Boolean> skipSlaveDevices() {
    	return PropertySpecFactory.notNullableBooleanPropertySpec(AM540ConfigurationSupport.SKIP_SLAVE_DEVICES, Boolean.TRUE);
    }
    
    /**
     * Returns the "UseUndefinedAs" property spec.
     * 
     * @return	The property specification.
     */
    private final PropertySpec<Boolean> useUndefinedForClockStatus() {
    	return PropertySpecFactory.notNullableBooleanPropertySpec(AM540ConfigurationSupport.USE_UNDEFINED_AS_CLOCK_STATUS, Boolean.TRUE);
    }
    
    /**
     * Returns the "UseUndefinedAsTimeDeviation" property spec.
     * 
     * @return	The property specification.
     */
    private final PropertySpec<Boolean> useUndefinedForTimeDeviation() {
    	return PropertySpecFactory.notNullableBooleanPropertySpec(AM540ConfigurationSupport.USE_UNDEFINED_AS_TIME_DEVIATION, Boolean.TRUE);
    }
    
    /**
     * Indicates whether or not to use a fixed object list.
     * 
     * @return	Whether or not to use a fixed object list.
     */
    private final PropertySpec<Boolean> useFixedObjectList() {
    	return PropertySpecFactory.notNullableBooleanPropertySpec(AM540ConfigurationSupport.USE_FIXED_OBJECT_LIST, Boolean.FALSE);
    }
    
    /**
     * Indicates whether or not to use a cached frame counter.
     * 
     * @return	<code>true</code> for a cached frame counter, <code>false</code> if not.
     */
    private PropertySpec useCachedFrameCounter() {
        return PropertySpecFactory.booleanPropertySpec(AM540ConfigurationSupport.USE_CACHED_FRAME_COUNTER);
    }

    private PropertySpec requestAuthenticatedFrameCounter() {
        return PropertySpecFactory.booleanPropertySpec(AM540ConfigurationSupport.REQUEST_AUTHENTICATED_FRAME_COUNTER);
    }

    private PropertySpec lastSeenDatePropertySpec() {
        return PropertySpecFactory.bigDecimalPropertySpec(G3Properties.PROP_LASTSEENDATE);
    }

    private PropertySpec pollingDelayPropertySpec() {
        return PropertySpecFactory.timeDurationPropertySpecWithSmallUnitsAndDefaultValue(POLLING_DELAY, new TimeDuration(0));
    }

    private PropertySpec pskPropertySpec() {
        return PropertySpecFactory.stringPropertySpec(G3Properties.PSK);
    }

    protected PropertySpec nodeAddressPropertySpec() {
        return PropertySpecFactory.bigDecimalPropertySpec(MeterProtocol.NODEID);
    }

    private PropertySpec serverLowerMacAddressPropertySpec() {
        return PropertySpecFactory.bigDecimalPropertySpec(DlmsProtocolProperties.SERVER_LOWER_MAC_ADDRESS, DEFAULT_SERVER_LOWER_MAC_ADDRESS);
    }

    protected PropertySpec mirrorLogicalDeviceIdPropertySpec() {
        return PropertySpecFactory.bigDecimalPropertySpec(AS330DConfigurationSupport.MIRROR_LOGICAL_DEVICE_ID);
    }

    protected PropertySpec actualLogicalDeviceIdPropertySpec() {
        return PropertySpecFactory.bigDecimalPropertySpec(AS330DConfigurationSupport.GATEWAY_LOGICAL_DEVICE_ID);
    }

    private PropertySpec useEquipmentIdentifierAsSerialNumberPropertySpec() {
        return PropertySpecFactory.notNullableBooleanPropertySpec(USE_EQUIPMENT_IDENTIFIER_AS_SERIAL, USE_EQUIPMENT_IDENTIFIER_AS_SERIAL_DEFAULT_VALUE);
    }

    private PropertySpec aarqTimeoutPropertySpec() {
        return PropertySpecFactory.timeDurationPropertySpecWithSmallUnitsAndDefaultValue(AARQ_TIMEOUT_PROPERTY, new TimeDuration(NOT_USED_AARQ_TIMEOUT));
    }

    private PropertySpec aarqRetriesPropertySpec() {
        return PropertySpecFactory.bigDecimalPropertySpec(AARQ_RETRIES_PROPERTY, BigDecimal.valueOf(2));
    }

    private PropertySpec initialFrameCounter() {
        return PropertySpecFactory.positiveDecimalPropertySpec(INITIAL_FRAME_COUNTER);
    }

    private PropertySpec useMeterInTransparentMode() {
        return PropertySpecFactory.notNullableBooleanPropertySpec(USE_METER_IN_TRANSPARENT_MODE, false);
    }

    private PropertySpec transparentConnectTime() {
        return PropertySpecFactory.bigDecimalPropertySpec(TRANSP_CONNECT_TIME, BigDecimal.valueOf(10));
    }

    private PropertySpec transparentPassword() {
        return PropertySpecFactory.stringPropertySpec(PASSWORD, "00000000");
    }

    private PropertySpec transparentSecurityLevel() {
        return PropertySpecFactory.stringPropertySpec(METER_SECURITY_LEVEL, "1:0");
    }

    public PropertySpec deviceSystemTitlePropertySpec() {
        return PropertySpecFactory.stringPropertySpec(DlmsProtocolProperties.DEVICE_SYSTEM_TITLE);
    }
    
    /**
     * Overriding this one for the max-apdu-size.
     * 
     * {@inheritDoc}
     */
    protected final PropertySpec<BigDecimal> maxRecPduSizePropertySpec() {
        return PropertySpecFactory.bigDecimalPropertySpec(MAX_REC_PDU_SIZE, DEFAULT_MAX_REC_PDU_SIZE);
    }

    protected PropertySpec validateLoadProfileChannelsPropertySpec() {
        return PropertySpecFactory.notNullableBooleanPropertySpec(VALIDATE_LOAD_PROFILE_CHANNELS);
    }

}