package com.energyict.protocolimpl.iec1107.emh.nxt4;

import com.energyict.mdc.io.NestedIOException;
import com.energyict.mdc.upl.MeterProtocol;
import com.energyict.mdc.upl.UnsupportedException;
import com.energyict.mdc.upl.messages.legacy.Message;
import com.energyict.mdc.upl.messages.legacy.MessageCategorySpec;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.MessageTag;
import com.energyict.mdc.upl.messages.legacy.MessageValue;
import com.energyict.mdc.upl.properties.InvalidPropertyException;
import com.energyict.mdc.upl.properties.MissingPropertyException;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.TypedProperties;

import com.energyict.cbo.Quantity;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dialer.connection.HHUSignOn;
import com.energyict.dialer.connection.IEC1107HHUConnection;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.HHUEnabler;
import com.energyict.protocol.MessageProtocol;
import com.energyict.protocol.MessageResult;
import com.energyict.protocol.MeterExceptionInfo;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterProtocol;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.base.PluggableMeterProtocol;
import com.energyict.protocolimpl.base.ProtocolChannelMap;
import com.energyict.protocolimpl.iec1107.ChannelMap;
import com.energyict.protocolimpl.iec1107.FlagIEC1107Connection;
import com.energyict.protocolimpl.iec1107.FlagIEC1107ConnectionException;
import com.energyict.protocolimpl.iec1107.ProtocolLink;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.logging.Logger;

/**
 * @author sva
 * @since 4/11/2014 - 13:34
 */
public class NXT4 extends PluggableMeterProtocol implements MeterProtocol, MeterExceptionInfo, HHUEnabler, ProtocolLink, RegisterProtocol, MessageProtocol {

    private TimeZone timeZone;
    private Logger logger;
    private byte[] dataReadout;
    private boolean reconnect;
    private NXT4Properties properties;
    private NXT4Profile profile;
    private NXT4Registry registry;
    private NXT4Messages messages;
    private NXT4RegisterFactory registerFactory;
    private FlagIEC1107Connection flagIEC1107Connection;

    private static final Map<String, String> EXCEPTION_INFO_MAP = new HashMap<>();

    static {
        EXCEPTION_INFO_MAP.put("ERROR", "Request could not be executed!");
    }

    private final PropertySpecService propertySpecService;

    public NXT4(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Override
    public void init(InputStream inputStream, OutputStream outputStream, TimeZone timeZone, Logger logger) throws IOException {
        this.timeZone = timeZone;
        this.logger = logger;
        flagIEC1107Connection = new FlagIEC1107Connection(
                inputStream,
                outputStream,
                getProperties().getIEC1107TimeOut(),
                getProperties().getRetries(),
                getProperties().getForcedDelay(),
                getProperties().getEchoCancelling(),
                getProperties().getIEC1107Compatible(),
                getProperties().useSoftware7E1(),
                logger);
        flagIEC1107Connection.setAddCRLF(true);
    }

    @Override
    public void connect() throws IOException {
        try {
            if ((getFlagIEC1107Connection().getHhuSignOn() == null) && (getProperties().isDataReadout())) {
                dataReadout = flagIEC1107Connection.dataReadout(getProperties().getDeviceId(), getProperties().getNodeAddress());
                flagIEC1107Connection.disconnectMAC();
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new NestedIOException(e);
                }
            }

            flagIEC1107Connection.connectMAC(
                    getProperties().getDeviceId(),
                    getProperties().getPassword(),
                    getProperties().getSecurityLevel(),
                    getProperties().getNodeAddress()
            );

            if ((getFlagIEC1107Connection().getHhuSignOn() != null) && (getProperties().isDataReadout())) {
                dataReadout = getFlagIEC1107Connection().getHhuSignOn().getDataReadout();
            }
        } catch (FlagIEC1107ConnectionException e) {
            throw new IOException(e.getMessage());
        }

        if (getProperties().useExtendedLogging() && ! isReconnect()) {
            printRegisterInfo();
        }
    }

