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
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import com.energyict.dlms.ScalerUnit;
import com.energyict.dlms.axrdencoding.TypeEnum;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.IntervalStateBits;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.MessageProtocol;
import com.energyict.protocol.MessageResult;
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
import com.energyict.protocolimpl.base.ContactorController;
import com.energyict.protocolimpl.base.ParseUtils;

public class AS220 extends DLMSSNAS220 implements RegisterProtocol, MessageProtocol {

	private static final String			CONNECT									= "ConnectLoad";
	private static final String			DISCONNECT								= "DisconnectLoad";
	private static final String			ARM										= "ArmMeter";
	private static final String			TARIFF_OPTION_SWITCH_BASE				= "TariffOptionSwitchBase";
	private static final String			TARIFF_OPTION_SWITCH_DAYNIGHT			= "TariffOptionSwitchDayNight";

	private static final String			CONNECT_DISPLAY							= "Connect Load";
	private static final String			DISCONNECT_DISPLAY						= "Disconnect Load";
	private static final String			ARM_DISPLAY								= "Arm Meter";
	private static final String			TARIFF_OPTION_SWITCH_BASE_DISPLAY		= "Switch tariff option BASE";
	private static final String			TARIFF_OPTION_SWITCH_DAYNIGHT_DISPLAY	= "Switch tariff option DAY/NIGHT";

	private final ContactorController contactorController;

    public AS220() {
    	this.contactorController = new AS220ContactorController(this);
    }

    public ContactorController getContactorController() {
    	return contactorController;
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
        final byte[] aarqlowlevelAS220 = {
        		(byte)0xE6, (byte)0xE6, (byte)0x00,
        		(byte)0x60,
        		(byte)0x36,
        		(byte)0xA1, (byte)0x09, (byte)0x06, (byte)0x07,
        		(byte)0x60, (byte)0x85, (byte)0x74, (byte)0x05, (byte)0x08, (byte)0x01, (byte)0x02,
        		(byte)0x8A, (byte)0x02, (byte)0x07, (byte)0x80,
        		(byte)0x8B, (byte)0x07, (byte)0x60, (byte)0x85, (byte)0x74, (byte)0x05, (byte)0x08, (byte)0x02, (byte)0x01};

        final byte[] aarqlowlevelAS220_2= {
        		(byte)0xBE, (byte)0x10, (byte)0x04, (byte)0x0E,
        		(byte)0x01,
        		(byte)0x00, (byte)0x00, (byte)0x00,
        		(byte)0x06,
        		(byte)0x5F, (byte)0x1F, (byte)0x04, (byte)0x00, (byte)0x18, (byte)0x02, (byte)0x20,
                (byte)0x00, (byte)0xEF};

    	return AS220AAREUtil.buildaarq(aarqlowlevelAS220,aarqlowlevelAS220_2, strPassword);
    }

    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        try {
			ObisCodeMapper ocm = new ObisCodeMapper(getCosemObjectFactory());
			return ocm.getRegisterValue(obisCode);
		} catch (Exception e) {
			e.printStackTrace();
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
				getContactorController().doDisconnect();
			} else if (messageEntry.getContent().indexOf("<" + CONNECT) >= 0) {
				getContactorController().doConnect();
			} else if (messageEntry.getContent().indexOf("<" + ARM) >= 0) {
				getContactorController().doArm();
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
