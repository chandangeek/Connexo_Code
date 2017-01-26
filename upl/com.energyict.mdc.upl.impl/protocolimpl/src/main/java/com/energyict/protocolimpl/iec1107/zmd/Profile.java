
package com.energyict.protocolimpl.iec1107.zmd;

import com.energyict.cbo.Unit;
import com.energyict.mdc.io.NestedIOException;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.IntervalStateBits;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.MeterExceptionInfo;
import com.energyict.protocol.ProfileData;
import com.energyict.protocolimpl.base.ParseUtils;
import com.energyict.protocolimpl.base.ProtocolChannelMap;
import com.energyict.protocolimpl.iec1107.ChannelMap;
import com.energyict.protocolimpl.iec1107.FlagIEC1107Connection;
import com.energyict.protocolimpl.iec1107.ProtocolLink;
import com.energyict.protocolimpl.iec1107.vdew.AbstractVDEWRegistry;
import com.energyict.protocolimpl.iec1107.vdew.VDEWProfile;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * header format
 * KZ(ZSTs13)(S)(RP)(z)(KZ1)(E1)..(KZz)(Ez)(Mw1)...(MwZ)
 *
 * - KZ     EDIS-Identifier "P.01"
 * - ZSTs13 Time stamp format of the oldest measured value
 * - S      Profile status byte
 * - RP     Demand integration period in minutes
 * - z      Numbered of different measured values in one demand integration
 *          period.
 * - KZn    Identifier of the measured values (without tariff particulars or
 *          preceding value Identifier)
 * - En     Units of measured values
 * - Mwn    Measured values
 *
 *
 * @author fbo
 *
 */

class Profile extends VDEWProfile {

    private final boolean DBG = false;

    private final static String DBG_PROFILE_DMP = "c:\\prof.txt";
    private final static String DBG_EVENT_DMP   = "c:\\event.txt";
    private final static String ERROR_PATTERN   = "P.98(ER";

    private final SimpleDateFormat dateFormat;

    private final TimeZone pTimeZone;
    private final int pInterval;
    private final int pNrChannels;

    private List channelInfos;
    private List channelEdis;

    private ArrayList<Date> intervalsWithRecordsNotOnBoundaries = new ArrayList<Date>();

    public Profile(
            MeterExceptionInfo meterExceptionInfo, ProtocolLink protocolLink,
            AbstractVDEWRegistry abstractVDEWRegistry) throws IOException {

        super(meterExceptionInfo,protocolLink,abstractVDEWRegistry,false);

        pTimeZone = getProtocolLink().getTimeZone();
        pInterval = getProtocolLink().getProfileInterval();
        pNrChannels = getProtocolLink().getNumberOfChannels();

        dateFormat = new SimpleDateFormat( "yyMMddHHmmss" );
        dateFormat.setTimeZone(pTimeZone);

    }

    public ProfileData getProfileData(
            Date lastReading,boolean includeEvents) throws IOException {

        Calendar fromCalendar = ProtocolUtils.getCleanCalendar(pTimeZone);
        fromCalendar.setTime(lastReading);

        ProfileData profileData =  doGetProfileData(fromCalendar,ProtocolUtils.getCalendar(pTimeZone),1);
        if (includeEvents) {
           List meterEvents = tryGetLogBook(fromCalendar,ProtocolUtils.getCalendar(pTimeZone));
           profileData.getMeterEvents().addAll(meterEvents);
           profileData.sort();
        }

        profileData.applyEvents(pInterval/60);
        return profileData;
    }

    public ProfileData getProfileData(Date fromReading, Date toReading,
            boolean includeEvents) throws IOException {

        Calendar fromCalendar = ProtocolUtils.getCleanCalendar(pTimeZone);
        fromCalendar.setTime(fromReading);
        Calendar toCalendar = ProtocolUtils.getCleanCalendar(pTimeZone);
        toCalendar.setTime(toReading);

        ProfileData profileData =  doGetProfileData(fromCalendar,toCalendar,1);
        if (includeEvents) {
           List meterEvents = tryGetLogBook(fromCalendar, toCalendar);
           profileData.getMeterEvents().addAll(meterEvents);
           profileData.sort();
        }

        profileData.applyEvents(pInterval/60);
        return profileData;
    }

