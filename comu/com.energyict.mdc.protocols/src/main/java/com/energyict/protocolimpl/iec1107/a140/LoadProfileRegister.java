/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.iec1107.a140;

import com.energyict.mdc.common.Unit;
import com.energyict.mdc.common.interval.IntervalStateBits;
import com.energyict.mdc.protocol.api.device.data.ChannelInfo;
import com.energyict.mdc.protocol.api.device.data.IntervalData;
import com.energyict.mdc.protocol.api.device.data.IntervalValue;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.device.events.MeterEvent;
import com.energyict.protocols.util.ProtocolUtils;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.logging.Level;

public class LoadProfileRegister extends Register {

    /* When debugging parsing */
    static final int pDbg = 0;

    private TimeZone timeZone;
    private ProfileData profileData;

    private SortedMap demandMap = new TreeMap();

    public LoadProfileRegister(A140 a140, String id, int length, int sets, int options) {
        super(a140, id, length, sets, options);
        timeZone = a140.getTimeZone();
    }

    public ProfileData getProfileData() throws IOException {
        a140.getRegisterFactory().getConfigureRead().setValue( 0xFFFF );
        return doGetProfileData();
    }

    public ProfileData getProfileData(Date from, Date to) throws IOException {

        int nrOfDays = getNrDaysToRetrieve(from, to);

        if( nrOfDays >= 300 )
            a140.getRegisterFactory().getConfigureRead().setValue( 0xFFFF );
        else
            a140.getRegisterFactory().getConfigureRead().setValue( nrOfDays );

        return doGetProfileData();

    }

    public ProfileData doGetProfileData() throws IOException {
        a140.getRegisterFactory().getConfigureRead().write();
        read();

        ArrayList events = new ArrayList();
        events.addAll(a140.getRegisterFactory().getReverseRun().getEvents());
        events.addAll(a140.getRegisterFactory().getPowerFail().getEvents());
        events.addAll(a140.getRegisterFactory().getBilling().getEvents());

        Iterator i = events.iterator();
        while( i.hasNext() )
            profileData.addEvent((MeterEvent) i.next());
        // this is not necessary ...
        // profileData.applyEvents( a140.getProfileInterval() );

        if(pDbg>0)
            System.out.println(profileDataToCsv(profileData));

        return profileData;
    }

    public void parse(byte[] ba) throws IOException {
        ChannelInfo ci = new ChannelInfo( 0, "Import Kw", Unit.get( "W") );
        profileData = new ProfileData();
        profileData.addChannel( ci );

        // no matter how the date is parsed, we start at the next 0xE4 sign ...
        for( int i = 0; i < ba.length; i ++ ) {
            if( (ba[i]&0xFF) == 0xE4 ) {
                new Day( ba, i );
                i = i + 5;   // bugfix 24/11/2006
            }
        }

        Iterator i = demandMap.keySet().iterator();
        while( i.hasNext() ){
            Date key = (Date)i.next();
            IntervalData interval = ((DemandEntry)demandMap.get(key)).toIntervalData();
            if( interval != null )
                profileData.addInterval(interval);
        }

        profileData.sort();

    }

    /* (non-Javadoc)
     * @see com.energyict.protocolimpl.iec1107.a140.Register#getSets()
     */
    public int[] getSets( ) throws IOException{
        int nrPackets =  a140.getRegisterFactory().getConfigureRead().getValue();
        int [] result = new int[nrPackets];
        for( int i = 0; i < nrPackets; i ++ ) {
            result[i] = 64; // all packets are 64 long
        }
        return result;
    }

