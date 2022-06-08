package com.energyict.smartmeterprotocolimpl.actaris.sl7000;

import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dlms.DLMSCOSEMGlobals;
import com.energyict.dlms.DLMSCache;
import com.energyict.dlms.DLMSConnection;
import com.energyict.dlms.DLMSConnectionException;
import com.energyict.dlms.DLMSMeterConfig;
import com.energyict.dlms.DLMSReference;
import com.energyict.dlms.DataContainer;
import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.AXDRDecoder;
import com.energyict.dlms.axrdencoding.AxdrType;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.dlms.cosem.Data;
import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.dlms.cosem.StoredValues;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.mdc.upl.SerialNumberSupport;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileFinder;
import com.energyict.mdc.upl.messages.legacy.Message;
import com.energyict.mdc.upl.messages.legacy.MessageCategorySpec;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.MessageTag;
import com.energyict.mdc.upl.messages.legacy.MessageValue;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
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
import com.energyict.protocolimpl.dlms.common.AbstractSmartDlmsProtocol;
import com.energyict.smartmeterprotocolimpl.actaris.sl7000.composedobjects.ComposedMeterInfo;
import com.energyict.smartmeterprotocolimpl.actaris.sl7000.messaging.Messages;

import java.io.IOException;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

/**
 * Copyrights EnergyICT
 * User: sva
 * Date: 17/07/12
 * Time: 16:41
 */
public class ActarisSl7000 extends AbstractSmartDlmsProtocol implements ProtocolLink, MessageProtocol, SerialNumberSupport {

    private final PropertySpecService propertySpecService;
    private final DeviceMessageFileFinder messageFileFinder;
    private final DeviceMessageFileExtractor deviceMessageFileExtractor;
    /**
     * Contains properties related to the Actaris SL7000 protocol
     */
    private SL7000Properties properties;
    /**
     * Contains information about the meter (serialNumber, firmwareVersion,...)
     */
    private ComposedMeterInfo meterInfo;
    /**
     * The serial number of the device
     */
    private String meterSerial;
    /**
     * Boolean indicating whether or not this device has an old type of firmware.
     * In this case, some commands (for example the MAC association release) are not supported.
     */
    private boolean oldFirmware;
    /**
     * The LoadProfileBuilder, used for fetching the LoadProfileConfiguration and reading of the LoadProfiles.
     */
    private LoadProfileBuilder loadProfileBuilder;
    /**
     * The RegisterReader, used to read out the meters registers.
     */
    private RegisterReader registerReader;
    private StoredValuesImpl storedValues;
    private Messages messageProtocol;

    public ActarisSl7000(PropertySpecService propertySpecService, DeviceMessageFileFinder messageFileFinder, DeviceMessageFileExtractor deviceMessageFileExtractor) {
        this.propertySpecService = propertySpecService;
        this.messageFileFinder = messageFileFinder;
        this.deviceMessageFileExtractor = deviceMessageFileExtractor;
    }

    @Override
    protected SL7000Properties getProperties() {
        if (properties == null) {
            properties = new SL7000Properties(this.propertySpecService);
        }
        return properties;
    }

    /**
     * Make a connection to the physical device.
     * Setup the association and check the objectList
     *
     * @throws java.io.IOException if errors occurred during data fetching
     */
    @Override
    public void connect() throws IOException {
        getDlmsSession().setUseLegacyHDLCConnection(getProperties().getUseLegacyHDLCConnection());
        getDlmsSession().connect();
        checkCacheObjects();
        initAfterConnect();
    }

    /**
     * Method to check whether the cache needs to be read out or not, if so the read will be forced
     */
    @Override
    protected void checkCacheObjects() throws IOException {
        boolean readCache = getProperties().isReadCache();
        if (readCache) {
            getLogger().info("ReReadCache property is true, reading cache!");
            requestConfiguration();
            getCache().saveObjectList(getDlmsSession().getMeterConfig().getInstantiatedObjectList());
        } else {
            super.checkCacheObjects();
        }
    }

