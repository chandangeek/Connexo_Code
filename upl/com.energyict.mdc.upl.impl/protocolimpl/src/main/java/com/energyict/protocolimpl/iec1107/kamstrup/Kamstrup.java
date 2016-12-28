/*
 * Kamstrup.java
 *
 * Created on 8 mei 2003, 17:56
 */

package com.energyict.protocolimpl.iec1107.kamstrup;

import com.energyict.mdc.upl.UnsupportedException;
import com.energyict.mdc.upl.properties.InvalidPropertyException;
import com.energyict.mdc.upl.properties.MissingPropertyException;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecBuilderWizard;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.TypedProperties;

import com.energyict.cbo.Quantity;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterProtocol;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.base.PluggableMeterProtocol;
import com.energyict.protocolimpl.base.ProtocolChannelMap;
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
import java.util.List;
import java.util.TimeZone;
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
 *         Class that implements the ABB A1500 Alpha meter protocol. This class implements the MeterProtocol interface.
 *         <BR>
 *         <B>@beginchanges</B><BR>
 *         KV |27092002|Initial version
 *         KV|31102002| Reengineered to MeterProtocol interface
 *         KV|23032005| Changed header to be compatible with protocol version tool
 *         KV|30032005| Handle StringOutOfBoundException in IEC1107 connection layer
 *         JME|30032005|Added support for software 7E1 communication: Added parity bit to outputStream, stripped parity bit from inputStream
 * @version 1.0
 * @endchanges
 */
public class Kamstrup extends PluggableMeterProtocol implements ProtocolLink, RegisterProtocol {

    private static final byte DEBUG = 0;

    private static final int KAMSTRUP_NR_OF_CHANNELS = 6;
    private static final String[] KAMSTRUP_METERREADINGS_979D1 = {"23.2.0", "13.1.0", "1:13.0.0", "0:41.0.0", "0:42.0.0", "97.97.0"};
    private static final String[] KAMSTRUP_METERREADINGS_979E1 = {"23.2.0", "1:12.0.0", "1:13.0.0", "0:41.0.0", "0:42.0.0", "97.97.0"};
    private static final String[] KAMSTRUP_METERREADINGS_979A1 = {"23.2.0", "13.1.0", "1:13.0.0", "0:41.0.0", "0:42.0.0", "97.97.0"};
    private static final String[] KAMSTRUP_METERREADINGS_DEFAULT = {"23.2.0", "13.1.0", "1:13.0.0", "0:41.0.0", "0:42.0.0", "97.97.0"};
    private final PropertySpecService propertySpecService;

    private String strID;
    private String strPassword;
    private int iIEC1107TimeoutProperty;
    private int iProtocolRetriesProperty;
    private int iRoundtripCorrection;
    private int iSecurityLevel;
    private String nodeId;
    private int iEchoCancelling;
    private int iIEC1107Compatible;
    private int iProfileInterval;
    private boolean software7E1;
    private TimeZone timeZone;
    private Logger logger;

    private FlagIEC1107Connection flagIEC1107Connection = null;
    private KamstrupRegistry kamstrupRegistry = null;
    private KamstrupProfile kamstrupProfile = null;
    private int extendedLogging;

    private byte[] dataReadout = null;

    public Kamstrup(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Override
    public ProfileData getProfileData(boolean includeEvents) throws IOException {
        Calendar calendar = ProtocolUtils.getCalendar(timeZone);
        calendar.add(Calendar.YEAR, -10);
        return doGetProfileData(calendar.getTime());
    }

    @Override
    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        return doGetProfileData(lastReading);
    }

    @Override
    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException {
        Calendar fromCalendar = ProtocolUtils.getCleanCalendar(timeZone);
        fromCalendar.setTime(from);
        Calendar toCalendar = ProtocolUtils.getCleanCalendar(timeZone);
        toCalendar.setTime(to);
        return getKamstrupProfile().getProfileData(fromCalendar,
                toCalendar,
                getNumberOfChannels(),
                1);
    }

