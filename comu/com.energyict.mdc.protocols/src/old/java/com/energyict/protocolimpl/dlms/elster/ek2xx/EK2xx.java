package com.energyict.protocolimpl.dlms.elster.ek2xx;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.common.NotFoundException;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.HHUEnabler;
import com.energyict.mdc.protocol.api.InvalidPropertyException;
import com.energyict.mdc.protocol.api.MissingPropertyException;
import com.energyict.mdc.protocol.api.NoSuchRegisterException;
import com.energyict.mdc.protocol.api.UnsupportedException;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterProtocol;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;
import com.energyict.mdc.protocol.api.dialer.connection.ConnectionException;
import com.energyict.mdc.protocol.api.dialer.core.HHUSignOn;
import com.energyict.mdc.protocol.api.legacy.MeterProtocol;
import com.energyict.mdc.protocol.api.legacy.dynamic.PropertySpecFactory;
import com.energyict.protocols.mdc.services.impl.OrmClient;
import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.cbo.Quantity;
import com.energyict.dialer.connection.IEC1107HHUConnection;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.dlms.DLMSCOSEMGlobals;
import com.energyict.dlms.DLMSCache;
import com.energyict.dlms.DLMSConnection;
import com.energyict.dlms.DLMSConnectionException;
import com.energyict.dlms.DLMSMeterConfig;
import com.energyict.dlms.DataContainer;
import com.energyict.dlms.HDLCConnection;
import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.TCPIPConnection;
import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.aso.ApplicationServiceObject;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.AxdrType;
import com.energyict.dlms.cosem.Clock;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.Data;
import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.dlms.cosem.Register;
import com.energyict.dlms.cosem.StoredValues;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.base.PluggableMeterProtocol;
import com.energyict.protocolimpl.dlms.CapturedObjects;
import com.energyict.protocolimpl.dlms.RtuDLMS;
import com.energyict.protocolimpl.dlms.RtuDLMSCache;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.Logger;

public class EK2xx extends PluggableMeterProtocol implements HHUEnabler, ProtocolLink, RegisterProtocol {

    @Override
    public String getProtocolDescription() {
        return "Elster EK240 DLMS";
    }

    private static final int DEBUG = 0;
    private static final String DEVICE_ID = "ELS";

    protected String strID;
    protected String strPassword;

    protected int iHDLCTimeoutProperty;
    protected int iProtocolRetriesProperty;
    protected int iDelayAfterFailProperty;
    protected int iSecurityLevelProperty;
    protected int iRequestTimeZone;
    protected int iRoundtripCorrection;
    protected int iClientMacAddress;
    protected int iServerUpperMacAddress;
    protected int iServerLowerMacAddress;
    protected int iRequestClockObject;
    protected String nodeId;
    private String serialNumber;
    private int extendedLogging;
    private int profileInterval = -1;

    //private boolean boolAbort=false;

    DLMSConnection dlmsConnection = null;
    CosemObjectFactory cosemObjectFactory = null;
    StoredValuesImpl storedValuesImpl = null;

    // lazy initializing
    private DLMSMeterConfig meterConfig = null;
    private EK2xxAarq ek2xxAarq = null;
    private EK2xxRegisters ek2xxRegisters = null;
    private EK2xxProfile ek2xxProfile = null;
    private int numberOfChannels = -1;

    // Added for MeterProtocol interface implementation
    private Logger logger = null;
    private TimeZone timeZone = null;
    //private Properties properties=null;

    // filled in when getTime is invoked!
    private int dstFlag; // -1=unknown, 0=not set, 1=set
    int addressingMode;
    int connectionMode;
    private final OrmClient ormClient;

    @Inject
    public EK2xx(PropertySpecService propertySpecService, OrmClient ormClient) {
        super(propertySpecService);
        this.ormClient = ormClient;
        this.meterConfig = DLMSMeterConfig.getInstance(getDeviceID());
        this.ek2xxAarq = new EK2xxAarq(this);
        this.ek2xxRegisters = new EK2xxRegisters();
        this.ek2xxProfile = new EK2xxProfile(this);
    }

    /*
      * Private getters, setters and methods
      */

    private String getDeviceID() {
        return DEVICE_ID;
    }

    private EK2xxProfile getEk2xxProfile() {
        return this.ek2xxProfile;
    }

    EK2xxRegisters getEk2xxRegisters() {
        return this.ek2xxRegisters;
    }

