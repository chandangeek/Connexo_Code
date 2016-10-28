package com.energyict.protocolimpl.iec1107.emh.nxt4;

import com.energyict.mdc.upl.properties.InvalidPropertyException;
import com.energyict.mdc.upl.properties.MissingPropertyException;

import com.energyict.cbo.ConfigurationSupport;
import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.protocolimpl.base.ProtocolChannelMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @author sva
 * @since 4/11/2014 - 13:45
 */
public class NXT4Properties implements ConfigurationSupport {

    private final NXT4 meterProtocol;
    private Properties protocolProperties;

    public NXT4Properties(NXT4 meterProtocol) {
        this.meterProtocol = meterProtocol;
    }

    /**
     * Validate and set the given properties.
     *
     * @param properties the properties to validate and set
     * @throws InvalidPropertyException thrown in case a property has an invalid value
     * @throws MissingPropertyException thrown in case a required property was missing
     */
    public void validateAndSetProperties(Properties properties) throws InvalidPropertyException, MissingPropertyException {
        // 1. Validate the properties
        validateAllRequiredPropertiesAreFilledIn(properties);
        validateIntegerProperty(properties, "Timeout");
        validateIntegerProperty(properties, "Retries");
        validateIntegerProperty(properties, "RoundTripCorrection");
        validateIntegerProperty(properties, "SecurityLevel");
        validateIntegerProperty(properties, "EchoCancelling");
        validateIntegerProperty(properties, "ForcedDelay");
        validateIntegerProperty(properties, "IEC1107Compatible");
        validateIntegerProperty(properties, "ProfileInterval");
        validateIntegerProperty(properties, "RequestHeader");
        validateIntegerProperty(properties, "DataReadout");
        validateIntegerProperty(properties, "ExtendedLogging");
        validateProtocolChannelMap(properties, "ChannelMap");

        // 2. Set the properties
        this.protocolProperties = properties;
    }

    private void validateIntegerProperty(Properties properties, String propertyName) throws InvalidPropertyException {
        try {
            Integer.parseInt(properties.getProperty(propertyName, "0"));
        } catch (NumberFormatException e) {
            throw new InvalidPropertyException("Property " + propertyName + " has an invalid value: NumberFormatException, " + e.getMessage());
        }
    }

    private void validateProtocolChannelMap(Properties properties, String propertyName) throws InvalidPropertyException {
        try {
            new ProtocolChannelMap(properties.getProperty("ChannelMap", "0,0,0,0"));
        } catch (InvalidPropertyException e) {
            throw new InvalidPropertyException("Property " + propertyName + " has an invalid value: InvalidPropertyException, " + e.getMessage());
        } catch (NumberFormatException e) {
            throw new InvalidPropertyException("Property " + propertyName + " has an invalid value: NumberFormatException, " + e.getMessage());
        }
    }

    private void validateAllRequiredPropertiesAreFilledIn(Properties properties) throws MissingPropertyException {
        for (Object key : getRequiredKeys()) {
            if (properties.getProperty((String) key) == null) {
                throw new MissingPropertyException("Required property " + key + " is missing");
            }
        }
    }

    public String getDeviceId() {
        return protocolProperties.getProperty(com.energyict.mdc.upl.MeterProtocol.Property.ADDRESS.getName());
    }

    public String getNodeAddress() {
        return getProperty(com.energyict.mdc.upl.MeterProtocol.Property.NODEID.getName(), "");
    }

    public String getPassword() {
        return getProperty(com.energyict.mdc.upl.MeterProtocol.Property.PASSWORD.getName());
    }

    public int getIEC1107TimeOut() {
        return getIntProperty("Timeout", "10000");
    }

    public int getRetries() {
        return getIntProperty("Retries", "3");
    }

    public int getRoundTripCorrection() {
        return getIntProperty("RoundTripCorrection", "0");
    }

    public int getSecurityLevel() {
        return getIntProperty("SecurityLevel", "1");
    }

    public int getEchoCancelling() {
        return getIntProperty("EchoCancelling", "0");
    }

    public int getForcedDelay() {
        return getIntProperty("ForcedDelay", "300");
    }

    public int getIEC1107Compatible() {
        return getIntProperty("IEC1107Compatible", "1");
    }

    public int getProfileInterval() {
        return getIntProperty("ProfileInterval", "900");
    }

    public boolean isRequestHeader() {
        return getBooleanProperty("RequestHeader", "0");
    }

    public boolean isDataReadout() {
        return getBooleanProperty("DataReadout", "1");
    }

    protected void setDataReadout(boolean useDataReadout) {
        getProtocolProperties().setProperty("DataReadout", useDataReadout ? "1" : "0");
    }

    public boolean useExtendedLogging() {
        return getBooleanProperty("ExtendedLogging", "0");
    }

    public boolean useSoftware7E1() {
        return getBooleanProperty("Software7E1", "0");
    }

    public boolean readUserLogBook() {
        return getBooleanProperty("ReadUserLogBook", "0");
    }

    public boolean reconnectAfterR6Read() {
        return getBooleanProperty("ReconnectAfterR6Read", "1");
    }

    public ProtocolChannelMap getProtocolChannelMap() {
        try {
            return new ProtocolChannelMap(getProperty("ChannelMap", "0,0,0,0"));
        } catch (InvalidPropertyException e) {
            return null;
        }
    }

    public String getDateFormat() {
        return getProperty("DateFormat", "yyMMddHHmmsswwnz");
    }

    private boolean getBooleanProperty(String propertyName, String defaultValue) {
        return getIntProperty(propertyName, defaultValue) == 1;
    }

    private int getIntProperty(String propertyName, String defaultValue) {
        return Integer.parseInt(protocolProperties.getProperty(propertyName, defaultValue));
    }

    private String getProperty(String propertyName) {
        return getProperty(propertyName, "");
    }

    private String getProperty(String propertyName, String defaultValue) {
        return getProtocolProperties().getProperty(propertyName, defaultValue);
    }

    public Properties getProtocolProperties() {
        return protocolProperties;
    }

    public NXT4 getMeterProtocol() {
        return meterProtocol;
    }

    public List<String> getRequiredKeys() {
        return new ArrayList<>(0);
    }

    public List<String> getOptionalKeys() {
        List<String> result = new ArrayList<>();
        result.add("Timeout");
        result.add("Retries");
        result.add("ChannelMap");
        result.add("SecurityLevel");
        result.add("EchoCancelling");
        result.add("IEC1107Compatible");
        result.add("ForcedDelay");
        result.add("RequestHeader");
        result.add("DataReadout");
        result.add("ExtendedLogging");
        result.add("Software7E1");
        result.add("DateFormat");
        result.add("ReadUserLogBook");
        result.add("ReconnectAfterR6Read");
        return result;
    }

    @Override
    public List<PropertySpec> getRequiredProperties() {
        return PropertySpecFactory.toPropertySpecs(getRequiredKeys());
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        return PropertySpecFactory.toPropertySpecs(getOptionalKeys());
    }
}