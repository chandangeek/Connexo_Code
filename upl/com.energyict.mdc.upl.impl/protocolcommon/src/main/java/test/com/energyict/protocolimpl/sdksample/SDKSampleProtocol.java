/*
 * SDKSampleProtocol.java
 *
 * Created on 13 juni 2007, 11:30
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package test.com.energyict.protocolimpl.sdksample;

import com.energyict.mdc.upl.NoSuchRegisterException;
import com.energyict.mdc.upl.ProtocolException;
import com.energyict.mdc.upl.cache.CachingProtocol;
import com.energyict.mdc.upl.cache.ProtocolCacheFetchException;
import com.energyict.mdc.upl.cache.ProtocolCacheUpdateException;
import com.energyict.mdc.upl.messages.legacy.Message;
import com.energyict.mdc.upl.messages.legacy.MessageAttribute;
import com.energyict.mdc.upl.messages.legacy.MessageCategorySpec;
import com.energyict.mdc.upl.messages.legacy.MessageElement;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.MessageSpec;
import com.energyict.mdc.upl.messages.legacy.MessageTag;
import com.energyict.mdc.upl.messages.legacy.MessageTagSpec;
import com.energyict.mdc.upl.messages.legacy.MessageValue;
import com.energyict.mdc.upl.messages.legacy.MessageValueSpec;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.properties.TypedProperties;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.MessageProtocol;
import com.energyict.protocol.MessageResult;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.base.AbstractProtocol;
import com.energyict.protocolimpl.base.CacheObject;
import com.energyict.protocolimpl.base.Encryptor;
import com.energyict.protocolimpl.base.ParseUtils;
import com.energyict.protocolimpl.base.ProtocolConnection;
import com.energyict.protocolimpl.base.RTUCache;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * @author kvds
 *         SDKSampleProtocol
 */
public class SDKSampleProtocol extends AbstractProtocol implements MessageProtocol, CachingProtocol {

    private static final String PK_SAMPLE = "SDKSampleProperty";
    private static final String PK_SIMULATE_REAL_COMMUNICATION = "SimulateRealCommunication";
    private static final String PK_LOAD_PROFILE_OBIS_CODE = "LoadProfileObisCode";
    private static final String FIRMWAREPROGRAM = "UpgradeMeterFirmware";
    private static final String FIRMWAREPROGRAM_DISPLAY = "Upgrade Meter Firmware";

    private CacheObject cache;

    private SDKSampleProtocolConnection connection;
    private int sDKSampleProperty;
    private boolean simulateRealCommunication = false;
    private ObisCode loadProfileObisCode;

