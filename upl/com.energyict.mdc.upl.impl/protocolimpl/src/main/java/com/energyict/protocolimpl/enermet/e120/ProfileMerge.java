package com.energyict.protocolimpl.enermet.e120;

import com.energyict.cbo.Quantity;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.ProfileData;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

/**
 * Tiny utility class for merging SeriesResponse object into 1 ProfileData.
 */
class ProfileMerge {

    /**
     *
     */
    private final E120 e120;

    /**
     * @param e120
     */
    ProfileMerge(E120 e120) {
        this.e120 = e120;
    }

    TreeMap map = new TreeMap();

    /* The goal is to build a map with date/time as key and IntervalData
     * as value.
     *
     * - Loop over the SeriesResponse entries.
     * - If map does not contain an entry for an interval/time then
     *   create a new IntervalData object.
     * - If an IntervalData object does exist for an interval/time, then
     *   add the value at the end of the IntervalData.
     *   (in other words the column of channel)
     *
     * The sequence of merging determines the order of a SeriesResponse
     * in the ProfileData.
     *
     */
    void merge(SeriesResponse serie) {

        Iterator i = serie.keySet().iterator();
        while(i.hasNext()) {
            Date time = (Date)i.next();
            IntervalData id = null;
            if( map.containsKey(time) )
                id = (IntervalData)map.get(time);
            else {
                id = new IntervalData(time);
                map.put(time, id);
            }
            E120RegisterValue rValue = (E120RegisterValue)serie.get(time);
            Quantity q = rValue.toQuantity();
            int eiStatus = rValue.getProtocolStatus();
            id.addValue(q);
            id.addEiStatus(eiStatus);
        }

    }

    ProfileData toProfileData(boolean includeEvents)
        throws IllegalArgumentException, IOException {

        checkNrChannels();
        checkProfileInterval();

        ProfileData profileData = new ProfileData();

        // Let's make a ChannelInfoList
        List ciList = this.e120.getPChannelMap().toChannelInfoList();
        if( !map.isEmpty() ) {
            Iterator i = ciList.iterator();
            IntervalData id = (IntervalData)map.get(map.firstKey());
            while( i.hasNext() ){
                ChannelInfo ci = (ChannelInfo)i.next();
                ci.setUnit(((Quantity)id.get(0)).getUnit());
            }
        }

        profileData.setChannelInfos(ciList);

        Iterator it = map.keySet().iterator();
        while (it.hasNext()) {
            Date time = (Date) it.next();
            IntervalData intervalData = (IntervalData) map.get(time);
            profileData.addInterval(intervalData);
        }

        if(includeEvents) profileData.generateEvents();
        return profileData;

    }



    /*
     * All interval data objects must have the same nr of channels.
     * Loop over all the entries and compare them.
     */
    private void checkNrChannels( )
        throws IllegalArgumentException, IOException {

        if( map.isEmpty() ) return;

        Iterator i = map.values().iterator();
        IntervalData firstInterval = (IntervalData) i.next();
        int valueCount = firstInterval.getValueCount();

        while (i.hasNext()) {

            IntervalData interval = (IntervalData) i.next();
            if( interval.getValueCount() != valueCount ) {
                throw createException(
                    E120.ERROR_1, new Object [] { firstInterval, interval } );
            }

        }

    }

    /*
     * The interval between all the entries must be exactly the same size.
     * Loop over all the intervals in reverse order and compare the
     * interval.
     */
    private void checkProfileInterval()
        throws IOException {

        if( map.isEmpty() ) return;

        // check nr compare time with profileInterval
        long msProfileInterval = this.e120.getProfileInterval() * 1000;

        List keys = new ArrayList( map.keySet() );

        Iterator i = keys.iterator();
        Date previousTime = (Date)i.next();

        while( i.hasNext() ){
            Date time = (Date)i.next();

            long diff = time.getTime() - previousTime.getTime();
            if( diff != msProfileInterval ) {
                throw createException(
                    E120.ERROR_0, new Object [] {
                                new Integer( this.e120.getProfileInterval() ),
                                new Integer( (int)diff/1000 ) } );

            }

            previousTime = time;

        }
    }

    IllegalArgumentException createException(MessageFormat mf, Object []arg) {
        return new IllegalArgumentException( mf.format(arg) );
    }

}