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
import java.util.*;

import com.energyict.dlms.*;
import com.energyict.dlms.axrdencoding.TypeEnum;
import com.energyict.dlms.cosem.CapturedObject;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;
import com.energyict.protocol.messaging.*;
import com.energyict.protocolimpl.base.ParseUtils;
import com.energyict.protocolimpl.iec1107.abba230.*;

public class AS220 extends DLMSSNAS220 implements RegisterProtocol,MessageProtocol {
    private static final byte DEBUG=1;
    
    
    private static String CONNECT 			= "ConnectLoad";
    private static String DISCONNECT 		= "DisconnectLoad";
    private static String ARM 				= "ArmMeter";
    private static String TARIFF_OPTION_SWITCH_BASE = "TariffOptionSwitchBase";
    private static String TARIFF_OPTION_SWITCH_DAYNIGHT = "TariffOptionSwitchDayNight";
    
    private static String CONNECT_DISPLAY 			= "Connect Load";
    private static String DISCONNECT_DISPLAY 		= "Disconnect Load";
    private static String ARM_DISPLAY 				= "Arm Meter";
    private static String TARIFF_OPTION_SWITCH_BASE_DISPLAY = "Switch tariff option BASE";
    private static String TARIFF_OPTION_SWITCH_DAYNIGHT_DISPLAY = "Switch tariff option DAY/NIGHT";
    
    public AS220() {
    }
    
    protected String getDeviceID() {
        return "GEC";
    }
    
    // Interval List
    private static final byte IL_CAPUTURETIME=0;
    private static final byte IL_EVENT=12;
    private static final byte IL_DEMANDVALUE=13;
    
    // Event codes as interpreted by MV90 for the Siemens ZMD meter
    private static final long EV_NORMAL_END_OF_INTERVAL=0x00800000;
    private static final long EV_START_OF_INTERVAL=     0x00080000;
    private static final long EV_FATAL_ERROR=           0x00000001;
    private static final long EV_CORRUPTED_MEASUREMENT= 0x00000004;
    private static final long EV_SUMMER_WINTER=         0x00000008;
    private static final long EV_TIME_DATE_ADJUSTED=    0x00000020;
    private static final long EV_POWER_UP=              0x00000040;
    private static final long EV_POWER_DOWN=            0x00000080;
    private static final long EV_EVENT_LOG_CLEARED=     0x00002000;
    private static final long EV_LOAD_PROFILE_CLEARED=  0x00004000;
    //private static final long EV_CAPTURED_EVENTS=       0x008860E5; // Add new events...

    //KV 27102003
    public Calendar initCalendarSW(boolean protocolDSTFlag,TimeZone timeZone) {
        Calendar calendar;
        if (protocolDSTFlag)
           calendar = Calendar.getInstance(ProtocolUtils.getSummerTimeZone(timeZone));
        else
           calendar = Calendar.getInstance(ProtocolUtils.getWinterTimeZone(timeZone));
        return calendar;
    }
    
    
    private List readMainLogbook(Calendar fromCalendar, Calendar toCalendar) throws IOException {
        DataContainer dc = getCosemObjectFactory().getProfileGeneric(ObisCode.fromString("0.0.99.98.1.255")).getBuffer(fromCalendar,toCalendar);
        if (DEBUG>=1) System.out.println("readMainLogbook");
        if (DEBUG>=1) dc.printDataContainer();
 
        List meterEvents = new ArrayList();
        for (int i=0;i<dc.getRoot().getNrOfElements();i++) {
           Date dateTime = dc.getRoot().getStructure(i).getOctetString(0).toDate(getTimeZone());
           int id=0;
           id = dc.getRoot().getStructure(i).getInteger(1);
           MeterEvent meterEvent = EventNumber.toMeterEvent(id, dateTime); 
           if (meterEvent != null)
        	   meterEvents.add(meterEvent);
        }
    	
        return meterEvents;
    }
    
