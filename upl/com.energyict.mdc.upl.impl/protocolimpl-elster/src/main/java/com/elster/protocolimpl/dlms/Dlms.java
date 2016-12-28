package com.elster.protocolimpl.dlms;

import com.energyict.mdc.upl.NoSuchRegisterException;
import com.energyict.mdc.upl.messages.legacy.Message;
import com.energyict.mdc.upl.messages.legacy.MessageCategorySpec;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.MessageTag;
import com.energyict.mdc.upl.messages.legacy.MessageValue;
import com.energyict.mdc.upl.properties.InvalidPropertyException;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.properties.TypedProperties;

import com.elster.dlms.cosem.application.services.common.DataAccessResult;
import com.elster.dlms.cosem.application.services.get.GetDataResult;
import com.elster.dlms.cosem.applicationlayer.CosemApplicationLayer;
import com.elster.dlms.cosem.applicationlayer.CosemDataAccessException;
import com.elster.dlms.cosem.classes.class03.ScalerUnit;
import com.elster.dlms.cosem.classes.common.CosemClassIds;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators;
import com.elster.dlms.cosem.simpleobjectmodel.CommonDefs;
import com.elster.dlms.cosem.simpleobjectmodel.Ek280Defs;
import com.elster.dlms.cosem.simpleobjectmodel.SimpleClockObject;
import com.elster.dlms.cosem.simpleobjectmodel.SimpleCosemObjectManager;
import com.elster.dlms.cosem.simpleobjectmodel.SimpleProfileObject;
import com.elster.dlms.types.basic.CosemAttributeDescriptor;
import com.elster.dlms.types.basic.DlmsDateTime;
import com.elster.dlms.types.basic.ObisCode;
import com.elster.dlms.types.data.DlmsData;
import com.elster.dlms.types.data.DlmsDataOctetString;
import com.elster.protocolimpl.dlms.connection.DlmsConnection;
import com.elster.protocolimpl.dlms.messaging.DlmsMessageExecutor;
import com.elster.protocolimpl.dlms.messaging.XmlMessageWriter;
import com.elster.protocolimpl.dlms.objects.ObjectPool;
import com.elster.protocolimpl.dlms.profile.ArchiveProcessorFactory;
import com.elster.protocolimpl.dlms.profile.DlmsProfile;
import com.elster.protocolimpl.dlms.profile.ILogProcessor;
import com.elster.protocolimpl.dlms.registers.DlmsRegisterReader;
import com.elster.protocolimpl.dlms.registers.DlmsSimpleRegisterDefinition;
import com.elster.protocolimpl.dlms.registers.RegisterMap;
import com.elster.protocolimpl.dlms.util.ElsterProtocolIOExceptionHandler;
import com.elster.protocolimpl.dlms.util.ProtocolLink;
import com.energyict.cbo.Quantity;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.MessageProtocol;
import com.energyict.protocol.MessageResult;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterProtocol;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.support.SerialNumberSupport;
import com.energyict.protocolimpl.base.PluggableMeterProtocol;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Logger;

import static com.energyict.mdc.upl.MeterProtocol.Property.PASSWORD;
import static com.energyict.mdc.upl.MeterProtocol.Property.RETRIES;
import static com.energyict.mdc.upl.MeterProtocol.Property.SERIALNUMBER;
import static com.energyict.mdc.upl.MeterProtocol.Property.TIMEOUT;

/**
 * ProtocolImplementation for DLMS devices. <br>
 * <br>
 * <p/>
 * <b>General Description:</b><br>
 * <br>
 * <br>
 * <b>Data interface:</b><br>
 * <li>Optical interface according to IEC1107 <li>Internal GSM modem <br>
 * <br>
 * <b>Additional information:</b><br>
 *
 * @author gh
 * @since 5-mai-2010
 */
