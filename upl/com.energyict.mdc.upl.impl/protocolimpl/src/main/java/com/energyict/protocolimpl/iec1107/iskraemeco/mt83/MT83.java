package com.energyict.protocolimpl.iec1107.iskraemeco.mt83;

import com.energyict.cbo.Quantity;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dialer.connection.HHUSignOn;
import com.energyict.dialer.connections.IEC1107HHUConnection;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.mdc.upl.UnsupportedException;
import com.energyict.mdc.upl.io.NestedIOException;
import com.energyict.mdc.upl.messages.legacy.Message;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.MessageTag;
import com.energyict.mdc.upl.messages.legacy.MessageValue;
import com.energyict.mdc.upl.nls.TranslationKey;
import com.energyict.mdc.upl.properties.InvalidPropertyException;
import com.energyict.mdc.upl.properties.MissingPropertyException;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecBuilder;
import com.energyict.mdc.upl.properties.PropertySpecBuilderWizard;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.TypedProperties;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.HHUEnabler;
import com.energyict.protocol.MessageProtocol;
import com.energyict.protocol.MessageResult;
import com.energyict.protocol.MeterExceptionInfo;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterProtocol;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.support.SerialNumberSupport;
import com.energyict.protocolimpl.base.DataDumpParser;
import com.energyict.protocolimpl.base.PluggableMeterProtocol;
import com.energyict.protocolimpl.base.ProtocolChannelMap;
import com.energyict.protocolimpl.errorhandling.ProtocolIOExceptionHandler;
import com.energyict.protocolimpl.iec1107.ChannelMap;
import com.energyict.protocolimpl.iec1107.FlagIEC1107Connection;
import com.energyict.protocolimpl.iec1107.FlagIEC1107ConnectionException;
import com.energyict.protocolimpl.iec1107.ProtocolLink;
import com.energyict.protocolimpl.iec1107.iskraemeco.mt83.registerconfig.MT83RegisterConfig;
import com.energyict.protocolimpl.iec1107.iskraemeco.mt83.registerconfig.MT83Registry;
import com.energyict.protocolimpl.nls.PropertyTranslationKeys;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;
import com.energyict.protocolimpl.utils.ProtocolUtils;
import com.google.common.collect.Range;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.function.Supplier;
import java.util.logging.Logger;

import static com.energyict.mdc.upl.MeterProtocol.Property.ADDRESS;
import static com.energyict.mdc.upl.MeterProtocol.Property.NODEID;
import static com.energyict.mdc.upl.MeterProtocol.Property.PASSWORD;
import static com.energyict.mdc.upl.MeterProtocol.Property.PROFILEINTERVAL;
import static com.energyict.mdc.upl.MeterProtocol.Property.RETRIES;
import static com.energyict.mdc.upl.MeterProtocol.Property.ROUNDTRIPCORRECTION;
import static com.energyict.mdc.upl.MeterProtocol.Property.SECURITYLEVEL;
import static com.energyict.mdc.upl.MeterProtocol.Property.SERIALNUMBER;
import static com.energyict.mdc.upl.MeterProtocol.Property.TIMEOUT;

/**
 * @author jme
 *         <p/>
 *         <B>Description :</B><BR>
 *         Class that implements the Iskra MT83 meter IEC1107 protocol.
 *         <BR>
 *         <p/>
 *         Changes:
 *         JME	|07042009|	Added support for more registers
 *         JME	|14042009|	Removed channelmap property. The protocol now reads this data from the meter.
 *         JME	|20042009|	Fixed nullpointer exception when there is no profile data.
 */
public class MT83 extends PluggableMeterProtocol implements ProtocolLink, HHUEnabler, MeterExceptionInfo, RegisterProtocol, MessageProtocol,SerialNumberSupport {

    private static final byte DEBUG = 0;

    private static final int LOADPROFILES_FIRST = 1;
    private static final int LOADPROFILES_LAST = 2;
    private final PropertySpecService propertySpecService;

    private String strID;
    private String strPassword;
    private String serialNumber;
    private int iIEC1107TimeoutProperty;
    private int iProtocolRetriesProperty;
    private int iRoundtripCorrection;
    private int iSecurityLevel;
    private String nodeId;
    private int iEchoCancelling;
    private int iIEC1107Compatible;
    private int iProfileInterval;
    private ProtocolChannelMap channelMap = null;
    private int extendedLogging;

