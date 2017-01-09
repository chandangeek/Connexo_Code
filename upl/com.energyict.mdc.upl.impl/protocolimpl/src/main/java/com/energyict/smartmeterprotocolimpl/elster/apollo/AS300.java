package com.energyict.smartmeterprotocolimpl.elster.apollo;

import com.energyict.mdc.upl.messages.legacy.DateFormatter;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileFinder;
import com.energyict.mdc.upl.messages.legacy.Message;
import com.energyict.mdc.upl.messages.legacy.MessageCategorySpec;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.MessageTag;
import com.energyict.mdc.upl.messages.legacy.MessageValue;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarExtractor;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarFinder;
import com.energyict.mdc.upl.properties.PropertySpec;

import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.protocol.LoadProfileConfiguration;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocol.MessageProtocol;
import com.energyict.protocol.MessageResult;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.Register;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.support.SerialNumberSupport;
import com.energyict.protocolimpl.dlms.common.AbstractSmartDlmsProtocol;
import com.energyict.smartmeterprotocolimpl.common.SimpleMeter;
import com.energyict.smartmeterprotocolimpl.elster.apollo.eventhandling.ApolloEventProfiles;
import com.energyict.smartmeterprotocolimpl.elster.apollo.messaging.AS300MessageExecutor;
import com.energyict.smartmeterprotocolimpl.elster.apollo.messaging.AS300Messaging;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 29-jun-2011
 * Time: 11:32:30
 */
public class AS300 extends AbstractSmartDlmsProtocol implements SimpleMeter, MessageProtocol, SerialNumberSupport {

    protected AS300Properties properties;
    private AS300ObjectFactory objectFactory;
    private RegisterReader registerReader;
    protected AS300LoadProfileBuilder loadProfileBuilder;
    protected AS300Messaging messageProtocol;
    private final TariffCalendarFinder calendarFinder;
    private final TariffCalendarExtractor calendarExtractor;
    private final DeviceMessageFileFinder messageFileFinder;
    private final DeviceMessageFileExtractor messageFileExtractor;
    private final DateFormatter dateFormatter;

    public AS300(TariffCalendarFinder calendarFinder, TariffCalendarExtractor calendarExtractor, DeviceMessageFileFinder messageFileFinder, DeviceMessageFileExtractor messageFileExtractor, DateFormatter dateFormatter) {
        this.calendarFinder = calendarFinder;
        this.calendarExtractor = calendarExtractor;
        this.messageFileFinder = messageFileFinder;
        this.messageFileExtractor = messageFileExtractor;
        this.dateFormatter = dateFormatter;
    }

    protected TariffCalendarFinder getCalendarFinder() {
        return calendarFinder;
    }

    protected TariffCalendarExtractor getCalendarExtractor() {
        return calendarExtractor;
    }

    protected DeviceMessageFileFinder getMessageFileFinder() {
        return messageFileFinder;
    }

    protected DeviceMessageFileExtractor getMessageFileExtractor() {
        return messageFileExtractor;
    }

    protected DateFormatter getDateFormatter() {
        return dateFormatter;
    }

    @Override
    public AS300Properties getProperties() {
        if (properties == null) {
            properties = new AS300Properties();
        }
        return properties;
    }

    public AS300ObjectFactory getObjectFactory() {
        if (objectFactory == null) {
            objectFactory = new AS300ObjectFactory(getDlmsSession());
        }
        return objectFactory;
    }

    private RegisterReader getRegisterReader() {
        if (registerReader == null) {
            registerReader = new RegisterReader(this);
        }
        return registerReader;
    }

    protected AS300LoadProfileBuilder getLoadProfileBuilder() {
        if (loadProfileBuilder == null) {
            loadProfileBuilder = new AS300LoadProfileBuilder(this);
        }
        return loadProfileBuilder;
    }

    @Override
    protected void initAfterConnect() throws ConnectionException {
        if(this.dlmsSession != null){
            // We need to update the correct TimeZone!!
            this.dlmsSession.updateTimeZone(getTimeZone());
        }
    }

