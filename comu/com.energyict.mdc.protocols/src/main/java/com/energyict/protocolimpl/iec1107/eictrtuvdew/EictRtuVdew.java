/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * EictRtuVdew.java
 *
 * Created on 10 januari 2005, 09:19
 */

package com.energyict.protocolimpl.iec1107.eictrtuvdew;

import com.elster.jupiter.properties.PropertySpec;
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
import com.energyict.mdc.protocol.api.legacy.HalfDuplexController;
import com.energyict.mdc.protocol.api.legacy.HalfDuplexEnabler;
import com.energyict.mdc.protocol.api.legacy.MeterProtocol;
import com.energyict.mdc.protocol.api.legacy.dynamic.PropertySpecFactory;
import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.dialer.connection.IEC1107HHUConnection;
import com.energyict.protocolimpl.base.DataParser;
import com.energyict.protocolimpl.base.PluggableMeterProtocol;
import com.energyict.protocolimpl.base.ProtocolChannelMap;
import com.energyict.protocolimpl.iec1107.ChannelMap;
import com.energyict.protocolimpl.iec1107.FlagIEC1107Connection;
import com.energyict.protocolimpl.iec1107.FlagIEC1107ConnectionException;
import com.energyict.protocolimpl.iec1107.ProtocolLink;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
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
 *         Class that implements the EictRtuVdew meter protocol.
 *         <BR>
 *         <B>@beginchanges</B><BR>
 *         KV|10012005|Initial version
 *         KV|23032005|Changed header to be compatible with protocol version tool
 *         KV|30032005|Handle StringOutOfBoundException in IEC1107 connection layer
 *         KV|06092005|VDEW changed to do channel mapping!
 * @version 1.0
 * @endchanges
 */
public class EictRtuVdew extends PluggableMeterProtocol implements HHUEnabler, ProtocolLink, MeterExceptionInfo, RegisterProtocol, HalfDuplexEnabler {

    @Override
    public String getProtocolDescription() {
        return "EnergyICT RTU IEC1107 (VDEW)";
    }

    private static final byte DEBUG = 0;

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
    ProtocolChannelMap protocolChannelMap = null;
    private int scaler;
    private int forcedDelay;

    private int halfDuplex;
    private HalfDuplexController halfDuplexController;

    private TimeZone timeZone;
    private Logger logger;

    private boolean software7E1;

    FlagIEC1107Connection flagIEC1107Connection = null;
    EictRtuVdewRegistry eictRtuVdewRegistry = null;
    EictRtuVdewProfile eictRtuVdewProfile = null;

    private byte[] dataReadout = null;

    @Inject
    public EictRtuVdew(PropertySpecService propertySpecService) {
        super(propertySpecService);
    } // public EictRtuVdew()

