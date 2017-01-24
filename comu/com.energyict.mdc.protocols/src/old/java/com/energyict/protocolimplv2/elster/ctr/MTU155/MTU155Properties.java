package com.energyict.protocolimplv2.elster.ctr.MTU155;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.legacy.MeterProtocol;
import com.energyict.mdc.protocol.api.timezones.TimeZoneInUse;

import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.common.BasicDynamicPropertySupport;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;

/**
 * Copyrights EnergyICT
 * Date: 29-sep-2010
 * Time: 15:58:55
 */
public class MTU155Properties extends BasicDynamicPropertySupport{

    public static final String LEGACY_TIMEZONE_PROPERTY_NAME = "LegacyTimeZone";
    public static final String ENCRTYPTION_KEY_C_PROPERTY_NAME = "KeyC";
    public static final String ENCRTYPTION_KEY_F_PROPERTY_NAME = "KeyF";
    public static final String ENCRYPTION_KEY_T_PROPERTY_NAME = "KeyT";
    public static final String PASSWORD_PROPERTY_NAME = MeterProtocol.PASSWORD;
    public static final String ADDRESS_PROPERTY_NAME = MeterProtocol.NODEID;
    public static final String CHANNEL_CONFIG_PROPERTY_NAME = "ChannelConfig";
    public static final String SECURITY_LEVEL_PROPERTY_NAME = "SecurityLevel";
    public static final String DEBUG_PROPERTY_NAME = "Debug";
    public static final String CHANNEL_BACKLOG_PROPERTY_NAME = "ChannelBacklog";
    public static final String SEND_END_OF_SESSION_PROPERTY_NAME = "SendEndOfSession";
    public static final String MAX_ALLOWED_INVALID_PROFILE_RESPONSES_PROPERTY_NAME = "MaxAllowedInvalidProfileResponses";
    public static final String EXTRACT_INSTALLATION_DATE_PROPERTY_NAME = "ExtractInstallationDate";
    public static final String REMOVE_DAY_PROFILE_OFFSET_PROPERTY_NAME = "RemoveDayProfileOffset";
    public static final String USE_LONG_FRAME_FORMAT_PROPERTY_NAME = "UseLongFrameFormat";

    public static final TimeZone DEFAULT_TIMEZONE = TimeZone.getDefault();
    public static final BigDecimal DEFAULT_TIMEOUT = new BigDecimal(10000);
    public static final BigDecimal DEFAULT_RETRIES = new BigDecimal(3);
    public static final BigDecimal DEFAULT_DELAY_AFTER_ERROR = new BigDecimal(100);
    public static final BigDecimal DEFAULT_FORCED_DELAY = BigDecimal.ZERO;
    public static final String DEFAULT_KEYC = "1234567890123456";
    public static final String DEFAULT_KEYT = "1234567890123456";
    public static final String DEFAULT_KEYF = "1234567890123456";
    public static final String DEFAULT_PASSWORD = "000001";
    public static final BigDecimal DEFAULT_ADDRESS = BigDecimal.ZERO;
    public static final String DEFAULT_CHANNEL_CONFIG = "1.0.2:1.2.2:4.0.2:7.0.2:1.1.3:1.3.3:1.F.2:2.0.3:2.1.3:2.3.3:1.A.3";
    public static final String DEFAULT_SECURITY_LEVEL = "1"; // 0 == KeyT, 1 == KeyC, 2 == KeyF
    public static final Boolean DEFAULT_DEBUG = false;
    public static final BigDecimal DEFAULT_CHANNEL_BACKLOG = new BigDecimal(85);
    public static final Boolean DEFAULT_SEND_END_OF_SESSION = true;
    public static final BigDecimal DEFAULT_MAX_ALLOWED_INVALID_PROFILE_RESPONSES = new BigDecimal(5);
    public static final Boolean DEFAULT_EXTRACT_INSTALLATION_DATE = true;
    public static final Boolean DEFAULT_REMOVE_DAY_PROFILE_OFFSET = false;
    public static final Boolean DEFAULT_USE_LONG_FRAME_FORMAT = true;

