package com.energyict.protocolimpl.iec1107.abba1140;

import com.energyict.cbo.Quantity;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dialer.connection.HHUSignOn;
import com.energyict.dialer.connection.IEC1107HHUConnection;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.mdc.upl.UnsupportedException;
import com.energyict.mdc.upl.io.NestedIOException;
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
import com.energyict.mdc.upl.properties.MissingPropertyException;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecBuilderWizard;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.TypedProperties;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.HHUEnabler;
import com.energyict.protocol.MessageProtocol;
import com.energyict.protocol.MessageResult;
import com.energyict.protocol.MeterExceptionInfo;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterProtocol;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.SerialNumber;
import com.energyict.protocol.exceptions.ConnectionCommunicationException;
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
import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
import static com.energyict.mdc.upl.MeterProtocol.Property.RETRIES;
import static com.energyict.mdc.upl.MeterProtocol.Property.ROUNDTRIPCORRECTION;
import static com.energyict.mdc.upl.MeterProtocol.Property.SECURITYLEVEL;
import static com.energyict.mdc.upl.MeterProtocol.Property.SERIALNUMBER;
import static com.energyict.mdc.upl.MeterProtocol.Property.TIMEOUT;

/**
 * ABBA1140.java
 * <p/>
 * <B>Description :</B><BR>
 * Class that implements the Elster A1140 meter protocol. <BR>
 * <B>@beginchanges</B><BR>
 * <p/>
 * <PRE>
 * FBO 02022006	Initial version
 * FBO 29052006	Fix profile data: data was fetched as energy values, but the meter
 * stores power/demand values.
 * FBO 30052006	Fix profile data: When a time set occurs and the time meter is set
 * back for more then one interval period, there will be double entries in the
 * profile data.  These entries will not get a SL flag from the meter.  Since these
 * entries occur twice or more they need an SL flag.
 * JME 26032009	Made extended logging more robust for meter configuration
 * Added support for new firmware by adding the following features:
 * * Added new registers:
 * - Serial number (0.0.96.1.0.255)
 * - Device type and version (0.0.96.51.0.255)
 * - Daily historical registers: Added for obisCode field F from 24 to 37
 * - Historical registers: Increased from 15 to 24 billing points (obis field F from 0 to 23)
 * - Time of billing point registers: Increased from 0-14 to 0-37 and added billing trigger source as registertext
 * * Implemented message protocol with the following messages:
 * - Billing reset message
 * JME 10042009	Merged fix from ABBA230 to ABBA1140 profile, to adjust GMT profile timestamps to correct timezone.
 * JME 15042009	Mapped firmware version and device type to obiscode 1.1.0.2.0.255
 * JME 20042009	Added exact fw revision number to getFirmware method. (Mantis #4342)
 * Changed EiEventCode from other to more specific eventcode. (Mantis #4379)
 * JME 22052009	Added eventlogs: Terminal cover, Main cover, Phase failure, Reverse run, power failure, Transient reset,
 * Internal battery, Billing event and Meter error.
 * JME 26052009	Added new property DelayBeforeConnect because some modems block the first bytes of communication. This delay
 * resolves the problem.
 * JME 22102009	Added quick fix for ImServe to remove the properties with a value of "offline.EMPTY"
 * This behavior is temporary, it will move later on to the CommServerJOffline code
 * JME 02112009 Removed previous quick fix and moved this feature to the CommServerJOffline code
 * </PRE>
 *
 * @author fbo
 */
public class ABBA1140 extends PluggableMeterProtocol implements ProtocolLink, HHUEnabler, SerialNumber, MeterExceptionInfo, RegisterProtocol, MessageProtocol, SerialNumberSupport {

    private static final long FORCE_DELAY = 300;

