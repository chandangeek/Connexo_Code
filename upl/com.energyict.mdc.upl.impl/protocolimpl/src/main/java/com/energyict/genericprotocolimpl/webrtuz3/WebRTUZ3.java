package com.energyict.genericprotocolimpl.webrtuz3;

import com.energyict.cbo.BusinessException;
import com.energyict.cpo.Environment;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dlms.*;
import com.energyict.dlms.aso.*;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.cosem.Data;
import com.energyict.dlms.cosem.StoredValues;
import com.energyict.genericprotocolimpl.common.*;
import com.energyict.genericprotocolimpl.webrtu.common.obiscodemappers.ObisCodeMapper;
import com.energyict.genericprotocolimpl.webrtukp.WebRTUKP;
import com.energyict.genericprotocolimpl.webrtuz3.composedobjects.ComposedMeterInfo;
import com.energyict.genericprotocolimpl.webrtuz3.messagehandling.MessageExecutor;
import com.energyict.genericprotocolimpl.webrtuz3.profiles.*;
import com.energyict.mdw.amr.Register;
import com.energyict.mdw.core.*;
import com.energyict.mdw.shadow.CommunicationProtocolShadow;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;
import com.energyict.protocol.messaging.MessageCategorySpec;
import com.energyict.protocol.messaging.MessageSpec;
import com.energyict.protocolimpl.base.CachedMeterTime;
import com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties;
import com.energyict.protocolimpl.messages.*;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;

/**
 * <p>
 * Implements the WebRTUZ3 protocol. Initially it's a copy of the {@link WebRTUKP} protocol,
 * but with more extensions to it.
 * </p>
 *
 * @author gna
 */
public class WebRTUZ3 extends DLMSProtocol implements EDevice {

    /**
     * Indicates whether specific messages are allowed for the firmwareTeam
     */
    private static final boolean FIRMWAREBUILD = false;

    /**
     * Property names
     */
    private static final String PROPERTY_PASSWORD = "Password";
    private static final String PROPERTY_DELAY_AFTER_FAIL = "DelayAfterFail";
    private static final String PROPERTY_REQUEST_TIME_ZONE = "RequestTimeZone";
    private static final String PROPERTY_FIRMWARE_VERSION = "FirmwareVersion";
    private static final String PROPERTY_EXTENDED_LOGGING = "ExtendedLogging";
    private static final String PROPERTY_TEST_LOGGING = "TestLogging";
    private static final String PROPERTY_READ_DAILY_VALUES = "ReadDailyValues";
    private static final String PROPERTY_READ_MONTHLY_VALUES = "ReadMonthlyValues";
    private static final String PROPERTY_FOLDER_EXT_NAME = "FolderExtName";
    private static final String PROPERTY_EMETER_RTUTYPE = "EMeterRtuType";
    private static final String PROPERTY_MBUS_RTUTYPE = "MBusRtuType";
    private static final String PROPERTY_RUNTESTMETHOD = "RunTestMethod";


    /**
     * Property default values
     */
    private static final String DEFAULT_READ_DAILY_VALUES = "1";
    private static final String DEFAULT_READ_MONTHLY_VALUES = "1";
    private static final String DEFAULT_REQUEST_TIMEZONE = "0";
    private static final String DEFAULT_PASSWORD = "";
    private static final String DEFAULT_RUNTESTMETHOD = "0";

    /**
     * Device channel mappings
     */
    public static final DeviceMappingRange MBUS_DEVICES = new DeviceMappingRange(0x01, 0x20);
    public static final DeviceMappingRange EMETER_DEVICES = new DeviceMappingRange(0x21, 0x40);

    /**
     * The device obisCodes
     */
    public static final ObisCode SERIALNR_OBISCODE = ObisCode.fromString("0.0.96.1.0.255");
    public static final ObisCode LOGGER_PROFILE_BASE_OBISCODE = ObisCode.fromString("0.0.99.1.0.255");

    /**
     * The serialNumber of the Device
     */
    private String serialNumber;

    /**
     * The password of the Device
     */
    private String password;

    /**
     * The prototype {@link com.energyict.mdw.core.DeviceType} of the mbus devices
     */
    private String mbusRtuType;

    /**
     * The prototype {@link com.energyict.mdw.core.DeviceType} of the eMeter
     */
    private String eMeterRtuType;

    /**
     * The external name of the folder where to place the autodiscovered MbusMeters
     */
    private String folderExtName;

    /**
     * Property to indicate to read the timeZone from the device or use the one configured on the Device
     */
    private int requestTimeZone;

    /**
     * Property to indicate the timedifference between System and device is larger then the maximum configured
     */
    private boolean badTime = false;

    /**
     * Property to allow reading the daily values
     */
    private boolean readDaily = true;

    /**
     * Property to allow reading the monthly values
     */
    private boolean readMonthly = true;

    /**
     * An array of 'slave' MbusDevices
     */
    private MbusDevice[] mbusDevices;

    /**
     * An array of 'slave' EMeters
     */
    private EMeter[] eMeters;

    /**
     * The used TicDevice
     */
    private TicDevice ticDevice;

    /**
     * The {@link StoreObject} used
     */
    private StoreObject storeObject;

    /**
     * The {@link ObisCodeMapper} used
     */
    private WebRtuZ3ObisCodeMapper ocm;
    private ComposedMeterInfo meterInfo;

    private boolean runTestMethod = false;
    private CachedMeterTime cachedMeterTime = null;