    private TimeZone timeZone;
    private static Logger logger;

    private int readCurrentDay;
    private int loadProfileNumber;

    private FlagIEC1107Connection flagIEC1107Connection = null;
    private MT83Registry mt83Registry = null;
    private MT83Profile mt83Profile = null;
    private MT83RegisterConfig mt83RegisterConfig = new MT83RegisterConfig(); // we should use an infotype property to determine the registerset
    private MT83MeterMessages meterMessages = new MT83MeterMessages(this);

    private byte[] dataReadout = null;
    private boolean software7E1;
    private int dataReadoutRequest;

    public MT83(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Override
    public ProfileData getProfileData(boolean includeEvents) throws IOException {
        Calendar fromCalendar = ProtocolUtils.getCalendar(timeZone);
        fromCalendar.add(Calendar.YEAR, -10);
        return doGetProfileData(fromCalendar, ProtocolUtils.getCalendar(timeZone), includeEvents);
    }

    @Override
    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        Calendar fromCalendar = ProtocolUtils.getCleanCalendar(timeZone);
        fromCalendar.setTime(lastReading);
        return doGetProfileData(fromCalendar, ProtocolUtils.getCalendar(timeZone), includeEvents);
    }

    @Override
    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException {
        Calendar fromCalendar = ProtocolUtils.getCleanCalendar(timeZone);
        fromCalendar.setTime(from);
        Calendar toCalendar = ProtocolUtils.getCleanCalendar(timeZone);
        toCalendar.setTime(to);
        return doGetProfileData(fromCalendar, toCalendar, includeEvents);
    }

    private ProfileData doGetProfileData(Calendar fromCalendar, Calendar toCalendar, boolean includeEvents) throws IOException {
        ProfileData mt83profile = getMT83Profile().getProfileData(fromCalendar,
                toCalendar, getNumberOfChannels(), loadProfileNumber, includeEvents,
                isReadCurrentDay());

        mt83profile.applyEvents(getProfileInterval() / 60);

        return mt83profile;
    }

//    // Only for debugging
//    public ProfileData getProfileData(Calendar from,Calendar to) throws IOException {
//        return getMT83Profile().getProfileData(from,
//        to,
//        getNumberOfChannels(),
//        1,
//        false, isReadCurrentDay());
//    }

    @Override
    public Quantity getMeterReading(String name) throws IOException {
        throw new UnsupportedException();
    }

    @Override
    public Quantity getMeterReading(int channelId) throws IOException {
        throw new UnsupportedException();
    }

    @Override
    public void setTime() throws IOException {
        Calendar calendar = ProtocolUtils.getCalendar(timeZone);
        calendar.add(Calendar.MILLISECOND, iRoundtripCorrection);
        Date date = calendar.getTime();
        getMT83Registry().setRegister(MT83Registry.TIME_AND_DATE_READWRITE, date);
    }

    @Override
    public Date getTime() throws IOException {
        Date date = (Date) getMT83Registry().getRegister(MT83Registry.TIME_AND_DATE_READONLY);
        sendDebug("getTime() result: METER: " + date.toString() + " METER-ROUNDTRIP: " + new Date(date.getTime() - iRoundtripCorrection).toString(), DEBUG);
        return new Date(date.getTime() - iRoundtripCorrection);
    }

    public byte getLastProtocolState() {
        return -1;
    }

