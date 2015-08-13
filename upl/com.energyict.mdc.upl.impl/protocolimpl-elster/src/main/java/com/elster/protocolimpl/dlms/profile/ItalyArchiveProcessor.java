package com.elster.protocolimpl.dlms.profile;

import com.elster.dlms.cosem.simpleobjectmodel.Ek280Defs;
import com.elster.dlms.cosem.simpleobjectmodel.SimpleProfileObject;
import com.elster.dlms.types.basic.BitString;
import com.elster.dlms.types.basic.DlmsDateTime;
import com.elster.dlms.types.basic.ObisCode;
import com.elster.protocolimpl.dlms.util.DlmsUtils;
import com.energyict.cbo.Unit;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.IntervalStateBits;
import com.energyict.protocol.IntervalValue;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

/**
 * User: heuckeg
 * Date: 04.09.12
 * Time: 14:49
 */
public class ItalyArchiveProcessor implements IArchiveProcessor
{

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
    private final ObisCode[] capturedObjects = {
            Ek280Defs.VOLUME_AT_BASE_CONDITIONS,           // Vcu
            Ek280Defs.VOLUME_AT_MEASUREMENT_CONDITIONS,    // Vmu
            Ek280Defs.VB_WITHIN_LAST_MEASURING_PERIOD,     // Delta Vcu
            Ek280Defs.VM_WITHIN_LAST_MEASURING_PERIOD,     //Delta Vmu
            Ek280Defs.MEAN_VALUE_P_OF_LAST_MEAS_PERIOD,    // P
            Ek280Defs.MEAN_VALUE_T_OF_LAST_MEAS_PERIOD     // T
    };

    private final int[] capturedOverflow = {
            8
            , 8
            , 0
            , 0
            , 0
            , 0
    };

    private final Logger logger;
    private SimpleProfileObject profile;

    public ItalyArchiveProcessor(final Logger logger)
    {
        this.logger = logger;
    }

    public void prepare(final SimpleProfileObject profileObject, final Object archiveStructure)
    {
        this.profile = profileObject;
    }

    public int getNumberOfChannels()
    {
        return capturedObjects.length;
    }

    public int getInterval()
            throws IOException
    {
        return (int) profile.getCapturePeriod();
    }

    @SuppressWarnings("deprecation")
    public List<ChannelInfo> buildChannelInfo()
            throws IOException
    {
        String unitString;
        ChannelInfo ci;

        List<ChannelInfo> channelInfo = new ArrayList<ChannelInfo>();

        // create a ChannelInfo object for all captured objects
        for (int i = 0; i < capturedObjects.length; i++) {

            // do we have the wanted object in the profile?
            int index = profile.indexOfCapturedObject(capturedObjects[i]);
            if (index >= 0) {
                // get the unit of the object
                com.elster.dlms.cosem.classes.class03.Unit u = profile.getUnit(index);
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
            channelInfo.add(ci);
            //System.out.println(String.format("%d: %s [%s] %s %g", ci.getChannelId(), ci.getName(), ci.getUnit().toString(), ci.isCumulative(), ci.getCumulativeWrapValue()));
        }

        return channelInfo;
    }

    public List<IntervalData> getIntervalData(Date from, Date to)
            throws IOException
    {
        List<IntervalData> intervalList = new ArrayList<IntervalData>();

        System.out.println(String.format("ItalyArchiveProcessor.getIntervalData: try to get data from %s to %s", from.toString(), to.toString()));

        long readLines = profile.readProfileData(from, to);
        logger.info("ItalyArchiveProcessor.getIntervalData: " + readLines + " lines read.");
        if (readLines == 0) {
            logger.info("ItalyArchiveProcessor.getIntervalData: no data to readout");
            return intervalList;
        }

        int[] indexes = getIndexArray();

        int tstIndex = profile.indexOfCapturedObject(Ek280Defs.CLOCK_OBJECT);
        int stateIndex = profile.indexOfCapturedObject(Ek280Defs.STATUS_REGISTER_1_2);
        int tariffIndex = profile.indexOfCapturedObject(Ek280Defs.VB_WITHIN_LAST_MEASURING_PERIOD, 4);

        logger.info("ItalyArchiveProcessor.getIntervalData: lines to parse: " + profile.getRowCount());

        Object v;
        List<IntervalValue> intervalValues;
        IntervalData id;

        /* for every line we have... */
        for (int i = 0; i < profile.getRowCount(); i++) {

            /* get time stamp */
            DlmsDateTime tst = (DlmsDateTime) profile.getValue(i, tstIndex);

            /* determine status */
            int eiStatus = 0;
            //if ((tst.getClockStatus() & 0xF) > 0)
            //{
                //eiStatus |= IntervalStateBits.BADTIME;
            //}
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
                    intervalValues.add(new IntervalValue(BigDecimal.ZERO, 0, IntervalStateBits.MISSING));
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

        logger.info("getIntervalData: generated interval list with " + intervalList.size() + " of IntervalData.");
        return intervalList;
    }

    private int[] getIndexArray() throws IOException {

        int[] result = new int[capturedObjects.length];

        /* for all >possible< captured objects */
        for (int i = 0; i < capturedObjects.length; i++) {
            result[i] = profile.indexOfCapturedObject(capturedObjects[i]);
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
