/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * EICTTestProtocol.java
 *
 * Created on 20 may 2010, 14:34
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.eicttest;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.InvalidPropertyException;
import com.energyict.mdc.protocol.api.MessageProtocol;
import com.energyict.mdc.protocol.api.MissingPropertyException;
import com.energyict.mdc.protocol.api.NoSuchRegisterException;
import com.energyict.mdc.protocol.api.UnsupportedException;
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

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 *
 * @author fde
 * com.energyict.protocolimpl.eicttest.EICTTestProtocol
 */
public class EICTTestProtocol extends AbstractProtocol implements MessageProtocol  {

	@Override
	public String getProtocolDescription() {
		return "EnergyICT Test Protocol";
	}

	private static final String FIRMWAREPROGRAM = "UpgradeMeterFirmware";
	private static final String FIRMWAREPROGRAM_DISPLAY_1 = "Upgrade Meter Firmware 1";
	private static final String FIRMWAREPROGRAM_DISPLAY_2 = "Upgrade Meter Firmware 2";
	private static final String FIRMWAREPROGRAM_DISPLAY_3 = "Upgrade Meter Firmware 3";
	private static String INCLUDE_FILE_TAG = "FirmwareFileID";

	private final OrmClient ormClient;
	private CacheObject cache;

	EICTTestProtocolConnection connection;
	private int eICTTestProperty;
	ObisCode loadProfileObisCode;

	private long steps;

	@Inject
    public EICTTestProtocol(PropertySpecService propertySpecService, OrmClient ormClient) {
		super(propertySpecService);
	    this.ormClient = ormClient;
    }

    /*******************************************************************************************
     M e s s a g e P r o t o c o l  i n t e r f a c e
     *******************************************************************************************/
    // message protocol
    public void applyMessages(List messageEntries) throws IOException {
        Iterator it = messageEntries.iterator();
        while(it.hasNext()) {
            MessageEntry messageEntry = (MessageEntry)it.next();
            //System.out.println(messageEntry);
        }
    }

    public MessageResult queryMessage(MessageEntry messageEntry) throws IOException {

    	getLogger().info("MessageEntry: "+messageEntry.getContent());

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

        msgSpec = addBasicMsg(FIRMWAREPROGRAM_DISPLAY_1, FIRMWAREPROGRAM, true);
        cat.addMessageSpec(msgSpec);
		msgSpec = addUpgradeMsg1(FIRMWAREPROGRAM_DISPLAY_2, FIRMWAREPROGRAM, true);
        cat.addMessageSpec(msgSpec);
		msgSpec = addUpgradeMsg2(FIRMWAREPROGRAM_DISPLAY_3, FIRMWAREPROGRAM, true);
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

	private MessageSpec addUpgradeMsg1(String keyId, String tagName, boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);

        MessageTagSpec rootTag = new MessageTagSpec(tagName);
		MessageTagSpec fileId = new MessageTagSpec(INCLUDE_FILE_TAG);
		fileId.add(new MessageValueSpec());
        rootTag.add(fileId);

        msgSpec.add(rootTag);
        return msgSpec;
    }

	private MessageSpec addUpgradeMsg2(String keyId, String tagName, boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);

        MessageTagSpec rootTag = new MessageTagSpec(tagName);
		rootTag.add(new MessageValueSpec());

		MessageTagSpec fileId = new MessageTagSpec(INCLUDE_FILE_TAG);
		fileId.add(new MessageValueSpec());

