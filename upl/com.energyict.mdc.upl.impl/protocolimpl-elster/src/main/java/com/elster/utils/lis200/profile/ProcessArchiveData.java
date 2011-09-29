package com.elster.utils.lis200.profile;

import com.elster.utils.lis200.events.EventInterpreter;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.MeterEvent;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Class to process lis200 and dsfg archive data.
 * <p/>
 * User: heuckeg
 * Date: 09.07.2010
 * Time: 09:53:14
 */
public class ProcessArchiveData {

    private TimeZone timeZone;
    private IArchiveRawData archiveData;
    private List<IArchiveLineData> specialEvents = new ArrayList<IArchiveLineData>();

    /**
     * Constructor of general profile processor
     *
     * @param archiveData - class containing the archive raw data
     * @param timeZone    - time zone of device to process
     */
    public ProcessArchiveData(IArchiveRawData archiveData, TimeZone timeZone) {
        this.archiveData = archiveData;
        this.timeZone = timeZone;
    }

    /**
     * This method processes the archive data
     *
     * @return list of IntervalData records
     */
    public List<IntervalData> processArchiveData() {

        /* new list as return result */
        List<IntervalData> iList = new ArrayList<IntervalData>();

        /* reset special event list */
        specialEvents = new ArrayList<IArchiveLineData>();

        IntervalData id;
        int event;
        int ieStatus;

        /* start with the first record */
        archiveData.resetRecordPointer();

        /* process lines until we have no further data... */
        do {
            IArchiveLineData line = archiveData.getNextRecord();
            if (line == null) {
                break;
            }

            /* check if entry is a special event */
            if (archiveData.hasEvent()) {
                event = line.getEvent();
                /* if event is not an interval end event... */
                if ((event & 0xFF00) != 0x8100) {
                    /* ...add it to list of specials */
                    specialEvents.add(line);
                    continue;
                }
            }

            /* create new interval data record */
            id = new IntervalData(line.getTimeStampUtc(timeZone));

            /* add interval data */
            for (int j = 0; j < archiveData.getNumberOfChannels(); j++) {
                if (!archiveData.hasValueStatus()) {
                    id.addValue(line.getValue(j));
                } else {
                    int valueStatus = line.getValueState(j);
                    id.addValue(line.getValue(j), valueStatus, 0);
                }
            }

            ieStatus = 0;
            if (archiveData.hasSystemStatus()) {
                ieStatus = line.getLineState();
            }
            if (archiveData.hasInstanceStatus()) {
                ieStatus |= line.getInstanceState();
            }
            id.addEiStatus(ieStatus);

            iList.add(id);

        } while (true);
        return iList;
    }

    /**
     * @return the list of special events detected during processing
     */
    public List<IArchiveLineData> getSpecialEvents() {
        return this.specialEvents;
    }

    /**
     * Builds a list of meter events and delivers it back
     * @param eventInterpreter - interpreter for the special events
     * @return List<MeterEvent>
     */
    public List<MeterEvent> getMeterEvents(EventInterpreter eventInterpreter) {
        String msg;

        ArrayList<MeterEvent> events = new ArrayList<MeterEvent>();

        /* Remark: if we have special events, so we have a event column.... */

        IArchiveLineData line;
        for (int i = 0; i < specialEvents.size(); i++) {
            line = specialEvents.get(i);

            switch (line.getEvent() & 0xFF00) {
                case 0x8200: /* post change */
                    MeterEvent me;

                    /* find previous pre change record */
                    IArchiveLineData iar_pre = null;
                    for (int j = i - 1; j >= 0; j--) {
                        iar_pre = specialEvents.get(j);
                        int evt_pre = iar_pre.getEvent() & 0xFF00;
                        if (evt_pre == 0x8300)
                            break;
                        iar_pre = null;
                        if (evt_pre == 0x8200)
                            break;
                    }

                    /* only if we have both records... */
                    if (iar_pre != null) {
                        /*
                           * change of value or time (if delta of date is greater than
                           * 1s)
                           */
                        DateFormat dfs = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);
                        String valDesc;
                        String oldVal;
                        String newVal;
                        int meId;
                        if (Math.abs(iar_pre.getTimeStampUtc(timeZone).getTime()
                                - line.getTimeStampUtc(timeZone).getTime()) > 1999) {
                            /* change of time */
                            meId = MeterEvent.SETCLOCK;
                            valDesc = "Clock";
                            oldVal = dfs.format(iar_pre.getTimeStampUtc(timeZone));
                            newVal = dfs.format(line.getTimeStampUtc(timeZone));
                        } else {
                            BigDecimal d1 = null;
                            BigDecimal d2 = null;
                            /*
                                * change of value: find out which value has been
                                * changed
                                */
                            int j;
                            for (j = 0; j < archiveData.getNumberOfChannels(); j++) {
                                d1 = iar_pre.getValue(j);
                                d2 = line.getValue(j);
                                if ((d1 == null) || (d2 == null)) {
                                    continue;
                                }
                                if (d1.compareTo(d2) != 0) {
                                    break;
                                }
                            }

                            /* exit if no different values found */
                            if (j >= archiveData.getNumberOfChannels()) {
                                break;
                            }
                            oldVal = "" + d1;
                            newVal = "" + d2;
                            meId = MeterEvent.CONFIGURATIONCHANGE;
                            valDesc = "Value " + j;
                        }
                        msg = String.format("%s changed from %s to %s", valDesc, oldVal, newVal);
                        me = new MeterEvent(line.getTimeStampUtc(timeZone),
                                meId,
                                line.getEvent(),
                                msg);
                        events.add(me);
                    }
                    break;
                case 0x8300: /* pre change */
                    /* ignore */
                    break;
                default:
                    /* add event to meter events */
                    Date date = line.getTimeStampUtc(timeZone);
                    int event = line.getEvent();
                    events.add(eventInterpreter.interpretEvent(date, event));
                    break;
            }

        }
        return events;
    }

}
