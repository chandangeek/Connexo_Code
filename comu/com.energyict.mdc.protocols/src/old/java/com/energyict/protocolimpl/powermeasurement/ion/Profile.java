package com.energyict.protocolimpl.powermeasurement.ion;

import com.energyict.mdc.common.interval.IntervalStateBits;
import com.energyict.mdc.protocol.api.UnsupportedException;
import com.energyict.mdc.protocol.api.device.data.ChannelInfo;
import com.energyict.mdc.protocol.api.device.data.IntervalData;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import com.energyict.cbo.Unit;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;

/**
 *
 * @author fbo
 *
 */

class Profile {

    private final static boolean DBG = false;

    private Ion ion;

    private IonHandle dataRecorderModule;
    private IonHandle dataRecorderHandle;
    private int numberOfChannels [];
    private boolean inputUsed[];
    private Date meterTime;

    Profile( Ion ion ) {
        this.ion = ion;
    }

    /** Procedure for counting the number of channels
     *
     * 1 get the module handle
     * 2 read it's input handles
     * 3 of the first 16 input handles count the ones that are not 0
     *
     */
    int getNumberOfChannels() throws IOException {
        if( numberOfChannels == null ) {

            IonHandle h = getDataRecorderHandle().getModule();
            Command c = new Command( h, IonMethod.READ_MODULE_INPUT_HANDLES );
            ion.getApplicationLayer().read( c );

            numberOfChannels = new int[1];
            inputUsed = new boolean[16];
            Iterator i = ((IonList)c.getResponse()).iterator();
            for( int ci = 0; ci < 16; ci ++ ) {
                int handle = ((IonInteger)i.next()).getIntValue();
                if( handle != 0 ) {
                    numberOfChannels[0] = numberOfChannels[0] + 1;
                    inputUsed[ci] = true;
                } else {
                    inputUsed[ci] = false;
                }
            }
        }
        return numberOfChannels[0];
    }

    /* Get all the available profile data.  This means for position 0
     * to last LogPosition */
    ProfileData getProfileData(boolean includeEvents) throws IOException {

        IonHandle pHandle = getDataRecorderHandle();
        int logPosition = getLogPosition( pHandle ) - 1;

        int start = 0;          // 0 is first logposition
        int end = logPosition;

        ProfileData pd = getProfileData( pHandle, start, end );
        pd.setIntervalDatas(ProtocolTools.mergeDuplicateIntervals(pd.getIntervalDatas()));

        if( includeEvents ) {
            Iterator iter = readLogRecords(null, null).iterator();
            while (iter.hasNext()) {
                MeterEvent me = (MeterEvent) iter.next();
                pd.addEvent( me );
            }
            pd.applyEvents( ion.getProfileInterval()/60 );
        }
        return pd;
    }

    /* Get profile data since lastReading.  By calculating the number
     * of intervals that have passed since lastReading. */
    ProfileData getProfileData( Date lastReading, boolean includeEvents)
        throws IOException {

        if( lastReading == null || !lastReading.before( getMeterTime() ) )
            lastReading = getMeterTime();

        IonHandle pHandle = getDataRecorderHandle();
        int logPosition = getLogPosition( pHandle ) - 1;

        int start = logPosition - nrIntervalsSince(lastReading);
        int end = logPosition;

        start = start < 0 ? 0 : start;

        ProfileData pd = getProfileData( pHandle, start, end );
        pd.setIntervalDatas(ProtocolTools.mergeDuplicateIntervals(pd.getIntervalDatas()));

        if( includeEvents ) {
            Iterator iter = readLogRecords(lastReading, null).iterator();
            while (iter.hasNext()) {
                MeterEvent me = (MeterEvent) iter.next();
                pd.addEvent( me );
            }
            pd.applyEvents( ion.getProfileInterval()/60);
        }
        return pd;
    }

