/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.iec1107.abba1500;


import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.common.BaseUnit;
import com.energyict.mdc.common.NestedIOException;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.HHUEnabler;
import com.energyict.mdc.protocol.api.InvalidPropertyException;
import com.energyict.mdc.protocol.api.MeterExceptionInfo;
import com.energyict.mdc.protocol.api.MissingPropertyException;
import com.energyict.mdc.protocol.api.NoSuchRegisterException;
import com.energyict.mdc.protocol.api.UnsupportedException;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterProtocol;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;
import com.energyict.mdc.protocol.api.dialer.connection.ConnectionException;
import com.energyict.mdc.protocol.api.dialer.core.HHUSignOn;
import com.energyict.mdc.protocol.api.dialer.core.SerialCommunicationChannel;
import com.energyict.mdc.protocol.api.legacy.MeterProtocol;
import com.energyict.mdc.protocol.api.legacy.dynamic.PropertySpecFactory;
import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.dialer.connection.IEC1107HHUConnection;
import com.energyict.protocolimpl.base.DataDumpParser;
import com.energyict.protocolimpl.base.DataParseException;
import com.energyict.protocolimpl.base.DataParser;
import com.energyict.protocolimpl.base.PluggableMeterProtocol;
import com.energyict.protocolimpl.base.ProtocolChannelMap;
import com.energyict.protocolimpl.iec1107.ChannelMap;
import com.energyict.protocolimpl.iec1107.FlagIEC1107Connection;
import com.energyict.protocolimpl.iec1107.FlagIEC1107ConnectionException;
import com.energyict.protocolimpl.iec1107.ProtocolLink;
import com.energyict.protocolimpl.iec1107.vdew.VDEWTimeStamp;
import com.energyict.protocolimpl.utils.ProtocolTools;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.logging.Logger;

/**
 * @author Koenraad Vanderschaeve
 *         <p/>
 *         <B>Description :</B><BR>
 *         Class that implements the ABBA1500 meter protocol.
 *         <BR>
 *         <B>@beginchanges</B><BR>
 *         KV|20012005|Initial version
 *         KV|23032005|Changed header to be compatible with protocol version tool
 *         KV|30032005|Handle StringOutOfBoundException in IEC1107 connection layer
 *         KV|06092005|VDEW changed to do channel mapping!
 *         KV|20092005|Add VDEWCompatible custom property to allow alternative time setting
 *         KV|20042007|Fix registerreading
 *         KV|16112007|Add workaround due to a meterbug (DataReadoutRequest=2)
 *         KV|13122007|Avoid index out of bound exception and retry for datareadout reception
 *         KV|17012008|Add forced delay as property and add reconnect to connection layer in case of break received during protocolsession
 *         GN|25032008|Added roundTripTime to correct the readout time when retries have occurred
 *         JME|05012009|Added filter for CORRUPTED flag when PU or PD for firmware 3.02
 *         JME|05012009|Added eventTime to billingPointRegister (Obiscode = 1.1.0.1.0.255) to get the last billing reset time.
 *         JME|22012009|Removed break command after dataReadout, to prevent non responding meter issues.
 * @version 1.0
 */
public class ABBA1500 extends PluggableMeterProtocol implements HHUEnabler, ProtocolLink, MeterExceptionInfo, RegisterProtocol {

    @Override
    public String getProtocolDescription() {
        return "Elster/ABB A1500 IEC1107 (VDEW)";
    }

    private String strID;
    private String strPassword;
    private String serialNumber;
    private String mSerialNumber = null;
    private int iIEC1107TimeoutProperty;
    private int iProtocolRetriesProperty;
    private int iRoundtripCorrection;
    private int iSecurityLevel;
    private String nodeId;
    private int iEchoCancelling;
    private int iIEC1107Compatible;
    private int profileInterval;
    private ChannelMap channelMap;
    private int requestHeader;
    private ProtocolChannelMap protocolChannelMap = null;
    private int dataReadoutRequest;
    private long roundTripTime = 0;
    private String strDateFormat;

    private TimeZone timeZone;
    private Logger logger;
    private int extendedLogging;
    private int vdewCompatible;
    private String iFirmwareVersion = "";

