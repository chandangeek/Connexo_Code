/*
 * EICTTestProtocol.java
 *
 * Created on 20 may 2010, 14:34
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package test.com.energyict.protocolimpl.eicttest;

import com.energyict.mdc.upl.NoSuchRegisterException;
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
import com.energyict.protocolimpl.dlms.common.ObisCodePropertySpec;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;

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
 *
 * @author fde
 * test.com.energyict.protocolimpl.eicttest.EICTTestProtocol
 */
public class EICTTestProtocol extends AbstractProtocol implements MessageProtocol, CachingProtocol {

    private static final String FIRMWAREPROGRAM = "UpgradeMeterFirmware";
	private static final String FIRMWAREPROGRAM_DISPLAY_1 = "Upgrade Meter Firmware 1";
	private static final String FIRMWAREPROGRAM_DISPLAY_2 = "Upgrade Meter Firmware 2";
	private static final String FIRMWAREPROGRAM_DISPLAY_3 = "Upgrade Meter Firmware 3";
	private static final String INCLUDE_FILE_TAG = "FirmwareFileID";
    private static final String PK_TEST_PROPERTY = "EICTTestProperty";
    private static final String PK_LOAD_PROFILE_OBIS_CODE = "LoadProfileObisCode";

    private CacheObject cache;

	private EICTTestProtocolConnection connection;
	private int eICTTestProperty;
	private ObisCode loadProfileObisCode;

	private long steps;

	@Override
    public void applyMessages(List messageEntries) throws IOException {
        Iterator it = messageEntries.iterator();
        while (it.hasNext()) {
            MessageEntry messageEntry = (MessageEntry)it.next();
            //System.out.println(messageEntry);
        }
    }

	@Override
    public MessageResult queryMessage(MessageEntry messageEntry) throws IOException {

    	getLogger().info("MessageEntry: "+messageEntry.getContent());

        return MessageResult.createSuccess(messageEntry);
        //messageEntry.setTrackingId("tracking ID for "+messageEntry.);
        //return MessageResult.createQueued(messageEntry);
        //return MessageResult.createFailed(messageEntry);
        //return MessageResult.createUnknown(messageEntry);
    }

	@Override
    public List getMessageCategories() {
        List<MessageCategorySpec> theCategories = new ArrayList<>();
        // General Parameters
        MessageCategorySpec cat = new MessageCategorySpec("SAMPLE");
		cat.addMessageSpec(addBasicMsg("Disconnect meter", "DISCONNECT", false));
		cat.addMessageSpec(addBasicMsg("Connect meter", "CONNECT", false));
		cat.addMessageSpec(addBasicMsg("Limit current to 6A", "LIMITCURRENT6A", false));
		cat.addMessageSpec(addBasicMsg(FIRMWAREPROGRAM_DISPLAY_1, FIRMWAREPROGRAM, true));
		cat.addMessageSpec(addUpgradeMsg1(FIRMWAREPROGRAM_DISPLAY_2, FIRMWAREPROGRAM, true));
		cat.addMessageSpec(addUpgradeMsg2(FIRMWAREPROGRAM_DISPLAY_3, FIRMWAREPROGRAM, true));

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

	@Override
    public String writeMessage(Message msg) {
        return msg.write(this);
    }

	@Override
    public String writeTag(MessageTag msgTag) {
        StringBuilder builder = new StringBuilder();

        // a. Opening tag
        builder.append("<");
        builder.append( msgTag.getName() );

        // b. Attributes
        for (Iterator<MessageAttribute> it = msgTag.getAttributes().iterator(); it.hasNext();) {
            MessageAttribute att = it.next();
            if ((att.getValue()==null) || (att.getValue().isEmpty())) {
				continue;
			}
            builder.append(" ").append(att.getSpec().getName());
            builder.append("=").append('"').append(att.getValue()).append('"');
        }
        builder.append(">");

        // c. sub elements
        for (Iterator it = msgTag.getSubElements().iterator(); it.hasNext();) {
            MessageElement elt = (MessageElement)it.next();
            if (elt.isTag()) {
				builder.append( writeTag((MessageTag)elt) );
			} else if (elt.isValue()) {
                String value = writeValue((MessageValue)elt);
                if ((value==null) || (value.isEmpty())) {
					return "";
				}
                builder.append(value);
            }
        }

        // d. Closing tag
        builder.append("</");
        builder.append( msgTag.getName() );
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

        if(this.cache != null){
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
        this.cache.setText("Hi I'm cached data -> " + Long.toString(Calendar.getInstance().getTimeInMillis()));
    }

	@Override
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

		if (!isCumulative) {
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

			if (!isCumulative) {
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
		int utcOffset = (cal.get(Calendar.ZONE_OFFSET) + cal.get(Calendar.DST_OFFSET));
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

	private BigDecimal calculateValueCumulative(Calendar cal, long amplitude) throws IOException {
		int utcOffset = (cal.get(Calendar.ZONE_OFFSET) + cal.get(Calendar.DST_OFFSET));
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

	@Override
    protected String getRegistersInfo(int extendedLogging) throws IOException {
        getLogger().info("call overrided method getRegistersInfo("+extendedLogging+")");
        getLogger().info("--> You can provide info about meter register configuration here. If the ExtendedLogging property is set, that info will be logged.");
        return "1.1.1.8.1.255 Active Import energy";
    }

	@Override
    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        return new RegisterInfo(obisCode.toString());
    }

	@Override
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
		if ((cal.getTime().getTime() % (15*60*1000)) == 0) // insert a suspected value every 15 minutes
		{
			suspect = true;
		}

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
					Quantity quantity;
					if (!suspect) {
						quantity = new Quantity(new BigDecimal("12345678.8").multiply(new BigDecimal("2")), Unit.get("kWh"));
					} else {
						quantity = new Quantity(new BigDecimal("11945679.8").multiply(new BigDecimal("2")), Unit.get("kWh"));
					}
					return new RegisterValue(obisCode, quantity, eventTime, fromTime, toTime);
	        	}
	        	else {
					Quantity quantity;
					if (!suspect) {
						quantity = new Quantity(new BigDecimal("12345678.8"), Unit.get("kWh"));
					} else {
						quantity = new Quantity(new BigDecimal("11945679.8"), Unit.get("kWh"));
					}
					return new RegisterValue(obisCode, quantity, eventTime, fromTime, toTime);
	        	}
	        }
        }
        throw new NoSuchRegisterException("Register "+obisCode+" not supported!");
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        List<PropertySpec> propertySpecs = new ArrayList<>(super.getPropertySpecs());
        propertySpecs.add(UPLPropertySpecFactory.integer(PK_TEST_PROPERTY, false));
        propertySpecs.add(new ObisCodePropertySpec(PK_TEST_PROPERTY, false));
        return propertySpecs;
    }

