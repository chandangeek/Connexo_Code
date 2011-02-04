package com.energyict.protocolimpl.sdksample;

import com.energyict.cbo.BusinessException;
import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;
import com.energyict.protocolimpl.base.ParseUtils;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * Date: 17-jan-2011
 * Time: 15:14:31
 */
public class SDKSampleExtendedProtocol implements SmartMeterProtocol {

    /**
     * The used Logger
     */
    private Logger logger;
    /**
     * The used timeZone
     */
    private TimeZone timeZone;

    /**
     * Contains a list of <CODE>LoadProfileConfiguration</CODE> objects which corresponds with the LoadProfiles in the METER
     */
    private List<LoadProfileConfiguration> loadProfileConfigurationList;

    /**
     * The used <CODE>Connection</CODE> class
     */
    private SDKSampleProtocolConnection connection;

    private boolean simulateRealCommunication = false;

    /**
     * Constructor ...
     */
    public SDKSampleExtendedProtocol() {

    }

    /**
     * <p>
     * Initializes the MeterProtocol.
     * </p><p>
     * Implementers should save the arguments for future use.
     * </p><p>
     * All times exchanged between the data collection system and a MeterProtocol are java.util.Date ,
     * expressed in milliseconds since 1/1/1970 in UTC. The implementer has
     * to convert the device times to UTC. </p><p>
     * The timeZone argument is the timezone that is configured in the collecting system
     * for the device. If the device knows its own timezone, this argument can be ignored </p><p>
     * Implementers can use the argument to convert from device
     * time format to java.util.Date, e.g.</p>
     * <PRE>
     * Calendar deviceCalendar = Calendar.getInstance(timeZone);
     * deviceCalendar.clear();
     * deviceCalendar.set(year, month - 1 , day , hour , minute , second);
     * java.util.Date deviceDate = deviceCalendar.getTime();
     * </PRE>
     * <p>
     * The last argument is used to inform the data collection system of problems and/or progress.
     * Messages with level INFO or above are logged to the collection system's
     * logbook. Messages with level below INFO are only displayed in diagnostic mode
     * </p>
     *
     * @param inputStream  byte stream to read data from the device
     * @param outputStream byte stream to send data to the device
     * @param timeZone     the device's timezone
     * @param logger       used to provide feedback to the collection system
     * @throws java.io.IOException Thrown when an exception happens
     */
    public void init(InputStream inputStream, OutputStream outputStream, TimeZone timeZone, Logger logger) throws IOException {
        this.logger = logger;
        this.timeZone = timeZone;
        this.connection = new SDKSampleProtocolConnection(inputStream, outputStream, 100, 2, 50, 0, 1, null, getLogger());
    }

    /**
     * <p>
     * Sets the protocol specific properties.
     * </p><p>
     * This method can also be called at device configuration time to check the validity of
     * the configured values </p><p>
     * The implementer has to specify which keys are mandatory,
     * and which are optional. Convention is to use lower case keys.</p><p>
     * Typical keys are: <br>
     * "address"  (MeterProtocol.ADDRESS) <br>
     * "password"  (MeterProtocol.PASSWORD) </p>
     *
     * @param properties contains a set of protocol specific key value pairs
     * @throws com.energyict.protocol.InvalidPropertyException
     *          if a property value is not compatible with the device type
     * @throws com.energyict.protocol.MissingPropertyException
     *          if a required property is not present
     */
    public void setProperties(Properties properties) throws InvalidPropertyException, MissingPropertyException {
        try {
            for (String key : getRequiredKeys()) {
                if (properties.getProperty(key) == null) {
                    throw new MissingPropertyException(key + " key missing");
                }
            }
            this.simulateRealCommunication = properties.getProperty("SimulateRealCommunication", "0").trim().equalsIgnoreCase("1");
        }
        catch (NumberFormatException e) {
            throw new InvalidPropertyException(" validateProperties, NumberFormatException, " + e.getMessage());
        }
    }

    /**
     * <p>
     * Sets up the logical connection with the device.
     * </p><p>
     * As the physical connection has already been setup by the collection system,
     * it is up to the implementer to decide if any additional implementation is needed
     * </p>
     *
     * @throws java.io.IOException <br>
     */
    public void connect() throws IOException {
        getLogger().info("call abstract method doConnect()");
        getLogger().info("--> at that point, we have a communicationlink with the meter (modem, direct, optical, ip, ...)");
        getLogger().info("--> here the login and other authentication and setup should be done");
        doGenerateCommunication();
    }