        rootTag.add(fileId);
        msgSpec.add(rootTag);
        return msgSpec;
    }

    public String writeMessage(Message msg) {
        return msg.write(this);
    }

    public String writeTag(MessageTag msgTag) {
        StringBuilder buf = new StringBuilder();

        // a. Opening tag
        buf.append("<");
        buf.append( msgTag.getName() );

        // b. Attributes
        for (Iterator it = msgTag.getAttributes().iterator(); it.hasNext();) {
            MessageAttribute att = (MessageAttribute)it.next();
            if ((att.getValue()==null) || (att.getValue().isEmpty())) {
				continue;
			}
            buf.append(" ").append(att.getSpec().getName());
            buf.append("=").append('"').append(att.getValue()).append('"');
        }
        buf.append(">");

        // c. sub elements
        for (Iterator it = msgTag.getSubElements().iterator(); it.hasNext();) {
            MessageElement elt = (MessageElement)it.next();
            if (elt.isTag()) {
				buf.append( writeTag((MessageTag)elt) );
			} else if (elt.isValue()) {
                String value = writeValue((MessageValue)elt);
                if ((value==null) || (value.isEmpty())) {
					return "";
				}
                buf.append(value);
            }
        }

        // d. Closing tag
        buf.append("</");
        buf.append( msgTag.getName() );
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

        if(this.cache != null){
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
        this.cache.setText("Hi I'm cached data -> " + Long.toString(Calendar.getInstance().getTimeInMillis()));
    }

    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {

        getLogger().info("call overrided method getProfileData("+lastReading+","+includeEvents+")");
        getLogger().info("--> here we read the profiledata for "+getLoadProfileObisCode().toString()+" from the meter and construct a profiledata object");

        ProfileData pd = new ProfileData();

		boolean isCumulative;
		if (getLoadProfileObisCode().getD() == 1) {
			isCumulative = false;
		} else if (getLoadProfileObisCode().getD() == 2) {
			isCumulative = true;
		} else {
			throw new NoSuchRegisterException("Invalid load profile request "
					+ getLoadProfileObisCode().toString());
		}

		if (isCumulative == false) {
			pd.addChannel(new ChannelInfo(0, 0, "EICT test profile "
					+ getLoadProfileObisCode().toString() + " channel 1", Unit
					.get("kWh")));
			pd.addChannel(new ChannelInfo(1, 1, "EICT test profile "
					+ getLoadProfileObisCode().toString() + " channel 2", Unit
					.get("kvarh")));
			pd.addChannel(new ChannelInfo(2, 2, "EICT test profile "
					+ getLoadProfileObisCode().toString() + " channel 3", Unit
					.get("\u00B0C")));
			pd.addChannel(new ChannelInfo(3, 3, "EICT test profile "
					+ getLoadProfileObisCode().toString() + " channel 4", Unit
					.get("kWh")));
			pd.addChannel(new ChannelInfo(4, 4, "EICT test profile "
					+ getLoadProfileObisCode().toString() + " channel 5", Unit
					.get("kvarh")));
			pd.addChannel(new ChannelInfo(5, 5, "EICT test profile "
					+ getLoadProfileObisCode().toString() + " channel 6", Unit
					.get("\u00B0C")));
			pd.addChannel(new ChannelInfo(6, 6, "EICT test profile "
					+ getLoadProfileObisCode().toString() + " channel 7", Unit
					.get("kWh")));
			pd.addChannel(new ChannelInfo(7, 7, "EICT test profile "
					+ getLoadProfileObisCode().toString() + " channel 8", Unit
					.get("kvarh")));
			pd.addChannel(new ChannelInfo(8, 8, "EICT test profile "
					+ getLoadProfileObisCode().toString() + " channel 9", Unit
					.get("\u00B0C")));
			pd.addChannel(new ChannelInfo(9, 9, "EICT test profile "
					+ getLoadProfileObisCode().toString() + " channel 10", Unit
					.get("kWh")));
		} else {
			ChannelInfo ci = new ChannelInfo(0, 0, "EICT test profile "
					+ getLoadProfileObisCode().toString() + " channel 1", Unit
					.get("kWh"));
			ci.setCumulative();
			pd.addChannel(ci);
			ci = new ChannelInfo(1, 1, "EICT test profile "
					+ getLoadProfileObisCode().toString() + " channel 2", Unit
					.get("kvarh"));
			ci.setCumulative();
			pd.addChannel(ci);
			pd.addChannel(new ChannelInfo(2, 2, "EICT test profile "
					+ getLoadProfileObisCode().toString() + " channel 3", Unit
					.get("\u00B0C")));

			ci = new ChannelInfo(3, 3, "EICT test profile "
					+ getLoadProfileObisCode().toString() + " channel 4", Unit
					.get("kWh"));
			ci.setCumulative();
			pd.addChannel(ci);
			ci = new ChannelInfo(4, 4, "EICT test profile "
					+ getLoadProfileObisCode().toString() + " channel 5", Unit
					.get("kvarh"));
			ci.setCumulative();
			pd.addChannel(ci);
			pd.addChannel(new ChannelInfo(5, 5, "EICT test profile "
					+ getLoadProfileObisCode().toString() + " channel 6", Unit
					.get("\u00B0C")));

			ci = new ChannelInfo(6, 6, "EICT test profile "
					+ getLoadProfileObisCode().toString() + " channel 7", Unit
					.get("kWh"));
			ci.setCumulative();
			pd.addChannel(ci);
			ci = new ChannelInfo(7, 7, "EICT test profile "
					+ getLoadProfileObisCode().toString() + " channel 8", Unit
					.get("kvarh"));
			ci.setCumulative();
			pd.addChannel(ci);
			pd.addChannel(new ChannelInfo(8, 8, "EICT test profile "
					+ getLoadProfileObisCode().toString() + " channel 9", Unit
					.get("\u00B0C")));

			ci = new ChannelInfo(9, 9, "EICT test profile "
					+ getLoadProfileObisCode().toString() + " channel 10", Unit
					.get("kWh"));
			ci.setCumulative();
			pd.addChannel(ci);
		}

        // getTimeZone() returns the time zone that has been selected
        // in the RMR-tab of the device properties is EIServer
        Calendar cal = Calendar.getInstance(getTimeZone());
        cal.setTime(lastReading);
        if (getProfileInterval()<=0) {
			throw new IOException("load profile interval must be > 0 sec. (is "+getProfileInterval()+")");
		}
        ParseUtils.roundDown2nearestInterval(cal,getProfileInterval());
        Date now = new Date();
        steps = 21600 / getProfileInterval(); // the amount of steps to take in 6 hours
        while(cal.getTime().before(now)) {
           IntervalData id = new IntervalData(cal.getTime());

			if (isCumulative == false) {
				id.addValue(calculateValue(cal, 10000, 1000));
				id.addValue(calculateValue(cal, 1000, 50));
				id.addValue(calculateValue(cal, 20, 5));
				id.addValue(calculateValue(cal, 10000, 1000));
				id.addValue(calculateValue(cal, 1000, 50));
				id.addValue(calculateValue(cal, 20, 5));
				id.addValue(calculateValue(cal, 10000, 1000));
				id.addValue(calculateValue(cal, 1000, 50));
				id.addValue(calculateValue(cal, 20, 5));
				id.addValue(calculateValue(cal, 5000, 100));
			} else {
				id.addValue(calculateValueCumulative(cal, 1000));
				id.addValue(calculateValueCumulative(cal, 50));
				id.addValue(calculateValue(cal, 20, 5));
				id.addValue(calculateValueCumulative(cal, 1000));
				id.addValue(calculateValueCumulative(cal, 50));
				id.addValue(calculateValue(cal, 20, 5));
				id.addValue(calculateValueCumulative(cal, 1000));
				id.addValue(calculateValueCumulative(cal, 50));
				id.addValue(calculateValue(cal, 20, 5));
				id.addValue(calculateValueCumulative(cal, 100));
			}

           pd.addInterval(id);
           cal.add(Calendar.SECOND, getProfileInterval());
        }

		String version = getProtocolVersion().substring(1, getProtocolVersion().length()-1);
		pd.addEvent(new MeterEvent(now, MeterEvent.APPLICATION_ALERT_START, "EICT Test (" + version + ")"));
        return pd;
    }

	private BigDecimal calculateValue(Calendar cal, long base, long amplitude) {
		int utcOffset = (cal.get(Calendar.ZONE_OFFSET) + cal
				.get(Calendar.DST_OFFSET));
		long localTime = cal.getTime().getTime() + utcOffset;
		long offset = localTime % 86400000;
		if (offset <= 21600000) {
			long delta = offset * amplitude / 21600000;
			return new BigDecimal(base + delta);
		} else if (offset <= 43200000) {
			offset -= 21600000;
			long delta = offset * amplitude / 21600000;
			return new BigDecimal(base + amplitude - delta);
		} else if (offset <= 64800000) {
			offset -= 43200000;
			long delta = offset * amplitude / 21600000;
			return new BigDecimal(base - delta);
		} else {
			offset -= 64800000;
			long delta = offset * amplitude / 21600000;
			return new BigDecimal(base - amplitude + delta);
		}
	}

	private BigDecimal calculateValueCumulative(Calendar cal, long amplitude)
			throws UnsupportedException, IOException {
		int utcOffset = (cal.get(Calendar.ZONE_OFFSET) + cal
				.get(Calendar.DST_OFFSET));
		long localTime = (cal.getTime().getTime() + utcOffset) / 1000; // seconds
		long value = localTime % 1000000; // overflow is 1000000
		long step = (localTime % 86400) / getProfileInterval();
		if (amplitude > getProfileInterval()) {
			amplitude = getProfileInterval();
		}
		long delta = amplitude / steps;
		if (delta == 0) {
			delta = 1;
		}

		if (step <= steps) {
			long factor = getFactor1(step);
			return new BigDecimal(value + (factor * delta));
		} else if (step <= steps * 2) {
			step = steps - (step - steps);
			long factor = getFactor1(steps) + getFactor2(step);
			return new BigDecimal(value + (factor * delta));
		} else if (step <= steps * 3) {
			step -= (steps * 2);
			long factor = getFactor1(steps) + getFactor2(0) - getFactor1(step);
			return new BigDecimal(value + (factor * delta));
		} else {
			step -= (steps * 2);
			step = steps - (step - steps);
			long factor = getFactor2(0) - getFactor2(step);
			return new BigDecimal(value + (factor * delta));
		}
	}

	private long getFactor1(long step) {
		long result = 0;
		for (long i = 0; i <= step; i++) {
			result += i;
		}
		return result;
	}

	private long getFactor2(long step) {
		long result = 0;
		for (long i = steps - 1; i >= step; i--) {
			result += i;
		}
		return result;
	}

    protected String getRegistersInfo(int extendedLogging) throws IOException {
        getLogger().info("call overrided method getRegistersInfo("+extendedLogging+")");
        getLogger().info("--> You can provide info about meter register configuration here. If the ExtendedLogging property is set, that info will be logged.");
        return "1.1.1.8.1.255 Active Import energy";
    }

    /*******************************************************************************************
     R e g i s t e r P r o t o c o l  i n t e r f a c e
     *******************************************************************************************/
    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        //getLogger().info("call overridden method translateRegister()");
        return new RegisterInfo(obisCode.getDescription());
    }

    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
    	getLogger().info("call overrided method readRegister("+obisCode+")");
        getLogger().info("--> request the register from the meter here");

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

		Boolean suspect = false;
		Calendar cal = Calendar.getInstance();
        if (getProfileInterval() <= 0) {
			throw new IOException("load profile interval must be > 0 sec. (is "+getProfileInterval()+")");
		}
        ParseUtils.roundDown2nearestInterval(cal, getProfileInterval());
		cal.set(Calendar.MILLISECOND, 0);
		if((cal.getTime().getTime() % (15*60*1000)) == 0) // insert a suspected value every 15 minutes
			suspect = true;

        if (obisCode.getA() == 1) {
			if (obisCode.getD() == 8) {
		        if (obisCode.getE() > 0) {
					Quantity quantity = new Quantity(new BigDecimal("" + (((System.currentTimeMillis() / 1000) * 2) % 10000) * obisCode.getB()), Unit.get("kWh"));
					return new RegisterValue(obisCode, quantity, eventTime, fromTime, toTime);
		        }
		        else {
		        	Quantity quantity = new Quantity(new BigDecimal("" + ((System.currentTimeMillis() / 1000) % 10000) * obisCode.getB()), Unit.get("kWh"));
					return new RegisterValue(obisCode, quantity, eventTime, fromTime, toTime);
		        }
	        }
	        else {
	        	if (obisCode.getE() > 0) {
					Quantity quantity = null;
					if(suspect==false)
						quantity = new Quantity(new BigDecimal("12345678.8").multiply(new BigDecimal("2")),Unit.get("kWh"));
					else
						quantity = new Quantity(new BigDecimal("11945679.8").multiply(new BigDecimal("2")),Unit.get("kWh"));
					return new RegisterValue(obisCode, quantity, eventTime, fromTime, toTime);
	        	}
	        	else {
					Quantity quantity = null;
					if(suspect==false)
						quantity = new Quantity(new BigDecimal("12345678.8"),Unit.get("kWh"));
					else
						quantity = new Quantity(new BigDecimal("11945679.8"),Unit.get("kWh"));
					return new RegisterValue(obisCode, quantity, eventTime, fromTime, toTime);
	        	}
	        }
        }
        throw new NoSuchRegisterException("Register "+obisCode+" not supported!");
    }

    protected void doValidateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
        // Override or add new properties here e.g. below
        setEICTTestProperty(Integer.parseInt(properties.getProperty("EICTTestProperty", "123")));
       	setLoadProfileObisCode(ObisCode.fromString(properties.getProperty("LoadProfileObisCode", "0.0.99.1.0.255")));
    }

    protected List doGetOptionalKeys() {
        List list = new ArrayList();
        //add new properties here, e.g. below
        list.add("EICTTestProperty");
        list.add("LoadProfileObisCode");
        return list;
    }

    protected ProtocolConnection doInit(InputStream inputStream,OutputStream outputStream,int timeoutProperty,int protocolRetriesProperty,int forcedDelay,int echoCancelling,int protocolCompatible,Encryptor encryptor,HalfDuplexController halfDuplexController) throws IOException {
        getLogger().info("call doInit(...)");
        getLogger().info("--> construct the ProtocolConnection and all other object here");

        connection = new EICTTestProtocolConnection(inputStream,outputStream,timeoutProperty,protocolRetriesProperty,forcedDelay,echoCancelling,protocolCompatible,encryptor,getLogger());
        return connection;
    }

    public int getNumberOfChannels() throws UnsupportedException, IOException {
        getLogger().info("call overrided method getNumberOfChannels() (return 2 as sample)");
        getLogger().info("--> report the nr of load profile channels in the meter here");
        return 2;
    }

    public Date getTime() throws IOException {
        getLogger().info("call getTime() (if time is different from system time taken into account the properties, setTime will be called) ");
        getLogger().info("--> request the metertime here");
        long currenttime = new Date().getTime();
        return new Date(currenttime-(1000*30));
    }

    public void setTime() throws IOException {
        getLogger().info("call setTime() (this method is called automatically when needed)");
        getLogger().info("--> sync the metertime with the systemtime here");
    }

    public String getProtocolVersion() {
        //getLogger().info("call getProtocolVersion()");
        return "$Date: 2013-10-31 11:22:19 +0100 (Thu, 31 Oct 2013) $";
        //return "EICT Test protocol version";
    }

    public String getFirmwareVersion() throws IOException, UnsupportedException {
        getLogger().info("call getFirmwareVersion()");
        getLogger().info("--> report the firmware version and other important meterinfo here");
        return "EICT Test firmware version";
    }

    public int getEICTTestProperty() {
        return eICTTestProperty;
    }

    public void setEICTTestProperty(int eICTTestProperty) {
        this.eICTTestProperty = eICTTestProperty;
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
        if(rtuid != 0){
            /* Use the RTUCache to set the blob (cache) to the database */
            RTUCache rtu = new RTUCache(rtuid, ormClient);
            rtu.setBlob(cacheObject);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void setCache(Object cacheObject) {
        this.cache = (CacheObject)cacheObject;
    }

    /**
     * {@inheritDoc}
     */
    public Object fetchCache(int rtuid) throws SQLException {
        if(rtuid != 0){

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
}
