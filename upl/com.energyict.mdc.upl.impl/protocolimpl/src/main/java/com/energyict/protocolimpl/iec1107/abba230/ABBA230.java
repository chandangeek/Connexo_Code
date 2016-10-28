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

import com.energyict.mdc.upl.UnsupportedException;
import com.energyict.mdc.upl.properties.InvalidPropertyException;
import com.energyict.mdc.upl.properties.MissingPropertyException;

import com.energyict.cbo.NestedIOException;
import com.energyict.cbo.Quantity;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dialer.connection.HHUSignOn;
import com.energyict.dialer.connection.IEC1107HHUConnection;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.CacheMechanism;
import com.energyict.protocol.EventMapper;
import com.energyict.protocol.HHUEnabler;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.MessageProtocol;
import com.energyict.protocol.MessageResult;
import com.energyict.protocol.MeterExceptionInfo;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterProtocol;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.SerialNumber;
import com.energyict.protocol.messaging.Message;
import com.energyict.protocol.messaging.MessageAttribute;
import com.energyict.protocol.messaging.MessageCategorySpec;
import com.energyict.protocol.messaging.MessageElement;
import com.energyict.protocol.messaging.MessageSpec;
import com.energyict.protocol.messaging.MessageTag;
import com.energyict.protocol.messaging.MessageTagSpec;
import com.energyict.protocol.messaging.MessageValue;
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

import java.io.File;
import java.io.FileInputStream;
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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author fbo
 */
