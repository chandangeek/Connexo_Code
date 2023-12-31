package com.energyict.protocolimpl.iec1107.abba1700;

import com.energyict.mdc.upl.NoSuchRegisterException;
import com.energyict.mdc.upl.SerialNumberSupport;
import com.energyict.mdc.upl.UnsupportedException;
import com.energyict.mdc.upl.io.NestedIOException;
import com.energyict.mdc.upl.messages.legacy.Message;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.MessageTag;
import com.energyict.mdc.upl.messages.legacy.MessageValue;
import com.energyict.mdc.upl.nls.TranslationKey;
import com.energyict.mdc.upl.properties.InvalidPropertyException;
import com.energyict.mdc.upl.properties.MissingPropertyException;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecBuilderWizard;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.TypedProperties;

import com.energyict.cbo.Quantity;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dialer.connection.HHUSignOn;
import com.energyict.dialer.connections.IEC1107HHUConnection;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.HHUEnabler;
import com.energyict.protocol.MessageProtocol;
import com.energyict.protocol.MessageResult;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.MeterExceptionInfo;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterProtocol;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.SerialNumber;
import com.energyict.protocol.meteridentification.DiscoverInfo;
import com.energyict.protocol.meteridentification.MeterType;
import com.energyict.protocolimpl.base.PluggableMeterProtocol;
import com.energyict.protocolimpl.base.ProtocolChannelMap;
import com.energyict.protocolimpl.errorhandling.ProtocolIOExceptionHandler;
import com.energyict.protocolimpl.iec1107.ChannelMap;
import com.energyict.protocolimpl.iec1107.FlagIEC1107Connection;
import com.energyict.protocolimpl.iec1107.FlagIEC1107ConnectionException;
import com.energyict.protocolimpl.iec1107.ProtocolLink;
import com.energyict.protocolimpl.nls.PropertyTranslationKeys;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;
import com.energyict.protocolimpl.utils.ComServerModeUtil;
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
import java.util.logging.Logger;

import static com.energyict.mdc.upl.MeterProtocol.Property.*;
import static com.energyict.protocolimpl.iec1107.abba1700.ABBA1700RegisterFactory.BillingResetKey;
import static com.energyict.protocolimpl.iec1107.abba1700.ABBA1700RegisterFactory.TimeDateKey;

/**
 * @author Koen
 */
public class ABBA1700 extends PluggableMeterProtocol implements ProtocolLink, HHUEnabler, SerialNumber, MeterExceptionInfo, RegisterProtocol, MessageProtocol, SerialNumberSupport { // KV 19012004

    private static final int BREAK_DELAY = 500;
    private static final int BREAK_BAUDRATE = 9600;

    private static final String[] ABB1700_REGISTERCONFIG = {
            "CummMainImport", "CummMainExport", "CummMainQ1", "CummMainQ2",
            "CummMainQ3", "CummMainQ4", "CummMainVA", "CummMainCustDef1",
            "CummMainCustDef2", "CummMainCustDef3", "ExternalInput1",
            "ExternalInput2", "ExternalInput3", "ExternalInput4"
    };

    // KV 19012004 implementation of MeterExceptionInfo
    private static final Map<String, String> EXCEPTIONINFOMAP = new HashMap<>();

    static {
        EXCEPTIONINFOMAP.put("ERR1", "Invalid Command/Function type e.g. other than W1, R1 etc");
        EXCEPTIONINFOMAP.put("ERR2", "Invalid Data Identity Number e.g. Data id does not exist in the meter");
        EXCEPTIONINFOMAP.put("ERR3", "Invalid Packet Number");
        EXCEPTIONINFOMAP.put("ERR5", "Data Identity is locked - password timeout");
        EXCEPTIONINFOMAP.put("ERR6", "General Comms error");
    }

    private final PropertySpecService propertySpecService;

    private boolean soft7E1 = false;
    private boolean breakBeforeConnect = false;
    private String nodeId = null;
    private String strID = null;
    private String strPassword = null;
    private TimeZone timeZone = null;
    private Logger logger = null;
    private FlagIEC1107Connection connection = null;
    private ABBA1700RegisterFactory abba1700RegisterFactory = null;
    private ABBA1700Profile abba1700Profile = null;
    private ABBA1700MeterType abba1700MeterType = null;
    private SerialCommunicationChannel commChannel = null;
    private ABBA1700Messages messages = new ABBA1700Messages(this);
    private ABBA1700MeterEvents meterEvents;

