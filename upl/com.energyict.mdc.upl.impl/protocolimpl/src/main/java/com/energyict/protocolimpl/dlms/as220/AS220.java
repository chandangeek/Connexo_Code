/**
 * @version  2.0
 * @author   Koenraad Vanderschaeve
 * <P>
 * <B>Description :</B><BR>
 * Class that implements the Siemens ZMD DLMS profile implementation
 * <BR>
 * <B>@beginchanges</B><BR>
KV|08042003|Initial version
KV|08102003|Set default of RequestTimeZone to 0
KV|10102003|generate OTHER MeterEvent when statusbit is not supported
KV|27102003|changed code for correct dst transition S->W
KV|20082004|Extended with obiscode mapping for register reading
KV|17032005|improved registerreading
KV|23032005|Changed header to be compatible with protocol version tool
KV|30032005|Improved registerreading, configuration data
KV|31032005|Handle DataContainerException
KV|15072005|applyEvents() done AFTER getting the logbook!
KV|10102006|extension to support cumulative values in load profile
KV|10102006|fix to support 64 bit values in load profile
KV|29042009|as220

 * @endchanges
 */

package com.energyict.protocolimpl.dlms.as220;


import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;

import com.energyict.dlms.DataContainer;
import com.energyict.dlms.ScalerUnit;
import com.energyict.dlms.axrdencoding.TypeEnum;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.IntervalStateBits;
import com.energyict.protocol.InvalidPropertyException;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.MessageProtocol;
import com.energyict.protocol.MessageResult;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.MeterProtocol;
import com.energyict.protocol.MissingPropertyException;
import com.energyict.protocol.NoSuchRegisterException;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterProtocol;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.messaging.Message;
import com.energyict.protocol.messaging.MessageAttribute;
import com.energyict.protocol.messaging.MessageCategorySpec;
import com.energyict.protocol.messaging.MessageElement;
import com.energyict.protocol.messaging.MessageSpec;
import com.energyict.protocol.messaging.MessageTag;
import com.energyict.protocol.messaging.MessageTagSpec;
import com.energyict.protocol.messaging.MessageValue;
import com.energyict.protocolimpl.base.ParseUtils;

public class AS220 extends DLMSSNAS220 implements RegisterProtocol, MessageProtocol {

	private static final String	CONNECT									= "ConnectLoad";
	private static final String	DISCONNECT								= "DisconnectLoad";
	private static final String	ARM										= "ArmMeter";
	private static final String	TARIFF_OPTION_SWITCH_BASE				= "TariffOptionSwitchBase";
	private static final String	TARIFF_OPTION_SWITCH_DAYNIGHT			= "TariffOptionSwitchDayNight";

	private static final String	CONNECT_DISPLAY							= "Connect Load";
	private static final String	DISCONNECT_DISPLAY						= "Disconnect Load";
	private static final String	ARM_DISPLAY								= "Arm Meter";
	private static final String	TARIFF_OPTION_SWITCH_BASE_DISPLAY		= "Switch tariff option BASE";
	private static final String	TARIFF_OPTION_SWITCH_DAYNIGHT_DISPLAY	= "Switch tariff option DAY/NIGHT";

    public AS220() {

    }

    @Override
	protected String getDeviceID() {
        return "GEC";
    }

    //KV 27102003
	public Calendar initCalendarSW(boolean protocolDSTFlag, TimeZone timeZone) {
		Calendar calendar;
		if (protocolDSTFlag) {
			calendar = Calendar.getInstance(ProtocolUtils.getSummerTimeZone(timeZone));
		} else {
			calendar = Calendar.getInstance(ProtocolUtils.getWinterTimeZone(timeZone));
		}
		return calendar;
	}

    private List<MeterEvent> readMainLogbook(Calendar fromCalendar, Calendar toCalendar) throws IOException {

    	DataContainer dc = getCosemObjectFactory().getProfileGeneric(ObisCode.fromString("0.0.99.98.1.255")).getBuffer(fromCalendar,toCalendar);

        if (isDebug()) {
			System.out.println("readMainLogbook");
			dc.printDataContainer();
		}

		List<MeterEvent> meterEvents = new ArrayList<MeterEvent>();
		for (int i = 0; i < dc.getRoot().getNrOfElements(); i++) {
			Date dateTime = dc.getRoot().getStructure(i).getOctetString(0).toDate(getTimeZone());
			int id = 0;
			id = dc.getRoot().getStructure(i).getInteger(1);
			MeterEvent meterEvent = EventNumber.toMeterEvent(id, dateTime);
			if (meterEvent != null) {
				meterEvents.add(meterEvent);
			}
		}

        return meterEvents;
    }

