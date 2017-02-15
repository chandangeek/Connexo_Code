/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * SDKSampleProtocol.java
 *
 * Created on 13 juni 2007, 11:30
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.sdksample;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.InvalidPropertyException;
import com.energyict.mdc.protocol.api.MessageProtocol;
import com.energyict.mdc.protocol.api.MissingPropertyException;
import com.energyict.mdc.protocol.api.NoSuchRegisterException;
import com.energyict.mdc.protocol.api.device.data.ChannelInfo;
import com.energyict.mdc.protocol.api.device.data.IntervalData;
import com.energyict.mdc.protocol.api.device.data.MessageEntry;
import com.energyict.mdc.protocol.api.device.data.MessageResult;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;
import com.energyict.mdc.protocol.api.device.events.MeterEvent;
import com.energyict.mdc.protocol.api.legacy.HalfDuplexController;
import com.energyict.mdc.protocol.api.messaging.Message;
import com.energyict.mdc.protocol.api.messaging.MessageAttribute;
import com.energyict.mdc.protocol.api.messaging.MessageCategorySpec;
import com.energyict.mdc.protocol.api.messaging.MessageElement;
import com.energyict.mdc.protocol.api.messaging.MessageSpec;
import com.energyict.mdc.protocol.api.messaging.MessageTag;
import com.energyict.mdc.protocol.api.messaging.MessageTagSpec;
import com.energyict.mdc.protocol.api.messaging.MessageValue;
import com.energyict.mdc.protocol.api.messaging.MessageValueSpec;
import com.energyict.protocols.mdc.services.impl.OrmClient;

import com.energyict.protocolimpl.base.AbstractProtocol;
import com.energyict.protocolimpl.base.CacheObject;
import com.energyict.protocolimpl.base.Encryptor;
import com.energyict.protocolimpl.base.ParseUtils;
import com.energyict.protocolimpl.base.ProtocolConnection;
import com.energyict.protocolimpl.base.RTUCache;
import com.energyict.protocolimpl.utils.ProtocolTools;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 * @author kvds
 *         SDKSampleProtocol
 */
public class SDKSampleProtocol extends AbstractProtocol implements MessageProtocol {

    @Override
    public String getProtocolDescription() {
        return "EnergyICT SDK MeterProtocol";
    }

    private static final String FIRMWAREPROGRAM = "UpgradeMeterFirmware";
    private static final String FIRMWAREPROGRAM_DISPLAY = "Upgrade Meter Firmware";

    private final OrmClient ormClient;
    private CacheObject cache;

    SDKSampleProtocolConnection connection;
    private int sDKSampleProperty;
    private boolean simulateRealCommunication = false;
    ObisCode loadProfileObisCode;

    @Inject
    public SDKSampleProtocol(PropertySpecService propertySpecService, OrmClient ormClient) {
        super(propertySpecService);
        this.ormClient = ormClient;
    }

    /**
     * ****************************************************************************************
     * M e s s a g e P r o t o c o l  i n t e r f a c e
     * *****************************************************************************************
     */
    // message protocol
    public void applyMessages(List messageEntries) throws IOException {
        Iterator it = messageEntries.iterator();
        while (it.hasNext()) {
            MessageEntry messageEntry = (MessageEntry) it.next();
            //System.out.println(messageEntry);
        }
    }

    public MessageResult queryMessage(MessageEntry messageEntry) throws IOException {
        getLogger().info("MessageEntry: " + messageEntry.getContent());
        doGenerateCommunication();

        return MessageResult.createSuccess(messageEntry);
        //messageEntry.setTrackingId("tracking ID for "+messageEntry.);
        //return MessageResult.createQueued(messageEntry);
        //return MessageResult.createFailed(messageEntry);
        //return MessageResult.createUnknown(messageEntry);
    }

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

    public String writeMessage(Message msg) {
        return msg.write(this);
    }

    public String writeTag(MessageTag msgTag) {
        StringBuilder buf = new StringBuilder();

        // a. Opening tag
        buf.append("<");
        buf.append(msgTag.getName());

        // b. Attributes
        for (Iterator it = msgTag.getAttributes().iterator(); it.hasNext();) {
            MessageAttribute att = (MessageAttribute) it.next();
            if ((att.getValue() == null) || (att.getValue().isEmpty())) {
                continue;
            }
            buf.append(" ").append(att.getSpec().getName());
            buf.append("=").append('"').append(att.getValue()).append('"');
        }
        buf.append(">");

        // c. sub elements
        for (Iterator it = msgTag.getSubElements().iterator(); it.hasNext();) {
            MessageElement elt = (MessageElement) it.next();
            if (elt.isTag()) {
                buf.append(writeTag((MessageTag) elt));
            } else if (elt.isValue()) {
                String value = writeValue((MessageValue) elt);
                if ((value == null) || (value.isEmpty())) {
                    return "";
                }
                buf.append(value);
            }
        }

        // d. Closing tag
        buf.append("</");
        buf.append(msgTag.getName());
        buf.append(">");

        return buf.toString();
    }