    /* When there are no events for the time interval, an P.98(ERROR) will
     * be returned by the meter.
     *
     * The IEC1107Connection will throw an IOException.  But the protocol
     * must not stop for this reason, so the exception must be ignored.
     * (tricky stuff)
     */
    private List tryGetLogBook(Calendar from, Calendar to) throws IOException{
        try {
            return doGetLogBook(from,to);
        }catch(IOException ioe){
            String msg = ioe.getMessage();
            if( msg.indexOf( ERROR_PATTERN ) == -1 ) throw ioe;
        }
        return new ArrayList();
    }

    /* Overrides VDEWProfile#buildProfileData().
     *
     * Difference:
     * DST transition is different from "standard"/other vdew protocol
     *
     * (non-Javadoc)
     * @see VDEWProfile#buildProfileData(byte[])
     */
    protected ProfileData buildProfileData(byte[] data) throws IOException {

        dump( data, DBG_PROFILE_DMP );
        Assembly assembly = null;

        try {

            assembly = new Assembly( data );

            ProfileData profileData = new ProfileData();

            IntervalMap iMap    = new IntervalMap();

            String line = assembly.readLine();
            if( isError(line) ) return profileData;

            while( line != null ) {

                dbg( assembly.dbgString() );
                String fields[] = split( line );

                Date    mTime   = asDate(fields[0]);
                int     eiCode  = asEiCode(getStatus(fields[1]));

                checkIntervalPeriod(fields[2]);
                checkNrChannels(fields[3]);

                if( channelInfos == null )
                    profileData.setChannelInfos( parseChannelInfo(fields) );
                else
                    checkEdisCodes(fields, mTime);

                Date intervalDate = mTime;

                while( ((line = assembly.readLine())!= null) && !isHeader(line) ) {

                    String values[] = split( line.trim() );
                    nrChannelCheck( values.length );

                    Interval interval = iMap.get( intervalDate );
                    IntervalRow ir = new IntervalRow(eiCode, values);

                    if (!isOnIntervalBoundary(intervalDate)) {
                        getProtocolLink().getLogger().log(Level.INFO, "Timestamp of interval record [" + intervalDate + "]was not on interval boundaries - Interval record: " + ir);
                        intervalsWithRecordsNotOnBoundaries.add(interval.asIntervalData().getEndTime());
                    }

                    boolean addEntry = true;
                    if (!interval.entries.isEmpty()) {
                        if (!intervalsWithRecordsNotOnBoundaries.contains(interval.asIntervalData().getEndTime())) {
                            for (Object each : interval.entries) {
                                IntervalRow row = (IntervalRow) each;
                                if (row.values.equals(ir.values) && row.eiStatus == ir.eiStatus) {
                                    addEntry = false;
                                    break;
                                }
                            }
                        }
                        getProtocolLink().getLogger().log(Level.INFO, addEntry ? "Merging interval record [" + intervalDate + " - " + ir + "] with the existing interval [" + interval.toString() + "]."
                                : "Interval record [" + intervalDate + " - " + ir + "] clashes with the existing interval records [" + interval.toString() + "]. The interval record will not be added.");
                    }

                    if (addEntry) {
                        interval.addEntry(ir);
                    }
                    dbg( interval.toString() + " " + assembly.dbgString() );

                    intervalDate = interval.next();
                }
            }

            iMap.addToProfile(profileData);

            dbg(iMap.toString());

            return profileData;

        } catch(Throwable t) {
            t.printStackTrace();
            String msg = "";
            if( assembly == null ) {
                msg = "(" + assembly.dbgString() + ")";
            }
            throw new NestedIOException(t, msg );
        }

    }

