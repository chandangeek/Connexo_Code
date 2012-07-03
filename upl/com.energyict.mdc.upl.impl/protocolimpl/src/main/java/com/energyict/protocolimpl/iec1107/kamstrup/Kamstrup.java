/*
 * Kamstrup.java
 *
 * Created on 8 mei 2003, 17:56
 */

package com.energyict.protocolimpl.iec1107.kamstrup;

import com.energyict.cbo.Quantity;
import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;
import com.energyict.protocolimpl.base.PluggableMeterProtocol;
import com.energyict.protocolimpl.base.ProtocolChannelMap;
import com.energyict.protocolimpl.iec1107.*;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;

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
public class Kamstrup extends PluggableMeterProtocol implements ProtocolLink, RegisterProtocol { //,CommunicationParameters {

    private static final byte DEBUG = 0;

    private static final int KAMSTRUP_NR_OF_CHANNELS = 6;
    private static final String[] KAMSTRUP_METERREADINGS_979D1 = {"23.2.0", "13.1.0", "1:13.0.0", "0:41.0.0", "0:42.0.0", "97.97.0"};
    private static final String[] KAMSTRUP_METERREADINGS_979E1 = {"23.2.0", "1:12.0.0", "1:13.0.0", "0:41.0.0", "0:42.0.0", "97.97.0"};
    private static final String[] KAMSTRUP_METERREADINGS_979A1 = {"23.2.0", "13.1.0", "1:13.0.0", "0:41.0.0", "0:42.0.0", "97.97.0"};
    private static final String[] KAMSTRUP_METERREADINGS_DEFAULT = {"23.2.0", "13.1.0", "1:13.0.0", "0:41.0.0", "0:42.0.0", "97.97.0"};

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

    FlagIEC1107Connection flagIEC1107Connection = null;
    KamstrupRegistry kamstrupRegistry = null;
    KamstrupProfile kamstrupProfile = null;
    int extendedLogging;

    byte[] dataReadout = null;

    /**
     * Creates a new instance of ABBA1500, empty constructor
     */
    public Kamstrup() {
    } // public Kamstrup()

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
        return getKamstrupProfile().getProfileData(fromCalendar,
                toCalendar,
                getNumberOfChannels(),
                1);
    }

    private ProfileData doGetProfileData(Date lastReading, boolean includeEvents) throws IOException {
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

    public Quantity getMeterReading(String name) throws UnsupportedException, IOException {
        try {
            return (Quantity) getKamstrupRegistry().getRegister(name);
        } catch (ClassCastException e) {
            throw new IOException("Kamstrup, getMeterReading, register " + name + " is not type Quantity");
        }
    }

    public Quantity getMeterReading(int channelId) throws UnsupportedException, IOException {
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
        getKamstrupRegistry().setRegister("0.9.1", date);
        getKamstrupRegistry().setRegister("0.9.2", date);
    } // public void setTime() throws IOException

    public Date getTime() throws IOException {
        Date date = (Date) getKamstrupRegistry().getRegister("TimeDate");
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
            extendedLogging = Integer.parseInt(properties.getProperty("ExtendedLogging", "0").trim());
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
        return ProtocolUtils.obj2String(getKamstrupRegistry().getRegister(name));
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
        getKamstrupRegistry().setRegister(name, value);
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
        result.add("ExtendedLogging");
        result.add("Software7E1");
        return result;
    }

    public String getProtocolVersion() {
        return "$Date$";
    }

    public String getFirmwareVersion() throws IOException, UnsupportedException {
        try {
            return ((String) getKamstrupRegistry().getRegister("CI software revision number") + " " + (String) getKamstrupRegistry().getRegister("UNIGAS software revision number"));
        } catch (IOException e) {
            throw new IOException("Kamstrup, getFirmwareVersion, " + e.getMessage());
        }
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

//        	if (isSoftware7E1()) {
//        		Software7E1InputStream softIn = new Software7E1InputStream(inputStream);
//        		Software7E1OutputStream softOut = new Software7E1OutputStream(outputStream);
//        		flagIEC1107Connection=new FlagIEC1107Connection(softIn,softOut,iIEC1107TimeoutProperty,iProtocolRetriesProperty,0,iEchoCancelling,iIEC1107Compatible);
//        	} else {
//            	flagIEC1107Connection=new FlagIEC1107Connection(inputStream,outputStream,iIEC1107TimeoutProperty,iProtocolRetriesProperty,0,iEchoCancelling,iIEC1107Compatible);
//        	}
            flagIEC1107Connection = new FlagIEC1107Connection(inputStream, outputStream, iIEC1107TimeoutProperty, iProtocolRetriesProperty, 0, iEchoCancelling, iIEC1107Compatible, software7E1, logger);
            kamstrupRegistry = new KamstrupRegistry(this);
            kamstrupProfile = new KamstrupProfile(this, kamstrupRegistry);
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

            registerInfo();
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
        return KAMSTRUP_NR_OF_CHANNELS;
    }

    public int getProfileInterval() throws UnsupportedException, IOException {
        return iProfileInterval;
    }

    public KamstrupRegistry getKamstrupRegistry() {
        return kamstrupRegistry;
    }

    private KamstrupProfile getKamstrupProfile() {
        return kamstrupProfile;
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
        return null;
    }

    public ProtocolChannelMap getProtocolChannelMap() {
        return null;
    }

    public void release() throws IOException {
    }

    public Logger getLogger() {
        return logger;
    }

    public int getNrOfRetries() {
        return iProtocolRetriesProperty;
    }

    public boolean isRequestHeader() {
        return false;
    }

    private void registerInfo() {
        ObisCodeMapper ocm = new ObisCodeMapper(this);
        if (extendedLogging >= 1) {
            logger.info(ocm.getRegisterInfo());
        }
    }

    // RegisterProtocol Interface implementation
    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        return ObisCodeMapper.getRegisterInfo(obisCode);
    }

    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        ObisCodeMapper ocm = new ObisCodeMapper(this);
        return ocm.getRegisterValue(obisCode);
    }

} // public class Kamstrup implements MeterProtocol {