    public SDKSampleProtocol(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @Override
    public void applyMessages(List messageEntries) throws IOException {
        Iterator it = messageEntries.iterator();
        while (it.hasNext()) {
            MessageEntry messageEntry = (MessageEntry) it.next();
            //System.out.println(messageEntry);
        }
    }

    @Override
    public MessageResult queryMessage(MessageEntry messageEntry) throws IOException {
        getLogger().info("MessageEntry: " + messageEntry.getContent());
        doGenerateCommunication();

        return MessageResult.createSuccess(messageEntry);
        //messageEntry.setTrackingId("tracking ID for "+messageEntry.);
        //return MessageResult.createQueued(messageEntry);
        //return MessageResult.createFailed(messageEntry);
        //return MessageResult.createUnknown(messageEntry);
    }

    @Override
    public List getMessageCategories() {
        List theCategories = new ArrayList();
        // General Parameters
        MessageCategorySpec cat = new MessageCategorySpec("SAMPLE");
        MessageSpec msgSpec = addBasicMsg("Disconnect meter", "DISCONNECT", false);
        cat.addMessageSpec(msgSpec);
        msgSpec = addBasicMsg("Connect meter", "CONNECT", false);
        cat.addMessageSpec(msgSpec);
        msgSpec = addBasicMsg("Limit current to 6A", "LIMITCURRENT6A", false);
        cat.addMessageSpec(msgSpec);

        msgSpec = addBasicMsg(FIRMWAREPROGRAM_DISPLAY, FIRMWAREPROGRAM, true);
        cat.addMessageSpec(msgSpec);

        theCategories.add(cat);
        return theCategories;
    }

    private MessageSpec addBasicMsg(String keyId, String tagName, boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        tagSpec.add(new MessageValueSpec());
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    @Override
    public String writeMessage(Message msg) {
        return msg.write(this);
    }

    @Override
    public String writeTag(MessageTag msgTag) {
        StringBuilder builder = new StringBuilder();

        // a. Opening tag
        builder.append("<");
        builder.append(msgTag.getName());

        // b. Attributes
        for (Iterator<MessageAttribute> it = msgTag.getAttributes().iterator(); it.hasNext();) {
            MessageAttribute att = it.next();
            if ((att.getValue() == null) || (att.getValue().isEmpty())) {
                continue;
            }
            builder.append(" ").append(att.getSpec().getName());
            builder.append("=").append('"').append(att.getValue()).append('"');
        }
        builder.append(">");

        // c. sub elements
        for (Iterator it = msgTag.getSubElements().iterator(); it.hasNext();) {
            MessageElement elt = (MessageElement) it.next();
            if (elt.isTag()) {
                builder.append(writeTag((MessageTag) elt));
            } else if (elt.isValue()) {
                String value = writeValue((MessageValue) elt);
                if ((value == null) || (value.isEmpty())) {
                    return "";
                }
                builder.append(value);
            }
        }

        // d. Closing tag
        builder.append("</");
        builder.append(msgTag.getName());
        builder.append(">");

        return builder.toString();
    }

    @Override
    public String writeValue(MessageValue value) {
        return value.getValue();
    }

    @Override
    protected void doConnect() throws IOException {
        getLogger().info("call abstract method doConnect()");
        getLogger().info("--> at that point, we have a communicationlink with the meter (modem, direct, optical, ip, ...)");
        getLogger().info("--> here the login and other authentication and setup should be done");
        doGenerateCommunication(0, "GET / HTTP/1.1\r\n\r\n");

        if (this.cache != null) {
            getLogger().info("Text from cache : " + this.cache.getText());
        } else {
            getLogger().info("Empty cache, will create one.");
            this.cache = new CacheObject("");
        }

    }

    @Override
    protected void doDisconnect() throws IOException {
        getLogger().info("call abstract method doDisConnect()");
        getLogger().info("--> here the logoff should be done");
        getLogger().info("--> after that point, we will close the communicationlink with the meter");
        doGenerateCommunication();

        this.cache.setText("Hi I'm cached data -> " + Long.toString(Calendar.getInstance().getTimeInMillis()));
    }

    @Override
    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {

        getLogger().info("call overrided method getProfileData(" + lastReading + "," + includeEvents + ")");
        getLogger().info("--> here we read the profiledata for " + getLoadProfileObisCode().toString() + " from the meter and construct a profiledata object");
        doGenerateCommunication();

        ProfileData pd = new ProfileData();
        if (getLoadProfileObisCode().getD() == 1) {
            pd.addChannel(new ChannelInfo(0, 0, "SDK sample profile " + getLoadProfileObisCode().toString() + " channel 1", Unit.get("kWh")));
            pd.addChannel(new ChannelInfo(1, 1, "SDK sample profile " + getLoadProfileObisCode().toString() + " channel 2", Unit.get("kvarh")));
        } else if (getLoadProfileObisCode().getD() == 2) {
            pd.addChannel(new ChannelInfo(0, 0, "SDK sample profile " + getLoadProfileObisCode().toString() + " channel 1", Unit.get("kWh")));
            pd.addChannel(new ChannelInfo(1, 1, "SDK sample profile " + getLoadProfileObisCode().toString() + " channel 2", Unit.get("kvarh")));
        } else {
            throw new NoSuchRegisterException("Invalid load profile request " + getLoadProfileObisCode().toString());
        }

        Calendar cal = Calendar.getInstance(getTimeZone());
        cal.setTime(lastReading);
        if (getProfileInterval() <= 0) {
            throw new ProtocolException("load profile interval must be > 0 sec. (is " + getProfileInterval() + ")");
        }
        ParseUtils.roundDown2nearestInterval(cal, getProfileInterval());

        Calendar currentCal = Calendar.getInstance();

        String outputData = "";
        while (cal.getTime().before(currentCal.getTime())) {
            IntervalData id = new IntervalData(cal.getTime());

            id.addValue(new BigDecimal(10000 + Math.round(Math.random() * 100)));
            id.addValue(new BigDecimal(1000 + Math.round(Math.random() * 10)));
            pd.addInterval(id);
            cal.add(Calendar.SECOND, getProfileInterval());

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

        currentCal.set(Calendar.MILLISECOND, 0);
        pd.addEvent(new MeterEvent(currentCal.getTime(), MeterEvent.APPLICATION_ALERT_START, "SDK Sample - First Event"));
        currentCal.set(Calendar.MILLISECOND, 20);
        pd.addEvent(new MeterEvent(currentCal.getTime(), MeterEvent.APPLICATION_ALERT_START, "SDK Sample - Second Event"));
        currentCal.add(Calendar.SECOND, 1);
        pd.addEvent(new MeterEvent(currentCal.getTime(), MeterEvent.APPLICATION_ALERT_START, "SDK Sample - Third Event"));
        return pd;
    }

    @Override
    protected String getRegistersInfo(int extendedLogging) throws IOException {
        getLogger().info("call overrided method getRegistersInfo(" + extendedLogging + ")");
        getLogger().info("--> You can provide info about meter register configuration here. If the ExtendedLogging property is set, that info will be logged.");
        return "1.1.1.8.1.255 Active Import energy";
    }

    @Override
    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        return new RegisterInfo(obisCode.toString());
    }

    @Override
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        getLogger().info("call overrided method readRegister(" + obisCode + ")");
        getLogger().info("--> request the register from the meter here");
        doGenerateCommunication();

        Calendar now = Calendar.getInstance();
        Date toTime = now.getTime();
        Date eventTime = null;

        now.set(Calendar.DAY_OF_MONTH, 1);
        now.set(Calendar.HOUR_OF_DAY, 0);
        now.set(Calendar.MINUTE, 0);
        now.set(Calendar.SECOND, 0);
        now.set(Calendar.MILLISECOND, 0);

        Date fromTime = now.getTime();

        if (obisCode.getF() != 255) {
            int billing = obisCode.getF();
            if (billing < 0) {
                billing *= -1;
            }

            now.add(Calendar.MONTH, -1 * billing);
            toTime = now.getTime();
            eventTime = new Date(toTime.getTime());
            now.add(Calendar.MONTH, -1);
            fromTime = now.getTime();

        }

        if (obisCode.getA() == 1) {
            if (obisCode.getD() == 8) {
                if (obisCode.getE() > 0) {
                    Quantity quantity = new Quantity(new BigDecimal("" + (((System.currentTimeMillis() / 1000) * 2) * obisCode.getB())), Unit.get("kWh"));
                    return new RegisterValue(obisCode, quantity, eventTime, fromTime, toTime);
                } else {
                    Quantity quantity = new Quantity(new BigDecimal("" + ((System.currentTimeMillis() / 1000) * obisCode.getB())), Unit.get("kWh"));
                    return new RegisterValue(obisCode, quantity, eventTime, fromTime, toTime);
                }
            } else {
                if (obisCode.getE() > 0) {
                    Quantity quantity = new Quantity(new BigDecimal("12345678.8").multiply(new BigDecimal("2")), Unit.get("kWh"));
                    return new RegisterValue(obisCode, quantity, eventTime, fromTime, toTime);
                } else {
                    Quantity quantity = new Quantity(new BigDecimal("12345678.8"), Unit.get("kWh"));
                    return new RegisterValue(obisCode, quantity, eventTime, fromTime, toTime);
                }
            }
        }
        throw new NoSuchRegisterException("Register " + obisCode + " not supported!");
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        List<PropertySpec> propertySpecs = new ArrayList<>(super.getUPLPropertySpecs());
        propertySpecs.add(this.integerSpec(PK_SAMPLE, false));
        propertySpecs.add(this.stringSpec(PK_SIMULATE_REAL_COMMUNICATION, false));
        propertySpecs.add(this.stringSpec(PK_LOAD_PROFILE_OBIS_CODE, false));
        return propertySpecs;
    }

    @Override
    public void setUPLProperties(TypedProperties properties) throws PropertyValidationException {
        super.setUPLProperties(properties);
        setSDKSampleProperty(Integer.parseInt(properties.getTypedProperty(PK_SAMPLE, "123")));
        this.simulateRealCommunication = "1".equalsIgnoreCase(properties.getTypedProperty(PK_SIMULATE_REAL_COMMUNICATION, "0").trim());
        setLoadProfileObisCode(ObisCode.fromString(properties.getTypedProperty(PK_LOAD_PROFILE_OBIS_CODE, "0.0.99.1.0.255")));
    }

    @Override
    protected ProtocolConnection doInit(InputStream inputStream, OutputStream outputStream, int timeoutProperty, int protocolRetriesProperty, int forcedDelay, int echoCancelling, int protocolCompatible, Encryptor encryptor, HalfDuplexController halfDuplexController) throws IOException {
        getLogger().info("call doInit(...)");
        getLogger().info("--> construct the ProtocolConnection and all other object here");

        connection = new SDKSampleProtocolConnection(inputStream, outputStream, timeoutProperty, protocolRetriesProperty, forcedDelay, echoCancelling, protocolCompatible, encryptor, getLogger());
        return connection;
    }

    @Override
    public int getNumberOfChannels() throws IOException {
        getLogger().info("call overrided method getNumberOfChannels() (return 2 as sample)");
        getLogger().info("--> report the nr of load profile channels in the meter here");
        doGenerateCommunication();
        return 2;
    }

    @Override
    public Date getTime() throws IOException {
        getLogger().info("call getTime() (if time is different from system time taken into account the properties, setTime will be called) ");
        getLogger().info("--> request the metertime here");
        doGenerateCommunication();

        long currenttime = new Date().getTime();
        return new Date(currenttime - (1000 * 30));
    }

    @Override
    public void setTime() throws IOException {
        getLogger().info("call setTime() (this method is called automatically when needed)");
        getLogger().info("--> sync the metertime with the systemtime here");
        doGenerateCommunication();
    }

    @Override
    public String getProtocolVersion() {
        return "$Date: 2014-06-20 14:07:47 +0200 (Fri, 20 Jun 2014) $";
    }

    @Override
    public String getFirmwareVersion() throws IOException {
        getLogger().info("call getFirmwareVersion()");
        getLogger().info("--> report the firmware version and other important meterinfo here");
        doGenerateCommunication();
        return "SDK Sample firmware version";
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
            getConnection().read();
        }
    }