    /* Get profile data between from and to.  */
    ProfileData getProfileData( Date from, Date to, boolean includeEvents)
        throws IOException {

        if( from == null || !from.before( getMeterTime() ) )
            from = getMeterTime();

        if( to == null || !to.before( getMeterTime() ) )
            to = getMeterTime();

        IonHandle pHandle = getDataRecorderHandle();
        int logPosition = getLogPosition( pHandle ) - 1;

        int start = logPosition - nrIntervalsSince(from);
        int end = logPosition - nrIntervalsSince(to);

        start = start < 0 ? 0 : start;
        end = end < 0 ? 0 : end;

        ProfileData pd = getProfileData( pHandle, start, end );
        pd.setIntervalDatas(ProtocolTools.mergeDuplicateIntervals(pd.getIntervalDatas()));
        if( includeEvents ) {
            Iterator iter = readLogRecords(from, to).iterator();
            while (iter.hasNext()) {
                MeterEvent me = (MeterEvent) iter.next();
                pd.addEvent( me );
            }
            pd.applyEvents( ion.getProfileInterval()/60 );
        }
        return pd;
    }

    /* Actuall fetching of profile data. Fetching happens on basis of positions.
     * - read from start to end
     * - a group of records is returned from start to start+x
     * - read from start+x to end
     * and repeat until end received
     */
    private ProfileData getProfileData( IonHandle handle, int start, int end )
        throws IOException {

        List cInfo = null;
        if( ion.pChannelMap == null ) {
            cInfo = new ArrayList();
            for( int i = 0; i < getNumberOfChannels(); i ++ ) {
                Unit undef = Unit.getUndefined();
                cInfo.add( new ChannelInfo( i, "channel_"+i, undef ) );
            }
        } else {
            cInfo = ion.pChannelMap.toChannelInfoList();
        }

        ProfileData pd = new ProfileData();
        pd.setChannelInfos( cInfo );

        while( start < end ) {

            debug( "\n REQUESTING: start " + start + " to end " + end );

            Command cmd =
                new Command( handle, IonMethod.READ_VALUE )
                    .setArguments( new IonRange(start, end).toByteArray() );

            ion.getApplicationLayer().read( cmd );
            if( cmd.getResponse() != null ) {
                if( !cmd.getResponse().isException() ) {
                    start = addToProfile(pd, (IonList)cmd.getResponse() ) + 1;
                    printDates( (IonList)cmd.getResponse());
                } else {
                    debug( "ION EXCEPTION => skip interval" );
                    start += 15;
                }
            } else {
                debug( "NULL => again " );
            }

        }

        debug( "nr of intervals " + pd.getNumberOfIntervals() );

        return pd;


    }

    /**
     * Add a list of profile values to a profile.  The list contains
     * log counters, dates and values
     *
     * @return the last logPosition that was added
     * @throws IOException
     * */
    private int addToProfile(ProfileData profile, IonList list) throws IOException {

        int iId = 0;
        for (Iterator i = list.iterator(); i.hasNext();) {

            Object o = i.next();
            iId = ((IonInteger)o).getIntValue();   // read logPosition
            Date iDate = (Date)((IonObject)i.next()).getValue();
            IntervalData intervalData = new IntervalData( iDate );  // read logDate

            for( int ci = 0; ci < getNumberOfChannels(); ci ++ ) {

                IonObject anObject = (IonObject)i.next();

                // Float indicates a valid entry
                if( anObject.isFloat() ) {
                    Float v = (Float)((IonFloat)anObject).getValue();
                    intervalData.addValue( v );
                } else {
                    // not a valid entry
                    // Exception values are ignored
                    intervalData.addValue( new BigDecimal( 0 ) );
                    intervalData.setProtocolStatus( ci, IntervalStateBits.MISSING );

                }
            }
            i.next();
            profile.addInterval( intervalData );

        }

        return iId;

    }

