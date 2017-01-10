package com.energyict.smartmeterprotocolimpl.prenta.iskra.mx372;

import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.messages.legacy.Message;
import com.energyict.mdc.upl.messages.legacy.MessageCategorySpec;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.MessageTag;
import com.energyict.mdc.upl.messages.legacy.MessageValue;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarExtractor;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarFinder;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dlms.DLMSConnection;
import com.energyict.dlms.DLMSMeterConfig;
import com.energyict.dlms.DLMSUtils;
import com.energyict.dlms.DataContainer;
import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.ScalerUnit;
import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.StoredValues;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.messaging.LegacyLoadProfileRegisterMessageBuilder;
import com.energyict.messaging.LegacyPartialLoadProfileMessageBuilder;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.LoadProfileConfiguration;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocol.MessageProtocol;
import com.energyict.protocol.MessageResult;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocol.Register;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.support.SerialNumberSupport;
import com.energyict.protocolimpl.dlms.common.AbstractSmartDlmsProtocol;
import com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties;
import com.energyict.smartmeterprotocolimpl.prenta.iskra.mx372.messaging.IskraMx372Messaging;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * User: sva
 * Date: 19/01/12
 * Time: 16:21
 */
public class IskraMx372 extends AbstractSmartDlmsProtocol implements ProtocolLink, MessageProtocol, SerialNumberSupport {

    private IskraMX372Properties properties;
    private String serialnr = null;
    private String devID = null;
    private CosemObjectFactory cosemObjectFactory;
    private LoadProfileBuilder loadProfileBuilder;
    private RegisterReader registerReader;

    private ObisCode deviceLogicalName = ObisCode.fromString("0.0.42.0.0.255");

    private IskraMx372Messaging messageProtocol;

    public static ScalerUnit[] demandScalerUnits = {new ScalerUnit(0, 30), new ScalerUnit(0, 255), new ScalerUnit(0, 255), new ScalerUnit(0, 255), new ScalerUnit(0, 255)};
    private static final int ELECTRICITY = 0x00;
    private static final int MBUS = 0x01;

    /**
     * Indicating if the meter has a breaker.
     * This implies whether or not we can control the breaker and read the control logbook.
     * This will be set to false in the cryptoserver protocols, because these meters don't have a breaker anymore.
     */
    private boolean hasBreaker = true;
    private final PropertySpecService propertySpecService;
    private final TariffCalendarFinder calendarFinder;
    private final TariffCalendarExtractor extractor;
    private final DeviceMessageFileExtractor messageFileExtractor;

    public IskraMx372(PropertySpecService propertySpecService, TariffCalendarFinder calendarFinder, TariffCalendarExtractor extractor, DeviceMessageFileExtractor messageFileExtractor) {
        this.propertySpecService = propertySpecService;
        this.calendarFinder = calendarFinder;
        this.extractor = extractor;
        this.messageFileExtractor = messageFileExtractor;
    }

    /**
     * Getter for the {@link com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties}
     *
     * @return the requested Properties
     */
    @Override
    public DlmsProtocolProperties getProperties() {
        if (this.properties == null) {
            this.properties = new IskraMX372Properties(this.propertySpecService);
        }
        return this.properties;
    }

    @Override
    public void init(InputStream inputStream, OutputStream outputStream, TimeZone timeZone, Logger logger) throws IOException {
        if (!properties.madeCSDCall()) {
            super.init(inputStream, outputStream, timeZone, logger);
            cosemObjectFactory = new CosemObjectFactory(this);
        }
    }

    /**
     * Make a connection to the physical device.
     * Setup the association and check the objectList
     *
     * @throws java.io.IOException if errors occurred during data fetching
     */
    @Override
    public void connect() throws IOException {
        if (!properties.madeCSDCall()) {
            super.connect();
        }
    }

