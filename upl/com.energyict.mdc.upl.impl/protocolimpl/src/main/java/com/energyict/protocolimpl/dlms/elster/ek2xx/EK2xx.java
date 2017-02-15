package com.energyict.protocolimpl.dlms.elster.ek2xx;

import com.energyict.cbo.Quantity;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dialer.connection.HHUSignOn;
import com.energyict.dialer.connections.IEC1107HHUConnection;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.dlms.DLMSCOSEMGlobals;
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
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.mdc.upl.NoSuchRegisterException;
import com.energyict.mdc.upl.UnsupportedException;
import com.energyict.mdc.upl.nls.TranslationKey;
import com.energyict.mdc.upl.properties.InvalidPropertyException;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecBuilderWizard;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.properties.TypedProperties;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.HHUEnabler;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterProtocol;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.support.SerialNumberSupport;
import com.energyict.protocolimpl.base.PluggableMeterProtocol;
import com.energyict.protocolimpl.dlms.CapturedObjects;
import com.energyict.protocolimpl.nls.PropertyTranslationKeys;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.function.Supplier;
import java.util.logging.Logger;

import static com.energyict.mdc.upl.MeterProtocol.Property.ADDRESS;
import static com.energyict.mdc.upl.MeterProtocol.Property.NODEID;
import static com.energyict.mdc.upl.MeterProtocol.Property.PASSWORD;
import static com.energyict.mdc.upl.MeterProtocol.Property.RETRIES;
import static com.energyict.mdc.upl.MeterProtocol.Property.ROUNDTRIPCORRECTION;
import static com.energyict.mdc.upl.MeterProtocol.Property.SECURITYLEVEL;
import static com.energyict.mdc.upl.MeterProtocol.Property.SERIALNUMBER;
import static com.energyict.mdc.upl.MeterProtocol.Property.TIMEOUT;

public class EK2xx extends PluggableMeterProtocol implements HHUEnabler, ProtocolLink, RegisterProtocol, SerialNumberSupport {

    private static final int MAX_ADDRESS_LENGTH = 16;

    private static final int DEBUG = 0;
    private static final String DEVICE_ID = "ELS";
    private final PropertySpecService propertySpecService;

    protected String strID;
    protected String strPassword;

    private int iHDLCTimeoutProperty;
    protected int iProtocolRetriesProperty;
    protected int iDelayAfterFailProperty;
    private int iSecurityLevelProperty;
    protected int iRequestTimeZone;
    protected int iRoundtripCorrection;
    protected int iClientMacAddress;
    protected int iServerUpperMacAddress;
    protected int iServerLowerMacAddress;
    private int iRequestClockObject;
    protected String nodeId;
    private String serialNumber;
    private int extendedLogging;
    private int profileInterval = -1;

    private DLMSConnection dlmsConnection = null;
    private CosemObjectFactory cosemObjectFactory = null;
    private StoredValuesImpl storedValuesImpl = null;

    // lazy initializing
    private DLMSMeterConfig meterConfig = null;
    private EK2xxAarq ek2xxAarq = null;
    private EK2xxRegisters ek2xxRegisters = null;
    private EK2xxProfile ek2xxProfile = null;
    private int numberOfChannels = -1;

    // Added for MeterProtocol interface implementation
    private Logger logger = null;
    private TimeZone timeZone = null;

    // filled in when getTime is invoked!
    private int dstFlag; // -1=unknown, 0=not set, 1=set
    private int addressingMode;
    private int connectionMode;