    FlagIEC1107Connection flagIEC1107Connection = null;
    ABBA1500Registry abba1500Registry = null;
    ABBA1500Profile abba1500Profile = null;
    ObisCode serialNumbObisCode = ObisCode.fromString("1.0.0.0.0.255");

    private List registerValues = null;

    byte[] dataReadout = null;
    boolean profileDateRead = false;

    boolean software7E1;

    int forcedDelay;
    int MaxNrOfDaysProfileData;

    @Inject
    public ABBA1500(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    public ProfileData getProfileData(boolean includeEvents) throws IOException {
        Calendar calendar = ProtocolUtils.getCalendar(timeZone);
        calendar.add(Calendar.YEAR, -10);
        return getProfileData(calendar.getTime(), includeEvents);
    }

    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        profileDateRead = true;
        return getAbba1500Profile().getProfileData(lastReading, includeEvents);
    }

    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException, UnsupportedException {
        profileDateRead = true;
        return getAbba1500Profile().getProfileData(from, to, includeEvents);
    }

    public Quantity getMeterReading(String name) throws UnsupportedException, IOException {
        throw new UnsupportedException();
    }

    public Quantity getMeterReading(int channelId) throws UnsupportedException, IOException {
        throw new UnsupportedException();
    }

    /**
     * This method sets the time/date in the remote meter equal to the system time/date of the machine where this object resides.
     *
     * @throws IOException
     */

    public void setTime() throws IOException {
        if ((getDataReadoutRequest() != 2) || profileDateRead) {
            if (vdewCompatible == 1) {
                setTimeVDEWCompatible();
            } else {
                setTimeAlternativeMethod();
            }
        }
    }

    private void setTimeAlternativeMethod() throws IOException {
        Calendar calendar = null;
        calendar = ProtocolUtils.getCalendar(timeZone);
        calendar.add(Calendar.MILLISECOND, iRoundtripCorrection);
        Date date = calendar.getTime();
        getAbba1500Registry().setRegister("TimeDate2", date);
    } // public void setTime() throws IOException

    private void setTimeVDEWCompatible() throws IOException {
        Calendar calendar = null;
        calendar = ProtocolUtils.getCalendar(timeZone);
        calendar.add(Calendar.MILLISECOND, iRoundtripCorrection);
        Date date = calendar.getTime();
        getAbba1500Registry().setRegister("Time", date);
        getAbba1500Registry().setRegister("Date", date);
    } // public void setTime() throws IOException

    public Date getTime() throws IOException {
        if ((getDataReadoutRequest() != 2) | profileDateRead) {
            roundTripTime = Calendar.getInstance().getTime().getTime();
            Date date = (Date) getAbba1500Registry().getRegister("TimeDate");
            roundTripTime = Calendar.getInstance().getTime().getTime() - roundTripTime;
//            return new Date(date.getTime()-iRoundtripCorrection);
            return new Date(date.getTime() - roundTripTime);
        } else {
            return new Date();
        }
    }

    public byte getLastProtocolState() {
        return -1;
    }

    /************************************** MeterProtocol implementation ***************************************/

