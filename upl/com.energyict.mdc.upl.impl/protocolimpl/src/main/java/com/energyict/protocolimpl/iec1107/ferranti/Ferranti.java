/*
 * Ferranti.java
 *
 * Created on 04 mei 2004, 10:00
 */

package com.energyict.protocolimpl.iec1107.ferranti;

import com.energyict.mdc.upl.UnsupportedException;
import com.energyict.mdc.upl.nls.TranslationKey;
import com.energyict.mdc.upl.properties.InvalidPropertyException;
import com.energyict.mdc.upl.properties.MissingPropertyException;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecBuilderWizard;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.TypedProperties;

import com.energyict.cbo.Quantity;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.protocol.MeterExceptionInfo;
import com.energyict.protocol.ProfileData;
import com.energyict.protocolimpl.base.PluggableMeterProtocol;
import com.energyict.protocolimpl.base.ProtocolChannelMap;
import com.energyict.protocolimpl.iec1107.ChannelMap;
import com.energyict.protocolimpl.iec1107.FlagIEC1107Connection;
import com.energyict.protocolimpl.iec1107.FlagIEC1107ConnectionException;
import com.energyict.protocolimpl.iec1107.ProtocolLink;
import com.energyict.protocolimpl.nls.PropertyTranslationKeys;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.function.Supplier;
import java.util.logging.Logger;

import static com.energyict.mdc.upl.MeterProtocol.Property.*;

/**
 * @author Koenraad Vanderschaeve
 *         <p/>
 *         <B>Description :</B><BR>
 *         Class that implements the Ferranti meter protocol.
 *         <BR>
 *         <B>@beginchanges</B><BR>
 *         KV|04052004|Initial version
 *         KV|30032005|Handle StringOutOfBoundException in IEC1107 connection layer
 *         KV|06092005|VDEW changed to do channel mapping!
 * @version 1.0
 * @endchanges
 */
@Deprecated
public class Ferranti extends PluggableMeterProtocol implements ProtocolLink, MeterExceptionInfo {

    private static final byte DEBUG = 0;

    private static final int FERRANTI_NR_OF_PROFILE_CHANNELS = 3;
    private static final int FERRANTI_NR_OF_METERREADINGS = 4;
    private static final String[] FERRANTI_METERREADINGS = {"7-0:23.0.0*101", "7-0:23.2.0*101", "7-0:97.97.0*101", "7-0:0.1.2*101"};
    private final PropertySpecService propertySpecService;

    private String strID;
    private String strPassword;
    private int iIEC1107TimeoutProperty;
    private int iProtocolRetriesProperty;
    private int iRoundtripCorrection;
    private int iSecurityLevel;
    private String nodeId;
    private int iEchoCancelling;
    private int iIEC1107Compatible;
    private int iProfileInterval;
    private ChannelMap channelMap;

    private TimeZone timeZone;
    private Logger logger;

    private FlagIEC1107Connection flagIEC1107Connection = null;
    private FerrantiRegistry ferrantiRegistry = null;
    private FerrantiProfile ferrantiProfile = null;

    private byte[] dataReadout = null;

    private boolean software7E1;

    public Ferranti(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Override
    public ProfileData getProfileData(boolean includeEvents) throws IOException {
        Calendar calendar = ProtocolUtils.getCalendar(timeZone);
        calendar.add(Calendar.YEAR, -10);
        return doGetProfileData(calendar.getTime());
    }

    @Override
    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        return doGetProfileData(lastReading);
    }

    @Override
    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException {
        Calendar fromCalendar = ProtocolUtils.getCleanCalendar(timeZone);
        fromCalendar.setTime(from);
        Calendar toCalendar = ProtocolUtils.getCleanCalendar(timeZone);
        toCalendar.setTime(to);
        return getFerrantiProfile().getProfileData(fromCalendar,
                toCalendar,
                getNumberOfChannels());
    }

    private ProfileData doGetProfileData(Date lastReading) throws IOException {
        Calendar from = ProtocolUtils.getCleanCalendar(timeZone);
        from.setTime(lastReading);
        return getFerrantiProfile().getProfileData(from,
                ProtocolUtils.getCalendar(timeZone),
                getNumberOfChannels());
    }

    // Only for debugging
    public ProfileData getProfileData(Calendar from, Calendar to) throws IOException {
        return getFerrantiProfile().getProfileData(from,
                to,
                getNumberOfChannels());
    }

    @Override
    public Quantity getMeterReading(String name) throws IOException {
        try {
            return (Quantity) getFerrantiRegistry().getRegister(name);
        } catch (ClassCastException e) {
            throw new IOException("Ferranti, getMeterReading, register " + name + " is not type Quantity");
        }
    }

