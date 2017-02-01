package com.energyict.smartmeterprotocolimpl.eict.ukhub.zigbee.gas;

import com.energyict.dialer.connection.ConnectionException;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileFinder;
import com.energyict.mdc.upl.messages.legacy.Message;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.MessageTag;
import com.energyict.mdc.upl.messages.legacy.MessageValue;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
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
import com.energyict.smartmeterprotocolimpl.eict.ukhub.zigbee.gas.composedobjects.ComposedMeterInfo;
import com.energyict.smartmeterprotocolimpl.eict.ukhub.zigbee.gas.events.ZigbeeGasEventProfiles;
import com.energyict.smartmeterprotocolimpl.eict.ukhub.zigbee.gas.messaging.ZigbeeGasMessaging;
import com.energyict.smartmeterprotocolimpl.eict.ukhub.zigbee.gas.messaging.ZigbeeMessageExecutor;
import com.energyict.smartmeterprotocolimpl.eict.ukhub.zigbee.gas.profile.ZigbeeGasLoadProfile;
import com.energyict.smartmeterprotocolimpl.eict.ukhub.zigbee.gas.registers.ZigbeeGasRegisterFactory;

import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * The ZigbeeGas logical device has the same protocolBase as the WebRTUZ3. Additional functionality is added for SSE.
 */
public class ZigbeeGas extends AbstractSmartDlmsProtocol implements SimpleMeter, MessageProtocol, SerialNumberSupport {

    private final DeviceMessageFileFinder messageFileFinder;
    private final DeviceMessageFileExtractor messageFileExtractor;
    private final PropertySpecService propertySpecService;
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

    public ZigbeeGas(DeviceMessageFileFinder messageFileFinder, DeviceMessageFileExtractor messageFileExtractor, PropertySpecService propertySpecService) {
        this.messageFileFinder = messageFileFinder;
        this.messageFileExtractor = messageFileExtractor;
        this.propertySpecService = propertySpecService;
    }

    public ZigbeeGasMessaging getMessageProtocol() {
        if (zigbeeGasMessaging == null) {
            this.zigbeeGasMessaging = new ZigbeeGasMessaging(new ZigbeeMessageExecutor(this, this.messageFileFinder, this.messageFileExtractor));
        }
        return this.zigbeeGasMessaging;
    }

    public Date getTime() throws IOException {
        if (getProperties().isFirmwareUpdateSession()) {
            getLogger().severe("Using firmware update client. Skipping clock readout!");
            return new Date();
        } else {
            return super.getTime();
        }
    }

    /**
     * Getter for the {@link com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties}
     *
     * @return the requested Properties
     */
    @Override
    public ZigbeeGasProperties getProperties() {
        if (this.properties == null) {
            this.properties = new ZigbeeGasProperties(this.propertySpecService);
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
        if (this.dlmsSession != null) {
            // We need to update the correct TimeZone!!
            this.dlmsSession.updateTimeZone(getTimeZone());
        }
    }

    /**
     * Get the firmware version of the meter
     *
     * @return the version of the meter firmware
     * @throws java.io.IOException Thrown in case of an exception
     */
    public String getFirmwareVersion() throws IOException {
        if (getProperties().isFirmwareUpdateSession()) {
            getLogger().severe("Using firmware update client. Skipping firmware version readout!");
            return "";
        } else {
            try {
                StringBuilder firmware = new StringBuilder();
                firmware.append(getMeterInfo().getFirmwareVersionMonolitic());
                return firmware.toString();
            } catch (IOException e) {
                getLogger().finest("Could not fetch the firmwareVersion. " + e.getMessage());
                return "UnKnown version";
            }
        }
    }

    /**
     * Get the SerialNumber of the device
     *
     * @return the serialNumber of the device
     * @throws java.io.IOException thrown in case of an exception
     */
    public String getMeterSerialNumber() throws IOException {
        if (getProperties().isFirmwareUpdateSession()) {
            getLogger().severe("Using firmware update client. Skipping serial number check!");
            return getSerialNumber();
        } else {
            try {
                return getMeterInfo().getSerialNumber();
            } catch (IOException e) {
                String message = "Could not retrieve the SerialNumber of the meter. " + e.getMessage();
                getLogger().finest(message);
                throw new IOException(message);
            }
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
     * if not then all channels may be returned in the <CODE>ProfileData</CODE>. If {@link LoadProfileReader#channelInfos} contains an empty list
     * or null, then all channels from the corresponding LoadProfile should be fetched.
     * </p>
     * <p>
     * <b>Implementors should throw an exception if all data since {@link LoadProfileReader#getStartReadingTime()} can NOT be fetched</b>,
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
     * @return the version
     */
    public String getVersion() {
        return "$Date: 2015-11-26 15:23:42 +0200 (Thu, 26 Nov 2015)$";
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
    public List<PropertySpec> getUPLPropertySpecs() {
        return getProperties().getUPLPropertySpecs();
    }
}