    private ProfileData doGetProfileData(Date lastReading) throws IOException {
        Calendar fromCalendar = ProtocolUtils.getCleanCalendar(timeZone);
        fromCalendar.setTime(lastReading);
        return getKamstrupProfile().getProfileData(fromCalendar,
                ProtocolUtils.getCalendar(timeZone),
                getNumberOfChannels(),
                1);
    }

    // Only for debugging
    public ProfileData getProfileData(Calendar from, Calendar to) throws IOException {
        return getKamstrupProfile().getProfileData(from,
                to,
                getNumberOfChannels(),
                1);
    }

    @Override
    public Quantity getMeterReading(String name) throws IOException {
        try {
            return (Quantity) getKamstrupRegistry().getRegister(name);
        } catch (ClassCastException e) {
            throw new IOException("Kamstrup, getMeterReading, register " + name + " is not type Quantity");
        }
    }

    @Override
    public Quantity getMeterReading(int channelId) throws IOException {
        String[] KAMSTRUP_METERREADINGS = null;
        try {
            String revision = (String) getKamstrupRegistry().getRegister("UNIGAS software revision number");
            if ("979D1".compareTo(revision) == 0) {
                KAMSTRUP_METERREADINGS = KAMSTRUP_METERREADINGS_979D1;
            } else if ("979E1".compareTo(revision) == 0) {
                KAMSTRUP_METERREADINGS = KAMSTRUP_METERREADINGS_979E1;
            } else if ("979A1".compareTo(revision) == 0) {
                KAMSTRUP_METERREADINGS = KAMSTRUP_METERREADINGS_979A1;
            } else {
                KAMSTRUP_METERREADINGS = KAMSTRUP_METERREADINGS_DEFAULT;
            }

            if (channelId >= getNumberOfChannels()) {
                throw new IOException("Kamstrup, getMeterReading, invalid channelId, " + channelId);
            }
            return (Quantity) getKamstrupRegistry().getRegister(KAMSTRUP_METERREADINGS[channelId]);
        } catch (ClassCastException e) {
            throw new IOException("Kamstrup, getMeterReading, register " + KAMSTRUP_METERREADINGS[channelId] + " (" + channelId + ") is not type Quantity");
        }
    }

    @Override
    public void setTime() throws IOException {
        Calendar calendar = ProtocolUtils.getCalendar(timeZone);
        calendar.add(Calendar.MILLISECOND, iRoundtripCorrection);
        Date date = calendar.getTime();
        getKamstrupRegistry().setRegister("0.9.1", date);
        getKamstrupRegistry().setRegister("0.9.2", date);
    }

