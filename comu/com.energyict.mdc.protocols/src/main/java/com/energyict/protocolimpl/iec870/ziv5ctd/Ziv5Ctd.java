/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.iec870.ziv5ctd;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.InvalidPropertyException;
import com.energyict.mdc.protocol.api.MissingPropertyException;
import com.energyict.mdc.protocol.api.SerialNumber;
import com.energyict.mdc.protocol.api.UnsupportedException;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterProtocol;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;
import com.energyict.mdc.protocol.api.device.events.MeterEvent;
import com.energyict.mdc.protocol.api.dialer.connection.ConnectionException;
import com.energyict.mdc.protocol.api.dialer.core.SerialCommunicationChannel;
import com.energyict.mdc.protocol.api.inbound.DiscoverInfo;
import com.energyict.mdc.protocol.api.legacy.MeterProtocol;
import com.energyict.mdc.protocol.api.legacy.dynamic.PropertySpecFactory;
import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.protocolimpl.base.PluggableMeterProtocol;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author fbo
 * @beginchanges
 * @endchanges
 */

public class Ziv5Ctd extends PluggableMeterProtocol implements SerialNumber, RegisterProtocol {

    @Override
    public String getProtocolDescription() {
        return "Ziv 5CTD IEC870-102";
    }

    static final BigDecimal MAX_PROFILE_VALUE = new BigDecimal(9999999);

    /**
     * Property keys
     */
    static final String PK_TIMEOUT = "Timeout";
    static final String PK_RETRIES = "Retries";
    static final String PK_EXTENDED_LOGGING = "ExtendedLogging";
    static final String PK_FETCH_PROGRAM_PROFILE = "FetchProgramProfile";
    static final String PK_CUMULATIVE_PROFILE = "CumulativeProfile";

    /**
     * Property Default values
     */
    static final String PD_NODE_ID = "";
    static final int PD_TIMEOUT = 10000;
    static final int PD_RETRIES = 5;
    static final int PD_ROUNDTRIP_CORRECTION = 0;
    static final int PD_SECURITY_LEVEL = 2;
    static final String PD_EXTENDED_LOGGING = "0";
    static final boolean PD_CUMULATIVE_PROFILE = true;

    /**
     * Property values Required properties will have NO default value Optional
     * properties make use of default value
     */
    String pAddress = null;
    String pNodeId = PD_NODE_ID;
    String pSerialNumber = null;
    int pProfileInterval;
    int pPassword;

    /* Protocol timeout fail in msec */
    int pTimeout = PD_TIMEOUT;

    /* Max nr of consecutive protocol errors before end of communication */
    int pRetries = PD_RETRIES;
    /* Offset in ms to the get/set time */
    int pRountTripCorrection = PD_ROUNDTRIP_CORRECTION;
    int pCorrectTime = 0;

    String pFetchProgramProfile = "0";
    boolean pCumulativeProfile = PD_CUMULATIVE_PROFILE;
    String pExtendedLogging = PD_EXTENDED_LOGGING;

    LinkLayer linkLayer;
    FrameFactory frameFactory;
    AsduFactory asduFactory;

    private RegisterFactory rFactory = null;
    private ObisCodeMapper obisCodeMapper = null;
    private TimeZone timeZone = null;
    private Logger logger = null;

