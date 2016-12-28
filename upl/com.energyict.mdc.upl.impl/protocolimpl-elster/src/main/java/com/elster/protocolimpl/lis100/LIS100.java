package com.elster.protocolimpl.lis100;

import com.energyict.mdc.upl.NoSuchRegisterException;
import com.energyict.mdc.upl.properties.InvalidPropertyException;
import com.energyict.mdc.upl.properties.MissingPropertyException;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.TypedProperties;

import com.elster.protocolimpl.lis100.connection.Lis100Connection;
import com.elster.protocolimpl.lis100.profile.Lis100Profile;
import com.elster.protocolimpl.lis100.registers.Lis100Register;
import com.elster.protocolimpl.lis100.registers.RegisterMap;
import com.elster.protocolimpl.lis100.registers.SimpleObisCodeMapper;
import com.energyict.cbo.Quantity;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterProtocol;
import com.energyict.protocol.RegisterValue;
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

    public LIS100() {
        super(propertySpecService);
    }

    @Override
    public void init(InputStream inputStream, OutputStream outputStream,
                     TimeZone timezone, Logger logger) throws IOException {
        connection = new Lis100Connection(inputStream, outputStream);
        this.timeZone = timezone;
        this.logger = logger;
    }

    @Override
    public String getProtocolVersion() {
        return "$Date: 2012-01-09 15:06:09 +0100 (ma, 09 jan 2012) $";
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return Arrays.asList(
                UPLPropertySpecFactory.string(SERIALNUMBER.getName(), false),
                UPLPropertySpecFactory.string(PASSWORD.getName(), false),
                UPLPropertySpecFactory.integer(RETRIES.getName(), false));
    }

    @Override
    public void setProperties(TypedProperties properties) throws InvalidPropertyException, MissingPropertyException {
        try {
            serialNumber = properties.getTypedProperty(SERIALNUMBER.getName());
            strPassword = properties.getTypedProperty(PASSWORD.getName());
            protocolRetriesProperty = Integer.parseInt(properties.getTypedProperty(RETRIES.getName(), "3").trim());
        } catch (NumberFormatException e) {
            throw new InvalidPropertyException(e, this.getClass().getSimpleName() + ": validation of properties failed before");
        }
    }

    @Override
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

    @Override
    public void disconnect() throws IOException {
        connection.disconnect();
    }

    public void verifySerialNumber() throws IOException {
    }

    @Override
    public String getFirmwareVersion() throws IOException {
        return String.format("%d.%d", deviceData.getSoftwareVersion() / 100, deviceData.getSoftwareVersion() % 100);
    }

    @Override
    public int getNumberOfChannels() {
        return deviceData.getNumberOfChannels();
    }

    @Override
    public ProfileData getProfileData(boolean includeEvents) throws IOException {
        Calendar calendar = Calendar.getInstance(getTimeZone());
        /* maximum readout range set to 2 year */
        calendar.add(Calendar.MONTH, -24);
        return getProfileData(calendar.getTime(), includeEvents);
    }

    @Override
    public ProfileData getProfileData(Date lastReading, boolean includeEvents)
            throws IOException {
        return getProfileData(lastReading, new Date(), includeEvents);
    }

    @Override
    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException {
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

    @Override
    public int getProfileInterval() throws IOException {
        /* interval time of archive can't be easily read out as a value */
        return getObjectFactory().getIntervalObject().getIntervalSeconds();
    }

    @Override
    public String getRegister(String arg0) throws IOException {
        throw new NoSuchRegisterException("Lis100 devices have no register to read out!");
    }

    @Override
    public Date getTime() throws IOException {
        return getObjectFactory().getClockObject().getDate();
    }

    @Override
    public void initializeDevice() throws IOException {
    }

    @Override
    public void release() throws IOException {
    }

    @Override
    public void setRegister(String arg0, String arg1) throws IOException {
        throw new NoSuchRegisterException("Lis100 devices have no register to set!");

    }

    @Override
    public void setTime() throws IOException {
        getObjectFactory().getClockObject().setDate(new Date());
    }

    DeviceData getDeviceData() {
        return deviceData;
    }

    public Lis100ObjectFactory getObjectFactory() {
        if (this.objectFactory == null) {
            this.objectFactory = new Lis100ObjectFactory(this);
        }
        return this.objectFactory;
    }

    @SuppressWarnings({"unused"})
    protected Lis100Profile getProfileObject() {
        if (this.profile == null) {
            this.profile = new Lis100Profile(deviceData, getLogger());
        }
        return this.profile;
    }

    @Override
    public byte[] getDataReadout() {
        return null;
    }

    @Override
    public Lis100Connection getLis100Connection() {
        return connection;
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
    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        Lis100Register reg = getObisCodeMapper().getRegister(obisCode);
        String desc = (reg == null) ? "" : reg.getDesc();
        return new RegisterInfo(desc);
    }

    @Override
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        return getObisCodeMapper().getRegisterValue(obisCode);
    }

    private SimpleObisCodeMapper getObisCodeMapper() {
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

    @Override
    public Quantity getMeterReading(int arg0) throws IOException {
        return null;
    }

    @Override
    public Quantity getMeterReading(String arg0) throws IOException {
        return null;
    }

}