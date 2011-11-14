package com.energyict.smartmeterprotocolimpl.eict.ukhub.zigbee.gas;

import com.energyict.cbo.BusinessException;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dialer.core.Link;
import com.energyict.dialer.coreimpl.SocketStreamConnection;
import com.energyict.dlms.aso.SecurityProvider;
import com.energyict.genericprotocolimpl.nta.abstractnta.NTASecurityProvider;
import com.energyict.protocol.*;
import com.energyict.protocol.messaging.*;
import com.energyict.protocolimpl.dlms.common.AbstractSmartDlmsProtocol;
import com.energyict.protocolimpl.dlms.common.DlmsSession;
import com.energyict.smartmeterprotocolimpl.common.SimpleMeter;
import com.energyict.smartmeterprotocolimpl.eict.ukhub.common.MultipleClientRelatedObisCodes;
import com.energyict.smartmeterprotocolimpl.eict.ukhub.zigbee.gas.composedobjects.ComposedMeterInfo;
import com.energyict.smartmeterprotocolimpl.eict.ukhub.zigbee.gas.events.ZigbeeGasEventProfiles;
import com.energyict.smartmeterprotocolimpl.eict.ukhub.zigbee.gas.messaging.ZigbeeGasMessaging;
import com.energyict.smartmeterprotocolimpl.eict.ukhub.zigbee.gas.messaging.ZigbeeMessageExecutor;
import com.energyict.smartmeterprotocolimpl.eict.ukhub.zigbee.gas.profile.ZigbeeGasLoadProfile;
import com.energyict.smartmeterprotocolimpl.eict.ukhub.zigbee.gas.registers.ZigbeeGasRegisterFactory;

import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

/**
 * The ZigbeeGas logical device has the same protocolBase as the WebRTUZ3. Additional functionality is added for SSE.
 */
public class ZigbeeGas extends AbstractSmartDlmsProtocol implements SimpleMeter, MessageProtocol, TimeOfUseMessaging, WakeUpProtocolSupport {

    /**
     * The properties to use for this protocol
     */
    private ZigbeeGasProperties properties;
    private ZigbeeGasMessaging zigbeeGasMessaging = null;

    /**
     * The used ComposedMeterInfo
     */
    private ComposedMeterInfo meterInfo;
    private ZigbeeGasEventProfiles zigbeeGasEventProfiles;
    private ZigbeeGasLoadProfile zigbeeGasLoadProfile;
    private ZigbeeGasRegisterFactory registerFactory;

    public ZigbeeGasMessaging getMessageProtocol() {
        if (zigbeeGasMessaging == null) {
            this.zigbeeGasMessaging = new ZigbeeGasMessaging(new ZigbeeMessageExecutor(this));
        }
        return this.zigbeeGasMessaging;
    }

    /**
     * Getter for the {@link com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties}
     *
     * @return the requested Properties
     */
    @Override
    protected ZigbeeGasProperties getProperties() {
        if (this.properties == null) {
            this.properties = new ZigbeeGasProperties();
        }
        return this.properties;
    }

    private ComposedMeterInfo getMeterInfo() {
        if (this.meterInfo == null) {
            this.meterInfo = new ComposedMeterInfo(getDlmsSession(), getProperties().isBulkRequest());
        }
        return this.meterInfo;
    }

    /**
     * Initialization method right after we are connected to the physical device.
     */
    @Override
    protected void initAfterConnect() throws ConnectionException {
        //Currently nothing to implement
    }

    /**
     * Get the firmware version of the meter
     *
     * @return the version of the meter firmware
     * @throws java.io.IOException Thrown in case of an exception
     */
    public String getFirmwareVersion() throws IOException {
        try {
            StringBuilder firmware = new StringBuilder();
            firmware.append(getMeterInfo().getFirmwareVersionMonolitic());
            return firmware.toString();
        } catch (IOException e) {
            getLogger().finest("Could not fetch the firmwareVersion. " + e.getMessage());
            return "UnKnown version";
        }
    }

    /**
     * Get the SerialNumber of the device
     *
     * @return the serialNumber of the device
     * @throws java.io.IOException thrown in case of an exception
     */
    public String getMeterSerialNumber() throws IOException {
        try {
            return getMeterInfo().getSerialNumber();
        } catch (IOException e) {
            String message = "Could not retrieve the SerialNumber of the meter. " + e.getMessage();
            getLogger().finest(message);
            throw new IOException(message);
        }
    }

    /**
     * This method is used to request a RegisterInfo object that gives info
     * about the meter's supporting the specific ObisCode. If the ObisCode is
     * not supported, NoSuchRegister is thrown.
     *
     * @param register the Register to request RegisterInfo for
     * @return RegisterInfo about the ObisCode
     * @throws java.io.IOException Thrown in case of an exception
     */
    public RegisterInfo translateRegister(final Register register) throws IOException {
        return getRegisterFactory().translateRegister(register);
    }

    /**
     * Request an array of RegisterValue objects for an given List of ObisCodes. If the ObisCode is not
     * supported, there should not be a register value in the list.
     *
     * @param registers The Registers for which to request a RegisterValues
     * @return List<RegisterValue> for an List of ObisCodes
     * @throws java.io.IOException Thrown in case of an exception
     */
    public List<RegisterValue> readRegisters(final List<Register> registers) throws IOException {
        return getRegisterFactory().readRegisters(registers);
    }

    /**
     * Get all the meter events from the device starting from the given date.
     *
     * @param lastLogbookDate the date of the last <CODE>MeterEvent</CODE> stored in the database
     * @return a list of <CODE>MeterEvents</CODE>
     * @throws java.io.IOException when a logical error occurred
     */
    public List<MeterEvent> getMeterEvents(final Date lastLogbookDate) throws IOException {
        return getZigbeeGasEventProfiles().getEvents(lastLogbookDate);
    }

