package com.energyict.protocolimplv2.eict.rtu3.beacon3100.profiles;


import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.protocol.IntervalData;
import com.energyict.protocolimplv2.dlms.DLMSProfileIntervals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Logger;


public abstract class AbstractHardcodedProfileParser extends DLMSProfileIntervals {

    private Logger logger = Logger.getLogger(this.getClass().getName());

    public AbstractHardcodedProfileParser(byte[] encodedData) throws IOException {
        super(encodedData, DLMSProfileIntervals.DefaultClockMask, DLMSProfileIntervals.DefaultStatusMask, 0, null);
    }

    public Logger getLogger(){
        return logger;
    }


    @Override
    public List<IntervalData> parseIntervals(int profileInterval, TimeZone timeZone) throws IOException {
        this.profileInterval = profileInterval;
        List<IntervalData> intervalList = new ArrayList<IntervalData>();

        int profileStatus = 0;
        if (getAllDataTypes().size() != 0) {

            for (int i = 0; i < nrOfDataTypes(); i++) {
                IntervalData currentInterval = parseElement((Structure) getDataType(i), timeZone, profileStatus);
                intervalList.add(currentInterval);
            }
        }

        return intervalList;
    }


    /**
     * Parse a load profile entry - by default the first element is date/time and the others are just numerical values.
     *
     * @param element
     * @param timeZone
     * @param profileStatus
     * @return
     * @throws IOException
     */
    protected  IntervalData parseElement(Structure element, TimeZone timeZone, int profileStatus) throws IOException{
        IntervalData currentInterval = null;
        int ch=0;

        // 0 = calendar
        Calendar cal = null;
        cal = constructIntervalCalendar(cal, element.getDataType(ch++), timeZone);
        cal = sanitizeTimeStamp(cal);
        currentInterval = new IntervalData(cal.getTime(), profileStatus);

        // all other channels are considered by default numerical
        while (element.hasMoreElements()){
            currentInterval.addValue(getNumericalValue(element.getNextDataType()));
            Number value = getNumericalValue(element.getNextDataType());
            Number finalValue = postProcess(ch++, value);
            currentInterval.addValue(finalValue);
        }
        return currentInterval;
    }

    /**
     * Allow lower classes to post-process some value - e.g. RSRP, RSRQ adjustint, etc
     *
     * @param channelId channel ID (0 = date/time)
     * @param rawValue  original value (read from Beacon)
     * @return  adjusted value (read to be displayed)
     */
    protected Number postProcess(int channelId, Number rawValue){
        return rawValue;
    }

    /**
     * Reset all seconds and hundred of seconds to 00:00, since this will not be accepted by Connexo storing
     * There are bugs in the Beacons, until they are solved, fix things here as much as we can
     *
     * @param cal - dirty time stamp from Beacon
     * @return - cleaned time stamp
     */
    protected Calendar sanitizeTimeStamp(Calendar cal){
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        return cal;
    }

    /**
     * Extract a numerical value from various data types which support a numerical value
     *
     * @param dataType
     * @return
     */
    protected Number getNumericalValue(AbstractDataType dataType) {
        if (dataType.isUnsigned8()){
            return dataType.getUnsigned8().getValue();
        }
        if (dataType.isUnsigned16()){
            return dataType.getUnsigned16().getValue();
        }
        if (dataType.isUnsigned32()){
            return dataType.getUnsigned32().getValue();
        }
        if (dataType.isInteger8()){
            return dataType.getInteger8().getValue();
        }
        if (dataType.isInteger16()){
            return dataType.getInteger16().getValue();
        }
        if (dataType.isInteger32()){
            return dataType.getInteger32().getValue();
        }
        if (dataType.isInteger64()){
            return dataType.getInteger64().getValue();
        }
        if (dataType.isUnsigned64()){
            return dataType.getUnsigned64().getValue();
        }
        if (dataType.isTypeEnum()){
            return dataType.getTypeEnum().getValue();
        }

        return dataType.longValue();
    }




}
