package test.com.energyict.smartmeterprotocolimpl.sdksample;

import com.energyict.mdc.upl.messages.legacy.Message;
import com.energyict.mdc.upl.messages.legacy.MessageCategorySpec;
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
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.MeterReadingData;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.Register;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolcommon.MeterDataMessageResult;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.smartmeterprotocolimpl.common.AbstractSmartMeterProtocol;
import org.xml.sax.SAXException;
import test.com.energyict.protocolimpl.sdksample.SDKSampleProtocolConnection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
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
public class SDKSmartMeterProtocol extends AbstractSmartMeterProtocol implements MessageProtocol {

    private static final String MeterSerialNumber = "Master";
    private final PropertySpecService propertySpecService;
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

    public SDKSmartMeterProtocol(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Override
    public void init(InputStream inputStream, OutputStream outputStream, TimeZone timeZone, Logger logger) throws IOException {
        super.init(inputStream, outputStream, timeZone, logger);
        this.connection = new SDKSampleProtocolConnection(inputStream, outputStream, 100, 2, 50, 0, 1, null, getLogger());
    }

    @Override
    public void connect() throws IOException {
        getLogger().info("call abstract method doConnect()");
        getLogger().info("--> at that point, we have a communicationlink with the meter (modem, direct, optical, ip, ...)");
        getLogger().info("--> here the login and other authentication and setup should be done");
        doGenerateCommunication();
    }

    @Override
    public void disconnect() throws IOException {
        getLogger().info("call abstract method doDisConnect()");
        getLogger().info("--> here the logoff should be done");
        getLogger().info("--> after that point, we will close the communicationlink with the meter");
        doGenerateCommunication();
    }

    @Override
    public SDKSmartMeterProperties getProtocolProperties() {
        if (properties == null) {
            this.properties = new SDKSmartMeterProperties(propertySpecService);
        }
        return properties;
    }

    @Override
    public String getProtocolDescription() {
        return "EnergyICT SDK SmartMeterProtocol";
    }

    @Override
    public String getVersion() {
        return "$Date: 2014-09-23 16:08:34 +0200 (Tue, 23 Sep 2014) $";
    }

    @Override
    public String getFirmwareVersion() throws IOException {
        getLogger().info("call getFirmwareVersion()");
        getLogger().info("--> report the firmware version and other important meterinfo here");
        return "SDK MultipleLoadProfile Sample firmware version";
    }

    @Override
    public String getMeterSerialNumber() throws IOException {
        return MeterSerialNumber;
    }

    @Override
    public Date getTime() throws IOException {
        getLogger().info("call getTime() (if time is different from system time taken into account the properties, setTime will be called) ");
        getLogger().info("--> request the metertime here");
        long currenttime = new Date().getTime();
        return new Date(currenttime - (1000 * 15));
    }

    @Override
    public void setTime(Date newMeterTime) throws IOException {
        getLogger().info("call setTime() (this method is called automatically when needed)");
        getLogger().info("--> sync the metertime with the systemtime here");
    }

    @Override
    public List<LoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfileObisCodes) {
        return getSMartMeterProfile().fetchLoadProfileConfiguration(loadProfileObisCodes);
    }

    @Override
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

    @Override
    public List<RegisterValue> readRegisters(List<Register> registers) throws IOException {
        return getRegisterFactory().readRegisters(registers);
    }

    @Override
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

    private SDKSmartMeterProfile getSMartMeterProfile() {
        if (smartMeterProfile == null) {
            smartMeterProfile = new SDKSmartMeterProfile(this);
        }
        return smartMeterProfile;
    }

    private SDKSmartMeterRegisterFactory getRegisterFactory() {
        if (registerFactory == null) {
            registerFactory = new SDKSmartMeterRegisterFactory();
        }
        return registerFactory;
    }

    private SDKSampleProtocolConnection getConnection() {
        return connection;
    }

    private void doGenerateCommunication() {
        doGenerateCommunication(1000, null);
    }

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

    @Override
    public void applyMessages(final List messageEntries) throws IOException {
        //TODO implement proper functionality.
    }

    @Override
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
            LegacyLoadProfileRegisterMessageBuilder builder = LegacyLoadProfileRegisterMessageBuilder.fromXml(msgEntry.getContent());
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
                        final RegisterValue registerValue = new RegisterValue(register, new Quantity(id.get(i), channel.getUnit()), id.getEndTime(), null, id.getEndTime(), new Date(), register.getRtuRegisterId());
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
            LegacyPartialLoadProfileMessageBuilder builder = LegacyPartialLoadProfileMessageBuilder.fromXml(msgEntry.getContent());

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

    @Override
    public List<MessageCategorySpec> getMessageCategories() {
        return Collections.emptyList();
    }

    @Override
    public String writeMessage(final Message msg) {
        return "";  //TODO implement proper functionality.
    }

    @Override
    public String writeTag(final MessageTag tag) {
        return "";  //TODO implement proper functionality.
    }

    @Override
    public String writeValue(final MessageValue value) {
        return "";  //TODO implement proper functionality.
    }

    @Override
    public Serializable getCache() {
        return null;
    }

    @Override
    public void setCache(Serializable cacheObject) {

    }

}