    /**
     * Terminates the logical connection with the device.
     * The implementer should not close the inputStream and outputStream. This
     * is the responsibility of the collection system
     *
     * @throws java.io.IOException thrown in case of an exception
     */
    public void disconnect() throws IOException {
        getLogger().info("call abstract method doDisConnect()");
        getLogger().info("--> here the logoff should be done");
        getLogger().info("--> after that point, we will close the communicationlink with the meter");
        doGenerateCommunication();
    }

    private SDKSampleProtocolConnection getConnection() {
        return connection;
    }

    private void doGenerateCommunication() {
        doGenerateCommunication(1000, null);
    }

    private void doGenerateCommunication(long delay, String value) {
        if (isSimulateRealCommunication()) {
            ProtocolTools.delay(delay);
            byte[] bytes;
            if (value == null) {
                StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
                bytes = stackTrace[4].getMethodName().getBytes();
            } else {
                bytes = value.getBytes();
            }
            getConnection().write(bytes);
        }
    }

    public boolean isSimulateRealCommunication() {
        return simulateRealCommunication;
    }

    /**
     * Getter for the framework Logger object
     *
     * @return Logger object
     */
    public Logger getLogger() {
        return logger;
    }

    /**
     * @return the version of the specific protocol implementation
     */
    public String getProtocolVersion() {
        String rev = "$Revision: 43720 $" + " - " + "$Date: 2010-10-11 13:43:52 +0200 (ma, 11 okt 2010) $";
        String manipulated = "Revision " + rev.substring(rev.indexOf("$Revision: ") + "$Revision: ".length(), rev.indexOf("$ -")) + "at "
                + rev.substring(rev.indexOf("$Date: ") + "$Date: ".length(), rev.indexOf("$Date: ") + "$Date: ".length() + 19);
        return manipulated;
    }

    /**
     * Get the firmware version of the meter
     *
     * @return the version of the meter firmware
     *         </p>
     * @throws java.io.IOException Thrown in case of an exception
     * @throws com.energyict.protocol.UnsupportedException
     *                             Thrown if method is not supported
     */
    public String getFirmwareVersion() throws IOException {
        getLogger().info("call getFirmwareVersion()");
        getLogger().info("--> report the firmware version and other important meterinfo here");
        return "SDK MultipleLoadProfile Sample firmware version";
    }

    /**
     * <p></p>
     *
     * @return the current device time
     * @throws java.io.IOException <br>
     */
    public Date getTime() throws IOException {
        getLogger().info("call getTime() (if time is different from system time taken into account the properties, setTime will be called) ");
        getLogger().info("--> request the metertime here");
        long currenttime = new Date().getTime();
        return new Date(currenttime - (1000 * 15));
    }

    /**
     * <p>
     * sets the device time to the current system time.
     * </p><p>
     *
     * @throws java.io.IOException Thrown in case of an exception
     */
    public void setTime() throws IOException {
        getLogger().info("call setTime() (this method is called automatically when needed)");
        getLogger().info("--> sync the metertime with the systemtime here");
    }

    /**
     * <p>
     * Initializes the device, typically clearing all profile data
     * </p>
     *
     * @throws java.io.IOException <br>
     * @throws com.energyict.protocol.UnsupportedException
     *                             if the device does not support this operation
     */
    public void initializeDevice() throws UnsupportedException, IOException {
        //TODO implement proper functionality.
    }

    /**
     * Set the cache object. the object itself is an implementation of a protocol
     * specific cache object representing persistent data to be used with the protocol.
     *
     * @param cacheObject a protocol specific cache object
     */
    public void setCache(Object cacheObject) {
        //TODO implement proper functionality.
    }

    /**
     * Returns the protocol specific cache object from the meter protocol implementation.
     *
     * @return the protocol specific cache object
     */
    public Object getCache() {
        return null;  //TODO implement proper functionality.
    }

    /**
     * Fetch the protocol specific cache object from the database.
     *
     * @param rtuid Database ID of the RTU
     * @return the protocol specific cache object
     * @throws java.sql.SQLException Thrown in case of an SQLException
     * @throws com.energyict.cbo.BusinessException
     *                               Thrown in case of an BusinessException
     */
    public Object fetchCache(int rtuid) throws SQLException, BusinessException {
        return null;  //TODO implement proper functionality.
    }

