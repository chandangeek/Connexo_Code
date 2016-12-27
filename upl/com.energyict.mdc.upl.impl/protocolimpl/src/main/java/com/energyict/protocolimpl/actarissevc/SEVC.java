package com.energyict.protocolimpl.actarissevc;

import com.energyict.mdc.io.NestedIOException;
import com.energyict.mdc.upl.ProtocolException;
import com.energyict.mdc.upl.UnsupportedException;
import com.energyict.mdc.upl.properties.InvalidPropertyException;
import com.energyict.mdc.upl.properties.MissingPropertyException;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.TypedProperties;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dialer.connection.HHUSignOn;
import com.energyict.dialer.connection.IEC1107HHUConnection;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.protocol.HHUEnabler;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocol.SerialNumber;
import com.energyict.protocol.meteridentification.DiscoverInfo;
import com.energyict.protocol.support.SerialNumberSupport;
import com.energyict.protocolimpl.base.PluggableMeterProtocol;
import com.energyict.protocolimpl.base.ProtocolConnectionException;
import com.energyict.protocolimpl.errorhandling.ProtocolIOExceptionHandler;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Logger;

import static com.energyict.mdc.upl.MeterProtocol.Property.ADDRESS;
import static com.energyict.mdc.upl.MeterProtocol.Property.NODEID;
import static com.energyict.mdc.upl.MeterProtocol.Property.PASSWORD;
import static com.energyict.mdc.upl.MeterProtocol.Property.RETRIES;
import static com.energyict.mdc.upl.MeterProtocol.Property.ROUNDTRIPCORRECTION;
import static com.energyict.mdc.upl.MeterProtocol.Property.SERIALNUMBER;
import static com.energyict.mdc.upl.MeterProtocol.Property.TIMEOUT;

/*
 *
 * @version  1.0
 * @author   Koenraad Vanderschaeve
 * <P>
 * <B>Description :</B><BR>
 * Class that implements the Schlumberger gascorrector SEVCD meter protocol. This class extends from the abstract MeterProtocol class
 * to implement most common methods for meterreading purposes.
 * <BR>
 * @beginchanges
KV|27092002|Initial version
KV|31102002|Reengineered to MeterProtocol interface
KV|25082003|readout of SEVC_INTEGER_BINARY type, signed byte!.
KV|23032005|Changed header to be compatible with protocol version tool
KV|07042006|Bugfix to correct read gross and corrected volume
 * @endchanges
 */
public class SEVC extends PluggableMeterProtocol implements HHUEnabler, SerialNumber, SerialNumberSupport {

    private static final byte DEBUG = 0;

    private final String[] strRegisters = {"CVC", null, null, null, null};

    private static final int SEVC_NR_OF_CHANNELS = 4;

    // Read commands
    private static final byte SEVC_READ_PTE = (byte) 0x01;
    private static final byte SEVC_READ_PVA = (byte) 0x06;
    private static final byte SEVC_READ_PVN = (byte) 0x07;
    private static final byte SEVC_READ_HO = (byte) 0x0F;
    private static final byte SEVC_READ_TCPR = (byte) 0x13;
    private static final byte SEVC_WRITE_HO = (byte) 0x8F;
    private static final byte SEVC_WRITE_RANGE = (byte) 0xc7; // profile data range
    private static final byte SEVC_READ_CFCOM1 = (byte) 0x47; // profile data
    private static final byte SEVC_READ_TEVT = (byte) 0x15;   // event log 200 items
    private static final byte SEVC_READ_VERSION = (byte) 0x0D; // Get firmware version

    private static final int SCALEFACTOR = 4;
    private static final int LOGBOOK_SIZE = 1200;

    private final Unit[] SEVC_METERREADINGSUNITS = {Unit.get(BaseUnit.CUBICMETER), null, null, null, null};

    private String strID;
    private String strPassword;
    private String serialNumber;
    private int iProtocolRetriesProperty;
    private int iIEC1107TimeoutProperty;
    private int iRoundtripCorrection;
    private String nodeId;  // KV 13082003

    private TimeZone timeZone;
    private Logger logger;

    private int interval = 0;
    protected byte bNROfChannels = 0;

    private SEVCIEC1107Connection sevciec1107Connection = null;
    private SEVCRegisterFactory sevcRegisterFactory = null;

    private int forcedDelay;

    SEVCRegisterFactory getSEVCRegisterFactory() {
        return sevcRegisterFactory;
    }

    private SEVCIEC1107Connection getSEVCIEC1107Connection() {
        return sevciec1107Connection;
    }

