package com.energyict.smartmeterprotocolimpl.eict.ukhub;

import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileFinder;
import com.energyict.mdc.upl.messages.legacy.Message;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.MessageTag;
import com.energyict.mdc.upl.messages.legacy.MessageValue;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.dlms.ConnectionMode;
import com.energyict.dlms.DLMSMeterConfig;
import com.energyict.dlms.IF2HHUSignon;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.DLMSClassId;
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
import com.energyict.smartmeterprotocolimpl.common.MasterMeter;
import com.energyict.smartmeterprotocolimpl.common.SimpleMeter;
import com.energyict.smartmeterprotocolimpl.eict.ukhub.composedobjects.ComposedMeterInfo;
import com.energyict.smartmeterprotocolimpl.eict.ukhub.events.UkHubEventProfiles;
import com.energyict.smartmeterprotocolimpl.eict.ukhub.messaging.UkHubMessageExecutor;
import com.energyict.smartmeterprotocolimpl.eict.ukhub.messaging.UkHubMessaging;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * The UK hub has the same protocolBase as the WebRTUZ3. Additional functionality is added for SSE, more specifically Zigbee HAN functionality
 * and Prepayment
 */
public class UkHub extends AbstractSmartDlmsProtocol implements MasterMeter, SimpleMeter, MessageProtocol, SerialNumberSupport {

    /**
     * The properties to use for this protocol
     */
    private UkHubProperties properties;

    /**
     * The used ComposedMeterInfo
     */
    private ComposedMeterInfo meterInfo;

    protected MessageProtocol messageProtocol;

    /**
     * The used <code>UkHubRegisterFactory</code> to read and manage the HUB registers
     */
    private UkHubRegisterFactory registerFactory = null;
    private UkHubEventProfiles ukHubEventProfiles = null;
    private boolean reboot = false;
    private final PropertySpecService propertySpecService;
    private final DeviceMessageFileFinder messageFileFinder;
    private final DeviceMessageFileExtractor messageFileExtractor;

    public UkHub(PropertySpecService propertySpecService, DeviceMessageFileFinder messageFileFinder, DeviceMessageFileExtractor messageFileExtractor) {
        this.propertySpecService = propertySpecService;
        this.messageFileFinder = messageFileFinder;
        this.messageFileExtractor = messageFileExtractor;
    }

    /**
     * Getter for the MessageProtocol implementation
     *
     * @return the UkHubMessaging implementation
     */
    public MessageProtocol getMessageProtocol() {
        if (messageProtocol == null) {
            messageProtocol = new UkHubMessaging(new UkHubMessageExecutor(this, this.messageFileFinder, this.messageFileExtractor));
        }
        return messageProtocol;
    }

    /**
     * Getter for the {@link com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties}
     *
     * @return the requested Properties
     */
    @Override
    public UkHubProperties getProperties() {
        if (this.properties == null) {
            this.properties = new UkHubProperties(this.propertySpecService);
        }
        return this.properties;
    }

    protected ComposedMeterInfo getMeterInfo() {
        if (this.meterInfo == null) {
            this.meterInfo = new ComposedMeterInfo(getDlmsSession(), getProperties().isBulkRequest());
        }
        return this.meterInfo;
    }

    /**
     * <p>
     * sets the device time to the current system time.
     * </p>
     *
     * @param newMeterTime the time to set in the meter
     * @throws java.io.IOException Thrown in case of an exception
     */
    @Override
    public void setTime(final Date newMeterTime) throws IOException {
        getLogger().info("TimeSet not applied, meter is synchronized by the E-meter.");
    }

    /**
     * Initialization method right after we are connected to the physical device.
     */
    @Override
    protected void initAfterConnect() throws ConnectionException {
        if(this.dlmsSession != null){
            // We need to update the correct TimeZone!!
            this.dlmsSession.updateTimeZone(getTimeZone());
        }
    }

    /**
     * <p></p>
     *
     * @return the current device time
     * @throws java.io.IOException <br>
     */
    public Date getTime() throws IOException {
        if (getProperties().isFirmwareUpdateSession()) {
            getLogger().severe("Using firmware update client. Skipping clock readout!");
            return new Date();
        } else {
            return super.getTime();
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
                firmware.append(getMeterInfo().getFirmwareVersion());

                // TODO possible to add the ZigBee versions etc.
                //            String rfFirmware = getRFFirmwareVersion();
                //            if (!rfFirmware.equalsIgnoreCase("")) {
                //                firmware.append(" - RF-FirmwareVersion : ");
                //                firmware.append(rfFirmware);
                //            }
                return firmware.toString();
            } catch (IOException e) {
                String message = "Could not fetch the firmwareVersion. " + e.getMessage();
                getLogger().finest(message);
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
        //TODO implement proper functionality.
        return new RegisterInfo("Unknown");
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
        return getUkHubEventProfiles().getEvents(lastLogbookDate);
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
        //Currently no LoadProfile is supported
        return new ArrayList<LoadProfileConfiguration>();
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
     * @param loadProfiles a list of <CODE>LoadProfileReader</CODE> which have to be read
     * @return a list of <CODE>ProfileData</CODE> objects containing interval records
     * @throws java.io.IOException if a communication or parsing error occurred
     */
    public List<ProfileData> getLoadProfileData(final List<LoadProfileReader> loadProfiles) throws IOException {
        //Currently not LoadProfile is supported
        return new ArrayList<ProfileData>();
    }

    /**
     * Returns the version
     *
     * @return the version string
     */
    public String getVersion() {
        return "$Date: 2015-11-26 15:26:01 +0200 (Thu, 26 Nov 2015)$";
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
     * Search for local slave devices so a general topology can be build up
     */
    public void searchForSlaveDevices() throws ConnectionException {
        // Not required for AM110R protocol
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
     *
     * @return the physical Address of the Meter.
     */
    public int getPhysicalAddress() {
        return 0;  // indicates the Master
    }

    public UkHubRegisterFactory getRegisterFactory() {
        if(this.registerFactory == null) {
            this.registerFactory = new UkHubRegisterFactory(this);
        }
        return registerFactory;
    }

    public UkHubEventProfiles getUkHubEventProfiles() {
        if (ukHubEventProfiles == null) {
            this.ukHubEventProfiles = new UkHubEventProfiles(this);
        }
        return ukHubEventProfiles;
    }

    public void disConnect() throws IOException {
        super.disconnect();
    }

    /**
     * Disconnect from the physical device.
     * Close the association and check if we need to close the underlying connection
     */
    public void disconnect() throws IOException {
        if (reboot) {
            getCosemObjectFactory().getGenericInvoke(ObisCodeProvider.REBOOT_OBISCODE, DLMSClassId.SCRIPT_TABLE.getClassId(), 1).invoke();
        } else {
            getDlmsSession().disconnect();
        }
    }

    public void setReboot(boolean reboot) {
        this.reboot = reboot;
    }

    public DLMSMeterConfig getMeterConfig() {
        return getDlmsSession().getMeterConfig();
    }

    public CosemObjectFactory getCosemObjectFactory() {
        return getDlmsSession().getCosemObjectFactory();
    }

    @Override
    public void enableHHUSignOn(SerialCommunicationChannel commChannel, boolean enableDataReadout) throws ConnectionException {
        if (commChannel != null) {
            if (getProperties().getConnectionMode() == ConnectionMode.IF2) {
                getDlmsSession().setHhuSignOn(new IF2HHUSignon(commChannel, getLogger()));
            }
        }
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        return getProperties().getUPLPropertySpecs();
    }
}