    @Override
    public DLMSCache getCache() {
        DLMSCache deviceCache = (DLMSCache) super.getCache();
        if (deviceCache == null) {
            deviceCache = new DLMSCache();
            setCache(deviceCache);
        }
        return deviceCache;
    }

    @Override
    public void setCache(Serializable deviceProtocolCache) {
        if ((deviceProtocolCache != null) && (deviceProtocolCache instanceof DLMSCache)) {
            DLMSCache dlmsCache = (DLMSCache) deviceProtocolCache;
            super.setCache(dlmsCache);
        }
    }

    @Override
    protected void initAfterConnect() throws ConnectionException {
    }

    @Override
    public void disconnect() throws IOException {
        try {
            if (getDlmsSession().getDLMSConnection() != null && !isOldFirmware()) {
                getDlmsSession().getDLMSConnection().disconnectMAC();
            }
        } catch (IOException | DLMSConnectionException e) {
            getLogger().log(Level.FINEST, e.getMessage());
        }
    }

    private boolean isClockSetAllowed(Date currentMeterTime, Date newMeterTime, int intervalInSeconds) {
        if (Math.abs(currentMeterTime.getTime()-newMeterTime.getTime()) > 300000) {
            // Assume this is a Force Clock since the time difference > 300 seconds
            return true;
        }
        long intervalInMilliseconds = intervalInSeconds*1000L;
        long meterTimeStartOfInterval = (currentMeterTime.getTime()/intervalInMilliseconds)*intervalInMilliseconds;
        long clockTimeStartOfInterval = (newMeterTime.getTime()/intervalInMilliseconds)*intervalInMilliseconds;
        return meterTimeStartOfInterval==clockTimeStartOfInterval;
    }

    @Override
    public void setTime(Date newMeterTime) throws IOException {
        try {
            Date meterTime = getTime();
            if (isClockSetAllowed(meterTime, newMeterTime, 900)) {
                doSetTime(newMeterTime);
            }
            else {
                getLogger().log(Level.INFO, "Clock set Rejected, crossing interval boundary");
            }
        } catch (IOException e) {
            getLogger().log(Level.FINEST, e.getMessage());
            throw new IOException("Could not set the Clock object." + e);
        }
    }

    private void doSetTime(Date newMeterTime) throws IOException {
        Calendar calendar = Calendar.getInstance(getTimeZone());
        calendar.setTime(newMeterTime);

        byte[] byteTimeBuffer = new byte[14];
        byteTimeBuffer[0] = AxdrType.OCTET_STRING.getTag();
        byteTimeBuffer[1] = 12;
        byteTimeBuffer[2] = (byte) (calendar.get(Calendar.YEAR) >> 8);
        byteTimeBuffer[3] = (byte) calendar.get(Calendar.YEAR);
        byteTimeBuffer[4] = (byte) (calendar.get(Calendar.MONTH) + 1);
        byteTimeBuffer[5] = (byte) calendar.get(Calendar.DAY_OF_MONTH);
        byte bDOW = (byte) calendar.get(Calendar.DAY_OF_WEEK);
        byteTimeBuffer[6] = bDOW-- == 1 ? (byte) 7 : bDOW;
        byteTimeBuffer[7] = (byte) calendar.get(Calendar.HOUR_OF_DAY);
        byteTimeBuffer[8] = (byte) calendar.get(Calendar.MINUTE);
        byteTimeBuffer[9] = (byte) calendar.get(Calendar.SECOND);
        byteTimeBuffer[10] = (byte) 0x00;
        byteTimeBuffer[11] = (byte) 0x00;
        byteTimeBuffer[12] = (byte) 0x00;
        byteTimeBuffer[13] = (getTimeZone().inDaylightTime(calendar.getTime()) ? (byte) 0x80 : (byte) 0x00);

        AXDRDateTime axdrDateTime = new AXDRDateTime(byteTimeBuffer);
        this.dlmsSession.getCosemObjectFactory().getClock().setAXDRDateTimeAttr(axdrDateTime);
    }

