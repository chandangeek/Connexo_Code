package com.energyict.protocolimplv2.dlms.actaris.sl7000.readers.loadprofile;

import com.energyict.dlms.DataContainer;
import com.energyict.dlms.DataStructure;
import com.energyict.dlms.cosem.CapturedObject;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.mdc.upl.ProtocolException;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfileConfiguration;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.IntervalStateBits;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocolimpl.utils.ProtocolUtils;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.common.obis.readers.loadprofile.data.BufferParser;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


/**
 * Obviously this class has many issues, many also highlighted by the IDE but since this is part of a migration from v1 to v2/3 I will leave it as close as possible to v1 version...I have no specs and time to rewrite it completely
 */
public class ActarisSl7000BufferParser implements BufferParser {

    private static final int EV_DST = 0x08;
    private static final int EV_POWER_FAILURE = 0x40;

    private final AbstractDlmsProtocol meterProtocol;

    public ActarisSl7000BufferParser(AbstractDlmsProtocol meterProtocol) {
        this.meterProtocol = meterProtocol;
    }

    @Override
    public List<IntervalData> parse(DataContainer dataContainer, CollectedLoadProfileConfiguration lpc, LoadProfileReader lpr) throws IOException {
        final List<IntervalData> values = new ArrayList<>();
        boolean currentAdd = true;
        boolean previousAdd = true;
        IntervalData previousIntervalData = null, currentIntervalData;

        if (dataContainer.getRoot().element.length == 0) {
            throw new ProtocolException("No entries in object list.");
        }

        Calendar calendar = ProtocolUtils.initCalendar(false, meterProtocol.getTimeZone());

        for (int i = 0; i < dataContainer.getRoot().element.length; i++) { // for all retrieved intervals
            DataStructure currentStructure = dataContainer.getRoot().getStructure(i);
            if (currentStructure.isStructure(0)) {
                calendar = parseProfileStartDateTime(currentStructure, calendar, lpc.getProfileInterval()); // new date and/or time?
            }
            int intervalStatus = 0;
            // Start of interval
            if (currentStructure.isStructure(0)) {
                currentAdd = parseStart(currentStructure, calendar, lpc.getProfileInterval());
                intervalStatus = getEiServerStatus(currentStructure.getStructure(0).getInteger(1));
            }
            // End of interval
            if (currentStructure.isStructure(1)) {
                currentAdd = parseEnd(currentStructure, calendar, lpc.getProfileInterval());
                intervalStatus = getEiServerStatus(currentStructure.getStructure(1).getInteger(1));
            }
            // time1
            if (currentStructure.isStructure(2)) {
                currentAdd = parseTime1(currentStructure, calendar, lpc.getProfileInterval());
                intervalStatus = getEiServerStatus(currentStructure.getStructure(2).getInteger(1));
            }
            // Time2
            if (currentStructure.isStructure(3)) {
                currentAdd = parseTime2(currentStructure, calendar, lpc.getProfileInterval());
                intervalStatus = getEiServerStatus(currentStructure.getStructure(3).getInteger(1));
            }

            // Adjust calendar for interval with profile interval period
            if (currentAdd) {
                calendar.add(Calendar.MINUTE, (lpc.getProfileInterval() / 60));
            }
            currentIntervalData = getIntervalData(currentStructure, calendar, lpr);
            if (currentAdd & !previousAdd) {
                currentIntervalData = addIntervalData(currentIntervalData, previousIntervalData);
            }
            currentIntervalData.setEiStatus(intervalStatus);
            // Add interval data...
            if (currentAdd) {
                values.add(currentIntervalData);
            }
            previousIntervalData = currentIntervalData;
            previousAdd = currentAdd;
        }
        return values;
    }


    private Calendar parseProfileStartDateTime(DataStructure dataStructure, Calendar calendar, int profileInterval) {
        if (isNewDate(dataStructure.getStructure(0).getOctetString(0).getArray()) || isNewTime(dataStructure.getStructure(0).getOctetString(0).getArray())) {
            calendar = setCalendar(calendar, dataStructure.getStructure(0), 0, profileInterval);
        }
        return calendar;
    }

    private boolean isNewDate(byte[] array) {
        return (array[0] != -1) && (array[1] != -1) && (array[2] != -1) && (array[3] != -1);
    }

    private boolean isNewTime(byte[] array) {
        return (array[5] != -1) && (array[6] != -1) && (array[7] != -1);
    }

    private Calendar setCalendar(Calendar cal, DataStructure dataStructure, int btype, int profileInterval) {
        Calendar calendar = (Calendar) cal.clone();
        if (dataStructure.getOctetString(0).getArray()[0] != -1) {
            calendar.set(Calendar.YEAR, (((int) dataStructure.getOctetString(0).getArray()[0] & 0xff) << 8) |
                    (((int) dataStructure.getOctetString(0).getArray()[1] & 0xff)));
        }

        if (dataStructure.getOctetString(0).getArray()[2] != -1) {
            calendar.set(Calendar.MONTH, ((int) dataStructure.getOctetString(0).getArray()[2] & 0xff) - 1);
        }

        if (dataStructure.getOctetString(0).getArray()[3] != -1) {
            calendar.set(Calendar.DAY_OF_MONTH, ((int) dataStructure.getOctetString(0).getArray()[3] & 0xff));
        }

        if (dataStructure.getOctetString(0).getArray()[5] != -1) {
            calendar.set(Calendar.HOUR_OF_DAY, ((int) dataStructure.getOctetString(0).getArray()[5] & 0xff));
        } else {
            calendar.set(Calendar.HOUR_OF_DAY, 0);
        }

        if (btype == 0) {
            if (dataStructure.getOctetString(0).getArray()[6] != -1) {
                calendar.set(Calendar.MINUTE, (((int) dataStructure.getOctetString(0).getArray()[6] & 0xff) / (profileInterval / 60)) * (profileInterval / 60));
            } else {
                calendar.set(Calendar.MINUTE, 0);
            }

            calendar.set(Calendar.SECOND, 0);
        } else {
            if (dataStructure.getOctetString(0).getArray()[6] != -1) {
                calendar.set(Calendar.MINUTE, ((int) dataStructure.getOctetString(0).getArray()[6] & 0xff));
            } else {
                calendar.set(Calendar.MINUTE, 0);
            }

            if (dataStructure.getOctetString(0).getArray()[7] != -1) {
                calendar.set(Calendar.SECOND, ((int) dataStructure.getOctetString(0).getArray()[7] & 0xff));
            } else {
                calendar.set(Calendar.SECOND, 0);
            }
        }

        // if DSA, add 1 hour
        if (dataStructure.getOctetString(0).getArray()[11] != -1) {
            if ((dataStructure.getOctetString(0).getArray()[11] & (byte) 0x80) == 0x80) {
                calendar.add(Calendar.HOUR_OF_DAY, -1);
            }
        }
        return calendar;
    }

