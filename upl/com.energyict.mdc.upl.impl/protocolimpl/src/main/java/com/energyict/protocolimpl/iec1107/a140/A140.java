package com.energyict.protocolimpl.iec1107.a140;

import com.energyict.mdc.upl.ProtocolException;
import com.energyict.mdc.upl.properties.InvalidPropertyException;
import com.energyict.mdc.upl.properties.MissingPropertyException;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecBuilderWizard;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.TypedProperties;

import com.energyict.cbo.Quantity;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dialer.connection.HHUSignOn;
import com.energyict.dialer.connection.IEC1107HHUConnection;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.HHUEnabler;
import com.energyict.protocol.MeterExceptionInfo;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterProtocol;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.SerialNumber;
import com.energyict.protocol.meteridentification.DiscoverInfo;
import com.energyict.protocol.meteridentification.MeterType;
import com.energyict.protocol.support.SerialNumberSupport;
import com.energyict.protocolimpl.base.PluggableMeterProtocol;
import com.energyict.protocolimpl.base.ProtocolChannelMap;
import com.energyict.protocolimpl.errorhandling.ProtocolIOExceptionHandler;
import com.energyict.protocolimpl.iec1107.ChannelMap;
import com.energyict.protocolimpl.iec1107.FlagIEC1107Connection;
import com.energyict.protocolimpl.iec1107.FlagIEC1107ConnectionException;
import com.energyict.protocolimpl.iec1107.ProtocolLink;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;
import com.google.common.base.Supplier;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.energyict.mdc.upl.MeterProtocol.Property.ADDRESS;
import static com.energyict.mdc.upl.MeterProtocol.Property.CORRECTTIME;
import static com.energyict.mdc.upl.MeterProtocol.Property.NODEID;
import static com.energyict.mdc.upl.MeterProtocol.Property.PASSWORD;
import static com.energyict.mdc.upl.MeterProtocol.Property.ROUNDTRIPCORRECTION;
import static com.energyict.mdc.upl.MeterProtocol.Property.SERIALNUMBER;

/**
 * @author fbo
 * @beginchanges FBL |03112005| bugfix for DST transition, timechange in past and serialnr
 * || DST transition, a single value was lost.
 * || (due to generation of bad unique id for entry)
 * || timechange in past, overwrite behaviour for bad values.
 * || the newest value is now saved, unless it is an init value
 * || serialnr does not take dashes into account like the other Elster protocols.
 * FBL |24112005| bugfix TimeDate was buffered by protocol.  Must be
 * || reloaded every time.
 * FBL |24112006| bugfix for 0xE4 byte in date field.  A new day in Load
 * || Profile starts with "0xE4-date-demand period".  Within such a date
 * || an 0xE4 character can occur again.  Solution: when an 0xE4 byte is
 * || found, skip next 5 bytes (date=4 bytes and demand period = 1 byte).
 * @endchanges
 */