    private List readVoltageCutLogbook(Calendar fromCalendar, Calendar toCalendar) throws IOException {
    	
        DataContainer dc = getCosemObjectFactory().getProfileGeneric(ObisCode.fromString("0.0.99.98.5.255")).getBuffer(fromCalendar,toCalendar);
        
        if (DEBUG>=1) System.out.println("readVoltageCutLogbook");
        if (DEBUG>=1) dc.printDataContainer();
        
 
        List meterEvents = new ArrayList();
        for (int i=0;i<dc.getRoot().getNrOfElements();i++) {
           Date dateTime = dc.getRoot().getStructure(i).getOctetString(0).toDate(getTimeZone());
           int id=dc.getRoot().getStructure(i).getInteger(1);
    	   MeterEvent meterEvent = EventNumber.toMeterEvent(id, dateTime); 
           if (meterEvent != null)
        	   meterEvents.add(meterEvent);
        }
        
        return meterEvents;
    }
    
    private List readCoverLogbook(Calendar fromCalendar, Calendar toCalendar) throws IOException {
    	
        DataContainer dc = getCosemObjectFactory().getProfileGeneric(ObisCode.fromString("0.0.99.98.2.255")).getBuffer(fromCalendar,toCalendar);
        
        if (DEBUG>=1) System.out.println("readCoverLogbook");
        if (DEBUG>=1) dc.printDataContainer();
        
 
        List meterEvents = new ArrayList();
        for (int i=0;i<dc.getRoot().getNrOfElements();i++) {
           Date dateTime = dc.getRoot().getStructure(i).getOctetString(0).toDate(getTimeZone());
           int id=dc.getRoot().getStructure(i).getInteger(1);
    	   MeterEvent meterEvent = EventNumber.toMeterEvent(id, dateTime); 
           if (meterEvent != null)
        	   meterEvents.add(meterEvent);
        }
        
        return meterEvents;
    }    
    
    protected void getEventLog(ProfileData profileData,Calendar fromCalendar, Calendar toCalendar) throws IOException {
        
        List meterEvents = new ArrayList();
        meterEvents.addAll(readMainLogbook(fromCalendar, toCalendar));
        meterEvents.addAll(readVoltageCutLogbook(fromCalendar, toCalendar));
        meterEvents.addAll(readCoverLogbook(fromCalendar, toCalendar));
        profileData.setMeterEvents(meterEvents);
    }
    
