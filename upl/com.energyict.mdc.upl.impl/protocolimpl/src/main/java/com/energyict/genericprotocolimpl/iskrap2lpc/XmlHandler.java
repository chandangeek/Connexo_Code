package com.energyict.genericprotocolimpl.iskrap2lpc;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.energyict.cbo.*;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;

/**
 * Some example xml documents:
 * 
 * <Device DeviceID="40349033" P2LPCIdent="43355084">
 *   <Results>
 *     <Profile Ident="99.1.0" Period="3600">
 *     
 *       <Register Ident="1.8.0" Value="0000000.000" 
 *          DateTime="2007-03-29T07:00:00Z" Status="192" />
 *     
 *     </Profile>
 *     
 *    <Register Ident="1.8.1" Value="0000039.795" DateTime="2002-01-13T22:00:00Z" Status="0" />
 *     
 *   </Results>
 * </Device>
 * 
 * <MeterEvents>
 *   <Device DeviceID="40349033" P2LPCIdent="37463587">
 *   
 *     <Event DateTime="2007-06-07T05:38:43Z" Status="32786" />
 *   
 *   </Device>
 * </MeterEvents>
 * 
 * <MeterPowerEvents>
 *   <Device DeviceID="40349033" P2LPCIdent="37463587">
 *   
 *     <PowerEvent DateTimeLog="2007-06-20T15:45:58Z"
 *         DateTimeFail="2007-06-15T10:46:22Z" Duration="449976" />
 *   
 *   </Device>
 * </MeterPowerEvents>
 * 
 * @author fbo
 */

class XmlHandler extends DefaultHandler {
	
	private int debug = 1;

    private final static String DEVICE = "Device";
    private final static String PROFILE = "Profile";
    private final static String REGISTER = "Register";
    private final static String EVENT = "Event";
    private final static String POWER_EVENT = "PowerEvent";
    
    private final static String DEVICE_ID = "DeviceID";
    private final static String VALUE = "Value";
    private final static String DATE_TIME = "DateTime";
    private final static String STATUS = "Status";
    
    private final static int PROFILE_STATUS_DEVICE_DISTURBANCE = 0x1;
    private final static int PROFILE_STATUS_RESET_CUMULATION = 0x10;
    private final static int PROFILE_STATUS_DEVICE_CLOCK_CHANGED = 0x20;
    private final static int PROFILE_STATUS_POWER_RETURNED = 0x40;
    private final static int PROFILE_STATUS_POWER_FAILURE = 0x80;
    
    private static final int EVENT_FATAL_ERROR=0x0001; 
    private static final int EVENT_DEVICE_CLOCK_RESERVE=0x0002;
    private static final int EVENT_VALUE_CORRUPT=0x0004;
    private static final int EVENT_DAYLIGHT_CHANGE=0x0008;
    private static final int EVENT_BILLING_RESET=0x0010; 
    private static final int EVENT_DEVICE_CLOCK_CHANGED=0x0020; 
    private static final int EVENT_POWER_RETURNED=0x0040; 
    private static final int EVENT_POWER_FAILURE=0x0080; 
    private static final int EVENT_VARIABLE_SET=0x0100;
    private static final int EVENT_UNRELIABLE_OPERATING_CONDITIONS=0x0200;
    private static final int EVENT_END_OF_UNRELIABLE_OPERATING_CONDITIONS=0x0400;
    private static final int EVENT_UNRELIABLE_EXTERNAL_CONTROL=0x0800;
    private static final int EVENT_END_OF_UNRELIABLE_EXTERNAL_CONTROL=0x1000;
    private static final int EVENT_EVENTLOG_CLEARED=0x2000; 
    private static final int EVENT_LOADPROFILE_CLEARED=0x4000; 
    private static final int EVENT_L1_POWER_FAILURE=0x8001; 
    private static final int EVENT_L2_POWER_FAILURE=0x8002; 
    private static final int EVENT_L3_POWER_FAILURE=0x8003; 
    private static final int EVENT_L1_POWER_RETURNED=0x8004; 
    private static final int EVENT_L2_POWER_RETURNED=0x8005; 
    private static final int EVENT_L3_POWER_RETURNED=0x8006; 
    private static final int EVENT_METER_COVER_OPENED=0x8010; 
    private static final int EVENT_TERMINAL_COVER_OPENED=0x8011; 
    
