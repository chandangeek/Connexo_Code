package com.energyict.protocolimplv2.dlms.hon.as300n.properties;

import com.energyict.dlms.CipheringType;
import com.energyict.mdc.upl.nls.TranslationKey;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecBuilder;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.nls.PropertyTranslationKeys;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;
import com.energyict.protocolimplv2.dlms.idis.am540.properties.AM540ConfigurationSupport;
import com.energyict.protocolimplv2.nta.dsmr23.DlmsConfigurationSupport;
import com.energyict.protocolimplv2.nta.dsmr23.DlmsProperties;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static com.energyict.dlms.common.DlmsProtocolProperties.ADDRESSING_MODE;
import static com.energyict.dlms.common.DlmsProtocolProperties.DEFAULT_FORCED_DELAY;
import static com.energyict.dlms.common.DlmsProtocolProperties.FORCED_DELAY;


public class AS300NConfigurationSupport extends DlmsConfigurationSupport {
    /**
     * Use service specific global ciphering by default.
     */
    public static final CipheringType DEFAULT_CIPHERING_TYPE = CipheringType.GLOBAL;

    public static final String AARQ_TIMEOUT_PROPERTY = "AARQTimeout";
    public static final String AARQ_RETRIES_PROPERTY = "AARQRetries";
    public static final String USE_EQUIPMENT_IDENTIFIER_AS_SERIAL = "UseEquipmentIdentifierAsSerialNumber";
    public static final String POLLING_DELAY = "PollingDelay";
    public static final String INITIAL_FRAME_COUNTER = "InitialFrameCounter";
    public static final String USE_METER_IN_TRANSPARENT_MODE = "UseMeterInTransparentMode";
    public static final String TRANSP_CONNECT_TIME = "TransparentConnectTime";
    public static final String PASSWORD = "IEC1107Password";
    public static final String METER_SECURITY_LEVEL = "SecurityLevel";
    public static final String REQUEST_AUTHENTICATED_FRAME_COUNTER = "RequestAuthenticatedFrameCounter";
    public static final String USE_CACHED_FRAME_COUNTER = "UseCachedFrameCounter";
    public static final String VALIDATE_CACHED_FRAMECOUNTER = "ValidateCachedFrameCounterAndFallback";
    public static final String FRAME_COUNTER_RECOVERY_RETRIES = "FrameCounterRecoveryRetries";
    public static final String FRAME_COUNTER_RECOVERY_STEP = "FrameCounterRecoveryStep";
    public static final String IP_V4_ADDRESS = "IPv4Address";
    public static final String IP_V6_ADDRESS = "IPv6Address";
    public static final String SHORT_ADDRESS_PAN = "ShortAddressPAN";
    public static final String DEFAULT_TRANSPARENT_PASSWORD = "00000000";
    public static final String DEFAULT_TRANSPARENT_SECURITY_LEVEL = "1:0";

    /**
     * Indicates whether the meter supports hundreths or not.
     * <p/>
     * For example SAG meters will generate an other-reason if this field is included.
     */
    public static final String SUPPORTS_HUNDRETHS_TIMEFIELD = "SupportsHundredthsTimeField";

    /**
     * Indicates whether the meter does not accept a time deviation other than undefined. (SAG again).
     */
    public static final String USE_UNDEFINED_AS_TIME_DEVIATION = "UseUndefinedAsTimeDeviation";

    /**
     * Indicates whether the meter will accept anything else but undefined as clock status.
     */
    public static final String USE_UNDEFINED_AS_CLOCK_STATUS = "UseUndefinedAsClockStatus";

    /**
     * Indicates whether or not to skip the authentication tag validation.
     */
    public static final String SKIP_FC_AUTH_TAG_VALIDATION = "SkipFrameCounterAuthenticationTag";

    /**
     * Indicates whether or not to use a static object list.
     */
    public static final String USE_FIXED_OBJECT_LIST = "UseFixedObjectList";

    /**
     * Skips slave devices.
     */
    public static final String SKIP_SLAVE_DEVICES = "SkipSlaveDevices";

    private static final BigDecimal DEFAULT_MAX_REC_PDU_SIZE = new BigDecimal(207);

    public static final boolean USE_EQUIPMENT_IDENTIFIER_AS_SERIAL_DEFAULT_VALUE = false;
    public static final BigDecimal DEFAULT_SERVER_LOWER_MAC_ADDRESS = BigDecimal.valueOf(256);
    public static final BigDecimal DEFAULT_SERVER_UPPER_MAC_ADDRESS = BigDecimal.valueOf(1);
    public static final Duration DEFAULT_NOT_USED_AARQ_TIMEOUT = Duration.ofSeconds(0);



    public AS300NConfigurationSupport(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        List<PropertySpec> uplPropertySpecs = new ArrayList<>(super.getUPLPropertySpecs());

        uplPropertySpecs.add( this.addressingModePropertySpec());
        return uplPropertySpecs;
    }

    protected PropertySpec addressingModePropertySpec() {
        return bigDecimalSpec(ADDRESSING_MODE, false, PropertyTranslationKeys.V2_TASKS_ADDRESSING_MODE, BigDecimal.valueOf(4));
    }


}