    protected void buildProfileData(byte bNROfChannels,ProfileData profileData,ScalerUnit[] scalerunit,List loadProfileCompactArrayEntries)  throws IOException {

        int i,t; 
        IntervalData savedIntervalData=null;
        List intervalDatas = new ArrayList();
        int latestProfileInterval= getProfileInterval();
        int eiCode=0;
        if (DEBUG >= 1) System.out.println("loadProfileCompactArrayEntries.size() = "+loadProfileCompactArrayEntries.size());
        
        LoadProfileCompactArrayEntry dateStamp=null;
        Calendar calendar = null;
        for (i=0;i<loadProfileCompactArrayEntries.size();i++) {
        	LoadProfileCompactArrayEntry lpcae = (LoadProfileCompactArrayEntry)loadProfileCompactArrayEntries.get(i);
        	if (DEBUG >= 1) System.out.println(lpcae);
        	
        	if (lpcae.isValue()) { // normal interval value
        		if (calendar == null) continue; // first the calendar has to be initialized with the start of load profile marker
        		IntervalData ivd = new IntervalData(calendar.getTime());
        		ivd.addValue(new BigDecimal(""+lpcae.getValue()));
        		intervalDatas.add(ivd);
        		latestProfileInterval = lpcae.getIntervalInSeconds();
           		calendar.add(Calendar.SECOND,latestProfileInterval); // set the calendar to the next interval endtime
        	}
        	else if (lpcae.isPartialValue()) { // partial interval value
        		eiCode |= IntervalStateBits.SHORTLONG;
        		if (calendar == null) continue; // first the calendar has to be initialized with the start of load profile marker
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
        
        if (DEBUG >= 1) System.out.println();
        
        if (DEBUG >= 1) System.out.println("intervalDatas.size() = "+intervalDatas.size());
        profileData.setIntervalDatas(intervalDatas);
        
    } // ProfileData buildProfileData(...)
    
    
    
    // KV 15122003
    private void roundDown2nearestInterval(IntervalData intervalData) throws IOException {
        int rest = (int)(intervalData.getEndTime().getTime()/1000) % getProfileInterval();
        if (rest > 0)
           intervalData.getEndTime().setTime(((intervalData.getEndTime().getTime()/1000) - rest) * 1000);
    }
    
    // KV 15122003
    private void roundUp2nearestInterval(IntervalData intervalData) throws IOException {
        int rest = (int)(intervalData.getEndTime().getTime()/1000) % getProfileInterval();
        if (rest > 0)
           intervalData.getEndTime().setTime(((intervalData.getEndTime().getTime()/1000) + (getProfileInterval() - rest)) * 1000);
    }
    
    // KV 15122003
    private int getNrOfIntervals(IntervalData intervalData) throws IOException {
        return (int)(intervalData.getEndTime().getTime()/1000) / getProfileInterval();
    }
    
    // KV 15122003 changed
    private IntervalData addIntervalData(IntervalData cumulatedIntervalData,IntervalData currentIntervalData) throws IOException {
        int currentCount = currentIntervalData.getValueCount();
        IntervalData intervalData = new IntervalData(currentIntervalData.getEndTime());
        int i;
        long current;
        for (i=0;i<currentCount;i++) {
            if (getMeterConfig().getChannelObject(i).isCapturedObjectCumulative())
                current = ((Number)currentIntervalData.get(i)).longValue();
            else
                current = ((Number)currentIntervalData.get(i)).longValue()+((Number)cumulatedIntervalData.get(i)).longValue();
            intervalData.addValue(new Long(current));
        }
        return intervalData;
    }
    
    byte[] aarqlowlevelAS220_old = {(byte)0xE6,(byte)0xE6,(byte)0x00,
            0x60, 0x36, (byte) 0xA1, 0x09, 0x06, 0x07,
            0x60, (byte)0x85, 0x74, 0x05, 0x08, 0x01,
            0x02, (byte)0x8A, 0x02, 0x07, (byte)0x80, (byte)0x8B,
            0x07, 0x60, (byte)0x85, 0x74, 0x05, 0x08,
            0x02, 0x01, (byte)0xAC, 0x0A, (byte)0x80, 0x08,
            0x31, 0x32, 0x33, 0x34, 0x35, 0x36,
            0x37, 0x38};
    byte[] aarqlowlevelAS220 = {(byte)0xE6,(byte)0xE6,(byte)0x00,
            0x60, 0x36, (byte) 0xA1, 0x09, 0x06, 0x07,
            0x60, (byte)0x85, 0x74, 0x05, 0x08, 0x01,
            0x02, (byte)0x8A, 0x02, 0x07, (byte)0x80, (byte)0x8B,
            0x07, 0x60, (byte)0x85, 0x74, 0x05, 0x08,
            0x02, 0x01};
    byte[] aarqlowlevelAS220_2= {(byte)0xBE, 0x10, 0x04, 0x0E,
            0x01, 0x00, 0x00, 0x00, 0x06, 0x5F,
            0x1F, 0x04, 0x00, 0x18, 0x02, 0x20,
            0x00, (byte)0xEF};

    protected byte[] getLowLevelSecurity() {
        return buildaarq(aarqlowlevelAS220,aarqlowlevelAS220_2); 
    }    
    
    protected void doValidateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
        try {
            Iterator iterator= getRequiredKeys().iterator();
            while (iterator.hasNext()) {
                String key = (String) iterator.next();
                if (properties.getProperty(key) == null)
                    throw new MissingPropertyException(key + " key missing");
            }
            strID = properties.getProperty(MeterProtocol.ADDRESS);
            // KV 19012004
            if ((strID != null) &&(strID.length()>16)) throw new InvalidPropertyException("ID must be less or equal then 16 characters.");
            
            strPassword = properties.getProperty(MeterProtocol.PASSWORD);
            //if (strPassword.length()!=8) throw new InvalidPropertyException("Password must be exact 8 characters.");
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

	public List getMessageCategories() {
        List theCategories = new ArrayList();
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

    public String writeTag(MessageTag msgTag) {
        StringBuffer buf = new StringBuffer();
        
        // a. Opening tag
        buf.append("<");
        buf.append( msgTag.getName() );
        
        // b. Attributes
        for (Iterator it = msgTag.getAttributes().iterator(); it.hasNext();) {
            MessageAttribute att = (MessageAttribute)it.next();
            if (att.getValue()==null || att.getValue().length()==0)
                continue;
            buf.append(" ").append(att.getSpec().getName());
            buf.append("=").append('"').append(att.getValue()).append('"');
        }
        buf.append(">");
        
        // c. sub elements
        for (Iterator it = msgTag.getSubElements().iterator(); it.hasNext();) {
            MessageElement elt = (MessageElement)it.next();
            if (elt.isTag())
                buf.append( writeTag((MessageTag)elt) );
            else if (elt.isValue()) {
                String value = writeValue((MessageValue)elt);
                if (value==null || value.length()==0)
                    return "";
                buf.append(value);
            }
        }
        
        // d. Closing tag
        buf.append("</");
        buf.append( msgTag.getName() );
        buf.append(">");
        
        return buf.toString();    
    }	
	
	public String writeTag2(MessageTag msgTag) {
        StringBuffer buf = new StringBuffer();
        
        // a. Opening tag
        buf.append("<");
        buf.append(msgTag.getName());
        
        // b. Attributes
        for (Iterator it = msgTag.getAttributes().iterator(); it.hasNext();) {
            MessageAttribute att = (MessageAttribute) it.next();
            if (att.getValue() == null || att.getValue().length() == 0)
                continue;
            buf.append(" ").append(att.getSpec().getName());
            buf.append("=").append('"').append(att.getValue()).append('"');
        }
        if (msgTag.getSubElements().isEmpty()) {
            buf.append("/>");
            return buf.toString();
        }
        buf.append(">");
        // c. sub elements
        for (Iterator it = msgTag.getSubElements().iterator(); it.hasNext();) {
            MessageElement elt = (MessageElement) it.next();
            if (elt.isTag())
                buf.append(writeTag((MessageTag) elt));
            else if (elt.isValue()) {
                String value = writeValue((MessageValue) elt);
                if (value == null || value.length() == 0)
                    return "";
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

	public void applyMessages(List messageEntries) throws IOException {
		
	}

	public MessageResult queryMessage(MessageEntry messageEntry) {
		
		try {
			if (messageEntry.getContent().indexOf("<"+DISCONNECT)>=0) {
				getCosemObjectFactory().getDisconnector(ObisCode.fromString("0.0.96.3.10.255")).writeControlState(new TypeEnum(0));
				getLogger().info("DISCONNECT message received");
				if (DEBUG >= 1) System.out.println("DISCONNECT message received");
			}
			else if (messageEntry.getContent().indexOf("<"+CONNECT)>=0) {
				getCosemObjectFactory().getDisconnector(ObisCode.fromString("0.0.96.3.10.255")).writeControlState(new TypeEnum(1));
				getLogger().info("CONNECT message received");
				if (DEBUG >= 1) System.out.println("CONNECT message received");
			}
			else if (messageEntry.getContent().indexOf("<"+ARM)>=0) {
				getCosemObjectFactory().getDisconnector(ObisCode.fromString("0.0.96.3.10.255")).writeControlState(new TypeEnum(2));
				getLogger().info("ARM message received");
				if (DEBUG >= 1) System.out.println("ARM message received");
			}
			else if (messageEntry.getContent().indexOf("<"+TARIFF_OPTION_SWITCH_BASE)>=0) {
				getCosemObjectFactory().getData(ObisCode.fromString("0.0.96.50.0.255")).setValueAttr(new TypeEnum(0));
				getLogger().info("TARIFF_OPTION_SWITCH_BASE message received");
				if (DEBUG >= 1) System.out.println("TARIFF_OPTION_SWITCH_BASE message received");
			}
			else if (messageEntry.getContent().indexOf("<"+TARIFF_OPTION_SWITCH_DAYNIGHT)>=0) {
				getCosemObjectFactory().getData(ObisCode.fromString("0.0.96.50.0.255")).setValueAttr(new TypeEnum(1));
				getLogger().info("TARIFF_OPTION_SWITCH_DAYNIGHT message received");
				if (DEBUG >= 1) System.out.println("TARIFF_OPTION_SWITCH_DAYNIGHT message received");
			}
			return MessageResult.createSuccess(messageEntry);
		}
		catch(IOException e) {
			getLogger().severe("QueryMessage(), "+e.getMessage());
			return MessageResult.createFailed(messageEntry);
		}
	}

    
} // public class DLMSZMD