    private static final String POWER_UP_MSG = "Power up.";
    private static final String POWER_DOWN_MSG = "Power down.";
    
    private static final int ELECTRICITY 	= 0x00;
    private static final int MBUS 			= 0x01;
    private static final String ELECTRICITY_NAME 	= "ELECTRICITY";
    private static final String MBUS_NAME 			= "MBUS";
    private static final int DAILY			= 0x00;
    private static String dailyStr = null;
    private static final int MONTHLY		= 0x01;
    private static String monthlyStr = null;
    
    private int profileDuration = -1;
    
    private ArrayList messageEnds = new ArrayList();
    
    private SimpleDateFormat dateFormat;
    private boolean inProfile;
    private Logger logger;
    
    private ProtocolChannelMap channelMap;
    
    /* index of the channel to store interval data into (if encountered) */
    private int currentChannelIndex = 0;
    
    private MeterReadingData meterReadingData = new MeterReadingData();
    private Profile profile = new Profile();
    private List eventList = new ArrayList();
	private boolean checkOndemands;
    
    public XmlHandler(Logger logger, ProtocolChannelMap channelMap) {
        this.logger = logger;
        this.channelMap = channelMap;
       
        dateFormat = Constant.getInstance().getDateFormatFixed();
    }
    
    public void setChannelIndex(int channelIndex) {
        currentChannelIndex = channelIndex;
    }
    
    public void startElement(String uri, String lName, String qName, Attributes attrbs) 
    throws SAXException {

        if( PROFILE.equals(qName) )
            handleStartProfile(attrbs);
        if( REGISTER.equals(qName) )
            handleStartRegister(attrbs);
        if( EVENT.equals(qName) )
            handleStartEvent(attrbs);
        if( POWER_EVENT.equals(qName) )
            handleStartPowerEvent(attrbs);
        
    }

    public void endElement(String uri, String localName, String qName) 
    throws SAXException {

        if( PROFILE.equals(qName) )
            handleEndProfile( );
        
    }


    private void handleStartProfile(Attributes attrbs) {
    	String ident = attrbs.getValue("Ident");
//    	if ( ident.equals("99.2.0"))
//    		profileDuration = DAILY;
//    	else if ( ident.equals("98.1.0") | ident.equals("98.2.0"))
//    		profileDuration = MONTHLY;
//    	else
    	if ( ident.equals(getDailyStr()) )
    		setProfileDuration(DAILY);
    	else if ( ident.equals(getMonthlyStr()) )
    		setProfileDuration(MONTHLY);
    	else
    		inProfile = true;
    }
    