    private int iTimeout;
    private int iProtocolRetries;
    private int iRoundtripCorrection;
    private int iSecurityLevel;
    private int iEchoCancelling;
    private int iIEC1107Compatible;
    private int extendedLogging;
    private int forcedDelay;

    public ABBA1700(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Override
    public Quantity getMeterReading(String name) throws IOException {
        return (Quantity) getABBA1700RegisterFactory().getRegister(name);
    }

    @Override
    public Quantity getMeterReading(int channelID) throws IOException {
        return (Quantity) getABBA1700RegisterFactory().getRegister(ABB1700_REGISTERCONFIG[getABBA1700Profile().getChannelIndex(channelID)]);
    }

    @Override
    public ProfileData getProfileData(boolean includeEvents) throws IOException {
        return abba1700Profile.getProfileData();
    }

    @Override
    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        Calendar calendar = ProtocolUtils.getCalendar(getTimeZone());
        ProfileData pd = abba1700Profile.getProfileData(lastReading, calendar.getTime());
        if (includeEvents) {
            List<MeterEvent> meterEventList = getMeterEvents().getMeterEventList(lastReading);
            for (MeterEvent meterEvent : meterEventList) {
                pd.addEvent(meterEvent);
            }
        }
        return pd;
    }

    private ABBA1700MeterEvents getMeterEvents() {
        if (this.meterEvents == null) {
            this.meterEvents = new ABBA1700MeterEvents(this, this.abba1700MeterType);
        }
        return this.meterEvents;
    }

    @Override
    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException {
        throw new UnsupportedException("getProfileData(from,to) is not supported by this meter");
    }

    @Override
    public String getRegister(String name) throws IOException {
        String regName = name;
        int billingPoint = -1;
        String current = getABBA1700RegisterFactory().getRegister(regName, billingPoint).toString();
        if (name.contains("_")) {
            String[] strings = name.split("_");
            regName = strings[0];
            billingPoint = Integer.parseInt(strings[1]);
            String hist = ((HistoricalValues) getABBA1700RegisterFactory().getRegister("HistoricalValues", billingPoint)).getHistoricalValueSetInfo().toString();
            return current + ", " + hist;
        } else {
            return current;
        }
    }

    public void setRegister(String name, String value) throws IOException {
        getABBA1700RegisterFactory().setRegister(name, value);
    }

    public void resetDemand() throws IOException {
        getABBA1700RegisterFactory().invokeRegister(BillingResetKey);
    }

    public Date getTime() throws IOException {
        return (Date) getABBA1700RegisterFactory().getRegister(TimeDateKey);
    }

    public void setTime() throws IOException {
        Calendar calendar = ProtocolUtils.getCalendar(timeZone);
        calendar.add(Calendar.MILLISECOND, iRoundtripCorrection);
        getFlagIEC1107Connection().authenticate();
        getABBA1700RegisterFactory().setRegister(TimeDateKey, calendar.getTime());
    }

