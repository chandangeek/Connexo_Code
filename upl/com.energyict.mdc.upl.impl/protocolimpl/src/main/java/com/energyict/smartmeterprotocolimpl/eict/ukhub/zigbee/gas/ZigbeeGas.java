package com.energyict.smartmeterprotocolimpl.eict.ukhub.zigbee.gas;

import com.energyict.dialer.connection.ConnectionException;
import com.energyict.protocol.*;
import com.energyict.protocol.messaging.*;
import com.energyict.protocolimpl.dlms.common.AbstractSmartDlmsProtocol;
import com.energyict.smartmeterprotocolimpl.common.SimpleMeter;
import com.energyict.smartmeterprotocolimpl.eict.ukhub.zigbee.gas.composedobjects.ComposedMeterInfo;
import com.energyict.smartmeterprotocolimpl.eict.ukhub.zigbee.gas.events.ZigbeeGasEventProfiles;
import com.energyict.smartmeterprotocolimpl.eict.ukhub.zigbee.gas.profile.ZigbeeGasLoadProfile;
import com.energyict.smartmeterprotocolimpl.eict.ukhub.zigbee.gas.registers.ZigbeeGasRegisterFactory;

import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * The ZigbeeGas logical device has the same protocolBase as the WebRTUZ3. Additional functionality is added for SSE.
 */
public class ZigbeeGas extends AbstractSmartDlmsProtocol implements SimpleMeter, MessageProtocol, TimeOfUseMessaging {

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
            this.zigbeeGasMessaging = new ZigbeeGasMessaging();
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
        //TODO implement proper functionality.
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
            firmware.append(getMeterInfo().getFirmwareVersionMid());
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

    @Override
    protected void checkCacheObjects() throws IOException {
        getDlmsSession().getMeterConfig().setInstantiatedObjectList(ObisCodeProvider.OBJECT_LIST);

    }

    public boolean needsName() {
        return getMessageProtocol().needsName();
    }

    public boolean supportsCodeTables() {
        return getMessageProtocol().supportsCodeTables();
    }

    public boolean supportsUserFiles() {
        return getMessageProtocol().supportsUserFiles();
    }

    /**
     * Indicates whether the implementer supports {@link com.energyict.mdw.core.UserFile} references. This will typically be used by generic protocols, as these have
     * access to the database. If {@link #supportsUserFiles()} is true, and this method returns <code>false</code>, the entire file is sent as payload in
     * the message. If this method returns <code>true</code>, a user file ID is passed on to the implementer, who can then query the database for the
     * contents of the file.
     *
     * @return <code>true</code> if the protocol supports a file ID reference, <code>false</code> if it does not. This can equally be translated into
     *         <code>true</code> for a generic protocol, and <code>false</code> for a normal one.
     */
    public boolean supportsUserFileReferences() {
        return getMessageProtocol().supportsUserFileReferences();
    }

    /**
     * Indicates whether the implementer supports {@link com.energyict.mdw.core.Code} references. This will typically be used by generic protocols, as these have
     * access to the database. If {@link #supportsCodeTables()} is true, and this method returns <code>false</code>, the entire file is sent as payload in
     * the message. If this method returns <code>true</code>, a user file ID is passed on to the implementer, who can then query the database for the
     * contents of the file.
     *
     * @return <code>true</code> if the protocol supports a codeTable ID reference, <code>false</code> if it does not. This can equally be translated into
     *         <code>true</code> for a generic protocol, and <code>false</code> for a normal one.
     */
    public boolean supportsCodeTableReferences() {
        return getMessageProtocol().supportsCodeTableReferences();
    }

    /**
     * Indicates whether the content of the {@link com.energyict.mdw.core.UserFile} of the {@link com.energyict.mdw.core.Code} must be zipped when it is inlined in the
     * RtuMessage. This is only taken into account when {@link #supportsCodeTableReferences()} or {@link #supportsUserFileReferences()} is false.</br>
     * <b><u>NOTE:</u> If the content is zipped, then Base64 Encoding is also applied!</b>
     *
     * @return true if the content needs to be zipped, false otherwise
     */
    public boolean zipContent() {
        return getMessageProtocol().zipContent();
    }

    /**
     * Indicate whether the content of the {@link com.energyict.mdw.core.UserFile} or {@link com.energyict.mdw.core.Code} must be Base64 Encoded.
     * <b><u>NOTE:</u> Base64 encoding will be automatically applied if {@link #zipContent()} returns true</b>
     *
     * @return true if the content needs to be Base64 encoded, false otherwise
     */
    public boolean encodeContentToBase64() {
        return getMessageProtocol().encodeContentToBase64();
    }

    public TimeOfUseMessageBuilder getTimeOfUseMessageBuilder() {
        return getMessageProtocol().getTimeOfUseMessageBuilder();
    }
}
