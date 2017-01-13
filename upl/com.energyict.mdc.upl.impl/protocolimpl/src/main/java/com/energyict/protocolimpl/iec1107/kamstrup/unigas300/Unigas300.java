/*
 * Unigas300.java
 *
 * Created on 8 mei 2003, 17:56
 */

package com.energyict.protocolimpl.iec1107.kamstrup.unigas300;

import com.energyict.mdc.upl.NoSuchRegisterException;
import com.energyict.mdc.upl.properties.InvalidPropertyException;
import com.energyict.mdc.upl.properties.MissingPropertyException;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecBuilderWizard;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.TypedProperties;

import com.energyict.dialer.connection.ConnectionException;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.support.SerialNumberSupport;
import com.energyict.protocolimpl.iec1107.FlagIEC1107Connection;
import com.energyict.protocolimpl.iec1107.FlagIEC1107ConnectionException;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
import static com.energyict.mdc.upl.MeterProtocol.Property.SERIALNUMBER;

/**
 * Copyrights EnergyICT
 * Date: 6-dec-2010
 * Time: 9:28:03
 */
public class Unigas300 extends AbstractUnigas300 implements SerialNumberSupport {

    private static final int KAMSTRUP_NR_OF_CHANNELS = 11;

    private String strID;
    private String strPassword;
    private int iIEC1107TimeoutProperty;
    private int iProtocolRetriesProperty;
    private int iRoundtripCorrection;
    private int iSecurityLevel;
    private String nodeId;
    private int iEchoCancelling;
    private int iIEC1107Compatible;
    private int iProfileInterval;
    private boolean software7E1;

    private String deviceSerialNumber = null;
    private String serialNumber = null;

    private FlagIEC1107Connection flagIEC1107Connection = null;
    private Unigas300Registry unigas300Registry = null;
    private Unigas300Profile unigas300Profile = null;
    private int extendedLogging;

    private byte[] dataReadout = null;

    private final PropertySpecService propertySpecService;