    /**
     * Property keys specific for A140 protocol.
     */
    private static final String PK_TIMEOUT = TIMEOUT.getName();
    private static final String PK_RETRIES = RETRIES.getName();
    private static final String PK_SECURITY_LEVEL = SECURITYLEVEL.getName();
    private static final String PK_EXTENDED_LOGGING = "ExtendedLogging";
    private static final String PK_IEC1107_COMPATIBLE = "IEC1107Compatible";
    private static final String PK_ECHO_CANCELING = "EchoCancelling";
    private static final String PK_DELAY_BEFORE_CONNECT = "DelayBeforeConnect";
    /**
     * Property Default values
     */
    private static final String PD_NODE_ID = "";
    private static final int PD_TIMEOUT = 10000;
    private static final int PD_RETRIES = 5;
    private static final int PD_ROUNDTRIP_CORRECTION = 0;
    private static final int PD_SECURITY_LEVEL = 2;
    private static final int PD_EXTENDED_LOGGING = 0;
    private static final int PD_IEC1107_COMPATIBLE = 0;
    private static final int PD_ECHO_CANCELING = 0;
    private static final String BILLINGRESET = RtuMessageConstant.BILLINGRESET;
    private static final String BILLINGRESET_DISPLAY = "Billing reset";
    private static Map<String, String> exceptionInfoMap = new HashMap<>();

    static {
        exceptionInfoMap.put("ERR1", "Invalid Command/Function type e.g. other than W1, R1 etc");
        exceptionInfoMap.put("ERR2", "Invalid Data Identity Number e.g. Data id does not exist in the meter");
        exceptionInfoMap.put("ERR3", "Invalid Packet Number");
        exceptionInfoMap.put("ERR5", "Data Identity is locked - pPassword timeout");
        exceptionInfoMap.put("ERR6", "General Comms error");
    }

    private final PropertySpecService propertySpecService;

    private boolean extendedProfileStatus;
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
    private int pRoundTripCorrection = PD_ROUNDTRIP_CORRECTION;
    private int pSecurityLevel = PD_SECURITY_LEVEL;
    private int pCorrectTime = 0;
    private int pExtendedLogging = PD_EXTENDED_LOGGING;
    private int pEchoCancelling = PD_ECHO_CANCELING;
    private int pIEC1107Compatible = PD_IEC1107_COMPATIBLE;
    private TimeZone timeZone;
    private Logger logger;
    private FlagIEC1107Connection flagConnection = null;
    private ABBA1140RegisterFactory rFactory = null;
    private ABBA1140Profile profile = null;
    private MeterType meterType = null;
    private boolean software7E1;
    private int pDelayBeforeConnect = 0;
    /**
     * Indication whether to send a break command before a retry
     */
    private boolean dontSendBreakCommand;
    /**
     * Indicate whether to send a break before the disconnect. The state is dependant on the other break parameter ({@link #dontSendBreakCommand})
     * Mostly you will send it, just not if the signon failed.
     */
    private boolean sendBreakBeforeDisconnect;

    private boolean useSelectiveAccessByFromAndToDate = true;