    /** Calculate the nr of days that must be retrieved */
    private int getNrDaysToRetrieve( Date from, Date to ) throws IOException {

        // from MUST be >= TO, otherwise: "no can do"
        if (to.getTime() < from.getTime())
            throw new IOException("error ("+from+") > ("+to+")");

        String msg = "getProfileData() -> a140.getTimeZone() " + a140.getTimeZone();
        a140.getLogger().log( Level.FINEST, msg );

        Calendar fromC = ProtocolUtils.getCalendar( a140.getTimeZone() );
        fromC.setTime( from );

        fromC.set( Calendar.HOUR_OF_DAY, 0 );
        fromC.set( Calendar.MINUTE, 0 );
        fromC.set( Calendar.SECOND, 0 );
        fromC.set( Calendar.MILLISECOND, 0 );

        Calendar toC = ProtocolUtils.getCalendar( a140.getTimeZone() );
        toC.setTime( to );

        toC.set( Calendar.HOUR_OF_DAY, 23 );
        toC.set( Calendar.MINUTE, 59 );
        toC.set( Calendar.SECOND, 59 );
        toC.set( Calendar.MILLISECOND, 999 );

        long mDiff = toC.getTimeInMillis() - fromC.getTimeInMillis();
        long nrDays = ( ( mDiff ) / 86400000 ) + ( ( mDiff % 86400000 ) > 0 ? 1 : 0 );
        if( nrDays > 301 ) nrDays = 301;

        msg = "Retrieving " + nrDays + " days. ";
        a140.getLogger().log(Level.FINEST, msg );

        return (int)nrDays;

    }

    /**
     * A New Day marker comprises: (6 bytes)
     * a) E4h - New Day Marker (1 byte, HEX) followed by
     * b) Time/Date stamp (4bytes, HEX) followed by
     * c) Demand Period/Daylight Saving Adjusted (1 byte, HEX)
     */
    class Day {

        /* b field (time stamp) */
        Date date;
        Date sDate;

        /* c field (demand period) */
        boolean adjustEnabled = false;      // bit 7 adjust enabled
        boolean adjustToday = false;        // bit 6 adjust today
        int adjustHours = 0;                // bit 5,4 amount of dls adjustment
        boolean adjustAdvance = false;      // bit 3 advance (=true) or retard
        int demandPeriod = 0;               // bit 2,1,0 demandPeriod

        Day( byte[] ba, int pos ) throws IOException{

            // parse b-field
            long dl = ProtocolUtils.getIntLE(ba,pos+1,4)&0xFFFFFFFFL;
            Calendar c = ProtocolUtils.getCalendar(timeZone,dl);

            c.clear( Calendar.HOUR_OF_DAY );
            c.clear( Calendar.MINUTE );
            c.clear( Calendar.SECOND );
            c.clear( Calendar.MILLISECOND );
            sDate = c.getTime();

            if( pDbg > 0 )
                System.out.println(sDate);

            // parse c-field
            long l = ba[pos+5]&0xFF;

            adjustEnabled   = ( ( l & 0x80 ) == 1 );
            adjustToday     = ( ( l & 0x40 ) == 1 );

            adjustHours = (int)l & 0x30;

            adjustAdvance   = ( ( l & 0x08 ) == 1 );

            if( ( l & 0x07 ) == 1 )
                demandPeriod = 10;
            if( ( l & 0x07 ) == 2 )
                demandPeriod = 15;
            if( ( l & 0x07 ) == 3 )
                demandPeriod = 20;
            if( ( l & 0x07 ) == 4 )
                demandPeriod = 30;
            if( ( l & 0x07 ) == 5 )
                demandPeriod = 60;

            int nrIntervals = 24 * 60 / demandPeriod;
            if( adjustToday ) nrIntervals += adjustHours;

            int dPos = pos + 4;
            for( int i = 0; (i < nrIntervals) && (dPos < ba.length-2); i ++ ) {
                dPos += 2;
                c.add( Calendar.MINUTE, demandPeriod );
                Date t = c.getTime();
                DemandEntry dEntry = new DemandEntry( c.getTimeZone(), t, ba, dPos );
                add( dEntry );

                if( pDbg > 0 )
                    System.out.println( dEntry );

            }

        }

        void add( DemandEntry d ){
            Object previous = demandMap.get( d.getId() );
            if( previous == null ) {
                demandMap.put( d.getId(), d );
            } else {
                if( !d.isInitData() )
                    demandMap.put( d.getId(), d );
            }
        }

        public String toString( ){
            StringBuffer result = new StringBuffer();
            result.append( "Day " );
            result.append( "date=" + date + ", " );
            result.append( "adjustEnabled=" + adjustEnabled + ", " );
            result.append( "adjustToday=" + adjustToday + ", " );
            result.append( "adjustHours=" + adjustHours + ", " );
            result.append( "adjustAdvance=" + adjustAdvance + ", " );
            result.append( "demandPeriod=" + demandPeriod  );
            return result.toString();
        }

    }

    /**
     * Load profile demand data entry 2 bytes (16 bit hexadecimal number)
     * comprises:
     * a) Load Profile Status Flags - 7 bits
     * b) Load Profile Demand Data - 9 least significant bits
     */
    class DemandEntry {

