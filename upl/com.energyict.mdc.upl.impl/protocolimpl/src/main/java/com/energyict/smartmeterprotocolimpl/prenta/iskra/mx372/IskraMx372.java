package com.energyict.smartmeterprotocolimpl.prenta.iskra.mx372;

import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileFinder;
import com.energyict.mdc.upl.messages.legacy.Message;
import com.energyict.mdc.upl.messages.legacy.MessageCategorySpec;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.MessageTag;
import com.energyict.mdc.upl.messages.legacy.MessageValue;
import com.energyict.mdc.upl.messages.legacy.NumberLookupExtractor;
import com.energyict.mdc.upl.messages.legacy.NumberLookupFinder;
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
import com.energyict.obis.ObisCode;
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
import com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties;
import com.energyict.protocolimpl.utils.ProtocolUtils;
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

    private static final int ELECTRICITY = 0x00;
    private static final int MBUS = 0x01;
    public static ScalerUnit[] demandScalerUnits = {new ScalerUnit(0, 30), new ScalerUnit(0, 255), new ScalerUnit(0, 255), new ScalerUnit(0, 255), new ScalerUnit(0, 255)};
    private final PropertySpecService propertySpecService;
    private final TariffCalendarFinder calendarFinder;
    private final TariffCalendarExtractor extractor;
    private final DeviceMessageFileExtractor messageFileExtractor;
    private final DeviceMessageFileFinder deviceMessageFileFinder;
    private final NumberLookupFinder numberLookupFinder;
    private final NumberLookupExtractor numberLookupExtractor;
    private IskraMX372Properties properties;
    private String serialnr = null;
    private String devID = null;
    private CosemObjectFactory cosemObjectFactory;
    private LoadProfileBuilder loadProfileBuilder;
    private RegisterReader registerReader;
    private ObisCode deviceLogicalName = ObisCode.fromString("0.0.42.0.0.255");
    private IskraMx372Messaging messageProtocol;
    /**
     * Indicating if the meter has a breaker.
     * This implies whether or not we can control the breaker and read the control logbook.
     * This will be set to false in the cryptoserver protocols, because these meters don't have a breaker anymore.
     */
    private boolean hasBreaker = true;

    public IskraMx372(PropertySpecService propertySpecService, TariffCalendarFinder calendarFinder, TariffCalendarExtractor extractor, DeviceMessageFileExtractor messageFileExtractor, DeviceMessageFileFinder deviceMessageFileFinder, NumberLookupFinder numberLookupFinder, NumberLookupExtractor numberLookupExtractor) {
        this.propertySpecService = propertySpecService;
        this.calendarFinder = calendarFinder;
        this.extractor = extractor;
        this.messageFileExtractor = messageFileExtractor;
        this.deviceMessageFileFinder = deviceMessageFileFinder;
        this.numberLookupFinder = numberLookupFinder;
        this.numberLookupExtractor = numberLookupExtractor;
    }

    public static ScalerUnit getScalerUnit(ObisCode obisCode) {

        if (obisCode.toString().indexOf("1.0") == 0) {
            return demandScalerUnits[ELECTRICITY];
        } else {
            return demandScalerUnits[MBUS];
        }
    }

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

    @Override
    public void connect() throws IOException {
        if (!properties.madeCSDCall()) {
            super.connect();
        }
    }

    @Override
    public void disconnect() throws IOException {
        if (!properties.madeCSDCall()) {
            super.disconnect();
        }
    }

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
     */
    public void searchForSlaveDevices() throws ConnectionException {
        try {
            messageProtocol.checkMbusDevices();
        } catch (Exception e) {
            throw new ConnectionException("Got an error while loading the attached Slave devices: " + e.getMessage());
        }
    }

    @Override
    public String getFirmwareVersion() throws IOException {
        return ((IskraMX372Properties) getProperties()).getFirmwareVersion();
    }

    @Override
    public String getMeterSerialNumber() {
        try {
            if (!properties.madeCSDCall()) {
                UniversalObject uo = getMeterConfig().getSerialNumberObject();
                return getCosemObjectFactory().getGenericRead(uo).getString();
            } else {
                return getProperties().getSerialNumber();
            }
        } catch (IOException e) {
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

    @Override
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

    @Override
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

    @Override
    public List<LoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfilesToRead) throws IOException {
        if (!properties.madeCSDCall()) {
            return getLoadProfileBuilder().fetchLoadProfileConfiguration(loadProfilesToRead);
        } else {
            throw new IOException("Reading of demand values not allowed in the CSD schedule. Please correct this first.");
        }
    }

    @Override
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

    @Override
    public String getProtocolDescription() {
        return "Iskraemeco Mx372 DLMS (PRE-NTA)";
    }

    @Override
    public String getVersion() {
        return "$Date: 2015-11-26 15:25:15 +0200 (Thu, 26 Nov 2015)$";
    }

    @Override
    public Date getTime() throws IOException {
        if (!properties.madeCSDCall()) {
            return super.getTime();
        } else {
            return new Date();
        }
    }

    @Override
    public DLMSConnection getDLMSConnection() {
        return getDlmsSession().getDLMSConnection();
    }

    @Override
    public DLMSMeterConfig getMeterConfig() {
        return getDlmsSession().getMeterConfig();
    }

    @Override
    public boolean isRequestTimeZone() {
        return (((IskraMX372Properties) getProperties()).getRequestTimeZone() == 1);
    }

    @Override
    public int getRoundTripCorrection() {
        return getProperties().getRoundTripCorrection();
    }

    @Override
    public int getReference() {
        return 0;
    }

    @Override
    public StoredValues getStoredValues() {
        return null;
    }

    @Override
    public TimeZone getTimeZone() {
        return super.getTimeZone();    //To change body of overridden methods use File | Settings | File Templates.
    }

    public IskraMx372Messaging getMessageProtocol() {
        if (messageProtocol == null) {
            messageProtocol = new IskraMx372Messaging(this, propertySpecService, calendarFinder, extractor, messageFileExtractor, deviceMessageFileFinder, numberLookupFinder, numberLookupExtractor);
        }
        return messageProtocol;
    }

    @Override
    public void applyMessages(List messageEntries) throws IOException {
        getMessageProtocol().applyMessages(messageEntries);
    }

    @Override
    public MessageResult queryMessage(MessageEntry messageEntry) throws IOException {
        if (!properties.madeCSDCall()) {
            return getMessageProtocol().queryMessage(messageEntry);
        } else {
            throw new IOException("Execution of device messages not allowed in the CSD schedule. Please correct this first.");
        }
    }

    @Override
    public List<MessageCategorySpec> getMessageCategories() {
        return getMessageProtocol().getMessageCategories();
    }

    @Override
    public String writeMessage(Message msg) {
        return getMessageProtocol().writeMessage(msg);
    }

    @Override
    public String writeTag(MessageTag tag) {
        return getMessageProtocol().writeTag(tag);
    }

    @Override
    public String writeValue(MessageValue value) {
        return getMessageProtocol().writeValue(value);
    }

    /**
     * Based on the serial number, find out the physical address of the slave Mbus meter.
     *
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
     *
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