    @Override
    public Date getTime() throws IOException {
        Date date = (Date) getKamstrupRegistry().getRegister("TimeDate");
        return new Date(date.getTime() - iRoundtripCorrection);
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return Arrays.asList(
                this.stringSpec(ADDRESS.getName()),
                this.stringSpec(PASSWORD.getName()),
                this.integerSpec(TIMEOUT.getName()),
                this.integerSpec(RETRIES.getName()),
                this.integerSpec(ROUNDTRIPCORRECTION.getName()),
                this.integerSpec(SECURITYLEVEL.getName()),
                this.stringSpec(NODEID.getName()),
                this.integerSpec("EchoCancelling"),
                this.integerSpec("IEC1107Compatible"),
                this.stringSpec(PROFILEINTERVAL.getName()),
                this.integerSpec("ExtendedLogging"),
                this.stringSpec("Software7E1"));
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
    public void setProperties(TypedProperties properties) throws MissingPropertyException, InvalidPropertyException {
        try {
            strID = properties.getTypedProperty(com.energyict.mdc.upl.MeterProtocol.Property.ADDRESS.getName());
            strPassword = properties.getTypedProperty(com.energyict.mdc.upl.MeterProtocol.Property.PASSWORD.getName());
            iIEC1107TimeoutProperty = Integer.parseInt(properties.getTypedProperty(TIMEOUT.getName(), "20000").trim());
            iProtocolRetriesProperty = Integer.parseInt(properties.getTypedProperty(RETRIES.getName(), "5").trim());
            iRoundtripCorrection = Integer.parseInt(properties.getTypedProperty(ROUNDTRIPCORRECTION.getName(), "0").trim());
            iSecurityLevel = Integer.parseInt(properties.getTypedProperty(SECURITYLEVEL.getName(), "1").trim());
            nodeId = properties.getTypedProperty(com.energyict.mdc.upl.MeterProtocol.Property.NODEID.getName(), "");
            iEchoCancelling = Integer.parseInt(properties.getTypedProperty("EchoCancelling", "0").trim());
            iIEC1107Compatible = Integer.parseInt(properties.getTypedProperty("IEC1107Compatible", "1").trim());
            iProfileInterval = Integer.parseInt(properties.getTypedProperty(PROFILEINTERVAL.getName(), "3600").trim());
            extendedLogging = Integer.parseInt(properties.getTypedProperty("ExtendedLogging", "0").trim());
            this.software7E1 = !"0".equalsIgnoreCase(properties.getTypedProperty("Software7E1", "0"));
        } catch (NumberFormatException e) {
            throw new InvalidPropertyException(e, "Kamstrup: validation of properties failed before");
        }
    }

    @Override
    public String getRegister(String name) throws IOException {
        return ProtocolUtils.obj2String(getKamstrupRegistry().getRegister(name));
    }

    @Override
    public void setRegister(String name, String value) throws IOException {
        getKamstrupRegistry().setRegister(name, value);
    }

    @Override
    public void initializeDevice() throws IOException {
        throw new UnsupportedException();
    }

    @Override
    public String getProtocolVersion() {
        return "$Date: 2014-06-02 13:26:25 +0200 (Mon, 02 Jun 2014) $";
    }

    @Override
    public String getFirmwareVersion() throws IOException {
        try {
            return (getKamstrupRegistry().getRegister("CI software revision number") + " " + getKamstrupRegistry().getRegister("UNIGAS software revision number"));
        } catch (IOException e) {
            throw new IOException("Kamstrup, getFirmwareVersion, " + e.getMessage());
        }
    }

    @Override
    public void init(InputStream inputStream, OutputStream outputStream, TimeZone timeZone, Logger logger) {
        this.timeZone = timeZone;
        this.logger = logger;

        try {
            flagIEC1107Connection = new FlagIEC1107Connection(inputStream, outputStream, iIEC1107TimeoutProperty, iProtocolRetriesProperty, 0, iEchoCancelling, iIEC1107Compatible, software7E1, logger);
            kamstrupRegistry = new KamstrupRegistry(this);
            kamstrupProfile = new KamstrupProfile(this, kamstrupRegistry);
        } catch (ConnectionException e) {
            logger.severe("ABBA1500: init(...), " + e.getMessage());
        }

    }

    @Override
    public void connect() throws IOException {
        try {
            dataReadout = flagIEC1107Connection.dataReadout(strID, nodeId);
            flagIEC1107Connection.disconnectMAC();
            flagIEC1107Connection.connectMAC(strID, strPassword, iSecurityLevel, nodeId);
            registerInfo();
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
        return KAMSTRUP_NR_OF_CHANNELS;
    }

    @Override
    public int getProfileInterval() throws IOException {
        return iProfileInterval;
    }

    public KamstrupRegistry getKamstrupRegistry() {
        return kamstrupRegistry;
    }

    private KamstrupProfile getKamstrupProfile() {
        return kamstrupProfile;
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
    public ProtocolChannelMap getProtocolChannelMap() {
        return null;
    }

    @Override
    public void release() throws IOException {
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    public int getNrOfRetries() {
        return iProtocolRetriesProperty;
    }

    @Override
    public boolean isRequestHeader() {
        return false;
    }

    private void registerInfo() {
        ObisCodeMapper ocm = new ObisCodeMapper(this);
        if (extendedLogging >= 1) {
            logger.info(ocm.getRegisterInfo());
        }
    }

    @Override
    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        return ObisCodeMapper.getRegisterInfo(obisCode);
    }

    @Override
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        ObisCodeMapper ocm = new ObisCodeMapper(this);
        return ocm.getRegisterValue(obisCode);
    }

}