    private boolean isOnIntervalBoundary(Date intervalDate) {
        Calendar c = Calendar.getInstance(pTimeZone);
        c.setTime(intervalDate);
        return ParseUtils.isOnIntervalBoundary(c, pInterval);
    }

    private Date asDate(String string) throws IOException{
        Date result = null;

        try {

            int dst = Integer.parseInt(string.substring(0,1));
            result = dateFormat.parse( string.substring(1) );

            if( (dst==1) && !pTimeZone.inDaylightTime(result) ) {
                result = new Date(result.getTime() - 3600L * 1000L);
            }

        } catch (ParseException e) {
            throw new NestedIOException(e);
        }

        return result;
    }

    private int getStatus(String string) {
        return Integer.parseInt(string, 16);
    }

    private int checkIntervalPeriod(String string) throws IOException {

        int mInterval = Integer.parseInt(string) * 60 ;

        if( mInterval != pInterval ) {
            String msg =
                "Profile interval period protocol " +
                "(" + pInterval + "s)" +
                "is different from profile interval in meter " +
                "(" + mInterval + "s)";

            throw new IOException(msg);
        }

        return mInterval;

    }

    private int checkNrChannels(String string) throws IOException {

        int mNrChannels = Integer.parseInt(string);

        if( pNrChannels != mNrChannels ) {
            String msg =
                "Configured number of channels (" + pNrChannels + ") " +
                "differs from number of channels in meter " +
                "(" + mNrChannels + ").  " +
                " To correct see property ChannelMap.";
            throw new IOException(msg);
        }

        return mNrChannels;

    }

    private void checkEdisCodes(String [] fields, Date time) throws IOException {

        for(int i = 0; i+4<fields.length; i+=2){
            if( !fields[i+4].equals(channelEdis.get(i/2)) ) {
                String msg =
                    "Channel configuration has changed on " + time +
                    ".  Profile data can not be read.";
                throw new IOException(msg);
            }
        }

    }

    /* ChannelInfo is created from the first profile interval that is fetched */
    private List parseChannelInfo(String[] fields) {
        if(channelInfos==null) {
            channelInfos = new ArrayList();
            channelEdis = new ArrayList();
            int id = 0;
            for( int i = 4; i<fields.length; i+=2 ) {
                String cName = fields[i];
                Unit unit = Unit.get(fields[i+1]);
                channelInfos.add( new ChannelInfo(id, cName, unit ) );
                channelEdis.add(cName);
                id = id + 1;
            }
        }
        return channelInfos;
    }

    private List getChannelInfos() {
        return channelInfos;
    }

    private void nrChannelCheck(int nrChannel) throws IOException {
        if( nrChannel != pNrChannels ){
            String msg =
                "Nr of channels in profile (" + nrChannel +") differs from" +
                " nr channels configured in protocol (" + pNrChannels + ").";
            throw new IOException(msg);
        }
    }

    /* (non-Javadoc)
     * @see VDEWProfile#getMeterEvent(Date, long, String)
     */
    private MeterEvent getMeterEvent(Date date, long logcode) {
        return new MeterEvent(date,getEventCode(logcode),(int)logcode);
    }