    /**
     * Update the protocol specific cach object information in the database.
     *
     * @param rtuid       Database ID of the RTU
     * @param cacheObject the protocol specific cach object
     * @throws java.sql.SQLException Thrown in case of an SQLException
     * @throws com.energyict.cbo.BusinessException
     *                               Thrown in case of an BusinessException
     */
    public void updateCache(int rtuid, Object cacheObject) throws SQLException, BusinessException {
        //TODO implement proper functionality.
    }

    /**
     * This method is called by the collection software before the disconnect()
     * and can be used to free resources that cannot be freed in the disconnect()
     * method.
     *
     * @throws java.io.IOException Thrown in case of an exception
     */
    public void release() throws IOException {
        //TODO implement proper functionality.
    }

    /**
     * Returns a list of required property keys
     *
     * @return a List of String objects
     */
    public List<String> getRequiredKeys() {
        return new ArrayList<String>();  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Returns a list of optional property keys
     *
     * @return a List of String objects
     */
    public List<String> getOptionalKeys() {
        List<String> result = new ArrayList();
        result.add("SimulateRealCommunication");
        return result;
    }

    /**
     * Get the configuration(interval, number of channels, channelUnits) of all given LoadProfiles from the meter.
     * Build up a list of {@link com.energyict.protocol.LoadProfileConfiguration} objects and return them so the
     * framework can validate them to the configuration in EIServer
     *
     * @param loadProfileObisCodes the list of LoadProfile ObisCodes
     * @return a list of {@link com.energyict.protocol.LoadProfileConfiguration} objects corresponding with the meter
     */
    public List<LoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfileObisCodes) {

        loadProfileConfigurationList = new ArrayList<LoadProfileConfiguration>();

        LoadProfileConfiguration lpc = new LoadProfileConfiguration(loadProfileObisCodes.get(0).getProfileObisCode(), loadProfileObisCodes.get(0).getMeterSerialNumber());
        Map<ObisCode, Unit> channelUnitMap = new HashMap<ObisCode, Unit>();
        channelUnitMap.put(ObisCode.fromString("1.0.1.8.1.255"), Unit.get("kWh"));
        channelUnitMap.put(ObisCode.fromString("1.0.1.8.2.255"), Unit.get("kWh"));
        channelUnitMap.put(ObisCode.fromString("1.0.2.8.1.255"), Unit.get("kWh"));
        channelUnitMap.put(ObisCode.fromString("1.0.2.8.2.255"), Unit.get("kWh"));
        channelUnitMap.put(ObisCode.fromString("0.x.24.2.1.255"), Unit.get("m3"));
        channelUnitMap.put(ObisCode.fromString("0.x.24.2.1.255"), Unit.get("m3"));
        lpc.setChannelUnits(channelUnitMap);
        lpc.setProfileInterval(86400);
        loadProfileConfigurationList.add(lpc);

        lpc = new LoadProfileConfiguration(loadProfileObisCodes.get(1).getProfileObisCode(), loadProfileObisCodes.get(1).getMeterSerialNumber());
        lpc.setProfileInterval(0); //we set 0 as interval because the monthly profile has an asynchronous capture period

        // we use the same channelMap ...
        lpc.setChannelUnits(channelUnitMap);
        loadProfileConfigurationList.add(lpc);

        return loadProfileConfigurationList;
    }

    /**
     * <p>
     * Fetches one or more LoadProfiles from the device.
     * </p>
     * <p>
     * <b>Implementors should throw an exception if all data since {@link com.energyict.protocol.LoadProfileReader#getStartReadingTime()} can NOT be fetched</b>,
     * as the collecting system will update its lastReading setting based on the returned ProfileData
     * </p>
     *
     * @param loadProfiles a list of {@link com.energyict.protocol.LoadProfileReader}s which have to be read
     * @return a list of {@link com.energyict.protocol.ProfileData}s containing interval records
     * @throws java.io.IOException if a communication or parsing error occurred
     */
    public List<ProfileData> getLoadProfileData(List<LoadProfileReader> loadProfiles) throws IOException {
        List<ProfileData> profileDataList = new ArrayList<ProfileData>();
        for (LoadProfileReader loadProfile : loadProfiles) {
            profileDataList.add(getRawProfileData(loadProfile));
        }

        return profileDataList;
    }