@SuppressWarnings("unused")
public class Dlms extends PluggableMeterProtocol implements ProtocolLink, RegisterProtocol, MessageProtocol, EventProtocol, HasSimpleObjectManager, SerialNumberSupport {

    private static final String CLIENTID = "ClientID";
    private static final String SERVERADDRESS = "ServerAddress";
    private static final String DLMSSECURITYLEVEL = "DlmsSecurityLevel";
    private static final String ENCRYPTIONKEY = "EncryptionKey";
    private static final String AUTHENTICATIONKEY = "AuthenticationKey";
    private static final String RETRIEVEOFFSET = "RetrieveOffset";
    private static final String LOGICALDEVICE = "LogicalDevice";
    private static final String USEMODEE = "UseModeE";
    private static final String OC_INTERVALPROFILE = "ObisCodeIntervalProfile";
    private static final String OC_LOGPROFILE = "ObisCodeLogProfile";
    private static final String MAXPDUSIZE = "ClientMaxReceivePduSize";

    protected static final String ARCHIVESTRUCTURE = "ArchiveStructure";
    protected static final String LOGSTRUCTURE = "LogStructure";

    protected static DlmsSimpleRegisterDefinition[] mappings = {};

    /**
     * time zone of device
     */
    protected TimeZone timeZone;
    /**
     * reference to logger
     */
    protected Logger logger;
    /**
     * serial number
     */
    protected String serialNumber = "";
    /**
     * class of dlms connection
     */
    protected DlmsConnection connection;
    /**
     * class for simple data access
     */
    protected SimpleCosemObjectManager objectManager = null;
    /**
     * Obis code mapper for register reading
     */
    protected DlmsRegisterReader ocMapper = null;

    /* password from given properties */
    protected String strPassword;
    /* client id for Dlms connection */
    protected int clientID;
    protected int serverAddress;
    protected int logicalDevice;
    SecurityData securityData;

    protected boolean useModeE;
    protected long retrieveOffset = 0;
    protected int timeout = -1;
    protected int clientMaxReceivePduSize = 0;
    /**
     * compatibility properties..., currently not used
     */
    protected int protocolRetriesProperty;
    //
    protected String firmwareVersion = "";

    /**
     * factory for common data objects
     */
    protected DlmsObjectFactory objectFactory = null;

    /**
     * type of meter
     */
    protected String meterType;

    /**
     * The DlmsMessageExecutor is responsible for the handling of the messages to the device
     */
    protected DlmsMessageExecutor messageExecutor = null;

    /*
     *Archive structure information
     */
    protected String archiveStructure = "";
    protected ObisCode ocIntervalProfile = null;
    protected DlmsProfile intervalProfile = null;

    protected String logStructure = "";
    protected ObisCode ocLogProfile = null;
    protected ILogProcessor logProfile = null;

    public Dlms() {
        super(propertySpecService);
    }

    @Override
    public void init(
            InputStream inputStream, OutputStream outputStream,
            TimeZone timezone, Logger logger) throws IOException {
        connection = new DlmsConnection(inputStream, outputStream);
        this.timeZone = timezone;
        this.logger = logger;
    }

    @Override
    public String getProtocolVersion() {
        return "$Date: 2015-11-26 15:25:57 +0200 (Thu, 26 Nov 2015)$";
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return Arrays.asList(
                UPLPropertySpecFactory.string(PASSWORD.getName(), false),
                UPLPropertySpecFactory.integer(RETRIES.getName(), false),
                UPLPropertySpecFactory.string(SERIALNUMBER.getName(), false),
                new VariableBaseIntegerPropertySpec(CLIENTID, true),
                new VariableBaseIntegerPropertySpec(SERVERADDRESS, false),
                new VariableBaseIntegerPropertySpec(LOGICALDEVICE, false),
                new VariableBaseIntegerPropertySpec(TIMEOUT.getName(), false),
                new SecurityLevelPropertySpec(DLMSSECURITYLEVEL, true),
                UPLPropertySpecFactory.string(ENCRYPTIONKEY, false),
                UPLPropertySpecFactory.string(AUTHENTICATIONKEY, false),
                new VariableBaseIntegerPropertySpec(USEMODEE, false),
                new VariableBaseIntegerPropertySpec(RETRIEVEOFFSET, false),
                UPLPropertySpecFactory.string(ARCHIVESTRUCTURE, false),
                new ObisCodePropertySpec(LOGSTRUCTURE, false),
                new ObisCodePropertySpec(OC_INTERVALPROFILE, false),
                new VariableBaseIntegerPropertySpec(MAXPDUSIZE, false));
    }