    /*
     * Calculate the number of intervals that are stored in the meter since
     * the since time.
     */
    private int nrIntervalsSince( Date since ) throws IOException {

        // meter time in sec
        long meterTime = ion.getTime().getTime() / 1000;
        // from time in sec
        long fromTime = since.getTime() / 1000;
        // profile interval in sec
        int pInterval = ion.getProfileInterval();
        // diff in seconds
        long diff = meterTime - fromTime;

        if( diff < 0 ) {
            String msg =
                "Attempting to retrieve intervals that have not occurec yet.";
            throw new IOException( msg );
        }

        return (int)diff / pInterval;

    }

    /*
     * For debug purposes.
     */
    void printDates( IonList list ) {
        int i = 0;
        String buffer = "";

        for (Iterator iter = list.iterator(); iter.hasNext(); ) {

            IonObject element = (IonObject) iter.next();
            if( i > 0  && (i%17) == 0 ) {
                debug( buffer );
                i = 0;
                buffer = "";
            } else {
                buffer = buffer + element + "\t";
                i += 1;
            }

        }
    }

    /**
     * Read the LogArray in reverse order, until a Log Record is encountered
     * that was created before the from date.
     *
     * The events are fetched in groups of 15.
     *
     * @return List of all Log Records
     * @throws IOException
     */
    public Collection readLogRecords( Date from, Date to ) throws IOException {

        TreeMap nativeEvents = new TreeMap();
        Collection eiEvents = new ArrayList();

        int logPos = getLogPosition( IonHandle.ELC_1_EVENT_LOG_ELR );

        Date oldestRec = new Date( );   /* the oldest read record           */
        int begin = logPos - 15;        /* begin log position               */
        int previousBegin = -1;         /* previously fetched first log pos
                                         * -1 does not exist as logpos      */

        boolean fetch = true;
        while( fetch ) {

            Command c =
                ion.toCmd(IonHandle.ELC_1_EVENT_LOG_ELR, IonMethod.READ_VALUE)
                    .setArguments( new IonRange( begin, logPos ).toByteArray() );
            ion.getApplicationLayer().read( c );

            RecordInfo rec = addLogRecord((IonList)c.getResponse(), from, to );

            /* the first record that is returned is actually the oldest */
            oldestRec = rec.firstRecordDate;

            /* safety measure: when the same record is returned twice/
             * the beginning of the logs was reached, so stop fetching */
            if( rec.start == previousBegin ) {
                break;
            }

            nativeEvents.putAll( rec.nativeEvents );
            eiEvents.addAll( rec.eiEvents );
            previousBegin = rec.start;

            /* calculate the next interval begin */
            if( rec.start < 15 )
                begin = 0;
            else
                begin = rec.start - 15;

            fetch = (from != null) && from.before( oldestRec );
            fetch = fetch & (begin > 0);

        }
        printEvents( nativeEvents );
        return eiEvents;

    }

    /*
     * For debugging.
     */
    void printEvents( Map map ) {
        if( ion.logger.getLevel().intValue() >= Level.INFO.intValue() ) {
            Iterator i = map.keySet().iterator();
            while (i.hasNext()) {
                Object key = i.next();
                Object value = map.get( key );
                ion.logger.log( Level.INFO, key + " " + value );
                System.out.println( key + " " + value  );
            }
        }
    }

    RecordInfo addLogRecord( IonList list, Date from, Date to ) {

        List al = stripEndOfStructs( list );
        RecordInfo log = new RecordInfo();
        int iId = -1;

        for (Iterator i = al.iterator(); i.hasNext();) {

            IonObject ionO = (IonObject)i.next();
            if( ionO.isInteger() ) {

                // read logPosition
                iId = ((IonInteger)ionO).getIntValue();
                Date iDate = (Date)((IonObject)i.next()).getValue();
                IonObject event = (IonObject) i.next();

                boolean add = true;
                add &= ( from!= null && iDate.after( from ) );
                if (to == null)
                	to = new Date();
                add &= ( to!=null && iDate.before( to ) );

                if( add ) {
                    log.eiEvents.add( toMeterEvent(iId, iDate, event) );
                    log.nativeEvents.put( new Integer( iId ), event );
                }

                if(log.firstRecordDate == null) {
                    log.start = iId;
                    log.firstRecordDate = iDate;
                }

            } else {

                System.out.println( " Problem event " + ionO );

            }
        }

        log.stop = iId;
        return log;

    }