    @Inject
    public Ziv5Ctd(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    /* ___ Implement interface MeterProtocol ___ */

    /*
    * (non-Javadoc)
    *
    * @see com.energyict.protocol.MeterProtocol#
    *      setProperties(java.util.Properties)
    */
    public void setProperties(Properties p) throws InvalidPropertyException,
            MissingPropertyException {

        if (p.getProperty(MeterProtocol.ADDRESS) != null) {
            pAddress = p.getProperty(MeterProtocol.ADDRESS);
        }

        if (p.getProperty(MeterProtocol.NODEID) != null) {
            pNodeId = p.getProperty(MeterProtocol.NODEID);
        }

        if (p.getProperty(MeterProtocol.SERIALNUMBER) != null) {
            pSerialNumber = p.getProperty(MeterProtocol.SERIALNUMBER);
        }

        if (p.getProperty(MeterProtocol.PROFILEINTERVAL) != null) {
            pProfileInterval = Integer.parseInt(p.getProperty(MeterProtocol.PROFILEINTERVAL));
        }

        if (p.getProperty(MeterProtocol.PASSWORD) != null) {
            pPassword = Integer.parseInt(p.getProperty(MeterProtocol.PASSWORD));
        }

        if (p.getProperty(PK_TIMEOUT) != null) {
            pTimeout = Integer.parseInt(p.getProperty(PK_TIMEOUT));
        }

        if (p.getProperty(PK_RETRIES) != null) {
            pRetries = Integer.parseInt(p.getProperty(PK_RETRIES));
        }

        if (p.getProperty(MeterProtocol.ROUNDTRIPCORR) != null) {
            pRountTripCorrection = Integer.parseInt(p.getProperty(MeterProtocol.ROUNDTRIPCORR));
        }

        if (p.getProperty(MeterProtocol.CORRECTTIME) != null) {
            pCorrectTime = Integer.parseInt(p.getProperty(MeterProtocol.CORRECTTIME));
        }

        if (p.getProperty(PK_EXTENDED_LOGGING) != null) {
            pExtendedLogging = p.getProperty(PK_EXTENDED_LOGGING);
        }

        if (p.getProperty(PK_FETCH_PROGRAM_PROFILE) != null) {
            pFetchProgramProfile = p.getProperty(PK_FETCH_PROGRAM_PROFILE);
        }

        if (p.getProperty(PK_CUMULATIVE_PROFILE) != null) {
            pCumulativeProfile = ("1".equals(p.getProperty(PK_CUMULATIVE_PROFILE)));
        }

        validateProperties();

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
        return Arrays.asList(
                    MeterProtocol.ADDRESS,
                    PK_TIMEOUT,
                    PK_RETRIES,
                    PK_EXTENDED_LOGGING,
                    PK_FETCH_PROGRAM_PROFILE,
                    PK_CUMULATIVE_PROFILE);
    }

    /*
    * (non-Javadoc)
    *
    * @see com.energyict.protocol.MeterProtocol#init( java.io.InputStream,
    *      java.io.OutputStream, java.util.TimeZone, java.util.logging.Logger)
    */
    public void init(InputStream inputStream, OutputStream outputStream,
                     TimeZone timeZone, Logger logger) throws IOException {

        this.timeZone = timeZone;
        this.logger = logger;

        try {

            TypeIdentificationFactory tif = new TypeIdentificationFactory(timeZone);
            asduFactory = new AsduFactory(Address.DEFAULT, tif);
            frameFactory = new FrameFactory(Address.DEFAULT, asduFactory);
            linkLayer = new LinkLayer(inputStream, outputStream, 0, 0, this, pRetries);
            rFactory = new RegisterFactory(this, asduFactory);
            obisCodeMapper = new ObisCodeMapper(this, rFactory);

        } catch (ConnectionException e) {
            logger.severe("Ziv5Ctd, " + e.getMessage());
            throw e;
        }

        if (logger.isLoggable(Level.INFO)) {
            String infoMsg =
                    "ZIV protocol init \n"
                            + " Address = " + pAddress + ","
                            + " Node Id = " + pNodeId + ","
                            + " SerialNr = " + pSerialNumber + ","
                            + " Psswd = " + pPassword + ","
                            + " Timeout = " + pTimeout + ","
                            + " Retries = " + pRetries + ","
                            + " Ext. Logging = " + pExtendedLogging + ","
                            + " RoundTripCorr = " + pRountTripCorrection + ","
                            + " Correct Time = " + pCorrectTime + ","
                            + " TimeZone = " + timeZone.getID();

            logger.info(infoMsg);

        }

    }

    /*
    * (non-Javadoc)
    *
    * @see com.energyict.protocol.MeterProtocol#connect()
    */
    public void connect() throws IOException {
        connect(0);
    }

    void connect(int baudRate) throws IOException {
        try {

            linkLayer.connect();
            linkLayer.requestRespond(asduFactory.createType0xB7(pPassword));

            validateSerialNumber();
            doExtendedLogging();

        } catch (NumberFormatException nex) {
            throw new IOException(nex.getMessage());
        }
    }

    public void disconnect() throws IOException {
        rFactory = null;
        obisCodeMapper = null;
    }

    public int getNumberOfChannels() throws IOException {
        return 6;   // always 6 channels ...
    }

    /* (non-Javadoc)
    * @see com.energyict.protocol.MeterProtocol#getProfileData(boolean)
    */
    public ProfileData getProfileData(boolean includeEvents) throws IOException {
        Calendar c = Calendar.getInstance(timeZone);

        Date to = c.getTime();
        c.set(Calendar.YEAR, c.get(Calendar.YEAR) - 1);
        Date from = c.getTime();

        return getProfileData(from, to, includeEvents);
    }

    /* (non-Javadoc)
    * @see com.energyict.protocol.MeterProtocol#getProfileData(java.util.Date, boolean)
    */
    public ProfileData getProfileData(Date lastReading, boolean includeEvents)
            throws IOException {

        return getProfileData(lastReading, new Date(), includeEvents);

    }

    /* (non-Javadoc)
    * @see com.energyict.protocol.MeterProtocol#getProfileData(java.util.Date, java.util.Date, boolean)
    */
    public ProfileData getProfileData(Date from, Date to, boolean includeEvents)
            throws IOException {

        int registerAddress = 0x0b;
        if ("1".equals(pFetchProgramProfile)) {
            registerAddress = 0x0c;
        }

        Asdu a;
        if (pCumulativeProfile) {
            a = asduFactory.create0x7a(registerAddress, from, to);
        } else {
            a = asduFactory.create0x7b(registerAddress, from, to);
        }

        ApplicationFunction appFunction = new ApplicationFunction(this);
        ProfileData result = (ProfileData) appFunction.read(a);

        result.generateEvents();

        if (includeEvents) {
            Iterator ei = rFactory.getMeterEvents(from, to).iterator();
            while (ei.hasNext()) {
                result.addEvent((MeterEvent) ei.next());
            }
        }

        return result;
    }

    public int getProfileInterval() throws IOException {
        if ("1".equals(pFetchProgramProfile))
        // programmed profile has a configurable integration time
        {
            return pProfileInterval;
        } else
        // standard profile always has an integration time of 1 hour
        {
            return 3600;
        }
    }

    /*
    * (non-Javadoc)
    *
    * @see com.energyict.protocolimpl.base.SerialNumber#getSerialNumber(com.energyict.dialer.core.SerialCommunicationChannel,
    *      java.lang.String)
    */
    public String getSerialNumber(DiscoverInfo discoverInfo) throws IOException {
        SerialCommunicationChannel cChannel = discoverInfo.getCommChannel();
        String nodeId = discoverInfo.getNodeId();
        int baudrate = discoverInfo.getBaudrate();

        Properties p = new Properties();
        p.setProperty(MeterProtocol.NODEID, nodeId == null ? "" : nodeId);
        setProperties(p);

        init(cChannel.getInputStream(), cChannel.getOutputStream(), null, null);
        connect(baudrate);
        String serialNumber = rFactory.getInfoObject47().getProductCode();
        disconnect();
        return serialNumber;
    }

    /* ___ Implement interface RegisterProtocol ___ */

    /*
    * (non-Javadoc)
    *
    * @see com.energyict.protocol.RegisterProtocol#readRegister(com.energyict.obis.ObisCode)
    */
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        return obisCodeMapper.getRegisterValue(obisCode);
    }