    @Override
    public String getSerialNumber() {
        try {
            return (String) getABBA1700RegisterFactory().getRegister("SerialNumber");
        } catch (IOException e){
            throw ProtocolIOExceptionHandler.handle(e, getNrOfRetries() + 1);
        }
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        return Arrays.asList(
                this.stringSpec(ADDRESS.getName(), PropertyTranslationKeys.IEC1107_ADDRESS),
                this.integerSpec(TIMEOUT.getName(), PropertyTranslationKeys.IEC1107_TIMEOUT),
                this.integerSpec(RETRIES.getName(), PropertyTranslationKeys.IEC1107_RETRIES),
                this.integerSpec(ROUNDTRIPCORRECTION.getName(), PropertyTranslationKeys.IEC1107_ROUNDTRIPCORRECTION),
                this.stringSpec(NODEID.getName(), PropertyTranslationKeys.IEC1107_NODEID),
                this.stringSpec(OFFLINE_NODEID.getName(), PropertyTranslationKeys.IEC1107_OFFLINE_NODEID),
                this.integerSpec("EchoCancelling", PropertyTranslationKeys.IEC1107_ECHOCANCELLING),
                this.integerSpec("IEC1107Compatible", PropertyTranslationKeys.IEC1107_COMPATIBLE),
                this.integerSpec(EXTENDED_LOGGING.getName(), PropertyTranslationKeys.IEC1107_EXTENDED_LOGGING),
                this.integerSpec("MeterType", PropertyTranslationKeys.IEC1107_METER_TYPE),
                this.integerSpec("ForcedDelay", PropertyTranslationKeys.IEC1107_FORCEDELAY),
                this.stringSpec(SOFTWARE7E1.getName(), PropertyTranslationKeys.IEC1107_SOFTWARE_7E1),
                this.stringSpec("BreakBeforeConnect", PropertyTranslationKeys.IEC1107_BREAK_BEFORE_CONNECT),

                this.integerSpec("AddressingMode", PropertyTranslationKeys.IEC1107_ADDRESSING_MODE),
                this.integerSpec("Connection", PropertyTranslationKeys.IEC1107_CONNECTION),
                this.integerSpec("DelayAfterfail", PropertyTranslationKeys.IEC1107_DELAY_AFTER_FAIL),
                this.integerSpec("RequestTimeZone", PropertyTranslationKeys.IEC1107_REQUEST_TIME_ZONE),
                this.integerSpec("RequestClockObject", PropertyTranslationKeys.IEC1107_REQUEST_CLOCK_OBJECT),
                this.integerSpec("ServerUpperMacAddress", PropertyTranslationKeys.IEC1107_SERVER_UPPER_MAC_ADDRESS),
                this.integerSpec("ServerLowerMacAddress", PropertyTranslationKeys.IEC1107_SERVER_LOWER_MAC_ADDRESS));
    }

    private <T> PropertySpec spec(String name, TranslationKey translationKey, Supplier<PropertySpecBuilderWizard.NlsOptions<T>> optionsSupplier) {
        return UPLPropertySpecFactory.specBuilder(name, false, translationKey, optionsSupplier).finish();
    }

    private PropertySpec stringSpec(String name, TranslationKey translationKey) {
        return this.spec(name, translationKey, this.propertySpecService::stringSpec);
    }

    private PropertySpec integerSpec(String name, TranslationKey translationKey) {
        return this.spec(name, translationKey, this.propertySpecService::integerSpec);
    }

    public void setUPLProperties(TypedProperties properties) throws MissingPropertyException, InvalidPropertyException {
        strID = properties.getTypedProperty(ADDRESS.getName());
        strPassword = properties.getTypedProperty(PASSWORD.getName(), "");
        iSecurityLevel = properties.getTypedProperty(SECURITYLEVEL.getName(), 2);
        if (iSecurityLevel != 0) {
            if (strPassword == null || strPassword.isEmpty()) {
                throw new InvalidPropertyException("Password field is empty! correct first!");
            }
            if (strPassword.length() != 8) {
                throw new InvalidPropertyException("Password must have a length of 8 characters!, correct first!");
            }
        }
        iTimeout = properties.getTypedProperty(TIMEOUT.getName(), 10000);
        iProtocolRetries = properties.getTypedProperty(RETRIES.getName(), 5);
        iRoundtripCorrection = properties.getTypedProperty(ROUNDTRIPCORRECTION.getName(), 0);
        if (ComServerModeUtil.isOnline()) {
            nodeId = properties.getTypedProperty(NODEID.getName(), "");
        }
        else {
            nodeId = properties.getTypedProperty(OFFLINE_NODEID.getName(), "");
        }


        iEchoCancelling = properties.getTypedProperty("EchoCancelling", 0);
        iIEC1107Compatible = properties.getTypedProperty("IEC1107Compatible", 0);
        extendedLogging = properties.getTypedProperty(EXTENDED_LOGGING.getName(), 0);

        // 0 = 16 TOU registers type (most in the UK)
        // 1 = 32 TOU registers type (Portugal, etc...)
        // -1 = use identification string from signon later...
        abba1700MeterType = new ABBA1700MeterType(properties.getTypedProperty("MeterType", -1));
        forcedDelay = properties.getTypedProperty("ForcedDelay", 300);
        this.soft7E1 = !"0".equalsIgnoreCase(properties.getTypedProperty(SOFTWARE7E1.getName(), "0"));
        this.breakBeforeConnect = !"0".equalsIgnoreCase(properties.getTypedProperty("BreakBeforeConnect", "0"));
    }

    @Override
    public String getProtocolDescription() {
        return "Elster/ABB A1700 IEC1107";
    }

