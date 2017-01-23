package com.energyict.protocolimpl.landisgyr.maxsys2510;

import com.energyict.mdc.protocol.api.device.data.ChannelInfo;
import com.energyict.mdc.protocol.api.device.data.IntervalData;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.device.events.MeterEvent;
import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Stack;
import java.util.TimeZone;

class Table12 {

    int storageChnnls;
    int storK;
    Date lastIntlTime;

    int dataSize;
    int noOfChnls;
    int intervalMinutes;

    int nrOfIntervals;

    ArrayList channelInfo;
    ProfileData profileData;
    TimeZone timeZone;

    static Table12 parse( Assembly assembly, boolean includeEvents, int nrOfIntervals, boolean readProfileDataBeforeConfigChange) throws IOException{
        Table12 t = new Table12();

        MaxSys max = assembly.getMaxSys();
        Table0 t0 = max.getTable0();
        Table11 t11 = max.getTable11();

        t.nrOfIntervals = nrOfIntervals;

        t.timeZone = max.getTimeZone();
        t.storageChnnls = t0.getTypeMaximumValues().getMaxStorageChnnls();
        t.storK = t0.getTypeMaximumValues().getMaxStorK();
        t.dataSize = t11.getTypeStoreCntrlRcd().getDataSize();
        t.noOfChnls = t11.getTypeStoreCntrlRcd().getNoOfChnls();

        t.intervalMinutes = t11.getTypeStoreCntrlRcd().getIntvlInMins();

        t.parseChannelInfo(assembly);

        //The next
        //item is the LAST_INTVL_TIME which defines the time of the most recent End Of
        //Interval (EOI). This time is the time tag for the first interval to be
        //returned to the Central Computer.

        t.lastIntlTime  = TypeDateTimeRcd.parse( assembly ).toDate();

        t.parseProfileData(assembly, readProfileDataBeforeConfigChange, max.getBeginningOfRecording());

        if( includeEvents ) {
            Iterator i = assembly.getMaxSys().getTable4().getMeterEvents().iterator();
            while( i.hasNext() )
                t.profileData.addEvent( (MeterEvent)i.next() );

            t.profileData.applyEvents(t.intervalMinutes);
        }

        return t;

    }

    ProfileData getProfile( ){
        return profileData;
    }

    void parseChannelInfo(Assembly assembly) throws IOException {
        channelInfo = new ArrayList();
        MaxSys max = assembly.getMaxSys();
        Table0 t0 = max.getTable0();
        Table11 t11 = max.getTable11();
        for( int i = 0; i < noOfChnls; i ++ ) {
            int umi = assembly.wordValue();
            UnitOfMeasureCode code = UnitOfMeasureCode.get(umi);
            int id = i;
            String name = ( code != null ) ? code.getDescription() : "unknown";
            Unit unit = ( code != null ) ? code.getUnit() : Unit.getUndefined();
            ChannelInfo ci = null;

            //Ke_PULSE_VALUE 9 THRU 16 This value is used as a divisor to
            //convert a double precision floating
            //point number representing KWH into a
            //binary number analogous to pulse
            //counts.

            if (unit.getDlmsCode() != BaseUnit.VOLTSQUARE)
            	ci = new ChannelInfo(id, name, unit, 0, id,
            			new BigDecimal(t11.getTypeStoreCntrlRcd().getChnlCntrl(i).kePulseValue) );
            else
            	ci = new ChannelInfo(id, name, unit);
            channelInfo.add( ci );
        }

        // KV_CHANGED 20032007
        // That is what the doc tells: UNIT_OF_MEAS : ARRAY[1..MAX_STORAGE_CHNNLS] OF WORD;
        // So, skip the remaining (unused channels)
        for( int i = 0; i < (t0.getTypeMaximumValues().getMaxStorageChnnls()-noOfChnls); i ++ )
            assembly.wordValue();
    }


    void parseProfileData(Assembly assembly, boolean readProfileDataBeforeConfigChange, Date clipDate) throws IOException{
        profileData = new ProfileData();
        Iterator ci = channelInfo.iterator();
        while( ci.hasNext() ){
            profileData.addChannel( (ChannelInfo)ci.next());
        }
        profileData.setChannelInfos( channelInfo );

        Calendar iCalendar = ProtocolUtils.getCleanCalendar(timeZone);
        iCalendar.setTime(lastIntlTime);
        BitAssembly ba = new BitAssembly( assembly.getBytes( assembly.elementsLeft() ) );

        int intervalRecordSize = noOfChnls * dataSize;

        for( int i = 0; i < nrOfIntervals && ba.bitsLeft() > intervalRecordSize ; i ++ ) {
            IntervalData id = new IntervalData(iCalendar.getTime());
            // KV_CHANGED, end of interval!
            iCalendar.add( Calendar.MINUTE, (-1*intervalMinutes) );
            parseInterval(ba, id);

            if (readProfileDataBeforeConfigChange)
            	profileData.addInterval(id);
            else {
            	if (id.getEndTime().after(clipDate))
            		profileData.addInterval(id);
            }


            //profileData.addInterval(id);
        }
        profileData.sort();

    }

    void parseInterval(BitAssembly bas, IntervalData id) throws IOException{
        Stack stack = new Stack();
        for( int i = 0; i < noOfChnls; i ++ ){
            int value = popIntValue(bas);
            stack.push(new Integer( value ));
        }
        while( !stack.isEmpty() )
            id.addValue( (Number)stack.pop() );
    }

    /** support 3 distinct formats 8, 12, 15 and 16 bits */
    int popIntValue(BitAssembly bas) throws IOException {
        if( dataSize == 8 ){
            int b1 = bas.nibbleIntValue();
            int b2 = bas.nibbleIntValue();
            return b1 * 16 + b2;
        }
        if( dataSize == 12 ){
            int b1 = bas.nibbleIntValue();
            int b2 = bas.nibbleIntValue();
            int b3 = bas.nibbleIntValue();
            return b1 * 256 + b2 * 16 + b3;
        }
        if( dataSize == 15 || dataSize == 16 ){
            int b1 = bas.nibbleIntValue();
            int b2 = bas.nibbleIntValue();
            int b3 = bas.nibbleIntValue();
            int b4 = bas.nibbleIntValue();
            return b1 * 4096 + b2 * 256 + b3 * 16 + b4;
        }
        throw new IOException( "datasize of the profile intervals is unknown" );
    }

}
