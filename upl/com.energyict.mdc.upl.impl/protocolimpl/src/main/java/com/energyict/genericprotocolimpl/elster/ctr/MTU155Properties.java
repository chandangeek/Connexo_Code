package com.energyict.genericprotocolimpl.elster.ctr;

import com.energyict.protocol.*;
import com.energyict.protocolimpl.base.AbstractProtocolProperties;
import com.energyict.protocolimpl.base.ProtocolProperty;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.util.*;

/**
 * Copyrights EnergyICT
 * Date: 29-sep-2010
 * Time: 15:58:55
 */
public class MTU155Properties extends AbstractProtocolProperties {

    public static final String TIMEOUT = "Timeout";
    public static final String RETRIES = "Retries";
    public static final String DELAY_AFTER_ERROR = "DelayAfterError";
    public static final String FORCED_DELAY = "ForcedDelay";
    public static final String KEYC = "KeyC";
    public static final String KEYF = "KeyF";
    public static final String KEYT = "KeyT";
    public static final String PASSWORD = MeterProtocol.PASSWORD;
    public static final String ADDRESS = MeterProtocol.NODEID;
    public static final String CHANNEL_CONFIG = "ChannelConfig";
    public static final String SECURITY_LEVEL = "SecurityLevel";
    public static final String DEBUG = "Debug";
    public static final String FOLDER_EXTERNAL_NAME = "FolderExtName";
    public static final String RTU_TYPE = "RtuType";
    public static final String SMS_QUEUE = "SmsQueue";
    public static final String CHANNEL_BACKLOG = "ChannelBacklog";
    public static final String FAST_DEPLOYMENT = "FastDeployment";
    public static final String SEND_END_OF_SESSION = "SendEndOfSession";
    public static final String GENERATE_RANDOM_MTU_SERIAL = "GenerateRandomMTUSerial";
    public static final String MAX_ALLOWED_INVALID_PROFILE_RESPONSES = "MaxAllowedInvalidProfileResponses";
    public static final String DISABLE_DST_FOR_KNOCKING_DEVICES = "DisableDSTForKnockingDevices";
    public static final String EXTRACT_INSTALLATION_DATE = "ExtractInstallationDate";
    public static final String REMOVE_DAY_PROFILE_OFFSET = "RemoveDayProfileOffset";

    public static final String DEFAULT_TIMEOUT = "10000";
    public static final String DEFAULT_RETRIES = "3";
    public static final String DEFAULT_DELAY_AFTER_ERROR = "100";
    public static final String DEFAULT_FORCED_DELAY = "0";
    public static final String DEFAULT_KEYC = "1234567890123456";
    public static final String DEFAULT_KEYT = "1234567890123456";
    public static final String DEFAULT_KEYF = "1234567890123456";
    public static final String DEFAULT_PASSWORD = "000001";
    public static final String DEFAULT_ADDRESS = "0";
    public static final String DEFAULT_CHANNEL_CONFIG = "1.0.2:1.2.2:4.0.2:7.0.2:1.1.3:1.3.3:1.F.2:2.0.3:2.1.3:2.3.3:1.A.3";
    public static final String DEFAULT_SECURITY_LEVEL = "1"; // 0 == KeyT, 1 == KeyC, 2 == KeyF
    public static final String DEFAULT_DEBUG = "0";
    public static final String DEFAULT_FOLDER_EXTERNAL_NAME = null;
    public static final String DEFAULT_RTU_TYPE = null;
    public static final String DEFAULT_SMS_QUEUE = null;
    public static final String DEFAULT_CHANNEL_BACKLOG = "85";
    public static final String DEFAULT_FAST_DEPLOYMENT = "0";
    public static final String DEFAULT_SEND_END_OF_SESSION = "1";
    public static final String DEFAULT_GENERATE_RANDOM_MTU_SERIAL = "0";
    public static final String DEFAULT_MAX_ALLOWED_INVALID_PROFILE_RESPONSES = "5";
    public static final String DEFAULT_DISABLE_DST_FOR_KNOCKING_DEVICES = "0";
    public static final String DEFAULT_EXTRACT_INSTALLATION_DATE = "1";
    public static final String DEFAULT_REMOVE_DAY_PROFILE_OFFSET = "0";

    public MTU155Properties() {
        this(new Properties());
    }

    @Override
    protected void doValidateProperties() throws MissingPropertyException, InvalidPropertyException {

    }

    public MTU155Properties(Properties properties) {
        super(properties);
    }

    public List<String> getOptionalKeys() {
        List<String> optional = new ArrayList<String>();
        optional.add(TIMEOUT);
        optional.add(RETRIES);
        optional.add(DELAY_AFTER_ERROR);
        optional.add(FORCED_DELAY);
        optional.add(PASSWORD);
        optional.add(ADDRESS);
        optional.add(CHANNEL_CONFIG);
        optional.add(SECURITY_LEVEL);
        optional.add(DEBUG);
        optional.add(FOLDER_EXTERNAL_NAME);
        optional.add(CHANNEL_BACKLOG);
        optional.add(FAST_DEPLOYMENT);
        optional.add(SEND_END_OF_SESSION);
        optional.add(GENERATE_RANDOM_MTU_SERIAL);
        optional.add(MAX_ALLOWED_INVALID_PROFILE_RESPONSES);
        optional.add(DISABLE_DST_FOR_KNOCKING_DEVICES);
        optional.add(EXTRACT_INSTALLATION_DATE);
        optional.add(REMOVE_DAY_PROFILE_OFFSET);
        return optional;
    }

