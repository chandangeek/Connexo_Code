package com.energyict.smartmeterprotocolimpl.elster.apollo;

import com.energyict.cbo.BusinessException;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dialer.core.Link;
import com.energyict.dialer.coreimpl.SocketStreamConnection;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.genericprotocolimpl.nta.abstractnta.NTASecurityProvider;
import com.energyict.protocol.*;
import com.energyict.protocol.messaging.*;
import com.energyict.protocolimpl.dlms.common.AbstractSmartDlmsProtocol;
import com.energyict.protocolimpl.dlms.common.DlmsSession;
import com.energyict.smartmeterprotocolimpl.common.SimpleMeter;
import com.energyict.smartmeterprotocolimpl.elster.apollo.eventhandling.ApolloEventProfiles;
import com.energyict.smartmeterprotocolimpl.elster.apollo.messaging.AS300MessageExecutor;
import com.energyict.smartmeterprotocolimpl.elster.apollo.messaging.AS300Messaging;

import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * Date: 29-jun-2011
 * Time: 11:32:30
 */
public class AS300 extends AbstractSmartDlmsProtocol implements SimpleMeter, MessageProtocol, TimeOfUseMessaging, FirmwareUpdateMessaging, WakeUpProtocolSupport {

    private AS300Properties properties;
    private AS300ObjectFactory objectFactory;
    private RegisterReader registerReader;
    private LoadProfileBuilder loadProfileBuilder;
    protected AS300Messaging messageProtocol;

    @Override
    protected AS300Properties getProperties() {
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

    private LoadProfileBuilder getLoadProfileBuilder() {
        if (loadProfileBuilder == null) {
            loadProfileBuilder = new LoadProfileBuilder(this);
        }
        return loadProfileBuilder;
    }

    @Override
    protected void initAfterConnect() throws ConnectionException {
        //Nothing, no topology available.
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
                strBuilder.append(new String(getObjectFactory().getActiveFirmwareIdACOR().getAttrbAbstractDataType(-1).toByteArray()));
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
        return "$Date$";
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
     * At a later timestamp the framework will query each {@link com.energyict.protocol.MessageEntry} (see {@link #queryMessage(com.energyict.protocol.MessageEntry)}) to actually
     * perform the message.
     *
     * @param messageEntries a list of {@link com.energyict.protocol.MessageEntry}s
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
     * @return The {@link com.energyict.protocol.messaging.MessageBuilder} capable of generating and parsing 'time of use' messages.
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

    public FirmwareUpdateMessagingConfig getFirmwareUpdateMessagingConfig() {
        return getMessageProtocol().getFirmwareUpdateMessagingConfig();
    }

    public FirmwareUpdateMessageBuilder getFirmwareUpdateMessageBuilder() {
        return getMessageProtocol().getFirmwareUpdateMessageBuilder();
    }


    /**
     * Executes the WakeUp call. The implementer should use and/or update the <code>Link</code> if a WakeUp succeeded. The communicationSchedulerId
     * can be used to find the task which triggered this wakeUp or which Rtu is being waked up.
     *
     * @param communicationSchedulerId the ID of the <code>CommunicationScheduler</code> which started this task
     * @param link                     Link created by the comserver, can be null if a NullDialer is configured
     * @param logger                   Logger object - when using a level of warning or higher message will be stored in the communication session's database log,
     *                                 messages with a level lower than warning will only be logged in the file log if active.
     * @throws com.energyict.cbo.BusinessException
     *                             if a business exception occurred
     * @throws java.io.IOException if an io exception occurred
     */
    public boolean executeWakeUp(final int communicationSchedulerId, Link link, final Logger logger) throws BusinessException, IOException {

        boolean success = true;

        init(link.getInputStream(), link.getOutputStream(), TimeZone.getDefault(), logger);
        if(getDlmsSession().getProperties().getDataTransportSecurityLevel() != 0 || getDlmsSession().getProperties().getAuthenticationSecurityLevel() == 5){
            int backupClientId = getDlmsSession().getProperties().getClientMacAddress();
            String backupSecurityLevel = getDlmsSession().getProperties().getSecurityLevel();
            String password = getDlmsSession().getProperties().getPassword();

            Properties pClientProps = getDlmsSession().getProperties().getProtocolProperties();

            pClientProps.setProperty(AS300Properties.CLIENT_MAC_ADDRESS, "16");
            pClientProps.setProperty(AS300Properties.SECURITY_LEVEL, "0:0");
            getDlmsSession().getProperties().addProperties(pClientProps);

            getDlmsSession().connect();
            long initialFrameCounter = getDlmsSession().getCosemObjectFactory().getData(getObjectFactory().getObisCodeProvider().getFrameCounterObisCode(backupClientId)).getValue();
            getDlmsSession().disconnect();

            Properties restoredProperties = getDlmsSession().getProperties().getProtocolProperties();
            restoredProperties.setProperty(AS300Properties.CLIENT_MAC_ADDRESS, Integer.toString(backupClientId));
            restoredProperties.setProperty(AS300Properties.SECURITY_LEVEL, backupSecurityLevel);
            restoredProperties.setProperty(SmartMeterProtocol.PASSWORD, password);

            String ipAddress = link.getStreamConnection().getSocket().getInetAddress().getHostAddress();

            link.getStreamConnection().serverClose();
            link.setStreamConnection(new SocketStreamConnection(ipAddress + ":4059"));
            link.getStreamConnection().serverOpen();
            reInitDlmsSession(link);

            getDlmsSession().getProperties().addProperties(restoredProperties);
            ((AS300Properties) getDlmsSession().getProperties()).setSecurityProvider(new NTASecurityProvider(getDlmsSession().getProperties().getProtocolProperties()));

            ((NTASecurityProvider) (getDlmsSession().getProperties().getSecurityProvider())).setInitialFrameCounter(initialFrameCounter + 1);
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