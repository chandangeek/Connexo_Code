/*
 * Ferranti.java
 *
 * Created on 04 mei 2004, 10:00
 */

package com.energyict.protocolimpl.iec1107.ferranti;

import com.energyict.cbo.Quantity;
import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dialer.core.*;
import com.energyict.protocol.*;
import com.energyict.protocolimpl.base.ProtocolChannelMap;
import com.energyict.protocolimpl.iec1107.*;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;

/**
 * @author Koenraad Vanderschaeve
 *         <p/>
 *         <B>Description :</B><BR>
 *         Class that implements the Ferranti meter protocol.
 *         <BR>
 *         <B>@beginchanges</B><BR>
 *         KV|04052004|Initial version
 *         KV|30032005|Handle StringOutOfBoundException in IEC1107 connection layer
 *         KV|06092005|VDEW changed to do channel mapping!
 * @version 1.0
 * @endchanges
 */
public class Ferranti implements MeterProtocol, ProtocolLink, MeterExceptionInfo {

    private static final byte DEBUG = 0;

    private static final int FERRANTI_NR_OF_PROFILE_CHANNELS = 3;
    private static final int FERRANTI_NR_OF_METERREADINGS = 4;
    private static final String[] FERRANTI_METERREADINGS = {"7-0:23.0.0*101", "7-0:23.2.0*101", "7-0:97.97.0*101", "7-0:0.1.2*101"};

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
    private ChannelMap channelMap;

    private TimeZone timeZone;
    private Logger logger;

    FlagIEC1107Connection flagIEC1107Connection = null;
    FerrantiRegistry ferrantiRegistry = null;
    FerrantiProfile ferrantiProfile = null;

    byte[] dataReadout = null;

    private boolean software7E1;

    /**
     * Creates a new instance of Ferranti, empty constructor
     */
    public Ferranti() {
    } // public Ferranti()