        final static long INIT_DATA = 0xFFFF;

        final static long METER_ERROR = 0x02;
        final static long TIME_SYNC = 0x04;
        final static long DATA_CHANGE = 0x08;
        final static long BATTERY_FAIL = 0x10;
        final static long TIME_CHANGE = 0x20;
        final static long REVERSE_RUNNING = 0x40;
        final static long SUPPLY_FAIL = 0x80;

        byte [] source;
        int sourcePos;

        long allData;
        long status = 0;
        long demand = 0;

        TimeZone timeZone = null;
        Date date = null;

        /* one format per thread, so one per instance */
        DateFormat dateFormat = null;

        DemandEntry( TimeZone timeZone, Date date, byte[] ba, int pos ) throws IOException{
            this.source = ba;
            this.sourcePos = pos;

            this.timeZone = timeZone;
            this.date = date;
            this.allData = ProtocolUtils.getLong( ba, pos, 2 );

            if( this.allData != INIT_DATA ) {
                this.status = ba[pos+1] & 0xFF;
                this.demand = ( ( (ba[pos+1]&0x01) * 256) + (ba[pos]&0xFF) ) * 60;
            }

            dateFormat = new SimpleDateFormat(" MMddHHmm");
            dateFormat.setTimeZone( timeZone );
        }

        boolean hasStatus( long aStatus ) {
            return ( status & aStatus ) > 0;
        }

        boolean isInitData( ){
            return allData == INIT_DATA;
        }

        private Date getId( ){
            return date;
        }

        IntervalData toIntervalData( ){

            IntervalData intervalData = new IntervalData( date );

            if ( (status & METER_ERROR) > 0 )
                intervalData.addEiStatus(IntervalStateBits.OTHER);
            if ( (status & TIME_SYNC) > 0 )
                intervalData.addEiStatus(IntervalStateBits.SHORTLONG);
            if ( (status & DATA_CHANGE) > 0 )
                intervalData.addEiStatus(IntervalStateBits.CONFIGURATIONCHANGE);
            if ( (status & BATTERY_FAIL) > 0 )
                intervalData.addEiStatus(IntervalStateBits.OTHER);
            if( (status & TIME_CHANGE) > 0 )
                intervalData.addEiStatus(IntervalStateBits.SHORTLONG);
            if ( (status & REVERSE_RUNNING) > 0 )
                intervalData.addEiStatus(IntervalStateBits.REVERSERUN);
            if ( (status & SUPPLY_FAIL) > 0 )
                intervalData.addEiStatus(IntervalStateBits.POWERDOWN);

            if( isInitData() )
                return null;

            intervalData.addValue(new Long(demand));

            return intervalData;

        }

        public String toString( ){
            StringBuffer result = new StringBuffer();
            if( pDbg > 0 )
                DataType.toShortString( result, source, sourcePos );
            result.append( timeZone.getID() + " " + dateFormat.format( getId() )  );
            if( isInitData() ) {
                result.append("[Init Data] ");
            } else {
                result.append( " demand=" + demand );
                if(hasStatus(METER_ERROR))      result.append(" [Meter Error] ");
                if(hasStatus(TIME_SYNC))        result.append(" [Time Sync] ");
                if(hasStatus(DATA_CHANGE))      result.append(" [Data Change] ");
                if(hasStatus(BATTERY_FAIL))     result.append(" [Battery Fail] ");
                if(hasStatus(TIME_CHANGE))      result.append(" [Time Change] ");
                if(hasStatus(REVERSE_RUNNING))  result.append(" [Reverse Running] ");
                if(hasStatus(SUPPLY_FAIL))      result.append(" [Supply Fail] ");

            }
            return result.toString();
        }
    }

    private String profileDataToCsv(ProfileData pd) {

        StringBuffer result = new StringBuffer();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");

        Iterator intervalI = pd.getIntervalIterator();
        while( intervalI.hasNext() ){
            IntervalData id = (IntervalData)intervalI.next();
            Date time = id.getEndTime();
            IntervalValue iv = (IntervalValue)id.getValuesIterator().next();

            result
                .append(dateFormat.format(time))
                .append(",")
                .append(iv.toString().replace(' ', ','))
                .append( "\n" );
        }

        return result.toString();
    }

}