    private int getEventCode(long status){
        int eiCode = 0;
        if( (status & CLEAR_LOADPROFILE) > 0 )
            eiCode |= MeterEvent.CLEAR_DATA;
        if( (status & CLEAR_LOGBOOK) > 0 )
            eiCode |= MeterEvent.CLEAR_DATA;
        if( (status & END_OF_ERROR) > 0 )
            eiCode |= MeterEvent.METER_ALARM;
        if( (status & BEGIN_OF_ERROR) > 0 )
            eiCode |= MeterEvent.METER_ALARM;
        if( (status & VARIABLE_SET) > 0 )
            eiCode |= MeterEvent.CONFIGURATIONCHANGE;
        if( (status & DEVICE_CLOCK_SET_INCORRECT) > 0 ) {
            getProtocolLink().getLogger().log(Level.INFO, "Applying SETCLOCK flag - Meter Event: DEVICE_CLOCK_SET_INCORRECT.");
            eiCode |= MeterEvent.SETCLOCK;
        }
        if( (status & SEASONAL_SWITCHOVER) > 0 )
            eiCode |= MeterEvent.OTHER;
        if( (status & FATAL_DEVICE_ERROR) > 0 )
            eiCode |= MeterEvent.FATAL_ERROR;
        if( (status & DISTURBED_MEASURE) > 0 )
            eiCode |= MeterEvent.OTHER;
        if( (status & POWER_FAILURE) > 0 )
            eiCode |= MeterEvent.POWERDOWN;
        if( (status & POWER_RECOVERY) > 0 )
            eiCode |= MeterEvent.POWERUP;
        if( (status & DEVICE_RESET) > 0 )
            eiCode |= MeterEvent.MAXIMUM_DEMAND_RESET;
        if( (status & RUNNING_RESERVE_EXHAUSTED) > 0 )
            eiCode |= MeterEvent.OTHER;
        return eiCode;
    }

    /* Overrides VDEWProfile#mapStatus2IntervalStateBits().
     *
     * Difference:
     * map bit 2 (Measure value disturbed) to IntervalStateBits.SHORTLONG
     *
     * (non-Javadoc)
     * @see VDEWProfile#mapStatus2IntervalStateBits(int)
     */
    private int asEiCode(int status) throws IOException {
        int eiCode = 0;
        if( (status & CLEAR_LOADPROFILE) > 0 )
            eiCode |= IntervalStateBits.OTHER;
        if( (status & CLEAR_LOGBOOK) > 0 )
            eiCode |= IntervalStateBits.OTHER;
        if( (status & END_OF_ERROR) > 0 )
            eiCode |= IntervalStateBits.OTHER;
        if( (status & BEGIN_OF_ERROR) > 0 )
            eiCode |= IntervalStateBits.OTHER;
        if( (status & VARIABLE_SET) > 0 )
            eiCode |=  IntervalStateBits.CONFIGURATIONCHANGE;
        if ((status & DEVICE_CLOCK_SET_INCORRECT) > 0) {
            getProtocolLink().getLogger().log(Level.INFO, "Applying ShortLong flag - Device status flag: DEVICE_CLOCK_SET_INCORRECT.");
            eiCode |= IntervalStateBits.SHORTLONG;
        }
        if( (status & FATAL_DEVICE_ERROR) > 0 )
            eiCode |= IntervalStateBits.OTHER;
        if ((status & DISTURBED_MEASURE) > 0) {
            getProtocolLink().getLogger().log(Level.INFO, "Applying ShortLong flag - Device status flag: DISTURBED_MEASURE.");
            eiCode |= IntervalStateBits.SHORTLONG;
        }
        if( (status & POWER_FAILURE) > 0 )
            eiCode |=  IntervalStateBits.POWERDOWN;
        if( (status & POWER_RECOVERY) > 0 )
            eiCode |= IntervalStateBits.POWERUP;
        if( (status & DEVICE_RESET) > 0 )
            eiCode |= IntervalStateBits.OTHER;
        if( (status & RUNNING_RESERVE_EXHAUSTED) > 0 )
            eiCode |= IntervalStateBits.OTHER;
        return eiCode;
    }

    /* Parse a (typical) line of data into an array of strings.
     *
     * For example:
     *
     * P.01(0070325013000)(0000)(15)(2)(1.5.0)(kW)(3.5.0)(kvar)
     * =>
     * new String {
     *      "0070325013000", "0000", "15", "2", "1.5.0", "kW",3.5.0", "kvar" }
     *
     */
    private String[] split(String line) throws IOException {

        ArrayList result = new ArrayList();

        StringBuffer tmp = new StringBuffer();
        boolean open = false;

        for( int i = 0; i < line.length(); i++ ) {

            if( open ) {
                if( line.charAt(i) == ')' ) {
                    result.add(tmp.toString());
                    tmp = new StringBuffer();
                    open = false;
                } else {
                    tmp.append( line.charAt(i) );
                }
            }

            if( !open && line.charAt(i) == '(' ) open = true;


        }

        return (String[]) result.toArray(new String[0]);

    }

