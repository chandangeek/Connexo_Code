package com.energyict.protocolimpl.iec1107.emh.nxt4;

import com.energyict.cbo.BusinessException;
import com.energyict.cbo.NestedIOException;
import com.energyict.cbo.Quantity;
import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dialer.connection.HHUSignOn;
import com.energyict.dialer.connection.IEC1107HHUConnection;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.HHUEnabler;
import com.energyict.protocol.InvalidPropertyException;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.MessageProtocol;
import com.energyict.protocol.MessageResult;
import com.energyict.protocol.MeterExceptionInfo;
import com.energyict.protocol.MeterProtocol;
import com.energyict.protocol.MissingPropertyException;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterProtocol;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.UnsupportedException;
import com.energyict.protocol.messaging.Message;
import com.energyict.protocol.messaging.MessageTag;
import com.energyict.protocol.messaging.MessageValue;
import com.energyict.protocolimpl.base.PluggableMeterProtocol;
import com.energyict.protocolimpl.base.ProtocolChannelMap;
import com.energyict.protocolimpl.iec1107.ChannelMap;
import com.energyict.protocolimpl.iec1107.FlagIEC1107Connection;
import com.energyict.protocolimpl.iec1107.FlagIEC1107ConnectionException;
import com.energyict.protocolimpl.iec1107.ProtocolLink;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
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

    private static Map<String, String> exceptionInfoMap = new HashMap<String, String>();

    static {
        exceptionInfoMap.put("ERROR", "Request could not be executed!");
    }

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

    public void disconnect() throws IOException {
        try {
            getFlagIEC1107Connection().disconnectMAC();
        } catch (FlagIEC1107ConnectionException e) {
            //absorb -> trying to close communication
            logger.severe("Failed to disconnect the connection, " + e.getMessage());
        }
    }

    public void initializeDevice() throws IOException {
    }

    public void release() throws IOException {
    }

    public int getNumberOfChannels() throws IOException {
        if (getProperties().isRequestHeader()) {
            return getProfile().getProfileHeader().getNrOfChannels();
        } else {
            return getProtocolChannelMap().getNrOfProtocolChannels();
        }
    }

    public int getProfileInterval() throws IOException {
        if (getProperties().isRequestHeader()) {
            return getProfile().getProfileHeader().getProfileInterval();
        } else {
            return getProperties().getProfileInterval();
        }
    }

    public String getFirmwareVersion() throws IOException {
        return getRegister(NXT4Registry.FIRMWARE_VERSION);
    }

    //********** Clock ********** //
    public Date getTime() throws IOException {
        String dateTimeString = getRegister(NXT4Registry.TIME_DATE);

        try {
            DateTime dateTime = new DateTime(getTimeZone(), getProperties().getDateFormat());
            return dateTime.parseDate(dateTimeString);
        } catch (ParseException e) {
            throw new IOException("Failed to parse the date/time, " + e.getMessage());
        }
    }

    public void setTime() throws IOException {
        DateTime dateTime = new DateTime(getTimeZone(), getProperties().getDateFormat());
        String dateTimeString = dateTime.formatDateTime(new Date());
        setRegister(NXT4Registry.TIME_DATE, dateTimeString);
    }

    //********** Register reading ********** //
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        return getRegisterFactory().readRegister(obisCode);
    }

    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        return getRegisterFactory().translateRegister(obisCode);
    }

    public String getRegister(String name) throws IOException {
        return (String) getRegistry().getRegister(name);
    }

    public void setRegister(String name, String value) throws IOException {
        getRegistry().setRegister(name, value);
    }

    //********** Profile data reading **********//

    public ProfileData getProfileData(boolean includeEvents) throws IOException {
        Calendar calendar = ProtocolUtils.getCalendar(timeZone);
        calendar.add(Calendar.YEAR, -10);
        return getProfileData(calendar.getTime(), new Date(), includeEvents);
    }

    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        return getProfileData(lastReading, new Date(), includeEvents);
    }

    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException {
        return getProfile().getProfileData(from, to, includeEvents);
    }

    public Quantity getMeterReading(int channelId) throws IOException {
        throw new UnsupportedException();
    }

    public Quantity getMeterReading(String name) throws IOException {
        throw new UnsupportedException();
    }

    //********** Device message support **********//
    public void applyMessages(List messageEntries) throws IOException {
        getMessages().applyMessages(messageEntries);
    }

    public MessageResult queryMessage(MessageEntry messageEntry) throws IOException {
        return getMessages().queryMessage(messageEntry);
    }

    public List getMessageCategories() {
        return getMessages().getMessageCategories();
    }

    public String writeMessage(Message msg) {
        return getMessages().writeMessage(msg);
    }

    public String writeTag(MessageTag tag) {
        return getMessages().writeTag(tag);
    }

    public String writeValue(MessageValue value) {
        return getMessages().writeValue(value);
    }

    //********** Configuration support **********//
    public void setProperties(Properties properties) throws InvalidPropertyException, MissingPropertyException {
        getProperties().validateAndSetProperties(properties);
    }

    public List<String> getRequiredKeys() {
        return getProperties().getRequiredKeys();
    }

    @Override
    public List<PropertySpec> getRequiredProperties() {
        return PropertySpecFactory.toPropertySpecs(getRequiredKeys());
    }

    public List<String> getOptionalKeys() {
        return getProperties().getOptionalKeys();
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        return PropertySpecFactory.toPropertySpecs(getOptionalKeys());
    }

    public int getNrOfRetries() {
        return getProperties().getRetries();
    }

    public boolean isRequestHeader() {
        return getProperties().isRequestHeader();
    }

    public String getPassword() {
        return getProperties().getPassword();
    }

    public boolean isIEC1107Compatible() {
        return getProperties().getIEC1107Compatible() == 1;
    }

    public ChannelMap getChannelMap() {
        return null; // Not used, the ProtocolChannelMap is used instead
    }

    public ProtocolChannelMap getProtocolChannelMap() {
        return getProperties().getProtocolChannelMap();
    }

    //********** HHU SignOn **********//
    public void enableHHUSignOn(SerialCommunicationChannel commChannel) throws ConnectionException {
        enableHHUSignOn(commChannel, getProperties().isDataReadout());
    }

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

    //********** Cache mechanism **********//
    public Object getCache() {
        return null; // Cache mechanism not used for this protocol
    }

    public void setCache(Object cacheObject) {
        // Cache mechanism not used for this protocol
    }

    public Object fetchCache(int rtuid) throws SQLException, BusinessException {
        return null; // Cache mechanism not used for this protocol
    }

    public void updateCache(int rtuid, Object cacheObject) throws SQLException, BusinessException {
        // Cache mechanism not used for this protocol
    }

    //********** MeterExceptionInfo **********//
    public String getExceptionInfo(String id) {
        String exceptionInfo = exceptionInfoMap.get(ProtocolUtils.stripBrackets(id));
        if (exceptionInfo != null) {
            return id + ", " + exceptionInfo;
        } else {
            return "No meter specific exception info for " + id;
        }
    }

    public byte[] getHHUDataReadout() {
        return getFlagIEC1107Connection().getHhuSignOn().getDataReadout();
    }

    /**
     * The protocol version date
     */
    public String getProtocolVersion() {
        return "$Date: 2015-04-10 12:16:12 +0200 (Fri, 10 Apr 2015) $";
    }

    public TimeZone getTimeZone() {
        return timeZone;
    }

    public Logger getLogger() {
        return logger;
    }

    public byte[] getDataReadout() {
        return dataReadout;
    }

    public boolean isReconnect() {
        return reconnect;
    }

    public void setReconnect(boolean reconnect) {
        this.reconnect = reconnect;
    }

    public FlagIEC1107Connection getFlagIEC1107Connection() {
        return flagIEC1107Connection;
    }

    public NXT4Properties getProperties() {
        if (this.properties == null) {
            this.properties = new NXT4Properties(this);
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