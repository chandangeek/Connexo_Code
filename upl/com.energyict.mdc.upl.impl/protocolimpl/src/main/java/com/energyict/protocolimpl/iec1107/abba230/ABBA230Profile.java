package com.energyict.protocolimpl.iec1107.abba230;

import java.io.*;
import java.util.*;
import java.util.logging.*;

import com.energyict.protocol.*;
import com.energyict.protocolimpl.iec1107.ProtocolLink;
import com.energyict.protocolimpl.iec1107.abba230.eventlogs.AbstractEventLog;

/**
 * As example a new AS230 meter with intervals of half an hour.
 *
 * The meter can divide a single interval into several ProfileEntries.
 * For example
 *  ProfileEntry 09.00 09.10: 0.001
 *  ProfileEntry 09.10 09.20: 0.002
 *  ProfileEntry 09.20 09.30: 0.003
 *
 * This could happen when 2 timesets occured within this interval.  To
 * create an IntervalData object for this Interval all of the values need to
 * be summed up.
 * So in the ProfileEntry an IntervalData element will be build:
 *  IntervalData 09.00 - 09.30: 0.001 + 0.002 + 0.003 = 0.006
 *
 * How-protocol-stuff-works:
 * In this Profile class we have:
 * IntervalMap
 * - a map of Intervals
 * - random access through "get()" function
 * - knows to what interval a date
 *
 * Interval
 * - a collection of ProfileEntries
 *
 * IntervalMap is a "random" access map of Intervals.  It knows in what
 * Interval a date belongs.  So a IntervalMap.get() with a random date
 * will return the Interval wich encloses that date.
 *
 *
 * @author fbl 
 * 
 */
        


public class ABBA230Profile {
    
    private static final int DEBUG=0;
    
    private final ABBA230RegisterFactory rFactory;
    private final ProtocolLink protocolLink;
    private TimeZone adjustedTimeZone=null;
    // configuration of the meter
    private LoadProfileConfigRegister meterConfig;
    private Date meterTime = null;
    /** integration period in seconds */
    private int integrationPeriod;
    ABBA230 abba230;
    
    
    ABBA230Profile(ABBA230 abba230,ABBA230RegisterFactory abba230RegisterFactory) throws IOException {
    	this.abba230=abba230;
    	this.protocolLink = (ProtocolLink)abba230;
        this.rFactory = abba230RegisterFactory;
        long val = ((Long)rFactory.getRegister("LoadProfileDSTConfig")).longValue();
        if ((val&0x01)==0)
        	adjustedTimeZone = ProtocolUtils.getWinterTimeZone(protocolLink.getTimeZone());
        else
        	adjustedTimeZone = protocolLink.getTimeZone();
        
    }
    
    private void requestLoadProfile(Date from, Date to, boolean includeEvents) throws IOException {
        Logger l = protocolLink.getLogger();
        if( l.isLoggable( Level.INFO ) ) {
            String msg = "getProfileData(Date " + from + ", Date "
                    + to + ", boolean " + includeEvents + ")";
            protocolLink.getLogger().info( msg );
        }
        
        if (to.getTime() < from.getTime())
            throw new IOException("ABBA230Profile, getProfileData, error ("+from+") > ("+to+")");
        
        /** If the to date is after the metertime, set the to date to the meter
         * time.  Obvious isn't it. */
        if( to.after( getMeterTime() ) )
            to = getMeterTime();
        
        /* by writing the dates in register 554 */
        LoadProfileReadByDate lpbd = new LoadProfileReadByDate(from, to);
        rFactory.setRegister("LoadProfileReadByDate", lpbd );
        
        if( DEBUG > 0 )
            System.out.println( "Inquiring meter for " + lpbd );
        
        /** Before the 554 LoadProfileReadByDate existed the meter was asked
         * to return x nr of days in the past.  This might still be a more
         * reliable approach. */
//        final long ONEDAY=24*60*60*1000;
//        long tostd = to.getTime() + (long)timeZone.getOffset(to.getTime());
//        long fromstd = from.getTime() + (long)timeZone.getOffset(from.getTime());
//        long nrOfDaysToRetrieve = ((tostd/ONEDAY) - (fromstd/ONEDAY)) + 1;
//
//        rFactory.setRegister("LoadProfileSet",new Long(nrOfDaysToRetrieve));
    }
    