    @Override
    public String getProtocolVersion() {
        return "$Date: 2023-03-31$";
    }

    @Override
    public String getFirmwareVersion() throws IOException {
        if (abba1700MeterType != null && abba1700MeterType.isAssigned()) {
            return abba1700MeterType.getFirmwareVersion();
        } else {
            return "Unknown";
        }
    }

    @Override
    public void initializeDevice() throws IOException {
        throw new UnsupportedException();
    }

    @Override
    public void init(InputStream in, OutputStream out, TimeZone tz, Logger log) {
        this.timeZone = tz;
        this.logger = log;

        try {
            connection = new FlagIEC1107Connection(in, out, iTimeout, iProtocolRetries, forcedDelay, iEchoCancelling, iIEC1107Compatible, new CAI700(), soft7E1, logger);
        } catch (ConnectionException e) {
            log.severe("ABBA1500: init(...), " + e.getMessage());
        }
    }

    @Override
    public void connect() throws IOException {
        connect(0);
    }

    public void connect(int baudrate) throws IOException {
        MeterType meterType;

        if (isBreakBeforeConnect()) {
            switchBaudRate(BREAK_BAUDRATE);
            getFlagIEC1107Connection().sendBreak();
            getFlagIEC1107Connection().delayAndFlush(BREAK_DELAY);
        }

        try {
            meterType = getFlagIEC1107Connection().connectMAC(strID, strPassword, iSecurityLevel, nodeId, baudrate);
            if (!abba1700MeterType.isAssigned()) {
                abba1700MeterType.updateWith(meterType);
            }
            abba1700RegisterFactory = new ABBA1700RegisterFactory(this, this, abba1700MeterType); // KV 19012004
            abba1700Profile = new ABBA1700Profile(this, getABBA1700RegisterFactory());
        } catch (FlagIEC1107ConnectionException e) {
            throw new IOException(e.getMessage());
        } catch (IOException e) {
            disconnect();
            throw e;
        }

        if (extendedLogging >= 1) {
            getRegistersInfo();
        }
    }

