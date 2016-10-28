/*
 * Unigas300.java
 *
 * Created on 8 mei 2003, 17:56
 */

package com.energyict.protocolimpl.iec1107.kamstrup.unigas300;

import com.energyict.mdc.upl.NoSuchRegisterException;
import com.energyict.mdc.upl.properties.InvalidPropertyException;
import com.energyict.mdc.upl.properties.MissingPropertyException;

import com.energyict.dialer.connection.ConnectionException;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.support.SerialNumberSupport;
import com.energyict.protocolimpl.iec1107.FlagIEC1107Connection;
import com.energyict.protocolimpl.iec1107.FlagIEC1107ConnectionException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.Logger;

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

    byte[] dataReadout = null;

    /**
     * Creates a new instance of Unigas300, empty constructor
     */
    public Unigas300() {

    }

    /**
     * Initializes the protocol, set up connection, profile and registry
     *
     * @param inputStream  <br>
     * @param outputStream <br>
     * @param timeZone     <br>
     * @param logger       <br>
     */
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

    /**
     * Connect to the meter. Send the wake-up, read the data readout, do the sign on,
     * validate the serial number and if enabled, show the extended logging.
     *
     * @throws IOException
     */
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

    /**
     * Disconnect from the device.
     *
     * @throws IOException
     */
    public void disconnect() throws IOException {
        try {
            flagIEC1107Connection.disconnectMAC();
        } catch (FlagIEC1107ConnectionException e) {
            getLogger().severe("disconnect() error, " + e.getMessage());
        }
    }

    /**
     * Read the profile data from the given last reading to the given to date
     *
     * @param from
     * @param to
     * @param includeEvents
     * @return
     * @throws IOException
     */
    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException {
        Calendar fromCalendar = ProtocolUtils.getCleanCalendar(getTimeZone());
        fromCalendar.setTime(from);
        Calendar toCalendar = ProtocolUtils.getCleanCalendar(getTimeZone());
        toCalendar.setTime(to);
        return getUnigas300Profile().getProfileData(fromCalendar, toCalendar, getNumberOfChannels(), 1);
    }

    /**
     * This method sets the time/date in the remote meter equal to the system time/date of the machine where this object resides.
     *
     * @throws IOException
     */
    public void setTime() throws IOException {
        Calendar calendar = ProtocolUtils.getCalendar(getTimeZone());
        calendar.add(Calendar.MILLISECOND, iRoundtripCorrection);
        getUnigas300Registry().setRegister("0.9.1", calendar.getTime());
        getUnigas300Registry().setRegister("0.9.2", calendar.getTime());
    }

    /**
     * Read the clock from the connected device. Use the round trip time to compensate.
     *
     * @return
     * @throws IOException
     */
    public Date getTime() throws IOException {
        Date date = (Date) getUnigas300Registry().getRegister("TimeDate");
        return new Date(date.getTime() - iRoundtripCorrection);
    }

    /**
     * validates the properties, and checks if all required parameters are present.
     *
     * @param properties <br>
     * @throws MissingPropertyException <br>
     * @throws InvalidPropertyException <br>
     */
    protected void validateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
        try {
            checkMissingProperties(properties);
            strID = properties.getProperty(com.energyict.mdc.upl.MeterProtocol.Property.ADDRESS.getName());
            strPassword = properties.getProperty(com.energyict.mdc.upl.MeterProtocol.Property.PASSWORD.getName());
            iIEC1107TimeoutProperty = Integer.parseInt(properties.getProperty("Timeout", "20000").trim());
            iProtocolRetriesProperty = Integer.parseInt(properties.getProperty("Retries", "5").trim());
            iRoundtripCorrection = Integer.parseInt(properties.getProperty("RoundtripCorrection", "0").trim());
            iSecurityLevel = Integer.parseInt(properties.getProperty("SecurityLevel", "1").trim());
            nodeId = properties.getProperty(com.energyict.mdc.upl.MeterProtocol.Property.NODEID.getName(), "");
            iEchoCancelling = Integer.parseInt(properties.getProperty("EchoCancelling", "0").trim());
            iIEC1107Compatible = Integer.parseInt(properties.getProperty("IEC1107Compatible", "1").trim());
            iProfileInterval = Integer.parseInt(properties.getProperty("ProfileInterval", "3600").trim());
            extendedLogging = Integer.parseInt(properties.getProperty("ExtendedLogging", "0").trim());
            this.software7E1 = !"0".equalsIgnoreCase(properties.getProperty("Software7E1", "0"));
            this.serialNumber = properties.getProperty(com.energyict.mdc.upl.MeterProtocol.Property.SERIALNUMBER.getName(), "");
        } catch (NumberFormatException e) {
            throw new InvalidPropertyException("Unigas300, validateProperties, NumberFormatException, " + e.getMessage());
        }

    }

    public List<String> getRequiredKeys() {
        return Collections.emptyList();
    }

    public List<String> getOptionalKeys() {
        return  Arrays.asList(
                    "Timeout",
                    "Retries",
                    "SecurityLevel",
                    "EchoCancelling",
                    "IEC1107Compatible",
                    "ExtendedLogging",
                    "Software7E1");
    }

    public String getProtocolVersion() {
        return "$Date: 2015-11-26 15:24:27 +0200 (Thu, 26 Nov 2015)$";
    }

    /**
     * Read the firmware version from the device, or return 'Unknown' if not avaiable.
     *
     * @return
     * @throws IOException
     */
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

    public String getSerialNumber() {
        return serialNumber;
    }

    /**
     * Read a register from the device, by obiscode
     *
     * @param obisCode
     * @return
     * @throws IOException
     */
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

    public int getNumberOfChannels() throws IOException {
        return KAMSTRUP_NR_OF_CHANNELS;
    }

    public int getProfileInterval() throws IOException {
        return iProfileInterval;
    }

    public Unigas300Registry getUnigas300Registry() {
        return unigas300Registry;
    }

    private Unigas300Profile getUnigas300Profile() {
        return unigas300Profile;
    }

    public FlagIEC1107Connection getFlagIEC1107Connection() {
        return flagIEC1107Connection;
    }

    public boolean isIEC1107Compatible() {
        return (iIEC1107Compatible == 1);
    }

    public String getPassword() {
        return strPassword;
    }

    public byte[] getDataReadout() {
        return dataReadout;
    }

    public int getNrOfRetries() {
        return iProtocolRetriesProperty;
    }

    public boolean isRequestHeader() {
        return false;
    }

}
