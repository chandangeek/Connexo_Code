package com.energyict.smartmeterprotocolimpl.elster.apollo;

import com.energyict.dialer.core.impl.IPDialer;
import com.energyict.dialer.core.impl.SocketStreamConnection;
import com.energyict.mdc.protocol.api.WakeUpProtocolSupport;
import com.energyict.mdc.protocol.api.dialer.connection.ConnectionException;
import com.energyict.dlms.DlmsSession;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.protocol.api.LoadProfileReader;
import com.energyict.mdc.protocol.api.device.data.MessageEntry;
import com.energyict.mdc.protocol.api.device.data.MessageResult;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.device.data.Register;
import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;
import com.energyict.mdc.protocol.api.device.events.MeterEvent;
import com.energyict.mdc.protocol.api.LoadProfileConfiguration;
import com.energyict.mdc.protocol.api.MessageProtocol;
import com.energyict.mdc.protocol.api.dialer.core.Link;
import com.energyict.mdc.protocol.api.legacy.SmartMeterProtocol;
import com.energyict.mdc.protocol.api.messaging.Message;
import com.energyict.mdc.protocol.api.messaging.MessageTag;
import com.energyict.mdc.protocol.api.messaging.MessageValue;

import com.energyict.protocols.mdc.services.impl.OrmClient;
import com.energyict.protocols.messaging.MessageBuilder;
import com.energyict.protocols.messaging.TimeOfUseMessageBuilder;
import com.energyict.protocols.messaging.TimeOfUseMessaging;
import com.energyict.protocols.messaging.TimeOfUseMessagingConfig;
import com.energyict.protocolimpl.dlms.common.AbstractSmartDlmsProtocol;
import com.energyict.smartmeterprotocolimpl.common.SimpleMeter;
import com.energyict.smartmeterprotocolimpl.eict.ukhub.common.UkHubSecurityProvider;
import com.energyict.smartmeterprotocolimpl.elster.apollo.eventhandling.ApolloEventProfiles;
import com.energyict.smartmeterprotocolimpl.elster.apollo.messaging.AS300MessageExecutor;
import com.energyict.smartmeterprotocolimpl.elster.apollo.messaging.AS300Messaging;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * Date: 29-jun-2011
 * Time: 11:32:30
 */
public class AS300 extends AbstractSmartDlmsProtocol implements SimpleMeter, MessageProtocol, TimeOfUseMessaging, WakeUpProtocolSupport {

    protected AS300Properties properties;
    private AS300ObjectFactory objectFactory;
    private RegisterReader registerReader;
    protected AS300LoadProfileBuilder loadProfileBuilder;
    protected AS300Messaging messageProtocol;

    @Inject
    public AS300(OrmClient ormClient) {
        super(ormClient);
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

    @Override
    public String getProtocolDescription() {
        return "Elster AS300-P (SSWG IC) DLMS";
    }

    /**
     * Returns the version
     *
     * @return the version string
     */
    public String getVersion() {
        return "$Date: 2013-10-31 11:22:19 +0100 (Thu, 31 Oct 2013) $";
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
            this.messageProtocol = new AS300Messaging(new AS300MessageExecutor(this));
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

    public List getMessageCategories() {
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

    /**
     * Returns the message builder capable of generating and parsing 'time of use' messages.
     *
     * @return The {@link MessageBuilder} capable of generating and parsing 'time of use' messages.
     */
    public TimeOfUseMessageBuilder getTimeOfUseMessageBuilder() {
        return getMessageProtocol().getTimeOfUseMessageBuilder();
    }

    /**
     * Returns the config object for the TimeOfUseMessages
     *
     * @return the config object
     */
    public TimeOfUseMessagingConfig getTimeOfUseMessagingConfig() {
        return getMessageProtocol().getTimeOfUseMessagingConfig();
    }

    /**
     * Executes the WakeUp call. The implementer should use and/or update the <code>Link</code> if a WakeUp succeeded. The communicationSchedulerId
     * can be used to find the task which triggered this wakeUp or which Device is being waked up.
     *
     * @param communicationSchedulerId the ID of the <code>CommunicationScheduler</code> which started this task
     * @param link                     Link created by the comserver, can be null if a NullDialer is configured
     * @param logger                   Logger object - when using a level of warning or higher message will be stored in the communication session's database log,
     *                                 messages with a level lower than warning will only be logged in the file log if active.
     * @throws BusinessException
     *                             if a business exception occurred
     * @throws java.io.IOException if an io exception occurred
     */
    public boolean executeWakeUp(final int communicationSchedulerId, Link link, final Logger logger) throws BusinessException, IOException {

        boolean success = true;

        init(link.getInputStream(), link.getOutputStream(), TimeZone.getDefault(), logger);
        if(getProperties().getDataTransportSecurityLevel() != 0 || getProperties().getAuthenticationSecurityLevel() == 5){
            int backupClientId = getProperties().getClientMacAddress();
            String backupSecurityLevel = getProperties().getSecurityLevel();
            String password = getProperties().getPassword();

            getProperties().getProtocolProperties().setProperty(AS300Properties.CLIENT_MAC_ADDRESS, "16");
            getProperties().getProtocolProperties().setProperty(AS300Properties.SECURITY_LEVEL, "0:0");

            getDlmsSession().connect();
            long initialFrameCounter = getDlmsSession().getCosemObjectFactory().getData(getObjectFactory().getObisCodeProvider().getFrameCounterObisCode(backupClientId)).getValue();
            getDlmsSession().disconnect();

            getProperties().getProtocolProperties().setProperty(AS300Properties.CLIENT_MAC_ADDRESS, Integer.toString(backupClientId));
            getProperties().getProtocolProperties().setProperty(AS300Properties.SECURITY_LEVEL, backupSecurityLevel);
            getProperties().getProtocolProperties().setProperty(SmartMeterProtocol.PASSWORD, password);

            if (link instanceof IPDialer) {
                String ipAddress = link.getStreamConnection().getSocket().getInetAddress().getHostAddress();
                link.getStreamConnection().serverClose();
                link.setStreamConnection(new SocketStreamConnection(ipAddress + ":4059"));
                link.getStreamConnection().serverOpen();
            }

            getProperties().setSecurityProvider(new UkHubSecurityProvider(getProperties().getProtocolProperties()));
            ((UkHubSecurityProvider) (getProperties().getSecurityProvider())).setInitialFrameCounter(initialFrameCounter + 1);

            reInitDlmsSession(link);
            this.objectFactory = null;
        } else {
            this.dlmsSession = null;
        }
        return success;
    }

    private void reInitDlmsSession(final Link link) {
        this.dlmsSession = new DlmsSession(link.getInputStream(), link.getOutputStream(), getLogger(), getProperties(), getTimeZone());
    }
}