    @Override
    public ProfileData getProfileData(boolean includeEvents) throws IOException {
        Calendar fromCalendar = ProtocolUtils.getCalendar(timeZone);
        fromCalendar.add(Calendar.YEAR, -10);
        return doGetProfileData(fromCalendar, ProtocolUtils.getCalendar(timeZone), includeEvents);
    }

    @Override
    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        Calendar fromCalendar = ProtocolUtils.getCleanCalendar(timeZone);
        fromCalendar.setTime(lastReading);
        return doGetProfileData(fromCalendar, ProtocolUtils.getCalendar(timeZone), includeEvents);
    }

    @Override
    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException {
        throw new UnsupportedException("getProfileData(from,to) is not supported by this meter");
    }

    private ProfileData doGetProfileData(Calendar fromCalendar, Calendar toCalendar, boolean includeEvents) throws IOException {
        return doGetDemandValues(fromCalendar, toCalendar, includeEvents);
    }

    protected TimeZone getTimeZone() {
        return timeZone;
    }

    protected SEVCIEC1107Connection getIEC1107Connection() {
        return sevciec1107Connection;
    }

    private ProfileData doGetDemandValues(Calendar fromCalendar, Calendar toCalendar, boolean includeEvents) throws IOException {
        int range = (int) ((((toCalendar.getTime().getTime() - fromCalendar.getTime().getTime())
                / 1000)
                / 60)
                / (getProfileInterval() / 60));

        SEVCProfile profile = new SEVCProfile(this);
        try {
            byte[] intervalData = null;
            byte[] logbookData = null;

            if (range > 0) {
                doReadDatabaseProfile(range);
                intervalData = sevciec1107Connection.receiveSegmentedData(range * profile.getFrameSize());
            }

            if (includeEvents) {
                logbookData = doReadDatabaseLogbook();
            }
            return (profile.getProfile(intervalData, logbookData));
        } catch (SEVCIEC1107ConnectionException e) {
            throw new IOException("doGetDemandValues() error, " + e.getMessage());
        }

    }

    private void doReadDatabaseProfile(int nrOfBlocks) throws IOException {
        byte[] buffer = new byte[4];
        int i;

        buffer[0] = 0x01;
        buffer[1] = 0x00;
        buffer[2] = (byte) (nrOfBlocks & 0xFF);
        buffer[3] = (byte) ((nrOfBlocks >> 8) & 0xFF);

        try {
            sevciec1107Connection.sendWriteFrame(SEVC_WRITE_RANGE, buffer);
            sevciec1107Connection.sendReadFrame(SEVC_READ_CFCOM1);
        } catch (SEVCIEC1107ConnectionException e) {
            throw new IOException("doReadDatabase() error, " + e.getMessage());
        }

    }

    private byte[] doReadDatabaseLogbook() throws IOException {
        try {
            sevciec1107Connection.sendReadFrame(SEVC_READ_TEVT);
            return (sevciec1107Connection.receiveSegmentedData(LOGBOOK_SIZE));
        } catch (SEVCIEC1107ConnectionException e) {
            throw new IOException("doReadDatabase() error, " + e.getMessage());
        }

    }

    @Override
    public Quantity getMeterReading(String name) throws IOException {
        throw new UnsupportedException("SEVC, using meterreading names is not supported!");
    }

    @Override
    public Quantity getMeterReading(int channelId) throws IOException {
        Quantity quantity;
        quantity = new Quantity((BigDecimal) doGetMeterReading(channelId), SEVC_METERREADINGSUNITS[channelId]);
        return quantity;
    }

    private Number doGetMeterReading(int iChannelNr) throws IOException {
        if (strRegisters[iChannelNr] != null) {
            return getSEVCRegisterFactory().getValue(strRegisters[iChannelNr], getSEVCIEC1107Connection());
        } else {
            return null;
        }
    }

    @Override
    public void setTime() throws IOException {
        Calendar calendar = ProtocolUtils.getCalendar(timeZone);
        calendar.add(Calendar.MILLISECOND, iRoundtripCorrection);
        doSetTime(calendar);
    }

    private void doSetTime(Calendar calendar) throws IOException {
        byte[] byteTimeBuffer = new byte[7];
        byteTimeBuffer[0] = (byte) calendar.get(Calendar.YEAR);
        byteTimeBuffer[1] = (byte) (calendar.get(Calendar.YEAR) >> 8);
        byteTimeBuffer[2] = (byte) (calendar.get(Calendar.MONTH) + 1);
        byteTimeBuffer[3] = (byte) calendar.get(Calendar.DAY_OF_MONTH);
        byteTimeBuffer[4] = (byte) calendar.get(Calendar.HOUR_OF_DAY);
        byteTimeBuffer[5] = (byte) calendar.get(Calendar.MINUTE);
        byteTimeBuffer[6] = (byte) calendar.get(Calendar.SECOND);

        try {
            sevciec1107Connection.sendWriteFrame(SEVC_WRITE_HO, byteTimeBuffer);
        } catch (SEVCIEC1107ConnectionException e) {
            throw new IOException("getTime() error, " + e.getMessage());
        }
    }

    @Override
    public Date getTime() throws IOException {
        int iRetries = 0;
        while (true) {
            try {
                sevciec1107Connection.sendReadFrame(SEVC_READ_HO);
                byte[] data = sevciec1107Connection.receiveData();
                if (data.length != 7) {
                    throw new IOException("getTime() error, wrong framelength! (" + data.length + ")");
                }
                Calendar calendar = ProtocolUtils.getCleanCalendar(timeZone);
                calendar.set(Calendar.YEAR, (int) data[0] & 0xff | (((int) data[1] & 0xff) << 8));
                calendar.set(Calendar.MONTH, ((int) data[2] & 0xff) - 1);
                calendar.set(Calendar.DAY_OF_MONTH, (int) data[3] & 0xff);
                calendar.set(Calendar.HOUR_OF_DAY, (int) data[4] & 0xff);
                calendar.set(Calendar.MINUTE, (int) data[5] & 0xff);
                calendar.set(Calendar.SECOND, (int) data[6] & 0xff);
                return new Date(calendar.getTime().getTime() - iRoundtripCorrection);
            } catch (SEVCIEC1107ConnectionException e) {
                if (e.isReasonTimeout()) {
                    if (iRetries++ >= iProtocolRetriesProperty) {
                        throw new IOException("getMeterReading() error, " + e.getMessage());
                    }
                } else {
                    throw new IOException("getMeterReading() error, " + e.getMessage());
                }
            }
        }
    }

    @Override
    public String getSerialNumber() {
        String versionAndSerialNr;
        try {
            versionAndSerialNr = getFirmwareVersion();
            return versionAndSerialNr.substring(versionAndSerialNr.indexOf("EP"));
        } catch (IOException e) {
           throw ProtocolIOExceptionHandler.handle(e, iProtocolRetriesProperty + 1);
        }
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return Arrays.asList(
                UPLPropertySpecFactory.string(ADDRESS.getName(), false),
                UPLPropertySpecFactory.string(PASSWORD.getName(), false),
                UPLPropertySpecFactory.integer(TIMEOUT.getName(), false),
                UPLPropertySpecFactory.integer(RETRIES.getName(), false),
                UPLPropertySpecFactory.integer(ROUNDTRIPCORRECTION.getName(), false),
                UPLPropertySpecFactory.string(NODEID.getName(), false),
                UPLPropertySpecFactory.string(SERIALNUMBER.getName(), false),
                UPLPropertySpecFactory.integer("ForcedDelay", false));
    }

    @Override
    public void setProperties(TypedProperties properties) throws MissingPropertyException, InvalidPropertyException {
        try {
            strID = properties.getTypedProperty(ADDRESS.getName());
            strPassword = properties.getTypedProperty(PASSWORD.getName());
            iIEC1107TimeoutProperty = Integer.parseInt(properties.getTypedProperty(TIMEOUT.getName(), "10000").trim());
            iProtocolRetriesProperty = Integer.parseInt(properties.getTypedProperty(RETRIES.getName(), "3").trim());
            iRoundtripCorrection = Integer.parseInt(properties.getTypedProperty(ROUNDTRIPCORRECTION.getName(), "0").trim());
            nodeId = properties.getTypedProperty(NODEID.getName(), ""); // KV 13082003
            serialNumber = properties.getTypedProperty(SERIALNUMBER.getName());
            setForcedDelay(Integer.parseInt(properties.getTypedProperty("ForcedDelay", "0"))); // KV 27022006
        } catch (NumberFormatException e) {
            throw new InvalidPropertyException(e, this.getClass().getSimpleName() + ": validation of properties failed before");
        }

    }

    @Override
    public String getRegister(String name) throws IOException {

        if (name.compareTo("GET_CLOCK_OBJECT") == 0) {
            return null;
        } else {
            BigDecimal bd = (BigDecimal) getSEVCRegisterFactory().getValue(name, sevciec1107Connection);
            bd = BigDecimal.valueOf(Math.round((bd.movePointRight(SCALEFACTOR).doubleValue()))).movePointLeft(SCALEFACTOR);
            return (bd.toString());
        }
    }

    @Override
    public void setRegister(String name, String value) throws IOException {
        throw new UnsupportedException();
    }

    @Override
    public void initializeDevice() throws IOException {
        throw new UnsupportedException();
    }

    @Override
    public String getProtocolVersion() {
        return "$Date: 2015-11-26 15:24:25 +0200 (Thu, 26 Nov 2015)$";
    }

    @Override
    public String getFirmwareVersion() throws ProtocolException, NestedIOException, ConnectionException {
        try {
            ByteArrayOutputStream byteByffer = new ByteArrayOutputStream();
            sevciec1107Connection.sendReadFrame(SEVC_READ_VERSION);
            byte[] data = sevciec1107Connection.receiveData();
            for (int i = 0; i < data.length; i++) {
                if (data[i + 6] == 0) {
                    break;
                }
                byteByffer.write((int) data[i + 6]);
            }
            return byteByffer.toString();
        } catch (SEVCIEC1107ConnectionException e) {
            throw new ProtocolConnectionException("sevc: getFirmwareVersion(), IEC1107ConnectionException, " + e.getMessage(), e.getReason());
        }
    }

    @Override
    public String getSerialNumber(DiscoverInfo discoverInfo) throws IOException {
        SerialCommunicationChannel commChannel = discoverInfo.getCommChannel();
        TypedProperties properties = com.energyict.cpo.TypedProperties.empty();
        properties.setProperty(PASSWORD.getName(), "PASS");
        setProperties(properties);
        init(commChannel.getInputStream(), commChannel.getOutputStream(), null, null);
        enableHHUSignOn(commChannel);
        connect();
        String versionAndSerialNr = getFirmwareVersion();
        String serialNumber = versionAndSerialNr.substring(versionAndSerialNr.indexOf("EP"));
        disconnect();
        return serialNumber;
    }

    @Override
    public void init(InputStream inputStream, OutputStream outputStream, TimeZone timeZone, Logger logger) {
        this.timeZone = timeZone;
        this.logger = logger;
        bNROfChannels = 0;
        interval = 0;

        try {
            sevcRegisterFactory = new SEVCRegisterFactory();
            sevciec1107Connection = new SEVCIEC1107Connection(inputStream, outputStream, iIEC1107TimeoutProperty, iProtocolRetriesProperty, getForcedDelay());
        } catch (SEVCIEC1107ConnectionException e) {
            logger.severe("SEVC: init(...), " + e.getMessage());
        }
    }

    @Override
    public void connect() throws IOException {
        try {
            sevciec1107Connection.connectMAC(strID, strPassword, nodeId); // KV 13082003
        } catch (SEVCIEC1107ConnectionException e) {
            throw new IOException("connect() error, " + e.getMessage());
        }
    }

    @Override
    public void disconnect() {
        try {
            sevciec1107Connection.disconnectMAC();
        } catch (SEVCIEC1107ConnectionException e) {
            logger.severe("disconnect() error, " + e.getMessage());
        }
    }

    @Override
    public int getNumberOfChannels() throws IOException {
        if (bNROfChannels == 0) {
            bNROfChannels = SEVC_NR_OF_CHANNELS;
        }
        return bNROfChannels;
    }

    @Override
    public int getProfileInterval() throws IOException {
        if (interval == 0) {
            interval = getSEVCRegisterFactory().getValue("SAV", sevciec1107Connection).intValue() * 60;
        }
        return interval;
    }

    @Override
    public void release() throws IOException {
    }

    // KV 02022004
    @Override
    public void enableHHUSignOn(SerialCommunicationChannel commChannel) throws com.energyict.dialer.connection.ConnectionException {
        enableHHUSignOn(commChannel, false);
    }

    @Override
    public void enableHHUSignOn(SerialCommunicationChannel commChannel, boolean enableDataReadout) throws com.energyict.dialer.connection.ConnectionException {
        HHUSignOn hhuSignOn =
                new IEC1107HHUConnection(commChannel, iIEC1107TimeoutProperty, iProtocolRetriesProperty, 300, 0);
        hhuSignOn.setMode(HHUSignOn.MODE_MANUFACTURER_SPECIFIC_SEVCD);
        hhuSignOn.setProtocol(HHUSignOn.PROTOCOL_NORMAL);
        hhuSignOn.enableDataReadout(enableDataReadout);
        getSEVCIEC1107Connection().setHHUSignOn(hhuSignOn);
    }

    @Override
    public byte[] getHHUDataReadout() {
        return getSEVCIEC1107Connection().getHhuSignOn().getDataReadout();
    }

    public void enableHHUSignOn(com.energyict.dialer.core.StreamConnection streamConnection) throws com.energyict.dialer.connection.ConnectionException {
    }

    public void enableHHUSignOn(com.energyict.dialer.core.StreamConnection streamConnection, boolean enableDataReadout) throws com.energyict.dialer.connection.ConnectionException {
    }

    public int getForcedDelay() {
        return forcedDelay;
    }

    private void setForcedDelay(int forcedDelay) {
        this.forcedDelay = forcedDelay;
    }

}