    private TypedProperties typedProperties;

    public MTU155Properties(TypedProperties typedProperties, PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super(propertySpecService, thesaurus);
        this.typedProperties = typedProperties;
    }

    public MTU155Properties(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super(propertySpecService, thesaurus);
        this.typedProperties = TypedProperties.empty();
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        List<PropertySpec> propertySpecs = new ArrayList<>(super.getPropertySpecs());
        Collections.addAll(
                propertySpecs,
                debugPropertySpec(),
                channelBacklogPropertySpec(),
                extractInstallationDatePropertySpec(),
                removeDayProfileOffsetPropertySpec());
        return propertySpecs;
    }

    private PropertySpec debugPropertySpec() {
        return getPropertySpecService()
                .booleanSpec()
                .named(DEBUG_PROPERTY_NAME, MTU155TranslationKeys.DEBUG)
                .fromThesaurus(this.getThesaurus())
                .setDefaultValue(DEFAULT_DEBUG)
                .finish();
    }

    private PropertySpec channelBacklogPropertySpec() {
        return getPropertySpecService()
                .bigDecimalSpec()
                .named(CHANNEL_BACKLOG_PROPERTY_NAME, MTU155TranslationKeys.CHANNEL_BACKLOG)
                .fromThesaurus(this.getThesaurus())
                .setDefaultValue(DEFAULT_CHANNEL_BACKLOG)
                .finish();
    }

    private PropertySpec extractInstallationDatePropertySpec() {
        return getPropertySpecService()
                .booleanSpec()
                .named(EXTRACT_INSTALLATION_DATE_PROPERTY_NAME, MTU155TranslationKeys.EXTRACT_INSTALLATION_DATE)
                .fromThesaurus(this.getThesaurus())
                .setDefaultValue(DEFAULT_EXTRACT_INSTALLATION_DATE)
                .finish();
    }

    private PropertySpec removeDayProfileOffsetPropertySpec() {
        return getPropertySpecService()
                .booleanSpec()
                .named(REMOVE_DAY_PROFILE_OFFSET_PROPERTY_NAME, MTU155TranslationKeys.REMOVE_DAY_PROFILE_OFFSET)
                .fromThesaurus(this.getThesaurus())
                .setDefaultValue(DEFAULT_REMOVE_DAY_PROFILE_OFFSET)
                .finish();
    }

    public TimeZone getTimeZone() {
        TimeZoneInUse timeZoneInUse = (TimeZoneInUse) typedProperties.getProperty(TIMEZONE);
        String legacyTimeZone = (String) typedProperties.getProperty(LEGACY_TIMEZONE_PROPERTY_NAME);
        if (timeZoneInUse != null) {
            return timeZoneInUse.getTimeZone();
        } else if (legacyTimeZone != null) {
            return TimeZone.getTimeZone(legacyTimeZone);
        } else {
            return DEFAULT_TIMEZONE;
        }
    }

    public int getTimeout() {
        return ((BigDecimal) typedProperties.getProperty(TIMEOUT, DEFAULT_TIMEOUT)).intValue();
    }

    public int getRetries() {
        return ((BigDecimal) typedProperties.getProperty(RETRIES, DEFAULT_RETRIES)).intValue();
    }

    public int getDelayAfterError() {
        return ((BigDecimal) typedProperties.getProperty(DELAY_AFTER_ERROR, DEFAULT_DELAY_AFTER_ERROR)).intValue();
    }

    public int getForcedDelay() {
        return ((BigDecimal) typedProperties.getProperty(FORCED_DELAY, DEFAULT_FORCED_DELAY)).intValue();
    }

    public String getKeyC() {
        return getKeyValue(ENCRTYPTION_KEY_C_PROPERTY_NAME, DEFAULT_KEYC);
    }

    public String getKeyT() {
        return getKeyValue(ENCRYPTION_KEY_T_PROPERTY_NAME, DEFAULT_KEYT);
    }