    /** Retrieve the load profile between from and to date.
     *
     * @param from
     * @param to
     * @param includeEvents
     * @throws java.io.IOException
     * @return
     */
    ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException {
    	requestLoadProfile(from,to,includeEvents);
        return doGetProfileData(includeEvents,from);
    }
    
    /** Retrieve the complete load profile.
     * @param includeEvents
     * @throws java.io.IOException
     * @return
     */
    ProfileData getProfileData(boolean includeEvents) throws IOException {
        
        Logger l = protocolLink.getLogger();
        if( l.isLoggable( Level.INFO ) ) {
            String msg = "getProfileData( boolean " + includeEvents + ")";
            protocolLink.getLogger().info( msg );
        }
        
        /* by writing the value FFFF to register 551, the complete load profile is read */
        rFactory.setRegister("LoadProfileSet",new Long(0xFFFF));
        
        return doGetProfileData(includeEvents,null);
    }

	private void executeProfileDataScript(long nrOfBlocks) throws IOException {
		if ((abba230.getCache() != null) && (abba230.getCache() instanceof CacheMechanism)) {
			StringBuffer strBuff = new StringBuffer();
			for(int i=0;i<nrOfBlocks;i++) {
				if (i>0)
					strBuff.append(",");
				strBuff.append("55000"+(i+1)+"(40)");
			}
			// call the scriptexecution  scriptId,script
			((CacheMechanism)abba230.getCache()).setCache(new String[]{"3",strBuff.toString()});
		}
	}
	private void executeLogbookDataScript() throws IOException {
		if ((abba230.getCache() != null) && (abba230.getCache() instanceof CacheMechanism)) {
			// call the scriptexecution  scriptId,script
			String script = "678001(40),678002(13),679001(40),679002(13),680001(40),680002(40),680003(2d),685001(40),685002(13),695001(40),695002(13),691001(40),691002(13),692001(40),692002(13),693001(40),693002(13),694001(2b),696001(2b),699001(35),422001(35),423001(35),424001(35),425001(35),426001(35),427001(35),428001(35),429001(35),430001(35),431001(35),432001(35),433001(35),701001(35),705001(2b)";
			((CacheMechanism)abba230.getCache()).setCache(new String[]{"3",script});
		}
	}
	
    private ProfileData doGetProfileData( boolean includeEvents,Date from ) throws IOException {
        byte[] data;
        
        long nrOfBlocks = ((Long)rFactory.getRegister("LoadProfileByDate64Blocks")).longValue();
        
        // specific for the scripting with wavenis
        executeProfileDataScript(nrOfBlocks);
        
        data = rFactory.getRegisterRawData("LoadProfile", (int)nrOfBlocks*64);
        
        ProfileData profileData = parse(includeEvents,new ByteArrayInputStream(data), protocolLink.getNumberOfChannels());
        
        if( includeEvents ) {
        	
        	executeLogbookDataScript();
        	
        	List meterEvents= new ArrayList();
        	
        	getMeterEvents(rFactory.getOverVoltageEventLog(),meterEvents);
        	getMeterEvents(rFactory.getUnderVoltageEventLog(),meterEvents);
        	getMeterEvents(rFactory.getProgrammingEventLog(),meterEvents);
        	getMeterEvents(rFactory.getLongPowerFailEventLog(),meterEvents);
        	getMeterEvents(rFactory.getPowerFailEventLog(),meterEvents);
        	getMeterEvents(rFactory.getTerminalCoverEventLog(),meterEvents);
        	getMeterEvents(rFactory.getMainCoverEventLog(),meterEvents);
        	getMeterEvents(rFactory.getMagneticTamperEventLog(),meterEvents);
        	getMeterEvents(rFactory.getReverserunEventLog(),meterEvents);
        	getMeterEvents(rFactory.getTransientEventLog(),meterEvents);
        	getMeterEvents(rFactory.getEndOfBillingEventLog(),meterEvents);
        	getMeterEvents(rFactory.getContactorOpenOpticalLog(),meterEvents);
        	getMeterEvents(rFactory.getContactorOpenModuleLog(),meterEvents);
        	getMeterEvents(rFactory.getContactorOpenLoadMonitorLowEventLog(),meterEvents);
        	getMeterEvents(rFactory.getContactorOpenLoadMonitorHighEventLog(),meterEvents);
        	getMeterEvents(rFactory.getContactorOpenAutoDisconnectEventLog(),meterEvents);
        	getMeterEvents(rFactory.getContactorArmOpticalEventLog(),meterEvents);
        	getMeterEvents(rFactory.getContactorArmModuleEventLog(),meterEvents);
        	getMeterEvents(rFactory.getContactorArmLoadMonitorEventLog(),meterEvents);
        	getMeterEvents(rFactory.getContactorArmDisconnectEventLog(),meterEvents);
        	getMeterEvents(rFactory.getContactorCloseOpticalEventLog(),meterEvents);
        	getMeterEvents(rFactory.getContactorCloseModuleEventLog(),meterEvents);
        	getMeterEvents(rFactory.getContactorCloseButtonEventLog(),meterEvents);
        	getMeterEvents(rFactory.getMeterErrorEventLog(),meterEvents);
        	getMeterEvents(rFactory.getBatteryVoltageLowEventLog(),meterEvents);
        	
        	profileData.getMeterEvents().addAll(truncateMeterEvents(meterEvents,from));
       		//profileData.setMeterEvents(truncateMeterEvents(meterEvents,from));
        }
        
        profileData.setIntervalDatas(truncateIntervalDatas(profileData.getIntervalDatas(),from));
        
        return profileData;
    }
    