    private SDKSampleProtocolConnection getConnection() {
        return connection;
    }

    private void doGenerateCommunication() {
        doGenerateCommunication(1000, null);
    }

    public int getSDKSampleProperty() {
        return sDKSampleProperty;
    }

    private void setSDKSampleProperty(int sDKSampleProperty) {
        this.sDKSampleProperty = sDKSampleProperty;
    }

    public ObisCode getLoadProfileObisCode() {
        return loadProfileObisCode;
    }

    public void setLoadProfileObisCode(ObisCode loadProfileObisCode) {
        this.loadProfileObisCode = loadProfileObisCode;
    }

    @Override
    public Serializable getCache() {
        return this.cache;
    }

    @Override
    public void setCache(Serializable cacheObject) {
        this.cache = (CacheObject) cacheObject;
    }

    @Override
    public void updateCache(int deviceId, Serializable cacheObject, Connection connection) throws SQLException, ProtocolCacheUpdateException {
        if (deviceId != 0) {
            /* Use the RTUCache to set the blob (cache) to the database */
            RTUCache rtu = new RTUCache(deviceId);
            rtu.setBlob(cacheObject, connection);
        }
    }

    @Override
    public Serializable fetchCache(int deviceId, Connection connection) throws SQLException, ProtocolCacheFetchException {
        if (deviceId != 0) {
            /* Use the RTUCache to get the blob from the database */
            RTUCache rtu = new RTUCache(deviceId);
            try {
                return rtu.getCacheObject(connection);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }

    private boolean isSimulateRealCommunication() {
        return simulateRealCommunication;
    }

}