package com.energyict.protocolimpl.iec1107.emh.nxt4;

import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.nls.TranslationKey;
import com.energyict.mdc.upl.properties.InvalidPropertyException;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.protocolimpl.base.ProtocolChannelMap;
import com.energyict.protocolimpl.nls.PropertyTranslationKeys;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;
import com.energyict.protocolimplv2.messages.nls.Thesaurus;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static com.energyict.mdc.upl.MeterProtocol.Property.*;

/**
 * @author sva
 * @since 4/11/2014 - 13:45
 */
public class NXT4Properties {

    private final NXT4 meterProtocol;
    private Properties protocolProperties;
    private final PropertySpecService propertySpecService;
    private final NlsService nlsService;

    public NXT4Properties(NXT4 meterProtocol, PropertySpecService propertySpecService, NlsService nlsService) {
        this.meterProtocol = meterProtocol;
        this.propertySpecService = propertySpecService;
        this.nlsService = nlsService;
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

  /*  List<PropertySpec> getPropertySpecs() {
        List<PropertySpec> specs = new ArrayList<>();
        this.getIntegerPropertyNames()
                .stream()
                .map(this::integerSpec)
                .forEach(specs::add);
        specs.add(ProtocolChannelMap.propertySpec("ChannelMap", false, this.nlsService.getThesaurus(Thesaurus.ID.toString()).getFormat(PropertyTranslationKeys.IEC1107_CHANNEL_MAP).format(), this.nlsService.getThesaurus(Thesaurus.ID.toString()).getFormat(PropertyTranslationKeys.IEC1107_CHANNEL_MAP_DESCRIPTION).format()));
        return specs;
    }*/

    List<PropertySpec> getPropertySpecs() {
        return Arrays.asList(
                this.integerSpec(TIMEOUT.getName(), PropertyTranslationKeys.IEC1107_TIMEOUT),
                this.integerSpec(RETRIES.getName(), PropertyTranslationKeys.IEC1107_RETRIES),
                this.integerSpec(SECURITYLEVEL.getName(), PropertyTranslationKeys.IEC1107_SECURITYLEVEL),
                this.integerSpec("EchoCancelling", PropertyTranslationKeys.IEC1107_ECHOCANCELLING),
                this.integerSpec("IEC1107Compatible", PropertyTranslationKeys.IEC1107_COMPATIBLE),
                this.integerSpec("ForcedDelay", PropertyTranslationKeys.IEC1107_FORCED_DELAY),
                this.integerSpec("RequestHeader", PropertyTranslationKeys.IEC1107_REQUESTHEADER),
                this.integerSpec("DataReadout", PropertyTranslationKeys.IEC1107_DATAREADOUT),
                this.integerSpec("ExtendedLogging", PropertyTranslationKeys.IEC1107_EXTENDED_LOGGING),
                this.integerSpec("Software7E1", PropertyTranslationKeys.IEC1107_SOFTWARE_7E1),
                this.integerSpec("DateFormat", PropertyTranslationKeys.IEC1107_DATE_FORMAT),
                this.integerSpec("ReadUserLogBook", PropertyTranslationKeys.IEC1107_READ_USER_LOGBOOK),
                this.integerSpec("ReconnectAfterR6Read", PropertyTranslationKeys.IEC1107_RECONNECT_AFTER_R6_READ),
                ProtocolChannelMap.propertySpec("ChannelMap", false, this.nlsService.getThesaurus(Thesaurus.ID.toString()).getFormat(PropertyTranslationKeys.IEC1107_CHANNEL_MAP).format(), this.nlsService.getThesaurus(Thesaurus.ID.toString()).getFormat(PropertyTranslationKeys.IEC1107_CHANNEL_MAP_DESCRIPTION).format()));
    }


    private PropertySpec integerSpec(String name, TranslationKey translationKey) {
        return UPLPropertySpecFactory.specBuilder(name, false, translationKey, this.propertySpecService::integerSpec).finish();
    }

}