    @Override
    public void setProperties(TypedProperties properties) throws PropertyValidationException {
        try {
            strPassword = properties.getTypedProperty(PASSWORD.getName());
            protocolRetriesProperty = Integer.parseInt(properties.getTypedProperty(RETRIES.getName(), "5").trim());
            serialNumber = properties.getTypedProperty(Property.SERIALNUMBER.getName());

            clientID = getPropertyAsInteger(properties.getTypedProperty(Dlms.CLIENTID));
            serverAddress = getPropertyAsInteger(properties.getTypedProperty(Dlms.SERVERADDRESS, "5959"));
            logicalDevice = getPropertyAsInteger(properties.getTypedProperty(Dlms.LOGICALDEVICE, "0"));

            timeout = getPropertyAsInteger(properties.getTypedProperty("Timeout", "-1"));

            String dsl = properties.getTypedProperty(Dlms.DLMSSECURITYLEVEL);
            securityData = new SecurityData(dsl);
            securityData.setEncryptionKey(properties.getTypedProperty(Dlms.ENCRYPTIONKEY, ""));
            securityData.setAuthenticationKey(properties.getTypedProperty(Dlms.AUTHENTICATIONKEY, ""));
            String msg = securityData.checkSecurityData();
            if (!msg.isEmpty()) {
                throw new InvalidPropertyException("Security data: " + msg);
            }

            useModeE = getPropertyAsInteger(properties.getTypedProperty(Dlms.USEMODEE, "0")) == 1;

            // debugging tools...
            retrieveOffset = getPropertyAsInteger(properties.getTypedProperty(Dlms.RETRIEVEOFFSET, "0"));

            String archiveStructurePropertyValue = properties.getTypedProperty(Dlms.ARCHIVESTRUCTURE);
            if ((archiveStructurePropertyValue != null) && (!archiveStructurePropertyValue.isEmpty())) {
                archiveStructure = archiveStructurePropertyValue;
            }
            String logStructurePropertyValue = properties.getTypedProperty(Dlms.LOGSTRUCTURE);
            if ((logStructurePropertyValue != null) && (!logStructurePropertyValue.isEmpty())) {
                this.logStructure = logStructurePropertyValue;
            }

            // check if an obis code for interval profile is defined
            String ocIntervalProfile = properties.getTypedProperty(Dlms.OC_INTERVALPROFILE, "");
            if ((ocIntervalProfile != null) && (!ocIntervalProfile.isEmpty())) {
                this.ocIntervalProfile = new ObisCode(ocIntervalProfile);
            }
            String ocLogProfile = properties.getTypedProperty(Dlms.OC_LOGPROFILE, "");
            if ((ocLogProfile != null) && (!ocLogProfile.isEmpty())) {
                this.ocLogProfile = new ObisCode(ocLogProfile);
            }

            if (this.ocIntervalProfile == null) {
                throw new InvalidPropertyException(" validateProperties, no obis code for interval profile defined");
            }

            clientMaxReceivePduSize = getPropertyAsInteger(properties.getTypedProperty(Dlms.MAXPDUSIZE, "0"));

        } catch (NumberFormatException e) {
            throw new InvalidPropertyException(e, this.getClass().getSimpleName() + ": validation of properties failed before");
        }
    }