    public ProfileData getProfileData(boolean includeEvents) throws IOException {
        Calendar calendar = ProtocolUtils.getCalendar(timeZone);
        calendar.add(Calendar.YEAR, -10);
        return getProfileData(calendar.getTime(), includeEvents);
    }

    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        return getEictRtuVdewProfile().getProfileData(lastReading, includeEvents);
    }

    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException {
        return getEictRtuVdewProfile().getProfileData(from, to, includeEvents);
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
        Calendar calendar = null;
        calendar = ProtocolUtils.getCalendar(timeZone);
        calendar.add(Calendar.MILLISECOND, iRoundtripCorrection);
        Date date = calendar.getTime();
        getEictRtuVdewRegistry().setRegister("Time", date);
        getEictRtuVdewRegistry().setRegister("Date", date);
    } // public void setTime() throws IOException

    public Date getTime() throws IOException {
        Date date = (Date) getEictRtuVdewRegistry().getRegister("TimeDate");
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
            requestHeader = Integer.parseInt(properties.getProperty("RequestHeader", "0").trim());
            // KV 07092005 K&P
            protocolChannelMap = new ProtocolChannelMap(properties.getProperty("ChannelMap", "0.0 1.1 2.2 3.3 4.4 5.5 6.6 7.7 8.8 9.9 10.10 11.11 12.12 13.13 14.14 15.15 16.16 17.17 18.18 19.19 20.20 21.21 22.22 23.23 24.24 25.25 26.26 27.27 28.28 29.29 30.30 31.31"));
            scaler = Integer.parseInt(properties.getProperty("Scaler", "0").trim());
            halfDuplex = Integer.parseInt(properties.getProperty("HalfDuplex", "0").trim());
            forcedDelay = Integer.parseInt(properties.getProperty("ForcedDelay", "0").trim());
            this.software7E1 = !properties.getProperty("Software7E1", "0").equalsIgnoreCase("0");
        } catch (NumberFormatException e) {
            throw new InvalidPropertyException("DukePower, validateProperties, NumberFormatException, " + e.getMessage());
        }

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
        return ProtocolUtils.obj2String(getEictRtuVdewRegistry().getRegister(name));
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
        getEictRtuVdewRegistry().setRegister(name, value);
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

    public List<String> getOptionalKeys() {
        List<String> result = new ArrayList<>();
        result.add("Timeout");
        result.add("Retries");
        result.add("SecurityLevel");
        result.add("EchoCancelling");
        result.add("IEC1107Compatible");
        result.add("ChannelMap");
        result.add("RequestHeader");
        result.add("Scaler");
        result.add("HalfDuplex");
        result.add("ForcedDelay");
        result.add("Software7E1");
        return result;
    }

    public String getProtocolVersion() {
        return "$Date: 2013-10-31 11:22:19 +0100 (Thu, 31 Oct 2013) $";
    }

    public String getFirmwareVersion() throws IOException {
        return ("Unknown");
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
            flagIEC1107Connection = new FlagIEC1107Connection(inputStream, outputStream, iIEC1107TimeoutProperty, iProtocolRetriesProperty, forcedDelay, iEchoCancelling, iIEC1107Compatible, null, halfDuplex != 0 ? halfDuplexController : null, software7E1, logger);
            eictRtuVdewRegistry = new EictRtuVdewRegistry(this, this);
            eictRtuVdewProfile = new EictRtuVdewProfile(this, this, eictRtuVdewRegistry);
        } catch (ConnectionException e) {
            logger.severe("ABBA1500: init(...), " + e.getMessage());
        }

    } // public void init(InputStream inputStream,OutputStream outputStream,TimeZone timeZone,Logger logger)

    /**
     * @throws IOException
     */
    public void connect() throws IOException {
        try {
            //dataReadout = flagIEC1107Connection.dataReadout(strID,nodeId);
            //flagIEC1107Connection.disconnectMAC();
            /*   try {
                Thread.sleep(2000);
            }
            catch(InterruptedException e) {
                throw new NestedIOException(e);
            }*/
            flagIEC1107Connection.connectMAC(strID, strPassword, iSecurityLevel, nodeId);
        } catch (FlagIEC1107ConnectionException e) {
            throw new IOException(e.getMessage());
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
            return getEictRtuVdewProfile().getProfileHeader().getNrOfChannels();
        } else {
            return getProtocolChannelMap().getNrOfProtocolChannels();
        }
    }

    public int getProfileInterval() throws IOException {
        if (requestHeader == 1) {
            return getEictRtuVdewProfile().getProfileHeader().getProfileInterval();
        } else {
            return profileInterval;
        }
    }

    private EictRtuVdewRegistry getEictRtuVdewRegistry() {
        return eictRtuVdewRegistry;
    }

    private EictRtuVdewProfile getEictRtuVdewProfile() {
        return eictRtuVdewProfile;
    }

    // Implementation of interface ProtocolLink
    public FlagIEC1107Connection getFlagIEC1107Connection() {
        return flagIEC1107Connection;
    }

    public TimeZone getTimeZone() {
        return timeZone;
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

    static Map exceptionInfoMap = new HashMap();

    static {
        exceptionInfoMap.put("ERROR", "Request could not execute!");
    }

    public String getExceptionInfo(String id) {
        String exceptionInfo = (String) exceptionInfoMap.get(id);
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

    private BigDecimal doGetRegister(String name) throws IOException {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byteArrayOutputStream.write(name.getBytes());
            flagIEC1107Connection.sendRawCommandFrame(FlagIEC1107Connection.READ5, byteArrayOutputStream.toByteArray());
            byte[] data = flagIEC1107Connection.receiveRawData();

            DataParser dp = new DataParser(getTimeZone());
            BigDecimal bd = new BigDecimal(dp.parseBetweenBrackets(data, 0));

            return bd;
        } catch (FlagIEC1107ConnectionException e) {
            throw new IOException("getMeterReading() error, " + e.getMessage());
        } catch (IOException e) {
            throw new IOException("getMeterReading() error, " + e.getMessage());
        } catch (NumberFormatException e) {
            throw new NoSuchRegisterException("Register with EDIS code " + name + " does not exist!");
        }
    }

    private Date doGetRegisterDate(String name) throws IOException {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byteArrayOutputStream.write(name.getBytes());
            flagIEC1107Connection.sendRawCommandFrame(FlagIEC1107Connection.READ5, byteArrayOutputStream.toByteArray());
            byte[] data = flagIEC1107Connection.receiveRawData();

            DataParser dp = new DataParser(getTimeZone());
            dp.parseBetweenBrackets(data, 1);

            return null;
        } catch (FlagIEC1107ConnectionException e) {
            throw new IOException("getMeterReading() error, " + e.getMessage());
        } catch (IOException e) {
            throw new IOException("getMeterReading() error, " + e.getMessage());
        }
    }


    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        String edisNotation = obisCode.getA() + "-" + obisCode.getB() + ":" + obisCode.getC() + "." + obisCode.getD() + "." + obisCode.getE() + (obisCode.getF() == 255 ? "" : "*" + Math.abs(obisCode.getF()));
        BigDecimal bd = doGetRegister(edisNotation + "(;)");
        return new RegisterValue(obisCode, new Quantity(bd, obisCode.getUnitElectricity(scaler)));
    }

    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        return new RegisterInfo(obisCode.getDescription());
    }

    // ********************************************************************************************************
    // implementation of the HHUEnabler interface
    public void enableHHUSignOn(SerialCommunicationChannel commChannel) throws ConnectionException {
        enableHHUSignOn(commChannel, false);
    }

    public void enableHHUSignOn(SerialCommunicationChannel commChannel, boolean datareadout) throws ConnectionException {
        HHUSignOn hhuSignOn = new IEC1107HHUConnection(commChannel, iIEC1107TimeoutProperty, iProtocolRetriesProperty, 300, iEchoCancelling);
        hhuSignOn.setMode(HHUSignOn.MODE_PROGRAMMING);
        hhuSignOn.setProtocol(HHUSignOn.PROTOCOL_NORMAL);
        hhuSignOn.enableDataReadout(datareadout);
        getFlagIEC1107Connection().setHHUSignOn(hhuSignOn);
    }

    public byte[] getHHUDataReadout() {
        setDataReadout(getFlagIEC1107Connection().getHhuSignOn().getDataReadout());
        return getDataReadout();
    }

    public void setDataReadout(byte[] dataReadout) {
        this.dataReadout = dataReadout;
    }

    // implement HalfDuplexEnabler
    public void setHalfDuplexController(HalfDuplexController halfDuplexController) {
        this.halfDuplexController = halfDuplexController;
        halfDuplexController.setDelay(halfDuplex);
    }
} // public class EictRtuVdew implements MeterProtocol {