    private List truncateIntervalDatas(List intervalDatas,Date from) {
    	if (from == null)
    		return intervalDatas;
    	Iterator it = intervalDatas.iterator();
    	while(it.hasNext()) {
    		IntervalData intervalData = (IntervalData) it.next();
    		if (intervalData.getEndTime().before(from))
    			it.remove();
    	}
    	return intervalDatas;
    }    
    
    private List truncateMeterEvents(List meterEvents,Date from) {
    	if (from == null)
    		return meterEvents;
    	Iterator it = meterEvents.iterator();
    	while(it.hasNext()) {
    		MeterEvent meterEvent = (MeterEvent) it.next();
    		if (meterEvent.getTime().before(from))
    			it.remove();
    	}
    	return meterEvents;
    }
    
    private void getMeterEvents(ABBA230Register reg,List meterEvents) {
    	try {
    		AbstractEventLog o = (AbstractEventLog)rFactory.getRegister( reg );
    		meterEvents.addAll(o.getMeterEvents());
    	}
    	catch(IOException e) {
    		//e.printStackTrace();
    		protocolLink.getLogger().info("No "+reg.getName()+" available");
    	}
    	catch(NullPointerException e) {
    		e.printStackTrace();
    	}
    }
    
    public Date getMeterTime() throws IOException {
        if( meterTime == null )
            meterTime = (Date)rFactory.getRegister("TimeDate");
        return meterTime;
    }
    