    private void handleStartRegister(Attributes att) {
        try {
            
            if( ! inProfile ) {
                
                /* register data */
                String ident    = att.getValue("Ident");
                String dateTime = att.getValue("DateTime");
                String value    = att.getValue("Value");
                String error    = att.getValue("Error");
                
                ObisCode oc = null;
                
                if( error == null ){
                    if (profileDuration == DAILY){
                    	
                    	if( ident.split("\\.").length == 3 )
                    		oc = ObisCode.fromString( "1.0." + ident + ".VZ");
                      
                    	if( ident.split("\\.").length == 5 )
                    		oc = ObisCode.fromString( ident + ".VZ");
	                      
	                    if( ident.split("\\.").length ==  6 )                
	                    	oc = ObisCode.fromString( ident );
                    }
                    else if ( profileDuration == MONTHLY ){
                    	if( ident.split("\\.").length == 3 )
                    		oc = ObisCode.fromString( "1.0." + ident + ".VZ-1");
                      
                    	if( ident.split("\\.").length == 5 )
                    		oc = ObisCode.fromString( ident + ".VZ-1");
	                      
	                    if( ident.split("\\.").length ==  6 )                
	                    	oc = ObisCode.fromString( ident );
                    }
                    
                    if (checkOndemands){
                    	
                    	String end = ".255";
                    	
//                    	if( messageEnds.get(0).equals(0) ){
//                    		end = ".VZ";
//                    		messageEnds.remove(0);
//                    	}
//                    	else if ( messageEnds.get(0).equals(1) ){
//                    		end = ".VZ-1";
//                    		messageEnds.remove(0);
//                    	}
//                    	else{
//                    		end = ".255";
//                    		messageEnds.remove(0);
//                    	}
                    		
                    	if (!end.equals(null)){
	                    	if( ident.split("\\.").length == 3 )
	                    		oc = ObisCode.fromString( "1.0." + ident + end);
	                      
	                    	if( ident.split("\\.").length == 5 )
	                    		oc = ObisCode.fromString( ident + end);
		                      
		                    if( ident.split("\\.").length ==  6 )                
		                    	oc = ObisCode.fromString( ident );
                    	}
                    	
                    }
                }
                
                if( oc != null ) {
                    
                    Date d = (dateTime!=null) ? dateFormat.parse(dateTime) : new Date();
                    RegisterValue rv = toRegisterValue(oc, value, d);
                    if( rv!=null ) meterReadingData.add(rv);
                    
                    
                } else {
                    
                    String msg = "Code: " + ident + " not supported ";
                    if( error != null ) msg += "msg: [" + error + "]";
                    logger.log(Level.INFO, msg);
                
                }
                
                
            } else {
                
                /* profile data */
                String valueString      = att.getValue(VALUE);
                String dateTimeString   = att.getValue(DATE_TIME);
                String statusString     = att.getValue(STATUS);
                
                BigDecimal value = new BigDecimal(valueString);
                
                Date time = dateFormat.parse(dateTimeString);
                int intervalStatus = toIntervalState(statusString);
                
                int pStatus = Integer.parseInt(statusString);
                
                profile.add(time, value, intervalStatus, pStatus);
                
            }

        } catch (ParseException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            e.printStackTrace();
            throw new ApplicationException(e);
        } 
        
    }
    