    /** Convert a ion event to a MeterEvent object
     *
     * @param id logPosition of Log Record
     * @param iDate date of event occurence
     * @param ionObject ion event object
     * @return MeterEvent describing the ion event
     */
    private MeterEvent toMeterEvent(int id, Date iDate, IonObject ionObject) {

        IonStructure s = (IonStructure)ionObject;
        StringBuffer sb = new StringBuffer();
        Object o = s.get( "causeValue" );
        if( o!= null ) {
            Object value = ((IonObject)o ).getValue();
            sb.append( value + ", " );
        }

        Object effectObject = s.get( "effectValue" );
        String effectValue = "";
        if(((IonObject) effectObject).isException()){
            effectValue = effectObject.toString();
        } else if( ((IonObject)effectObject).getValue() != null ){
            effectValue = ((IonObject)effectObject).getValue().toString();
        }

        if( o!= null ) {
            sb.append( effectValue + ", " );
        }

        Object effectHandle = s.get( "effectHandle" );
        Object causeHandle  = s.get( "causeHandle" );
        Object priority     = s.get( "priority" );
        Object eventState   = s.get( "eventState" );

        sb  .append( "id "              + Integer.toString(id) + ", " )
            .append( "effect handle "   + effectHandle + ", " )
            .append( "cause handle "    + causeHandle + ", " )
            .append( "priority "        + priority + ", " )
            .append( "eventState "      + eventState + ", " );

        MeterEvent me = null;
        if( s.get( "effectHandle" ) != null ) {
            int eh = ((IonInteger)effectHandle).getIntValue();
            me = new MeterEvent( iDate, toEiCode( effectValue ), eh, sb.toString() );
        } else {
            me = new MeterEvent( iDate, toEiCode( effectValue ), sb.toString() );
        }

        return me;

    }

    int toEiCode( String effectValue ) {

        if( effectValue == null )
            return MeterEvent.OTHER;

        effectValue = effectValue.toUpperCase();


        if( effectValue.indexOf( "POWER DOWN" ) != -1 )
            return MeterEvent.POWERDOWN;

        if( effectValue.indexOf( "POWER UP" ) != -1 )
            return MeterEvent.POWERUP;

        if( effectValue.indexOf( "TIME ABOUT TO BE CHA" ) != -1 )
            return MeterEvent.SETCLOCK_BEFORE;

        if( effectValue.indexOf( "TIME CHANGED" ) != -1 )
            return MeterEvent.SETCLOCK_AFTER;

        if( effectValue.indexOf( "9011" ) != -1 )
            return MeterEvent.WATCHDOGRESET;

        if( effectValue.indexOf( "PROGRAM MALFUNCTION DETECTED" ) != -1 )
            return MeterEvent.FATAL_ERROR;

        if( effectValue.indexOf( "BILLING RESET" ) != -1 )
            return MeterEvent.BILLING_ACTION;

        if( effectValue.indexOf( "VOLTAGE SAG" ) != -1 )
            return MeterEvent.VOLTAGE_SAG;

        if( effectValue.indexOf( "VOLTAGE SWELL" ) != -1 )
            return MeterEvent.VOLTAGE_SWELL;

        return MeterEvent.OTHER;

    }

    /* filter out the end of strucuture's */
    private List stripEndOfStructs( IonList aList ) {

        ArrayList rslt = new ArrayList();

        Iterator i = aList.iterator();
        while (i.hasNext()) {
            IonObject element = (IonObject) i.next();
            if( !element.isEndOf() )
                rslt.add( element );
        }

        return rslt;

    }

    private class RecordInfo {
        Date firstRecordDate;
        int start;
        int stop;
        Map nativeEvents = new HashMap();
        List eiEvents = new ArrayList();
    }