/*
 *
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
        RegisterProtocol, MessageProtocol, EventMapper, SerialNumberSupport {

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
    private static final String PK_TIMEOUT = "Timeout";
    private static final String PK_RETRIES = "Retries";
    private static final String PK_FORCED_DELAY = "ForcedDelay";
    private static final String PK_SECURITY_LEVEL = "SecurityLevel";
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

    private ABBA230MeterType abba230MeterType = null;
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

    public ABBA230() {
    }

    /* ________ Impelement interface MeterProtocol ___________ */

    @Override
    public String getSerialNumber() {
        try {
            return (String) this.rFactory.getRegister("SerialNumber");
        } catch (IOException e) {
            throw ProtocolIOExceptionHandler.handle(e, getNrOfRetries() + 1);
        }
    }

    /* (non-Javadoc)
          * @see com.energyict.protocol.MeterProtocol#setProperties(java.util.Properties)
          */
    public void setProperties(Properties p) throws MissingPropertyException, InvalidPropertyException {
        try {

            Iterator iterator = getRequiredKeys().iterator();
            while (iterator.hasNext()) {
                String key = (String) iterator.next();
                if (p.getProperty(key) == null) {
                    String msg = key + " key missing";
                    throw new MissingPropertyException(msg);
                }
            }

            if (p.getProperty(com.energyict.mdc.upl.MeterProtocol.Property.ADDRESS.getName()) != null) {
                this.pAddress = p.getProperty(com.energyict.mdc.upl.MeterProtocol.Property.ADDRESS.getName());
            }

            if (p.getProperty(com.energyict.mdc.upl.MeterProtocol.Property.NODEID.getName()) != null) {
                this.pNodeId = p.getProperty(com.energyict.mdc.upl.MeterProtocol.Property.NODEID.getName());
            }

            if (p.getProperty(com.energyict.mdc.upl.MeterProtocol.Property.SERIALNUMBER.getName()) != null) {
                this.pSerialNumber = p.getProperty(com.energyict.mdc.upl.MeterProtocol.Property.SERIALNUMBER.getName());
            }

            if (p.getProperty(com.energyict.mdc.upl.MeterProtocol.Property.PASSWORD.getName()) != null) {
                this.pPassword = p.getProperty(com.energyict.mdc.upl.MeterProtocol.Property.PASSWORD.getName());
            }

            if (p.getProperty(PK_TIMEOUT) != null) {
                this.pTimeout = Integer.parseInt(p.getProperty(PK_TIMEOUT));
            }

            if (p.getProperty(PK_RETRIES) != null) {
                this.pRetries = Integer.parseInt(p.getProperty(PK_RETRIES));
            }

            if (p.getProperty(com.energyict.mdc.upl.MeterProtocol.Property.ROUNDTRIPCORR.getName()) != null) {
                this.pRoundTripCorrection = Integer.parseInt(p.getProperty(Property.ROUNDTRIPCORR.getName()));
            }

            if (p.getProperty(com.energyict.mdc.upl.MeterProtocol.Property.CORRECTTIME.getName()) != null) {
                this.pCorrectTime = Integer.parseInt(p.getProperty(com.energyict.mdc.upl.MeterProtocol.Property.CORRECTTIME.getName()));
            }

            if (p.getProperty(PK_EXTENDED_LOGGING) != null) {
                this.pExtendedLogging = Integer.parseInt(p.getProperty(PK_EXTENDED_LOGGING));
            }

            this.pSecurityLevel = Integer.parseInt(p.getProperty("SecurityLevel", "3").trim());
            if (this.pSecurityLevel != 0) {
                if ("".equals(this.pPassword)) {
                    String msg = "Password field is empty! correct first!";
                    throw new InvalidPropertyException(msg);
                }
                if (this.pPassword == null) {
                    String msg = "Password must be filled in!, correct first!";
                    throw new InvalidPropertyException(msg);
                }
                if (this.pPassword.length() != 8) {
                    String msg = "Password must have a length of 8 characters!, correct first!";
                    throw new InvalidPropertyException(msg);
                }
            }

            if (p.getProperty(PK_ECHO_CANCELING) != null) {
                this.pEchoCancelling = Integer.parseInt(p.getProperty(PK_ECHO_CANCELING));
            }

            if (p.getProperty(PK_SCRIPTING_ENABLED) != null) {
                this.scriptingEnabled = Integer.parseInt(p.getProperty(PK_SCRIPTING_ENABLED, "0"));
            }

            // tricky... If scripting is enabled, we know it is an RF meter. So set the forced delay default to 0!
            if (this.scriptingEnabled > 0) {
                this.forcedDelay = 0;
            }

            if (p.getProperty("ForcedDelay") != null) {
                this.forcedDelay = Integer.parseInt(p.getProperty(PK_FORCED_DELAY));
            }

            if (p.getProperty(PK_IEC1107_COMPATIBLE) != null) {
                this.pIEC1107Compatible = Integer.parseInt(p.getProperty(PK_IEC1107_COMPATIBLE));
            }

            this.software7E1 = !"0".equalsIgnoreCase(p.getProperty("Software7E1", "0"));

            this.dontSendBreakCommand = !"0".equalsIgnoreCase(p.getProperty("DisableLogOffCommand", "0"));
            this.sendBreakBeforeDisconnect = !this.dontSendBreakCommand;

            this.instrumentationProfileMode = !"0".equalsIgnoreCase(p.getProperty(INSTRUMENTATION_PROFILE_MODE, "0"));
        } catch (NumberFormatException e) {
            throw new InvalidPropertyException("Elster A230, validateProperties, NumberFormatException, " + e.getMessage());
        }
    }

    public List map2MeterEvent(String event) throws IOException {
        EventMapperFactory emf = new EventMapperFactory();
        return emf.getMeterEvents(event);
    }


    public List<String> getRequiredKeys() {
        return Collections.emptyList();
    }

    public List<String> getOptionalKeys() {
        return Arrays.asList(
                    "Timeout",
                    "Retries",
                    "SecurityLevel",
                    "EchoCancelling",
                    "IEC1107Compatible",
                    "ExtendedLogging",
                    "EventMapperEnabled",
                    "Software7E1",
                    "ScriptingEnabled",
                    "ForcedDelay",
                    "DisableLogOffCommand",
                    "InstrumentationProfileMode");
    }

    /* (non-Javadoc)
      * @see com.energyict.protocol.MeterProtocol
      * #init(java.io.InputStream, java.io.OutputStream, java.util.TimeZone, java.util.logging.Logger)
      */
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

    /* (non-Javadoc)
      * @see com.energyict.protocol.MeterProtocol#connect()
      */
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

    /* (non-Javadoc)
      * @see com.energyict.protocol.MeterProtocol#disconnect()
      */
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

    /* (non-Javadoc)
      * @see com.energyict.protocol.MeterProtocol#getProfileData(boolean)
      */
    public ProfileData getProfileData(boolean includeEvents) throws IOException {
        return this.profile.getProfileData(includeEvents);
    }

    /* (non-Javadoc)
      * @see com.energyict.protocol.MeterProtocol#getProfileData(java.util.Date, boolean)
      */
    public ProfileData getProfileData(Date lastReading, boolean includeEvents)
            throws IOException {
        Calendar calendar = ProtocolUtils.getCalendar(getTimeZone());
        return this.profile.getProfileData(lastReading, calendar.getTime(), includeEvents);
    }

    /* (non-Javadoc)
      * @see com.energyict.protocol.MeterProtocol#getProfileData(java.util.Date, java.util.Date, boolean)
      */
    public ProfileData getProfileData(Date from, Date to, boolean includeEvents)
            throws IOException {
        return this.profile.getProfileData(from, to, includeEvents);
    }

    /* (non-Javadoc)
      * @see com.energyict.protocol.MeterProtocol#getTime()
      */
    public Date getTime() throws IOException {
        return (Date) this.rFactory.getRegister("TimeDate");
    }

    /* (non-Javadoc)
      * @see com.energyict.protocol.MeterProtocol#setTime()
      */
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

    public String getProtocolVersion() {
        return "$Date: 2015-11-26 15:23:41 +0200 (Thu, 26 Nov 2015)$";
    }

    /* (non-Javadoc)
      * @see com.energyict.protocol.MeterProtocol#getFirmwareVersion()
      */
    public String getFirmwareVersion() throws IOException {
        String str = "unknown";
        // KV 15122003 only if pAddress is filled in
        if ((this.pAddress != null) && (this.pAddress.length() > 5)) {
            str = this.pAddress.substring(5, this.pAddress.length());
        }
        return str;
    }


    /* (non-Javadoc)
      * @see com.energyict.protocol.MeterProtocol#release()
      */
    public void release() throws IOException {

        /* In case we use the caching for some extra functionality, clean it up! */
        setCache(null);
    }


    /* ________ Impelement interface ProtocolLink ___________ */

    /* (non-Javadoc)
      * @see com.energyict.protocolimpl.iec1107.ProtocolLink#getProfileInterval()
      */
    public int getProfileInterval() throws IOException {
        return instrumentationProfileMode
                ? ((Integer) this.rFactory.getRegister("InstrumentationProfileIntegrationPeriod")).intValue()
                : ((Integer) this.rFactory.getRegister("LoadProfileIntegrationPeriod")).intValue();
    }

    /* (non-Javadoc)
      * @see com.energyict.protocolimpl.iec1107.ProtocolLink#getFlagIEC1107Connection()
      */
    public FlagIEC1107Connection getFlagIEC1107Connection() {
        return this.flagConnection;
    }

    /* (non-Javadoc)
      * @see com.energyict.protocolimpl.iec1107.ProtocolLink#getLogger()
      */
    public Logger getLogger() {
        return this.logger;
    }

    /* (non-Javadoc)
      * @see com.energyict.protocolimpl.iec1107.ProtocolLink#getPassword()
      */
    public String getPassword() {
        return this.pPassword;
    }

    /* (non-Javadoc)
      * @see com.energyict.protocolimpl.iec1107.ProtocolLink#getTimeZone()
      */
    public TimeZone getTimeZone() {
        return this.timeZone;
    }

    /* (non-Javadoc)
      * @see com.energyict.protocolimpl.iec1107.ProtocolLink#getNrOfRetries()
      */
    public int getNrOfRetries() {
        return this.pRetries;
    }

    /* (non-Javadoc)
      * @see com.energyict.protocolimpl.iec1107.ProtocolLink#isIEC1107Compatible()
      */
    public boolean isIEC1107Compatible() {
        return this.pIEC1107Compatible == 1;
    }

    /* (non-Javadoc)
      * @see com.energyict.protocolimpl.iec1107.ProtocolLink#isRequestHeader()
      */
    public boolean isRequestHeader() {
        return false;
    }

    /* ________ Implement interface HHUEnabler ___________ */

    /* (non-Javadoc)
      * @see com.energyict.protocol.HHUEnabler#enableHHUSignOn(com.energyict.dialer.core.SerialCommunicationChannel)
      */
    public void enableHHUSignOn(SerialCommunicationChannel commChannel) throws ConnectionException {
        enableHHUSignOn(commChannel, false);
    }

    /* (non-Javadoc)
      * @see com.energyict.protocol.HHUEnabler#enableHHUSignOn(com.energyict.dialer.core.SerialCommunicationChannel, boolean)
      */
    public void enableHHUSignOn(SerialCommunicationChannel commChannel, boolean datareadout) throws ConnectionException {
        HHUSignOn hhuSignOn =
                new IEC1107HHUConnection(commChannel, this.pTimeout, this.pRetries, 300, this.pEchoCancelling);
        hhuSignOn.setMode(HHUSignOn.MODE_PROGRAMMING);
        hhuSignOn.setProtocol(HHUSignOn.PROTOCOL_NORMAL);
        hhuSignOn.enableDataReadout(datareadout);
        getFlagIEC1107Connection().setHHUSignOn(hhuSignOn);
    }

    /* (non-Javadoc)
      * @see com.energyict.protocol.HHUEnabler#getHHUDataReadout()
      */
    public byte[] getHHUDataReadout() {
        return getFlagIEC1107Connection().getHhuSignOn().getDataReadout();
    }


    /* ________ Impelement interface SerialNumber ___________ */

    /* (non-Javadoc)
      * @see com.energyict.protocol.SerialNumber#
      * getSerialNumber(com.energyict.protocol.meteridentification.DiscoverInfo)
      */
    public String getSerialNumber(DiscoverInfo discoverInfo) throws IOException {
        SerialCommunicationChannel commChannel = discoverInfo.getCommChannel();
        String nodeId = discoverInfo.getNodeId();
        int baudrate = discoverInfo.getBaudrate();
        Properties properties = new Properties();
        properties.setProperty("SecurityLevel", "0");
        properties.setProperty(com.energyict.mdc.upl.MeterProtocol.Property.NODEID.getName(), nodeId == null ? "" : nodeId);
        properties.setProperty("IEC1107Compatible", "1");
        setProperties(properties);
        init(commChannel.getInputStream(), commChannel.getOutputStream(), null, null);
        enableHHUSignOn(commChannel);
        connect(baudrate);
        String serialNumber = getRegister("SerialNumber");
        disconnect();
        return serialNumber;
    }

    /* ________ Impelement interface MeterProtocol ___________ */

    /* (non-Javadoc)
      * @see com.energyict.protocol.RegisterProtocol#readRegister(com.energyict.obis.ObisCode)
      */
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        return this.rFactory.readRegister(obisCode);
    }

    /* (non-Javadoc)
      * @see com.energyict.protocol.RegisterProtocol#translateRegister(com.energyict.obis.ObisCode)
      */
    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        return ObisCodeMapper.getRegisterInfo(obisCode);
    }

    /* ________ Impelement interface MeterExceptionInfo ___________ */

    /* (non-Javadoc)
      * @see com.energyict.protocol.MeterExceptionInfo#getExceptionInfo(java.lang.String)
      */
    public String getExceptionInfo(String id) {
        String exceptionInfo = EXCEPTION_INFO_MAP.get(id);
        if (exceptionInfo != null) {
            return id + ", " + exceptionInfo;
        } else {
            return "No meter specific exception info for " + id;
        }
    }


    private static final Map<String, String> EXCEPTION_INFO_MAP = new HashMap<>();

    static {
        EXCEPTION_INFO_MAP.put("ERR1", "Invalid Command/Function type e.g. other than W1, R1 etc");
        EXCEPTION_INFO_MAP.put("ERR2", "Invalid Data Identity Number e.g. Data id does not exist in the meter");
        EXCEPTION_INFO_MAP.put("ERR3", "Invalid Packet Number");
        EXCEPTION_INFO_MAP.put("ERR5", "Data Identity is locked - pPassword timeout");
        EXCEPTION_INFO_MAP.put("ERR6", "General Comms error");
    }

    public ABBA230MeterType getAbba230MeterType() {
        return this.abba230MeterType;
    }

    /* ________ Not supported methods ___________ */

    /* method not supported
      * @see com.energyict.protocol.MeterProtocol#getMeterReading(java.lang.String)
      */
    public Quantity getMeterReading(String name) throws IOException {
        String msg = "method getMeterReading( String name ) is not supported.";
        throw new UnsupportedException(msg);
    }

    /* method not supported
      * @see com.energyict.protocol.MeterProtocol#getMeterReading(int)
      */
    public Quantity getMeterReading(int channelID) throws IOException {
        String msg = "method getMeterReading( int channelID ) is not supported.";
        throw new UnsupportedException(msg);
    }

    /* method not supported
      * @see com.energyict.protocolimpl.iec1107.ProtocolLink#getDataReadout()
      */
    public byte[] getDataReadout() {
        return null;
    }

    /* method not supported
      * @see com.energyict.protocol.MeterProtocol#getCache()
      */
    public Object getCache() {
        return this.cacheObject;
    }

    /* method not supported
      * @see com.energyict.protocol.MeterProtocol#fetchCache(int)
      */
    public Object fetchCache(int rtuid)
            throws java.sql.SQLException, com.energyict.cbo.BusinessException {
        return null;
    }

    /* method not supported
      * @see com.energyict.protocol.MeterProtocol#setCache(java.lang.Object)
      */
    public void setCache(Object cacheObject) {
        this.cacheObject = (CacheMechanism) cacheObject;
    }

    /* method not supported
      * @see com.energyict.protocol.MeterProtocol#updateCache(int, java.lang.Object)
      */
    public void updateCache(int rtuid, Object cacheObject)
            throws java.sql.SQLException, com.energyict.cbo.BusinessException {
    }

    /* method not supported
      * @see com.energyict.protocolimpl.iec1107.ProtocolLink#getChannelMap()
      */
    public ChannelMap getChannelMap() {
        return null;
    }

    /* method not supported
      * @see com.energyict.protocolimpl.iec1107.ProtocolLink#getProtocolChannelMap()
      */
    public ProtocolChannelMap getProtocolChannelMap() {
        return null;
    }

    /* method not supported
      * @see com.energyict.protocol.MeterProtocol#initializeDevice()
      */
    public void initializeDevice() throws IOException {
        throw new UnsupportedException();
    }

    /* (non-Javadoc)
      * @see com.energyict.protocol.MeterProtocol#setRegister(java.lang.String, java.lang.String)
      */
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

    /* (non-Javadoc)
      * @see com.energyict.protocol.MeterProtocol#getRegister(java.lang.String)
      */
    public String getRegister(String name) throws IOException {

        //rFactory.getRegister("BlankCheck");

        //byte[] data = rFactory.getABBA230DataIdentityFactory().getDataIdentity("002", false, 1, 0);

        //    	ABBA230DataIdentity di = new ABBA230DataIdentity("002", 1, 1, false, rFactory.getABBA230DataIdentityFactory());
        //    	byte[] data = di.read(false,1,0);

        /*
          long val = ((Long)rFactory.getRegister("ContactorStatus")).longValue();
          System.out.println("status -> "+val);

          if (val == 0) {
              rFactory.setRegister("ContactorStatus",new byte[]{1});
              try { Thread.sleep(5000); } catch(InterruptedException e) {}
              System.out.println("should read 1, open contactor "+((Long)rFactory.getRegister("ContactorStatus")).longValue());
          }
          else {
              rFactory.setRegister("ContactorCloser",new byte[]{0});
              try { Thread.sleep(5000); } catch(InterruptedException e) {}
              System.out.println("should read 0, open contactor "+((Long)rFactory.getRegister("ContactorStatus")).longValue());
          }

          rFactory.setRegister("ContactorStatus",new byte[]{0});
          rFactory.setRegister("ContactorCloser",new byte[]{0});
          try { Thread.sleep(5000); } catch(InterruptedException e) {}
          System.out.println("should read 0, closed contactor "+((Long)rFactory.getRegister("ContactorStatus")).longValue());

          rFactory.setRegister("ContactorStatus",new byte[]{1});
          try { Thread.sleep(5000); } catch(InterruptedException e) {}
          System.out.println("should read 1, open contactor "+((Long)rFactory.getRegister("ContactorStatus")).longValue());

          rFactory.setRegister("ContactorStatus",new byte[]{0});
          rFactory.setRegister("ContactorCloser",new byte[]{0});
          try { Thread.sleep(5000); } catch(InterruptedException e) {}
          System.out.println("should read 0, closed contactor "+((Long)rFactory.getRegister("ContactorStatus")).longValue());

          return "";
           */
        throw new UnsupportedException("getRegister() is not supported");
    }


    /* ________ Get Register Info, extended logging ___________ */

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
                List tarifRegisters = new ArrayList();

                //            if (billingPoint[bpi] == 255) {
                //                ts = (TariffSources)rFactory.getRegister("TariffSources");
                //            } else {
                //
                //            	if ((billingPoint[bpi]>=0) && (billingPoint[bpi]<=11)) {
                //	                HistoricalRegister hv = (HistoricalRegister)rFactory.getRegister( "HistoricalRegister", billingPoint[bpi] );
                //	                if( hv.getBillingDate() == null ) continue;
                //	                ts = hv.getTariffSources();
                //            	}
                //            	else if ((billingPoint[bpi]>=12) && (billingPoint[bpi]<=25)) {
                //	                HistoricalRegister hv = (HistoricalRegister)rFactory.getRegister( "DailyHistoricalRegister", billingPoint[bpi] );
                //	                if( hv.getBillingDate() == null ) continue;
                //	                ts = hv.getTariffSources();
                //            	}
                //
                //            }

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

                builder.append("Cumulative registers: \n");
                List list = EnergyTypeCode.getEnergyTypeCodes();
                Iterator it = list.iterator();
                while (it.hasNext()) {
                    EnergyTypeCode etc = (EnergyTypeCode) it.next();

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
                it = tarifRegisters.iterator();
                while (it.hasNext()) {
                    try {
                        obisCodeString = (String) it.next();
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

    public List getMessageCategories() {
        List theCategories = new ArrayList();
        MessageCategorySpec cat = new MessageCategorySpec("BasicMessages");

        MessageSpec msgSpec = addBasicMsg(DISCONNECT_DISPLAY, DISCONNECT, false);
        cat.addMessageSpec(msgSpec);

        msgSpec = addBasicMsg(ARM_DISPLAY, ARM, false);
        cat.addMessageSpec(msgSpec);

        msgSpec = addBasicMsg(CONNECT_DISPLAY, CONNECT, false);
        cat.addMessageSpec(msgSpec);

        msgSpec = addBasicMsg(TARIFFPROGRAM_DISPLAY, TARIFFPROGRAM, false);
        cat.addMessageSpec(msgSpec);

        msgSpec = addBasicMsg(BILLINGRESET_DISPLAY, BILLINGRESET, false);
        cat.addMessageSpec(msgSpec);

        msgSpec = addBasicMsg(FIRMWAREPROGRAM_DISPLAY, FIRMWAREPROGRAM, true);
        cat.addMessageSpec(msgSpec);

        theCategories.add(cat);
        return theCategories;
    }

    private MessageSpec addBasicMsg(String keyId, String tagName, boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    public String writeMessage(Message msg) {
        return msg.write(this);
    }

    public String writeTag(MessageTag msgTag) {
        StringBuilder builder = new StringBuilder();

        // a. Opening tag
        builder.append("<");
        builder.append(msgTag.getName());

        // b. Attributes
        for (Iterator it = msgTag.getAttributes().iterator(); it.hasNext(); ) {
            MessageAttribute att = (MessageAttribute) it.next();
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

    public String writeTag2(MessageTag msgTag) {
        StringBuilder builder = new StringBuilder();

        // a. Opening tag
        builder.append("<");
        builder.append(msgTag.getName());

        // b. Attributes
        for (Iterator it = msgTag.getAttributes().iterator(); it.hasNext(); ) {
            MessageAttribute att = (MessageAttribute) it.next();
            if ((att.getValue() == null) || (att.getValue().isEmpty())) {
                continue;
            }
            builder.append(" ").append(att.getSpec().getName());
            builder.append("=").append('"').append(att.getValue()).append('"');
        }
        if (msgTag.getSubElements().isEmpty()) {
            builder.append("/>");
            return builder.toString();
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

    public String writeValue(MessageValue msgValue) {
        return msgValue.getValue();
    }

    public void applyMessages(List messageEntries) throws IOException {

    }

    public ABBA230RegisterFactory getRegisterFactory() {
        return rFactory;
    }

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
                int start = messageEntry.getContent().indexOf(BILLINGRESET) + BILLINGRESET.length() + 1;
                int end = messageEntry.getContent().lastIndexOf(BILLINGRESET) - 2;
                String mdresetXMLData = messageEntry.getContent().substring(start, end);

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

    public int getScriptingEnabled() {
        return this.scriptingEnabled;
    }

    public int getNrOfProfileBlocks() {
        return this.nrOfProfileBlocks;
    }

    /**
     * Getter for boolean InstrumentationProfile
     *
     * @return true if the Instrumentation Profile data should be read out, instead of the normal Load Profile data
     */
    public boolean isInstrumentationProfileMode() {
        return this.instrumentationProfileMode;
    }

}