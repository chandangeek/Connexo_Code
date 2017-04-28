/*
 * EictRtuVdew.java
 *
 * Created on 10 januari 2005, 09:19
 */

package com.energyict.protocolimpl.iec1107.eictrtuvdew;

import com.energyict.mdc.upl.NoSuchRegisterException;
import com.energyict.mdc.upl.UnsupportedException;
import com.energyict.mdc.upl.nls.NlsService;
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
import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.protocol.HHUEnabler;
import com.energyict.protocol.HalfDuplexEnabler;
import com.energyict.protocol.MeterExceptionInfo;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterProtocol;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.base.DataParser;
import com.energyict.protocolimpl.base.PluggableMeterProtocol;
import com.energyict.protocolimpl.base.ProtocolChannelMap;
import com.energyict.protocolimpl.iec1107.ChannelMap;
import com.energyict.protocolimpl.iec1107.FlagIEC1107Connection;
import com.energyict.protocolimpl.iec1107.FlagIEC1107ConnectionException;
import com.energyict.protocolimpl.iec1107.ProtocolLink;
import com.energyict.protocolimpl.nls.PropertyTranslationKeys;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;
import com.energyict.protocolimpl.utils.ProtocolUtils;
import com.energyict.protocolimplv2.messages.nls.Thesaurus;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.function.Supplier;
import java.util.logging.Logger;