    private boolean isError(String line) {
        return line.indexOf( "ERROR" ) != -1;
    }

    private boolean isHeader(String line){
        return line.indexOf( "P.01" ) != -1;
    }

    /** IntervalMap:
     *
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
     */
    class IntervalMap {

        TreeMap map = new TreeMap();

        /** retrieve the interval for a date
         * @throws IOException  */
        Interval get( Date date ) throws IOException {

            Calendar c = Calendar.getInstance(pTimeZone);
            c.setTime(date);

            if( !ParseUtils.isOnIntervalBoundary(c, pInterval) )
                ParseUtils.roundUp2nearestInterval(c, pInterval);

            Date key = c.getTime();
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
                IntervalData id = value.asIntervalData();
                if( id != null )
                    profileData.addInterval(id);
            }
        }

        public String toString(){
            StringBuffer sb = new StringBuffer();
            sb.append( "IntervalMap [ \n ");
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

        private Calendar key;
        private ArrayList entries = new ArrayList();

        Interval(Date endTime) {
            this.key = ProtocolUtils.getCalendar(pTimeZone);
            this.key.setTime(endTime);
        }

        Date next( ){
            return new Date( key.getTime().getTime() + (pInterval*1000) );
        }

        boolean isIn( Date time ){
            Date start = new Date(key.getTime().getTime() - (pInterval*1000));
            Date end = key.getTime();
            return
                start.equals(time) ||
                end.equals(time) ||
                ( time.after(start) && time.before(end) );
        }

        void addEntry(IntervalRow ir){
            entries.add(ir);
        }

        IntervalData asIntervalData(){

            int eiCode = 0;

            Iterator i = entries.iterator();
            while( i.hasNext() ) {
                IntervalRow ir = (IntervalRow)i.next();
                eiCode |= ir.getEiStatus();
            }

            if (entries.size() > 1) {
                getProtocolLink().getLogger().log(Level.INFO, "Applying ShortLong flag - The interval has multiple entries, interval: " + this.toString());
                eiCode |= IntervalStateBits.SHORTLONG;
            }

            IntervalData id = new IntervalData(key.getTime(),eiCode);

            for( int channel=0; channel<pNrChannels; channel++) {

                BigDecimal sum = BigDecimal.valueOf(0);
                Iterator ei = entries.iterator();

                while( ei.hasNext() ) {
                    IntervalRow ir = (IntervalRow)ei.next();
                    sum = sum.add(ir.get(channel));
                }

                ChannelInfo ci = ((ChannelInfo)getChannelInfos().get(0));

                //***************************************************************
                // Not correct exactly, normally the flows should be devided,
                // but now it is according to the MAN software

//                if( ci.getUnit().isFlowUnit() ) {
//                    BigDecimal size = new BigDecimal( entries.size());
//                    sum = sum.divide(size, BigDecimal.ROUND_HALF_UP);
//                }
                //***************************************************************
                id.addValue(sum);

            }

            return id;

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

            Date start = new Date(key.getTime().getTime() - (pInterval*1000));
            Date end = key.getTime();

            SimpleDateFormat longDf = new SimpleDateFormat( "yyyy.MM.dd HH:mm" );
            SimpleDateFormat shortDf = new SimpleDateFormat( "HH:mm z" );

            StringBuffer result = new StringBuffer()
                .append( "Interval [" )
                .append( longDf.format(start) )
                .append( " - " )
                .append( shortDf.format(end) )
                .append( ", " );

            Iterator i = entries.iterator();
            while( i.hasNext() ){
                result.append( i.next() + " " );
            }

            result.append( "]" );
            return result.toString();

        }

    }

    class IntervalRow {

        private int eiStatus;
        private List values;

        IntervalRow(int eiStatus, String [] values){

            this.eiStatus = eiStatus;
            this.values = new ArrayList();

            for( int i = 0; i < values.length; i ++ )
                addInterval(values, i);

        }

