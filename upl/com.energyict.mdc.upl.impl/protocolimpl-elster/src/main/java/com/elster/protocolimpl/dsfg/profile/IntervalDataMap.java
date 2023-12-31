package com.elster.protocolimpl.dsfg.profile;

import com.elster.protocolimpl.dsfg.telegram.DataElement;
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
 * class to hold the read out raw data from a dsfg archive
 *
 * @author heuckeg
 */
@SuppressWarnings({"unused"})
public class IntervalDataMap {

    private final TreeMap<Long, ArrayList<DataElement>> data;

    public IntervalDataMap() {
        data = new TreeMap<Long, ArrayList<DataElement>>();
    }

    /**
     * Adds a read out data element to the data.<BR>
     * Data elements with the same date are grouped together (in a "line").
     *
     * @param date  - date of DataElement
     * @param index - position in the "line"
     * @param de    - the DateElement to add
     */
    public void addElement(Date date, int index, DataElement de) {
        if (date == null) {
            return;
        }

        ArrayList<DataElement> line = null;

        long d = date.getTime();
        for (Entry<Long, ArrayList<DataElement>> entry : data.entrySet()) {
            if (entry.getKey() == d) {
                line = entry.getValue();
            }
        }

        if (line == null) {
            line = new ArrayList<DataElement>();
            data.put(d, line);
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
            s.append(new Date(entry.getKey()));
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
            if (s.length() > 0) {
                s = s + ";";
            }
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
     * @param timeZone - time zone of device
     * @return array of IntervalData
     */
    public List<IntervalData> buildIntervalData(TimeZone timeZone) {

        IntervalData id;
        Object o;
        int state;

        List<IntervalData> iList = new ArrayList<IntervalData>();

		/* for every entry in the map (remember: it's sorted!) ... */
        for (Entry<Long, ArrayList<DataElement>> entry : data.entrySet()) {

			/* create an entry */
            /* but: calc local time to UTC */
			/* 07/12/2010 gh */
            id = new IntervalData(new Date(entry.getKey()));

			/* and for every data element in the line... */
            for (DataElement de : entry.getValue()) {
                if (de == null) {
                    id.addValue(0.0, 0, IntervalStateBits.MISSING);
                } else {
                    state = IntervalStateBits.OK;
                    if ((de.getState() != null) && (de.getState() != 0)) {
                        state = IntervalStateBits.CORRUPTED;
                    }

                    o = de.getValue();
                    if (o instanceof Double) {
                        id.addValue((Double) o, 0, state);
                    } else if (o instanceof Long) {
                        id.addValue((Long) o, 0, state);
                    } else if (o instanceof Integer) {
                        id.addValue((Integer) o, 0, state);
                    } else {
                        id.addValue(null);
                    }
                }
            }

            //id.addEiStatus(state);
            iList.add(id);
        }

        return iList;
    }
}