    public List<String> getRequiredKeys() {
        List<String> required = new ArrayList<String>();
        required.add(KEYC);
        required.add(KEYF);
        required.add(KEYT);
        required.add(RTU_TYPE);
        required.add(SMS_QUEUE);
        return required;
    }

    public void addProperty(String key, String value) {
        getProtocolProperties().setProperty(key, value);
    }

    @ProtocolProperty
    public int getRetries() {
        return getIntProperty(RETRIES, DEFAULT_RETRIES);
    }

    @ProtocolProperty
    public int getTimeout() {
        return getIntProperty(TIMEOUT, DEFAULT_TIMEOUT);
    }

    @ProtocolProperty
    public boolean removeDayProfileOffset() {
        return getBooleanProperty(REMOVE_DAY_PROFILE_OFFSET, DEFAULT_REMOVE_DAY_PROFILE_OFFSET);
    }

    @ProtocolProperty
    public int getDelayAfterError() {
        return getIntProperty(DELAY_AFTER_ERROR, DEFAULT_DELAY_AFTER_ERROR);
    }

    @ProtocolProperty
    public int getForcedDelay() {
        return getIntProperty(FORCED_DELAY, DEFAULT_FORCED_DELAY);
    }

    @ProtocolProperty
    public int getExtractInstallationDate() {
        return getIntProperty(EXTRACT_INSTALLATION_DATE, DEFAULT_EXTRACT_INSTALLATION_DATE);
    }

    @ProtocolProperty
    public String getKeyC() {
        return getKeyValue(KEYC, DEFAULT_KEYC);
    }

    @ProtocolProperty
    public String getKeyT() {
        return getKeyValue(KEYT, DEFAULT_KEYT);
    }

    @ProtocolProperty
    public String getKeyF() {
        return getKeyValue(KEYF, DEFAULT_KEYF);
    }

    public void updateKeyC(String keyC) {
        getProtocolProperties().setProperty(KEYC, keyC);
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

    @ProtocolProperty
    public String getPassword() {
        return getStringValue(PASSWORD, DEFAULT_PASSWORD);
    }

    @ProtocolProperty
    public int getAddress() {
        return getIntProperty(ADDRESS, DEFAULT_ADDRESS);
    }

    @ProtocolProperty
    public MTU155ChannelConfig getChannelConfig() {
        return new MTU155ChannelConfig(getStringValue(CHANNEL_CONFIG, DEFAULT_CHANNEL_CONFIG));
    }

    @ProtocolProperty
    public int getSecurityLevel() {
        return getIntProperty(SECURITY_LEVEL, DEFAULT_SECURITY_LEVEL);
    }

    @ProtocolProperty
    public boolean isDebug() {
        return getIntProperty(DEBUG, DEFAULT_DEBUG) == 1;
    }

    @ProtocolProperty
    public String getFolderExternalName() {
        return getStringValue(FOLDER_EXTERNAL_NAME, DEFAULT_FOLDER_EXTERNAL_NAME);
    }

    @ProtocolProperty
    public String getRtuType() {
        return getStringValue(RTU_TYPE, DEFAULT_RTU_TYPE);
    }

    @ProtocolProperty
    public String getSmsQueue() {
        return getStringValue(SMS_QUEUE, DEFAULT_SMS_QUEUE);
    }

    /**
     * Number of days we are expected to be able read from the meter. The MTU155 can only store 90 days of data.
     * This is used to determine a last reading date in discovery that actually makes sense instead of a ridiculous old date.
     * It is also used to determine the last reading date on each channel after receiving data by SMS
     *
     * @return int value describing the number of days.
     */
    @ProtocolProperty
    public int getChannelBacklog() {
        return getIntProperty(CHANNEL_BACKLOG, DEFAULT_CHANNEL_BACKLOG);
    }

    @ProtocolProperty
    public boolean isFastDeployment() {
        return getIntProperty(FAST_DEPLOYMENT, DEFAULT_FAST_DEPLOYMENT) == 1;
    }

    @ProtocolProperty
    public boolean isSendEndOfSession() {
        return getIntProperty(SEND_END_OF_SESSION, DEFAULT_SEND_END_OF_SESSION) == 1;
    }

    @ProtocolProperty
    public boolean isGenerateRandomMTUSerial() {
        return getIntProperty(GENERATE_RANDOM_MTU_SERIAL, DEFAULT_GENERATE_RANDOM_MTU_SERIAL) == 1;
    }

    @ProtocolProperty
    public int getMaxAllowedInvalidProfileResponses() {
        return getIntProperty(MAX_ALLOWED_INVALID_PROFILE_RESPONSES, DEFAULT_MAX_ALLOWED_INVALID_PROFILE_RESPONSES);
    }

    @ProtocolProperty
    public boolean isDisableDSTForKnockingDevices() {
        return getIntProperty(DISABLE_DST_FOR_KNOCKING_DEVICES, DEFAULT_DISABLE_DST_FOR_KNOCKING_DEVICES) == 1;
    }

    private String getKeyValue(String propertyName, String defaultValue) {
        String key = getStringValue(propertyName, defaultValue);
        if (key.length() == 16) {
            return ProtocolTools.getHexStringFromBytes(key.getBytes(), "");
        } else if (key.length() == 32) {
            return key;
        } else {
            throw new IllegalArgumentException("Invalid key format! Key should be 16 bytes or 32 bytes long.");
        }
    }

}