    private void handleStartEvent(Attributes attrbs) {
        
        try {
            
            String dateTime = attrbs.getValue("DateTime");
            String status = attrbs.getValue("Status");
            String ident = attrbs.getValue("Ident");
            int eventId = Integer.parseInt(status);
            Date time = dateFormat.parse(dateTime);
            
            if(ident != null)
            	addMeterEvent(time, MeterEvent.OTHER, ident);
            else{
            	if ( mask(eventId, EVENT_FATAL_ERROR) ){
            		final String msg = "Fatal error.";
            		addMeterEvent(time,MeterEvent.FATAL_ERROR, msg);
            	}
            	if ( mask(eventId, EVENT_DEVICE_CLOCK_RESERVE) ){
            		final String msg = "Event status device clock reserve.";
            		addMeterEvent(time,MeterEvent.OTHER,msg);
            	}
            	if ( mask(eventId, EVENT_VALUE_CORRUPT) ){
            		final String msg = "Event status value corrupt.";
            		addMeterEvent(time,MeterEvent.OTHER,msg);
            	}
            	if ( mask(eventId, EVENT_DAYLIGHT_CHANGE) ){
            		final String msg = "Event status daylight change.";
            		addMeterEvent(time,MeterEvent.OTHER,msg);
            	}
            	if ( mask(eventId, EVENT_BILLING_RESET) ){
            		final String msg = "Billing.";
            		addMeterEvent(time,MeterEvent.BILLING_ACTION, msg);
            	}
            	if ( mask(eventId, EVENT_DEVICE_CLOCK_CHANGED) ){
            		final String msg = "Set Clock.";
            		addMeterEvent(time,MeterEvent.SETCLOCK, msg);
            	}
            	if ( mask(eventId, EVENT_POWER_RETURNED) ){
            		final String msg = POWER_UP_MSG;
            		addMeterEvent(time,MeterEvent.POWERUP, msg);
            	}
            	if ( mask(eventId, EVENT_POWER_FAILURE) ){
            		final String msg = POWER_DOWN_MSG;
            		addMeterEvent(time,MeterEvent.POWERDOWN, msg);
            	}
            	if ( mask(eventId, EVENT_VARIABLE_SET) ){
            		final String msg = "Event status variable set.";
            		addMeterEvent(time,MeterEvent.OTHER,msg);
            	}
            	if ( mask(eventId, EVENT_UNRELIABLE_OPERATING_CONDITIONS) ){
            		final String msg = "Event status unreliable operating conditions.";
            		addMeterEvent(time,MeterEvent.OTHER,msg);
            	}
            	if ( mask(eventId, EVENT_END_OF_UNRELIABLE_OPERATING_CONDITIONS) ){
            		final String msg = "Event status end of unreliable operating conditions.";
            		addMeterEvent(time,MeterEvent.OTHER,msg);
            	}
            	if ( mask(eventId, EVENT_UNRELIABLE_EXTERNAL_CONTROL) ){
            		final String msg = "Event status unreliable external control.";
            		addMeterEvent(time,MeterEvent.OTHER,msg);
            	}
            	if ( mask(eventId, EVENT_END_OF_UNRELIABLE_EXTERNAL_CONTROL) ){
            		final String msg = "Event status end of unreliable external control.";
            		addMeterEvent(time,MeterEvent.OTHER,msg);
            	}
            	if ( mask(eventId, EVENT_EVENTLOG_CLEARED) ){
            		final String msg = "Event status event log cleared.";
            		addMeterEvent(time,MeterEvent.CLEAR_DATA,msg);
            	}
            	if ( mask(eventId, EVENT_LOADPROFILE_CLEARED) ){
            		final String msg = "Event status load profile cleared.";
            		addMeterEvent(time,MeterEvent.CLEAR_DATA,msg);
            	}
            	if ( mask(eventId, EVENT_L1_POWER_FAILURE) ) {
            		final String msg = "Event status L1 phase failure.";
            		addMeterEvent(time,MeterEvent.PHASE_FAILURE,msg);
            	}
            	if ( mask(eventId, EVENT_L2_POWER_FAILURE) ) {
            		final String msg = "Event status L2 phase failure.";
            		addMeterEvent(time,MeterEvent.PHASE_FAILURE,msg); 
            	}
            	if ( mask(eventId, EVENT_L3_POWER_FAILURE) ){
            		final String msg = "Event status L3 phase failure.";
            		addMeterEvent(time,MeterEvent.PHASE_FAILURE,msg);
            	}
            	if (mask(eventId, EVENT_L1_POWER_RETURNED) ){
            		final String msg = "Event status end of L1 phase failure.";
            		addMeterEvent(time,MeterEvent.OTHER,msg);
            	}
            	if ( mask(eventId, EVENT_L2_POWER_RETURNED) ){
            		final String msg = "Event status end of L2 phase failure.";
            		addMeterEvent(time,MeterEvent.OTHER,msg);
            	}
            	if ( mask(eventId, EVENT_L3_POWER_RETURNED) ){
            		final String msg = "Event status end of L3 phase failure.";
            		addMeterEvent(time,MeterEvent.OTHER,msg);
            	}
            	if ( mask(eventId, EVENT_METER_COVER_OPENED) ){
            		final String msg = "Event status meter cover opened.";
            		addMeterEvent(time,MeterEvent.OTHER,msg);
            	}
            	if ( mask(eventId, EVENT_TERMINAL_COVER_OPENED) ){
            		final String msg = "Event status terminal cover opened.";
            		addMeterEvent(time,MeterEvent.OTHER,msg);
            	}
            }
           
        
        } catch (ParseException e) {
            e.printStackTrace();
            throw new ApplicationException(e);
        }
        
    }
    
    /** check if event IS mask */
    private boolean mask(int event, int mask){
        return event == mask;
    }

    private RegisterValue toRegisterValue(ObisCode obis, String value, Date time) {
    
        try {
        
            if( obis.getC()==1 && obis.getD()==0 && obis.getE()==0 )
                return new RegisterValue(obis, dateFormat.parse(value));
            
            Unit unit = null;
            if( obis.getC()==1 && obis.getD()==8 )
                unit = Unit.get(BaseUnit.WATTHOUR, 3);

            if( obis.getC()==1 && obis.getD()==6 )
                unit = Unit.get(BaseUnit.WATT, 3);
            
            if(unit==null){
                unit = Unit.getUndefined();
            }
             
            BigDecimal amount = new BigDecimal(value);
            Quantity q = new Quantity(amount, unit);
            return new RegisterValue(obis, q, null, null, time, time);
            
        } catch (ParseException e) {
            e.printStackTrace();
            throw new ApplicationException(e);
        } 
        
    }
    