import static com.energyict.mdc.upl.MeterProtocol.Property.ADDRESS;
import static com.energyict.mdc.upl.MeterProtocol.Property.NODEID;
import static com.energyict.mdc.upl.MeterProtocol.Property.PASSWORD;
import static com.energyict.mdc.upl.MeterProtocol.Property.PROFILEINTERVAL;
import static com.energyict.mdc.upl.MeterProtocol.Property.RETRIES;
import static com.energyict.mdc.upl.MeterProtocol.Property.ROUNDTRIPCORRECTION;
import static com.energyict.mdc.upl.MeterProtocol.Property.SECURITYLEVEL;
import static com.energyict.mdc.upl.MeterProtocol.Property.TIMEOUT;

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

    private static final byte DEBUG = 0;
    private final PropertySpecService propertySpecService;
    private final NlsService nlsService;

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

    public EictRtuVdew(PropertySpecService propertySpecService, NlsService nlsService) {
        this.propertySpecService = propertySpecService;
        this.nlsService = nlsService;
    }

    public ProfileData getProfileData(boolean includeEvents) throws IOException {
        Calendar calendar = ProtocolUtils.getCalendar(timeZone);
        calendar.add(Calendar.YEAR, -10);
        return getProfileData(calendar.getTime(), includeEvents);
    }

    @Override
    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        return getEictRtuVdewProfile().getProfileData(lastReading, includeEvents);
    }

    @Override
    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException {
        return getEictRtuVdewProfile().getProfileData(from, to, includeEvents);
    }

    @Override
    public Quantity getMeterReading(String name) throws IOException {
        throw new UnsupportedException();
    }

    @Override
    public Quantity getMeterReading(int channelId) throws IOException {
        throw new UnsupportedException();
    }

    @Override
    public void setTime() throws IOException {
        Calendar calendar = ProtocolUtils.getCalendar(timeZone);
        calendar.add(Calendar.MILLISECOND, iRoundtripCorrection);
        Date date = calendar.getTime();
        getEictRtuVdewRegistry().setRegister("Time", date);
        getEictRtuVdewRegistry().setRegister("Date", date);
    }

    @Override
    public Date getTime() throws IOException {
        Date date = (Date) getEictRtuVdewRegistry().getRegister("TimeDate");
        return new Date(date.getTime() - iRoundtripCorrection);
    }

    public byte getLastProtocolState() {
        return -1;
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        return Arrays.asList(
                this.stringSpec(ADDRESS.getName(), PropertyTranslationKeys.IEC1107_ADDRESS),
                this.stringSpec(PASSWORD.getName(), PropertyTranslationKeys.IEC1107_PASSWORD),
                this.integerSpec(TIMEOUT.getName(), PropertyTranslationKeys.IEC1107_TIMEOUT),
                this.integerSpec(RETRIES.getName(), PropertyTranslationKeys.IEC1107_RETRIES),
                this.integerSpec(ROUNDTRIPCORRECTION.getName(), PropertyTranslationKeys.IEC1107_ROUNDTRIPCORRECTION),
                this.integerSpec(SECURITYLEVEL.getName(), PropertyTranslationKeys.IEC1107_SECURITYLEVEL),
                this.stringSpec(NODEID.getName(), PropertyTranslationKeys.IEC1107_NODEID),
                this.integerSpec("EchoCancelling", PropertyTranslationKeys.IEC1107_ECHOCANCELLING),
                this.integerSpec("IEC1107Compatible", PropertyTranslationKeys.IEC1107_COMPATIBLE),
                this.integerSpec(PROFILEINTERVAL.getName(), PropertyTranslationKeys.IEC1107_PROFILEINTERVAL),
                this.integerSpec("RequestHeader", PropertyTranslationKeys.IEC1107_REQUESTHEADER),
                ProtocolChannelMap.propertySpec("ChannelMap", false, this.nlsService.getThesaurus(Thesaurus.ID.toString()).getFormat(PropertyTranslationKeys.IEC1107_CHANNEL_MAP).format(), this.nlsService.getThesaurus(Thesaurus.ID.toString()).getFormat(PropertyTranslationKeys.IEC1107_CHANNEL_MAP_DESCRIPTION).format()),
                this.integerSpec("Scaler", PropertyTranslationKeys.IEC1107_SCALER),
                this.integerSpec("HalfDuplex", PropertyTranslationKeys.IEC1107_HALF_DUPLEX),
                this.integerSpec("ForcedDelay", PropertyTranslationKeys.IEC1107_FORCED_DELAY),
                this.stringSpec("Software7E1", PropertyTranslationKeys.IEC1107_SOFTWARE_7E1));
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

    @Override
    public void setUPLProperties(TypedProperties properties) throws MissingPropertyException, InvalidPropertyException {
        strID = properties.getTypedProperty(ADDRESS.getName());
        strPassword = properties.getTypedProperty(PASSWORD.getName());
        iIEC1107TimeoutProperty = properties.getTypedProperty(TIMEOUT.getName(), 20000);
        iProtocolRetriesProperty = properties.getTypedProperty(RETRIES.getName(), 5);
        iRoundtripCorrection = properties.getTypedProperty(ROUNDTRIPCORRECTION.getName(), 0);
        iSecurityLevel = properties.getTypedProperty(SECURITYLEVEL.getName(), 1);
        nodeId = properties.getTypedProperty(com.energyict.mdc.upl.MeterProtocol.Property.NODEID.getName(), "");
        iEchoCancelling = properties.getTypedProperty("EchoCancelling", 0);
        iIEC1107Compatible = properties.getTypedProperty("IEC1107Compatible", 1);
        profileInterval = properties.getTypedProperty(PROFILEINTERVAL.getName(), 3600);
        requestHeader = properties.getTypedProperty("RequestHeader", 0);
        // KV 07092005 K&P
        protocolChannelMap = properties.getTypedProperty("ChannelMap", new ProtocolChannelMap("0.0 1.1 2.2 3.3 4.4 5.5 6.6 7.7 8.8 9.9 10.10 11.11 12.12 13.13 14.14 15.15 16.16 17.17 18.18 19.19 20.20 21.21 22.22 23.23 24.24 25.25 26.26 27.27 28.28 29.29 30.30 31.31"));
        scaler = properties.getTypedProperty("Scaler", 0);
        halfDuplex = properties.getTypedProperty("HalfDuplex", 0);
        forcedDelay = properties.getTypedProperty("ForcedDelay", 0);
        this.software7E1 = !"0".equalsIgnoreCase(properties.getTypedProperty("Software7E1", "0"));
    }

    @Override
    public String getRegister(String name) throws IOException {
        return ProtocolUtils.obj2String(getEictRtuVdewRegistry().getRegister(name));
    }

    @Override
    public void setRegister(String name, String value) throws IOException {
        getEictRtuVdewRegistry().setRegister(name, value);
    }

    @Override
    public void initializeDevice() throws IOException {
        throw new UnsupportedException();
    }

    @Override
    public String getProtocolDescription() {
        return "EnergyICT RTU IEC1107 (VDEW)";
    }

    @Override
    public String getProtocolVersion() {
        return "$Date: 2015-10-20 09:39:12 +0200 (Tue, 20 Oct 2015) $";
    }

    @Override
    public String getFirmwareVersion() throws IOException {
        return ("Unknown");
    }

    @Override
    public void init(InputStream inputStream, OutputStream outputStream, TimeZone timeZone, Logger logger) {
        this.timeZone = timeZone;
        this.logger = logger;

        flagIEC1107Connection = new FlagIEC1107Connection(inputStream, outputStream, iIEC1107TimeoutProperty, iProtocolRetriesProperty, forcedDelay, iEchoCancelling, iIEC1107Compatible, null, halfDuplex != 0 ? halfDuplexController : null, software7E1, logger);
        eictRtuVdewRegistry = new EictRtuVdewRegistry(this, this);
        eictRtuVdewProfile = new EictRtuVdewProfile(this, this, eictRtuVdewRegistry);

    }

    @Override
    public void connect() throws IOException {
        try {
            flagIEC1107Connection.connectMAC(strID, strPassword, iSecurityLevel, nodeId);
        } catch (FlagIEC1107ConnectionException e) {
            throw new IOException(e.getMessage());
        }
    }

    @Override
    public void disconnect() throws IOException {
        try {
            flagIEC1107Connection.disconnectMAC();
        } catch (FlagIEC1107ConnectionException e) {
            logger.severe("disconnect() error, " + e.getMessage());
        }
    }

    @Override
    public int getNumberOfChannels() throws IOException {
        if (requestHeader == 1) {
            return getEictRtuVdewProfile().getProfileHeader().getNrOfChannels();
        } else {
            return getProtocolChannelMap().getNrOfProtocolChannels();
        }
    }

    @Override
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

    @Override
    public FlagIEC1107Connection getFlagIEC1107Connection() {
        return flagIEC1107Connection;
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
        return dataReadout;
    }

    @Override
    public ChannelMap getChannelMap() {
        return null;
    }

    @Override
    public void release() throws IOException {
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    private static final Map<String, String> EXCEPTION_INFO_MAP = new HashMap<>();

    static {
        EXCEPTION_INFO_MAP.put("ERROR", "Request could not execute!");
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
    public int getNrOfRetries() {
        return iProtocolRetriesProperty;
    }

    @Override
    public boolean isRequestHeader() {
        return requestHeader == 1;
    }

    @Override
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
            return new BigDecimal(dp.parseBetweenBrackets(data, 0));
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

    @Override
    public RegisterValue readRegister(com.energyict.obis.ObisCode obisCode) throws IOException {
        String edisNotation = obisCode.getA() + "-" + obisCode.getB() + ":" + obisCode.getC() + "." + obisCode.getD() + "." + obisCode.getE() + (obisCode.getF() == 255 ? "" : "*" + Math.abs(obisCode.getF()));
        BigDecimal bd = doGetRegister(edisNotation + "(;)");
        return new RegisterValue(obisCode, new Quantity(bd, obisCode.getUnitElectricity(scaler)));
    }

    @Override
    public RegisterInfo translateRegister(com.energyict.obis.ObisCode obisCode) throws IOException {
        return new RegisterInfo(obisCode.toString());
    }

    @Override
    public void enableHHUSignOn(SerialCommunicationChannel commChannel) throws ConnectionException {
        enableHHUSignOn(commChannel, false);
    }

    @Override
    public void enableHHUSignOn(SerialCommunicationChannel commChannel, boolean datareadout) throws ConnectionException {
        HHUSignOn hhuSignOn = new IEC1107HHUConnection(commChannel, iIEC1107TimeoutProperty, iProtocolRetriesProperty, 300, iEchoCancelling);
        hhuSignOn.setMode(HHUSignOn.MODE_PROGRAMMING);
        hhuSignOn.setProtocol(HHUSignOn.PROTOCOL_NORMAL);
        hhuSignOn.enableDataReadout(datareadout);
        getFlagIEC1107Connection().setHHUSignOn(hhuSignOn);
    }

    @Override
    public byte[] getHHUDataReadout() {
        setDataReadout(getFlagIEC1107Connection().getHhuSignOn().getDataReadout());
        return getDataReadout();
    }

    public void setDataReadout(byte[] dataReadout) {
        this.dataReadout = dataReadout;
    }

    @Override
    public void setHalfDuplexController(HalfDuplexController halfDuplexController) {
        this.halfDuplexController = halfDuplexController;
        this.halfDuplexController.setDelay(halfDuplex);

        if (getFlagIEC1107Connection() != null) {
            getFlagIEC1107Connection().setHalfDuplexController(halfDuplex != 0 ? this.halfDuplexController : null);
        }
    }

}