    /*
    * (non-Javadoc)
    *
    * @see com.energyict.protocol.RegisterProtocol#translateRegister(com.energyict.obis.ObisCode)
    */
    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        return ObisCodeMapper.getRegisterInfo(obisCode);
    }

    /**
     * @throws java.io.IOException
     */
    public void doExtendedLogging() throws IOException {
        if ("1".equals(pExtendedLogging)) {
            logger.log(Level.INFO, obisCodeMapper.getExtendedLogging() + "\n");
        }
        if ("2".equals(pExtendedLogging)) {
            logger.log(Level.INFO, obisCodeMapper.getDebugLogging() + "\n");
        }
    }

    public String getProtocolVersion() {
        return "$Date: 2013-10-31 11:22:19 +0100 (Thu, 31 Oct 2013) $";
    }

    public String getFirmwareVersion() throws IOException {
        throw new UnsupportedException();
    }

    public Quantity getMeterReading(int channelId) throws
            IOException {
        throw new UnsupportedException();
    }

    public Quantity getMeterReading(String name) throws
            IOException {
        throw new UnsupportedException();
    }

    public Date getTime() throws IOException {
        return rFactory.get48().getDate();
    }

    public void setTime() throws IOException {
        Calendar calendar = null;
        calendar = ProtocolUtils.getCalendar(timeZone);
        calendar.add(Calendar.MILLISECOND, pRountTripCorrection);

        CP56Time time = new CP56Time(timeZone, calendar.getTime());
        Asdu a = asduFactory.createType0xB5(time);
        ApplicationFunction appFunction = new ApplicationFunction(this);
        appFunction.read(a);

    }

    public String getRegister(String name) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    public void setRegister(String name, String value) throws IOException {
        // TODO Auto-generated method stub

    }

    public void initializeDevice() throws IOException {
        // TODO Auto-generated method stub
    }

    public void release() throws IOException {
        // TODO Auto-generated method stub
    }

    public boolean isRequestHeader() {
        // TODO Auto-generated method stub
        return false;
    }

    /* ___ Private property checking ___ */

    private void validateSerialNumber() throws IOException {

        String sn = rFactory.getInfoObject47().getProductCode();

        if (pSerialNumber == null || pSerialNumber.equals(sn)) {
            return;
        }

        throw new IOException("SerialNumber mismatch! meter sn=" + sn
                + ", configured sn=" + pSerialNumber);
    }

    private void validateProperties() {

    }

    /* ___ Unsupported methods ___ */

    public void setCache(Object cacheObject) {
    }

    public Object getCache() {
        return null;
    }

    public Object fetchCache(int rtuid) {
        return null;
    }

    public void updateCache(int rtuid, Object cacheObject) {
    }

    /*
    * (non-Javadoc)
    *
    * @see com.energyict.protocolimpl.iec1107.ProtocolLink#getDataReadout()
    */
    public byte[] getDataReadout() {
        return null;
    }

    /**
     * for easy debugging
     */
    void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
    }

    TimeZone getTimeZone() {
        return timeZone;
    }

    /**
     * for easy debugging
     */
    void setLogger(Logger logger) {
        this.logger = logger;
    }

}