    @Override
    protected void doExecute() throws BusinessException, SQLException, IOException {
//
//        try {
//
//            testMethod();
//
//            // Check if the time is greater then allowed, if so then no data can be stored...
//            // Don't do this when a forceClock is scheduled
//            if (!getCommunicationScheduler().getCommunicationProfile().getForceClock() && !getCommunicationScheduler().getCommunicationProfile().getAdHoc()) {
//                badTime = verifyMaxTimeDifference();
//            }
//
//            // Read the events
//            if (getCommunicationProfile().getReadMeterEvents()) {
//                getLogger().log(Level.INFO, "Getting events for meter with serialnumber: " + this.serialNumber);
//                EMeterEventProfile evp = new EMeterEventProfile(this);
//                ProfileData pd = evp.getEvents();
//                storeObject.add(pd, getMeter());
//            }
//
//            // Read the register values
//            if (getCommunicationProfile().getReadMeterReadings()) {
//                getLogger().log(Level.INFO, "Getting registers for meter with serialnumber: " + this.serialNumber);
//                Map<Register, RegisterValue> registerMap = doReadRegisters();
//                storeObject.addAll(registerMap);
//            }
//
//            // Read the muc/z3 profiles (temp, DI/DO, ...)
//            if (getCommunicationProfile().getReadDemandValues()) {
//                getLogger().log(Level.INFO, "Getting profile data for meter with serialnumber: " + this.serialNumber);
//                int profileIndex = 0;
//                for (int i = 0; i <= 255; i++) {
//                    ObisCode profileObisCode = ProtocolTools.setObisCodeField(LOGGER_PROFILE_BASE_OBISCODE, 4, (byte) i);
//                    if (getMeterConfig().isObisCodeInObjectList(profileObisCode)) {
//                        ProfileData profileData = readChannels(profileIndex, profileObisCode);
//                        storeObject.add(profileData, getMeter());
//                        profileIndex += profileData.getNumberOfChannels();
//                    }
//                }
//            }
//
//            //Send the meter messages
//            if (getCommunicationProfile().getSendRtuMessage()) {
//                sendMeterMessages();
//            }
//
//            // Discover and handle MbusMeters
//            discoverMbusDevices();
//            if (getValidMbusDevices() != 0) {
//                getLogger().log(Level.INFO, "Starting to handle the MBus meters.");
//                handleMbusMeters();
//            }
//
//            // Discover and handle MbusMeters
//            discoverEMeters();
//            if (getValidEMeters() != 0) {
//                getLogger().log(Level.INFO, "Starting to handle the Emeters.");
//                handleEmeters();
//            }
//
//            // Check for TIC devices and if there is one handle it
//            if (hasTicDevices()) {
//                getLogger().log(Level.INFO, "Starting to handle the Tic device.");
//                handleTicDevice();
//            }
//
//            // Set clock or Force clock... if necessary
//            if (getCommunicationProfile().getForceClock()) {
//                Date meterTime = getTime();
//                Date currentTime = Calendar.getInstance(getTimeZone()).getTime();
//                setTimeDifference(Math.abs(currentTime.getTime() - meterTime.getTime()));
//                getLogger().log(Level.INFO, "Forced to set meterClock to systemTime: " + currentTime);
//                forceClock(currentTime);
//            } else {
//                verifyAndWriteClock();
//            }
//
//        } finally {
//            if (storeObject != null) {
//                Environment.getDefault().execute(storeObject);
//            }
//        }

    }

    private ProfileData readChannels(int firstChannelIndex, ObisCode profileObisCode) {
        Calendar fromCalendar = getChannelFromCalendar(firstChannelIndex);
        try {
            if (fromCalendar == null) {
                getLogger().log(Level.WARNING, "Unable to get lastReading for channel with index [" + firstChannelIndex + "]! Skipping channel.");
            } else if (!getMeterConfig().isObisCodeInObjectList(profileObisCode)) {
                getLogger().log(Level.WARNING, "Profile with obisCode [" + profileObisCode.toString() + "] does not exist in device.");
            } else {
                getLogger().log(Level.WARNING, "Reading profile with obisCode [" + profileObisCode.toString() + "]");
                LoggerProfile loggerProfile = new LoggerProfile(firstChannelIndex, profileObisCode, getCosemObjectFactory());
                return loggerProfile.getProfileData(fromCalendar);

            }
        } catch (Exception e) {
            e.printStackTrace();
            getLogger().log(Level.WARNING, "Unable to read profile with obisCode [" + profileObisCode.toString() + "]: " + e.getMessage());
        }
        return new ProfileData();
    }

    private Calendar getChannelFromCalendar(int channelIndex) {
        List<Channel> channels = getMeter().getChannels();
        for (Channel channel : channels) {
            if (channel.getLoadProfileIndex() == channelIndex + 1) {
                Date lastReading = channel.getLastReading();
                if (lastReading == null) {
                    lastReading = com.energyict.genericprotocolimpl.common.ParseUtils.getClearLastMonthDate(channel.getRtu());
                }
                Calendar cal = ProtocolUtils.getCleanCalendar(getTimeZone());
                cal.setTime(lastReading);
                return cal;
            }
        }
        return null;
    }

    /**
     * Testmethod for debigging purposes, used to dump some data like the object list of the device.
     * This method will only run when the property "RunTestMethod" == "1"
     *
     * @throws IOException
     */
    private void testMethod() {
        if (isRunTestMethod()) {
            StringBuffer sb = new StringBuffer();
            sb.append("\r\n\r\n").append("ObjectList = ").append("\r\n");
            try {
                UniversalObject[] objects = getMeterConfig().getInstantiatedObjectList();
                for (int i = 0; i < objects.length; i++) {
                    UniversalObject object = objects[i];
                    sb.append(object.getDescription()).append("\r\n");
                }
            } catch (Exception e) {
                sb.append("\r\n");
                sb.append("An error occured while reading the objectList: ").append(e.getMessage());
            }
            getLogger().warning(sb.toString());
        }
    }

    @Override
    protected ConformanceBlock configureConformanceBlock() {
        return new ConformanceBlock(ConformanceBlock.DEFAULT_LN_CONFORMANCE_BLOCK);
    }

    @Override
    protected InvokeIdAndPriority configureInvokeIdAndPriority() {
        try {
            return buildDefaultInvokeIdAndPriority();
        } catch (DLMSConnectionException e) {
            log(Level.FINEST, e.getMessage());
            // if we can't get it, then return null so the default should be used
            return null;
        }
    }

    @Override
    protected XdlmsAse configureXdlmsAse() {
        return new XdlmsAse(null, true, -1, 6, configureConformanceBlock(), 1200);
    }

    @Override
    protected void doConnect() throws IOException {
        verifyMeterSerialNumber();
        log(Level.INFO, "FirmwareVersion: " + getFirmWareVersion());
        //check if RF-Firmware exists
        String rfFirmware = getRFFirmwareVersion();
        if (!rfFirmware.equalsIgnoreCase("")) {
            log(Level.INFO, "RF-FirmwareVersion: " + rfFirmware);
        }
    }

    @Override
    public int requestConfigurationChanges() throws IOException {
        return getMeterInfo().getConfigurationChanges();
    }

    /**
     * {@inheritDoc}
     *
     * @throws SQLException      during smsWakeup, if we couldn't clear the IP-address in the database
     * @throws BusinessException if a business error occurred
     */
    @Override
    protected void doInit() throws SQLException, BusinessException, IOException {
        this.mbusDevices = new MbusDevice[MBUS_DEVICES.getNumberOfDevices()];
        this.eMeters = new EMeter[EMETER_DEVICES.getNumberOfDevices()];
        this.storeObject = new StoreObject();
        this.ocm = new WebRtuZ3ObisCodeMapper(getCosemObjectFactory());
    }