        /**
         * Create a single interval value.
         * When a blank is encountered (---.-) an missing interval flag is set.
         */
        private void addInterval(String[] values, int i) {

            if( "---.-".equals(values[i]) ) {
                this.values.add( new BigDecimal( 0 ) );
                this.eiStatus |= IntervalStateBits.MISSING;
            } else {
                this.values.add( new BigDecimal( values[i] ) );
            }

        }

        int getEiStatus(){
            return eiStatus;
        }

        BigDecimal get(int idx){
            return (BigDecimal)values.get(idx);
        }

        public String toString( ){
            StringBuffer rslt = new StringBuffer();
            rslt.append( "[ st " + eiStatus );

            Iterator i = values.iterator();
            while( i.hasNext() )
                rslt.append( ", " ).append( i.next() );
            rslt.append( "]" );

            return rslt.toString();
        }

    }

    protected List buildMeterEvents(byte[] data) throws IOException {

        dump( data, DBG_EVENT_DMP );

        List rslt = new ArrayList();

        Assembly assembly = new Assembly(data);
        String line = null;

        while( (line = assembly.readLine()) != null ) {

            String [] field = split(line);

            Date time = asDate(field[0]);
            int pCode = Integer.parseInt(field[1], 16);

            if( pCode == 0 ) continue;

            rslt.add( getMeterEvent(time, pCode) );

            dbg( assembly.dbgString() );
            dbg( asDate(field[0]) + " " + getMeterEvent(time, pCode) );

        }

        return rslt;

    }

    /* Keep track of parsing progress. */
    class Assembly {

        private int lineNr;
        private String line;
        private BufferedReader reader;

        Assembly(byte[] data){
            String s = new String(data);
            reader = new BufferedReader( new StringReader( s.trim() ) );
        }

        public String readLine() throws IOException {
            lineNr = lineNr + 1;
            line = reader.readLine();
            return line;
        }

        String dbgString( ){
            return "" + lineNr + ":" + line;
        }

    }

    private void dbg(String msg){
        if( DBG ) System.out.println(msg);
    }

    /* during dbging, dump data to a file */
    private void dump(byte [] data, String fName){
        if( DBG ){
            try {
                File f = new File( fName );
                FileOutputStream fos = new FileOutputStream(f);
                fos.write(data);
                fos.close();
            } catch( IOException ioe){
                ioe.printStackTrace();
            }
        }
    }

    /*
     * When running in dbg mode, the profile data is dmped into a file:
     * c:\dmp.txt.
     *
     * When starting Profile.main this file is parsed to profile data.
     *
     */
    public static void main(String [] args) throws IOException {

        Profile p = new Profile( null, new ProtocolLink(){

            public ChannelMap getChannelMap() {     return null; }
            public byte[] getDataReadout() {        return null; }

            public FlagIEC1107Connection getFlagIEC1107Connection() {
                return null;
            }
            public Logger getLogger() {             return null; }
            public int getNrOfRetries() {           return 0; }

            public int getNumberOfChannels() throws IOException {
                return 2;
            }
            public String getPassword() {           return null; }
            public int getProfileInterval() throws IOException { return 900; }
            public ProtocolChannelMap getProtocolChannelMap() { return null; }
            public TimeZone getTimeZone() { return TimeZone.getTimeZone("CET"); }
            public boolean isIEC1107Compatible() { return false; }
            public boolean isRequestHeader() { return false;
            }}, null);


        File f = new File( DBG_PROFILE_DMP );
        FileInputStream fis = new FileInputStream(f);

        byte [] data = new byte[(int)f.length()];
        fis.read(data);

        System.out.println(p.buildProfileData(data));
//
//        System.out.println( "------------------------" );
//
        f = new File( DBG_EVENT_DMP );
        fis = new FileInputStream(f);

        data = new byte[(int)f.length()];
        fis.read(data);

        p.buildMeterEvents(data);



    }

}