    private ProfileData parse(boolean includeEvents, ByteArrayInputStream bai, int nrOfChannels) throws IOException {
        
        ProfileData profileData = new ProfileData();
        
        // configuration of the meter
        meterConfig = (LoadProfileConfigRegister) rFactory.getRegister( rFactory.getLoadProfileConfiguration() );
        
        // last encountered profile entry
        ABBA230ProfileEntry current;
        // last encountered start of day (e4)
        LoadProfileConfigRegister e4Config;
        // last encountered integration period
        int e4Integration;
        // last encountered dst
        boolean e4Dst;
        
        /* ProfileData gets the configuration of the meter */
        Iterator i = meterConfig.toChannelInfo().iterator();
        while( i.hasNext() )
            profileData.addChannel( (ChannelInfo)i.next() );
        
        // profile data must start with e4
        current = new ABBA230ProfileEntry(rFactory,bai,nrOfChannels);
        if ((current.getType()) == ABBA230ProfileEntry.NEWDAY) {
            e4Config = current.getLoadProfileConfig();
            e4Integration = current.getIntegrationPeriod();
            e4Dst = current.isDST();
            integrationPeriod = e4Integration;
        } else {
            String msg =
                    "ABBA16700ProfileParser, parse, profile data should " +
                    "start with 'E4', new day marker!";
            throw new IOException(msg);
        }
        
        IntervalMap iMap = new IntervalMap();
        Interval interval = iMap.get(createDate(current.getTime()));
        
        bai.reset();
        // parse datastream & build profiledata
        while(bai.available() > 0) {
            
            current = new ABBA230ProfileEntry(rFactory,bai, e4Config.getNumberRegisters() );
            
            if( current.getType() == ABBA230ProfileEntry.TIMECHANGE ) {
                interval = new Interval( createDate(current.getTime()) );
                iMap.get( createDate(current.getTime()) ).timeChange(true) ;
            }
            
            if( current.getType() ==  ABBA230ProfileEntry.DAYLIGHTSAVING ) {
                interval = new Interval( createDate(current.getTime()) );
            }
            
            if (DEBUG>=1) System.out.println(current.toString(getAdjustedTimeZone(),e4Dst));
            if (DEBUG>=1) System.out.println(interval.toString());
            
            // The opposite check is not possible since the meter local tim
            // dst behaviour can be set NOT to follow DST while
            // the profile data has it's dst flag set.
            if ((getAdjustedTimeZone().useDaylightTime()) && !e4Dst) {
                String msg = "ABBA230Profile, parse, configured timezone expects profiledata to follow DST, correct first!";
                throw new IOException( msg );
            }
            
            addEventToProfile(profileData,current);
            
            if( current.getType() == ABBA230ProfileEntry.NEWDAY ||
                current.getType() == ABBA230ProfileEntry.CONFIGURATIONCHANGE ) {
                
                e4Config = current.getLoadProfileConfig();
                e4Integration = current.getIntegrationPeriod();
                e4Dst = current.isDST();
                /* add the interval to iMap */
                interval = iMap.get(createDate(current.getTime()));
                
            }
            
            if( current.getType() == ABBA230ProfileEntry.LOADPROFILECLEARED ) {
                profileData = new ProfileData();
                /* ProfileData gets the configuration of the meter */
                Iterator ci = meterConfig.toChannelInfo().iterator();
                while( ci.hasNext() )
                    profileData.addChannel( (ChannelInfo)ci.next() );
                iMap.clear();
            }
            
            
            if( current.getType() == ABBA230ProfileEntry.POWERUP ) {
                Date e5Date = new Date( current.getTime() * 1000 );
                if( interval.isIn( e5Date ) ) {
                    continue;
                } else {
                    for( int ch = 0; (ch < 26) && !(interval.isIn(e5Date)); ch ++ ) {
                        interval = interval.next();
                    }
                }
            }
            
            if( !current.isMarker() ) {
                if( e4Config.getChannelMask() == meterConfig.getChannelMask() ) {
                    interval = iMap.get( interval.startTime );
                    interval.addEntry( current );
                    interval = iMap.get( interval.endTime );
                }else {
                    String msg = 
                        "Invalid interval: (" + interval.endTime + ") "
                        + "current meter conf. " + meterConfig.toShortString() 
                        + " interval conf. " + e4Config.toShortString();
                    protocolLink.getLogger().info( msg );
                }
            }
            
            if (current.getType() == ABBA230ProfileEntry.ENDOFDATA) break;
            
        }
        
        iMap.addToProfile(profileData);
        
        //if (includeEvents)
        	profileData.applyEvents(e4Integration/60);
        
        return profileData;
    }
    
