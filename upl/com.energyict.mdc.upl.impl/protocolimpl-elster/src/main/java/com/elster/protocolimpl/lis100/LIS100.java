package com.elster.protocolimpl.lis100;

import com.energyict.mdc.upl.NoSuchRegisterException;
import com.energyict.mdc.upl.properties.InvalidPropertyException;
import com.energyict.mdc.upl.properties.MissingPropertyException;

import com.elster.protocolimpl.lis100.connection.Lis100Connection;
import com.elster.protocolimpl.lis100.profile.Lis100Profile;
import com.elster.protocolimpl.lis100.registers.Lis100Register;
import com.elster.protocolimpl.lis100.registers.RegisterMap;
import com.elster.protocolimpl.lis100.registers.SimpleObisCodeMapper;
import com.energyict.cbo.BusinessException;
import com.energyict.cbo.Quantity;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterProtocol;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.base.PluggableMeterProtocol;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.Logger;

import static com.energyict.protocolimpl.utils.ProtocolTools.delay;

/**
 * ProtocolImplementation for LIS100 devices.
 * <p/>
 * General Description:
 *
 * @author gh
 * @since 5-mai-2010
 */
@SuppressWarnings({"unused"})
public class LIS100 extends PluggableMeterProtocol implements ProtocolLink, RegisterProtocol {

    private final Lis100Register[] registers = {};

    /* time zone of device */
    private TimeZone timeZone;
    /* reference to logger */
    private Logger logger;
    /* class of lis100 protocol */
    private Lis100Connection connection;

    private Lis100Profile profile = null;

    /**
     * password from given properties
     */
    private String strPassword;
    /**
     * compatibility properties..., currently not used
     */
    private int protocolRetriesProperty;
    /**
     * factory for common data objects
     */
    private Lis100ObjectFactory objectFactory = null;

    /* class to hold all data of a lis100 device */
    private DeviceData deviceData = null;
    /* class to get values as registers */
    private SimpleObisCodeMapper obisCodeMapper = null;
    /* Serial number to verify */
    protected String serialNumber;

    /**
     * initialization -> create connection class
     */
    public void init(InputStream inputStream, OutputStream outputStream,
                     TimeZone timezone, Logger logger) throws IOException {
        connection = new Lis100Connection(inputStream, outputStream);
        this.timeZone = timezone;
        this.logger = logger;
    }

    public String getProtocolVersion() {
        return "$Date: 2012-01-09 15:06:09 +0100 (ma, 09 jan 2012) $";
    }

    @Override
    public List<String> getRequiredKeys() {
        return Collections.emptyList();
    }

    @Override
    public List<String> getOptionalKeys() {
        return Arrays.asList("Timeout", "Retries");
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

        connection.connect();

        delay(250);
        connection.signon(strPassword);

        deviceData = new DeviceData(getObjectFactory(), timeZone);
        deviceData.prepareDeviceData();

        // verify device type
        getLogger().info("-- Type of device: " + deviceData.getMeterType());
        getLogger().info("---- has channels: " + deviceData.getNumberOfChannels());

        if ((serialNumber != null) && (!serialNumber.isEmpty()))
        {
            verifySerialNumber();
        }
    }

    public void disconnect() throws IOException {
        connection.disconnect();
    }

    public void verifySerialNumber() throws IOException
    {

    }

    public String getFirmwareVersion() throws IOException {
        return String.format("%d.%d", deviceData.getSoftwareVersion() / 100, deviceData.getSoftwareVersion() % 100);
    }

    public int getNumberOfChannels() {
        return deviceData.getNumberOfChannels();
    }

    public ProfileData getProfileData(boolean includeEvents) throws IOException {
        Calendar calendar = Calendar.getInstance(getTimeZone());
        /* maximum readout range set to 2 year */
        calendar.add(Calendar.MONTH, -24);
        return getProfileData(calendar.getTime(), includeEvents);
    }

    public ProfileData getProfileData(Date lastReading, boolean includeEvents)
            throws IOException {
        return getProfileData(lastReading, new Date(), includeEvents);
    }

    public ProfileData getProfileData(Date from, Date to, boolean includeEvents)
            throws IOException {

        getLogger().info("getProfileData(" + from + "," + to + "," + includeEvents + ")");

        ProfileData profileData = new ProfileData();

        profileData.setIntervalDatas(getProfileObject().getIntervalData(from, to));

        /* since we get the channel while reading profile data, we have to set channel info <after>! */
        profileData.setChannelInfos(getProfileObject().buildChannelInfo());

        if (includeEvents) {
            profileData.setMeterEvents(getProfileObject().getMeterEvents());

            getProfileObject().applyEvents(profileData.getMeterEvents(), profileData.getIntervalDatas());

        }

        return profileData;
    }