    @Override
    public void disconnect() throws IOException {
        try {
            getFlagIEC1107Connection().disconnectMAC();
        } catch (FlagIEC1107ConnectionException e) {
            //absorb -> trying to close communication
            logger.severe("Failed to disconnect the connection, " + e.getMessage());
        }
    }

    @Override
    public void initializeDevice() throws IOException {
    }

    @Override
    public void release() throws IOException {
    }

    @Override
    public int getNumberOfChannels() throws IOException {
        if (getProperties().isRequestHeader()) {
            return getProfile().getProfileHeader().getNrOfChannels();
        } else {
            return getProtocolChannelMap().getNrOfProtocolChannels();
        }
    }

    @Override
    public int getProfileInterval() throws IOException {
        if (getProperties().isRequestHeader()) {
            return getProfile().getProfileHeader().getProfileInterval();
        } else {
            return getProperties().getProfileInterval();
        }
    }

    @Override
    public String getFirmwareVersion() throws IOException {
        return getRegister(NXT4Registry.FIRMWARE_VERSION);
    }

    @Override
    public Date getTime() throws IOException {
        String dateTimeString = getRegister(NXT4Registry.TIME_DATE);

        try {
            DateTime dateTime = new DateTime(getTimeZone(), getProperties().getDateFormat());
            return dateTime.parseDate(dateTimeString);
        } catch (ParseException e) {
            throw new IOException("Failed to parse the date/time, " + e.getMessage());
        }
    }

    @Override
    public void setTime() throws IOException {
        DateTime dateTime = new DateTime(getTimeZone(), getProperties().getDateFormat());
        String dateTimeString = dateTime.formatDateTime(new Date());
        setRegister(NXT4Registry.TIME_DATE, dateTimeString);
    }