    public String writeValue(MessageValue value) {
        return value.getValue();
    }

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

    protected void doDisConnect() throws IOException {
        getLogger().info("call abstract method doDisConnect()");
        getLogger().info("--> here the logoff should be done");
        getLogger().info("--> after that point, we will close the communicationlink with the meter");
        doGenerateCommunication();

        this.cache.setText("Hi I'm cached data -> " + Long.toString(Calendar.getInstance().getTimeInMillis()));
    }

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
            throw new IOException("load profile interval must be > 0 sec. (is " + getProfileInterval() + ")");
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

    protected String getRegistersInfo(int extendedLogging) throws IOException {
        getLogger().info("call overrided method getRegistersInfo(" + extendedLogging + ")");
        getLogger().info("--> You can provide info about meter register configuration here. If the ExtendedLogging property is set, that info will be logged.");
        return "1.1.1.8.1.255 Active Import energy";
    }


    /**
     * ****************************************************************************************
     * R e g i s t e r P r o t o c o l  i n t e r f a c e
     * *****************************************************************************************
     */
    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        //getLogger().info("call overrided method translateRegister()");
        return new RegisterInfo(obisCode.getDescription());
    }

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

    protected void doValidateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
        // Override or add new properties here e.g. below
        setSDKSampleProperty(Integer.parseInt(properties.getProperty("SDKSampleProperty", "123")));
        this.simulateRealCommunication = "1".equalsIgnoreCase(properties.getProperty("SimulateRealCommunication", "0").trim());
        setLoadProfileObisCode(ObisCode.fromString(properties.getProperty("LoadProfileObisCode", "0.0.99.1.0.255")));
    }

    protected List<String> doGetOptionalKeys() {
        return Arrays.asList("SDKSampleProperty", "SimulateRealCommunication");
    }

    protected ProtocolConnection doInit(InputStream inputStream, OutputStream outputStream, int timeoutProperty, int protocolRetriesProperty, int forcedDelay, int echoCancelling, int protocolCompatible, Encryptor encryptor, HalfDuplexController halfDuplexController) throws IOException {
        getLogger().info("call doInit(...)");
        getLogger().info("--> construct the ProtocolConnection and all other object here");

        connection = new SDKSampleProtocolConnection(inputStream, outputStream, timeoutProperty, protocolRetriesProperty, forcedDelay, echoCancelling, protocolCompatible, encryptor, getLogger());
        return connection;
    }

    public int getNumberOfChannels() throws IOException {
        getLogger().info("call overrided method getNumberOfChannels() (return 2 as sample)");
        getLogger().info("--> report the nr of load profile channels in the meter here");
        doGenerateCommunication();
        return 2;
    }

    public Date getTime() throws IOException {
        getLogger().info("call getTime() (if time is different from system time taken into account the properties, setTime will be called) ");
        getLogger().info("--> request the metertime here");
        doGenerateCommunication();

        long currenttime = new Date().getTime();
        return new Date(currenttime - (1000 * 30));
    }

    public void setTime() throws IOException {
        getLogger().info("call setTime() (this method is called automatically when needed)");
        getLogger().info("--> sync the metertime with the systemtime here");
        doGenerateCommunication();
    }

    public String getProtocolVersion() {
        return "$Date: 2013-10-31 11:22:19 +0100 (Thu, 31 Oct 2013) $";
    }

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

    public void setSDKSampleProperty(int sDKSampleProperty) {
        this.sDKSampleProperty = sDKSampleProperty;
    }

    public ObisCode getLoadProfileObisCode() {
        return loadProfileObisCode;
    }

    public void setLoadProfileObisCode(ObisCode loadProfileObisCode) {
        this.loadProfileObisCode = loadProfileObisCode;
    }

    /* Implementation of the Cache interface */

    /**
     * {@inheritDoc}
     */
    public void updateCache(int rtuid, Object cacheObject) throws SQLException {
        if (rtuid != 0) {
            /* Use the RTUCache to set the blob (cache) to the database */
            RTUCache rtu = new RTUCache(rtuid, ormClient);
            rtu.setBlob(cacheObject);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void setCache(Object cacheObject) {
        this.cache = (CacheObject) cacheObject;
    }

    /**
     * {@inheritDoc}
     */
    public Object fetchCache(int rtuid) {
        if (rtuid != 0) {

            /* Use the RTUCache to get the blob from the database */
            RTUCache rtu = new RTUCache(rtuid, ormClient);
            try {
                return rtu.getCacheObject();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public Object getCache() {
        return this.cache;
    }

    public boolean isSimulateRealCommunication() {
        return simulateRealCommunication;
    }
}
