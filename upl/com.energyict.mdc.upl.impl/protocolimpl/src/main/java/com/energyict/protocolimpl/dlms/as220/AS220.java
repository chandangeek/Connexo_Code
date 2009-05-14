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
import com.energyict.dlms.cosem.CapturedObject;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;
import com.energyict.protocolimpl.base.ParseUtils;

public class AS220 extends DLMSSNAS220 implements RegisterProtocol {
    private static final byte DEBUG=1;
    
    int eventIdIndex;
    
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
    
    

    
    
    protected void getEventLog(ProfileData profileData,Calendar fromCalendar, Calendar toCalendar) throws IOException {
        DataContainer dc = getCosemObjectFactory().getProfileGeneric(getMeterConfig().getEventLogObject().getObisCode()).getBuffer(fromCalendar,toCalendar);
        
        if (DEBUG>=1) dc.printDataContainer();
        
        if (DEBUG>=1) {
           getCosemObjectFactory().getProfileGeneric(getMeterConfig().getEventLogObject().getObisCode()).getCaptureObjectsAsDataContainer().printDataContainer();            
        }
        
        int index=0;
        if (eventIdIndex == -1) {
            Iterator it = getCosemObjectFactory().getProfileGeneric(getMeterConfig().getEventLogObject().getObisCode()).getCaptureObjects().iterator();
            while(it.hasNext()) {
                CapturedObject capturedObject = (CapturedObject)it.next();
                if (capturedObject.getLogicalName().getObisCode().equals(ObisCode.fromString("0.0.96.240.12.255")) &&
                    (capturedObject.getAttributeIndex() == 2) &&
                    (capturedObject.getClassId() == 3))
                    break;
                else
                    index++;
            }        
        }
        
        for (int i=0;i<dc.getRoot().getNrOfElements();i++) {
           Date dateTime = dc.getRoot().getStructure(i).getOctetString(0).toDate(getTimeZone());
           int id=0;
           if (eventIdIndex == -1) {
               id = dc.getRoot().getStructure(i).getInteger(index);
           } 
           else {
               id = dc.getRoot().getStructure(i).convert2Long(eventIdIndex).intValue();
           }
           MeterEvent meterEvent = EventNumber.toMeterEvent(id, dateTime); 
           if (meterEvent != null)
              profileData.addEvent(meterEvent);
        }
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
        		if (calendar == null) continue; // first the calendar has to be initialized with the start of load profile marker
        		IntervalData ivd = new IntervalData(calendar.getTime(),eiCode);
        		eiCode=0;
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
    
    private long mapLogCodes(long lLogCode) {
        switch((int)lLogCode) {
            case (int)EV_FATAL_ERROR: return(MeterEvent.FATAL_ERROR);
            case (int)EV_CORRUPTED_MEASUREMENT: return(MeterEvent.OTHER);
            case (int)EV_TIME_DATE_ADJUSTED: return(MeterEvent.SETCLOCK);
            case (int)EV_POWER_UP: return(MeterEvent.POWERUP);
            case (int)EV_POWER_DOWN: return(MeterEvent.POWERDOWN);
            case (int)EV_EVENT_LOG_CLEARED: return(MeterEvent.OTHER);
            case (int)EV_LOAD_PROFILE_CLEARED: return(MeterEvent.CLEAR_DATA);
            default: return(MeterEvent.OTHER);
        } // switch(lLogCode)
    } // private void mapLogCodes(long lLogCode)
    
    
    
    byte[] aarqlowlevelAS220 = {(byte)0xE6,(byte)0xE6,(byte)0x00,
            0x60, 0x36, (byte) 0xA1, 0x09, 0x06, 0x07,
            0x60, (byte)0x85, 0x74, 0x05, 0x08, 0x01,
            0x02, (byte)0x8A, 0x02, 0x07, (byte)0x80, (byte)0x8B,
            0x07, 0x60, (byte)0x85, 0x74, 0x05, 0x08,
            0x02, 0x01, (byte)0xAC, 0x0A, (byte)0x80, 0x08,
            0x31, 0x32, 0x33, 0x34, 0x35, 0x36,
            0x37, 0x38};
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
            eventIdIndex=Integer.parseInt(properties.getProperty("EventIdIndex","-1").trim()); // ZMD=1, ZMQ=2
            
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
    
} // public class DLMSZMD