    /**
     * Do the actual reading of the loadProfile data from the Meter
     *
     * @param lpro the identification of which LoadProfile to read
     * @return a {@link com.energyict.protocol.ProfileData} object with the necessary intervals filled in.
     * @throws IOException
     */
    private ProfileData getRawProfileData(LoadProfileReader lpro) throws IOException {

        LoadProfileConfiguration lpc = getLoadProfileConfigurationForGivenReadObject(lpro);
        int timeInterval = Calendar.SECOND;
        int timeDuration = lpc.getProfileInterval();

        List<ChannelInfo> channelInfoList = new ArrayList<ChannelInfo>();
        int channelCounter = 0;
        for (Map.Entry<ObisCode, Unit> entry : lpc.getChannelUnits().entrySet()) {
            channelInfoList.add(new ChannelInfo(channelCounter, channelCounter, entry.getKey().toString(), entry.getValue()));
            channelCounter++;
        }

        ProfileData pd = new ProfileData(lpro.getLoadProfileId());
        pd.setChannelInfos(channelInfoList);

        Calendar cal = Calendar.getInstance(this.timeZone);
        cal.setTime(lpro.getStartReadingTime());


        if (timeDuration == 0) { //monthly
            cal.set(Calendar.DAY_OF_MONTH, 1);
            cal.set(Calendar.HOUR, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);

            timeInterval = Calendar.MONTH;
            timeDuration = 1;
        } else {
            ParseUtils.roundDown2nearestInterval(cal, lpc.getProfileInterval());
        }

        Calendar currentCal = Calendar.getInstance();

        String outputData = "";
        while (cal.getTime().before(currentCal.getTime())) {
            IntervalData id = new IntervalData(cal.getTime());

            for (int i = 0; i < lpc.getNumberOfChannels(); i++) {
                id.addValue(new BigDecimal(10 * 10 ^ i + Math.round(Math.random() * 10 * i)));
            }

            pd.addInterval(id);
            cal.add(timeInterval, timeDuration);

            if (isSimulateRealCommunication()) {
                ProtocolTools.delay(1);
                String second = String.valueOf(System.currentTimeMillis() / 500);
                second = second.substring(second.length() - 1);
                if (!outputData.equalsIgnoreCase(second)) {
                    outputData = second;
                    doGenerateCommunication(1, outputData);
                }
            }
        }
        return pd;
    }

    /**
     * Search for the {@link com.energyict.protocol.LoadProfileConfiguration} object which is linked to the given {@link com.energyict.protocol.LoadProfileReader}.
     * The link is made using the {@link com.energyict.obis.ObisCode} from both objects.
     *
     * @param lpro the {@link com.energyict.protocol.LoadProfileReader}
     * @return the requested {@link com.energyict.protocol.LoadProfileConfiguration}
     * @throws LoadProfileConfigurationException
     *          if no corresponding object is found
     */
    private LoadProfileConfiguration getLoadProfileConfigurationForGivenReadObject(LoadProfileReader lpro) throws LoadProfileConfigurationException {
        for (LoadProfileConfiguration lpc : this.loadProfileConfigurationList) {
            if (lpc.getObisCode().equals(lpro.getProfileObisCode())) {
                return lpc;
            }
        }
        throw new LoadProfileConfigurationException("Could not find the configurationObject to read.");
    }

    /**
     * This method is used to request a RegisterInfo object that gives info
     * about the meter's supporting the specific ObisCode. If the ObisCode is
     * not supported, NoSuchRegister is thrown.
     *
     * @param register the Register to request RegisterInfo for
     * @return RegisterInfo about the ObisCode
     * @throws java.io.IOException Thrown in case of an exception
     */
    public RegisterInfo translateRegister(Register register) throws IOException {
        return null;  //TODO implement proper functionality.
    }

    /**
     * Request a RegisterValue object for an ObisCode. If the ObisCode is not
     * supported, NoSuchRegister is thrown.
     *
     * @param registers The ObisCode for which to request a RegisterValue
     * @return RegisterValue object for an ObisCode
     * @throws java.io.IOException Thrown in case of an exception
     */
    public List<RegisterValue> readRegisters(List<Register> registers) throws IOException {
        return null;  //TODO implement proper functionality.
    }

    /**
     * Get all the meter events from the device starting from the given date.
     *
     * @param lastLogbookDate the date of the last <CODE>MeterEvent</CODE> stored in the database
     * @return a list of <CODE>MeterEvents</CODE>
     */
    public List<MeterEvent> getMeterEvents(Date lastLogbookDate) {
        return null;  //TODO implement proper functionality.
    }
}
