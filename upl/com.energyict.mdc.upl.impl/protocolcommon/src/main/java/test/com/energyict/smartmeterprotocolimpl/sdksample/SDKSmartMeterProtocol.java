package test.com.energyict.smartmeterprotocolimpl.sdksample;

import com.energyict.mdc.upl.UnsupportedException;
import com.energyict.mdc.upl.messages.legacy.Message;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.MessageTag;
import com.energyict.mdc.upl.messages.legacy.MessageValue;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.cbo.Quantity;
import com.energyict.messaging.LegacyLoadProfileRegisterMessageBuilder;
import com.energyict.messaging.LegacyPartialLoadProfileMessageBuilder;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.LoadProfileConfiguration;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocol.MessageProtocol;
import com.energyict.protocol.MessageResult;
import com.energyict.protocol.MeterData;
import com.energyict.protocol.MeterDataMessageResult;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.MeterReadingData;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.Register;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.smartmeterprotocolimpl.common.AbstractSmartMeterProtocol;
import org.xml.sax.SAXException;
import test.com.energyict.protocolimpl.sdksample.SDKSampleProtocolConnection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * Date: 17-jan-2011
 * Time: 15:14:31
 */
public abstract class SDKSmartMeterProtocol extends AbstractSmartMeterProtocol implements MessageProtocol {

    private static final String MeterSerialNumber = "Master";

    /**
     * The used <CODE>Connection</CODE> class
     */
    private SDKSampleProtocolConnection connection;

    /**
     * This field contains the ProtocolProperies,
     * a class that manages the properties for this particular protocol
     */
    private SDKSmartMeterProperties properties;
    private SDKSmartMeterProfile smartMeterProfile;
    private SDKSmartMeterRegisterFactory registerFactory;

    private final PropertySpecService propertySpecService;