    /**
     * Read the firmwareVersion from the device
     *
     * @return the firmwareVersion
     * @throws IOException if we couldn't get the version
     */
    private String getFirmWareVersion() throws IOException {
        try {
            return getMeterInfo().getFirmwareVersion();
        } catch (IOException e) {
            String message = "Could not fetch the firmwareVersion." + e.getMessage();
            log(Level.FINEST, message);
            throw new IOException(message);
        }
    }

    /**
     * Read the Z3/R2 RF-Firmwareversion
     *
     * @return the firmwareversion, if it's not available then return an empty string
     */
    private String getRFFirmwareVersion() {
        try {
            if (hasRfFirmwareVersion()) {
                return getMeterInfo().getRFFirmwareVersion();
            }
        } catch (IOException e) {
            log(Level.FINEST, "Unable to read the RFFirmwareVersion: " + e.getMessage());
        }
        return "";
    }

    public boolean hasRfFirmwareVersion() {
        return getMeterConfig().isObisCodeInObjectList(ComposedMeterInfo.RF_FIRMWAREVERSION.getObisCode());
    }

    /**
     * Checks if the serialnumber from the device matches the one configured in EIServer
     *
     * @throws IOException if it doesn't match
     */
    private void verifyMeterSerialNumber() throws IOException {
        String serial = getSerialNumber();
        if (!(this.serialNumber.equalsIgnoreCase("")) && (!this.serialNumber.equals(serial))) {
            throw new IOException("Wrong serialnumber, EIServer settings: " + this.serialNumber + " - Meter settings: " + serial);
        }
    }

    /**
     * Get the serialNumber from the device
     *
     * @return the serialnumber from the device
     * @throws IOException we couldn't read the serialnumber
     */
    public String getSerialNumber() throws IOException {
        try {
            return getMeterInfo().getSerialNr();
        } catch (IOException e) {
            log(Level.FINEST, e.getMessage());
            throw new IOException("Could not retrieve the serialnumber of the meter." + e);
        }
    }

    @Override
    protected void doDisconnect() {
    }

    @Override
    protected List<String> doGetOptionalKeys() {
        List<String> result = new ArrayList<String>();
        result.add(PROPERTY_DELAY_AFTER_FAIL);
        result.add(PROPERTY_REQUEST_TIME_ZONE);
        result.add(PROPERTY_FIRMWARE_VERSION);
        result.add(PROPERTY_EXTENDED_LOGGING);
        result.add(PROPERTY_TEST_LOGGING);
        result.add(PROPERTY_READ_DAILY_VALUES);
        result.add(PROPERTY_READ_MONTHLY_VALUES);
        result.add(PROPERTY_FOLDER_EXT_NAME);
        result.add(LocalSecurityProvider.DATATRANSPORTKEY);
        result.add(LocalSecurityProvider.DATATRANSPORT_AUTHENTICATIONKEY);
        result.add(LocalSecurityProvider.MASTERKEY);
        result.add(LocalSecurityProvider.NEW_GLOBAL_KEY);
        result.add(LocalSecurityProvider.NEW_AUTHENTICATION_KEY);
        result.add(LocalSecurityProvider.NEW_HLS_SECRET);
        result.add(PROPERTY_RUNTESTMETHOD);
        return result;
    }

    public List<String> getOptionalKeys() {
        List<String> optionalKeys = new ArrayList<String>();
        optionalKeys.add(DlmsProtocolProperties.TIMEOUT);
        optionalKeys.add("ForceDelay");
        optionalKeys.add("Retries");
        optionalKeys.add("Connection");
        optionalKeys.add("SecurityLevel");
        optionalKeys.add("ClientMacAddress");
        optionalKeys.add("InformationFieldSize");
        optionalKeys.add("LoadProfileId");
        optionalKeys.add("AddressingMode");
        optionalKeys.add("MaxMbusDevices");
        optionalKeys.add("IIAPInvokeId");
        optionalKeys.add("IIAPPriority");
        optionalKeys.add("IIAPServiceClass");
        optionalKeys.add("Manufacturer");
        optionalKeys.add("RoundTripCorrection");
        optionalKeys.add("IpPortNumber");
        optionalKeys.add("WakeUp");
        optionalKeys.add("CipheringType");
        optionalKeys.add("BulkRequest");
        optionalKeys.add("ServerMacAddress"); // this replaces the ServerUpperMacAddress and ServerLowerMacAddress
        List<String> protocolKeys = doGetOptionalKeys();
        if (protocolKeys != null) {
            optionalKeys.addAll(protocolKeys);
        }
        return optionalKeys;
    }

    @Override
    protected List<String> doGetRequiredKeys() {
        List<String> result = new ArrayList<String>();
        result.add(PROPERTY_MBUS_RTUTYPE);
        result.add(PROPERTY_EMETER_RTUTYPE);
        return result;
    }

    @Override
    protected void doValidateProperties() throws MissingPropertyException, InvalidPropertyException {

        if ((getMeter() != null) && (!getMeter().getSerialNumber().equals(""))) {
            this.serialNumber = getMeter().getSerialNumber();
        } else {
            this.serialNumber = "";
        }

//        if ((getMeter() != null) && (!getMeter().getPassword().equals(""))) {
//            this.password = getMeter().getPassword();
//        } else if (getMeter() == null) {
        // if you want you can get the password from a proper property or securityObject
            this.password = getProperties().getProperty(PROPERTY_PASSWORD, DEFAULT_PASSWORD);
//        }

        this.requestTimeZone = ProtocolTools.getPropertyAsInt(getProperties(), PROPERTY_REQUEST_TIME_ZONE, DEFAULT_REQUEST_TIMEZONE);
        this.readDaily = (ProtocolTools.getPropertyAsInt(getProperties(), PROPERTY_READ_DAILY_VALUES, DEFAULT_READ_DAILY_VALUES) == 1) ? true : false;
        this.readMonthly = (ProtocolTools.getPropertyAsInt(getProperties(), PROPERTY_READ_MONTHLY_VALUES, DEFAULT_READ_MONTHLY_VALUES) == 1) ? true : false;
        this.mbusRtuType = getProperties().getProperty(PROPERTY_MBUS_RTUTYPE);
        this.eMeterRtuType = getProperties().getProperty(PROPERTY_EMETER_RTUTYPE);
        this.folderExtName = getProperties().getProperty(PROPERTY_FOLDER_EXT_NAME);
        this.runTestMethod = (ProtocolTools.getPropertyAsInt(getProperties(), PROPERTY_RUNTESTMETHOD, DEFAULT_RUNTESTMETHOD) == 1) ? true : false;

    }