    /**
     * Fetch the profile interval
     * @param ion
     * @return profile interval in seconds
     * @throws UnsupportedException
     * @throws IOException
     */
    public int getProfileInterval(Ion ion) throws UnsupportedException, IOException {

        Command cPeriod =
            new Command( IonHandle.PRT_1_PERIOD_NBR, IonMethod.READ_REGISTER_VALUE );

        ArrayList list = new ArrayList();
        list.add( cPeriod );

        ion.getApplicationLayer().read( list );

        return (int)((IonFloat)cPeriod.getResponse()).getFloatValue();

    }

    /* Retrieve the log position of an Event Log register. */
    private int getLogPosition( IonHandle logHandle ) throws IOException{
        return
            ((IonInteger)
                ion.getApplicationLayer().read(
                new Command( logHandle, IonMethod.READ_LOG_REGISTER_POSITION ) )
                .getResponse()).getIntValue();
    }

    private Date getMeterTime( ) throws IOException {
        if (meterTime == null) {
            meterTime = ion.getTime();
        }
        return meterTime;
    }

    /* Fetch all module handles from the feature manager. Read all the classes
     * of the modules.  The module with class 537 is the Data Recorder. */
    IonHandle getDataRecorderModule( ) throws IOException {
        if( dataRecorderModule == null ) {

            IonHandle fm = IonHandle.FEATURE_MANAGER;
            IonMethod sh = IonMethod.READ_MODULE_SETUP_HANDLES;
            Command cFManager = new Command( fm, sh );
            ion.getApplicationLayer().read( cFManager );

            ArrayList cmdList = new ArrayList();
            IonList ionList = (IonList)cFManager.getResponse();
            for (Iterator iter = ionList.iterator(); iter.hasNext();) {
                IonInteger ionInt = (IonInteger) iter.next();
                IonHandle handle = IonHandle.create( ionInt.getIntValue() );
                cmdList.add( new Command( handle, IonMethod.READ_MANAGED_CLASS) );
            }

            ion.getApplicationLayer().read( cmdList );
            for (Iterator iter = cmdList.iterator(); iter.hasNext();) {
                Command cmd = (Command) iter.next();
                IonInteger ionInt = (IonInteger)cmd.getResponse();
                if( ionInt.getIntValue() == 537 ) {
                    dataRecorderModule = cmd.getHandle();
                    break;
                }
            }

            if( dataRecorderModule == null ) {
                String msg = "No data recorder module found.";
                throw new IOException( msg );
            }

        }
        return dataRecorderModule;

    }

    IonHandle getDataRecorderHandle( ) throws IOException {
        if( dataRecorderHandle == null ){
            Command cDataRec =
                ion.toCmd( getDataRecorderModule(), IonMethod.READ_MODULE_SETUP_HANDLES );
            ion.getApplicationLayer().read( cDataRec );

            List cmds =
                ion.toCmd( ion.collectHandles(
                        (IonList)cDataRec.getResponse() ), IonMethod.READ_ION_LABEL );

            ion.getApplicationLayer().read( cmds );
            for (Iterator iter = cmds.iterator(); iter.hasNext();) {
                Command cmd = (Command) iter.next();
                String string = (String)cmd.getResponse().getValue();
                if( ion.pDataRecorderName.equals( string ) )
                    dataRecorderHandle = cmd.getHandle();
            }

            if( dataRecorderHandle == null ) {
                String msg =
                    "Data Recorder whith name: \"" + ion.pDataRecorderName +
                    "\" could not be found";
                throw new IOException( msg );
            }

            Command c = ion.toCmd( dataRecorderHandle, IonMethod.READ_MODULE_OUTPUT_HANDLES );
            ion.getApplicationLayer().read( c );
            IonList list = (IonList)c.getResponse();

            dataRecorderHandle = IonHandle.get( ((IonInteger)list.get( 0 )).getIntValue() );

        }
        return dataRecorderHandle;
    }

    private void debug(String msg) {
        if(DBG) {
            ion.logger.log( Level.INFO, msg );
            System.out.println( msg );
        }
    }

}