    @Override
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        return getRegisterFactory().readRegister(obisCode);
    }

    @Override
    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        return getRegisterFactory().translateRegister(obisCode);
    }

    @Override
    public String getRegister(String name) throws IOException {
        return (String) getRegistry().getRegister(name);
    }

    @Override
    public void setRegister(String name, String value) throws IOException {
        getRegistry().setRegister(name, value);
    }

    @Override
    public ProfileData getProfileData(boolean includeEvents) throws IOException {
        Calendar calendar = ProtocolUtils.getCalendar(timeZone);
        calendar.add(Calendar.YEAR, -10);
        return getProfileData(calendar.getTime(), new Date(), includeEvents);
    }

    @Override
    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        return getProfileData(lastReading, new Date(), includeEvents);
    }

    @Override
    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException {
        return getProfile().getProfileData(from, to, includeEvents);
    }

    @Override
    public Quantity getMeterReading(int channelId) throws UnsupportedException {
        throw new UnsupportedException();
    }

    @Override
    public Quantity getMeterReading(String name) throws UnsupportedException {
        throw new UnsupportedException();
    }

    @Override
    public void applyMessages(List messageEntries) throws IOException {
        getMessages().applyMessages(messageEntries);
    }

    @Override
    public MessageResult queryMessage(MessageEntry messageEntry) throws IOException {
        return getMessages().queryMessage(messageEntry);
    }

    @Override
    public List<MessageCategorySpec> getMessageCategories() {
        return getMessages().getMessageCategories();
    }

    @Override
    public String writeMessage(Message msg) {
        return getMessages().writeMessage(msg);
    }

    @Override
    public String writeTag(MessageTag tag) {
        return getMessages().writeTag(tag);
    }

    @Override
    public String writeValue(MessageValue value) {
        return getMessages().writeValue(value);
    }

    @Override
    public void setUPLProperties(TypedProperties properties) throws InvalidPropertyException, MissingPropertyException {
        getProperties().setProperties(properties.toStringProperties());
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        return this.getProperties().getPropertySpecs();
    }

    @Override
    public int getNrOfRetries() {
        return getProperties().getRetries();
    }

    @Override
    public boolean isRequestHeader() {
        return getProperties().isRequestHeader();
    }

    @Override
    public String getPassword() {
        return getProperties().getPassword();
    }

    @Override
    public boolean isIEC1107Compatible() {
        return getProperties().getIEC1107Compatible() == 1;
    }

    @Override
    public ChannelMap getChannelMap() {
        return null; // Not used, the ProtocolChannelMap is used instead
    }

    @Override
    public ProtocolChannelMap getProtocolChannelMap() {
        return getProperties().getProtocolChannelMap();
    }

    @Override
    public void enableHHUSignOn(SerialCommunicationChannel commChannel) throws ConnectionException {
        enableHHUSignOn(commChannel, getProperties().isDataReadout());
    }

    @Override
    public void enableHHUSignOn(SerialCommunicationChannel commChannel, boolean enableDataReadout) throws ConnectionException {
        HHUSignOn hhuSignOn = new IEC1107HHUConnection(
                commChannel,
                getProperties().getIEC1107TimeOut(),
                getProperties().getRetries(),
                getProperties().getForcedDelay(),
                getProperties().getEchoCancelling()
        );
        hhuSignOn.setMode(HHUSignOn.MODE_PROGRAMMING);
        hhuSignOn.setProtocol(HHUSignOn.PROTOCOL_NORMAL);
        hhuSignOn.enableDataReadout(enableDataReadout);
        getFlagIEC1107Connection().setHHUSignOn(hhuSignOn);
    }

    //********** Extended logging **********//
    private void printRegisterInfo() {
        StringBuilder strBuilder = new StringBuilder();
        if (getProperties().isDataReadout()) {
            strBuilder.append("******************* ExtendedLogging *******************\n");
            strBuilder.append(new String(getDataReadout()));
        } else {
            strBuilder.append("******************* ExtendedLogging *******************\n");
            strBuilder.append("It is not possible to retrieve a list with all registers in the meter. Consult the configuration of the meter.");
            strBuilder.append("\n");
        }
        logger.info(strBuilder.toString());
    }

    @Override
    public String getExceptionInfo(String id) {
        String exceptionInfo = EXCEPTION_INFO_MAP.get(ProtocolUtils.stripBrackets(id));
        if (exceptionInfo != null) {
            return id + ", " + exceptionInfo;
        } else {
            return "No meter specific exception info for " + id;
        }
    }

    @Override
    public byte[] getHHUDataReadout() {
        return getFlagIEC1107Connection().getHhuSignOn().getDataReadout();
    }

    @Override
    public String getProtocolVersion() {
        return "$Date: 2015-04-10 12:16:12 +0200 (Fri, 10 Apr 2015) $";
    }

    @Override
    public TimeZone getTimeZone() {
        return timeZone;
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    public byte[] getDataReadout() {
        return dataReadout;
    }

    public boolean isReconnect() {
        return reconnect;
    }

    public void setReconnect(boolean reconnect) {
        this.reconnect = reconnect;
    }

    @Override
    public FlagIEC1107Connection getFlagIEC1107Connection() {
        return flagIEC1107Connection;
    }

    public NXT4Properties getProperties() {
        if (this.properties == null) {
            this.properties = new NXT4Properties(this, this.propertySpecService);
        }
        return properties;
    }

    public NXT4Registry getRegistry() {
        if (this.registry == null) {
            this.registry = new NXT4Registry(this);
        }
        return registry;
    }

    public NXT4Profile getProfile() {
        if (this.profile == null) {
            this.profile = new NXT4Profile(this, getRegistry());
        }
        return this.profile;
    }

    public NXT4Messages getMessages() {
        if (this.messages == null) {
            this.messages = new NXT4Messages(this);
        }
        return this.messages;
    }

    public NXT4RegisterFactory getRegisterFactory() {
        if (this.registerFactory == null) {
            this.registerFactory = new NXT4RegisterFactory(this);
        }
        return this.registerFactory;
    }
}