    @Override
    public Quantity getMeterReading(int channelId) throws IOException {
        try {
            if (channelId >= FERRANTI_NR_OF_METERREADINGS) {
                throw new IOException("Ferranti, getMeterReading, invalid channelId, " + channelId);
            }
            return (Quantity) getFerrantiRegistry().getRegister(FERRANTI_METERREADINGS[channelId]);
        } catch (ClassCastException e) {
            throw new IOException("Ferranti, getMeterReading, register " + FERRANTI_METERREADINGS[channelId] + " (" + channelId + ") is not type Quantity");
        }
    }

    @Override
    public void setTime() throws IOException {
        Calendar calendar = ProtocolUtils.getCalendar(timeZone);
        calendar.add(Calendar.MILLISECOND, iRoundtripCorrection);
        Date date = calendar.getTime();
        getFerrantiRegistry().setRegister("Time in the device", date);
    }

    @Override
    public Date getTime() throws IOException {
        Date date = (Date) getFerrantiRegistry().getRegister("Time in the device");
        return new Date(date.getTime() - iRoundtripCorrection);
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        return Arrays.asList(
                this.stringSpec(ADDRESS.getName(), PropertyTranslationKeys.IEC1107_ADDRESS),
                this.integerSpec(TIMEOUT.getName(), PropertyTranslationKeys.IEC1107_TIMEOUT),
                this.integerSpec(RETRIES.getName(), PropertyTranslationKeys.IEC1107_RETRIES),
                this.integerSpec(ROUNDTRIPCORRECTION.getName(), PropertyTranslationKeys.IEC1107_ROUNDTRIPCORRECTION),
                this.stringSpec(NODEID.getName(), PropertyTranslationKeys.IEC1107_NODEID),
                this.integerSpec("EchoCancelling", PropertyTranslationKeys.IEC1107_ECHOCANCELLING),
                this.integerSpec("IEC1107Compatible", PropertyTranslationKeys.IEC1107_COMPATIBLE),
                this.integerSpec(PROFILEINTERVAL.getName(), PropertyTranslationKeys.IEC1107_PROFILEINTERVAL),
                this.stringSpec("ChannelMap", PropertyTranslationKeys.IEC1107_CHANNEL_MAP),
                this.stringSpec(SOFTWARE7E1.getName(), PropertyTranslationKeys.IEC1107_SOFTWARE_7E1),
                this.integerSpec("ServerLowerMacAddress", PropertyTranslationKeys.IEC1107_SERVER_LOWER_MAC_ADDRESS),
                this.integerSpec("ServerUpperMacAddress", PropertyTranslationKeys.IEC1107_SERVER_UPPER_MAC_ADDRESS),
                this.integerSpec("ForceDelay", PropertyTranslationKeys.IEC1107_FORCEDELAY),
                this.integerSpec("AddressingMode", PropertyTranslationKeys.IEC1107_ADDRESSING_MODE),
                this.integerSpec("RequestTimeZone", PropertyTranslationKeys.IEC1107_REQUEST_TIME_ZONE));
    }

    private <T> PropertySpec spec(String name, TranslationKey translationKey, Supplier<PropertySpecBuilderWizard.NlsOptions<T>> optionsSupplier) {
        return UPLPropertySpecFactory.specBuilder(name, false, translationKey, optionsSupplier).finish();
    }

    private PropertySpec stringSpec(String name, TranslationKey translationKey) {
        return this.spec(name, translationKey, this.propertySpecService::stringSpec);
    }

    private PropertySpec integerSpec(String name, TranslationKey translationKey) {
        return this.spec(name, translationKey, this.propertySpecService::integerSpec);
    }

    @Override
    public void setUPLProperties(TypedProperties properties) throws MissingPropertyException, InvalidPropertyException {
        strID = properties.getTypedProperty(ADDRESS.getName());
        strPassword = properties.getTypedProperty(PASSWORD.getName());
        iIEC1107TimeoutProperty = properties.getTypedProperty(TIMEOUT.getName(), 20000);
        iProtocolRetriesProperty = properties.getTypedProperty(RETRIES.getName(), 5);
        iRoundtripCorrection = properties.getTypedProperty(ROUNDTRIPCORRECTION.getName(), 0);
        iSecurityLevel = properties.getTypedProperty(SECURITYLEVEL.getName(), 1);
        nodeId = properties.getTypedProperty(NODEID.getName(), "");
        iEchoCancelling = properties.getTypedProperty("EchoCancelling", 0);
        iIEC1107Compatible = properties.getTypedProperty("IEC1107Compatible", 1);
        iProfileInterval = properties.getTypedProperty(PROFILEINTERVAL.getName(), 3600);
        channelMap = properties.getTypedProperty("ChannelMap", new ChannelMap("0"));
        this.software7E1 = !"0".equalsIgnoreCase(properties.getTypedProperty(SOFTWARE7E1.getName(), "0"));
    }