    /**
     * Disconnect from the physical device.
     * Close the association and check if we need to close the underlying connection
     */
    @Override
    public void disconnect() throws IOException {
       if (!properties.madeCSDCall()) {
            super.disconnect();
        }
    }

    /**
     * Initialization method right after we are connected to the physical device.
     */
    @Override
    protected void initAfterConnect() throws ConnectionException {
        validateMeterID();
        searchForSlaveDevices();
    }

    private void validateMeterID() throws ConnectionException {
        if ((getProperties().getDeviceId() == null) || ("".compareTo(getProperties().getDeviceId()) == 0)) {
            return;
        }
        String deviceID = getDeviceAddress();
        if ((deviceID != null) && (deviceID.compareTo(getProperties().getDeviceId()) == 0)) {
            return;
        }
        throw new ConnectionException("DeviceID mismatch! meter device ID=" + deviceID + ", configured device ID=" + getProperties().getDeviceId());
    }

    public String getDeviceAddress() throws ConnectionException {
        if (devID == null) {
            try {
                devID = getCosemObjectFactory().getGenericRead(deviceLogicalName, DLMSUtils.attrLN2SN(2), 1).getString();
            } catch (IOException e) {
                throw new ConnectionException(e.getMessage());
            }
        }
        return devID;
    }

    /**
     * Search for slave devices
     *
     *
     */
    public void searchForSlaveDevices() throws ConnectionException {
        try {
            messageProtocol.checkMbusDevices();
        } catch (Exception e) {
            throw new ConnectionException("Got an error while loading the attached Slave devices: "+e.getMessage());
        }
    }


    /**
     * Get the firmware version of the meter
     *
     * @return the version of the meter firmware
     * @throws java.io.IOException Thrown in case of an exception
     */
    public String getFirmwareVersion() throws IOException {
        return ((IskraMX372Properties)getProperties()).getFirmwareVersion();
    }

    /**
     * Get the SerialNumber of the device
     *
     * @return the serialNumber of the device
     */
    public String getMeterSerialNumber()  {
        try{
            if (!properties.madeCSDCall()) {
                UniversalObject uo = getMeterConfig().getSerialNumberObject();
                return getCosemObjectFactory().getGenericRead(uo).getString();
            } else {
                return getProperties().getSerialNumber();
            }
        } catch (IOException e){
            throw DLMSIOExceptionHandler.handle(e, getDlmsSession().getProperties().getRetries() + 1);
        }
    }