    private int toIntervalState(String status){
        int flag = Integer.parseInt(status);
        int eiStatus = IntervalStateBits.OK;
        
        if( (flag & PROFILE_STATUS_DEVICE_DISTURBANCE) > 0 )
            eiStatus |= IntervalStateBits.DEVICE_ERROR;
        
        if( (flag & PROFILE_STATUS_RESET_CUMULATION) > 0 )
            eiStatus |= IntervalStateBits.OTHER;
        
        if( (flag & PROFILE_STATUS_DEVICE_CLOCK_CHANGED) > 0 )
            eiStatus |= IntervalStateBits.SHORTLONG;        

        if( (flag & PROFILE_STATUS_POWER_RETURNED) > 0 )
            eiStatus |= IntervalStateBits.POWERUP;        

        if( (flag & PROFILE_STATUS_POWER_FAILURE) > 0 )
            eiStatus |= IntervalStateBits.POWERDOWN;        

        return eiStatus;
    }
    
    private void addMeterEvent(Date time, int event, String description){
        eventList.add( new MeterEvent(time,event,description) );
    }

    private void handleStartPowerEvent(Attributes attrbs) {
        
        try {
            
            Date log  = dateFormat.parse( attrbs.getValue( "DateTimeLog" ) );
            Date fail = dateFormat.parse( attrbs.getValue( "DateTimeFail" ) );
            
            addMeterEvent(fail, MeterEvent.POWERDOWN, POWER_DOWN_MSG);
            addMeterEvent(log, MeterEvent.POWERUP, POWER_UP_MSG);
            
        } catch (ParseException e) {
            e.printStackTrace();
            throw new ApplicationException(e);
        }            
        
    }

    private void handleEndProfile() {
        inProfile = false;
    }

    private List[] getChannelInfos() {
        List[] result = {new ArrayList(), new ArrayList()}; 
        int ID = -1;
        int[] chID = {-1,-1};
        String name = null;
        
        for (int j = 0; j < channelMap.getNrOfProtocolChannels(); j++) {
            
        	Unit u = null;
            ProtocolChannel channel = channelMap.getProtocolChannel(j);

            if ( (channel.register.equals("1.8.0")) | (channel.register.equals("2.8.0")) | (channel.register.equals("1.5.0"))) {
            	u = Unit.get(BaseUnit.WATTHOUR,3);
            	name = ELECTRICITY_NAME;
            	ID = ELECTRICITY;
            }           	
            
            else if ( channel.register.equals("0.1.128.50.0") ){   //MBus(gas)
            	u = Unit.get(BaseUnit.CUBICMETER);
            	name = MBUS_NAME;
            	ID = MBUS;
            }
            
            else{
            	u = Unit.getUndefined();
            	name = ELECTRICITY_NAME;
            	ID = ELECTRICITY;
            }
            	
            chID[ID] ++;
            
//            ChannelInfo ci = new ChannelInfo(j, "" + j, Unit.getUndefined() );
            ChannelInfo ci = new ChannelInfo(chID[ID], name, u );
//            ci.setName(name);
            if( channel.isCumul() )
                ci.setCumulativeWrapValue( channel.getWrapAroundValue() );
            
            result[ID].add( ci );
            
        }
        
        return result;
    }
    
    MeterReadingData getMeterReadingData( ) {
        return meterReadingData;
    }
    
    ProfileData[] getProfileData( ) {
        return profile.toProfileData();
    }

    private class Profile {
        
        private Map intervalMap = new HashMap();
        
        void add( Date time, BigDecimal bd, int eiStatus, int protocolStatus ) {
            
            if( intervalMap.get(time) == null )
                intervalMap.put(time, new Interval(time));
            
            Interval interval = (Interval)intervalMap.get(time);
            interval.values.set(currentChannelIndex, bd);
            
            interval.eiStatus |= eiStatus;
            interval.protocolStatus |= protocolStatus;
            
        }
        