    /**
     * @return true if it's allowed to read the daily values
     */
    boolean isReadDaily() {
        return this.readDaily;
    }

    /**
     * @return true if it's allowed to read the monthly values
     */
    boolean isReadMonthly() {
        return this.readMonthly;
    }

    @Override
    public SecurityProvider getSecurityProvider() {
        if ((getMeter() != null) && (password != null)) {
            getProperties().put(MeterProtocol.PASSWORD, password);
        }
        LocalSecurityProvider lsp = new LocalSecurityProvider(getProperties());
        return lsp;
    }

    /**
     * {@inheritDoc}
     */
    public String getVersion() {
        return "$Date$";
    }

    /**
     * {@inheritDoc}
     */
    public int getReference() {
        return ProtocolLink.LN_REFERENCE;
    }

    //TODO to complete with a property value

    public int getRoundTripCorrection() {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    public StoredValues getStoredValues() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isRequestTimeZone() {
        return (this.requestTimeZone == 1) ? true : false;
    }

    /**
     * @return the connectionMode
     */
    public int getConnectionMode() {
        return super.getConnectionMode();
    }

    /**
     * @return the storeObject from the Z3
     */
    public StoreObject getStoreObject() {
        return this.storeObject;
    }

    /**
     * @return the badTime parameter. It's true if the timedifference exceeds the configured boundaries
     */
    public boolean isBadTime() {
        return badTime;
    }

    @Override
    protected void prepareBulkRegisterReading(Map<ObisCode, com.energyict.mdw.amr.Register> toRead) {
        WebRtuZ3BulkObisCodeMapper bulkOCM = new WebRtuZ3BulkObisCodeMapper(getCosemObjectFactory());
        for (ObisCode obisCode : toRead.keySet()) {
            bulkOCM.enableRegisterMapping(obisCode);
        }
        ocm = bulkOCM;
    }

    @Override
    protected RegisterValue readRegister(ObisCode obisCode) throws IOException {
        return getObisCodeMapper().getRegisterValue(obisCode);
    }

    private WebRtuZ3ObisCodeMapper getObisCodeMapper() {
        if (ocm == null) {
            ocm = new WebRtuZ3ObisCodeMapper(getCosemObjectFactory());
        }
        return ocm;
    }

    /**
     * Messages
     *
     * @throws SQLException      if a database access error occurs
     * @throws BusinessException if a business error occurs
     */
    private void sendMeterMessages() throws BusinessException, SQLException {

        MessageExecutor messageExecutor = new MessageExecutor(this);

        Iterator<OldDeviceMessage> it = getMeter().getOldPendingMessages().iterator();
        OldDeviceMessage rm = null;
        while (it.hasNext()) {
            rm = it.next();
            messageExecutor.doMessage(rm);
        }
    }

    /**
     * Discover Mbus devices
     *
     * @throws SQLException
     * @throws BusinessException
     * @throws IOException
     */
    public void discoverMbusDevices() throws SQLException, BusinessException, IOException {
        log(Level.FINE, "Starting discovery of MBusDevices");
        // get an MbusDeviceMap
        List<DeviceMapping> mbusMap = getMbusMapper();
        // check if the current mbus slaves are still on the meter disappeared
        checkForDisappearedMbusMeters(mbusMap);
        // check if all the mbus devices are configured in EIServer
        checkToUpdateMbusMeters(mbusMap);

        StringBuffer sb = new StringBuffer();
        sb.append("Found ").append(mbusMap.size()).append(" MBus devices: ").append("\r\n");
        for (int i = 0; i < mbusMap.size(); i++) {
            DeviceMapping deviceMapping = mbusMap.get(i);
            sb.append(deviceMapping).append("\r\n");
        }
        getLogger().log(Level.INFO, sb.toString());

        // fail all ghost devices
        failGhostSlaveDevices(mbusMap);
    }

    private void failGhostSlaveDevices(List<DeviceMapping> mbusMap) throws BusinessException, IOException, SQLException {
        for (DeviceMapping deviceMapping : mbusMap) {
            if (deviceMapping.isGhostDevice()) {
                Device ghostDevice = CommonUtils.findDeviceBySerialNumber(deviceMapping.getSerialNumber());
                failAllPendingSchedules(ghostDevice);
            }
        }
    }

    private void failAllPendingSchedules(Device device) {
        if (device != null) {
//            List<CommunicationScheduler> schedulers = device.getCommunicationSchedulers();
//            if ((schedulers != null) && (!schedulers.isEmpty())) {
//                for (CommunicationScheduler scheduler : schedulers) {
//                    String commSchedName = scheduler.displayString();
//                    Date nextCommunication = scheduler.getNextCommunication();
//                    if (nextCommunication != null) {
//                        if (nextCommunication.getTime() <= getNow().getTime()) {
//                            getLogger().fine("Next communication date [" + nextCommunication + "] for [" + commSchedName + "] reached, but device is ghost device. Failing schedule.");
//                            try {
//                                scheduler.startCommunication();
//                                scheduler.startReadingNow();
//                                MeterAmrLogging meterAmrLogging = new MeterAmrLogging();
//                                meterAmrLogging.logInfo("Device is a ghost device: device is configured in EIServer, but not found on the real concentrator!");
//                                logFailure(scheduler, meterAmrLogging);
//                            } catch (SQLException e) {
//                                getLogger().warning("Tried to fail all ghost device schedules for a given rtu, but an exception occured: " + e.getMessage());
//                            } catch (BusinessException e) {
//                                getLogger().warning("Tried to fail all ghost device schedules for a given rtu, but an exception occured: " + e.getMessage());
//                            }
//                        } else {
//                            getLogger().fine("Next communication date for Communication schedule [" + commSchedName + "] on ghost device [" + device.getName() + "] not reached yet. Skipping.");
//                        }
//                    } else {
//                        StringBuilder sb = new StringBuilder();
//                        sb.append("Communication schedule [").append(commSchedName).append("] is not active.");
//                        sb.append(" Next communication date is 'null'. ");
//                        sb.append(" Skipping.");
//                        getLogger().fine(sb.toString());
//                    }
//                }
//            }
        } else {
            getLogger().warning("Tried to fail all ghost device schedules for a given rtu, but rtu was 'null'.");
        }
    }

    /**
     * Discover Mbus devices
     *
     * @throws SQLException
     * @throws BusinessException
     * @throws IOException
     */
    public void discoverEMeters() throws SQLException, BusinessException, IOException {
        log(Level.FINE, "Starting discovery of eMeters");
        // get an MbusDeviceMap
        List<DeviceMapping> eMeterMap = getEmeterMapper();
        // check if the current mbus slaves are still on the meter disappeared
        checkForDisappearedEMeters(eMeterMap);
        // check if all the mbus devices are configured in EIServer
        checkToUpdateEMeters(eMeterMap);

        StringBuffer sb = new StringBuffer();
        sb.append("Found ").append(eMeterMap.size()).append(" eMeter devices: ").append("\r\n");
        for (int i = 0; i < eMeterMap.size(); i++) {
            DeviceMapping deviceMapping = eMeterMap.get(i);
            sb.append(deviceMapping).append("\r\n");
        }
        getLogger().log(Level.INFO, sb.toString());

        // fail all ghost devices
        failGhostSlaveDevices(eMeterMap);

    }

    /**
     * Constructs a map containing the serialNumber and the physical address of the mbusdevice.
     * If the serialNumber can't be retrieved from the device then we just log and try the next one.
     *
     * @return a map containing SerailNumber - Physical mbus address
     * @throws ConnectionException if interframeTimeout has passed and maximum retries have been reached
     */
    private List<DeviceMapping> getMbusMapper() throws ConnectionException {
        String mbusSerial;
        List<DeviceMapping> mbusMap = new ArrayList<DeviceMapping>();
        for (int i = MBUS_DEVICES.getFrom(); i <= MBUS_DEVICES.getTo(); i++) {
            mbusSerial = "";
            try {
                ObisCode serialObisCode = ProtocolTools.setObisCodeField(SERIALNR_OBISCODE, 1, (byte) i);
                if (getMeterConfig().isObisCodeInObjectList(serialObisCode)) {
                    Data serialDataObject = getCosemObjectFactory().getData(serialObisCode);
                    OctetString serialOctetString = serialDataObject.getAttrbAbstractDataType(2).getOctetString();
                    mbusSerial = serialOctetString != null ? serialOctetString.stringValue() : null;
                    if ((mbusSerial != null) && (!mbusSerial.equalsIgnoreCase(""))) {
                        mbusMap.add(new DeviceMapping(mbusSerial, i));
                    }
                }
            } catch (IOException e) {
                if (e.getMessage().indexOf("com.energyict.dialer.connection.ConnectionException: receiveResponse() interframe timeout error") > -1) {
                    throw new ConnectionException("InterframeTimeout occurred. Meter probably not accessible anymore." + e);
                }
                //log(Level.FINE, "Could not retrieve the mbus serialNumber for channel " + i + ": " + e.getMessage());
            }
        }
        return mbusMap;
    }

    /**
     * Constructs a map containing the serialNumber and the physical address of the e-meter.
     * If the serialNumber can't be retrieved from the device then we just log and try the next one.
     *
     * @return a map containing SerailNumber - Physical address
     * @throws ConnectionException if interframeTimeout has passed and maximum retries have been reached
     */
    private List<DeviceMapping> getEmeterMapper() throws ConnectionException {
        String eMeterSerial;
        List<DeviceMapping> eMeterMap = new ArrayList<DeviceMapping>();
        for (int i = EMETER_DEVICES.getFrom(); i <= EMETER_DEVICES.getTo(); i++) {
            eMeterSerial = "";
            try {
                ObisCode serialObisCode = ProtocolTools.setObisCodeField(SERIALNR_OBISCODE, 1, (byte) i);
                if (getMeterConfig().isObisCodeInObjectList(serialObisCode)) {
                    Data serialDataObject = getCosemObjectFactory().getData(serialObisCode);
                    OctetString serialOctetString = serialDataObject.getAttrbAbstractDataType(2).getOctetString();
                    eMeterSerial = serialOctetString != null ? serialOctetString.stringValue() : null;
                    if ((eMeterSerial != null) && (!eMeterSerial.equalsIgnoreCase(""))) {
                        eMeterMap.add(new DeviceMapping(eMeterSerial, i));
                    }
                }
            } catch (IOException e) {
                if (e.getMessage().indexOf("com.energyict.dialer.connection.ConnectionException: receiveResponse() interframe timeout error") > -1) {
                    throw new ConnectionException("InterframeTimeout occurred. Meter probably not accessible anymore." + e);
                }
                //log(Level.FINE, "Could not retrieve the eMeter serialNumber for channel " + i + ": " + e.getMessage());
            }
        }
        return eMeterMap;
    }

    /**
     * Check to see if you find MbusDevices as slaves for the current Z3 in the DataBase, but NOT on the physical device
     *
     * @param mbusMap - a map of serialNumbers read from the Z3
     */
    private void checkForDisappearedMbusMeters(List<DeviceMapping> mbusMap) {

        List<Device> slaves = getMeter().getDownstreamDevices();
        Iterator<Device> it = slaves.iterator();
        while (it.hasNext()) {
            Device mbus = it.next();
            Class device = null;
            try {
                CommunicationProtocolShadow protocolShadow = mbus.getDeviceType().getShadow().getCommunicationProtocolShadow();
                if (protocolShadow == null) {
                    log(Level.SEVERE, "Slave deviceType [" + mbus.getDeviceType().displayString() + "] in EIServer has no communication protocol! Skipping device until fixed.");
                } else {
                    device = Class.forName(protocolShadow.getJavaClassName());
                    if ((device != null) && (device.newInstance() instanceof MbusDevice)) {        // we check to see if it's an Mbus device and no TIC device
                        if (!mbusMap.contains(new DeviceMapping(mbus.getSerialNumber()))) {
                            getLogger().log(Level.INFO, "MbusDevice [" + mbus + "] is not installed on the physical device.");
                            mbusMap.add(new DeviceMapping(mbus.getSerialNumber(), true));
                        }
                    }
                }
            } catch (ClassNotFoundException e) {
                log(Level.FINEST, e.getMessage());
                // should never come here because if the rtuType has the className, then you should be able to create a class for it...
            } catch (InstantiationException e) {
                log(Level.FINEST, e.getMessage());
                getLogger().log(Level.INFO, "Could not check if the mbusDevice " + mbus.getSerialNumber() + " exists.");
            } catch (IllegalAccessException e) {
                log(Level.FINEST, e.getMessage());
                getLogger().log(Level.INFO, "Could not check if the mbusDevice " + mbus.getSerialNumber() + " exists.");
            }
        }

    }

    /**
     * Check to see if you find MbusDevices as slaves for the current Z3 in the DataBase, but NOT on the physical device
     *
     * @param eMeterMap - a map of serialNumbers read from the Z3
     */
    private void checkForDisappearedEMeters(List<DeviceMapping> eMeterMap) {

        List<Device> slaves = getMeter().getDownstreamDevices();
        Iterator<Device> it = slaves.iterator();
        while (it.hasNext()) {
            Device eMeter = it.next();
            Class device = null;
            try {
                CommunicationProtocolShadow protocolShadow = eMeter.getDeviceType().getShadow().getCommunicationProtocolShadow();
                if (protocolShadow == null) {
                    log(Level.SEVERE, "Slave deviceType [" + eMeter.getDeviceType().displayString() + "] in EIServer has no communication protocol! Skipping device until fixed.");
                } else {
                    device = Class.forName(protocolShadow.getJavaClassName());
                    if ((device != null) && (device.newInstance() instanceof EMeter)) {        // we check to see if it's an Mbus device and no TIC device
                        if (!eMeterMap.contains(new DeviceMapping(eMeter.getSerialNumber()))) {
                            getLogger().log(Level.INFO, "EMeter [" + eMeter + "] is not installed on the physical device.");
                            eMeterMap.add(new DeviceMapping(eMeter.getSerialNumber(), true));
                        }
                    }
                }
            } catch (ClassNotFoundException e) {
                log(Level.FINEST, e.getMessage());
                // should never come here because if the rtuType has the className, then you should be able to create a class for it...
            } catch (InstantiationException e) {
                log(Level.FINEST, e.getMessage());
                getLogger().log(Level.INFO, "Could not check if the eMeter " + eMeter.getSerialNumber() + " exists.");
            } catch (IllegalAccessException e) {
                log(Level.FINEST, e.getMessage());
                getLogger().log(Level.INFO, "Could not check if the eMeter " + eMeter.getSerialNumber() + " exists.");
            }
        }

    }


    /**
     * Check the ghostMbusDevices and create the mbusDevices
     *
     * @param mbusMap
     * @throws BusinessException if a business error occurred
     * @throws SQLException      if database exception occurred
     * @throws IOException       if multiple meters were found in the database
     */
    private void checkToUpdateMbusMeters(List<DeviceMapping> mbusMap) throws SQLException, BusinessException, IOException {
        int count = 0;
        for (int i = 0; i < mbusMap.size(); i++) {
            DeviceMapping deviceMapping = mbusMap.get(i);
            if (!deviceMapping.isGhostDevice()) {
                Device mbus = CommonUtils.findOrCreateDeviceBySerialNumber(deviceMapping.getSerialNumber(), mbusRtuType, folderExtName);
                if (mbus != null) {
                    // Check if gateway has changed, and update if it has
                    if ((mbus.getGateway() == null) || (mbus.getGateway().getId() != getMeter().getId())) {
                        mbus.updateGateway(getMeter());
                    }
                    this.mbusDevices[count++] = new MbusDevice(deviceMapping.getSerialNumber(), deviceMapping.getPhysicalAddress(), mbus, getLogger());
                }
            }
        }
    }

    /**
     * Check the ghostMbusDevices and create the mbusDevices
     *
     * @param eMeterMap
     * @throws BusinessException if a business error occurred
     * @throws SQLException      if database exception occurred
     * @throws IOException       if multiple meters were found in the database
     */
    private void checkToUpdateEMeters(List<DeviceMapping> eMeterMap) throws SQLException, BusinessException, IOException {
        int count = 0;
        for (int i = 0; i < eMeterMap.size(); i++) {
            DeviceMapping deviceMapping = eMeterMap.get(i);
            if (!deviceMapping.isGhostDevice()) {
                Device eMeter = CommonUtils.findOrCreateDeviceBySerialNumber(deviceMapping.getSerialNumber(), eMeterRtuType, folderExtName);
                if (eMeter != null) {
                    // Check if gateway has changed, and update if it has
                    if ((eMeter.getGateway() == null) || (eMeter.getGateway().getId() != getMeter().getId())) {
                        eMeter.updateGateway(getMeter());
                    }
                    this.eMeters[count++] = new EMeter(deviceMapping.getSerialNumber(), deviceMapping.getPhysicalAddress(), eMeter, getLogger());
                }
            }
        }
    }


    /**
     * Check to see if there are mbusDevices
     *
     * @return the number of MbusDevices
     */
    private int getValidMbusDevices() {
        int count = 0;
        for (int i = 0; i < MBUS_DEVICES.getNumberOfDevices(); i++) {
            if (this.mbusDevices[i] != null) {
                count++;
            }
        }
        return count;
    }

    /**
     * Check to see if there are eMeters
     *
     * @return the number of {@link EMeter}
     */
    private int getValidEMeters() {
        int count = 0;
        for (int i = 0; i < EMETER_DEVICES.getNumberOfDevices(); i++) {
            if (this.eMeters[i] != null) {
                count++;
            }
        }
        return count;
    }

    /**
     * Handles all the EMeters like a separate device
     */
    private void handleEmeters() throws BusinessException, SQLException, IOException {
        for (EMeter eMeter : eMeters) {
//            try {
//                if (eMeter != null) {
//                    eMeter.addProperties(getProperties());
//                    eMeter.addProperties(ProtocolTools.getRtuProperties(eMeter.geteMeterRtu()));
//                    List<CommunicationScheduler> commSchedulers = eMeter.geteMeterRtu().getCommunicationSchedulers();
//                    for (CommunicationScheduler commSchedule : commSchedulers) {
//                        try {
//                            handleEmeterSingleSchedule(eMeter, commSchedule);
//                        } catch (BusinessException e) {
//                            logFailure(commSchedule, eMeter.getMeterAmrLogging());
//                            throw e;
//                        } catch (SQLException e) {
//                            logFailure(commSchedule, eMeter.getMeterAmrLogging());
//                            throw e;
//                        } catch (IOException e) {
//                            logFailure(commSchedule, eMeter.getMeterAmrLogging());
//                            throw e;
//                        }
//                    }
//                }
//            } catch (BusinessException e) {
//                log(Level.FINEST, e.getMessage());
//                getLogger().log(Level.SEVERE, "Emeter with serial: " + eMeter.getSerialNumber() + " has failed.");
//            } catch (SQLException e) {
//                /** Close the connection after an SQL exception, connection will startup again if requested */
//                Environment.getDefault().closeConnection();
//                log(Level.FINEST, e.getMessage());
//                getLogger().log(Level.SEVERE, "Emeter with serial: " + eMeter.getSerialNumber() + " has failed.");
//            } catch (IOException e) {
//                log(Level.FINEST, e.getMessage());
//                getLogger().log(Level.SEVERE, "Emeter with serial: " + eMeter.getSerialNumber() + " has failed. [" + e.getMessage() + "]");
//            }
        }
    }

    /**
     * Handles all the MBus devices like a separate device
     */
    private void handleMbusMeters() {
        for (MbusDevice mbusDevice : mbusDevices) {
//            try {
//                if (mbusDevice != null) {
//                    mbusDevice.addProperties(getProperties());
//                    mbusDevice.addProperties(ProtocolTools.getRtuProperties(mbusDevice.getMbus()));
//                    List<CommunicationScheduler> commSchedulers = mbusDevice.getMbus().getCommunicationSchedulers();
//                    for (CommunicationScheduler commSchedule : commSchedulers) {
//                        try {
//                            handleMbusSingleSchedule(mbusDevice, commSchedule);
//                        } catch (SQLException e) {
//                            logFailure(commSchedule, mbusDevice.getMeterAmrLogging());
//                            throw e;
//                        } catch (BusinessException e) {
//                            logFailure(commSchedule, mbusDevice.getMeterAmrLogging());
//                            throw e;
//                        } catch (IOException e) {
//                            logFailure(commSchedule, mbusDevice.getMeterAmrLogging());
//                            throw e;
//                        }
//                    }
//                }
//            } catch (BusinessException e) {
//
//                /*
//                     * A single MBusMeter failed: log and try next MBusMeter.
//                     */
//                log(Level.FINEST, e.getMessage());
//                getLogger().log(Level.SEVERE, "MBusMeter with serial: " + mbusDevice.getCustomerID() + " has failed.");
//
//            } catch (SQLException e) {
//
//                /** Close the connection after an SQL exception, connection will startup again if requested */
//                Environment.getDefault().closeConnection();
//
//                /*
//                     * A single MBusMeter failed: log and try next MBusMeter.
//                     */
//                log(Level.FINEST, e.getMessage());
//                getLogger().log(Level.SEVERE, "MBusMeter with serial: " + mbusDevice.getCustomerID() + " has failed.");
//
//            } catch (IOException e) {
//
//                /*
//                     * A single MBusMeter failed: log and try next MBusMeter.
//                     */
//                log(Level.FINEST, e.getMessage());
//                getLogger().log(Level.SEVERE, "MBusMeter with serial: " + mbusDevice.getCustomerID() + " has failed. [" + e.getMessage() + "]");
//
//            }
        }
    }

    /**
     * Checks whether there is a TicDevice configured in EIServer
     *
     * @return true if there is a TicDevice configured as a slave of the WebRTU
     */
    private boolean hasTicDevices() {
        Device tic;
        List<Device> slaves = getMeter().getDownstreamDevices();
        Iterator<Device> it = slaves.iterator();
        while (it.hasNext()) {
            tic = it.next();
            Class ticDevice = null;

            try {
                CommunicationProtocolShadow protocolShadow = tic.getDeviceType().getShadow().getCommunicationProtocolShadow();
                if (protocolShadow == null) {
                    log(Level.SEVERE, "Slave deviceType [" + tic.getDeviceType().displayString() + "] in EIServer has no communication protocol! Skipping device until fixed.");
                } else {
                    ticDevice = Class.forName(protocolShadow.getJavaClassName());
                    if ((ticDevice != null) && (ticDevice.newInstance() instanceof TicDevice)) {
                        this.ticDevice = new TicDevice(tic);
                        return true;
                    }
                }
            } catch (ClassNotFoundException e) {
                log(Level.FINEST, e.getMessage());
//				should never come here because if the rtuType has the className, then you should be able to create a class for it...
            } catch (InstantiationException e) {
                log(Level.FINEST, e.getMessage());
                getLogger().log(Level.INFO, "Could not check for TicDevices exists.");
            } catch (IllegalAccessException e) {
                log(Level.FINEST, e.getMessage());
                getLogger().log(Level.INFO, "Could not check for TicDevices exists.");
            }
        }
        return false;
    }

    /**
     * Handle the TIC device.
     * Only profileData and events can be read
     *
     * @throws BusinessException
     * @throws SQLException
     * @throws IOException
     */
//    private void handleTicDevice() throws BusinessException, SQLException, IOException {
//        this.ticDevice.setWebRTU(this);
//        this.ticDevice.execute(getCommunicationScheduler(), null, getLogger());
//    }

//    private void handleEmeterSingleSchedule(EMeter eMeter, CommunicationScheduler commSchedule) throws BusinessException, SQLException, IOException {
//        String commSchedName = commSchedule.displayString();
//        Date nextCommunicationDate = commSchedule.getNextCommunication();
//
//        if (nextCommunicationDate != null) {
//            if (nextCommunicationDate.getTime() <= getNow().getTime()) {
//                getLogger().fine("Next communication date [" + nextCommunicationDate + "] for [" + commSchedName + "] reached. Executing schedule now.");
//                commSchedule.startCommunication();
//                commSchedule.startReadingNow();
//                eMeter.setWebRtu(this);
//                eMeter.execute(commSchedule, null, null);
//                logSuccess(commSchedule, eMeter.getMeterAmrLogging());
//                getLogger().info("Emeter " + eMeter + " has finished.");
//            } else {
//                getLogger().fine("Next communication date for Communication schedule [" + commSchedName + "] not reached yet. Skipping.");
//            }
//        } else {
//            StringBuilder sb = new StringBuilder();
//            sb.append("Communication schedule [").append(commSchedName).append("] is not active.");
//            sb.append(" Next communication date is 'null'. ");
//            sb.append(" Skipping.");
//            getLogger().fine(sb.toString());
//        }
//    }

//    private void handleMbusSingleSchedule(MbusDevice mbusDevice, CommunicationScheduler commSchedule) throws SQLException, BusinessException, IOException {
//        try {
//            String commSchedName = commSchedule.displayString();
//            Date nextCommunicationDate = commSchedule.getNextCommunication();
//
//            if (nextCommunicationDate != null) {
//                if (nextCommunicationDate.getTime() <= getNow().getTime()) {
//                    getLogger().fine("Next communication date [" + nextCommunicationDate + "] for [" + commSchedName + "] reached. Executing schedule now.");
//                    commSchedule.startCommunication(getCommunicationScheduler().getComPortId());
//                    commSchedule.startReadingNow();
//                    mbusDevice.setWebRtu(this);
//                    mbusDevice.execute(commSchedule, null, null);
//                    logSuccess(commSchedule, mbusDevice.getMeterAmrLogging());
//                    getLogger().info("MbusDevice " + mbusDevice + " has finished.");
//                } else {
//                    getLogger().fine("Next communication date for Communication schedule [" + commSchedName + "] not reached yet. Skipping.");
//                }
//            } else {
//                StringBuilder sb = new StringBuilder();
//                sb.append("Communication schedule [").append(commSchedName).append("] is not active: ");
//                sb.append(" Next communication date is 'null'. ");
//                sb.append(" Skipping.");
//                getLogger().fine(sb.toString());
//            }
//        } catch (SQLException e) {
//            mbusDevice.getMeterAmrLogging().logInfo(e);
//            throw e;
//        } catch (BusinessException e) {
//            mbusDevice.getMeterAmrLogging().logInfo(e);
//            throw e;
//        } catch (IOException e) {
//            mbusDevice.getMeterAmrLogging().logInfo(e);
//            throw e;
//        }
//    }

//    private void logSuccess(CommunicationScheduler commSchedule, MeterAmrLogging meterAmrLogging) {
//        List<AmrJournalEntry> journal = new ArrayList<AmrJournalEntry>();
//        journal.add(new AmrJournalEntry(getNow(), AmrJournalEntry.CONNECTTIME, "0"));
//        journal.add(new AmrJournalEntry(getNow(), AmrJournalEntry.TIMEDIFF, "0"));
//        journal.add(new AmrJournalEntry(getNow(), AmrJournalEntry.PROTOCOL_LOG, "See logfile of [" + getMeter().toString() + "]"));
//        journal.add(new AmrJournalEntry(getNow(), AmrJournalEntry.TIMEDIFF, "0"));
//        journal.add(new AmrJournalEntry(AmrJournalEntry.CC_OK));
//        journal.addAll(meterAmrLogging.getJournalEntries());
//        try {
//            commSchedule.journal(journal);
//            commSchedule.logSuccess(getNow());
//        } catch (SQLException e) {
//            e.printStackTrace();
//        } catch (BusinessException e) {
//            e.printStackTrace();
//        }
//    }

//    private void logFailure(CommunicationScheduler commSchedule, MeterAmrLogging meterAmrLogging) {
//        List<AmrJournalEntry> journal = new ArrayList<AmrJournalEntry>();
//        journal.add(new AmrJournalEntry(getNow(), AmrJournalEntry.CONNECTTIME, "0"));
//        journal.add(new AmrJournalEntry(getNow(), AmrJournalEntry.TIMEDIFF, "0"));
//        journal.add(new AmrJournalEntry(getNow(), AmrJournalEntry.PROTOCOL_LOG, "See logfile of [" + getMeter().toString() + "]"));
//        journal.add(new AmrJournalEntry(getNow(), AmrJournalEntry.TIMEDIFF, "0"));
//        journal.add(new AmrJournalEntry(AmrJournalEntry.CC_PROTOCOLERROR));
//        journal.addAll(meterAmrLogging.getJournalEntries());
//        try {
//            commSchedule.journal(journal);
//            commSchedule.logFailure(getNow(), "");
//        } catch (SQLException e) {
//            e.printStackTrace();
//        } catch (BusinessException e) {
//            e.printStackTrace();
//        }
//    }

    private Date getNow() {
        return new Date();
    }

    @Override
    public List getMessageCategories() {
        List<MessageCategorySpec> categories = new ArrayList();

        categories.add(getAuthEncryptCategory());
        categories.add(getFirmwareCategory());
        categories.add(getActivityCalendarCategory());
        categories.add(getTimeCategory());
        categories.add(getConnectivityCategory());
        categories.add(getGlobalResetCategory());
        categories.add(getXmlConfigCategory());
        categories.add(getConnectControlCategory());

/*

		categories.add(getP1Category());
		categories.add(getLoadLimitCategory());

*/
        if (FIRMWAREBUILD) {
            categories.add(getTestCategory());
            categories.add(getDataBaseEntriesCategory());
        }

        return categories;
    }

    /**
     * This messageCategory let's you upgrade two types of firmware.
     * One is the normal meter firmware, the other is the RF-firmware
     * Both are imported with a userfile
     *
     * @return the messages for the FirmwareUpgrade
     */
    @Override
    public MessageCategorySpec getFirmwareCategory() {
        MessageCategorySpec catFirmware = new MessageCategorySpec(
                RtuMessageCategoryConstants.FIRMWARE);
        MessageSpec msgSpec = addFirmwareMsg(RtuMessageKeyIdConstants.FIRMWARE,
                RtuMessageConstant.FIRMWARE_UPGRADE, false);
        catFirmware.addMessageSpec(msgSpec);
        msgSpec = addFirmwareMsg(RtuMessageKeyIdConstants.RFFIRMWARE,
                RtuMessageConstant.RF_FIRMWARE_UPGRADE, false);
        catFirmware.addMessageSpec(msgSpec);
        return catFirmware;
    }

    /**
     * @return the messages for the ConnectivityCategory
     */
    private MessageCategorySpec getConnectivityCategory() {
        MessageCategorySpec catGPRSModemSetup = new MessageCategorySpec(
                RtuMessageCategoryConstants.CHANGECONNECTIVITY);
        MessageSpec msgSpec = addChangeGPRSSetup(
                RtuMessageKeyIdConstants.GPRSMODEMSETUP,
                RtuMessageConstant.GPRS_MODEM_SETUP, false);
        catGPRSModemSetup.addMessageSpec(msgSpec);
        msgSpec = addPhoneListMsg(RtuMessageKeyIdConstants.SETWHITELIST,
                RtuMessageConstant.WAKEUP_ADD_WHITELIST, false);
        catGPRSModemSetup.addMessageSpec(msgSpec);
        msgSpec = addNoValueMsg(RtuMessageKeyIdConstants.ACTIVATESMSWAKEUP,
                RtuMessageConstant.WAKEUP_ACTIVATE, false);
        catGPRSModemSetup.addMessageSpec(msgSpec);
        msgSpec = addNoValueMsg(RtuMessageKeyIdConstants.DEACTIVATESMSWAKEUP,
				RtuMessageConstant.WAKEUP_DEACTIVATE, false);
		catGPRSModemSetup.addMessageSpec(msgSpec);
		return catGPRSModemSetup;
	}

	/* (non-Javadoc)
	 * @see com.energyict.genericprotocolimpl.webrtuz3.profiles.EDevice#getPhysicalAddress()
	 */
	public int getPhysicalAddress() {
		return 0;
	}

    public boolean isRunTestMethod() {
        return runTestMethod;
    }

    public ComposedMeterInfo getMeterInfo() {
        if (meterInfo == null) {
            meterInfo = new ComposedMeterInfo(getProtocolLink(), isBulkRequest(), hasRfFirmwareVersion());
        }
        return meterInfo;
    }

    private ProtocolLink getProtocolLink() {
        return (ProtocolLink) this;
    }

    @Override
    public Date getTime() throws IOException {
        if (cachedMeterTime == null) {
            cachedMeterTime = new CachedMeterTime(super.getTime());
        }
        return cachedMeterTime.getTime();
    }

}