    /**
     * Get the configuration(interval, number of channels, channelUnits) of all given LoadProfiles from the meter.
     * Build up a list of <CODE>LoadProfileConfiguration</CODE> objects and return them so the
     * framework can validate them to the configuration in EIServer
     *
     * @param loadProfilesToRead the <CODE>List</CODE> of <CODE>LoadProfileReaders</CODE> to indicate which profiles will be read
     * @return a list of <CODE>LoadProfileConfiguration</CODE> objects corresponding with the meter
     * @throws java.io.IOException if a communication or parsing error occurred
     */
    public List<LoadProfileConfiguration> fetchLoadProfileConfiguration(final List<LoadProfileReader> loadProfilesToRead) throws IOException {
        return getZigbeeGasLoadProfile().fetchLoadProfileConfiguration(loadProfilesToRead);
    }

    /**
     * <p>
     * Fetches one or more LoadProfiles from the device. Each <CODE>LoadProfileReader</CODE> contains a list of necessary
     * channels({@link com.energyict.protocol.LoadProfileReader#channelInfos}) to read. If it is possible then only these channels should be read,
     * if not then all channels may be returned in the <CODE>ProfileData</CODE>.
     * </p>
     * <p>
     * <b>Implementors should throw an exception if all data since {@link com.energyict.protocol.LoadProfileReader#getStartReadingTime()} can NOT be fetched</b>,
     * as the collecting system will update its lastReading setting based on the returned ProfileData
     * </p>
     *
     * @param loadProfilesToRead a list of <CODE>LoadProfileReader</CODE> which have to be read
     * @return a list of <CODE>ProfileData</CODE> objects containing interval records
     * @throws java.io.IOException if a communication or parsing error occurred
     */
    public List<ProfileData> getLoadProfileData(final List<LoadProfileReader> loadProfilesToRead) throws IOException {
        return getZigbeeGasLoadProfile().getLoadProfileData(loadProfilesToRead);
    }

    /**
     * Returns the implementation version
     *
     * @return a version string
     */
    public String getVersion() {
        return "$Date$";
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
     * The serialNumber of the meter
     *
     * @return the serialNumber of the meter
     */
    public String getSerialNumber() {
        return getProperties().getSerialNumber();
    }

    /**
     * Get the physical address of the Meter. Mostly this will be an index of the meterList
     * No physical addressing used for ZigbeeGas devices. Each ZigbeeGas device has its own logical device,
     * so Physical address should always be '0'
     *
     * @return the physical Address of the Meter.
     */
    public int getPhysicalAddress() {
        return 0;
    }

    public ZigbeeGasEventProfiles getZigbeeGasEventProfiles() {
        if (zigbeeGasEventProfiles == null) {
            zigbeeGasEventProfiles = new ZigbeeGasEventProfiles(this);
        }
        return zigbeeGasEventProfiles;
    }

    public ZigbeeGasLoadProfile getZigbeeGasLoadProfile() {
        if (zigbeeGasLoadProfile == null) {
            this.zigbeeGasLoadProfile = new ZigbeeGasLoadProfile(this);
        }
        return zigbeeGasLoadProfile;
    }

    public ZigbeeGasRegisterFactory getRegisterFactory() {
        if (this.registerFactory == null) {
            this.registerFactory = new ZigbeeGasRegisterFactory(this);
        }
        return registerFactory;
    }

    public TimeOfUseMessageBuilder getTimeOfUseMessageBuilder() {
        return getMessageProtocol().getTimeOfUseMessageBuilder();
    }

    public TimeOfUseMessagingConfig getTimeOfUseMessagingConfig() {
        return getMessageProtocol().getTimeOfUseMessagingConfig();
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

            pClientProps.setProperty(ZigbeeGasProperties.CLIENT_MAC_ADDRESS, "16");
            pClientProps.setProperty(ZigbeeGasProperties.SECURITY_LEVEL, "0:0");
            getDlmsSession().getProperties().addProperties(pClientProps);

            getDlmsSession().connect();
            long initialFrameCounter = getDlmsSession().getCosemObjectFactory().getData(MultipleClientRelatedObisCodes.frameCounterForClient(backupClientId)).getValue();
            getDlmsSession().disconnect();

            Properties restoredProperties = getDlmsSession().getProperties().getProtocolProperties();
            restoredProperties.setProperty(ZigbeeGasProperties.CLIENT_MAC_ADDRESS, Integer.toString(backupClientId));
            restoredProperties.setProperty(ZigbeeGasProperties.SECURITY_LEVEL, backupSecurityLevel);
            restoredProperties.setProperty(SmartMeterProtocol.PASSWORD, password);

            String ipAddress = link.getStreamConnection().getSocket().getInetAddress().getHostAddress();

            link.getStreamConnection().serverClose();
            link.setStreamConnection(new SocketStreamConnection(ipAddress + ":4059"));
            link.getStreamConnection().serverOpen();
            reInitDlmsSession(link);

            getDlmsSession().getProperties().addProperties(restoredProperties);
            ((ZigbeeGasProperties) getDlmsSession().getProperties()).setSecurityProvider(new NTASecurityProvider(getDlmsSession().getProperties().getProtocolProperties()));

            ((NTASecurityProvider) (getDlmsSession().getProperties().getSecurityProvider())).setInitialFrameCounter(initialFrameCounter + 1);
        } else {
            this.dlmsSession = null;
        }
        return success;
    }

    private void reInitDlmsSession(final Link link) {
        this.dlmsSession = new DlmsSession(link.getInputStream(), link.getOutputStream(), getLogger(), getProperties(), getTimeZone());
    }
}