    @Override
    public String getSerialNumber() {
        try {
            return (String) getMT83Registry().getRegister(MT83Registry.SERIAL);
        } catch (IOException e) {
            throw ProtocolIOExceptionHandler.handle(e, getNrOfRetries() + 1);
        }
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        return Arrays.asList(
                this.stringSpec(ADDRESS.getName(), PropertyTranslationKeys.IEC1107_ADDRESS),
                this.stringSpec(PASSWORD.getName(), PropertyTranslationKeys.IEC1107_PASSWORD),
                this.stringSpec(SERIALNUMBER.getName(), PropertyTranslationKeys.IEC1107_SERIALNUMBER),
                this.integerSpec(TIMEOUT.getName(), PropertyTranslationKeys.IEC1107_TIMEOUT),
                this.integerSpec(RETRIES.getName(), PropertyTranslationKeys.IEC1107_RETRIES),
                this.integerSpec(ROUNDTRIPCORRECTION.getName(), PropertyTranslationKeys.IEC1107_ROUNDTRIPCORRECTION),
                this.integerSpec(SECURITYLEVEL.getName(), PropertyTranslationKeys.IEC1107_SECURITYLEVEL),
                this.stringSpec(NODEID.getName(), PropertyTranslationKeys.IEC1107_NODEID),
                this.integerSpec("EchoCancelling", PropertyTranslationKeys.IEC1107_ECHOCANCELLING),
                this.integerSpec("IEC1107Compatible", PropertyTranslationKeys.IEC1107_COMPATIBLE),
                this.integerSpec(PROFILEINTERVAL.getName(), PropertyTranslationKeys.IEC1107_PROFILEINTERVAL),
                this.integerSpec("ExtendedLogging", PropertyTranslationKeys.IEC1107_EXTENDED_LOGGING),
                this.integerSpec("ReadCurrentDay", PropertyTranslationKeys.IEC1107_READ_CURRENT_DAY),
                this.integerSpec("LoadProfileNumber", PropertyTranslationKeys.IEC1107_LOADPROFILE_NUMBER, Range.closed(LOADPROFILES_FIRST, LOADPROFILES_LAST)),
                this.stringSpec("Software7E1", PropertyTranslationKeys.IEC1107_SOFTWARE_7E1),
                this.integerSpec("DataReadout", PropertyTranslationKeys.IEC1107_DATAREADOUT));
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

    private PropertySpec integerSpec(String name, TranslationKey translationKey, Range<Integer> validValue) {
        PropertySpecBuilder<Integer> specBuilder = UPLPropertySpecFactory.specBuilder(name, false, translationKey, this.propertySpecService::integerSpec);
        UPLPropertySpecFactory.addIntegerValues(specBuilder, validValue);
        return specBuilder.finish();
    }

    @Override
    public void setUPLProperties(TypedProperties properties) throws MissingPropertyException, InvalidPropertyException {
        try {
            strID = properties.getTypedProperty(com.energyict.mdc.upl.MeterProtocol.Property.ADDRESS.getName());
            strPassword = properties.getTypedProperty(com.energyict.mdc.upl.MeterProtocol.Property.PASSWORD.getName());
            serialNumber = properties.getTypedProperty(com.energyict.mdc.upl.MeterProtocol.Property.SERIALNUMBER.getName());
            iIEC1107TimeoutProperty = Integer.parseInt(properties.getTypedProperty(TIMEOUT.getName(), "20000").trim());
            iProtocolRetriesProperty = Integer.parseInt(properties.getTypedProperty(RETRIES.getName(), "5").trim());
            iRoundtripCorrection = Integer.parseInt(properties.getTypedProperty(ROUNDTRIPCORRECTION.getName(), "0").trim());
            iSecurityLevel = Integer.parseInt(properties.getTypedProperty(SECURITYLEVEL.getName(), "1").trim());
            nodeId = properties.getTypedProperty(com.energyict.mdc.upl.MeterProtocol.Property.NODEID.getName(), "");
            iEchoCancelling = Integer.parseInt(properties.getTypedProperty("EchoCancelling", "0").trim());
            iIEC1107Compatible = Integer.parseInt(properties.getTypedProperty("IEC1107Compatible", "1").trim());
            iProfileInterval = Integer.parseInt(properties.getTypedProperty(PROFILEINTERVAL.getName(), "900").trim());
            extendedLogging = Integer.parseInt(properties.getTypedProperty("ExtendedLogging", "0").trim());
            readCurrentDay = Integer.parseInt(properties.getTypedProperty("ReadCurrentDay", "0").trim());
            loadProfileNumber = Integer.parseInt(properties.getTypedProperty("LoadProfileNumber", "1").trim());
            this.software7E1 = !"0".equalsIgnoreCase(properties.getTypedProperty("Software7E1", "0"));
            this.dataReadoutRequest = Integer.parseInt(properties.getTypedProperty("DataReadout", "0").trim());
        } catch (NumberFormatException e) {
            throw new InvalidPropertyException(e, "Iskra MT83: validation of properties failed before");
        }
    }

    @Override
    public String getRegister(String name) throws IOException {
        sendDebug("getRegister(): name = " + name, DEBUG);
        return ProtocolUtils.obj2String(getMT83Registry().getRegister(name));
    }

    @Override
    public void setRegister(String name, String value) throws IOException {
        sendDebug("setRegister(): name = " + name + " value = " + value, DEBUG);
        getMT83Registry().setRegister(name, value);
    }

    @Override
    public void initializeDevice() throws IOException {
        throw new UnsupportedException();
    }

    @Override
    public String getProtocolVersion() {
        return "$Date: 2015-11-26 15:26:00 +0200 (Thu, 26 Nov 2015)$";
    }

    @Override
    public String getFirmwareVersion() throws IOException {
        try {
            String fwversion = "";
            fwversion += "Version: " + getMT83Registry().getRegister(MT83Registry.SOFTWARE_REVISION) + " - ";
            fwversion += "Device date: " + getMT83Registry().getRegister(MT83Registry.SOFTWARE_DATE) + " - ";
            fwversion += "Device Type: " + getMT83Registry().getRegister(MT83Registry.DEVICE_TYPE);

            return fwversion;
        } catch (IOException e) {
            throw new IOException("MT83, getFirmwareVersion, " + e.getMessage());
        }
    }

    @Override
    public void init(InputStream inputStream, OutputStream outputStream, TimeZone timeZone, Logger logger) {
        this.timeZone = timeZone;
        MT83.logger = logger;
        try {
            flagIEC1107Connection = new FlagIEC1107Connection(inputStream, outputStream, iIEC1107TimeoutProperty, iProtocolRetriesProperty, 0, iEchoCancelling, iIEC1107Compatible, software7E1, logger);
            flagIEC1107Connection.setErrorSignature("ER");
            mt83Registry = new MT83Registry(this, this);
            mt83Profile = new MT83Profile(this, this, mt83Registry);
        } catch (ConnectionException e) {
            logger.severe("MT83: init(...), " + e.getMessage());
        }
    }

    @Override
    public void connect() throws IOException {
        try {
            if (getFlagIEC1107Connection().getHhuSignOn() == null) {
                dataReadout = flagIEC1107Connection.dataReadout(strID, nodeId);
                flagIEC1107Connection.disconnectMAC();
            }

            flagIEC1107Connection.connectMAC(strID, strPassword, iSecurityLevel, nodeId);

            if (getFlagIEC1107Connection().getHhuSignOn() != null) {
                dataReadout = getFlagIEC1107Connection().getHhuSignOn().getDataReadout();
            }
        } catch (FlagIEC1107ConnectionException e) {
            throw new IOException(e.getMessage());
        }

        if (extendedLogging >= 1) {
            logger.info(getRegistersInfo(extendedLogging));
        }
    }

    protected String getRegistersInfo(int extendedLogging) throws IOException {
        return mt83RegisterConfig.getRegisterInfo();
    }

    @Override
    public void disconnect() throws NestedIOException {
        try {
            flagIEC1107Connection.disconnectMAC();
        } catch (FlagIEC1107ConnectionException e) {
            logger.severe("disconnect() error, " + e.getMessage());
        }
    }

    @Override
    public int getNumberOfChannels() throws IOException {
        return getProtocolChannelMap().getNrOfProtocolChannels();
    }

    @Override
    public int getProfileInterval() throws IOException {
        Object obj = getMT83Registry().getRegister(MT83Registry.PROFILE_INTERVAL);
        if (obj == null) {
            return iProfileInterval;
        } else {
            return ProtocolUtils.obj2int(obj) * 60;
        }
    }

    private MT83Registry getMT83Registry() {
        return mt83Registry;
    }

    private MT83Profile getMT83Profile() {
        return mt83Profile;
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
    public ProtocolChannelMap getProtocolChannelMap() {
        if (channelMap == null) {
            try {
                channelMap = getMT83Profile().buildChannelMap(getProfileInterval(), loadProfileNumber);
            } catch (Exception e) {
                try {
                    channelMap = new ProtocolChannelMap("");
                } catch (InvalidPropertyException e1) {
                }
                e.printStackTrace();
            }
        }
        return channelMap;
    }

    @Override
    public void enableHHUSignOn(SerialCommunicationChannel commChannel) throws ConnectionException {
        enableHHUSignOn(commChannel, true);
    }

    @Override
    public void enableHHUSignOn(SerialCommunicationChannel commChannel, boolean datareadout) throws ConnectionException {
        HHUSignOn hhuSignOn =
                new IEC1107HHUConnection(commChannel, iIEC1107TimeoutProperty, iProtocolRetriesProperty, 300, iEchoCancelling);
        hhuSignOn.setMode(HHUSignOn.MODE_PROGRAMMING);
        hhuSignOn.setProtocol(HHUSignOn.PROTOCOL_NORMAL);
        hhuSignOn.enableDataReadout(datareadout);
        getFlagIEC1107Connection().setHHUSignOn(hhuSignOn);
    }

    @Override
    public byte[] getHHUDataReadout() {
        setDataReadout(getFlagIEC1107Connection().getHhuSignOn().getDataReadout());
        return getDataReadout();
    }

    public void setDataReadout(byte[] dataReadout) {
        this.dataReadout = dataReadout;
    }

    @Override
    public void release() throws IOException {
    }

    public String getExceptionInfo(String id) {
        String exceptionInfo = (String) MT83CodeMapper.exceptionInfoMap.get(id);
        if (exceptionInfo != null) {
            exceptionInfo = id + ", " + exceptionInfo;
        } else {
            exceptionInfo = "No meter specific exception info for " + id;
        }
        return exceptionInfo;
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        return new RegisterInfo(this.mt83RegisterConfig.getRegisterDescription(obisCode));
    }

    @Override
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        RegisterValue regvalue;
        sendDebug("readRegister() obiscode = " + obisCode.toString(), DEBUG);
        MT83ObisCodeMapper ocm = new MT83ObisCodeMapper(getMT83Registry(), getTimeZone(), mt83RegisterConfig, isDataReadout());

        try {
            ocm.setDataDumpParser(new DataDumpParser(getDataReadout(), "yymmddHHMISS"));
            regvalue = ocm.getRegisterValue(obisCode);
        } catch (IOException e) {
            sendDebug(e.getMessage(), DEBUG);
            throw e;
        }
        return regvalue;
    }

    @Override
    public int getNrOfRetries() {
        return iProtocolRetriesProperty;
    }

    @Override
    public boolean isRequestHeader() {
        return false;
    }

    public boolean isReadCurrentDay() {
        return readCurrentDay == 1;
    }

    @Override
    public ChannelMap getChannelMap() {
        return null;
    }

    public static void sendDebug(String message, int debuglvl) {
        String actualMessage = " ##### DEBUG [" + new Date().toString() + "] ######## > " + message;
        if ((debuglvl > 0) && (DEBUG > 0) && (logger != null)) {
            System.out.println(actualMessage);
            logger.info(actualMessage);
        }
    }

    protected void setConnection(final FlagIEC1107Connection connection) {
        this.flagIEC1107Connection = connection;
    }

    /**
     * Execute a billing reset on the device. After receiving the 'Demand Reset'
     * command the meter executes a demand reset by doing a snap shot of all
     * energy and demand registers.
     *
     * @throws java.io.IOException
     */
    public void resetDemand() throws IOException {
        mt83Registry.setRegister(MT83Registry.BILLING_RESET_COMMAND, "");
    }

    /**
     * Provides the full list of outstanding messages to the protocol.
     * If for any reason certain messages have to be grouped before they are sent to a device, then this is the place to do it.
     * At a later timestamp the framework will query each {@link MessageEntry} (see {@link #queryMessage(MessageEntry)}) to actually
     * perform the message.
     *
     * @param messageEntries a list of {@link MessageEntry}s
     * @throws java.io.IOException if a logical error occurs
     */
    @Override
    public void applyMessages(final List messageEntries) throws IOException {
        this.meterMessages.applyMessages(messageEntries);
    }

    /**
     * Indicates that each message has to be executed by the protocol.
     *
     * @param messageEntry a definition of which message needs to be sent
     * @return a state of the message which was just sent
     * @throws java.io.IOException if a logical error occurs
     */
    @Override
    public MessageResult queryMessage(final MessageEntry messageEntry) throws IOException {
        return this.meterMessages.queryMessage(messageEntry);
    }

    @Override
    public List getMessageCategories() {
        return this.meterMessages.getMessageCategories();
    }

    @Override
    public String writeMessage(final Message msg) {
        return this.meterMessages.writeMessage(msg);
    }

    @Override
    public String writeTag(final MessageTag tag) {
        return this.meterMessages.writeTag(tag);
    }

    @Override
    public String writeValue(final MessageValue value) {
        return this.meterMessages.writeValue(value);
    }

    public boolean isDataReadout() {
        return (this.dataReadoutRequest == 1);
    }

}