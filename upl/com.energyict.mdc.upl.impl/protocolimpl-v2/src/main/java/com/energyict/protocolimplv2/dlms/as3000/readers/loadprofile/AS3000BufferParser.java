package com.energyict.protocolimplv2.dlms.as3000.readers.loadprofile;

import com.energyict.mdc.upl.ProtocolException;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfileConfiguration;

import com.energyict.dlms.DataContainer;
import com.energyict.dlms.DataStructure;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.IntervalValue;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocolimplv2.dlms.common.obis.readers.loadprofile.data.BufferParser;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * This class should be used when you have timestamp (unix), status and value within buffer. No specific status mapping or other stuff.
 */
public class AS3000BufferParser implements BufferParser {
    private Date lastTimeStamp;
    private int loadProfileIntervalInSeconds;

    @Override
    public List<IntervalData> parse(DataContainer buffer, CollectedLoadProfileConfiguration collectedLoadProfileConfiguration, LoadProfileReader lpr) throws ProtocolException {
        List<IntervalData> intervalData = new ArrayList<>();
        Object[] loadProfileEntries;
        loadProfileEntries = buffer.getRoot().getElements();

        loadProfileIntervalInSeconds = collectedLoadProfileConfiguration.getProfileInterval();
        for (int index = 0; index < loadProfileEntries.length; index++) {
            DataStructure structure = buffer.getRoot().getStructure(index);
            readStructure(intervalData, structure);
        }
        return intervalData;
    }

    protected void readStructure(List<IntervalData> intervalData, DataStructure structure) throws ProtocolException {
        try {
            Date timeStamp = getTimeStamp(structure);
            int status = structure.getInteger(1);
            final List<IntervalValue> values = new ArrayList<>();
            for (int channel = 2; channel < structure.getNrOfElements(); channel++) {
                values.add(new IntervalValue(structure.getBigDecimalValue(channel), status, status));
            }
            intervalData.add(new IntervalData(timeStamp, 0, 0, 0, values));
        } catch (ClassCastException e) {
            // Not doing anything while likely this is a missing reading interval (meter not powered up)
            //throw new ProtocolException("Could not parse buffer, expecting different type on channels (0/timestamp or 1/status where we expect OctetString(timestamp in hex) and Integer(status) :" + e.getMessage());
        }
    }

    private Date getTimeStamp(DataStructure structure) {
        // Due to CXO-13002 we need to alter the value, however keeping the change only to seconds and bellow units. If offset is larger then store job will fail when storing the values...
        if (structure.isOctetString(0)) {
            Calendar calendar = structure.getOctetString(0).toCalendar(TimeZone.getDefault());
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            lastTimeStamp = calendar.getTime();
        } else {
            Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
            calendar.setTime(lastTimeStamp);
            calendar.add(Calendar.SECOND, loadProfileIntervalInSeconds);
            lastTimeStamp = calendar.getTime();
        }
        return lastTimeStamp;
    }
}
