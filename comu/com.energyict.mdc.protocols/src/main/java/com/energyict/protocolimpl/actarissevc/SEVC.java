package com.energyict.protocolimpl.actarissevc;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.dialer.connection.IEC1107HHUConnection;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.mdc.common.BaseUnit;
import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.HHUEnabler;
import com.energyict.mdc.protocol.api.InvalidPropertyException;
import com.energyict.mdc.protocol.api.MissingPropertyException;
import com.energyict.mdc.protocol.api.NoSuchRegisterException;
import com.energyict.mdc.protocol.api.SerialNumber;
import com.energyict.mdc.protocol.api.UnsupportedException;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.dialer.connection.ConnectionException;
import com.energyict.mdc.protocol.api.dialer.core.HHUSignOn;
import com.energyict.mdc.protocol.api.inbound.DiscoverInfo;
import com.energyict.mdc.protocol.api.legacy.MeterProtocol;
import com.energyict.mdc.protocol.api.legacy.dynamic.PropertySpecFactory;
import com.energyict.protocolimpl.base.PluggableMeterProtocol;
import com.energyict.protocols.util.ProtocolUtils;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.Logger;

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
public class SEVC extends PluggableMeterProtocol implements HHUEnabler, SerialNumber {


    @Override
    public String getProtocolDescription() {
        return "Actaris SEVCD IEC1107";
    }
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

    SEVCIEC1107Connection sevciec1107Connection = null;
    SEVCRegisterFactory sevcRegisterFactory = null;

    private int forcedDelay;