    public SDKSmartMeterProtocol(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
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
        super.init(inputStream, outputStream, timeZone, logger);
        this.connection = new SDKSampleProtocolConnection(inputStream, outputStream, 100, 2, 50, 0, 1, null, getLogger());
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

    /**
     * Get the ProtocolProperties for the SDKSmartMeterProtocol.
     * This objects contains, manages and validates the properties in a clean way
     *
     * @return
     */
    public SDKSmartMeterProperties getProtocolProperties() {
        if (properties == null) {
            this.properties = new SDKSmartMeterProperties(propertySpecService);
        }
        return properties;
    }

    /**
     * @return the version of the specific protocol implementation
     */
    public String getVersion() {
        return "$Date: 2014-09-23 16:08:34 +0200 (Tue, 23 Sep 2014) $";
    }

    /**
     * Get the firmware version of the meter
     *
     * @return the version of the meter firmware
     *         </p>
     * @throws java.io.IOException Thrown in case of an exception
     * @throws UnsupportedException
     *                             Thrown if method is not supported
     */
    public String getFirmwareVersion() throws IOException {
        getLogger().info("call getFirmwareVersion()");
        getLogger().info("--> report the firmware version and other important meterinfo here");
        return "SDK MultipleLoadProfile Sample firmware version";
    }

    /**
     * Get the SerialNumber of the device
     *
     * @return the serialNumber of the device
     * @throws java.io.IOException thrown in case of an exception
     */
    public String getMeterSerialNumber() throws IOException {
        return MeterSerialNumber;
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
     * </p>
     *
     * @param newMeterTime the time to set in the meter
     * @throws java.io.IOException Thrown in case of an exception
     */
    public void setTime(Date newMeterTime) throws IOException {
        getLogger().info("call setTime() (this method is called automatically when needed)");
        getLogger().info("--> sync the metertime with the systemtime here");
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
        return getSMartMeterProfile().fetchLoadProfileConfiguration(loadProfileObisCodes);
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
        return getSMartMeterProfile().getLoadProfileData(loadProfiles);
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
        return getRegisterFactory().translateRegister(register);
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
        return getRegisterFactory().readRegisters(registers);
    }

    /**
     * Get all the meter events from the device starting from the given date.
     *
     * @param lastLogbookDate the date of the last <CODE>MeterEvent</CODE> stored in the database
     * @return a list of <CODE>MeterEvents</CODE>
     */
    public List<MeterEvent> getMeterEvents(Date lastLogbookDate) {
        getLogger().info("call getMeterEvents()");
        getLogger().info("Three events will be generated, a ClockSet, a PowerDown and a PowerUp.");
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR, -1);
        List<MeterEvent> meterEvents = new ArrayList<MeterEvent>();
        meterEvents.add(new MeterEvent(cal.getTime(), MeterEvent.SETCLOCK, 20));
        cal.add(Calendar.MINUTE, 13);
        meterEvents.add(new MeterEvent(cal.getTime(), MeterEvent.POWERDOWN, 4));
        cal.add(Calendar.MINUTE, 24);
        meterEvents.add(new MeterEvent(cal.getTime(), MeterEvent.POWERUP, 3));
        return meterEvents;
    }

    /**
     * Lazy getter for the SDKSmartMeterProfile object
     *
     * @return
     */
    private SDKSmartMeterProfile getSMartMeterProfile() {
        if (smartMeterProfile == null) {
            smartMeterProfile = new SDKSmartMeterProfile(this);
        }
        return smartMeterProfile;
    }

    /**
     * Lazy getter for the SDKSmartMeterRegisterFactory object
     *
     * @return
     */
    private SDKSmartMeterRegisterFactory getRegisterFactory() {
        if (registerFactory == null) {
            registerFactory = new SDKSmartMeterRegisterFactory();
        }
        return registerFactory;
    }

    /**
     * Getter for the SDKSampleProtocolConnection object
     *
     * @return
     */
    private SDKSampleProtocolConnection getConnection() {
        return connection;
    }

    /**
     *
     */
    private void doGenerateCommunication() {
        doGenerateCommunication(1000, null);
    }

    /**
     * @param delay
     * @param value
     */
    protected void doGenerateCommunication(long delay, String value) {
        if (getProtocolProperties().isSimulateRealCommunication()) {
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

    public LegacyPartialLoadProfileMessageBuilder getPartialLoadProfileMessageBuilder() {
        return new LegacyPartialLoadProfileMessageBuilder();
    }

    /**
     * Provides the full list of outstanding messages to the protocol.
     * If for any reason certain messages have to be grouped before they are sent to a device, then this is the place to do it.
     * At a later timestamp the framework will query each {@link MessageEntry} (see {@link #queryMessage(MessageEntry)}) to actually
     * perform the message.
     *
     * @param messageEntries a list of {@link MessageEntry}s
     * @throws java.io.IOException if a logical error occurs
     */
    public void applyMessages(final List messageEntries) throws IOException {
        //TODO implement proper functionality.
    }

    /**
     * Indicates that each message has to be executed by the protocol.
     *
     * @param messageEntry a definition of which message needs to be sent
     * @return a state of the message which was just sent
     * @throws java.io.IOException if a logical error occurs
     */
    public MessageResult queryMessage(final MessageEntry messageEntry) {
        MessageResult result = MessageResult.createFailed(messageEntry);
        if (messageEntry.getContent().contains(LegacyPartialLoadProfileMessageBuilder.getMessageNodeTag())) {
            result = doReadPartialLoadProfile(messageEntry);
        } else if (messageEntry.getContent().contains(LegacyLoadProfileRegisterMessageBuilder.getMessageNodeTag())) {
            result = doReadLoadProfileRegisters(messageEntry);
        }
        return result;
    }

    private MessageResult doReadLoadProfileRegisters(final MessageEntry msgEntry) {
        try {
            getLogger().info("Handling message Read LoadProfile Registers.");
            LegacyLoadProfileRegisterMessageBuilder builder = getLoadProfileRegisterMessageBuilder();
            builder = (LegacyLoadProfileRegisterMessageBuilder) LegacyLoadProfileRegisterMessageBuilder.fromXml(msgEntry.getContent());
            if (builder.getRegisters() == null || builder.getRegisters().isEmpty()) {
                return MessageResult.createFailed(msgEntry, "Unable to execute the message, there are no channels attached under LoadProfile " + builder.getProfileObisCode() + "!");
            }

            LoadProfileReader lpr = builder.getLoadProfileReader();
            final List<LoadProfileConfiguration> loadProfileConfigurations = fetchLoadProfileConfiguration(Collections.singletonList(lpr));
            final List<ProfileData> profileDatas = getLoadProfileData(Collections.singletonList(lpr));

            if (profileDatas.size() != 1) {
                return MessageResult.createFailed(msgEntry, "We are supposed to receive 1 LoadProfile configuration in this message.");
            }

            ProfileData pd = profileDatas.get(0);
            IntervalData id = null;
            for (IntervalData intervalData : pd.getIntervalDatas()) {
                if (intervalData.getEndTime().equals(builder.getStartReadingTime())) {
                    id = intervalData;
                }
            }

            if (id == null) {
                return MessageResult.createFailed(msgEntry, "Didn't receive data for requested interval (" + builder.getStartReadingTime() + ")");
            }

            MeterReadingData mrd = new MeterReadingData();
            for (Register register : builder.getRegisters()) {
                for (int i = 0; i < pd.getChannelInfos().size(); i++) {
                    final ChannelInfo channel = pd.getChannel(i);
                    if (register.getObisCode().equalsIgnoreBChannel(ObisCode.fromString(channel.getName())) && register.getSerialNumber().equals(channel.getMeterIdentifier())) {
                        final RegisterValue registerValue = new RegisterValue(register, new Quantity(id.get(i), channel.getUnit()), id.getEndTime(), null, id.getEndTime(), new Date(), builder.getRtuRegisterIdForRegister(register));
                        mrd.add(registerValue);
                    }
                }
            }

            MeterData md = new MeterData();
            md.setMeterReadingData(mrd);

            getLogger().info("Message Read LoadProfile Registers Finished.");
            return MeterDataMessageResult.createSuccess(msgEntry, "", md);
        } catch (SAXException e) {
            return MessageResult.createFailed(msgEntry, e.getMessage());
        } catch (IOException e) {
            return MessageResult.createFailed(msgEntry, e.getMessage());
        }
    }

    private MessageResult doReadPartialLoadProfile(final MessageEntry msgEntry) {
        try {
            getLogger().info("Handling message Read Partial LoadProfile.");
            LegacyPartialLoadProfileMessageBuilder builder = getPartialLoadProfileMessageBuilder();
            builder = (LegacyPartialLoadProfileMessageBuilder) LegacyPartialLoadProfileMessageBuilder.fromXml(msgEntry.getContent());

            LoadProfileReader lpr = builder.getLoadProfileReader();
            final List<LoadProfileConfiguration> loadProfileConfigurations = fetchLoadProfileConfiguration(Collections.singletonList(lpr));
            final List<ProfileData> profileDatas = getLoadProfileData(Collections.singletonList(lpr));
            MeterData md = new MeterData();
            for (ProfileData data : profileDatas) {
                data.sort();
                md.addProfileData(data);
            }
            getLogger().info("Message Read Partial LoadProfile Finished.");
            return MeterDataMessageResult.createSuccess(msgEntry, "", md);
        } catch (SAXException e) {
            return MessageResult.createFailed(msgEntry, e.getMessage());
        } catch (IOException e) {
            return MessageResult.createFailed(msgEntry, e.getMessage());
        }
    }

    public List getMessageCategories() {
        return null;  //TODO implement proper functionality.
    }

    public String writeMessage(final Message msg) {
        return null;  //TODO implement proper functionality.
    }

    public String writeTag(final MessageTag tag) {
        return null;  //TODO implement proper functionality.
    }

    public String writeValue(final MessageValue value) {
        return null;  //TODO implement proper functionality.
    }

    public LegacyLoadProfileRegisterMessageBuilder getLoadProfileRegisterMessageBuilder() {
        return new LegacyLoadProfileRegisterMessageBuilder();
    }
}