    protected int getEiServerStatus(int protocolStatus) {
        int status = IntervalStateBits.OK;
        BigInteger protocolStatusBig = BigInteger.valueOf(protocolStatus);

        if (protocolStatusBig.testBit(6)) { // Power failure
            status = status | IntervalStateBits.POWERDOWN;
            status = status | IntervalStateBits.POWERUP;
        }
        if (protocolStatusBig.testBit(5)) { // Clock Settings
            status = status | IntervalStateBits.SHORTLONG;
        }
        if (protocolStatusBig.testBit(3)) { // DST
            status = status | IntervalStateBits.OTHER;
        }
        if (protocolStatusBig.testBit(2)) {
            status = status | IntervalStateBits.WATCHDOGRESET;
        }

        return status;
    }

    private IntervalData addIntervalData(IntervalData currentIntervalData, IntervalData previousIntervalData) {
        int currentCount = currentIntervalData.getValueCount();
        IntervalData intervalData = new IntervalData(currentIntervalData.getEndTime());
        int current;
        for (int i = 0; i < currentCount; i++) {
            current = (currentIntervalData.get(i)).intValue() + (previousIntervalData.get(i)).intValue();
            intervalData.addValue(current);
        }
        return intervalData;
    }

    private IntervalData getIntervalData(DataStructure dataStructure, Calendar calendar, LoadProfileReader lpr) throws IOException {
        // Add interval data...
        calendar.set(Calendar.MILLISECOND, 0); // fix to reset milliseconds to 0 to prevent errors when storing
        IntervalData intervalData = new IntervalData(((Calendar) calendar.clone()).getTime());
        List<CapturedObject> captureObjects = meterProtocol.getDlmsSession().getCosemObjectFactory().getLoadProfile().getProfileGeneric().getCaptureObjects();
        for (int i = 0; i < captureObjects.size(); i++) {
            if ((captureObjects.get(i).getClassId() == DLMSClassId.REGISTER.getClassId() ||
                    captureObjects.get(i).getClassId() == DLMSClassId.EXTENDED_REGISTER.getClassId() ||
                    captureObjects.get(i).getClassId() == DLMSClassId.DEMAND_REGISTER.getClassId()) && isChannelInLpr(lpr, captureObjects.get(i).getObisCode())) {
                intervalData.addValue(dataStructure.getInteger(i));
            }
        }
        return intervalData;
    }

    private boolean isChannelInLpr(LoadProfileReader lpr, ObisCode obisCode) {
        for (ChannelInfo each : lpr.getChannelInfos()) {
            if (each.getChannelObisCode().equals(obisCode)) {
                return true;
            }
        }
        return false;
    }

    private boolean parseStart(DataStructure dataStructure, Calendar calendar, int profileInterval) {
        // this method added some events but this is no longer possible on v2 model and also in v1 adapters were actually losing those events...
        // keeping the method only for code structure so it can be compared easier with v1, since this code is part of a migration not rewriting
        return true;
    }

    private boolean parseEnd(DataStructure dataStructure, Calendar calendar,  int profileInterval) {
        // this method added some events but this is no longer possible on v2 model and also in v1 adapters were actually losing those events...
        // keeping the method only for code structure so it can be compared easier with v1, since this code is part of a migration not rewriting

        Calendar endIntervalCal = setCalendar(calendar, dataStructure.getStructure(1), 1, profileInterval);
        final int endOfIntervalDate = dataStructure.getStructure(1).getInteger(1);
        if ((dataStructure.getStructure(1).getInteger(1) & EV_POWER_FAILURE) != 0) { // power down
            return true; // KV 16012004
        }
        /* No WD event added cause time is set to 00h00'00" */
        if ((dataStructure.getStructure(1).getInteger(1) & EV_DST) != 0) { // power down
            return true;
        }

        return (profileInterval * 1000) - (endIntervalCal.getTimeInMillis() - calendar.getTimeInMillis()) <= 2000;
    }

    private boolean parseTime1(DataStructure dataStructure, Calendar calendar, int profileInterval) {
        // this method added some events but this is no longer possible on v2 model and also in v1 adapters were actually losing those events...
        // keeping the method only for code structure so it can be compared easier with v1, since this code is part of a migration not rewriting
        return true;
    }

    private boolean parseTime2(DataStructure dataStructure, Calendar calendar, int profileInterval) {
        // this method added some events but this is no longer possible on v2 model and also in v1 adapters were actually losing those events...
        // keeping the method only for code structure so it can be compared easier with v1, since this code is part of a migration not rewriting
        return true;
    }

}
