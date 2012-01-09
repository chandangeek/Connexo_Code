/**
 *
 */
package com.elster.protocolimpl.dlms.profile;

import com.elster.dlms.cosem.simpleobjectmodel.Ek280Defs;
import com.elster.dlms.cosem.simpleobjectmodel.SimpleProfileObject;
import com.elster.dlms.types.basic.*;
import com.elster.protocolimpl.dlms.util.DlmsUtils;
import com.elster.protocolimpl.dlms.util.ProtocolLink;
import com.energyict.cbo.Unit;
import com.energyict.protocol.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

/**
 * @author gh
 * @since 5/26/2010
 */
public class DlmsConsumptionProfile {

    /**
     * The used {@link com.elster.protocolimpl.dsfg.ProtocolLink}
     */
    private final ProtocolLink link;
    /*
     * profile object to get the profile data
     */
    private SimpleProfileObject profile = null;

    /*
    ONO         0.128.96.8.67.255, attributeIndex=2, dataIndex=0}
    TST         0.0.1.0.0.255, attributeIndex=2, dataIndex=0}
    Vcu         7.0.11.2.0.255, attributeIndex=2, dataIndex=0}
    Vmu         7.0.11.0.0.255, attributeIndex=2, dataIndex=0}
    Vcu Del     7.0.11.17.0.255, attributeIndex=2, dataIndex=0}
    Vcu Stat    7.0.11.17.0.255, attributeIndex=4, dataIndex=0}
    Vmu Del     7.0.11.15.0.255, attributeIndex=2, dataIndex=0}
    P           7.0.42.42.255.255, attributeIndex=2, dataIndex=0}
    T           7.0.41.42.255.255, attributeIndex=2, dataIndex=0}
    Stat1       0.2.96.10.1.255, attributeIndex=2, dataIndex=0}
    GNO         7.128.96.5.67.255, attributeIndex=2, dataIndex=0}
    */
    private ObisCode[] capturedObjects = {
            Ek280Defs.VOLUME_AT_BASE_CONDITIONS,           // Vcu
            Ek280Defs.VOLUME_AT_MEASUREMENT_CONDITIONS,    // Vmu
            Ek280Defs.VB_WITHIN_LAST_MEASURING_PERIOD,     // Delta Vcu
            Ek280Defs.VM_WITHIN_LAST_MEASURING_PERIOD,     //Delta Vmu
            Ek280Defs.MEAN_VALUE_P_OF_LAST_MEAS_PERIOD,    // P
            Ek280Defs.MEAN_VALUE_T_OF_LAST_MEAS_PERIOD     // T
    };

    private int[] capturedOverflow = {
            8
            , 8
            , 0
            , 0
            , 0
            , 0
    };

    /**
     * Default constructor
     *
     * @param link - reference to ProtocolLink
     */
    public DlmsConsumptionProfile(ProtocolLink link) {
        this.link = link;
    }

    /**
     * @return the number of channels
     * @throws java.io.IOException - in case of an io error
     */
    public int getNumberOfChannels() throws IOException {
        return capturedObjects.length;
    }

    /**
     * @return the interval of the Profile
     * @throws java.io.IOException when something happens during the read
     */
    public int getInterval() throws IOException {
        return (int) getProfileObject().getCapturePeriod();
    }

    /**
     * Construct the channelInfos
     *
     * @return a list of {@link com.energyict.protocol.ChannelInfo}s
     * @throws java.io.IOException if an error occurred during the read of the
     *                             {@link com.energyict.protocol.ChannelInfo}s
     */
    @SuppressWarnings({"unused", "deprecation"})
    public List<ChannelInfo> buildChannelInfos() throws IOException {

        String unitString;
        ChannelInfo ci;

        List<ChannelInfo> channelInfos = new ArrayList<ChannelInfo>();

        // create a ChannelInfo object for all captured objects
        for (int i = 0; i < capturedObjects.length; i++) {

            // do we have the wanted object in the profile?
            int index = getProfileObject().indexOfCapturedObject(capturedObjects[i]);
            if (index >= 0) {
                // get the unit of the object
                com.elster.dlms.cosem.classes.class03.Unit u = getProfileObject().getUnit(index);
                unitString = u.getDisplayName();
            } else {
                unitString = "";
            }

            Unit unit = DlmsUtils.getUnitFromString(unitString);
            ci = new ChannelInfo(i, "Channel " + i, unit);

            int cov = capturedOverflow[i];
            if (cov > 0) {
                ci.setCumulative();
                // We also use the deprecated method for 8.3 versions
                int ov = 1;
                for (int j = 0; j < cov; j++) {
                    ov *= 10;
                }
                ci.setCumulativeWrapValue(new BigDecimal(ov));
            }
            channelInfos.add(ci);
            //System.out.println(String.format("%d: %s [%s] %s %g", ci.getChannelId(), ci.getName(), ci.getUnit().toString(), ci.isCumulative(), ci.getCumulativeWrapValue()));
        }

        return channelInfos;
    }

