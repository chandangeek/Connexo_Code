package com.elster.protocolimpl.dlms;

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
import com.elster.protocolimpl.dlms.util.ProtocolLink;
import com.energyict.cbo.BusinessException;
import com.energyict.cbo.Quantity;
import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.InvalidPropertyException;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.MessageProtocol;
import com.energyict.protocol.MessageResult;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.MeterProtocol;
import com.energyict.protocol.MissingPropertyException;
import com.energyict.protocol.NoSuchRegisterException;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterProtocol;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.messaging.Message;
import com.energyict.protocol.messaging.MessageCategorySpec;
import com.energyict.protocol.messaging.MessageTag;
import com.energyict.protocol.messaging.MessageValue;
import com.energyict.protocolimpl.base.PluggableMeterProtocol;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.Logger;

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
public class Dlms extends PluggableMeterProtocol implements ProtocolLink, RegisterProtocol, MessageProtocol, EventProtocol, HasSimpleObjectManager {

    protected static String CLIENTID = "ClientID";
    protected static String SERVERADDRESS = "ServerAddress";
    protected static String DLMSSECURITYLEVEL = "DlmsSecurityLevel";
    protected static String ENCRYPTIONKEY = "EncryptionKey";
    protected static String AUTHENTICATIONKEY = "AuthenticationKey";
    protected static String RETIEVEOFFSET = "RetrieveOffset";
    protected static String LOGICALDEVICE = "LogicalDevice";
    protected static String USEMODEE = "UseModeE";
    protected static String OC_INTERVALPROFILE = "ObisCodeIntervalProfile";
    protected static String OC_LOGPROFILE = "ObisCodeLogProfile";
    protected static String ARCHIVESTRUCTURE = "ArchiveStructure";
    protected static String LOGSTRUCTURE = "LogStructure";
    protected static String MAXPDUSIZE = "ClientMaxReceivePduSize";

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

    /**
     * initialization -> create connection class
     */
    public void init(InputStream inputStream, OutputStream outputStream,
                     TimeZone timezone, Logger logger) throws IOException {

        connection = new DlmsConnection(inputStream, outputStream);
        this.timeZone = timezone;
        this.logger = logger;
    }

    public String getProtocolVersion() {
        return "$Date: 2011-06-10 16:05:38 +0200 (vr, 10 jun 2011) $";
    }


    @Override
    public List<PropertySpec> getRequiredProperties() {
        return PropertySpecFactory.toPropertySpecs(getRequiredKeys());
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        return PropertySpecFactory.toPropertySpecs(getOptionalKeys());
    }

    /**
     * the implementation returns both the address and password key
     *
     * @return a list of strings
     */
    @SuppressWarnings("unchecked")
    public List getRequiredKeys() {
        ArrayList<String> result = new ArrayList<String>();
        result.add(Dlms.DLMSSECURITYLEVEL);
        result.add(Dlms.CLIENTID);
        return result;
    }

    /**
     * List of optional keys
     *
     * @return a list of strings
     */
    @SuppressWarnings("unchecked")
    public List getOptionalKeys() {
        List result = new ArrayList();
        result.add("Timeout");
        result.add("Retries");

        result.add(Dlms.SERVERADDRESS);
        result.add(Dlms.LOGICALDEVICE);
        result.add(Dlms.AUTHENTICATIONKEY);
        result.add(Dlms.ENCRYPTIONKEY);
        result.add(Dlms.RETIEVEOFFSET);
        result.add(Dlms.USEMODEE);
        result.add(Dlms.OC_INTERVALPROFILE);
        result.add(Dlms.OC_LOGPROFILE);
        result.add(Dlms.ARCHIVESTRUCTURE);
        result.add(Dlms.LOGSTRUCTURE);
        result.add(Dlms.MAXPDUSIZE);

        List result2 = doGetOptionalKeys();
        if (result2 != null) {
            result.addAll(result2);
        }
        return result;
    }

    /**
     * enable derived class to add more keys
     *
     * @return a list of keys (Strings)
     */
    @SuppressWarnings("unchecked")
    protected List doGetOptionalKeys() {
        return null;
    }

    /**
     * set the protocol specific properties
     *
     * @param properties - properties to use
     */
    public void setProperties(Properties properties)
            throws InvalidPropertyException, MissingPropertyException {
        validateProperties(properties);
    }

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