    private void addEventToProfile(ProfileData profileData, ABBA230ProfileEntry profileEntry) {
        if (profileEntry.isMarker()) {
            int eiCode = -1;
            int protocolCode = -1;
            switch (profileEntry.getType()) {
                case ABBA230ProfileEntry.NEWDAY:
                case ABBA230ProfileEntry.CONFIGURATIONCHANGE:
                case ABBA230ProfileEntry.ENDOFDATA:
                    break;
                case ABBA230ProfileEntry.POWERDOWN:
                    eiCode = MeterEvent.POWERDOWN;
                    protocolCode = ABBA230ProfileEntry.POWERDOWN;
                    break;
                case ABBA230ProfileEntry.POWERUP:
                    eiCode = MeterEvent.POWERUP;
                    protocolCode = ABBA230ProfileEntry.POWERUP;
                    break;
                case ABBA230ProfileEntry.DAYLIGHTSAVING:
                    eiCode = MeterEvent.SETCLOCK;
                    protocolCode = ABBA230ProfileEntry.DAYLIGHTSAVING;
                    break;
                case ABBA230ProfileEntry.FORCEDENDOFDEMAND:
                    eiCode = MeterEvent.OTHER;
                    protocolCode = ABBA230ProfileEntry.FORCEDENDOFDEMAND;
                    break;
                case ABBA230ProfileEntry.TIMECHANGE:
                    eiCode = MeterEvent.SETCLOCK;
                    protocolCode = ABBA230ProfileEntry.TIMECHANGE;
                    break;
                case ABBA230ProfileEntry.LOADPROFILECLEARED:
                    eiCode = MeterEvent.CLEAR_DATA;
                    protocolCode = ABBA230ProfileEntry.LOADPROFILECLEARED;
                    break;
            }
            if( eiCode != -1 && protocolCode != -1 ) {
                Date date = ProtocolUtils.getCalendar(getAdjustedTimeZone(),profileEntry.getTime()).getTime();
                MeterEvent me = new MeterEvent(date,eiCode,protocolCode);
                profileData.addEvent( me );
            }
            
        }
    }
    
    Date createDate(long seconds){
        return ProtocolUtils.getCalendar(getAdjustedTimeZone(), seconds).getTime();
    }
    
    private static final int METER_TRANSIENT_RESET=0x01;
    private static final int TIME_SYNC=0x02;
    private static final int DATA_CHANGE=0x04;
    private static final int BATTERY_FAIL=0x08;
    private static final int CT_RATIO_CHANGE = 0x10;
    private static final int REVERSE_RUN=0x20;
    private static final int PHASE_FAILURE=0x40;
    
    /** IntervalMap:
     * Creates and manages all the Interval objects.  
     * Maintains a map of all the Interval objects, accessible with a date 
     * object.  If no interval exists for a given date object, a new one is 
     * (lazily) initialized. 
     *
     * (For mapping the dates to the right interval, the integrationPeriod 
     * needs to be known.)
     *
     * e.g.:  given an integrationPeriod of 1800 secs
     * get( 12/08/77 13:13 ) -> will return the interval ]13:00-13:30]
     * get( 12/08/77 13:00 ) -> will return the interval ]13:00-13:30]
     *
     * */
    class IntervalMap {
        
        TreeMap map = new TreeMap();
        
        /** retrieve the interval for a date */
        Interval get( Date date ){
            long d = date.getTime();
            int y = 60 * 1000;          // sec * milli
            int integMilli = integrationPeriod*1000;     
                                        // integration in milli 
            d = d - ( d % y );          // round of sec and milli
                                        // round to the next integration border
            if( d % integMilli > 0 ) {  
                d = d - ( d % integMilli );      
            } 
            
            Date key = new Date(d);
            Interval r = (Interval)map.get(key);
            if( r == null ) {
                r = new Interval(key);
                map.put(key,r);
            }
            return r;
        }
        
        /** add all the intervals to the profileData */
        void addToProfile(ProfileData profileData){
            Iterator i = map.keySet().iterator();
            while( i.hasNext() ){
                Object key = i.next();
                Interval value = (Interval)map.get(key);
                IntervalData id = value.getIntervalData();
                if( id != null )
                    profileData.addInterval(id);
            }
        }
        
        /** remove all intervals from profileData */
        void clear( ){
            map.clear();
        }
        
        public String toString(){
            StringBuffer sb = new StringBuffer();
            sb.append( "Day [ \n ");
            Iterator i = map.keySet().iterator();
            while( i.hasNext() ){
                Object key = i.next();
                Object value = map.get(key);
                sb.append( " " + value + "\n" );
            }
            sb.append( "]" );
            return sb.toString();
        }
        
    }
    
    /** Interval is a wrapper class around ProfileEntries. */
    
    class Interval {
        Date startTime;
        Date endTime;
        ArrayList entries = new ArrayList();
        Calendar key;
        boolean timeChange = false;
        