    @Inject
    public SEVC(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    protected SEVCRegisterFactory getSEVCRegisterFactory() {
        return sevcRegisterFactory;
    }

    private SEVCIEC1107Connection getSEVCIEC1107Connection() {
        return sevciec1107Connection;
    }

    public ProfileData getProfileData(boolean includeEvents) throws IOException {
        Calendar fromCalendar = ProtocolUtils.getCalendar(timeZone);
        fromCalendar.add(Calendar.YEAR, -10);
        return doGetProfileData(fromCalendar, ProtocolUtils.getCalendar(timeZone), includeEvents);
    }

    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        Calendar fromCalendar = ProtocolUtils.getCleanCalendar(timeZone);
        fromCalendar.setTime(lastReading);
        return doGetProfileData(fromCalendar, ProtocolUtils.getCalendar(timeZone), includeEvents);
    }

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

    } // private void doReadDatabase(int nrOfBlocks)

    private byte[] doReadDatabaseLogbook() throws IOException {
        try {
            sevciec1107Connection.sendReadFrame(SEVC_READ_TEVT);
            return (sevciec1107Connection.receiveSegmentedData(LOGBOOK_SIZE));
        } catch (SEVCIEC1107ConnectionException e) {
            throw new IOException("doReadDatabase() error, " + e.getMessage());
        }

    } // private byte[] doReadDatabaseLogbook()

    public Quantity getMeterReading(String name) throws IOException {
        throw new UnsupportedException("SEVC, using meterreading names is not supported!");
    }

    public Quantity getMeterReading(int channelId) throws IOException {
        Quantity quantity;
        quantity = new Quantity((BigDecimal) doGetMeterReading(channelId), SEVC_METERREADINGSUNITS[channelId]);
        return quantity;
    }

    public Number doGetMeterReading(int iChannelNr) throws IOException {
        if (strRegisters[iChannelNr] != null) {
            return getSEVCRegisterFactory().getValue(strRegisters[iChannelNr], getSEVCIEC1107Connection());
        } else {
            return null;
        }
    } // public Number doGetMeterReading(int iChannelNr) throws IOException

    public int getMeterReadingScale(int iChannelNr) throws IOException {
        return (SCALEFACTOR);
    }

    public byte getRecorderMemoryPage() throws IOException {
        throw new IOException("Not yet implemented!");
    }

    public short getNROfIntervals() throws IOException {
        throw new IOException("Not yet implemented!");
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
        doSetTime(calendar);
    } // public void setTime() throws IOException

    private void doSetTime(Calendar calendar) throws IOException {
        byte[] byteTimeBuffer = new byte[7];
        int i;
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

    } // private void doSetTime(Calendar calendar)


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

    public byte getLastProtocolState() {
        return -1;
    }

    /**
     * *********************************** MeterProtocol implementation **************************************
     */

    @Override
    public List<PropertySpec> getRequiredProperties() {
        return PropertySpecFactory.toPropertySpecs(getRequiredKeys(), this.getPropertySpecService());
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        return PropertySpecFactory.toPropertySpecs(getOptionalKeys(), this.getPropertySpecService());
    }

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
            iIEC1107TimeoutProperty = Integer.parseInt(properties.getProperty("Timeout", "10000").trim());
            iProtocolRetriesProperty = Integer.parseInt(properties.getProperty("Retries", "3").trim());
            iRoundtripCorrection = Integer.parseInt(properties.getProperty("RoundtripCorrection", "0").trim());
            nodeId = properties.getProperty(MeterProtocol.NODEID, ""); // KV 13082003
            serialNumber = properties.getProperty(MeterProtocol.SERIALNUMBER);
            setForcedDelay(Integer.parseInt(properties.getProperty("ForcedDelay", "0"))); // KV 27022006
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

        if (name.compareTo("GET_CLOCK_OBJECT") == 0) {
            return null;
        } else {
            BigDecimal bd = (BigDecimal) getSEVCRegisterFactory().getValue(name, sevciec1107Connection);
//            getSEVCRegisterFactory().init(name).getUnit();
            bd = BigDecimal.valueOf(Math.round((bd.movePointRight(SCALEFACTOR).doubleValue()))).movePointLeft(SCALEFACTOR);
            return (bd.toString());
        }
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
        throw new UnsupportedException();
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

    /**
     * the implementation returns both the address and password key
     *
     * @return a list of strings
     */
    public List<String> getRequiredKeys() {
        return new ArrayList<String>(0);
    }

    /**
     * this implementation returns an empty list
     *
     * @return a list of strings
     */
    public List<String> getOptionalKeys() {
        List<String> result = new ArrayList<String>(2);
        result.add("Timeout");
        result.add("Retries");
        result.add("ForcedDelay");
        return result;
    }

    public String getProtocolVersion() {
        return "$Date: 2013-10-31 11:22:19 +0100 (Thu, 31 Oct 2013) $";
    }

    public String getFirmwareVersion() throws IOException {
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
            throw new IOException("sevc: getFirmwareVersion(), IEC1107ConnectionException, " + e.getMessage());
        }
    } // public String getFirmwareVersion()

    // KV 15122003
    private void validateSerialNumber() throws IOException {
        boolean check = true;
        if ((serialNumber == null) || ("".compareTo(serialNumber) == 0)) {
            return;
        }

        String versionAndSerialNr = getFirmwareVersion();
        String sn = versionAndSerialNr.substring(versionAndSerialNr.indexOf("EP"));
        if (sn.compareTo(serialNumber) == 0) {
            return;
        }
        throw new IOException("SerialNumber mismatch! meter sn=" + sn + ", configured sn=" + serialNumber);
    }

    public String getSerialNumber(DiscoverInfo discoverInfo) throws IOException {
        SerialCommunicationChannel commChannel = discoverInfo.getCommChannel();
        Properties properties = new Properties();
        properties.setProperty(MeterProtocol.PASSWORD, "PASS");
        setProperties(properties);
        init(commChannel.getInputStream(), commChannel.getOutputStream(), null, null);
        enableHHUSignOn(commChannel);
        connect();
        String versionAndSerialNr = getFirmwareVersion();
        String serialNumber = versionAndSerialNr.substring(versionAndSerialNr.indexOf("EP"));
        disconnect();
        return serialNumber;
    }

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
        bNROfChannels = 0;
        interval = 0;

        try {
            sevcRegisterFactory = new SEVCRegisterFactory();
            sevciec1107Connection = new SEVCIEC1107Connection(inputStream, outputStream, iIEC1107TimeoutProperty, iProtocolRetriesProperty, getForcedDelay());
        } catch (SEVCIEC1107ConnectionException e) {
            logger.severe("SEVC: init(...), " + e.getMessage());
        }


    } // public void init(InputStream inputStream,OutputStream outputStream,TimeZone timeZone,Logger logger)

    /**
     * @throws IOException
     */
    public void connect() throws IOException {
        try {
            sevciec1107Connection.connectMAC(strID, strPassword, nodeId); // KV 13082003
        } catch (SEVCIEC1107ConnectionException e) {
            throw new IOException("connect() error, " + e.getMessage());
        }
        try {
            validateSerialNumber(); // KV 15122003
        } catch (IOException e) {
            disconnect();
            throw e;
        }

    }

    public void disconnect() {
        try {
            sevciec1107Connection.disconnectMAC();
        } catch (SEVCIEC1107ConnectionException e) {
            logger.severe("disconnect() error, " + e.getMessage());
        }
    }


    public int getNumberOfChannels() throws IOException {
        if (bNROfChannels == 0) {
            bNROfChannels = SEVC_NR_OF_CHANNELS;
        }
        return bNROfChannels;
    }

    public int getProfileInterval() throws IOException {
        if (interval == 0) {
            interval = getSEVCRegisterFactory().getValue("SAV", sevciec1107Connection).intValue() * 60;
        }
        return interval;
    }

    public Object getCache() {
        return null;
    }

    public Object fetchCache(int rtuid){
        return null;
    }

    public void setCache(Object cacheObject) {
    }

    public void updateCache(int rtuid, Object cacheObject) {
    }

    public void release() throws IOException {
    }

    // KV 02022004
    public void enableHHUSignOn(SerialCommunicationChannel commChannel) throws ConnectionException {
        enableHHUSignOn(commChannel, false);
    }

    public void enableHHUSignOn(SerialCommunicationChannel commChannel, boolean enableDataReadout) throws ConnectionException {
        HHUSignOn hhuSignOn =
                new IEC1107HHUConnection(commChannel, iIEC1107TimeoutProperty, iProtocolRetriesProperty, 300, 0);
        hhuSignOn.setMode(HHUSignOn.MODE_MANUFACTURER_SPECIFIC_SEVCD);
        hhuSignOn.setProtocol(HHUSignOn.PROTOCOL_NORMAL);
        hhuSignOn.enableDataReadout(enableDataReadout);
        getSEVCIEC1107Connection().setHHUSignOn(hhuSignOn);
    }

    public byte[] getHHUDataReadout() {
        return getSEVCIEC1107Connection().getHhuSignOn().getDataReadout();
    }

    public int getForcedDelay() {
        return forcedDelay;
    }

    private void setForcedDelay(int forcedDelay) {
        this.forcedDelay = forcedDelay;
    }

}