    public String getFirmwareVersion() throws IOException {
        if (getProperties().isFirmwareUpdateSession()) {
            getLogger().severe("Using firmware update client. Skipping firmware version readout!");
            return "";
        } else {
            StringBuilder strBuilder = new StringBuilder();
            strBuilder.append(getObjectFactory().getFirmwareVersion().getString());
            strBuilder.append("(");
            try {
                strBuilder.append(new String(getObjectFactory().getActiveFirmwareIdACOR().getAttrbAbstractDataType(-1).getContentByteArray()));
            } catch (IOException e) {
                strBuilder.append("unknown");
            }
            strBuilder.append(")");
            return strBuilder.toString();
        }
    }

    @Override
    public Date getTime() throws IOException {
        if (getProperties().isFirmwareUpdateSession()) {
            getLogger().severe("Using firmware update client. Skipping clock readout!");
            return new Date();
        } else {
            return getObjectFactory().getClock().getDateTime();
        }
    }

    @Override
    public void setTime(Date newMeterTime) throws IOException {
        if (getProperties().isFirmwareUpdateSession()) {
            getLogger().severe("Using firmware update client. Skipping clock set!");
        } else {
            getObjectFactory().getClock().setAXDRDateTimeAttr(new AXDRDateTime(newMeterTime));
        }
    }

    public String getMeterSerialNumber() throws IOException {
        if (getProperties().isFirmwareUpdateSession()) {
            getLogger().severe("Using firmware update client. Skipping serial number check!");
            return getSerialNumber();
        } else {
            return getObjectFactory().getSerialNumber().getString();
        }
    }

    public RegisterInfo translateRegister(Register register) throws IOException {
        return null; //TODO
    }

    /**
     * Overridden because the object list is not used.
     */
    @Override
    public void connect() throws IOException {
        getDlmsSession().connect();
        checkCacheObjects();
        initAfterConnect();
    }

    public List<RegisterValue> readRegisters(List<Register> registers) throws IOException {
        return getRegisterReader().read(registers);
    }

    public List<MeterEvent> getMeterEvents(Date lastLogbookDate) throws IOException {
        ApolloEventProfiles logs = new ApolloEventProfiles(this);
        Calendar cal = Calendar.getInstance(getTimeZone());
        cal.setTime(lastLogbookDate == null ? new Date(0) : lastLogbookDate);
        return logs.getEventLog(cal);
    }

    public List<LoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfilesToRead) throws IOException {
        return getLoadProfileBuilder().fetchLoadProfileConfiguration(loadProfilesToRead);
    }

    public List<ProfileData> getLoadProfileData(List<LoadProfileReader> loadProfiles) throws IOException {
        return getLoadProfileBuilder().getLoadProfileData(loadProfiles);
    }

    public String getVersion() {
        return "$Date: 2015-11-26 15:26:47 +0200 (Thu, 26 Nov 2015)$";
    }

    public String getSerialNumber() {
        return getProperties().getSerialNumber();
    }

    public int getPhysicalAddress() {
        return 0;
    }

    @Override
    protected void checkCacheObjects() throws IOException {
        getDlmsSession().getMeterConfig().setInstantiatedObjectList(AS300ObjectList.OBJECT_LIST);
    }

    public AS300Messaging getMessageProtocol() {
        if (this.messageProtocol == null) {
            this.messageProtocol = new AS300Messaging(new AS300MessageExecutor(this, this.calendarFinder, this.calendarExtractor, this.messageFileFinder, this.messageFileExtractor, this.dateFormatter));
        }
        return messageProtocol;
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
    public void applyMessages(final List messageEntries) throws IOException {
        getMessageProtocol().applyMessages(messageEntries);
    }

    /**
     * Indicates that each message has to be executed by the protocol.
     *
     * @param messageEntry a definition of which message needs to be sent
     * @return a state of the message which was just sent
     * @throws java.io.IOException if a logical error occurs
     */
    public MessageResult queryMessage(final MessageEntry messageEntry) throws IOException {
        return getMessageProtocol().queryMessage(messageEntry);
    }

    public List<MessageCategorySpec> getMessageCategories() {
        return getMessageProtocol().getMessageCategories();
    }

    public String writeMessage(final Message msg) {
        return getMessageProtocol().writeMessage(msg);
    }

    public String writeTag(final MessageTag tag) {
        return getMessageProtocol().writeTag(tag);
    }

    public String writeValue(final MessageValue value) {
        return getMessageProtocol().writeValue(value);
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        return getProperties().getUPLPropertySpecs();
    }
}