    /**
     * this implementation calls <code> validateProperties </code>
     * and assigns the argument to the properties field
     *
     * @param properties <br>
     * @throws MissingPropertyException <br>
     * @throws InvalidPropertyException <br>
     */
    public void setProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
        validateProperties(properties);
    }

    /**
     * <p>validates the properties.</p><p>
     * The default implementation checks that all required parameters are present.
     * </p>
     *
     * @param properties <br>
     * @throws MissingPropertyException <br>
     * @throws InvalidPropertyException <br>
     */
    private void validateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
        try {
            Iterator iterator = getRequiredKeys().iterator();
            while (iterator.hasNext()) {
                String key = (String) iterator.next();
                if (properties.getProperty(key) == null) {
                    throw new MissingPropertyException(key + " key missing");
                }
            }
            strID = properties.getProperty(MeterProtocol.ADDRESS);
            strPassword = properties.getProperty(MeterProtocol.PASSWORD);
            iIEC1107TimeoutProperty = Integer.parseInt(properties.getProperty("Timeout", "20000").trim());
            iProtocolRetriesProperty = Integer.parseInt(properties.getProperty("Retries", "5").trim());
            iRoundtripCorrection = Integer.parseInt(properties.getProperty("RoundtripCorrection", "0").trim());
            iSecurityLevel = Integer.parseInt(properties.getProperty("SecurityLevel", "1").trim());
            nodeId = properties.getProperty(MeterProtocol.NODEID, "");
            iEchoCancelling = Integer.parseInt(properties.getProperty("EchoCancelling", "0").trim());
            iIEC1107Compatible = Integer.parseInt(properties.getProperty("IEC1107Compatible", "1").trim());
            profileInterval = Integer.parseInt(properties.getProperty("ProfileInterval", "3600").trim());
            channelMap = new ChannelMap(properties.getProperty("ChannelMap", "0"));
            requestHeader = Integer.parseInt(properties.getProperty("RequestHeader", "1").trim());
            protocolChannelMap = new ProtocolChannelMap(properties.getProperty("ChannelMap", "0,0,0,0"));
            dataReadoutRequest = Integer.parseInt(properties.getProperty("DataReadout", "0").trim());
            extendedLogging = Integer.parseInt(properties.getProperty("ExtendedLogging", "0").trim());
            vdewCompatible = Integer.parseInt(properties.getProperty("VDEWCompatible", "1").trim());
            forcedDelay = Integer.parseInt(properties.getProperty("ForcedDelay", "0").trim());
            serialNumber = properties.getProperty(MeterProtocol.SERIALNUMBER);
            iFirmwareVersion = properties.getProperty("FirmwareVersion", "3.03").trim();
            this.software7E1 = !properties.getProperty("Software7E1", "0").equalsIgnoreCase("0");
            this.MaxNrOfDaysProfileData = Integer.parseInt(properties.getProperty("MaxNrOfDaysProfileData", "0").trim());

            strDateFormat = properties.getProperty("DateFormat", "yy/MM/dd").trim().toLowerCase();
            // Check for valid DateFormat
            StringTokenizer tokenizer = new StringTokenizer(strDateFormat, "/");
            for (int i = 0; i < 3; i++) {
                String token = tokenizer.nextToken();
                if (!token.equals("mm") && !token.equals("yy") && !token.equals("dd")) {
                    throw new InvalidPropertyException("Invalid format of DateFormat property: " + strDateFormat + "! Valid formats should match pattern 'xx/xx/xx'.");
                }
            }
        } catch (NumberFormatException e) {
            throw new InvalidPropertyException("DukePower, validateProperties, NumberFormatException, " + e.getMessage());
        }
    }

    //    private boolean isDataReadout() {