    public CosemObjectFactory getCosemObjectFactory() {
        return cosemObjectFactory;
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
    public RegisterInfo translateRegister(Register register) throws IOException {
        return ObisCodeMapper.getRegisterInfo(register.getObisCode());
    }

    /**
     * Request an array of RegisterValue objects for an given List of ObisCodes. If the ObisCode is not
     * supported, there should not be a register value in the list.
     *
     * @param registers The Registers for which to request a RegisterValues
     * @return List<RegisterValue> for an List of ObisCodes
     * @throws java.io.IOException Thrown in case of an exception
     */
    public List<RegisterValue> readRegisters(List<Register> registers) throws IOException {
        if (!properties.madeCSDCall()) {
            return getRegisterReader().read(registers);
        } else {
            throw new IOException("Reading of meter readings not allowed in the CSD schedule. Please correct this first.");
        }
    }

     public RegisterReader getRegisterReader() {
        if (registerReader == null) {
            registerReader = new RegisterReader(this);
        }
        return registerReader;
    }

    public static ScalerUnit getScalerUnit(ObisCode obisCode) {

        if (obisCode.toString().indexOf("1.0") == 0) {
            return demandScalerUnits[ELECTRICITY];
        } else {
            return demandScalerUnits[MBUS];
        }
    }

    /**
     * Get all the meter events from the device starting from the given date.
     *
     * @param lastLogbookDate the date of the last <CODE>MeterEvent</CODE> stored in the database
     * @return a list of <CODE>MeterEvents</CODE>
     * @throws java.io.IOException when a logical error occurred
     */
    public List<MeterEvent> getMeterEvents(Date lastLogbookDate) throws IOException {
        if (!properties.madeCSDCall()) {
            Calendar cal = Calendar.getInstance(getTimeZone());
            cal.setTime(lastLogbookDate);
            return getEventLog(cal);
        } else {
            throw new IOException("Reading of meter events not allowed in the CSD schedule. Please correct this first.");
        }
    }

    /**
     * Getter for the MeterEvent list
     *
     * @param fromCalendar the time to start collection events from the device
     * @return a list of MeterEvents
     */
    public List<MeterEvent> getEventLog(Calendar fromCalendar) throws IOException {
        Calendar toCalendar = ProtocolUtils.getCalendar(getTimeZone()); // Must be in the device time zone
        DataContainer dc = getCosemObjectFactory().getProfileGeneric(getMeterConfig().getEventLogObject().getObisCode()).getBuffer(fromCalendar, toCalendar);
        Logbook logbook = new Logbook(getTimeZone(), getLogger());
        return logbook.getMeterEvents(dc);
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
    public List<LoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfilesToRead) throws IOException {
        if (!properties.madeCSDCall()) {
            return getLoadProfileBuilder().fetchLoadProfileConfiguration(loadProfilesToRead);
        } else {
            throw new IOException("Reading of demand values not allowed in the CSD schedule. Please correct this first.");
        }
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
    public List<ProfileData> getLoadProfileData(List<LoadProfileReader> loadProfiles) throws IOException {
        if (!properties.madeCSDCall()) {
            return getLoadProfileBuilder().getLoadProfileData(loadProfiles);
        } else {
            throw new IOException("Reading of demand values not allowed in the CSD schedule. Please correct this first.");
        }
    }

    private LoadProfileBuilder getLoadProfileBuilder() {
        if (loadProfileBuilder == null) {
            loadProfileBuilder = new LoadProfileBuilder(this);
        }
        return loadProfileBuilder;
    }

    /**
     * Returns implementation version
     *
     * @return a version string
     */
    public String getVersion() {
        return "$Date: 2015-11-26 15:25:15 +0200 (Thu, 26 Nov 2015)$";
    }

    /**
     * <p></p>
     *
     * @return the current device time
     * @throws java.io.IOException <br>
     */
    @Override
    public Date getTime() throws IOException {
        if (!properties.madeCSDCall()) {
            return super.getTime();
        } else {
            return new Date();
        }
    }

    /**
     * Getter for property {@link com.energyict.dlms.DLMSConnection}.
     *
     * @return the {@link com.energyict.dlms.DLMSConnection}.
     */
    public DLMSConnection getDLMSConnection() {
        return getDlmsSession().getDLMSConnection();
    }

    /**
     * Getter for property meterConfig.
     *
     * @return Value of property meterConfig.
     */
    public DLMSMeterConfig getMeterConfig() {
        return getDlmsSession().getMeterConfig();
    }

    /**
     * Check if the {@link java.util.TimeZone} is read from the DLMS device, or if the
     * {@link java.util.TimeZone} from the {@link com.energyict.protocol.MeterProtocol} should be used.
     *
     * @return true is the {@link java.util.TimeZone} is read from the device
     */
    public boolean isRequestTimeZone() {
        return (((IskraMX372Properties) getProperties()).getRequestTimeZone() == 1) ? true : false;
    }

    /**
     * Getter for the round trip correction.
     *
     * @return the value of the round trip correction
     */
    public int getRoundTripCorrection() {
        return getProperties().getRoundTripCorrection();
    }

    /**
     * Getter for the type of reference used in the DLMS protocol. This can be
     * {@link com.energyict.dlms.ProtocolLink}.SN_REFERENCE or {@link com.energyict.dlms.ProtocolLink}.LN_REFERENCE
     *
     * @return {@link com.energyict.dlms.ProtocolLink}.SN_REFERENCE for short name or
     *         {@link com.energyict.dlms.ProtocolLink}.LN_REFERENCE for long name
     */
    public int getReference() {
        return 0;
    }

    /**
     * Getter for the {@link com.energyict.dlms.cosem.StoredValues} object
     *
     * @return the {@link com.energyict.dlms.cosem.StoredValues} object
     */
    public StoredValues getStoredValues() {
        return null;
    }

    @Override
    public TimeZone getTimeZone() {
        return super.getTimeZone();    //To change body of overridden methods use File | Settings | File Templates.
    }

    //*******************************************************************************************
    //    M e s s a g e P r o t o c o l  i n t e r f a c e
    //     * @throws IOException
    //     * @throws SQLException
    //     * @throws BusinessException
    //*******************************************************************************************/
    public LegacyLoadProfileRegisterMessageBuilder getLoadProfileRegisterMessageBuilder() {
        return getMessageProtocol().getLoadProfileRegisterMessageBuilder();
    }

    public IskraMx372Messaging getMessageProtocol() {
        if (messageProtocol == null) {
            messageProtocol = new IskraMx372Messaging(this, propertySpecService, calendarFinder, extractor, messageFileExtractor);
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
    public void applyMessages(List messageEntries) throws IOException {
        getMessageProtocol().applyMessages(messageEntries);
    }

    /**
     * Indicates that each message has to be executed by the protocol.
     *
     * @param messageEntry a definition of which message needs to be sent
     * @return a state of the message which was just sent
     * @throws java.io.IOException if a logical error occurs
     */
    public MessageResult queryMessage(MessageEntry messageEntry) throws IOException {
        if (!properties.madeCSDCall()) {
            return getMessageProtocol().queryMessage(messageEntry);
        } else {
            throw new IOException("Execution of device messages not allowed in the CSD schedule. Please correct this first.");
        }
    }

    public List<MessageCategorySpec> getMessageCategories() {
        return getMessageProtocol().getMessageCategories();
    }

    public String writeMessage(Message msg) {
        return getMessageProtocol().writeMessage(msg);
    }

    public String writeTag(MessageTag tag) {
        return getMessageProtocol().writeTag(tag);
    }

    public String writeValue(MessageValue value) {
        return getMessageProtocol().writeValue(value);
    }

    public LegacyPartialLoadProfileMessageBuilder getPartialLoadProfileMessageBuilder() {
        return getMessageProtocol().getPartialLoadProfileMessageBuilder();
    }

    /**
     * Based on the serial number, find out the physical address of the slave Mbus meter.
     * @param serialNumber
     * @return
     */
    public int getPhysicalAddressFromSerialNumber(String serialNumber) throws IOException {
        for (MbusDevice mbusDevice : messageProtocol.getMbusDevices()) {
            if (serialNumber.equalsIgnoreCase(mbusDevice.getCustomerID())) {
                return mbusDevice.getPhysicalAddress();
            }
        }
        throw new IOException("No slave device with serial number " + serialNumber + " found!");
    }

    /**
     * Based on the physical address of the mbus slave, find out the serial number of the slave Mbus meter.
     * @param physicalAddress
     * @return
     */
    public String getSerialNumberFromPhysicalAddress(int physicalAddress) {
        for (MbusDevice mbusDevice : messageProtocol.getMbusDevices()) {
            if ((mbusDevice != null) && (mbusDevice.getPhysicalAddress() == physicalAddress)) {
                return mbusDevice.getCustomerID();
            }
        }
        return null;
    }

    public boolean hasBreaker() {
        return hasBreaker;
    }

    /**
     * Setter is only called from the cryptoserver protocols to remove the breaker functionality
     */
    public void setHasBreaker(boolean hasBreaker) {
        this.hasBreaker = hasBreaker;
    }

    @Override
    public String getSerialNumber() {
        return getMeterSerialNumber();
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        return getProperties().getUPLPropertySpecs();
    }
}