    public String getKeyF() {
        return getKeyValue(ENCRTYPTION_KEY_F_PROPERTY_NAME, DEFAULT_KEYF);
    }

    public byte[] getKeyCBytes() {
        return getByteValue(getKeyC());
    }

    public byte[] getKeyTBytes() {
        return getByteValue(getKeyT());
    }

    public byte[] getKeyFBytes() {
        return getByteValue(getKeyF());
    }

    public void updateKeyC(String keyC) {
        typedProperties.setProperty(ENCRTYPTION_KEY_C_PROPERTY_NAME, keyC);
    }

    public String getPassword() {
        return (String) typedProperties.getProperty(PASSWORD_PROPERTY_NAME, DEFAULT_PASSWORD);
    }

    public int getAddress() {
        return ((BigDecimal) typedProperties.getProperty(ADDRESS_PROPERTY_NAME, DEFAULT_ADDRESS)).intValue();
    }

    public MTU155ChannelConfig getChannelConfig() {
        String property = (String) typedProperties.getProperty(CHANNEL_CONFIG_PROPERTY_NAME, DEFAULT_CHANNEL_CONFIG);
        return new MTU155ChannelConfig(property);
    }

    public int getSecurityLevel() {
        return Integer.parseInt((String) typedProperties.getProperty(SECURITY_LEVEL_PROPERTY_NAME, DEFAULT_SECURITY_LEVEL));
    }

    public boolean isDebug() {
        return (Boolean) typedProperties.getProperty(DEBUG_PROPERTY_NAME, DEFAULT_DEBUG);
    }

    /**
     * Number of days we are expected to be able read from the meter. The MTU155 can only store 90 days of data.
     * This is used to determine a last reading date in discovery that actually makes sense instead of a ridiculous old date.
     * It is also used to determine the last reading date on each channel after receiving data by SMS
     *
     * @return int value describing the number of days.
     */
    public int getChannelBacklog() {
        return ((BigDecimal) typedProperties.getProperty(CHANNEL_BACKLOG_PROPERTY_NAME, DEFAULT_CHANNEL_BACKLOG)).intValue();
    }

    public boolean isSendEndOfSession() {
        return (Boolean) typedProperties.getProperty(SEND_END_OF_SESSION_PROPERTY_NAME, DEFAULT_SEND_END_OF_SESSION);
    }

    public int getMaxAllowedInvalidProfileResponses() {
        return ((BigDecimal) typedProperties.getProperty(MAX_ALLOWED_INVALID_PROFILE_RESPONSES_PROPERTY_NAME, DEFAULT_MAX_ALLOWED_INVALID_PROFILE_RESPONSES)).intValue();
    }

    public boolean getExtractInstallationDate() {
        return (Boolean) typedProperties.getProperty(EXTRACT_INSTALLATION_DATE_PROPERTY_NAME, DEFAULT_EXTRACT_INSTALLATION_DATE);
    }

    public boolean removeDayProfileOffset() {
        return (Boolean) typedProperties.getProperty(REMOVE_DAY_PROFILE_OFFSET_PROPERTY_NAME, DEFAULT_REMOVE_DAY_PROFILE_OFFSET);
    }

    public boolean useLongFrameFormat() {
        return (Boolean) typedProperties.getProperty(USE_LONG_FRAME_FORMAT_PROPERTY_NAME, DEFAULT_USE_LONG_FRAME_FORMAT);
    }

    public boolean isParseInstallationArchive() {
        return false;
   }

    private String getKeyValue(String propertyName, String defaultValue) {
        String key = (String) typedProperties.getProperty(propertyName, defaultValue);
        if (key.isEmpty()) {
            key = defaultValue;
        }

        if (key.length() == 16) {
            return ProtocolTools.getHexStringFromBytes(key.getBytes(), "");
        } else if (key.length() == 32) {
            return key;
        } else {
            throw new IllegalArgumentException("Invalid key format! Key should be 16 bytes or 32 bytes long.");
        }
    }

    private byte[] getByteValue(String value) {
        return ProtocolTools.getBytesFromHexString(value, "");
    }
}