    public ABBA1140(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    /**
     * Returns the serial number
     *
     * @return String serial number
     */
    @Override
    public String getSerialNumber() {
        try {
            return (String)getRegisterFactory().getRegister("SerialNumber");
        }catch (IOException e) {
            throw ProtocolIOExceptionHandler.handle(e, getNrOfRetries() + 1);
        }
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        return Arrays.asList(
                this.stringSpec(ADDRESS.getName()),
                this.stringSpec(NODEID.getName()),
                this.stringSpec(SERIALNUMBER.getName()),
                this.stringSpec(PASSWORD.getName()),
                this.integerSpec(PK_TIMEOUT),
                this.integerSpec(PK_RETRIES),
                this.integerSpec(ROUNDTRIPCORRECTION.getName()),
                this.integerSpec(CORRECTTIME.getName()),
                this.integerSpec(PK_EXTENDED_LOGGING),
                this.integerSpec(PK_DELAY_BEFORE_CONNECT),
                this.integerSpec(PK_SECURITY_LEVEL),
                this.integerSpec(PK_ECHO_CANCELING),
                this.integerSpec(PK_IEC1107_COMPATIBLE),
                this.stringSpec("Software7E1"),
                this.stringSpec("DisableLogOffCommand"),
                this.stringSpec("ExtendedProfileStatus"),
                this.stringSpec("UseSelectiveAccessByFromAndToDate"));
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
    public void setUPLProperties(TypedProperties p) throws MissingPropertyException, InvalidPropertyException {
        try {
            if (p.getTypedProperty(ADDRESS.getName()) != null) {
                pAddress = p.getTypedProperty(ADDRESS.getName());
            }

            if (p.getTypedProperty(NODEID.getName()) != null) {
                pNodeId = p.getTypedProperty(NODEID.getName());
            }

            if (p.getTypedProperty(SERIALNUMBER.getName()) != null) {
                pSerialNumber = p.getTypedProperty(SERIALNUMBER.getName());
            }

            if (p.getTypedProperty(PASSWORD.getName()) != null) {
                pPassword = p.getTypedProperty(PASSWORD.getName());
            }

            if (p.getTypedProperty(PK_TIMEOUT) != null) {
                pTimeout = Integer.parseInt(p.getTypedProperty(PK_TIMEOUT));
            }

            if (p.getTypedProperty(PK_RETRIES) != null) {
                pRetries = Integer.parseInt(p.getTypedProperty(PK_RETRIES));
            }

            if (p.getTypedProperty(ROUNDTRIPCORRECTION.getName()) != null) {
                pRoundTripCorrection = Integer.parseInt(p.getTypedProperty(ROUNDTRIPCORRECTION.getName()));
            }

            if (p.getTypedProperty(CORRECTTIME.getName()) != null) {
                pCorrectTime = Integer.parseInt(p.getTypedProperty(CORRECTTIME.getName()));
            }

            if (p.getTypedProperty(PK_EXTENDED_LOGGING) != null) {
                pExtendedLogging = Integer.parseInt(p.getTypedProperty(PK_EXTENDED_LOGGING));
            }

            if (p.getTypedProperty(PK_DELAY_BEFORE_CONNECT) != null) {
                pDelayBeforeConnect = Integer.parseInt(p.getTypedProperty(PK_DELAY_BEFORE_CONNECT));
            }

            pSecurityLevel = Integer.parseInt(p.getTypedProperty(PK_SECURITY_LEVEL, "2").trim());
            if (pSecurityLevel != 0) {
                if (pPassword == null || pPassword.isEmpty()) {
                    throw InvalidPropertyException.forNameAndValue(PASSWORD.getName(), null);
                }
                if (pPassword.length() != 8) {
                    throw new InvalidPropertyException("Password must have a length of 8 characters!, correct first!");
                }
            }

            if (p.getTypedProperty(PK_ECHO_CANCELING) != null) {
                pEchoCancelling = Integer.parseInt(p.getTypedProperty(PK_ECHO_CANCELING));
            }

            if (p.getTypedProperty(PK_IEC1107_COMPATIBLE) != null) {
                pIEC1107Compatible = Integer.parseInt(p.getTypedProperty(PK_IEC1107_COMPATIBLE));
            }

            this.software7E1 = !"0".equalsIgnoreCase(p.getTypedProperty("Software7E1", "0"));

            this.dontSendBreakCommand = !"0".equalsIgnoreCase(p.getTypedProperty("DisableLogOffCommand", "0"));
            this.sendBreakBeforeDisconnect = !this.dontSendBreakCommand;

            this.extendedProfileStatus = !"0".equalsIgnoreCase(p.getTypedProperty("ExtendedProfileStatus", "0"));

            this.useSelectiveAccessByFromAndToDate = "1".equals(p.getTypedProperty("UseSelectiveAccessByFromAndToDate", "1"));

        } catch (NumberFormatException e) {
            throw new InvalidPropertyException(e, "ABBA1140: validation of properties failed before");
        }

    }

    boolean isUseSelectiveAccessByFromAndToDate() {
        return useSelectiveAccessByFromAndToDate;
    }

    @Override
    public void init(InputStream inputStream, OutputStream outputStream, TimeZone timeZone, Logger logger) {
        this.timeZone = timeZone;
        this.logger = logger;

        if (logger.isLoggable(Level.INFO)) {
            String infoMsg =
                    "A1140 protocol init \n"
                            + " Address = " + pAddress + ","
                            + " Node Id = " + pNodeId + ","
                            + " SerialNr = " + pSerialNumber + ","
                            + " Psswd = " + pPassword + ","
                            + " Timeout = " + pTimeout + ","
                            + " Retries = " + pRetries + ","
                            + " Ext. Logging = " + pExtendedLogging + ","
                            + " RoundTripCorr = " + pRoundTripCorrection + ","
                            + " Correct Time = " + pCorrectTime + ","
                            + " TimeZone = " + timeZone.getID();

            logger.info(infoMsg);
        }

        try {
            flagConnection =
                    new FlagIEC1107Connection(
                            inputStream, outputStream, pTimeout, pRetries,
                            FORCE_DELAY, pEchoCancelling, pIEC1107Compatible,
                            new CAI700(), null, software7E1, dontSendBreakCommand, logger);
        } catch (ConnectionException e) {
            logger.severe("ABBA1140: init(...), " + e.getMessage());
        }
    }

    @Override
    public void connect() throws IOException {
        connect(0);
    }

    private void connect(int baudrate) throws IOException {
        // Configurable delay to prevent some modems to block first communication bytes.
        try {
            Thread.sleep(pDelayBeforeConnect);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw ConnectionCommunicationException.communicationInterruptedException(e);
        }

        try {
            this.meterType = getFlagIEC1107Connection().connectMAC(pAddress, pPassword, pSecurityLevel, pNodeId, baudrate);
			getRegisterFactory().setABBA1140(this);
			setProfile(new ABBA1140Profile(this, getRegisterFactory()));

            this.sendBreakBeforeDisconnect = true;

        } catch (FlagIEC1107ConnectionException e) {
            throw new IOException(e.getMessage());
        } catch (IOException e) {
            disconnect();
            throw e;
        }
        if (pExtendedLogging > 0) {
            getRegistersInfo();
        }

    }

    protected ABBA1140RegisterFactory getRegisterFactory() {
        if (this.rFactory == null){
            this.rFactory = new ABBA1140RegisterFactory(this,this);
        }
        return this.rFactory;
    }

    @Override
    public void disconnect() throws NestedIOException {
        try {
            if (this.sendBreakBeforeDisconnect) {
                getFlagIEC1107Connection().disconnectMAC();
            } else {
                getFlagIEC1107Connection().disconnectMACWithoutBreak();
            }

            // all exceptions are eaten because the communication needs to be terminated
        } catch (FlagIEC1107ConnectionException e) {
            logger.severe("disconnect() error, " + e.getMessage());
        } catch (ConnectionException e) {
            logger.severe("disconnect() error while disconnection without break, " + e.getMessage());
        }
    }

    @Override
    public int getNumberOfChannels() throws IOException {
        ABBA1140Register r = getRegisterFactory().getLoadProfileConfiguration();
		LoadProfileConfigRegister lpcr = (LoadProfileConfigRegister) getRegisterFactory().getRegister(r);
        return lpcr.getNumberRegisters();
    }

    @Override
    public ProfileData getProfileData(boolean includeEvents) throws IOException {
		return getProfile().getProfileData(includeEvents);
    }

    @Override
    public ProfileData getProfileData(Date lastReading, boolean includeEvents)
            throws IOException {
        Calendar calendar = ProtocolUtils.getCalendar(getTimeZone());
		return getProfile().getProfileData(lastReading, calendar.getTime(), includeEvents);
    }

    @Override
    public ProfileData getProfileData(Date from, Date to, boolean includeEvents)
            throws IOException {
        return getProfile().getProfileData(from, to, includeEvents);
    }

    @Override
    public Date getTime() throws IOException {
		return (Date) getRegisterFactory().getRegister("TimeDate");
    }

    @Override
    public void setTime() throws IOException {
        Calendar calendar = ProtocolUtils.getCalendar(timeZone);
        calendar.add(Calendar.MILLISECOND, pRoundTripCorrection);
        getFlagIEC1107Connection().authenticate();
		getRegisterFactory().setRegister("TimeDate", calendar.getTime());
    }

    @Override
    public String getProtocolVersion() {
        return "$Date: 2016-05-18 13:34:03 +0200 (Wed, 18 May 2016)$";
    }

    @Override
    public String getFirmwareVersion() throws IOException {
        String str = "";
        try {
            ABBA1140MeterTypeParser mtp = new ABBA1140MeterTypeParser(getMeterType());
            str = mtp.toString() + " ";
        } catch (Exception e) {
        }

        try {
            str += (String) getRegisterFactory().getRegister("FirmwareVersion");
        } catch (Exception e) {
        }

        // KV 15122003 only if pAddress is filled in
        if ((pAddress != null) && (pAddress.length() > 5)) {
            str += " " + pAddress.substring(5, pAddress.length());
        }
        return str;
    }


    @Override
    public void setRegister(String name, String value) throws IOException {
        getRegisterFactory().setRegister(name, value);
    }

    @Override
    public void release() throws IOException {
    }

    @Override
    public int getProfileInterval() throws IOException {
        return ((Integer) getRegisterFactory().getRegister("IntegrationPeriod")).intValue();
    }

    @Override
    public FlagIEC1107Connection getFlagIEC1107Connection() {
        return flagConnection;
    }

    @Override
    public Logger getLogger() {
        return logger;
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
    public boolean isIEC1107Compatible() {
        return pIEC1107Compatible == 1;
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
        HHUSignOn hhuSignOn =
                new IEC1107HHUConnection(commChannel, pTimeout, pRetries, 300, pEchoCancelling);
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
        TypedProperties properties = com.energyict.protocolimpl.properties.TypedProperties.empty();
        properties.setProperty("SecurityLevel", "0");
        properties.setProperty(com.energyict.mdc.upl.MeterProtocol.Property.NODEID.getName(), nodeId == null ? "" : nodeId);
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
        return getRegisterFactory().readRegister(obisCode);
    }

    @Override
    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        return ObisCodeMapper.getRegisterInfo(obisCode);
    }

    @Override
    public String getExceptionInfo(String id) {
        String exceptionInfo = exceptionInfoMap.get(id);
        if (exceptionInfo != null) {
            return id + ", " + exceptionInfo;
        } else {
            return "No meter specific exception info for " + id;
        }
    }

    private MeterType getMeterType() {
        return meterType;
    }

    @Override
    public Quantity getMeterReading(String name) throws IOException {
        throw new UnsupportedException("method getMeterReading( String name ) is not supported.");
    }

    @Override
    public Quantity getMeterReading(int channelID) throws IOException {
        throw new UnsupportedException("method getMeterReading( int channelID ) is not supported.");
    }

    @Override
    public byte[] getDataReadout() {
        return null;
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
    public String getRegister(String name) throws IOException {
        throw new UnsupportedException("getRegister() is not supported");
    }

    private void getRegistersInfo() {

        StringBuilder builder = new StringBuilder();
        String obisCodeString;
        ObisCode obisCode;
        RegisterInfo obisCodeInfo;

        logger.info("************************* Extended Logging *************************");

        int[] billingPoint = {255, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37};

        for (int bpi = 0; bpi < billingPoint.length; bpi++) {

            try {
                TariffSources ts;
                List<String> tarifRegisters = new ArrayList<>();

                if (billingPoint[bpi] == 0) {
					ts = (TariffSources) getRegisterFactory().getRegister("TariffSources");
                } else if ((billingPoint[bpi] > 0) && (billingPoint[bpi] <= 14)) {
                    HistoricalRegister hv = (HistoricalRegister)
					getRegisterFactory().getRegister("HistoricalRegister", billingPoint[bpi]);
                    if (hv.getBillingDate() == null) {
                        continue;
                    }
                    ts = hv.getTariffSources();
                } else {
                    HistoricalRegister hv = (HistoricalRegister)
					getRegisterFactory().getRegister("DailyHistoricalRegister", billingPoint[bpi]);
                    if (hv.getBillingDate() == null) {
                        continue;
                    }
                    ts = hv.getTariffSources();
                }

                builder.append("Billing point: ").append(billingPoint[bpi]).append("\n");

                if (bpi > 0) {
                    try {
                        obisCodeString = "1.1.0.1.2." + billingPoint[bpi];
                        obisCode = ObisCode.fromString(obisCodeString);
                        obisCodeInfo = ObisCodeMapper.getRegisterInfo(obisCode);
                        builder.append(" ").append(obisCodeString).append(", ").append(obisCodeInfo).append("\n");
                        if (pExtendedLogging == 2) {
							builder.append(" ").append(getRegisterFactory().readRegister(obisCode).toString()).append("\n");
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
                    obisCodeString = "1.1." + etc.getObisC() + ".8.0." + billingPoint[bpi];
                    obisCode = ObisCode.fromString(obisCodeString);
                    obisCodeInfo = ObisCodeMapper.getRegisterInfo(obisCode);
                    builder.append(" ").append(obisCodeString).append(", ").append(obisCodeInfo).append("\n");
                    if (pExtendedLogging == 2) {
						builder.append(" ").append(getRegisterFactory().readRegister(obisCode).toString()).append("\n");
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
                        if (pExtendedLogging == 2) {
							builder.append(" ").append(getRegisterFactory().readRegister(obisCode).toString()).append("\n");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                builder.append("\n");

                builder.append("Cumulative Maximum Demand registers:\n");
                int[] md = {0, 1, 2, 3};
                for (int i = 0; i < md.length; i++) {

                    try {
						CumulativeMaximumDemand cmd = (CumulativeMaximumDemand) getRegisterFactory().getRegister("CumulativeMaximumDemand" + i, billingPoint[bpi]);
                        int c = EnergyTypeCode.getObisCFromRegSource(cmd.getRegSource(), false);
                        obisCodeString = "1." + md[i] + "." + c + ".2.0." + billingPoint[bpi];
                        obisCode = ObisCode.fromString(obisCodeString);
                        obisCodeInfo = ObisCodeMapper.getRegisterInfo(obisCode);
                        builder.append(" ").append(obisCodeString).append(", ").append(obisCodeInfo).append("\n");
                        if (pExtendedLogging == 2) {
							builder.append(" ").append(getRegisterFactory().readRegister(obisCode).toString()).append("\n");
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                builder.append("\n");

                builder.append("Maximum demand registers:\n");

                int[] cmd = {0, 1, 2, 3};
                for (int i = 0; i < cmd.length; i++) {
                    int c;
                    try {
						MaximumDemand mdRegister = (MaximumDemand) getRegisterFactory().getRegister("MaximumDemand" + (i * 3), billingPoint[bpi]);
                        c = EnergyTypeCode.getObisCFromRegSource(mdRegister.getRegSource(), false);
                        if (mdRegister.getQuantity() == null) {
                            continue;
                        }

                        obisCodeString = "1.1." + c + ".6.0." + billingPoint[bpi];
                        obisCode = ObisCode.fromString(obisCodeString);
                        System.out.println("obisCode " + obisCodeString);
                        obisCodeInfo = ObisCodeMapper.getRegisterInfo(ObisCode.fromString(obisCodeString));
                        System.out.println("obisCodeInfo " + obisCodeInfo);
                        builder.append(" ").append(obisCodeString).append(", ").append(obisCodeInfo).append("\n");
                        if (pExtendedLogging == 2) {
							builder.append(" ").append(getRegisterFactory().readRegister(obisCode).toString()).append("\n");
                        }

                        obisCodeString = "1.2." + c + ".6.0." + billingPoint[bpi];
                        obisCode = ObisCode.fromString(obisCodeString);
                        System.out.println("obisCode " + obisCodeString);
                        obisCodeInfo = ObisCodeMapper.getRegisterInfo(ObisCode.fromString(obisCodeString));
                        System.out.println("obisCodeInfo " + obisCodeInfo);
                        builder.append(" ").append(obisCodeString).append(", ").append(obisCodeInfo).append("\n");
                        if (pExtendedLogging == 2) {
							builder.append(" ").append(getRegisterFactory().readRegister(obisCode).toString()).append("\n");
                        }

                        obisCodeString = "1.3." + c + ".6.0." + billingPoint[bpi];
                        obisCode = ObisCode.fromString(obisCodeString);
                        System.out.println("obisCode " + obisCodeString);
                        obisCodeInfo = ObisCodeMapper.getRegisterInfo(ObisCode.fromString(obisCodeString));
                        System.out.println("obisCodeInfo " + obisCodeInfo);
                        builder.append(" ").append(obisCodeString).append(", ").append(obisCodeInfo).append("\n");
                        if (pExtendedLogging == 2) {
							builder.append(" ").append(getRegisterFactory().readRegister(obisCode).toString()).append("\n");
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
            if (pExtendedLogging == 2) {
				builder.append(" ").append(getRegisterFactory().readRegister(obisCode).toString()).append("\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            obisCodeString = "1.1.0.4.5.255";
            obisCode = ObisCode.fromString(obisCodeString);
            obisCodeInfo = ObisCodeMapper.getRegisterInfo(ObisCode.fromString(obisCodeString));
            builder.append(" ").append(obisCodeString).append(", ").append(obisCodeInfo).append("\n");
            if (pExtendedLogging == 2) {
				builder.append(" ").append(getRegisterFactory().readRegister(obisCode).toString()).append("\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            obisCodeString = "1.0.0.0.1.255";
            obisCode = ObisCode.fromString(obisCodeString);
            obisCodeInfo = ObisCodeMapper.getRegisterInfo(ObisCode.fromString(obisCodeString));
            builder.append(" ").append(obisCodeString).append(", ").append(obisCodeInfo).append("\n");
            if (pExtendedLogging == 2) {
				builder.append(" ").append(getRegisterFactory().readRegister(obisCode).toString()).append("\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            obisCodeString = "0.0.96.50.0.255";
            obisCode = ObisCode.fromString(obisCodeString);
            obisCodeInfo = ObisCodeMapper.getRegisterInfo(ObisCode.fromString(obisCodeString));
            builder.append(" ").append(obisCodeString).append(", ").append(obisCodeInfo).append("\n");
            if (pExtendedLogging == 2) {
				builder.append(" ").append(getRegisterFactory().readRegister(obisCode).toString()).append("\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            obisCodeString = "0.0.96.51.0.255";
            obisCode = ObisCode.fromString(obisCodeString);
            obisCodeInfo = ObisCodeMapper.getRegisterInfo(ObisCode.fromString(obisCodeString));
            builder.append(" ").append(obisCodeString).append(", ").append(obisCodeInfo).append("\n");
            if (pExtendedLogging == 2) {
				builder.append(" ").append(getRegisterFactory().readRegister(obisCode).toString()).append("\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        builder.append("************************* End Extended Logging *********************");
        logger.info(builder.toString());

    }

    @Override
    public List getMessageCategories() {
        List<MessageCategorySpec> theCategories = new ArrayList<>();
        MessageCategorySpec cat = new MessageCategorySpec("BasicMessages");

        MessageSpec msgSpec = addBasicMsg(BILLINGRESET_DISPLAY, BILLINGRESET, false);
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

    @Override
    public String writeMessage(Message msg) {
        return msg.write(this);
    }

    @Override
    public String writeValue(MessageValue msgValue) {
        return msgValue.getValue();
    }

    @Override
    public void applyMessages(List messageEntries) throws IOException {
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
            if (att.getValue() == null || att.getValue().isEmpty()) {
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
                if (value == null || value.isEmpty()) {
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
    public MessageResult queryMessage(MessageEntry messageEntry) throws IOException {
        if (messageEntry.getContent().contains("<" + BILLINGRESET)) {
            try {
                logger.info("************************* BILLING RESET *************************");
                logger.info("Performing billing reset ...");
                int start = messageEntry.getContent().indexOf(BILLINGRESET) + BILLINGRESET.length() + 1;
                int end = messageEntry.getContent().lastIndexOf(BILLINGRESET) - 2;
                doBillingReset();
                logger.info("Billing reset succes!");
                return MessageResult.createSuccess(messageEntry);
            } catch (IOException e) {
                logger.info("Billing reset failed! => " + e.getMessage());
                e.printStackTrace();
            }
        }
        return MessageResult.createFailed(messageEntry);
    }

    private void doBillingReset() throws IOException {
		getRegisterFactory().setRegister("EndOfBillingPeriod", "01");
    }

    private ABBA1140Profile getProfile() {
        return profile;
    }

    protected void setProfile(ABBA1140Profile profile) {
        this.profile = profile;
    }

    public boolean useExtendedProfileStatus() {
        return extendedProfileStatus;
    }

}