public class A140 extends PluggableMeterProtocol implements ProtocolLink, HHUEnabler,
        SerialNumber, MeterExceptionInfo, RegisterProtocol, SerialNumberSupport {

    private static final int DEBUG = 0;

    private static final long FORCE_DELAY = 350;

    /**
     * Property keys specific for A140 protocol.
     */
    private static final String PK_TIMEOUT = Property.TIMEOUT.getName();
    private static final String PK_RETRIES = Property.RETRIES.getName();
    private static final String PK_EXTENDED_LOGGING = "ExtendedLogging";

    /**
     * Property Default values
     */
    private static final String PD_NODE_ID = "";
    private static final int PD_TIMEOUT = 10000;
    private static final int PD_RETRIES = 5;
    private static final int PD_ROUNDTRIP_CORRECTION = 0;
    private static final int PD_SECURITY_LEVEL = 2;
    private static final String PD_EXTENDED_LOGGING = "0";
    private final PropertySpecService propertySpecService;

    /**
     * Property values Required properties will have NO default value Optional
     * properties make use of default value
     */
    private String pAddress = null;
    private String pNodeId = PD_NODE_ID;
    private String pSerialNumber = null;
    private String pPassword = null;

    /* Protocol timeout fail in msec */
    private int pTimeout = PD_TIMEOUT;

    /* Max nr of consecutive protocol errors before end of communication */
    private int pRetries = PD_RETRIES;
    /* Offset in ms to the get/set time */
    private int pRountTripCorrection = PD_ROUNDTRIP_CORRECTION;
    private int pCorrectTime = 0;

    private String pExtendedLogging = PD_EXTENDED_LOGGING;

    private MeterType meterType = null;
    private RegisterFactory rFactory = null;
    private ObisCodeMapper obisCodeMapper = null;
    private FlagIEC1107Connection flagConnection = null;
    private TimeZone timeZone = null;
    private Logger logger = null;
    private DataType dataType = null;

    private boolean software7E1;

    public A140(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Override
    public String getSerialNumber() {
        try {
            SerialNumberRegister serialNumber = getRegisterFactory().getSerialNumber();
            if(serialNumber == null){
                throw new ProtocolException("Serial number not available!");
            }
            return serialNumber.getSerialNumber();
        } catch (IOException e) {
            throw ProtocolIOExceptionHandler.handle(e, getNrOfRetries() + 1);
        }
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return Arrays.asList(
                this.stringSpec(ADDRESS.getName()),
                this.stringSpec(NODEID.getName()),
                this.stringSpec(SERIALNUMBER.getName()),
                this.stringSpec(PASSWORD.getName()),
                this.integerSpec(PK_TIMEOUT),
                this.integerSpec(PK_RETRIES),
                this.integerSpec(ROUNDTRIPCORRECTION.getName()),
                this.integerSpec(CORRECTTIME.getName()),
                this.stringSpec(PK_EXTENDED_LOGGING),
                this.stringSpec("Software7E1"));
    }

    protected  <T> PropertySpec spec(String name, Supplier<PropertySpecBuilderWizard.NlsOptions<T>> optionsSupplier) {
        return UPLPropertySpecFactory.specBuilder(name, false, optionsSupplier).finish();
    }

    protected PropertySpec stringSpec(String name) {
        return this.spec(name, this.propertySpecService::stringSpec);
    }

    protected PropertySpec integerSpec(String name) {
        return this.spec(name, this.propertySpecService::integerSpec);
    }

    @Override
    public void setProperties(TypedProperties p) throws InvalidPropertyException, MissingPropertyException {
        try {
            if (p.getTypedProperty(Property.ADDRESS.getName()) != null) {
                pAddress = p.getTypedProperty(Property.ADDRESS.getName());
            }

            if (p.getTypedProperty(Property.NODEID.getName()) != null) {
                pNodeId = p.getTypedProperty(Property.NODEID.getName());
            }

            if (p.getTypedProperty(Property.SERIALNUMBER.getName()) != null) {
                pSerialNumber = p.getTypedProperty(Property.SERIALNUMBER.getName());
            }

            if (p.getTypedProperty(Property.PASSWORD.getName()) != null) {
                pPassword = p.getTypedProperty(Property.PASSWORD.getName());
            }

            if (p.getTypedProperty(PK_TIMEOUT) != null) {
                pTimeout = Integer.parseInt(p.getTypedProperty(PK_TIMEOUT));
            }

            if (p.getTypedProperty(PK_RETRIES) != null) {
                pRetries = Integer.parseInt(p.getTypedProperty(PK_RETRIES));
            }

            if (p.getTypedProperty(Property.ROUNDTRIPCORRECTION.getName()) != null) {
                pRountTripCorrection = Integer.parseInt(p.getTypedProperty(Property.ROUNDTRIPCORRECTION.getName()));
            }

            if (p.getTypedProperty(Property.CORRECTTIME.getName()) != null) {
                pCorrectTime = Integer.parseInt(p.getTypedProperty(Property.CORRECTTIME.getName()));
            }

            if (p.getTypedProperty(PK_EXTENDED_LOGGING) != null) {
                pExtendedLogging = p.getTypedProperty(PK_EXTENDED_LOGGING);
            }
            this.software7E1 = !"0".equalsIgnoreCase(p.getTypedProperty("Software7E1", "0"));
        } catch (NumberFormatException e) {
            throw new InvalidPropertyException(e, this.getClass().getSimpleName() + ": validation of properties failed before");
        }

    }

    @Override
    public void init(InputStream inputStream, OutputStream outputStream,
                     TimeZone timeZone, Logger logger) throws IOException {
        this.timeZone = timeZone;
        this.logger = logger;
        if (logger.isLoggable(Level.INFO)) {
            String infoMsg =
                    "A140 protocol init \n"
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
        try {
            flagConnection = new FlagIEC1107Connection(inputStream,
                    outputStream, pTimeout, pRetries, FORCE_DELAY, 0, 1,
                    new CAI700(), software7E1, logger);
        } catch (ConnectionException e) {
            logger.severe("A140: init(...), " + e.getMessage());
        }
    }

    @Override
    public void connect() throws IOException {
        connect(0);
    }

    private void connect(int baudRate) throws IOException {
        try {
            meterType = flagConnection.connectMAC(pAddress, pPassword, PD_SECURITY_LEVEL, pNodeId, baudRate);
            logger.log(Level.INFO, "Meter " + meterType.getReceivedIdent());
            rFactory = new RegisterFactory(this);
            dataType = new DataType(timeZone);
            obisCodeMapper = new ObisCodeMapper(this, rFactory);
            doExtendedLogging();
        } catch (FlagIEC1107ConnectionException e) {
            disconnect();
            throw new IOException(e.getMessage());
        } catch (NumberFormatException nex) {
            throw new IOException(nex.getMessage());
        }
    }

    @Override
    public void disconnect() throws IOException {
        meterType = null;
        rFactory = null;
        obisCodeMapper = null;
        flagConnection.disconnectMAC();
    }

    @Override
    public int getNumberOfChannels() {
        return 1;
    }

    @Override
    public ProfileData getProfileData(boolean includeEvents) throws IOException {
        return rFactory.getLoadProfile().getProfileData();
    }

    @Override
    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        return rFactory.getLoadProfile().getProfileData(lastReading, new Date());
    }

    @Override
    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException {
        return rFactory.getLoadProfile().getProfileData(from, to);
    }

    @Override
    public int getProfileInterval() throws IOException {
        return rFactory.getLoadProfileConfig().getDemandPeriod();
    }

    @Override
    public FlagIEC1107Connection getFlagIEC1107Connection() {
        return flagConnection;
    }

    @Override
    public Logger getLogger() {
        return this.logger;
    }

    @Override
    public String getPassword() {
        return pPassword;
    }

    @Override
    public TimeZone getTimeZone() {
        return timeZone;
    }

    @Override
    public int getNrOfRetries() {
        return pRetries;
    }

    @Override
    public void enableHHUSignOn(SerialCommunicationChannel commChannel,
                                boolean enableDataReadout) throws ConnectionException {
        HHUSignOn hhuSignOn = new IEC1107HHUConnection(commChannel, pTimeout, pRetries, FORCE_DELAY, 0);
        hhuSignOn.setMode(HHUSignOn.MODE_PROGRAMMING);
        hhuSignOn.setProtocol(HHUSignOn.PROTOCOL_NORMAL);
        hhuSignOn.enableDataReadout(enableDataReadout);
        getFlagIEC1107Connection().setHHUSignOn(hhuSignOn);
    }

    @Override
    public void enableHHUSignOn(SerialCommunicationChannel commChannel) throws ConnectionException {
        enableHHUSignOn(commChannel, false);
    }

    @Override
    public byte[] getHHUDataReadout() {
        return getFlagIEC1107Connection().getHhuSignOn().getDataReadout();
    }

    @Override
    public String getSerialNumber(DiscoverInfo discoverInfo) throws IOException {
        SerialCommunicationChannel cChannel = discoverInfo.getCommChannel();
        String nodeId = discoverInfo.getNodeId();
        int baudrate = discoverInfo.getBaudrate();

        TypedProperties p = com.energyict.cpo.TypedProperties.empty();
        p.setProperty("SecurityLevel", "0");
        p.setProperty(com.energyict.mdc.upl.MeterProtocol.Property.NODEID.getName(), nodeId == null ? "" : nodeId);
        p.setProperty("IEC1107Compatible", "1");
        setProperties(p);

        init(cChannel.getInputStream(), cChannel.getOutputStream(), null, null);
        enableHHUSignOn(cChannel);
        connect(baudrate);
        String serialNumber = rFactory.getSerialNumber().getSerialNumber();
        disconnect();
        return serialNumber;
    }

    static final Map<String, String> EXCEPTION = new HashMap<>();

    static {
        EXCEPTION.put("ERR1", "Invalid Command/Function type e.g. other than W1, R1 etc");
        EXCEPTION.put("ERR2", "Invalid Data Identity Number e.g. Data id does not exist in the meter");
        EXCEPTION.put("ERR3", "Invalid Packet Number");
        EXCEPTION.put("ERR5", "Data Identity is locked - password timeout");
        EXCEPTION.put("ERR6", "General Comms error");
    }

    @Override
    public String getExceptionInfo(String id) {
        String exceptionInfo = EXCEPTION.get(id);
        if (exceptionInfo != null) {
            return id + ", " + exceptionInfo;
        } else {
            return "No meter specific exception info for " + id;
        }
    }

    @Override
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        return obisCodeMapper.getRegisterValue(obisCode);
    }

    @Override
    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        return ObisCodeMapper.getRegisterInfo(obisCode);
    }

    public RegisterFactory getRegisterFactory() {
        return rFactory;
    }

    private void doExtendedLogging() throws IOException {
        if ("1".equals(pExtendedLogging)) {
            logger.log(Level.INFO, obisCodeMapper.getExtendedLogging() + "\n");
            if (DEBUG > 0) {
                logger.log(Level.INFO, obisCodeMapper.getDebugLogging() + "\n");
            }
        }
    }

    @Override
    public String getProtocolVersion() {
        return "$Date: 2015-11-26 15:24:26 +0200 (Thu, 26 Nov 2015)$";
    }

    @Override
    public String getFirmwareVersion() {
        return null;
    }

    @Override
    public Quantity getMeterReading(int channelId) {
        return null;
    }

    @Override
    public Quantity getMeterReading(String name) throws IOException {
        return null;
    }

    @Override
    public Date getTime() throws IOException {
        return rFactory.getTimeAndDate().getTime();
    }

    @Override
    public void setTime() throws IOException {
        getFlagIEC1107Connection().authenticate();
        Calendar calendar = ProtocolUtils.getCalendar(timeZone);
        calendar.add(Calendar.MILLISECOND, pRountTripCorrection);
        rFactory.getTimeAndDate().setTime(calendar.getTime());
        rFactory.getTimeAndDate().write();
    }

    @Override
    public String getRegister(String name) {
        return null;
    }

    @Override
    public void setRegister(String name, String value) {
    }

    @Override
    public void initializeDevice() {
    }

    @Override
    public void release() {
    }

    @Override
    public boolean isIEC1107Compatible() {
        return true;
    }

    @Override
    public ProtocolChannelMap getProtocolChannelMap() {
        return null;
    }

    @Override
    public boolean isRequestHeader() {
        return false;
    }

    public DataType getDataType() {
        return dataType;
    }

    @Override
    public ChannelMap getChannelMap() {
        return null;
    }

    @Override
    public byte[] getDataReadout() {
        return null;
    }

    void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
    }

    void setLogger(Logger logger) {
        this.logger = logger;
    }

}