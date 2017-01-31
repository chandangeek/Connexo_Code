/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.iec1107.emh.lzqj;


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
import java.util.TimeZone;
import java.util.logging.Logger;

/**
 * @author Koenraad Vanderschaeve
 *         <p/>
 *         <B>Description :</B><BR>
 *         Class that implements the LZQJ meter protocol.
 *         <BR>
 *         <B>@beginchanges</B><BR>
 *         KV|14112007|	Initial version
 *         JM|02102009|	Fix for mantis issue #5322
 *         Changed register date fields (eventTime, toTime, fromTime) to show the correct billing timestamps.
 *         The protocol can only read these values if the are configured to be in the datadump of the device.
 *         The registers with the billing timestamps are 0.1.2*01, 0.1.2*02, ... 0.1.2*xx
 *         JM|11032010| Added new registermappings JIRA: COMMUNICATION-28
 * @version 1.0
 * @endchanges
 */
public class LZQJ extends PluggableMeterProtocol implements HHUEnabler, ProtocolLink, MeterExceptionInfo, RegisterProtocol {

    @Override
    public String getProtocolDescription() {
        return "EMH LZQJ IEC1107 (VDEW)";
    }

    private String strID;
    private String strPassword;
    private int iIEC1107TimeoutProperty;
    private int iProtocolRetriesProperty;
    private int iRoundtripCorrection;
    private int iSecurityLevel;
    private String nodeId;
    private int iEchoCancelling;
    private int iIEC1107Compatible;
    private int profileInterval;
    private int requestHeader;
    private ProtocolChannelMap protocolChannelMap = null;
    private int dataReadoutRequest;

    private TimeZone timeZone;
    private Logger logger;
    private int extendedLogging;
    private int vdewCompatible;

    private FlagIEC1107Connection flagIEC1107Connection = null;
    private LZQJRegistry lzqjRegistry = null;
    private LZQJProfile lzqjProfile = null;

    private List registerValues = null;

    private byte[] dataReadout = null;

    private boolean software7E1;
    private boolean isFixedProfileTimeZone;
    private boolean profileHelper = false;

    /**
     * Indication whether longNameObisCodes can be used
     */
    private boolean longNameObisCodes = false;

    @Inject
    public LZQJ(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    public ProfileData getProfileData(boolean includeEvents) throws IOException {
        this.profileHelper = true;
        Calendar calendar = ProtocolUtils.getCalendar(timeZone);
        calendar.add(Calendar.YEAR, -10);
        ProfileData pd = getProfileData(calendar.getTime(), includeEvents);
        this.profileHelper = false;
        return pd;
    }

    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        this.profileHelper = true;
        ProfileData pd = getLzqjProfile().getProfileData(lastReading, includeEvents);
        this.profileHelper = false;
        return pd;
    }

    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException {
        this.profileHelper = true;
        ProfileData pd = getLzqjProfile().getProfileData(from, to, includeEvents);
        this.profileHelper = false;
        return pd;
    }

    public Quantity getMeterReading(String name) throws IOException {
        throw new UnsupportedException();
    }

    public Quantity getMeterReading(int channelId) throws IOException {
        throw new UnsupportedException();
    }

    /**
     * This method sets the time/date in the remote meter equal to the system time/date of the machine where this object resides.
     *
     * @throws IOException
     */

    public void setTime() throws IOException {
        if (vdewCompatible == 1) {
            setTimeVDEWCompatible();
        } else {
            setTimeAlternativeMethod();
        }
    }

    private void setTimeAlternativeMethod() throws IOException {
        Calendar calendar;
        calendar = ProtocolUtils.getCalendar(timeZone);
        calendar.add(Calendar.MILLISECOND, iRoundtripCorrection);
        Date date = calendar.getTime();
        getLzqjRegistry().setRegister("TimeDate2", date);
    } // public void setTime() throws IOException