        ProfileData[] toProfileData( ) {
        	
            ProfileData[] profileData = {new ProfileData( ), new ProfileData( )};
            profileData[ELECTRICITY].setChannelInfos( getChannelInfos()[ELECTRICITY] );
            profileData[MBUS].setChannelInfos( getChannelInfos()[MBUS] );
            Interval[] interval = {new Interval(null), new Interval(null), new Interval(null)};
            Iterator i = intervalMap.values().iterator();
            while( i.hasNext() ){
            	
            	interval[2] = (Interval)i.next();
//            	interval = copyIntervalSettings(interval);
            	interval[ELECTRICITY].values.clear();
            	interval[MBUS].values.clear();
            	int j = 0;
                Iterator it = interval[2].values.iterator();
                while( it.hasNext() ){
                	Object value = it.next();
                	
                	if (value != null ){
                    	if ( (channelMap.getProtocolChannel(j).register.equals("1.8.0")) 
                    			| (channelMap.getProtocolChannel(j).register.equals("2.8.0")) 
                    			| (channelMap.getProtocolChannel(j).register.equals("1.5.0"))) {
                    		interval = copyIntervalSettings(interval, ELECTRICITY);
                    		interval[ELECTRICITY].values.add(value);
                    	}
                    	
                    	else if ( channelMap.getProtocolChannel(j).register.equals("0.1.128.50.0") ){ 
                    		interval = copyIntervalSettings(interval, MBUS);
                    		interval[MBUS].values.add(value);
                    	}
                	}
                	j++;
                }
                
                if ( interval[ELECTRICITY].values.size() != 0 )
                	profileData[ELECTRICITY].addInterval( interval[ELECTRICITY].toIntervalData() );
                if ( interval[MBUS].values.size() != 0 ){
                	interval[MBUS].eiStatus = 0;
                	profileData[MBUS].addInterval( interval[MBUS].toIntervalData() );                	
                }
                
            }
            
            i = eventList.iterator();
            while( i.hasNext() ){
                MeterEvent event = (MeterEvent)i.next();
                profileData[ELECTRICITY].addEvent( event );
            }
            
            profileData[ELECTRICITY].sort();
            profileData[MBUS].sort();
            
            
            return profileData;
        }

		private Interval[] copyIntervalSettings(Interval[] interval, int index) {
			interval[index].eiStatus = interval[2].eiStatus;
			interval[index].endTime = interval[2].endTime;
			interval[index].protocolStatus = interval[2].protocolStatus;
			return interval;
		}
        
    }
    
    private class Interval {
        
        private Date endTime;
        private int eiStatus;
        private int protocolStatus;
        private List values;
        
        Interval(Date endTime) {
            this.endTime = endTime;
            
            int nrI = channelMap.getNrOfProtocolChannels();
            values = new ArrayList( channelMap.getNrOfProtocolChannels() );
            for( int i = 0; i < nrI; i ++ ) {
//                values.add( i, new BigDecimal(0) );
            	values.add( i, null );
            }
        }
        
        IntervalData toIntervalData( ){
            IntervalData id = new IntervalData( endTime, eiStatus, protocolStatus );
            id.addValues(values);
            return id;
        }
        
    }

	public void checkOnDemands(boolean b) {
		checkOndemands = b;
		
	}

	public int getProfileDuration() {
		return profileDuration;
	}

	public void setProfileDuration(int profileDuration) {
		this.profileDuration = profileDuration;
	}

	public String getDailyStr() {
		return dailyStr;
	}

	public void setDailyStr(String dailyStr) {
		XmlHandler.dailyStr = dailyStr;
	}

	public String getMonthlyStr() {
		return monthlyStr;
	}

	public void setMonthlyStr(String monthlyStr) {
		XmlHandler.monthlyStr = monthlyStr;
	}

	public ProfileData addEvents() {
		ProfileData profileData = new ProfileData();
        Iterator i = eventList.iterator();
        while( i.hasNext() ){
            MeterEvent event = (MeterEvent)i.next();
            profileData.addEvent( event );
        }
		return profileData;
	}

//	public void addMessageEnd(int f) {
//		messageEnds.add(f);	
//	}



}