        validateSerialNumber();
    }

    public void disconnect() throws IOException {
        if (connection != null) {
            connection.disconnect();
        }
    }

    public String getFirmwareVersion() throws IOException
    {
        if (firmwareVersion.length() > 0)
        {
            return firmwareVersion;
        }
        try
        {
            CosemApplicationLayer applicationLayer = connection.getApplicationLayer();
            DlmsData softwareVersionData = null;
            try
            {
                softwareVersionData =
                        applicationLayer.getAttributeAndCheckResult(new CosemAttributeDescriptor(
                                CommonDefs.SOFTWARE_VERSION, CosemClassIds.REGISTER, 2));
                if (ScalerUnit.isScalable(softwareVersionData))
                {
                    final DlmsData softwareVersionScalerData =
                            applicationLayer.getAttributeAndCheckResult(new CosemAttributeDescriptor(
                                    CommonDefs.SOFTWARE_VERSION, CosemClassIds.REGISTER, 3));
                    ScalerUnit scalerUnit = new ScalerUnit(softwareVersionScalerData);
                    firmwareVersion = scalerUnit.scale(softwareVersionData).toPlainString();
                    return firmwareVersion;
                }
            }
            catch (CosemDataAccessException ignore)
            {
            }

            if (softwareVersionData == null)
            {
                softwareVersionData =
                        applicationLayer.getAttributeAndCheckResult(new CosemAttributeDescriptor(
                                CommonDefs.SOFTWARE_VERSION, CosemClassIds.DATA, 2));
            }
            firmwareVersion = dataToString(softwareVersionData);
            return firmwareVersion;
        }
        catch (CosemAttributeValidators.ValidationExecption ex)
        {
            throw new IOException("Wrong data for scaler unit of the software version: ");
        }
    }

    private String dataToString(final DlmsData data)
    {

        if (data instanceof DlmsDataOctetString)
        {
            final DlmsDataOctetString octetString = (DlmsDataOctetString)data;
            return new String(octetString.getValue());
        }
        else
        {
            Object value = data.getValue();
            if (value != null)
            {
                return value.toString();
            }
            else
            {
                return "?";
            }
        }
    }

    /**
     * Validate the serialNumber of the device.
     *
     * @throws IOException if the serialNumber doesn't match the one from the Rtu
     */
    protected void validateSerialNumber() throws IOException {
        getLogger().info(
                "-- verifying serial number...");

        if ((this.serialNumber != null) && (this.serialNumber.length() > 0)) {

            SimpleCosemObjectManager scom = getObjectManager();
            String meterSerialNumber = scom.getSerialNumber();

            String meterSN_dev = adjustSN(meterSerialNumber);
            String meterSN_rtu = adjustSN(serialNumber);

            if (!meterSN_rtu.equals(meterSN_dev))
            {
                throw new IOException("Wrong serialnumber, EIServer settings: "
                        + this.serialNumber + " - Meter settings: "
                        + meterSerialNumber);
            }
        }
    }

    private String adjustSN(String meterSerialNumber)
    {
        if ((meterSerialNumber == null) || (meterSerialNumber.length() == 0))
        {
            return "";
        }
        meterSerialNumber = meterSerialNumber.trim();
        if (meterSerialNumber.length() == 8)
        {
            return meterSerialNumber;
        }
        if (meterSerialNumber.length() > 8)
        {
            return meterSerialNumber.substring(meterSerialNumber.length() - 8);
        }
        String result = meterSerialNumber;
        while (result.length() < 8)
        {
            result = "0" + result;
        }
        return result;
    }

    public int getNumberOfChannels() throws IOException {
        return getProfileObject().getNumberOfChannels();
    }

    public CosemApplicationLayer getCosemApplicationLayer() {
        return connection.getApplicationLayer();
    }

    public ProfileData getProfileData(boolean includeEvents) throws IOException {
        Calendar calendar = Calendar.getInstance(getTimeZone());
        /* maximum readout range set to 2 year - 6/18/2010 gh */
        calendar.add(Calendar.MONTH, -24);
        return getProfileData(calendar.getTime(), includeEvents);
    }

    public ProfileData getProfileData(Date lastReading, boolean includeEvents)
            throws IOException {
        return getProfileData(lastReading, new Date(), includeEvents);
    }

    public ProfileData getProfileData(Date from, Date to, boolean includeEvents)
            throws IOException {

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

    public List<MeterEvent> getMeterEvents(Date from)
    {
        return getMeterEvents(from, new Date());
    }

    public List<MeterEvent> getMeterEvents(Date from, Date to)
    {
        try
        {
            return getLogProfileObject().getMeterEvents(from, to);
        } catch (IOException e) {
            getLogger().severe(e.getMessage());
            return null;
        }
    }

    // *******************************************************************************************
    // * C l a s s i m p l e m e n t a t i o n c o d e

    public int getProfileInterval() throws IOException {
        return getProfileObject().getInterval();
    }

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

    public void setTime() throws IOException {

        SimpleCosemObjectManager objectManager = getObjectManager();
        SimpleClockObject clockObject =
                objectManager.getSimpleCosemObject(Ek280Defs.CLOCK_OBJECT, SimpleClockObject.class);
        clockObject.shiftTimeToSystemTime();
    }

    public String getRegister(String s) throws IOException {
        return null;
    }

    public void setRegister(String arg0, String arg1) throws IOException {
        /* dsfg register instances have no register values ! */
        throw new NoSuchRegisterException("Register setting currently not supported!");
    }

    public RegisterValue readRegister(com.energyict.obis.ObisCode obisCode) throws IOException {
        return getRegisterReader().getRegisterValue(obisCode, new Date());
    }

    /**
     * Getter for the ObisCodeMapper. getRegisterMap() has to be
     * overridden by the derived class.
     *
     * @return the used ObisCodeMapper}
     */
    protected DlmsRegisterReader getRegisterReader()
    {
        if (this.ocMapper == null)
        {
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

    public void initializeDevice() throws IOException {
    }


    public void release() throws IOException {
    }

    // *******************************************************************************************/

    /**
     * Validate certain protocol specific properties
     *
     * @param properties - The properties fetched from the Rtu
     * @throws MissingPropertyException - in case of a missing property
     * @throws InvalidPropertyException - in case of a invalid value in property
     */
    @SuppressWarnings({"unchecked"})
    protected void validateProperties(Properties properties)
            throws MissingPropertyException, InvalidPropertyException {
        try {
            //System.out.println("DLMS driver started with properties:");
            //Enumeration keys = properties.keys();
            //while (keys.hasMoreElements()) {
            //    String key = (String) keys.nextElement();
            //    String value = (String) properties.get(key);
            //    System.out.println(key + ": <" + value + ">");
            //}

            for (String key : (List<String>) getRequiredKeys()) {
                if (properties.getProperty(key) == null) {
                    throw new MissingPropertyException(key + " key missing");
                }
            }
            strPassword = properties.getProperty(MeterProtocol.PASSWORD);
            protocolRetriesProperty = Integer.parseInt(properties.getProperty(
                    "Retries", "5").trim());
            //extendedLogging = Integer.parseInt(properties.getProperty(
            //        "ExtendedLogging", "0").trim());

            serialNumber = properties.getProperty(MeterProtocol.SERIALNUMBER);

            clientID = getPropertyAsInteger(properties.getProperty(Dlms.CLIENTID));
            serverAddress = getPropertyAsInteger(properties.getProperty(Dlms.SERVERADDRESS, "5959"));
            logicalDevice = getPropertyAsInteger(properties.getProperty(Dlms.LOGICALDEVICE, "0"));

            timeout = getPropertyAsInteger(properties.getProperty("Timeout", "-1"));

            String dsl = properties.getProperty(Dlms.DLMSSECURITYLEVEL);
            if (!dsl.contains(":")) {
                throw new InvalidPropertyException("Security data: Invalid data (" + dsl + ")");
            }
            securityData = new SecurityData(dsl);
            securityData.setEncryptionKey(properties.getProperty(Dlms.ENCRYPTIONKEY, ""));
            securityData.setAuthenticationKey(properties.getProperty(Dlms.AUTHENTICATIONKEY, ""));
            String msg = securityData.checkSecurityData();
            if (msg.length() != 0) {
                throw new InvalidPropertyException("Security data: " + msg);
            }

            useModeE = getPropertyAsInteger(properties.getProperty(Dlms.USEMODEE, "0")) == 1;

            // debugging tools...
            retrieveOffset = getPropertyAsInteger(properties.getProperty(Dlms.RETIEVEOFFSET, "0"));
            doValidateProperties(properties);

            String structure = properties.getProperty(Dlms.ARCHIVESTRUCTURE);
            if ((structure != null) && (structure.length() > 0)) {
                archiveStructure = structure;
            }
            structure = properties.getProperty(Dlms.LOGSTRUCTURE);
            if ((structure != null) && (structure.length() > 0)) {
                logStructure = structure;
            }

            // check in an obis code for interval profile is defined
            String ocIntervalProfile = properties.getProperty(Dlms.OC_INTERVALPROFILE, "");
            if ((ocIntervalProfile != null) && (ocIntervalProfile.length() > 0)) {
                this.ocIntervalProfile = new ObisCode(ocIntervalProfile);
            }
            String ocLogProfile = properties.getProperty(Dlms.OC_LOGPROFILE, "");
            if ((ocLogProfile != null) && (ocLogProfile.length() > 0)) {
                this.ocLogProfile = new ObisCode(ocLogProfile);
            }

            if (this.ocIntervalProfile == null) {
                throw new InvalidPropertyException(" validateProperties, no obis code for interval profile defined");
            }

            clientMaxReceivePduSize = getPropertyAsInteger(properties.getProperty(Dlms.MAXPDUSIZE, "0"));

        } catch (NumberFormatException e) {
            throw new InvalidPropertyException(" validateProperties, NumberFormatException, "
                    + e.getMessage());
        }
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
            throws IOException
    {
        if (logProfile == null)
        {
            SimpleProfileObject profileObject = (SimpleProfileObject) getObjectManager().getSimpleCosemObject(ocLogProfile);
            logProfile = ArchiveProcessorFactory.createLogProcessor(meterType, logStructure, profileObject, getTimeZone(), getLogger());
        }
        return logProfile;
    }

    @SuppressWarnings({"unused"})
    public void doValidateProperties(Properties properties) {
    }

    // *******************************************************************************************
    // *
    // * Interface HasSimpleObjectManager
    // *
    // *******************************************************************************************/
    public SimpleCosemObjectManager getObjectManager() {
        if (objectManager == null) {
            objectManager = new SimpleCosemObjectManager(getDlmsConnection().getApplicationLayer(), Ek280Defs.DEFINITIONS);
        }
        return objectManager;
    }


    // *******************************************************************************************
    // *
    // * Interface ProtocolLink
    // *
    // *******************************************************************************************/
    public byte[] getDataReadout() {
        return null;
    }

    public Logger getLogger() {
        return logger;
    }

    public int getNrOfRetries() {
        return protocolRetriesProperty;
    }

    public String getPassword() {
        return strPassword;
    }

    public DlmsConnection getDlmsConnection() {
        return connection;
    }

    public TimeZone getTimeZone() {
        return timeZone;
    }

    public boolean isIEC1107Compatible() {
        return false;
    }

    public boolean isRequestHeader() {
        return false;
    }

    // *******************************************************************************************
    // *
    // * not yet used methods
    // *
    // *******************************************************************************************/
    public Object fetchCache(int arg0) throws SQLException, BusinessException {
        return null;
    }

    public Object getCache() {
        return null;
    }

    public void updateCache(int arg0, Object arg1) throws SQLException,
            BusinessException {
    }

    public void setCache(Object arg0) {
    }

    // *******************************************************************************************
    // *
    // * deprecicated methods
    // *
    // *******************************************************************************************/
    @Deprecated
    public Quantity getMeterReading(int arg0) throws IOException {
        return null;
    }

    @Deprecated
    public Quantity getMeterReading(String arg0) throws IOException {
        return null;
    }


    public RegisterInfo translateRegister(com.energyict.obis.ObisCode obisCode) throws IOException {
        return new RegisterInfo("");
    }

    // *******************************************************************************************
    // *
    // * "message" methods
    // *
    // *******************************************************************************************/
    public void applyMessages(List messageEntries)
            throws IOException
    {
    }

    public MessageResult queryMessage(MessageEntry messageEntry) throws IOException {
        return getMessageExecutor().doMessage(messageEntry);
    }

    public List<MessageCategorySpec> getMessageCategories() {
        return getMessageExecutor().getMessageCategories();
    }

    public String writeMessage(Message msg) {
        return new XmlMessageWriter().writeMessage(msg);
    }

    public String writeTag(MessageTag tag) {
        return new XmlMessageWriter().writeTag(tag);
    }

    public String writeValue(MessageValue value) {
        return new XmlMessageWriter().writeValue(value);
    }

    public DlmsMessageExecutor getMessageExecutor() {
        if (messageExecutor == null) {
            messageExecutor = new DlmsMessageExecutor(this);
        }
        return messageExecutor;
    }

    public ObjectPool getObjectPool()
    {
        return null;
    }

    public int getSoftwareVersion()
    {
        return 0;
    }
}