    @Override
    public void connect() throws IOException {

        connection.connect(serverAddress, logicalDevice, clientID, securityData, useModeE, timeout, clientMaxReceivePduSize);

        connection.signon(strPassword);

        GetDataResult dataResult1 =
                connection.getApplicationLayer().getAttribute(1, new ObisCode(7, 128, 0, 0, 2, 255), 2, null, null);

        // Very simple example to extract the value
        // Normally the scaler from attribute 3 must be applied to use this value.--->
        if (dataResult1.getAccessResult() == DataAccessResult.SUCCESS
                && dataResult1.getData() != null) {
            meterType = (String) dataResult1.getData().getValue();
        }
        getLogger().info("-- Type of device: " + meterType.trim());
    }

    @Override
    public void disconnect() throws IOException {
        if (connection != null) {
            connection.disconnect();
        }
    }

    @Override
    public String getFirmwareVersion() throws IOException {
        if (!firmwareVersion.isEmpty()) {
            return firmwareVersion;
        }
        try {
            CosemApplicationLayer applicationLayer = connection.getApplicationLayer();
            DlmsData softwareVersionData = null;
            try {
                softwareVersionData =
                        applicationLayer.getAttributeAndCheckResult(new CosemAttributeDescriptor(
                                CommonDefs.SOFTWARE_VERSION, CosemClassIds.REGISTER, 2));
                if (ScalerUnit.isScalable(softwareVersionData)) {
                    final DlmsData softwareVersionScalerData =
                            applicationLayer.getAttributeAndCheckResult(new CosemAttributeDescriptor(
                                    CommonDefs.SOFTWARE_VERSION, CosemClassIds.REGISTER, 3));
                    ScalerUnit scalerUnit = new ScalerUnit(softwareVersionScalerData);
                    firmwareVersion = scalerUnit.scale(softwareVersionData).toPlainString();
                    return firmwareVersion;
                }
            } catch (CosemDataAccessException ignore) {
            }

            if (softwareVersionData == null) {
                softwareVersionData =
                        applicationLayer.getAttributeAndCheckResult(new CosemAttributeDescriptor(
                                CommonDefs.SOFTWARE_VERSION, CosemClassIds.DATA, 2));
            }
            firmwareVersion = dataToString(softwareVersionData);
            return firmwareVersion;
        } catch (CosemAttributeValidators.ValidationExecption ex) {
            throw new IOException("Wrong data for scaler unit of the software version: ");
        }
    }

    private String dataToString(final DlmsData data) {
        if (data instanceof DlmsDataOctetString) {
            final DlmsDataOctetString octetString = (DlmsDataOctetString) data;
            return new String(octetString.getValue());
        } else {
            Object value = data.getValue();
            if (value != null) {
                return value.toString();
            } else {
                return "?";
            }
        }
    }

    private String adjustSN(String meterSerialNumber) {
        if ((meterSerialNumber == null) || (meterSerialNumber.isEmpty())) {
            return "";
        }
        meterSerialNumber = meterSerialNumber.trim();
        if (meterSerialNumber.length() == 8) {
            return meterSerialNumber;
        }
        if (meterSerialNumber.length() > 8) {
            return meterSerialNumber.substring(meterSerialNumber.length() - 8);
        }
        String result = meterSerialNumber;
        while (result.length() < 8) {
            result = "0" + result;
        }
        return result;
    }

    @Override
    public int getNumberOfChannels() throws IOException {
        return getProfileObject().getNumberOfChannels();
    }

    public CosemApplicationLayer getCosemApplicationLayer() {
        return connection.getApplicationLayer();
    }

    @Override
    public ProfileData getProfileData(boolean includeEvents) throws IOException {
        Calendar calendar = Calendar.getInstance(getTimeZone());
        /* maximum readout range set to 2 year - 6/18/2010 gh */
        calendar.add(Calendar.MONTH, -24);
        return getProfileData(calendar.getTime(), includeEvents);
    }