    private List<MeterEvent> readVoltageCutLogbook(Calendar fromCalendar, Calendar toCalendar) throws IOException {

        DataContainer dc = getCosemObjectFactory().getProfileGeneric(ObisCode.fromString("0.0.99.98.5.255")).getBuffer(fromCalendar,toCalendar);

        if (isDebug()) {
			System.out.println("readVoltageCutLogbook");
			dc.printDataContainer();
		}


		List<MeterEvent> meterEvents = new ArrayList<MeterEvent>();
		for (int i = 0; i < dc.getRoot().getNrOfElements(); i++) {
			Date dateTime = dc.getRoot().getStructure(i).getOctetString(0).toDate(getTimeZone());
			int id = dc.getRoot().getStructure(i).getInteger(1);
			MeterEvent meterEvent = EventNumber.toMeterEvent(id, dateTime);
			if (meterEvent != null) {
				meterEvents.add(meterEvent);
			}
		}

        return meterEvents;
    }

	private List<MeterEvent> readCoverLogbook(Calendar fromCalendar, Calendar toCalendar) throws IOException {

		DataContainer dc = getCosemObjectFactory().getProfileGeneric(ObisCode.fromString("0.0.99.98.2.255")).getBuffer(fromCalendar, toCalendar);

		if (isDebug()) {
			System.out.println("readCoverLogbook");
			dc.printDataContainer();
		}

		List<MeterEvent> meterEvents = new ArrayList<MeterEvent>();
		for (int i = 0; i < dc.getRoot().getNrOfElements(); i++) {
			Date dateTime = dc.getRoot().getStructure(i).getOctetString(0).toDate(getTimeZone());
			int id = dc.getRoot().getStructure(i).getInteger(1);
			MeterEvent meterEvent = EventNumber.toMeterEvent(id, dateTime);
			if (meterEvent != null) {
				meterEvents.add(meterEvent);
			}
		}

		return meterEvents;
	}

    @Override
	protected void getEventLog(ProfileData profileData,Calendar fromCalendar, Calendar toCalendar) throws IOException {
        List<MeterEvent> meterEvents = new ArrayList<MeterEvent>();
        meterEvents.addAll(readMainLogbook(fromCalendar, toCalendar));
        meterEvents.addAll(readVoltageCutLogbook(fromCalendar, toCalendar));
        meterEvents.addAll(readCoverLogbook(fromCalendar, toCalendar));
        profileData.setMeterEvents(meterEvents);
    }

