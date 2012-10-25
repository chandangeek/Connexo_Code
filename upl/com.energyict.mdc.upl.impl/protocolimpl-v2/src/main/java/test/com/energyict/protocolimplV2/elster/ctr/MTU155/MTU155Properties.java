package test.com.energyict.protocolimplV2.elster.ctr.MTU155;

import com.energyict.cpo.TypedProperties;
import com.energyict.protocol.MeterProtocol;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.math.BigDecimal;

/**
 * Copyrights EnergyICT
 * Date: 29-sep-2010
 * Time: 15:58:55
 */
public class MTU155Properties {

    public static final String TIMEOUT_PROPERTY_NAME = "Timeout";
    public static final String RETRIES_PROPERTY_NAME = "Retries";
    public static final String DELAY_AFTER_ERROR_PROPERTY_NAME = "DelayAfterError";
    public static final String FORCED_DELAY_PROPERTY_NAME = "ForcedDelay";
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

    public static final BigDecimal DEFAULT_TIMEOUT = new BigDecimal(10000);
    public static final BigDecimal DEFAULT_RETRIES = new BigDecimal(3);
    public static final BigDecimal DEFAULT_DELAY_AFTER_ERROR = new BigDecimal(100);
    public static final BigDecimal DEFAULT_FORCED_DELAY = new BigDecimal(0);
    public static final String DEFAULT_KEYC = "1234567890123456";
    public static final String DEFAULT_KEYT = "1234567890123456";
    public static final String DEFAULT_KEYF = "1234567890123456";
    public static final String DEFAULT_PASSWORD = "000001";
    public static final BigDecimal DEFAULT_ADDRESS = new BigDecimal(0);
    public static final String DEFAULT_CHANNEL_CONFIG = "1.0.2:1.2.2:4.0.2:7.0.2:1.1.3:1.3.3:1.F.2:2.0.3:2.1.3:2.3.3:1.A.3";
    public static final BigDecimal DEFAULT_SECURITY_LEVEL = new BigDecimal(1); // 0 == KeyT, 1 == KeyC, 2 == KeyF
    public static final Boolean DEFAULT_DEBUG = false;
    public static final BigDecimal DEFAULT_CHANNEL_BACKLOG = new BigDecimal(85);
    public static final Boolean DEFAULT_SEND_END_OF_SESSION = true;
    public static final BigDecimal DEFAULT_MAX_ALLOWED_INVALID_PROFILE_RESPONSES = new BigDecimal(5);
    public static final Boolean DEFAULT_EXTRACT_INSTALLATION_DATE = true;
    public static final Boolean DEFAULT_REMOVE_DAY_PROFILE_OFFSET = false;

    private TypedProperties typedProperties;

    public MTU155Properties(TypedProperties typedProperties) {
        this.typedProperties = typedProperties;
    }

    public int getTimeout() {
        return ((BigDecimal) typedProperties.getProperty(TIMEOUT_PROPERTY_NAME, DEFAULT_TIMEOUT)).intValue();
    }

    public int getRetries() {
        return ((BigDecimal) typedProperties.getProperty(RETRIES_PROPERTY_NAME, DEFAULT_RETRIES)).intValue();
    }

    public int getDelayAfterError() {
        return ((BigDecimal) typedProperties.getProperty(DELAY_AFTER_ERROR_PROPERTY_NAME, DEFAULT_DELAY_AFTER_ERROR)).intValue();
    }

    public int getForcedDelay() {
        return ((BigDecimal) typedProperties.getProperty(FORCED_DELAY_PROPERTY_NAME, DEFAULT_FORCED_DELAY)).intValue();
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
        // ToDo: update of Key C
        throw new UnsupportedOperationException("Update of KeyC is not yet implemented!");
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
        return ((BigDecimal) typedProperties.getProperty(SECURITY_LEVEL_PROPERTY_NAME, DEFAULT_SECURITY_LEVEL)).intValue();
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

    private String getKeyValue(String propertyName, String defaultValue) {
        String key = (String) typedProperties.getProperty(propertyName, defaultValue);
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
