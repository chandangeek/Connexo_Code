package com.energyict.smartmeterprotocolimpl.prenta.iskra.mx372;

import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.api.DeviceMessageFileService;
import com.energyict.mdc.protocol.api.LoadProfileConfiguration;
import com.energyict.mdc.protocol.api.LoadProfileReader;
import com.energyict.mdc.protocol.api.MessageProtocol;
import com.energyict.mdc.protocol.api.WakeUpProtocolSupport;
import com.energyict.mdc.protocol.api.device.LoadProfileFactory;
import com.energyict.mdc.protocol.api.device.data.MessageResult;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.device.data.Register;
import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;
import com.energyict.mdc.protocol.api.device.events.MeterEvent;
import com.energyict.mdc.protocol.api.dialer.connection.ConnectionException;
import com.energyict.mdc.protocol.api.dialer.core.Link;
import com.energyict.mdc.protocol.api.messaging.Message;
import com.energyict.mdc.protocol.api.messaging.MessageTag;
import com.energyict.mdc.protocol.api.messaging.MessageValue;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.protocols.mdc.services.impl.OrmClient;
import com.energyict.protocols.messaging.LegacyLoadProfileRegisterMessageBuilder;
import com.energyict.protocols.messaging.LegacyPartialLoadProfileMessageBuilder;
import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.dlms.DLMSConnection;
import com.energyict.dlms.DLMSMeterConfig;
import com.energyict.dlms.DLMSUtils;
import com.energyict.dlms.DataContainer;
import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.ScalerUnit;
import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.cosem.ActivityCalendar;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.StoredValues;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.dlms.common.AbstractSmartDlmsProtocol;
import com.energyict.protocolimpl.dlms.common.DLMSActivityCalendarController;
import com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties;
import com.energyict.smartmeterprotocolimpl.prenta.iskra.mx372.messaging.IskraMx372Messaging;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Clock;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * User: sva
 * Date: 19/01/12
 * Time: 16:21
 */
public class IskraMx372 extends AbstractSmartDlmsProtocol implements ProtocolLink, MessageProtocol, WakeUpProtocolSupport {

    @Override
    public String getProtocolDescription() {
        return "Iskraemeco Mx372 DLMS (PRE-NTA)";
    }

    private IskraMX372Properties properties;
    private String serialnr = null;
    private String devID = null;
    private CosemObjectFactory cosemObjectFactory;
    private LoadProfileBuilder loadProfileBuilder;
    private RegisterReader registerReader;

    private ObisCode deviceLogicalName = ObisCode.fromString("0.0.42.0.0.255");

    private IskraMx372Messaging messageProtocol;
    private final Clock clock;
    private final MdcReadingTypeUtilService readingTypeUtilService;
    private final TopologyService topologyService;
    private final LoadProfileFactory loadProfileFactory;
    private final DeviceMessageFileService deviceMessageFileService;

    public static ScalerUnit[] demandScalerUnits = {new ScalerUnit(0, 30), new ScalerUnit(0, 255), new ScalerUnit(0, 255), new ScalerUnit(0, 255), new ScalerUnit(0, 255)};
    private static final int ELECTRICITY = 0x00;
    private static final int MBUS = 0x01;

    @Inject
    public IskraMx372(PropertySpecService propertySpecService, OrmClient ormClient, Clock clock, MdcReadingTypeUtilService readingTypeUtilService, TopologyService topologyService, LoadProfileFactory loadProfileFactory, DeviceMessageFileService deviceMessageFileService) {
        super(propertySpecService, ormClient);
        this.clock = clock;
        this.readingTypeUtilService = readingTypeUtilService;
        this.topologyService = topologyService;
        this.loadProfileFactory = loadProfileFactory;
        this.deviceMessageFileService = deviceMessageFileService;
    }