    @Override
	protected void buildProfileData(byte bNROfChannels,ProfileData profileData,ScalerUnit[] scalerunit,List<LoadProfileCompactArrayEntry> loadProfileCompactArrayEntries)  throws IOException {

        int i;
        List<IntervalData> intervalDatas = new ArrayList<IntervalData>();
        int latestProfileInterval= getProfileInterval();
        int eiCode=0;
        if (isDebug()) {
			System.out.println("loadProfileCompactArrayEntries.size() = "+loadProfileCompactArrayEntries.size());
		}

        LoadProfileCompactArrayEntry dateStamp=null;
        Calendar calendar = null;
        for (i=0;i<loadProfileCompactArrayEntries.size();i++) {
        	LoadProfileCompactArrayEntry lpcae = loadProfileCompactArrayEntries.get(i);
        	if (isDebug()) {
				System.out.println(lpcae);
			}

        	if (lpcae.isValue()) { // normal interval value
        		if (calendar == null) {
					continue; // first the calendar has to be initialized with the start of load profile marker
				}
        		IntervalData ivd = new IntervalData(calendar.getTime());
        		ivd.addValue(new BigDecimal(""+lpcae.getValue()));
        		intervalDatas.add(ivd);
        		latestProfileInterval = lpcae.getIntervalInSeconds();
           		calendar.add(Calendar.SECOND,latestProfileInterval); // set the calendar to the next interval endtime
        	}
        	else if (lpcae.isPartialValue()) { // partial interval value
        		//eiCode |= IntervalStateBits.SHORTLONG;
        		if (calendar == null) {
					continue; // first the calendar has to be initialized with the start of load profile marker
				}
        		IntervalData ivd = new IntervalData(calendar.getTime(),eiCode);
        		eiCode = 0;
        		ivd.addValue(new BigDecimal(""+lpcae.getValue()));
        		intervalDatas.add(ivd);
        		latestProfileInterval = lpcae.getIntervalInSeconds();
        		calendar.add(Calendar.SECOND,latestProfileInterval); // set the calendar to the next interval endtime
        	}
        	else if (lpcae.isDate()) { // date stamp
        		// date always followed by time? Do the processing if time is received
        		dateStamp = lpcae;
        	}
        	else if (lpcae.isTime()) { // time stamp
        		if (dateStamp ==null) {

        			// change of the interval...
        			// only timestamp is received...
        			// adjust time here...
        		}
        		else {
        			// set the calendar
            		calendar = ProtocolUtils.getCleanCalendar(getTimeZone());
            		calendar.set(Calendar.YEAR, dateStamp.getYear());
            		calendar.set(Calendar.MONTH, dateStamp.getMonth());
            		calendar.set(Calendar.DATE, dateStamp.getDay());
            		calendar.set(Calendar.HOUR_OF_DAY, lpcae.getHours());
            		calendar.set(Calendar.MINUTE, lpcae.getMinutes());
            		calendar.set(Calendar.SECOND, lpcae.getSeconds());
            		dateStamp = null; // reset the dateStamp

	            	if (lpcae.isStartOfLoadProfile()) {
	            		// do nothing special...
	            	}
	            	else if (lpcae.isPowerOff()) {
	            		ParseUtils.roundUp2nearestInterval(calendar, latestProfileInterval);
	            		eiCode = IntervalStateBits.POWERDOWN;
	            	}
	            	else if (lpcae.isPowerOn()) {
	            		ParseUtils.roundUp2nearestInterval(calendar, latestProfileInterval);
	            		eiCode = IntervalStateBits.POWERUP;
	            	}
	            	else if (lpcae.isChangeclockOldTime()) {
	            		ParseUtils.roundUp2nearestInterval(calendar, latestProfileInterval);
	            		eiCode = IntervalStateBits.SHORTLONG;

	            	}
	            	else if (lpcae.isChangeclockNewTime()) {
	            		ParseUtils.roundUp2nearestInterval(calendar, latestProfileInterval);
	            		eiCode = IntervalStateBits.SHORTLONG;
	            	}
        		}
        	} // time


        } // for (i=0;i<loadProfileCompactArrayEntries.size();i++) {

        if (isDebug()) {
			System.out.println("\nintervalDatas.size() = "+intervalDatas.size());
		}
        profileData.setIntervalDatas(intervalDatas);

    } // ProfileData buildProfileData(...)

    @Override
	protected byte[] getLowLevelSecurity() {
        final byte[] aarqlowlevelAS220 = {(byte)0xE6,(byte)0xE6,(byte)0x00,
                0x60, 0x36, (byte) 0xA1, 0x09, 0x06, 0x07,
                0x60, (byte)0x85, 0x74, 0x05, 0x08, 0x01,
                0x02, (byte)0x8A, 0x02, 0x07, (byte)0x80, (byte)0x8B,
                0x07, 0x60, (byte)0x85, 0x74, 0x05, 0x08,
                0x02, 0x01};

        final byte[] aarqlowlevelAS220_2= {(byte)0xBE, 0x10, 0x04, 0x0E,
                0x01, 0x00, 0x00, 0x00, 0x06, 0x5F,
                0x1F, 0x04, 0x00, 0x18, 0x02, 0x20,
                0x00, (byte)0xEF};

    	return buildaarq(aarqlowlevelAS220,aarqlowlevelAS220_2);
    }