    public EK2xx(PropertySpecService propertySpecService) {
        this.meterConfig = DLMSMeterConfig.getInstance(getDeviceID());
        this.ek2xxAarq = new EK2xxAarq(this);
        this.ek2xxRegisters = new EK2xxRegisters();
        this.ek2xxProfile = new EK2xxProfile(this);
        this.propertySpecService = propertySpecService;
    }

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
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        return Arrays.asList(
                this.stringSpecOfMaxLength(ADDRESS.getName(), MAX_ADDRESS_LENGTH, PropertyTranslationKeys.DLMS_ADDRESS),
                this.stringSpec(NODEID.getName(), PropertyTranslationKeys.DLMS_NODEID),
                this.stringSpec(SERIALNUMBER.getName(), PropertyTranslationKeys.DLMS_SERIALNUMBER),
                this.integerSpec("ExtendedLogging", PropertyTranslationKeys.DLMS_EXTENDED_LOGGING),
                this.integerSpec("AddressingMode", PropertyTranslationKeys.DLMS_ADDRESSING_MODE),
                this.integerSpec("Connection", PropertyTranslationKeys.DLMS_CONNECTION),
                this.stringSpec(PASSWORD.getName(), PropertyTranslationKeys.DLMS_PASSWORD),
                this.integerSpec(TIMEOUT.getName(), PropertyTranslationKeys.DLMS_TIMEOUT),
                this.integerSpec(RETRIES.getName(), PropertyTranslationKeys.DLMS_RETRIES),
                this.integerSpec("DelayAfterfail", PropertyTranslationKeys.DLMS_DELAY_AFTERFAIL),
                this.integerSpec("RequestTimeZone", PropertyTranslationKeys.DLMS_REQUEST_TIME_ZONE),
                this.integerSpec("RequestClockObject", PropertyTranslationKeys.DLMS_REQUEST_CLOCK_OBJECT),
                this.integerSpec(ROUNDTRIPCORRECTION.getName(), PropertyTranslationKeys.DLMS_ROUNDTRIPCORRECTION),
                this.integerSpec(SECURITYLEVEL.getName(), PropertyTranslationKeys.DLMS_SECURITYLEVEL),
                this.integerSpec("ClientMacAddress", PropertyTranslationKeys.DLMS_CLIENT_MAC_ADDRESS),
                this.integerSpec("ServerUpperMacAddress", PropertyTranslationKeys.DLMS_SERVER_UPPER_MAC_ADDRESS),
                this.integerSpec("ServerLowerMacAddress", PropertyTranslationKeys.DLMS_SERVER_LOWER_MAC_ADDRESS));
    }

    private <T> PropertySpec spec(String name, TranslationKey translationKey, Supplier<PropertySpecBuilderWizard.NlsOptions<T>> optionsSupplier) {
        return UPLPropertySpecFactory.specBuilder(name, false, translationKey,optionsSupplier).finish();
    }

    private PropertySpec stringSpec(String name, TranslationKey translationKey) {
        return this.spec(name, translationKey, this.propertySpecService::stringSpec);
    }

    private PropertySpec stringSpecOfMaxLength(String name, int length, TranslationKey translationKey) {
        return this.spec(name, translationKey, () -> this.propertySpecService.stringSpecOfMaximumLength(length));
    }

    private PropertySpec integerSpec(String name, TranslationKey translationKey) {
        return this.spec(name, translationKey, this.propertySpecService::integerSpec);
    }

    @Override
    public void setUPLProperties(TypedProperties properties) throws PropertyValidationException {
        try {
            this.strID = properties.getTypedProperty(ADDRESS.getName());
            this.nodeId = properties.getTypedProperty(NODEID.getName(), "");
            // KV 19012004 get the serialNumber
            this.serialNumber = properties.getTypedProperty(SERIALNUMBER.getName());
            this.extendedLogging = Integer.parseInt(properties.getTypedProperty("ExtendedLogging", "0"));
            this.addressingMode = Integer.parseInt(properties.getTypedProperty("AddressingMode", "-1"));
            this.connectionMode = Integer.parseInt(properties.getTypedProperty("Connection", "0")); // 0=HDLC, 1= TCP/IP
            this.strPassword = properties.getTypedProperty(PASSWORD.getName(), "");
            this.iHDLCTimeoutProperty = Integer.parseInt(properties.getTypedProperty(TIMEOUT.getName(), "10000").trim());
            this.iProtocolRetriesProperty = Integer.parseInt(properties.getTypedProperty(RETRIES.getName(), "5").trim());
            this.iDelayAfterFailProperty = Integer.parseInt(properties.getTypedProperty("DelayAfterfail", "3000").trim());
            this.iRequestTimeZone = Integer.parseInt(properties.getTypedProperty("RequestTimeZone", "0").trim());
            this.iRequestClockObject = Integer.parseInt(properties.getTypedProperty("RequestClockObject", "0").trim());
            this.iRoundtripCorrection = Integer.parseInt(properties.getTypedProperty("RoundtripCorrection", "0").trim());
            this.iSecurityLevelProperty = Integer.parseInt(properties.getTypedProperty(SECURITYLEVEL.getName(), "0").trim());
            this.iClientMacAddress = Integer.parseInt(properties.getTypedProperty("ClientMacAddress", "16").trim());
            this.iServerUpperMacAddress = Integer.parseInt(properties.getTypedProperty("ServerUpperMacAddress", "1").trim());
            this.iServerLowerMacAddress = Integer.parseInt(properties.getTypedProperty("ServerLowerMacAddress", "0").trim());
            if (DEBUG >= 1) {
                System.out.println();
                properties.toStringProperties().list(System.out);
                System.out.println();
            }
        } catch (NumberFormatException e) {
            throw new InvalidPropertyException(e, this.getClass().getSimpleName() + ": validation of properties failed before");
        }
    }

    @Override
    public int getProfileInterval() throws IOException {
        if (this.profileInterval == -1) {
            this.profileInterval = (int) (getCosemObjectFactory().getData(EK2xxRegisters.PROFILE_INTERVAL).getValue() & 0xEFFFFFFF);
        }
        return this.profileInterval;
    }

    @Override
    public int getNumberOfChannels() throws IOException {
        if (this.numberOfChannels == -1) {
            this.numberOfChannels = getCapturedObjects().getNROfChannels();
        }
        return this.numberOfChannels;
    }

    @Override
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

    @Override
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
    }

    @Override
    public String getSerialNumber() {
        UniversalObject uo;
        try {
            uo = this.meterConfig.getSerialNumberObject();
            return getCosemObjectFactory().getGenericRead(uo.getBaseName(), uo.getValueAttributeOffset()).getString();
        }  catch (IOException e) {
            throw DLMSIOExceptionHandler.handle(e, iProtocolRetriesProperty + 1);
        }
    }

    public CosemObjectFactory getCosemObjectFactory() {
        return this.cosemObjectFactory;
    }

    private void requestObjectList() throws IOException {
        this.meterConfig.setInstantiatedObjectList(getCosemObjectFactory().getAssociationSN().getBuffer());
    }

    protected String getRegistersInfo(int extendedLogging) {
        StringBuilder builder = new StringBuilder();
        String regInfo;
        if (extendedLogging < 1) {
            return "";
        }

        try {
            builder.append("********************* All instantiated objects in the meter *********************\n");
            for (int i = 0; i < getMeterConfig().getInstantiatedObjectList().length; i++) {
                UniversalObject uo = getMeterConfig().getInstantiatedObjectList()[i];
                ObisCode obis = uo.getObisCode();
                regInfo = obis.toString() + " = " + translateRegister(obis).toString();
                builder.append(regInfo).append("\n");
            }
            builder.append("*********************************************************************************\n");
            for (int i = 0; i < getMeterConfig().getInstantiatedObjectList().length; i++) {
                UniversalObject uo = getMeterConfig().getInstantiatedObjectList()[i];
                ObisCode obis = uo.getObisCode();
                if (getEk2xxRegisters().isProfileObject(obis)) {
                    ProfileGeneric profile = getCosemObjectFactory().getProfileGeneric(obis);

                    builder.append("profile generic = ")
                            .append(profile.toString())
                            .append("\n")
                            .append("\t")
                            .append("obisCode = ")
                            .append(obis.toString())
                            .append("\n")
                            .append("\t")
                            .append("getCapturePeriod = ")
                            .append(profile.getCapturePeriod())
                            .append("\n")
                            .append("\t")
                            .append("getNumberOfProfileChannels = ")
                            .append(profile.getNumberOfProfileChannels())
                            .append("\n")
                            .append("\t")
                            .append("getProfileEntries = ")
                            .append(profile.getProfileEntries())
                            .append("\n")
                            .append("\t")
                            .append("getResetCounter = ")
                            .append(profile.getResetCounter())
                            .append("\n")
                            .append("\t")
                            .append("getScalerUnit = ")
                            .append(profile.getScalerUnit())
                            .append("\n")
                            .append("\t")
                            .append("containsCapturedObjects = ")
                            .append(profile.containsCapturedObjects())
                            .append("\n")
                            .append("\t")
                            .append("getEntriesInUse = ")
                            .append(profile.getEntriesInUse())
                            .append("\n");
                }
            }
            builder.append("*********************************************************************************\n\n");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return builder.toString();
    }

    @Override
    public String getFirmwareVersion() throws IOException {
        return getCosemObjectFactory().getData(EK2xxRegisters.SOFTWARE_VERSION).getString();
    }

    @Override
    public Quantity getMeterReading(int channelId) throws IOException {
        throw new UnsupportedException("getMeterReading(int channelId) is not suported!!!");
    }

    @Override
    public Quantity getMeterReading(String name) throws IOException {
        throw new UnsupportedException("getMeterReading(String name) is not suported!!!");
    }

    @Override
    public ProfileData getProfileData(boolean includeEvents) throws IOException {
        Calendar calendar = Calendar.getInstance(getTimeZone());
        calendar.add(Calendar.MONTH, -2);
        return getProfileData(calendar.getTime(), includeEvents);
    }

    @Override
    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        Calendar calendar = Calendar.getInstance(getTimeZone());
        return getProfileData(lastReading, calendar.getTime(), includeEvents);
    }

    @Override
    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException {
        Date now = new Date();
        if (to.compareTo(now) >= 0) {
            to = now;
        }

        List<DataContainer> dataContainers = new ArrayList<>();
        ProfileData profileData = new ProfileData();
        DataContainer dc;
        Calendar fromCalendar = ProtocolUtils.getCleanCalendar(getTimeZone());
        Calendar toCalendar = ProtocolUtils.getCleanCalendar(getTimeZone());
        Calendar fromDate_ptr = ProtocolUtils.getCleanCalendar(getTimeZone());
        Calendar toDate_ptr = ProtocolUtils.getCleanCalendar(getTimeZone());
        Date profileDate;
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
                if (dataReceived) {
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
            List<MeterEvent> meterEvents = this.ek2xxProfile.getMeterEvents();
            profileData.getMeterEvents().addAll(meterEvents);
        }

        return profileData;
    }

    private CapturedObjects getCapturedObjects() throws IOException {
        return getEk2xxProfile().getCapturedObjects();
    }

    @Override
    public String getProtocolVersion() {
        return "$Date: 2015-11-26 15:23:39 +0200 (Thu, 26 Nov 2015)$";
    }

    @Override
    public String getRegister(String name) throws IOException {
        throw new UnsupportedException();
    }

    @Override
    public Date getTime() throws IOException {
        Clock clock = getCosemObjectFactory().getClock(EK2xxRegisters.CLOCK);
        Date date = clock.getDateTime();
        this.dstFlag = clock.getDstFlag();
        return date;
    }

    @Override
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

    @Override
    public void initializeDevice() throws IOException {
        throw new UnsupportedException("initializeDevice() is not suported!!!");
    }

    @Override
    public void release() throws IOException {
    }

    @Override
    public void setRegister(String name, String value) throws IOException {
        throw new UnsupportedException("setRegister() not suported!");
    }

    @Override
    public void setTime() throws IOException {
        Calendar calendar;
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

    }

    @Override
    public void enableHHUSignOn(SerialCommunicationChannel commChannel) throws ConnectionException {
        enableHHUSignOn(commChannel, false);
    }

    @Override
    public void enableHHUSignOn(SerialCommunicationChannel commChannel, boolean enableDataReadout) throws ConnectionException {
        HHUSignOn hhuSignOn = new IEC1107HHUConnection(commChannel, this.iHDLCTimeoutProperty, this.iProtocolRetriesProperty, 300, 0);
        hhuSignOn.setMode(HHUSignOn.MODE_BINARY_HDLC);
        hhuSignOn.setProtocol(HHUSignOn.PROTOCOL_HDLC);
        hhuSignOn.enableDataReadout(enableDataReadout);
        getDLMSConnection().setHHUSignOn(hhuSignOn, this.nodeId);
    }

    public String getPassword() {
        return this.strPassword;
    }

    @Override
    public byte[] getHHUDataReadout() {
        return getDLMSConnection().getHhuSignOn().getDataReadout();
    }

    @Override
    public DLMSConnection getDLMSConnection() {
        return this.dlmsConnection;
    }

    @Override
    public Logger getLogger() {
        return this.logger;
    }

    @Override
    public DLMSMeterConfig getMeterConfig() {
        return this.meterConfig;
    }

    @Override
    public int getReference() {
        return ProtocolLink.SN_REFERENCE;
    }

    @Override
    public int getRoundTripCorrection() {
        return this.iRoundtripCorrection;
    }

    @Override
    public StoredValues getStoredValues() {
        return this.storedValuesImpl;
    }

    @Override
    public TimeZone getTimeZone() {
        return this.timeZone;
    }

    @Override
    public boolean isRequestTimeZone() {
        return (this.iRequestTimeZone != 0);
    }

    @Override
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

    @Override
    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        String regType = getEk2xxRegisters().getObjectType(obisCode);
        String regName = getEk2xxRegisters().getObjectName(obisCode);
        return new RegisterInfo(regName + " - Type: " + regType);
    }

}