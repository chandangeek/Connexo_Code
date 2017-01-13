/*
 * ABBA230.java
 *
 * <B>Description :</B><BR>
 * Class that implements the Elster AS230 meter protocol.
 * <BR>
 * <B>@beginchanges</B><BR>
FBO|02022006|Initial version
FBO|29052006|Fix profile data: data was fetched as energy values, but the meter
stores power/demand values.
FBO|30052006|Fix profile data: When a time set occurs and the time meter is set
back for more then one interval period, there will be double entries in the
profile data.  These entries will not get a SL flag from the meter.  Since these
entries occur twice or more they need an SL flag.
 *@endchanges
 */

package com.energyict.protocolimpl.iec1107.abba230;

import com.energyict.mdc.io.NestedIOException;
import com.energyict.mdc.upl.UnsupportedException;
import com.energyict.mdc.upl.cache.CacheMechanism;
import com.energyict.mdc.upl.cache.CachingProtocol;
import com.energyict.mdc.upl.cache.ProtocolCacheFetchException;
import com.energyict.mdc.upl.cache.ProtocolCacheUpdateException;
import com.energyict.mdc.upl.messages.legacy.Message;
import com.energyict.mdc.upl.messages.legacy.MessageAttribute;
import com.energyict.mdc.upl.messages.legacy.MessageCategorySpec;
import com.energyict.mdc.upl.messages.legacy.MessageElement;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.MessageSpec;
import com.energyict.mdc.upl.messages.legacy.MessageTag;
import com.energyict.mdc.upl.messages.legacy.MessageTagSpec;
import com.energyict.mdc.upl.messages.legacy.MessageValue;
import com.energyict.mdc.upl.properties.InvalidPropertyException;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecBuilderWizard;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.properties.TypedProperties;

import com.energyict.cbo.Quantity;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dialer.connection.HHUSignOn;
import com.energyict.dialer.connection.IEC1107HHUConnection;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.EventMapper;
import com.energyict.protocol.HHUEnabler;
import com.energyict.protocol.MessageProtocol;
import com.energyict.protocol.MessageResult;
import com.energyict.protocol.MeterExceptionInfo;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterProtocol;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.SerialNumber;
import com.energyict.protocol.meteridentification.DiscoverInfo;
import com.energyict.protocol.support.SerialNumberSupport;
import com.energyict.protocolimpl.base.ContactorController;
import com.energyict.protocolimpl.base.PluggableMeterProtocol;
import com.energyict.protocolimpl.base.ProtocolChannelMap;
import com.energyict.protocolimpl.errorhandling.ProtocolIOExceptionHandler;
import com.energyict.protocolimpl.iec1107.ChannelMap;
import com.energyict.protocolimpl.iec1107.FlagIEC1107Connection;
import com.energyict.protocolimpl.iec1107.FlagIEC1107ConnectionException;
import com.energyict.protocolimpl.iec1107.ProtocolLink;
import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.function.Supplier;
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
 * KV	25112008 	Changed authentication mechanism with new security level
 * KV	02122008 	Add intervalstate bits to logbook
 * JME	23012009	Fixed Java 1.5 <=> 1.4 issues to port from 8.1 to 7.5, 7.3 or 7.1
 * JME	20022009	Implemented Billing reset message
 * JME	13032009	Fixed bug in Billing reset message
 * JME	24032009	Added delay during close contactor message execution between the ARM and CLOSE command.
 * JME	27032009	Made extended logging more robust for meter configuration
 * JME	02042009	Moved contactor code to new class and fixed bug in close message (Mantis issue 4047)
 * SVA  18102011    Extending the protocol to enable the collection of Instrumentation Channels
 *                  If property 'InstrumentationProfileMode' is set, the Instrumentation Profiles will be read out instead of the Load Profiles.
 *
 */