    @Override
	protected void doValidateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
        try {
            Iterator<String> iterator= getRequiredKeys().iterator();
            while (iterator.hasNext()) {
                String key = iterator.next();
                if (properties.getProperty(key) == null) {
					throw new MissingPropertyException(key + " key missing");
				}
            }
            strID = properties.getProperty(MeterProtocol.ADDRESS);
            // KV 19012004
            if ((strID != null) &&(strID.length()>16)) {
				throw new InvalidPropertyException("ID must be less or equal then 16 characters.");
			}

            strPassword = properties.getProperty(MeterProtocol.PASSWORD);
            iHDLCTimeoutProperty=Integer.parseInt(properties.getProperty("Timeout","10000").trim());
            iProtocolRetriesProperty=Integer.parseInt(properties.getProperty("Retries","5").trim());
            iDelayAfterFailProperty=Integer.parseInt(properties.getProperty("DelayAfterfail","3000").trim());
            iRequestTimeZone=Integer.parseInt(properties.getProperty("RequestTimeZone","0").trim());
            iRequestClockObject=Integer.parseInt(properties.getProperty("RequestClockObject","0").trim());
            iRoundtripCorrection=Integer.parseInt(properties.getProperty("RoundtripCorrection","0").trim());
            iSecurityLevelProperty=Integer.parseInt(properties.getProperty("SecurityLevel","1").trim());
            iClientMacAddress=Integer.parseInt(properties.getProperty("ClientMacAddress","32").trim());
            iServerUpperMacAddress=Integer.parseInt(properties.getProperty("ServerUpperMacAddress","1").trim());
            iServerLowerMacAddress=Integer.parseInt(properties.getProperty("ServerLowerMacAddress","0").trim());

        }
        catch (NumberFormatException e) {
            throw new InvalidPropertyException("DukePower, validateProperties, NumberFormatException, "+e.getMessage());
        }
    }

    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        try {
			ObisCodeMapper ocm = new ObisCodeMapper(getCosemObjectFactory());
			return ocm.getRegisterValue(obisCode);
		} catch (Exception e) {
			throw new NoSuchRegisterException("Problems while reading register " + obisCode.toString() + ": " + e.getMessage());
		}
    }

    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        return ObisCodeMapper.getRegisterInfo(obisCode);
    }

	public List<MessageCategorySpec> getMessageCategories() {
        List<MessageCategorySpec> theCategories = new ArrayList<MessageCategorySpec>();
        MessageCategorySpec cat = new MessageCategorySpec("BasicMessages");

        MessageSpec msgSpec = addBasicMsg(DISCONNECT_DISPLAY, DISCONNECT, false);
        cat.addMessageSpec(msgSpec);

        msgSpec = addBasicMsg(ARM_DISPLAY, ARM, false);
        cat.addMessageSpec(msgSpec);

        msgSpec = addBasicMsg(CONNECT_DISPLAY, CONNECT, false);
        cat.addMessageSpec(msgSpec);

        msgSpec = addBasicMsg(TARIFF_OPTION_SWITCH_BASE_DISPLAY, TARIFF_OPTION_SWITCH_BASE, false);
        cat.addMessageSpec(msgSpec);

        msgSpec = addBasicMsg(TARIFF_OPTION_SWITCH_DAYNIGHT_DISPLAY, TARIFF_OPTION_SWITCH_DAYNIGHT, false);
        cat.addMessageSpec(msgSpec);

        theCategories.add(cat);
        return theCategories;
	}

    private MessageSpec addBasicMsg(String keyId, String tagName, boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        msgSpec.add(tagSpec);
        return msgSpec;
    }

	public String writeMessage(Message msg) {
		return msg.write(this);
	}

    @SuppressWarnings("unchecked")
	public String writeTag(MessageTag msgTag) {
        StringBuffer buf = new StringBuffer();

        // a. Opening tag
        buf.append("<");
        buf.append( msgTag.getName() );

        // b. Attributes
        for (Iterator<MessageAttribute> it = msgTag.getAttributes().iterator(); it.hasNext();) {
            MessageAttribute att = it.next();
            if ((att.getValue()==null) || (att.getValue().length()==0)) {
				continue;
			}
            buf.append(" ").append(att.getSpec().getName());
            buf.append("=").append('"').append(att.getValue()).append('"');
        }
        buf.append(">");

        // c. sub elements
        for (Iterator<MessageElement> it = msgTag.getSubElements().iterator(); it.hasNext();) {
            MessageElement elt = it.next();
            if (elt.isTag()) {
				buf.append( writeTag((MessageTag)elt) );
			} else if (elt.isValue()) {
                String value = writeValue((MessageValue)elt);
                if ((value==null) || (value.length()==0)) {
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

	@SuppressWarnings("unchecked")
	public String writeTag2(MessageTag msgTag) {
        StringBuffer buf = new StringBuffer();

        // a. Opening tag
        buf.append("<");
        buf.append(msgTag.getName());

        // b. Attributes
        for (Iterator<MessageAttribute> it = msgTag.getAttributes().iterator(); it.hasNext();) {
            MessageAttribute att = it.next();
            if ((att.getValue() == null) || (att.getValue().length() == 0)) {
				continue;
			}
            buf.append(" ").append(att.getSpec().getName());
            buf.append("=").append('"').append(att.getValue()).append('"');
        }
        if (msgTag.getSubElements().isEmpty()) {
            buf.append("/>");
            return buf.toString();
        }
        buf.append(">");
        // c. sub elements
        for (Iterator<MessageElement> it = msgTag.getSubElements().iterator(); it.hasNext();) {
            MessageElement elt = it.next();
            if (elt.isTag()) {
				buf.append(writeTag((MessageTag) elt));
			} else if (elt.isValue()) {
                String value = writeValue((MessageValue) elt);
                if ((value == null) || (value.length() == 0)) {
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

	public String writeValue(MessageValue msgValue) {
		return msgValue.getValue();
	}

	@SuppressWarnings("unchecked")
	public void applyMessages(List messageEntries) throws IOException {

	}

	public MessageResult queryMessage(MessageEntry messageEntry) {
		try {
			if (messageEntry.getContent().indexOf("<" + DISCONNECT) >= 0) {
				getLogger().info("DISCONNECT message received");
				getCosemObjectFactory().getDisconnector(ObisCode.fromString("0.0.96.3.10.255")).writeControlState(new TypeEnum(0));
				if (isDebug()) {
					System.out.println("DISCONNECT message received");
				}
			} else if (messageEntry.getContent().indexOf("<" + CONNECT) >= 0) {
				getLogger().info("CONNECT message received");
				getCosemObjectFactory().getDisconnector(ObisCode.fromString("0.0.96.3.10.255")).writeControlState(new TypeEnum(1));
				if (isDebug()) {
					System.out.println("CONNECT message received");
				}
			} else if (messageEntry.getContent().indexOf("<" + ARM) >= 0) {
				getLogger().info("ARM message received");
				getCosemObjectFactory().getDisconnector(ObisCode.fromString("0.0.96.3.10.255")).writeControlState(new TypeEnum(2));
				if (isDebug()) {
					System.out.println("ARM message received");
				}
			} else if (messageEntry.getContent().indexOf("<" + TARIFF_OPTION_SWITCH_BASE) >= 0) {
				getLogger().info("TARIFF_OPTION_SWITCH_BASE message received");
				getCosemObjectFactory().getData(ObisCode.fromString("0.0.96.50.0.255")).setValueAttr(new TypeEnum(0));
				if (isDebug()) {
					System.out.println("TARIFF_OPTION_SWITCH_BASE message received");
				}
			} else if (messageEntry.getContent().indexOf("<" + TARIFF_OPTION_SWITCH_DAYNIGHT) >= 0) {
				getLogger().info("TARIFF_OPTION_SWITCH_DAYNIGHT message received");
				getCosemObjectFactory().getData(ObisCode.fromString("0.0.96.50.0.255")).setValueAttr(new TypeEnum(1));
				if (isDebug()) {
					System.out.println("TARIFF_OPTION_SWITCH_DAYNIGHT message received");
				}
			}
			return MessageResult.createSuccess(messageEntry);
		} catch (IOException e) {
			getLogger().severe("QueryMessage(), " + e.getMessage());
			return MessageResult.createFailed(messageEntry);
		}
	}

} // public class DLMSZMD