    @Override
    public DlmsProtocolProperties getProperties() {
        if (this.properties == null) {
            this.properties = new IskraMX372Properties();
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
        validateSerialNumber();
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

    private void validateSerialNumber() throws ConnectionException {
        if ((getProperties().getSerialNumber() == null) || ("".compareTo(getProperties().getSerialNumber()) == 0)) {
            return;
        }
        String sn = getSerialNumber();
        if ((sn != null) && (sn.compareTo(getProperties().getSerialNumber()) == 0)) {
            return;
        }
        throw new ConnectionException("SerialNumber mismatch! meter sn=" + sn + ", configured sn=" + getProperties().getSerialNumber());
    }

     public String getSerialNumber() throws ConnectionException {
        if (serialnr == null) {
            try {
            UniversalObject uo = getMeterConfig().getSerialNumberObject();
            serialnr = getCosemObjectFactory().getGenericRead(uo).getString();
            } catch (IOException e) {
                throw new ConnectionException(e.getMessage());
            }
        }
         return serialnr;
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

    public void searchForSlaveDevices() throws ConnectionException {
        try {
            messageProtocol.checkMbusDevices();
        } catch (Exception e) {
            throw new ConnectionException("Got an error while loading the attached Slave devices: "+e.getMessage());
        }
    }


    public String getFirmwareVersion() throws IOException {
        return ((IskraMX372Properties)getProperties()).getFirmwareVersion();
    }

    public String getMeterSerialNumber() throws IOException {
        if (!properties.madeCSDCall()) {
            if (serialnr == null) {
                UniversalObject uo = getMeterConfig().getSerialNumberObject();
                serialnr = getCosemObjectFactory().getGenericRead(uo).getString();
            }
            return serialnr;
        } else {
            return getProperties().getSerialNumber();
        }
    }

    public CosemObjectFactory getCosemObjectFactory() {
        return cosemObjectFactory;
    }

    public RegisterInfo translateRegister(Register register) throws IOException {
        return ObisCodeMapper.getRegisterInfo(register.getObisCode());
    }

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

    public List<LoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfilesToRead) throws IOException {
        if (!properties.madeCSDCall()) {
            List<LoadProfileConfiguration> loadProfileConfigurations = getLoadProfileBuilder().fetchLoadProfileConfiguration(loadProfilesToRead);
            return loadProfileConfigurations;
        } else {
            throw new IOException("Reading of demand values not allowed in the CSD schedule. Please correct this first.");
        }
    }

    public List<ProfileData> getLoadProfileData(List<LoadProfileReader> loadProfiles) throws IOException {
        if (!properties.madeCSDCall()) {
            return getLoadProfileBuilder().getLoadProfileData(loadProfiles);
        } else {
            throw new IOException("Reading of demand values not allowed in the CSD schedule. Please correct this first.");
        }
    }

    private LoadProfileBuilder getLoadProfileBuilder() {
        if (loadProfileBuilder == null) {
            loadProfileBuilder = new LoadProfileBuilder(this, this.readingTypeUtilService);
        }
        return loadProfileBuilder;
    }

    public String getVersion() {
        return "$Date: 2014-06-02 13:26:25 +0200 (Mon, 02 Jun 2014) $";
    }

    @Override
    public Date getTime() throws IOException {
        if (!properties.madeCSDCall()) {
            return super.getTime();
        } else {
            return new Date();
        }
    }

    public DLMSConnection getDLMSConnection() {
        return getDlmsSession().getDLMSConnection();
    }

    public DLMSMeterConfig getMeterConfig() {
        return getDlmsSession().getMeterConfig();
    }

    public boolean isRequestTimeZone() {
        return (((IskraMX372Properties) getProperties()).getRequestTimeZone() == 1) ? true : false;
    }

    public int getRoundTripCorrection() {
        return getProperties().getRoundTripCorrection();
    }

    public int getReference() {
        return 0;
    }

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
    //*******************************************************************************************/
    public LegacyLoadProfileRegisterMessageBuilder getLoadProfileRegisterMessageBuilder() {
        return getMessageProtocol().getLoadProfileRegisterMessageBuilder();
    }

    public IskraMx372Messaging getMessageProtocol() {
        if (messageProtocol == null) {
            messageProtocol = new IskraMx372Messaging(this, clock, this.topologyService, readingTypeUtilService, loadProfileFactory, deviceMessageFileService);
        }
        return messageProtocol;
    }

    public void applyMessages(List messageEntries) throws IOException {
        getMessageProtocol().applyMessages(messageEntries);
    }

    public MessageResult queryMessage(MessageEntry messageEntry) throws IOException {
        if (!properties.madeCSDCall()) {
            return getMessageProtocol().queryMessage(messageEntry);
        } else {
            throw new IOException("Execution of device messages not allowed in the CSD schedule. Please correct this first.");
        }
    }

    public List getMessageCategories() {
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

    public boolean executeWakeUp(int communicationSchedulerId, Link link, Logger logger) throws IOException {
        return getMessageProtocol().executeWakeUp(communicationSchedulerId, link, logger);
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
    public String getSerialNumberFromPhysicalAddress(int physicalAddress) throws IOException {
        for (MbusDevice mbusDevice : messageProtocol.getMbusDevices()) {
            if ((mbusDevice != null) && (mbusDevice.getPhysicalAddress() == physicalAddress)) {
                return mbusDevice.getCustomerID();
            }
        }
        return null;
    }

    @Override
    public Optional<String> getActiveCalendarName() throws IOException {
        ActivityCalendar activityCalendar = this.getDlmsSession().getCosemObjectFactory().getActivityCalendar(DLMSActivityCalendarController.ACTIVITY_CALENDAR_OBISCODE);
        String calendarName = activityCalendar.readCalendarNameActive().stringValue();
        if (calendarName != null && !calendarName.isEmpty()) {
            return Optional.of(calendarName);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<String> getPassiveCalendarName() throws IOException {
        ActivityCalendar activityCalendar = this.getDlmsSession().getCosemObjectFactory().getActivityCalendar(DLMSActivityCalendarController.ACTIVITY_CALENDAR_OBISCODE);
        String calendarName = activityCalendar.readCalendarNamePassive().stringValue();
        if (calendarName != null && !calendarName.isEmpty()) {
            return Optional.of(calendarName);
        } else {
            return Optional.empty();
        }
    }

}