    @Override
	public void setProperties(TypedProperties properties) throws PropertyValidationException {
		super.setProperties(properties);
        setEICTTestProperty(Integer.parseInt(properties.getTypedProperty(PK_TEST_PROPERTY, "123")));
       	setLoadProfileObisCode(ObisCode.fromString(properties.getTypedProperty(PK_LOAD_PROFILE_OBIS_CODE, "0.0.99.1.0.255")));
    }

    @Override
    protected ProtocolConnection doInit(InputStream inputStream,OutputStream outputStream,int timeoutProperty,int protocolRetriesProperty,int forcedDelay,int echoCancelling,int protocolCompatible,Encryptor encryptor,HalfDuplexController halfDuplexController) throws IOException {
        getLogger().info("call doInit(...)");
        getLogger().info("--> construct the ProtocolConnection and all other object here");

        connection = new EICTTestProtocolConnection(inputStream,outputStream,timeoutProperty,protocolRetriesProperty,forcedDelay,echoCancelling,protocolCompatible,encryptor,getLogger());
        return connection;
    }

    @Override
    public int getNumberOfChannels() throws IOException {
        getLogger().info("call overrided method getNumberOfChannels() (return 2 as sample)");
        getLogger().info("--> report the nr of load profile channels in the meter here");
        return 2;
    }

    @Override
    public Date getTime() throws IOException {
        getLogger().info("call getTime() (if time is different from system time taken into account the properties, setTime will be called) ");
        getLogger().info("--> request the metertime here");
        long currenttime = new Date().getTime();
        return new Date(currenttime-(1000*30));
    }

    @Override
    public void setTime() throws IOException {
        getLogger().info("call setTime() (this method is called automatically when needed)");
        getLogger().info("--> sync the metertime with the systemtime here");
    }

    @Override
    public String getProtocolVersion() {
        //getLogger().info("call getProtocolVersion()");
        return "$Date: 2015-11-26 15:25:15 +0200 (Thu, 26 Nov 2015)$";
        //return "EICT Test protocol version";
    }

    @Override
    public String getFirmwareVersion() throws IOException {
        getLogger().info("call getFirmwareVersion()");
        getLogger().info("--> report the firmware version and other important meterinfo here");
        return "EICT Test firmware version";
    }

    public int getEICTTestProperty() {
        return eICTTestProperty;
    }

    private void setEICTTestProperty(int eICTTestProperty) {
        this.eICTTestProperty = eICTTestProperty;
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
        this.cache = (CacheObject)cacheObject;
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

}