    @Override
    public String getFirmwareVersion() throws IOException {
        return getMeterInfo().getFirmwareVersion();
    }

    @Override
    public String getMeterSerialNumber() {
        Data data;
        try {
            oldFirmware = true;
            data = getDlmsSession().getCosemObjectFactory().getData(ObisCodeMapper.OBISCODE_SERIAL_NUMBER_OBJ2);
            return AXDRDecoder.decode(data.getRawValueAttr()).getVisibleString().getStr().trim();
        } catch (IOException e) {
            throw DLMSIOExceptionHandler.handle(e, getDlmsSession().getProperties().getRetries() + 1);
        }
    }

    public RegisterInfo translateRegister(Register register) throws IOException {
        return ObisCodeMapper.getRegisterInfo(register.getObisCode());
    }

    @Override
    public List<RegisterValue> readRegisters(List<Register> registers) throws IOException {
        return getRegisterReader().readRegisters(registers);
    }

    @Override
    public List<LoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfilesToRead) throws IOException {
        return getLoadProfileBuilder().fetchLoadProfileConfiguration(loadProfilesToRead);
    }

    @Override
    public List<ProfileData> getLoadProfileData(List<LoadProfileReader> loadProfiles) throws IOException {
        return getLoadProfileBuilder().getLoadProfileData(loadProfiles);
    }

    @Override
    public List<MeterEvent> getMeterEvents(Date lastLogbookDate) throws IOException {
        Logbook logbook = new Logbook(getTimeZone(), getFirmwareVersion());
        ProfileGeneric profileGeneric = getDlmsSession().getCosemObjectFactory().getProfileGeneric(ObisCode.fromByteArray(DLMSCOSEMGlobals.LOGBOOK_PROFILE_LN));
        DataContainer dc = profileGeneric.getBuffer();  // Selective access not supported - the whole buffer (max. 500 elements) will be read out!
        return logbook.getMeterEvents(dc);
    }

    @Override
    public String getProtocolDescription() {
        return "Itron SL7000 DLMS";
    }

    @Override
    public String getVersion() {
        return "$Date: 2022-06-08 14:24:00 +0300 (Wed, 08 Jun 2022)$";
    }

    public ComposedMeterInfo getMeterInfo() {
        if (meterInfo == null) {
            meterInfo = new ComposedMeterInfo(getDlmsSession(), supportsBulkRequests());
        }
        return meterInfo;
    }

    public boolean isOldFirmware() {
        return oldFirmware;
    }

    private LoadProfileBuilder getLoadProfileBuilder() {
        if (loadProfileBuilder == null) {
            loadProfileBuilder = new LoadProfileBuilder(this);
        }
        return loadProfileBuilder;
    }

    private RegisterReader getRegisterReader() {
        if (registerReader == null) {
            registerReader = new RegisterReader(this);
        }
        return registerReader;
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
        return (getProperties().getRequestTimeZone() != 0);
    }

    @Override
    public int getRoundTripCorrection() {
        return getProperties().getRoundTripCorrection();
    }

    @Override
    public int getReference() {
        return DLMSReference.LN.getReference();
    }

    @Override
    public StoredValues getStoredValues() {
        if (storedValues == null) {
            storedValues = new StoredValuesImpl(this);
        }
        return storedValues;
    }

    @Override
    public void applyMessages(List<MessageEntry> messageEntries) throws IOException {
        getMessageProtocol().applyMessages(messageEntries);
    }

    @Override
    public MessageResult queryMessage(MessageEntry messageEntry) throws IOException {
        return getMessageProtocol().queryMessage(messageEntry);
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

    public Messages getMessageProtocol() {
        if (messageProtocol == null) {
            messageProtocol = new Messages(this, this.messageFileFinder, this.deviceMessageFileExtractor);
        }
        return messageProtocol;
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