public class ABBA230 extends PluggableMeterProtocol implements ProtocolLink, HHUEnabler, SerialNumber, MeterExceptionInfo,
                                                               RegisterProtocol, MessageProtocol, EventMapper, SerialNumberSupport, CachingProtocol {

    private static final int DEBUG = 0;

    private boolean firmwareUpgrade = false;

    private static final String CONNECT = "ConnectLoad";
    private static final String DISCONNECT = "DisconnectLoad";
    private static final String ARM = "ArmMeter";
    private static final String TARIFFPROGRAM = "UploadMeterScheme";
    private static final String FIRMWAREPROGRAM = "UpgradeMeterFirmware";
    private static final String BILLINGRESET = RtuMessageConstant.BILLINGRESET;

    private static final String CONNECT_DISPLAY = "Connect Load";
    private static final String DISCONNECT_DISPLAY = "Disconnect Load";
    private static final String ARM_DISPLAY = "Arm Meter";
    private static final String TARIFFPROGRAM_DISPLAY = "Upload Meter Scheme";
    private static final String FIRMWAREPROGRAM_DISPLAY = "Upgrade Meter Firmware";
    private static final String BILLINGRESET_DISPLAY = "Billing reset";

    /**
     * Property keys specific for AS230 protocol.
     */
    private static final String PK_TIMEOUT = Property.TIMEOUT.getName();
    private static final String PK_RETRIES = Property.RETRIES.getName();
    private static final String PK_SECURITYLEVEL = Property.SECURITYLEVEL.getName();
    private static final String PK_FORCED_DELAY = "ForcedDelay";
    private static final String PK_EXTENDED_LOGGING = "ExtendedLogging";
    private static final String PK_IEC1107_COMPATIBLE = "IEC1107Compatible";
    private static final String PK_ECHO_CANCELING = "EchoCancelling";

    private static final String PK_SCRIPTING_ENABLED = "ScriptingEnabled";
    private static final String INSTRUMENTATION_PROFILE_MODE = "InstrumentationProfileMode";

    /**
     * Property Default values
     */
    private static final String PD_NODE_ID = "";
    private static final int PD_TIMEOUT = 10000;
    private static final int PD_RETRIES = 5;
    private static final int PD_ROUNDTRIP_CORRECTION = 0;
    private static final int PD_SECURITY_LEVEL = 2;
    private static final int PD_EXTENDED_LOGGING = 0;
    private static final int PD_IEC1107_COMPATIBLE = 1;
    private static final int PD_ECHO_CANCELING = 0;
    private static final int PD_FORCED_DELAY = 300;

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
    private int forcedDelay = PD_FORCED_DELAY;
    /* Offset in ms to the get/set time */
    private int pRoundTripCorrection = PD_ROUNDTRIP_CORRECTION;
    private int pSecurityLevel = PD_SECURITY_LEVEL;
    private int pCorrectTime = 0;
    private int pExtendedLogging = PD_EXTENDED_LOGGING;
    private int pEchoCancelling = PD_ECHO_CANCELING;
    private int pIEC1107Compatible = PD_IEC1107_COMPATIBLE;

    private TimeZone timeZone;
    private Logger logger;
    private FlagIEC1107Connection flagConnection = null;
    private ABBA230RegisterFactory rFactory = null;
    private ABBA230Profile profile = null;

    private CacheMechanism cacheObject = null;
    private boolean software7E1;
    private int scriptingEnabled = 0;
    private int nrOfProfileBlocks = 0;

    /**
     * Indication whether to send a break command before a retry
     */
    private boolean dontSendBreakCommand;
    /**
     * Indicate whether to send a break before the disconnect. The state is dependant on the other break parameter ({@link #dontSendBreakCommand})
     * Mostly you will send it, just not if the signon failed.
     */
    private boolean sendBreakBeforeDisconnect;

    /**
     * Indicate whether the normal Load Profile Data should be read out, or if the Instrumentation Profile Data must be read instead.
     * By default (boolean false) the Load Profile Data will be read out.
     */
    private boolean instrumentationProfileMode;

    private final PropertySpecService propertySpecService;

    public ABBA230(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Override
    public String getSerialNumber() {
        try {
            return (String) this.rFactory.getRegister("SerialNumber");
        } catch (IOException e) {
            throw ProtocolIOExceptionHandler.handle(e, getNrOfRetries() + 1);
        }
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        return Arrays.asList(
                UPLPropertySpecFactory.specBuilder(ADDRESS.getName(), false, this.propertySpecService::stringSpec).finish(),
                UPLPropertySpecFactory.specBuilder(NODEID.getName(), false, this.propertySpecService::stringSpec).finish(),
                UPLPropertySpecFactory.specBuilder(SERIALNUMBER.getName(), false, this.propertySpecService::stringSpec).finish(),
                passwordPropertySpec(false),
                UPLPropertySpecFactory.specBuilder(PK_TIMEOUT, false, this.propertySpecService::integerSpec).finish(),
                UPLPropertySpecFactory.specBuilder(PK_RETRIES, false, this.propertySpecService::integerSpec).finish(),
                UPLPropertySpecFactory.specBuilder(ROUNDTRIPCORRECTION.getName(), false, this.propertySpecService::integerSpec).finish(),
                UPLPropertySpecFactory.specBuilder(CORRECTTIME.getName(), false, this.propertySpecService::integerSpec).finish(),
                UPLPropertySpecFactory.specBuilder(PK_EXTENDED_LOGGING, false, this.propertySpecService::stringSpec).finish(),
                UPLPropertySpecFactory.specBuilder(PK_SECURITYLEVEL, false, this.propertySpecService::integerSpec).finish(),
                UPLPropertySpecFactory.specBuilder(PK_ECHO_CANCELING, false, this.propertySpecService::integerSpec).finish(),
                UPLPropertySpecFactory.specBuilder(PK_SCRIPTING_ENABLED, false, this.propertySpecService::integerSpec).finish(),
                UPLPropertySpecFactory.specBuilder(PK_FORCED_DELAY, false, this.propertySpecService::integerSpec).finish(),
                UPLPropertySpecFactory.specBuilder(PK_IEC1107_COMPATIBLE, false, this.propertySpecService::integerSpec).finish(),
                UPLPropertySpecFactory.specBuilder("Software7E1", false, propertySpecService::stringSpec).finish(),
                UPLPropertySpecFactory.specBuilder("DisableLogOffCommand", false, propertySpecService::stringSpec).finish(),
                UPLPropertySpecFactory.specBuilder(INSTRUMENTATION_PROFILE_MODE, false, propertySpecService::stringSpec).finish());
    }

    private PropertySpec passwordPropertySpec(boolean required) {
        return this.stringSpecOfExactLength(PASSWORD.getName(), required, 8);
    }

    @Override
    public void setUPLProperties(TypedProperties p) throws PropertyValidationException {
        try {
            if (p.getTypedProperty(ADDRESS.getName()) != null) {
                this.pAddress = p.getTypedProperty(ADDRESS.getName());
            }

            if (p.getTypedProperty(NODEID.getName()) != null) {
                this.pNodeId = p.getTypedProperty(NODEID.getName());
            }

            if (p.getTypedProperty(SERIALNUMBER.getName()) != null) {
                this.pSerialNumber = p.getTypedProperty(SERIALNUMBER.getName());
            }

            if (p.getTypedProperty(PASSWORD.getName()) != null) {
                this.pPassword = p.getTypedProperty(PASSWORD.getName());
            }

            if (p.getTypedProperty(PK_TIMEOUT) != null) {
                this.pTimeout = Integer.parseInt(p.getTypedProperty(PK_TIMEOUT));
            }

            if (p.getTypedProperty(PK_RETRIES) != null) {
                this.pRetries = Integer.parseInt(p.getTypedProperty(PK_RETRIES));
            }

            if (p.getTypedProperty(ROUNDTRIPCORRECTION.getName()) != null) {
                this.pRoundTripCorrection = Integer.parseInt(p.getTypedProperty(ROUNDTRIPCORRECTION.getName()));
            }

            if (p.getTypedProperty(CORRECTTIME.getName()) != null) {
                this.pCorrectTime = Integer.parseInt(p.getTypedProperty(CORRECTTIME.getName()));
            }

            if (p.getTypedProperty(PK_EXTENDED_LOGGING) != null) {
                this.pExtendedLogging = Integer.parseInt(p.getTypedProperty(PK_EXTENDED_LOGGING));
            }

            this.pSecurityLevel = Integer.parseInt(p.getTypedProperty(PK_SECURITYLEVEL, "3").trim());
            if (this.pSecurityLevel != 0) {
                // Password is required when security level != 0
                this.passwordPropertySpec(true).validateValue(this.pPassword);
            }

            if (p.getTypedProperty(PK_ECHO_CANCELING) != null) {
                this.pEchoCancelling = Integer.parseInt(p.getTypedProperty(PK_ECHO_CANCELING));
            }

            if (p.getTypedProperty(PK_SCRIPTING_ENABLED) != null) {
                this.scriptingEnabled = Integer.parseInt(p.getTypedProperty(PK_SCRIPTING_ENABLED, "0"));
            }
            // tricky... If scripting is enabled, we know it is an RF meter. So set the forced delay default to 0!
            if (this.scriptingEnabled > 0) {
                this.forcedDelay = 0;
            }

            if (p.getTypedProperty(PK_FORCED_DELAY) != null) {
                this.forcedDelay = Integer.parseInt(p.getTypedProperty(PK_FORCED_DELAY));
            }

            if (p.getTypedProperty(PK_IEC1107_COMPATIBLE) != null) {
                this.pIEC1107Compatible = Integer.parseInt(p.getTypedProperty(PK_IEC1107_COMPATIBLE));
            }

            this.software7E1 = !"0".equalsIgnoreCase(p.getTypedProperty("Software7E1", "0"));

            this.dontSendBreakCommand = !"0".equalsIgnoreCase(p.getTypedProperty("DisableLogOffCommand", "0"));
            this.sendBreakBeforeDisconnect = !this.dontSendBreakCommand;

            this.instrumentationProfileMode = !"0".equalsIgnoreCase(p.getTypedProperty(INSTRUMENTATION_PROFILE_MODE, "0"));
        } catch (NumberFormatException e) {
            throw new InvalidPropertyException(e, this.getClass().getSimpleName() + ": validation of properties failed before");
        }
    }

    @Override
    public List map2MeterEvent(String event) throws IOException {
        EventMapperFactory emf = new EventMapperFactory();
        return emf.getMeterEvents(event);
    }

    @Override
    public void init(InputStream inputStream, OutputStream outputStream, TimeZone timeZone, Logger logger) {
        this.timeZone = timeZone;
        this.logger = logger;
        if (logger.isLoggable(Level.INFO)) {
            String infoMsg =
                    "A230 protocol init \n"
                            + " Address = " + this.pAddress + ","
                            + " Node Id = " + this.pNodeId + ","
                            + " SerialNr = " + this.pSerialNumber + ","
                            + " Psswd = " + this.pPassword + ",\n"
                            + " Timeout = " + this.pTimeout + ","
                            + " Retries = " + this.pRetries + ","
                            + " Ext. Logging = " + this.pExtendedLogging + ","
                            + " RoundTripCorr = " + this.pRoundTripCorrection + ","
                            + " Correct Time = " + this.pCorrectTime + ","
                            + " TimeZone = " + timeZone.getID();

            logger.info(infoMsg);
        }

        try {
            this.flagConnection =
                    new FlagIEC1107Connection(
                            inputStream, outputStream, this.pTimeout, this.pRetries,
                            this.forcedDelay, this.pEchoCancelling, this.pIEC1107Compatible,
                            new CAI700(), null, this.software7E1, this.dontSendBreakCommand, logger);

        } catch (ConnectionException e) {
            logger.severe("Elster A230: init(...), " + e.getMessage());
        }
    }

    @Override
    public void connect() throws IOException {
        connect(0);
    }

    public void connect(int baudrate) throws IOException {
        try {
            getFlagIEC1107Connection().connectMAC(this.pAddress, this.pPassword, this.pSecurityLevel, this.pNodeId, baudrate);

            executeDefaultScript();
            executeRegisterScript();

            this.rFactory = new ABBA230RegisterFactory(this, this);
            this.profile = new ABBA230Profile(this, this.rFactory);

            this.sendBreakBeforeDisconnect = true;

        } catch (FlagIEC1107ConnectionException e) {
            throw new IOException(e.getMessage());
        } catch (IOException e) {
            disconnect();
            throw e;
        }

        if (this.pExtendedLogging > 0) {
            getRegistersInfo();
        }

    }

    @Override
    public void disconnect() throws NestedIOException {
        try {
            if (!this.firmwareUpgrade) {
                if (this.sendBreakBeforeDisconnect) {
                    getFlagIEC1107Connection().disconnectMAC();
                } else {
                    getFlagIEC1107Connection().disconnectMACWithoutBreak();
                }
            }
        } catch (FlagIEC1107ConnectionException e) {
            this.logger.severe("disconnect() error, " + e.getMessage());
        } catch (ConnectionException e) {
            this.logger.severe("disconnect() error while disconnection without break, " + e.getMessage());
        }
    }

    @Override
    public int getNumberOfChannels() throws IOException {
        ABBA230Register r;
        ProfileConfigRegister lpcr;

        if (instrumentationProfileMode) {
            r = this.rFactory.getInstrumentationProfileConfiguration();
            lpcr = (InstrumentationProfileConfigRegister) this.rFactory.getRegister(r);
        } else {
            r = this.rFactory.getLoadProfileConfiguration();
            lpcr = (LoadProfileConfigRegister) this.rFactory.getRegister(r);
        }

        return lpcr.getNumberRegisters();
    }

    @Override
    public ProfileData getProfileData(boolean includeEvents) throws IOException {
        return this.profile.getProfileData(includeEvents);
    }

    @Override
    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        Calendar calendar = ProtocolUtils.getCalendar(getTimeZone());
        return this.profile.getProfileData(lastReading, calendar.getTime(), includeEvents);
    }

    @Override
    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException {
        return this.profile.getProfileData(from, to, includeEvents);
    }

    @Override
    public Date getTime() throws IOException {
        return (Date) this.rFactory.getRegister("TimeDate");
    }

    @Override
    public void setTime() throws IOException {
        // if scripting enabled, clear the cache to force the IEC1107 transparant mode!
        // then get time to measure roundtrip and add roundtrip/2 to the clock to set...
        if ((getCache() != null) && (getCache() instanceof CacheMechanism) && (getScriptingEnabled() > 0)) {
            ((CacheMechanism) getCache()).setCache(null);

            long roundtrip = System.currentTimeMillis();
            roundtrip = (System.currentTimeMillis() - roundtrip) / 2;

            Calendar calendar = ProtocolUtils.getCalendar(this.timeZone);
            calendar.add(Calendar.MILLISECOND, (int) roundtrip);
            getFlagIEC1107Connection().authenticate();
            this.rFactory.setRegister("TimeDate", calendar.getTime());
        } else {
            Calendar calendar = ProtocolUtils.getCalendar(this.timeZone);
            calendar.add(Calendar.MILLISECOND, this.pRoundTripCorrection);
            getFlagIEC1107Connection().authenticate();
            this.rFactory.setRegister("TimeDate", calendar.getTime());
        }
    }

    @Override
    public String getProtocolVersion() {
        return "$Date: 2015-11-26 15:23:41 +0200 (Thu, 26 Nov 2015)$";
    }

    @Override
    public String getFirmwareVersion() throws IOException {
        String str = "unknown";
        // KV 15122003 only if pAddress is filled in
        if ((this.pAddress != null) && (this.pAddress.length() > 5)) {
            str = this.pAddress.substring(5, this.pAddress.length());
        }
        return str;
    }

    @Override
    public void release() throws IOException {
        setCache(null);
    }

    @Override
    public int getProfileInterval() throws IOException {
        return instrumentationProfileMode
                ? ((Integer) this.rFactory.getRegister("InstrumentationProfileIntegrationPeriod")).intValue()
                : ((Integer) this.rFactory.getRegister("LoadProfileIntegrationPeriod")).intValue();
    }

    @Override
    public FlagIEC1107Connection getFlagIEC1107Connection() {
        return this.flagConnection;
    }

    @Override
    public Logger getLogger() {
        return this.logger;
    }

    @Override
    public String getPassword() {
        return this.pPassword;
    }

    @Override
    public TimeZone getTimeZone() {
        return this.timeZone;
    }

    @Override
    public int getNrOfRetries() {
        return this.pRetries;
    }

    @Override
    public boolean isIEC1107Compatible() {
        return this.pIEC1107Compatible == 1;
    }

    @Override
    public boolean isRequestHeader() {
        return false;
    }

    @Override
    public void enableHHUSignOn(SerialCommunicationChannel commChannel) throws ConnectionException {
        enableHHUSignOn(commChannel, false);
    }

    @Override
    public void enableHHUSignOn(SerialCommunicationChannel commChannel, boolean datareadout) throws ConnectionException {
        HHUSignOn hhuSignOn = new IEC1107HHUConnection(commChannel, this.pTimeout, this.pRetries, 300, this.pEchoCancelling);
        hhuSignOn.setMode(HHUSignOn.MODE_PROGRAMMING);
        hhuSignOn.setProtocol(HHUSignOn.PROTOCOL_NORMAL);
        hhuSignOn.enableDataReadout(datareadout);
        getFlagIEC1107Connection().setHHUSignOn(hhuSignOn);
    }

    @Override
    public byte[] getHHUDataReadout() {
        return getFlagIEC1107Connection().getHhuSignOn().getDataReadout();
    }

    @Override
    public String getSerialNumber(DiscoverInfo discoverInfo) throws IOException {
        SerialCommunicationChannel commChannel = discoverInfo.getCommChannel();
        String nodeId = discoverInfo.getNodeId();
        int baudrate = discoverInfo.getBaudrate();
        TypedProperties properties = com.energyict.cpo.TypedProperties.empty();
        properties.setProperty("SecurityLevel", "0");
        properties.setProperty(NODEID.getName(), nodeId == null ? "" : nodeId);
        properties.setProperty("IEC1107Compatible", "1");
        setUPLProperties(properties);
        init(commChannel.getInputStream(), commChannel.getOutputStream(), null, null);
        enableHHUSignOn(commChannel);
        connect(baudrate);
        String serialNumber = getRegister("SerialNumber");
        disconnect();
        return serialNumber;
    }

    @Override
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        return this.rFactory.readRegister(obisCode);
    }

    @Override
    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        return ObisCodeMapper.getRegisterInfo(obisCode);
    }

    private static final Map<String, String> EXCEPTION_INFO_MAP = new HashMap<>();

    static {
        EXCEPTION_INFO_MAP.put("ERR1", "Invalid Command/Function type e.g. other than W1, R1 etc");
        EXCEPTION_INFO_MAP.put("ERR2", "Invalid Data Identity Number e.g. Data id does not exist in the meter");
        EXCEPTION_INFO_MAP.put("ERR3", "Invalid Packet Number");
        EXCEPTION_INFO_MAP.put("ERR5", "Data Identity is locked - pPassword timeout");
        EXCEPTION_INFO_MAP.put("ERR6", "General Comms error");
    }

    @Override
    public String getExceptionInfo(String id) {
        String exceptionInfo = EXCEPTION_INFO_MAP.get(id);
        if (exceptionInfo != null) {
            return id + ", " + exceptionInfo;
        } else {
            return "No meter specific exception info for " + id;
        }
    }

    @Override
    public Quantity getMeterReading(String name) throws UnsupportedException {
        throw new UnsupportedException("method getMeterReading( String name ) is not supported.");
    }

    @Override
    public Quantity getMeterReading(int channelID) throws UnsupportedException {
        throw new UnsupportedException("method getMeterReading( int channelID ) is not supported.");
    }

    @Override
    public byte[] getDataReadout() {
        return null;
    }

    @Override
    public Serializable getCache() {
        if (this.cacheObject != null) {
            return this.cacheObject.getCache();
        } else {
            return null;
        }
    }

    @Override
    public Serializable fetchCache(int deviceId, Connection connection) throws SQLException, ProtocolCacheFetchException {
        return null;
    }

    public void setCache(Serializable cacheObject) {
        this.cacheObject = (CacheMechanism) cacheObject;
    }

    @Override
    public void updateCache(int deviceId, Serializable cacheObject, Connection connection) throws SQLException, ProtocolCacheUpdateException {
    }

    @Override
    public ChannelMap getChannelMap() {
        return null;
    }

    @Override
    public ProtocolChannelMap getProtocolChannelMap() {
        return null;
    }

    @Override
    public void initializeDevice() throws IOException {
        throw new UnsupportedException();
    }

    @Override
    public void setRegister(String name, String value) throws IOException {
        if (name.compareTo("FIRMWAREPROGRAM") == 0) {
            try {
                blankCheck();
                File file = new File(value);
                byte[] data = new byte[(int) file.length()];
                FileInputStream fis = new FileInputStream(file);
                fis.read(data);
                fis.close();
                programFirmware(new String(data));
                activate();
                //firmwareUpgrade=true;
                try {
                    Thread.sleep(20000);
                    disconnect();
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
                connect();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        throw new UnsupportedException();
    }

    private void blankCheck() throws IOException {
        long timeout = 0;
        final int AUTHENTICATE_REARM_FIRMWARE = 60000;
        ABBA230DataIdentity di = new ABBA230DataIdentity("002", 1, 64, false, this.rFactory.getABBA230DataIdentityFactory());
        for (int set = 0; set < 64; set++) {
            int retries = 0;
            while (true) {
                try {

                    if (((System.currentTimeMillis() - timeout)) > 0) {
                        timeout = System.currentTimeMillis() + AUTHENTICATE_REARM_FIRMWARE; // arm again...
                        if (DEBUG >= 1) {
                            System.out.println("Authenticate...");
                        }
                        getFlagIEC1107Connection().authenticate();
                    }

                    if (DEBUG >= 1) {
                        System.out.println("Blankcheck set " + set);
                    }
                    byte[] data = di.read(false, 1, set);
                    if (data[0] == 0) {
                        if (DEBUG >= 1) {
                            System.out.println("Erase set " + set);
                        }
                        di.writeRawRegisterHex(set + 1, "1");

                    }
                    break;
                } catch (FlagIEC1107ConnectionException e) {
                    if (retries++ >= 1) {
                        throw e;
                    }
                    e.printStackTrace();
                } catch (IOException e) {
                    if (retries++ >= 1) {
                        throw e;
                    }
                    e.printStackTrace();
                }
            }
        }
    }

    private void programFirmware(String firmwareXMLData) throws IOException {
        FirmwareSaxParser o = new FirmwareSaxParser(this.rFactory.getABBA230DataIdentityFactory());
        o.start(firmwareXMLData, false);
    }

    private void activate() throws IOException {
        ABBA230DataIdentity di = new ABBA230DataIdentity("005", 1, 1, false, this.rFactory.getABBA230DataIdentityFactory());
        int retries = 0;
        while (true) {
            try {
                di.writeRawRegister(1, "0");
                break;
            } catch (FlagIEC1107ConnectionException e) {
                if (retries++ >= 1) {
                    throw e;
                }
                e.printStackTrace();
            } catch (IOException e) {
                if (retries++ >= 1) {
                    throw e;
                }
                e.printStackTrace();
            }
        }
    }

    private void doBillingReset() throws IOException {
        this.rFactory.setRegister("EndOfBillingPeriod", "1");
    }

    @Override
    public String getRegister(String name) throws UnsupportedException {
        throw new UnsupportedException("getRegister() is not supported");
    }

    private void getRegistersInfo() {
        StringBuilder builder = new StringBuilder();
        String obisCodeString;
        ObisCode obisCode;
        RegisterInfo obisCodeInfo;

        this.logger.info("************************* Extended Logging *************************");

        int[] billingPoint = {255, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25};

        for (int bpi = 0; bpi < billingPoint.length; bpi++) {

            try {

                TariffSources ts = (TariffSources) this.rFactory.getRegister("TariffSources");
                if (billingPoint[bpi] == 0) {
                    ts = (TariffSources) this.rFactory.getRegister("TariffSources");
                } else if ((billingPoint[bpi] > 0) && (billingPoint[bpi] <= 11)) {
                    HistoricalRegister hv = (HistoricalRegister)
                            this.rFactory.getRegister("HistoricalRegister", billingPoint[bpi]);
                    if (hv.getBillingDate() == null) {
                        continue;
                    }
                } else {
                    HistoricalRegister hv = (HistoricalRegister)
                            this.rFactory.getRegister("DailyHistoricalRegister", billingPoint[bpi]);
                    if (hv.getBillingDate() == null) {
                        continue;
                    }
                }

                builder.append("Billing point: ").append(billingPoint[bpi]).append("\n");

                if (bpi > 0) {
                    try {
                        obisCodeString = "1.1.0.1.2." + billingPoint[bpi];
                        obisCode = ObisCode.fromString(obisCodeString);
                        obisCodeInfo = ObisCodeMapper.getRegisterInfo(obisCode);
                        builder.append(" ").append(obisCodeString).append(", ").append(obisCodeInfo).append("\n");
                        if (this.pExtendedLogging == 2) {
                            builder.append(" ").append(this.rFactory.readRegister(obisCode).toString()).append("\n");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                List<String> tarifRegisters = new ArrayList<>();
                builder.append("Cumulative registers: \n");
                List<EnergyTypeCode> list = EnergyTypeCode.getEnergyTypeCodes();
                Iterator<EnergyTypeCode> it = list.iterator();
                while (it.hasNext()) {
                    EnergyTypeCode etc = it.next();
                    try {
                        obisCodeString = "1.1." + etc.getObisC() + ".8.0." + billingPoint[bpi];
                        obisCode = ObisCode.fromString(obisCodeString);
                        obisCodeInfo = ObisCodeMapper.getRegisterInfo(obisCode);
                        builder.append(" ").append(obisCodeString).append(", ").append(obisCodeInfo).append("\n");
                        if (this.pExtendedLogging == 2) {
                            builder.append(" ").append(this.rFactory.readRegister(obisCode).toString()).append("\n");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    for (int i = 0; i < ts.getRegSource().length; i++) {
                        if (ts.getRegSource()[i] == etc.getRegSource()) {
                            obisCodeString = "1.1." + etc.getObisC() + ".8." + (i + 1) + "." + billingPoint[bpi];
                            tarifRegisters.add(obisCodeString);
                        }
                    }

                }
                builder.append("\n");
                builder.append("Tou Registers: \n");
                for (String tarifRegister : tarifRegisters) {
                    try {
                        obisCodeString = tarifRegister;
                        obisCode = ObisCode.fromString(obisCodeString);
                        obisCodeInfo = ObisCodeMapper.getRegisterInfo(obisCode);
                        builder.append(" ").append(obisCodeString).append(", ").append(obisCodeInfo).append("\n");
                        if (this.pExtendedLogging == 2) {
                            builder.append(" ").append(this.rFactory.readRegister(obisCode).toString()).append("\n");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                builder.append("\n");

                builder.append("Cumulative Maximum Demand registers:\n");
                int[] md = {0, 1};
                for (int i = 0; i < md.length; i++) {
                    try {
                        CumulativeMaximumDemand cmd = (CumulativeMaximumDemand) this.rFactory.getRegister("CumulativeMaximumDemand" + i, billingPoint[bpi]);
                        int c = EnergyTypeCode.getObisCFromRegSource(cmd.getRegSource(), false);
                        obisCodeString = "1." + md[i] + "." + c + ".2.0." + billingPoint[bpi];
                        obisCode = ObisCode.fromString(obisCodeString);
                        obisCodeInfo = ObisCodeMapper.getRegisterInfo(obisCode);
                        builder.append(" ").append(obisCodeString).append(", ").append(obisCodeInfo).append("\n");
                        if (this.pExtendedLogging == 2) {
                            builder.append(" ").append(this.rFactory.readRegister(obisCode).toString()).append("\n");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                builder.append("\n");

                builder.append("Maximum demand registers:\n");

                int[] cmd = {0, 1};
                for (int i = 0; i < cmd.length; i++) {
                    try {
                        MaximumDemand mdRegister = (MaximumDemand) this.rFactory.getRegister("MaximumDemand" + i, billingPoint[bpi]);
                        int c = EnergyTypeCode.getObisCFromRegSource(mdRegister.getRegSource(), false);
                        if (mdRegister.getQuantity() == null) {
                            continue;
                        }

                        try {
                            obisCodeString = "1.1." + c + ".6.0." + billingPoint[bpi];
                            obisCode = ObisCode.fromString(obisCodeString);
                            obisCodeInfo = ObisCodeMapper.getRegisterInfo(ObisCode.fromString(obisCodeString));
                            builder.append(" ").append(obisCodeString).append(", ").append(obisCodeInfo).append("\n");
                            if (this.pExtendedLogging == 2) {
                                builder.append(" ").append(this.rFactory.readRegister(obisCode).toString()).append("\n");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                builder.append("\n");

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            builder.append("\n");
            obisCodeString = "1.1.0.4.2.255";
            obisCode = ObisCode.fromString(obisCodeString);
            obisCodeInfo = ObisCodeMapper.getRegisterInfo(ObisCode.fromString(obisCodeString));
            builder.append(" ").append(obisCodeString).append(", ").append(obisCodeInfo).append("\n");
            if (this.pExtendedLogging == 2) {
                builder.append(" ").append(this.rFactory.readRegister(obisCode).toString()).append("\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            obisCodeString = "1.1.0.4.5.255";
            obisCode = ObisCode.fromString(obisCodeString);
            obisCodeInfo = ObisCodeMapper.getRegisterInfo(ObisCode.fromString(obisCodeString));
            builder.append(" ").append(obisCodeString).append(", ").append(obisCodeInfo).append("\n");
            if (this.pExtendedLogging == 2) {
                builder.append(" ").append(this.rFactory.readRegister(obisCode).toString()).append("\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            obisCodeString = "1.0.0.0.1.255";
            obisCode = ObisCode.fromString(obisCodeString);
            obisCodeInfo = ObisCodeMapper.getRegisterInfo(ObisCode.fromString(obisCodeString));
            builder.append(" ").append(obisCodeString).append(", ").append(obisCodeInfo).append("\n");
            if (this.pExtendedLogging == 2) {
                builder.append(" ").append(this.rFactory.readRegister(obisCode).toString()).append("\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            obisCodeString = "0.0.96.50.0.255";
            obisCode = ObisCode.fromString(obisCodeString);
            obisCodeInfo = ObisCodeMapper.getRegisterInfo(ObisCode.fromString(obisCodeString));
            builder.append(" ").append(obisCodeString).append(", ").append(obisCodeInfo).append("\n");
            if (this.pExtendedLogging == 2) {
                builder.append(" ").append(this.rFactory.readRegister(obisCode).toString()).append("\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        builder.append("************************* End Extended Logging *********************");
        this.logger.info(builder.toString());

    }

    @Override
    public List getMessageCategories() {
        List<MessageCategorySpec> theCategories = new ArrayList<>();
        MessageCategorySpec cat = new MessageCategorySpec("BasicMessages");
        cat.addMessageSpec(addBasicMsg(DISCONNECT_DISPLAY, DISCONNECT, false));
        cat.addMessageSpec(addBasicMsg(ARM_DISPLAY, ARM, false));
        cat.addMessageSpec(addBasicMsg(CONNECT_DISPLAY, CONNECT, false));
        cat.addMessageSpec(addBasicMsg(TARIFFPROGRAM_DISPLAY, TARIFFPROGRAM, false));
        cat.addMessageSpec(addBasicMsg(BILLINGRESET_DISPLAY, BILLINGRESET, false));
        cat.addMessageSpec(addBasicMsg(FIRMWAREPROGRAM_DISPLAY, FIRMWAREPROGRAM, true));
        theCategories.add(cat);
        return theCategories;
    }

    private MessageSpec addBasicMsg(String keyId, String tagName, boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    @Override
    public String writeMessage(Message msg) {
        return msg.write(this);
    }

    @Override
    public String writeTag(MessageTag msgTag) {
        StringBuilder builder = new StringBuilder();

        // a. Opening tag
        builder.append("<");
        builder.append(msgTag.getName());

        // b. Attributes
        for (Iterator<MessageAttribute> it = msgTag.getAttributes().iterator(); it.hasNext(); ) {
            MessageAttribute att = it.next();
            if ((att.getValue() == null) || (att.getValue().isEmpty())) {
                continue;
            }
            builder.append(" ").append(att.getSpec().getName());
            builder.append("=").append('"').append(att.getValue()).append('"');
        }
        builder.append(">");

        // c. sub elements
        for (Iterator it = msgTag.getSubElements().iterator(); it.hasNext(); ) {
            MessageElement elt = (MessageElement) it.next();
            if (elt.isTag()) {
                builder.append(writeTag((MessageTag) elt));
            } else if (elt.isValue()) {
                String value = writeValue((MessageValue) elt);
                if ((value == null) || (value.isEmpty())) {
                    return "";
                }
                builder.append(value);
            }
        }

        // d. Closing tag
        builder.append("</");
        builder.append(msgTag.getName());
        builder.append(">");

        return builder.toString();
    }

    @Override
    public String writeValue(MessageValue msgValue) {
        return msgValue.getValue();
    }

    @Override
    public void applyMessages(List messageEntries) throws IOException {
    }

    public ABBA230RegisterFactory getRegisterFactory() {
        return rFactory;
    }

    @Override
    public MessageResult queryMessage(MessageEntry messageEntry) throws IOException {
        try {
            if (messageEntry.getContent().contains("<" + DISCONNECT)) {
                ContactorController cc = new ABBA230ContactorController(this);
                cc.doDisconnect();
            } else if (messageEntry.getContent().contains("<" + CONNECT)) {
                ContactorController cc = new ABBA230ContactorController(this);
                cc.doConnect();
            } else if (messageEntry.getContent().contains("<" + ARM)) {
                ContactorController cc = new ABBA230ContactorController(this);
                cc.doArm();
            } else if (messageEntry.getContent().contains("<" + TARIFFPROGRAM)) {
                this.logger.info("*************************** PROGRAM TARIFF *****************************");
                int start = messageEntry.getContent().indexOf(TARIFFPROGRAM) + TARIFFPROGRAM.length() + 1;
                int end = messageEntry.getContent().lastIndexOf(TARIFFPROGRAM) - 2;
                String tariffXMLData = messageEntry.getContent().substring(start, end);
                TariffSaxParser o = new TariffSaxParser(this.rFactory.getABBA230DataIdentityFactory());
                o.start(tariffXMLData, false);
            } else if (messageEntry.getContent().contains("<" + FIRMWAREPROGRAM)) {
                this.logger.info("*************************** FIRMWARE UPGRADE ***************************");
                int start = messageEntry.getContent().indexOf(FIRMWAREPROGRAM) + FIRMWAREPROGRAM.length() + 1;
                int end = messageEntry.getContent().lastIndexOf(FIRMWAREPROGRAM) - 2;
                String firmwareXMLData = messageEntry.getContent().substring(start, end);
                blankCheck();

                //	    		File file = new File("C:/Documents and Settings/kvds/My Documents/projecten/ESB/sphasefw.xml");
                //	    		byte[] data = new byte[(int)file.length()];
                //	    		FileInputStream fis = new FileInputStream(file);
                //	    		fis.read(data);
                //	    		fis.close();
                //	    		programFirmware(new String(data));

                programFirmware(firmwareXMLData);

                activate();
                this.firmwareUpgrade = true;
            } else if (messageEntry.getContent().contains("<" + BILLINGRESET)) {
                this.logger.info("************************* MD RESET *************************");
                try {
                    doBillingReset();
                } catch (Exception e) {
                    return MessageResult.createFailed(messageEntry);
                }

            }
            return MessageResult.createSuccess(messageEntry);
        } catch (IOException e) {
            return MessageResult.createFailed(messageEntry);
        }
    }

    private void executeDefaultScript() {
        if ((getCache() != null) && (getCache() instanceof CacheMechanism) && (getScriptingEnabled() == 2)) {
            ((CacheMechanism) getCache()).setCache(new String[]{"0", null});
            this.nrOfProfileBlocks = ((Integer) ((CacheMechanism) getCache()).getCache()).intValue();
        }
    }

    private void executeRegisterScript() {
        if ((getCache() != null) && (getCache() instanceof CacheMechanism) && (getScriptingEnabled() == 1)) {
            // call the scriptexecution  scriptId,script
            String script = instrumentationProfileMode
                    ? "776001(1),775001(2),879001(3),798001(10),507001(40),507002(40),508001(40),508002(40),510001(18)"
                    : "778001(1),777001(2),878001(3),798001(10),507001(40),507002(40),508001(40),508002(40),510001(18)";
            ((CacheMechanism) getCache()).setCache(new String[]{"2", script});
        }
    }

    int getScriptingEnabled() {
        return this.scriptingEnabled;
    }

    int getNrOfProfileBlocks() {
        return this.nrOfProfileBlocks;
    }

    boolean isInstrumentationProfileMode() {
        return this.instrumentationProfileMode;
    }

    private <T> PropertySpec spec(String name, boolean required, Supplier<PropertySpecBuilderWizard.NlsOptions<T>> optionsSupplier) {
        return UPLPropertySpecFactory.specBuilder(name, required, optionsSupplier).finish();
    }

    private PropertySpec stringSpecOfExactLength(String name, boolean required, int length) {
        return this.spec(name,required, () -> this.propertySpecService.stringSpecOfExactLength(length));
    }

}