    @Override
    public String getRegister(String name) throws IOException {
        return ProtocolUtils.obj2String(getFerrantiRegistry().getRegister(name));
    }

    @Override
    public void setRegister(String name, String value) throws IOException {
        getFerrantiRegistry().setRegister(name, value);
    }

    @Override
    public void initializeDevice() throws UnsupportedException {
        throw new UnsupportedException();
    }

    @Override
    public String getProtocolDescription() {
        return "Ferranti";
    }

    @Override
    public String getProtocolVersion() {
        return "$Date: 2014-06-20 14:07:47 +0200 (Fri, 20 Jun 2014) $";
    }

    @Override
    public String getFirmwareVersion() {
        return "Unknown";
    }

    @Override
    public void init(InputStream inputStream, OutputStream outputStream, TimeZone timeZone, Logger logger) {
        this.timeZone = timeZone;
        this.logger = logger;

        try {
            flagIEC1107Connection = new FlagIEC1107Connection(inputStream, outputStream, iIEC1107TimeoutProperty, iProtocolRetriesProperty, 0, iEchoCancelling, iIEC1107Compatible, software7E1, logger);
            ferrantiRegistry = new FerrantiRegistry(this, this);
            ferrantiProfile = new FerrantiProfile(this, this, ferrantiRegistry);
        } catch (ConnectionException e) {
            logger.severe("ABBA1500: init(...), " + e.getMessage());
        }

    }

    @Override
    public void connect() throws IOException {
        try {
            dataReadout = flagIEC1107Connection.dataReadout(strID, nodeId);
            flagIEC1107Connection.disconnectMAC();
            flagIEC1107Connection.connectMAC(strID, strPassword, iSecurityLevel, nodeId);
        } catch (FlagIEC1107ConnectionException e) {
            throw new IOException(e.getMessage());
        }
    }

    @Override
    public void disconnect() throws IOException {
        try {
            flagIEC1107Connection.disconnectMAC();
        } catch (FlagIEC1107ConnectionException e) {
            logger.severe("disconnect() error, " + e.getMessage());
        }
    }

    @Override
    public int getNumberOfChannels() throws IOException {
        return FERRANTI_NR_OF_PROFILE_CHANNELS;
    }

    @Override
    public int getProfileInterval() throws IOException {
        return iProfileInterval;
    }

    private FerrantiRegistry getFerrantiRegistry() {
        return ferrantiRegistry;
    }

    private FerrantiProfile getFerrantiProfile() {
        return ferrantiProfile;
    }

    @Override
    public FlagIEC1107Connection getFlagIEC1107Connection() {
        return flagIEC1107Connection;
    }

    @Override
    public TimeZone getTimeZone() {
        return timeZone;
    }

    @Override
    public boolean isIEC1107Compatible() {
        return (iIEC1107Compatible == 1);
    }

    @Override
    public String getPassword() {
        return strPassword;
    }

    @Override
    public byte[] getDataReadout() {
        return dataReadout;
    }

    @Override
    public ChannelMap getChannelMap() {
        return channelMap;
    }

    @Override
    public ProtocolChannelMap getProtocolChannelMap() {
        return null;
    }

    @Override
    public void release() throws IOException {
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    // KV 17022004 implementation of MeterExceptionInfo
    private static final Map<String, String> EXCEPTION_INFO_MAP = new HashMap<>();

    static {
        EXCEPTION_INFO_MAP.put("#ERR00001", "Unknown OBIS code");
        EXCEPTION_INFO_MAP.put("#ERR00002", "Unknown Read Command");
        EXCEPTION_INFO_MAP.put("#ERR00003", "Unknown Write Command");
        EXCEPTION_INFO_MAP.put("#ERR00004", "Bad VHI Id");
        EXCEPTION_INFO_MAP.put("#ERR00005", "Invalid load profile request");

    }

    @Override
    public String getExceptionInfo(String id) {
        String exceptionInfo = EXCEPTION_INFO_MAP.get(id);
        if (exceptionInfo != null) {
            return id + ", " + exceptionInfo;
        } else {
            return "No meter specific exception info for " + id;
        }
    }

    @Override
    public int getNrOfRetries() {
        return iProtocolRetriesProperty;
    }

    @Override
    public boolean isRequestHeader() {
        return false;
    }

}