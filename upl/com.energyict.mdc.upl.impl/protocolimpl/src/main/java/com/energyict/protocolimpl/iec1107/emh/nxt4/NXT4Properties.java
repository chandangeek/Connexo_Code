package com.energyict.protocolimpl.iec1107.emh.nxt4;

import com.energyict.mdc.upl.properties.InvalidPropertyException;

import com.energyict.protocolimpl.base.ProtocolChannelMap;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @author sva
 * @since 4/11/2014 - 13:45
 */
public class NXT4Properties {

    private final NXT4 meterProtocol;
    private Properties protocolProperties;

    public NXT4Properties(NXT4 meterProtocol) {
        this.meterProtocol = meterProtocol;
    }

    public void setProperties(Properties properties) {
        this.protocolProperties = properties;
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

    List<com.energyict.mdc.upl.properties.PropertySpec> getPropertySpecs() {
        List<com.energyict.mdc.upl.properties.PropertySpec> specs = new ArrayList<>();
        this.getIntegerPropertyNames()
                .stream()
                .map(name -> UPLPropertySpecFactory.integral(name, false))
                .forEach(specs::add);
        specs.add(ProtocolChannelMap.propertySpec("ChannelMap", false));
        return specs;
    }

    private List<String> getIntegerPropertyNames() {
        List<String> result = new ArrayList<>();
        result.add("Timeout");
        result.add("Retries");
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

}