    public ProfileData getProfileData(boolean includeEvents) throws IOException {
        Calendar calendar = ProtocolUtils.getCalendar(timeZone);
        calendar.add(Calendar.YEAR, -10);
        return doGetProfileData(calendar.getTime(), includeEvents);
    }

    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        return doGetProfileData(lastReading, includeEvents);
    }

    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException, UnsupportedException {
        Calendar fromCalendar = ProtocolUtils.getCleanCalendar(timeZone);
        fromCalendar.setTime(from);
        Calendar toCalendar = ProtocolUtils.getCleanCalendar(timeZone);
        toCalendar.setTime(to);
        return getFerrantiProfile().getProfileData(fromCalendar,
                toCalendar,
                getNumberOfChannels());
    }

    private ProfileData doGetProfileData(Date lastReading, boolean includeEvents) throws IOException {
        Calendar from = ProtocolUtils.getCleanCalendar(timeZone);
        from.setTime(lastReading);
        return getFerrantiProfile().getProfileData(from,
                ProtocolUtils.getCalendar(timeZone),
                getNumberOfChannels());
    }

    // Only for debugging
    public ProfileData getProfileData(Calendar from, Calendar to) throws IOException {
        return getFerrantiProfile().getProfileData(from,
                to,
                getNumberOfChannels());
    }

    public Quantity getMeterReading(String name) throws UnsupportedException, IOException {
        try {
            return (Quantity) getFerrantiRegistry().getRegister(name);
        } catch (ClassCastException e) {
            throw new IOException("Ferranti, getMeterReading, register " + name + " is not type Quantity");
        }
    }

    public Quantity getMeterReading(int channelId) throws UnsupportedException, IOException {
        try {
            if (channelId >= FERRANTI_NR_OF_METERREADINGS) {
                throw new IOException("Ferranti, getMeterReading, invalid channelId, " + channelId);
            }
            return (Quantity) getFerrantiRegistry().getRegister(FERRANTI_METERREADINGS[channelId]);
        } catch (ClassCastException e) {
            throw new IOException("Ferranti, getMeterReading, register " + FERRANTI_METERREADINGS[channelId] + " (" + channelId + ") is not type Quantity");
        }
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
        Date date = calendar.getTime();
        getFerrantiRegistry().setRegister("Time in the device", date);
    } // public void setTime() throws IOException

    public Date getTime() throws IOException {
        Date date = (Date) getFerrantiRegistry().getRegister("Time in the device");
        return new Date(date.getTime() - iRoundtripCorrection);
    }

    public byte getLastProtocolState() {
        return -1;
    }

    /************************************** MeterProtocol implementation ***************************************/

    /**
     * this implementation calls <code> validateProperties </code>
     * and assigns the argument to the properties field
     *
     * @param properties <br>
     * @throws MissingPropertyException <br>
     * @throws InvalidPropertyException <br>
     * @see AbstractMeterProtocol#validateProperties
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
            iIEC1107TimeoutProperty = Integer.parseInt(properties.getProperty("Timeout", "20000").trim());
            iProtocolRetriesProperty = Integer.parseInt(properties.getProperty("Retries", "5").trim());
            iRoundtripCorrection = Integer.parseInt(properties.getProperty("RoundtripCorrection", "0").trim());
            iSecurityLevel = Integer.parseInt(properties.getProperty("SecurityLevel", "1").trim());
            nodeId = properties.getProperty(MeterProtocol.NODEID, "");
            iEchoCancelling = Integer.parseInt(properties.getProperty("EchoCancelling", "0").trim());
            iIEC1107Compatible = Integer.parseInt(properties.getProperty("IEC1107Compatible", "1").trim());
            iProfileInterval = Integer.parseInt(properties.getProperty("ProfileInterval", "3600").trim());
            channelMap = new ChannelMap(properties.getProperty("ChannelMap", "0"));
            this.software7E1 = !properties.getProperty("Software7E1", "0").equalsIgnoreCase("0");
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
    public String getRegister(String name) throws IOException, UnsupportedException, NoSuchRegisterException {
        return ProtocolUtils.obj2String(getFerrantiRegistry().getRegister(name));
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
    public void setRegister(String name, String value) throws IOException, NoSuchRegisterException, UnsupportedException {
        getFerrantiRegistry().setRegister(name, value);
    }

    /**
     * this implementation throws UnsupportedException. Subclasses may override
     *
     * @throws IOException          <br>
     * @throws UnsupportedException <br>
     */
    public void initializeDevice() throws IOException, UnsupportedException {
        throw new UnsupportedException();
    }

    @Override
    public List<PropertySpec> getRequiredProperties() {
        return PropertySpecFactory.toPropertySpecs(getRequiredKeys());
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        return PropertySpecFactory.toPropertySpecs(getOptionalKeys());
    }

    /**
     * the implementation returns both the address and password key
     *
     * @return a list of strings
     */
    public List getRequiredKeys() {
        List result = new ArrayList(0);
        return result;
    }

    /**
     * this implementation returns an empty list
     *
     * @return a list of strings
     */
    public List getOptionalKeys() {
        List result = new ArrayList();
        result.add("Timeout");
        result.add("Retries");
        result.add("SecurityLevel");
        result.add("EchoCancelling");
        result.add("IEC1107Compatible");
        result.add("ChannelMap");
        result.add("Software7E1");
        return result;
    }

    public String getProtocolVersion() {
        return "$Date$";
    }

    public String getFirmwareVersion() throws IOException, UnsupportedException {
        return ("Unknown");
    } // public String getFirmwareVersion()

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

        try {
            flagIEC1107Connection = new FlagIEC1107Connection(inputStream, outputStream, iIEC1107TimeoutProperty, iProtocolRetriesProperty, 0, iEchoCancelling, iIEC1107Compatible, software7E1, logger);
            ferrantiRegistry = new FerrantiRegistry(this, this);
            ferrantiProfile = new FerrantiProfile(this, this, ferrantiRegistry);
        } catch (ConnectionException e) {
            logger.severe("ABBA1500: init(...), " + e.getMessage());
        }

    } // public void init(InputStream inputStream,OutputStream outputStream,TimeZone timeZone,Logger logger)

    /**
     * @throws IOException
     */
    public void connect() throws IOException {
        try {
            dataReadout = flagIEC1107Connection.dataReadout(strID, nodeId);
            flagIEC1107Connection.disconnectMAC();
            /*   try {
                Thread.sleep(2000);
            }
            catch(InterruptedException e) {
                throw new NestedIOException(e);
            }*/
            flagIEC1107Connection.connectMAC(strID, strPassword, iSecurityLevel, nodeId);
        } catch (FlagIEC1107ConnectionException e) {
            throw new IOException(e.getMessage());
        }
    }

    public void disconnect() throws IOException {
        try {
            flagIEC1107Connection.disconnectMAC();
        } catch (FlagIEC1107ConnectionException e) {
            logger.severe("disconnect() error, " + e.getMessage());
        }
    }

    public int getNumberOfChannels() throws UnsupportedException, IOException {
        return FERRANTI_NR_OF_PROFILE_CHANNELS;
    }

    public int getProfileInterval() throws UnsupportedException, IOException {
        return iProfileInterval;
    }

    private FerrantiRegistry getFerrantiRegistry() {
        return ferrantiRegistry;
    }

    private FerrantiProfile getFerrantiProfile() {
        return ferrantiProfile;
    }

    public static void main(String[] args) {

        Dialer dialer = null;
        Ferranti ferranti = null;
        try {
            dialer = DialerFactory.getDefault().newDialer();
            dialer.init("COM1", "AT+CBST=71,0,1");
            dialer.connect("0031652363361", 60000);
            InputStream is = dialer.getInputStream();
            OutputStream os = dialer.getOutputStream();
            ferranti = new Ferranti();
            Properties properties = new Properties();
            properties.setProperty(MeterProtocol.ADDRESS, "/KAM500000006723");
            properties.setProperty(MeterProtocol.PASSWORD, "00000000");
            properties.setProperty("Retries", "5");
            properties.setProperty("ProfileInterval", "3600");
            ferranti.setProperties(properties);


            dialer.getSerialCommunicationChannel().setParamsAndFlush(9600,
                    SerialCommunicationChannel.DATABITS_7,
                    SerialCommunicationChannel.PARITY_EVEN,
                    SerialCommunicationChannel.STOPBITS_1);

            // initialize
            ferranti.init(is, os, TimeZone.getTimeZone("ECT"), Logger.getLogger("name"));

            System.out.println("Start session");

            // connect
            ferranti.connect();

            if (DEBUG >= 1) {
                if (ferranti.getDataReadout() != null) {
                    System.out.println(new String(ferranti.getDataReadout()));
                }
            }

            // get/set time
            System.out.println(ferranti.getTime());
            ferranti.setTime();

            // get registers
            System.out.println(ProtocolUtils.obj2String(ferranti.getRegister("97.97.0")));

            // get meterreadings
            for (int i = 0; i < ferranti.FERRANTI_NR_OF_METERREADINGS; i++) {
                System.out.println(ferranti.getMeterReading(i).toString());
            }
            System.out.println(ferranti.getMeterReading("0:41.0.0").toString());

            // read profile
            Calendar from = Calendar.getInstance(ferranti.getTimeZone());
            from.add(Calendar.DAY_OF_MONTH, -1);
            Calendar to = Calendar.getInstance(ferranti.getTimeZone());
            to.add(Calendar.MONTH, +6);

            //ferranti.getFerrantiProfile().doLogMeterDataCollection(ferranti.getProfileData(from.getTime(),true));

            ferranti.getFerrantiProfile().doLogMeterDataCollection(ferranti.getProfileData(from, to));
            // disconnect
            ferranti.disconnect();

            System.out.println("End session");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                ferranti.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Implementation of interface ProtocolLink
    public FlagIEC1107Connection getFlagIEC1107Connection() {
        return flagIEC1107Connection;
    }

    public TimeZone getTimeZone() {
        return timeZone;
    }

    public boolean isIEC1107Compatible() {
        return (iIEC1107Compatible == 1);
    }

    public String getPassword() {
        return strPassword;
    }

    public byte[] getDataReadout() {
        return dataReadout;
    }

    public Object getCache() {
        return null;
    }

    public Object fetchCache(int rtuid) throws java.sql.SQLException, com.energyict.cbo.BusinessException {
        return null;
    }

    public void setCache(Object cacheObject) {
    }

    public void updateCache(int rtuid, Object cacheObject) throws java.sql.SQLException, com.energyict.cbo.BusinessException {
    }

    public ChannelMap getChannelMap() {
        return channelMap;
    }

    public ProtocolChannelMap getProtocolChannelMap() {
        return null;
    }

    public void release() throws IOException {
    }

    public Logger getLogger() {
        return logger;
    }

    // KV 17022004 implementation of MeterExceptionInfo
    static Map exceptionInfoMap = new HashMap();

    static {
        exceptionInfoMap.put("#ERR00001", "Unknown OBIS code");
        exceptionInfoMap.put("#ERR00002", "Unknown Read Command");
        exceptionInfoMap.put("#ERR00003", "Unknown Write Command");
        exceptionInfoMap.put("#ERR00004", "Bad VHI Id");
        exceptionInfoMap.put("#ERR00005", "Invalid load profile request");

    }

    public String getExceptionInfo(String id) {
        String exceptionInfo = (String) exceptionInfoMap.get(id);
        if (exceptionInfo != null) {
            return id + ", " + exceptionInfo;
        } else {
            return "No meter specific exception info for " + id;
        }
    }

    public int getNrOfRetries() {
        return iProtocolRetriesProperty;
    }

    public boolean isRequestHeader() {
        return false;
    }

} // public class Ferranti implements MeterProtocol {