    private void getRegistersInfo() throws IOException {
        StringBuffer builder = new StringBuffer();
        String code;
        logger.info("************************* Extended Logging *************************");

        for (int billingPoint = -1; billingPoint < 12; billingPoint++) {

            TariffSources ts;
            if (billingPoint == -1) {
                ts = (TariffSources) getABBA1700RegisterFactory().getRegister("TariffSources");
            } else {
                HistoricalDisplayScalings hds = (HistoricalDisplayScalings) abba1700RegisterFactory.getRegister("HistoricalDisplayScalings", billingPoint);
                HistoricalDisplayScalingSet hdss = hds.getHistoricalDisplayScalingSet();
                ts = hdss.getTariffSources();
            }

            builder.append("Cumulative registers (total & tariff):\n");
            for (EnergyTypeCode etc : EnergyTypeCode.getEnergyTypeCodes()) {
                try {
                code = "1.1." + etc.getObisC() + ".8.0." + (billingPoint == -1 ? 255 : billingPoint);
                    builder.append(code + ", " + ObisCodeMapper.getRegisterInfo(ObisCode.fromString(code)) + "\n");
                for (int i = 0; i < ts.getRegSource().length; i++) {
                    if (ts.getRegSource()[i] == etc.getRegSource()) {
                        code = "1.1." + etc.getObisC() + ".8." + (i + 1) + "." + (billingPoint == -1 ? 255 : billingPoint);
                            builder.append(code + ", " + ObisCodeMapper.getRegisterInfo(ObisCode.fromString(code)) + "\n");
                    }
                }
            }
                catch (NoSuchRegisterException ignored) {
                    // absorb
                }
            }

            builder.append("Maximum demand registers:\n");
            for (int i = 0; i < ABBA1700RegisterFactory.MAX_MD_REGS; i += 3) {
                try {
                    //MaximumDemand md = (MaximumDemand)abba1700RegisterFactory.getRegister("MaximumDemand"+(i+obisCode.getB()-1),billingPoint);
                    List mds = new ArrayList();
                    for (int j = 0; j < 3; j++) {
                        mds.add(getABBA1700RegisterFactory().getRegister("MaximumDemand" + (i + j), billingPoint));
                    }
                    // sort in accending datetime
                    MaximumDemand.sortOnQuantity(mds);
                    // energytype code match with the maximumdemand register with the most
                    // recent datetime stamp.

                    for (int bField = 1; bField <= 3; bField++) {
                        MaximumDemand md = (MaximumDemand) mds.get(3 - bField); // B=1 => get(2), B=2 => get(1), B=3 => get(0)

                        //                   if (unit != null) // in case of customer defined registers unit is defined earlier
                        //                       md.setQuantity(new Quantity(md.getQuantity().getAmount(),unit.getFlowUnit()));
                        //

                        // We suppose that the rate 1 mostly covers the continue schedule (=total). So, we map rate 1 also to 0 to
                        // be backwards compatible with previous implementation
                        if (i == 0) {
                            code = "1." + bField + "." + EnergyTypeCode.getObisCFromRegSource(md.getRegSource(), false) + ".6.0." + (billingPoint == -1 ? 255 : billingPoint);
                            // KV_DEBUG
                            builder.append(code).append(", ").append(ObisCodeMapper.getRegisterInfo(ObisCode.fromString(code))).append(", ").append(md).append("\n");

                            code = "1." + bField + "." + EnergyTypeCode.getObisCFromRegSource(md.getRegSource(), false) + ".6.1." + (billingPoint == -1 ? 255 : billingPoint);
                            // KV_DEBUG
                            builder.append(code).append(", ").append(ObisCodeMapper.getRegisterInfo(ObisCode.fromString(code))).append(", ").append(md).append("\n");
                        } else {
                            code = "1." + bField + "." + EnergyTypeCode.getObisCFromRegSource(md.getRegSource(), false) + ".6." + ((i / 3) + 1) + "." + (billingPoint == -1 ? 255 : billingPoint);
                            // KV_DEBUG
                            builder.append(code).append(", ").append(ObisCodeMapper.getRegisterInfo(ObisCode.fromString(code))).append(", ").append(md).append("\n");
                        }


                    }
                } catch (NoSuchRegisterException ignored) {
                    //builder.append("KV_DEBUG> Unknown Code...");
                    // absorb...
                }

            }

            builder.append("cumulative maximum demand registers:\n");
            for (int i = 0; i < ABBA1700RegisterFactory.MAX_CMD_REGS; i++) {
                try {
                    CumulativeMaximumDemand cmd = (CumulativeMaximumDemand) getABBA1700RegisterFactory().getRegister("CumulativeMaximumDemand" + i, billingPoint);
                    if (i == 0) {
                        code = "1.1." + EnergyTypeCode.getObisCFromRegSource(cmd.getRegSource(), false) + ".2.0." + (billingPoint == -1 ? 255 : billingPoint);
                        builder.append(code).append(", ").append(ObisCodeMapper.getRegisterInfo(ObisCode.fromString(code))).append(", ").append(cmd).append("\n");

                        code = "1.1." + EnergyTypeCode.getObisCFromRegSource(cmd.getRegSource(), false) + ".2.1." + (billingPoint == -1 ? 255 : billingPoint);
                        builder.append(code).append(", ").append(ObisCodeMapper.getRegisterInfo(ObisCode.fromString(code))).append(", ").append(cmd).append("\n");
                    } else {
                        code = "1.1." + EnergyTypeCode.getObisCFromRegSource(cmd.getRegSource(), false) + ".2." + (i + 1) + "." + (billingPoint == -1 ? 255 : billingPoint);
                        builder.append(code).append(", ").append(ObisCodeMapper.getRegisterInfo(ObisCode.fromString(code))).append(", ").append(cmd).append("\n");
                    }
                } catch (NoSuchRegisterException e) {
                    //builder.append("KV_DEBUG> Unknown Code...");
                    // absorb...
                }
            }
        }

        logger.info(builder.toString());
        abba1700RegisterFactory.getProtocolLink().getFlagIEC1107Connection().authenticate();
    }

    @Override
    public void disconnect() throws NestedIOException {
        try {
            getFlagIEC1107Connection().disconnectMAC();
        } catch (FlagIEC1107ConnectionException e) {
            logger.severe("disconnect() error, " + e.getMessage());
        }
    }


    @Override
    public int getNumberOfChannels() throws IOException {
        long channelMask = getABBA1700Profile().getChannelMask();
        int nrOfChannels = 0;
        for (long i = 1; i != 0x10000; i <<= 1) {
            if ((channelMask & i) != 0) {
                nrOfChannels++;
            }
        }
        return nrOfChannels;
    }