    private void requestSAP() throws IOException {
        String devID = (String) getCosemObjectFactory().getSAPAssignment().getLogicalDeviceNames().get(0);
        if ((this.strID != null) && ("".compareTo(this.strID) != 0)) {
            if (this.strID.compareTo(devID) != 0) {
                throw new IOException("DLMSSN, requestSAP, Wrong DeviceID!, settings=" + this.strID + ", meter=" + devID);
            }
        }
    } // public void requestSAP() throws IOException

    public void setProperties(Properties properties) throws InvalidPropertyException, MissingPropertyException {
        validateProperties(properties);
    }

    protected void validateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
        try {
            Iterator iterator = getRequiredKeys().iterator();
            while (iterator.hasNext()) {
                String key = (String) iterator.next();
                if (properties.getProperty(key) == null) {
                    throw new MissingPropertyException(key + " key missing");
                }
            }
            this.strID = properties.getProperty(MeterProtocol.ADDRESS);
            // KV 19012004
            if ((this.strID != null) && (this.strID.length() > 16)) {
                throw new InvalidPropertyException("ID must be less or equal then 16 characters.");
            }


            this.nodeId = properties.getProperty(MeterProtocol.NODEID, "");
            // KV 19012004 get the serialNumber
            this.serialNumber = properties.getProperty(MeterProtocol.SERIALNUMBER);
            this.extendedLogging = Integer.parseInt(properties.getProperty("ExtendedLogging", "0"));
            this.addressingMode = Integer.parseInt(properties.getProperty("AddressingMode", "-1"));
            this.connectionMode = Integer.parseInt(properties.getProperty("Connection", "0")); // 0=HDLC, 1= TCP/IP
            this.strPassword = properties.getProperty(MeterProtocol.PASSWORD, "");
            //if (strPassword.length()!=8) throw new InvalidPropertyException("Password must be exact 8 characters.");
            this.iHDLCTimeoutProperty = Integer.parseInt(properties.getProperty("Timeout", "10000").trim());
            this.iProtocolRetriesProperty = Integer.parseInt(properties.getProperty("Retries", "5").trim());
            this.iDelayAfterFailProperty = Integer.parseInt(properties.getProperty("DelayAfterfail", "3000").trim());
            this.iRequestTimeZone = Integer.parseInt(properties.getProperty("RequestTimeZone", "0").trim());
            this.iRequestClockObject = Integer.parseInt(properties.getProperty("RequestClockObject", "0").trim());
            this.iRoundtripCorrection = Integer.parseInt(properties.getProperty("RoundtripCorrection", "0").trim());
            this.iSecurityLevelProperty = Integer.parseInt(properties.getProperty("SecurityLevel", "0").trim());
            this.iClientMacAddress = Integer.parseInt(properties.getProperty("ClientMacAddress", "16").trim());
            this.iServerUpperMacAddress = Integer.parseInt(properties.getProperty("ServerUpperMacAddress", "1").trim());
            this.iServerLowerMacAddress = Integer.parseInt(properties.getProperty("ServerLowerMacAddress", "0").trim());

            if (DEBUG >= 1) {
                System.out.println();
                properties.list(System.out);
                System.out.println();
            }

        } catch (NumberFormatException e) {
            throw new InvalidPropertyException("EK2xx, validateProperties, NumberFormatException, " + e.getMessage());
        }
    }

    public int getProfileInterval() throws IOException {
        if (this.profileInterval == -1) {
            this.profileInterval = (int) (getCosemObjectFactory().getData(EK2xxRegisters.PROFILE_INTERVAL).getValue() & 0xEFFFFFFF);
        }
        return this.profileInterval;
    }

    public int getNumberOfChannels() throws IOException {
        if (this.numberOfChannels == -1) {
            this.numberOfChannels = getCapturedObjects().getNROfChannels();
        }
        return this.numberOfChannels;
    } // public int getNumberOfChannels() throws IOException

    public void disconnect() throws IOException {
        try {
            if (getDLMSConnection() != null) {
                getDLMSConnection().disconnectMAC();
            }
        } catch (DLMSConnectionException e) {
            this.logger.severe("DLMSLN: disconnect(), " + e.getMessage());
        }
    }

    @Override
    public ApplicationServiceObject getAso() {
        return null;      //Not used
    }

    public void connect() throws IOException {
        try {
            getDLMSConnection().connectMAC();
            this.ek2xxAarq.requestApplAssoc(this.iSecurityLevelProperty);
            requestSAP();
            requestObjectList();

            this.logger.info(getRegistersInfo(this.extendedLogging));

        } catch (IOException e) {
            throw new IOException("connect(): " + e.getMessage());
        } catch (DLMSConnectionException e) {
            throw new IOException("connect() DLMSConnectionException: "
                    + e.getMessage());
        }

        validateSerialNumber();

    }

    private void validateSerialNumber() {
    }

    private String getSerialNumber() throws IOException {
        UniversalObject uo = this.meterConfig.getSerialNumberObject();
        return getCosemObjectFactory().getGenericRead(uo.getBaseName(), uo.getValueAttributeOffset()).getString();
    }

    public CosemObjectFactory getCosemObjectFactory() {
        return this.cosemObjectFactory;
    }

    private void requestObjectList() throws IOException {
        this.meterConfig.setInstantiatedObjectList(getCosemObjectFactory().getAssociationSN().getBuffer());
    }

    protected String getRegistersInfo(int extendedLogging) {
        StringBuffer strBuff = new StringBuffer();
        String regInfo = "";
        if (extendedLogging < 1) {
            return "";
        }

        try {
            strBuff.append("********************* All instantiated objects in the meter *********************\n");
            for (int i = 0; i < getMeterConfig().getInstantiatedObjectList().length; i++) {
                UniversalObject uo = getMeterConfig().getInstantiatedObjectList()[i];
                ObisCode obis = uo.getObisCode();
                regInfo = obis.toString() + " = " + translateRegister(obis).toString();
                strBuff.append(regInfo + "\n");
            }
            strBuff.append("*********************************************************************************\n");
            for (int i = 0; i < getMeterConfig().getInstantiatedObjectList().length; i++) {
                UniversalObject uo = getMeterConfig().getInstantiatedObjectList()[i];
                ObisCode obis = uo.getObisCode();
                if (getEk2xxRegisters().isProfileObject(obis)) {
                    ProfileGeneric profile = getCosemObjectFactory().getProfileGeneric(obis);

                    strBuff.append(
                            "profile generic = " + profile.toString() + "\n" +
                                    "\t" + "obisCode = " + obis.toString() + "\n" +
                                    "\t" + "getCapturePeriod = " + profile.getCapturePeriod() + "\n" +
                                    "\t" + "getNumberOfProfileChannels = " + profile.getNumberOfProfileChannels() + "\n" +
                                    "\t" + "getProfileEntries = " + profile.getProfileEntries() + "\n" +
                                    "\t" + "getResetCounter = " + profile.getResetCounter() + "\n" +
                                    "\t" + "getScalerUnit = " + profile.getScalerUnit() + "\n" +
                                    "\t" + "containsCapturedObjects = " + profile.containsCapturedObjects() + "\n" +
                                    "\t" + "getEntriesInUse = " + profile.getEntriesInUse() + "\n"

                    );

                }
            }
            strBuff.append("*********************************************************************************\n\n");

        } catch (Exception e) {
            e.printStackTrace();
        }

        return strBuff.toString();
    }

    public Object fetchCache(int rtuid) throws SQLException {
        if (rtuid != 0) {
            RtuDLMSCache rtuCache = new RtuDLMSCache(rtuid, this.ormClient);
            RtuDLMS rtu = new RtuDLMS(rtuid, ormClient);
            try {
                return new DLMSCache(rtuCache.getObjectList(), rtu.getConfProgChange());
            } catch (NotFoundException e) {
                return new DLMSCache(null, -1);
            }
        } else {
            throw new IllegalArgumentException("invalid RtuId!");
        }
    }

    public String getFirmwareVersion() throws IOException {
        return getCosemObjectFactory().getData(EK2xxRegisters.SOFTWARE_VERSION).getString();
    }

    public Quantity getMeterReading(int channelId) throws IOException {
        throw new UnsupportedException("getMeterReading(int channelId) is not suported!!!");
    }

    public Quantity getMeterReading(String name) throws IOException {
        throw new UnsupportedException("getMeterReading(String name) is not suported!!!");
    }

    public ProfileData getProfileData(boolean includeEvents) throws IOException {
        Calendar calendar = Calendar.getInstance(getTimeZone());
        calendar.add(Calendar.MONTH, -2);
        return getProfileData(calendar.getTime(), includeEvents);
    }

    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        Calendar calendar = Calendar.getInstance(getTimeZone());
        return getProfileData(lastReading, calendar.getTime(), includeEvents);
    }

    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException {
        Date now = new Date();
        if (to.compareTo(now) >= 0) {
            to = now;
        }

        List dataContainers = new ArrayList(0);
        ProfileData profileData = new ProfileData();
        DataContainer dc;
        Calendar fromCalendar = ProtocolUtils.getCleanCalendar(getTimeZone());
        Calendar toCalendar = ProtocolUtils.getCleanCalendar(getTimeZone());
        Calendar fromDate_ptr = ProtocolUtils.getCleanCalendar(getTimeZone());
        Calendar toDate_ptr = ProtocolUtils.getCleanCalendar(getTimeZone());
        Date profileDate = null;
        boolean lastRead = false;
        boolean dataReceived = false;

        this.ek2xxProfile.setGenerateEvents(includeEvents);

        fromCalendar.setTime(from);
        toCalendar.setTime(to);

        toDate_ptr.setTime(toCalendar.getTime());
        fromDate_ptr.setTime(toCalendar.getTime());

        do {
            fromDate_ptr.add(Calendar.HOUR, -24);
            if (fromDate_ptr.getTime().compareTo(fromCalendar.getTime()) <= 0) {
                fromDate_ptr.setTime(fromCalendar.getTime());
            }

            if (DEBUG >= 1) {
                System.out.println(" ################ fromDate_ptr = " + fromDate_ptr.getTime().toString());
                System.out.println(" ################ toDate_ptr   = " + toDate_ptr.getTime().toString());
                System.out.println(" ################ fromCalendar = " + fromCalendar.getTime().toString());
                System.out.println(" ################ toCalendar   = " + toCalendar.getTime().toString());
            }

            dc = getCosemObjectFactory().getProfileGeneric(EK2xxRegisters.PROFILE).getBuffer(fromDate_ptr, toDate_ptr);
            profileDate = this.ek2xxProfile.getDateFromDataContainer(dc);

            if (profileDate == null) {
                if (dataReceived == true) {
                    lastRead = true;
                }
            } else {
                dataContainers.add(dc);
                dataReceived = true;
            }

            toDate_ptr.setTime(fromDate_ptr.getTime());

        } while ((fromDate_ptr.getTime().compareTo(fromCalendar.getTime()) > 0) && !lastRead);

        this.ek2xxProfile.parseDataContainers(dataContainers);
        profileData.setChannelInfos(this.ek2xxProfile.getChannelInfos());
        profileData.getIntervalDatas().addAll(this.ek2xxProfile.getIntervalDatas());

        if (includeEvents) {
            List meterEvents = this.ek2xxProfile.getMeterEvents();
            profileData.getMeterEvents().addAll(meterEvents);
        }

        return profileData;
    }

    private CapturedObjects getCapturedObjects() throws IOException {
        return getEk2xxProfile().getCapturedObjects();
    } // private CapturedObjects getCapturedObjects()  throws UnsupportedException, IOException

    public String getProtocolVersion() {
        return "$Date: 2014-06-02 13:26:25 +0200 (Mon, 02 Jun 2014) $";
    }

    public String getRegister(String name) throws IOException {
        throw new UnsupportedException();
    }

    public Date getTime() throws IOException {
        Clock clock = getCosemObjectFactory().getClock(EK2xxRegisters.CLOCK);
        Date date = clock.getDateTime();
        this.dstFlag = clock.getDstFlag();
        return date;
    }

    public void init(InputStream inputStream, OutputStream outputStream, TimeZone timeZone, Logger logger) throws IOException {

        this.timeZone = timeZone;
        this.logger = logger;

        this.dstFlag = -1;

        try {
            this.cosemObjectFactory = new CosemObjectFactory(this);
            this.storedValuesImpl = new StoredValuesImpl(this.cosemObjectFactory);
            // KV 19092003 set forcedelay to 100 ms for the optical delay when using HHU?
            if (this.connectionMode == 0) {
                this.dlmsConnection = new HDLCConnection(inputStream, outputStream, this.iHDLCTimeoutProperty, 100, this.iProtocolRetriesProperty, this.iClientMacAddress, this.iServerLowerMacAddress, this.iServerUpperMacAddress, this.addressingMode);
            } else {
                this.dlmsConnection = new TCPIPConnection(inputStream, outputStream, this.iHDLCTimeoutProperty, 100, this.iProtocolRetriesProperty, this.iClientMacAddress, this.iServerLowerMacAddress, getLogger());
            }
        } catch (DLMSConnectionException e) {
            throw new IOException(e.getMessage());
        }

    }

    public void initializeDevice() throws IOException {
        throw new UnsupportedException("initializeDevice() is not suported!!!");
    }

    public void release() throws IOException {
    }

    public void setRegister(String name, String value) throws IOException {
        throw new UnsupportedException("setRegister() not suported!");
    }

    public void setTime() throws IOException {
        Calendar calendar = null;
        if (this.iRequestTimeZone != 0) {
            calendar = ProtocolUtils.getCalendar(getTimeZone());
        } else {
            calendar = ProtocolUtils.initCalendar(false, this.timeZone);
        }
        calendar.add(Calendar.MILLISECOND, this.iRoundtripCorrection);
        doSetTime(calendar);
    }

    private void doSetTime(Calendar calendar) throws IOException {
        byte[] byteTimeBuffer = new byte[15];

        byteTimeBuffer[0] = 1;
        byteTimeBuffer[1] = AxdrType.OCTET_STRING.getTag();
        byteTimeBuffer[2] = 12; // length
        byteTimeBuffer[3] = (byte) (calendar.get(Calendar.YEAR) >> 8);
        byteTimeBuffer[4] = (byte) calendar.get(Calendar.YEAR);
        byteTimeBuffer[5] = (byte) (calendar.get(Calendar.MONTH) + 1);
        byteTimeBuffer[6] = (byte) calendar.get(Calendar.DAY_OF_MONTH);
        byte bDOW = (byte) calendar.get(Calendar.DAY_OF_WEEK);
        byteTimeBuffer[7] = bDOW-- == 1 ? (byte) 7 : bDOW;
        byteTimeBuffer[8] = (byte) calendar.get(Calendar.HOUR_OF_DAY);
        byteTimeBuffer[9] = (byte) calendar.get(Calendar.MINUTE);
        byteTimeBuffer[10] = (byte) calendar.get(Calendar.SECOND);
        byteTimeBuffer[11] = (byte) 0xFF;
        byteTimeBuffer[12] = (byte) 0x80;
        byteTimeBuffer[13] = 0x00;

        if (isRequestTimeZone()) {
            if (this.dstFlag == 0) {
                byteTimeBuffer[14] = 0x00;
            } else if (this.dstFlag == 1) {
                byteTimeBuffer[14] = (byte) 0x80;
            } else {
                throw new IOException("doSetTime(), dst flag is unknown! setTime() before getTime()!");
            }
        } else {
            if (getTimeZone().inDaylightTime(calendar.getTime())) {
                byteTimeBuffer[14] = (byte) 0x80;
            } else {
                byteTimeBuffer[14] = 0x00;
            }
        }

        getCosemObjectFactory().getGenericWrite((short) this.meterConfig.getClockSN(), DLMSCOSEMGlobals.TIME_TIME).write(byteTimeBuffer);

    } // private void doSetTime(Calendar calendar)


    /*
      * Public methods
      */

    public void enableHHUSignOn(SerialCommunicationChannel commChannel) throws ConnectionException {
        enableHHUSignOn(commChannel, false);
    }

    public void enableHHUSignOn(SerialCommunicationChannel commChannel, boolean enableDataReadout) throws ConnectionException {
        HHUSignOn hhuSignOn = new IEC1107HHUConnection(commChannel, this.iHDLCTimeoutProperty, this.iProtocolRetriesProperty, 300, 0);
        hhuSignOn.setMode(HHUSignOn.MODE_BINARY_HDLC);
        hhuSignOn.setProtocol(HHUSignOn.PROTOCOL_HDLC);
        hhuSignOn.enableDataReadout(enableDataReadout);
        getDLMSConnection().setHHUSignOn(hhuSignOn, this.nodeId);
    }

    public List<String> getOptionalKeys() {
        return Arrays.asList(
                    "Timeout",
                    "Retries",
                    "DelayAfterFail",
                    "RequestTimeZone",
                    "RequestClockObject",
                    "SecurityLevel",
                    "ClientMacAddress",
                    "ServerUpperMacAddress",
                    "ServerLowerMacAddress",
                    "ExtendedLogging",
                    "AddressingMode",
                    "EventIdIndex");
    }

    public List<String> getRequiredKeys() {
        return Collections.emptyList();
    }

    @Override
    public List<PropertySpec> getRequiredProperties() {
        return PropertySpecFactory.toPropertySpecs(getRequiredKeys(), this.getPropertySpecService());
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        return PropertySpecFactory.toPropertySpecs(getOptionalKeys(), this.getPropertySpecService());
    }

    /*
      * Public getters and setters
      */

    public String getPassword() {
        return this.strPassword;
    }

    public byte[] getHHUDataReadout() {
        return getDLMSConnection().getHhuSignOn().getDataReadout();
    }

    public DLMSConnection getDLMSConnection() {
        return this.dlmsConnection;
    }

    public Logger getLogger() {
        return this.logger;
    }

    public DLMSMeterConfig getMeterConfig() {
        return this.meterConfig;
    }

    public int getReference() {
        return ProtocolLink.SN_REFERENCE;
    }

    public int getRoundTripCorrection() {
        return this.iRoundtripCorrection;
    }

    public StoredValues getStoredValues() {
        return this.storedValuesImpl;
    }

    public TimeZone getTimeZone() {
        return this.timeZone;
    }

    public boolean isRequestTimeZone() {
        return (this.iRequestTimeZone != 0);
    }

    public RegisterValue readRegister(ObisCode obisCode) throws IOException {

        /*
           * Obiscode refers to an DLMS DATA OBJECT
           */
        if (getEk2xxRegisters().isDataObject(obisCode)) {

            Data data = getCosemObjectFactory().getData(obisCode);
            AbstractDataType valueAttr = data.getValueAttr();

            if (DEBUG >= 1) {
                System.out.println("ObisCode = " + obisCode + " value = " + valueAttr.toString());
            }

            if (valueAttr.isOctetString()) {
                return new RegisterValue(obisCode, data.getString());
            }
            if (valueAttr.isUnsigned32()) {
                return new RegisterValue(obisCode, data.getQuantityValue());
            }

        }

        /*
           *  Obiscode refers to an DLMS CLOCK OBJECT
           */
        if (getEk2xxRegisters().isClockObject(obisCode)) {
            Clock clockObject = getCosemObjectFactory().getClock(obisCode);
            Date rtuDateTime = clockObject.getDateTime();
            Calendar cal = ProtocolUtils.getCalendar(getTimeZone());
            cal.setTime(rtuDateTime);
            return new RegisterValue(obisCode, cal.getTime().toString());
        }

        /*
           *  Obiscode refers to an DLMS REGISTER OBJECT
           */
        if (getEk2xxRegisters().isRegisterObject(obisCode)) {
            Register reg = getCosemObjectFactory().getRegister(obisCode);
            Date readTime = reg.getCaptureTime();
            Date toTime = reg.getBillingDate();
            Quantity value = null;
            String text = null;

            try {
                value = reg.getQuantityValue();
            } catch (Exception e) {
                text = reg.getText();
            }

            if (value != null) {
                return new RegisterValue(obisCode, value, readTime, null, toTime, new Date());
            } else {
                return new RegisterValue(obisCode, text);
            }

        }

        /*
           * Obiscode refers to an DLMS PROFILE GENERIC OBJECT
           */
        if (getEk2xxRegisters().isProfileObject(obisCode)) {
            ProfileGeneric pg = getCosemObjectFactory().getProfileGeneric(obisCode);
            if (DEBUG >= 1) {
                System.out.println(
                        "profile generic = " + pg.toString() + "\n" +
                                "obisCode = " + obisCode.toString() + "\n" +
                                "getCapturePeriod = " + pg.getCapturePeriod() + "\n" +
                                "getNumberOfProfileChannels = " + pg.getNumberOfProfileChannels() + "\n" +
                                "getProfileEntries = " + pg.getProfileEntries() + "\n" +
                                "getResetCounter = " + pg.getResetCounter() + "\n" +
                                "getScalerUnit = " + pg.getScalerUnit() + "\n"
                );
            }
        }

        throw new NoSuchRegisterException(obisCode.toString() + " is not supported.");

    }

    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        String regType = getEk2xxRegisters().getObjectType(obisCode);
        String regName = getEk2xxRegisters().getObjectName(obisCode);
        return new RegisterInfo(regName + " - Type: " + regType);
    }

    public void setCache(Object cacheObject) {
    }

    public void updateCache(int rtuid, Object cacheObject){
    }

    public Object getCache() {
        return null;
    }

}