    private void setTimeVDEWCompatible() throws IOException {
        Calendar calendar;
        calendar = ProtocolUtils.getCalendar(timeZone);
        calendar.add(Calendar.MILLISECOND, iRoundtripCorrection);
        Date date = calendar.getTime();
        getLzqjRegistry().setRegister("Time", date);
        getLzqjRegistry().setRegister("Date", date);
    } // public void setTime() throws IOException

    public Date getTime() throws IOException {
        Date date = (Date) getLzqjRegistry().getRegister("TimeDate");
        return new Date(date.getTime() - iRoundtripCorrection);
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
    protected void validateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
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
            profileInterval = Integer.parseInt(properties.getProperty("ProfileInterval", "900").trim());
            requestHeader = Integer.parseInt(properties.getProperty("RequestHeader", "0").trim());
            protocolChannelMap = new ProtocolChannelMap(properties.getProperty("ChannelMap", "0,0,0,0"));
            dataReadoutRequest = Integer.parseInt(properties.getProperty("DataReadout", "1").trim());
            extendedLogging = Integer.parseInt(properties.getProperty("ExtendedLogging", "0").trim());
            vdewCompatible = Integer.parseInt(properties.getProperty("VDEWCompatible", "1").trim());
            isFixedProfileTimeZone = (Integer.parseInt(properties.getProperty("FixedProfileTimeZone", "1")) == 1);
            this.software7E1 = !properties.getProperty("Software7E1", "0").equalsIgnoreCase("0");
        } catch (NumberFormatException e) {
            throw new InvalidPropertyException("DukePower, validateProperties, NumberFormatException, " + e.getMessage());
        }

    }

    private boolean isDataReadout() {
        return (dataReadoutRequest == 1);
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
    public String getRegister(String name) throws IOException {
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
    public void setRegister(String name, String value) throws IOException {
        getLzqjRegistry().setRegister(name, value);
    }

    /**
     * this implementation throws UnsupportedException. Subclasses may override
     *
     * @throws IOException          <br>
     * @throws UnsupportedException <br>
     */
    public void initializeDevice() throws IOException {
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

    public List<String> getRequiredKeys() {
        return Collections.emptyList();
    }

    public List getOptionalKeys() {
        return Arrays.asList(
                    "Timeout",
                    "Retries",
                    "SecurityLevel",
                    "EchoCancelling",
                    "IEC1107Compatible",
                    "ChannelMap",
                    "RequestHeader",
                    "DataReadout",
                    "ExtendedLogging",
                    "VDEWCompatible",
                    "Software7E1",
                    "FixedProfileTimeZone");
    }

    public String getProtocolVersion() {
        return "$Date: 2013-10-31 11:22:19 +0100 (Thu, 31 Oct 2013) $";
    }

    public String getFirmwareVersion() throws IOException {

        String name;
        String firmware = "";
        ByteArrayOutputStream byteArrayOutputStream;
        byte[] data;

        try {
            name = "0.2.0" + "(;)";
            byteArrayOutputStream = new ByteArrayOutputStream();
            byteArrayOutputStream.write(name.getBytes());
            flagIEC1107Connection.sendRawCommandFrame(FlagIEC1107Connection.READ5, byteArrayOutputStream.toByteArray());
            data = flagIEC1107Connection.receiveRawData();
            firmware += "Configuration program version number: " + new String(data);
        } catch (Exception e) {
            firmware += "Configuration program version number: (none)";
        }

        try {
            name = "0.2.1*01" + "(;)";
            byteArrayOutputStream = new ByteArrayOutputStream();
            byteArrayOutputStream.write(name.getBytes());
            flagIEC1107Connection.sendRawCommandFrame(FlagIEC1107Connection.READ5, byteArrayOutputStream.toByteArray());
            data = flagIEC1107Connection.receiveRawData();
            firmware += " - Parameter number: " + new String(data);
        } catch (Exception e) {
            firmware += " - Parameter number: (none)";
        }

        try {
            name = "0.2.1*02" + "(;)";
            byteArrayOutputStream = new ByteArrayOutputStream();
            byteArrayOutputStream.write(name.getBytes());
            flagIEC1107Connection.sendRawCommandFrame(FlagIEC1107Connection.READ5, byteArrayOutputStream.toByteArray());
            data = flagIEC1107Connection.receiveRawData();
            firmware += " - Parameter settings: " + new String(data);
        } catch (Exception e) {
            firmware += " - Parameter settings: (none)";
        }

        try {
            name = "0.2.1*50" + "(;)";
            byteArrayOutputStream = new ByteArrayOutputStream();
            byteArrayOutputStream.write(name.getBytes());
            flagIEC1107Connection.sendRawCommandFrame(FlagIEC1107Connection.READ5, byteArrayOutputStream.toByteArray());
            data = flagIEC1107Connection.receiveRawData();
            firmware += " - Set number: " + new String(data);
        } catch (Exception e) {
            firmware += " - Set number: (none)";
        }
        return firmware;
    } // public String getFirmwareVersion()

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
            flagIEC1107Connection = new FlagIEC1107Connection(inputStream, outputStream, iIEC1107TimeoutProperty, iProtocolRetriesProperty, 0, iEchoCancelling, iIEC1107Compatible, software7E1, logger);
            flagIEC1107Connection.setAddCRLF(true);
            lzqjRegistry = new LZQJRegistry(this, this);
        } catch (ConnectionException e) {
            logger.severe("LZQJ: init(...), " + e.getMessage());
        }

    } // public void init(InputStream inputStream,OutputStream outputStream,TimeZone timeZone,Logger logger)

    /**
     * @throws IOException
     */
    public void connect() throws IOException {
        try {
            if ((getFlagIEC1107Connection().getHhuSignOn() == null) && (isDataReadout())) {
                dataReadout = flagIEC1107Connection.dataReadout(strID, nodeId);
                flagIEC1107Connection.disconnectMAC();
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    throw new NestedIOException(e);
                }
            }

//			flagIEC1107Connection.getHhuSignOn().setMode(HHUSignOn.MODE_READOUT);
//			flagIEC1107Connection.getHhuSignOn().setProtocol(HHUSignOn.PROTOCOL_NORMAL);

            flagIEC1107Connection.connectMAC(strID, strPassword, iSecurityLevel, nodeId);

            if ((getFlagIEC1107Connection().getHhuSignOn() != null) && (isDataReadout())) {
                dataReadout = getFlagIEC1107Connection().getHhuSignOn().getDataReadout();
            }

            /*
            Check if we need to use LongName ObisCodes(the term longNames is not entirely correct because the F field is not even put in ...)
             */
            if (isDataReadout()) {
                this.longNameObisCodes = new String(dataReadout).indexOf("1-1:") > 0;
            } else {
                this.longNameObisCodes = getLzqjProfile().checkForLongObisCodes(getProfileInterval());
            }

        } catch (FlagIEC1107ConnectionException e) {
            throw new IOException(e.getMessage());
        }

        if (extendedLogging >= 1) {
            getRegistersInfo();
        }

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
            return getLzqjProfile().getProfileHeader().getNrOfChannels();
        } else {
            return getProtocolChannelMap().getNrOfProtocolChannels();
        }
    }

    public int getProfileInterval() throws IOException {
        if (requestHeader == 1) {
            return getLzqjProfile().getProfileHeader().getProfileInterval();
        } else {
            return profileInterval;
        }
    }


    // Implementation of interface ProtocolLink
    public FlagIEC1107Connection getFlagIEC1107Connection() {
        return flagIEC1107Connection;
    }

    /**
     * Apparently the profile is always returned in GMT+01 ...
     */
    public TimeZone getTimeZone() {
        if (profileHelper) {
            if (isFixedProfileTimeZone) {
                return TimeZone.getTimeZone("GMT+01:00");
            } else {
                return timeZone;
            }
        } else {
            return timeZone;
        }
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

    /**
     * Setter for the DataReadout
     *
     * @param dataReadout the new DataReadout to set
     */
    protected void setDataReadout(byte[] dataReadout) {
        this.dataReadout = dataReadout.clone();
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
        return null;
    }

    public void release() throws IOException {
    }

    public Logger getLogger() {
        return logger;
    }

    private static final Map<String, String> exceptionInfoMap = new HashMap<>();

    static {
        exceptionInfoMap.put("ERROR", "Request could not execute!");
        exceptionInfoMap.put("ERROR01", "EMH LZQJ ERROR 01, invalid command!");
        exceptionInfoMap.put("ERROR06", "EMH LZQJ ERROR 06, invalid command!");
    }

    public String getExceptionInfo(String id) {
        String exceptionInfo = (String) exceptionInfoMap.get(ProtocolUtils.stripBrackets(id));
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

            if ((billingPoint - VZ) < 0) {
                throw new NoSuchRegisterException("No such a billing point.");
            }

            obisCode = new ObisCode(obisCode.getA(), obisCode.getB(), obisCode.getC(), obisCode.getD(), obisCode.getE(), billingPoint - VZ);

            // read the non billing register to reuse the unit in case of billingpoints...
            try {
                doReadRegister(new ObisCode(obisCode.getA(), obisCode.getB(), obisCode.getC(), obisCode.getD(), obisCode.getE(), 255), false);
            } catch (NoSuchRegisterException e) {
                // absorb if not exist...
            }

            // read the billing point timestamp (toTime)
            try {
                int toBP = billingPoint - VZ;
                if ((toBP > 0) && (toBP <= 99)) {
                    doReadRegister(new ObisCode(1, 1, 0, 1, 2, toBP), true);
                }
            } catch (NoSuchRegisterException e) {
                // absorb if not exist...
            }

            // read the billing point timestamp (fromTime)
            try {
                int fromBP = billingPoint - (VZ - 1);
                if ((fromBP > 0) && (fromBP <= 99)) {
                    doReadRegister(new ObisCode(1, 1, 0, 1, 2, fromBP), true);
                }
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
        String edisNotation = EdisObisMapper.getEdisCodeFromObisCode(obisCode, longNameObisCodes);
        byte[] data;
        if (!isDataReadout()) {
            String name = edisNotation + "(;)";
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byteArrayOutputStream.write(name.getBytes());
            flagIEC1107Connection.sendRawCommandFrame(FlagIEC1107Connection.READ5, byteArrayOutputStream.toByteArray());
            data = flagIEC1107Connection.receiveRawData();
        } else {
            DataDumpParser ddp = new DataDumpParser(getDataReadout());
            if (edisNotation.indexOf("0.1.0") >= 0) {
                String name = edisNotation + "(;)";
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                byteArrayOutputStream.write(name.getBytes());
                flagIEC1107Connection.sendRawCommandFrame(FlagIEC1107Connection.READ5, byteArrayOutputStream.toByteArray());
                data = flagIEC1107Connection.receiveRawData();
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
            RegisterValue registerValue;
            Quantity quantity = parseQuantity(data);
            Date eventTime = parseDate(data, 1);
            Date fromTime = null;
            Date toTime = null;

            // in case of unitless AND billing register
            // find the non billing register and use that unit if the non billing register exist
            // also find the timestamp for that billingpoint and add it to the registervalue
            if (obisCode.getF() != 255) {

                if (quantity.getBaseUnit().getDlmsCode() == BaseUnit.UNITLESS) {
                    registerValue = findRegisterValue(new ObisCode(obisCode.getA(), obisCode.getB(), obisCode.getC(), obisCode.getD(), obisCode.getE(), 255));
                    if (registerValue != null) {
                        quantity = new Quantity(quantity.getAmount(), registerValue.getQuantity().getUnit());
                    }
                }

                registerValue = findRegisterValue(new ObisCode(1, 1, 0, 1, 2, obisCode.getF()));
                if (registerValue != null) {
                    toTime = registerValue.getToTime();
                }

                int bp = obisCode.getF() - 1;
                if (bp > 0) {
                    registerValue = findRegisterValue(new ObisCode(1, 1, 0, 1, 2, bp));
                    if (registerValue != null) {
                        fromTime = registerValue.getToTime();
                    }
                }

            } else if (!obisCode.equals(ObisCode.fromString("1.1.0.1.0.255"))) {
                RegisterValue billingPointRegister = doReadRegister(ObisCode.fromString("1.1.0.1.0.255"), false);
                int billingPoint = billingPointRegister.getQuantity().intValue();
                if (billingPoint > 0) {
                    registerValue = findRegisterValue(ObisCode.fromString("1.1.0.1.2." + billingPoint));
                    if (registerValue != null) {
                        fromTime = registerValue.getToTime();
                    }
                }
            }

            return new RegisterValue(obisCode, quantity, eventTime, fromTime, toTime, new Date());
        } catch (NoSuchRegisterException e) {
            throw new NoSuchRegisterException("ObisCode " + obisCode.toString() + " is not supported!");
        } catch (FlagIEC1107ConnectionException e) {
            throw new IOException("doTheReadRegister(), error, " + e.getMessage());
        } catch (IOException e) {
            throw new IOException("doTheReadRegister(), error, " + e.getMessage());
        } catch (NumberFormatException e) {
            throw new NoSuchRegisterException("ObisCode " + obisCode.toString() + " is not supported!");
        }
    }

    private RegisterValue doTheReadBillingRegisterTimestamp(ObisCode obisCode) throws IOException {
        try {
            byte[] data = readRegisterData(obisCode);
            Date date = parseDate(data, 0);
            return new RegisterValue(obisCode, null, null, date);
        } catch (NoSuchRegisterException e) {
            throw new NoSuchRegisterException("ObisCode " + obisCode.toString() + " is not supported!");
        } catch (FlagIEC1107ConnectionException e) {
            throw new IOException("doTheReadBillingRegisterTimestamp(), error, " + e.getMessage());
        } catch (IOException e) {
            throw new IOException("doTheReadBillingRegisterTimestamp(), error, " + e.getMessage());
        } catch (NumberFormatException e) {
            throw new NoSuchRegisterException("ObisCode " + obisCode.toString() + " is not supported!");
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
        StringBuffer strBuff = new StringBuffer();


        if (isDataReadout()) {
            strBuff.append("******************* ExtendedLogging *******************\n");
            strBuff.append(new String(getDataReadout()));
        } else {
            strBuff.append("******************* ExtendedLogging *******************\n");
            strBuff.append("All OBIS codes are translated to EDIS codes but not all codes are configured in the meter.\n");
            strBuff.append("It is not possible to retrieve a list with all registers in the meter. Consult the configuration of the meter.");
            strBuff.append("\n");
        }
        logger.info(strBuff.toString());

    }


    // ********************************************************************************************************
    // implementation of the HHUEnabler interface
    public void enableHHUSignOn(SerialCommunicationChannel commChannel) throws ConnectionException {
        enableHHUSignOn(commChannel, isDataReadout());
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
     * Getter for property lzqjRegistry.
     *
     * @return Value of property lzqjRegistry.
     */
    public LZQJRegistry getLzqjRegistry() {
        return lzqjRegistry;
    }

    /**
     * Getter for property lzqjProfile.
     *
     * @return Value of property lzqjProfile.
     * @throws IOException
     */
    public LZQJProfile getLzqjProfile() {
        lzqjProfile = new LZQJProfile(this, this, lzqjRegistry);
        return lzqjProfile;
    }

    /**
     * Setter for the TimeZone
     *
     * @param timeZone - the TimeZone to set
     */
    public void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
    }

    /**
     * Setter for the ProfileHelper
     *
     * @param value
     */
    public void profileHelperSetter(boolean value) {
        this.profileHelper = value;
    }

    /**
     * Setter for the connection class
     *
     * @param connection the new connection class to set
     */
    protected void setConnection(FlagIEC1107Connection connection) {
        this.flagIEC1107Connection = connection;
    }

} // public class LZQJ implements MeterProtocol {