    public int getProfileInterval() throws IOException {
        /* interval time of archive can't be easily read out as a value */
        return getObjectFactory().getIntervalObject().getIntervalSeconds();
    }

    public String getRegister(String arg0) throws IOException {
        throw new NoSuchRegisterException(
                "Lis100 devices have no register to read out!");
    }

    public Date getTime() throws IOException {
        return getObjectFactory().getClockObject().getDate();
    }

    public void initializeDevice() throws IOException {
    }

    public void release() throws IOException {
    }

    public void setRegister(String arg0, String arg1) throws IOException {
        throw new NoSuchRegisterException(
                "Lis100 devices have no register to set!");

    }

    public void setTime() throws IOException {
        getObjectFactory().getClockObject().setDate(new Date());
    }

    public DeviceData getDeviceData() {
        return deviceData;
    }

    /**
     * Validate certain protocol specific properties
     *
     * @param properties - The properties fetched from the Device
     * @throws MissingPropertyException
     *          - if a required property is missed
     * @throws InvalidPropertyException
     *          - if there is a wrong parameter defined
     */
    @SuppressWarnings({"unchecked"})
    private void validateProperties(Properties properties)
            throws MissingPropertyException, InvalidPropertyException {
        try {
            for (String key : getRequiredKeys()) {
                if (properties.getProperty(key) == null) {
                    throw new MissingPropertyException(key + " key missing");
                }
            }

            serialNumber = properties.getProperty(com.energyict.mdc.upl.MeterProtocol.Property.SERIALNUMBER.getName());

            strPassword = properties.getProperty(com.energyict.mdc.upl.MeterProtocol.Property.PASSWORD.getName());
            protocolRetriesProperty = Integer.parseInt(properties.getProperty(
                    "Retries", "3").trim());

            doValidateProperties(properties);
        } catch (NumberFormatException e) {
            throw new InvalidPropertyException(
                    " validateProperties, NumberFormatException, "
                            + e.getMessage());
        }
    }

    /**
     * Getter for the ObjectFactory
     *
     * @return the current ObjectFactory
     */
    public Lis100ObjectFactory getObjectFactory() {
        if (this.objectFactory == null) {
            this.objectFactory = new Lis100ObjectFactory(this);
        }
        return this.objectFactory;
    }

    /**
     * Getter for dsfg profile object
     *
     * @return DsfgProfile object
     */
    @SuppressWarnings({"unused"})
    protected Lis100Profile getProfileObject() {
        if (this.profile == null) {
            this.profile = new Lis100Profile(deviceData, getLogger());
        }
        return this.profile;
    }

    @SuppressWarnings({"unused"})
    public void doValidateProperties(Properties properties) {
    }

    // *******************************************************************************************
    // *
    // * Interface ProtocolLink
    // *
    // *******************************************************************************************/
    public byte[] getDataReadout() {
        return null;
    }

    public Lis100Connection getLis100Connection() {
        return connection;
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
    // * Interface RegisterProtocol
    // *
    // *******************************************************************************************/
    /**
     * Gets a description for the obis code
     *
     * @param obisCode - obis code to get the description for
     */
    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        Lis100Register reg = getObisCodeMapper().getRegister(obisCode);
        String desc = (reg == null) ? "" : reg.getDesc();
        return new RegisterInfo(desc);
    }

    /**
     * interface function to read a register value
     *
     * @param obisCode - code for the register to read
     */
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        return getObisCodeMapper().getRegisterValue(obisCode);
    }

    /**
     * Getter for the ObisCodeMapper. getRegisterMap() has to be
     * overridden by the derived class.
     *
     * @return the used ObisCodeMapper}
     */
    protected SimpleObisCodeMapper getObisCodeMapper() {
        if (this.obisCodeMapper == null) {
            this.obisCodeMapper = new SimpleObisCodeMapper(getRegisterMap(), getDeviceData());
        }
        return this.obisCodeMapper;
    }

    /**
     * Gets an empty register map
     *
     * @return an empty RegisterMap
     */
    protected RegisterMap getRegisterMap() {
        Lis100Register[] empty = {};
        return new RegisterMap(empty);
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
    // * depreciated methods
    // *
    // *******************************************************************************************/
    public Quantity getMeterReading(int arg0) throws IOException {
        return null;
    }

    public Quantity getMeterReading(String arg0) throws IOException {
        return null;
    }

}
