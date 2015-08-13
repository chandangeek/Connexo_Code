package com.elster.protocolimpl.lis100.profile;

import com.energyict.protocol.IntervalData;
import com.energyict.protocol.IntervalStateBits;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;

/**
 * class to hold the read out raw data from a lis100 device
 *
 * @author heuckeg
 */
@SuppressWarnings({"unused"})
public class IntervalDataMap {

    private final int noOfChannel;
    private TreeMap<Long, ArrayList<DataElement>> data = null;

    public IntervalDataMap(int noOfChannel) {
        this.noOfChannel = noOfChannel;
        data = new TreeMap<Long, ArrayList<DataElement>>();
    }

    /**
     * Adds a read out data element to the data.<BR>
     * Data elements with the same date are grouped together (in a "line").
     *
     * @param de    - the DateElement to add
     * @param index - position in the "line"
     */
    public void addElement(DataElement de, int index) {

        Long date = de.getDateLong();
        if (date == 0) {
            return;
        }

        ArrayList<DataElement> line;

        if (data.containsKey(date)) {
            line = data.get(date);
        } else {
            line = new ArrayList<DataElement>();
            data.put(date, line);
        }

        while (line.size() <= index) {
            line.add(null);
        }
        line.set(index, de);
    }


    public Set<Long> keySet() {
        return data.keySet();
    }

    /**
     * build a string of the complete content of the map...
     */
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();

        for (Entry<Long, ArrayList<DataElement>> entry : data.entrySet()) {
            s.append("[");
            s.append(Long.toHexString(entry.getKey()));
            s.append("<");
            s.append(getDataElementListAsString(entry.getValue()));
            s.append(">]\n\r");
        }

        return s.toString();
    }

    /**
     * build a string of the data elements...
     *
     * @param value - array list of data elements of a line
     * @return string of data
     */
    private String getDataElementListAsString(ArrayList<DataElement> value) {
        String s = "";

        for (DataElement de : value) {
            if (s.length() > 0)
                s = s + ";";
            if (de != null) {
                s = s + de.toString(",");
            } else {
                s = s + "-";
            }
        }
        return s;
    }

    /**
     * makes an array of IntervalData from the read out "data"
     *
     * @param timeZone - timezone of device
     * @return array of IntervalData
     */
    public List<IntervalData> buildIntervalData(TimeZone timeZone) {

        IntervalData id;
        Object o;
        DataElement de;

        int state = IntervalStateBits.OK;

        List<IntervalData> iList = new ArrayList<IntervalData>();

        /* for every entry in the map (remember: it's sorted!) ... */
        for (Entry<Long, ArrayList<DataElement>> entry : data.entrySet()) {

            /* create an entry */
            /* but: calc local time to UTC */
            /* 07/12/2010 gh */
            id = new IntervalData(new Date(entry.getKey()));

            /* and for every data element in the line... */
            ArrayList<DataElement> des = entry.getValue();
            for (int i = 0; i < noOfChannel; i++) {
                if ((i < des.size()) && (des.get(i) != null)) {
                    de = des.get(i);
                    state = IntervalStateBits.OK;
                    if ((de.getState() != null) && (de.getState() != 0)) {
                        state = IntervalStateBits.CORRUPTED;
                    }

                    o = de.getValue();
                    if (o instanceof Number) {
                        id.addValue((Number)o, 0, state);
                    } else {
                        id.addValue(0.0, 0, IntervalData.MISSING);
                    }
                } else {
                    id.addValue(0.0, 0, IntervalData.MISSING);
                }
            }

            id.addEiStatus(state);
            iList.add(id);
        }

        return iList;
    }

    /**
     * clear all data
     */
    public void clear() {
        data.clear();
    }

    public void addAll(ArrayList<DataElement> processedData, int index) {
        for (DataElement e : processedData) {
            addElement(e, index);
        }
    }
}