    /**
     * Get interval data within the request period
     *
     * @param from - the initial date for the interval data
     * @param to   - the end date for the interval data
     * @return the requested interval data
     * @throws java.io.IOException when reading of the data failed
     */
    @SuppressWarnings({"unused"})
    public List<IntervalData> getIntervalData(Date from, Date to)
            throws IOException {

        List<IntervalData> intervalList = new ArrayList<IntervalData>();

        System.out.println(String.format("getIntervalData: try to get data from %s to %s", from.toString(), to.toString()));

        long readLines = getProfileObject().readProfileData(from, to);
        link.getLogger().info("getIntervalData: " + readLines + " lines read.");
        if (readLines == 0) {
            System.out.println("getIntervalData: no data to readout");
            return intervalList;
        }

        int[] indexes = getIndexArray();

        int tstIndex = getProfileObject().indexOfCapturedObject(Ek280Defs.CLOCK_OBJECT);
        int stateIndex = getProfileObject().indexOfCapturedObject(Ek280Defs.STATUS_REGISTER_1_2);
        int tariffIndex = getProfileObject().indexOfCapturedObject(Ek280Defs.VB_WITHIN_LAST_MEASURING_PERIOD, 4);

        System.out.println("getIntervalData: lines to parse: " + getProfileObject().getRowCount());

        Object v;
        List<IntervalValue> intervalValues;
        IntervalData id;

        /* for every line we have... */
        for (int i = 0; i < getProfileObject().getRowCount(); i++) {

            /* get time stamp */
            DlmsDateTime tst = (DlmsDateTime) profile.getValue(i, tstIndex);

            /* determine status */
            int eiStatus = 0;
            if ((tst.getClockStatus() & 0xF) > 0) {
                //eiStatus |= IntervalStateBits.BADTIME;
            }
            if (stateIndex >= 0) {
                BitString state = getAsBitString(profile.getValue(i, stateIndex));
                if (state != null) {
                    if (state.isBitSet(0) || state.isBitSet(1)) {
                        eiStatus |= IntervalStateBits.DEVICE_ERROR;
                    }
                    if (state.isBitSet(2)) {
                        eiStatus |= IntervalStateBits.BADTIME;
                    }

                }
            }

            /* build list of values of line */
            intervalValues = new ArrayList<IntervalValue>();
            for (int index: indexes) {
                if (index >= 0) {
                    v = profile.getValue(i, index);
                    if (v instanceof Number) {
                        intervalValues.add(new IntervalValue((Number) v, 0, eiStatus));
                    }
                } else {
                    intervalValues.add(new IntervalValue(null, 0, 0));
                }
            }

            /* determine tariff code */
            int tariffCode = 0;
            if (tariffIndex >= 0) {
                BitString state = getAsBitString(profile.getValue(i, tariffIndex));
                if (state.isBitSet(13)) {
                    tariffCode = 1;
                }
                if (state.isBitSet(14)) {
                    tariffCode += 2;
                }
            }

            id = new IntervalData(tst.getUtcDate(), eiStatus, 0, tariffCode, intervalValues);
            intervalList.add(id);
            //System.out.println("(" + Integer.toHexString(tst.getClockStatus()) + ")  " + id);
        }

        link.getLogger().info("getIntervalData: generated interval list with " + intervalList.size() + " of IntervalData.");
        return intervalList;
    }

    /**
     * returns an object to get profile data.
     * Iff it still doesn't exist, it will be created
     *
     * @return a profile object
     * @throws IOException - in case of error
     */
    private SimpleProfileObject getProfileObject() throws IOException {
        if (profile == null) {
            profile = (SimpleProfileObject) link.getObjectManager().getSimpleCosemObject(Ek280Defs.LOAD_PROFILE_60);
        }
        return profile;
    }

    private int[] getIndexArray() throws IOException {

        int[] result = new int[capturedObjects.length];

        /* for all >possible< captured objects */
        for (int i = 0; i < capturedObjects.length; i++) {
            result[i] = getProfileObject().indexOfCapturedObject(capturedObjects[i]);
        }
        return result;
    }

    private BitString getAsBitString(Object value) {
        if (value instanceof BitString) {
            return (BitString)value;
        }
        return null;
    }

}