    @Override
    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        return getProfileData(lastReading, new Date(), includeEvents);
    }

    @Override
    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException {
        if (retrieveOffset > 0) {
            from.setTime(to.getTime() - retrieveOffset);
        }
        getLogger().info("getProfileData(" + from + "," + to + ")");

        ProfileData profileData = null;

        try {
            profileData = new ProfileData();

            profileData.setChannelInfos(getProfileObject().buildChannelInfo());

            List<IntervalData> ivd = getProfileObject().getIntervalData(from, to);

            //TODO: remove double entries...
            profileData.setIntervalDatas(ivd);

            if (includeEvents && (getLogProfileObject() != null)) {
                profileData.setMeterEvents(getMeterEvents(from, to));
            }
        } catch (IOException e) {
            getLogger().severe(e.getMessage());
        }

        return profileData;
    }

    @Override
    public List<MeterEvent> getMeterEvents(Date from) {
        return getMeterEvents(from, new Date());
    }

    @Override
    public List<MeterEvent> getMeterEvents(Date from, Date to) {
        try {
            return getLogProfileObject().getMeterEvents(from, to);
        } catch (IOException e) {
            getLogger().severe(e.getMessage());
            return null;
        }
    }

    @Override
    public int getProfileInterval() throws IOException {
        return getProfileObject().getInterval();
    }

    @Override
    public Date getTime() throws IOException {
        Calendar c = Calendar.getInstance(this.getTimeZone());

        DlmsDateTime ddt = getObjectManager().getDateTime();

        c.set(Calendar.YEAR, ddt.getDlmsDate().getYear());
        c.set(Calendar.MONTH, ddt.getDlmsDate().getMonth() - 1);
        c.set(Calendar.DAY_OF_MONTH, ddt.getDlmsDate().getDayOfMonth());
        c.set(Calendar.HOUR_OF_DAY, ddt.getDlmsTime().getHour());
        c.set(Calendar.MINUTE, ddt.getDlmsTime().getMinute());
        c.set(Calendar.SECOND, ddt.getDlmsTime().getSecond());
        c.set(Calendar.MILLISECOND, ddt.getDlmsTime().getHundredths());

        System.out.println(c.getTime());

        return c.getTime();
    }

    @Override
    public void setTime() throws IOException {

        SimpleCosemObjectManager objectManager = getObjectManager();
        SimpleClockObject clockObject =
                objectManager.getSimpleCosemObject(Ek280Defs.CLOCK_OBJECT, SimpleClockObject.class);
        clockObject.shiftTimeToSystemTime();
    }

    @Override
    public String getRegister(String s) throws IOException {
        return null;
    }

    @Override
    public void setRegister(String arg0, String arg1) throws IOException {
        /* dsfg register instances have no register values ! */
        throw new NoSuchRegisterException("Register setting currently not supported!");
    }

    @Override
    public RegisterValue readRegister(com.energyict.obis.ObisCode obisCode) throws IOException {
        return getRegisterReader().getRegisterValue(obisCode, new Date());
    }

    /**
     * Getter for the ObisCodeMapper. getRegisterMap() has to be
     * overridden by the derived class.
     *
     * @return the used ObisCodeMapper}
     */
    protected DlmsRegisterReader getRegisterReader() {
        if (this.ocMapper == null) {
            this.ocMapper = new DlmsRegisterReader(connection.getApplicationLayer(), this, getRegisterMap(), getObjectManager());
        }
        return this.ocMapper;
    }

    /**
     * Gets an empty register map
     *
     * @return an empty RegisterMap
     */
    protected RegisterMap getRegisterMap() {

        return new RegisterMap(mappings);
    }

    @Override
    public void initializeDevice() throws IOException {
    }

    @Override
    public void release() throws IOException {
    }

    private int getPropertyAsInteger(String value) {
        int base = 10;
        if (value.toUpperCase().startsWith("0X")) {
            base = 16;
            value = value.substring(2);
        }
        return Integer.parseInt(value, base);
    }

    /**
     * Getter for the ObjectFactory
     *
     * @return the current ObjectFactory
     */
    @SuppressWarnings({"unused"})
    protected DlmsObjectFactory getObjectFactory() {
        if (this.objectFactory == null) {
            this.objectFactory = new DlmsObjectFactory(this);
        }
        return this.objectFactory;
    }

    /**
     * Getter for dsfg profile object
     *
     * @return DsfgProfile object
     */
    @SuppressWarnings({"unused"})
    protected DlmsProfile getProfileObject() throws IOException {
        if (intervalProfile == null) {
            SimpleProfileObject profileObject = (SimpleProfileObject) getObjectManager().getSimpleCosemObject(ocIntervalProfile);
            intervalProfile = new DlmsProfile(this, meterType, archiveStructure, profileObject);
        }
        return intervalProfile;
    }

    protected ILogProcessor getLogProfileObject()
            throws IOException {
        if (logProfile == null) {
            SimpleProfileObject profileObject = (SimpleProfileObject) getObjectManager().getSimpleCosemObject(ocLogProfile);
            logProfile = ArchiveProcessorFactory.createLogProcessor(meterType, logStructure, profileObject, getTimeZone(), getLogger());
        }
        return logProfile;
    }

    @Override
    public SimpleCosemObjectManager getObjectManager() {
        if (objectManager == null) {
            objectManager = new SimpleCosemObjectManager(getDlmsConnection().getApplicationLayer(), Ek280Defs.DEFINITIONS);
        }
        return objectManager;
    }

    @Override
    public byte[] getDataReadout() {
        return null;
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    public int getNrOfRetries() {
        return protocolRetriesProperty;
    }

    @Override
    public String getPassword() {
        return strPassword;
    }

    @Override
    public DlmsConnection getDlmsConnection() {
        return connection;
    }

    @Override
    public TimeZone getTimeZone() {
        return timeZone;
    }

    @Override
    public boolean isIEC1107Compatible() {
        return false;
    }

    @Override
    public boolean isRequestHeader() {
        return false;
    }

    @Override
    public Quantity getMeterReading(int arg0) throws IOException {
        return null;
    }

    @Override
    public Quantity getMeterReading(String arg0) throws IOException {
        return null;
    }

    @Override
    public RegisterInfo translateRegister(com.energyict.obis.ObisCode obisCode) throws IOException {
        return new RegisterInfo("");
    }

    @Override
    public void applyMessages(List messageEntries)
            throws IOException {
    }

    @Override
    public MessageResult queryMessage(MessageEntry messageEntry) throws IOException {
        return getMessageExecutor().doMessage(messageEntry);
    }

    @Override
    public List<MessageCategorySpec> getMessageCategories() {
        return getMessageExecutor().getMessageCategories();
    }

    @Override
    public String writeMessage(Message msg) {
        return new XmlMessageWriter().writeMessage(msg);
    }

    @Override
    public String writeTag(MessageTag tag) {
        return new XmlMessageWriter().writeTag(tag);
    }

    @Override
    public String writeValue(MessageValue value) {
        return new XmlMessageWriter().writeValue(value);
    }

    public DlmsMessageExecutor getMessageExecutor() {
        if (messageExecutor == null) {
            messageExecutor = new DlmsMessageExecutor(this);
        }
        return messageExecutor;
    }

    public ObjectPool getObjectPool() {
        return null;
    }

    public int getSoftwareVersion() {
        return 0;
    }

    @Override
    public String getSerialNumber() {
        SimpleCosemObjectManager scom = getObjectManager();
        try {
            return scom.getSerialNumber();
        } catch (IOException e) {
            throw ElsterProtocolIOExceptionHandler.handle(e, getNrOfRetries() + 1);
        }
    }

}