    public Unigas300(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Override
    public void init(InputStream inputStream, OutputStream outputStream, TimeZone timeZone, Logger logger) {
        setTimeZone(timeZone);
        setLogger(logger);

        try {
            flagIEC1107Connection = new FlagIEC1107Connection(inputStream, outputStream, iIEC1107TimeoutProperty, iProtocolRetriesProperty, 0, iEchoCancelling, iIEC1107Compatible, software7E1, logger);
            unigas300Registry = new Unigas300Registry(this);
            unigas300Profile = new Unigas300Profile(this, unigas300Registry);
        } catch (ConnectionException e) {
            logger.severe("ABBA1500: init(...), " + e.getMessage());
        }

    }

    @Override
    public void connect() throws IOException {
        try {

            sendWakeUp();
            dataReadout = flagIEC1107Connection.dataReadout(strID, nodeId);
            flagIEC1107Connection.disconnectMAC();
            flagIEC1107Connection.connectMAC(strID, strPassword, iSecurityLevel, nodeId);

            getLogger().info("Connected to device with serial number: " + getDeviceSerialNumber());
            registerInfo();
        } catch (FlagIEC1107ConnectionException e) {
            throw new IOException(e.getMessage());
        }
    }

    /**
     * Send 20 bytes of 0x00 to the device, to wake it up.
     * This is used by battery powered devices.
     *
     * @throws ConnectionException
     */
    private void sendWakeUp() throws ConnectionException {
        byte[] wakeUp = new byte[20];
        for (int i = 0; i < wakeUp.length; i++) {
            wakeUp[i] = (byte) 0x00;
        }
        getFlagIEC1107Connection().sendOut(wakeUp);
    }

    @Override
    public void disconnect() throws IOException {
        try {
            flagIEC1107Connection.disconnectMAC();
        } catch (FlagIEC1107ConnectionException e) {
            getLogger().severe("disconnect() error, " + e.getMessage());
        }
    }

    @Override
    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException {
        Calendar fromCalendar = ProtocolUtils.getCleanCalendar(getTimeZone());
        fromCalendar.setTime(from);
        Calendar toCalendar = ProtocolUtils.getCleanCalendar(getTimeZone());
        toCalendar.setTime(to);
        return getUnigas300Profile().getProfileData(fromCalendar, toCalendar, getNumberOfChannels(), 1);
    }

    @Override
    public void setTime() throws IOException {
        Calendar calendar = ProtocolUtils.getCalendar(getTimeZone());
        calendar.add(Calendar.MILLISECOND, iRoundtripCorrection);
        getUnigas300Registry().setRegister("0.9.1", calendar.getTime());
        getUnigas300Registry().setRegister("0.9.2", calendar.getTime());
    }

    @Override
    public Date getTime() throws IOException {
        Date date = (Date) getUnigas300Registry().getRegister("TimeDate");
        return new Date(date.getTime() - iRoundtripCorrection);
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        return Arrays.asList(
                this.stringSpec(ADDRESS.getName()),
                this.stringSpec(PASSWORD.getName()),
                this.integerSpec("Timeout"),
                this.integerSpec("Retries"),
                this.integerSpec("RoundtripCorrection"),
                this.integerSpec("SecurityLevel"),
                this.stringSpec(NODEID.getName()),
                this.integerSpec("EchoCancelling"),
                this.integerSpec("IEC1107Compatible"),
                this.integerSpec("ProfileInterval"),
                this.integerSpec("ExtendedLogging"),
                this.stringSpec("Software7E1"),
                this.stringSpec(SERIALNUMBER.getName()));
    }

    private <T> PropertySpec spec(String name, Supplier<PropertySpecBuilderWizard.NlsOptions<T>> optionsSupplier) {
        return UPLPropertySpecFactory.specBuilder(name, false, optionsSupplier).finish();
    }

    private PropertySpec stringSpec(String name) {
        return this.spec(name, this.propertySpecService::stringSpec);
    }

    private PropertySpec integerSpec(String name) {
        return this.spec(name, this.propertySpecService::integerSpec);
    }

    @Override
    public void setUPLProperties(TypedProperties properties) throws MissingPropertyException, InvalidPropertyException {
        try {
            strID = properties.getTypedProperty(com.energyict.mdc.upl.MeterProtocol.Property.ADDRESS.getName());
            strPassword = properties.getTypedProperty(com.energyict.mdc.upl.MeterProtocol.Property.PASSWORD.getName());
            iIEC1107TimeoutProperty = Integer.parseInt(properties.getTypedProperty("Timeout", "20000").trim());
            iProtocolRetriesProperty = Integer.parseInt(properties.getTypedProperty("Retries", "5").trim());
            iRoundtripCorrection = Integer.parseInt(properties.getTypedProperty("RoundtripCorrection", "0").trim());
            iSecurityLevel = Integer.parseInt(properties.getTypedProperty("SecurityLevel", "1").trim());
            nodeId = properties.getTypedProperty(com.energyict.mdc.upl.MeterProtocol.Property.NODEID.getName(), "");
            iEchoCancelling = Integer.parseInt(properties.getTypedProperty("EchoCancelling", "0").trim());
            iIEC1107Compatible = Integer.parseInt(properties.getTypedProperty("IEC1107Compatible", "1").trim());
            iProfileInterval = Integer.parseInt(properties.getTypedProperty("ProfileInterval", "3600").trim());
            extendedLogging = Integer.parseInt(properties.getTypedProperty("ExtendedLogging", "0").trim());
            this.software7E1 = !"0".equalsIgnoreCase(properties.getTypedProperty("Software7E1", "0"));
            this.serialNumber = properties.getTypedProperty(com.energyict.mdc.upl.MeterProtocol.Property.SERIALNUMBER.getName(), "");
        } catch (NumberFormatException e) {
            throw new InvalidPropertyException(e, "Unigas300: validation of properties failed before");
        }
    }

    @Override
    public String getProtocolVersion() {
        return "$Date: 2015-11-26 15:24:27 +0200 (Thu, 26 Nov 2015)$";
    }

    @Override
    public String getFirmwareVersion() throws IOException {
        try {
            return ((String) getUnigas300Registry().getRegister(RegisterMappingFactory.FW_VERSION_D));
        } catch (IOException e) {
            return "Unknown: " + e.getMessage();
        }
    }

    /**
     * Generate more info about the meter
     */
    private void registerInfo() {
        ObisCodeMapper ocm = new ObisCodeMapper(this);
        if (extendedLogging >= 1) {
            getLogger().info(ocm.getRegisterInfo());
        }
    }

    /**
     * Read the serial number from the device,
     *
     * @return
     * @throws IOException
     */
    public String getDeviceSerialNumber() throws IOException {
        if (deviceSerialNumber == null) {
            deviceSerialNumber = (String) getUnigas300Registry().getRegister("DeviceSerialNumber");
        }
        return deviceSerialNumber;
    }

    @Override
    public String getSerialNumber() {
        return serialNumber;
    }

    @Override
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        try {
            ObisCodeMapper ocm = new ObisCodeMapper(this);
            return ocm.getRegisterValue(obisCode);
        } catch (Exception e) {
            if ((e instanceof IOException) && (e.getMessage().contains("not initialized"))) {
                return new RegisterValue(obisCode, "No value available");
            }
            throw new NoSuchRegisterException("Problems while reading register " + obisCode + ": " + e.getMessage());
        }
    }

    @Override
    public int getNumberOfChannels() throws IOException {
        return KAMSTRUP_NR_OF_CHANNELS;
    }

    @Override
    public int getProfileInterval() throws IOException {
        return iProfileInterval;
    }

    public Unigas300Registry getUnigas300Registry() {
        return unigas300Registry;
    }

    private Unigas300Profile getUnigas300Profile() {
        return unigas300Profile;
    }

    @Override
    public FlagIEC1107Connection getFlagIEC1107Connection() {
        return flagIEC1107Connection;
    }

    @Override
    public boolean isIEC1107Compatible() {
        return (iIEC1107Compatible == 1);
    }

    @Override
    public String getPassword() {
        return strPassword;
    }

    @Override
    public byte[] getDataReadout() {
        return dataReadout;
    }

    @Override
    public int getNrOfRetries() {
        return iProtocolRetriesProperty;
    }

    @Override
    public boolean isRequestHeader() {
        return false;
    }

}