//        return (dataReadoutRequest == 1);
//    }
    private int getDataReadoutRequest() {
        return dataReadoutRequest;
    }

    /**
     * this implementation throws UnsupportedException. Subclasses may override
     *
     * @param name <br>
     * @return the register value
     * @throws IOException             <br>
     * @throws UnsupportedException    <br>
     * @throws NoSuchRegisterException <br>
     */
    public String getRegister(String name) throws IOException, UnsupportedException, NoSuchRegisterException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byteArrayOutputStream.write(name.getBytes());
        flagIEC1107Connection.sendRawCommandFrame(FlagIEC1107Connection.READ5, byteArrayOutputStream.toByteArray());
        byte[] data = flagIEC1107Connection.receiveRawData();
        return new String(data);
    }

    /**
     * this implementation throws UnsupportedException. Subclasses may override
     *
     * @param name  <br>
     * @param value <br>
     * @throws IOException             <br>
     * @throws NoSuchRegisterException <br>
     * @throws UnsupportedException    <br>
     */
    public void setRegister(String name, String value) throws IOException, NoSuchRegisterException, UnsupportedException {
        getAbba1500Registry().setRegister(name, value);
    }

    /**
     * this implementation throws UnsupportedException. Subclasses may override
     *
     * @throws IOException          <br>
     * @throws UnsupportedException <br>
     */
    public void initializeDevice() throws IOException, UnsupportedException {
        throw new UnsupportedException();
    }

    @Override
    public List<PropertySpec> getRequiredProperties() {
        return PropertySpecFactory.toPropertySpecs(getRequiredKeys(), this.getPropertySpecService());
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        return PropertySpecFactory.toPropertySpecs(getOptionalKeys(), this.getPropertySpecService());
    }

    /**
     * the implementation returns both the address and password key
     *
     * @return a list of strings
     */
    public List<String> getRequiredKeys() {
        return Collections.emptyList();
    }

    /**
     * this implementation returns an empty list
     *
     * @return a list of strings
     */
    public List<String> getOptionalKeys() {
        return Arrays.asList(
                "Timeout",
                "Retries",
                "SecurityLevel",
                "EchoCancelling",
                "IEC1107Compatible",
                "ChannelMap",
                "RequestHeader",
                "Scaler",
                "DataReadout",
                "ExtendedLogging",
                "VDEWCompatible",
                "ForcedDelay",
                "FirmwareVersion",
                "Software7E1",
                "DateFormat",
                "MaxNrOfDaysProfileData");
    }

    /**
     * The protocol version
     */
    public String getProtocolVersion() {
        return "$Date: 2013-10-31 11:22:19 +0100 (Thu, 31 Oct 2013) $";
    }

    public String getFirmwareVersion() throws IOException {
        return ("Unknown");
    }

    /**
     * initializes the receiver
     *
     * @param inputStream  <br>
     * @param outputStream <br>
     * @param timeZone     <br>
     * @param logger       <br>
     */
    public void init(InputStream inputStream, OutputStream outputStream, TimeZone timeZone, Logger logger) {
        this.timeZone = timeZone;
        this.logger = logger;

        try {
            flagIEC1107Connection = new FlagIEC1107Connection(inputStream, outputStream, iIEC1107TimeoutProperty, iProtocolRetriesProperty, forcedDelay, iEchoCancelling, iIEC1107Compatible, software7E1, logger);
            abba1500Registry = new ABBA1500Registry(this, this, getDateFormat());
            abba1500Profile = new ABBA1500Profile(this, this, abba1500Registry);
            abba1500Profile.setFirmwareVersion(getIFirmwareVersion());
        } catch (ConnectionException e) {
            logger.severe("ABBA1500: init(...), " + e.getMessage());
        }

    }

    /**
     * @throws IOException
     */
    public void connect() throws IOException {
        try {
            if ((getFlagIEC1107Connection().getHhuSignOn() == null) && (getDataReadoutRequest() == 1)) {
                dataReadout = flagIEC1107Connection.dataReadout(strID, nodeId);
                // ABBA1500 doesn't respond after sending a break in dataReadoutMode, so disconnect without sending break
                flagIEC1107Connection.disconnectMACWithoutBreak();
            }

            flagIEC1107Connection.connectMAC(strID, strPassword, iSecurityLevel, nodeId);


            if ((getFlagIEC1107Connection().getHhuSignOn() != null) && (getDataReadoutRequest() == 1)) {
                dataReadout = getFlagIEC1107Connection().getHhuSignOn().getDataReadout();
            }

            if (!verifyMeterSerialNR()) {
                throw new IOException("ABB A1500, connect, Wrong SerialNR!, EISerialNumber=" + serialNumber + ", MeterSerialNumber=" + getSerialNumber());
            }
        } catch (FlagIEC1107ConnectionException e) {
            throw new IOException(e.getMessage());
        }

        if (extendedLogging >= 1) {
            getRegistersInfo();
        }

    }

    private String getSerialNumber() throws IOException {
        if (mSerialNumber == null) {
            RegisterValue serialInfo = readRegister(serialNumbObisCode);
            mSerialNumber = serialInfo.getText();
        }

        return mSerialNumber;
    }

    private boolean verifyMeterSerialNR() throws IOException {
        return (serialNumber == null) ||
                ("".compareTo(serialNumber) == 0) ||
                (serialNumber.compareTo(getSerialNumber()) == 0);
    }

    public void disconnect() throws IOException {
        try {
            flagIEC1107Connection.disconnectMAC();
        } catch (FlagIEC1107ConnectionException e) {
            logger.severe("disconnect() error, " + e.getMessage());
        }
    }

    public int getNumberOfChannels() throws IOException {
        if (requestHeader == 1) {
            return getAbba1500Profile().getProfileHeader().getNrOfChannels();
        } else {
            return getProtocolChannelMap().getNrOfProtocolChannels();
        }
    }

    public int getProfileInterval() throws IOException {
        if (requestHeader == 1) {
            return getAbba1500Profile().getProfileHeader().getProfileInterval();
        } else {
            return profileInterval;
        }
    }


    // Implementation of interface ProtocolLink
    public FlagIEC1107Connection getFlagIEC1107Connection() {
        return flagIEC1107Connection;
    }

    public TimeZone getTimeZone() {
        return timeZone;
    }

    public String getDateFormat() {
        return strDateFormat;
    }

    public boolean isIEC1107Compatible() {
        return (iIEC1107Compatible == 1);
    }

    public String getPassword() {
        return strPassword;
    }

    public byte[] getDataReadout() {

        if ((dataReadout == null) && (getDataReadoutRequest() == 2)) {
            try {
                flagIEC1107Connection.disconnectMAC();
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    throw new NestedIOException(e);
                }
                dataReadout = flagIEC1107Connection.dataReadout(strID, nodeId);
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    throw new NestedIOException(e);
                }
            } catch (IOException e) {
                getLogger().severe("getDataReadout(), error reading datareadout, " + e.toString());
            }
        }
        return dataReadout;
    }

    public Object getCache() {
        return null;
    }

    public Object fetchCache(int rtuid) {
        return null;
    }

    public void setCache(Object cacheObject) {
    }

    public void updateCache(int rtuid, Object cacheObject) {
    }

    public ChannelMap getChannelMap() {
        return channelMap;
    }

    public void release() throws IOException {
    }

    public Logger getLogger() {
        return logger;
    }

    static Map<String, String> exceptionInfoMap = new HashMap<>();

    static {
        exceptionInfoMap.put("ERROR", "Request could not execute!");
        exceptionInfoMap.put("ERROR01", "A1500 ERROR 01, invalid command!");
        exceptionInfoMap.put("ERROR06", "A1500 ERROR 06, invalid command!");
    }

    public String getExceptionInfo(String id) {
        String exceptionInfo = exceptionInfoMap.get(ProtocolUtils.stripBrackets(id));
        if (exceptionInfo != null) {
            return id + ", " + exceptionInfo;
        } else {
            return "No meter specific exception info for " + id;
        }
    }

    public int getNrOfRetries() {
        return iProtocolRetriesProperty;
    }

    /**
     * Getter for property requestHeader.
     *
     * @return Value of property requestHeader.
     */
    public boolean isRequestHeader() {
        return requestHeader == 1;
    }

    public com.energyict.protocolimpl.base.ProtocolChannelMap getProtocolChannelMap() {
        return protocolChannelMap;
    }

    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        if (obisCode.getF() != 255) {
            RegisterValue billingPointRegister = doReadRegister(ObisCode.fromString("1.1.0.1.0.255"), false);
            int billingPoint = billingPointRegister.getQuantity().intValue();
            int VZ = Math.abs(obisCode.getF());
            obisCode = new ObisCode(obisCode.getA(), obisCode.getB(), obisCode.getC(), obisCode.getD(), obisCode.getE(), billingPoint - VZ);

            // read the non billing register to reuse the unit in case of billingpoints...
            try {
                doReadRegister(new ObisCode(obisCode.getA(), obisCode.getB(), obisCode.getC(), obisCode.getD(), obisCode.getE(), 255), false);
            } catch (NoSuchRegisterException e) {
                // absorb if not exist...
            }

            // read the billing point timestamp
            try {
                doReadRegister(new ObisCode(1, 1, 0, 1, 2, billingPoint - VZ), true);
            } catch (NoSuchRegisterException e) {
                // absorb if not exist...
            }

        } // if (obisCode.getF() != 255)


        // JME:	Special case for obiscode == 1.1.0.1.0.255 (billing point):
        //		Read the date of the billing reset and apply it to the billingPointRegister as eventTime
        if (obisCode.toString().equalsIgnoreCase("1.1.0.1.0.255")) {
            RegisterValue billingPointRegister = doReadRegister(ObisCode.fromString("1.1.0.1.0.255"), false);
            int billingPoint = billingPointRegister.getQuantity().intValue();

            RegisterValue reg_date;
            try {
                reg_date = doReadRegister(new ObisCode(1, 1, 0, 1, 2, billingPoint), true);
                if (reg_date != null) {
                    billingPointRegister = new RegisterValue(
                            billingPointRegister.getObisCode(),
                            billingPointRegister.getQuantity(),
                            reg_date.getToTime(), // eventTime from billing point
                            billingPointRegister.getFromTime(),
                            billingPointRegister.getToTime(),
                            billingPointRegister.getReadTime(),
                            billingPointRegister.getRegisterSpecId(),
                            billingPointRegister.getText()
                    );
                }
            } catch (NoSuchRegisterException e) {
                // absorb if not exist...
            }

            return billingPointRegister;
        }

        return doReadRegister(obisCode, false);
    }

    private RegisterValue doReadRegister(ObisCode obisCode, boolean billingTimestamp) throws IOException {
        RegisterValue registerValue = findRegisterValue(obisCode);
        if (registerValue == null) {
            if (billingTimestamp) {
                registerValue = doTheReadBillingRegisterTimestamp(obisCode);
            } else {
                registerValue = doTheReadRegister(obisCode);
            }
            registerValues.add(registerValue);
        }
        return registerValue;
    }

    private byte[] readRegisterData(ObisCode obisCode) throws IOException {
        String edisNotation = obisCode.getC() + "." + obisCode.getD() + "." + obisCode.getE() + (obisCode.getF() == 255 ? "" : "*" + ProtocolUtils.buildStringDecimal(Math.abs(obisCode.getF()), 2));
        byte[] data;
        if (getDataReadoutRequest() == 0) {
            String name = edisNotation + "(;)";
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byteArrayOutputStream.write(name.getBytes());
            flagIEC1107Connection.sendRawCommandFrame(FlagIEC1107Connection.READ5, byteArrayOutputStream.toByteArray());
            data = flagIEC1107Connection.receiveRawData();
        } else {
            DataDumpParser ddp = new DataDumpParser(getDataReadout());
            if (edisNotation.indexOf("97.97.0") >= 0) {
                data = ddp.getRegisterFFStrValue("F.F").getBytes();
            } else {
                data = ddp.getRegisterStrValue(edisNotation).getBytes();
            }
        }
        return data;
    }

    private Quantity parseQuantity(byte[] data) throws IOException {
        DataParser dp = new DataParser(getTimeZone());
        return dp.parseQuantityBetweenBrackets(data, 0, 0);
    }

    private Date parseDate(byte[] data, int pos) throws IOException {
        Date date;
        try {
            DataParser dp = new DataParser(getTimeZone());
            VDEWTimeStamp vts = new VDEWTimeStamp(getTimeZone());
            String dateStr = dp.parseBetweenBrackets(data, 0, pos);
            if ("".compareTo(dateStr) == 0) {
                return null;
            }
            vts.setStrDateFormat(strDateFormat);
            vts.parse(dateStr);
            date = vts.getCalendar().getTime();
            return date;
        } catch (DataParseException e) {
            //absorb
            return null;
        }
    }

    private RegisterValue doTheReadRegister(ObisCode obisCode) throws IOException {
        try {

            byte[] data = readRegisterData(obisCode);

            if (obisCode.equals(serialNumbObisCode)) {
                String text = parseText(data);
                return new RegisterValue(obisCode, null, null, null, null, null, 0, text);
            }

            Quantity quantity = parseQuantity(data);
            Date date = parseDate(data, 1);
            Date billlingDate = null;
            RegisterValue registerValue;

            // in case of unitless AND billing register
            // find the non billing register and use that unit if the non billing register exist
            // also find the timestamp for that billingpoint and add it to the registervalue
            if (quantity.getBaseUnit().getDlmsCode() == BaseUnit.UNITLESS && obisCode.getF() != 255) {
                registerValue = findRegisterValue(ProtocolTools.setObisCodeField(obisCode, 5, (byte) 255));
                if (registerValue != null) {
                    quantity = new Quantity(quantity.getAmount(), registerValue.getQuantity().getUnit());
                }

            }
            if (obisCode.getF() != 255) {
                registerValue = findRegisterValue(new ObisCode(1, 1, 0, 1, 2, obisCode.getF()));
                if (registerValue != null) {
                    billlingDate = registerValue.getToTime();
                }
            }

            return new RegisterValue(obisCode, quantity, date, billlingDate);
        } catch (NoSuchRegisterException | NumberFormatException e) {
            throw new NoSuchRegisterException("ObisCode " + obisCode.toString() + " is not supported!");
        }
        catch (IOException e) {
            throw new IOException("doTheReadRegister(), error, " + e.getMessage());
        }
    }

    private String parseText(byte[] data) throws IOException {
        DataParser dp = new DataParser(getTimeZone());
        return dp.parseBetweenBrackets(data, 0, 0);
    }

    private RegisterValue doTheReadBillingRegisterTimestamp(ObisCode obisCode) throws IOException {
        try {
            byte[] data = readRegisterData(obisCode);

//System.out.println(new String(data));

            Date date = parseDate(data, 0);
            return new RegisterValue(obisCode, null, null, date);
        } catch (NoSuchRegisterException | NumberFormatException e) {
            throw new NoSuchRegisterException("ObisCode " + obisCode.toString() + " is not supported!");
        }
        catch (IOException e) {
            throw new IOException("doTheReadBillingRegisterTimestamp(), error, " + e.getMessage());
        }
    }

    private RegisterValue findRegisterValue(ObisCode obisCode) {
        if (registerValues == null) {
            registerValues = new ArrayList();
        } else {
            Iterator it = registerValues.iterator();
            while (it.hasNext()) {
                RegisterValue r = (RegisterValue) it.next();
                if (r.getObisCode().equals(obisCode)) {
                    return r;
                }
            }
        }
        return null;
    }


    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        return new RegisterInfo(obisCode.getDescription());
    }

    private void getRegistersInfo() {
        StringBuilder builder = new StringBuilder();
        if (getDataReadoutRequest() == 1) {
            builder.append("******************* ExtendedLogging *******************\n");
            builder.append(new String(getDataReadout()));
        } else {
            builder.append("******************* ExtendedLogging *******************\n");
            builder.append("All OBIS codes are translated to EDIS codes but not all codes are configured in the meter.\n");
            builder.append("It is not possible to retrieve a list with all registers in the meter. Consult the configuration of the meter.");
            builder.append("\n");
        }
        logger.info(builder.toString());
    }


    // ********************************************************************************************************
    // implementation of the HHUEnabler interface
    public void enableHHUSignOn(SerialCommunicationChannel commChannel) throws ConnectionException {
        enableHHUSignOn(commChannel, getDataReadoutRequest() == 1);
    }

    public void enableHHUSignOn(SerialCommunicationChannel commChannel, boolean datareadout) throws ConnectionException {
        HHUSignOn hhuSignOn = new IEC1107HHUConnection(commChannel, iIEC1107TimeoutProperty, iProtocolRetriesProperty, 300, iEchoCancelling);
        hhuSignOn.setMode(HHUSignOn.MODE_PROGRAMMING);
        hhuSignOn.setProtocol(HHUSignOn.PROTOCOL_NORMAL);
        hhuSignOn.enableDataReadout(datareadout);
        getFlagIEC1107Connection().setHHUSignOn(hhuSignOn);
    }

    public byte[] getHHUDataReadout() {
        return getFlagIEC1107Connection().getHhuSignOn().getDataReadout();
    }

    /**
     * Getter for property abba1500Registry.
     *
     * @return Value of property abba1500Registry.
     */
    public com.energyict.protocolimpl.iec1107.abba1500.ABBA1500Registry getAbba1500Registry() {
        return abba1500Registry;
    }

    /**
     * Getter for property abba1500Profile.
     *
     * @return Value of property abba1500Profile.
     */
    public com.energyict.protocolimpl.iec1107.abba1500.ABBA1500Profile getAbba1500Profile() {
        return abba1500Profile;
    }

    public String getIFirmwareVersion() {
        return iFirmwareVersion;
    }

    public int getMaxNrOfDaysProfileData() {
        return MaxNrOfDaysProfileData;
    }
} // public class ABBA1500 implements MeterProtocol {