        Interval(long seconds){
            Calendar startC = ProtocolUtils.getCalendar(getAdjustedTimeZone(), seconds);
            this.startTime = startC.getTime();
            this.endTime = new Date( ((startTime.getTime()/1000) + integrationPeriod)*1000 );
            this.key = startC;
        }
        
        Interval(Date startTime) {
            this.startTime = startTime;
            this.endTime = new Date( ((startTime.getTime()/1000) + integrationPeriod)*1000 );
            this.key = ProtocolUtils.getCalendar(getAdjustedTimeZone());
            this.key.setTime(startTime);
        }
        
        Interval next( ){
            return new Interval(endTime);
        }
        
        boolean isIn( Date time ){
            return
                    startTime.equals(time) ||
                    endTime.equals(time) ||
                    ( time.after(startTime) && time.before(endTime) );
        }
        
        List getEntries( ){
            return entries;
        }
        
        void addEntry( ABBA230ProfileEntry entry ){
            entries.add(entry);
        }
        
        void timeChange( boolean flag ) {
            timeChange = flag;
        }
        
        IntervalData getIntervalData(){
            if( entries.size() > 0 ){
                ABBA230ProfileEntry profileEntry = (ABBA230ProfileEntry) entries.get(0);
                int status = profileEntry.getStatus();
                
                IntervalData intervalData = new IntervalData(endTime);
                
                if ((status & METER_TRANSIENT_RESET) == METER_TRANSIENT_RESET)
                    intervalData.addEiStatus(IntervalStateBits.OTHER);
                if ((status & TIME_SYNC) == TIME_SYNC)
                    intervalData.addEiStatus(IntervalStateBits.SHORTLONG);
                if ((status & DATA_CHANGE) == DATA_CHANGE)
                    intervalData.addEiStatus(IntervalStateBits.CONFIGURATIONCHANGE);
                if ((status & BATTERY_FAIL) == BATTERY_FAIL)
                    intervalData.addEiStatus(IntervalStateBits.OTHER);
                if ((status & CT_RATIO_CHANGE) == CT_RATIO_CHANGE)
                    intervalData.addEiStatus(IntervalStateBits.CONFIGURATIONCHANGE);
                if ((status & REVERSE_RUN) == REVERSE_RUN)
                    intervalData.addEiStatus(IntervalStateBits.REVERSERUN);
                if ((status & PHASE_FAILURE) == PHASE_FAILURE)
                    intervalData.addEiStatus(IntervalStateBits.PHASEFAILURE);
                
                long [] temp = new long [profileEntry.getValues().length];
                System.arraycopy(profileEntry.getValues(),0, temp, 0, temp.length  );
                for( int i = 1; i < entries.size(); i ++ ){
                    profileEntry = (ABBA230ProfileEntry) entries.get(i);
                    long [] pValues = profileEntry.getValues();
                    for( int ti = 0; ti < temp.length; ti ++ )
                        temp[ti] = temp[ti] + pValues[ti];
                }
                for( int i = 0; i < profileEntry.getNumberOfChannels(); i ++ ){
                    intervalData.addValue(new Long( temp[i] ));
                }
                
                if( timeChange || entries.size() > 1 )
                    intervalData.addEiStatus(IntervalStateBits.SHORTLONG);
                
                if( entries.size() > 1 ) {
                    String msg = 
                        "Merging Interval [" + startTime + ", " + endTime + ", " 
                        + entries.size() + "] ";
                    protocolLink.getLogger().info( msg );
                    intervalData.addEiStatus(IntervalStateBits.SHORTLONG);
                }
                
                return intervalData;
                
            }
            return null;
        }
        
        public int hashCode() {
            return key.hashCode();
        }
        
        public boolean equals( Object other ){
            if(!(other instanceof Interval)) return false;
            Interval oi = (Interval) other;
            return key.equals(oi.key);
        }
        
        public String toString(){
            StringBuffer result = new StringBuffer();
            result.append( "Interval [" + startTime + ", " + endTime + ", " + entries.size() );
            result.append( "] " + getIntervalData()  );
            return result.toString();
        }
        
    }

	public TimeZone getAdjustedTimeZone() {
		return adjustedTimeZone;
	}
    
}