    @Override
    public int getProfileInterval() throws IOException {
        return ((Integer) getABBA1700RegisterFactory().getRegister("IntegrationPeriod")).intValue() * 60;
    }

    public ABBA1700RegisterFactory getABBA1700RegisterFactory() {
        return abba1700RegisterFactory;
    }

    private ABBA1700Profile getABBA1700Profile() {
        return abba1700Profile;
    }


    @Override
    public FlagIEC1107Connection getFlagIEC1107Connection() {
        return connection;
    }

    @Override
    public TimeZone getTimeZone() {
        return timeZone;
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
        return null;
    }

    @Override
    public void release() throws IOException {
    }

    @Override
    public ProtocolChannelMap getProtocolChannelMap() {
        return null;
    }

    @Override
    public ChannelMap getChannelMap() {
        return null;
    }

    @Override
    public void enableHHUSignOn(SerialCommunicationChannel serialCommunicationChannel) throws ConnectionException {
        enableHHUSignOn(serialCommunicationChannel, false);
    }

    @Override
    public void enableHHUSignOn(SerialCommunicationChannel serialCommunicationChannel, boolean datareadout) throws ConnectionException {
        this.commChannel = serialCommunicationChannel;

        HHUSignOn hhuSignOn = new IEC1107HHUConnection(serialCommunicationChannel, iTimeout, iProtocolRetries, 300, iEchoCancelling);
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
        SerialCommunicationChannel serialCommunicationChannel = discoverInfo.getCommChannel();
        String nodeId = discoverInfo.getNodeId();
        int baudrate = discoverInfo.getBaudrate();
        TypedProperties properties = com.energyict.mdc.upl.TypedProperties.empty();
        properties.setProperty("SecurityLevel", "0");
        properties.setProperty(NODEID.getName(), nodeId == null ? "" : nodeId);
        properties.setProperty("IEC1107Compatible", "1");
        setUPLProperties(properties);
        init(serialCommunicationChannel.getInputStream(), serialCommunicationChannel.getOutputStream(), null, null);
        enableHHUSignOn(serialCommunicationChannel);
        connect(baudrate);
        String sn = getRegister("SerialNumber");
        disconnect();
        return sn;
    }

    @Override
    public String getExceptionInfo(String id) {
        String exceptionInfo = EXCEPTIONINFOMAP.get(id);
        if (exceptionInfo != null) {
            return id + ", " + exceptionInfo;
        } else {
            return "No meter specific exception info for " + id;
        }
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    public RegisterValue readRegister(com.energyict.obis.ObisCode obisCode) throws IOException {
        return getABBA1700RegisterFactory().readRegister(obisCode);
    }

    @Override
    public RegisterInfo translateRegister(com.energyict.obis.ObisCode obisCode) throws IOException {
        return ObisCodeMapper.getRegisterInfo(obisCode);
    }

    @Override
    public int getNrOfRetries() {
        return iProtocolRetries;
    }

    @Override
    public boolean isRequestHeader() {
        return false;
    }

    public boolean isBreakBeforeConnect() {
        return breakBeforeConnect;
    }

    private void switchBaudRate(int baudRate) throws IOException {
        if (isBreakBeforeConnect() && (commChannel != null)) {
            commChannel.setParams(baudRate, SerialCommunicationChannel.DATABITS_7, SerialCommunicationChannel.PARITY_EVEN, SerialCommunicationChannel.STOPBITS_1);
        }
    }

    @Override
    public void applyMessages(final List<MessageEntry> messageEntries) throws IOException {
        this.messages.applyMessages(messageEntries);
    }

    @Override
    public MessageResult queryMessage(final MessageEntry messageEntry) throws IOException {
        return this.messages.queryMessage(messageEntry);
    }

    @Override
    public List getMessageCategories() {
        return this.messages.getMessageCategories();
    }

    @Override
    public String writeMessage(final Message msg) {
        return this.messages.writeMessage(msg);
    }

    @Override
    public String writeTag(final MessageTag tag) {
        return this.messages.writeTag(tag);
    }

    @Override
    public String writeValue(final MessageValue value) {
        return this.messages.writeValue(value);
    }

    protected void setRegisterFactory(ABBA1700RegisterFactory registerFactory) {
        this.abba1700RegisterFactory = registerFactory;
    }

    protected void setConnection(final FlagIEC1107Connection connection) {
        this.connection = connection;
    }

}
